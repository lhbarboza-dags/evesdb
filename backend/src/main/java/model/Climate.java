package model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * The persistent class for the "climate" database table.
 * 
 */
@Entity
@Table(name = "\"climate\"")
@NamedQuery(name = "Climate.findAll", query = "SELECT c FROM Climate c")
public class Climate implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "\"climateid\"")
	private int climateid;

//	@Column(name = "\"climateclassid\"")
//	private int climateclassid;

	@Column(name = "\"climatedesc\"")
	private String climatedesc;
	
	@Column(name = "\"isdeleted\"")
	private boolean isdeleted;

//	@Column(name = "\"climatetypeid\"")
//	private int climatetypeid;

	// bi-directional many-to-one association to Climateclass
	@ManyToOne
	@JoinColumns({ @JoinColumn(name = "climateclassid", insertable = true, updatable = true) })
	private Climateclass climateclass;

	// bi-directional many-to-one association to Climatetype
	@ManyToOne
	@JoinColumns({ @JoinColumn(name = "climatetypeid", insertable = true, updatable = true) })
	private Climatetype climatetype;
	
	@ManyToOne
	@JoinColumns({ @JoinColumn(name = "siteid", insertable = true, updatable = true) })
	private Site site;

	public Climate() {
	}

	public int getClimateid() {
		return this.climateid;
	}

	public void setClimateid(int climateid) {
		this.climateid = climateid;
	}

//	public int getClimateclassid() {
//		return this.climateclassid;
//	}
//
//	public void setClimateclassid(int climateclassid) {
//		this.climateclassid = climateclassid;
//	}

	public String getClimatedesc() {
		return this.climatedesc;
	}

	public void setClimatedesc(String climatedesc) {
		this.climatedesc = climatedesc;
	}

//	public int getClimatetypeid() {
//		return this.climatetypeid;
//	}
//
//	public void setClimatetypeid(int climatetypeid) {
//		this.climatetypeid = climatetypeid;
//	}

	public Climateclass getClimateclass() {
		return this.climateclass;
	}

	public void setClimateclass(Climateclass climateclass) {
		this.climateclass = climateclass;
	}

	public Climatetype getClimatetype() {
		return this.climatetype;
	}

	public void setClimatetype(Climatetype climatetype) {
		this.climatetype = climatetype;
	}

	public Site getSite() {
		return site;
	}

	public void setSite(Site site) {
		this.site = site;
	}

	public boolean isIsdeleted() {
		return isdeleted;
	}

	public void setIsdeleted(boolean isdeleted) {
		this.isdeleted = isdeleted;
	}
	
	
	
	


}