package model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * The persistent class for the "cv" database table.
 * 
 */
@Entity
@Table(name = "\"cv\"")
@NamedQuery(name = "Cv.findAll", query = "SELECT c FROM Cv c")
public class Cv implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "\"cvid\"")
	private int cvid;

	@Column(name = "\"cvdesc\"")
	private String cvdesc;

	@Column(name = "\"cvname\"")
	private String cvname;

	// bi-directional many-to-one association to Cvterm
	@OneToMany(mappedBy = "cv", fetch = FetchType.LAZY)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Cvterm> cvterms;

	public Cv() {
	}

	public int getCvid() {
		return this.cvid;
	}

	public void setCvid(int cvid) {
		this.cvid = cvid;
	}

	public String getCvdesc() {
		return this.cvdesc;
	}

	public void setCvdesc(String cvdesc) {
		this.cvdesc = cvdesc;
	}

	public String getCvname() {
		return this.cvname;
	}

	public void setCvname(String cvname) {
		this.cvname = cvname;
	}

	public List<Cvterm> getCvterms() {
		return this.cvterms;
	}

	public void setCvterms(List<Cvterm> cvterms) {
		this.cvterms = cvterms;
	}

	public Cvterm addCvterm(Cvterm cvterm) {
		getCvterms().add(cvterm);
		cvterm.setCv(this);

		return cvterm;
	}

	public Cvterm removeCvterm(Cvterm cvterm) {
		getCvterms().remove(cvterm);
		cvterm.setCv(null);

		return cvterm;
	}

}