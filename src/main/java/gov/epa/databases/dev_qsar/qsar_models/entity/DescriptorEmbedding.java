package gov.epa.databases.dev_qsar.qsar_models.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name="descriptor_embeddings", indexes={@Index(name="embed_name_idx", columnList="name", unique=true)})
public class DescriptorEmbedding {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message="Name required")
	@Column(name="name", unique=true)
	private String name;
	
	@NotBlank(message="Description required")
	@Column(name="description", length=2047)
	private String description;
	
	@NotBlank(message="Descriptor set name required")
	@Column(name="descriptor_set_name")
	private String descriptorSetName;
	
	@NotBlank(message="Dataset name required")
	@Column(name="dataset_name")
	private String datasetName;
	
	@NotNull
	@Column(name="embedding_tsv", length=2047)
	private String embeddingTsv;
	
	@Column(name="qsar_method")
	private String qsarMethod;
	
	@NotNull
	@Column(name="importance_tsv", length=2047)
	private String importanceTsv;

	@Column(name="updated_at")
	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedAt;
	
	@Column(name="updated_by")
	private String updatedBy;
	
	@Column(name="created_at")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;
	
	@NotBlank(message="Creator required")
	@Column(name="created_by")
	private String createdBy;
	
	
	
	public DescriptorEmbedding() {}
	
	public DescriptorEmbedding(String name, String description, String embeddingTsv, String createdBy,
			String descriptorSetName) {
		this.setName(name);
		this.setDescription(description);
		this.setEmbeddingTsv(embeddingTsv);
		this.setCreatedBy(createdBy);
	}

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getEmbeddingTsv() {
		return embeddingTsv;
	}

	public void setEmbeddingTsv(String embeddingTsv) {
		this.embeddingTsv = embeddingTsv;
	}
	
	public String getQsarMethod() {
		return qsarMethod;
	}
	
	public void setQsarMethod(String qsarMethod) {
		this.qsarMethod = qsarMethod;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getDescriptorSetName() {
		return descriptorSetName;
	}

	public void setDescriptorSetName(String descriptorSetName) {
		this.descriptorSetName = descriptorSetName;
	}

	public String getDatasetName() {
		return datasetName;
	}

	public void setDatasetName(String datasetName) {
		this.datasetName = datasetName;
	}

	public String getImportanceTsv() {
		return importanceTsv;
	}

	public void setImportanceTsv(String importanceTsv) {
		this.importanceTsv = importanceTsv;
	}
}
