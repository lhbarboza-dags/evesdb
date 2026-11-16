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
 * The persistent class for the "efgrp" database table.
 * 
 */
@Entity
@Table(name = "\"efgrp\"")
@NamedQuery(name = "Efgrp.findAll", query = "SELECT e FROM Efgrp e")
public class Efgrp implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "\"efgrpid\"")
	private int efgrpid;

	@Column(name = "\"code\"")
	private String code;

	@Column(name = "\"desc\"")
	private String desc;

	@Column(name = "\"name\"")
	private String name;

	// bi-directional many-to-one association to Ecosystem
	@OneToMany(mappedBy = "efgrp", fetch = FetchType.LAZY)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Ecosystem> ecosystems;

	// bi-directional many-to-one association to Biome
	@ManyToOne
	@JoinColumns({ @JoinColumn(name = "ebiomeid", insertable = true, updatable = true) })
	private Biome biome;

	public Efgrp() {
	}

	public int getEfgrpid() {
		return this.efgrpid;
	}

	public void setEfgrpid(int efgrpid) {
		this.efgrpid = efgrpid;
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

	public List<Ecosystem> getEcosystems() {
		return this.ecosystems;
	}

	public void setEcosystems(List<Ecosystem> ecosystems) {
		this.ecosystems = ecosystems;
	}

	public Ecosystem addEcosystem(Ecosystem ecosystem) {
		getEcosystems().add(ecosystem);
		ecosystem.setEfgrp(this);

		return ecosystem;
	}

	public Ecosystem removeEcosystem(Ecosystem ecosystem) {
		getEcosystems().remove(ecosystem);
		ecosystem.setEfgrp(null);

		return ecosystem;
	}

	public Biome getBiome() {
		return this.biome;
	}

	public void setBiome(Biome biome) {
		this.biome = biome;
	}

}