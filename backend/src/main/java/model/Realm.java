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
 * The persistent class for the "realm" database table.
 * 
 */
@Entity
@Table(name = "\"realm\"")
@NamedQuery(name = "Realm.findAll", query = "SELECT r FROM Realm r")
public class Realm implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "\"realmid\"")
	private int realmid;

	@Column(name = "\"code\"")
	private String code;

	@Column(name = "\"desc\"")
	private String desc;

	@Column(name = "\"name\"")
	private String name;

	// bi-directional many-to-one association to Biome
	@OneToMany(mappedBy = "realm", fetch = FetchType.EAGER)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Biome> biomes;

	// bi-directional many-to-one association to Ecosystem
	@OneToMany(mappedBy = "realm", fetch = FetchType.LAZY)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Ecosystem> ecosystems;

	public Realm() {
	}

	public int getRealmid() {
		return this.realmid;
	}

	public void setRealmid(int realmid) {
		this.realmid = realmid;
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

	public List<Biome> getBiomes() {
		return this.biomes;
	}

	public void setBiomes(List<Biome> biomes) {
		this.biomes = biomes;
	}

	public Biome addBiome(Biome biome) {
		getBiomes().add(biome);
		biome.setRealm(this);

		return biome;
	}

	public Biome removeBiome(Biome biome) {
		getBiomes().remove(biome);
		biome.setRealm(null);

		return biome;
	}

	public List<Ecosystem> getEcosystems() {
		return this.ecosystems;
	}

	public void setEcosystems(List<Ecosystem> ecosystems) {
		this.ecosystems = ecosystems;
	}

	public Ecosystem addEcosystem(Ecosystem ecosystem) {
		getEcosystems().add(ecosystem);
		ecosystem.setRealm(this);

		return ecosystem;
	}

	public Ecosystem removeEcosystem(Ecosystem ecosystem) {
		getEcosystems().remove(ecosystem);
		ecosystem.setRealm(null);

		return ecosystem;
	}

}