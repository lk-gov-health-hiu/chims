package lk.gov.health.phsp.bean;

import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.bean.util.JsfUtil.PersistAction;
import lk.gov.health.phsp.facade.InstitutionFacade;

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
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.enums.AreaType;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.facade.AreaFacade;

@Named
@SessionScoped
public class InstitutionController implements Serializable {

    @EJB
    private InstitutionFacade ejbFacade;

    @EJB
    private AreaFacade areaFacade;

    @Inject
    private WebUserController webUserController;

    @Inject
    ApplicationController applicationController;
    @Inject
    UserTransactionController userTransactionController;

    private List<Institution> items = null;
    private Institution selected;
    private Institution deleting;
    private List<Institution> myClinics;
    private List<Area> gnAreasOfSelected;
    private Area area;
    private Area removingArea;

    public Institution getInstitutionById(Long id) {
        return getFacade().find(id);
    }

    public void addGnToPmc() {
        if (selected == null) {
            JsfUtil.addErrorMessage("No PMC is selected");
            return;
        }
        if (area == null) {
            JsfUtil.addErrorMessage("No GN is selected");
            return;
        }
        area.setPmci(selected);
        getAreaFacade().edit(area);
        area = null;
        fillGnAreasOfSelected();
        JsfUtil.addSuccessMessage("Successfully added.");
        userTransactionController.recordTransaction("Add Gn To Pmc");
    }

    public String toAddInstitution() {
        selected = new Institution();
        userTransactionController.recordTransaction("To Add Institution");
        return "/institution/institution";
    }

    public String toEditInstitution() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Please select");
            return "";
        }
        return "/institution/institution";
    }

    public boolean thisIsAParentInstitution(Institution checkingInstitution){
        boolean flag=false;
        if(checkingInstitution==null){
            return false;
        }
        for(Institution i:getItems()){
            if(i.getParent()!=null && i.getParent().equals(checkingInstitution)){
                flag=true;
                return flag;
            }
        }
        return flag;
    }
    
    public String deleteInstitution() {
        if (deleting == null) {
            JsfUtil.addErrorMessage("Please select");
            return "";
        }
        if(thisIsAParentInstitution(deleting)){
            JsfUtil.addErrorMessage("Can't delete. This has child institutions.");
            return "";
        }
        deleting.setRetired(true);
        deleting.setRetiredAt(new Date());
        deleting.setRetirer(webUserController.getLoggedUser());
        getFacade().edit(deleting);
        JsfUtil.addSuccessMessage("Deleted");
        applicationController.getInstitutions().remove(deleting);
        fillItems();
        return "/institution/list";
    }

    public String toListInstitutions() {
        userTransactionController.recordTransaction("To List Institutions");
        return "/institution/list";
    }

    public String toSearchInstitutions() {
        return "/institution/search";
    }

    public void removeGnFromPmc() {
        if (removingArea == null) {
            JsfUtil.addErrorMessage("Nothing to remove");
            return;
        }
        removingArea.setPmci(null);
        getAreaFacade().edit(removingArea);
        fillGnAreasOfSelected();
        removingArea = null;
        userTransactionController.recordTransaction("Remove Gn From Pmc");
    }

    public void fillGnAreasOfSelected() {
        if (selected == null) {
            gnAreasOfSelected = new ArrayList<>();
            return;
        }
        String j = "select a from Area a where a.retired=false "
                + " and a.type=:t "
                + " and a.pmci=:p "
                + " order by a.name";
        Map m = new HashMap();
        m.put("t", AreaType.GN);
        m.put("p", selected);
        gnAreasOfSelected = areaFacade.findByJpql(j, m);
    }

    public List<Area> findDrainingGnAreas(Institution ins) {
        List<Area> gns;
        if (ins == null) {
            gns = new ArrayList<>();
            return gns;
        }
        String j = "select a from Area a where a.retired=false "
                + " and a.type=:t "
                + " and a.pmci=:p "
                + " order by a.name";
        Map m = new HashMap();
        m.put("t", AreaType.GN);
        m.put("p", ins);
        gns = areaFacade.findByJpql(j, m);
        return gns;
    }

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

    public List<Institution> findChildrenPmcis(Institution ins) {
        String j;
        Map m = new HashMap();
        j = "select i from Institution i where i.retired=:ret and i.pmci=:pmci "
                + " and i.parent=:p ";
        m.put("p", ins);
        m.put("pmci", true);
        m.put("ret", false);
        List<Institution> cins = getFacade().findByJpql(j, m);
        List<Institution> tins = new ArrayList<>();
        tins.addAll(cins);
        if (cins.isEmpty()) {
            return tins;
        } else {
            for (Institution i : cins) {
                tins.addAll(findChildrenPmcis(i));
            }
        }
        return tins;
    }

    public List<Institution> findChildrenInstitutions(Institution ins) {
        String j;
        Map m = new HashMap();
        j = "select i from Institution i where i.retired=:ret "
                + " and i.parent=:p ";
        m.put("p", ins);
        m.put("ret", false);
        List<Institution> cins = getFacade().findByJpql(j, m);
        List<Institution> tins = new ArrayList<>();
        tins.addAll(cins);
        if (cins.isEmpty()) {
            return tins;
        } else {
            for (Institution i : cins) {
                tins.addAll(findChildrenInstitutions(i));
            }
        }
        return tins;
    }

    public List<Institution> completeInstitutions(String nameQry) {
        return fillInstitutions(null, nameQry, null);
    }

    public Institution findInstitutionByName(String name) {
        String j = "Select i from Institution i where i.retired=:ret ";
        Map m = new HashMap();
        if (name != null) {
            j += " and lower(i.name)=:n ";
            m.put("n", name.trim().toLowerCase());
        }
        m.put("ret", false);
        return getFacade().findFirstByJpql(j, m);
    }

    public Institution findInstitutionById(Long id) {
        String j = "Select i from Institution i where i.retired=:ret ";
        Map m = new HashMap();
        if (id != null) {
            j += " and i.id=:n ";
            m.put("n", id);
        }
        m.put("ret", false);
        return getFacade().findFirstByJpql(j, m);
    }

    public List<Institution> completePmcis(String nameQry) {
        String j = "Select i from Institution i where i.retired=false and i.pmci=true ";
        Map m = new HashMap();
        if (nameQry != null) {
            j += " and lower(i.name) like :n ";
            m.put("n", "%" + nameQry.trim().toLowerCase() + "%");
        }
        j += " order by i.name";
        return getFacade().findByJpql(j, m);
    }

    public void fillItems() {
        if(applicationController.getInstitutions()!=null){
            items = applicationController.getInstitutions();
            return;
        }
        String j = "select i "
                + " from Institution i "
                + " where i.retired=:ret "
                + " order by i.name";
        Map m = new HashMap<>();
        m.put("ret", false);
        items = getFacade().findByJpql(j, m);
        applicationController.setInstitutions(items);
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

    public void saveOrUpdateInstitution() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing to select");
            return;
        }
        if (selected.getId() == null) {
            selected.setCreatedAt(new Date());
            selected.setCreater(webUserController.getLoggedUser());
            getFacade().create(selected);
            applicationController.getInstitutions().add(selected);
            fillItems();
            JsfUtil.addSuccessMessage("Saved");
        } else {
            selected.setEditedAt(new Date());
            selected.setEditer(webUserController.getLoggedUser());
            getFacade().edit(selected);
            items = null;
            getItems();
            JsfUtil.addSuccessMessage("Updates");
        }
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/BundleClinical").getString("InstitutionCreated"));
        if (!JsfUtil.isValidationFailed()) {
            applicationController.getInstitutions().add(selected);
            fillItems();
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
            fillItems();
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

    public void refreshMyInstitutions() {
      userTransactionController.recordTransaction("refresh My Institutions");  
        myClinics = null;
    }

    public List<Institution> getMyClinics() {
        if (myClinics == null) {
            myClinics = new ArrayList<>();
            int count = 0;
            for (Institution i : webUserController.getLoggableInstitutions()) {
                if (i.getInstitutionType().equals(InstitutionType.Clinic)) {
                    myClinics.add(i);
                    count++;
                }
                if (count > 50) {
                    return myClinics;
                }
            }
        }
        return myClinics;
    }

    public lk.gov.health.phsp.facade.InstitutionFacade getEjbFacade() {
        return ejbFacade;
    }

    public AreaFacade getAreaFacade() {
        return areaFacade;
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public List<Area> getGnAreasOfSelected() {
        if (gnAreasOfSelected == null) {
            gnAreasOfSelected = new ArrayList<>();
        }
        return gnAreasOfSelected;
    }

    public void setGnAreasOfSelected(List<Area> gnAreasOfSelected) {
        this.gnAreasOfSelected = gnAreasOfSelected;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public Area getRemovingArea() {
        return removingArea;
    }

    public void setRemovingArea(Area removingArea) {
        this.removingArea = removingArea;

    }

    public void setMyClinics(List<Institution> myClinics) {
        this.myClinics = myClinics;
    }

    public Institution getDeleting() {
        return deleting;
    }

    public void setDeleting(Institution deleting) {
        this.deleting = deleting;
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
