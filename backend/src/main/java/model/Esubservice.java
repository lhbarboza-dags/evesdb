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
 * The persistent class for the "esubservice" database table.
 * 
 */
@Entity
@Table(name = "\"esubservice\"")
@NamedQuery(name = "Esubservice.findAll", query = "SELECT e FROM Esubservice e")
public class Esubservice implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "\"esubserviceid\"")
	private int esubserviceid;

	@Column(name = "\"code\"")
	private String code;

	@Column(name = "\"desc\"")
	private String desc;

//	@Column(name="\"esserviceid\"")
//	private int eserviceid;

	@Column(name = "\"name\"")
	private String name;

	// bi-directional many-to-one association to Eservice
	@ManyToOne
	@JoinColumns({ @JoinColumn(name = "esserviceid", insertable = true, updatable = true) })
	private Eservice eservice;

	// bi-directional many-to-one association to Service
	@OneToMany(mappedBy = "esubservice", fetch = FetchType.LAZY)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Service> services;

	public Esubservice() {
	}

	public int getEsubserviceid() {
		return this.esubserviceid;
	}

	public void setEsubserviceid(int esubserviceid) {
		this.esubserviceid = esubserviceid;
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

//	public int getEserviceid() {
//		return this.eserviceid;
//	}
//
//	public void setEserviceid(int eserviceid) {
//		this.eserviceid = eserviceid;
//	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Eservice getEservice() {
		return this.eservice;
	}

	public void setEservice(Eservice eservice) {
		this.eservice = eservice;
	}

	public List<Service> getServices() {
		return this.services;
	}

	public void setServices(List<Service> services) {
		this.services = services;
	}

	public Service addService(Service service) {
		getServices().add(service);
		service.setEsubservice(this);

		return service;
	}

	public Service removeService(Service service) {
		getServices().remove(service);
		service.setEsubservice(null);

		return service;
	}

}