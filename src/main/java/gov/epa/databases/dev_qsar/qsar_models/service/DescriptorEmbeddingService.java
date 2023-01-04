package gov.epa.databases.dev_qsar.qsar_models.service;

import javax.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.DescriptorEmbedding;

public interface DescriptorEmbeddingService {
	
	public DescriptorEmbedding findByName(String descriptorEmbeddingName);
	
	public DescriptorEmbedding findByName(String descriptorEmbeddingName, Session session);
	
	public DescriptorEmbedding findByGASettings(String qsar_method, String dataset_name, String descriptor_set_name, 
			String descriptionJson);
	
	public DescriptorEmbedding findByGASettings(String qsar_method, String dataset_name, String descriptor_set_name, 
			String descriptionJson, Session session);
	
	public DescriptorEmbedding create(DescriptorEmbedding descriptorEmbedding) throws ConstraintViolationException;
	
	public DescriptorEmbedding create(DescriptorEmbedding descriptorEmbedding, Session session) throws ConstraintViolationException;

}
