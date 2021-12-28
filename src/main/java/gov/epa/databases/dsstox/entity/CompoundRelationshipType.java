package gov.epa.databases.dsstox.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

@Entity
@Table(name="compound_relationship_types")
public class CompoundRelationshipType {
	@Id
	@GeneratedValue
	@Column(name="id")
	private Long id;

	@Column(name="name")
	private String name;

	@Column(name="short_description_forward")
	private String descriptionForward;

	@Column(name="label_forward")
	private String labelForward;

	@Column(name="short_description_backward")
	private String descriptionBackward;

	@Column(name="label_backward")
	private String labelBackward;

	@OneToMany(mappedBy = "compoundRelationshipType")
	List<CompoundRelationship> compoundRelationships;

	@Column(name="created_at")
	@Generated(value=GenerationTime.INSERT)
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdDate;

	@Column(name="updated_at")
	@Generated(value=GenerationTime.ALWAYS)
	@Temporal(TemporalType.TIMESTAMP)
	private Date modifiedDate;

	@Column(name="created_by")
	private String createdBy;

	@Column(name="updated_by")
	private String updatedBy;

	public CompoundRelationshipType() {}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescriptionForward() {
		return descriptionForward;
	}

	public void setDescriptionForward(String descriptionForward) {
		this.descriptionForward = descriptionForward;
	}

	public String getLabelForward() {
		return labelForward;
	}

	public void setLabelForward(String labelForward) {
		this.labelForward = labelForward;
	}

	public String getDescriptionBackward() {
		return descriptionBackward;
	}

	public void setDescriptionBackward(String descriptionBackward) {
		this.descriptionBackward = descriptionBackward;
	}

	public String getLabelBackward() {
		return labelBackward;
	}

	public void setLabelBackward(String labelBackward) {
		this.labelBackward = labelBackward;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public List<CompoundRelationship> getCompoundRelationships() {
		return compoundRelationships;
	}
}
