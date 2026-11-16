package org.sibol.eves.api.users;

import java.util.function.BiConsumer;
import java.util.function.Function;

import model.User;

/**
 * Contact-Us categories (contactUsContent.zul lst_purpose) and the User column that stores the recipient invitation
 * state for each (UserMgtController "Email Config" tab): "" = not a recipient, token = invited, "accepted_invi" = accepted.
 */
public enum MailCategory {
	IMPROVEMENT("Suggest Improvements", "improvement", User::getImprovement, User::setImprovement),
	INQUIRY("Inquiry", "inquiry", User::getInquiry, User::setInquiry),
	BUG_REPORT("Bug Report", "bug report", User::getBugreport, User::setBugreport),
	SUGGEST_A_STUDY("Suggest a study", "suggest a study", User::getSuggestastudy, User::setSuggestastudy);

	public final String label;
	public final String purpose;
	public final Function<User, String> getter;
	public final BiConsumer<User, String> setter;

	MailCategory(String label, String purpose, Function<User, String> getter, BiConsumer<User, String> setter) {
		this.label = label;
		this.purpose = purpose;
		this.getter = getter;
		this.setter = setter;
	}
}
