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
 * The persistent class for the "siteattr" database table.
 * 
 */
@Entity
@Table(name="\"siteattr\"")
@NamedQuery(name="Siteattr.findAll", query="SELECT s FROM Siteattr s")
public class Siteattr implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="\"siteattrid\"")
	private int siteattrid;

//	@Column(name="\"cvtattrid\"")
//	private int cvtattrid;

	@Column(name="\"ovalue\"")
	private String ovalue;
	
	@Column(name="\"isdeleted\"")
	private boolean isdeleted;
	

	//bi-directional many-to-one association to Cvterm
	@ManyToOne
	@JoinColumns({ @JoinColumn(name = "\"cvtattrid\"", insertable = true, updatable = true) })
	private Cvterm areaattr;

	//bi-directional many-to-one association to Site
	@ManyToOne
	@JoinColumns({ @JoinColumn(name = "\"siteid\"", insertable = true, updatable = true) })
	private Site site;

	public Siteattr() {
	}

	public int getSiteattrid() {
		return this.siteattrid;
	}

	public void setSiteattrid(int siteattrid) {
		this.siteattrid = siteattrid;
	}

//	public int getCvtattrid() {
//		return this.cvtattrid;
//	}
//
//	public void setCvtattrid(int cvtattrid) {
//		this.cvtattrid = cvtattrid;
//	}

//	public int getSiteid() {
//		return this.siteid;
//	}
//
//	public void setSiteid(int siteid) {
//		this.siteid = siteid;
//	}

	public Cvterm getSiteAttr() {
		return this.areaattr;
	}

	public void setSiteAttr(Cvterm areaattr) {
		this.areaattr = areaattr;
	}

	public Site getSite() {
		return this.site;
	}

	public void setSite(Site site) {
		this.site = site;
	}

	public String getOvalue() {
		return ovalue;
	}

	public void setOvalue(String ovalue) {
		this.ovalue = ovalue;
	}

	public boolean getIsdeleted() {
		return isdeleted;
	}

	public void setIsdeleted(boolean isdeleted) {
		this.isdeleted = isdeleted;
	}

	public Cvterm getAreaattr() {
		return areaattr;
	}

	public void setAreaattr(Cvterm areaattr) {
		this.areaattr = areaattr;
	}

	
		
	

}