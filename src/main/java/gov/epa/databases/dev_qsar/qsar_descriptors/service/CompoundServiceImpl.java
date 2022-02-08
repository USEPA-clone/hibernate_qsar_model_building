package gov.epa.databases.dev_qsar.qsar_descriptors.service;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_descriptors.QsarDescriptorsSession;
import gov.epa.databases.dev_qsar.qsar_descriptors.dao.CompoundDao;
import gov.epa.databases.dev_qsar.qsar_descriptors.dao.CompoundDaoImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.Compound;

public class CompoundServiceImpl implements CompoundService {
	
	Validator validator;
	
	public CompoundServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public Compound findByDtxcidAndStandardizer(String dtxcid, String standardizer) {
		Session session = QsarDescriptorsSession.getSessionFactory().getCurrentSession();
		return findByDtxcidAndStandardizer(dtxcid, standardizer, session);
	}
	
	public Compound findByDtxcidAndStandardizer(String dtxcid, String standardizer, Session session) {
		Transaction t = session.beginTransaction();
		CompoundDao compoundDao = new CompoundDaoImpl();
		Compound compound = compoundDao.findByDtxcidAndStandardizer(dtxcid, standardizer, session);
		t.rollback();
		return compound;
	}
	
	public List<Compound> findByCanonQsarSmiles(String canonQsarSmiles) {
		Session session = QsarDescriptorsSession.getSessionFactory().getCurrentSession();
		return findByCanonQsarSmiles(canonQsarSmiles, session);
	}
	
	public List<Compound> findByCanonQsarSmiles(String canonQsarSmiles, Session session) {
		Transaction t = session.beginTransaction();
		CompoundDao compoundDao = new CompoundDaoImpl();
		List<Compound> compounds = compoundDao.findByCanonQsarSmiles(canonQsarSmiles, session);
		t.rollback();
		return compounds;
	}
	
	@Override
	public Compound create(Compound compound) throws ConstraintViolationException {
		Session session = QsarDescriptorsSession.getSessionFactory().getCurrentSession();
		return create(compound, session);
	}

	@Override
	public Compound create(Compound compound, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<Compound>> violations = validator.validate(compound);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.save(compound);
			session.flush();
			session.refresh(compound);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return compound;
	}
}
