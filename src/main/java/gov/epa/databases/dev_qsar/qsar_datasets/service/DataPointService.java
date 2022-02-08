package gov.epa.databases.dev_qsar.qsar_datasets.service;

import java.util.List;

import javax.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;

public interface DataPointService {
	
	public List<DataPoint> findByDatasetName(String datasetName);
	
	public List<DataPoint> findByDatasetName(String datasetName, Session session);
	
	public DataPoint create(DataPoint dataPoint) throws ConstraintViolationException;
	
	public DataPoint create(DataPoint dataPoint, Session session) throws ConstraintViolationException;

}
