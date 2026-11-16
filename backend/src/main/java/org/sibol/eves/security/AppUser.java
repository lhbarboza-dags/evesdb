package org.sibol.eves.security;

import java.io.Serializable;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/** Session principal; replaces org.sibol.org.domain.UserCredential stored under the "userCredential" session key. */
public class AppUser implements UserDetails, Serializable {
	private static final long serialVersionUID = 1L;

	private final int userId;
	private final String email;
	private final String firstName;
	private final String lastName;
	private final Role role;

	public AppUser(int userId, String email, String firstName, String lastName, Role role) {
		this.userId = userId;
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
		this.role = role;
	}

	public int getUserId() { return userId; }
	public String getEmail() { return email; }
	public String getFirstName() { return firstName; }
	public String getLastName() { return lastName; }
	public Role getRole() { return role; }

	@Override public Collection<? extends GrantedAuthority> getAuthorities() { return role.authorities(); }
	@Override public String getPassword() { return null; }
	@Override public String getUsername() { return email; }
	@Override public boolean isAccountNonExpired() { return true; }
	@Override public boolean isAccountNonLocked() { return true; }
	@Override public boolean isCredentialsNonExpired() { return true; }
	@Override public boolean isEnabled() { return true; }
}
