package lk.gov.health.phsp.facade;

import lk.gov.health.phsp.entity.Upload;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Dr M H B Ariyaratne, buddhika.ari@gmail.com
 */
@Stateless
public class UploadFacade extends AbstractFacade<Upload> {

    @PersistenceContext(unitName = "hmisPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public UploadFacade() {
        super(Upload.class);
    }

}
