package lk.gov.health.phsp.bean;

import lk.gov.health.phsp.entity.QueryComponent;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.bean.util.JsfUtil.PersistAction;
import lk.gov.health.phsp.facade.QueryComponentFacade;

import java.io.Serializable;
import java.util.ArrayList;
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
import lk.gov.health.phsp.enums.Evaluation;
import lk.gov.health.phsp.facade.ClientEncounterComponentItemFacade;
import lk.gov.health.phsp.pojcs.Jpq;
import lk.gov.health.phsp.pojcs.Replaceable;

@Named("queryComponentController")
@SessionScoped
public class QueryComponentController implements Serializable {

    @EJB
    private lk.gov.health.phsp.facade.QueryComponentFacade ejbFacade;
    @EJB
    private ClientEncounterComponentItemFacade itemFacade;
    private List<QueryComponent> items = null;
    private QueryComponent selected;
    private String resultValue;

    public QueryComponentController() {
    }

    public String toProcessQuery() {

        return "/queryComponent/process_query";
    }

    public List<QueryComponent> completeQueries(String qry) {
        String j = "select q from QueryComponent q "
                + " where q.retired=false "
                + " and lower(q.name) like :q or lower(q.code) like :q "
                + " order by q.name";
        Map m = new HashMap();
        m.put("q", "%" + qry.toLowerCase() + "%");
        return getFacade().findByJpql(j, m);
    }

    public void processQuery() {
        System.out.println("processQuery");
        if (selected == null) {
            return;
        }
        if (selected.getSelectQuery().trim().equalsIgnoreCase("#{client_count}")) {
            Jpq j = createAClientCountQuery(selected);
            resultValue = j.getQc().getName() + " = " + j.getLongResult();
        }
    }

    public Jpq createAClientCountQuery(QueryComponent qc) {
        System.out.println("createAClientCountQuery");
        Jpq j = new Jpq();
        j.setQc(qc);
        j.setJselect("select count(distinct i.parentComponent.parentComponent.encounter.client)  ");
        j.setJfrom(" from ClientEncounterComponentItem i ");
        j.setJwhere(" where i.retired=:f ");
        j.getM().put("f", false);
        List<Replaceable> replaceblesInWhereQuery = findReplaceblesInWhereQuery(qc.getWhereQuery());
        int count = 0;
        for (Replaceable r : replaceblesInWhereQuery) {
            count++;
            String qs = "";
            if (r.isForForm()) {
                switch (r.getQueryDataType()) {
                    case it:
                        if (r.getEvaluation() == Evaluation.eq) {
                            qs += " and (i.item.code=:varc" + count + " and i.itemValue.code=:valc" + count + ")";
                            j.getM().put("varc" + count, r.getVariableCode());
                            j.getM().put("valc" + count, r.getValueCode());
                        }
                        break;
                    case in:
                        String e = "";
                        switch (r.getEvaluation()) {
                            case eq:
                                e = "==";
                                break;
                            case ge:
                                e = ">=";
                                break;
                            case gt:
                                e = ">";
                                break;
                            case in:
                                e = " is null ";
                                break;
                            case le:
                                e = "<=";
                                break;
                            case lt:
                                e = "<";
                                break;
                            case ne:
                                e = "!=";
                                break;
                            case nn:
                                e = " is not null ";
                                break;
                        }
                        qs += " and (i.item.code=:varc" + count +
                                " and (i.integerNumberValue" + e + ":val" + count + " ))";
                        j.getM().put("val"+count, CommonController.getIntegerValue(r.getValueCode()));
                        j.getM().put("varc"+count, r.getVariableCode());
                        break;

                }

            }
            j.setJwhere(j.getJwhere() + qs);
        }
        j.setJgroupby("");
        System.out.println("j.getJpql() = " + j.getJpql());
        System.out.println("j.getM() = " + j.getM());
        j.setLongResult(getItemFacade().countByJpql(j.getJpql(), j.getM()));
        return j;
    }

    public List<Replaceable> findReplaceblesInWhereQuery(String text) {
        // System.out.println("findReplaceblesInWhereQuery");
        // System.out.println("text = " + text);

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
                                s.setVariableCode(blockParts[1]);
                                break;
                            case 2:
                                s.setStrEvaluation(blockParts[2]);
                                break;
                            case 3:
                                s.setStrQueryDataType(blockParts[3]);
                                break;
                            case 4:
                                s.setValueCode(blockParts[4]);
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

    public QueryComponent getSelected() {
        return selected;
    }

    public void setSelected(QueryComponent selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private QueryComponentFacade getFacade() {
        return ejbFacade;
    }

    public QueryComponent prepareCreate() {
        selected = new QueryComponent();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/BundleQuery").getString("QueryComponentCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/BundleQuery").getString("QueryComponentUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/BundleQuery").getString("QueryComponentDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<QueryComponent> getItems() {
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
                    JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/BundleQuery").getString("PersistenceErrorOccured"));
                }
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/BundleQuery").getString("PersistenceErrorOccured"));
            }
        }
    }

    public QueryComponent getQueryComponent(java.lang.Long id) {
        return getFacade().find(id);
    }

    public List<QueryComponent> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<QueryComponent> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    public ClientEncounterComponentItemFacade getItemFacade() {
        return itemFacade;
    }

    public lk.gov.health.phsp.facade.QueryComponentFacade getEjbFacade() {
        return ejbFacade;
    }

    public String getResultValue() {
        return resultValue;
    }

    public void setResultValue(String resultValue) {
        this.resultValue = resultValue;
    }

    @FacesConverter(forClass = QueryComponent.class)
    public static class QueryComponentControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            QueryComponentController controller = (QueryComponentController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "queryComponentController");
            return controller.getQueryComponent(getKey(value));
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
            if (object instanceof QueryComponent) {
                QueryComponent o = (QueryComponent) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), QueryComponent.class.getName()});
                return null;
            }
        }

    }

}
