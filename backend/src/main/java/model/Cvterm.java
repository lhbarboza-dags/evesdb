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
 * The persistent class for the "cvterm" database table.
 * 
 */
@Entity
@Table(name = "\"cvterm\"")
@NamedQuery(name = "Cvterm.findAll", query = "SELECT c FROM Cvterm c")
public class Cvterm implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "\"cvtermid\"")
	private int cvtermid;

	@Column(name = "\"code\"")
	private String code;

//	@Column(name = "\"cvtcvid\"")
//	private int cvid;

	@Column(name = "\"desc\"")
	private String desc;

	@Column(name = "\"name\"")
	private String name;

	// bi-directional many-to-one association to Country
//	@OneToMany(mappedBy = "wbgrp")
//	private List<Country> countries1;

	// bi-directional many-to-one association to Country
	@OneToMany(mappedBy = "wregion", fetch = FetchType.LAZY)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Country> countries2;

	// bi-directional many-to-one association to Country
	@OneToMany(mappedBy = "currency", fetch = FetchType.LAZY)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Country> countries3;

	// bi-directional many-to-one association to Cv
	@ManyToOne
	@JoinColumns({ @JoinColumn(name = "cvtcvid", insertable = true, updatable = true) })
	private Cv cv;

	// bi-directional many-to-one association to Location
	@OneToMany(mappedBy = "protectedStatus", fetch = FetchType.LAZY)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Location> locations;

	// bi-directional many-to-one association to Studysrc
	@OneToMany(mappedBy = "doctype", fetch = FetchType.LAZY)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Studysrc> studysrcs;

	@OneToMany(mappedBy = "scale", fetch = FetchType.LAZY)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Site> site1;

	@OneToMany(mappedBy = "areaattr", fetch = FetchType.LAZY)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Siteattr> areaattr;

	@OneToMany(mappedBy = "ecomeasure", fetch = FetchType.LAZY)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Valuationmethod> valMethod1;
	
	@OneToMany(mappedBy = "method", fetch = FetchType.LAZY)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Valuationmethod> valMethod2;

//	@OneToMany(mappedBy = "availstudyinfo", fetch = FetchType.EAGER)
//	@Fetch(value = FetchMode.SUBSELECT)
//	private List<Valuationmethod> valMethod2;
	

	//bi-directional many-to-one association to Methdstudyinfo
	@OneToMany(mappedBy="cvterm" , fetch = FetchType.LAZY)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Methdstudyinfo> methdstudyinfos;

	@OneToMany(mappedBy = "value_currency", fetch = FetchType.LAZY)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Value> value1;

	@OneToMany(mappedBy = "unitid", fetch = FetchType.LAZY)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Value> value2;

	@OneToMany(mappedBy = "valuetype", fetch = FetchType.LAZY)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Value> value4;

	@OneToMany(mappedBy = "beneficiaryunit", fetch = FetchType.LAZY)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Value> value5;
	
	@Column(name = "\"isdeleted\"")
	private boolean isdeleted;

	public Cvterm() {
	}

	public int getCvtermid() {
		return this.cvtermid;
	}

	public void setCvtermid(int cvtermid) {
		this.cvtermid = cvtermid;
	}

	public String getCode() {
		return this.code;
	}

	public void setCode(String code) {
		this.code = code;
	}

//	public int getCvid() {
//		return this.cvid;
//	}
//
//	public void setCvid(int cvid) {
//		this.cvid = cvid;
//	}

	public String getDesc() {
		return this.desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

//	public List<Country> getCountries1() {
//		return this.countries1;
//	}
//
//	public void setCountries1(List<Country> countries1) {
//		this.countries1 = countries1;
//	}
//
//	public Country addCountries1(Country countries1) {
//		getCountries1().add(countries1);
//		countries1.setCvterm1(this);
//
//		return countries1;
//	}
//
//	public Country removeCountries1(Country countries1) {
//		getCountries1().remove(countries1);
//		countries1.setCvterm1(null);
//
//		return countries1;
//	}

	public List<Country> getCountries2() {
		return this.countries2;
	}

	public void setCountries2(List<Country> countries2) {
		this.countries2 = countries2;
	}

	public Country addCountries2(Country countries2) {
		getCountries2().add(countries2);
		countries2.setWregion(this);

		return countries2;
	}

	public Country removeCountries2(Country countries2) {
		getCountries2().remove(countries2);
		countries2.setWregion(null);

		return countries2;
	}

	public List<Country> getCountries3() {
		return this.countries3;
	}

	public void setCountries3(List<Country> countries3) {
		this.countries3 = countries3;
	}

	public Country addCountries3(Country countries3) {
		getCountries3().add(countries3);
		countries3.setCurrency(this);

		return countries3;
	}

	public Country removeCountries3(Country countries3) {
		getCountries3().remove(countries3);
		countries3.setCurrency(null);

		return countries3;
	}

	public Cv getCv() {
		return this.cv;
	}

	public void setCv(Cv cv) {
		this.cv = cv;
	}

	public List<Location> getLocations() {
		return this.locations;
	}

	public void setLocations(List<Location> locations) {
		this.locations = locations;
	}

	public Location addLocation(Location location) {
		getLocations().add(location);
		location.setProtectedStatus(this);

		return location;
	}

	public Location removeLocation(Location location) {
		getLocations().remove(location);
		location.setProtectedStatus(null);

		return location;
	}

	public List<Studysrc> getStudysrcs() {
		return this.studysrcs;
	}

	public void setStudysrcs(List<Studysrc> studysrcs) {
		this.studysrcs = studysrcs;
	}

	public Studysrc addStudysrc(Studysrc studysrc) {
		getStudysrcs().add(studysrc);
		studysrc.setDoctype(this);

		return studysrc;
	}

	public Studysrc removeStudysrc(Studysrc studysrc) {
		getStudysrcs().remove(studysrc);
		studysrc.setDoctype(null);

		return studysrc;
	}

	public List<Site> getSite1() {
		return site1;
	}

	public void setSite1(List<Site> site1) {
		this.site1 = site1;
	}

	public Site addSite(Site site) {
		getSite1().add(site);
		site.setScale(this);

		return site;
	}

	public Site removeSite(Site site) {
		getSite1().remove(site);
		site.setScale(null);

		return site;
	}

	public List<Siteattr> getSiteattrs() {
		return areaattr;
	}

	public void setSiteAttr(List<Siteattr> siteattr) {
		this.areaattr = siteattr;
	}

	public Siteattr addSiteattr(Siteattr siteattr) {
		getSiteattrs().add(siteattr);
		siteattr.setSiteAttr(this);

		return siteattr;
	}

	public Siteattr removeSiteattr(Siteattr siteattr) {
		getSiteattrs().remove(siteattr);
		siteattr.setSiteAttr(null);

		return siteattr;
	}

	public List<Valuationmethod> getValMethod1() {
		return valMethod1;
	}

	public void setValMethod1(List<Valuationmethod> valMethod1) {
		this.valMethod1 = valMethod1;
	}

	public Valuationmethod addValuationmethod1(Valuationmethod valMethd) {
		getValMethod1().add(valMethd);
		valMethd.setEcomeasure(this);

		return valMethd;
	}

	public Valuationmethod removeValuationmethod1(Valuationmethod valMethd) {
		getValMethod1().remove(valMethd);
		valMethd.setEcomeasure(null);
		return valMethd;
	}
	
	public List<Valuationmethod> getValMethod2() {
		return valMethod2;
	}

	public void setValMethod2(List<Valuationmethod> valMethod2) {
		this.valMethod2 = valMethod1;
	}

	public Valuationmethod addValuationmethod2(Valuationmethod valMethd) {
		getValMethod2().add(valMethd);
		valMethd.setMethod(this);

		return valMethd;
	}

	public Valuationmethod removeValuationmethod2(Valuationmethod valMethd) {
		getValMethod2().remove(valMethd);
		valMethd.setMethod(null);
		return valMethd;
	}

//	public List<Valuationmethod> getValMethod2() {
//		return valMethod2;
//	}
//
//	public void setValMethod2(List<Valuationmethod> valMethod2) {
//		this.valMethod2 = valMethod2;
//	}

//	public Valuationmethod addValuationmethod2(Valuationmethod valMethd) {
//		getValMethod2().add(valMethd);
//		valMethd.setEcomeasure(this);
//
//		return valMethd;
//	}
//
//	public Valuationmethod removeValuationmethod2(Valuationmethod valMethd) {
//		getValMethod2().remove(valMethd);
//		valMethd.setEcomeasure(null);
//		return valMethd;
//	}
	
	public List<Methdstudyinfo> getMethdstudyinfos() {
		return this.methdstudyinfos;
	}

	public void setMethdstudyinfos(List<Methdstudyinfo> methdstudyinfos) {
		this.methdstudyinfos = methdstudyinfos;
	}

	public Methdstudyinfo addMethdstudyinfo(Methdstudyinfo methdstudyinfo) {
		getMethdstudyinfos().add(methdstudyinfo);
		methdstudyinfo.setCvterm(this);

		return methdstudyinfo;
	}

	public Methdstudyinfo removeMethdstudyinfo(Methdstudyinfo methdstudyinfo) {
		getMethdstudyinfos().remove(methdstudyinfo);
		methdstudyinfo.setCvterm(null);

		return methdstudyinfo;
	}

	public List<Value> getValue1() {
		return value1;
	}

	public void setValue1(List<Value> value1) {
		this.value1 = value1;
	}

	public Value addValue1(Value value) {
		getValue1().add(value);
		value.setValue_currency(this);

		return value;
	}

	public Value removeValue1(Value value) {
		getValue1().remove(value);
		value.setValue_currency(null);
		return value;
	}

	public List<Value> getValue2() {
		return value2;
	}

	public void setValue2(List<Value> value2) {
		this.value2 = value2;
	}

	public Value addValue2(Value value) {
		getValue2().add(value);
		value.setUnit(this);

		return value;
	}

	public Value removeValue2(Value value) {
		getValue2().remove(value);
		value.setUnit(null);
		return value;
	}

	public List<Value> getValue4() {
		return value4;
	}

	public void setValue4(List<Value> value4) {
		this.value4 = value4;
	}

	public Value addValue4(Value value) {
		getValue4().add(value);
		value.setValuetype(this);

		return value;
	}

	public Value removeValue4(Value value) {
		getValue4().remove(value);
		value.setValuetype(null);
		return value;
	}

	public List<Value> getValue5() {
		return value5;
	}

	public void setValue5(List<Value> value5) {
		this.value5 = value5;
	}

	public Value addValue5(Value value) {
		getValue5().add(value);
		value.setBeneficiaryunit(this);

		return value;
	}

	public Value removeValue5(Value value) {
		getValue5().remove(value);
		value.setBeneficiaryunit(null);
		return value;
	}

	public List<Siteattr> getAreaattr() {
		return areaattr;
	}

	public void setAreaattr(List<Siteattr> areaattr) {
		this.areaattr = areaattr;
	}

	public boolean isIsdeleted() {
		return isdeleted;
	}

	public void setIsdeleted(boolean isdeleted) {
		this.isdeleted = isdeleted;
	}
	
	

}