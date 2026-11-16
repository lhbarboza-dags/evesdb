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
 * The persistent class for the "eservice" database table.
 * 
 */
@Entity
@Table(name = "\"eservice\"")
@NamedQuery(name = "Eservice.findAll", query = "SELECT e FROM Eservice e")
public class Eservice implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "\"eserviceid\"")
	private int eserviceid;

	@Column(name = "\"code\"")
	private String code;

	@Column(name = "\"desc\"")
	private String desc;

	@Column(name = "\"name\"")
	private String name;

	// bi-directional many-to-one association to Esubservice
	@OneToMany(mappedBy = "eservice", fetch = FetchType.EAGER)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Esubservice> esubservices;

	// bi-directional many-to-one association to Service
	@OneToMany(mappedBy = "eservice", fetch = FetchType.LAZY)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Service> services;

	public Eservice() {
	}

	public int getEserviceid() {
		return this.eserviceid;
	}

	public void setEserviceid(int eserviceid) {
		this.eserviceid = eserviceid;
	}

	public String getCode() {
		return this.code;
	}

	public void setCode(String code) {
		this.code = code;
	}

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

	public List<Esubservice> getEsubservices() {
		return this.esubservices;
	}

	public void setEsubservices(List<Esubservice> esubservices) {
		this.esubservices = esubservices;
	}

	public Esubservice addEsubservice(Esubservice esubservice) {
		getEsubservices().add(esubservice);
		esubservice.setEservice(this);

		return esubservice;
	}

	public Esubservice removeEsubservice(Esubservice esubservice) {
		getEsubservices().remove(esubservice);
		esubservice.setEservice(null);

		return esubservice;
	}

	public List<Service> getServices() {
		return this.services;
	}

	public void setServices(List<Service> services) {
		this.services = services;
	}

	public Service addService(Service service) {
		getServices().add(service);
		service.setEservice(this);

		return service;
	}

	public Service removeService(Service service) {
		getServices().remove(service);
		service.setEservice(null);

		return service;
	}

}