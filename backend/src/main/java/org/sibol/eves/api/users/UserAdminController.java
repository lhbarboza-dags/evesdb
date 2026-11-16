package org.sibol.eves.api.users;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sibol.eves.api.common.ApiException;
import org.sibol.eves.data.UserRepo;
import org.sibol.eves.mail.MailService;
import org.sibol.eves.security.CurrentUser;
import org.sibol.eves.security.Role;
import org.sibol.eves.security.Tokens;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import model.User;

/** usermgt.zul / userMgtContent.zul (UserMgtInit: Admin only) → UserMgtController. */
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasAuthority('ADMIN')")
@Transactional
public class UserAdminController {

	public record UserRow(int id, String email, String lastName, String firstName, String institution, int role, String roleName) {}

	public record RoleRequest(List<Integer> ids, int role) {}

	public enum RecipientState { NONE, INVITED, ACCEPTED }

	public record Recipient(String email, Map<MailCategory, RecipientState> categories) {}

	/** Email Config tab: one chosenbox per category with the selected e-mails. */
	public record EmailConfig(Map<MailCategory, List<String>> selected) {}

	private final UserRepo users;
	private final MailService mail;

	public UserAdminController(UserRepo users, MailService mail) {
		this.users = users;
		this.mail = mail;
	}

	/** grid_user; combobox_user role filter (onChange). */
	@GetMapping
	public List<UserRow> list(@RequestParam(required = false) Integer role) {
		List<User> list = role == null ? users.findAll(Sort.by("emailadd")) : users.findByRole(role);
		return list.stream().map(u -> new UserRow(u.getUserid(), u.getEmailadd(), u.getLastname(), u.getFirstname(),
				u.getInstitution(), u.getRole(), Role.of(u.getRole()).label)).toList();
	}

	/** btn_user / btn_curator / btn_Manager / btn_admin → setUserLevel(n). Manager and Admin were dead buttons in ZK. */
	@PostMapping("/role")
	public void setRole(@RequestBody RoleRequest req) {
		if (req.role() < Role.USER.id || req.role() > Role.ADMIN.id)
			throw ApiException.badRequest("Unknown role.");
		int me = CurrentUser.require().getUserId();
		for (Integer id : req.ids()) {
			if (id == me && req.role() != Role.ADMIN.id)
				throw ApiException.badRequest("You cannot remove your own Admin role.");
			User u = users.findById(id).orElseThrow(() -> ApiException.notFound("User not found."));
			u.setRole(req.role());
			users.save(u);
		}
	}

	/** Email Config tab initial state (UserMgtController.doAfterCompose). */
	@GetMapping("/email-config")
	public List<Recipient> emailConfig() {
		List<Recipient> out = new ArrayList<>();
		for (User u : users.findAll(Sort.by("emailadd"))) {
			Map<MailCategory, RecipientState> m = new EnumMap<>(MailCategory.class);
			for (MailCategory c : MailCategory.values())
				m.put(c, state(c.getter.apply(u)));
			out.add(new Recipient(u.getEmailadd(), m));
		}
		return out;
	}

	/**
	 * Email Config buttonSubmit → sendEmail(chosen, purpose) per category: newly selected users get an invitation
	 * token and e-mail; de-selected users are cleared; already invited/accepted users are left as they are.
	 */
	@PutMapping("/email-config")
	public void saveEmailConfig(@RequestBody EmailConfig cfg) {
		for (MailCategory c : MailCategory.values()) {
			Set<String> wanted = new HashSet<>();
			for (String e : cfg.selected().getOrDefault(c, List.of()))
				wanted.add(e.trim().toLowerCase());
			for (User u : users.findAll()) {
				boolean selected = u.getEmailadd() != null && wanted.contains(u.getEmailadd().trim().toLowerCase());
				RecipientState st = state(c.getter.apply(u));
				if (selected && st == RecipientState.NONE) {
					String token = Tokens.newInvitation();
					c.setter.accept(u, token);
					users.save(u);
					mail.send(List.of(u.getEmailadd()), null, "SIBOL-EVES-DB: Verification",
							"Hello,\n The admin selected you as recipient for emails regarding " + c.purpose
									+ " for SIBOL-EVES database. If you wish to accept this invitation, click the link below: \n"
									+ mail.link("/invitation?token=" + token) + "\n\n Kind Regards");
				} else if (!selected && st != RecipientState.NONE) {
					c.setter.accept(u, "");
					users.save(u);
				}
			}
		}
	}

	static RecipientState state(String v) {
		if (v == null || v.isEmpty())
			return RecipientState.NONE;
		return Tokens.ACCEPTED.equals(v) ? RecipientState.ACCEPTED : RecipientState.INVITED;
	}
}
