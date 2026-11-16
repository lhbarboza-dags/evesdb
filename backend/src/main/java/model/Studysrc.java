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
 * The persistent class for the "studysrc" database table.
 * 
 */
@Entity
@Table(name = "\"studysrc\"")
@NamedQuery(name = "Studysrc.findAll", query = "SELECT s FROM Studysrc s")
public class Studysrc implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "\"srcid\"")
	private int srcid;

	@Column(name = "\"abstract\"")
	private String abstract_;

	@Column(name = "\"author\"")
	private String author;

//	@Column(name = "\"doctypeId\"")
//	private int doctypeId;

	@Column(name = "\"dwnlinkext\"")
	private String dwnlinkext;

	@Column(name = "\"pubyear\"")
	private int pubyear;
	
	@Column(name = "\"status\"")
	private Integer status;

	@Column(name = "\"reference\"")
	private String reference;

	@Column(name = "\"title\"")
	private String title;
	
	@Column(name = "\"codeddate\"")
	private String codeddate;
	
	@Column(name = "\"revieweddate\"")
	private String revieweddate;

	// bi-directional many-to-one association to Dataset
	@OneToMany(mappedBy = "studysrc", fetch = FetchType.EAGER)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Dataset> datasets;
	
	@OneToMany(mappedBy = "studysrc", fetch = FetchType.EAGER)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Site> sites;

	// bi-directional many-to-one association to Cvterm
	@ManyToOne
	@JoinColumns({ @JoinColumn(name = "\"doctypeId\"", insertable = true, updatable = true) })
	private Cvterm doctype;
	
	// bi-directional many-to-one association to User
	@ManyToOne
	@JoinColumns({ @JoinColumn(name = "codedby", insertable = true, updatable = true) })
	private User codedby;

	// bi-directional many-to-one association to User
	@ManyToOne
	@JoinColumns({ @JoinColumn(name = "reviewedby", insertable = true, updatable = true) })
	private User reviewedby;
	
	
	@Column(name = "\"isdeleted\"")
	private boolean isdeleted;


	public Studysrc() {
	}

	public int getSrcid() {
		return this.srcid;
	}

	public void setSrcid(int srcid) {
		this.srcid = srcid;
	}

	public String getAbstract_() {
		return this.abstract_;
	}

	public void setAbstract_(String abstract_) {
		this.abstract_ = abstract_;
	}

	public String getAuthor() {
		return this.author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

//	public int getDoctypeId() {
//		return this.doctypeId;
//	}
//
//	public void setDoctypeId(int doctypeId) {
//		this.doctypeId = doctypeId;
//	}

	public String getDwnlinkext() {
		return this.dwnlinkext;
	}

	public void setDwnlinkext(String dwnlinkext) {
		this.dwnlinkext = dwnlinkext;
	}

	public int getPubyear() {
		return this.pubyear;
	}

	public void setPubyear(int pubyear) {
		this.pubyear = pubyear;
	}

	public String getReference() {
		return this.reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<Dataset> getDatasets() {
		return this.datasets;
	}

	public void setDatasets(List<Dataset> datasets) {
		this.datasets = datasets;
	}

	public Dataset addDataset(Dataset dataset) {
		getDatasets().add(dataset);
		dataset.setStudysrc(this);

		return dataset;
	}

	public Dataset removeDataset(Dataset dataset) {
		getDatasets().remove(dataset);
		dataset.setStudysrc(null);

		return dataset;
	}
	
	public List<Site> getSites() {
		return this.sites;
	}

	public void setSite(List<Site> sites) {
		this.sites = sites;
	}

	public Site addSite(Site site) {
		getSites().add(site);
		site.setStudysrc(this);

		return site;
	}

	public Site removeSite(Site site) {
		getSites().remove(site);
		site.setStudysrc(null);

		return site;
	}

	public Cvterm getDoctype() {
		return doctype;
	}

	public void setDoctype(Cvterm doctype) {
		this.doctype = doctype;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getCodeddate() {
		return codeddate;
	}

	public void setCodeddate(String codeddate) {
		this.codeddate = codeddate;
	}
	
	

	public String getRevieweddate() {
		return revieweddate;
	}

	public void setRevieweddate(String revieweddate) {
		this.revieweddate = revieweddate;
	}

	public User getCodedby() {
		return codedby;
	}

	public void setCodedby(User codedby) {
		this.codedby = codedby;
	}

	public User getReviewedby() {
		return reviewedby;
	}

	public void setReviewedby(User reviewedby) {
		this.reviewedby = reviewedby;
	}

	public boolean isIsdeleted() {
		return isdeleted;
	}

	public void setIsdeleted(boolean isdeleted) {
		this.isdeleted = isdeleted;
	}
	
	
	
	
	

}