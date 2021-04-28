/*
 * The MIT License
 *
 * Copyright 2021 buddhika.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lk.gov.health.phsp.bean;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.ClientEncounterComponentItem;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.entity.QueryComponent;
import lk.gov.health.phsp.enums.EncounterType;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.enums.Quarter;
import lk.gov.health.phsp.enums.QueryCriteriaMatchType;
import lk.gov.health.phsp.enums.QueryLevel;
import lk.gov.health.phsp.enums.QueryType;
import lk.gov.health.phsp.enums.RelationshipType;
import lk.gov.health.phsp.enums.TimePeriodType;
import lk.gov.health.phsp.facade.ClientEncounterComponentItemFacade;
import lk.gov.health.phsp.facade.EncounterFacade;
import lk.gov.health.phsp.pojcs.EncounterWithComponents;
import lk.gov.health.phsp.pojcs.Jpq;
import lk.gov.health.phsp.pojcs.NcdReportTem;
import lk.gov.health.phsp.pojcs.QueryWithCriteria;
import lk.gov.health.phsp.pojcs.Replaceable;
import lk.gov.health.phsp.pojcs.ReportTimePeriod;
import org.bouncycastle.jcajce.provider.digest.GOST3411;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author buddhika
 */
@Named(value = "indicatorController")
@SessionScoped
public class IndicatorController implements Serializable {

    @Inject
    private StoredQueryResultController storedQueryResultController;
    @Inject
    UserTransactionController userTransactionController;
    @Inject
    QueryComponentController queryComponentController;
    @Inject
    RelationshipController relationshipController;
    @Inject
    InstitutionController institutionController;
    @Inject
    InstitutionApplicationController institutionApplicationController;
    @Inject
    ApplicationController applicationController;

    @EJB
     ClientEncounterComponentItemFacade clientEncounterComponentItemFacade;
    @EJB
     EncounterFacade  encounterFacade;

    private Date fromDate;
    private Date toDate;
    private Institution institution;
    private Area area;
    private String message;
    String result;
    private QueryComponent queryComponent;
    private ReportTimePeriod reportTimePeriod;
    private TimePeriodType timePeriodType;
    private Integer year;
    private Integer quarter;
    private Integer month;
    private Integer dateOfMonth;
    private Quarter quarterEnum;
    private boolean recalculate;

    private List<QueryComponent> selectedIndicators;

    /**
     * Creates a new instance of IndicatorController
     */
    public IndicatorController() {
    }

    public String toProcesCountsForSelectedIndicators() {
        message = "";
        result = "";
        institution = null;
        return "/indicators/clinic_counts_for_selected_indicators";
    }

    public String toProcesCounts() {
        message = "";
        result = "";
        institution = null;
        return "/indicators/clinic_counts";
    }

    public String toRdhsMonthly() {
        message = "";
        result = "";
        institution = null;
        return "/indicators/rdhs_monthly";
    }

    public String toPdhsMonthly() {
        message = "";
        result = "";
        institution = null;
        return "/indicators/pdhs_monthly";
    }

    public String toHospitalMonthly() {
        message = "";
        result = "";
        institution = null;
        return "/indicators/hospital_monthly";
    }

    public String toClinicMonthly() {
        message = "";
        result = "";
        institution = null;
        return "/indicators/clinic_monthly";
    }

    public String toDistrictMonthly() {
        message = "";
        result = "";
        institution = null;
        area = null;
        return "/indicators/district_monthly";
    }

    public String toProvinceMonthly() {
        message = "";
        result = "";
        institution = null;
        area = null;
        return "/indicators/province_monthly";
    }

    public String toNationalMonthly() {
        message = "";
        result = "";
        institution = null;
        return "/indicators/national_monthly";
    }

    public String toIndicatorIndex() {
        userTransactionController.recordTransaction("To View Indicators");
        return "/indicators/index";
    }

    public void runClinicCounts() {
        if (institution == null) {
            JsfUtil.addErrorMessage("HLC ?");
            return;
        }
        if (queryComponent == null) {
            JsfUtil.addErrorMessage("Indicators ?");
            return;
        }
        if (year == 0) {
            JsfUtil.addErrorMessage("Year ?");
            return;
        }
        if (month == null) {
            JsfUtil.addErrorMessage("Month");
            return;
        }
        if (institution.getInstitutionType() == null) {
            JsfUtil.addErrorMessage("No Type for the institution");
            return;
        }
        if (institution.getInstitutionType() != InstitutionType.Clinic) {
            JsfUtil.addErrorMessage("Selected institution is NOT a HLC?");
            return;
        }
        Jpq j = new Jpq();
        fromDate = CommonController.startOfTheMonth(year, month);
        toDate = CommonController.endOfTheMonth(year, month);

        List<QueryWithCriteria> qs = new ArrayList<>();
        List<EncounterWithComponents> encountersWithComponents;

        List<Long> encounterIds = findEncounterIds(fromDate,
                toDate,
                institution);

        encountersWithComponents = findEncountersWithComponents(encounterIds);
        if (encountersWithComponents == null) {
            j.setErrorMessage("No data for the selected institution for the period");
            JsfUtil.addErrorMessage("No data?");
            return;
        }

        QueryWithCriteria qwc = new QueryWithCriteria();
        qwc.setQuery(queryComponent);
        qwc.setCriteria(findCriteriaForQueryComponent(queryComponent.getCode()));

        Long value = calculateIndividualQueryResult(encountersWithComponents, qwc);
        j.setMessage("Clinic : " + institution.getName() + "\n");
        j.setMessage(j.getMessage() + "From : " + CommonController.formatDate(fromDate) + "\n");
        j.setMessage(j.getMessage() + "To : " + CommonController.formatDate(toDate) + "\n");
        j.setMessage(j.getMessage() + "Number of Encounters : " + encountersWithComponents.size() + "\n");
        j.setMessage(j.getMessage() + "Count : " + qwc.getQuery().getName() + "\n");
        if (value != null) {
            storedQueryResultController.saveValue(qwc.getQuery(), fromDate, toDate, institution, value);
            j.setMessage(j.getMessage() + "Result : " + value + "\n");
        }else{
            j.setMessage(j.getMessage() + "Result : No Result\n");
        }
        message = CommonController.stringToHtml(j.getErrorMessage());
        result = CommonController.stringToHtml(j.getMessage());
    }

    public void runClinicCountsForSelectedIndicators() {
        if (institution == null) {
            JsfUtil.addErrorMessage("HLC ?");
            return;
        }
        if (selectedIndicators == null) {
            JsfUtil.addErrorMessage("Indicators ?");
            return;
        }
        if (selectedIndicators.isEmpty()) {
            JsfUtil.addErrorMessage("Indicators?");
            return;
        }
        if (year == 0) {
            JsfUtil.addErrorMessage("Year ?");
            return;
        }
        if (month == null) {
            JsfUtil.addErrorMessage("Month");
            return;
        }
        if (institution.getInstitutionType() == null) {
            JsfUtil.addErrorMessage("No Type for the institution");
            return;
        }
        if (institution.getInstitutionType() != InstitutionType.Clinic) {
            JsfUtil.addErrorMessage("Selected institution is NOT a HLC?");
            return;
        }
        Jpq j = new Jpq();
        fromDate = CommonController.startOfTheMonth(year, month);
        toDate = CommonController.endOfTheMonth(year, month);

        List<QueryWithCriteria> qs = new ArrayList<>();
        List<EncounterWithComponents> encountersWithComponents;

        List<Long> encounterIds = findEncounterIds(fromDate,
                toDate,
                institution);

        encountersWithComponents = findEncountersWithComponents(encounterIds);
        if (encountersWithComponents == null) {
            j.setErrorMessage("No data for the selected institution for the period");
            JsfUtil.addErrorMessage("No data?");
            return;
        }
        Map<Long, QueryComponent> qcs = new HashMap<>();
        List<Replaceable> rs = new ArrayList<>();
        for (QueryComponent qc : selectedIndicators) {
            List<Replaceable> trs = findReplaceblesInIndicatorQuery(qc.getIndicatorQuery());
            if (trs != null && !trs.isEmpty()) {
                rs.addAll(trs);
            }
        }

        for (Replaceable r : rs) {
            QueryComponent temqc = queryComponentController.findLastQuery(r.getQryCode());
            if (temqc == null) {
                j.setError(true);
                j.setErrorMessage(j.getErrorMessage() + "\nCount " + r.getQryCode() + " used in indicators not found.\n");
                continue;
            }
            if (null == temqc.getQueryType()) {
                j.setError(true);
                j.setErrorMessage(j.getErrorMessage() + "\n" + "No Type set for the query " + r.getQryCode() + " is not set. \n");

            } else {
                switch (temqc.getQueryType()) {
                    case Client_Count:
                    case Encounter_Count:
                        qcs.put(temqc.getId(), temqc);
                        break;
                    case Population:
                        break;
                    default:
                        j.setErrorMessage(j.getErrorMessage() + "\n" + "Wrong Query Type for - " + r.getQryCode() + "\n");
                }
            }
        }

        for (QueryComponent qcc : qcs.values()) {
            QueryWithCriteria qwc = new QueryWithCriteria();
            qwc.setQuery(qcc);
            qwc.setCriteria(findCriteriaForQueryComponent(qcc.getCode()));
            qs.add(qwc);
        }

        j.setMessage("Clinic : " + institution.getName());
        j.setMessage(j.getMessage() + "From : " + fromDate + "\n");
        j.setMessage(j.getMessage() + "To : " + toDate + "\n");
        j.setMessage(j.getMessage() + "Number of Encounters : " + encountersWithComponents.size() + "\n");
        j.setMessage(j.getMessage() + "Number of Counts : " + qs.size() + "\n");

        for (QueryWithCriteria qwc : qs) {
            Long value = calculateIndividualQueryResult(encountersWithComponents, qwc);
            if (value != null) {
                if (qwc != null) {
                    storedQueryResultController.saveValue(qwc.getQuery(), fromDate, toDate, institution, value);
                    j.setMessage(j.getMessage() + "" + "Count : " + qwc.getQuery().getName() + " = " + value + "\n");
                }
            }
        }

        message = CommonController.stringToHtml(j.getErrorMessage());
        result = CommonController.stringToHtml(j.getMessage());
    }

    public  Long calculateIndividualQueryResult(List<EncounterWithComponents> ewcs, QueryWithCriteria qwc) {
        Long result = 0l;
        if (ewcs == null) {
            JsfUtil.addErrorMessage("No Encounters");
            return result;
        }
        if (qwc == null) {
            JsfUtil.addErrorMessage("No Counts to perform");
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

    private  boolean findMatch(List<ClientEncounterComponentItem> ccs, QueryWithCriteria qrys) {
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
                if (!thisMatchOk) {
                    suitableForInclusion = false;
                }
            }

        }

        return suitableForInclusion;
    }

    private  boolean matchQuery(QueryComponent q, ClientEncounterComponentItem clientValue) {
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
                    real1 = q.getRealNumberValue();
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

    public  boolean clientValueIsNotNull(QueryComponent q, ClientEncounterComponentItem clientValue) {
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

    public  List<EncounterWithComponents> findEncountersWithComponents(List<Long> ids) {
        if (ids == null) {
            JsfUtil.addErrorMessage("No Encounter IDs");
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

    private  List<ClientEncounterComponentItem> findClientEncounterComponentItems(Long endId) {
        String j;
        Map m;
        m = new HashMap();
        j = "select f from ClientEncounterComponentItem f "
                + " where f.retired=false "
                + " and f.encounter.id=:eid";
        m.put("eid", endId);
        List<ClientEncounterComponentItem> ts = clientEncounterComponentItemFacade.findByJpql(j, m);
        return ts;
    }

    public  List<Long> findEncounterIds(Date fromDate, Date toDate, Institution institution) {
        String j = "select e.id "
                + " from  ClientEncounterComponentFormSet f join f.encounter e"
                + " where e.retired<>:er"
                + " and f.retired<>:fr ";
        j += " and f.completed=:fc ";
        j += " and e.institution=:i "
                + " and e.encounterType=:t "
                + " and e.encounterDate between :fd and :td"
                + " order by e.id";
        Map m = new HashMap();
        m.put("i", institution);
        m.put("t", EncounterType.Clinic_Visit);
        m.put("er", true);
        m.put("fr", true);
        m.put("fc", true);
        m.put("fd", fromDate);
        m.put("td", toDate);
        List<Long> encs = encounterFacade.findLongList(j, m);
        return encs;
    }

    public List<QueryComponent> findCriteriaForQueryComponent(String qryCode) {
        if (qryCode == null) {
            return null;
        }
        List<QueryComponent> output = new ArrayList<>();
        for (QueryComponent qc : applicationController.getQueryComponents()) {
            if (qc.getQueryLevel() == null) {
                continue;
            }
            if (qc.getParentComponent() == null) {
                continue;
            }

            if (qc.getQueryLevel() == QueryLevel.Criterian) {
                if (qc.getParentComponent().getCode().equalsIgnoreCase(qryCode)) {
                    output.add(qc);
                }
            }
        }
        return output;
    }

    public void runHlcMonthly() {
        if (institution == null) {
            JsfUtil.addErrorMessage("HLC ?");
            return;
        }
        if (queryComponent == null) {
            JsfUtil.addErrorMessage("Indicator ?");
            return;
        }
        if (queryComponent.getQueryType() == null) {
            JsfUtil.addErrorMessage("Indicator Type ?");
            return;
        }
        if (!queryComponent.getQueryType().equals(QueryType.Indicator)) {
            JsfUtil.addErrorMessage("Selected is not an indicator");
            return;
        }
        if (year == 0) {
            JsfUtil.addErrorMessage("Year ?");
            return;
        }
        if (month == null) {
            JsfUtil.addErrorMessage("Month");
            return;
        }
        if (institution.getInstitutionType() == null) {
            JsfUtil.addErrorMessage("No Type for the institution");
            return;
        }
        if (institution.getInstitutionType() != InstitutionType.Clinic) {
            JsfUtil.addErrorMessage("Selected institution is NOT a HLC?");
            return;
        }

        fromDate = CommonController.startOfTheMonth(year, month);
        toDate = CommonController.endOfTheMonth(year, month);
        Jpq j = new Jpq();
        j.setMessage("");
        j.setMessage("");

        List<Replaceable> rs = findReplaceblesInIndicatorQuery(queryComponent.getIndicatorQuery());

        for (Replaceable r : rs) {

            QueryComponent temqc = queryComponentController.findLastQuery(r.getQryCode());
            if (temqc == null) {
                j.setError(true);
                j.setMessage(j.getMessage() + "\n" + "Count " + r.getQryCode() + " in the indicator is not found. ");
                continue;
            }

            if (null == temqc.getQueryType()) {
                j.setError(true);
                j.setMessage(j.getMessage() + "\n" + "Type of query " + r.getQryCode() + " in is not set. ");

            } else {
                switch (temqc.getQueryType()) {
                    case Population:
                        if (temqc.getPopulationType() == null) {
                            j.setError(true);
                            j.setMessage(j.getMessage() + "\n" + "Type of Population " + r.getQryCode() + " in is not set. ");
                            continue;
                        }
                        int temYear;
                        Calendar c = Calendar.getInstance();
                        c.setTime(fromDate);
                        temYear = c.get(Calendar.YEAR);
                        Long tp = relationshipController.findPopulationValue(temYear, institution, temqc.getPopulationType());
                        if (tp != null) {
                            r.setTextReplacing(tp + "");
                            r.setSelectedValue(tp + "");
                            j.setMessage(j.getMessage() + r.getQryCode() + " - " + tp + "\n");
                        } else {
                            j.setError(true);
                            j.setMessage(j.getMessage() + "No Population data for " + r.getQryCode() + "\n");
                        }
                        break;

                    case Client_Count:
                    case Encounter_Count:
                        Long tv = storedQueryResultController.findStoredLongValue(temqc, fromDate, toDate, institution, r);
                        if (tv != null) {
                            r.setTextReplacing(tv + "");
                            r.setSelectedValue(tv + "");
                            j.setMessage(j.getMessage() + r.getQryCode() + " - " + tv + "\n");
                        } else {
                            j.setError(true);
                            j.setMessage(j.getMessage() + "\n" + "No count for " + r.getQryCode() + "\n");
                        }

                        break;
                    default:
                        j.setError(true);
                        j.setMessage(j.getMessage() + "\n" + "Wrong Query - " + r.getQryCode() + "\n");
                }
            }

        }

        String script = generateScript(queryComponent.getIndicatorQuery(), rs);
        result = evaluateScript(script);

        Long sv = CommonController.stringToLong(result);
        if (sv == null) {
            storedQueryResultController.saveValue(queryComponent, fromDate, toDate, institution, sv);
        }

        j.setMessage(j.getMessage() + "\n\n" + "Indicator Formula = " + queryComponent.getIndicatorQuery());

        j.setMessage(j.getMessage() + "\n\n" + "Formula with Values = " + script + "\n\nResult = " + result);

        message = CommonController.stringToHtml(j.getMessage());

    }

    public void runHlcQuarterly() {
        if (institution == null) {
            JsfUtil.addErrorMessage("HLC ?");
            return;
        }
        if (queryComponent == null) {
            JsfUtil.addErrorMessage("Indicator ?");
            return;
        }
        if (queryComponent.getQueryType() == null) {
            JsfUtil.addErrorMessage("Indicator Type ?");
            return;
        }
        if (!queryComponent.getQueryType().equals(QueryType.Indicator)) {
            JsfUtil.addErrorMessage("Selected is not an indicator");
            return;
        }
        if (year == 0) {
            JsfUtil.addErrorMessage("Year ?");
            return;
        }
        if (quarter == null) {
            JsfUtil.addErrorMessage("Month");
            return;
        }
        if (institution.getInstitutionType() == null) {
            JsfUtil.addErrorMessage("No Type for the institution");
            return;
        }
        if (institution.getInstitutionType() != InstitutionType.Clinic) {
            JsfUtil.addErrorMessage("Selected institution is NOT a HLC?");
            return;
        }

        fromDate = CommonController.startOfQuarter(year, quarter);
        toDate = CommonController.endOfQuarter(year, quarter);
        Jpq j = new Jpq();
        j.setMessage("");
        j.setMessage("");

        List<Replaceable> rs = findReplaceblesInIndicatorQuery(queryComponent.getIndicatorQuery());

        for (Replaceable r : rs) {

            QueryComponent temqc = queryComponentController.findLastQuery(r.getQryCode());
            if (temqc == null) {
                j.setError(true);
                j.setMessage(j.getMessage() + "\n" + "Count " + r.getQryCode() + " in the indicator is not found. ");
                continue;
            }

            if (null == temqc.getQueryType()) {
                j.setError(true);
                j.setMessage(j.getMessage() + "\n" + "Type of query " + r.getQryCode() + " in is not set. ");

            } else {
                switch (temqc.getQueryType()) {
                    case Population:
                        if (temqc.getPopulationType() == null) {
                            j.setError(true);
                            j.setMessage(j.getMessage() + "\n" + "Type of Population " + r.getQryCode() + " in is not set. ");
                            continue;
                        }
                        int temYear;
                        Calendar c = Calendar.getInstance();
                        c.setTime(fromDate);
                        temYear = c.get(Calendar.YEAR);
                        Long tp = relationshipController.findPopulationValue(temYear, institution, temqc.getPopulationType());
                        if (tp != null) {
                            r.setTextReplacing(tp + "");
                            r.setSelectedValue(tp + "");
                            j.setMessage(j.getMessage() + r.getQryCode() + " - " + tp + "\n");
                        } else {
                            j.setError(true);
                            j.setMessage(j.getMessage() + "No Population data for " + r.getQryCode() + "\n");
                        }
                        break;

                    case Client_Count:
                    case Encounter_Count:
                        Long tv = storedQueryResultController.findStoredLongValue(temqc, fromDate, toDate, institution, r);
                        if (tv != null) {
                            r.setTextReplacing(tv + "");
                            r.setSelectedValue(tv + "");
                            j.setMessage(j.getMessage() + r.getQryCode() + " - " + tv + "\n");
                        } else {
                            j.setError(true);
                            j.setMessage(j.getMessage() + "\n" + "No count for " + r.getQryCode() + "\n");
                        }

                        break;
                    default:
                        j.setError(true);
                        j.setMessage(j.getMessage() + "\n" + "Wrong Query - " + r.getQryCode() + "\n");
                }
            }

        }

        String script = generateScript(queryComponent.getIndicatorQuery(), rs);
        result = evaluateScript(script);

        Long sv = CommonController.stringToLong(result);
        if (sv == null) {
            storedQueryResultController.saveValue(queryComponent, fromDate, toDate, institution, sv);
        }

        j.setMessage(j.getMessage() + "\n\n" + "Indicator Formula = " + queryComponent.getIndicatorQuery());

        j.setMessage(j.getMessage() + "\n\n" + "Formula with Values = " + script + "\n\nResult = " + result);

        message = CommonController.stringToHtml(j.getMessage());

    }

    public void runAllInstitutionMonthly() {
        if (queryComponent == null) {
            JsfUtil.addErrorMessage("Indicator ?");
            return;
        }
        if (queryComponent.getQueryType() == null) {
            JsfUtil.addErrorMessage("Indicator Type ?");
            return;
        }
        if (!queryComponent.getQueryType().equals(QueryType.Indicator)) {
            JsfUtil.addErrorMessage("Selected is not an indicator");
            return;
        }
        if (year == 0) {
            JsfUtil.addErrorMessage("Year ?");
            return;
        }
        if (month == null) {
            JsfUtil.addErrorMessage("Month");
            return;
        }
        fromDate = CommonController.startOfTheMonth(year, month);
        toDate = CommonController.endOfTheMonth(year, month);
        Jpq j = new Jpq();
        j.setMessage("");
        j.setMessage("");

        List<Replaceable> rs = findReplaceblesInIndicatorQuery(queryComponent.getIndicatorQuery());
        List<Institution> allClinics = institutionController.findInstitutions(InstitutionType.Clinic);
        if (allClinics == null) {
            JsfUtil.addErrorMessage("Selected institution do not have HLCs under that");
            return;
        }
        for (Replaceable r : rs) {
            QueryComponent temqc = queryComponentController.findLastQuery(r.getQryCode());
            if (temqc == null) {
                j.setError(true);
                j.setMessage(j.getMessage() + "\n" + "Count " + r.getQryCode() + " in the indicator is not found. ");
                continue;
            }

            if (null == temqc.getQueryType()) {
                j.setError(true);
                j.setMessage(j.getMessage() + "\n" + "Type of query " + r.getQryCode() + " in is not set. ");

            } else {
                switch (temqc.getQueryType()) {
                    case Population:
                        if (temqc.getPopulationType() == null) {
                            j.setError(true);
                            j.setMessage(j.getMessage() + "\n" + "Type of Population " + r.getQryCode() + " in is not set. ");
                            continue;
                        }
                        int temYear;
                        Calendar c = Calendar.getInstance();
                        c.setTime(fromDate);
                        temYear = c.get(Calendar.YEAR);

                        Long tp = relationshipController.findPopulationValue(temYear, institution, temqc.getPopulationType());
                        if (tp != null) {
                            r.setTextReplacing(tp + "");
                            r.setSelectedValue(tp + "");
                            j.setMessage(j.getMessage() + r.getQryCode() + " - " + tp + "\n");
                        } else {
                            j.setError(true);
                            j.setMessage(j.getMessage() + "No Population data for " + r.getQryCode() + "\n");
                        }
                        break;

                    case Client_Count:
                    case Encounter_Count:
                        Long tv = storedQueryResultController.findStoredLongValue(temqc, fromDate, toDate, allClinics, r);
                        if (tv != null) {
                            r.setTextReplacing(tv + "");
                            r.setSelectedValue(tv + "");
                            j.setMessage(j.getMessage() + r.getQryCode() + " - " + tv + "\n");
                        } else {
                            j.setError(true);
                            j.setMessage(j.getMessage() + "\n" + "No count for " + r.getQryCode() + "\n");
                        }

                        break;
                    default:
                        j.setError(true);
                        j.setMessage(j.getMessage() + "\n" + "Wrong Query - " + r.getQryCode() + "\n");
                }
            }

        }

        String script = generateScript(queryComponent.getIndicatorQuery(), rs);
        result = evaluateScript(script);

        j.setMessage(j.getMessage() + "\n\n" + "Indicator Formula = " + queryComponent.getIndicatorQuery());

        j.setMessage(j.getMessage() + "\n\n" + "Formula with Values = " + script + "\n\nResult = " + result);

        Long sv = CommonController.stringToLong(result);
        if (sv == null) {
            storedQueryResultController.saveValue(queryComponent, fromDate, toDate, institution, sv);
        }

        message = CommonController.stringToHtml(j.getMessage());

    }

    public void runInstitutionMonthly() {
        if (institution == null) {
            JsfUtil.addErrorMessage("HLC ?");
            return;
        }
        if (queryComponent == null) {
            JsfUtil.addErrorMessage("Indicator ?");
            return;
        }
        if (queryComponent.getQueryType() == null) {
            JsfUtil.addErrorMessage("Indicator Type ?");
            return;
        }
        if (!queryComponent.getQueryType().equals(QueryType.Indicator)) {
            JsfUtil.addErrorMessage("Selected is not an indicator");
            return;
        }
        if (year == 0) {
            JsfUtil.addErrorMessage("Year ?");
            return;
        }
        if (month == null) {
            JsfUtil.addErrorMessage("Month");
            return;
        }
        if (institution.getInstitutionType() == null) {
            JsfUtil.addErrorMessage("No Type for the institution");
            return;
        }

        fromDate = CommonController.startOfTheMonth(year, month);
        toDate = CommonController.endOfTheMonth(year, month);
        Jpq j = new Jpq();
        j.setMessage("");
        j.setMessage("");

        List<Replaceable> rs = findReplaceblesInIndicatorQuery(queryComponent.getIndicatorQuery());
        List<Institution> clinicsUnderInstitute = institutionApplicationController.findChildrenInstitutions(institution, InstitutionType.Clinic);
       
        if (clinicsUnderInstitute == null) {
            JsfUtil.addErrorMessage("Selected institution do not have HLCs under that");
            return;
        }

        for (Replaceable r : rs) {

            QueryComponent temqc = queryComponentController.findLastQuery(r.getQryCode());
            if (temqc == null) {
                j.setError(true);
                j.setMessage(j.getMessage() + "\n" + "Count " + r.getQryCode() + " in the indicator is not found. ");
                continue;
            }

            if (null == temqc.getQueryType()) {
                j.setError(true);
                j.setMessage(j.getMessage() + "\n" + "Type of query " + r.getQryCode() + " in is not set. ");

            } else {
                switch (temqc.getQueryType()) {
                    case Population:
                        if (temqc.getPopulationType() == null) {
                            j.setError(true);
                            j.setMessage(j.getMessage() + "\n" + "Type of Population " + r.getQryCode() + " in is not set. ");
                            continue;
                        }
                        int temYear;
                        Calendar c = Calendar.getInstance();
                        c.setTime(fromDate);
                        temYear = c.get(Calendar.YEAR);

                        Long tp = relationshipController.findPopulationValue(temYear, institution, temqc.getPopulationType());
                        if (tp != null) {
                            r.setTextReplacing(tp + "");
                            r.setSelectedValue(tp + "");
                            j.setMessage(j.getMessage() + r.getQryCode() + " - " + tp + "\n");
                        } else {
                            j.setError(true);
                            j.setMessage(j.getMessage() + "No Population data for " + r.getQryCode() + "\n");
                        }
                        break;

                    case Client_Count:
                    case Encounter_Count:
                        Long tv = storedQueryResultController.findStoredLongValue(temqc, fromDate, toDate, clinicsUnderInstitute, r);
                        if (tv != null) {
                            r.setTextReplacing(tv + "");
                            r.setSelectedValue(tv + "");
                            j.setMessage(j.getMessage() + r.getQryCode() + " - " + tv + "\n");
                        } else {
                            j.setError(true);
                            j.setMessage(j.getMessage() + "\n" + "No count for " + r.getQryCode() + "\n");
                        }

                        break;
                    default:
                        j.setError(true);
                        j.setMessage(j.getMessage() + "\n" + "Wrong Query - " + r.getQryCode() + "\n");
                }
            }

        }

        String script = generateScript(queryComponent.getIndicatorQuery(), rs);
        result = evaluateScript(script);

        j.setMessage(j.getMessage() + "\n\n" + "Indicator Formula = " + queryComponent.getIndicatorQuery());

        j.setMessage(j.getMessage() + "\n\n" + "Formula with Values = " + script + "\n\nResult = " + result);

        message = CommonController.stringToHtml(j.getMessage());

        Long sv = CommonController.stringToLong(result);
        if (sv == null) {
            storedQueryResultController.saveValue(queryComponent, fromDate, toDate, institution, sv);
        }

        message = CommonController.stringToHtml(j.getMessage());

    }

    public void runAreaMonthly() {
        if (area == null) {
            JsfUtil.addErrorMessage("Area ?");
            return;
        }
        if (queryComponent == null) {
            JsfUtil.addErrorMessage("Indicator ?");
            return;
        }
        if (queryComponent.getQueryType() == null) {
            JsfUtil.addErrorMessage("Indicator Type ?");
            return;
        }
        if (!queryComponent.getQueryType().equals(QueryType.Indicator)) {
            JsfUtil.addErrorMessage("Selected is not an indicator");
            return;
        }
        if (year == 0) {
            JsfUtil.addErrorMessage("Year ?");
            return;
        }
        if (month == null) {
            JsfUtil.addErrorMessage("Month");
            return;
        }

        fromDate = CommonController.startOfTheMonth(year, month);
        toDate = CommonController.endOfTheMonth(year, month);
        Jpq j = new Jpq();
        j.setMessage("");
        j.setMessage("");

        List<Replaceable> rs = findReplaceblesInIndicatorQuery(queryComponent.getIndicatorQuery());
        List<Institution> clinicsOfArea = institutionController.findInstitutions(area, InstitutionType.Clinic);
        if (clinicsOfArea == null) {
            JsfUtil.addErrorMessage("Selected area do not have HLCs under that");
            return;
        }
        for (Replaceable r : rs) {
            QueryComponent temqc = queryComponentController.findLastQuery(r.getQryCode());
            if (temqc == null) {
                j.setError(true);
                j.setMessage(j.getMessage() + "\n" + "Count " + r.getQryCode() + " in the indicator is not found. ");
                continue;
            }

            if (null == temqc.getQueryType()) {
                j.setError(true);
                j.setMessage(j.getMessage() + "\n" + "Type of query " + r.getQryCode() + " in is not set. ");

            } else {
                switch (temqc.getQueryType()) {
                    case Population:
                        if (temqc.getPopulationType() == null) {
                            j.setError(true);
                            j.setMessage(j.getMessage() + "\n" + "Type of Population " + r.getQryCode() + " in is not set. ");
                            continue;
                        }
                        int temYear;
                        Calendar c = Calendar.getInstance();
                        c.setTime(fromDate);
                        temYear = c.get(Calendar.YEAR);

                        Long tp = relationshipController.findPopulationValue(temYear, area, temqc.getPopulationType());
                        if (tp != null) {
                            r.setTextReplacing(tp + "");
                            r.setSelectedValue(tp + "");
                            j.setMessage(j.getMessage() + r.getQryCode() + " - " + tp + "\n");
                        } else {
                            j.setError(true);
                            j.setMessage(j.getMessage() + "No Population data for " + r.getQryCode() + "\n");
                        }
                        break;

                    case Client_Count:
                    case Encounter_Count:
                        Long tv = storedQueryResultController.findStoredLongValue(temqc, fromDate, toDate, clinicsOfArea, r);
                        if (tv != null) {
                            r.setTextReplacing(tv + "");
                            r.setSelectedValue(tv + "");
                            j.setMessage(j.getMessage() + r.getQryCode() + " - " + tv + "\n");
                        } else {
                            j.setError(true);
                            j.setMessage(j.getMessage() + "\n" + "No count for " + r.getQryCode() + "\n");
                        }

                        break;
                    default:
                        j.setError(true);
                        j.setMessage(j.getMessage() + "\n" + "Wrong Query - " + r.getQryCode() + "\n");
                }
            }

        }

        String script = generateScript(queryComponent.getIndicatorQuery(), rs);
        result = evaluateScript(script);

        j.setMessage(j.getMessage() + "\n" + "Calculation Script = " + script + "\nResult = " + result);

        Long sv = CommonController.stringToLong(result);
        if (sv == null) {
            storedQueryResultController.saveValue(queryComponent, fromDate, toDate, area, sv);
        }

        message = CommonController.stringToHtml(j.getMessage());

    }

    public String generateScript(String calculationScript, List<Replaceable> selectables) {
        for (Replaceable s : selectables) {
            String patternStart = "#{";
            String patternEnd = "}";
            String toBeReplaced;
            toBeReplaced = patternStart + s.getFullText() + patternEnd;

            calculationScript = calculationScript.replace(toBeReplaced, s.getSelectedValue());

        }

        return calculationScript;
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
                s.setTextToBeReplaced(block);
                s.setQryCode(block);
                ss.add(s);
            }
        }

        return ss;

    }

    public StoredQueryResultController getStoredQueryResultController() {
        return storedQueryResultController;
    }

    public void setStoredQueryResultController(StoredQueryResultController storedQueryResultController) {
        this.storedQueryResultController = storedQueryResultController;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public QueryComponent getQueryComponent() {
        return queryComponent;
    }

    public void setQueryComponent(QueryComponent queryComponent) {
        this.queryComponent = queryComponent;
    }

    public ReportTimePeriod getReportTimePeriod() {
        return reportTimePeriod;
    }

    public void setReportTimePeriod(ReportTimePeriod reportTimePeriod) {
        this.reportTimePeriod = reportTimePeriod;
    }

    public TimePeriodType getTimePeriodType() {
        return timePeriodType;
    }

    public void setTimePeriodType(TimePeriodType timePeriodType) {
        this.timePeriodType = timePeriodType;
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

    public Integer getDateOfMonth() {
        return dateOfMonth;
    }

    public void setDateOfMonth(Integer dateOfMonth) {
        this.dateOfMonth = dateOfMonth;
    }

    public Quarter getQuarterEnum() {
        return quarterEnum;
    }

    public void setQuarterEnum(Quarter quarterEnum) {
        this.quarterEnum = quarterEnum;
    }

    public boolean isRecalculate() {
        return recalculate;
    }

    public void setRecalculate(boolean recalculate) {
        this.recalculate = recalculate;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public List<QueryComponent> getSelectedIndicators() {
        return selectedIndicators;
    }

    public void setSelectedIndicators(List<QueryComponent> selectedIndicators) {
        this.selectedIndicators = selectedIndicators;
    }

}
