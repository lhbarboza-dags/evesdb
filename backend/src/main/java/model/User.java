package model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * The persistent class for the "user" database table.
 * 
 */
@Entity
@Table(name = "\"user\"")
@NamedQuery(name = "User.findAll", query = "SELECT u FROM User u")
public class User implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "\"userid\"")
	private int userid;

	@Column(name = "\"emailadd\"")
	private String emailadd;

	@Column(name = "\"firstname\"")
	private String firstname;

	@Column(name = "\"institution\"")
	private String institution;
	
	@Column(name = "\"sector\"")
	private String sector;
	
	@Column(name = "\"purpose\"")
	private String purpose;

	@Column(name = "\"lastname\"")
	private String lastname;

	@Column(name = "\"password\"")
	private String password;
	
	@Column(name = "\"validation\"")
	private String validation;
	
	@Column(name = "\"inquiry\"")
	private String inquiry;
	
	@Column(name = "\"bugreport\"")
	private String bugreport;
	
	@Column(name = "\"suggestastudy\"")
	private String suggestastudy;
	
	@Column(name = "\"improvement\"")
	private String improvement;

	@Column(name = "\"role\"")
	private int role;

	// bi-directional many-to-one association to Dataset
	@OneToMany(mappedBy = "codedby")
	private List<Studysrc> studySrcs;
//
	// bi-directional many-to-one association to Dataset
	@OneToMany(mappedBy = "reviewedby")
	private List<Studysrc> studySrcs2;

	public User() {
	}

	public int getUserid() {
		return this.userid;
	}

	public void setUserid(int userid) {
		this.userid = userid;
	}

	public String getEmailadd() {
		return this.emailadd;
	}

	public void setEmailadd(String emailadd) {
		this.emailadd = emailadd;
	}

	public String getFirstname() {
		return this.firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getInstitution() {
		return this.institution;
	}

	public void setInstitution(String institution) {
		this.institution = institution;
	}
	
	

	public String getSector() {
		return sector;
	}

	public void setSector(String sector) {
		this.sector = sector;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public String getLastname() {
		return this.lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	
	
	public String getInquiry() {
		return inquiry;
	}

	public void setInquiry(String inquiry) {
		this.inquiry = inquiry;
	}

	public String getBugreport() {
		return bugreport;
	}

	public void setBugreport(String bugreport) {
		this.bugreport = bugreport;
	}

	public String getSuggestastudy() {
		return suggestastudy;
	}

	public void setSuggestastudy(String suggestastudy) {
		this.suggestastudy = suggestastudy;
	}

	public String getImprovement() {
		return improvement;
	}

	public void setImprovement(String improvement) {
		this.improvement = improvement;
	}

	public String getValidation() {
		return validation;
	}

	public void setValidation(String validation) {
		this.validation = validation;
	}

	public int getRole() {
		return this.role;
	}

	public void setRole(int role) {
		this.role = role;
	}

	public List<Studysrc> getStudysrcs1() {
		return this.studySrcs;
	}

	public void setStudysrcs1(List<Studysrc> studysrc) {
		this.studySrcs = studysrc;
	}

	public Studysrc addStudySrc1(Studysrc studySrcs) {
		getStudysrcs1().add(studySrcs);
		studySrcs.setCodedby(this);

		return studySrcs;
	}

	public Studysrc removeDatasets1(Studysrc studySrc) {
		getStudysrcs1().remove(studySrc);
		studySrc.setCodedby(null);

		return studySrc;
	}

	public List<Studysrc> getStudySrc2() {
		return this.studySrcs2;
	}

	public void setDatasets2(List<Studysrc> studySrc2) {
		this.studySrcs2 = studySrc2;
	}

	public Studysrc addStudySrc2(Studysrc studysrc2) {
		getStudySrc2().add(studysrc2);
		studysrc2.setReviewedby(this);

		return studysrc2;
	}

	public Studysrc removeStudySrc2(Studysrc studySrc2) {
		getStudySrc2().remove(studySrc2);
		studySrc2.setReviewedby(null);

		return studySrc2;
	}

}