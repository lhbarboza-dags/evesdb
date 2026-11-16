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
 * The persistent class for the "protectedareas" database table.
 * 
 */
@Entity
@Table(name = "\"protectedareas\"")
@NamedQuery(name = "Protectedarea.findAll", query = "SELECT p FROM Protectedarea p")
public class Protectedarea implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "\"paid\"")
	private int paid;

	@Column(name = "\"area\"")
	private String area;

	@Column(name = "\"established\"")
	private String established;

	@Column(name = "\"location\"")
	private String location;

	@Column(name = "\"name\"")
	private String name;

	@Column(name = "\"isdeleted\"")
	private boolean isdeleted;

	// bi-directional many-to-one association to Location
	@OneToMany(mappedBy = "protectedarea")
	private List<Location> locations;

	public Protectedarea() {
	}

	public int getPaid() {
		return this.paid;
	}

	public void setPaid(int paid) {
		this.paid = paid;
	}

	public String getArea() {
		return this.area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getEstablished() {
		return this.established;
	}

	public void setEstablished(String established) {
		this.established = established;
	}

	public String getLocation() {
		return this.location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Location> getLocations() {
		return this.locations;
	}

	public void setLocations(List<Location> locations) {
		this.locations = locations;
	}

	public Location addLocation(Location location) {
		getLocations().add(location);
		location.setProtectedarea(this);

		return location;
	}

	public Location removeLocation(Location location) {
		getLocations().remove(location);
		location.setProtectedarea(null);

		return location;
	}

	public boolean isIsdeleted() {
		return isdeleted;
	}

	public void setIsdeleted(boolean isdeleted) {
		this.isdeleted = isdeleted;
	}
	
	

}