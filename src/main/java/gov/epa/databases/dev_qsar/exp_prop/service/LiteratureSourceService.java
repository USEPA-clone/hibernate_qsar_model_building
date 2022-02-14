package gov.epa.databases.dev_qsar.exp_prop.service;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.exp_prop.entity.LiteratureSource;

public interface LiteratureSourceService {
	
	public LiteratureSource findByName(String sourceName);
	
	public LiteratureSource findByName(String sourceName, Session session);
	
	public List<LiteratureSource> findAll();
	
	public List<LiteratureSource> findAll(Session session);
	
}
