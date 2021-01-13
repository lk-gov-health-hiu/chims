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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.QueryComponent;
import lk.gov.health.phsp.enums.Quarter;
import lk.gov.health.phsp.enums.QueryType;
import lk.gov.health.phsp.enums.RelationshipType;
import lk.gov.health.phsp.enums.TimePeriodType;
import lk.gov.health.phsp.pojcs.Jpq;
import lk.gov.health.phsp.pojcs.NcdReportTem;
import lk.gov.health.phsp.pojcs.Replaceable;
import lk.gov.health.phsp.pojcs.ReportTimePeriod;
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

    /**
     * Creates a new instance of IndicatorController
     */
    public IndicatorController() {
    }

    public String toInstitutionMonthly() {
        message = "";
        return "/indicators/institution_monthly";
    }

    public String toIndicatorIndex() {
        userTransactionController.recordTransaction("To View Indicators");
        return "/indicators/index";
    }

    public void runSingleInstitutionalMonthly() {
        System.out.println("runSingleInstitutionalMonthly");
        if (institution == null) {
            JsfUtil.addErrorMessage("Institution ?");
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
        System.out.println("fromDate = " + fromDate);
        toDate = CommonController.endOfTheMonth(year, month);
        System.out.println("toDate = " + toDate);
        Jpq j = new Jpq();

        List<Replaceable> rs = findReplaceblesInIndicatorQuery(queryComponent.getIndicatorQuery());

        for (Replaceable r : rs) {

            QueryComponent temqc = queryComponentController.findLastQuery(r.getQryCode());
            if (temqc == null) {
                j.setError(true);
                j.setErrorMessage(j.getErrorMessage() + "\n" + "Count " + r.getQryCode() + " in the indicator is not found. ");
            }

            if (null == temqc.getQueryType()) {
                j.setError(true);
                j.setErrorMessage(j.getErrorMessage() + "\n" + "Type of query " + r.getQryCode() + " in is not set. ");

            } else {
                switch (temqc.getQueryType()) {
                    case Population:
                        System.out.println("Population");
                        if (temqc.getPopulationType() == null) {
                            j.setError(true);
                            j.setErrorMessage(j.getErrorMessage() + "\n" + "Type of Population " + r.getQryCode() + " in is not set. ");
                            continue;
                        }
                        int temYear;
                        Calendar c = Calendar.getInstance();
                        c.setTime(fromDate);
                        temYear = c.get(Calendar.YEAR);
                        Long tp = relationshipController.findPopulationValue(temYear, institution, temqc.getPopulationType());
                        System.out.println("pop is " + tp);
                        if (tp != null) {
                            r.setTextReplacing(tp + "");
                            r.setSelectedValue(tp + "");
                        } else {
                            j.setError(true);
                            j.setErrorMessage(j.getErrorMessage() + "\n" + "No Population data for " + r.getQryCode() + " in institution " + institution.getName());
                        }
                        break;

                    case Client_Count:
                    case Encounter_Count:
                        Long tv = storedQueryResultController.findStoredLongValue(temqc, fromDate, toDate, institution);
                        System.out.println("Count");
                        if (tv != null) {
                            r.setTextReplacing(tv + "");
                            r.setSelectedValue(tv + "");
                        } else {
                            j.setErrorMessage(j.getErrorMessage() + "\n" + "No data for " + r.getQryCode() + " in institution " + institution.getName());
                        }

                        break;
                    default:
                        j.setError(true);
                        j.setErrorMessage(j.getErrorMessage() + "\n" + "Type of Population " + r.getQryCode() + " in is not set. ");
                        continue;
                }

                System.out.println("getTextReplacing = " + r.getTextReplacing());
                System.out.println("getTextToBeReplaced = " + r.getTextToBeReplaced());
            }

        }

        if (j.isError()) {
            JsfUtil.addErrorMessage(j.getErrorMessage());
            message = j.getErrorMessage();
            return;
        }

        String script = generateScript(queryComponent.getIndicatorQuery(), rs);
        System.out.println("script = " + script);

        result = evaluateScript(script);
        message = j.getErrorMessage();
        
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
    
    public Jpq handleIndicatorQuery(QueryComponent qc,
            Institution ccIns,
            Date ccFrom,
            Date ccTo) {
        Jpq j = new Jpq();
        if (qc.getQueryType() != QueryType.Indicator) {
            j.setError(true);
            j.setErrorMessage("Query is not an indicator");
            return j;
        }

        List<Replaceable> replaceables = queryComponentController.findReplaceblesInIndicatorQuery(qc.getIndicatorQuery());

        for (Replaceable r : replaceables) {

            System.out.println("r.getQryCode() = " + r.getQryCode());

            QueryComponent temqc = queryComponentController.findLastQuery(r.getQryCode());

            if (temqc == null) {
                j.setError(true);
                j.setErrorMessage("Count " + r.getQryCode() + " in the indicator is not found. ");
                return new Jpq();
            }

            j = new Jpq();

            if (null == temqc.getQueryType()) {
                JsfUtil.addErrorMessage("Wrong Query. Check the names of queries");

                return j;
            } else {
                switch (temqc.getQueryType()) {
                    case Population:
//                        j = queryComponentController.createAPopulationCountQuery(temqc, ccArea, ccYear);
                        System.out.println("pop");
                        break;
                    case Client:
//                        j = queryComponentController.createClientQuery(temqc, ccArea, ccFrom, ccTo, ccYear, ccQuarter);
                        System.out.println("client");
                        break;
                    case Client_Count:
                        System.out.println("client");
                        break;
                    case Encounter_Count:
                        System.out.println("client");
                        break;
                    default:
                        JsfUtil.addErrorMessage("Wrong Query. Check the names of queries");
                        return j;
                }
            }

            r.setSelectedValue(j.getLongResult() + "");
        }
        String javaStringToEvaluate = queryComponentController.addTemplateToReport(qc.getIndicatorQuery().trim(), replaceables);
        String rs;
        rs = "Formula \t" + javaStringToEvaluate + "\n";
        String res = queryComponentController.evaluateScript(javaStringToEvaluate);
        rs += "Result : " + res;
        Double dbl = CommonController.getDoubleValue(res);

        Long lng = CommonController.getLongValue(res);

        j.setLongResult(lng);
        j.setDblResult(dbl);
        j.setSuccess(true);
        j.setSuccessMessage(rs);
        return j;
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

}
