package lk.gov.health.phsp.facade;

import lk.gov.health.phsp.entity.Encounter;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Dr M H B Ariyaratne, buddhika.ari@gmail.com
 */
@Stateless
public class EncounterFacade extends AbstractFacade<Encounter> {

    @PersistenceContext(unitName = "hmisPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public EncounterFacade() {
        super(Encounter.class);
    }

}
