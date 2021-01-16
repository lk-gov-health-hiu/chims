package lk.gov.health.phsp.bean;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import lk.gov.health.phsp.entity.QueryComponent;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.bean.util.JsfUtil.PersistAction;
import lk.gov.health.phsp.facade.QueryComponentFacade;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
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
import lk.gov.health.phsp.entity.ClientEncounterComponentItem;
import lk.gov.health.phsp.entity.Encounter;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.entity.Relationship;
import lk.gov.health.phsp.entity.Upload;
import lk.gov.health.phsp.enums.EncounterType;
import lk.gov.health.phsp.enums.Evaluation;
import lk.gov.health.phsp.enums.Month;
import lk.gov.health.phsp.enums.QueryCriteriaMatchType;
import lk.gov.health.phsp.enums.QueryFilterAreaType;
import lk.gov.health.phsp.enums.QueryFilterPeriodType;
import lk.gov.health.phsp.enums.QueryLevel;
import lk.gov.health.phsp.enums.QueryOutputType;
import lk.gov.health.phsp.enums.QueryType;
import lk.gov.health.phsp.enums.RelationshipType;
import lk.gov.health.phsp.enums.SelectionDataType;
import lk.gov.health.phsp.facade.ClientEncounterComponentItemFacade;
import lk.gov.health.phsp.facade.ClientFacade;
import lk.gov.health.phsp.facade.EncounterFacade;
import lk.gov.health.phsp.facade.RelationshipFacade;
import lk.gov.health.phsp.pojcs.EncounterBasicData;
import lk.gov.health.phsp.pojcs.EncounterWithComponents;
import lk.gov.health.phsp.pojcs.Jpq;
import lk.gov.health.phsp.pojcs.QueryResult;
import lk.gov.health.phsp.pojcs.QueryWithCriteria;
import lk.gov.health.phsp.pojcs.Replaceable;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

@Named("queryComponentController")
@SessionScoped
public class QueryComponentController implements Serializable {

    @EJB
    private QueryComponentFacade ejbFacade;
    @EJB
    private ClientEncounterComponentItemFacade itemFacade;
    @EJB
    private ClientFacade clientFacade;
    @EJB
    private EncounterFacade encounterFacade;
    @EJB
    private RelationshipFacade relationshipFacade;
    @EJB
    private ClientEncounterComponentItemFacade clientEncounterComponentItemFacade;

    @Inject
    private WebUserController webUserController;
    @Inject
    private RelationshipController relationshipController;
    @Inject
    private AreaController areaController;
    @Inject
    private ApplicationController applicationController;
    @Inject
    private UserTransactionController userTransactionController;
    @Inject
    UploadController uploadController;

    private List<QueryComponent> items = null;
    private List<QueryComponent> categories = null;
    private List<QueryComponent> excels = null;
    private List<QueryComponent> indicators = null;
    private List<QueryComponent> populations = null;
    private List<QueryComponent> counts = null;

    private QueryComponent selected;
    private QueryComponent selectedCountCriteria;
    private QueryComponent selectedQuery;
    private QueryComponent selectedToDuplicateQuery;
    private QueryComponent selectedCretirian;
    private List<QueryComponent> selectedCretiria = null;
    private List<QueryComponent> criteriaOfSelectedCount;

    private QueryComponent selectedCategory;
    private QueryComponent selectedSubcategory;
    private QueryComponent selectedForQuery;
    
    private QueryComponent selectedCount;
    private QueryComponent selectedPopulation;

    private QueryComponent addingQuery;
    QueryComponent addingCategory;
    QueryComponent addingSubcategory;
    QueryComponent addingCriterian;

    private QueryComponent removing;
    private QueryComponent moving;

    private String resultString;
    private List<Client> resultClientList;
    private List<Encounter> resultEncounterList;
    private List<ClientEncounterComponentForm> resultFormList;
    private List<Relationship> resultRelationshipList;

    private List<QueryResult> qrs = null;
    private QueryResult qr = null;

    private String chartString;

    private Area province;
    private Area district;
    private Area gn;
    private Area moh;
    private Area phm;
    private Institution institution;
    private Date from;
    private Date to;
    private Date date;
    private Integer year;
    private Integer quarter;
    private Integer month;
    private Month monthEnum;

    private boolean filterInstitutions;
    private boolean filterDistricts;
    private boolean filterProvices;
    private boolean filterMoh;
    private boolean filterPhm;
    private boolean filterGn;
    private boolean filterFrom;
    private boolean filterTo;
    private boolean filterYear;
    private boolean filterMonth;
    private boolean filterDate;
    private boolean filterQuarter;

    private QueryFilterPeriodType periodType;
    private QueryFilterAreaType areaType;

    private String searchText;

    private StreamedContent resultExcelFile;

    private StreamedContent downloadingFile;

    public String toManageExcelTemplates() {
        userTransactionController.recordTransaction("Manage Excel Templates");
        return "/queryComponent/excel";
    }

    public String toAddExcelTemplates() {
        userTransactionController.recordTransaction("Add New Excel Templates");
        selected = new QueryComponent();
        selected.setQueryType(QueryType.Excel_Report);
        return "/queryComponent/edit_excel";
    }

    public String toAddIndicator() {
        userTransactionController.recordTransaction("Add New Indicator");
        selected = new QueryComponent();
        selected.setQueryType(QueryType.Indicator);
        return "/queryComponent/edit_indicator";
    }

    public String toAddPopulation() {
        userTransactionController.recordTransaction("Add New Population");
        selected = new QueryComponent();
        selected.setQueryType(QueryType.Population);
        return "/queryComponent/edit_population";
    }

    public String toAddCount() {
        userTransactionController.recordTransaction("Add New Count");
        selected = new QueryComponent();
        selected.setQueryType(QueryType.Encounter_Count);
        return "/queryComponent/edit_count";
    }

    public String toManageIndicators() {
        userTransactionController.recordTransaction("Manage Indicators");
        return "/queryComponent/indicators";
    }

    public String toManagePopulations() {
        userTransactionController.recordTransaction("Manage Populations");
        return "/queryComponent/populations";
    }

    public String toManageCounts() {
        userTransactionController.recordTransaction("Manage Counts");
        return "/queryComponent/counts";
    }

    public String toEditExcelTemplate() {
        userTransactionController.recordTransaction("Edit Excel Templates");
        return "/queryComponent/edit_excel";
    }

    public String toEditIndicator() {
        userTransactionController.recordTransaction("Edit Indicator");
        return "/queryComponent/edit_indicator";
    }

    public String toEditPopulation() {
        userTransactionController.recordTransaction("Edit Population");
        return "/queryComponent/edit_population";
    }

    public String toEditCount() {
        userTransactionController.recordTransaction("Edit Count");
        return "/queryComponent/edit_count";
    }

    public String toManageAnalysis() {
        return "/analysis/index";
    }

    public String toListAnalysis() {
        items = getApplicationController().getQueryComponents();
        getApplicationController().getQueryComponents();
        return "/analysis/list";
    }

    public String toSearchAnalysis() {
        items = null;
        clearSearchText();
        return "/analysis/search";
    }

    public String toCreateNewAnalysis() {
        selected = new QueryComponent();
        return "/analysis/analysis";
    }

    public String toAddNewCriteriaeForCount() {
        selectedCountCriteria = new QueryComponent();
        selectedCountCriteria.setParentComponent(selectedCount);
        selectedCountCriteria.setQueryLevel(QueryLevel.Criterian);
        return toEditCountCriteriea();
    }

//    public void listQueries() {
//        listQueries(searchText);
//    }
    
    
    
    
    
    public List<Item> getItemsForCountCriteriaItems() {
        System.out.println("getItemsInDesignFormItemValues");
        System.out.println("getSelectedCountCriteria() = " + getSelectedCountCriteria());
        if (getSelectedCountCriteria() == null || getSelectedCountCriteria().getItem() == null) {
            System.out.println("Null Error Return back");
            return new ArrayList<>();
        }
        System.out.println("getSelectedCountCriteria().getItem() = " + getSelectedCountCriteria().getItem());
        String j = "select distinct(di.categoryOfAvailableItems) "
                + " from DesignComponentFormItem di "
                + " where di.retired<>:ret "
                + " and lower(di.item.code)=:qry ";
        Map m = new HashMap();
        m.put("ret", true);
        m.put("qry", getSelectedCountCriteria().getItem().getCode().trim().toLowerCase());
        
        System.out.println("m = " + m);
        System.out.println("j = " + j);
        
        List<Item> parentItems = getItemFacade().findByJpql(j, m);
        
        System.out.println("parentItems = " + parentItems);
        
        List<Item> temItsm = new ArrayList<>();

        if (parentItems == null || parentItems.isEmpty()) {
            return new ArrayList<>();
        }

        for (Item i : parentItems) {
            if (i == null || i.getCode() == null) {
                continue;
            }
            j = "select i from Item i "
                    + "where i.retired<>:ret "
                    + " and lower(i.parent.code)=:code "
                    + "";
            m = new HashMap();
            m.put("ret", true);
            m.put("code", i.getCode());
            
            System.out.println("j 1 = " +j);
            System.out.println("m 1 = " + m);
            
            List<Item> temIt = getItemFacade().findByJpql(j, m);
            
            System.out.println("temIt = " + temIt);
            
            if (temIt != null) {
                temItsm.addAll(temIt);
            }
        }
        
        System.out.println("temItsm = " + temItsm);
        
        return temItsm;
    }
    
    public List<Item> getItemsInDesignFormItemValues() {
        System.out.println("getItemsInDesignFormItemValues");
        System.out.println("getSelected() = " + getSelected());
        if (getSelected() == null || getSelected().getItem() == null) {
            System.out.println("Null Error Return back");
            return new ArrayList<>();
        }
        System.out.println("getSelected().getItem() = " + getSelected().getItem());
        String j = "select distinct(di.categoryOfAvailableItems) "
                + " from DesignComponentFormItem di "
                + " where di.retired<>:ret "
                + " and lower(di.item.code)=:qry ";
        Map m = new HashMap();
        m.put("ret", true);
        m.put("qry", getSelected().getItem().getCode().trim().toLowerCase());
        
        System.out.println("m = " + m);
        System.out.println("j = " + j);
        
        List<Item> parentItems = getItemFacade().findByJpql(j, m);
        
        System.out.println("parentItems = " + parentItems);
        
        List<Item> temItsm = new ArrayList<>();

        if (parentItems == null || parentItems.isEmpty()) {
            return new ArrayList<>();
        }

        for (Item i : parentItems) {
            if (i == null || i.getCode() == null) {
                continue;
            }
            j = "select i from Item i "
                    + "where i.retired<>:ret "
                    + " and lower(i.parent.code)=:code "
                    + "";
            m = new HashMap();
            m.put("ret", true);
            m.put("code", i.getCode());
            
            System.out.println("j 1 = " +j);
            System.out.println("m 1 = " + m);
            
            List<Item> temIt = getItemFacade().findByJpql(j, m);
            
            System.out.println("temIt = " + temIt);
            
            if (temIt != null) {
                temItsm.addAll(temIt);
            }
        }
        
        System.out.println("temItsm = " + temItsm);
        
        return temItsm;
    }

//    public void listQueries(String strSearch) {
//        String j;
//        Map m = new HashMap();
//        j = "select q from QueryComponent q "
//                + " where q.retired<>:ret ";
//        m.put("ret", true);
//        if (strSearch != null && !strSearch.trim().equals("")) {
//            j = j + " lower(q.name) like :n or lower(q.code) like :q ";
//            m.put("n", "%" + strSearch.trim().toLowerCase() + "%");
//        }
//        j = j + " order by q.name";
//        items = getFacade().findByJpql(j, m);
//
//    }
    public void clearSearchText() {
        searchText = "";
    }

    public void fillCriteriaofTheSelectedQuery() {
        selectedCretiria = QueryComponentController.this.fillCriteriaofTheSelectedQuery(selectedQuery);
    }

    public void saveCategory() {
        if (addingCategory == null) {
            JsfUtil.addErrorMessage("Nothing to save");
            return;
        }
        saveItem(addingCategory);
        categories = null;
        addingCategory = null;
    }

    public void saveExcel() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing to save");
            return;
        }
        selected.setQueryType(QueryType.Excel_Report);
        saveItem(selected);
        excels = null;
    }

    public void saveIndicator() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing to save");
            return;
        }
        selected.setQueryType(QueryType.Indicator);
        saveItem(selected);
        indicators = null;
    }

    public void savePopulation() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing to save");
            return;
        }
        selected.setQueryType(QueryType.Population);
        saveItem(selected);
        populations = null;
    }

    public void saveCount() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing to save");
            return;
        }
        if (selected.getQueryType() == null) {
            selected.setQueryType(QueryType.Encounter_Count);
        }
        saveItem(selected);
        counts = null;
    }

    public void saveSubCategory() {
        if (addingSubcategory == null) {
            JsfUtil.addErrorMessage("Nothing to save");
            return;
        }
        if (selectedCategory == null) {
            JsfUtil.addErrorMessage("No Category");
            return;
        }
        addingSubcategory.setParentComponent(selectedCategory);
        saveItem(addingSubcategory);
        addingSubcategory = null;
    }

    public void saveQuery() {
        if (addingQuery == null) {
            JsfUtil.addErrorMessage("Nothing to save");
            return;
        }
        if (selectedSubcategory == null) {
            JsfUtil.addErrorMessage("No Subcategory");
            return;
        }
        addingQuery.setParentComponent(selectedSubcategory);
        saveItem(addingQuery);
        addingQuery = null;
    }

    public String saveQueryAndEdit() {
        if (addingQuery == null) {
            JsfUtil.addErrorMessage("Nothing to save");
            return "";
        }
        if (selectedSubcategory == null) {
            JsfUtil.addErrorMessage("No Subcategory");
            return "";
        }
        addingQuery.setParentComponent(selectedSubcategory);
        saveItem(addingQuery);
        selected = addingQuery;
        addingQuery = null;
        return "edit_query";
    }

    public void saveCriterian() {
        if (addingCriterian == null) {
            JsfUtil.addErrorMessage("Nothing to save");
            return;
        }
        if (selectedQuery == null) {
            JsfUtil.addErrorMessage("No Subcategory");
            return;
        }
        addingCriterian.setParentComponent(selectedQuery);
        saveItem(addingCriterian);
        addingCriterian = null;
    }

    public List<QueryComponent> fillCriteriaofTheSelectedQuery(QueryComponent set) {
        if (set == null) {
            return new ArrayList<>();
        }

        List<QueryComponent> tqcs = applicationController.getQueryComponents();
        List<QueryComponent> nqs = new ArrayList<>();
        for (QueryComponent q : tqcs) {
            if (q.getParentComponent().equals(set)) {
                nqs.add(q);
            }
        }
        return nqs;

    }

    public void addCriterianToTheSelectedQueriesCriteria() {
        if (selectedQuery == null) {
            JsfUtil.addErrorMessage("No Formset");
            return;
        }
        if (addingQuery == null) {
            JsfUtil.addErrorMessage("No Form");
            return;
        }
        addingQuery.setParentComponent(selectedQuery);
        addingQuery.setCreatedAt(new Date());
        addingQuery.setCreatedBy(webUserController.getLoggedUser());
        getFacade().create(addingQuery);
        fillCriteriaofTheSelectedQuery();
        addingQuery = null;
    }

    public void removeExcel() {
        remove();
        fillExcels();
    }

    public void removeIndicator() {
        remove();
        fillIndicators();
    }

    public void removeCount() {
        remove();
        fillCountsAndPopulations();
    }

    public void removeQuery() {
        remove();

    }

    public void remove() {
        if (removing == null) {
            JsfUtil.addErrorMessage("No form to remove.");
            return;
        }
        removing.setRetired(true);
        removing.setRetiredAt(new Date());
        removing.setRetiredBy(webUserController.getLoggedUser());
        getFacade().edit(removing);
        if (removing.getQueryLevel() == QueryLevel.Category) {
            categories = null;
        }
        removing = null;
        try {
            applicationController.getQueryComponents().remove(removing);
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e.getMessage());
        }
        JsfUtil.addSuccessMessage("Removed");
    }

    public void moveUpTheSelectedSet() {
        if (moving == null) {
            JsfUtil.addErrorMessage("No form to move.");
            return;
        }
        moving.setOrderNo(moving.getOrderNo() - 1.5);
        getFacade().edit(moving);
        fillCriteriaofTheSelectedQuery();
        Double o = 0.0;
        for (QueryComponent f : getSelectedCretiria()) {
            o = o + 1;
            f.setOrderNo(o);
            getFacade().edit(f);
        }
        fillCriteriaofTheSelectedQuery();
        moving = null;
        JsfUtil.addSuccessMessage("Item Moved Up");
    }

    public void moveDownTheSelectedSet() {
        if (moving == null) {
            JsfUtil.addErrorMessage("No form to move.");
            return;
        }
        moving.setOrderNo(moving.getOrderNo() + 1.5);
        getFacade().edit(moving);
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

    public void saveSelectedItem() {
        saveItem(selected);
    }

    public void saveSelectedCountCriteria() {
        saveItem(selectedCountCriteria);
    }

    public String saveSelectedItemAndCriteria() {
        saveItem(selected);
        selectedQuery = selected;
        return "criteria";
    }

    public String saveCountAndToManageCriteria() {
        saveCount();
        selectedCount = selected;
        return "/queryComponent/count_criteria";
    }

    public String toCountCriteria() {
        return "/queryComponent/count_criteria";
    }

    public String toEditCountCriteriea() {
        return "/queryComponent/edit_count_criteriea";
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
            applicationController.getQueryComponents().add(saving);
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

    public String toQueriesAll() {
        userTransactionController.recordTransaction("To Queries - All");
        return "/queryComponent/query_process";
    }

    public String toQueriesMonthlyGn() {
        userTransactionController.recordTransaction("To Monthly Queries - GN Areas");
        return "/queryComponent/query_monthly_gn";
    }

    public String toQueryIndex() {
        userTransactionController.recordTransaction("To Query Index");
        return "/queryComponent/query_index";
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

    public void areaFilterSelectAction() {
        filterProvices = false;
        filterDistricts = false;
        filterMoh = false;
        filterPhm = false;
        filterGn = false;
        switch (areaType) {
            case National:
            case District_List:
            case Province_List:
                break;
            case Province:
            case Province_District_list:
                filterProvices = true;
                break;
            case Distirct:
            case District_MOH_List:
                filterDistricts = true;
                break;
            case MOH:
            case MOH_GN_List:
            case MOH_PHM_List:
                filterMoh = true;
                break;
            case PHM:
            case PHM_GN_List:
                filterPhm = true;
                break;
            case GN:
                filterGn = true;
        }
    }

    public void periodFilterSelectAction() {
        filterFrom = false;
        filterTo = false;
        filterYear = false;
        filterMonth = false;
        filterDate = false;
        filterQuarter = false;
        if (periodType == null) {
            return;
        }
        switch (periodType) {
            case Year:
                filterYear = true;
                break;
            case Quarter:
                filterYear = true;
                filterQuarter = true;
                break;
            case After:
                filterFrom = true;
                break;
            case Before:
                filterTo = true;
                break;
            case Period:
                filterFrom = true;
                filterTo = true;
                return;
            case All:
        }
    }

    public List<QueryComponent> completeQueries(String qry) {
        List<QueryComponent> tls = applicationController.getQueryComponents();
        List<QueryComponent> sls = new ArrayList<>();
        qry = qry.trim().toLowerCase();

        for (QueryComponent qc : tls) {
            if (qc.getName().toLowerCase().contains(qry) || qc.getName().toLowerCase().contains(qry)) {
                sls.add(qc);
            }
        }
        return sls;
    }

    public List<QueryComponent> completeExcelReports(String qry) {
        List<QueryComponent> tls = applicationController.getQueryComponents();
        List<QueryComponent> sls = new ArrayList<>();
        qry = qry.trim().toLowerCase();

        for (QueryComponent qc : tls) {
            if (qc.getQueryType() == QueryType.Excel_Report) {
                if (qc.getName().toLowerCase().contains(qry) || qc.getName().toLowerCase().contains(qry)) {
                    sls.add(qc);
                }
            }
        }
        return sls;
    }

    public List<QueryComponent> completeIndicators(String qry) {
        List<QueryComponent> tls = applicationController.getQueryComponents();
        List<QueryComponent> sls = new ArrayList<>();
        qry = qry.trim().toLowerCase();

        for (QueryComponent qc : tls) {
            if (qc.getQueryType() == QueryType.Indicator) {
                if (qc.getName().toLowerCase().contains(qry) || qc.getName().toLowerCase().contains(qry)) {
                    sls.add(qc);
                }
            }
        }
        return sls;
    }

    public List<QueryComponent> completePopulations(String qry) {
        List<QueryComponent> tls = applicationController.getQueryComponents();
        List<QueryComponent> sls = new ArrayList<>();
        qry = qry.trim().toLowerCase();

        for (QueryComponent qc : tls) {
            if (qc.getQueryType() == QueryType.Population) {
                if (qc.getName().toLowerCase().contains(qry) || qc.getName().toLowerCase().contains(qry)) {
                    sls.add(qc);
                }
            }
        }
        return sls;
    }

    public List<QueryComponent> completeCounts(String qry) {
        List<QueryComponent> tls = applicationController.getQueryComponents();
        List<QueryComponent> sls = new ArrayList<>();
        qry = qry.trim().toLowerCase();

        for (QueryComponent qc : tls) {
            if (qc.getQueryType() == QueryType.Encounter_Count || qc.getQueryType() == QueryType.Client_Count) {
                if (qc.getName().toLowerCase().contains(qry) || qc.getName().toLowerCase().contains(qry)) {
                    sls.add(qc);
                }
            }
        }
        return sls;
    }

    public List<QueryComponent> completeQueryCategories(String qry) {
        List<QueryComponent> nqs = new ArrayList<>();
        if (qry == null) {
            return nqs;
        }
        if (qry.trim().equals("")) {
            return nqs;
        } else {
            qry = qry.trim();
        }
        List<QueryComponent> tqcs = applicationController.getQueryComponents();

        for (QueryComponent q : tqcs) {
            if (q.getQueryLevel() == QueryLevel.Category) {
                String name;
                String code;
                if (q.getName() != null) {
                    name = q.getName().trim();
                } else {
                    name = "";
                }
                if (q.getCode() != null) {
                    code = q.getCode().trim();
                } else {
                    code = "";
                }
                if (name.equalsIgnoreCase(qry) || code.equalsIgnoreCase(qry)) {
                    nqs.add(q);
                }
            }
        }
        return nqs;

    }

    public List<QueryComponent> fillCategories() {
        List<QueryComponent> tqcs = applicationController.getQueryComponents();
        List<QueryComponent> nqs = new ArrayList<>();
        for (QueryComponent q : tqcs) {
            if (q.getQueryLevel() == QueryLevel.Category) {
                nqs.add(q);
            }
        }
        return nqs;
    }

    public List<QueryComponent> fillExcels() {
        List<QueryComponent> tqcs = applicationController.getQueryComponents();
        List<QueryComponent> nqs = new ArrayList<>();
        for (QueryComponent q : tqcs) {
            if (q.getQueryType() == QueryType.Excel_Report) {
                nqs.add(q);
            }
        }
        return nqs;
    }

    public List<QueryComponent> fillIndicators() {
        List<QueryComponent> tqcs = applicationController.getQueryComponents();
        List<QueryComponent> nqs = new ArrayList<>();
        for (QueryComponent q : tqcs) {
            if (q.getQueryType() == QueryType.Indicator) {
                nqs.add(q);
            }
        }
        return nqs;
    }

    public List<QueryComponent> fillPopulations() {
        List<QueryComponent> tqcs = applicationController.getQueryComponents();
        List<QueryComponent> nqs = new ArrayList<>();
        for (QueryComponent q : tqcs) {
            if (q.getQueryType() == QueryType.Population) {
                nqs.add(q);
            }
        }
        return nqs;
    }

    public List<QueryComponent> fillCountsAndPopulations() {
        List<QueryComponent> tqcs = applicationController.getQueryComponents();
        List<QueryComponent> nqs = new ArrayList<>();
        for (QueryComponent q : tqcs) {
            if (q.getQueryType() == QueryType.Client_Count
                    || q.getQueryType() == QueryType.Encounter_Count
                    || q.getQueryType() == QueryType.Population) {
                nqs.add(q);
            }
        }
        return nqs;
    }

    public List<QueryComponent> fillCounts() {
        List<QueryComponent> tqcs = applicationController.getQueryComponents();
        List<QueryComponent> nqs = new ArrayList<>();
        for (QueryComponent q : tqcs) {
            if (q.getQueryType() == QueryType.Client_Count
                    || q.getQueryType() == QueryType.Encounter_Count) {
                nqs.add(q);
            }
        }
        return nqs;
    }

    public List<QueryComponent> subcategories() {
        return subcategories(selectedCategory);
    }

    public List<QueryComponent> subcategories(QueryComponent p) {
        List<QueryComponent> tqcs = applicationController.getQueryComponents();
        List<QueryComponent> nqs = new ArrayList<>();
        for (QueryComponent q : tqcs) {
            if (q.getQueryLevel() == QueryLevel.Subcategory
                    && q.getParentComponent().equals(p)) {
                nqs.add(q);
            }
        }
        return nqs;
    }

    public List<QueryComponent> queries() {
        return queries(selectedSubcategory);
    }

    public void downloadAllQueriesWithDetails() {

        String FILE_NAME = "query_details" + "_" + (new Date()) + ".xlsx";
        String mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        String folder = "/tmp/";

        File newFile = new File(folder + FILE_NAME);

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Queries");

        int rowCount = 0;

        Row t1 = sheet.createRow(rowCount++);
        Cell th1_lbl = t1.createCell(0);
        th1_lbl.setCellValue("Report");
        Cell th1_val = t1.createCell(1);
        th1_val.setCellValue("List of Queries");

        Row t2 = sheet.createRow(rowCount++);
        Cell th2_lbl = t2.createCell(0);
        th2_lbl.setCellValue("Subgroup");
        Cell th2_val = t2.createCell(1);
        th2_val.setCellValue(selectedCategory.getName());

        Row t3 = sheet.createRow(rowCount++);
        Cell th3_lbl = t3.createCell(0);
        th3_lbl.setCellValue("To");
        Cell th3_val = t3.createCell(1);
        th3_val.setCellValue(selectedSubcategory.getName());

        rowCount++;

        Row t5 = sheet.createRow(rowCount++);
        Cell th5_1 = t5.createCell(0);
        th5_1.setCellValue("Serial");
        Cell th5_2 = t5.createCell(1);
        th5_2.setCellValue("Name");
        Cell th5_3 = t5.createCell(2);
        th5_3.setCellValue("Code");
        Cell th5_4 = t5.createCell(3);
        th5_4.setCellValue("Type");
        Cell th5_5 = t5.createCell(4);
        th5_5.setCellValue("Criteria");
        Cell th5_6 = t5.createCell(5);
        th5_6.setCellValue("Match Type");

        Cell th5_7 = t5.createCell(6);
        th5_7.setCellValue("Evaluation Type");
        Cell th5_8 = t5.createCell(7);
        th5_8.setCellValue("Item");
        Cell th5_9 = t5.createCell(8);
        th5_9.setCellValue("Data TYpe");
        Cell th5_10 = t5.createCell(9);
        th5_10.setCellValue("Item Value");
        Cell th5_11 = t5.createCell(10);
        th5_11.setCellValue("Int1");
        Cell th5_12 = t5.createCell(11);
        th5_12.setCellValue("Int2");

        Cell th5_13 = t5.createCell(12);
        th5_13.setCellValue("Long 1");
        Cell th5_14 = t5.createCell(13);
        th5_14.setCellValue("Long 2");
        Cell th5_15 = t5.createCell(14);
        th5_15.setCellValue("Real 1");
        Cell th5_16 = t5.createCell(15);
        th5_16.setCellValue("Real 2");
        Cell th5_17 = t5.createCell(16);
        th5_17.setCellValue("Boolean");

        int serial = 1;

        CellStyle cellStyle = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        cellStyle.setDataFormat(
                createHelper.createDataFormat().getFormat("dd/MMMM/yyyy hh:mm"));

        List<QueryComponent> tmpQueryComponents = queries(selectedSubcategory);
        for (QueryComponent tmpQc : tmpQueryComponents) {
            List<QueryComponent> tmpCs = criteria(tmpQc);

            for (QueryComponent c : tmpCs) {

                Row row = sheet.createRow(++rowCount);

                Cell c1 = row.createCell(0);
                c1.setCellValue(serial);

                Cell c2 = row.createCell(1);
                if (tmpQc.getName() != null) {
                    c2.setCellValue(tmpQc.getName());
                }

                Cell c3 = row.createCell(2);
                if (tmpQc.getCode() != null) {
                    c3.setCellValue(tmpQc.getCode());
                }

                Cell c4 = row.createCell(3);
                if (tmpQc.getQueryType() != null) {
                    c4.setCellValue(tmpQc.getQueryType().toString());
                }

                Cell c5 = row.createCell(4);
                if (c.getName() != null) {
                    c5.setCellValue(c.getName());
                }

                Cell c7 = row.createCell(5);
                if (c.getMatchType() != null) {
                    c7.setCellValue(c.getMatchType().toString());
                }

                Cell c8 = row.createCell(6);
                if (c.getEvaluationType() != null) {
                    c8.setCellValue(c.getEvaluationType().toString());
                }

                Cell c9 = row.createCell(7);
                if (c.getItem() != null && c.getItem().getName() != null) {
                    c9.setCellValue(c.getItem().getName());
                }

                Cell c10 = row.createCell(8);
                if (c.getQueryDataType() != null) {
                    c10.setCellValue(c.getQueryDataType().toString());
                }

                Cell c11 = row.createCell(9);
                if (c.getItemValue() != null && c.getItemValue().getName() != null) {
                    c11.setCellValue(c.getItemValue().getName());
                }

                Cell c12 = row.createCell(10);
                if (c.getIntegerNumberValue() != null) {
                    c12.setCellValue(c.getIntegerNumberValue());
                }

                Cell c13 = row.createCell(11);
                if (c.getIntegerNumberValue2() != null) {
                    c13.setCellValue(c.getIntegerNumberValue2());
                }

                Cell c14 = row.createCell(12);
                if (c.getLongNumberValue() != null) {
                    c14.setCellValue(c.getLongNumberValue());
                }

                Cell c15 = row.createCell(13);
                if (c.getLongNumberValue2() != null) {
                    c15.setCellValue(c.getLongNumberValue2());
                }

                Cell c16 = row.createCell(14);
                if (c.getRealNumberValue() != null) {
                    c16.setCellValue(c.getRealNumberValue());
                }

                Cell c17 = row.createCell(15);
                if (c.getRealNumberValue2() != null) {
                    c17.setCellValue(c.getRealNumberValue2());
                }

                Cell c18 = row.createCell(16);
                if (c.getBooleanValue() != null) {
                    c18.setCellValue(c.getBooleanValue());
                }

                serial++;
            }

        }
        try (FileOutputStream outputStream = new FileOutputStream(newFile)) {
            workbook.write(outputStream);
        } catch (Exception e) {

        }

        InputStream stream;
        try {
            stream = new FileInputStream(newFile);
            resultExcelFile = new DefaultStreamedContent(stream, mimeType, FILE_NAME);
        } catch (FileNotFoundException ex) {

        }

    }

    public List<QueryComponent> queries(QueryComponent p) {
        List<QueryComponent> tqcs = applicationController.getQueryComponents();
        List<QueryComponent> nqs = new ArrayList<>();
        for (QueryComponent q : tqcs) {
            if (q.getQueryLevel() == QueryLevel.Query
                    && q.getParentComponent().equals(p)) {
                nqs.add(q);
            }
        }
        return nqs;
    }

    public List<QueryComponent> criteria() {
        return criteria(selectedQuery);
    }

    public List<QueryComponent> getCriteriaOfSelectedCount() {
        System.out.println("getCriteriaOfSelectedCount");
        System.out.println("selectedCount = " + selectedCount.getName());
        return criteriaOfSelectedCount = criteria(selectedCount);
    }

    public List<QueryComponent> criteria(QueryComponent p) {
        System.out.println("criteria");
        System.out.println("p = " + p);
        List<QueryComponent> tqcs = applicationController.getQueryComponents();
        List<QueryComponent> nqs = new ArrayList<>();
        for (QueryComponent q : tqcs) {
            if (q.getQueryLevel() == QueryLevel.Criterian
                    && q.getParentComponent().equals(p)) {
                nqs.add(q);
            }
        }
        System.out.println("nqs = " + nqs.size());
        return nqs;
    }

    public QueryComponent findLastQuery(String qry) {
        if (qry == null) {
            return null;
        }
        QueryComponent nq = null;
        List<QueryComponent> tqcs = applicationController.getQueryComponents();
        List<QueryComponent> nqs = new ArrayList<>();
        for (QueryComponent q : tqcs) {
            if (q.getCode() != null && q.getCode().equalsIgnoreCase(qry)) {
                if (nq == null) {
                    nq = q;
                } else {
                    if (nq.getId() < q.getId()) {
                        nq = q;
                    }
                }
            }
        }
        return nq;

    }

    public String testQuery() {

        if (selectedForQuery == null) {
            JsfUtil.addErrorMessage("Nothing selected");
            return "";
        }

        resultString = null;
        resultClientList = null;
        resultFormList = null;
        resultEncounterList = null;
        resultRelationshipList = null;

        qrs = new ArrayList<>();
        QueryResult qr = new QueryResult();
        Map m = new HashMap();
        m.put("f", false);

        String j = "select count(distinct c) from ClientEncounterComponentItem i join i.itemClient c  where i.retired=:f";
        qr.setLongResult(getFacade().countByJpql(j, m));

        qrs.add(qr);
        return "graph";
    }

    public String processQueryNew() {

        if (selectedForQuery == null) {
            JsfUtil.addErrorMessage("Nothing selected");
            return "";
        }

        qrs = new ArrayList<>();
        QueryResult qr = new QueryResult();

        Date tfrom = null;
        Date tTo = null;
        Integer tYear = null;
        Integer tQuater = null;

        if (periodType != null) {
            switch (periodType) {
                case After:
                    tfrom = from;
                    break;
                case All:
                    break;
                case Before:
                    tTo = to;
                    break;
                case Period:
                    tfrom = from;
                    tTo = to;
                    break;
                case Quarter:
                    tQuater = quarter;
                    tfrom = CommonController.startOfQuarter(year, quarter);

                    tTo = CommonController.endOfQuarter(year, quarter);
                    break;
                case Year:
                    tYear = year;
                    tfrom = CommonController.startOfTheYear(year);
                    tTo = CommonController.endOfYear(year);
                    break;
            }
        }

        from = tfrom;
        to = tTo;

        qr.setAreaType(areaType);
        qr.setPeriodType(periodType);

        switch (areaType) {
            case Distirct:
                qr.setArea(district);
                qr.setTfrom(tfrom);
                qr.settTo(tTo);
                qr.settYear(tYear);
                qr.settQuater(tQuater);
                qrs.add(qr);
                break;
            case GN:
                qr.setTfrom(tfrom);
                qr.settTo(tTo);
                qr.settYear(tYear);
                qr.settQuater(tQuater);
                qr.setArea(gn);
                qrs.add(qr);
                break;
            case MOH:
                qr.setTfrom(tfrom);
                qr.settTo(tTo);
                qr.settYear(tYear);
                qr.settQuater(tQuater);
                qr.setArea(moh);
                qrs.add(qr);
                break;
            case National:
                qr.setTfrom(tfrom);
                qr.settTo(tTo);
                qr.settYear(tYear);
                qr.settQuater(tQuater);
                qr.setArea(areaController.getNationalArea());
                qrs.add(qr);
                break;
            case PHM:
                qr.setTfrom(tfrom);
                qr.settTo(tTo);
                qr.settYear(tYear);
                qr.settQuater(tQuater);
                qr.setArea(phm);
                qrs.add(qr);
                break;
            case Province:
                qr.setTfrom(tfrom);
                qr.settTo(tTo);
                qr.settYear(tYear);
                qr.settQuater(tQuater);
                qr.setArea(province);
                qrs.add(qr);
                break;

            case Province_List:
                for (Area p : areaController.getProvinces()) {
                    qr = new QueryResult();
                    qr.setArea(p);
                    qr.setAreaType(areaType);
                    qr.setPeriodType(periodType);
                    qr.setTfrom(tfrom);
                    qr.settTo(tTo);
                    qr.settYear(tYear);
                    qr.settQuater(tQuater);
                    qrs.add(qr);
                }
                break;
            case District_List:
                for (Area d : areaController.getDistricts()) {
                    qr = new QueryResult();
                    qr.setArea(d);
                    qr.setAreaType(areaType);
                    qr.setPeriodType(periodType);
                    qr.setTfrom(tfrom);
                    qr.settTo(tTo);
                    qr.settYear(tYear);
                    qr.settQuater(tQuater);
                    qrs.add(qr);
                }
                break;
            case District_MOH_List:
                for (Area d : areaController.getMohAreasOfADistrict(district)) {
                    qr = new QueryResult();
                    qr.setArea(d);
                    qr.setAreaType(areaType);
                    qr.setPeriodType(periodType);
                    qr.setTfrom(tfrom);
                    qr.settTo(tTo);
                    qr.settYear(tYear);
                    qr.settQuater(tQuater);
                    qrs.add(qr);
                }
                break;

            case MOH_GN_List:
                for (Area d : areaController.getGnAreasOfMoh(moh)) {
                    qr = new QueryResult();
                    qr.setArea(d);
                    qr.setAreaType(areaType);
                    qr.setPeriodType(periodType);
                    qr.setTfrom(tfrom);
                    qr.settTo(tTo);
                    qr.settYear(tYear);
                    qr.settQuater(tQuater);
                    qrs.add(qr);
                }
                break;
            case MOH_PHM_List:
                for (Area d : areaController.getPhmAreasOfMoh(moh)) {
                    qr = new QueryResult();
                    qr.setArea(d);
                    qr.setAreaType(areaType);
                    qr.setPeriodType(periodType);
                    qr.setTfrom(tfrom);
                    qr.settTo(tTo);
                    qr.settYear(tYear);
                    qr.settQuater(tQuater);
                    qrs.add(qr);
                }
                break;
            case PHM_GN_List:
                for (Area d : areaController.getGnAreasOfPhm(moh)) {
                    qr = new QueryResult();
                    qr.setArea(d);
                    qr.setAreaType(areaType);
                    qr.setPeriodType(periodType);
                    qr.setTfrom(tfrom);
                    qr.settTo(tTo);
                    qr.settYear(tYear);
                    qr.settQuater(tQuater);
                    qrs.add(qr);
                }
                break;
            case Province_District_list:
                for (Area d : areaController.getDistrictsOfAProvince(province)) {
                    qr = new QueryResult();
                    qr.setArea(d);
                    qr.setAreaType(areaType);
                    qr.setPeriodType(periodType);
                    qr.setTfrom(tfrom);
                    qr.settTo(tTo);
                    qr.settYear(tYear);
                    qr.settQuater(tQuater);
                    qrs.add(qr);
                }
                break;
        }

        for (QueryResult tqr : qrs) {

            switch (selectedForQuery.getQueryType()) {
                case Population:
                    Integer ty = tqr.gettYear();
                    if (ty == null) {
                        Calendar c = Calendar.getInstance();
                        if (tqr.getTfrom() != null) {
                            c.setTime(tqr.getTfrom());
                        } else if (tqr.gettTo() != null) {
                            c.setTime(tqr.gettTo());
                        }
                        ty = c.get(Calendar.YEAR);
                    }
                    tqr.setJpq(createAPopulationCountQuery(selectedForQuery, tqr.getArea(), ty));
                    if (tqr.getJpq().getLongResult() != null) {
                        tqr.setResultString(tqr.getJpq().getQc().getName() + " = " + tqr.getJpq().getLongResult());
                    }
                    tqr.setResultRelationshipList(tqr.getJpq().getRelationshipList());
                    break;

                case Indicator:
                    tqr.setJpq(handleIndicatorQuery(selectedForQuery, tqr.getArea(), tqr.getTfrom(), tqr.gettTo(),
                            tqr.gettYear(), tqr.gettQuater()));
                    tqr.setLongResult(tqr.getJpq().getLongResult());
                    tqr.setDblResult(tqr.getJpq().getDblResult());
                    break;
                case Client:
                    tqr.setJpq(createClientQuery(selectedForQuery, tqr.getArea(), tqr.getTfrom(), tqr.gettTo(),
                            tqr.gettYear(), tqr.gettQuater()));
                    if (tqr.getJpq().getLongResult() != null) {
                        tqr.setResultString(tqr.getJpq().getQc().getName() + " = " + tqr.getJpq().getLongResult());
                    }
                    tqr.setLongResult(tqr.getJpq().getLongResult());
                    tqr.setResultClientList(tqr.getJpq().getClientList());
                    break;

                case First_Encounter:
                    break;

                case Any_Encounter:
                    break;

                case Formset:
                    break;

            }

        }

        clearFilters();

        return "graph";
    }

    private List<Long> findEncounterIds(Date fromDate, Date toDate, Area gnArea) {
        String j = "select e.id "
                + " from  ClientEncounterComponentFormSet f join f.encounter e"
                + " where e.retired<>:er"
                + " and f.retired<>:fr ";
        j += " and f.completed=:fc ";
        j += " and e.client.gnArea=:i "
                + " and e.encounterType=:t "
                + " and e.encounterDate between :fd and :td"
                + " order by e.id";
        Map m = new HashMap();
        m.put("i", gnArea);
        m.put("t", EncounterType.Clinic_Visit);
        m.put("er", true);
        m.put("fr", true);
        m.put("fc", true);
        m.put("fd", fromDate);
        m.put("td", toDate);
        List<Long> encs = encounterFacade.findLongList(j, m);
        return encs;
    }

    private List<ClientEncounterComponentItem> findClientEncounterComponentItems(Long endId) {
        String j;
        Map m;
        m = new HashMap();
        j = "select f from ClientEncounterComponentItem f "
                + " where f.retired=false "
                + " and f.encounter.id=:eid";
        m.put("eid", endId);
        List<ClientEncounterComponentItem> ts = getClientEncounterComponentItemFacade().findByJpql(j, m);
        return ts;
    }

    public List<EncounterWithComponents> findEncountersWithComponents(List<Long> ids) {
        if (ids == null) {
            return null;
        }
        List<EncounterWithComponents> cs = new ArrayList<>();
        for (Long enId : ids) {
            EncounterWithComponents ewc = new EncounterWithComponents();
            ewc.setEncounterId(enId);
            ewc.setComponents(findClientEncounterComponentItems(enId));
            cs.add(ewc);
        }
        return cs;
    }

    private Long calculateIndividualQueryResult(List<EncounterWithComponents> ewcs, QueryWithCriteria qwc) {

        Long result = 0l;
        if (ewcs == null) {

            return result;
        }
        if (qwc == null) {

            return result;
        }
        List<QueryComponent> criteria = qwc.getCriteria();

        if (criteria == null || criteria.isEmpty()) {
            Integer ti = ewcs.size();
            result = ti.longValue();

            return result;
        } else {
            for (EncounterWithComponents ewc : ewcs) {
                if (findMatch(ewc.getComponents(), qwc)) {
                    result++;
                }
            }
        }
        return result;
    }

    private boolean findMatch(List<ClientEncounterComponentItem> ccs, QueryWithCriteria qrys) {

        if (qrys == null) {

            return false;
        }
        if (qrys.getQuery() == null) {

            return false;
        }
        if (qrys.getQuery().getCode() == null) {

            return false;
        }
        if (qrys.getQuery().getCode().trim().equals("")) {

            return false;
        }

        boolean suitableForInclusion = true;

        boolean isComplexQuery = false;

        for (QueryComponent qc : qrys.getCriteria()) {
            switch (qc.getMatchType()) {
                case Closing_Bracket:
                case Opening_Bracket:
                case Operator_AND:
                case Operator_OR:
                    isComplexQuery = true;
                    break;
            }
        }

        if (isComplexQuery) {
            String evaluationString = "";
            for (QueryComponent qc : qrys.getCriteria()) {
                if (qc.getMatchType() == QueryCriteriaMatchType.Opening_Bracket) {
                    evaluationString += "(";
                    continue;
                } else if (qc.getMatchType() == QueryCriteriaMatchType.Closing_Bracket) {
                    evaluationString += ")";
                    continue;
                } else if (qc.getMatchType() == QueryCriteriaMatchType.Operator_AND) {
                    evaluationString += " && ";
                    continue;
                } else if (qc.getMatchType() == QueryCriteriaMatchType.Operator_OR) {
                    evaluationString += " || ";
                    continue;
                } else {
                    if (qc.getItem() == null) {

                        continue;
                    }
                    if (qc.getItem().getCode() == null) {

                        continue;
                    }
                    for (ClientEncounterComponentItem cei : ccs) {

                        if (cei.getItem() == null) {

                            continue;
                        }
                        if (cei.getItem().getCode() == null) {

                            continue;
                        }

                        if (cei.getItem().getCode().trim().equalsIgnoreCase(qc.getItem().getCode().trim())) {
                            if (matchQuery(qc, cei)) {
                                evaluationString += "true";
                            } else {
                                evaluationString += "false";
                            }
                        }
                    }

                }

            }
            String evaluationResult = evaluateScript(evaluationString);
            if (evaluationResult == null) {
                suitableForInclusion = false;
            } else if (evaluationResult.trim().equalsIgnoreCase("true")) {
                suitableForInclusion = true;
            } else {
                suitableForInclusion = false;
            }
        } else {

            for (QueryComponent qc : qrys.getCriteria()) {
                if (qc.getItem() == null) {

                    continue;
                }
                if (qc.getItem().getCode() == null) {

                    continue;
                }

                boolean thisMatchOk = false;
                boolean componentFound = false;

                for (ClientEncounterComponentItem cei : ccs) {

                    if (cei.getItem() == null) {

                        continue;
                    }
                    if (cei.getItem().getCode() == null) {

                        continue;
                    }

                    if (cei.getItem().getCode().trim().equalsIgnoreCase(qc.getItem().getCode().trim())) {
                        componentFound = true;
                        if (matchQuery(qc, cei)) {
                            thisMatchOk = true;
                        }
                    }
                }
                if (!componentFound) {
                    System.out.println("componentFound = " + componentFound);
                }
                if (!thisMatchOk) {
                    suitableForInclusion = false;
                }
            }

        }

        return suitableForInclusion;
    }

    public boolean clientValueIsNotNull(QueryComponent q, ClientEncounterComponentItem clientValue) {
        boolean valueNotNull = false;

        if (q.getMatchType() == QueryCriteriaMatchType.Variable_Value_Check) {
            switch (q.getQueryDataType()) {
                case integer:

                    if (clientValue.getIntegerNumberValue() != null) {
                        valueNotNull = true;
                    }
                    break;
                case item:

                    if (clientValue.getItemValue() != null) {
                        valueNotNull = true;
                    }
                    break;
                case real:

                    if (clientValue.getRealNumberValue() != null) {
                        valueNotNull = true;
                    }
                    break;
                case longNumber:

                    if (clientValue.getLongNumberValue() != null) {
                        valueNotNull = true;
                    }
                    break;
                case Boolean:

                    if (clientValue.getBooleanValue() != null) {
                        valueNotNull = true;
                    }
                    break;
                case String:
                    if (clientValue.getShortTextValue() != null) {
                        valueNotNull = true;
                    }
                    break;
            }
        }
        return valueNotNull;
    }

    private boolean matchQuery(QueryComponent q, ClientEncounterComponentItem clientValue) {

        if (clientValue == null) {
            return false;
        }
        boolean m = false;
        Integer qInt1 = null;
        Integer qInt2 = null;
        Double real1 = null;
        Double real2 = null;
        Long lng1 = null;
        Long lng2 = null;
        Item itemVariable = null;
        Item itemValue = null;
        Boolean qBool = null;
        String qStr = null;

        if (q.getMatchType() == QueryCriteriaMatchType.Variable_Value_Check) {

            switch (q.getQueryDataType()) {
                case integer:

                    qInt1 = q.getIntegerNumberValue();
                    qInt2 = q.getIntegerNumberValue2();

                    break;
                case item:
                    itemValue = q.getItemValue();
                    itemVariable = q.getItem();
                    break;
                case real:
                    real2 = q.getRealNumberValue2();
                    break;
                case longNumber:

                    lng1 = q.getLongNumberValue();
                    lng2 = q.getLongNumberValue2();
                    break;
                case Boolean:

                    qBool = q.getBooleanValue();
                    break;
                case String:
                    qStr = q.getShortTextValue();
                    break;

            }

            switch (q.getEvaluationType()) {

                case Not_null:
                    m = clientValueIsNotNull(q, clientValue);
                    break;

                case Is_null:
                    m = !clientValueIsNotNull(q, clientValue);
                    break;
                case Equal:
                    if (qInt1 != null) {
                        Integer tmpIntVal = clientValue.getIntegerNumberValue();
                        if (tmpIntVal == null) {
                            tmpIntVal = CommonController.stringToInteger(clientValue.getShortTextValue());
                        }
                        if (tmpIntVal != null) {
                            m = qInt1.equals(tmpIntVal);
                        }
                    }
                    if (lng1 != null) {
                        Long tmpLLongVal = clientValue.getLongNumberValue();
                        if (tmpLLongVal == null) {
                            tmpLLongVal = CommonController.stringToLong(clientValue.getShortTextValue());
                        }
                        if (tmpLLongVal != null) {
                            m = lng1.equals(tmpLLongVal);
                        }
                    }
                    if (real1 != null) {
                        Double tmpDbl = clientValue.getRealNumberValue();
                        if (tmpDbl == null) {
                            tmpDbl = CommonController.stringToDouble(clientValue.getShortTextValue());
                        }
                        if (tmpDbl != null) {
                            m = real1.equals(tmpDbl);
                        }
                    }
                    if (qBool != null) {
                        if (clientValue.getBooleanValue() != null) {
                            m = qBool.equals(clientValue.getBooleanValue());
                        }
                    }
                    if (itemValue != null && itemVariable != null) {

                        if (itemValue != null
                                && itemValue.getCode() != null
                                && clientValue != null
                                && clientValue.getItemValue() != null
                                && clientValue.getItemValue().getCode() != null) {

                            if (itemValue.getCode().equals(clientValue.getItemValue().getCode())) {
                                m = true;
                            }
                        }
                    }
                    if (qStr != null) {
                        if (clientValue.getShortTextValue() != null) {
                            m = qStr.equals(clientValue.getShortTextValue());
                        }
                    }
                    break;
                case Less_than:
                    if (qInt1 != null) {
                        Integer tmpIntVal = clientValue.getIntegerNumberValue();
                        if (tmpIntVal == null) {
                            tmpIntVal = CommonController.stringToInteger(clientValue.getShortTextValue());
                        }
                        if (tmpIntVal != null) {
                            m = tmpIntVal < qInt1;
                        }
                    }
                    if (lng1 != null) {
                        Long tmpLong = clientValue.getLongNumberValue();
                        if (tmpLong == null) {
                            tmpLong = CommonController.stringToLong(clientValue.getShortTextValue());
                        }
                        if (tmpLong != null) {
                            m = tmpLong < lng1;
                        }
                    }
                    if (real1 != null) {
                        Double tmpDbl = clientValue.getRealNumberValue();
                        if (tmpDbl == null) {
                            tmpDbl = CommonController.stringToDouble(clientValue.getShortTextValue());
                        }
                        if (tmpDbl != null) {
                            m = tmpDbl < real1;
                        }
                    }
                    break;
                case Between:
                    if (qInt1 != null && qInt2 != null) {
                        if (qInt1 > qInt2) {
                            Integer intTem = qInt1;
                            qInt1 = qInt2;
                            qInt2 = intTem;
                        }

                        Integer tmpInt = clientValue.getIntegerNumberValue();
                        if (tmpInt == null) {
                            tmpInt = CommonController.stringToInteger(clientValue.getShortTextValue());
                        }
                        if (tmpInt != null) {
                            if (tmpInt > qInt1 && tmpInt < qInt2) {
                                m = true;
                            }
                        }

                    }
                    if (lng1 != null && lng2 != null) {
                        if (lng1 > lng2) {
                            Long intTem = lng1;
                            intTem = lng1;
                            lng1 = lng2;
                            lng2 = intTem;
                        }

                        Long tmpLong = clientValue.getLongNumberValue();
                        if (tmpLong == null) {
                            tmpLong = CommonController.stringToLong(clientValue.getShortTextValue());
                        }
                        if (tmpLong != null) {
                            if (tmpLong > lng1 && tmpLong < lng2) {
                                m = true;
                            }
                        }
                    }
                    if (real1 != null && real2 != null) {
                        if (real1 > real2) {
                            Double realTem = real1;
                            realTem = real1;
                            real1 = real2;
                            real2 = realTem;
                        }

                        Double tmpDbl = clientValue.getRealNumberValue();
                        if (tmpDbl == null) {
                            tmpDbl = CommonController.stringToDouble(clientValue.getShortTextValue());
                        }
                        if (tmpDbl != null) {
                            if (tmpDbl > real1 && tmpDbl < real2) {
                                m = true;
                            }
                        }
                    }
                    break;
                case Grater_than:
                    if (qInt1 != null) {
                        Integer tmpInt = clientValue.getIntegerNumberValue();
                        if (tmpInt == null) {
                            tmpInt = CommonController.stringToInteger(clientValue.getShortTextValue());
                        }
                        if (tmpInt != null) {
                            m = tmpInt > qInt1;
                        }
                    }
                    if (real1 != null) {
                        Double tmpDbl = clientValue.getRealNumberValue();
                        if (tmpDbl == null) {
                            tmpDbl = CommonController.stringToDouble(clientValue.getShortTextValue());
                        }
                        if (tmpDbl != null) {
                            m = tmpDbl > real1;
                        }
                    }
                    if (lng1 != null) {
                        Long tmpLng = clientValue.getLongNumberValue();
                        if (tmpLng == null) {
                            tmpLng = CommonController.stringToLong(clientValue.getShortTextValue());
                        }
                        if (tmpLng != null) {
                            m = tmpLng > lng1;
                        }
                    }
                    break;
                case Grater_than_or_equal:
                    if (qInt1 != null) {
                        Integer tmpInt = clientValue.getIntegerNumberValue();
                        if (tmpInt == null) {
                            tmpInt = CommonController.stringToInteger(clientValue.getShortTextValue());
                        }
                        if (tmpInt != null) {
                            m = tmpInt >= qInt1;
                        }
                    }
                    if (real1 != null) {
                        Double temDbl = clientValue.getRealNumberValue();
                        if (temDbl == null) {
                            temDbl = CommonController.stringToDouble(clientValue.getShortTextValue());
                        }
                        if (temDbl != null) {
                            m = temDbl >= real1;

                        }

                    }
                    if (lng1 != null) {
                        Long tmpLng = clientValue.getLongNumberValue();
                        if (tmpLng == null) {
                            tmpLng = CommonController.stringToLong(clientValue.getShortTextValue());
                        }
                        if (tmpLng != null) {
                            m = tmpLng >= lng1;
                        }
                    }
                    break;
                case Less_than_or_equal:
                    if (qInt1 != null) {
                        Integer tmpInt = clientValue.getIntegerNumberValue();
                        if (tmpInt == null) {
                            tmpInt = CommonController.stringToInteger(clientValue.getShortTextValue());
                        }
                        if (tmpInt != null) {
                            m = tmpInt <= qInt1;
                        }
                    }
                    if (real1 != null) {
                        Double tmpDbl = clientValue.getRealNumberValue();
                        if (tmpDbl == null) {
                            tmpDbl = CommonController.stringToDouble(clientValue.getShortTextValue());
                        }
                        if (tmpDbl != null) {
                            m = tmpDbl <= real1;
                        }
                    }
                    if (lng1 != null) {
                        Long tmpLng = clientValue.getLongNumberValue();
                        if (tmpLng == null) {
                            tmpLng = CommonController.stringToLong(clientValue.getShortTextValue());
                        }
                        if (tmpLng != null) {
                            m = tmpLng <= lng1;
                        }
                    }
                    break;
            }
        }

        return m;
    }

    public String processQueryMonthlyGn() {
        List<QueryWithCriteria> queriesWithCriteria = new ArrayList<>();
        if (selectedForQuery
                == null) {
            JsfUtil.addErrorMessage("Nothing selected");
            return "";
        }

        from = CommonController.startOfQuarter(year, month);
        ;
        to = CommonController.endOfQuarter(year, month);
        ;

        List<Long> encounterIds = findEncounterIds(from, date, gn);
        List<EncounterWithComponents> encountersWithComponents;
        encountersWithComponents = findEncountersWithComponents(encounterIds);
        if (encountersWithComponents
                == null) {
            JsfUtil.addErrorMessage("No Data");
            return "";
        }

        for (QueryResult tqr : qrs) {

            switch (selectedForQuery.getQueryType()) {
                case Population:
                    Integer ty = tqr.gettYear();
                    if (ty == null) {
                        Calendar c = Calendar.getInstance();
                        if (tqr.getTfrom() != null) {
                            c.setTime(tqr.getTfrom());
                        } else if (tqr.gettTo() != null) {
                            c.setTime(tqr.gettTo());
                        }
                        ty = c.get(Calendar.YEAR);
                    }
                    tqr.setJpq(createAPopulationCountQuery(selectedForQuery, tqr.getArea(), ty));
                    if (tqr.getJpq().getLongResult() != null) {
                        tqr.setResultString(tqr.getJpq().getQc().getName() + " = " + tqr.getJpq().getLongResult());
                    }
                    tqr.setResultRelationshipList(tqr.getJpq().getRelationshipList());
                    break;

                case Indicator:
                    tqr.setJpq(handleIndicatorQuery(selectedForQuery, tqr.getArea(), tqr.getTfrom(), tqr.gettTo(),
                            tqr.gettYear(), tqr.gettQuater()));
                    tqr.setLongResult(tqr.getJpq().getLongResult());
                    tqr.setDblResult(tqr.getJpq().getDblResult());
                    break;
                case Client:
                    tqr.setJpq(createClientQuery(selectedForQuery, tqr.getArea(), tqr.getTfrom(), tqr.gettTo(),
                            tqr.gettYear(), tqr.gettQuater()));
                    if (tqr.getJpq().getLongResult() != null) {
                        tqr.setResultString(tqr.getJpq().getQc().getName() + " = " + tqr.getJpq().getLongResult());
                    }
                    tqr.setLongResult(tqr.getJpq().getLongResult());
                    tqr.setResultClientList(tqr.getJpq().getClientList());
                    break;

                case First_Encounter:
                    break;

                case Any_Encounter:
                    break;

                case Formset:
                    break;

            }

        }

        clearFilters();

        return "graph";
    }

    public String processQuery() {

        if (selectedForQuery == null) {
            JsfUtil.addErrorMessage("Nothing selected");
            return "";
        }

        resultString = null;
        resultClientList = null;
        resultFormList = null;
        resultEncounterList = null;
        resultRelationshipList = null;

        qrs = new ArrayList<>();
        QueryResult qr = new QueryResult();

        Date tfrom = null;
        Date tTo = null;
        Integer tYear = null;
        Integer tQuater = null;

        if (periodType != null) {
            switch (periodType) {
                case After:
                    tfrom = from;
                    break;
                case All:
                    break;
                case Before:
                    tTo = to;
                    break;
                case Period:
                    tfrom = from;
                    tTo = to;
                    break;
                case Quarter:
                    tQuater = quarter;
                    tfrom = CommonController.startOfQuarter(year, quarter);

                    tTo = CommonController.endOfQuarter(year, quarter);
                    break;
                case Year:
                    tYear = year;
                    tfrom = CommonController.startOfTheYear(year);
                    tTo = CommonController.endOfYear(year);
                    break;
            }
        }

        from = tfrom;
        to = tTo;

        qr.setAreaType(areaType);
        qr.setPeriodType(periodType);

        switch (areaType) {
            case Distirct:
                qr.setArea(district);
                qr.setTfrom(tfrom);
                qr.settTo(tTo);
                qr.settYear(tYear);
                qr.settQuater(tQuater);
                qrs.add(qr);
                break;
            case GN:
                qr.setTfrom(tfrom);
                qr.settTo(tTo);
                qr.settYear(tYear);
                qr.settQuater(tQuater);
                qr.setArea(gn);
                qrs.add(qr);
                break;
            case MOH:
                qr.setTfrom(tfrom);
                qr.settTo(tTo);
                qr.settYear(tYear);
                qr.settQuater(tQuater);
                qr.setArea(moh);
                qrs.add(qr);
                break;
            case National:
                qr.setTfrom(tfrom);
                qr.settTo(tTo);
                qr.settYear(tYear);
                qr.settQuater(tQuater);
                qr.setArea(areaController.getNationalArea());
                qrs.add(qr);
                break;
            case PHM:
                qr.setTfrom(tfrom);
                qr.settTo(tTo);
                qr.settYear(tYear);
                qr.settQuater(tQuater);
                qr.setArea(phm);
                qrs.add(qr);
                break;
            case Province:
                qr.setTfrom(tfrom);
                qr.settTo(tTo);
                qr.settYear(tYear);
                qr.settQuater(tQuater);
                qr.setArea(province);
                qrs.add(qr);
                break;

            case Province_List:
                for (Area p : areaController.getProvinces()) {
                    qr = new QueryResult();
                    qr.setArea(p);
                    qr.setAreaType(areaType);
                    qr.setPeriodType(periodType);
                    qr.setTfrom(tfrom);
                    qr.settTo(tTo);
                    qr.settYear(tYear);
                    qr.settQuater(tQuater);
                    qrs.add(qr);
                }
                break;
            case District_List:
                for (Area d : areaController.getDistricts()) {
                    qr = new QueryResult();
                    qr.setArea(d);
                    qr.setAreaType(areaType);
                    qr.setPeriodType(periodType);
                    qr.setTfrom(tfrom);
                    qr.settTo(tTo);
                    qr.settYear(tYear);
                    qr.settQuater(tQuater);
                    qrs.add(qr);
                }
                break;
            case District_MOH_List:
                for (Area d : areaController.getMohAreasOfADistrict(district)) {
                    qr = new QueryResult();
                    qr.setArea(d);
                    qr.setAreaType(areaType);
                    qr.setPeriodType(periodType);
                    qr.setTfrom(tfrom);
                    qr.settTo(tTo);
                    qr.settYear(tYear);
                    qr.settQuater(tQuater);
                    qrs.add(qr);
                }
                break;

            case MOH_GN_List:
                for (Area d : areaController.getGnAreasOfMoh(moh)) {
                    qr = new QueryResult();
                    qr.setArea(d);
                    qr.setAreaType(areaType);
                    qr.setPeriodType(periodType);
                    qr.setTfrom(tfrom);
                    qr.settTo(tTo);
                    qr.settYear(tYear);
                    qr.settQuater(tQuater);
                    qrs.add(qr);
                }
                break;
            case MOH_PHM_List:
                for (Area d : areaController.getPhmAreasOfMoh(moh)) {
                    qr = new QueryResult();
                    qr.setArea(d);
                    qr.setAreaType(areaType);
                    qr.setPeriodType(periodType);
                    qr.setTfrom(tfrom);
                    qr.settTo(tTo);
                    qr.settYear(tYear);
                    qr.settQuater(tQuater);
                    qrs.add(qr);
                }
                break;
            case PHM_GN_List:
                for (Area d : areaController.getGnAreasOfPhm(moh)) {
                    qr = new QueryResult();
                    qr.setArea(d);
                    qr.setAreaType(areaType);
                    qr.setPeriodType(periodType);
                    qr.setTfrom(tfrom);
                    qr.settTo(tTo);
                    qr.settYear(tYear);
                    qr.settQuater(tQuater);
                    qrs.add(qr);
                }
                break;
            case Province_District_list:
                for (Area d : areaController.getDistrictsOfAProvince(province)) {
                    qr = new QueryResult();
                    qr.setArea(d);
                    qr.setAreaType(areaType);
                    qr.setPeriodType(periodType);
                    qr.setTfrom(tfrom);
                    qr.settTo(tTo);
                    qr.settYear(tYear);
                    qr.settQuater(tQuater);
                    qrs.add(qr);
                }
                break;
        }

        for (QueryResult tqr : qrs) {

            switch (selectedForQuery.getQueryType()) {
                case Population:
                    Integer ty = tqr.gettYear();
                    if (ty == null) {
                        Calendar c = Calendar.getInstance();
                        if (tqr.getTfrom() != null) {
                            c.setTime(tqr.getTfrom());
                        } else if (tqr.gettTo() != null) {
                            c.setTime(tqr.gettTo());
                        }
                        ty = c.get(Calendar.YEAR);
                    }
                    tqr.setJpq(createAPopulationCountQuery(selectedForQuery, tqr.getArea(), ty));
                    if (tqr.getJpq().getLongResult() != null) {
                        tqr.setResultString(tqr.getJpq().getQc().getName() + " = " + tqr.getJpq().getLongResult());
                    }
                    tqr.setResultRelationshipList(tqr.getJpq().getRelationshipList());
                    break;

                case Indicator:
                    tqr.setJpq(handleIndicatorQuery(selectedForQuery, tqr.getArea(), tqr.getTfrom(), tqr.gettTo(),
                            tqr.gettYear(), tqr.gettQuater()));
                    tqr.setLongResult(tqr.getJpq().getLongResult());
                    tqr.setDblResult(tqr.getJpq().getDblResult());
                    break;
                case Client:
                    tqr.setJpq(createClientQuery(selectedForQuery, tqr.getArea(), tqr.getTfrom(), tqr.gettTo(),
                            tqr.gettYear(), tqr.gettQuater()));
                    if (tqr.getJpq().getLongResult() != null) {
                        tqr.setResultString(tqr.getJpq().getQc().getName() + " = " + tqr.getJpq().getLongResult());
                    }
                    tqr.setLongResult(tqr.getJpq().getLongResult());
                    tqr.setResultClientList(tqr.getJpq().getClientList());
                    break;

                case First_Encounter:
                    break;

                case Any_Encounter:
                    break;

                case Formset:
                    break;

            }

        }

        clearFilters();

        return "graph";
    }

    public Jpq handleIndicatorQuery(QueryComponent qc, Area ccArea, Date ccFrom, Date ccTo, Integer ccYear, Integer ccQuarter) {
        String rs = "Nothing Calculated.";
        List<Replaceable> replaceables = findReplaceblesInIndicatorQuery(qc.getIndicatorQuery());
        Jpq j = new Jpq();
        for (Replaceable r : replaceables) {

            QueryComponent temqc = findLastQuery(r.getQryCode());

            if (temqc == null) {
                JsfUtil.addErrorMessage("Wrong Query. Check the names of queries");
                return new Jpq();
            }

            j = new Jpq();

            if (null == temqc.getQueryType()) {
                JsfUtil.addErrorMessage("Wrong Query. Check the names of queries");

                return j;
            } else {
                switch (temqc.getQueryType()) {
                    case Population:
                        //TODO: Add Logic Here
                        j = createAPopulationCountQuery(temqc, ccArea, ccYear);
                        break;
                    case Client:
                        j = createClientQuery(temqc, ccArea, ccFrom, ccTo, ccYear, ccQuarter);
                        break;
                    default:
                        JsfUtil.addErrorMessage("Wrong Query. Check the names of queries");
                        return j;
                }
            }

            r.setSelectedValue(j.getLongResult() + "");
        }
        String javaStringToEvaluate = addTemplateToReport(qc.getIndicatorQuery().trim(), replaceables);

        rs = "Formula \t" + javaStringToEvaluate + "\n";
        String res = evaluateScript(javaStringToEvaluate);
        rs += "Result : " + res;
        Double dbl = CommonController.getDoubleValue(res);

        Long lng = CommonController.getLongValue(res);

        j.setLongResult(lng);
        j.setDblResult(dbl);
        return j;
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

    public void duplicateSelected() {
        if (selectedToDuplicateQuery == null) {
            JsfUtil.addErrorMessage("Noting selected.");
            return;
        }
        List<QueryComponent> cs = criteria(selectedToDuplicateQuery);
        QueryComponent q = SerializationUtils.clone(selectedToDuplicateQuery);
        q.setId(null);
        q.setCreatedAt(new Date());
        q.setName(q.getName() + " Copy");
        q.setCreatedBy(webUserController.getLoggedUser());
        getFacade().create(q);

        for (QueryComponent c : cs) {
            QueryComponent newC = SerializationUtils.clone(c);
            newC.setId(null);
            newC.setName(newC.getName());
            newC.setParentComponent(q);
            newC.setCreatedAt(new Date());
            newC.setCreatedBy(webUserController.getLoggedUser());
            getFacade().create(newC);
        }

        items = null;
        selectedToDuplicateQuery = q;
        JsfUtil.addSuccessMessage("Duplicated");
    }

    public void duplicate() {
        if (selectedQuery == null) {
            JsfUtil.addErrorMessage("Noting selected.");
            return;
        }
        List<QueryComponent> cs = criteria(selectedQuery);
        QueryComponent q = SerializationUtils.clone(selectedQuery);
        q.setId(null);
        q.setCreatedAt(new Date());
        q.setName(q.getName() + "1");
        q.setCreatedBy(webUserController.getLoggedUser());
        getFacade().create(q);

        for (QueryComponent c : cs) {
            QueryComponent newC = SerializationUtils.clone(c);
            newC.setId(null);
            newC.setName(newC.getName() + "1");
            newC.setParentComponent(q);
            newC.setCreatedAt(new Date());
            newC.setCreatedBy(webUserController.getLoggedUser());
            getFacade().create(newC);
        }

        items = null;
        selectedQuery = q;
        JsfUtil.addSuccessMessage("Duplicated");
    }

    public void retire() {
        if (selectedQuery == null) {
            JsfUtil.addErrorMessage("Nothing Selected");
            return;
        }
        selectedQuery.setRetired(true);
        selectedQuery.setRetiredAt(new Date());
        selectedQuery.setRetiredBy(webUserController.getLoggedUser());
        getFacade().edit(selectedQuery);
        selectedQuery = null;
        items = null;
        JsfUtil.addSuccessMessage("Removed");
    }

    public Jpq createAPopulationCountQuery(QueryComponent qc, Area qarea, Integer qyear) {
        Jpq jpql = new Jpq();
        jpql.setQc(qc);
        jpql.setJselect("select r.longValue1  ");
        jpql.setJfrom(" from Relationship r ");
        jpql.setJwhere(" where r.retired=:f ");
        jpql.setJwhere(jpql.getJwhere() + " and r.area=:a ");
        jpql.getM().put("a", qarea);
        RelationshipType t = qc.getPopulationType();
        if (t != null) {
            jpql.setJwhere(jpql.getJwhere() + " and r.relationshipType=:t ");
            jpql.getM().put("t", t);
        }
        if (qyear != null && qyear != 0) {
            jpql.setJwhere(jpql.getJwhere() + " and r.yearInt=:y");
            jpql.getM().put("y", qyear);
        }
        jpql.setJorderBy(" order by r.id desc");
        jpql.getM().put("f", false);
        jpql.setJgroupby("");
        jpql.setLongResult(getItemFacade().findLongByJpql(jpql.getJpql(), jpql.getM(), 1));
        return jpql;
    }

    public Jpq createClientQuery(QueryComponent qc, Area ccArea, Date ccFrom, Date ccTo, Integer ccYear, Integer ccQuarter) {

        Jpq jpql = new Jpq();
        jpql.setQc(qc);
        List<QueryComponent> criterias = criteria(qc);
        jpql.getM().put("f", false);
        jpql.getM().put("res", true);
        if (criterias == null || criterias.isEmpty()) {
            // <editor-fold defaultstate="collapsed" desc="No Criteria">

            jpql.setJwhere(" where c.retired=:f and c.reservedClient<>:res ");
            jpql.setJfrom("  from Client c ");
            if (qc.getOutputType() == QueryOutputType.List) {
                jpql.setJselect("select c ");
            } else {

                jpql.setJselect("select count(c) ");
            }

            jpql.setJwhere(jpql.getJwhere());

            if (ccYear != null && ccQuarter != null) {
                //TODO: Correct Code
                jpql.setJwhere(jpql.getJwhere() + " and Extract(YEAR from c.createdAt)=:ey and "
                        + " (EXTRACT(Month from c.createdAt) between :em1 and :em2) ");
                jpql.getM().put("ey", ccYear);
                jpql.getM().put("em1", ccQuarter * 3 - 2);
                jpql.getM().put("em2", ccQuarter * 3);
            } else if (ccYear != null) {
                //TODO: Correct Code
                jpql.setJwhere(jpql.getJwhere() + " and EXTRACT(YEAR from c.createdAt)=:ey ");
                jpql.getM().put("ey", ccYear);
            } else if (ccFrom != null && ccTo != null) {
                jpql.setJwhere(jpql.getJwhere() + " and c.createdAt between :d1 and :d2 ");
                jpql.getM().put("d1", ccFrom);
                jpql.getM().put("d2", ccTo);
            } else if (ccFrom != null) {
                jpql.setJwhere(jpql.getJwhere() + " and c.createdAt > :d1 ");
                jpql.getM().put("d1", ccFrom);
            } else if (ccTo != null) {
                jpql.setJwhere(jpql.getJwhere() + " and c.createdAt < :d2 ");
                jpql.getM().put("d2", ccTo);
            }

            if (ccArea != null) {
                switch (ccArea.getType()) {
                    case District:
                        jpql.setJwhere(jpql.getJwhere() + " and c.person.district=:area ");
                        jpql.getM().put("area", ccArea);
                        break;
                    case Province:
                        jpql.setJwhere(jpql.getJwhere() + " and c.person.province=:area ");
                        jpql.getM().put("area", ccArea);
                        break;
                    //TODO: Add codes for other areas
                }

            }

            if (qc.getOutputType() == QueryOutputType.List) {
                jpql.setClientList(getClientFacade().findByJpql(jpql.getJpql(), jpql.getM()));
            } else {
                jpql.setLongResult(getItemFacade().findLongByJpql(jpql.getJpql(), jpql.getM(), 1));

            }

            return jpql;

            // </editor-fold>
        } else if (criterias.size() == 1) {
            // <editor-fold defaultstate="collapsed" desc="Single Criteria">

            if (qc.getOutputType() == QueryOutputType.List) {
                jpql.setJselect("select distinct(c)  ");
            } else {
                jpql.setJselect("select count(distinct c)  ");
            }

            jpql.setJfrom(" from ClientEncounterComponentItem i join i.itemClient c");
            jpql.setJwhere(" where i.retired=:f and c.reservedClient<>:res ");

            QueryComponent c = criterias.get(0);

            if (c.getMatchType() == QueryCriteriaMatchType.Variable_Value_Check) {
                jpql.setJwhere(jpql.getJwhere() + " and i.item=:v1 and i.itemValue=:d1 ");
                jpql.getM().put("v1", c.getItem());
                jpql.getM().put("d1", c.getItemValue());
            } else if (c.getMatchType() == QueryCriteriaMatchType.Variable_Range_check) {
                jpql.setJwhere(jpql.getJwhere() + " and i.item=:v1 ");
                jpql.getM().put("v1", c.getItem());
                String eval = "";

                switch (c.getEvaluationType()) {
                    case Equal:
                        eval = "=";
                        break;
                    case Grater_than_or_equal:
                        eval = ">=";
                        break;
                    case Grater_than:
                        eval = ">";
                        break;
                    case Less_than:
                        eval = "<";
                        break;
                    case Less_than_or_equal:
                        eval = "<=";
                        break;
                }

                switch (c.getEvaluationType()) {
                    case Equal:
                    case Grater_than_or_equal:
                    case Grater_than:
                    case Less_than:
                    case Less_than_or_equal:
                        switch (c.getQueryDataType()) {
                            case Boolean:
                                jpql.setJwhere(jpql.getJwhere() + " and i.booleanValue" + eval + ":d1");
                                jpql.getM().put("d1", c.getBooleanValue());
                                break;
                            case String:
                                jpql.setJwhere(jpql.getJwhere() + " and i.shortTextValue" + eval + ":d1");
                                jpql.getM().put("d1", c.getShortTextValue());
                                break;
                            case area:
                                jpql.setJwhere(jpql.getJwhere() + " and i.areaValue" + eval + ":d1");
                                jpql.getM().put("d1", c.getAreaValue());
                                break;
                            case institution:
                                jpql.setJwhere(jpql.getJwhere() + " and i.institutionValue" + eval + ":d1");
                                jpql.getM().put("d1", c.getInstitutionValue());
                                break;
                            case integer:
                                jpql.setJwhere(jpql.getJwhere() + " and i.integerNumberValue" + eval + ":d1");
                                jpql.getM().put("d1", c.getIntegerNumberValue());
                                break;
                            case item:
                                jpql.setJwhere(jpql.getJwhere() + " and i.itemValue" + eval + ":d1");
                                jpql.getM().put("d1", c.getItem());
                                break;
                            case real:
                                jpql.setJwhere(jpql.getJwhere() + " and i.realNumberValue" + eval + ":d1");
                                jpql.getM().put("d1", c.getRealNumberValue());
                                break;
                            case longNumber:
                                jpql.setJwhere(jpql.getJwhere() + " and i.longNumberValue" + eval + ":d1");
                                jpql.getM().put("d1", c.getLongNumberValue());
                                break;

                        }
                        break;
                    case Between:

                        switch (c.getQueryDataType()) {

                            case DateTime:
                                break;
                            case integer:
                                jpql.setJwhere(jpql.getJwhere() + " and i.integerNumberValue between :d1 and :d2 ");
                                jpql.getM().put("d1", c.getIntegerNumberValue());
                                jpql.getM().put("d2", c.getIntegerNumberValue2());
                                break;
                            case real:
                                jpql.setJwhere(jpql.getJwhere() + " and i.realNumberValue between :d1 and :d2 ");
                                jpql.getM().put("d1", c.getRealNumberValue());
                                jpql.getM().put("d2", c.getRealNumberValue2());
                                break;
                            case longNumber:
                                jpql.setJwhere(jpql.getJwhere() + " and i.longNumberValue between :d1 and :d2 ");
                                jpql.getM().put("d1", c.getLongNumberValue());
                                jpql.getM().put("d2", c.getLongNumberValue2());
                                break;

                        }

                        break;

                    case Is_null:
                        //TODO : Add logic
                        break;

                    case Not_null:
                        //TODO : Add logic
                        break;
                }

            }

            if (ccYear != null && ccQuarter != null) {
                //TODO: Correct Code
                jpql.setJwhere(jpql.getJwhere() + " and EXTRACT(YEAR FROM c.createdAt)=:ey and "
                        + " ( EXTRACT(MONTH FROM c.createdAt) BETWEEN :em1 and :em2  )");
                jpql.getM().put("ey", ccYear);
                jpql.getM().put("em1", ccQuarter * 3 - 2);
                jpql.getM().put("em2", ccQuarter * 3);
            } else if (ccYear != null) {
                //TODO: Correct Code
                jpql.setJwhere(jpql.getJwhere() + " and EXTRACT(YEAR FROM c.createdAt)=:ey ");
                jpql.getM().put("ey", ccYear);
            } else if (ccFrom != null && ccTo != null) {
                jpql.setJwhere(jpql.getJwhere() + " and c.createdAt between :date1 and :date2 ");
                jpql.getM().put("date1", ccFrom);
                jpql.getM().put("date2", ccTo);
            } else if (ccFrom != null) {
                jpql.setJwhere(jpql.getJwhere() + " and c.createdAt > :date1 ");
                jpql.getM().put("date1", ccFrom);
            } else if (ccTo != null) {
                jpql.setJwhere(jpql.getJwhere() + " and c.createdAt < :date2 ");
                jpql.getM().put("date2", ccTo);
            }

            if (ccArea != null) {
                switch (ccArea.getType()) {
                    case District:
                        jpql.setJwhere(jpql.getJwhere() + " and i.itemClient.person.district=:area ");
                        jpql.getM().put("area", ccArea);
                        break;
                    case Province:
                        jpql.setJwhere(jpql.getJwhere() + " and i.itemClient.person.province=:area ");
                        jpql.getM().put("area", ccArea);
                        break;
                    //TODO: Add codes for other areas
                }

            }

            if (qc.getOutputType() == QueryOutputType.List) {
                jpql.setClientList(getClientFacade().findByJpql(jpql.getJpql(), jpql.getM()));
            } else {
                jpql.setLongResult(getItemFacade().findLongByJpql(jpql.getJpql(), jpql.getM(), 1));

            }

            return jpql;

            // </editor-fold>
        } else {
            // <editor-fold defaultstate="collapsed" desc="Multiple Criteria">

            String ss = "";
            if (qc.getOutputType() == QueryOutputType.List) {
                ss = "select distinct (c) from Client c, ";
            } else {
                ss = "select count(distinct c) from Client c, ";
            }

            String w1 = " where ";
            String w2 = "";
            String w3 = "";

            int count = 1;
            for (QueryComponent qcm : criterias) {
                if (count != criterias.size()) {
                    ss += " ClientEncounterComponentItem i" + count + ", ";
                    w2 += " c.id=i" + count + ".itemClient.id and ";
                    w3 += createJpqlBlockFromQueryComponentCriteria(qcm, jpql.getM(), count) + " and ";
                } else {
                    ss += " ClientEncounterComponentItem i" + count + " ";
                    w2 += " c.id=i" + count + ".itemClient.id and ";
                    w3 += createJpqlBlockFromQueryComponentCriteria(qcm, jpql.getM(), count) + " ";
                }

                count++;
            }

            jpql.setJselect("");
            jpql.setJfrom(ss);
            jpql.setJwhere(w1 + w2 + w3 + " and c.retired=:f and c.reservedClient<>:res ");

            if (ccYear != null && ccQuarter != null) {
                //TODO: Correct Code
                jpql.setJwhere(jpql.getJwhere() + " and extract(year from c.createdAt)=:ey and "
                        + " (extract(month from c.createdAt) between :em1 and :em2 )");
                jpql.getM().put("ey", ccYear);
                jpql.getM().put("em1", ccQuarter * 3 - 2);
                jpql.getM().put("em2", ccQuarter * 3);
            } else if (ccYear != null) {
                //TODO: Correct Code
                jpql.setJwhere(jpql.getJwhere() + " and extract(year from c.createdAt)=:ey ");
                jpql.getM().put("ey", ccYear);
            } else if (ccFrom != null && ccTo != null) {
                jpql.setJwhere(jpql.getJwhere() + " and c.createdAt between :date1 and :date2 ");
                jpql.getM().put("date1", ccFrom);
                jpql.getM().put("date2", ccTo);
            } else if (ccFrom != null) {
                jpql.setJwhere(jpql.getJwhere() + " and c.createdAt > :date1 ");
                jpql.getM().put("date1", ccFrom);
            } else if (ccTo != null) {
                jpql.setJwhere(jpql.getJwhere() + " and c.createdAt < :date2 ");
                jpql.getM().put("date2", ccTo);
            } else {

            }

            if (ccArea != null) {
                switch (ccArea.getType()) {
                    case District:
                        jpql.setJwhere(jpql.getJwhere() + " and c.person.district=:area ");
                        jpql.getM().put("area", ccArea);
                        break;
                    case Province:
                        jpql.setJwhere(jpql.getJwhere() + " and c.person.province=:area ");
                        jpql.getM().put("area", ccArea);
                        break;
                    //TODO: Add codes for other areas
                    default:

                }

            }

            //TODO : More code needed for MOH, PHM ,etc
            jpql.setJgroupby("");

            if (qc.getOutputType() == QueryOutputType.List) {
                jpql.setClientList(getClientFacade().findByJpql(jpql.getJpql(), jpql.getM()));
            } else {
                jpql.setLongResult(getItemFacade().findLongByJpql(jpql.getJpql(), jpql.getM(), 1));
            }

            /**
             *
             *
             *
             * select count(distinct c) from Client c,
             * ClientEncounterComponentItem i1, ClientEncounterComponentItem i2
             * * where c.id=i1.itemClient.id and c.id=i2.itemClient.id and
             * i1.item=:v1 and and i2.item=:v2 and i2.itemValue=:d2 and
             * c.retired=:f and c.createdAt between :date1 and :date2 and
             * c.person.district=:area *
             *
             *
             *
             */
            //select count(distinct c) from Client c,
            // ClientEncounterComponentItem i1,  ClientEncounterComponentItem i2   
            // where  c.id=i1.itemClient.id and  
            // c.id=i2.itemClient.id and  
            // i1.item=:v1 and  and  i2.item=:v2 and i2.itemValue=:d2   and c.retired=:f  and c.createdAt between :date1 and :date2  and c.person.district=:area  
            return jpql;

            // </editor-fold>
        }
    }

    public String createJpqlBlockFromQueryComponentCriteria(QueryComponent c, Map m, int prefix) {
        String bq = "";
        if (c.getMatchType() == QueryCriteriaMatchType.Variable_Value_Check) {
            bq += " i" + prefix + ".item=:v" + prefix + " and i" + prefix + ".itemValue=:d" + prefix + " ";
            m.put("v" + prefix + "", c.getItem());
            m.put("d" + prefix + "", c.getItemValue());
        } else if (c.getMatchType() == QueryCriteriaMatchType.Variable_Range_check) {
            bq += " i" + prefix + ".item=:v" + prefix + " and ";
            m.put("v" + prefix + "", c.getItem());
            String eval = "";
            switch (c.getEvaluationType()) {
                case Equal:
                    eval = "=";
                    break;
                case Grater_than_or_equal:
                    eval = ">=";
                    break;
                case Grater_than:
                    eval = ">";
                    break;
                case Less_than:
                    eval = "<";
                    break;
                case Less_than_or_equal:
                    eval = "<=";
                    break;
            }
            switch (c.getEvaluationType()) {
                case Equal:
                case Grater_than_or_equal:
                case Grater_than:
                case Less_than:
                case Less_than_or_equal:
                    switch (c.getQueryDataType()) {
                        case Boolean:
                            bq += "  i" + prefix + ".booleanValue" + eval + ":d" + prefix + "";
                            m.put("d" + prefix + "", c.getBooleanValue());
                            break;
                        case DateTime:
                            bq += "  i" + prefix + ".dateValue" + eval + ":d" + prefix + "";
                            m.put("d" + prefix + "", c.getDateValue());
                            break;
                        case String:
                            bq += "  i" + prefix + ".shortTextValue" + eval + ":d" + prefix + "";
                            m.put("d" + prefix + "", c.getShortTextValue());
                            break;
                        case area:
                            bq += "  i" + prefix + ".areaValue" + eval + ":d" + prefix + "";
                            m.put("d" + prefix + "", c.getAreaValue());
                            break;
                        case institution:
                            bq += "  i" + prefix + ".institutionValue" + eval + ":d" + prefix + "";
                            m.put("d" + prefix + "", c.getInstitutionValue());
                            break;
                        case integer:
                            bq += "  i" + prefix + ".integerNumberValue" + eval + ":d" + prefix + "";
                            m.put("d" + prefix + "", c.getIntegerNumberValue());
                            break;
                        case item:
                            bq += "  i" + prefix + ".itemValue" + eval + ":d" + prefix + "";
                            m.put("d" + prefix + "", c.getItem());
                            break;
                        case real:
                            bq += "  i" + prefix + ".realNumberValue" + eval + ":d" + prefix + "";
                            m.put("d" + prefix + "", c.getRealNumberValue());
                            break;
                        case longNumber:
                            bq += "  i" + prefix + ".longNumberValue" + eval + ":d" + prefix + "";
                            m.put("d" + prefix + "", c.getLongNumberValue());
                            break;

                    }
                    break;
                case Not_null:
                    switch (c.getQueryDataType()) {
                        case Boolean:
                            bq += "  i" + prefix + ".booleanValue is NOT null ";
                            break;
                        case DateTime:
                            bq += "  i" + prefix + ".dateValue is NOT null ";
                            break;
                        case String:
                            bq += "  i" + prefix + ".shortTextValue is NOT null ";
                            break;
                        case area:
                            bq += "  i" + prefix + ".areaValue is NOT null ";
                            break;
                        case institution:
                            bq += "  i" + prefix + ".institutionValue is NOT null ";
                            break;
                        case integer:
                            bq += "  i" + prefix + ".integerNumberValue is NOT null ";
                            break;
                        case item:
                            bq += "  i" + prefix + ".itemValue is NOT null ";
                            break;
                        case real:
                            bq += "  i" + prefix + ".realNumberValue is NOT null ";
                            break;
                        case longNumber:
                            bq += "  i" + prefix + ".longNumberValue is NOT null ";
                            break;

                    }

            }
        }
        return bq;
    }

    public Jpq createAClientListQuery(QueryComponent qc) {

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
                    case integer:
                        if (r.getEvaluation() == Evaluation.eq) {
                            qs += " and (i.item.code=:varc" + count + " and i.itemValue.code=:valc" + count + ")";
                            j.getM().put("varc" + count, r.getVariableCode());
                            j.getM().put("valc" + count, r.getValueCode());
                        }
                        break;
                    case institution:
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

        j.setClientList(getClientFacade().findByJpql(j.getJpql(), j.getM()));
        return j;
    }

    public Jpq createAnEncounterListQuery(QueryComponent qc) {

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
                    case integer:
                        if (r.getEvaluation() == Evaluation.eq) {
                            qs += " and (i.item.code=:varc" + count + " and i.itemValue.code=:valc" + count + ")";
                            j.getM().put("varc" + count, r.getVariableCode());
                            j.getM().put("valc" + count, r.getValueCode());
                        }
                        break;
                    case institution:
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

        j.setEncounterList(getEncounterFacade().findByJpql(j.getJpql(), j.getM()));

        return j;
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

    public QueryComponent getSelectedQuery() {
        return selectedQuery;
    }

    public void setSelectedQuery(QueryComponent selectedQuery) {
        this.selectedQuery = selectedQuery;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private QueryComponentFacade getFacade() {
        return ejbFacade;
    }

    public QueryComponent prepareCreate() {
        selectedQuery = new QueryComponent();
        initializeEmbeddableKey();
        return selectedQuery;
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
            selectedQuery = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<QueryComponent> getItems() {
//        if (items == null) {
//            items = getFacade().findAll();
//        }
        return items;
    }

    private void persist(PersistAction persistAction, String successMessage) {
        if (selectedQuery != null) {
            setEmbeddableKeys();
            try {
                if (persistAction != PersistAction.DELETE) {
                    getFacade().edit(selectedQuery);
                } else {
                    getFacade().remove(selectedQuery);
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
        if (from == null) {
            from = CommonController.startOfTheYear();
        }
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Date getTo() {
        if (to == null) {
            to = new Date();
        }
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
        if (year == null) {
            year = CommonController.getYear(new Date());
        }
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getQuarter() {
        if (quarter == null) {
            quarter = CommonController.getQuarter(new Date());
        }
        return quarter;
    }

    public void setQuarter(Integer quarter) {
        this.quarter = quarter;
    }

    public Integer getMonth() {
        if (month == null) {
            month = CommonController.getMonth(new Date());
        }
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
            addingQuery.setQueryLevel(QueryLevel.Query);
        }

        return addingQuery;
    }

    public void setAddingQuery(QueryComponent addingQuery) {
        this.addingQuery = addingQuery;
    }

    public QueryComponent getRemoving() {
        return removing;
    }

    public void setRemoving(QueryComponent removing) {
        this.removing = removing;
    }

    public QueryComponent getSelectedCretirian() {
        return selectedCretirian;
    }

    public void setSelectedCretirian(QueryComponent selectedCretirian) {
        this.selectedCretirian = selectedCretirian;
    }

    public QueryComponent getMoving() {
        return moving;
    }

    public void setMoving(QueryComponent moving) {
        this.moving = moving;
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

    public List<QueryComponent> getCategories() {
        if (categories == null) {
            categories = fillCategories();
        }
        return categories;
    }

    public void setCategories(List<QueryComponent> categories) {
        this.categories = categories;
    }

    public QueryComponent getAddingCategory() {
        if (addingCategory == null) {
            addingCategory = new QueryComponent();
            addingCategory.setQueryLevel(QueryLevel.Category);
        }
        return addingCategory;
    }

    public void setAddingCategory(QueryComponent addingCategory) {
        this.addingCategory = addingCategory;
    }

    public QueryComponent getAddingSubcategory() {
        if (addingSubcategory == null) {
            addingSubcategory = new QueryComponent();
            addingSubcategory.setQueryLevel(QueryLevel.Subcategory);
        }
        return addingSubcategory;
    }

    public void setAddingSubcategory(QueryComponent addingSubcategory) {
        this.addingSubcategory = addingSubcategory;
    }

    public QueryComponent getAddingCriterian() {
        if (addingCriterian == null) {
            addingCriterian = new QueryComponent();
            addingCriterian.setQueryLevel(QueryLevel.Criterian);
        }
        return addingCriterian;
    }

    public void setAddingCriterian(QueryComponent addingCriterian) {
        this.addingCriterian = addingCriterian;
    }

    public QueryComponent getSelected() {
        return selected;
    }

    public void setSelected(QueryComponent selected) {
        this.selected = selected;
    }

    public QueryFilterPeriodType getPeriodType() {
        return periodType;
    }

    public void setPeriodType(QueryFilterPeriodType periodType) {
        this.periodType = periodType;
    }

    public QueryFilterAreaType getAreaType() {
        return areaType;
    }

    public void setAreaType(QueryFilterAreaType areaType) {
        this.areaType = areaType;
    }

    public boolean isFilterPhm() {
        return filterPhm;
    }

    public void setFilterPhm(boolean filterPhm) {
        this.filterPhm = filterPhm;
    }

    public Area getPhm() {
        return phm;
    }

    public void setPhm(Area phm) {
        this.phm = phm;
    }

    public List<QueryResult> getQrs() {
        return qrs;
    }

    public void setQrs(List<QueryResult> qrs) {
        this.qrs = qrs;
    }

    public QueryResult getQr() {
        return qr;
    }

    public void setQr(QueryResult qr) {
        this.qr = qr;
    }

    public AreaController getAreaController() {
        return areaController;
    }

    public String getBarChartStringForChartJs() {
        String js = "            var ctx = document.getElementById('myChart').getContext('2d');\n"
                + "            var chart = new Chart(ctx, {\n"
                + "                // The type of chart we want to create\n"
                + "                type: 'bar',\n"
                + "\n"
                + "                // The data for our dataset\n"
                + "                data: {\n"
                + "                    labels: [MyLabelsList],\n"
                + "                    datasets: [{\n"
                + "                            label: 'MyFirstdataset',\n"
                + "                            backgroundColor: 'rgb(255, 99, 132)',\n"
                + "                            borderColor: 'rgb(255, 99, 132)',\n"
                + "                            data: [MyDataList]\n"
                + "                        }]\n"
                + "                },\n"
                + "\n"
                + "                // Configuration options go here\n"
                + "                options: {}\n"
                + "            });\n"
                + "";

        js = js.replace("MyLabelsList", convertLabelsToChartNameSeries(qrs));
        if (selectedForQuery.getQueryType() == QueryType.Indicator) {
            js = js.replace("MyDataList", convertDoubleValuesToChartDataSeries(qrs));
        } else {
            js = js.replace("MyDataList", convertLongValuesToChartDataSeries(qrs));
        }
        js = js.replace("MyFirstdataset", selectedForQuery.getName());
        return js;
    }

    public String getChartString() {
        chartString = getBarChartStringForChartJs();
        return chartString;
    }

    public void setChartString(String chartString) {
        this.chartString = chartString;
    }

    public String convertLongValuesToChartDataSeries(List<QueryResult> cqrs) {
        String s = "";
        int i = 0;
        if (cqrs == null) {
            return "";
        }
        for (QueryResult e : cqrs) {
            i++;
            s += e.getJpq().getLongResult();
            if (i != cqrs.size()) {
                s += ", ";
            }
        }
        return s;
    }

    public String convertDoubleValuesToChartDataSeries(List<QueryResult> cqrs) {
        String s = "";
        int i = 0;
        if (cqrs == null) {
            return "";
        }
        for (QueryResult e : cqrs) {
            i++;
            s += e.getJpq().getDblResult();
            if (i != cqrs.size()) {
                s += ", ";
            }
        }
        return s;
    }

    public String convertLabelsToChartNameSeries(List<QueryResult> cqrs) {
        String s = "";
        int i = 0;
        if (cqrs == null) {
            return "";
        }
        for (QueryResult e : cqrs) {
            i++;
            s += "'" + e.getArea().getName() + "'";
            if (i != cqrs.size()) {
                s += ", ";
            }
        }
        return s;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public QueryComponent findByCode(String code) {
        if (code == null) {
            return null;
        }
        QueryComponent newQ = null;
        List<QueryComponent> tqcs = applicationController.getQueryComponents();
        for (QueryComponent q : tqcs) {
            if (q.getCode() != null && q.getCode().equalsIgnoreCase(code)) {
                if (newQ == null) {
                    newQ = q;
                } else {
                    if (newQ.getId() < q.getId()) {
                        newQ = q;
                    }
                }
            }
        }
        return newQ;
    }

    public QueryComponent getSelectedToDuplicateQuery() {
        return selectedToDuplicateQuery;
    }

    public void setSelectedToDuplicateQuery(QueryComponent selectedToDuplicateQuery) {
        this.selectedToDuplicateQuery = selectedToDuplicateQuery;
    }

    public ApplicationController getApplicationController() {
        return applicationController;
    }

    public StreamedContent getResultExcelFile() {
        return resultExcelFile;
    }

    public void setResultExcelFile(StreamedContent resultExcelFile) {
        this.resultExcelFile = resultExcelFile;
    }

    public List<QueryComponent> getExcels() {
        if (excels == null) {
            excels = fillExcels();
        }
        return excels;
    }

    public void setExcels(List<QueryComponent> excels) {
        this.excels = excels;
    }

    public List<QueryComponent> getIndicators() {
        if (indicators == null) {
            indicators = fillIndicators();
        }
        return indicators;
    }

    public void setIndicators(List<QueryComponent> indicators) {
        this.indicators = indicators;
    }

    public List<QueryComponent> getPopulations() {
        if (populations == null) {
            populations = fillPopulations();
        }
        return populations;
    }

    public void setPopulations(List<QueryComponent> populations) {
        this.populations = populations;
    }

    public Month getMonthEnum() {
        return monthEnum;
    }

    public void setMonthEnum(Month monthEnum) {
        this.monthEnum = monthEnum;
    }

    public UserTransactionController getUserTransactionController() {
        return userTransactionController;
    }

    public ClientEncounterComponentItemFacade getClientEncounterComponentItemFacade() {
        return clientEncounterComponentItemFacade;

    }

    public List<QueryComponent> getCounts() {
        if (counts == null) {
            counts = fillCounts();
        }
        return counts;
    }

    public void setCounts(List<QueryComponent> counts) {
        this.counts = counts;
    }

    public QueryComponent getSelectedCountCriteria() {
        return selectedCountCriteria;
    }

    public void setSelectedCountCriteria(QueryComponent selectedCountCriteria) {
        this.selectedCountCriteria = selectedCountCriteria;
    }

    public StreamedContent getDownloadingFile() {
        if (selected == null) {
            lk.gov.health.phsp.facade.util.JsfUtil.addErrorMessage("Nothing selected");
            return null;
        }
        Upload u = uploadController.findUploadForComponent(selected);
        if (u == null) {
            lk.gov.health.phsp.facade.util.JsfUtil.addErrorMessage("Nothing selected");
            return null;
        }

        if (u.getBaImage() == null) {
            lk.gov.health.phsp.facade.util.JsfUtil.addErrorMessage("No file is uploaded.");
            return null;
        }
        InputStream stream = new ByteArrayInputStream(u.getBaImage());
        if (u.getFileType() == null || u.getFileType().trim().equals("")) {
            u.setFileType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        }
        downloadingFile = new DefaultStreamedContent(stream, u.getFileType(), u.getFileName());
        return downloadingFile;
    }

    public void setDownloadingFile(StreamedContent downloadingFile) {
        this.downloadingFile = downloadingFile;
    }

    public QueryComponent getSelectedCount() {
        return selectedCount;
    }

    public void setSelectedCount(QueryComponent selectedCount) {
        this.selectedCount = selectedCount;
    }

    public QueryComponent getSelectedPopulation() {
        return selectedPopulation;
    }

    public void setSelectedPopulation(QueryComponent selectedPopulation) {
        this.selectedPopulation = selectedPopulation;
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
