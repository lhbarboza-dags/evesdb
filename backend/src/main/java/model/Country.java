package model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * The persistent class for the "country" database table.
 * 
 */
@Entity
@Table(name = "\"country\"")
@NamedQuery(name = "Country.findAll", query = "SELECT c FROM Country c")
public class Country implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "\"cid\"")
	private int cid;

	@Column(name = "\"ccode\"")
	private String ccode;

//	@Column(name = "\"climateid\"")
//	private int climateid;

	@Column(name = "\"cname\"")
	private String cname;

//	@Column(name = "\"currencyid\"")
//	private int currencyid;

//	@Column(name = "\"latitude\"")
//	private String latitude;
//
//	@Column(name = "\"longitude\"")
//	private String longitude;

//	@Column(name = "\"wregionid\"")
//	private int wregionid;
	
	@Column(name = "\"isdeleted\"")
	private boolean isdeleted;

	// bi-directional many-to-one association to Cvterm
	@ManyToOne
	@JoinColumns({ @JoinColumn(name = "wregionid", insertable = true, updatable = true) })
	private Cvterm wregion;

	// bi-directional many-to-one association to Cvterm
	@ManyToOne
	@JoinColumns({ @JoinColumn(name = "currencyid", insertable = true, updatable = true) })
	private Cvterm currency;

	// bi-directional many-to-one association to Countryinfo
	@OneToMany(mappedBy = "country", fetch = FetchType.EAGER)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Countryinfo> countryinfos;

	public Country() {
	}

	public int getCid() {
		return this.cid;
	}

	public void setCid(int cid) {
		this.cid = cid;
	}

	public String getCcode() {
		return this.ccode;
	}

	public void setCcode(String ccode) {
		this.ccode = ccode;
	}

//	public int getClimateid() {
//		return this.climateid;
//	}
//
//	public void setClimateid(int climateid) {
//		this.climateid = climateid;
//	}

	public String getCname() {
		return this.cname;
	}

	public void setCname(String cname) {
		this.cname = cname;
	}

//	public int getCurrencyid() {
//		return this.currencyid;
//	}
//
//	public void setCurrencyid(int currencyid) {
//		this.currencyid = currencyid;
//	}

//	public String getLatitude() {
//		return this.latitude;
//	}
//
//	public void setLatitude(String latitude) {
//		this.latitude = latitude;
//	}
//
//	public String getLongitude() {
//		return this.longitude;
//	}
//
//	public void setLongitude(String longitude) {
//		this.longitude = longitude;
//	}

//	public int getWregionid() {
//		return this.wregionid;
//	}
//
//	public void setWregionid(int wregionid) {
//		this.wregionid = wregionid;
//	}

	

	public Cvterm getWregion() {
		return wregion;
	}

	public void setWregion(Cvterm wregion) {
		this.wregion = wregion;
	}

	public Cvterm getCurrency() {
		return currency;
	}

	public void setCurrency(Cvterm currency) {
		this.currency = currency;
	}

	public List<Countryinfo> getCountryinfos() {
		return countryinfos;
	}

	public void setCountryinfos(List<Countryinfo> countryinfos) {
		this.countryinfos = countryinfos;
	}

	public boolean isIsdeleted() {
		return isdeleted;
	}

	public void setIsdeleted(boolean isdeleted) {
		this.isdeleted = isdeleted;
	}
	
	

}