package lk.gov.health.phsp.bean;

import lk.gov.health.phsp.entity.DemoAccount;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.bean.util.JsfUtil.PersistAction;
import lk.gov.health.phsp.facade.DemoAccountFacade;

import java.io.Serializable;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

@Named("demoAccountController")
@SessionScoped
public class DemoAccountController implements Serializable {

    @EJB
    private lk.gov.health.phsp.facade.DemoAccountFacade ejbFacade;
    @Inject
    WebUserController webUserController;
    @Inject
    private UserTransactionController userTransactionController;
    private List<DemoAccount> items = null;
    private DemoAccount selected;

    public String loginUseingDemoAccount() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Select a demo account to log");
        }
        webUserController.setUserName(selected.getUserName());
        webUserController.setPassword(selected.getPassword());
        userTransactionController.recordTransaction("Login Useing DemoAccount");
        return webUserController.login();
    }

    public DemoAccountController() {
    }

    public DemoAccount getSelected() {
        return selected;
    }

    public void setSelected(DemoAccount selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private DemoAccountFacade getFacade() {
        return ejbFacade;
    }

    public DemoAccount prepareCreate() {
        selected = new DemoAccount();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/BundleDemo").getString("DemoAccountCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/BundleDemo").getString("DemoAccountUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/BundleDemo").getString("DemoAccountDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<DemoAccount> getItems() {
//        if (items == null) {
//            items = getFacade().findAll();
//        }
        return items;
    }

    private void persist(PersistAction persistAction, String successMessage) {
        if (selected != null) {
            setEmbeddableKeys();
            try {
                if (persistAction != PersistAction.DELETE) {
                    getFacade().edit(selected);
                } else {
                    getFacade().remove(selected);
                }
                JsfUtil.addSuccessMessage(successMessage);
            } catch (EJBException ex) {
                String msg = "";
                Throwable cause = ex.getCause();
                if (cause != null) {
                    msg = cause.getLocalizedMessage();
                }
                if (msg.length() > 0) {
                    JsfUtil.addErrorMessage(msg);
                } else {
                    JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/BundleDemo").getString("PersistenceErrorOccured"));
                }
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/BundleDemo").getString("PersistenceErrorOccured"));
            }
        }
    }

    public DemoAccount getDemoAccount(java.lang.Long id) {
        return getFacade().find(id);
    }

    public List<DemoAccount> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<DemoAccount> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    @FacesConverter(forClass = DemoAccount.class)
    public static class DemoAccountControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            DemoAccountController controller = (DemoAccountController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "demoAccountController");
            return controller.getDemoAccount(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof DemoAccount) {
                DemoAccount o = (DemoAccount) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), DemoAccount.class.getName()});
                return null;
            }
        }

    }

}
