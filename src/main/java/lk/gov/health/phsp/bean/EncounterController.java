package lk.gov.health.phsp.bean;

import lk.gov.health.phsp.entity.Encounter;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.bean.util.JsfUtil.PersistAction;
import lk.gov.health.phsp.facade.EncounterFacade;

import java.io.Serializable;
import java.text.SimpleDateFormat;
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
import lk.gov.health.phsp.entity.Client;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.enums.EncounterType;

@Named("encounterController")
@SessionScoped
public class EncounterController implements Serializable {

    @EJB
    private lk.gov.health.phsp.facade.EncounterFacade ejbFacade;
    private List<Encounter> items = null;
    private Encounter selected;
    @Inject
    private WebUserController webUserController;

    public EncounterController() {
    }

  

    public String createClinicEnrollNumber(Institution clinic) {
        System.out.println("createClinicEnrollNumber");
        System.out.println("clinic = " + clinic);
        String j = "select count(e) from Encounter e "
                + " where e.institution=:ins "
                + " and e.encounterType=:ec "
                + " and e.createdAt>:d";
//        j = "select count(e) from Encounter e ";
        Map m = new HashMap();
        m.put("d", CommonController.startOfTheYear());
        m.put("ec", EncounterType.Clinic_Enroll);
        m.put("ins", clinic);
        System.out.println("m = " + m);
        System.out.println("j = " + j);
        Long c = getFacade().findLongByJpql(j, m);
        if (c == null) {
            c = 1l;
        } else {
            c += 1;
        }
        SimpleDateFormat format = new SimpleDateFormat("yy");
        String yy = format.format(new Date());
        return clinic.getCode() + "/" + yy + "/" + c;
    }

    public Long countOfEncounters(List<Institution> clinic, EncounterType ec) {
        System.out.println("countOfClinicEnrollments");
        System.out.println("clinic = " + clinic);
        String j = "select count(e) from Encounter e "
                + " where e.retired=:ret "
                + " and e.institution in :ins "
                + " and e.encounterType=:ec "
                + " and e.createdAt>:d";
        Map m = new HashMap();
        m.put("d", CommonController.startOfTheYear());
        m.put("ec", ec);
        m.put("ret", false);
        m.put("ins", clinic);
        System.out.println("m = " + m);
        System.out.println("j = " + j);
        Long c = getFacade().findLongByJpql(j, m);
        if (c == null) {
            c = 0l;
        }
        return c;
    }
    
    public void retireSelectedEncounter(){
        if(selected==null){
            JsfUtil.addErrorMessage("Nothing Selected");
            return;
        }
        selected.setRetired(true);
        selected.setRetiredAt(new Date());
        selected.setRetiredBy(webUserController.getLoggedUser());
        JsfUtil.addSuccessMessage("Retired Successfully");
        selected=null;
    }

    public boolean clinicEnrolmentExists(Institution i, Client c) {
        String j = "select e from Encounter e "
                + " where e.institution=:i "
                + " and e.client=:c"
                + " and e.completed=:com"
                + " and e.encounterType=:et";
        Map m = new HashMap();
        m.put("i", i);
        m.put("c", c);
        m.put("com", false);
        m.put("et", EncounterType.Clinic_Enroll);
        Encounter e = getFacade().findFirstByJpql(j, m);
        if (e == null) {
            return false;
        }
        if (e.getCompleted()) {
            return false;
        } else {
            return true;
        }
    }

    public Encounter getSelected() {
        return selected;
    }

    public void setSelected(Encounter selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private EncounterFacade getFacade() {
        return ejbFacade;
    }

    public Encounter prepareCreate() {
        selected = new Encounter();
        initializeEmbeddableKey();
        return selected;
    }

    public void save() {
        save(selected);
    }

    public void save(Encounter e) {
        if (e == null) {
            return;
        }
        if (e.getId() == null) {
            e.setCreatedAt(new Date());
            e.setCreatedBy(webUserController.getLoggedUser());
            getFacade().create(e);
        } else {
            e.setLastEditBy(webUserController.getLoggedUser());
            e.setLastEditeAt(new Date());
            getFacade().edit(e);
        }
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/BundleClinical").getString("EncounterCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/BundleClinical").getString("EncounterUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/BundleClinical").getString("EncounterDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<Encounter> getItems(String jpql, Map m) {
        return getFacade().findByJpql(jpql, m);
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

    public Encounter getEncounter(java.lang.Long id) {
        return getFacade().find(id);
    }

    public List<Encounter> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<Encounter> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public lk.gov.health.phsp.facade.EncounterFacade getEjbFacade() {
        return ejbFacade;
    }

    @FacesConverter(forClass = Encounter.class)
    public static class EncounterControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            EncounterController controller = (EncounterController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "encounterController");
            return controller.getEncounter(getKey(value));
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
            if (object instanceof Encounter) {
                Encounter o = (Encounter) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Encounter.class.getName()});
                return null;
            }
        }

    }

}
