package org.sibol.eves.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Port of org.sibol.eves.app.UserRole (User=0, Curator=1, Manager=2, Admin=3).
 * Authorities are cumulative: an Admin also holds MANAGER, CURATOR and USER.
 */
public enum Role {
	USER(0, "User"), CURATOR(1, "Curator"), MANAGER(2, "Manager"), ADMIN(3, "Admin");

	public final int id;
	public final String label;

	Role(int id, String label) {
		this.id = id;
		this.label = label;
	}

	public static Role of(int id) {
		for (Role r : values())
			if (r.id == id)
				return r;
		return USER;
	}

	public List<GrantedAuthority> authorities() {
		List<GrantedAuthority> list = new ArrayList<>();
		for (Role r : values())
			if (r.id <= id)
				list.add(new SimpleGrantedAuthority(r.name()));
		return list;
	}
}
