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

import com.mysql.cj.conf.PropertyKey;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import lk.gov.health.phsp.bean.CommonController;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.entity.Client;
import lk.gov.health.phsp.entity.ClientEncounterComponentFormSet;
import lk.gov.health.phsp.entity.ClientEncounterComponentItem;
import lk.gov.health.phsp.entity.DesignComponentForm;
import lk.gov.health.phsp.entity.DesignComponentFormItem;
import lk.gov.health.phsp.entity.DesignComponentFormSet;
import lk.gov.health.phsp.entity.Encounter;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.entity.QueryComponent;
import lk.gov.health.phsp.entity.ReportCell;
import lk.gov.health.phsp.entity.ReportColumn;
import lk.gov.health.phsp.entity.ReportRow;
import lk.gov.health.phsp.entity.StoredQueryResult;
import lk.gov.health.phsp.entity.StoredRequest;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.enums.EncounterType;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.enums.QueryCriteriaMatchType;
import lk.gov.health.phsp.enums.QueryLevel;
import lk.gov.health.phsp.enums.QueryType;
import lk.gov.health.phsp.enums.SelectionDataType;
import lk.gov.health.phsp.facade.ClientEncounterComponentFormSetFacade;
import lk.gov.health.phsp.facade.ClientEncounterComponentItemFacade;
import lk.gov.health.phsp.facade.ClientFacade;
import lk.gov.health.phsp.facade.DesignComponentFormFacade;
import lk.gov.health.phsp.facade.DesignComponentFormItemFacade;
import lk.gov.health.phsp.facade.EncounterFacade;
import lk.gov.health.phsp.facade.InstitutionFacade;
import lk.gov.health.phsp.facade.QueryComponentFacade;
import lk.gov.health.phsp.facade.ReportCellFacade;
import lk.gov.health.phsp.facade.ReportColumnFacade;
import lk.gov.health.phsp.facade.ReportRowFacade;
import lk.gov.health.phsp.facade.StoredQueryResultFacade;
import lk.gov.health.phsp.facade.StoredRequestFacade;
import lk.gov.health.phsp.pojcs.ClientFirstEncounterDetailsRemainingEncounterDatesAndTypes;
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
    @EJB
    ReportColumnFacade reportColumnFacade;
    @EJB
    ReportRowFacade reportRowFacade;
    @EJB
    ReportCellFacade reportCellFacade;
    @EJB
    DesignComponentFormFacade designComponentFormFacade;
    @EJB
    DesignComponentFormItemFacade designComponentFormItemFacade;
    @EJB
    ClientEncounterComponentFormSetFacade clientEncounterComponentFormSetFacade;
    @EJB
    ClientFacade clientFacade;
    @EJB
    StoredRequestFacade storedRequestFacade;

    static List<QueryComponent> queryComponents;
    static int year;
    static int month;
    static private List<QueryComponent> qcs;

    static List<InstitutionYearMonthCompleted> iymcs;

//    @Schedule(dayOfWeek = "Mon-Fri", month = "*", hour = "9-17", dayOfMonth = "*", year = "*", minute = "*", second = "0", persistent = false)
//    public void myTimer() {
//        // //System.out.println("Timer event: " + new Date());
//    }
    @Schedule(hour = "19-05", minute = "*/5", second = "0", persistent = false)
    public void startProcessingCounts() {

    }

//    @Schedule(hour = "05-18", minute = "*/15", second = "30", persistent = false)
//    public void endProcessingCounts() {
//
//    }

//    @Schedule(hour = "21-5", minute = "*/2", second = "0", persistent = false)
    public void processCounts() {
        // //System.out.println("processCounts Commented");
//        getIymcs();
//        InstitutionYearMonthCompleted next = selectNextIymcs();
//        // //System.out.println("next = " + next);
//
//        if (next != null) {
//            // //System.out.println("Next INS = " + next.getInstitution().getName());
//            runClinicCounts(next);
//        }
    }

    @Asynchronous
    public void createFormsetDataEntriesAndSubsequentVisitDates(Institution institution,
            DesignComponentFormSet designingComponentFormSet,
            Date fromDate, Date toDate,
            WebUser createdBy) {
        String j;
        Map m = new HashMap();
        if (institution == null) {
            return;
        }
        if (designingComponentFormSet == null) {
            return;
        }
        String excelFileName = "Form_set_data_and_clinic_visits" + "_" + (new Date()) + ".xlsx";
        StoredQueryResult sqr = new StoredQueryResult();
        sqr.setCreatedAt(new Date());
        sqr.setCreater(createdBy);
        sqr.setInstitution(institution);
        sqr.setResultFrom(fromDate);
        sqr.setResultTo(toDate);
        sqr.setResultType("cell_values");
        sqr.setProcessStarted(true);
        sqr.setProcessStartedAt(new Date());
        sqr.setWebUser(createdBy);
        sqr.setName(excelFileName);
        storedQueryResultFacade.create(sqr);

        List<ReportColumn> cols = new ArrayList<>();
        int colCount = 0;

        List<ReportRow> rows = new ArrayList<>();
        int rowCount = 0;

        List<ReportCell> cells = new ArrayList<>();
        int cellCount = 0;

        ReportColumn rcSerial = new ReportColumn();
        rcSerial.setColumnNumber(colCount++);
        rcSerial.setHeader("Serial No.");
        rcSerial.setStoredQueryResult(sqr);
        reportColumnFacade.create(rcSerial);
        cols.add(rcSerial);

        ReportColumn rcPhn = new ReportColumn();
        rcPhn.setColumnNumber(colCount++);
        rcPhn.setHeader("PHN");
        rcPhn.setStoredQueryResult(sqr);
        reportColumnFacade.create(rcPhn);
        cols.add(rcPhn);

        ReportColumn rcDob = new ReportColumn();
        rcDob.setColumnNumber(colCount++);
        rcDob.setStoredQueryResult(sqr);
        rcDob.setHeader("Date of Birth");
        rcDob.setDateFormat("dd MMMM yyyy");
        reportColumnFacade.create(rcDob);
        cols.add(rcDob);

        ReportColumn rcSex = new ReportColumn();
        rcSex.setColumnNumber(colCount++);
        rcSex.setHeader("Sex");
        rcSex.setStoredQueryResult(sqr);
        reportColumnFacade.create(rcSex);
        cols.add(rcSex);

        ReportColumn rcGn = new ReportColumn();
        rcGn.setColumnNumber(colCount++);
        rcGn.setHeader("GN Area");
        rcGn.setStoredQueryResult(sqr);
        reportColumnFacade.create(rcGn);
        cols.add(rcGn);

        ReportColumn rcRegIns = new ReportColumn();
        rcRegIns.setColumnNumber(colCount++);
        rcRegIns.setHeader("Empanalled Institute");
        rcRegIns.setStoredQueryResult(sqr);
        reportColumnFacade.create(rcRegIns);
        cols.add(rcRegIns);

        ReportColumn rcRegDate = new ReportColumn();
        rcRegDate.setColumnNumber(colCount++);
        rcRegDate.setHeader("Empanalled Date");
        rcRegDate.setDateFormat("dd MMMM yyyy");
        rcRegDate.setStoredQueryResult(sqr);
        reportColumnFacade.create(rcRegDate);
        cols.add(rcRegDate);

        List<DesignComponentForm> dForms = fillFormsofTheSelectedSet(designingComponentFormSet);

        for (DesignComponentForm dForm : dForms) {
            List<DesignComponentFormItem> dItems = fillItemsOfTheForm(dForm);
            for (DesignComponentFormItem dItem : dItems) {
                if (dItem.getItem() != null && dItem.getItem().getCode() != null) {
                    ReportColumn rc = new ReportColumn();
                    rc.setColumnNumber(colCount++);
                    rc.setHeader(dItem.getItem().getName());
                    rc.setCode(dItem.getItem().getCode().trim().toLowerCase());
                    rc.setStoredQueryResult(sqr);
                    reportColumnFacade.create(rc);
                    cols.add(rc);
                }
            }
        }

        ReportColumn rcVd = new ReportColumn();
        rcVd.setColumnNumber(colCount++);
        rcVd.setHeader("Visit Dates");
        rcVd.setStoredQueryResult(sqr);
        reportColumnFacade.create(rcVd);
        cols.add(rcVd);

        ReportColumn rcVfs = new ReportColumn();
        rcVfs.setColumnNumber(colCount++);
        rcVfs.setHeader("Visit Form Set");
        rcVfs.setStoredQueryResult(sqr);
        reportColumnFacade.create(rcVfs);
        cols.add(rcVfs);

        ReportRow insRow = new ReportRow();
        insRow.setRowNumber(rowCount++);
        insRow.setStoredQueryResult(sqr);
        reportRowFacade.create(insRow);

        ReportRow fromRow = new ReportRow();
        fromRow.setRowNumber(rowCount++);
        fromRow.setStoredQueryResult(sqr);
        reportRowFacade.create(fromRow);

        ReportRow toRow = new ReportRow();
        toRow.setRowNumber(rowCount++);
        toRow.setStoredQueryResult(sqr);
        reportRowFacade.create(toRow);

        ReportRow titleRow = new ReportRow();
        titleRow.setRowNumber(rowCount++);
        titleRow.setStoredQueryResult(sqr);
        reportRowFacade.create(titleRow);

        ReportCell cellIns = new ReportCell();
        cellIns.setColumn(rcPhn);
        cellIns.setRow(insRow);
        cellIns.setContainsStringValue(true);
        cellIns.setStringValue(institution.getName());
        cellIns.setStoredQueryResult(sqr);
        reportCellFacade.create(cellIns);

        ReportCell cellFrom = new ReportCell();
        cellFrom.setColumn(rcPhn);
        cellFrom.setRow(fromRow);
        cellFrom.setContainsDateValue(true);
        cellFrom.setDateValue(fromDate);
        cellFrom.setStoredQueryResult(sqr);
        reportCellFacade.create(cellFrom);

        ReportCell cellTo = new ReportCell();
        cellTo.setColumn(rcPhn);
        cellTo.setRow(toRow);
        cellTo.setContainsDateValue(true);
        cellTo.setDateValue(toDate);
        cellTo.setStoredQueryResult(sqr);
        reportCellFacade.create(cellTo);

        for (ReportColumn rc : cols) {
            ReportCell cell = new ReportCell();
            cell.setColumn(rc);
            cell.setRow(titleRow);
            cell.setContainsStringValue(true);
            cell.setStringValue(rc.getHeader());
            cell.setStoredQueryResult(sqr);
            reportCellFacade.create(cell);
            cells.add(cell);
        }

        rows.add(insRow);
        rows.add(fromRow);
        rows.add(toRow);
        rows.add(titleRow);

        j = "select s "
                + " from ClientEncounterComponentFormSet s join s.encounter e "
                + " where e.retired=false "
                + " and s.retired=false "
                + " and e.encounterDate between :fd and :td "
                + " and e.institution=:ins "
                + " and (s.referenceComponent=:rfs or s.referenceComponent.referenceComponent=:rfs) "
                + " order by s.id";
        m = new HashMap();
        m.put("ins", institution);
        m.put("fd", fromDate);
        m.put("td", toDate);
        m.put("rfs", designingComponentFormSet);
        List<ClientEncounterComponentFormSet> cSets = clientEncounterComponentFormSetFacade.findByJpql(j, m);

        Map<Long, ClientFirstEncounterDetailsRemainingEncounterDatesAndTypes> mapCes = new HashMap<>();

        for (ClientEncounterComponentFormSet cs : cSets) {
            if (cs.getEncounter() == null || cs.getEncounter().getClient() == null) {
                continue;
            }
            ClientFirstEncounterDetailsRemainingEncounterDatesAndTypes ce = mapCes.get(cs.getEncounter().getClient().getId());
            if (ce == null) {
                ce = new ClientFirstEncounterDetailsRemainingEncounterDatesAndTypes();
                ce.setClient(cs.getEncounter().getClient());
                ce.setFirstEncounter(cs.getEncounter());
                mapCes.put(cs.getEncounter().getClient().getId(), ce);
            } else {
                ce.getRemainigEncounters().put(cs.getEncounter().getId(), cs.getEncounter());
            }
        }

        for (ClientFirstEncounterDetailsRemainingEncounterDatesAndTypes ce : mapCes.values()) {
            Client c = ce.getClient();
            ReportRow clientRow = new ReportRow();
            clientRow.setRowNumber(rowCount++);
            clientRow.setStoredQueryResult(sqr);
            reportRowFacade.create(clientRow);

            ReportCell serialCell = new ReportCell();
            serialCell.setColumn(rcSerial);
            serialCell.setRow(clientRow);
            serialCell.setContainsLongValue(true);
            serialCell.setStoredQueryResult(sqr);
            reportCellFacade.create(serialCell);
            cells.add(serialCell);

            ReportCell phnCell = new ReportCell();
            phnCell.setColumn(rcPhn);
            phnCell.setRow(clientRow);
            phnCell.setContainsStringValue(true);
            phnCell.setStringValue(c.getPhn());
            phnCell.setStoredQueryResult(sqr);
            reportCellFacade.create(phnCell);
            cells.add(phnCell);

            ReportCell dobCell = new ReportCell();
            dobCell.setColumn(rcDob);
            dobCell.setRow(clientRow);
            dobCell.setContainsDateValue(true);
            dobCell.setDateValue(c.getPerson().getDateOfBirth());
            dobCell.setStoredQueryResult(sqr);
            reportCellFacade.create(dobCell);
            cells.add(dobCell);

            ReportCell sexCell = new ReportCell();
            sexCell.setColumn(rcSex);
            sexCell.setRow(clientRow);
            sexCell.setContainsStringValue(true);
            if (c.getPerson().getSex() != null) {
                sexCell.setStringValue(c.getPerson().getSex().getName());
            }
            sexCell.setStoredQueryResult(sqr);
            reportCellFacade.create(sexCell);
            cells.add(sexCell);

            ReportCell regInsCell = new ReportCell();
            regInsCell.setColumn(rcRegIns);
            regInsCell.setRow(clientRow);
            regInsCell.setContainsStringValue(true);
            if (c.getCreateInstitution() != null) {
                regInsCell.setStringValue(c.getCreateInstitution().getName());
            }
            regInsCell.setStoredQueryResult(sqr);
            reportCellFacade.create(regInsCell);
            cells.add(regInsCell);

            ReportCell regDateCell = new ReportCell();
            regDateCell.setColumn(rcRegDate);
            regDateCell.setRow(clientRow);
            regDateCell.setContainsDateValue(true);
            regDateCell.setDateValue(c.getCreatedOn());
            regDateCell.setStoredQueryResult(sqr);
            reportCellFacade.create(regDateCell);
            cells.add(regDateCell);

            ReportCell gnCell = new ReportCell();
            gnCell.setColumn(rcGn);
            gnCell.setRow(clientRow);
            gnCell.setContainsStringValue(true);
            if (c.getPerson().getGnArea() != null) {
                gnCell.setStringValue(c.getPerson().getGnArea().getName());
            } else {
                gnCell.setStringValue("Not set");
            }
            gnCell.setStoredQueryResult(sqr);
            reportCellFacade.create(gnCell);
            cells.add(gnCell);

            for (ReportColumn rc : cols) {
                //System.out.println("rc = " + rc);
                if (rc == null) {
                    continue;
                }
                if (rc.getCode() == null) {
                    continue;
                }
                if (rc.getCode().equals("")) {
                    continue;
                }
                if (rc.getCode() != null || !rc.getCode().trim().equals("")) {

                    if (ce == null) {
                        continue;
                    }
                    if (ce.getFirstEncounter() == null) {
                        continue;
                    }
                    if (ce.getFirstEncounter().getClientEncounterComponentItems() == null) {
                        continue;
                    }

                    for (ClientEncounterComponentItem cItem : ce.getFirstEncounter().getClientEncounterComponentItems()) {

                        if (cItem.getItem() == null || cItem.getItem().getCode() == null) {
                            continue;
                        }
                        if (rc.getCode().equalsIgnoreCase(cItem.getItem().getCode())) {
                            ReportCell ciCell = new ReportCell();
                            ciCell.setColumn(rc);
                            ciCell.setRow(clientRow);

                            SelectionDataType sdt = cItem.getReferanceDesignComponentFormItem().getSelectionDataType();
                            switch (sdt) {
                                case Boolean:
                                    ciCell.setContainsStringValue(true);
                                    if (cItem.getBooleanValue() == null) {
                                        ciCell.setStringValue("");
                                    } else if (cItem.getBooleanValue()) {
                                        ciCell.setStringValue("true");
                                    } else {
                                        ciCell.setStringValue("false");
                                    }
                                    break;
                                case DateTime:
                                    ciCell.setContainsDateValue(true);
                                    if (cItem.getDateValue() == null) {
                                        ciCell.setDateValue(null);
                                    } else {
                                        ciCell.setDateValue(cItem.getDateValue());
                                    }
                                    break;
                                case Integer_Number:
                                    ciCell.setContainsDoubleValue(true);
                                    if (cItem.getRealNumberValue() == null) {
                                        ciCell.setDblValue(null);
                                    } else {
                                        ciCell.setDblValue(cItem.getRealNumberValue());
                                    }
                                    break;
                                case Long_Number:
                                    ciCell.setContainsDoubleValue(true);
                                    if (cItem.getLongNumberValue() == null) {
                                        ciCell.setDblValue(null);
                                    } else {
                                        ciCell.setDblValue(cItem.getLongNumberValue().doubleValue());
                                    }
                                    break;
                                case Item_Reference:
                                    ciCell.setContainsStringValue(true);
                                    if (cItem.getItemValue() == null) {
                                        ciCell.setStringValue(null);
                                    } else {
                                        ciCell.setStringValue(cItem.getItemValue().getName());
                                    }
                                    break;
                                case Long_Text:
                                    ciCell.setContainsStringValue(true);
                                    if (cItem.getLongTextValue() == null) {
                                        ciCell.setStringValue(null);
                                    } else {
                                        ciCell.setStringValue(cItem.getLongTextValue());
                                    }
                                    break;
                                case Real_Number:
                                    ciCell.setContainsDoubleValue(true);
                                    if (cItem.getRealNumberValue() == null) {
                                        ciCell.setDblValue(null);
                                    } else {
                                        ciCell.setDblValue(cItem.getRealNumberValue());
                                    }
                                    break;
                                case Short_Text:
                                    ciCell.setContainsStringValue(true);
                                    if (cItem.getShortTextValue() == null) {
                                        ciCell.setStringValue(null);
                                    } else {
                                        ciCell.setStringValue(cItem.getShortTextValue());
                                    }
                                    break;
                                default:
                                    ciCell.setContainsStringValue(true);
                                    ciCell.setStringValue("Under Construction");
                            }
                            ciCell.setStoredQueryResult(sqr);
                            reportCellFacade.create(ciCell);
                            cells.add(ciCell);
                        }
                    }
                }
            }

            String dates = "";
            String visitType = "";


            for (Encounter e : ce.getRemainigEncounters().values()) {
                dates += CommonController.dateTimeToString(e.getEncounterDate()) + "\n";
            }

            ReportCell vdCell = new ReportCell();
            vdCell.setColumn(rcVd);
            vdCell.setRow(clientRow);
            vdCell.setContainsStringValue(true);
            if (dates.equals("")) {
                gnCell.setStringValue(dates);
            } else {
                gnCell.setStringValue("No more visits");
            }
            vdCell.setStoredQueryResult(sqr);
            reportCellFacade.create(vdCell);
            cells.add(vdCell);

            clientRow.setStoredQueryResult(sqr);
            reportRowFacade.create(clientRow);
            rows.add(clientRow);
        }

        sqr.setProcessCompleted(true);
        sqr.setProcessCompletedAt(new Date());
        storedQueryResultFacade.edit(sqr);

    }

    @Asynchronous
    public void createLongitudinalVisitDates(Institution institution,
            Date fromDate, Date toDate,
            WebUser createdBy) {
        String j;
        Map m = new HashMap();
        if (institution == null) {
            return;
        }
        String excelFileName = "Longitidunal_clinic_visits" + "_" + (new Date()) + ".xlsx";
        StoredQueryResult sqr = new StoredQueryResult();
        sqr.setCreatedAt(new Date());
        sqr.setCreater(createdBy);
        sqr.setInstitution(institution);
        sqr.setResultFrom(fromDate);
        sqr.setResultTo(toDate);
        sqr.setResultType("cell_values");
        sqr.setProcessStarted(true);
        sqr.setProcessStartedAt(new Date());
        sqr.setWebUser(createdBy);
        sqr.setName(excelFileName);
        storedQueryResultFacade.create(sqr);

        List<ReportColumn> cols = new ArrayList<>();
        int colCount = 0;

        List<ReportRow> rows = new ArrayList<>();
        int rowCount = 0;

        List<ReportCell> cells = new ArrayList<>();
        int cellCount = 0;

        ReportColumn rcSerial = new ReportColumn();
        rcSerial.setColumnNumber(colCount++);
        rcSerial.setHeader("Serial No.");
        rcSerial.setStoredQueryResult(sqr);
        reportColumnFacade.create(rcSerial);
        cols.add(rcSerial);

        ReportColumn rcPhn = new ReportColumn();
        rcPhn.setColumnNumber(colCount++);
        rcPhn.setHeader("PHN");
        rcPhn.setStoredQueryResult(sqr);
        reportColumnFacade.create(rcPhn);
        cols.add(rcPhn);

        ReportColumn rcDob = new ReportColumn();
        rcDob.setColumnNumber(colCount++);
        rcDob.setStoredQueryResult(sqr);
        rcDob.setHeader("Age");
        rcDob.setDateFormat("dd MMMM yyyy");
        reportColumnFacade.create(rcDob);
        cols.add(rcDob);

        ReportColumn rcSex = new ReportColumn();
        rcSex.setColumnNumber(colCount++);
        rcSex.setHeader("Sex");
        rcSex.setStoredQueryResult(sqr);
        reportColumnFacade.create(rcSex);
        cols.add(rcSex);

        ReportColumn rcGn = new ReportColumn();
        rcGn.setColumnNumber(colCount++);
        rcGn.setHeader("GN Area");
        rcGn.setStoredQueryResult(sqr);
        reportColumnFacade.create(rcGn);
        cols.add(rcGn);

        ReportColumn rcRegIns = new ReportColumn();
        rcRegIns.setColumnNumber(colCount++);
        rcRegIns.setHeader("Empanalled Institute");
        rcRegIns.setStoredQueryResult(sqr);
        reportColumnFacade.create(rcRegIns);
        cols.add(rcRegIns);

        ReportColumn rcRegDate = new ReportColumn();
        rcRegDate.setColumnNumber(colCount++);
        rcRegDate.setHeader("Empanalled Date");
        rcRegDate.setDateFormat("dd MMMM yyyy");
        rcRegDate.setStoredQueryResult(sqr);
        reportColumnFacade.create(rcRegDate);
        cols.add(rcRegDate);

        ReportColumn rcVd1 = new ReportColumn();
        rcVd1.setColumnNumber(colCount++);
        rcVd1.setHeader("Visit Date 1");
        rcVd1.setStoredQueryResult(sqr);
        reportColumnFacade.create(rcVd1);
        cols.add(rcVd1);

        ReportColumn rcVd2 = new ReportColumn();
        rcVd2.setColumnNumber(colCount++);
        rcVd2.setHeader("Visit Date 2");
        rcVd2.setStoredQueryResult(sqr);
        reportColumnFacade.create(rcVd2);
        cols.add(rcVd2);

        ReportColumn rcVd3 = new ReportColumn();
        rcVd3.setColumnNumber(colCount++);
        rcVd3.setHeader("Visit Date 3");
        rcVd3.setStoredQueryResult(sqr);
        reportColumnFacade.create(rcVd3);
        cols.add(rcVd3);

        ReportColumn rcVd4 = new ReportColumn();
        rcVd4.setColumnNumber(colCount++);
        rcVd4.setHeader("Visit Date 4");
        rcVd4.setStoredQueryResult(sqr);
        reportColumnFacade.create(rcVd4);
        cols.add(rcVd4);

        ReportColumn rcVd5 = new ReportColumn();
        rcVd5.setColumnNumber(colCount++);
        rcVd5.setHeader("Visit Date 5");
        rcVd5.setStoredQueryResult(sqr);
        reportColumnFacade.create(rcVd5);
        cols.add(rcVd5);

        ReportRow insRow = new ReportRow();
        insRow.setRowNumber(rowCount++);
        insRow.setStoredQueryResult(sqr);
        reportRowFacade.create(insRow);

        ReportRow fromRow = new ReportRow();
        fromRow.setRowNumber(rowCount++);
        fromRow.setStoredQueryResult(sqr);
        reportRowFacade.create(fromRow);

        ReportRow toRow = new ReportRow();
        toRow.setRowNumber(rowCount++);
        toRow.setStoredQueryResult(sqr);
        reportRowFacade.create(toRow);

        ReportRow titleRow = new ReportRow();
        titleRow.setRowNumber(rowCount++);
        titleRow.setStoredQueryResult(sqr);
        reportRowFacade.create(titleRow);

        ReportCell cellIns = new ReportCell();
        cellIns.setColumn(rcPhn);
        cellIns.setRow(insRow);
        cellIns.setContainsStringValue(true);
        cellIns.setStringValue(institution.getName());
        cellIns.setStoredQueryResult(sqr);
        reportCellFacade.create(cellIns);

        ReportCell cellFrom = new ReportCell();
        cellFrom.setColumn(rcPhn);
        cellFrom.setRow(fromRow);
        cellFrom.setContainsDateValue(true);
        cellFrom.setDateValue(fromDate);
        cellFrom.setStoredQueryResult(sqr);
        reportCellFacade.create(cellFrom);

        ReportCell cellTo = new ReportCell();
        cellTo.setColumn(rcPhn);
        cellTo.setRow(toRow);
        cellTo.setContainsDateValue(true);
        cellTo.setDateValue(toDate);
        cellTo.setStoredQueryResult(sqr);
        reportCellFacade.create(cellTo);

        rows.add(insRow);
        rows.add(fromRow);
        rows.add(toRow);
        rows.add(titleRow);

        for (ReportColumn rc : cols) {
            ReportCell cell = new ReportCell();
            cell.setColumn(rc);
            cell.setRow(titleRow);
            cell.setContainsStringValue(true);
            cell.setStringValue(rc.getHeader());
            cell.setStoredQueryResult(sqr);
            reportCellFacade.create(cell);
            cells.add(cell);
        }

        j = "select e "
                + " from Encounter e "
                + " where e.retired=false "
                + " and e.encounterDate between :fd and :td "
                + " and e.institution=:ins "
                + " order by e.id";
        m = new HashMap();
        m.put("ins", institution);
        m.put("fd", fromDate);
        m.put("td", toDate);
        List<Encounter> cSets = clientEncounterComponentFormSetFacade.findByJpql(j, m);

        Map<Long, ClientFirstEncounterDetailsRemainingEncounterDatesAndTypes> mapCes = new HashMap<>();

        for (Encounter cs : cSets) {
            ClientFirstEncounterDetailsRemainingEncounterDatesAndTypes ce = mapCes.get(cs.getClient().getId());
            if (ce == null) {
                ce = new ClientFirstEncounterDetailsRemainingEncounterDatesAndTypes();
                ce.setClient(cs.getClient());
                ce.setFirstEncounter(cs);
                ce.getRemainigEncounters().put(cs.getId(), cs);
                mapCes.put(cs.getClient().getId(), ce);
            } else {
                ce.getRemainigEncounters().put(cs.getId(), cs);
            }
        }

        for (ClientFirstEncounterDetailsRemainingEncounterDatesAndTypes ce : mapCes.values()) {
            Client c = ce.getClient();
            ReportRow clientRow = new ReportRow();
            clientRow.setRowNumber(rowCount++);
            clientRow.setStoredQueryResult(sqr);
            reportRowFacade.create(clientRow);

            ReportCell serialCell = new ReportCell();
            serialCell.setColumn(rcSerial);
            serialCell.setRow(clientRow);
            serialCell.setContainsLongValue(true);
            serialCell.setStoredQueryResult(sqr);
            reportCellFacade.create(serialCell);
            cells.add(serialCell);

            ReportCell phnCell = new ReportCell();
            phnCell.setColumn(rcPhn);
            phnCell.setRow(clientRow);
            phnCell.setContainsStringValue(true);
            phnCell.setStringValue(c.getPhn());
            phnCell.setStoredQueryResult(sqr);
            reportCellFacade.create(phnCell);
            cells.add(phnCell);

            ReportCell dobCell = new ReportCell();
            dobCell.setColumn(rcDob);
            dobCell.setRow(clientRow);
            dobCell.setContainsDoubleValue(true);
            Integer ageInYears = CommonController.calculateAge(c.getPerson().getDateOfBirth(), c.getCreatedOn());
            dobCell.setDblValue(ageInYears.doubleValue());
            dobCell.setStoredQueryResult(sqr);
            reportCellFacade.create(dobCell);
            cells.add(dobCell);

            ReportCell sexCell = new ReportCell();
            sexCell.setColumn(rcSex);
            sexCell.setRow(clientRow);
            sexCell.setContainsStringValue(true);
            if (c.getPerson().getSex() != null) {
                sexCell.setStringValue(c.getPerson().getSex().getName());
            }
            sexCell.setStoredQueryResult(sqr);
            reportCellFacade.create(sexCell);
            cells.add(sexCell);

            ReportCell regInsCell = new ReportCell();
            regInsCell.setColumn(rcRegIns);
            regInsCell.setRow(clientRow);
            regInsCell.setContainsStringValue(true);
            if (c.getCreateInstitution() != null) {
                regInsCell.setStringValue(c.getCreateInstitution().getName());
            }
            regInsCell.setStoredQueryResult(sqr);
            reportCellFacade.create(regInsCell);
            cells.add(regInsCell);

            ReportCell regDateCell = new ReportCell();
            regDateCell.setColumn(rcRegDate);
            regDateCell.setRow(clientRow);
            regDateCell.setContainsDateValue(true);
            regDateCell.setDateValue(c.getCreatedOn());
            regDateCell.setStoredQueryResult(sqr);
            reportCellFacade.create(regDateCell);
            cells.add(regDateCell);

            ReportCell gnCell = new ReportCell();
            gnCell.setColumn(rcGn);
            gnCell.setRow(clientRow);
            gnCell.setContainsStringValue(true);
            if (c.getPerson().getGnArea() != null) {
                gnCell.setStringValue(c.getPerson().getGnArea().getName());
            } else {
                gnCell.setStringValue("Not set");
            }
            gnCell.setStoredQueryResult(sqr);
            reportCellFacade.create(gnCell);
            cells.add(gnCell);

            String dates = "";
            String visitType = "";


            int encounterNo = 1;
            for (Encounter e : ce.getRemainigEncounters().values()) {
                ReportCell vdCell = new ReportCell();
                vdCell.setRow(clientRow);
                vdCell.setContainsDateValue(true);
                vdCell.setDateValue(e.getEncounterDate());
                switch (encounterNo) {
                    case 1:
                        vdCell.setColumn(rcVd1);
                        break;
                    case 2:
                        vdCell.setColumn(rcVd2);
                        break;
                    case 3:
                        vdCell.setColumn(rcVd3);
                        break;
                    case 4:
                        vdCell.setColumn(rcVd4);
                        break;
                    default:
                        vdCell.setColumn(rcVd1);
                        dates += CommonController.dateTimeToString(e.getEncounterDate()) + "\n";
                        break;
                }
                vdCell.setStoredQueryResult(sqr);
                reportCellFacade.create(vdCell);
                cells.add(vdCell);
                encounterNo++;
            }

            ReportCell vdCell = new ReportCell();
            vdCell.setColumn(rcVd5);
            vdCell.setRow(clientRow);
            vdCell.setContainsStringValue(true);
            if (dates.equals("")) {
                vdCell.setStringValue(dates);
            }

            vdCell.setStoredQueryResult(sqr);
            reportCellFacade.create(vdCell);
            cells.add(vdCell);

            clientRow.setStoredQueryResult(sqr);
            reportRowFacade.create(clientRow);
            rows.add(clientRow);
        }

        sqr.setProcessCompleted(true);
        sqr.setProcessCompletedAt(new Date());
        storedQueryResultFacade.edit(sqr);

    }

    @Asynchronous
    public void createAllClientsAndAllClinicVisits(Institution institution,
            WebUser createdBy) {
        String j;
        Map m = new HashMap();
        if (institution == null) {
            return;
        }
        String excelFileName = "All_clients_and_all_clinic_visits_" + institution + "_taken_on_" + (new Date()) + ".xlsx";
        StoredQueryResult sqr = new StoredQueryResult();
        sqr.setCreatedAt(new Date());
        sqr.setCreater(createdBy);
        sqr.setInstitution(institution);
        sqr.setResultType("cell_values");
        sqr.setProcessStarted(true);
        sqr.setProcessStartedAt(new Date());
        sqr.setWebUser(createdBy);
        sqr.setName(excelFileName);
        storedQueryResultFacade.create(sqr);

        List<ReportColumn> cols = new ArrayList<>();
        int colCount = 0;

        List<ReportRow> rows = new ArrayList<>();
        int rowCount = 0;

        List<ReportCell> cells = new ArrayList<>();
        int cellCount = 0;

        ReportColumn rcSerial = new ReportColumn();
        rcSerial.setColumnNumber(colCount++);
        rcSerial.setHeader("Serial No.");
        rcSerial.setStoredQueryResult(sqr);
        reportColumnFacade.create(rcSerial);
        cols.add(rcSerial);

        ReportColumn rcPhn = new ReportColumn();
        rcPhn.setColumnNumber(colCount++);
        rcPhn.setHeader("PHN");
        rcPhn.setStoredQueryResult(sqr);
        reportColumnFacade.create(rcPhn);
        cols.add(rcPhn);

        ReportColumn rcDob = new ReportColumn();
        rcDob.setColumnNumber(colCount++);
        rcDob.setStoredQueryResult(sqr);
        rcDob.setHeader("Age");
        rcDob.setDateFormat("dd MMMM yyyy");
        reportColumnFacade.create(rcDob);
        cols.add(rcDob);

        ReportColumn rcSex = new ReportColumn();
        rcSex.setColumnNumber(colCount++);
        rcSex.setHeader("Sex");
        rcSex.setStoredQueryResult(sqr);
        reportColumnFacade.create(rcSex);
        cols.add(rcSex);

        ReportColumn rcGn = new ReportColumn();
        rcGn.setColumnNumber(colCount++);
        rcGn.setHeader("GN Area");
        rcGn.setStoredQueryResult(sqr);
        reportColumnFacade.create(rcGn);
        cols.add(rcGn);

        ReportColumn rcRegIns = new ReportColumn();
        rcRegIns.setColumnNumber(colCount++);
        rcRegIns.setHeader("Empanalled Institute");
        rcRegIns.setStoredQueryResult(sqr);
        reportColumnFacade.create(rcRegIns);
        cols.add(rcRegIns);

        ReportColumn rcRegDate = new ReportColumn();
        rcRegDate.setColumnNumber(colCount++);
        rcRegDate.setHeader("Empanalled Date");
        rcRegDate.setDateFormat("dd MMMM yyyy");
        rcRegDate.setStoredQueryResult(sqr);
        reportColumnFacade.create(rcRegDate);
        cols.add(rcRegDate);

        ReportColumn rcVd1 = new ReportColumn();
        rcVd1.setColumnNumber(colCount++);
        rcVd1.setHeader("Visit Date 1");
        rcVd1.setStoredQueryResult(sqr);
        reportColumnFacade.create(rcVd1);
        cols.add(rcVd1);

        ReportColumn rcVd2 = new ReportColumn();
        rcVd2.setColumnNumber(colCount++);
        rcVd2.setHeader("Visit Date 2");
        rcVd2.setStoredQueryResult(sqr);
        reportColumnFacade.create(rcVd2);
        cols.add(rcVd2);

        ReportColumn rcVd3 = new ReportColumn();
        rcVd3.setColumnNumber(colCount++);
        rcVd3.setHeader("Visit Date 3");
        rcVd3.setStoredQueryResult(sqr);
        reportColumnFacade.create(rcVd3);
        cols.add(rcVd3);

        ReportColumn rcVd4 = new ReportColumn();
        rcVd4.setColumnNumber(colCount++);
        rcVd4.setHeader("Visit Date 4");
        rcVd4.setStoredQueryResult(sqr);
        reportColumnFacade.create(rcVd4);
        cols.add(rcVd4);

        ReportColumn rcVd5 = new ReportColumn();
        rcVd5.setColumnNumber(colCount++);
        rcVd5.setHeader("Visit Date 5");
        rcVd5.setStoredQueryResult(sqr);
        reportColumnFacade.create(rcVd5);
        cols.add(rcVd5);

        ReportRow insRow = new ReportRow();
        insRow.setRowNumber(rowCount++);
        insRow.setStoredQueryResult(sqr);
        reportRowFacade.create(insRow);

        rowCount++;

        ReportRow titleRow = new ReportRow();
        titleRow.setRowNumber(rowCount++);
        titleRow.setStoredQueryResult(sqr);
        reportRowFacade.create(titleRow);

        ReportCell cellIns = new ReportCell();
        cellIns.setColumn(rcPhn);
        cellIns.setRow(insRow);
        cellIns.setContainsStringValue(true);
        cellIns.setStringValue(institution.getName());
        cellIns.setStoredQueryResult(sqr);
        reportCellFacade.create(cellIns);

        ReportCell cellInsLabel = new ReportCell();
        cellInsLabel.setColumn(rcDob);
        cellInsLabel.setRow(insRow);
        cellInsLabel.setContainsDateValue(true);
        cellInsLabel.setStringValue("Institution");
        cellInsLabel.setStoredQueryResult(sqr);
        reportCellFacade.create(cellInsLabel);

        rows.add(insRow);
        rows.add(titleRow);

        for (ReportColumn rc : cols) {
            ReportCell cell = new ReportCell();
            cell.setColumn(rc);
            cell.setRow(titleRow);
            cell.setContainsStringValue(true);
            cell.setStringValue(rc.getHeader());
            cell.setStoredQueryResult(sqr);
            reportCellFacade.create(cell);
            cells.add(cell);
        }

        j = "select c "
                + " from Client c "
                + " where c.retired=false "
                + " and (c.createInstitution=:ins or c.createInstitution.parent=:ins or c.createInstitution=:pins or c.createInstitution.parent=:pins) "
                + " order by c.id";
        m = new HashMap();
        m.put("ins", institution);
        if (institution.getParent() != null) {
            m.put("pins", institution.getParent());
        } else {
            m.put("pins", institution);
        }
        Map<Long, ClientFirstEncounterDetailsRemainingEncounterDatesAndTypes> mapCes = new HashMap<>();

        List<Client> clients = clientFacade.findByJpql(j, m);

        for (Client c : clients) {
            ClientFirstEncounterDetailsRemainingEncounterDatesAndTypes ce = mapCes.get(c.getId());
            if (ce == null) {
                ce = new ClientFirstEncounterDetailsRemainingEncounterDatesAndTypes();
                ce.setClient(c);
                mapCes.put(c.getId(), ce);
            }
        }

        j = "select e "
                + " from Encounter e "
                + " where e.retired=false "
                + " and e.institution=:ins "
                + " and e.encounterType=:et "
                + " order by e.id";
        m = new HashMap();
        m.put("ins", institution);
        m.put("et", EncounterType.Clinic_Visit);
        List<Encounter> cSets = clientEncounterComponentFormSetFacade.findByJpql(j, m);

        for (Encounter cs : cSets) {
            ClientFirstEncounterDetailsRemainingEncounterDatesAndTypes ce = mapCes.get(cs.getClient().getId());
            if (ce == null) {
                ce = new ClientFirstEncounterDetailsRemainingEncounterDatesAndTypes();
                ce.setClient(cs.getClient());
                ce.setFirstEncounter(cs);
                ce.getRemainigEncounters().put(cs.getId(), cs);
                mapCes.put(cs.getClient().getId(), ce);
            } else {
                ce.getRemainigEncounters().put(cs.getId(), cs);
            }
        }

        for (ClientFirstEncounterDetailsRemainingEncounterDatesAndTypes ce : mapCes.values()) {
            Client c = ce.getClient();
            ReportRow clientRow = new ReportRow();
            clientRow.setRowNumber(rowCount++);
            clientRow.setStoredQueryResult(sqr);
            reportRowFacade.create(clientRow);

            ReportCell serialCell = new ReportCell();
            serialCell.setColumn(rcSerial);
            serialCell.setRow(clientRow);
            serialCell.setContainsLongValue(true);
            serialCell.setStoredQueryResult(sqr);
            reportCellFacade.create(serialCell);
            cells.add(serialCell);

            ReportCell phnCell = new ReportCell();
            phnCell.setColumn(rcPhn);
            phnCell.setRow(clientRow);
            phnCell.setContainsStringValue(true);
            phnCell.setStringValue(c.getPhn());
            phnCell.setStoredQueryResult(sqr);
            reportCellFacade.create(phnCell);
            cells.add(phnCell);

            ReportCell dobCell = new ReportCell();
            dobCell.setColumn(rcDob);
            dobCell.setRow(clientRow);
            dobCell.setContainsDoubleValue(true);
            Integer ageInYears = CommonController.calculateAge(c.getPerson().getDateOfBirth(), c.getCreatedOn());
            dobCell.setDblValue(ageInYears.doubleValue());
            dobCell.setStoredQueryResult(sqr);
            reportCellFacade.create(dobCell);
            cells.add(dobCell);

            ReportCell sexCell = new ReportCell();
            sexCell.setColumn(rcSex);
            sexCell.setRow(clientRow);
            sexCell.setContainsStringValue(true);
            if (c.getPerson().getSex() != null) {
                sexCell.setStringValue(c.getPerson().getSex().getName());
            }
            sexCell.setStoredQueryResult(sqr);
            reportCellFacade.create(sexCell);
            cells.add(sexCell);

            ReportCell regInsCell = new ReportCell();
            regInsCell.setColumn(rcRegIns);
            regInsCell.setRow(clientRow);
            regInsCell.setContainsStringValue(true);
            if (c.getCreateInstitution() != null) {
                regInsCell.setStringValue(c.getCreateInstitution().getName());
            }
            regInsCell.setStoredQueryResult(sqr);
            reportCellFacade.create(regInsCell);
            cells.add(regInsCell);

            ReportCell regDateCell = new ReportCell();
            regDateCell.setColumn(rcRegDate);
            regDateCell.setRow(clientRow);
            regDateCell.setContainsDateValue(true);
            regDateCell.setDateValue(c.getCreatedAt());
            regDateCell.setStoredQueryResult(sqr);
            reportCellFacade.create(regDateCell);
            cells.add(regDateCell);

            ReportCell gnCell = new ReportCell();
            gnCell.setColumn(rcGn);
            gnCell.setRow(clientRow);
            gnCell.setContainsStringValue(true);
            if (c.getPerson().getGnArea() != null) {
                gnCell.setStringValue(c.getPerson().getGnArea().getName());
            } else {
                gnCell.setStringValue("Not set");
            }
            gnCell.setStoredQueryResult(sqr);
            reportCellFacade.create(gnCell);
            cells.add(gnCell);

            String dates = "";

//            //System.out.println("ce.getRemainigEncounters() = " + ce.getRemainigEncounters());
            int encounterNo = 1;
            for (Encounter e : ce.getRemainigEncounters().values()) {
                ReportCell vdCell = new ReportCell();
                vdCell.setRow(clientRow);
                vdCell.setContainsDateValue(true);
                vdCell.setDateValue(e.getCreatedAt());
                switch (encounterNo) {
                    case 1:
                        vdCell.setColumn(rcVd1);
                        break;
                    case 2:
                        vdCell.setColumn(rcVd2);
                        break;
                    case 3:
                        vdCell.setColumn(rcVd3);
                        break;
                    case 4:
                        vdCell.setColumn(rcVd4);
                        break;
                    default:
                        vdCell.setColumn(rcVd5);
                        dates += CommonController.dateTimeToString(e.getEncounterDate()) + "\n";
                        break;
                }
                vdCell.setStoredQueryResult(sqr);
                reportCellFacade.create(vdCell);
                cells.add(vdCell);
                encounterNo++;
            }

            ReportCell vdCell = new ReportCell();
            vdCell.setColumn(rcVd5);
            vdCell.setRow(clientRow);
            vdCell.setContainsStringValue(true);
            if (dates.equals("")) {
                vdCell.setStringValue(dates);
            }

            vdCell.setStoredQueryResult(sqr);
            reportCellFacade.create(vdCell);
            cells.add(vdCell);

            clientRow.setStoredQueryResult(sqr);
            reportRowFacade.create(clientRow);
            rows.add(clientRow);
        }

        sqr.setProcessCompleted(true);
        sqr.setProcessCompletedAt(new Date());
        storedQueryResultFacade.edit(sqr);

    }

    public List<InstitutionYearMonthCompleted> getIymcs() {
        // //System.out.println("getIymcs");
        if (iymcs == null) {
            Calendar c = Calendar.getInstance();
            int ti = c.get(Calendar.YEAR);
            int tm = c.get(Calendar.MONTH);
            year = ti;
            month = tm;
            iymcs = new ArrayList<>();
            for (Institution ins : findClinics()) {
                // //System.out.println("ins = " + ins.getName());
                InstitutionYearMonthCompleted iymc = new InstitutionYearMonthCompleted();
                iymc.setInstitution(ins);
                iymc.setYear(ti);
                iymc.setMonth(tm);
                iymcs.add(iymc);
            }
        }
        return iymcs;
    }

    public List<DesignComponentForm> fillFormsofTheSelectedSet(DesignComponentFormSet set) {
        if (set == null) {
            return new ArrayList<>();
        }
        String j = "Select f from DesignComponentForm f "
                + "where f.retired=false "
                + " and f.parentComponent=:pc "
                + " order by f.orderNo";
        Map m = new HashMap();
        m.put("pc", set);
        return designComponentFormFacade.findByJpql(j, m);
    }

    public InstitutionYearMonthCompleted selectNextIymcs() {
        // //System.out.println("selectNextIymcs");
        InstitutionYearMonthCompleted r = null;
        boolean allCompletedForThisCycle = true;
        for (InstitutionYearMonthCompleted t : getIymcs()) {
            // //System.out.println("t = " + t.getInstitution().getName());
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
        // //System.out.println("Running clinic count");
        // //System.out.println("iymc = " + iymc);
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

        // //System.out.println("iymc.getInstitution() = " + iymc.getInstitution().getName());
        // //System.out.println("fromDate = " + fromDate);
        // //System.out.println("toDate = " + toDate);
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

            List<ClientEncounterComponentItem> tecs = findClientEncounterComponentItems(enId);
            if (tecs != null) {
                ewc.setComponents(tecs);
                cs.add(ewc);
            }
        }
        return cs;
    }

    private List<ClientEncounterComponentItem> findClientEncounterComponentItems(Long endId) {
        try {
            String j;
            Map m;
            m = new HashMap();
            j = "select f from ClientEncounterComponentItem f "
                    + " where f.retired=false "
                    + " and f.encounter.id=:eid ";
            m.put("eid", endId);
            List<ClientEncounterComponentItem> ts = clientEncounterComponentItemFacade.findByJpql(j, m);
            return ts;
        } catch (Exception e) {
            return null;
        }
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
                if (qc.getParentComponent().getCode() != null && qc.getParentComponent().getCode().equalsIgnoreCase(qryCode)) {
                    output.add(qc);
                }
            }
        }
        try{
            output.sort(Comparator.comparing(QueryComponent::getOrderNo));
        }catch(Exception e){
            System.out.println("e = " + e);
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

    public List<QueryComponent> fillIndicators() {
        List<QueryComponent> tqcs = getQueryComponents();
        List<QueryComponent> nqs = new ArrayList<>();
        for (QueryComponent q : tqcs) {
            if (q.getQueryType() == QueryType.Indicator) {
                nqs.add(q);
            }
        }
        return nqs;
    }

    public List<QueryComponent> fillCounts() {
        List<QueryComponent> tqcs = getQueryComponents();
        List<QueryComponent> nqs = new ArrayList<>();
        for (QueryComponent q : tqcs) {
            if (q.getQueryType() == QueryType.Client_Count || q.getQueryType() == QueryType.Encounter_Count) {
                nqs.add(q);
            }
        }
        return nqs;
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

        if (q.getMatchType() == QueryCriteriaMatchType.Variable_Value_Check || q.getMatchType() == QueryCriteriaMatchType.Variable_Range_check) {

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
       if (q.getMatchType() == QueryCriteriaMatchType.Variable_Value_Check || q.getMatchType() == QueryCriteriaMatchType.Variable_Range_check) {
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

    public List<DesignComponentFormItem> fillItemsOfTheForm(DesignComponentForm form) {
        List<DesignComponentFormItem> is;
        if (form == null) {
            is = new ArrayList<>();
            return is;
        }
        String j = "Select i from DesignComponentFormItem i where i.retired=false "
                + " and i.parentComponent=:p "
                + " order by i.orderNo";
        Map m = new HashMap();
        m.put("p", form);
        return designComponentFormItemFacade.findByJpql(j, m);
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

    @Asynchronous
    public void runClinicCountsForRequests(QueryComponent tQc,
            Date fromDate,
            Date toDate,
            List<Institution> inss) {

        //System.out.println("runClinicCountsForRequests");

        for (Institution tIns : inss) {
            //System.out.println("tIns = " + tIns);
            if (tIns.getInstitutionType() == null) {
                JsfUtil.addErrorMessage("No Type for the institution");
                return;
            }
            if (tIns.getInstitutionType() != InstitutionType.Clinic) {
                JsfUtil.addErrorMessage("Selected institution is NOT a HLC?");
                return;
            }
            Jpq j = new Jpq();

            List<QueryWithCriteria> qs = new ArrayList<>();
            List<EncounterWithComponents> encountersWithComponents;

            List<Long> encounterIds = findEncounterIds(fromDate,
                    toDate,
                    tIns);

            encountersWithComponents = findEncountersWithComponents(encounterIds);
            if (encountersWithComponents == null) {
                continue;
            }

            QueryWithCriteria qwc = new QueryWithCriteria();
            qwc.setQuery(tQc);
            qwc.setCriteria(findCriteriaForQueryComponent(tQc.getCode()));

            Long value = calculateIndividualQueryResult(encountersWithComponents, qwc);
            j.setMessage("Clinic : " + tIns.getName() + "\n");
            j.setMessage(j.getMessage() + "From : " + CommonController.formatDate(fromDate) + "\n");
            j.setMessage(j.getMessage() + "To : " + CommonController.formatDate(toDate) + "\n");
            j.setMessage(j.getMessage() + "Number of Encounters : " + encountersWithComponents.size() + "\n");
            j.setMessage(j.getMessage() + "Count : " + qwc.getQuery().getName() + "\n");
            if (value != null) {
                saveValue(qwc.getQuery(), fromDate, toDate, tIns, value);
                j.setMessage(j.getMessage() + "Result : " + value + "\n");
            } else {
                j.setMessage(j.getMessage() + "Result : No Result\n");
            }
            //System.out.println("j.getErrorMessage() = " + j.getErrorMessage());
//        message = CommonController.stringToHtml(j.getErrorMessage());
//        result = CommonController.stringToHtml(j.getMessage());

        }
    }

    //    @Schedule(dayOfWeek = "Mon-Fri", month = "*", hour = "9-17", dayOfMonth = "*", year = "*", minute = "*", second = "0", persistent = false)
//    public void myTimer() {
//        // //System.out.println("Timer event: " + new Date());
//    }
//    @Schedule(hour = "21-5", minute = "*/5", second = "0", persistent = false)
    public void runStoredRequests() {
        System.out.print("Running Stored Requests*/5");
        Map m = new HashMap();
        String j = "select s"
                + " from StoredRequest s "
                + " where s.pending=:pen";
        m.put("pen", true);

        StoredRequest request = storedRequestFacade.findFirstByJpql(j, m);

        if (request == null) {
            return;
        }

        request.setPending(false);
        request.setProcessFailed(true);
        request.setProcessSuccess(false);
        request.setProcessStartedAt(new Date());
        storedRequestFacade.edit(request);

        List<QueryComponent> indicators = fillIndicators();

        if (request.getInstitution() == null) {
            return;
        }
        if (indicators == null) {
            return;
        }
        if (indicators.isEmpty()) {
            return;
        }
        if (request.getRyear() == 0) {
            JsfUtil.addErrorMessage("Year ?");
            return;
        }
        if (request.getRmonth() == null) {
            JsfUtil.addErrorMessage("Month");
            return;
        }

        System.out.println("Institution = " + request.getInstitution().getName());
        System.out.println("Peroid = " + request.getRyear() + " - " + request.getRmonth());
        
        Date fromDate = CommonController.startOfTheMonth(request.getRyear(), request.getRmonth(), true);
        Date toDate = CommonController.endOfTheMonth(request.getRyear(), request.getRmonth(), true);

        List<QueryWithCriteria> qs = new ArrayList<>();
        List<EncounterWithComponents> encountersWithComponents;
        List<Long> encounterIds = findEncounterIds(fromDate,
                toDate,
                request.getInstitution());
        encountersWithComponents = findEncountersWithComponents(encounterIds);
        if (encountersWithComponents == null) {
            return;
        }
        Map<Long, QueryComponent> qcs = new HashMap<>();
        List<Replaceable> rs = new ArrayList<>();
        for (QueryComponent qc : indicators) {
            List<Replaceable> trs = findReplaceblesInIndicatorQuery(qc.getIndicatorQuery());
            if (trs != null && !trs.isEmpty()) {
                rs.addAll(trs);
            }
        }
        //System.out.println("6");
        //System.out.println("6. " + new Date());
        for (Replaceable r : rs) {
            QueryComponent temqc = findLastQuery(r.getQryCode());
            if (temqc == null) {
                continue;
            }
            if (null == temqc.getQueryType()) {
            } else {
                switch (temqc.getQueryType()) {
                    case Client_Count:
                    case Encounter_Count:
                        qcs.put(temqc.getId(), temqc);
                        break;
                    case Population:
                        break;
                    default:
                }
            }
        }
        //System.out.println("7");
        //System.out.println("7. " + new Date());

        if (qcs.isEmpty()) {
            return;
        }

        for (QueryComponent qcc : qcs.values()) {
            QueryWithCriteria qwc = new QueryWithCriteria();
            qwc.setQuery(qcc);
            qwc.setCriteria(findCriteriaForQueryComponent(qcc.getCode()));
            qs.add(qwc);
        }

        //System.out.println("8");
        //System.out.println("8. " + new Date());

        for (QueryWithCriteria qwc : qs) {
            Long value = calculateIndividualQueryResult(encountersWithComponents, qwc);
            if (value != null) {
                if (qwc != null) {
                    saveValue(qwc.getQuery(), fromDate, toDate, request.getInstitution(), value);
                }
            }
        }
        
        request.setProcessCompletedAt(new Date());
        request.setProcessSuccess(true);
        request.setProcessFailed(false);
        storedRequestFacade.edit(request);
        
        //System.out.println("9");

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

    public QueryComponent findLastQuery(String qry) {
        if (qry == null) {
            return null;
        }
        QueryComponent nq = null;
        List<QueryComponent> tqcs = getQueryComponents();
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

}
