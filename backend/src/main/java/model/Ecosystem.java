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
 * The persistent class for the "ecosystem" database table.
 * 
 */
@Entity
@Table(name = "\"ecosystem\"")
@NamedQuery(name = "Ecosystem.findAll", query = "SELECT e FROM Ecosystem e")
public class Ecosystem implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "\"ecosysid\"")
	private int ecosysid;

	@Column(name = "\"bdesc\"")
	private String bdesc;

//	@Column(name = "\"ebiomeid\"")
//	private int biomeid;

	@Column(name = "\"efgdesc\"")
	private String efgdesc;

//	@Column(name = "\"eefgrpid\"")
//	private int efgrpid;

	@Column(name = "\"rdesc\"")
	private String rdesc;

//	@Column(name = "\"erealmid\"")
//	private int realmid;

	// bi-directional many-to-one association to Dataset
	@OneToMany(mappedBy = "ecosystem")
	private List<Dataset> datasets;

	// bi-directional many-to-one association to Biome
	@ManyToOne
	@JoinColumns({ @JoinColumn(name = "ebiomeid", insertable = true, updatable = true) })
	private Biome biome;

	// bi-directional many-to-one association to Efgrp
	@ManyToOne
	@JoinColumns({ @JoinColumn(name = "eefgrpid", insertable = true, updatable = true) })
	private Efgrp efgrp;

	// bi-directional many-to-one association to Realm
	@ManyToOne
	@JoinColumns({ @JoinColumn(name = "erealmid", insertable = true, updatable = true) })
	private Realm realm;

	public Ecosystem() {
	}

	public int getEcosysid() {
		return this.ecosysid;
	}

	public void setEcosysid(int ecosysid) {
		this.ecosysid = ecosysid;
	}

	public String getBdesc() {
		return this.bdesc;
	}

	public void setBdesc(String bdesc) {
		this.bdesc = bdesc;
	}

//	public int getBiomeid() {
//		return this.biomeid;
//	}
//
//	public void setBiomeid(int biomeid) {
//		this.biomeid = biomeid;
//	}

	public String getEfgdesc() {
		return this.efgdesc;
	}

	public void setEfgdesc(String efgdesc) {
		this.efgdesc = efgdesc;
	}

//	public int getEfgrpid() {
//		return this.efgrpid;
//	}
//
//	public void setEfgrpid(int efgrpid) {
//		this.efgrpid = efgrpid;
//	}

	public String getRdesc() {
		return this.rdesc;
	}

	public void setRdesc(String rdesc) {
		this.rdesc = rdesc;
	}

//	public int getRealmid() {
//		return this.realmid;
//	}
//
//	public void setRealmid(int realmid) {
//		this.realmid = realmid;
//	}

	public List<Dataset> getDatasets() {
		return this.datasets;
	}

	public void setDatasets(List<Dataset> datasets) {
		this.datasets = datasets;
	}

	public Dataset addDataset(Dataset dataset) {
		getDatasets().add(dataset);
		dataset.setEcosystem(this);

		return dataset;
	}

	public Dataset removeDataset(Dataset dataset) {
		getDatasets().remove(dataset);
		dataset.setEcosystem(null);

		return dataset;
	}

	public Biome getBiome() {
		return this.biome;
	}

	public void setBiome(Biome biome) {
		this.biome = biome;
	}

	public Efgrp getEfgrp() {
		return this.efgrp;
	}

	public void setEfgrp(Efgrp efgrp) {
		this.efgrp = efgrp;
	}

	public Realm getRealm() {
		return this.realm;
	}

	public void setRealm(Realm realm) {
		this.realm = realm;
	}

}