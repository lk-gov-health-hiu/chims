package lk.gov.health.phsp.facade;

import lk.gov.health.phsp.entity.ClientEncounterComponent;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Dr M H B Ariyaratne, buddhika.ari@gmail.com
 */
@Stateless
public class ProjectInstitutionFacade extends AbstractFacade<ClientEncounterComponent> {

    @PersistenceContext(unitName = "hmisPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ProjectInstitutionFacade() {
        super(ClientEncounterComponent.class);
    }

}
