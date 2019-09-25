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
    
    private List<ClientEncounterComponentItem> items = null;
    private ClientEncounterComponentItem selected;


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
        System.out.println("calculate");
        System.out.println("i.isCalculateOnFocus() = " + i.isCalculateOnFocus());
        System.out.println("i.getCalculationScript() = " + i.getCalculationScript());
        if (!i.isCalculateOnFocus()) {
            return;
        }
        if (i.getCalculationScript() == null || i.getCalculationScript().trim().equals("")) {
            return;
        }

        List<Replaceable> replacingBlocks = findReplaceblesInCalculationString(i.getCalculationScript());

        System.out.println("replacingBlocks = " + replacingBlocks);

        for (Replaceable r : replacingBlocks) {
            if (r.getPef().toLowerCase().equals("f")) {
                r.setClientEncounterComponentItem(findFormsetValue(i, r.getCode()));
            }
            // TODO: Need to add Logic for Encounter values and patient values (p and e)!   

            if (r.getClientEncounterComponentItem() != null) {
                ClientEncounterComponentItem c = r.getClientEncounterComponentItem();
                System.out.println("c = " + c);
                switch (c.getSelectionDataType()) {
                    case Boolean:
                        if (c.getBooleanValue() != null) {
                            r.setSelectedValue(c.getBooleanValue().toString());
                        }

                        break;
                    case Real_Number:
                        System.out.println("c.getRealNumberValue() = " + c.getRealNumberValue());
                        System.out.println("c.getName() = " + c.getName());
                        System.out.println("c.getId() = " + c.getId());
                        if (c.getRealNumberValue() != null) {
                            r.setSelectedValue(c.getRealNumberValue().toString());
                        }
                        break;
                    case Integer_Number:
                        if (c.getLongNumberValue() != null) {
                            r.setSelectedValue(c.getLongNumberValue().toString());
                        }
                        break;
                    case Item_Reference:
                        if (c.getItem() != null) {
                            r.setSelectedValue(c.getItem().getCode());
                        }
                        break;
                    // TODO: Need to add Logic for Encounter values and patient values (p and e)!   
                }
            }

        }

        String javaStringToEvaluate = addTemplateToReport(i.getCalculationScript().trim(), replacingBlocks);
        System.out.println("javaStringToEvaluate = " + javaStringToEvaluate);
        
        
        String result = evaluateScript(javaStringToEvaluate);
        System.out.println("result = " + result);
        System.out.println("i.getSelectionDataType() = " + i.getSelectionDataType());
        
        if(null==i.getSelectionDataType()){
            i.setShortTextValue(result);
        }else switch (i.getSelectionDataType()) {
            case Real_Number:
                i.setRealNumberValue(commonController.getDoubleValue(result));
                System.out.println("i.getRealNumberValue() = " + i.getRealNumberValue());
                getFacade().edit(i);
                break;
            case Integer_Number:
                i.setIntegerNumberValue(commonController.getIntegerValue(result));
                getFacade().edit(i);
                break;
            default:
                i.setShortTextValue(result);
                getFacade().edit(i);
                break;
        }

        System.out.println("javaStringToEvaluate = " + javaStringToEvaluate);

    }

    public String evaluateScript(String script) {
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        try {
            return engine.eval(script) + "";
        } catch (ScriptException ex) {
            Logger.getLogger(ClientEncounterComponentItemController.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public ClientEncounterComponentItem findFormsetValue(ClientEncounterComponentItem i, String code) {
        System.out.println("findFormsetValue = ");
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
        String j = "select i from ClientEncounterComponentItem i where i.retired=false "
                + " and i.parentComponent.parentComponent=:pc "
                + " and lower(i.item.code)=:c";
        Map m = new HashMap();
        m.put("pc", i.getParentComponent().getParentComponent());
        m.put("c", code.toLowerCase());
        System.out.println("m = " + m);
        System.out.println("j = " + j);
        return getFacade().findFirstByJpql(j, m);
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
        System.out.println("findReplaceblesInCalculationString");
        System.out.println("text = " + text);

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
                        if (i == 0) {
                            s.setPef(blockParts[0]);
                        } else if (i == 1) {
                            s.setFl(blockParts[1]);
                        } else if (i == 2) {
                            s.setCode(blockParts[2]);
                        } else if (i == 3) {
                            s.setDefaultValue(blockParts[3]);
                        }
                    }
                    s.setInputText(false);
                    s.setSelectOneMenu(true);
                } else {
                    return ss;
                }
                ss.add(s);
            }
        }

        return ss;

    }

    public void save(ClientEncounterComponentItem i) {
        System.out.println("save");
        System.out.println("i = " + i);
        if (i == null) {
            return;
        }
        System.out.println("i.getId() = " + i.getId());
        System.out.println("i.getShortTextValue() = " + i.getShortTextValue());
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
