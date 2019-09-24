package lk.gov.health.phsp.bean;

import lk.gov.health.phsp.entity.Relationship;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.bean.util.JsfUtil.PersistAction;
import lk.gov.health.phsp.facade.RelationshipFacade;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
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
import lk.gov.health.phsp.enums.RelationshipType;

@Named("relationshipController")
@SessionScoped
public class RelationshipController implements Serializable {

    @EJB
    private lk.gov.health.phsp.facade.RelationshipFacade ejbFacade;

    @Inject
    private WebUserController webUserController;

    private List<Relationship> items = null;
    private Relationship selected;

    private Area area;
    private int year;
    private int month;

    private Relationship adding;
    private Relationship removing;

    public void addEmpowerementData() {
        if (adding == null) {
            JsfUtil.addErrorMessage("Select");
            return;
        }
        if (adding.getArea() == null) {
            JsfUtil.addErrorMessage("Select GN Area");
            return;
        }
        if (adding.getLongValue1() == null) {
            JsfUtil.addErrorMessage("Please enter the number empanelled");
            return;
        }
        if (adding.getYearInt() == 0) {
            JsfUtil.addErrorMessage("Please enter the nyear");
            return;
        }
        if (findRelationship(adding.getArea(), adding.getRelationshipType(), adding.getYearInt()) != null) {
            JsfUtil.addErrorMessage("Already data added.");
            return;
        }
        if(adding.getRelationshipType()==null){
            JsfUtil.addErrorMessage("Type ?");
            return;
        }
        save(adding);
        fillRelationshipData();
        adding = null;
        JsfUtil.addSuccessMessage("Updated");
    }

    public void removeRelationship() {
        if (removing == null) {
            JsfUtil.addErrorMessage("Nothing to remove");
            return;
        }
        removing.setRetired(true);
        removing.setRetiredAt(new Date());
        removing.setRetiredBy(webUserController.getLoggedUser());
        getFacade().edit(removing);
        removing = null;
        items = null;
    }

    public void save() {
        save(selected);
        JsfUtil.addSuccessMessage("Saved");
    }

    public void save(Relationship r) {
        if (r == null) {
            JsfUtil.addErrorMessage("Nothing selected");
            return;
        }
        if (r.getId() == null) {
            r.setCreatedAt(new Date());
            r.setCreatedBy(webUserController.getLoggedUser());
            getFacade().edit(r);
        } else {
            r.setLastEditBy(webUserController.getLoggedUser());
            r.setLastEditeAt(new Date());
            getFacade().edit(r);
        }
    }

    public void fillRelationshipData() {
        if (area == null) {
            return;
        }
       String j = "select r from Relationship r "
                + " where (r.area=:a or r.area.parentArea=:a or r.area.parentArea.parentArea=:a or r.area.parentArea.parentArea.parentArea=:a "
                + " or r.area.phm=:a or r.area.phi=:a or r.area.dsd=:a  or r.area.moh=:a  or  r.area.district=:a  or  r.area.province=:a  or r.area.rdhsArea=:a  or r.area.pdhsArea=:a)  "
                + " and r.retired=false "
                + " and r.yearInt=:y";
        
        Map m = new HashMap();
        m.put("a", area);
        m.put("y", year);
        items = getFacade().findByJpql(j, m);
    }

    public Relationship findRelationship(Area a, RelationshipType type, int year) {
        String j = "select r from Relationship r "
                + " where r.area=:a "
                + " and r.elationshipType=:t "
                + " and r.retired=false "
                + " and r.yearInt=:y";

        Map m = new HashMap();
        m.put("a", a);
        m.put("t", type);
        m.put("y", year);
        return getFacade().findFirstByJpql(j, m);
    }

    public RelationshipController() {
    }

    public Relationship getSelected() {
        return selected;
    }

    public void setSelected(Relationship selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private RelationshipFacade getFacade() {
        return ejbFacade;
    }

    public Relationship prepareCreate() {
        selected = new Relationship();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/BundleClinical").getString("RelationshipCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/BundleClinical").getString("RelationshipUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/BundleClinical").getString("RelationshipDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<Relationship> getItems() {
        if (items == null) {
            items = new ArrayList<>();
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

    public Relationship getRelationship(java.lang.Long id) {
        return getFacade().find(id);
    }

    public List<Relationship> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<Relationship> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public lk.gov.health.phsp.facade.RelationshipFacade getEjbFacade() {
        return ejbFacade;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public Relationship getAdding() {
        if (adding == null) {
            adding = new Relationship();
        }
        return adding;
    }

    public void setAdding(Relationship adding) {
        this.adding = adding;
    }

    public Relationship getRemoving() {
        return removing;
    }

    public void setRemoving(Relationship removing) {
        this.removing = removing;
    }

    public int getYear() {
        if (year == 0) {
            Calendar c = Calendar.getInstance();
            year = c.get(Calendar.YEAR);
        }
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    @FacesConverter(forClass = Relationship.class)
    public static class RelationshipControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            RelationshipController controller = (RelationshipController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "relationshipController");
            return controller.getRelationship(getKey(value));
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
            if (object instanceof Relationship) {
                Relationship o = (Relationship) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Relationship.class.getName()});
                return null;
            }
        }

    }

}
