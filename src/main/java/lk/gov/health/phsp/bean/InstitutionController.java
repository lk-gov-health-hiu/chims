package lk.gov.health.phsp.bean;

import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.bean.util.JsfUtil.PersistAction;
import lk.gov.health.phsp.facade.InstitutionFacade;

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
import javax.inject.Inject;
import lk.gov.health.phsp.enums.InstitutionType;


@Named("institutionController")
@SessionScoped
public class InstitutionController implements Serializable {

    @EJB
    private lk.gov.health.phsp.facade.InstitutionFacade ejbFacade;
    
    
    @Inject
    WebUserController webUserController;
    
    
    private List<Institution> items = null;
    private Institution selected;
    private List<Institution> myClinics;
    

    public InstitutionController() {
    }

    public Institution getSelected() {
        return selected;
    }

    public void setSelected(Institution selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private InstitutionFacade getFacade() {
        return ejbFacade;
    }

    public List<Institution> findChildrenInstitutions(Institution ins){
        System.out.println("findChildrenInstitutions for " + ins.getName());
        String j ;
        Map m= new HashMap();
        j = "select i from Institution i where i.retired=false "
                + " and i.parent=:p ";
        m.put("p", ins);
        List<Institution> cins = getFacade().findByJpql(j, m);
        List<Institution> tins = new ArrayList<>();
        tins.addAll(cins);
        if(cins.isEmpty()){
            return tins;
        }else{
            for(Institution i:cins){
                System.out.println("i = " + i);
                System.out.println("tins before finding children " + tins);
                tins.addAll(findChildrenInstitutions(i));
                System.out.println("tins after finding children " + tins);
            }
        }
        System.out.println("tins = " + tins);
        return tins;
    }
    
    public List<Institution> completeInstitutions(String nameQry) {
        return fillInstitutions(null, nameQry, null);
    }
    
    public List<Institution> fillInstitutions(InstitutionType type, String nameQry, Institution parent) {
        String j = "Select i from Institution i where i.retired=false ";
        Map m = new HashMap();
        if (nameQry != null) {
            j += " and lower(i.name) like :n ";
            m.put("n", "%" + nameQry.trim().toLowerCase() + "%");
        }
        if (type != null) {
            j += " and i.institutionType =:t ";
            m.put("t", type);
        }
        if (parent != null) {
            j += " and i.parent =:p ";
            m.put("p", parent);
        }
        j += " order by i.name";
        return getFacade().findByJpql(j, m);
    }

    public Institution prepareCreate() {
        selected = new Institution();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/BundleClinical").getString("InstitutionCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/BundleClinical").getString("InstitutionUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/BundleClinical").getString("InstitutionDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    
    
    public List<Institution> getItems() {
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

    public Institution getInstitution(java.lang.Long id) {
        return getFacade().find(id);
    }

    public List<Institution> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<Institution> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    public void refreshMyInstitutions(){
        myClinics = null;
    }
    
    public List<Institution> getMyClinics() {
        if(myClinics==null){
            myClinics= new ArrayList<>();
            for(Institution i:webUserController.getLoggableInstitutions()){
                if(i.getInstitutionType().equals(InstitutionType.Ward_Clinic)){
                    myClinics.add(i);
                }
            }
        }
        return myClinics;
    }

    public void setMyClinics(List<Institution> myClinics) {
        this.myClinics = myClinics;
    }

    @FacesConverter(forClass = Institution.class)
    public static class InstitutionControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            InstitutionController controller = (InstitutionController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "institutionController");
            return controller.getInstitution(getKey(value));
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
            if (object instanceof Institution) {
                Institution o = (Institution) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Institution.class.getName()});
                return null;
            }
        }

    }

}
