package gov.epa.databases.dev_qsar.qsar_models.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointContributor;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointInSplitting;


@Entity()
@Table(name = "predictions_dashboard", uniqueConstraints={@UniqueConstraint(columnNames = {"canon_qsar_smiles","fk_dsstox_records_id", "fk_model_id"})})
public class PredictionDashboard {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@NotBlank(message="Canonical QSAR-ready SMILES required")
	@Column(name="canon_qsar_smiles")
	private String canonQsarSmiles;


//	@NotNull(message="fk_dsstox_records_id required")
//	@Column(name="fk_dsstox_records_id")
//	private Long fk_dsstox_records_id;//alternatively can just store DTXCID and/or smiles in this table
//
//	@Transient
//	private DsstoxRecord dsstoxRecord;//temp storage for convenience (dont have in table because in different schema)- maybe move to this schema 

	@ManyToOne
	@NotNull(message="fk_dsstox_records_id required")
	@JoinColumn(name="fk_dsstox_records_id")
	private DsstoxRecord dsstoxRecord;//temp storage for convenience (dont have in table because in different schema)- maybe move to this schema 

	
	@ManyToOne
	@NotNull(message="Model required")
	@JoinColumn(name="fk_model_id")
	private Model model;
	
	@Column(name="experimental_value")
	private Double experimentalValue;
	
	@Column(name="experimental_string")
	private String experimentalString;
	
	@Column(name="prediction_value")
	private Double predictionValue;
	
	@Column(name="prediction_string")
	private String predictionString;
	
	@Column(name="prediction_error")
	private String predictionError;
	
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
	
	public String getKey() {
		return canonQsarSmiles+"\t"+getDsstoxRecord().getId()+"\t"+model.getId();
	}

	@OneToMany(mappedBy="predictionDashboard", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	private List<QsarPredictedNeighbor> qsarPredictedNeighbors;
	
	@OneToMany(mappedBy="predictionDashboard", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	private List<QsarPredictedADEstimate> qsarPredictedADEstimates;
	
		
	public Long getId() {
		return id;
	}

	
	public void setId(Long id) {
		this.id = id;
	}


	public String getCanonQsarSmiles() {
		return canonQsarSmiles;
	}

	public void setCanonQsarSmiles(String canonQsarSmiles) {
		this.canonQsarSmiles = canonQsarSmiles;
	}


	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	public Double getPredictionValue() {
		return predictionValue;
	}

	public void setPredictionValue(Double predictionValue) {
		this.predictionValue = predictionValue;
	}

	public String getPredictionString() {
		return predictionString;
	}

	public void setPredictionString(String predictionString) {
		this.predictionString = predictionString;
	}

	public String getPredictionError() {
		return predictionError;
	}

	public void setPredictionError(String predictionError) {
		this.predictionError = predictionError;
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



	public Double getExperimentalValue() {
		return experimentalValue;
	}

	public void setExperimentalValue(Double experimentalValue) {
		this.experimentalValue = experimentalValue;
	}

	public String getExperimentalString() {
		return experimentalString;
	}

	public void setExperimentalString(String experimentalString) {
		this.experimentalString = experimentalString;
	}


	public List<QsarPredictedNeighbor> getQsarPredictedNeighbors() {
		return qsarPredictedNeighbors;
	}


	public void setQsarPredictedNeighbors(List<QsarPredictedNeighbor> qsarPredictedNeighbors) {
		this.qsarPredictedNeighbors = qsarPredictedNeighbors;
	}


	public List<QsarPredictedADEstimate> getQsarPredictedADEstimates() {
		return qsarPredictedADEstimates;
	}


	public void setQsarPredictedADEstimates(List<QsarPredictedADEstimate> qsarPredictedADEstimates) {
		this.qsarPredictedADEstimates = qsarPredictedADEstimates;
	}


	public DsstoxRecord getDsstoxRecord() {
		return dsstoxRecord;
	}


	public void setDsstoxRecord(DsstoxRecord dsstoxRecord) {
		this.dsstoxRecord = dsstoxRecord;
	}

}
