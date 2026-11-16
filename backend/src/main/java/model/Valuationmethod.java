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
 * The persistent class for the "valuationmethod" database table.
 * 
 */
@Entity
@Table(name = "\"valuationmethod\"")
@NamedQuery(name = "Valuationmethod.findAll", query = "SELECT v FROM Valuationmethod v")
public class Valuationmethod implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "\"vmethodid\"")
	private int vmethodid;

//	@Column(name="\"availstudyinfo\"")
//	private String availstudyinfo;
//
//	@Column(name="\"ecomeasure\"")
//	private String ecomeasure;

//	@Column(name="\"vlmethodid\"")
//	private int methodid;

	@Column(name = "\"surveystudyinfo\"")
	private String surveystudyinfo;

	@Column(name = "\"valuedchange\"")
	private String valuedchange;

	@Column(name = "\"valuedeq\"")
	private String valuedeq;

	// bi-directional many-to-one association to Dataset
	@OneToMany(mappedBy = "valuationmethod")
	private List<Dataset> datasets;

	// bi-directional many-to-one association to Method
	@ManyToOne
	@JoinColumns({ @JoinColumn(name = "vlmethodid", insertable = true, updatable = true) })
	private Cvterm method;

	@ManyToOne
	@JoinColumns({ @JoinColumn(name = "ecomeasureid", insertable = true, updatable = true) })
	private Cvterm ecomeasure;

//	@ManyToOne
//	@JoinColumns({ @JoinColumn(name = "availstudyid", insertable = true, updatable = true) })
//	private Cvterm availstudyinfo;

	// bi-directional many-to-one association to Methdstudyinfo
	@OneToMany(mappedBy = "valuationmethod", fetch = FetchType.EAGER)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Methdstudyinfo> methdstudyinfos;

	public Valuationmethod() {
	}

	public int getVmethodid() {
		return this.vmethodid;
	}

	public void setVmethodid(int vmethodid) {
		this.vmethodid = vmethodid;
	}

//	public String getAvailstudyinfo() {
//		return this.availstudyinfo;
//	}
//
//	public void setAvailstudyinfo(String availstudyinfo) {
//		this.availstudyinfo = availstudyinfo;
//	}
//
//	public String getEcomeasure() {
//		return this.ecomeasure;
//	}
//
//	public void setEcomeasure(String ecomeasure) {
//		this.ecomeasure = ecomeasure;
//	}

//	public int getMethodid() {
//		return this.methodid;
//	}
//
//	public void setMethodid(int methodid) {
//		this.methodid = methodid;
//	}

	public String getSurveystudyinfo() {
		return this.surveystudyinfo;
	}

	public void setSurveystudyinfo(String surveystudyinfo) {
		this.surveystudyinfo = surveystudyinfo;
	}

	public String getValuedchange() {
		return this.valuedchange;
	}

	public void setValuedchange(String valuedchange) {
		this.valuedchange = valuedchange;
	}

	public String getValuedeq() {
		return this.valuedeq;
	}

	public void setValuedeq(String valuedeq) {
		this.valuedeq = valuedeq;
	}

	public List<Dataset> getDatasets() {
		return this.datasets;
	}

	public void setDatasets(List<Dataset> datasets) {
		this.datasets = datasets;
	}

	public Dataset addDataset(Dataset dataset) {
		getDatasets().add(dataset);
		dataset.setValuationmethod(this);

		return dataset;
	}

	public Dataset removeDataset(Dataset dataset) {
		getDatasets().remove(dataset);
		dataset.setValuationmethod(null);

		return dataset;
	}

	public List<Methdstudyinfo> getMethdstudyinfos() {
		return this.methdstudyinfos;
	}

	public void setMethdstudyinfos(List<Methdstudyinfo> methdstudyinfos) {
		this.methdstudyinfos = methdstudyinfos;
	}

	public Methdstudyinfo addMethdstudyinfo(Methdstudyinfo methdstudyinfo) {
		getMethdstudyinfos().add(methdstudyinfo);
		methdstudyinfo.setValuationmethod(this);

		return methdstudyinfo;
	}

	public Methdstudyinfo removeMethdstudyinfo(Methdstudyinfo methdstudyinfo) {
		getMethdstudyinfos().remove(methdstudyinfo);
		methdstudyinfo.setValuationmethod(null);

		return methdstudyinfo;
	}

	public Cvterm getMethod() {
		return this.method;
	}

	public void setMethod(Cvterm method) {
		this.method = method;
	}

	public Cvterm getEcomeasure() {
		return ecomeasure;
	}

	public void setEcomeasure(Cvterm ecomeasure) {
		this.ecomeasure = ecomeasure;
	}

//	public Cvterm getAvailstudyinfo() {
//		return availstudyinfo;
//	}
//
//	public void setAvailstudyinfo(Cvterm availstudyinfo) {
//		this.availstudyinfo = availstudyinfo;
//	}

}