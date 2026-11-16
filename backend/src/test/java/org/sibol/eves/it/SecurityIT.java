package org.sibol.eves.it;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.sibol.eves.security.AppUser;
import org.sibol.eves.security.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** Page guards of the ZK app (SearchInit, DataMgtInit, UserMgtInit, banner role rules) enforced by the API. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@EnabledIfEnvironmentVariable(named = "SPRING_DATASOURCE_URL", matches = ".+")
class SecurityIT {

	@Autowired MockMvc mvc;

	static Authentication as(Role r) {
		AppUser u = new AppUser(-1, "t@example.org", "T", "U", r);
		return new UsernamePasswordAuthenticationToken(u, null, u.getAuthorities());
	}

	@Test
	void anonymous() throws Exception {
		mvc.perform(get("/api/auth/me")).andExpect(status().isOk());
		mvc.perform(get("/api/public/config")).andExpect(status().isOk());
		mvc.perform(get("/api/search/filters")).andExpect(status().isUnauthorized());
		mvc.perform(post("/api/search").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isUnauthorized());
		mvc.perform(post("/api/search").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isForbidden()); // no CSRF token
	}

	@Test
	void userCanSearchButNotManageData() throws Exception {
		mvc.perform(get("/api/search/filters").with(authentication(as(Role.USER)))).andExpect(status().isOk());
		mvc.perform(get("/api/studies").with(authentication(as(Role.USER)))).andExpect(status().isForbidden());
		mvc.perform(get("/api/dictionary/terms?cv=Currency").with(authentication(as(Role.USER)))).andExpect(status().isForbidden());
	}

	@Test
	void curatorManagesStudiesButCannotApproveOrEditDictionary() throws Exception {
		mvc.perform(get("/api/studies").with(authentication(as(Role.CURATOR)))).andExpect(status().isOk());
		mvc.perform(post("/api/studies/status").with(authentication(as(Role.CURATOR))).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content("{\"ids\":[],\"status\":1}")).andExpect(status().isForbidden());
		mvc.perform(post("/api/dictionary/terms").with(authentication(as(Role.CURATOR))).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content("{\"cv\":\"Currency\",\"code\":\"X\",\"name\":\"X\"}")).andExpect(status().isForbidden());
		mvc.perform(get("/api/users").with(authentication(as(Role.CURATOR)))).andExpect(status().isForbidden());
	}

	@Test
	void managerAndAdmin() throws Exception {
		mvc.perform(post("/api/studies/status").with(authentication(as(Role.MANAGER))).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content("{\"ids\":[],\"status\":1}")).andExpect(status().isOk());
		mvc.perform(get("/api/users").with(authentication(as(Role.MANAGER)))).andExpect(status().isForbidden());
		mvc.perform(get("/api/users").with(authentication(as(Role.ADMIN)))).andExpect(status().isOk());
	}
}
