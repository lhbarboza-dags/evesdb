package org.sibol.eves.api.study;

/** EvesConstants / SibolEvesVariables status codes. A null status is shown as Pending (ViewStudyRenderer). */
public final class StudyStatus {
	public static final int PENDING = 0;
	public static final int APPROVED = 1;
	public static final int DISAPPROVED = 2;

	private StudyStatus() {}

	public static int of(Integer s) {
		return s == null ? PENDING : s;
	}

	public static String name(Integer s) {
		return switch (of(s)) {
		case APPROVED -> "Approved";
		case DISAPPROVED -> "Disapproved";
		default -> "Pending";
		};
	}
}
