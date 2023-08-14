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
import lk.gov.health.phsp.ejb.DataFormBean;
import lk.gov.health.phsp.entity.Client;
import lk.gov.health.phsp.entity.Component;
import lk.gov.health.phsp.entity.DesignComponentFormItem;
import lk.gov.health.phsp.entity.Encounter;
import lk.gov.health.phsp.entity.Person;
import lk.gov.health.phsp.entity.Prescription;
import lk.gov.health.phsp.enums.DataPopulationStrategy;
import lk.gov.health.phsp.enums.DataRepresentationType;
import lk.gov.health.phsp.enums.RenderType;
import lk.gov.health.phsp.enums.SelectionDataType;
import lk.gov.health.phsp.pojcs.dataentry.DataForm;
import lk.gov.health.phsp.pojcs.dataentry.DataFormset;
import lk.gov.health.phsp.pojcs.dataentry.DataItem;

@Named("clientEncounterComponentItemController")
@SessionScoped
public class ClientEncounterComponentItemController implements Serializable {

    @EJB
    private lk.gov.health.phsp.facade.ClientEncounterComponentItemFacade ejbFacade;

    @EJB
    DataFormBean dataFormBean;

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
        saveCi(selected);
    }

//
//    public void calculate(ClientEncounterComponentItem i) {
//        if (i == null) {
//            return;
//        }
//
//        if (i.getReferanceDesignComponentFormItem().getCalculationScript() == null || i.getReferanceDesignComponentFormItem().getCalculationScript().trim().equals("")) {
//            return;
//        }
//        if (i.getParentComponent() == null || i.getParentComponent().getParentComponent() == null) {
//            return;
//        }
//        if (!(i.getParentComponent().getParentComponent() instanceof ClientEncounterComponentFormSet)) {
//            return;
//        }
//
//        if (i.getReferanceDesignComponentFormItem().getCalculationScript().trim().equalsIgnoreCase("#{client_current_age_in_years}")) {
//            ClientEncounterComponentFormSet s = (ClientEncounterComponentFormSet) i.getParentComponent().getParentComponent();
//            Person p = s.getEncounter().getClient().getPerson();
//            i.setShortTextValue(p.getAgeYears() + "");
//            i.setRealNumberValue(Double.valueOf(p.getAgeYears()));
//            i.setIntegerNumberValue(p.getAgeYears());
//            getFacade().edit(i);
//            return;
//        } else {
//        }
//
//        List<Replaceable> replacingBlocks = findReplaceblesInCalculationString(i.getReferanceDesignComponentFormItem().getCalculationScript());
//
//        for (Replaceable r : replacingBlocks) {
//            if (r.getPef().equalsIgnoreCase("f")) {
//                if (r.getSm().equalsIgnoreCase("s")) {
//                    r.setClientEncounterComponentItem(findFormsetValue(i, r.getVariableCode()));
//                } else {
//                    r.setClientEncounterComponentItem(findFormsetValue(i, r.getVariableCode(), r.getValueCode()));
//                }
//            } else if (r.getPef().equalsIgnoreCase("p")) {
//                r.setClientEncounterComponentItem(findClientValue(i, r.getVariableCode()));
//            }
//            if (r.getClientEncounterComponentItem() != null) {
//                ClientEncounterComponentItem c = r.getClientEncounterComponentItem();
//
//                if (c == null || c.getReferanceDesignComponentFormItem() == null || c.getReferanceDesignComponentFormItem().getItem() == null) {
//                    continue;
//                } else {
//                    if (c.getReferanceDesignComponentFormItem().getItem().getDataType() == null) {
//                        continue;
//                    }
//                }
//                SelectionDataType dataType;
//                if (c.getReferanceDesignComponentFormItem().getSelectionDataType() == null && c.getReferanceDesignComponentFormItem().getItem().getDataType() == null) {
//                    dataType = SelectionDataType.Real_Number;
//                } else if (c.getReferanceDesignComponentFormItem().getSelectionDataType() != null && c.getReferanceDesignComponentFormItem().getItem().getDataType() == null) {
//                    dataType = c.getReferanceDesignComponentFormItem().getSelectionDataType();
//                } else if (c.getReferanceDesignComponentFormItem().getSelectionDataType() == null && c.getReferanceDesignComponentFormItem().getItem().getDataType() != null) {
//                    dataType = c.getItem().getDataType();
//                } else {
//                    if (c.getReferanceDesignComponentFormItem().getSelectionDataType() == c.getReferanceDesignComponentFormItem().getItem().getDataType()) {
//                        dataType = c.getReferanceDesignComponentFormItem().getItem().getDataType();
//                    } else {
//                        dataType = c.getReferanceDesignComponentFormItem().getItem().getDataType();
//                        System.err.println("Error in data types");
//                    }
//                }
//
//                if (dataType == null) {
//                    dataType = SelectionDataType.Real_Number;
//                }
//
//                switch (dataType) {
//                    case Short_Text:
//                        if (c.getShortTextValue() != null) {
//                            r.setSelectedValue(c.getShortTextValue());
//                        }
//                        break;
//                    case Boolean:
//                        if (c.getBooleanValue() != null) {
//                            r.setSelectedValue(c.getBooleanValue().toString());
//                        }
//                        break;
//                    case Real_Number:
//                        if (c.getRealNumberValue() != null) {
//                            r.setSelectedValue(c.getRealNumberValue().toString());
//                        }
//                        break;
//                    case Integer_Number:
//                        if (c.getIntegerNumberValue() != null) {
//                            r.setSelectedValue(c.getIntegerNumberValue().toString());
//                        }
//                        break;
//                    case Item_Reference:
//                        if (c.getItemValue() != null) {
//                            r.setSelectedValue(c.getItemValue().getCode());
//                        }
//                        break;
//                }
//
//            } else {
//                r.setSelectedValue(r.getDefaultValue());
//
//            }
//        }
//
//        String javaStringToEvaluate = addTemplateToReport(i.getReferanceDesignComponentFormItem().getCalculationScript().trim(), replacingBlocks);
//        String result = evaluateScript(javaStringToEvaluate);
//
//        if (null == i.getItem().getDataType()) {
//            i.setShortTextValue(result);
//        } else {
//            switch (i.getItem().getDataType()) {
//                case Real_Number:
//                    i.setRealNumberValue(CommonController.getDoubleValue(result));
////                    getFacade().edit(i);
//                    saveCi(i);
//                    break;
//                case Integer_Number:
//                    i.setIntegerNumberValue(CommonController.getIntegerValue(result));
////                    getFacade().edit(i);
//                    saveCi(i);
//                    break;
//                case Short_Text:
//                    i.setShortTextValue(result);
////                    getFacade().edit(i);
//                    saveCi(i);
//                    break;
//                case Long_Text:
//                    i.setLongTextValue(result);
////                    getFacade().edit(i);
//                    saveCi(i);
//                    break;
//                default:
//                    break;
//            }
////            getFacade().edit(i);
//            saveCi(i);
//        }
//        userTransactionController.recordTransaction("Calculate - Clinic Forms");
//
//    }
//
//    
    public void calculate(DataItem i) {

        //System.out.println("Calculate");
        if (i == null) {
            //System.out.println("i is null");
            return;
        }

        if (i.getDi().getCalculationScript() == null || i.getDi().getCalculationScript().trim().equals("")) {
            //System.out.println("i.getDi().getCalculationScript() = " + i.getDi().getCalculationScript());
            return;
        }

        if (i.getDi().getCalculationScript().trim().equalsIgnoreCase("#{client_current_age_in_years}")) {
            ClientEncounterComponentFormSet s = i.getForm().getFormset().getEfs();
            Person p = s.getEncounter().getClient().getPerson();
            i.getCi().setShortTextValue(p.getAgeYears() + "");
            i.getCi().setRealNumberValue(Double.valueOf(p.getAgeYears()));
            i.getCi().setIntegerNumberValue(p.getAgeYears());
            saveCi(i.getCi());
            return;
        } else {
        }

        List<Replaceable> replacingBlocks = findReplaceblesInCalculationString(i.getDi().getCalculationScript());

        for (Replaceable r : replacingBlocks) {
//            //System.out.println("r = " + r);
            if (r.getPef().equalsIgnoreCase("f")) {
//                //System.out.println("r.getPef() = " + r.getPef());
                if (r.getSm().equalsIgnoreCase("s")) {
//                    //System.out.println("r.getSm() = " + r.getSm());
                    r.setClientEncounterComponentItem(findFormsetValue(i, r.getVariableCode()));
//                    //System.out.println("1 r.getClientEncounterComponentItem() = " + r.getClientEncounterComponentItem());
                } else {
                    r.setClientEncounterComponentItem(findFormsetValue(i, r.getVariableCode(), r.getValueCode()));
                    //System.out.println("2 r.getClientEncounterComponentItem() = " + r.getClientEncounterComponentItem());
                }
            } else if (r.getPef().equalsIgnoreCase("p")) {
//                //System.out.println("2. r.getPef() = " + r.getPef());
                r.setClientEncounterComponentItem(findClientValue(i, r.getVariableCode()));
//                //System.out.println("r.getClientEncounterComponentItem() = " + r.getClientEncounterComponentItem());
            }
            if (r.getClientEncounterComponentItem() != null) {
                ClientEncounterComponentItem c = r.getClientEncounterComponentItem();
                //System.out.println("c = " + c);
                if (c == null || c.getReferanceDesignComponentFormItem() == null || c.getReferanceDesignComponentFormItem().getItem() == null) {
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
                    if (c.getReferanceDesignComponentFormItem().getSelectionDataType() == c.getReferanceDesignComponentFormItem().getItem().getDataType()) {
                        dataType = c.getReferanceDesignComponentFormItem().getItem().getDataType();
                    } else {
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
                        } else {
                            r.setSelectedValue(r.getDefaultValue());
                        }
                        break;
                    case Boolean:
                        if (c.getBooleanValue() != null) {
                            r.setSelectedValue(c.getBooleanValue().toString());
                        } else {
                            r.setSelectedValue(r.getDefaultValue());
                        }
                        break;
                    case Real_Number:
                        if (c.getRealNumberValue() != null) {
                            r.setSelectedValue(c.getRealNumberValue().toString());
                        } else {
                            r.setSelectedValue(r.getDefaultValue());
                        }
                        break;
                    case Integer_Number:
                        if (c.getIntegerNumberValue() != null) {
                            r.setSelectedValue(c.getIntegerNumberValue().toString());
                        } else {
                            r.setSelectedValue(r.getDefaultValue());
                        }
                        break;
                    case Item_Reference:
                        if (c.getItemValue() != null) {
                            r.setSelectedValue(c.getItemValue().getCode());
                        } else {
                            r.setSelectedValue(r.getDefaultValue());
                        }
                        break;
                }
                //System.out.println("r.getSelectedValue() = " + r.getSelectedValue());

            } else {
                //System.out.println("r.getDefaultValue() = " + r.getDefaultValue());
                r.setSelectedValue(r.getDefaultValue());

            }
        }

        String javaStringToEvaluate = addTemplateToReport(i.getDi().getCalculationScript().trim(), replacingBlocks);
        //System.out.println("javaStringToEvaluate = " + javaStringToEvaluate);
        String result = evaluateScript(javaStringToEvaluate);

        //System.out.println("result = " + result);
        if (null == i.getDi().getItem().getDataType()) {
            i.getCi().setShortTextValue(result);
        } else {
            switch (i.getDi().getItem().getDataType()) {
                case Real_Number:
                    i.getCi().setRealNumberValue(CommonController.getDoubleValue(result));
                    saveCi(i.getCi());
                    saveCi(i.getCi());
                    break;
                case Integer_Number:
                    i.getCi().setIntegerNumberValue(CommonController.getIntegerValue(result));
                    saveCi(i.getCi());
                    break;
                case Short_Text:
                    i.getCi().setShortTextValue(result);
                    saveCi(i.getCi());
                    break;
                case Long_Text:
                    i.getCi().setLongTextValue(result);
                    saveCi(i.getCi());
                    break;
                default:
                    break;
            }
            saveCi(i.getCi());
        }
        saveCi(i.getCi());
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

    public ClientEncounterComponentItem findFormsetValue(DataItem i, String code) {
        if (i == null) {
            return null;
        }
        if (code == null) {
            return null;
        }
        if (code.trim().equals("")) {
            return null;
        }
        DataFormset s = i.getForm().getFormset();
        ClientEncounterComponentItem temc = null;
        for (DataForm f : s.getForms()) {
            for (DataItem di : f.getItems()) {
                if (di.getDi().getItem().getCode().equalsIgnoreCase(code)) {
                    temc = di.getCi();
                }
            }
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

    public ClientEncounterComponentItem findFormsetValue(DataItem i, String variableCode, String valueCode) {
        //System.out.println("2 findFormsetValue");
        if (i == null) {
            //System.out.println("i null");
            return null;
        }
        if (variableCode == null) {
            //System.out.println("variableCode is null");
            return null;
        }
        if (variableCode.trim().equals("")) {
            return null;
        }
        if (valueCode == null) {
            //System.out.println("valueCode is null");
            return null;
        }
        if (valueCode.trim().equals("")) {
            return null;
        }

        DataFormset s = i.getForm().getFormset();
        //System.out.println("s = " + s);
        ClientEncounterComponentItem temc = null;
        for (DataForm f : s.getForms()) {
            //System.out.println("f = " + f.getDf().getName());
            for (DataItem di : f.getItems()) {
                //System.out.println("di = " + di);
                if (di == null) {
                    continue;
                }
                if (di.getDi() == null) {
                    continue;
                }
                if (di.getDi().getItem() == null) {
                    continue;
                }
                if (di.getDi().getItem().getCode() == null) {
                    continue;
                }
                if (di.getDi().getItem().getCode().equalsIgnoreCase(variableCode)) {
                    if (di.getAddedItems() == null) {
                        //System.out.println("di is null " );
                        continue;
                    }
                    for (DataItem tdi : di.getAddedItems()) {
                        //System.out.println("tdi = " + tdi);
                        //System.out.println("tdi = " + tdi.getAddedItems());
                        //TODO : Add Logic for Other Data Types in addition to Item Referance
                        if (tdi.getCi() != null && tdi.getCi().getItemValue() != null && tdi.getCi().getItemValue().getCode() != null) {
                            if (tdi.getCi().getItemValue().getCode().equalsIgnoreCase(valueCode)) {
                                temc = tdi.getCi();
                            }
                        }
                    }

                }
            }
        }
        return temc;
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

    public void saveCi(ClientEncounterComponentItem i) {
        if (i == null) {
            return;
        }
        if (i.getId() == null) {
//            i.setCreatedAt(new Date());
//            i.setCreatedBy(webUserController.getLoggedUser());
            getFacade().create(i);
        } else{
             getFacade().edit(i);
        }
//        dataFormBean.saveCi(i);
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
//            i.setLastEditBy(webUserController.getLoggedUser());
//            i.setLastEditeAt(new Date());
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

    public void addAnotherDataItem(DataItem i) {
        // //System.out.println("addAnother");
        // //System.out.println("Dataitem i = " + i);

        if (i == null) {
            JsfUtil.addErrorMessage("No Data Item");
            return;
        }

        // //System.out.println("i.getAddingItem() = " + i.getAddingItem());
        if (i.getAddingItem() == null) {
            JsfUtil.addErrorMessage("No Adding Item");
            return;
        }

        // //System.out.println("i.getAddingItem().getCi() = " + i.getAddingItem().getCi());
        if (i.getAddingItem().getCi() == null) {
            JsfUtil.addErrorMessage("No CI for Adding Item");
            // //System.out.println("No CI for Adding Item");
            return;
        }

        if (i.getAddingItem().getCi().getItemValue() == null) {
            JsfUtil.addErrorMessage("No Item value for CI");
            return;
        } else {
            // //System.out.println("i.getAddingItem().getCi().getItemValue() = " + i.getAddingItem().getCi().getItemValue().getName());
        }

        // //System.out.println("going to saveCi");
        // //System.out.println("i.getAddingItem().getCi().getId() = " + i.getAddingItem().getCi().getId());
        saveCi(i.getAddingItem().getCi());

        // //System.out.println("saved");
        // //System.out.println("i.getAddingItem().getCi().getId() = " + i.getAddingItem().getCi().getId());
        i.getAddedItems().add(i.getAddingItem());

        if (i.getAddingItem().getCi().getItemValue() == null) {
            JsfUtil.addErrorMessage("No Item value for CI");
            return;
        } else {
            // //System.out.println("i.getAddingItem().getCi().getItemValue() = " + i.getAddingItem().getCi().getItemValue().getName());
        }

        // //System.out.println("before new nci");
        ClientEncounterComponentItem nci = new ClientEncounterComponentItem();

        nci.setEncounter(i.getForm().getFormset().getEfs().getEncounter());
        nci.setInstitution(i.getForm().getFormset().getEfs().getInstitution());

        nci.setItemFormset(i.getForm().getFormset().getEfs());
        nci.setItemEncounter(i.getForm().getFormset().getEfs().getEncounter());
        nci.setItemClient(i.getForm().getFormset().getEfs().getClient());

        nci.setItem(i.getDi().getItem());

        nci.setReferenceComponent(i.getDi());
        nci.setParentComponent(i.getForm().getCf());
        nci.setName(i.getDi().getName());
        nci.setCss(i.getDi().getCss());
        nci.setOrderNo(i.getAddedItems().size() + 1.0);
        nci.setDataRepresentationType(DataRepresentationType.Encounter);

        nci.setInstitutionValue(i.getAddingItem().getCi().getInstitutionValue());

        if (i.getDi().getRenderType() == RenderType.Prescreption) {
            Prescription p = new Prescription();
            p.setClient(i.getForm().getFormset().getEfs().getEncounter().getClient());
            p.setEncounter(i.getForm().getFormset().getEfs().getEncounter());
            p.setCreatedAt(new Date());
            p.setCreatedBy(webUserController.getLoggedUser());
            nci.setPrescriptionValue(p);
        }

        // //System.out.println("before new ni");
        DataItem ni = new DataItem();
        ni.setMultipleEntries(true);
        ni.setCi(nci);
        ni.di = i.getDi();
        ni.id = (int) (i.getAddedItems().size() + 1.0);
        ni.orderNo = i.getAddedItems().size() + 1.0;
        ni.form = i.getForm();

        i.setAddingItem(ni);

        // //System.out.println("before recording user transaction");
//        userTransactionController.recordTransaction("Add Another - Clinic Forms");
        // //System.out.println("after saving user transaction");
    }

    public void removeDataItem(DataItem i, DataItem removingItem) {
        // //System.out.println("removeDataItem");
        // //System.out.println("Dataitem i = " + i);

        if (i == null) {
            JsfUtil.addErrorMessage("No Data Item");
            return;
        }

        if (removingItem == null) {
            JsfUtil.addErrorMessage("No Removing Item.");
            return;
        }

        if (removingItem.getCi() == null) {
            JsfUtil.addErrorMessage("No Removing c Item.");
            return;
        }

        removingItem.getCi().setRetired(true);
//        removingItem.getCi().setRetiredAt(new Date());
//        removingItem.getCi().setRetiredBy(webUserController.getLoggedUser());
        saveCi(removingItem.getCi());

        i.getAddedItems().remove(removingItem);

        userTransactionController.recordTransaction("Remove Item - Clinic Forms");
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
        return items;
    }

    public List<ClientEncounterComponentItem> getItems(String j, Map m) {
        return getFacade().findByJpql(j, m);
    }

    public ClientEncounterComponentItem getItem(String j, Map m) {
        return getFacade().findFirstByJpql(j, m);
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
                + " and i.item.code=:c";
        Map m = new HashMap();
        m.put("client", client);
        m.put("c", code.toLowerCase());

        ClientEncounterComponentItem fountVal = getFacade().findFirstByJpql(j, m);
        if (fountVal != null) {
            if (code.equalsIgnoreCase("client_current_age_in_years")) {
                Person p = client.getPerson();
//                fountVal.setLastEditeAt(new Date());
//                fountVal.setLastEditBy(webUserController.getLoggedUser());
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

    private ClientEncounterComponentItem findClientValue(DataItem i, String code) {
        //System.out.println("code = " + code);
        //System.out.println("i = " + i);
        if (i == null) {
            return null;
        }
        //System.out.println("i = " + i);
        //System.out.println("i.getForm() = " + i.getForm());
        //System.out.println("i.getForm().getFormset() = " + i.getForm().getFormset());
        //System.out.println("i.getForm().getFormset().getMapOfClientValues() = " + i.getForm().getFormset().getMapOfClientValues());

        ClientEncounterComponentItem fountVal = i.getForm().getFormset().getMapOfClientValues().get(code.toLowerCase());
        //System.out.println("fountVal = " + fountVal);

        if (fountVal != null) {
            if (code.equalsIgnoreCase("client_current_age_in_years")) {
                Person p = i.getForm().getFormset().getEfs().getClient().getPerson();
//                fountVal.setLastEditeAt(new Date());
//                fountVal.setLastEditBy(webUserController.getLoggedUser());
                fountVal.setShortTextValue(p.getAgeYears() + "");
                fountVal.setRealNumberValue(Double.valueOf(p.getAgeYears()));
                fountVal.setIntegerNumberValue(p.getAgeYears());
                getFacade().edit(fountVal);
            }
        } else {
            if (code.equalsIgnoreCase("client_current_age_in_years")) {
                ClientEncounterComponentItem ageItem = new ClientEncounterComponentItem();
                ageItem.setClient(i.getForm().getFormset().getEfs().getClient());
                ageItem.setCreatedAt(new Date());
                ageItem.setCreatedBy(webUserController.getLoggedUser());
                ageItem.setItem(itemController.findItemByCode("client_current_age_in_years"));
                Person p = i.getForm().getFormset().getEfs().getClient().getPerson();
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
