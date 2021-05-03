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
package lk.gov.health.phsp.ejb;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import lk.gov.health.phsp.bean.CommonController;
import lk.gov.health.phsp.bean.IndicatorController;
import lk.gov.health.phsp.bean.StoredQueryResultController;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.entity.ClientEncounterComponentItem;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.entity.QueryComponent;
import lk.gov.health.phsp.entity.StoredQueryResult;
import lk.gov.health.phsp.enums.EncounterType;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.enums.QueryCriteriaMatchType;
import lk.gov.health.phsp.enums.QueryLevel;
import lk.gov.health.phsp.enums.QueryType;
import lk.gov.health.phsp.facade.ClientEncounterComponentItemFacade;
import lk.gov.health.phsp.facade.EncounterFacade;
import lk.gov.health.phsp.facade.InstitutionFacade;
import lk.gov.health.phsp.facade.QueryComponentFacade;
import lk.gov.health.phsp.facade.StoredQueryResultFacade;
import lk.gov.health.phsp.pojcs.EncounterWithComponents;
import lk.gov.health.phsp.pojcs.InstitutionYearMonthCompleted;
import lk.gov.health.phsp.pojcs.Jpq;
import lk.gov.health.phsp.pojcs.QueryWithCriteria;
import lk.gov.health.phsp.pojcs.Replaceable;

/**
 *
 * @author buddhika
 */
@Stateless
public class AnalysisBean {

    @EJB
    InstitutionFacade institutionFacade;
    @EJB
    QueryComponentFacade queryComponentFacade;
    @EJB
    ClientEncounterComponentItemFacade clientEncounterComponentItemFacade;
    @EJB
    EncounterFacade encounterFacade;
    @EJB
    StoredQueryResultFacade storedQueryResultFacade;

    private List<QueryComponent> queryComponents;
    static int year;
    static int month;
    static private List<QueryComponent> qcs;

    static List<InstitutionYearMonthCompleted> iymcs;

//    @Schedule(dayOfWeek = "Mon-Fri", month = "*", hour = "9-17", dayOfMonth = "*", year = "*", minute = "*", second = "0", persistent = false)
//    public void myTimer() {
//        System.out.println("Timer event: " + new Date());
//    }
    @Schedule(hour = "19-05", minute = "*/5", second = "0", persistent = false)
    public void startProcessingCounts() {

    }

    @Schedule(hour = "05-18", minute = "*/15", second = "30", persistent = false)
    public void endProcessingCounts() {

    }

    @Schedule(hour = "21-5", minute = "*/2", second = "0", persistent = false)
    public void processCounts() {
        System.out.println("processCounts Commented");
//        getIymcs();
//        InstitutionYearMonthCompleted next = selectNextIymcs();
//        System.out.println("next = " + next);
//
//        if (next != null) {
//            System.out.println("Next INS = " + next.getInstitution().getName());
//            runClinicCounts(next);
//        }
    }

    public List<InstitutionYearMonthCompleted> getIymcs() {
        System.out.println("getIymcs");
        if (iymcs == null) {
            Calendar c = Calendar.getInstance();
            int ti = c.get(Calendar.YEAR);
            int tm = c.get(Calendar.MONTH);
            year = ti;
            month = tm;
            iymcs = new ArrayList<>();
            for (Institution ins : findClinics()) {
                System.out.println("ins = " + ins.getName());
                InstitutionYearMonthCompleted iymc = new InstitutionYearMonthCompleted();
                iymc.setInstitution(ins);
                iymc.setYear(ti);
                iymc.setMonth(tm);
                iymcs.add(iymc);
            }
        }
        return iymcs;
    }

    public InstitutionYearMonthCompleted selectNextIymcs() {
        System.out.println("selectNextIymcs");
        InstitutionYearMonthCompleted r = null;
        boolean allCompletedForThisCycle = true;
        for (InstitutionYearMonthCompleted t : getIymcs()) {
            System.out.println("t = " + t.getInstitution().getName());
            if (t.isCompleted()) {
                allCompletedForThisCycle = false;
                return t;
            }
        }
        if (allCompletedForThisCycle) {
            if (month == 1) {
                month = 12;
                year = year - 1;
            } else {
                month--;
            }

        }
        for (InstitutionYearMonthCompleted t : getIymcs()) {
            if (r == null) {
                r = t;
            }
            t.setCompleted(false);
        }
        return r;
    }

    public void runClinicCounts(InstitutionYearMonthCompleted iymc) {
        System.out.println("Running clinic count");
        System.out.println("iymc = " + iymc);
        if (iymc == null) {
            return;
        }
        if (iymc.getInstitution() == null) {
            JsfUtil.addErrorMessage("HLC ?");
            return;
        }
        if (iymc.getInstitution().getInstitutionType() == null) {
            return;
        }
        if (iymc.getInstitution().getInstitutionType() != InstitutionType.Clinic) {
            return;
        }
        if (iymc.getYear() == 0) {
            JsfUtil.addErrorMessage("Year ?");
            return;
        }
        if (iymc.getMonth() == 0) {
            JsfUtil.addErrorMessage("Month");
            return;
        }

        Jpq j = new Jpq();
        Date fromDate = CommonController.startOfTheMonth(iymc.getYear(), iymc.getMonth());
        Date toDate = CommonController.endOfTheMonth(iymc.getYear(), iymc.getMonth());

        System.out.println("iymc.getInstitution() = " + iymc.getInstitution().getName());
        System.out.println("fromDate = " + fromDate);
        System.out.println("toDate = " + toDate);

        List<QueryWithCriteria> qs = new ArrayList<>();
        List<EncounterWithComponents> encountersWithComponents;

        List<Long> encounterIds = findEncounterIds(fromDate,
                toDate,
                iymc.getInstitution());

        encountersWithComponents = findEncountersWithComponents(encounterIds);
        if (encountersWithComponents == null) {
            j.setErrorMessage("No data for the selected institution for the period");
            JsfUtil.addErrorMessage("No data?");
            return;
        }

        for (QueryComponent queryComponent : getQcs()) {
            QueryWithCriteria qwc = new QueryWithCriteria();
            qwc.setQuery(queryComponent);
            qwc.setCriteria(findCriteriaForQueryComponent(queryComponent.getCode()));

            Long value = calculateIndividualQueryResult(encountersWithComponents, qwc);
            if (value != null) {
                saveValue(qwc.getQuery(), fromDate, toDate, iymc.getInstitution(), value);
                j.setMessage(j.getMessage() + "Result : " + value + "\n");
            } else {
                j.setMessage(j.getMessage() + "Result : No Result\n");
            }
        }

    }

    public void saveValue(QueryComponent qc, Date fromDate, Date toDate, Institution institution, Long value) {
        StoredQueryResult s;
        s = findStoredQueryResult(qc, fromDate, toDate, institution);
        if (s == null) {
            s = new StoredQueryResult();
            s.setInstitution(institution);
            s.setResultFrom(fromDate);
            s.setResultTo(toDate);
            s.setQueryComponent(qc);
            s.setLongValue(value);
            storedQueryResultFacade.create(s);
        } else {
            s.setLongValue(value);
            storedQueryResultFacade.edit(s);
        }

    }

    public List<Institution> findClinics() {
        InstitutionType type = InstitutionType.Clinic;
        List<Institution> tins;
        String j;
        Map m = new HashMap();
        j = "select i "
                + "from Institution i "
                + "where i.retired=:ret "
                + " and i.institutionType=:it "
                + " order by i.name ";
        m.put("ret", false);
        m.put("it", type);
        tins = institutionFacade.findByJpql(j, m);
        return tins;
    }

    public List<QueryComponent> getQcs() {
        if (qcs == null) {
            qcs = new ArrayList<>();
            for (QueryComponent tc : getQueryComponents()) {
                if (tc.getQueryType() == QueryType.Client_Count || tc.getQueryType() == QueryType.Client_Count) {
                    qcs.add(tc);
                }
            }
        }
        return qcs;
    }

    public List<Long> findEncounterIds(Date fromDate, Date toDate, Institution institution) {
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

    public List<EncounterWithComponents> findEncountersWithComponents(List<Long> ids) {
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

    private List<ClientEncounterComponentItem> findClientEncounterComponentItems(Long endId) {
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

    public List<QueryComponent> findCriteriaForQueryComponent(String qryCode) {
        if (qryCode == null) {
            return null;
        }
        List<QueryComponent> output = new ArrayList<>();

        for (QueryComponent qc : getQueryComponents()) {
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

    private List<QueryComponent> findQueryComponents() {
        String j = "select q from QueryComponent q "
                + " where q.retired=false "
                + " order by q.orderNo, q.name";
        Map m = new HashMap();
        return queryComponentFacade.findByJpql(j, m);

    }

    public List<QueryComponent> getQueryComponents() {
        if (queryComponents == null) {
            queryComponents = findQueryComponents();
        }
        return queryComponents;
    }

    public Long calculateIndividualQueryResult(List<EncounterWithComponents> ewcs, QueryWithCriteria qwc) {
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
                if (!thisMatchOk) {
                    suitableForInclusion = false;
                }
            }

        }

        return suitableForInclusion;
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

    public String evaluateScript(String script) {
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        try {
            return engine.eval(script) + "";
        } catch (ScriptException ex) {

            return null;
        }
    }

    public StoredQueryResult findStoredQueryResult(QueryComponent qc, Date fromDate, Date toDate, Institution institution) {
        String j;
        Map m;
        m = new HashMap();
        m.put("fd", fromDate);
        m.put("td", toDate);
        m.put("ins", institution);
        m.put("qc", qc);
        j = "select s "
                + " from StoredQueryResult s "
                + " where s.institution=:ins "
                + " and s.resultFrom=:fd "
                + " and s.resultTo=:td "
                + " and s.queryComponent=:qc "
                + " order by s.id desc";
        return storedQueryResultFacade.findFirstByJpql(j, m);
    }
}
