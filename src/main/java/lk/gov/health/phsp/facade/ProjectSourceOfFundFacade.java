package lk.gov.health.phsp.facade;

import lk.gov.health.phsp.entity.ProjectSourceOfFund;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Dr M H B Ariyaratne, buddhika.ari@gmail.com
 */
@Stateless
public class ProjectSourceOfFundFacade extends AbstractFacade<ProjectSourceOfFund> {

    @PersistenceContext(unitName = "hmisPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ProjectSourceOfFundFacade() {
        super(ProjectSourceOfFund.class);
    }

}
