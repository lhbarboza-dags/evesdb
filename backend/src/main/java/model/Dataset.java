package model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

/**
 * The persistent class for the "dataset" database table.
 * 
 */
@Entity
@Table(name = "\"dataset\"")
@NamedQuery(name = "Dataset.findAll", query = "SELECT d FROM Dataset d")
public class Dataset implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "\"datasetid\"")
	private int datasetid;

//	@Column(name = "\"dcodebyid\"")
//	private int codebyid;

//	@Temporal(TemporalType.DATE)
//	@Column(name = "\"codeddate\"")
//	private Date codeddate;

//	@Column(name = "\"decosystemid\"")
//	private int ecosystemid;
//
//	@Column(name = "\"dreviewedbyid\"")
//	private int reviewedbyid;

//	@Temporal(TemporalType.DATE)
//	@Column(name = "\"revieweddate\"")
//	private Date revieweddate;

//	@Column(name = "\"dserviceid\"")
//	private int serviceid;

//	@Column(name = "\"dsiteid\"")
//	private int siteid;

//	@Column(name = "\"dstudysrcid\"")
//	private int studysrcid;

//	@Column(name = "\"dvalueid\"")
//	private int valueid;

//	@Column(name = "\"dvmethodid\"")
//	private int vmethodid;
	
	@Column(name = "\"isdeleted\"")
	private boolean isdeleted;

	// bi-directional many-to-one association to Ecosystem
	@ManyToOne
	@JoinColumns({ @JoinColumn(name = "decosystemid", insertable = true, updatable = true) })
	private Ecosystem ecosystem;

	// bi-directional many-to-one association to Service
	@ManyToOne
	@JoinColumns({ @JoinColumn(name = "dserviceid", insertable = true, updatable = true) })
	private Service service;

	// bi-directional many-to-one association to Site
	@ManyToOne
	@NotFound(action = NotFoundAction.IGNORE)
	@JoinColumns({ @JoinColumn(name = "dsiteid", insertable = true, updatable = true) })
	private Site site;

	// bi-directional many-to-one association to Studysrc
	@ManyToOne
	@JoinColumns({ @JoinColumn(name = "dstudysrcid", insertable = true, updatable = true) })
	private Studysrc studysrc;

//	// bi-directional many-to-one association to User
//	@ManyToOne
//	@JoinColumns({ @JoinColumn(name = "dcodebyid", insertable = true, updatable = true) })
//	private User codeby;
//
//	// bi-directional many-to-one association to User
//	@ManyToOne
//	@JoinColumns({ @JoinColumn(name = "dreviewedbyid", insertable = true, updatable = true) })
//	private User reviewedby;

	// bi-directional many-to-one association to Valuationmethod
	@ManyToOne
	@JoinColumns({ @JoinColumn(name = "dvmethodid", insertable = true, updatable = true) })
	private Valuationmethod valuationmethod;

	// bi-directional many-to-one association to Value
	@ManyToOne
	@JoinColumns({ @JoinColumn(name = "valueid", insertable = true, updatable = true) })
	private Value value;

	public Dataset() {
	}

	public int getDatasetid() {
		return this.datasetid;
	}

	public void setDatasetid(int datasetid) {
		this.datasetid = datasetid;
	}

//	public int getCodebyid() {
//		return this.codebyid;
//	}
//
//	public void setCodebyid(int codebyid) {
//		this.codebyid = codebyid;
//	}

//	public Date getCodeddate() {
//		return this.codeddate;
//	}
//
//	public void setCodeddate(Date codeddate) {
//		this.codeddate = codeddate;
//	}

//	public int getEcosystemid() {
//		return this.ecosystemid;
//	}
//
//	public void setEcosystemid(int ecosystemid) {
//		this.ecosystemid = ecosystemid;
//	}
//
//	public int getReviewedbyid() {
//		return this.reviewedbyid;
//	}
//
//	public void setReviewedbyid(int reviewedbyid) {
//		this.reviewedbyid = reviewedbyid;
//	}

//	public Date getRevieweddate() {
//		return this.revieweddate;
//	}
//
//	public void setRevieweddate(Date revieweddate) {
//		this.revieweddate = revieweddate;
//	}

//	public int getServiceid() {
//		return this.serviceid;
//	}
//
//	public void setServiceid(int serviceid) {
//		this.serviceid = serviceid;
//	}

//	public int getSiteid() {
//		return this.siteid;
//	}
//
//	public void setSiteid(int siteid) {
//		this.siteid = siteid;
//	}
//
//	public int getStudysrcid() {
//		return this.studysrcid;
//	}
//
//	public void setStudysrcid(int studysrcid) {
//		this.studysrcid = studysrcid;
//	}
//
//	public int getValueid() {
//		return this.valueid;
//	}
//
//	public void setValueid(int valueid) {
//		this.valueid = valueid;
//	}
//
//	public int getVmethodid() {
//		return this.vmethodid;
//	}
//
//	public void setVmethodid(int vmethodid) {
//		this.vmethodid = vmethodid;
//	}

	public Ecosystem getEcosystem() {
		return this.ecosystem;
	}

	public void setEcosystem(Ecosystem ecosystem) {
		this.ecosystem = ecosystem;
	}

	public Service getService() {
		return this.service;
	}

	public void setService(Service service) {
		this.service = service;
	}

	public Site getSite() {
		return this.site;
	}

	public void setSite(Site site) {
		this.site = site;
	}

	public Studysrc getStudysrc() {
		return this.studysrc;
	}

	public void setStudysrc(Studysrc studysrc) {
		this.studysrc = studysrc;
	}
	
	

//	public User getUser1() {
//		return this.user1;
//	}
//
//	public void setUser1(User user1) {
//		this.user1 = user1;
//	}
//
//	public User getUser2() {
//		return this.user2;
//	}
//
//	public void setUser2(User user2) {
//		this.user2 = user2;
//	}

	public boolean isIsdeleted() {
		return isdeleted;
	}

	public void setIsdeleted(boolean isdeleted) {
		this.isdeleted = isdeleted;
	}

	public Valuationmethod getValuationmethod() {
		return this.valuationmethod;
	}

	public void setValuationmethod(Valuationmethod valuationmethod) {
		this.valuationmethod = valuationmethod;
	}

	public Value getValue() {
		return this.value;
	}

	public void setValue(Value value) {
		this.value = value;
	}


//	public User getCodeby() {
//		return codeby;
//	}
//
//	public void setCodeby(User codebyid) {
//		this.codeby = codebyid;
//	}
//
//	public User getReviewedby() {
//		return reviewedby;
//	}
//
//	public void setReviewedby(User reviewedbyid) {
//		this.reviewedby = reviewedbyid;
//	}

}