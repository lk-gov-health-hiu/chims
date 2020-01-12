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
import lk.gov.health.phsp.entity.Person;

@Named("clientEncounterComponentItemController")
@SessionScoped
public class ClientEncounterComponentItemController implements Serializable {

    @EJB
    private lk.gov.health.phsp.facade.ClientEncounterComponentItemFacade ejbFacade;
    @Inject
    private WebUserController webUserController;

    @Inject
    private CommonController commonController;

    private List<ClientEncounterComponentItem> items = null;
    private List<ClientEncounterComponentItem> formsetItems = null;
    private ClientEncounterComponentItem selected;

    private Long searchId;
    
    
    public void searchById(){
        //System.out.println("searchById");
        //System.out.println("searchId = " + searchId);
        selected = getFacade().find(searchId);
    }
    
    public void findClientEncounterComponentItemOfAFormset(ClientEncounterComponentFormSet fs) {
        // //System.out.println("findClientEncounterComponentItemOfAForm = ");
        // //System.out.println("fs = " + fs);
        String j = "select f from ClientEncounterComponentItem f "
                + " where f.retired=false "
                + " and f.parentComponent.parentComponent=:p "
                + " order by f.orderNo";
        Map m = new HashMap();
        m.put("p", fs);
        formsetItems = getFacade().findByJpql(j, m);
    }


    
    public List<ClientEncounterComponentItem> findClientEncounterComponentItemOfAForm(ClientEncounterComponentForm fs) {
        System.out.println("findClientEncounterComponentItemOfAForm");
        System.out.println("fs = " + fs.getName() + " " + fs.getId());
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

    public ClientEncounterComponentItem prepareCreate() {
        selected = new ClientEncounterComponentItem();
        initializeEmbeddableKey();
        return selected;
    }

    public void save() {
        save(selected);
    }

    public void calculate(ClientEncounterComponentItem i) {
        //System.out.println("calculate");
        if (i == null) {
            //System.out.println("i is null. NOT calculating " + i);
            return;
        }

        if (i.getCalculationScript() == null || i.getCalculationScript().trim().equals("")) {
            //System.out.println("No Cript. Not calculating ");
            return;
        }
        if (i.getParentComponent() == null || i.getParentComponent().getParentComponent() == null) {
            //System.out.println("No Formset. Not calculating ");
            return;
        }
        if (!(i.getParentComponent().getParentComponent() instanceof ClientEncounterComponentFormSet)) {
            //System.out.println("Not a Formset. Not calculating ");
            //System.out.println("i.getParentComponent().getParentComponent() = " + i.getParentComponent().getParentComponent());
            return;
        }

        if (i.getCalculationScript().trim().equalsIgnoreCase("#{client_current_age_in_years}")) {
            ClientEncounterComponentFormSet s = (ClientEncounterComponentFormSet) i.getParentComponent().getParentComponent();
            //System.out.println("s = " + s);
            Person p = s.getEncounter().getClient().getPerson();
            //System.out.println("p = " + p);
            //System.out.println("p.getAgeYears() = " + p.getAgeYears());
            i.setShortTextValue(p.getAgeYears() + "");
            i.setIntegerNumberValue(p.getAgeYears());
            getFacade().edit(i);
            return;
        } else {
            //System.out.println("No age is to calculate. Proceeding to normal calculation.");
        }

        List<Replaceable> replacingBlocks = findReplaceblesInCalculationString(i.getCalculationScript());

        // //System.out.println("replacingBlocks = " + replacingBlocks);
        for (Replaceable r : replacingBlocks) {
            //System.out.println("r.getPef() = " + r.getPef());
            if (r.getPef().equalsIgnoreCase("f")) {
                if (r.getSm().equalsIgnoreCase("s")) {
                    //System.out.println("Single Value Search");
                    r.setClientEncounterComponentItem(findFormsetValue(i, r.getVariableCode()));
                } else {
                    r.setClientEncounterComponentItem(findFormsetValue(i, r.getVariableCode(), r.getValueCode()));
                }
            } else if (r.getPef().equalsIgnoreCase("p")) {
                r.setClientEncounterComponentItem(findClientValue(i, r.getVariableCode()));
            }
            if (r.getClientEncounterComponentItem() != null) {
                ClientEncounterComponentItem c = r.getClientEncounterComponentItem();
                //System.out.println("Found Item " + c.getId());
                //System.out.println("c.getItem().getCode() = " + c.getItem().getCode());
                //System.out.println("Data Type = " + c.getSelectionDataType());
                //System.out.println("c.getShortTextValue() = " + c.getShortTextValue());
                //System.out.println("c.getBooleanValue() = " + c.getBooleanValue());
                //System.out.println("c.getRealNumberValue() = " + c.getRealNumberValue());
                //System.out.println("c.getLongNumberValue() = " + c.getLongNumberValue());
                //System.out.println("c.getItemValue().getItemValue() = " + c.getItemValue());
                
                
                switch (c.getItem().getDataType()) {
                    case Short_Text:
                        if (c.getShortTextValue() != null) {
                            r.setSelectedValue(c.getShortTextValue());
                        }
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
                        if (c.getItemValue()!= null) {
                            r.setSelectedValue(c.getItemValue().getCode());
                        }
                        break;
                }
                //System.out.println("Found Value is r.getSelectedValue() = " + r.getSelectedValue());
            } else {
                r.setSelectedValue(r.getDefaultValue());
                //System.out.println("No Value Found - Default Value is r.getSelectedValue() = " + r.getSelectedValue());
            }
        }

        String javaStringToEvaluate = addTemplateToReport(i.getCalculationScript().trim(), replacingBlocks);
        //System.out.println("javaString To Evaluate = \n" + javaStringToEvaluate);

        String result = evaluateScript(javaStringToEvaluate);
        
        //System.out.println("Assigning Found Calculation Results");
        //System.out.println("i.getId() = " + i.getId());
        //System.out.println("result = " + result);
        //System.out.println("i.getSelectionDataType() = " + i.getSelectionDataType());

        if (null == i.getItem().getDataType()) {
            i.setShortTextValue(result);
        } else {
            switch (i.getItem().getDataType()) {
                case Real_Number:
                    i.setRealNumberValue(commonController.getDoubleValue(result));
                    //System.out.println("i.getRealNumberValue() = " + i.getRealNumberValue());
                    getFacade().edit(i);
                    break;
                case Integer_Number:
                    i.setIntegerNumberValue(commonController.getIntegerValue(result));
                    //System.out.println("i.getIntegerNumberValue() = " + i.getIntegerNumberValue());
                    getFacade().edit(i);
                    break;
                case Short_Text:
                    i.setShortTextValue(result);
                    //System.out.println("i.getShortTextValue() = " + i.getShortTextValue());
                    getFacade().edit(i);
                    break;
                case Long_Text:
                    //System.out.println("i.getLongTextValue() = " + i.getLongTextValue());
                    i.setLongTextValue(result);
                    getFacade().edit(i);
                    break;
                default:
                    break;
            }
            getFacade().edit(i);
        }

        // //System.out.println("javaStringToEvaluate = " + javaStringToEvaluate);
    }

    public String evaluateScript(String script) {
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        try {
            return engine.eval(script) + "";
        } catch (ScriptException ex) {
            //System.out.println("ex = " + ex.getMessage());
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
        //System.out.println("m = " + m);
        //System.out.println("j = " + j);
        ClientEncounterComponentItem temc = getFacade().findFirstByJpql(j, m);
        if (temc == null) {
            //System.out.println("Single Match NOT Found ");
        } else {
            //System.out.println("Single Match Found. ID is " + temc.getId());
        }
        return temc;
    }

    public ClientEncounterComponentItem findFormsetValue(ClientEncounterComponentItem i, String variableCode, String valueCode) {
        //System.out.println("findFormsetValue");
        //System.out.println("i = " + i);
        //System.out.println("valueCode = " + valueCode);
        //System.out.println("variableCode = " + variableCode);
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
            //System.out.println("Multiple Match NOT Found.");
        } else {
            //System.out.println("Multiple Match Found. ID is " + ti.getId());
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
        // //System.out.println("findReplaceblesInCalculationString");
        // //System.out.println("text = " + text);

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
        // //System.out.println("save");
        // //System.out.println("i = " + i);
        if (i == null) {
            return;
        }
        // //System.out.println("i.getId() = " + i.getId());
        // //System.out.println("i.getShortTextValue() = " + i.getShortTextValue());
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
        // //System.out.println("addAnother");
        // //System.out.println("i = " + i);
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

        ClientEncounterComponentItem ci = new ClientEncounterComponentItem();

        ci.setParentComponent(i.getParentComponent());
        ci.setReferenceComponent(i.getReferenceComponent());

        ci.setEncounter(i.getEncounter());
        ci.setInstitution(i.getInstitution());

        ci.setItem(i.getItem());
        ci.setDescreption(i.getDescreption());

        ci.setRequired(i.isRequired());
        ci.setRequiredErrorMessage(i.getRequiredErrorMessage());
        ci.setRegexValidationString(i.getRegexValidationString());
        ci.setRegexValidationFailedMessage(i.getRegexValidationFailedMessage());

        ci.setName(i.getName());
        ci.setRenderType(i.getRenderType());
        ci.setMimeType(i.getMimeType());
//        ci.setSelectionDataType(i.getSelectionDataType());
        ci.setTopPercent(i.getTopPercent());
        ci.setLeftPercent(i.getLeftPercent());
        ci.setWidthPercent(i.getWidthPercent());
        ci.setHeightPercent(i.getHeightPercent());
        ci.setCategoryOfAvailableItems(i.getCategoryOfAvailableItems());

        ci.setDataPopulationStrategy(i.getDataPopulationStrategy());
        ci.setDataModificationStrategy(i.getDataModificationStrategy());
        ci.setDataCompletionStrategy(i.getDataCompletionStrategy());
        ci.setIntHtmlColor(i.getIntHtmlColor());
        ci.setHexHtmlColour(i.getHexHtmlColour());

        ci.setForegroundColour(i.getForegroundColour());
        ci.setBackgroundColour(i.getBackgroundColour());
        ci.setBorderColour(i.getBorderColour());

        ci.setCalculateOnFocus(i.isCalculateOnFocus());
        ci.setCalculationScript(i.getCalculationScript());

        ci.setCalculateButton(i.isCalculateButton());
        ci.setCalculationScriptForColour(i.getCalculationScriptForColour());
        ci.setDisplayDetailsBox(i.isDisplayDetailsBox());
        ci.setDiscreptionAsAToolTip(i.isDiscreptionAsAToolTip());
        ci.setDiscreptionAsASideLabel(i.isDiscreptionAsASideLabel());
        ci.setCalculationScriptForBackgroundColour(i.getCalculationScriptForBackgroundColour());
        ci.setMultipleEntiesPerForm(i.isMultipleEntiesPerForm());

        // //System.out.println("getParentComponent = " + ci.getParentComponent());
        // //System.out.println("getReferenceComponent = " + ci.getReferenceComponent());
        ci.setParentComponent(i.getParentComponent());
        ci.setReferenceComponent(i.getReferenceComponent());
        // //System.out.println("ni = " + ci);
        // //System.out.println("ni = " + ci.getBackgroundColour());
        // //System.out.println("ni = " + ci.getDescreption());
        // //System.out.println("ni = " + ci.getAreaValue());
        // //System.out.println("ni = " + ci.getRealNumberValue());
        // //System.out.println("ni = " + ci.getLongNumberValue());
        // //System.out.println("ni = " + ci.getIntegerNumberValue());
        // //System.out.println("ni = " + ci.getItemValue());
        // //System.out.println("ni = " + ci.getPrescriptionValue());
        // //System.out.println("ni = " + ci.getInstitutionValue());
        // //System.out.println("getParentComponent = " + ci.getParentComponent());
        // //System.out.println("getReferenceComponent = " + ci.getReferenceComponent());

        ci.setOrderNo(i.getOrderNo() + (i.getOrderNo() / 0.001));
        ci.setCreatedAt(new Date());
        ci.setCreatedBy(webUserController.getLoggedUser());

        getFacade().create(ci);
        // //System.out.println("ni = " + ci);
        // //System.out.println("ni = " + ci.getId());

        findClientEncounterComponentItemOfAForm((ClientEncounterComponentForm) i.getParentComponent());

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
        //System.out.println("findClientValue");
        //System.out.println("code = " + code);
        //System.out.println("i = " + i);
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
        //System.out.println("m = " + m);
        //System.out.println("j = " + j);
        ClientEncounterComponentItem fountVal = getFacade().findFirstByJpql(j, m);
        if (fountVal == null) {
            //System.out.println("Patient Value NOT Found ");
        } else {
            //System.out.println("Patient Value Found. ID is " + fountVal);
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
