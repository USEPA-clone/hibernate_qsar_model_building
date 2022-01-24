package gov.epa.databases.dev_qsar.qsar_descriptors.service;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_descriptors.entity.Compound;

public interface CompoundService {
	
	public Compound findByDtxcidAndStandardizer(String dtxcid, String standardizer);
	
	public Compound findByDtxcidAndStandardizer(String dtxcid, String standardizer, Session session);
	
	public List<Compound> findByCanonQsarSmiles(String canonQsarSmiles);
	
	public List<Compound> findByCanonQsarSmiles(String canonQsarSmiles, Session session);
	
	public Set<ConstraintViolation<Compound>> create(Compound compound);
	
	public Set<ConstraintViolation<Compound>> create(Compound compound, Session session);

}
