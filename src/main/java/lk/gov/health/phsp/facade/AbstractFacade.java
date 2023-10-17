/*
 * Author
 * Dr. M H B Ariyaratne, MO(Health Information), email : buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.facade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.QueryHint;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import lk.gov.health.phsp.entity.SequenceNumber;
import lk.gov.health.phsp.pojcs.Identifiable;
import org.eclipse.persistence.config.CacheUsage;
import org.eclipse.persistence.config.QueryHints;

/**
 *
 * @author Dr. M H B Ariyaratne <buddhika.ari at gmail.com>
 * @param <T>
 */
public abstract class AbstractFacade<T extends Identifiable> {

    private Class<T> entityClass;

    public void flush() {
        getEntityManager().flush();

    }

    public boolean isEntityManaged(T entity) {
        return getEntityManager().contains(entity);
    }

    public Long getNextId() {
        SequenceNumber sequence = getEntityManager().find(SequenceNumber.class, 1L); // Always 1 for the single row
        if (sequence == null) {
            sequence = new SequenceNumber();
            sequence.setLastUsedId(0L);
            getEntityManager().persist(sequence);
        }
        Long nextId = sequence.getLastUsedId() + 1;
        sequence.setLastUsedId(nextId);
        getEntityManager().merge(sequence);
        return nextId;
    }

    public List<Object> findObjects(String jpql, Map<String, Object> parameters) {
        return findObjects(jpql, parameters, TemporalType.DATE);
    }

    public List<Object> findObjects(String jpql, Map<String, Object> parameters, TemporalType tt) {
        TypedQuery<Object> qry = getEntityManager().createQuery(jpql, Object.class);
        Set s = parameters.entrySet();
        Iterator it = s.iterator();
        while (it.hasNext()) {
            Map.Entry m = (Map.Entry) it.next();
            Object pVal = m.getValue();
            String pPara = (String) m.getKey();
            if (pVal instanceof Date) {
                Date pDate = (Date) pVal;
                qry.setParameter(pPara, pDate, TemporalType.DATE);
            } else {
                qry.setParameter(pPara, pVal);
            }
        }
        try {
            return qry.getResultList();
        } catch (Exception e) {
            return null;
        }
    }

    public List<Long> findLongList(String jpql, Map<String, Object> parameters) {
        return findLongList(jpql, parameters, TemporalType.DATE);
    }

    public List<Long> findLongList(String jpql, Map<String, Object> parameters, TemporalType tt) {
        TypedQuery<Long> qry = getEntityManager().createQuery(jpql, Long.class);
        Set s = parameters.entrySet();
        Iterator it = s.iterator();
        while (it.hasNext()) {
            Map.Entry m = (Map.Entry) it.next();
            Object pVal = m.getValue();
            String pPara = (String) m.getKey();
            if (pVal instanceof Date) {
                Date pDate = (Date) pVal;
                qry.setParameter(pPara, pDate, TemporalType.DATE);
            } else {
                qry.setParameter(pPara, pVal);
            }
        }
        try {
            return qry.getResultList();
        } catch (Exception e) {
            return null;
        }
    }

    public List<T> findRange(int[] range) {
        javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        cq.select(cq.from(entityClass));
        javax.persistence.Query q = getEntityManager().createQuery(cq);
        q.setMaxResults(range[1] - range[0] + 1);
        q.setFirstResult(range[0]);
        return q.getResultList();
    }

    // Comment by Dr M H B Ariyaratne with assistance from ChatGPT from OpenAI
    public T findFirstByJpql(String jpql, Map<String, Object> parameters) {
        try {
            System.out.println("Entering findFirstByJpql method");
            System.out.println("JPQL: " + jpql);

            TypedQuery<T> qry = getEntityManager().createQuery(jpql, entityClass);
            qry.setMaxResults(1);

            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                String paramName = entry.getKey();
                Object paramValue = entry.getValue();
                System.out.println("Parameter: " + paramName + ", Value: " + paramValue);

                if (paramValue instanceof Date) {
                    qry.setParameter(paramName, (Date) paramValue, TemporalType.DATE);
                } else {
                    qry.setParameter(paramName, paramValue);
                }
            }

            T result = qry.getSingleResult();
            System.out.println("Result found: " + (result != null ? result.toString() : "null"));
            return result;
        } catch (NoResultException nre) {
            System.out.println("No result found");
            return null;
        } catch (Exception e) {
            System.out.println("Exception in findFirstByJpql: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public AbstractFacade(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected abstract EntityManager getEntityManager();

// Comment indicating the code was done by Dr M H B Ariyaratne with assistance from ChatGPT from OpenAI
    public void create(T entity) {
        if (entity.getId() == null) {
            entity.setId(getNextId());
        }
        getEntityManager().persist(entity);
        // Uncommenting flush if you want to immediately sync with the database
        // getEntityManager().flush();
    }

    public void refresh(T entity) {
        getEntityManager().refresh(entity);
    }

// Comment by Dr M H B Ariyaratne with assistance from ChatGPT from OpenAI
    public void edit(T entity) {
        if (entity == null) {
            return;
        }
        EntityManager em = getEntityManager();
        if (((Identifiable) entity).getId() == null) {
            create(entity);
        } else {
            if (em.contains(entity)) {
                em.merge(entity);
            } else {
                T managedEntity = em.find((Class<T>) entity.getClass(), ((Identifiable) entity).getId());
                if (managedEntity != null) {
                    em.merge(entity);
                } else {
                    create(entity);
                }
            }
        }
        em.flush();
    }

    public void remove(T entity) {
        getEntityManager().remove(getEntityManager().merge(entity));
    }

    // Comment by Dr M H B Ariyaratne with assistance from ChatGPT from OpenAI
    public T find(Object id) {
        try {
            System.out.println("Attempting to find entity with ID: " + id);
            T entity = getEntityManager().find(entityClass, id);
            System.out.println("Entity found: " + entity);
            return entity;
        } catch (Exception e) {
            System.out.println("Exception while finding entity: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public List<T> findAll(boolean withoutRetired) {
        javax.persistence.criteria.CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        javax.persistence.criteria.CriteriaQuery<T> cq = cb.createQuery(entityClass);
        javax.persistence.criteria.Root<T> rt = cq.from(entityClass);
        ParameterExpression<String> p = cb.parameter(String.class);
        Predicate predicateRetired = cb.equal(rt.<Boolean>get("retired"), false);
        if (withoutRetired) {
            cq.where(predicateRetired);
        }
        return getEntityManager().createQuery(cq).getResultList();
    }

    public List<T> findAll() {
        javax.persistence.criteria.CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        javax.persistence.criteria.CriteriaQuery<T> cq = cb.createQuery(entityClass);
        javax.persistence.criteria.Root<T> rt = cq.from(entityClass);
        return getEntityManager().createQuery(cq).getResultList();
    }

    public List<T> findAll(String fieldName) {
        return findAll(fieldName, "", false);
    }

    public List<T> findAll(String fieldName, boolean withoutRetired) {
        return findAll(fieldName, "", withoutRetired);
    }

    public List<T> findAll(String fieldName, String fieldValue) {
        return findAll(fieldName, fieldValue, false);
    }

    public List<T> findByJpql(String jpql) {
        TypedQuery<T> qry = getEntityManager().createQuery(jpql, entityClass);
        return qry.getResultList();
    }

    public List<T> findByJpql(String jpql, int maxResults) {
        TypedQuery<T> qry = getEntityManager().createQuery(jpql, entityClass);
        qry.setMaxResults(maxResults);
        return qry.getResultList();
    }

    // Comment by Dr M H B Ariyaratne with assistance from ChatGPT from OpenAI
    public List<T> findByJpql(String jpql, Map<String, Object> parameters) {
        try {
            System.out.println("Entering findByJpql method");
            System.out.println("JPQL: " + jpql);

            TypedQuery<T> qry = getEntityManager().createQuery(jpql, entityClass);
            Set s = parameters.entrySet();
            Iterator it = s.iterator();

            while (it.hasNext()) {
                Map.Entry m = (Map.Entry) it.next();
                String pPara = (String) m.getKey();
                System.out.println("Parameter: " + pPara + ", Value: " + m.getValue());

                if (m.getValue() instanceof Date) {
                    Date pVal = (Date) m.getValue();
                    qry.setParameter(pPara, pVal, TemporalType.DATE);
                } else {
                    Object pVal = (Object) m.getValue();
                    qry.setParameter(pPara, pVal);
                }
            }

            List<T> results = qry.getResultList();
            System.out.println("Results found: " + (results != null ? results.size() : "null"));
            return results;
        } catch (Exception e) {
            System.out.println("Exception in findByJpql: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public List<T> findByJpql(String jpql, Map<String, Object> parameters, boolean withoutCache) {

        TypedQuery<T> qry = getEntityManager().createQuery(jpql, entityClass);

        Set s = parameters.entrySet();
        Iterator it = s.iterator();

        while (it.hasNext()) {
            Map.Entry m = (Map.Entry) it.next();
            String pPara = (String) m.getKey();
            if (m.getValue() instanceof Date) {
                Date pVal = (Date) m.getValue();
                qry.setParameter(pPara, pVal, TemporalType.DATE);

            } else {
                Object pVal = (Object) m.getValue();
                qry.setParameter(pPara, pVal);

            }
        }
        if (withoutCache) {
            qry.setHint(QueryHints.CACHE_USAGE, CacheUsage.DoNotCheckCache);
        }

        return qry.getResultList();
    }

    public List<T> findByJpql(String jpql, Map<String, Object> parameters, TemporalType tt) {
        TypedQuery<T> qry = getEntityManager().createQuery(jpql, entityClass);
        Set s = parameters.entrySet();
        Iterator it = s.iterator();
        while (it.hasNext()) {
            Map.Entry m = (Map.Entry) it.next();
            Object pVal = m.getValue();
            String pPara = (String) m.getKey();
            if (pVal instanceof Date) {
                Date d = (Date) pVal;
                qry.setParameter(pPara, d, tt);
            } else {
                qry.setParameter(pPara, pVal);
            }

        }
        return qry.getResultList();
    }

    public List<Object[]> findObjectsArrayByJpql(String jpql, Map<String, Object> parameters, TemporalType tt) {
        TypedQuery<Object[]> qry = getEntityManager().createQuery(jpql, Object[].class);
        Set s = parameters.entrySet();
        Iterator it = s.iterator();
        while (it.hasNext()) {
            Map.Entry m = (Map.Entry) it.next();
            Object pVal = m.getValue();
            String pPara = (String) m.getKey();
            if (pVal instanceof Date) {
                Date d = (Date) pVal;
                qry.setParameter(pPara, d, tt);
            } else {
                qry.setParameter(pPara, pVal);
            }

        }
        return qry.getResultList();
    }

    public List<Object> findObjectByJpql(String jpql, Map<String, Object> parameters, TemporalType tt) {
        TypedQuery<Object> qry = getEntityManager().createQuery(jpql, Object.class);
        Set s = parameters.entrySet();
        Iterator it = s.iterator();
        while (it.hasNext()) {
            Map.Entry m = (Map.Entry) it.next();
            Object pVal = m.getValue();
            String pPara = (String) m.getKey();
            if (pVal instanceof Date) {
                Date d = (Date) pVal;
                qry.setParameter(pPara, d, tt);
            } else {
                qry.setParameter(pPara, pVal);
            }

        }
        return qry.getResultList();
    }

    public Object findFirstObjectByJpql(String jpql, Map<String, Object> parameters, TemporalType tt) {
        TypedQuery<Object> qry = getEntityManager().createQuery(jpql, Object.class);
        Set s = parameters.entrySet();
        Iterator it = s.iterator();
        while (it.hasNext()) {
            Map.Entry m = (Map.Entry) it.next();
            Object pVal = m.getValue();
            String pPara = (String) m.getKey();
            if (pVal instanceof Date) {
                Date d = (Date) pVal;
                qry.setParameter(pPara, d, tt);
            } else {
                qry.setParameter(pPara, pVal);
            }

        }

        qry.setMaxResults(1);
        if (!qry.getResultList().isEmpty()) {
            return qry.getResultList().get(0);
        } else {
            return null;
        }
    }

    public Object[] findObjectListByJpql(String jpql, Map<String, Object> parameters, TemporalType tt) {
        TypedQuery<Object[]> qry = getEntityManager().createQuery(jpql, Object[].class);
        Set s = parameters.entrySet();
        Iterator it = s.iterator();
        while (it.hasNext()) {
            Map.Entry m = (Map.Entry) it.next();
            Object pVal = m.getValue();
            String pPara = (String) m.getKey();
            if (pVal instanceof Date) {
                Date d = (Date) pVal;
                qry.setParameter(pPara, d, tt);
            } else {
                qry.setParameter(pPara, pVal);
            }

        }
        return qry.getSingleResult();
    }

    public List<Date> findDateListByJpql(String jpql, Map<String, Object> parameters) {
        return findDateListByJpql(jpql, parameters, TemporalType.DATE);
    }

    public List<Date> findDateListByJpql(String jpql, Map<String, Object> parameters, TemporalType tt) {
        TypedQuery<Date> qry = getEntityManager().createQuery(jpql, Date.class);
        Set s = parameters.entrySet();
        Iterator it = s.iterator();
        while (it.hasNext()) {
            Map.Entry m = (Map.Entry) it.next();
            Object pVal = m.getValue();
            String pPara = (String) m.getKey();
            if (pVal instanceof Date) {
                Date d = (Date) pVal;
                qry.setParameter(pPara, d, tt);
            } else {
                qry.setParameter(pPara, pVal);
            }

        }
        return qry.getResultList();
    }

    public List<Object> findObjectByJpql(String jpql) {
        TypedQuery<Object> qry = getEntityManager().createQuery(jpql, Object.class);
        return qry.getResultList();
    }

    public Object[] findAggregateModified(String jpql, Map<String, Object> parameters, TemporalType tt) {
        TypedQuery<Object[]> qry = getEntityManager().createQuery(jpql, Object[].class);
        setParameterObjectList(qry, parameters, tt);

        try {
            Object[] obj = qry.getSingleResult();

            for (Object o : obj) {
                if (o == null) {
                    return null;
                }
            }

            return obj;
        } catch (Exception e) {
            return null;
        }
    }

    public double findDoubleByJpql(String jpql, Map<String, Object> parameters, TemporalType tt) {
        TypedQuery<Double> qry = (TypedQuery<Double>) getEntityManager().createQuery(jpql);
        Set s = parameters.entrySet();
        Iterator it = s.iterator();
        while (it.hasNext()) {
            Map.Entry m = (Map.Entry) it.next();
            Object pVal = m.getValue();
            String pPara = (String) m.getKey();
            if (pVal instanceof Date) {

                Date d = (Date) pVal;
                qry.setParameter(pPara, d, tt);
            } else {

                qry.setParameter(pPara, pVal);
            }

        }
        try {
            return (double) qry.getSingleResult();
        } catch (Exception e) {
            return 0.0;
        }
    }

    public double findDoubleByJpql(String jpql) {
        TypedQuery<Double> qry = (TypedQuery<Double>) getEntityManager().createQuery(jpql);

        try {
            return (double) qry.getSingleResult();
        } catch (Exception e) {
            return 0.0;
        }
    }

    public Date findDateByJpql(String jpql, Map<String, Object> parameters, TemporalType tt) {
        TypedQuery<Date> qry = (TypedQuery<Date>) getEntityManager().createQuery(jpql);
        Set s = parameters.entrySet();
        Iterator it = s.iterator();
        while (it.hasNext()) {
            Map.Entry m = (Map.Entry) it.next();
            Object pVal = m.getValue();
            String pPara = (String) m.getKey();
            if (pVal instanceof Date) {

                Date d = (Date) pVal;
                qry.setParameter(pPara, d, tt);
            } else {

                qry.setParameter(pPara, pVal);
            }

        }
        try {
            return (Date) qry.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public long findLongByJpql(String jpql, Map<String, Object> parameters) {
        return findLongByJpql(jpql, parameters, TemporalType.DATE);
    }

    public long findLongByJpql(String jpql, Map<String, Object> parameters, int maxResults) {
        TypedQuery<Long> qry = (TypedQuery<Long>) getEntityManager().createQuery(jpql);
        Set s = parameters.entrySet();
        Iterator it = s.iterator();
        while (it.hasNext()) {
            Map.Entry m = (Map.Entry) it.next();
            Object pVal = m.getValue();
            String pPara = (String) m.getKey();
            if (pVal instanceof Date) {
                Date d = (Date) pVal;
                qry.setParameter(pPara, d, TemporalType.TIMESTAMP);
            } else {
                qry.setParameter(pPara, pVal);
            }
            qry.setMaxResults(maxResults);
        }
        try {
            return (long) qry.getSingleResult();
        } catch (Exception e) {

            return 0l;
        }
    }

    public long findLongByJpql(String jpql, Map<String, Object> parameters, TemporalType tt) {
        TypedQuery<Long> qry = (TypedQuery<Long>) getEntityManager().createQuery(jpql);
        Set s = parameters.entrySet();
        Iterator it = s.iterator();
        while (it.hasNext()) {
            Map.Entry m = (Map.Entry) it.next();
            Object pVal = m.getValue();
            String pPara = (String) m.getKey();
            if (pVal instanceof Date) {
                Date d = (Date) pVal;
                qry.setParameter(pPara, d, tt);
            } else {
                qry.setParameter(pPara, pVal);
            }
        }
        try {
            return (long) qry.getSingleResult();
        } catch (Exception e) {

            return 0l;
        }
    }

    public double findDoubleByJpql(String jpql, Map<String, Object> parameters) {
        return findDoubleByJpql(jpql, parameters, TemporalType.DATE);
    }

    public Date findDateByJpql(String jpql, Map<String, Object> parameters) {
        return findDateByJpql(jpql, parameters, TemporalType.DATE);
    }

    public List<T> findByJpql(String jpql, Map<String, Object> parameters, TemporalType tt, int maxRecords) {
        TypedQuery<T> qry = getEntityManager().createQuery(jpql, entityClass);
        Set s = parameters.entrySet();
        Iterator it = s.iterator();
        while (it.hasNext()) {
            Map.Entry m = (Map.Entry) it.next();
            Object pVal = m.getValue();
            String pPara = (String) m.getKey();
            if (pVal instanceof Date) {
                Date d = (Date) pVal;
                qry.setParameter(pPara, d, tt);
            } else {
                qry.setParameter(pPara, pVal);
            }

        }
        qry.setMaxResults(maxRecords);

        return qry.getResultList();
    }

    public List<T> findByJpqlWithoutCache(String jpql, Map<String, Object> parameters, TemporalType tt, int maxRecords) {
        TypedQuery<T> qry = getEntityManager().createQuery(jpql, entityClass);
        Set s = parameters.entrySet();
        Iterator it = s.iterator();
        while (it.hasNext()) {
            Map.Entry m = (Map.Entry) it.next();
            Object pVal = m.getValue();
            String pPara = (String) m.getKey();
            if (pVal instanceof Date) {
                Date d = (Date) pVal;
                qry.setParameter(pPara, d, tt);
            } else {
                qry.setParameter(pPara, pVal);
            }
        }
        qry.setMaxResults(maxRecords);
        qry.setHint("javax.persistence.cache.storeMode", "REFRESH");
        return qry.getResultList();
    }

    public List<T> findByJpqlWithoutCache(String jpql, Map<String, Object> parameters, TemporalType tt) {
        TypedQuery<T> qry = getEntityManager().createQuery(jpql, entityClass);
        Set s = parameters.entrySet();
        Iterator it = s.iterator();
        while (it.hasNext()) {
            Map.Entry m = (Map.Entry) it.next();
            Object pVal = m.getValue();
            String pPara = (String) m.getKey();
            if (pVal instanceof Date) {
                Date d = (Date) pVal;
                qry.setParameter(pPara, d, tt);
            } else {
                qry.setParameter(pPara, pVal);
            }

        }
//        qry.setMaxResults(maxRecords);
        qry.setHint("javax.persistence.cache.storeMode", "REFRESH");
        return qry.getResultList();
    }

    public List<T> findByJpqlWithoutCache(String jpql, Map<String, Object> parameters) {
        TypedQuery<T> qry = getEntityManager().createQuery(jpql, entityClass);
        Set s = parameters.entrySet();
        Iterator it = s.iterator();
        while (it.hasNext()) {
            Map.Entry m = (Map.Entry) it.next();
            Object pVal = m.getValue();
            String pPara = (String) m.getKey();
            if (pVal instanceof Date) {
                Date d = (Date) pVal;
                qry.setParameter(pPara, d, TemporalType.DATE);
            } else {
                qry.setParameter(pPara, pVal);
            }

        }
        qry.setHint("javax.persistence.cache.storeMode", "REFRESH");
        return qry.getResultList();
    }

    public List<T> findByJpql(String jpql, Map<String, Object> parameters, int maxRecords) {
        return AbstractFacade.this.findByJpql(jpql, parameters, TemporalType.DATE, maxRecords);
    }

    public Long countByJpql(String sql) {
        return countByJpql(sql, null, TemporalType.DATE);
    }

    public Long countByJpql(String sql, Map parameters) {
        return countByJpql(sql, parameters, TemporalType.DATE);
    }

    public Long countByJpql(String sql, Map parameters, TemporalType tt) {
        Query q = getEntityManager().createQuery(sql);
        if (parameters != null) {
            Set s = parameters.entrySet();
            Iterator it = s.iterator();
            while (it.hasNext()) {
                Map.Entry m = (Map.Entry) it.next();
                Object pVal = m.getValue();
                String pPara = (String) m.getKey();
                if (pVal instanceof Date) {
                    Date d = (Date) pVal;
                    q.setParameter(pPara, d, tt);
                } else {
                    q.setParameter(pPara, pVal);
                }
            }
        }
        Object o;
        try {
            o = q.getSingleResult();
        } catch (Exception e) {
            return null;
        }
        if (o == null) {
            return null;
        }
        if (!(o instanceof Long)) {
            return null;
        }
        return (Long) o;
    }

    public double sumByJpql(String sql, Map parameters, TemporalType tt) {
        Query q = getEntityManager().createQuery(sql);
        if (parameters != null) {
            Set s = parameters.entrySet();
            Iterator it = s.iterator();
            while (it.hasNext()) {
                Map.Entry m = (Map.Entry) it.next();
                Object pVal = m.getValue();
                String pPara = (String) m.getKey();
                if (pVal instanceof Date) {
                    Date d = (Date) pVal;
                    q.setParameter(pPara, d, tt);
                } else {
                    q.setParameter(pPara, pVal);
                }
            }
        }
        Object o;
        try {
            o = q.getSingleResult();
        } catch (Exception e) {
            return 0.0;
        }
        if (o == null) {
            return 0.0;
        }
        if (!(o instanceof Long)) {
            return 0.0;
        }
        return (double) o;
    }

    public Double sumByJpql(String sql) {
        Query q = getEntityManager().createQuery(sql);
        return (Double) q.getSingleResult();
    }

    public List<T> findAll(String fieldName, String fieldValue, boolean withoutRetired) {
        javax.persistence.criteria.CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        javax.persistence.criteria.CriteriaQuery<T> cq = cb.createQuery(entityClass);
        javax.persistence.criteria.Root<T> rt = cq.from(entityClass);
        ParameterExpression<String> p = cb.parameter(String.class);
        Predicate predicateField = cb.like(rt.<String>get(fieldName), fieldValue);
        Predicate predicateRetired = cb.equal(rt.<Boolean>get("retired"), false);
        Predicate predicateFieldRetired = cb.and(predicateField, predicateRetired);

        if (withoutRetired && !fieldValue.equals("")) {
            cq.where(predicateFieldRetired);
        } else if (withoutRetired) {
            cq.where(predicateRetired);
        } else if (!fieldValue.equals("")) {
            cq.where(predicateField);
        }

        if (!fieldName.equals("")) {
            cq.orderBy(cb.asc(rt.get(fieldName)));
        }

        return getEntityManager().createQuery(cq).getResultList();
    }

    public List<T> findExact(String fieldName, String fieldValue, boolean withoutRetired) {
        javax.persistence.criteria.CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        javax.persistence.criteria.CriteriaQuery<T> cq = cb.createQuery(entityClass);
        javax.persistence.criteria.Root<T> rt = cq.from(entityClass);
        ParameterExpression<String> p = cb.parameter(String.class);
//        Predicate predicateField = cb.like(rt.<String>get(fieldName), fieldValue);
        Predicate predicateField = cb.equal(cb.upper(rt.<String>get(fieldName)), fieldValue.toLowerCase());
        Predicate predicateRetired = cb.equal(rt.<Boolean>get("retired"), false);
        Predicate predicateFieldRetired = cb.and(predicateField, predicateRetired);

        if (withoutRetired && !fieldValue.equals("")) {
            cq.where(predicateFieldRetired);
        } else if (withoutRetired) {
            cq.where(predicateRetired);
        } else if (!fieldValue.equals("")) {
            cq.where(predicateField);
        }

        if (!fieldName.equals("")) {
            cq.orderBy(cb.asc(rt.get(fieldName)));
        }

        return getEntityManager().createQuery(cq).getResultList();
    }

    public List<T> findContains(String fieldName, String fieldValue) {
        javax.persistence.criteria.CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        javax.persistence.criteria.CriteriaQuery<T> cq = cb.createQuery(entityClass);
        javax.persistence.criteria.Root<T> rt = cq.from(entityClass);
        ParameterExpression<String> p = cb.parameter(String.class);
//        Predicate predicateField = cb.like(rt.<String>get(fieldName), fieldValue);
        Predicate predicateField = cb.like(cb.upper(rt.<String>get(fieldName)), "*" + fieldValue.toLowerCase());
        //    Predicate predicateRetired = cb.equal(rt.<Boolean>get("retired"), withoutRetired);
        //    Predicate predicateFieldRetired = cb.and(predicateField, predicateRetired);
        //    (cb.like(pet.get(Pet_.name), "*do"));

        if (!fieldValue.equals("")) {
            cq.where(predicateField);
        }

        if (!fieldName.equals("")) {
            cq.orderBy(cb.asc(rt.get(fieldName)));
        }

        return getEntityManager().createQuery(cq).getResultList();
    }

    public T findByField(String fieldName, String fieldValue, boolean withoutRetired) {
        List<T> lstAll = findExact(fieldName, fieldValue, true);

        if (lstAll.isEmpty()) {

            return null;
        } else {

            return lstAll.get(0);
        }
    }

    public String findByFieldContains(String fieldName, String fieldValue) {
        List<T> lstAll = findContains(fieldName, fieldValue);

        if (lstAll.isEmpty()) {

            return "";
        } else {

            return lstAll.get(0).toString();
        }
    }

    public T findFirstByJpql(String jpql) {
        TypedQuery<T> qry = getEntityManager().createQuery(jpql, entityClass);
        qry.setMaxResults(1);
        try {
            return qry.getResultList().get(0);
        } catch (Exception e) {

            return null;
        }
    }

    public T findFirstByJpql(String jpql, Map<String, Object> parameters, TemporalType tt) {
        TypedQuery<T> qry = getEntityManager().createQuery(jpql, entityClass);
        Set s = parameters.entrySet();
        Iterator it = s.iterator();
        qry.setMaxResults(1);
        while (it.hasNext()) {
            Map.Entry m = (Map.Entry) it.next();
            Object pVal = m.getValue();
            String pPara = (String) m.getKey();
            if (pVal instanceof Date) {
                Date d = (Date) pVal;
                qry.setParameter(pPara, d, tt);
            } else {
                qry.setParameter(pPara, pVal);
            }

        }

        if (!qry.getResultList().isEmpty()) {
            return qry.getResultList().get(0);
        } else {
            return null;
        }
    }

    public <U> List<T> testMethod(U[] a, Collection<U> all) {
        List<T> myList = new ArrayList<T>();
        return myList;
    }

    public <U> List<T> findAll(String fieldName, int searchID, boolean withoutRetired) {

//        final long userId,
//    final long contactNumber){
//
//    final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
//    final CriteriaQuery<TaUser> query = cb.createQuery(TaUser.class);
//    final Root<TaUser> root = query.from(TaUser.class);
//    query
//        .where(cb.and(
//            cb.equal(root.get("userId"), userId),
//            cb.equal(root.get("taContact").get("contactNumber"), contactNumber)
//        ));
//    return entityManager.createQuery(query).getSingleResult();
        javax.persistence.criteria.CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        javax.persistence.criteria.CriteriaQuery<T> cq = cb.createQuery(entityClass);
        javax.persistence.criteria.Root<T> rt = cq.from(entityClass);

        if (withoutRetired) {
            cq.where(cb.and(cb.equal(rt.get("retired"), false)),
                    (cb.equal(rt.get(fieldName).get("id"), searchID)));
        } else {
            cq.where(cb.equal(rt.get("retired"), false));
        }

        return getEntityManager().createQuery(cq).getResultList();
    }

    public int count() {
        javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        javax.persistence.criteria.Root<T> rt = cq.from(entityClass);
        cq.select(getEntityManager().getCriteriaBuilder().count(rt));
        javax.persistence.Query q = getEntityManager().createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }

    public Double findAggregateDbl(String strJQL) {
        Query q = getEntityManager().createQuery(strJQL);
        Double temd;
        try {
            temd = (Double) q.getSingleResult();
            if (temd == null) {
                temd = 0.0;
            }
        } catch (Exception e) {

            temd = 0.0;
        }
        return temd;
    }

    public Long findAggregateLong(String strJQL) {
        Query q = getEntityManager().createQuery(strJQL);
        Long temd;
        try {
            temd = (Long) q.getSingleResult();
            if (temd == null) {
                temd = 0L;
            }
        } catch (Exception e) {
            temd = 0L;
        }
        return temd;
    }

    public Long findLongByJpql(String strJQL) {
        Query q = getEntityManager().createQuery(strJQL);
        try {
            return (Long) q.getSingleResult();
        } catch (Exception e) {
            return 0l;
        }
    }

    public List<String> findString(String strJQL) {
        Query q = getEntityManager().createQuery(strJQL);
        try {
            return q.getResultList();
        } catch (Exception e) {

            return null;
        }
    }

    public List<String> findString(String strJQL, int maxRecords) {
        Query q = getEntityManager().createQuery(strJQL);
        q.setMaxResults(maxRecords);
        try {
            return q.getResultList();
        } catch (Exception e) {

            return null;
        }
    }

    public List<String> findString(String strJQL, Map map) {
        return findString(strJQL, map, TemporalType.DATE);
    }

    public List<String> findString(String strJQL, Map map, TemporalType tt, int noOfRows) {
        Query q = getEntityManager().createQuery(strJQL);
        Set s = map.entrySet();
        Iterator it = s.iterator();
        while (it.hasNext()) {
            Map.Entry m = (Map.Entry) it.next();
            String pPara = (String) m.getKey();
            if (m.getValue() instanceof Date) {
                Date pVal = (Date) m.getValue();
                q.setParameter(pPara, pVal, tt);
            } else {
                q.setParameter(pPara, m.getValue());
            }
        }
        if (noOfRows != 0) {
            q.setMaxResults(noOfRows);
        }
        try {
            return q.getResultList();
        } catch (Exception e) {

            return null;
        }
    }

    public List<String> findString(String strJQL, Map map, TemporalType tt) {
        return findString(strJQL, map, tt, 0);
    }

    public List<Object[]> findAggregates(String jpql, Map<String, Object> parameters) {
        return findAggregates(jpql, parameters, TemporalType.DATE);
    }

    public List<Object[]> findAggregates(String jpql) {
        TypedQuery<Object[]> qry = getEntityManager().createQuery(jpql, Object[].class);
        try {
            return qry.getResultList();
        } catch (Exception e) {

            return null;
        }
    }

    public List<Object[]> findAggregates(String jpql, Map<String, Object> parameters, TemporalType tt) {
        TypedQuery<Object[]> qry = getEntityManager().createQuery(jpql, Object[].class);
        Set s = parameters.entrySet();
        Iterator it = s.iterator();
        while (it.hasNext()) {
            Map.Entry m = (Map.Entry) it.next();
            Object pVal = m.getValue();
            String pPara = (String) m.getKey();
            if (pVal instanceof Date) {
                Date pDate = (Date) pVal;
                // qry.setParameter(pPara, pDate, TemporalType.DATE);
                qry.setParameter(pPara, pDate, tt);
            } else {
                qry.setParameter(pPara, pVal);
            }
        }
        try {
            return qry.getResultList();
        } catch (Exception e) {
            return null;
        }
    }

    private void setParameterObjectList(TypedQuery<Object[]> qry, Map<String, Object> parameters, TemporalType temporalType) {
        if (parameters == null) {
            return;
        }

        Set s = parameters.entrySet();
        Iterator it = s.iterator();

        while (it.hasNext()) {
            Map.Entry m = (Map.Entry) it.next();
            String pPara = (String) m.getKey();
            if (m.getValue() instanceof Date) {
                Date pVal = (Date) m.getValue();
                qry.setParameter(pPara, pVal, temporalType);
            } else {
                Object pVal = (Object) m.getValue();
                qry.setParameter(pPara, pVal);
            }
        }
    }

    public Object[] findAggregat(String jpql, Map<String, Object> parameters, TemporalType tt) {
        TypedQuery<Object[]> qry = getEntityManager().createQuery(jpql, Object[].class);
        setParameterObjectList(qry, parameters, tt);

        try {
            Object[] obj = qry.getSingleResult();

            for (Object o : obj) {
                if (o == null) {
                    return null;
                }
            }

            return obj;
        } catch (Exception e) {
            return null;
        }
    }

    public Object[] findAggregate(String jpql, Map<String, Object> parameters, TemporalType tt) {
        TypedQuery<Object[]> qry = getEntityManager().createQuery(jpql, Object[].class);
        Set s = parameters.entrySet();
        Iterator it = s.iterator();
        while (it.hasNext()) {
            Map.Entry m = (Map.Entry) it.next();
            Object pVal = m.getValue();
            String pPara = (String) m.getKey();
            if (pVal instanceof Date) {
                Date pDate = (Date) pVal;
                // qry.setParameter(pPara, pDate, TemporalType.DATE);
                qry.setParameter(pPara, pDate, tt);
            } else {
                qry.setParameter(pPara, pVal);
            }
        }
        try {
            return qry.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public Object[] findSingleAggregate(String jpql, Map<String, Object> parameters) {

        return findSingleAggregate(jpql, parameters, TemporalType.DATE);
    }

    public Object[] findSingleAggregate(String jpql, Map<String, Object> parameters, TemporalType tt) {

        TypedQuery<Object[]> qry = getEntityManager().createQuery(jpql, Object[].class);

        Set s = parameters.entrySet();

        Iterator it = s.iterator();

        while (it.hasNext()) {

            Map.Entry m = (Map.Entry) it.next();
            Object pVal = m.getValue();
            String pPara = (String) m.getKey();
            if (pVal instanceof Date) {
                Date pDate = (Date) pVal;
                qry.setParameter(pPara, pDate, TemporalType.DATE);
            } else {
                qry.setParameter(pPara, pVal);
            }

        }

        try {
            return qry.getSingleResult();
        } catch (Exception e) {

            return null;
        }
    }

    public Double findAggregateDbl(String jpql, Map<String, Date> parameters) {
        Query qry = getEntityManager().createQuery(jpql);
        Set s = parameters.entrySet();
        Iterator it = s.iterator();

        while (it.hasNext()) {
            Map.Entry m = (Map.Entry) it.next();
            Date pVal = (Date) m.getValue();
            String pPara = (String) m.getKey();
            qry.setParameter(pPara, pVal, TemporalType.DATE);

        }

        try {
            return (Double) qry.getSingleResult();
        } catch (Exception e) {

            return 0.0;
        }
    }

    public Long findAggregateLong(String jpql, Map<String, Object> parameters, TemporalType tt) {
        TypedQuery<Long> qry = getEntityManager().createQuery(jpql, Long.class);
        Set s = parameters.entrySet();
        Iterator it = s.iterator();
        while (it.hasNext()) {
            Map.Entry m = (Map.Entry) it.next();
            Object pVal = m.getValue();
            String pPara = (String) m.getKey();
            if (pVal instanceof Date) {
                Date d = (Date) pVal;
                qry.setParameter(pPara, d, tt);
            } else {
                qry.setParameter(pPara, pVal);
            }

        }

        try {
            return (Long) qry.getSingleResult();
        } catch (Exception e) {

            return 0L;
        }
    }
}
