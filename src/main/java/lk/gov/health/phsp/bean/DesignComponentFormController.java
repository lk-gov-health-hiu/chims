package lk.gov.health.phsp.bean;

import lk.gov.health.phsp.entity.DesignComponentForm;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.bean.util.JsfUtil.PersistAction;
import lk.gov.health.phsp.facade.DesignComponentFormFacade;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import lk.gov.health.phsp.entity.DesignComponentFormSet;

@Named("designComponentFormController")
@SessionScoped
public class DesignComponentFormController implements Serializable {

    @EJB
    private lk.gov.health.phsp.facade.DesignComponentFormFacade ejbFacade;
    private List<DesignComponentForm> items = null;
    private DesignComponentForm selected;
    private List<DesignComponentForm> formsOfTheSelectedSet = null;
    private DesignComponentForm addingForm;
    private DesignComponentForm removingForm;

    private DesignComponentFormSet designComponentFormSet;
    
    private void fillFormsofTheSelectedSet(){
        if(designComponentFormSet==null){
            formsOfTheSelectedSet= new ArrayList<>();
            return;
        }
        String j = "Select f from DesignComponentForm f "
                + "where f.retired=false "
                + " and f.parentComponent=:pc "
                + " order by f.id";
        Map m =new HashMap();
        m.put("pc", designComponentFormSet);
        
    }
    
    public DesignComponentFormController() {
    }

    public DesignComponentForm getSelected() {
        return selected;
    }

    public void setSelected(DesignComponentForm selected) {
        this.selected = selected;
    }
    
    

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private DesignComponentFormFacade getFacade() {
        return ejbFacade;
    }

    public DesignComponentForm prepareCreate() {
        selected = new DesignComponentForm();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/BundleClinical").getString("DesignComponentFormCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/BundleClinical").getString("DesignComponentFormUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/BundleClinical").getString("DesignComponentFormDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<DesignComponentForm> getItems() {
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

    public DesignComponentForm getDesignComponentForm(java.lang.Long id) {
        return getFacade().find(id);
    }

    public List<DesignComponentForm> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<DesignComponentForm> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    
    
    public DesignComponentFormSet getDesignComponentFormSet() {
        return designComponentFormSet;
    }

    public void setDesignComponentFormSet(DesignComponentFormSet designComponentFormSet) {
        this.designComponentFormSet = designComponentFormSet;
    }

    public List<DesignComponentForm> getFormsOfTheSelectedSet() {
        return formsOfTheSelectedSet;
    }

    public void setFormsOfTheSelectedSet(List<DesignComponentForm> formsOfTheSelectedSet) {
        this.formsOfTheSelectedSet = formsOfTheSelectedSet;
    }

    public DesignComponentForm getAddingForm() {
        return addingForm;
    }

    public void setAddingForm(DesignComponentForm addingForm) {
        if(addingForm==null){
            addingForm = new DesignComponentForm();
            addingForm.setParentComponent(selected);
        }
        this.addingForm = addingForm;
    }

    public DesignComponentForm getRemovingForm() {
        return removingForm;
    }

    public void setRemovingForm(DesignComponentForm removingForm) {
        this.removingForm = removingForm;
    }

    @FacesConverter(forClass = DesignComponentForm.class)
    public static class DesignComponentFormControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            DesignComponentFormController controller = (DesignComponentFormController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "designComponentFormController");
            return controller.getDesignComponentForm(getKey(value));
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
            if (object instanceof DesignComponentForm) {
                DesignComponentForm o = (DesignComponentForm) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), DesignComponentForm.class.getName()});
                return null;
            }
        }

    }

}
