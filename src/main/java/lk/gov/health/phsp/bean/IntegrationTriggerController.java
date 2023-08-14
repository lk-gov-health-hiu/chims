package lk.gov.health.phsp.bean;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.entity.IntegrationEndpoint;
import lk.gov.health.phsp.entity.IntegrationTrigger;
import lk.gov.health.phsp.enums.IntegrationEvent;
import lk.gov.health.phsp.facade.IntegrationTriggerFacade;

/**
 *
 * @author buddh
 */
@Named
@SessionScoped
public class IntegrationTriggerController implements Serializable {

    @EJB
    private IntegrationTriggerFacade ejbFacade;
    private List<IntegrationTrigger> items = null;
    private IntegrationTrigger selected;

    @Inject
    private WebUserController webUserController;
    @Inject
    private CommonController commonController;

    public String navigateToAddNew() {
        selected = new IntegrationTrigger();
        return "/systemAdmin/integrationTrigger/edit";
    }

    public String navigateToView() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Please select an IntegrationTrigger to view");
            return "";
        }
        return "/systemAdmin/integrationTrigger/view";
    }

    public String navigateToEdit() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Please select an IntegrationTrigger to edit");
            return "";
        }
        return "/systemAdmin/integrationTrigger/edit";
    }

    public String deleteIntegrationTrigger() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Please select an IntegrationTrigger to delete");
            return "";
        }
        selected.setRetired(true);
        selected.setRetiredAt(new Date());
        selected.setRetiredBy(webUserController.getLoggedUser());
        getFacade().edit(selected);
        items = null;
        selected = null;
        return navigateToList();
    }

    public String saveOrUpdateIntegrationTrigger() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Please select an IntegrationTrigger");
            return "";
        }
        if (selected.getId() == null) {
            selected.setCreatedAt(new Date());
            selected.setCreatedBy(webUserController.getLoggedUser());
            getFacade().create(selected);
            JsfUtil.addSuccessMessage("Saved");
        } else {
            getFacade().edit(selected);
            JsfUtil.addSuccessMessage("Updated");
        }
        items = null;
        selected = null;
        return navigateToList();
    }

    public String navigateToList() {
        items = fillAllItems();
        return "/systemAdmin/integrationTrigger/list";
    }

    public List<IntegrationEvent> getIntegrationEvents() {
        return Arrays.asList(IntegrationEvent.values());
    }

    public IntegrationTriggerController() {
    }

    public IntegrationTrigger getSelected() {
        return selected;
    }

    public void setSelected(IntegrationTrigger selected) {
        this.selected = selected;
    }

    private IntegrationTriggerFacade getFacade() {
        return ejbFacade;
    }

    public List<IntegrationTrigger> getItems() {
        if (items == null) {
            items = fillAllItems();
        }
        return items;
    }

    private List<IntegrationTrigger> fillAllItems() {
        String jpql = "select i "
                + " from IntegrationTrigger i "
                + " where i.retired=:ret "
                + " order by i.integrationEvent";
        Map m = new HashMap();
        m.put("ret", false);
        return getFacade().findByJpql(jpql, m);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Converters">

    @FacesConverter(forClass = IntegrationTrigger.class)
    public static class IntegrationTriggerControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            IntegrationTriggerController controller = (IntegrationTriggerController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "integrationTriggerController");
            return controller.getFacade().find(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            try {
                key = Long.valueOf(value);
            } catch (NumberFormatException e) {
                key = 0l;
            }
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
            if (object instanceof IntegrationTrigger) {
                IntegrationTrigger o = (IntegrationTrigger) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), IntegrationTrigger.class.getName()});
                return null;
            }
        }

    }
// </editor-fold>

}
