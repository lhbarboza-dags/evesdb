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
 * The persistent class for the "biome" database table.
 * 
 */
@Entity
@Table(name="\"biome\"")
@NamedQuery(name="Biome.findAll", query="SELECT b FROM Biome b")
public class Biome implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="\"biomeid\"")
	private int biomeid;

	@Column(name="\"bcode\"")
	private String bcode;

	@Column(name="\"bdesc\"")
	private String bdesc;

	@Column(name="\"bname\"")
	private String bname;

//	@Column(name="\"brealmid\"")
//	private int realmid;

	//bi-directional many-to-one association to Realm
	@ManyToOne
	@JoinColumns({ @JoinColumn(name = "brealmid", insertable = true, updatable = true)
		})
	private Realm realm;

	//bi-directional many-to-one association to Ecosystem
	@OneToMany(mappedBy="biome", fetch = FetchType.LAZY)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Ecosystem> ecosystems;

	//bi-directional many-to-one association to Efgrp
	@OneToMany(mappedBy="biome", fetch = FetchType.EAGER)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Efgrp> efgrps;

	public Biome() {
	}

	public int getBiomeid() {
		return this.biomeid;
	}

	public void setBiomeid(int biomeid) {
		this.biomeid = biomeid;
	}

	public String getBcode() {
		return this.bcode;
	}

	public void setBcode(String bcode) {
		this.bcode = bcode;
	}

	public String getBdesc() {
		return this.bdesc;
	}

	public void setBdesc(String bdesc) {
		this.bdesc = bdesc;
	}

	public String getBname() {
		return this.bname;
	}

	public void setBname(String bname) {
		this.bname = bname;
	}

//	public int getRealmid() {
//		return this.realmid;
//	}
//
//	public void setRealmid(int realmid) {
//		this.realmid = realmid;
//	}

	public Realm getRealm() {
		return this.realm;
	}

	public void setRealm(Realm realm) {
		this.realm = realm;
	}

	public List<Ecosystem> getEcosystems() {
		return this.ecosystems;
	}

	public void setEcosystems(List<Ecosystem> ecosystems) {
		this.ecosystems = ecosystems;
	}

	public Ecosystem addEcosystem(Ecosystem ecosystem) {
		getEcosystems().add(ecosystem);
		ecosystem.setBiome(this);

		return ecosystem;
	}

	public Ecosystem removeEcosystem(Ecosystem ecosystem) {
		getEcosystems().remove(ecosystem);
		ecosystem.setBiome(null);

		return ecosystem;
	}

	public List<Efgrp> getEfgrps() {
		return this.efgrps;
	}

	public void setEfgrps(List<Efgrp> efgrps) {
		this.efgrps = efgrps;
	}

	public Efgrp addEfgrp(Efgrp efgrp) {
		getEfgrps().add(efgrp);
		efgrp.setBiome(this);

		return efgrp;
	}

	public Efgrp removeEfgrp(Efgrp efgrp) {
		getEfgrps().remove(efgrp);
		efgrp.setBiome(null);

		return efgrp;
	}

}