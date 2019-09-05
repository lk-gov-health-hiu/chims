package lk.gov.health.phsp.facade;

import lk.gov.health.phsp.entity.Project;
import lk.gov.health.phsp.entity.ProjectArea;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Dr M H B Ariyaratne, buddhika.ari@gmail.com
 */
@Stateless
public class ProjectAreaFacade extends AbstractFacade<ProjectArea> {

    @PersistenceContext(unitName = "hmisPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ProjectAreaFacade() {
        super(ProjectArea.class);
    }

}
