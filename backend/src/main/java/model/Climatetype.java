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
 * The persistent class for the "climatetype" database table.
 * 
 */
@Entity
@Table(name = "\"climatetype\"")
@NamedQuery(name = "Climatetype.findAll", query = "SELECT c FROM Climatetype c")
public class Climatetype implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "\"cltid\"")
	private int cltid;

	@Column(name = "\"cltDesc\"")
	private String cltDesc;

	@Column(name = "\"cltname\"")
	private String cltname;

	// bi-directional many-to-one association to Climate
	@OneToMany(mappedBy = "climatetype", fetch = FetchType.LAZY)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Climate> climates;

	// bi-directional many-to-one association to Climateclass
	@OneToMany(mappedBy = "climatetype", fetch = FetchType.EAGER)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Climateclass> climateclasses;
	
	

	public Climatetype() {
	}

	public int getCltid() {
		return this.cltid;
	}

	public void setCltid(int cltid) {
		this.cltid = cltid;
	}

	public String getCltDesc() {
		return this.cltDesc;
	}

	public void setCltDesc(String cltDesc) {
		this.cltDesc = cltDesc;
	}

	public String getCltname() {
		return this.cltname;
	}

	public void setCltname(String cltname) {
		this.cltname = cltname;
	}

	public List<Climate> getClimates() {
		return this.climates;
	}

	public void setClimates(List<Climate> climates) {
		this.climates = climates;
	}

	public Climate addClimate(Climate climate) {
		getClimates().add(climate);
		climate.setClimatetype(this);

		return climate;
	}

	public Climate removeClimate(Climate climate) {
		getClimates().remove(climate);
		climate.setClimatetype(null);

		return climate;
	}
	
	

	public List<Climateclass> getClimateclasses() {
		return this.climateclasses;
	}

	public void setClimateclasses(List<Climateclass> climateclasses) {
		this.climateclasses = climateclasses;
	}

	public Climateclass addClimateclass(Climateclass climateclass) {
		getClimateclasses().add(climateclass);
		climateclass.setClimatetype(this);

		return climateclass;
	}

	public Climateclass removeClimateclass(Climateclass climateclass) {
		getClimateclasses().remove(climateclass);
		climateclass.setClimatetype(null);

		return climateclass;
	}

}