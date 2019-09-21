package lk.gov.health.phsp.bean;

// <editor-fold defaultstate="collapsed" desc="Imports">
import lk.gov.health.phsp.entity.DesignComponentForm;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.bean.util.JsfUtil.PersistAction;
import lk.gov.health.phsp.facade.DesignComponentFormFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
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
import javax.inject.Inject;
import lk.gov.health.phsp.entity.DesignComponentFormSet;
// </editor-fold>

@Named("designComponentFormController")
@SessionScoped
public class DesignComponentFormController implements Serializable {

// <editor-fold defaultstate="collapsed" desc="EJBs">
    @EJB
    private lk.gov.health.phsp.facade.DesignComponentFormFacade ejbFacade;
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Controllers">
    @Inject
    WebUserController webUserController;
    @Inject
    DesignComponentFormItemController designComponentFormItemController;
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Class Variables">
    private List<DesignComponentForm> items = null;
    private DesignComponentForm selected;
    private List<DesignComponentForm> formsOfTheSelectedSet = null;
    private DesignComponentForm addingForm;
    private DesignComponentForm removingForm;
    private DesignComponentForm movingForm;
    private DesignComponentFormSet designComponentFormSet;
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Constructor">

    public DesignComponentFormController() {
    }
// </editor-fold>    
// <editor-fold defaultstate="collapsed" desc="Navigation Functions">

    public String toEditDesignComponentFrom() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing to Edit");
            return "";
        }

        return "/designComponentForm/form";
    }

    public String toManageDesignComponentItems() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing to Manage");
            return "";
        }
        designComponentFormItemController.setDesignComponentForm(selected);
        designComponentFormItemController.fillItemsOfTheForm();
        designComponentFormItemController.createNewAddingItem();
        return "/designComponentFormItem/manage_items";
    }

    // </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Main Functions">
    public void saveSelected() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing to save");
            return;
        }
        if (selected.getId() == null) {
            selected.setCreatedAt(new Date());
            selected.setCreatedBy(webUserController.getLoggedUser());
            getFacade().create(selected);
            JsfUtil.addSuccessMessage("Saved Successfully");
        } else {
            selected.setLastEditBy(webUserController.getLoggedUser());
            selected.setLastEditeAt(new Date());
            getFacade().edit(selected);
            JsfUtil.addSuccessMessage("Updated Successfully");
        }
    }
    
    public void save(DesignComponentForm f) {
        if (f == null) {
            return;
        }
        if (f.getId() == null) {
            f.setCreatedAt(new Date());
            f.setCreatedBy(webUserController.getLoggedUser());
            getFacade().create(f);
        } else {
            f.setLastEditBy(webUserController.getLoggedUser());
            f.setLastEditeAt(new Date());
            getFacade().edit(f);
        }
    }

    public void fillFormsofTheSelectedSet() {
        formsOfTheSelectedSet = fillFormsofTheSelectedSet(designComponentFormSet);
    }

    public List<DesignComponentForm> fillFormsofTheSelectedSet(DesignComponentFormSet set) {
        if (set == null) {
            return new ArrayList<>();
        }
        String j = "Select f from DesignComponentForm f "
                + "where f.retired=false "
                + " and f.parentComponent=:pc "
                + " order by f.orderNo";
        Map m = new HashMap();
        m.put("pc", set);
        return getFacade().findByJpql(j, m);
    }

    public void addFormToTheSelectedSet() {
//        System.out.println("addFormToTheSelectedSet");
//        System.out.println("designComponentFormSet = " + designComponentFormSet);
//        System.out.println("addingForm = " + addingForm);
        if (designComponentFormSet == null) {
            JsfUtil.addErrorMessage("No Formset");
            return;
        }
        if (addingForm == null) {
            JsfUtil.addErrorMessage("No Form");
            return;
        }
        addingForm.setParentComponent(designComponentFormSet);
        addingForm.setCreatedAt(new Date());
        addingForm.setCreatedBy(webUserController.getLoggedUser());
        getFacade().create(addingForm);

        fillFormsofTheSelectedSet();
        addingForm = null;
    }

    public void removeFromFromTheSelectedSet() {
        if (removingForm == null) {
            JsfUtil.addErrorMessage("No form to remove.");
            return;
        }
        removingForm.setRetired(true);
        removingForm.setRetiredAt(new Date());
        removingForm.setRetiredBy(webUserController.getLoggedUser());
        getFacade().edit(removingForm);
        fillFormsofTheSelectedSet();
        JsfUtil.addSuccessMessage("Item Removed");
    }

    public void moveUpTheSelectedSet() {
        if (movingForm == null) {
            JsfUtil.addErrorMessage("No form to move.");
            return;
        }
        movingForm.setOrderNo(movingForm.getOrderNo() - 1.5);
        getFacade().edit(movingForm);
        fillFormsofTheSelectedSet();
        Double o = 0.0;
        for (DesignComponentForm f : getFormsOfTheSelectedSet()) {
            o = o + 1;
            f.setOrderNo(o);
            getFacade().edit(f);
        }
        fillFormsofTheSelectedSet();
        movingForm = null;
        JsfUtil.addSuccessMessage("Item Moved Up");
    }

    public void moveDownTheSelectedSet() {
        if (movingForm == null) {
            JsfUtil.addErrorMessage("No form to move.");
            return;
        }
        movingForm.setOrderNo(movingForm.getOrderNo() + 1.5);
        getFacade().edit(movingForm);
        fillFormsofTheSelectedSet();
        Double o = 0.0;
        for (DesignComponentForm f : getFormsOfTheSelectedSet()) {
            o = o + 1;
            f.setOrderNo(o);
            getFacade().edit(f);
        }
        fillFormsofTheSelectedSet();
        JsfUtil.addSuccessMessage("Item Moved Down");
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Default Functions">
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

    public void setEjbFacade(DesignComponentFormFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    public void setWebUserController(WebUserController webUserController) {
        this.webUserController = webUserController;
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

    public List<DesignComponentForm> getItems() {
        if (items == null) {
            items = getFacade().findAll();
        }
        return items;
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
        if (addingForm == null && designComponentFormSet != null) {
            addingForm = new DesignComponentForm();
            addingForm.setParentComponent(designComponentFormSet);
            addingForm.setComponentSex(designComponentFormSet.getComponentSex());
            if (getFormsOfTheSelectedSet() != null) {
                addingForm.setOrderNo(Double.valueOf(getFormsOfTheSelectedSet().size() + 1));
            } else {
                addingForm.setOrderNo(1.0);
            }
        }
        return addingForm;
    }

    public void setAddingForm(DesignComponentForm addingForm) {
        this.addingForm = addingForm;
    }

    public DesignComponentForm getRemovingForm() {
        return removingForm;
    }

    public void setRemovingForm(DesignComponentForm removingForm) {
        this.removingForm = removingForm;
    }

    public DesignComponentForm getMovingForm() {
        return movingForm;
    }

    public void setMovingForm(DesignComponentForm movingForm) {
        this.movingForm = movingForm;
    }
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Converter">

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

// </editor-fold>
}
