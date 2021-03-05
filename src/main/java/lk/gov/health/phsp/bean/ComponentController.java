package lk.gov.health.phsp.bean;

import lk.gov.health.phsp.entity.Component;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.bean.util.JsfUtil.PersistAction;
import lk.gov.health.phsp.facade.ComponentFacade;

import java.io.Serializable;
import java.util.List;
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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.ClientEncounterComponent;
import lk.gov.health.phsp.entity.ClientEncounterComponentForm;
import lk.gov.health.phsp.entity.ClientEncounterComponentFormSet;
import lk.gov.health.phsp.entity.ClientEncounterComponentItem;
import lk.gov.health.phsp.entity.DesignComponent;
import lk.gov.health.phsp.entity.DesignComponentForm;
import lk.gov.health.phsp.entity.DesignComponentFormItem;
import lk.gov.health.phsp.entity.DesignComponentFormSet;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.entity.QueryComponent;
import lk.gov.health.phsp.enums.AvailableDataType;
import lk.gov.health.phsp.enums.ComponentSetType;
import lk.gov.health.phsp.enums.DataCompletionStrategy;
import lk.gov.health.phsp.enums.DataModificationStrategy;
import lk.gov.health.phsp.enums.DataPopulationStrategy;
import lk.gov.health.phsp.enums.PanelType;
import lk.gov.health.phsp.enums.RenderType;
import lk.gov.health.phsp.enums.SelectionDataType;

@Named("componentController")
@SessionScoped
public class ComponentController implements Serializable {

    @EJB
    private lk.gov.health.phsp.facade.ComponentFacade ejbFacade;
    private List<Component> items = null;
    private Component selected;

    public ComponentController() {
    }

    public Component getSelected() {
        return selected;
    }

    public static Component cloneComponent(Component c) {
        Component nc = null;
        if (c == null) {
            return nc;
        }
        if (c instanceof ClientEncounterComponent) {

        } else if (c instanceof ClientEncounterComponentFormSet) {

        } else if (c instanceof ClientEncounterComponentForm) {

        } else if (c instanceof ClientEncounterComponentItem) {

        } else if (c instanceof DesignComponent) {

        } else if (c instanceof DesignComponentFormSet) {

        } else if (c instanceof DesignComponentForm) {

        } else if (c instanceof DesignComponentFormItem) {

        } else if (c instanceof QueryComponent) {

        } else {
            nc = new Component();
            nc.setName(c.getName());
            nc.setCode(c.getCode());
            nc.setItem(c.getItem());
            nc.setDescreption(c.getDescreption());
            nc.setOrderNo(c.getOrderNo());
            nc.setInstitution(c.getInstitution());
            nc.setParentComponent(c.getParentComponent());
            nc.setReferenceComponent(c.getReferenceComponent());
            nc.setCss(c.getCss());

        }
        return nc;
    }

    public static QueryComponent cloneComponent(QueryComponent c) {
        QueryComponent nc = null;
        if (c == null) {
            return nc;
        }
        nc = new QueryComponent();
        nc.setName(c.getName());
        nc.setCode(c.getCode());
        nc.setItem(c.getItem());
        nc.setDescreption(c.getDescreption());
        nc.setOrderNo(c.getOrderNo());
        nc.setInstitution(c.getInstitution());
        nc.setParentComponent(c.getParentComponent());
        nc.setReferenceComponent(c.getReferenceComponent());
        nc.setCss(c.getCss());
//
        nc.setIndicatorQuery(c.getIndicatorQuery());
        nc.setIndicatorQuery(c.getIndicatorQuery());
        nc.setSelectQuery(c.getSelectQuery());
        nc.setFromQuery(c.getFromQuery());
        nc.setWhereQuery(c.getWhereQuery());
        nc.setGroupQuery(c.getGroupQuery());
        nc.setHavingQuery(c.getHavingQuery());
        nc.setFilterQuery(c.getFilterQuery());
        nc.setOrderQuery(c.getOrderQuery());
        nc.setQueryType(c.getQueryType());
        nc.setOutputType(c.getOutputType());
        nc.setMatchType(c.getMatchType());
        nc.setEvaluationType(c.getEvaluationType());
        nc.setPopulationType(c.getPopulationType());
        nc.setQueryLevel(c.getQueryLevel());
        nc.setQueryDataType(c.getQueryDataType());

        nc.setLongTextValue(c.getLongTextValue());
        nc.setDescreptionValue(c.getDescreptionValue());
        nc.setShortTextValue(c.getShortTextValue());
        nc.setByteArrayValue(c.getByteArrayValue());
        nc.setIntegerNumberValue(c.getIntegerNumberValue());
        nc.setLongNumberValue(c.getLongNumberValue());
        nc.setRealNumberValue(c.getRealNumberValue());
        nc.setBooleanValue(c.getBooleanValue());
        nc.setDateValue(c.getDateValue());
        nc.setItemValue(c.getItemValue());
        nc.setAreaValue(c.getAreaValue());
        nc.setInstitutionValue(c.getInstitutionValue());
        nc.setClientValue(c.getClientValue());
        nc.setPrescriptionValue(c.getPrescriptionValue());
        nc.setObservationValue(c.getObservationValue());
        nc.setProcedureValue(c.getProcedureValue());
        nc.setMovementValue(c.getMovementValue());

        nc.setIntegerNumberValue2(c.getIntegerNumberValue2());
        nc.setLongNumberValue2(c.getLongNumberValue2());
        nc.setRealNumberValue2(c.getRealNumberValue2());

//        nc.setCompleted(c.isCompleted());
//        nc.setCompletedAt(c.getCompletedAt());
//        nc.setCompletedBy(c.getCompletedBy());

        nc.setFilterByDistrict(c.isFilterByDistrict());
        nc.setFilterByProvince(c.isFilterByProvince());
        nc.setFilterByRdhs(c.isFilterByRdhs());
        nc.setFilterByMoh(c.isFilterByMoh());
        nc.setFilterByFrom(c.isFilterByFrom());
        nc.setFilterByDate(c.isFilterByDate());
        nc.setFilterByTo(c.isFilterByTo());
        nc.setFilterByInstitution(c.isFilterByInstitution());
        nc.setFilterByGn(c.isFilterByGn());
        nc.setFilterByYear(c.isFilterByYear());
        nc.setFilterByMonth(c.isFilterByMonth());
        nc.setFilterByQuarter(c.isFilterByQuarter());
        nc.setRequired(c.isRequired());
        nc.setRequired(c.isRequired());

//        nc.setClient(c.getClient());
        return nc;
    }

    public static ClientEncounterComponent cloneComponent(ClientEncounterComponent c) {
        ClientEncounterComponent nc = null;
        if (c == null) {
            return nc;
        }
        nc = new ClientEncounterComponent();
        nc.setName(c.getName());
        nc.setCode(c.getCode());
        nc.setItem(c.getItem());
        nc.setDescreption(c.getDescreption());
        nc.setOrderNo(c.getOrderNo());
        nc.setInstitution(c.getInstitution());
        nc.setParentComponent(c.getParentComponent());
        nc.setReferenceComponent(c.getReferenceComponent());
        nc.setCss(c.getCss());
//
        nc.setEncounter(c.getEncounter());
        nc.setClient(c.getClient());
        return nc;
    }

    public static ClientEncounterComponentFormSet cloneComponent(ClientEncounterComponentFormSet c) {
        ClientEncounterComponentFormSet nc = null;
        if (c == null) {
            return nc;
        }
        nc = new ClientEncounterComponentFormSet();
        nc.setName(c.getName());
        nc.setCode(c.getCode());
        nc.setItem(c.getItem());
        nc.setDescreption(c.getDescreption());
        nc.setOrderNo(c.getOrderNo());
        nc.setInstitution(c.getInstitution());
        nc.setParentComponent(c.getParentComponent());
        nc.setReferenceComponent(c.getReferenceComponent());
        nc.setCss(c.getCss());
//
        nc.setEncounter(c.getEncounter());
        nc.setClient(c.getClient());
//        
        nc.setClientEncounterComponentItems(c.getClientEncounterComponentItems());
        return nc;
    }

    public static ClientEncounterComponentForm cloneComponent(ClientEncounterComponentForm c) {
        ClientEncounterComponentForm nc = null;
        if (c == null) {
            return nc;
        }
        nc = new ClientEncounterComponentForm();
        nc.setName(c.getName());
        nc.setCode(c.getCode());
        nc.setItem(c.getItem());
        nc.setDescreption(c.getDescreption());
        nc.setOrderNo(c.getOrderNo());
        nc.setInstitution(c.getInstitution());
        nc.setParentComponent(c.getParentComponent());
        nc.setReferenceComponent(c.getReferenceComponent());
        nc.setCss(c.getCss());
//
        nc.setEncounter(c.getEncounter());
        nc.setClient(c.getClient());
//        

        return nc;
    }

    public static ClientEncounterComponentItem cloneComponent(ClientEncounterComponentItem c) {
        ClientEncounterComponentItem nc = null;
        if (c == null) {
            return nc;
        }
        nc = new ClientEncounterComponentItem();
        nc.setName(c.getName());
        nc.setCode(c.getCode());
        nc.setItem(c.getItem());
        nc.setDescreption(c.getDescreption());
        nc.setOrderNo(c.getOrderNo());
        nc.setInstitution(c.getInstitution());
        nc.setParentComponent(c.getParentComponent());
        nc.setReferenceComponent(c.getReferenceComponent());
        nc.setCss(c.getCss());
//
        nc.setEncounter(c.getEncounter());
        nc.setClient(c.getClient());
//        
        nc.setItemClient(c.getItemClient());
        nc.setItemEncounter(c.getItemEncounter());
        nc.setItemFormset(c.getItemFormset());
        nc.setDataRepresentationType(c.getDataRepresentationType());
        nc.setLongTextValue(c.getLongTextValue());
        nc.setDescreptionValue(c.getDescreptionValue());
        nc.setShortTextValue(c.getShortTextValue());
        nc.setByteArrayValue(c.getByteArrayValue());
        nc.setIntegerNumberValue(c.getIntegerNumberValue());
        nc.setLongNumberValue(c.getLongNumberValue());
        nc.setRealNumberValue(c.getRealNumberValue());
        nc.setBooleanValue(c.getBooleanValue());
        nc.setDateValue(c.getDateValue());
        nc.setItemValue(c.getItemValue());
        nc.setAreaValue(c.getAreaValue());
        nc.setInstitutionValue(c.getInstitutionValue());
        nc.setClientValue(c.getClientValue());
        nc.setPrescriptionValue(c.getPrescriptionValue());
        nc.setObservationValue(c.getObservationValue());
        nc.setProcedureValue(c.getProcedureValue());
        nc.setMovementValue(c.getMovementValue());
        nc.setIntegerNumberValue2(c.getIntegerNumberValue2());
        nc.setLongNumberValue2(c.getLongNumberValue2());
        nc.setRealNumberValue2(c.getRealNumberValue2());
        return nc;
    }

    public static DesignComponent cloneComponent(DesignComponent c) {
        DesignComponent nc = null;
        if (c == null) {
            return nc;
        }
        nc = new DesignComponent();
        nc.setName(c.getName());
        nc.setCode(c.getCode());
        nc.setItem(c.getItem());
        nc.setDescreption(c.getDescreption());
        nc.setOrderNo(c.getOrderNo());
        nc.setInstitution(c.getInstitution());
        nc.setParentComponent(c.getParentComponent());
        nc.setReferenceComponent(c.getReferenceComponent());
        nc.setCss(c.getCss());
        //
        nc.setComponentSex(c.getComponentSex());
        return nc;
    }

    public static DesignComponentFormSet cloneComponent(DesignComponentFormSet c) {
        DesignComponentFormSet nc = null;
        if (c == null) {
            return nc;
        }
        nc = new DesignComponentFormSet();
        nc.setName(c.getName());
        nc.setCode(c.getCode());
        nc.setItem(c.getItem());
        nc.setDescreption(c.getDescreption());
        nc.setOrderNo(c.getOrderNo());
        nc.setInstitution(c.getInstitution());
        nc.setParentComponent(c.getParentComponent());
        nc.setReferenceComponent(c.getReferenceComponent());
        nc.setCss(c.getCss());
        //
        nc.setComponentSex(c.getComponentSex());
        //
        nc.setComponentSetType(c.getComponentSetType());
        nc.setPanelType(c.getPanelType());
        return nc;
    }

    public static DesignComponentForm cloneComponent(DesignComponentForm c) {
        DesignComponentForm nc = null;
        if (c == null) {
            return nc;
        }
        nc = new DesignComponentForm();
        nc.setName(c.getName());
        nc.setCode(c.getCode());
        nc.setItem(c.getItem());
        nc.setDescreption(c.getDescreption());
        nc.setOrderNo(c.getOrderNo());
        nc.setInstitution(c.getInstitution());
        nc.setParentComponent(c.getParentComponent());
        nc.setReferenceComponent(c.getReferenceComponent());
        nc.setCss(c.getCss());
        //
        nc.setComponentSex(c.getComponentSex());
        //
        nc.setItemArrangementStrategy(c.getItemArrangementStrategy());
        return nc;
    }

    public static DesignComponentFormItem cloneComponent(DesignComponentFormItem c) {
        DesignComponentFormItem nc = null;
        if (c == null) {
            return nc;
        }
        nc = new DesignComponentFormItem();
        nc.setName(c.getName());
        nc.setCode(c.getCode());
        nc.setItem(c.getItem());
        nc.setDescreption(c.getDescreption());
        nc.setOrderNo(c.getOrderNo());
        nc.setInstitution(c.getInstitution());
        nc.setParentComponent(c.getParentComponent());
        nc.setReferenceComponent(c.getReferenceComponent());
        nc.setCss(c.getCss());
        //
        nc.setComponentSex(c.getComponentSex());
        //
        nc.setRequired(c.isRequired());
        nc.setCalculateOnFocus(c.isCalculateOnFocus());
        nc.setCalculateButton(c.isCalculateButton());
        nc.setCalculationScriptForColour(c.getCalculationScriptForColour());
        nc.setCalculationScriptForBackgroundColour(c.getCalculationScriptForBackgroundColour());
        nc.setDisplayDetailsBox(c.isDisplayDetailsBox());
        nc.setDiscreptionAsAToolTip(c.isDiscreptionAsAToolTip());
        nc.setDiscreptionAsASideLabel(c.isDiscreptionAsASideLabel());
        nc.setDisplayLastResult(c.isDisplayLastResult());
        nc.setDisplayLinkToResultList(c.isDisplayLinkToResultList());
        nc.setDisplayLinkToClientValues(c.isDisplayLinkToClientValues());
        nc.setMultipleEntiesPerForm(c.isMultipleEntiesPerForm());
        nc.setCalculationScript(c.getCalculationScript());
        nc.setRequiredErrorMessage(c.getRequiredErrorMessage());
        nc.setRegexValidationString(c.getRegexValidationString());
        nc.setRegexValidationFailedMessage(c.getRegexValidationFailedMessage());
        nc.setMimeType(c.getMimeType());
        nc.setRenderType(c.getRenderType());
        nc.setComponentSetType(c.getComponentSetType());
        nc.setPanelType(c.getPanelType());
        nc.setSelectionDataType(c.getSelectionDataType());
        nc.setAvailableDataType(c.getAvailableDataType());
        nc.setDataPopulationStrategy(c.getDataPopulationStrategy());
        nc.setResultDisplayStrategy(c.getResultDisplayStrategy());
        nc.setDataCompletionStrategy(c.getDataCompletionStrategy());
        nc.setDataModificationStrategy(c.getDataModificationStrategy());
        nc.setParentAreaOfAvailableAreas(c.getParentAreaOfAvailableAreas());
        nc.setCategoryOfAvailableItems(c.getCategoryOfAvailableItems());
        nc.setParentInstitutionOfAvailableInstitutions(c.getParentInstitutionOfAvailableInstitutions());
        return nc;
    }

    public void setSelected(Component selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private ComponentFacade getFacade() {
        return ejbFacade;
    }

    public Component prepareCreate() {
        selected = new Component();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/BundleClinical").getString("ComponentCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/BundleClinical").getString("ComponentUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/BundleClinical").getString("ComponentDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<Component> getItems() {
//        if (items == null) {
//            items = getFacade().findAll();
//        }
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

    public Component getComponent(java.lang.Long id) {
        return getFacade().find(id);
    }

    public List<Component> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<Component> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    @FacesConverter(forClass = Component.class)
    public static class ComponentControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ComponentController controller = (ComponentController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "componentController");
            return controller.getComponent(getKey(value));
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
            if (object instanceof Component) {
                Component o = (Component) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Component.class.getName()});
                return null;
            }
        }

    }

}
