package lk.gov.health.phsp.bean;

import lk.gov.health.phsp.entity.IntegrationEndpoint;
import lk.gov.health.phsp.facade.IntegrationEndpointFacade;
import lk.gov.health.phsp.facade.util.JsfUtil;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.inject.Named;
import lk.gov.health.phsp.entity.SecurityProtocol;
import lk.gov.health.phsp.enums.CommunicationProtocol;
import lk.gov.health.phsp.enums.IntegrationEndpointType;

@Named
@SessionScoped
public class IntegrationEndpointController implements Serializable {

    @EJB
    private IntegrationEndpointFacade ejbFacade;
    private List<IntegrationEndpoint> items = null;
    private IntegrationEndpoint selected;

    @Inject
    private WebUserController webUserController;
    @Inject
    private CommonController commonController;

    public String navigateToAddNew() {
        selected = new IntegrationEndpoint();
        return "/systemAdmin/integrationEndpoint/edit";
    }

    public String navigateToView() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Please select an IntegrationEndpoint to Edit");
            return "";
        }
        return "/systemAdmin/integrationEndpoint/view";
    }

    public String navigateToEdit() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Please select an IntegrationEndpoint to Edit");
            return "";
        }
        return "/systemAdmin/integrationEndpoint/edit";
    }

    public String deleteIntegrationEndpoint() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Please select an IntegrationEndpoint to Delete");
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

    public String saveOrUpdateIntegrationEndpoint() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Please select an IntegrationEndpoint");
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
        return "/systemAdmin/integrationEndpoint/list";
    }
    
    public String navigateToTest() {
        items = fillAllItems();
        return "/systemAdmin/integrationEndpoint/test";
    }

    public List<IntegrationEndpointType> getIntegrationEndpointTypes() {
        return Arrays.asList(IntegrationEndpointType.values());
    }

    public List<CommunicationProtocol> getCommunicationProtocols() {
        return Arrays.asList(CommunicationProtocol.values());
    }

    public List<SecurityProtocol> getSecurityProtocols() {
        return Arrays.asList(SecurityProtocol.values());
    }

    public IntegrationEndpointController() {
    }

    public IntegrationEndpoint getSelected() {
        return selected;
    }

    public void setSelected(IntegrationEndpoint selected) {
        this.selected = selected;
    }

    private IntegrationEndpointFacade getFacade() {
        return ejbFacade;
    }

    public List<IntegrationEndpoint> getItems() {
        if (items == null) {
            items = fillAllItems();
        }
        return items;
    }

    // <editor-fold defaultstate="collapsed" desc="Getters and Setters">
    public WebUserController getWebUserController() {
        return webUserController;
    }

    public void setWebUserController(WebUserController webUserController) {
        this.webUserController = webUserController;
    }

    public IntegrationEndpointFacade getEjbFacade() {
        return ejbFacade;
    }

    public void setEjbFacade(IntegrationEndpointFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    public CommonController getCommonController() {
        return commonController;
    }

    private List<IntegrationEndpoint> fillAllItems() {
        String jpql = "select i "
                + " from IntegrationEndpoint i "
                + " where i.retired=:ret "
                + " order by i.name";
        Map m = new HashMap();
        m.put("ret", false);
        return getFacade().findByJpql(jpql, m);
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Converters">
    @FacesConverter(forClass = IntegrationEndpoint.class)
    public static class IntegrationEndpointControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            IntegrationEndpointController controller = (IntegrationEndpointController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "integrationEndpointController");
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
            if (object instanceof IntegrationEndpoint) {
                IntegrationEndpoint o = (IntegrationEndpoint) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), IntegrationEndpoint.class.getName()});
                return null;
            }
        }

    }

    // </editor-fold>
}
