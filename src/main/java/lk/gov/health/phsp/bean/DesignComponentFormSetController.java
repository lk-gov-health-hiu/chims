package lk.gov.health.phsp.bean;

// <editor-fold defaultstate="collapsed" desc="Imports">
import lk.gov.health.phsp.entity.DesignComponentFormSet;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.bean.util.JsfUtil.PersistAction;
import lk.gov.health.phsp.facade.DesignComponentFormSetFacade;

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
// </editor-fold>

@Named("designComponentFormSetController")
@SessionScoped
public class DesignComponentFormSetController implements Serializable {

    // <editor-fold defaultstate="collapsed" desc="EJBs">
    @EJB
    private lk.gov.health.phsp.facade.DesignComponentFormSetFacade ejbFacade;
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Controllers">
    @Inject
    DesignComponentFormController designComponentFormController;
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Class Variables">
    private List<DesignComponentFormSet> items = null;
    private List<DesignComponentFormSet> insItems = null;
    private DesignComponentFormSet selected;

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Constructors">
    public DesignComponentFormSetController() {
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Main Functions">
    public String toAddFormsForTheSelectedSet() {
        designComponentFormController.setDesignComponentFormSet(selected);
        designComponentFormController.fillFormsofTheSelectedSet();
        designComponentFormController.getAddingForm();
        return "/designComponentFormSet/manage_forms";
    }
    
    public List<DesignComponentFormSet> fillInsItems(){
        fill
    }
    
    public List<DesignComponentFormSet> fillInsItems(List insLst){
        return null;
    } 

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Default Functions">
    public DesignComponentFormSet prepareCreate() {
        selected = new DesignComponentFormSet();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/BundleClinical").getString("DesignComponentFormSetCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/BundleClinical").getString("DesignComponentFormSetUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/BundleClinical").getString("DesignComponentFormSetDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
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

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Getters & Setters">
    
    
    public DesignComponentFormSet getSelected() {
        return selected;
    }

    public void setSelected(DesignComponentFormSet selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private DesignComponentFormSetFacade getFacade() {
        return ejbFacade;
    }

    public List<DesignComponentFormSet> getItems() {
        if (items == null) {
            items = getFacade().findAll();
        }
        return items;
    }

    public DesignComponentFormSet getDesignComponentFormSet(java.lang.Long id) {
        return getFacade().find(id);
    }

    public List<DesignComponentFormSet> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<DesignComponentFormSet> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Converter">

    @FacesConverter(forClass = DesignComponentFormSet.class)
    public static class DesignComponentFormSetControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            DesignComponentFormSetController controller = (DesignComponentFormSetController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "designComponentFormSetController");
            return controller.getDesignComponentFormSet(getKey(value));
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
            if (object instanceof DesignComponentFormSet) {
                DesignComponentFormSet o = (DesignComponentFormSet) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), DesignComponentFormSet.class.getName()});
                return null;
            }
        }

    }
    // </editor-fold>

}
