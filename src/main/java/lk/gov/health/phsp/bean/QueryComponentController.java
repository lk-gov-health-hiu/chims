package lk.gov.health.phsp.bean;

import lk.gov.health.phsp.entity.QueryComponent;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.bean.util.JsfUtil.PersistAction;
import lk.gov.health.phsp.facade.QueryComponentFacade;

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
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.Client;
import lk.gov.health.phsp.entity.ClientEncounterComponentForm;
import lk.gov.health.phsp.entity.DesignComponentForm;
import lk.gov.health.phsp.entity.DesignComponentFormSet;
import lk.gov.health.phsp.entity.Encounter;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Relationship;
import lk.gov.health.phsp.enums.Evaluation;
import lk.gov.health.phsp.enums.QueryLevel;
import lk.gov.health.phsp.enums.QueryOutputType;
import lk.gov.health.phsp.enums.RelationshipType;
import lk.gov.health.phsp.facade.ClientEncounterComponentItemFacade;
import lk.gov.health.phsp.facade.ClientFacade;
import lk.gov.health.phsp.facade.EncounterFacade;
import lk.gov.health.phsp.facade.RelationshipFacade;
import lk.gov.health.phsp.pojcs.Jpq;
import lk.gov.health.phsp.pojcs.Replaceable;
import org.apache.commons.lang3.SerializationUtils;

@Named("queryComponentController")
@SessionScoped
public class QueryComponentController implements Serializable {

    @EJB
    private lk.gov.health.phsp.facade.QueryComponentFacade ejbFacade;
    @EJB
    private ClientEncounterComponentItemFacade itemFacade;
    @EJB
    private ClientFacade clientFacade;
    @EJB
    private EncounterFacade encounterFacade;
    @EJB
    private RelationshipFacade relationshipFacade;

    @Inject
    private WebUserController webUserController;
    @Inject
    private RelationshipController relationshipController;

    private List<QueryComponent> items = null;
    private QueryComponent selected;
    private QueryComponent selectedCretirian;
    private List<QueryComponent> selectedCretiria = null;

    private QueryComponent selectedCategory;
    private QueryComponent selectedSubcategory;
    private QueryComponent selectedForQuery;

    private QueryComponent addingQuery;
    private QueryComponent removingQuery;
    private QueryComponent movingForm;

    private String resultString;
    private List<Client> resultClientList;
    private List<Encounter> resultEncounterList;
    private List<ClientEncounterComponentForm> resultFormList;
    private List<Relationship> resultRelationshipList;

    private Area province;
    private Area district;
    private Area gn;
    private Area moh;
    private Institution institution;
    private Date from;
    private Date to;
    private Date date;
    private Integer year;
    private Integer quarter;
    private Integer month;

    private boolean filterInstitutions;
    private boolean filterDistricts;
    private boolean filterProvices;
    private boolean filterMoh;
    private boolean filterGn;
    private boolean filterFrom;
    private boolean filterTo;
    private boolean filterYear;
    private boolean filterMonth;
    private boolean filterDate;
    private boolean filterQuarter;

    public void fillCriteriaofTheSelectedQuery() {
        selectedCretiria = QueryComponentController.this.fillCriteriaofTheSelectedQuery(selected);
    }

    public List<QueryComponent> fillCriteriaofTheSelectedQuery(QueryComponent set) {
        if (set == null) {
            return new ArrayList<>();
        }
        String j = "Select f from QueryComponent f "
                + "where f.retired=false "
                + " and f.parentComponent=:pc "
                + " order by f.orderNo";
        Map m = new HashMap();
        m.put("pc", set);
        return getFacade().findByJpql(j, m);
    }

    public void addCriterianToTheSelectedQueriesCriteria() {
        if (selected == null) {
            JsfUtil.addErrorMessage("No Formset");
            return;
        }
        if (addingQuery == null) {
            JsfUtil.addErrorMessage("No Form");
            return;
        }
        addingQuery.setParentComponent(selected);
        addingQuery.setCreatedAt(new Date());
        addingQuery.setCreatedBy(webUserController.getLoggedUser());
        getFacade().create(addingQuery);
        fillCriteriaofTheSelectedQuery();
        addingQuery = null;
    }

    public void removeCriterianFromTheSelectedQuery() {
        if (removingQuery == null) {
            JsfUtil.addErrorMessage("No form to remove.");
            return;
        }
        removingQuery.setRetired(true);
        removingQuery.setRetiredAt(new Date());
        removingQuery.setRetiredBy(webUserController.getLoggedUser());
        getFacade().edit(removingQuery);
        fillCriteriaofTheSelectedQuery();
        JsfUtil.addSuccessMessage("Item Removed");
    }

    public void moveUpTheSelectedSet() {
        if (movingForm == null) {
            JsfUtil.addErrorMessage("No form to move.");
            return;
        }
        movingForm.setOrderNo(movingForm.getOrderNo() - 1.5);
        getFacade().edit(movingForm);
        fillCriteriaofTheSelectedQuery();
        Double o = 0.0;
        for (QueryComponent f : getSelectedCretiria()) {
            o = o + 1;
            f.setOrderNo(o);
            getFacade().edit(f);
        }
        fillCriteriaofTheSelectedQuery();
        movingForm = null;
        JsfUtil.addSuccessMessage("Item Moved Up");
    }

    public void moveDownTheSelectedSet() {
        if (movingForm == null) {
            JsfUtil.addErrorMessage("No form to move.");
            return;
        }
        movingForm.setOrderNo(movingForm.getOrderNo() + 1.5);
        getFacade().edit(movingForm);
        fillCriteriaofTheSelectedQuery();
        Double o = 0.0;
        for (QueryComponent f : getSelectedCretiria()) {
            o = o + 1;
            f.setOrderNo(o);
            getFacade().edit(f);
        }
        fillCriteriaofTheSelectedQuery();
        JsfUtil.addSuccessMessage("Item Moved Down");
    }

    public void saveItem() {
        saveItem(selected);
    }

    public void saveItem(QueryComponent saving) {
        if (saving == null) {
            JsfUtil.addErrorMessage("No item selected.");
            return;
        }
        if (saving.getId() == null) {
            saving.setCreatedAt(new Date());
            saving.setCreatedBy(webUserController.getLoggedUser());
            getFacade().create(saving);
            JsfUtil.addSuccessMessage("Saved Successfully.");
        } else {
            saving.setLastEditBy(webUserController.getLoggedUser());
            saving.setLastEditeAt(new Date());
            getFacade().edit(saving);
            JsfUtil.addSuccessMessage("Updated Successfully.");
        }
    }

    public String backToManageQueries() {
        return "/queryComponent/List";
    }

    public String toEditCriterian() {
        if (selectedCretirian == null) {
            JsfUtil.addErrorMessage("Nothing to Edit");
            return "";
        }

        return "/queryComponent/item";
    }

    public QueryComponentController() {
    }

    public String toProcessQuery() {

        return "/queryComponent/process_query";
    }

    public void clearFilters() {
        province = null;
        district = null;
        gn = null;
        moh = null;
        institution = null;
        from = null;
        to = null;
        date = null;
        year = null;
        quarter = null;
        month = null;
    }

    private void querySelectAction() {
        if (selectedForQuery == null) {
            return;
        }
        String filterQuery = selectedForQuery.getFilterQuery();
        filterDistricts = false;
        filterFrom = false;
        filterGn = false;
        filterInstitutions = false;
        filterMoh = false;
        filterProvices = false;
        filterTo = false;
        filterYear = false;
        filterMonth = false;
        filterDate = false;
        filterQuarter = false;
        if (filterQuery == null) {
            return;
        }
        if (filterQuery.toLowerCase().contains("pro")) {
            filterProvices = true;
        }
        if (filterQuery.toLowerCase().contains("dis")) {
            filterDistricts = true;
        }
        if (filterQuery.toLowerCase().contains("moh")) {
            filterMoh = true;
        }
        if (filterQuery.toLowerCase().contains("gn")) {
            filterGn = true;
        }
        if (filterQuery.toLowerCase().contains("ins")) {
            filterInstitutions = true;
        }
        if (filterQuery.toLowerCase().contains("date")) {
            filterDate = true;
        }
        if (filterQuery.toLowerCase().contains("frm")) {
            filterFrom = true;
        }
        if (filterQuery.toLowerCase().contains("to")) {
            filterTo = true;
        }
        if (filterQuery.toLowerCase().contains("year")) {
            filterYear = true;
        }
        if (filterQuery.toLowerCase().contains("qtr")) {
            filterQuarter = true;
        }
        if (filterQuery.toLowerCase().contains("month")) {
            filterMonth = true;
        }

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

    public List<QueryComponent> completeQueryCategories(String qry) {
        String j = "select q from QueryComponent q "
                + " where q.retired=false "
                + " and q.queryLevel =:l "
                + " and lower(q.name) like :q or lower(q.code) like :q "
                + " order by q.name";
        Map m = new HashMap();
        m.put("q", "%" + qry.toLowerCase() + "%");
        m.put("l", QueryLevel.Category);
        return getFacade().findByJpql(j, m);
    }

    public List<QueryComponent> subcategories(QueryComponent p) {
        String j = "select q from QueryComponent q "
                + " where q.retired=false "
                + " and q.queryLevel =:l "
                + " and q.parentComponent =:p "
                + " order by q.name";
        Map m = new HashMap();
        m.put("p", p);
        m.put("l", QueryLevel.Subcategory);
        return getFacade().findByJpql(j, m);
    }

    public List<QueryComponent> queries(QueryComponent p) {
        String j = "select q from QueryComponent q "
                + " where q.retired=false "
                + " and q.queryLevel =:l "
                + " and q.parentComponent =:p "
                + " order by q.name";
        Map m = new HashMap();
        m.put("p", p);
        m.put("l", QueryLevel.Query);
        return getFacade().findByJpql(j, m);
    }

    public List<QueryComponent> criteria(QueryComponent p) {
        String j = "select q from QueryComponent q "
                + " where q.retired=false "
                + " and q.queryLevel =:l "
                + " and q.parentComponent =:p "
                + " order by q.name";
        Map m = new HashMap();
        m.put("p", p);
        m.put("l", QueryLevel.Criterian);
        return getFacade().findByJpql(j, m);
    }

    public QueryComponent findLastQuery(String qry) {
        String j = "select q from QueryComponent q "
                + " where q.retired=false "
                + " and lower(q.name) like :q or lower(q.code) like :q "
                + " order by q.name";
        Map m = new HashMap();
        m.put("q", "%" + qry.toLowerCase() + "%");
        return getFacade().findFirstByJpql(j, m);
    }

    public void processQuery() {
        System.out.println("processQuery");
        System.out.println("selectedForQuery = " + selectedForQuery);
        if (selectedForQuery == null) {
            JsfUtil.addErrorMessage("Nothing selected");
            return;
        }

        resultString = null;
        resultClientList = null;
        resultFormList = null;
        resultEncounterList = null;
        resultRelationshipList = null;

        Jpq qr;

        switch (selectedForQuery.getQueryType()) {
            case Population:
                qr = createAPopulationCountQuery(selectedForQuery);
                if (qr.getLongResult() != null) {
                    resultString = qr.getQc().getName() + " = " + qr.getLongResult();
                }
                resultRelationshipList = qr.getRelationshipList();
                break;

            case Indicator:
                resultString = handleIndicatorQuery(selectedForQuery);
                break;

            case Client:
                qr = createAClientCountQuery(selectedForQuery);
                if (qr.getLongResult() != null) {
                    resultString = qr.getQc().getName() + " = " + qr.getLongResult();
                }
                resultClientList = qr.getClientList();
                break;

            case First_Encounter:
                break;

            case Any_Encounter:
                break;

            case Formset:
                break;

        }

        boolean tf = false;

        if (!tf) {
            return;
        }

        if (selectedForQuery.getIndicatorQuery() != null && !selectedForQuery.getIndicatorQuery().trim().equals("")) {
            resultString = handleIndicatorQuery(selectedForQuery);
        } else if (selectedForQuery.getSelectQuery().trim().equalsIgnoreCase("#{client_count}")) {
            Jpq j = createAClientCountQuery(selectedForQuery);
            resultString = j.getQc().getName() + " = " + j.getLongResult();
        } else if (selectedForQuery.getSelectQuery().trim().equalsIgnoreCase("#{client_list}")) {
            Jpq j = createAClientListQuery(selectedForQuery);
            resultClientList = j.getClientList();
        } else if (selectedForQuery.getSelectQuery().trim().equalsIgnoreCase("#{encounter_list}")) {
            Jpq j = createAnEncounterListQuery(selectedForQuery);
            resultClientList = j.getClientList();
        } else if (selectedForQuery.getFromQuery().trim().equalsIgnoreCase("#{pop}")) {
            Jpq j = createAPopulationCountQuery(selectedForQuery);
            resultString = j.getQc().getName() + " = " + j.getLongResult();
        } else {
            JsfUtil.addSuccessMessage("Feature NOT yet Supported");
        }

        clearFilters();
    }

    public String handleIndicatorQuery(QueryComponent qc) {
        String rs = "Nothing Calculated.";
        List<Replaceable> replaceables = findReplaceblesInIndicatorQuery(qc.getIndicatorQuery());
        for (Replaceable r : replaceables) {
            QueryComponent temqc = findLastQuery(r.getQryCode());
            System.out.println("r.getQryCode() = " + r.getQryCode());
            System.out.println("temqc.getName() = " + temqc.getName());
            if (temqc == null) {
                JsfUtil.addErrorMessage("Wrong Query. Check the names of queries");
                return rs;
            }

            Jpq j;
            if (temqc.getSelectQuery().trim().equalsIgnoreCase("#{client_count}")) {
                j = createAClientCountQuery(temqc);
            } else if (temqc.getFromQuery().trim().equalsIgnoreCase("#{pop}")) {
                j = createAPopulationCountQuery(temqc);
            } else {
                JsfUtil.addErrorMessage("Wrong Query. Check the names of queries");
                return rs;
            }
            System.out.println("j.getLongResult() = " + j.getLongResult());
            r.setSelectedValue(j.getLongResult() + "");
        }
        String javaStringToEvaluate = addTemplateToReport(qc.getIndicatorQuery().trim(), replaceables);
        System.out.println("javaString To Evaluate = \n" + javaStringToEvaluate);
        rs = "Formula \t" + javaStringToEvaluate + "\n";
        String res = evaluateScript(javaStringToEvaluate);
        rs += "Result : " + res;
        return rs;
    }

    public String evaluateScript(String script) {
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        try {
            return engine.eval(script) + "";
        } catch (ScriptException ex) {
            System.out.println("ex = " + ex.getMessage());
            return null;
        }
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

    public void duplicate() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Noting selected.");
            return;
        }
        QueryComponent q = SerializationUtils.clone(selected);
        q.setId(null);
        q.setCreatedAt(new Date());
        q.setCreatedBy(webUserController.getLoggedUser());
        getFacade().create(q);
        items = null;
        selected = q;
        JsfUtil.addSuccessMessage("Duplicated");
    }

    public void retire() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing Selected");
            return;
        }
        selected.setRetired(true);
        selected.setRetiredAt(new Date());
        selected.setRetiredBy(webUserController.getLoggedUser());
        getFacade().edit(selected);
        selected = null;
        items = null;
        JsfUtil.addSuccessMessage("Removed");
    }

    public Jpq createAPopulationCountQuery(QueryComponent qc) {
        System.out.println("createAPopulationCountQuery");
        Jpq jpql = new Jpq();
        jpql.setQc(qc);
        if (qc.getOutputType() == QueryOutputType.Count) {
            jpql.setJselect("select r.longValue1  ");
        } else if (qc.getOutputType() == QueryOutputType.List) {
            jpql.setJselect("select r  ");
        }
        jpql.setJfrom(" from Relationship r ");

        jpql.setJwhere(" where r.retired=:f ");

        if (district != null) {
            jpql.setJwhere(jpql.getJwhere() + " and r.area=:a ");
            jpql.getM().put("a", district);
        }

        if (province != null) {
            jpql.setJwhere(jpql.getJwhere() + " and r.area=:a ");
            jpql.getM().put("a", province);
        }

        RelationshipType t = qc.getPopulationType();
        if (t != null) {
            jpql.setJwhere(jpql.getJwhere() + " and r.relationshipType=:t ");
            jpql.getM().put("t", t);
        }

        if (year != null && year != 0) {
            jpql.setJwhere(jpql.getJwhere() + " and r.yearInt=:y");
            jpql.getM().put("y", year);
        }

        jpql.setJorderBy(" order by r.id desc");
        jpql.getM().put("f", false);

        jpql.setJgroupby("");
        System.out.println("jpql.getM() = " + jpql.getM());
        System.out.println("jpql.getJpql() = " + jpql.getJpql());
        if (qc.getOutputType() == QueryOutputType.Count) {
            jpql.setLongResult(getItemFacade().findLongByJpql(jpql.getJpql(), jpql.getM(), 1));
        } else if (qc.getOutputType() == QueryOutputType.List) {
            jpql.setRelationshipList(getRelationshipFacade().findByJpql(jpql.getJpql(), jpql.getM()));
        }

        return jpql;
    }

    public Jpq createAClientCountQuery(QueryComponent qc) {
        System.out.println("createAClientCountQuery");
        Jpq jpql = new Jpq();
        jpql.setQc(qc);

        List<QueryComponent> criterias = criteria(qc);

        jpql.getM().put("f", false);
        jpql.setJfrom(" from ClientEncounterComponentItem i ");
        jpql.setJwhere(" where i.retired=:f ");
        jpql.setJwhere(jpql.getJwhere() + addFilterStringForClient(jpql.getM()));

        if (criterias == null || criterias.isEmpty()) {
            if (qc.getOutputType() == QueryOutputType.Count) {
                jpql.setJselect("select c from Client c  ");
            } else if (qc.getOutputType() == QueryOutputType.List) {
                jpql.setJselect("select distinct(i)  ");
            }
            System.out.println("j.getJpql() = " + jpql.getJpql());
            System.out.println("j.getM() = " + jpql.getM());
           
            if (qc.getOutputType() == QueryOutputType.Count) {
                jpql.setLongResult(getItemFacade().findLongByJpql(jpql.getJpql(), jpql.getM(), 1));
            } else if (qc.getOutputType() == QueryOutputType.List) {
                jpql.setRelationshipList(getRelationshipFacade().findByJpql(jpql.getJpql(), jpql.getM()));
            }

            return jpql;

        } else {

            if (qc.getOutputType() == QueryOutputType.Count) {
                jpql.setJselect("select count(distinct i.parentComponent.parentComponent.encounter.client)  ");
            } else if (qc.getOutputType() == QueryOutputType.List) {
                jpql.setJselect("select distinct(i)  ");
            }

        }

        List<Replaceable> replaceblesInWhereQuery = findReplaceblesInWhereQuery(qc.getWhereQuery());
        int count = 0;
        for (Replaceable r : replaceblesInWhereQuery) {
            count++;
            String qs = "";
            if (r.isForForm() || r.isForClient()) {
                switch (r.getQueryDataType()) {
                    case it:
                        if (r.getEvaluation() == Evaluation.eq) {
                            qs += " and (i.item.code=:varc" + count + " and i.itemValue.code=:valc" + count + ")";
                            jpql.getM().put("varc" + count, r.getVariableCode());
                            jpql.getM().put("valc" + count, r.getValueCode());
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
                        qs += " and (i.item.code=:varc" + count
                                + " and (i.integerNumberValue" + e + ":val" + count + " ))";
                        jpql.getM().put("val" + count, CommonController.getIntegerValue(r.getValueCode()));
                        jpql.getM().put("varc" + count, r.getVariableCode());
                        break;

                }

            }
            jpql.setJwhere(jpql.getJwhere() + qs + addFilterString(jpql.getM()));

        }

        jpql.setJgroupby("");
        System.out.println("j.getJpql() = " + jpql.getJpql());
        System.out.println("j.getM() = " + jpql.getM());
        jpql.setLongResult(getItemFacade().countByJpql(jpql.getJpql(), jpql.getM()));
        return jpql;
    }

    public Jpq createAClientListQuery(QueryComponent qc) {
        System.out.println("createAClientListQuery");
        Jpq j = new Jpq();
        j.setQc(qc);
        j.setJselect("select distinct i.parentComponent.parentComponent.encounter.client  ");
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
                        qs += " and (i.item.code=:varc" + count
                                + " and (i.integerNumberValue" + e + ":val" + count + " ))";
                        j.getM().put("val" + count, CommonController.getIntegerValue(r.getValueCode()));
                        j.getM().put("varc" + count, r.getVariableCode());
                        break;

                }

            }
            j.setJwhere(j.getJwhere() + qs + addFilterString(j.getM()));

        }

        j.setJgroupby("");
        System.out.println("j.getJpql() = " + j.getJpql());
        System.out.println("j.getM() = " + j.getM());
        j.setClientList(getClientFacade().findByJpql(j.getJpql(), j.getM()));
        return j;
    }

    public Jpq createAnEncounterListQuery(QueryComponent qc) {
        System.out.println("createAClientListQuery");
        Jpq j = new Jpq();
        j.setQc(qc);
        j.setJselect("select distinct i.parentComponent.parentComponent.encounter  ");
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
                        qs += " and (i.item.code=:varc" + count
                                + " and (i.integerNumberValue" + e + ":val" + count + " ))";
                        j.getM().put("val" + count, CommonController.getIntegerValue(r.getValueCode()));
                        j.getM().put("varc" + count, r.getVariableCode());
                        break;

                }

            }
            j.setJwhere(j.getJwhere() + qs + addFilterString(j.getM()));

        }

        j.setJgroupby("");
        System.out.println("j.getJpql() = " + j.getJpql());
        System.out.println("j.getM() = " + j.getM());
        j.setEncounterList(getEncounterFacade().findByJpql(j.getJpql(), j.getM()));
        System.out.println("j.getEncounterList() = " + j.getEncounterList());
        return j;
    }

    public String addFilterStringForClient(Map m) {
        String f = "";
        if (date != null) {
            f += " and c.createdAt=:encounterDate ";
            m.put("encounterDate", date);
        }
        if (from != null && to != null) {
            f += " and c.createdAt between :from and :to ";
            m.put("from", from);
            m.put("to", to);
        } else if (from != null) {
            f += " and c.createdAt > :from ";
            m.put("from", from);
        } else if (to != null) {
            f += " and c.createdAt < :to ";
            m.put("to", to);
        }
        if (province != null) {
            f += " and c.person.gnArea.province =:province ";
            m.put("province", province);
        }
        if (district != null) {
            f += " and c.person.gnArea.district =:district ";
            m.put("district", district);
        }
        if (moh != null) {
            f += " and c.person.gnArea.moh =:moh ";
            m.put("moh", moh);
        }
        if (gn != null) {
            f += " and c.person.gnArea =:moh ";
            m.put("gn", gn);
        }
        if (institution != null) {
            f += " and (c.createdInstitution =:institution "
                    + " or  c.createdInstitution.parent =:institution "
                    + " or c.createdInstitution.parent.parent =:institution "
                    + " or c.createdInstitution.parent.parent.parent =:institution "
                    + " or c.createdInstitution.parent.parent.parent.parent =:institution ) ";
            m.put("institution", institution);
        }

        Encounter e;
        Client c;
        Institution i;

        return f;
    }

    public String addFilterString(Map m) {
        String f = "";
        if (date != null) {
            f += " and i.parentComponent.parentComponent.encounter.encounterDate=:encounterDate ";
            m.put("encounterDate", date);
        }
        if (from != null && to != null) {
            f += " and i.parentComponent.parentComponent.encounter.encounterDate between :from and :to ";
            m.put("from", from);
            m.put("to", to);
        } else if (from != null) {
            f += " and i.parentComponent.parentComponent.encounter.encounterDate > :from ";
            m.put("from", from);
        } else if (to != null) {
            f += " and i.parentComponent.parentComponent.encounter.encounterDate < :to ";
            m.put("to", to);
        }
        if (province != null) {
            f += " and i.parentComponent.parentComponent.encounter.client.person.gnArea.province =:province ";
            m.put("province", province);
        }
        if (district != null) {
            f += " and i.parentComponent.parentComponent.encounter.client.person.gnArea.district =:district ";
            m.put("district", district);
        }
        if (moh != null) {
            f += " and i.parentComponent.parentComponent.encounter.client.person.gnArea.moh =:moh ";
            m.put("moh", moh);
        }
        if (gn != null) {
            f += " and i.parentComponent.parentComponent.encounter.client.person.gnArea =:moh ";
            m.put("gn", gn);
        }
        if (institution != null) {
            f += " and (i.parentComponent.parentComponent.encounter.institution =:institution "
                    + " or  i.parentComponent.parentComponent.encounter.institution.parent =:institution "
                    + " or i.parentComponent.parentComponent.encounter.institution.parent.parent =:institution "
                    + " or i.parentComponent.parentComponent.encounter.institution.parent.parent.parent =:institution "
                    + " or i.parentComponent.parentComponent.encounter.institution.parent.parent.parent.parent =:institution ) ";
            m.put("institution", institution);
        }

        Encounter e;
        Client c;
        Institution i;

        return f;
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

    public List<Replaceable> findReplaceblesInIndicatorQuery(String text) {
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
                s.setQryCode(block);
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

    public String getResultString() {
        return resultString;
    }

    public void setResultString(String resultString) {
        this.resultString = resultString;
    }

    public boolean isFilterInstitutions() {
        return filterInstitutions;
    }

    public void setFilterInstitutions(boolean filterInstitutions) {
        this.filterInstitutions = filterInstitutions;
    }

    public boolean isFilterDistricts() {
        return filterDistricts;
    }

    public void setFilterDistricts(boolean filterDistricts) {
        this.filterDistricts = filterDistricts;
    }

    public boolean isFilterProvices() {
        return filterProvices;
    }

    public void setFilterProvices(boolean filterProvices) {
        this.filterProvices = filterProvices;
    }

    public boolean isFilterMoh() {
        return filterMoh;
    }

    public void setFilterMoh(boolean filterMoh) {
        this.filterMoh = filterMoh;
    }

    public boolean isFilterGn() {
        return filterGn;
    }

    public void setFilterGn(boolean filterGn) {
        this.filterGn = filterGn;
    }

    public boolean isFilterFrom() {
        return filterFrom;
    }

    public void setFilterFrom(boolean filterFrom) {
        this.filterFrom = filterFrom;
    }

    public boolean isFilterTo() {
        return filterTo;
    }

    public void setFilterTo(boolean filterTo) {
        this.filterTo = filterTo;
    }

    public boolean isFilterYear() {
        return filterYear;
    }

    public void setFilterYear(boolean filterYear) {
        this.filterYear = filterYear;
    }

    public boolean isFilterMonth() {
        return filterMonth;
    }

    public void setFilterMonth(boolean filterMonth) {
        this.filterMonth = filterMonth;
    }

    public boolean isFilterDate() {
        return filterDate;
    }

    public void setFilterDate(boolean filterDate) {
        this.filterDate = filterDate;
    }

    public boolean isFilterQuarter() {
        return filterQuarter;
    }

    public void setFilterQuarter(boolean filterQuarter) {
        this.filterQuarter = filterQuarter;
    }

    public Area getProvince() {
        return province;
    }

    public void setProvince(Area province) {
        this.province = province;
    }

    public Area getDistrict() {
        return district;
    }

    public void setDistrict(Area district) {
        this.district = district;
    }

    public Area getGn() {
        return gn;
    }

    public void setGn(Area gn) {
        this.gn = gn;
    }

    public Area getMoh() {
        return moh;
    }

    public void setMoh(Area moh) {
        this.moh = moh;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Date getTo() {
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getQuarter() {
        return quarter;
    }

    public void setQuarter(Integer quarter) {
        this.quarter = quarter;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public ClientFacade getClientFacade() {
        return clientFacade;
    }

    public List<Client> getResultClientList() {
        return resultClientList;
    }

    public void setResultClientList(List<Client> resultClientList) {
        this.resultClientList = resultClientList;
    }

    public List<Encounter> getResultEncounterList() {
        return resultEncounterList;
    }

    public void setResultEncounterList(List<Encounter> resultEncounterList) {
        this.resultEncounterList = resultEncounterList;
    }

    public List<ClientEncounterComponentForm> getResultFormList() {
        return resultFormList;
    }

    public void setResultFormList(List<ClientEncounterComponentForm> resultFormList) {
        this.resultFormList = resultFormList;
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public RelationshipController getRelationshipController() {
        return relationshipController;
    }

    public QueryComponent getSelectedForQuery() {
        return selectedForQuery;
    }

    public void setSelectedForQuery(QueryComponent selectedForQuery) {
        querySelectAction();
        this.selectedForQuery = selectedForQuery;
    }

    public EncounterFacade getEncounterFacade() {
        return encounterFacade;
    }

    public void setEncounterFacade(EncounterFacade encounterFacade) {
        this.encounterFacade = encounterFacade;
    }

    public QueryComponent getSelectedCategory() {
        return selectedCategory;
    }

    public void setSelectedCategory(QueryComponent selectedCategory) {
        this.selectedCategory = selectedCategory;
    }

    public QueryComponent getSelectedSubcategory() {
        return selectedSubcategory;
    }

    public void setSelectedSubcategory(QueryComponent selectedSubcategory) {
        this.selectedSubcategory = selectedSubcategory;
    }

    public List<QueryComponent> getSelectedCretiria() {
        return selectedCretiria;
    }

    public void setSelectedCretiria(List<QueryComponent> selectedCretiria) {
        this.selectedCretiria = selectedCretiria;
    }

    public QueryComponent getAddingQuery() {
        if (addingQuery == null) {
            addingQuery = new QueryComponent();
        }

        return addingQuery;
    }

    public void setAddingQuery(QueryComponent addingQuery) {
        this.addingQuery = addingQuery;
    }

    public QueryComponent getRemovingQuery() {
        return removingQuery;
    }

    public void setRemovingQuery(QueryComponent removingQuery) {
        this.removingQuery = removingQuery;
    }

    public QueryComponent getSelectedCretirian() {
        return selectedCretirian;
    }

    public void setSelectedCretirian(QueryComponent selectedCretirian) {
        this.selectedCretirian = selectedCretirian;
    }

    public QueryComponent getMovingForm() {
        return movingForm;
    }

    public void setMovingForm(QueryComponent movingForm) {
        this.movingForm = movingForm;
    }

    public RelationshipFacade getRelationshipFacade() {
        return relationshipFacade;
    }

    public void setRelationshipFacade(RelationshipFacade relationshipFacade) {
        this.relationshipFacade = relationshipFacade;
    }

    public List<Relationship> getResultRelationshipList() {
        return resultRelationshipList;
    }

    public void setResultRelationshipList(List<Relationship> resultRelationshipList) {
        this.resultRelationshipList = resultRelationshipList;
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
