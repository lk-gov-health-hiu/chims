package lk.gov.health.phsp.facade;

import lk.gov.health.phsp.entity.EncounterFormItem;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Dr M H B Ariyaratne, buddhika.ari@gmail.com
 */
@Stateless
public class ProjectInstitutionFacade extends AbstractFacade<EncounterFormItem> {

    @PersistenceContext(unitName = "hmisPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ProjectInstitutionFacade() {
        super(EncounterFormItem.class);
    }

}
