package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.dao.ModelStatisticDao;
import gov.epa.databases.dev_qsar.qsar_models.dao.ModelStatisticDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelStatistic;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

public class ModelStatisticServiceImpl implements ModelStatisticService {
	
	private Validator validator;
	
	public ModelStatisticServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}

	@Override
	public List<ModelStatistic> findByModelId(Long modelId) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findByModelId(modelId, session);
	}

	@Override
	public List<ModelStatistic> findByModelId(Long modelId, Session session) {
		Transaction t = session.beginTransaction();
		ModelStatisticDao modelStatisticDao = new ModelStatisticDaoImpl();
		List<ModelStatistic> modelStatistic = modelStatisticDao.findByModelId(modelId, session);
		t.rollback();
		return modelStatistic;
	}

	@Override
	public Set<ConstraintViolation<ModelStatistic>> create(ModelStatistic modelStatistic) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return create(modelStatistic, session);
	}

	@Override
	public Set<ConstraintViolation<ModelStatistic>> create(ModelStatistic modelStatistic, Session session) {
		Set<ConstraintViolation<ModelStatistic>> violations = validator.validate(modelStatistic);
		if (!violations.isEmpty()) {
			return violations;
		}
		
		Transaction t = session.beginTransaction();
		session.save(modelStatistic);
		session.flush();
		session.refresh(modelStatistic);
		t.commit();
		return null;
	}

}
