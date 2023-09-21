package lk.gov.health.phsp.bean;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Inject;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.facade.WebUserFacade;

/**
 *
 * @author buddhika
 */
@Named(value = "sessionController")
@SessionScoped
public class SessionController implements Serializable {

    @EJB
    WebUserFacade webUserFacade;

    @Inject
    CommonController commonController;

    private WebUser loggedUser;
    private Institution loggedInstitution;
    private boolean logged;

    private String username;
    private String password;

    public SessionController() {
    }

    public void login() {
        System.out.println("login");
        String j = "SELECT u "
                + " FROM WebUser u "
                + " WHERE lower(u.name)=:name "
                + " and u.retired =:ret";
        Map m = new HashMap();
        m.put("name", username.toLowerCase());
        m.put("ret", Boolean.FALSE);
        loggedUser = webUserFacade.findFirstByJpql(j, m);
        if (loggedUser == null) {
            logged = false;
            return;
        }
        if (!commonController.matchPassword(password, loggedUser.getWebUserPassword())) {
            logged = false;
            return;
        }
        logged = true;

        loggedInstitution = loggedUser.getInstitution();
    }

    public WebUser getLoggedUser() {
        return loggedUser;
    }

    public void setLoggedUser(WebUser loggedUser) {
        this.loggedUser = loggedUser;
    }

    public Institution getLoggedInstitution() {
        return loggedInstitution;
    }

    public void setLoggedInstitution(Institution loggedInstitution) {
        this.loggedInstitution = loggedInstitution;
    }

    public boolean isLogged() {
        return logged;
    }

    public void setLogged(boolean logged) {
        this.logged = logged;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
