package lk.gov.health.phsp.bean;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import lk.gov.health.phsp.entity.ApiRequest;
import lk.gov.health.phsp.facade.ApiRequestFacade;

@Named
@SessionScoped
public class ApiRequestApplicationController implements Serializable {

    @EJB
    private ApiRequestFacade ejbFacade;

    @Inject
    private WebUserController webUserController;
    @Inject
    private CommonController commonController;
    @Inject
    private UserTransactionController userTransactionController;

    // <editor-fold defaultstate="collapsed" desc="Getters and Setters">
    private ApiRequestFacade getFacade() {
        return ejbFacade;
    }

    public List<ApiRequest> getPendingProcedure() {
        String j = "select a "
                + " from ApiRequest a "
                + " where a.retired=:ret "
                + " and a.convaied=:con"
                + " order by a.id";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("con", true);
        return getFacade().findByJpql(j, m);
    }

    public void saveApiRequests(ApiRequest p) {
        if (p == null) {
            return;
        }
        if (p.getId() == null) {
            p.setCreatedAt(new Date());
            p.setCreatedBy(webUserController.getLoggedUser());
            getFacade().create(p);
        } else {
            getFacade().edit(p);
        }
    }

    public ApiRequestFacade getEjbFacade() {
        return ejbFacade;
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public void setWebUserController(WebUserController webUserController) {
        this.webUserController = webUserController;
    }

    public CommonController getCommonController() {
        return commonController;
    }

    public void setCommonController(CommonController commonController) {
        this.commonController = commonController;
    }

    public UserTransactionController getUserTransactionController() {
        return userTransactionController;
    }

    public void setUserTransactionController(UserTransactionController userTransactionController) {
        this.userTransactionController = userTransactionController;
    }

}
