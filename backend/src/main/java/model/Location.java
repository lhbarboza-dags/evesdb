package model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * The persistent class for the "location" database table.
 * 
 */
@Entity
@Table(name = "\"location\"")
@NamedQuery(name = "Location.findAll", query = "SELECT l FROM Location l")
public class Location implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "\"locationid\"")
	private int locationid;

//	@Column(name = "\"lcountryinfoid\"")
//	private int countryinfoid;

	@Column(name = "\"latitude\"")
	private String latitude;

	@Column(name = "\"locationcode\"")
	private String locationcode;

	@Column(name = "\"locationname\"")
	private String locationname;

	@Column(name = "\"longitude\"")
	private String longitude;

//	@Column(name = "\"lpaid\"")
//	private int paid;

	// bi-directional many-to-one association to Countryinfo
	@ManyToOne
	@JoinColumns({ @JoinColumn(name = "lcountryinfoid", insertable = true, updatable = true) })
	private Countryinfo countryinfo;

	// bi-directional many-to-one association to Cvterm
	@ManyToOne
	@JoinColumns({ @JoinColumn(name = "\"lprotectedStatustypeId\"", insertable = true, updatable = true) })
	private Cvterm protectedStatus;

	// bi-directional many-to-one association to Protectedarea
	@ManyToOne
	@JoinColumns({ @JoinColumn(name = "lpaid", insertable = true, updatable = true) })
	private Protectedarea protectedarea;

	// bi-directional many-to-one association to Site
	@OneToMany(mappedBy = "location")
	private List<Site> sites;

	public Location() {
	}

	public int getLocationid() {
		return this.locationid;
	}

	public void setLocationid(int locationid) {
		this.locationid = locationid;
	}

//	public int getCountryinfoid() {
//		return this.countryinfoid;
//	}
//
//	public void setCountryinfoid(int countryinfoid) {
//		this.countryinfoid = countryinfoid;
//	}

	public String getLatitude() {
		return this.latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLocationcode() {
		return this.locationcode;
	}

	public void setLocationcode(String locationcode) {
		this.locationcode = locationcode;
	}

	public String getLocationname() {
		return this.locationname;
	}

	public void setLocationname(String locationname) {
		this.locationname = locationname;
	}

	public String getLongitude() {
		return this.longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

//	public int getPaid() {
//		return this.paid;
//	}
//
//	public void setPaid(int paid) {
//		this.paid = paid;
//	}

	public Countryinfo getCountryinfo() {
		return this.countryinfo;
	}

	public void setCountryinfo(Countryinfo countryinfo) {
		this.countryinfo = countryinfo;
	}

	public Protectedarea getProtectedarea() {
		return this.protectedarea;
	}

	public void setProtectedarea(Protectedarea protectedarea) {
		this.protectedarea = protectedarea;
	}

	public List<Site> getSites() {
		return this.sites;
	}

	public void setSites(List<Site> sites) {
		this.sites = sites;
	}

	public Site addSite(Site site) {
		getSites().add(site);
		site.setLocation(this);

		return site;
	}

	public Site removeSite(Site site) {
		getSites().remove(site);
		site.setLocation(null);

		return site;
	}

	public Cvterm getProtectedStatus() {
		return protectedStatus;
	}

	public void setProtectedStatus(Cvterm protectedStatus) {
		this.protectedStatus = protectedStatus;
	}

}