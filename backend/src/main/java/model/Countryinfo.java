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
 * The persistent class for the "countryinfo" database table.
 * 
 */
@Entity
@Table(name="\"countryinfo\"")
@NamedQuery(name="Countryinfo.findAll", query="SELECT c FROM Countryinfo c")
public class Countryinfo implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="\"ciid\"")
	private int ciid;

//	@Column(name="\"countryid\"")
//	private int countryid;

	@Column(name="\"gdpperincome\"")
	private String gdpperincome;

	@Column(name="\"popdensity\"")
	private String popdensity;

	@Column(name="\"wbincome\"")
	private String wbincome;

	@Column(name="\"year\"")
	private int year;
	
	@Column(name="\"isdeleted\"")
	private boolean isdeleted;

	//bi-directional many-to-one association to Country
	@ManyToOne
	@JoinColumns({
		@JoinColumn(name = "countryid", insertable = true, updatable = true)
		})
	private Country country;

	//bi-directional many-to-one association to Location
	@OneToMany(mappedBy="countryinfo")
	private List<Location> locations;

	public Countryinfo() {
	}

	public int getCiid() {
		return this.ciid;
	}

	public void setCiid(int ciid) {
		this.ciid = ciid;
	}

//	public int getCountryid() {
//		return this.countryid;
//	}
//
//	public void setCountryid(int countryid) {
//		this.countryid = countryid;
//	}

	public String getGdpperincome() {
		return this.gdpperincome;
	}

	public void setGdpperincome(String gdpperincome) {
		this.gdpperincome = gdpperincome;
	}

	public String getPopdensity() {
		return this.popdensity;
	}

	public void setPopdensity(String popdensity) {
		this.popdensity = popdensity;
	}

	public String getWbincome() {
		return this.wbincome;
	}

	public void setWbincome(String wbincome) {
		this.wbincome = wbincome;
	}

	public int getYear() {
		return this.year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public Country getCountry() {
		return this.country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}
	
	

	public boolean isDeleted() {
		return isdeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isdeleted = isDeleted;
	}

	public List<Location> getLocations() {
		return this.locations;
	}

	public void setLocations(List<Location> locations) {
		this.locations = locations;
	}

	public Location addLocation(Location location) {
		getLocations().add(location);
		location.setCountryinfo(this);

		return location;
	}

	public Location removeLocation(Location location) {
		getLocations().remove(location);
		location.setCountryinfo(null);

		return location;
	}

}