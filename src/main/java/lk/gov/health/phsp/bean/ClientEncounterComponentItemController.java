package lk.gov.health.phsp.bean;

import lk.gov.health.phsp.entity.ClientEncounterComponentItem;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.bean.util.JsfUtil.PersistAction;
import lk.gov.health.phsp.facade.ClientEncounterComponentItemFacade;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import lk.gov.health.phsp.entity.ClientEncounterComponentForm;
import lk.gov.health.phsp.entity.ClientEncounterComponentFormSet;
import lk.gov.health.phsp.pojcs.Replaceable;
import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import lk.gov.health.phsp.entity.Client;
import lk.gov.health.phsp.entity.Component;
import lk.gov.health.phsp.entity.Encounter;
import lk.gov.health.phsp.entity.Person;
import lk.gov.health.phsp.enums.SelectionDataType;

@Named("clientEncounterComponentItemController")
@SessionScoped
public class ClientEncounterComponentItemController implements Serializable {

    @EJB
    private lk.gov.health.phsp.facade.ClientEncounterComponentItemFacade ejbFacade;
    @Inject
    private WebUserController webUserController;

    @Inject
    private CommonController commonController;
    @Inject
    private ItemController itemController;
    @Inject
    private UserTransactionController userTransactionController;

    private List<ClientEncounterComponentItem> items = null;
    private List<ClientEncounterComponentItem> formsetItems = null;
    private ClientEncounterComponentItem selected;

    private Long searchId;

    public void searchById() {
        
        selected = getFacade().find(searchId);
    }

    public void findClientEncounterComponentItemOfAFormset(ClientEncounterComponentFormSet fs) {
        
        String j = "select f from ClientEncounterComponentItem f "
                + " where f.retired=false "
                + " and f.parentComponent.parentComponent=:p "
                + " order by f.orderNo";
        Map m = new HashMap();
        m.put("p", fs);
        formsetItems = getFacade().findByJpql(j, m);
    }

    public List<ClientEncounterComponentItem> findClientEncounterComponentItemOfAForm(ClientEncounterComponentForm fs) {
        String j = "select f from ClientEncounterComponentItem f "
                + " where f.retired=false "
                + " and f.parentComponent=:p "
                + " order by f.orderNo";
        Map m = new HashMap();
        m.put("p", fs);
        List<ClientEncounterComponentItem> t = getFacade().findByJpql(j, m);
        if (t == null) {
            t = new ArrayList<>();
        }
        return t;
    }

    public List<ClientEncounterComponentItem> findClientEncounterComponentItems(Encounter enc) {
        String j = "select f from ClientEncounterComponentItem f "
                + " where f.retired=false "
                + " and f.encounter=:e";
        Map m = new HashMap();
        m.put("e", enc);
        List<ClientEncounterComponentItem> t = getFacade().findByJpql(j, m);
        if (t == null) {
            t = new ArrayList<>();
        }
        return t;
    }

    public ClientEncounterComponentItemController() {
    }

    public ClientEncounterComponentItem getSelected() {
        return selected;
    }

    public void setSelected(ClientEncounterComponentItem selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private ClientEncounterComponentItemFacade getFacade() {
        return ejbFacade;
    }

//    public ClientEncounterComponentItem prepareCreate() {
//        selected = new ClientEncounterComponentItem();
//        initializeEmbeddableKey();
//        return selected;
//    }

    public void save() {
        save(selected);
    }

    public void calculate(ClientEncounterComponentItem i) {
        if (i == null) {
            return;
        }

        if (i.getReferanceDesignComponentFormItem().getCalculationScript() == null || i.getReferanceDesignComponentFormItem().getCalculationScript().trim().equals("")) {
           return;
        }
        if (i.getParentComponent() == null || i.getParentComponent().getParentComponent() == null) {
           return;
        }
        if (!(i.getParentComponent().getParentComponent() instanceof ClientEncounterComponentFormSet)) {
           return;
        }

        if (i.getReferanceDesignComponentFormItem().getCalculationScript().trim().equalsIgnoreCase("#{client_current_age_in_years}")) {
            ClientEncounterComponentFormSet s = (ClientEncounterComponentFormSet) i.getParentComponent().getParentComponent();
            Person p = s.getEncounter().getClient().getPerson();
            i.setShortTextValue(p.getAgeYears() + "");
            i.setRealNumberValue(Double.valueOf(p.getAgeYears()));
            i.setIntegerNumberValue(p.getAgeYears());
            getFacade().edit(i);
           return;
        } else {
        }

        List<Replaceable> replacingBlocks = findReplaceblesInCalculationString(i.getReferanceDesignComponentFormItem().getCalculationScript());
       
        for (Replaceable r : replacingBlocks) {
           if (r.getPef().equalsIgnoreCase("f")) {
                if (r.getSm().equalsIgnoreCase("s")) {
                    r.setClientEncounterComponentItem(findFormsetValue(i, r.getVariableCode()));
                } else {
                    r.setClientEncounterComponentItem(findFormsetValue(i, r.getVariableCode(), r.getValueCode()));
                }
            } else if (r.getPef().equalsIgnoreCase("p")) {
                r.setClientEncounterComponentItem(findClientValue(i, r.getVariableCode()));
            }
            if (r.getClientEncounterComponentItem() != null) {
                ClientEncounterComponentItem c = r.getClientEncounterComponentItem();

                if (c==null || c.getReferanceDesignComponentFormItem()==null || c.getReferanceDesignComponentFormItem().getItem() == null) {
                    continue;
                } else {
                    if (c.getReferanceDesignComponentFormItem().getItem().getDataType() == null) {
                        continue;
                    }
                }
                SelectionDataType dataType;
                if (c.getReferanceDesignComponentFormItem().getSelectionDataType() == null && c.getReferanceDesignComponentFormItem().getItem().getDataType() == null) {
                    dataType = SelectionDataType.Real_Number;
                } else if (c.getReferanceDesignComponentFormItem().getSelectionDataType() != null && c.getReferanceDesignComponentFormItem().getItem().getDataType() == null) {
                    dataType = c.getReferanceDesignComponentFormItem().getSelectionDataType();
                } else if (c.getReferanceDesignComponentFormItem().getSelectionDataType() == null && c.getReferanceDesignComponentFormItem().getItem().getDataType() != null) {
                    dataType = c.getItem().getDataType();
                } else {
                    if(c.getReferanceDesignComponentFormItem().getSelectionDataType() == c.getReferanceDesignComponentFormItem().getItem().getDataType()){
                        dataType = c.getReferanceDesignComponentFormItem().getItem().getDataType();
                    }else{
                        dataType = c.getReferanceDesignComponentFormItem().getItem().getDataType();
                        System.err.println("Error in data types");
                    }
                }

                if (dataType == null) {
                    dataType = SelectionDataType.Real_Number;
                }

                switch (dataType) {
                    case Short_Text:
                        if (c.getShortTextValue() != null) {
                            r.setSelectedValue(c.getShortTextValue());
                        }
                        break;
                    case Boolean:
                        if (c.getBooleanValue() != null) {
                            r.setSelectedValue(c.getBooleanValue().toString());
                        }
                        break;
                    case Real_Number:
                        if (c.getRealNumberValue() != null) {
                            r.setSelectedValue(c.getRealNumberValue().toString());
                        }
                        break;
                    case Integer_Number:
                        if (c.getIntegerNumberValue() != null) {
                            r.setSelectedValue(c.getIntegerNumberValue().toString());
                        }
                        break;
                    case Item_Reference:
                        if (c.getItemValue() != null) {
                            r.setSelectedValue(c.getItemValue().getCode());
                        }
                        break;
                }
               
            } else {
                r.setSelectedValue(r.getDefaultValue());
               
            }
        }

        String javaStringToEvaluate = addTemplateToReport(i.getReferanceDesignComponentFormItem().getCalculationScript().trim(), replacingBlocks);
        String result = evaluateScript(javaStringToEvaluate);
        
        if (null == i.getItem().getDataType()) {
            i.setShortTextValue(result);
        } else {
            switch (i.getItem().getDataType()) {
                case Real_Number:
                    i.setRealNumberValue(CommonController.getDoubleValue(result));
                    getFacade().edit(i);
                    break;
                case Integer_Number:
                    i.setIntegerNumberValue(CommonController.getIntegerValue(result));
                    getFacade().edit(i);
                    break;
                case Short_Text:
                    i.setShortTextValue(result);
                    getFacade().edit(i);
                    break;
                case Long_Text:
                   i.setLongTextValue(result);
                    getFacade().edit(i);
                    break;
                default:
                    break;
            }
            getFacade().edit(i);
        }
        userTransactionController.recordTransaction("Calculate - Clinic Forms");
         
    }

    public String evaluateScript(String script) {
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        try {
            return engine.eval(script) + "";
        } catch (ScriptException ex) {
            
            return null;
        }
    }

    public ClientEncounterComponentItem findFormsetValue(ClientEncounterComponentItem i, String code) {
        if (i == null) {
            return null;
        }
        if (i.getParentComponent() == null) {
            return null;
        }
        if (i.getParentComponent().getParentComponent() == null) {
            return null;
        }
        if (code == null) {
            return null;
        }
        if (code.trim().equals("")) {
            return null;
        }
        String j = "select i from ClientEncounterComponentItem i where i.retired=:r "
                + " and i.parentComponent.parentComponent.id=:pc "
                + " and i.item.code=:c";
        Map m = new HashMap();
        m.put("pc", i.getParentComponent().getParentComponent().getId());
        m.put("r", false);
        m.put("c", code);
        
        ClientEncounterComponentItem temc = getFacade().findFirstByJpql(j, m);
        if (temc == null) {
           
        } else {
            
        }
        return temc;
    }

    public ClientEncounterComponentItem findFormsetValue(ClientEncounterComponentItem i, String variableCode, String valueCode) {
       
        if (i == null) {
            return null;
        }
        if (i.getParentComponent() == null) {
            return null;
        }
        if (i.getParentComponent().getParentComponent() == null) {
            return null;
        }
        if (variableCode == null) {
            return null;
        }
        if (variableCode.trim().equals("")) {
            return null;
        }
        String j = "select i from ClientEncounterComponentItem i where i.retired=:r "
                + " and i.parentComponent.parentComponent.id=:pc "
                + " and i.item.code=:c "
                + " and i.itemValue.code=:vc";
        Map m = new HashMap();
        m.put("pc", i.getParentComponent().getParentComponent().getId());
        m.put("c", variableCode.toLowerCase());
        m.put("vc", valueCode.toLowerCase());
        m.put("r", false);
        ClientEncounterComponentItem ti = getFacade().findFirstByJpql(j, m);
        if (ti == null) {
           
        } else {
            
        }
        return ti;
    }

    public String addTemplateToReport(String calculationScript, List<Replaceable> selectables) {
        for (Replaceable s : selectables) {
            String patternStart = "#{";
            String patternEnd = "}";
            String toBeReplaced;
            toBeReplaced = patternStart + s.getFullText() + patternEnd;
            calculationScript = calculationScript.replace(toBeReplaced, s.getSelectedValue());
        }
        return calculationScript;
    }

    public List<Replaceable> findReplaceblesInCalculationString(String text) {
        

        List<Replaceable> ss = new ArrayList<>();

        String patternStart = "#{";
        String patternEnd = "}";
        String regexString = Pattern.quote(patternStart) + "(.*?)" + Pattern.quote(patternEnd);

        Pattern p = Pattern.compile(regexString);
        Matcher m = p.matcher(text);

        while (m.find()) {
            String block = m.group(1);
            if (!block.trim().equals("")) {
                Replaceable s = new Replaceable();
                s.setFullText(block);
                if (block.contains("|")) {
                    String[] blockParts = block.split("\\|");
                    for (int i = 0; i < blockParts.length; i++) {
                        switch (i) {
                            case 0:
                                s.setPef(blockParts[0]);
                                break;
                            case 1:
                                s.setFl(blockParts[1]);
                                break;
                            case 2:
                                s.setSm(blockParts[2]);
                                break;
                            case 3:
                                s.setVariableCode(blockParts[3]);
                                break;
                            case 4:
                                s.setValueCode(blockParts[4]);
                                break;
                            case 5:
                                s.setDefaultValue(blockParts[5]);
                                break;
                            default:
                                break;
                        }
                    }
                    s.setInputText(false);
                    s.setFormulaEvaluation(true);
                } else {
                    return ss;
                }
                ss.add(s);
            }
        }

        return ss;

    }

    public void save(ClientEncounterComponentItem i) {
        
        if (i == null) {
            return;
        }
        
        if (i.getId() == null) {
            i.setCreatedAt(new Date());
            i.setCreatedBy(webUserController.getLoggedUser());
            getFacade().create(i);
        } else {
            i.setLastEditBy(webUserController.getLoggedUser());
            i.setLastEditeAt(new Date());
            getFacade().edit(i);
        }
    }

    public void addAnother(ClientEncounterComponentItem i) {
        
        if (i == null) {
            return;
        }
        if (i.getId() == null) {
            i.setCreatedAt(new Date());
            i.setCreatedBy(webUserController.getLoggedUser());
            getFacade().create(i);
        } else {
            i.setLastEditBy(webUserController.getLoggedUser());
            i.setLastEditeAt(new Date());
            getFacade().edit(i);
        }

        Long temporaryFormSetStartTimeInLong;
        Long temporaryCurrentTimeInLong;

        if (i.getParentComponent() == null && i.getParentComponent().getParentComponent() == null && i.getParentComponent().getParentComponent().getCreatedAt() == null) {
            temporaryFormSetStartTimeInLong = i.getParentComponent().getParentComponent().getCreatedAt().getTime();
        } else {
            temporaryFormSetStartTimeInLong = (new Date()).getTime();
        }

        ClientEncounterComponentItem ci = new ClientEncounterComponentItem();

        ci.setParentComponent(i.getParentComponent());
        ci.setReferenceComponent(i.getReferenceComponent());
        ci.setEncounter(i.getEncounter());
        ci.setInstitution(i.getInstitution());
        ci.setItem(i.getItem());
        ci.setDescreption(i.getDescreption());
        ci.setName(i.getName());
        ci.setParentComponent(i.getParentComponent());
        ci.setReferenceComponent(i.getReferenceComponent());
        
        temporaryCurrentTimeInLong = (new Date()).getTime();

        ci.setOrderNo(i.getOrderNo() + ((temporaryCurrentTimeInLong - temporaryFormSetStartTimeInLong) / temporaryFormSetStartTimeInLong));

        ci.setCreatedAt(new Date());
        ci.setCreatedBy(webUserController.getLoggedUser());

        getFacade().create(ci);
        
        findClientEncounterComponentItemOfAForm((ClientEncounterComponentForm) i.getParentComponent());
        userTransactionController.recordTransaction("Add Another - Clinic Forms");
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/BundleClinical").getString("ClientEncounterComponentItemCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/BundleClinical").getString("ClientEncounterComponentItemUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/BundleClinical").getString("ClientEncounterComponentItemDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<ClientEncounterComponentItem> getItems() {
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

    public ClientEncounterComponentItem getClientEncounterComponentItem(java.lang.Long id) {
        return getFacade().find(id);
    }

    public List<ClientEncounterComponentItem> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<ClientEncounterComponentItem> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public lk.gov.health.phsp.facade.ClientEncounterComponentItemFacade getEjbFacade() {
        return ejbFacade;
    }

    public CommonController getCommonController() {
        return commonController;
    }

    private ClientEncounterComponentItem findClientValue(ClientEncounterComponentItem i, String code) {

        if (i == null) {
            return null;
        }
        if (i.getParentComponent() == null) {
            return null;
        }
        if (i.getParentComponent().getParentComponent() == null) {
            return null;
        }
        Component c = i.getParentComponent().getParentComponent();
        ClientEncounterComponentFormSet s;
        if (c instanceof ClientEncounterComponentFormSet) {
            s = (ClientEncounterComponentFormSet) c;
        } else {
            return null;
        }
        Client client;
        if (s.getEncounter() == null && s.getClient() == null) {
            return null;
        } else if (s.getClient() != null) {
            client = s.getClient();
        } else if (s.getEncounter().getClient() != null) {
            client = s.getEncounter().getClient();
        } else {
            return null;
        }

        if (code == null) {
            return null;
        }
        if (code.trim().equals("")) {
            return null;
        }

        String j = "select i from ClientEncounterComponentItem i where i.retired=false "
                + " and i.client=:client "
                + " and lower(i.item.code)=:c";
        Map m = new HashMap();
        m.put("client", client);
        m.put("c", code.toLowerCase());

        ClientEncounterComponentItem fountVal = getFacade().findFirstByJpql(j, m);
        if (fountVal != null) {
            if (code.equalsIgnoreCase("client_current_age_in_years")) {
                Person p = client.getPerson();
                fountVal.setLastEditeAt(new Date());
                fountVal.setLastEditBy(webUserController.getLoggedUser());
                fountVal.setShortTextValue(p.getAgeYears() + "");
                fountVal.setRealNumberValue(Double.valueOf(p.getAgeYears()));
                fountVal.setIntegerNumberValue(p.getAgeYears());
                getFacade().edit(fountVal);

            }

        } else {
            if (code.equalsIgnoreCase("client_current_age_in_years")) {
                ClientEncounterComponentItem ageItem = new ClientEncounterComponentItem();
                ageItem.setClient(client);
                ageItem.setCreatedAt(new Date());
                ageItem.setCreatedBy(webUserController.getLoggedUser());
                ageItem.setItem(itemController.findItemByCode("client_current_age_in_years"));
                Person p = client.getPerson();
                ageItem.setShortTextValue(p.getAgeYears() + "");
                ageItem.setRealNumberValue(Double.valueOf(p.getAgeYears()));
                ageItem.setIntegerNumberValue(p.getAgeYears());
                getFacade().create(ageItem);
                fountVal = ageItem;

            }

        }
        return fountVal;

    }

    public Long getSearchId() {
        return searchId;
    }

    public void setSearchId(Long searchId) {
        this.searchId = searchId;
    }

    public List<ClientEncounterComponentItem> getFormsetItems() {
        return formsetItems;
    }

    public void setFormsetItems(List<ClientEncounterComponentItem> formsetItems) {
        this.formsetItems = formsetItems;
    }

    public ItemController getItemController() {
        return itemController;
    }

    @FacesConverter(forClass = ClientEncounterComponentItem.class)
    public static class ClientEncounterComponentItemControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ClientEncounterComponentItemController controller = (ClientEncounterComponentItemController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "clientEncounterComponentItemController");
            return controller.getClientEncounterComponentItem(getKey(value));
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
            if (object instanceof ClientEncounterComponentItem) {
                ClientEncounterComponentItem o = (ClientEncounterComponentItem) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), ClientEncounterComponentItem.class.getName()});
                return null;
            }
        }

    }

}
