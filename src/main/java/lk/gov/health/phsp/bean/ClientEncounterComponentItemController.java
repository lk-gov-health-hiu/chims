package lk.gov.health.phsp.bean;

import lk.gov.health.phsp.entity.ClientEncounterComponentItem;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.bean.util.JsfUtil.PersistAction;
import lk.gov.health.phsp.facade.ClientEncounterComponentItemFacade;

import java.io.Serializable;
import java.util.Date;
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

@Named("clientEncounterComponentItemController")
@SessionScoped
public class ClientEncounterComponentItemController implements Serializable {

    @EJB
    private lk.gov.health.phsp.facade.ClientEncounterComponentItemFacade ejbFacade;
    @Inject
    private WebUserController webUserController;
    private List<ClientEncounterComponentItem> items = null;
    private ClientEncounterComponentItem selected;

    public ClientEncounterComponentItemController() {
    }

    public ClientEncounterComponentItem getSelected() {
        return selected;
    }

    public void setSelected(ClientEncounterComponentItem selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private ClientEncounterComponentItemFacade getFacade() {
        return ejbFacade;
    }

    public ClientEncounterComponentItem prepareCreate() {
        selected = new ClientEncounterComponentItem();
        initializeEmbeddableKey();
        return selected;
    }
    
     public void save(){
         save(selected);
     }
    
    public void save(ClientEncounterComponentItem i){
        if(i==null){
            return;
        }
        if(i.getId()==null){
            i.setCreatedAt(new Date());
            i.setCreatedBy(webUserController.getLoggedUser());
            getFacade().create(i);
        }else{
            i.setLastEditBy(webUserController.getLoggedUser());
            i.setLastEditeAt(new Date());
            getFacade().edit(i);
        }
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/BundleClinical").getString("ClientEncounterComponentItemCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/BundleClinical").getString("ClientEncounterComponentItemUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/BundleClinical").getString("ClientEncounterComponentItemDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<ClientEncounterComponentItem> getItems() {
        if (items == null) {
            items = getFacade().findAll();
        }
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
                    JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/BundleClinical").getString("PersistenceErrorOccured"));
                }
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/BundleClinical").getString("PersistenceErrorOccured"));
            }
        }
    }

    public ClientEncounterComponentItem getClientEncounterComponentItem(java.lang.Long id) {
        return getFacade().find(id);
    }

    public List<ClientEncounterComponentItem> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<ClientEncounterComponentItem> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public lk.gov.health.phsp.facade.ClientEncounterComponentItemFacade getEjbFacade() {
        return ejbFacade;
    }

    
    
    @FacesConverter(forClass = ClientEncounterComponentItem.class)
    public static class ClientEncounterComponentItemControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ClientEncounterComponentItemController controller = (ClientEncounterComponentItemController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "clientEncounterComponentItemController");
            return controller.getClientEncounterComponentItem(getKey(value));
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
            if (object instanceof ClientEncounterComponentItem) {
                ClientEncounterComponentItem o = (ClientEncounterComponentItem) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), ClientEncounterComponentItem.class.getName()});
                return null;
            }
        }

    }

}
