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
 * The persistent class for the "site" database table.
 * 
 */
@Entity
@Table(name = "\"site\"")
@NamedQuery(name = "Site.findAll", query = "SELECT s FROM Site s")
public class Site implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "\"siteid\"")
	private int siteid;

	@Column(name = "\"area\"")
	private String area;

//	@Column(name = "\"areaattr\"")
//	private integer areaattr;

	@Column(name = "\"areaunit\"")
	private String areaunit;

	@Column(name = "\"latitude\"")
	private String latitude;

	@Column(name = "\"longitude\"")
	private String longitude;

	@Column(name = "\"name\"")
	private String name;
	
	@Column(name = "\"isdeleted\"")
	private boolean isdeleted;

//	@Column(name = "\"scaletypeid\"")
//	private int scaletypeid;

//	@Column(name = "\"studyLocationId\"")
//	private int studyLocationId;

	// bi-directional many-to-one association to Dataset
	@OneToMany(mappedBy = "site", fetch = FetchType.EAGER)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Dataset> datasets;

	// bi-directional many-to-one association to Location
	@ManyToOne
	@JoinColumns({ @JoinColumn(name = "\"studyLocationId\"", insertable = true, updatable = true) })
	private Location location;

	// bi-directional many-to-one association to Location
	@ManyToOne
	@JoinColumns({ @JoinColumn(name = "scaletypeid", insertable = true, updatable = true) })
	private Cvterm scale;

	// bi-directional many-to-one association to Siteattr
	@OneToMany(mappedBy = "site", fetch = FetchType.EAGER)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Siteattr> siteattrs;
	// bi-directional many-to-one association to Climatetype

	// bi-directional many-to-one association to Climate
	@OneToMany(mappedBy = "site", fetch = FetchType.LAZY)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Climate> climates;
	
	@ManyToOne
	@JoinColumns({ @JoinColumn(name = "studysrcid", insertable = true, updatable = true) })
	private Studysrc studysrc;

	public Site() {
	}

	public int getSiteid() {
		return this.siteid;
	}

	public void setSiteid(int siteid) {
		this.siteid = siteid;
	}

	public String getArea() {
		return this.area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getAreaunit() {
		return this.areaunit;
	}

	public void setAreaunit(String areaunit) {
		this.areaunit = areaunit;
	}

	public String getLatitude() {
		return this.latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return this.longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

//	public int getScaletypeid() {
//		return this.scaletypeid;
//	}
//
//	public void setScaletypeid(int scaletypeid) {
//		this.scaletypeid = scaletypeid;
//	}
//
//	public int getStudyLocationId() {
//		return this.studyLocationId;
//	}
//
//	public void setStudyLocationId(int studyLocationId) {
//		this.studyLocationId = studyLocationId;
//	}

	public List<Dataset> getDatasets() {
		return this.datasets;
	}

	public void setDatasets(List<Dataset> datasets) {
		this.datasets = datasets;
	}

	public Dataset addDataset(Dataset dataset) {
		getDatasets().add(dataset);
		dataset.setSite(this);

		return dataset;
	}

	public Dataset removeDataset(Dataset dataset) {
		getDatasets().remove(dataset);
		dataset.setSite(null);

		return dataset;
	}

	public Location getLocation() {
		return this.location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Cvterm getScale() {
		return scale;
	}

	public void setScale(Cvterm scale) {
		this.scale = scale;
	}
	
	public List<Climate> getClimate() {
		return this.climates;
	}

	public void setClimates(List<Climate> climates) {
		this.climates = climates;
	}

	public Climate addClimate(Climate climate) {
		getClimate().add(climate);
		climate.setSite(this);

		return climate;
	}

	public Climate removeSite(Climate climate) {
		getClimate().remove(climate);
		climate.setSite(null);

		return climate;
	}
	
	public List<Siteattr> getSiteAttrs() {
		return this.siteattrs;
	}

	public void setSiteAttrs(List<Siteattr> siteAttrs) {
		this.siteattrs = siteAttrs;
	}

	public Siteattr addSiteAttr(Siteattr siteattr) {
		getSiteAttrs().add(siteattr);
		siteattr.setSite(this);

		return siteattr;
	}

	public Siteattr removeSiteattr(Siteattr siteattr) {
		getSiteAttrs().remove(siteattr);
		siteattr.setSite(null);

		return siteattr;
	}

	public boolean isIsdeleted() {
		return isdeleted;
	}

	public void setIsdeleted(boolean isdeleted) {
		this.isdeleted = isdeleted;
	}

	public List<Siteattr> getSiteattrs() {
		return siteattrs;
	}

	public void setSiteattrs(List<Siteattr> siteattrs) {
		this.siteattrs = siteattrs;
	}

	public List<Climate> getClimates() {
		return climates;
	}
	
	public Studysrc getStudysrc() {
		return this.studysrc;
	}

	public void setStudysrc(Studysrc studysrc) {
		this.studysrc = studysrc;
	}
	

	
	

}