package model;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the methdstudyinfo database table.
 * 
 */
@Entity
@NamedQuery(name="Methdstudyinfo.findAll", query="SELECT m FROM Methdstudyinfo m")
public class Methdstudyinfo implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "\"methdstudyinfoid\"")
	private Integer methdstudyinfoid;

	@Column(name = "\"isdeleted\"")
	private boolean isdeleted;

	
	//bi-directional many-to-one association to Cvterm
	@ManyToOne
	@JoinColumn(name="cvtermid" , insertable = true, updatable = true)
	private Cvterm cvterm;

	//bi-directional many-to-one association to Valuationmethod
	@ManyToOne
	@JoinColumn(name="valuationmthdid" , insertable = true, updatable = true)
	private Valuationmethod valuationmethod;

	public Methdstudyinfo() {
	}

	public Integer getMethdstudyinfoid() {
		return this.methdstudyinfoid;
	}

	public void setMethdstudyinfoid(Integer methdstudyinfoid) {
		this.methdstudyinfoid = methdstudyinfoid;
	}

	public Cvterm getCvterm() {
		return this.cvterm;
	}

	public void setCvterm(Cvterm cvterm) {
		this.cvterm = cvterm;
	}

	public Valuationmethod getValuationmethod() {
		return this.valuationmethod;
	}

	public void setValuationmethod(Valuationmethod valuationmethod) {
		this.valuationmethod = valuationmethod;
	}

	public boolean isIsdeleted() {
		return isdeleted;
	}

	public void setIsdeleted(boolean isdeleted) {
		this.isdeleted = isdeleted;
	}
	
	

}