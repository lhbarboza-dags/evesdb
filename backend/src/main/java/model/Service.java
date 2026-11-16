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
 * The persistent class for the "service" database table.
 * 
 */
@Entity
@Table(name="\"service\"")
@NamedQuery(name="Service.findAll", query="SELECT s FROM Service s")
public class Service implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="\"serviceid\"")
	private int serviceid;

	@Column(name="\"esdesc\"")
	private String esdesc;

//	@Column(name="\"esid\"")
//	private int esid;

	@Column(name="\"esubsdesc\"")
	private String esubsdesc;

//	@Column(name="\"esubsid\"")
//	private int esubsid;

	//bi-directional many-to-one association to Dataset
	@OneToMany(mappedBy="service")
	private List<Dataset> datasets;

	//bi-directional many-to-one association to Eservice
	@ManyToOne
	@JoinColumns({
		@JoinColumn(name = "esid", insertable = true, updatable = true)
		})
	private Eservice eservice;

	//bi-directional many-to-one association to Esubservice
	@ManyToOne
	@JoinColumns({
		@JoinColumn(name = "esubsid", insertable = true, updatable = true)
		})
	private Esubservice esubservice;

	public Service() {
	}

	public int getServiceid() {
		return this.serviceid;
	}

	public void setServiceid(int serviceid) {
		this.serviceid = serviceid;
	}

	public String getEsdesc() {
		return this.esdesc;
	}

	public void setEsdesc(String esdesc) {
		this.esdesc = esdesc;
	}

//	public int getEsid() {
//		return this.esid;
//	}
//
//	public void setEsid(int esid) {
//		this.esid = esid;
//	}

	public String getEsubsdesc() {
		return this.esubsdesc;
	}

	public void setEsubsdesc(String esubsdesc) {
		this.esubsdesc = esubsdesc;
	}

//	public int getEsubsid() {
//		return this.esubsid;
//	}
//
//	public void setEsubsid(int esubsid) {
//		this.esubsid = esubsid;
//	}

	public List<Dataset> getDatasets() {
		return this.datasets;
	}

	public void setDatasets(List<Dataset> datasets) {
		this.datasets = datasets;
	}

	public Dataset addDataset(Dataset dataset) {
		getDatasets().add(dataset);
		dataset.setService(this);

		return dataset;
	}

	public Dataset removeDataset(Dataset dataset) {
		getDatasets().remove(dataset);
		dataset.setService(null);

		return dataset;
	}

	public Eservice getEservice() {
		return this.eservice;
	}

	public void setEservice(Eservice eservice) {
		this.eservice = eservice;
	}

	public Esubservice getEsubservice() {
		return this.esubservice;
	}

	public void setEsubservice(Esubservice esubservice) {
		this.esubservice = esubservice;
	}

}