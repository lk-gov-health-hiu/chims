/*
 * Open Hospital Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */

package lk.gov.health.phsp.facade;


import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import lk.gov.health.phsp.entity.ApiKey;

/**
 *
 * @author Sniper 619
 */
@Stateless
public class ApiKeyFacade extends AbstractFacade<ApiKey> {
    @PersistenceContext(unitName = "hmisPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        if(em == null){}return em;
    }

    public ApiKeyFacade() {
        super(ApiKey.class);
    }
    
}
