package lk.gov.health.phsp.bean;

// <editor-fold defaultstate="collapsed" desc="Imports">
import lk.gov.health.phsp.entity.DesignComponentFormSet;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.bean.util.JsfUtil.PersistAction;
import lk.gov.health.phsp.facade.DesignComponentFormSetFacade;

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
import lk.gov.health.phsp.entity.DesignComponentForm;
import lk.gov.health.phsp.entity.DesignComponentFormItem;
import lk.gov.health.phsp.entity.Institution;
import org.apache.commons.lang3.SerializationUtils;
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
    private DesignComponentFormController designComponentFormController;
    @Inject
    private DesignComponentFormItemController designComponentFormItemController;
    @Inject
    private WebUserController webUserController;
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Class Variables">
    private List<DesignComponentFormSet> items = null;
    private List<DesignComponentFormSet> insItems = null;
    private DesignComponentFormSet selected;
    private DesignComponentFormSet referanceSet;
    private Institution institution;
    private String backString;

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Constructors">
    public DesignComponentFormSetController() {
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Navigation Functions">
    public String backToManageFormSets() {
        return backString;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Main Functions">
    public void importFormSet() {
        if (referanceSet == null) {
            JsfUtil.addErrorMessage("Formset to Import is NOT selected");
            return;
        }
        if (institution == null) {
            JsfUtil.addErrorMessage("Instituion NOT selected");
            return;
        }

        DesignComponentFormSet ns = (DesignComponentFormSet) SerializationUtils.clone(referanceSet);
        ns.setId(null);
        ns.setCreatedAt(new Date());
        ns.setCreatedBy(webUserController.getLoggedUser());
        ns.setLastEditBy(null);
        ns.setLastEditeAt(null);
        ns.setReferenceComponent(referanceSet);
        ns.setInstitution(institution);
        getFacade().create(ns);

        for (DesignComponentForm f : designComponentFormController.fillFormsofTheSelectedSet(referanceSet)) {
            DesignComponentForm nf = (DesignComponentForm) SerializationUtils.clone(f);
            nf.setId(null);
            nf.setCreatedAt(new Date());
            nf.setCreatedBy(webUserController.getLoggedUser());
            nf.setLastEditBy(null);
            nf.setLastEditeAt(null);
            nf.setReferenceComponent(f);
            nf.setParentComponent(ns);
            nf.setInstitution(institution);
            designComponentFormController.save(nf);

            for (DesignComponentFormItem i : designComponentFormItemController.fillItemsOfTheForm(f)) {

                DesignComponentFormItem ni = (DesignComponentFormItem) SerializationUtils.clone(i);
                ni.setId(null);
                ni.setCreatedAt(new Date());
                ni.setCreatedBy(webUserController.getLoggedUser());
                ni.setLastEditBy(null);
                ni.setLastEditeAt(null);
                ni.setReferenceComponent(i);
                ni.setParentComponent(nf);
                ni.setInstitution(institution);
                designComponentFormItemController.saveItem(ni);

            }
        }
        insItems = null;
        fillInsItems();
        referanceSet = null;
        institution = null;
        JsfUtil.addSuccessMessage("Formset Successfully Imported.");

    }

    public void setBackStringToSysAdmin() {
        backString = "/designComponentFormSet/List";
    }

    public void setBackStringToInsAdmin() {
        backString = "/designComponentFormSet/List_Ins";
    }

    public String toAddFormsForTheSelectedSet() {
        designComponentFormController.setDesignComponentFormSet(selected);
        designComponentFormController.fillFormsofTheSelectedSet();
        designComponentFormController.getAddingForm();
        return "/designComponentFormSet/manage_forms";
    }

    public List<DesignComponentFormSet> fillInsItems() {
        return fillInsItems(webUserController.getLoggableInstitutions());
    }

    private List<DesignComponentFormSet> fillMasterSets() {
        String j = "Select s from DesignComponentFormSet s "
                + " where s.retired=false "
                + " and s.institution is null "
                + " order by s.name";
        List<DesignComponentFormSet> ss = getFacade().findByJpql(j);
        if (ss == null) {
            ss = new ArrayList<>();
        }
        return ss;
    }
    
    
    
    
    public List<DesignComponentFormSet> getClinicFormSets(Institution clinic) {
        String j = "Select s from DesignComponentFormSet s "
                + " where s.retired=false "
                + " and s.institution = :inss "
                + " order by s.name";
        Map m = new HashMap();
        m.put("inss", clinic);
        List<DesignComponentFormSet> ss = getFacade().findByJpql(j, m);
        if (ss == null) {
            ss = new ArrayList<>();
        }
        return ss;
    }


    public List<DesignComponentFormSet> fillInsItems(List<Institution> insLst) {
        String j = "Select s from DesignComponentFormSet s "
                + " where s.retired=false "
                + " and s.institution in :inss "
                + " order by s.name";
        Map m = new HashMap();
        m.put("inss", insLst);
        List<DesignComponentFormSet> ss = getFacade().findByJpql(j, m);
        if (ss == null) {
            ss = new ArrayList<>();
        }
        return ss;
    }

    public List<DesignComponentFormSet> completeFormSets(String qry) {
        String j = "Select s from DesignComponentFormSet s "
                + " where s.retired=false "
                + " and s.institution is null"
                + " and lower(s.name) like :q "
                + " order by s.name";
        Map m = new HashMap();
        m.put("q", "%" + qry.trim().toLowerCase() + "%");
        List<DesignComponentFormSet> ss = getFacade().findByJpql(j, m);
        if (ss == null) {
            ss = new ArrayList<>();
        }
        return ss;
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
        if(selected==null){
            JsfUtil.addErrorMessage("Nothing to Delete");
            return ;
        }
        selected.setRetired(true);
        selected.setRetiredAt(new Date());
        selected.setRetiredBy(webUserController.getLoggedUser());
        getFacade().edit(selected);
        items = null;
        insItems=null;
        getItems();
        getInsItems();
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
    public DesignComponentFormSet getReferanceSet() {
        return referanceSet;
    }

    public void setReferanceSet(DesignComponentFormSet referanceSet) {
        this.referanceSet = referanceSet;
    }

    public DesignComponentFormController getDesignComponentFormController() {
        return designComponentFormController;
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public lk.gov.health.phsp.facade.DesignComponentFormSetFacade getEjbFacade() {
        return ejbFacade;
    }

    public List<DesignComponentFormSet> getInsItems() {
        if (insItems == null) {
            insItems = fillInsItems();
        }
        return insItems;
    }

    public void setInsItems(List<DesignComponentFormSet> insItems) {
        this.insItems = insItems;
    }

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
            items = fillMasterSets();
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

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public DesignComponentFormItemController getDesignComponentFormItemController() {
        return designComponentFormItemController;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Converter">
    public String getBackString() {
        return backString;
    }

    public void setBackString(String backString) {
        this.backString = backString;
    }

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
