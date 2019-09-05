package lk.gov.health.phsp.bean;

import lk.gov.health.phsp.entity.Coordinate;


import lk.gov.health.phsp.facade.CoordinateFacade;
import lk.gov.health.phsp.facade.util.JsfUtil;

import java.io.Serializable;
import java.util.List;
import java.util.ResourceBundle;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

@Named("coordinateController")
@SessionScoped
public class CoordinateController implements Serializable {

    private Coordinate current;
    private List<Coordinate> items = null;
    @EJB
    private lk.gov.health.phsp.facade.CoordinateFacade ejbFacade;

    public CoordinateController() {
        
    }

    
    
    private CoordinateFacade getFacade() {
        return ejbFacade;
    }

    public String prepareList() {
        recreateModel();
        return "List";
    }

    public String prepareView() {
        return "View";
    }

    public String prepareCreate() {
        current = new Coordinate();
        return "Create";
    }

    public String create() {
        try {
            getFacade().create(current);
            JsfUtil.addSuccessMessage(("CoordinateCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        return "Edit";
    }

    public String update() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(("CoordinateUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        performDestroy();
        recreateModel();
        return "List";
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (current != null) {
            return "View";
        } else {
            recreateModel();
            return "List";
        }
    }

    private void performDestroy() {
        try {
            getFacade().remove(current);
            JsfUtil.addSuccessMessage(("CoordinateDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        getFacade().edit(current);
    }

    public List<Coordinate> getItems() {
        if (items == null) {
            items = getFacade().findAll();
        }
        return items;
    }

    private void recreateModel() {
        items = null;
    }

    public List<Coordinate> getItemsAvailableSelectMany() {
        return getItems();
    }

    public List<Coordinate> getItemsAvailableSelectOne() {
        return getItems();
    }

    public Coordinate getCoordinate(java.lang.Long id) {
        return ejbFacade.find(id);
    }

    public Coordinate getCurrent() {
        return current;
    }

    public void setCurrent(Coordinate current) {
        this.current = current;
    }

    @FacesConverter(forClass = Coordinate.class)
    public static class CoordinateControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            CoordinateController controller = (CoordinateController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "coordinateController");
            return controller.getCoordinate(getKey(value));
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
            if (object instanceof Coordinate) {
                Coordinate o = (Coordinate) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Coordinate.class.getName());
            }
        }

    }

}
