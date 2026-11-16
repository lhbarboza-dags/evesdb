package model;

import java.io.Serializable;
import java.math.BigDecimal;
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
 * The persistent class for the "value" database table.
 * 
 */
@Entity
@Table(name = "\"value\"")
@NamedQuery(name = "Value.findAll", query = "SELECT v FROM Value v")
public class Value implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "\"valueid\"")
	private int valueid;

	@Column(name = "\"beneficiarynumber\"")
	private BigDecimal beneficiarynumber;

//	@Column(name = "\"beneficiarytype\"")
//	private String beneficiarytype;

//	@Column(name = "\"beneficiaryunit\"")
//	private String beneficiaryunit;

//	@Column(name = "\"currencyid\"")
//	private int currencyid;

	@Column(name = "\"pvdiscrate\"")
	private String pvdiscrate;

	@Column(name = "\"pvyears\"")
	private String pvyears;

//	@Column(name = "\"unitspatial\"")
//	private String unitspatial;
//
//	@Column(name = "\"unittemporal\"")
//	private String unittemporal;

	@Column(name = "\"vaggregate\"")
	private BigDecimal vaggregate;

	@Column(name = "\"valuept\"")
	private String valuept;

	@Column(name = "\"valuerangehigh\"")
	private BigDecimal valuerangehigh;

	@Column(name = "\"valuerangelow\"")
	private BigDecimal valuerangelow;

//	@Column(name = "\"valuetype\"")
//	private String valuetype;

	@Column(name = "\"year\"")
	private String year;

	// bi-directional many-to-one association to Method
	@ManyToOne
	@JoinColumns({ @JoinColumn(name = "currencyid", insertable = true, updatable = true) })
	private Cvterm value_currency;
		@ManyToOne
	@JoinColumns({ @JoinColumn(name = "unitid", insertable = true, updatable = true) })
	private Cvterm unitid;
	
	@ManyToOne
	@JoinColumns({ @JoinColumn(name = "valuetypeid", insertable = true, updatable = true) })
	private Cvterm valuetype;
	
	@ManyToOne
	@JoinColumns({ @JoinColumn(name = "beneficiaryunitid", insertable = true, updatable = true) })
	private Cvterm beneficiaryunit;
	
	@ManyToOne
	@JoinColumns({ @JoinColumn(name = "beneficiarytypeid", insertable = true, updatable = true) })
	private Cvterm beneficiarytype;

	// bi-directional many-to-one association to Dataset
	@OneToMany(mappedBy = "value")
	private List<Dataset> datasets;
	public Value() {
	}

	public int getValueid() {
		return this.valueid;
	}

	public void setValueid(int valueid) {
		this.valueid = valueid;
	}

	public BigDecimal getBeneficiarynumber() {
		return this.beneficiarynumber;
	}

	public void setBeneficiarynumber(BigDecimal beneficiarynumber) {
		this.beneficiarynumber = beneficiarynumber;
	}

//	public String getBeneficiarytype() {
//		return this.beneficiarytype;
//	}
//
//	public void setBeneficiarytype(String beneficiarytype) {
//		this.beneficiarytype = beneficiarytype;
//	}

//	public String getBeneficiaryunit() {
//		return this.beneficiaryunit;
//	}
//
//	public void setBeneficiaryunit(String beneficiaryunit) {
//		this.beneficiaryunit = beneficiaryunit;
//	}
//
//	public int getCurrencyid() {
//		return this.currencyid;
//	}
//
//	public void setCurrencyid(int currencyid) {
//		this.currencyid = currencyid;
//	}

	public String getPvdiscrate() {
		return this.pvdiscrate;
	}

	public void setPvdiscrate(String pvdiscrate) {
		this.pvdiscrate = pvdiscrate;
	}

	public String getPvyears() {
		return this.pvyears;
	}

	public void setPvyears(String pvyears) {
		this.pvyears = pvyears;
	}

//	public String getUnitspatial() {
//		return this.unitspatial;
//	}
//
//	public void setUnitspatial(String unitspatial) {
//		this.unitspatial = unitspatial;
//	}
//
//	public String getUnittemporal() {
//		return this.unittemporal;
//	}
//
//	public void setUnittemporal(String unittemporal) {
//		this.unittemporal = unittemporal;
//	}

	public BigDecimal getVaggregate() {
		return this.vaggregate;
	}

	public void setVaggregate(BigDecimal vaggregate) {
		this.vaggregate = vaggregate;
	}

	public String getValuept() {
		return this.valuept;
	}

	public void setValuept(String valuept) {
		this.valuept = valuept;
	}

	public BigDecimal getValuerangehigh() {
		return this.valuerangehigh;
	}

	public void setValuerangehigh(BigDecimal valuerangehigh) {
		this.valuerangehigh = valuerangehigh;
	}

	public BigDecimal getValuerangelow() {
		return this.valuerangelow;
	}

	public void setValuerangelow(BigDecimal valuerangelow) {
		this.valuerangelow = valuerangelow;
	}

//	public String getValuetype() {
//		return this.valuetype;
//	}
//
//	public void setValuetype(String valuetype) {
//		this.valuetype = valuetype;
//	}

	public String getYear() {
		return this.year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public List<Dataset> getDatasets() {
		return this.datasets;
	}

	public void setDatasets(List<Dataset> datasets) {
		this.datasets = datasets;
	}

	public Dataset addDataset(Dataset dataset) {
		getDatasets().add(dataset);
		dataset.setValue(this);

		return dataset;
	}

	public Dataset removeDataset(Dataset dataset) {
		getDatasets().remove(dataset);
		dataset.setValue(null);

		return dataset;
	}

	public Cvterm getValue_currency() {
		return value_currency;
	}

	public void setValue_currency(Cvterm value_currency) {
		this.value_currency = value_currency;
	}

	public Cvterm getUnit() {
		return unitid;
	}

	public void setUnit(Cvterm unit) {
		this.unitid = unit;
	}

	

	public Cvterm getValuetype() {
		return valuetype;
	}

	public void setValuetype(Cvterm valuetype) {
		this.valuetype = valuetype;
	}

	public Cvterm getBeneficiaryunit() {
		return beneficiaryunit;
	}

	public void setBeneficiaryunit(Cvterm beneficiaryunit) {
		this.beneficiaryunit = beneficiaryunit;
	}

	public Cvterm getUnitid() {
		return unitid;
	}

	public void setUnitid(Cvterm unitid) {
		this.unitid = unitid;
	}

	public Cvterm getBeneficiarytype() {
		return beneficiarytype;
	}

	public void setBeneficiarytype(Cvterm beneficiarytype) {
		this.beneficiarytype = beneficiarytype;
	}
	
	
	
	

}