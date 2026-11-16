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
 * The persistent class for the "climateclass" database table.
 * 
 */
@Entity
@Table(name = "\"climateclass\"")
@NamedQuery(name = "Climateclass.findAll", query = "SELECT c FROM Climateclass c")
public class Climateclass implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "\"ccid\"")
	private int ccid;

	@Column(name = "\"ccCode\"")
	private String ccCode;

	@Column(name = "\"ccDesc\"")
	private String ccDesc;

	@Column(name = "\"ccName\"")
	private String ccName;

//	@Column(name="\"climatetypeid\"")
//	private int climatetypeid;

	// bi-directional many-to-one association to Climate
	@OneToMany(mappedBy = "climateclass", fetch = FetchType.LAZY)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Climate> climates;

	// bi-directional many-to-one association to Climatetype
	@ManyToOne
	@JoinColumns({ @JoinColumn(name = "climatetypeid", insertable = true, updatable = true) })
	private Climatetype climatetype;

	public Climateclass() {
	}

	public int getCcid() {
		return this.ccid;
	}

	public void setCcid(int ccid) {
		this.ccid = ccid;
	}

	public String getCcCode() {
		return this.ccCode;
	}

	public void setCcCode(String ccCode) {
		this.ccCode = ccCode;
	}

	public String getCcDesc() {
		return this.ccDesc;
	}

	public void setCcDesc(String ccDesc) {
		this.ccDesc = ccDesc;
	}

	public String getCcName() {
		return this.ccName;
	}

	public void setCcName(String ccName) {
		this.ccName = ccName;
	}

//	public int getClimatetypeid() {
//		return this.climatetypeid;
//	}
//
//	public void setClimatetypeid(int climatetypeid) {
//		this.climatetypeid = climatetypeid;
//	}

	public List<Climate> getClimates() {
		return this.climates;
	}

	public void setClimates(List<Climate> climates) {
		this.climates = climates;
	}

	public Climate addClimate(Climate climate) {
		getClimates().add(climate);
		climate.setClimateclass(this);

		return climate;
	}

	public Climate removeClimate(Climate climate) {
		getClimates().remove(climate);
		climate.setClimateclass(null);

		return climate;
	}

	public Climatetype getClimatetype() {
		return this.climatetype;
	}

	public void setClimatetype(Climatetype climatetype) {
		this.climatetype = climatetype;
	}

}