package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.dao.ModelSetDao;
import gov.epa.databases.dev_qsar.qsar_models.dao.ModelSetDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelSet;

public class ModelSetServiceImpl implements ModelSetService {
	
	private Validator validator;
	
	public ModelSetServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public ModelSet findById(Long modelSetId) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findById(modelSetId, session);
	}
	
	public ModelSet findById(Long modelSetId, Session session) {
		Transaction t = session.beginTransaction();
		ModelSetDao modelSetDao = new ModelSetDaoImpl();
		ModelSet modelSet = modelSetDao.findById(modelSetId, session);
		t.rollback();
		return modelSet;
	}
	
	@Override
	public Set<ConstraintViolation<ModelSet>> create(ModelSet modelSet) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return create(modelSet, session);
	}

	@Override
	public Set<ConstraintViolation<ModelSet>> create(ModelSet modelSet, Session session) {
		Set<ConstraintViolation<ModelSet>> violations = validator.validate(modelSet);
		if (!violations.isEmpty()) {
			return violations;
		}
		
		Transaction t = session.beginTransaction();
		session.save(modelSet);
		session.flush();
		session.refresh(modelSet);
		t.commit();
		return null;
	}

}
