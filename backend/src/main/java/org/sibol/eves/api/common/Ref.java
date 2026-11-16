package org.sibol.eves.api.common;

/** Reference to a lookup row (cvterm, realm, biome, country, ...). For writes only {@code id} is read. */
public record Ref(Integer id, String code, String name) {
	public static Ref of(Integer id, String code, String name) {
		return id == null ? null : new Ref(id, code, name);
	}

	public String label() {
		if (code == null || code.isBlank())
			return name;
		return code + " - " + name;
	}
}
