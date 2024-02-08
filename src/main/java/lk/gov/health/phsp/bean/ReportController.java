/*
 * The MIT License
 *
 * Copyright 2019 buddhika.ari@gmail.com
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

// <editor-fold defaultstate="collapsed" desc="Imports">
import jakarta.validation.ReportAsSingleViolation;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Inject;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.Client;
import lk.gov.health.phsp.entity.Encounter;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.enums.EncounterType;
import lk.gov.health.phsp.facade.ClientEncounterComponentFormSetFacade;
import lk.gov.health.phsp.pojcs.NcdReportTem;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.TemporalType;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import lk.gov.health.phsp.ejb.AnalysisBean;
import lk.gov.health.phsp.entity.ClientEncounterComponentFormSet;
import lk.gov.health.phsp.entity.ClientEncounterComponentItem;
import lk.gov.health.phsp.entity.ConsolidatedQueryResult;
import lk.gov.health.phsp.entity.DesignComponentForm;
import lk.gov.health.phsp.entity.DesignComponentFormItem;
import lk.gov.health.phsp.entity.DesignComponentFormSet;
import lk.gov.health.phsp.entity.IndividualQueryResult;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.entity.Person;
import lk.gov.health.phsp.entity.QueryComponent;
import lk.gov.health.phsp.entity.StoredQueryResult;
import lk.gov.health.phsp.entity.Upload;
import lk.gov.health.phsp.enums.Quarter;
import lk.gov.health.phsp.enums.QueryCriteriaMatchType;
import lk.gov.health.phsp.enums.QueryType;
import lk.gov.health.phsp.enums.SelectionDataType;
import lk.gov.health.phsp.enums.TimePeriodType;
import lk.gov.health.phsp.facade.ClientEncounterComponentItemFacade;
import lk.gov.health.phsp.facade.ClientFacade;
import lk.gov.health.phsp.facade.ConsolidatedQueryResultFacade;
import lk.gov.health.phsp.facade.DesignComponentFormItemFacade;
import lk.gov.health.phsp.facade.EncounterFacade;
import lk.gov.health.phsp.facade.IndividualQueryResultFacade;
import lk.gov.health.phsp.facade.QueryComponentFacade;
import lk.gov.health.phsp.facade.StoredQueryResultFacade;
import lk.gov.health.phsp.facade.UploadFacade;
import lk.gov.health.phsp.facade.util.JsfUtil;
import lk.gov.health.phsp.pojcs.AreaCount;
import lk.gov.health.phsp.pojcs.ClientBasicData;
import lk.gov.health.phsp.pojcs.ClientFirstEncounterDetailsRemainingEncounterDatesAndTypes;
import lk.gov.health.phsp.pojcs.DateInstitutionCount;
import lk.gov.health.phsp.pojcs.EncounterBasicData;
import lk.gov.health.phsp.pojcs.InstitutionCount;
import lk.gov.health.phsp.pojcs.Replaceable;
import lk.gov.health.phsp.entity.ReportCell;
import lk.gov.health.phsp.entity.ReportColumn;
import lk.gov.health.phsp.entity.ReportRow;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.facade.InstitutionFacade;
import lk.gov.health.phsp.facade.ReportCellFacade;
import lk.gov.health.phsp.facade.ReportColumnFacade;
import lk.gov.health.phsp.facade.ReportRowFacade;
import lk.gov.health.phsp.pojcs.InstituteTypeCounts;
import lk.gov.health.phsp.pojcs.ReportTimePeriod;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
// </editor-fold>   

/**
 *
 * @author hiu_pdhs_sp
 */
@Named(value = "reportController")
@SessionScoped
public class ReportController implements Serializable {
// <editor-fold defaultstate="collapsed" desc="EJBs">

    @EJB
    AnalysisBean analysisBean;
    @EJB
    private DesignComponentFormItemFacade designComponentFormItemFacade;
    @EJB
    private ClientEncounterComponentItemFacade clientEncounterComponentItemFacade;
    @EJB
    private ClientEncounterComponentFormSetFacade clientEncounterComponentFormSetFacade;
    @EJB
    private ClientFacade clientFacade;
    @EJB
    private EncounterFacade encounterFacade;
    @EJB
    private InstitutionFacade institutionFacade;
    @EJB
    private QueryComponentFacade queryComponentFacade;
    @EJB
    private UploadFacade uploadFacade;
    @EJB
    private StoredQueryResultFacade storedQueryResultFacade;
    @EJB
    private ConsolidatedQueryResultFacade consolidatedQueryResultFacade;
    @EJB
    private IndividualQueryResultFacade individualQueryResultFacade;
    @EJB
    ReportColumnFacade reportColumnFacade;
    @EJB
    ReportRowFacade reportRowFacade;
    @EJB
    ReportCellFacade reportCellFacade;

// </editor-fold>     
// <editor-fold defaultstate="collapsed" desc="Controllers">
    @Inject
    StreamedContentController streamedContentController;
    @Inject
    private EncounterController encounterController;
    @Inject
    private ClientController clientController;
    @Inject
    private ComponentController componentController;
    @Inject
    private WebUserController webUserController;
    @Inject
    private InstitutionController institutionController;
    @Inject
    InstitutionApplicationController institutionApplicationController;
    @Inject
    private QueryComponentController queryComponentController;
    @Inject
    private ClientEncounterComponentItemController clientEncounterComponentItemController;
    @Inject
    private ExcelReportController excelReportController;
    @Inject
    DesignComponentFormController designComponentFormController;
    @Inject
    private DesignComponentFormItemController designComponentFormItemController;
    @Inject
    private UserTransactionController userTransactionController;
// </editor-fold>  
// <editor-fold defaultstate="collapsed" desc="Class Variables">
    private List<Encounter> encounters;
    private List<Client> clients;
    private Date fromDate;
    private Date toDate;
    private Institution institution;
    private DesignComponentFormSet fromSet;
    private Area area;
    private NcdReportTem ncdReportTem;
    private StreamedContent file;
    private String mergingMessage;
    private QueryComponent queryComponent;
    private DesignComponentFormSet formset;
    private List<ClientEncounterComponentFormSet> clientEncounterComponentFormSets = null;
// </editor-fold> 

// <editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Creates a new instance of ReportController
     */
    public ReportController() {
    }

// </editor-fold> 
    private List<StoredQueryResult> myResults;
    private List<StoredQueryResult> reportResults;
    private StoredQueryResult selectedStoredQueryResult;
    private List<InstitutionCount> institutionCounts;
    private Long reportCount;
    private List<AreaCount> areaCounts;
    private Long areaRepCount;
    private DesignComponentFormSet designingComponentFormSet;
    private List<DesignComponentFormItem> designComponentFormItems;
    private DesignComponentFormItem designComponentFormItem;

    private StoredQueryResult removingResult;
    private StoredQueryResult downloadingResult;

    private Upload currentUpload;
    private StreamedContent downloadingFile;
    private StreamedContent resultExcelFile;

    private ReportTimePeriod reportTimePeriod;
    private TimePeriodType timePeriodType;
    private Integer year;
    private Integer quarter;
    private Integer month;
    private Integer dateOfMonth;
    private Quarter quarterEnum;
    private boolean recalculate;

    public StreamedContent getDownloadingFile() {
        if (getDownloadingResult() == null) {
            JsfUtil.addErrorMessage("No Download file");
            return null;
        }
        if (getDownloadingResult().getUpload() == null) {
            JsfUtil.addErrorMessage("No Excel file");
            return null;
        }
        if (downloadingResult == null) {
            JsfUtil.addErrorMessage("Null Error - 1");
            return null;
        }
        if (downloadingResult.getUpload() == null) {
            JsfUtil.addErrorMessage("Null Error - 2");
            return null;
        }
        if (downloadingResult.getUpload().getBaImage() == null) {
            JsfUtil.addErrorMessage("Null Error - 3");
            return null;
        }
        InputStream stream = new ByteArrayInputStream(downloadingResult.getUpload().getBaImage());
        if (downloadingResult.getUpload().getFileType() == null || downloadingResult.getUpload().getFileType().trim().equals("")) {
            downloadingResult.getUpload().setFileType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            getStoredQueryResultFacade().edit(downloadingResult);
        }
//        downloadingFile = new DefaultStreamedContent(stream, downloadingResult.getUpload().getFileType(), downloadingResult.getUpload().getFileName());

        return downloadingFile;
    }

    public void listMyReports() {
        String j;
        Map m = new HashMap();
        j = "select s "
                + " from StoredQueryResult s "
                + " where s.retired=false "
                + " and s.creater=:me "
                + " order by s.id desc";

        m.put("me", webUserController.getLoggedUser());
        myResults = getStoredQueryResultFacade().findByJpql(j, m, true);
    }

    public void listMyReportsLast() {
        String j;
        Map m = new HashMap();
        j = "select s "
                + " from StoredQueryResult s "
                + " where s.retired=false "
                + " and s.creater=:me "
                + " order by s.id desc";

        m.put("me", webUserController.getLoggedUser());
        myResults = getStoredQueryResultFacade().findByJpql(j, m, 100);
    }

    public void listExistingMonthlyReports() {
        setTimePeriodType(TimePeriodType.Monthly);
        listExistingReports();
        userTransactionController.recordTransaction("List Existing Monthly Reports");
    }

    public void listExistingReports() {
        if (institution == null) {
            JsfUtil.addErrorMessage("Please select an institutions");
            return;
        }

        if (queryComponent == null) {
            JsfUtil.addErrorMessage("Please select a report");
            return;
        }

        switch (getTimePeriodType()) {
            case Yearley:
                setFromDate(CommonController.startOfTheYear(getYear()));
                setToDate(CommonController.endOfYear(getYear()));
                break;
            case Quarterly:
                setFromDate(CommonController.startOfQuarter(getYear(), getQuarter()));
                setToDate(CommonController.endOfQuarter(getYear(), getQuarter()));
                break;
            case Monthly:
                setFromDate(CommonController.startOfTheMonth(getYear(), getMonth()));
                setToDate(CommonController.endOfTheMonth(getYear(), getMonth()));
                break;
            case Dates:
            //TODO: Add what happens when selected dates

        }

        String j;
        Map m = new HashMap();
        j = "select s "
                + " from StoredQueryResult s "
                + " where s.retired=false "
                + " and s.institution=:ins "
                + " and s.queryComponent=:qc "
                + " and s.resultFrom=:f "
                + " and s.resultTo=:t "
                + " order by s.id desc";

        m.put("ins", institution);
        m.put("qc", queryComponent);
        m.put("f", getFromDate());
        m.put("t", getToDate());

        reportResults = getStoredQueryResultFacade().findByJpql(j, m);

    }

    public void removeReport() {
        if (removingResult == null) {
            JsfUtil.addErrorMessage("Nothing to remove");
            return;
        }
        if (removingResult.isProcessCompleted()
                && !removingResult.getCreater().equals(webUserController.getLoggedUser())) {
            JsfUtil.addErrorMessage("You can not remove others successful reports.");
            return;
        }
        removingResult.setRetired(true);
        removingResult.setRetirer(webUserController.getLoggedUser());
        removingResult.setRetiredAt(new Date());
        getStoredQueryResultFacade().edit(removingResult);
        JsfUtil.addSuccessMessage("Removed");
        listExistingReports();
        listMyReports();
        userTransactionController.recordTransaction("Remove Report");
    }

    public void fillItemsofTheSelectedSet() {
        designComponentFormItems = designComponentFormItemController.fillItemsOfTheFormSet(designingComponentFormSet);
    }

    public void createExcelFileOfClinicalEncounterItemsForSelectedDesignComponent() {

        if (institution == null) {
            JsfUtil.addErrorMessage("Please select an institutions");
            return;
        }
        if (designComponentFormItem == null) {
            JsfUtil.addErrorMessage("Please select a variable");
            return;
        }
        if (designComponentFormItem.getItem() == null
                || designComponentFormItem.getItem().getCode() == null) {
            JsfUtil.addErrorMessage("Error in selected variable.");
        }

        String j = "select f "
                + " from  ClientEncounterComponentItem f join f.itemEncounter e"
                + " where f.retired<>:fr "
                + " and f.item.code=:ic ";
        j += " and e.institution=:i "
                + " and e.retired<>:er "
                + " and e.encounterType=:t "
                + " and e.encounterDate between :fd and :td"
                + " order by e.id";
        Map m = new HashMap();
        m.put("fr", true);
        m.put("ic", designComponentFormItem.getItem().getCode().toLowerCase());
        m.put("i", institution);
        m.put("er", true);

        m.put("t", EncounterType.Clinic_Visit);
        m.put("fd", fromDate);
        m.put("td", toDate);

        List<ClientEncounterComponentItem> cis = clientEncounterComponentItemFacade.findByJpql(j, m);

        //String phn, String gnArea, String institution, Date dataOfBirth, Date encounterAt, String sex
//        List<Object> objs = getClientFacade().findAggregates(j, m);
        String FILE_NAME = "client_values" + "_" + (new Date()) + ".xlsx";
        String mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        String folder = "/tmp/";

        File newFile = new File(folder + FILE_NAME);

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Client Values");

        int rowCount = 0;

        Row t1 = sheet.createRow(rowCount++);
        Cell th1_lbl = t1.createCell(0);
        th1_lbl.setCellValue("Report");
        Cell th1_val = t1.createCell(1);
        th1_val.setCellValue("List of Clinic Values");

        Row t2 = sheet.createRow(rowCount++);
        Cell th2_lbl = t2.createCell(0);
        th2_lbl.setCellValue("From");
        Cell th2_val = t2.createCell(1);
        th2_val.setCellValue(CommonController.dateTimeToString(fromDate, "dd MMMM yyyy"));

        Row t3 = sheet.createRow(rowCount++);
        Cell th3_lbl = t3.createCell(0);
        th3_lbl.setCellValue("To");
        Cell th3_val = t3.createCell(1);
        th3_val.setCellValue(CommonController.dateTimeToString(toDate, "dd MMMM yyyy"));

        Row t4 = sheet.createRow(rowCount++);
        Cell th4_lbl = t4.createCell(0);
        th4_lbl.setCellValue("Institution");
        Cell th4_val = t4.createCell(1);
        th4_val.setCellValue(institution.getName());

        Row t5a = sheet.createRow(rowCount++);
        Cell th5a_lbl = t5a.createCell(0);
        th5a_lbl.setCellValue("Variable");
        Cell th5a_val = t5a.createCell(1);
        th5a_val.setCellValue(designComponentFormItem.getItem().getName());

        rowCount++;

        Row t5 = sheet.createRow(rowCount);
        Cell th5_1 = t5.createCell(0);
        th5_1.setCellValue("Serial");
        Cell th5_2 = t5.createCell(1);
        th5_2.setCellValue("PHN");
        Cell th5_3 = t5.createCell(2);
        th5_3.setCellValue("Sex");
        Cell th5_4 = t5.createCell(3);
        th5_4.setCellValue("Age in Years at Encounter");
        Cell th5_5 = t5.createCell(4);
        th5_5.setCellValue("Encounter at");
        Cell th5_6 = t5.createCell(5);
        th5_6.setCellValue("Short-text Value");
        Cell th5_7 = t5.createCell(6);
        th5_7.setCellValue("Long Value");
        Cell th5_8 = t5.createCell(7);
        th5_8.setCellValue("Int Value");
        Cell th5_9 = t5.createCell(8);
        th5_9.setCellValue("Real Value");
        Cell th5_10 = t5.createCell(9);
        th5_10.setCellValue("Item Value");
        Cell th5_11 = t5.createCell(10);
        th5_11.setCellValue("Item Value");
        Cell th5_12 = t5.createCell(11);
        th5_12.setCellValue("Completed");

        Cell th5_13 = t5.createCell(12);
        th5_13.setCellValue("Name");
        Cell th5_14 = t5.createCell(13);
        th5_14.setCellValue("Address");
        Cell th5_15 = t5.createCell(14);
        th5_15.setCellValue("Mobile");
        Cell th5_16 = t5.createCell(15);
        th5_16.setCellValue("Phone");
        Cell th5_17 = t5.createCell(16);
        th5_17.setCellValue("GN Area");

        int serial = 1;

        CellStyle cellStyle = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        cellStyle.setDataFormat(
                createHelper.createDataFormat().getFormat("dd/MMMM/yyyy hh:mm"));

        for (ClientEncounterComponentItem i : cis) {
            if (i.getItemEncounter() != null) {

                Encounter e = i.getItemEncounter();
                if (e.getClient() == null) {
                    continue;
                }
                Client c = e.getClient();
                if (c == null) {
                    continue;
                }
                Person p = c.getPerson();
                if (p == null) {
                    continue;
                }
                Row row = sheet.createRow(rowCount++);

                Cell c1 = row.createCell(0);
                c1.setCellValue(serial);

                Cell c2 = row.createCell(1);
                if (c.getPhn() != null) {
                    c2.setCellValue(c.getPhn());
                }

                Cell c3 = row.createCell(2);
                if (p.getSex() != null) {
                    c3.setCellValue(p.getSex().getName());
                }

                Cell c4 = row.createCell(3);
                int ageInYears = CommonController.calculateAge(p.getDateOfBirth(), e.getEncounterDate());
                c4.setCellValue(ageInYears);

                Cell c5 = row.createCell(4);
                if (e.getEncounterDate() != null) {
                    c5.setCellValue(e.getEncounterDate());
                }
                c5.setCellStyle(cellStyle);

                Cell c6 = row.createCell(5);
                if (i.getShortTextValue() != null) {
                    c6.setCellValue(i.getShortTextValue());
                }

                Cell c7 = row.createCell(6);
                if (i.getLongNumberValue() != null) {
                    c7.setCellValue(i.getLongNumberValue());
                }

                Cell c8 = row.createCell(7);
                if (i.getIntegerNumberValue() != null) {
                    c8.setCellValue(i.getIntegerNumberValue());
                }

                Cell c9 = row.createCell(8);
                if (i.getRealNumberValue() != null) {
                    c9.setCellValue(i.getRealNumberValue());
                }

                Cell c10 = row.createCell(9);
                if (i.getItemValue() != null && i.getItemValue().getName() != null) {
                    c10.setCellValue(i.getItemValue().getName());
                }

                Cell c11 = row.createCell(10);
                if (i.getBooleanValue() != null) {
                    c11.setCellValue(i.getBooleanValue() ? "True" : "False");
                }

                Cell c12 = row.createCell(11);

                if (i.getParentComponent() != null && i.getParentComponent().getParentComponent() != null) {
                    c12.setCellValue(i.getParentComponent().getParentComponent().isCompleted() ? "Complete" : "Not Completed");
                }

                Cell c13 = row.createCell(13);
                if (c.getPerson() != null) {
                    c13.setCellValue(c.getPerson().getName());
                }

                Cell c14 = row.createCell(14);
                if (c.getPerson() != null) {
                    c14.setCellValue(c.getPerson().getAddress());
                }

                Cell c15 = row.createCell(15);
                if (c.getPerson() != null) {
                    c15.setCellValue(c.getPerson().getPhone1());
                }

                Cell c16 = row.createCell(16);
                if (c.getPerson() != null) {
                    c16.setCellValue(c.getPerson().getPhone2());
                }

                Cell c17 = row.createCell(17);
                if (c.getPerson() != null && c.getPerson().getGnArea() != null) {
                    c17.setCellValue(c.getPerson().getGnArea().getName());
                }

                serial++;
            }
        }

        cis = null;

        try (FileOutputStream outputStream = new FileOutputStream(newFile)) {
            workbook.write(outputStream);
        } catch (Exception e) {

        }

        InputStream stream;
        try {
            stream = new FileInputStream(newFile);
            resultExcelFile = streamedContentController.generateStreamedContent(mimeType, FILE_NAME, stream);
        } catch (FileNotFoundException ex) {

        }
    }

    public void clearReportData() {
        if (institution == null) {
            JsfUtil.addErrorMessage("Please select an institutions");
            return;
        }

        StoredQueryResult sqr = new StoredQueryResult();
        switch (getTimePeriodType()) {
            case Yearley:
                sqr.setResultFrom(CommonController.startOfTheYear(getYear()));
                sqr.setResultTo(CommonController.endOfYear(getYear()));
                sqr.setResultYear(getYear());
                break;
            case Quarterly:
                sqr.setResultFrom(CommonController.startOfQuarter(getYear(), getQuarter()));
                sqr.setResultTo(CommonController.endOfQuarter(getYear(), getQuarter()));
                sqr.setResultYear(getYear());
                sqr.setResultQuarter(getQuarter());
                break;
            case Monthly:
                sqr.setResultFrom(CommonController.startOfTheMonth(getYear(), getMonth()));
                sqr.setResultTo(CommonController.endOfTheMonth(getYear(), getMonth()));
                sqr.setResultYear(getYear());
                sqr.setResultMonth(getMonth());
                break;
            case Dates:
                sqr.setResultFrom(fromDate);
                sqr.setResultTo(toDate);
                break;
        }
        setFromDate(sqr.getResultFrom());
        setToDate(sqr.getResultTo());

        String j;
        Map m = new HashMap();
        j = "select r "
                + " from ConsolidatedQueryResult r "
                + " where r.resultFrom=:fd "
                + " and r.resultTo=:td ";
        m.put("fd", sqr.getResultFrom());
        m.put("td", sqr.getResultTo());
        j += " and r.institution=:ins ";
        m.put("ins", institution);
        List<ConsolidatedQueryResult> crs = getConsolidatedQueryResultFacade().findByJpql(j, m);
        for (ConsolidatedQueryResult cr : crs) {
            cr.setLongValue(null);
            getConsolidatedQueryResultFacade().edit(cr);
        }

        List<Long> encIds = findEncounterIds(sqr.getResultFrom(), sqr.getResultTo(), institution);

        for (Long encId : encIds) {
            m = new HashMap();
            j = "select r "
                    + " from IndividualQueryResult r "
                    + " where r.encounterId=:enid";
            m.put("enid", encId);
            List<IndividualQueryResult> iqrs = getIndividualQueryResultFacade().findByJpql(j, m);

            for (IndividualQueryResult iqr : iqrs) {
                iqr.setIncluded(null);
                getIndividualQueryResultFacade().edit(iqr);
            }

        }

        JsfUtil.addSuccessMessage("All Previous Calculated Data Discarded.");

    }

    public void createNewMonthlyReport() {
        setTimePeriodType(TimePeriodType.Monthly);
        createNewReport();
        System.gc();
        userTransactionController.recordTransaction("Create New Monthly Report");
    }

    public void createNewReport() {
        if (institution == null) {
            JsfUtil.addErrorMessage("Please select an institutions");
            return;
        }

        if (queryComponent == null) {
            JsfUtil.addErrorMessage("Please select a report");
            return;
        }

        StoredQueryResult sqr = new StoredQueryResult();
        sqr.setCreatedAt(new Date());
        sqr.setCreater(webUserController.getLoggedUser());
        sqr.setRecalculate(recalculate);
        sqr.setInstitution(institution);
        sqr.setRequestCreatedAt(new Date());
        sqr.setTimePeriodType(getTimePeriodType());
        sqr.setQueryComponent(queryComponent);

        switch (getTimePeriodType()) {
            case Yearley:
                sqr.setResultFrom(CommonController.startOfTheYear(getYear()));
                sqr.setResultTo(CommonController.endOfYear(getYear()));
                sqr.setResultYear(getYear());

                break;
            case Quarterly:
                sqr.setResultFrom(CommonController.startOfQuarter(getYear(), getQuarter()));
                sqr.setResultTo(CommonController.endOfQuarter(getYear(), getQuarter()));
                sqr.setResultYear(getYear());
                sqr.setResultQuarter(getQuarter());
                break;
            case Monthly:

                sqr.setResultFrom(CommonController.startOfTheMonth(getYear(), getMonth()));
                sqr.setResultTo(CommonController.endOfTheMonth(getYear(), getMonth()));
                sqr.setResultYear(getYear());
                sqr.setResultMonth(getMonth());
                break;
            case Dates:
            //TODO: Add what happens when selected dates

        }

        getStoredQueryResultFacade().create(sqr);

        setFromDate(sqr.getResultFrom());
        setToDate(sqr.getResultTo());
        JsfUtil.addSuccessMessage("Added to the Queue to Process");
        boolean reportDone = getExcelReportController().processReport(sqr);
        if (reportDone) {
            JsfUtil.addSuccessMessage("Report Created. Please click the list button to list it.");
        } else {
            JsfUtil.addErrorMessage("Error");
        }

    }

    public TimePeriodType getTimePeriodType() {
        if (timePeriodType == null) {
            timePeriodType = TimePeriodType.Monthly;
        }
        return timePeriodType;
    }

    public void setTimePeriodType(TimePeriodType timePeriodType) {
        this.timePeriodType = timePeriodType;
    }

    public Integer getYear() {
        if (year == null || year == 0) {
            year = CommonController.getYear(CommonController.startOfTheLastQuarter());
        }
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getQuarter() {
        if (quarter == null) {
            quarter = CommonController.getQuarter(CommonController.startOfTheLastQuarter());
        }
        return quarter;
    }

    public void setQuarter(Integer quarter) {
        this.quarter = quarter;
    }

    public Integer getMonth() {
        if (month == null) {
            month = CommonController.getMonth(CommonController.startOfTheLastMonth());
        }
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
        if (quarterEnum == null) {
            switch (getQuarter()) {
                case 1:
                    quarterEnum = Quarter.First;
                    break;
                case 2:
                    quarterEnum = Quarter.Second;
                    break;
                case 3:
                    quarterEnum = Quarter.Third;
                    break;
                case 4:
                    quarterEnum = Quarter.Fourth;
                    break;
                default:
                    quarterEnum = Quarter.First;
            }
        }
        return quarterEnum;
    }

    public void setQuarterEnum(Quarter quarterEnum) {
        switch (quarterEnum) {
            case First:
                quarter = 1;
                break;
            case Second:
                quarter = 2;
                break;
            case Third:
                quarter = 3;
                break;
            case Fourth:
                quarter = 4;
                break;
            default:
                quarter = 1;
        }
        this.quarterEnum = quarterEnum;
    }

    public StoredQueryResultFacade getStoredQueryResultFacade() {
        return storedQueryResultFacade;
    }

    public ReportTimePeriod getReportTimePeriod() {
        return reportTimePeriod;
    }

    public void setReportTimePeriod(ReportTimePeriod reportTimePeriod) {
        this.reportTimePeriod = reportTimePeriod;
    }

// <editor-fold defaultstate="collapsed" desc="Navigation">
    public String toViewReports() {
        String forSys = "/national/reports/index";
        String forIns = "/hospital/reports/index";
        String forMe = "/national/reports/index";
        String forClient = "/reports/index";
        String forMoh = "/moh/reports/index";
        String noAction = "";
        String action = "";
        switch (webUserController.getLoggedUser().getWebUserRole()) {
            case Client:
                action = forClient;
                break;
            case Doctor:
            case Institution_Administrator:
            case Institution_Super_User:
            case Institution_User:
            case Nurse:
            case Midwife:
                action = forIns;
                break;
            case Me_Admin:
            case Me_Super_User:
                action = forMe;
                break;
            case Me_User:
            case User:
                action = noAction;
                break;
            case Super_User:
            case System_Administrator:
                action = forSys;
                break;
        }
        userTransactionController.recordTransaction("To View Clinic Visits By Institution");
        return action;
    }
    
    public String toReportCounts() {
        String forSys = "/national/counts/index";
        String forIns = "/hospital/counts/index";
        String forMe = "/national/counts/index";
        String forClient = "/counts/index";
        String forMoh = "/moh/counts/index";
        String noAction = "";
        String action = "";
        switch (webUserController.getLoggedUser().getWebUserRole()) {
            case Client:
                action = forClient;
                break;
            case Doctor:
            case Institution_Administrator:
            case Institution_Super_User:
            case Institution_User:
            case Nurse:
            case Midwife:
                action = forIns;
                break;
            case Me_Admin:
            case Me_Super_User:
                action = forMe;
                break;
            case Me_User:
            case User:
                action = noAction;
                break;
            case Super_User:
            case System_Administrator:
                action = forSys;
                break;
        }
        userTransactionController.recordTransaction("To View Clinic Visits By Institution");
        return action;
    }

    public String toInstitutionMonthlySummeries() {
        String forSys = "/reports/summaries/institution_monthly_summaries_sa";
        String forIns = "/reports/summaries/institution_monthly_summaries_ia";
        String forMeu = "/reports/summaries/institution_excel_reports_meu";
        String forMea = "/reports/summaries/institution_excel_reports_mea";
        String forClient = "";
        String noAction = "";
        String action = "";
        switch (webUserController.getLoggedUser().getWebUserRole()) {
            case Client:
                action = forClient;
                break;
            case Doctor:
            case Institution_Administrator:
            case Institution_Super_User:
            case Institution_User:
            case Nurse:
            case Midwife:
                action = forIns;
                break;
            case Me_Admin:
                action = forMea;
                break;
            case Me_Super_User:
                action = forMeu;
                break;
            case Me_User:
            case User:
                action = noAction;
                break;
            case Super_User:
            case System_Administrator:
                action = forSys;
                break;
        }
        userTransactionController.recordTransaction("To Institution Monthly Summeries");
        return action;
    }

    public String toConsolidateSummeries() {
        String forSys = "/reports/summaries/consolidate_summaries_sa";
        String forIns = "/reports/summaries/consolidate_summaries_ia";
        String forMeu = "/reports/summaries/consolidate_meu";
        String forMea = "/reports/summaries/consolidate_mea";
        String forClient = "";
        String noAction = "";
        String action = "";
        switch (webUserController.getLoggedUser().getWebUserRole()) {
            case Client:
                action = forClient;
                break;
            case Doctor:
            case Institution_Administrator:
            case Institution_Super_User:
            case Institution_User:
            case Nurse:
            case Midwife:
                action = forIns;
                break;
            case Me_Admin:
                action = forMea;
                break;
            case Me_Super_User:
                action = forMeu;
                break;
            case Me_User:
            case User:
                action = noAction;
                break;
            case Super_User:
            case System_Administrator:
                action = forSys;
                break;
        }
        userTransactionController.recordTransaction("To Consolidate Summeries");
        return action;
    }

    public String toSingleVariableClinicalData() {
        String forSys = "/reports/clinical_data/single_variable_sa";
        String forIns = "/reports/clinical_data/single_variable_ia";
        String forMeu = "/reports/clinical_data/single_variable_meu";
        String forMea = "/reports/clinical_data/single_variable_mea";
        String forClient = "";
        String noAction = "";
        String action = "";
        switch (webUserController.getLoggedUser().getWebUserRole()) {
            case Client:
                action = forClient;
                break;
            case Doctor:
            case Institution_Administrator:
            case Institution_Super_User:
            case Institution_User:
            case Nurse:
            case Midwife:
                action = forIns;
                break;
            case Me_Admin:
                action = forMea;
                break;
            case Me_Super_User:
                action = forMeu;
                break;
            case Me_User:
            case User:
                action = noAction;
                break;
            case Super_User:
            case System_Administrator:
                action = forSys;
                break;
        }
        userTransactionController.recordTransaction("To Single Variable Clinical Data");
        return action;
    }

    public String toSingleVariableClinicalDataCounts() {
        String forSys = "/national/reports/clinical_data_single_counts";
        String forIns = "/hospital/reports/clinical_data_single_counts";
        String forMeu = "/national/reports/clinical_data_single_counts";
        String forMea = "/national/reports/clinical_data_single_counts";
        String forClient = "";
        String noAction = "";
        String action = "";
        switch (webUserController.getLoggedUser().getWebUserRole()) {
            case Client:
                action = forClient;
                break;
            case Doctor:
            case Institution_Administrator:
            case Institution_Super_User:
            case Institution_User:
            case Nurse:
            case Midwife:
                action = forIns;
                break;
            case Me_Admin:
                action = forMea;
                break;
            case Me_Super_User:
                action = forMeu;
                break;
            case Me_User:
            case User:
                action = noAction;
                break;
            case Super_User:
            case System_Administrator:
                action = forSys;
                break;
        }
        return action;
    }

    public String toMultipleVariableClinicalData() {
        String forSys = "/reports/clinical_data/multiple_variable_sa";
        String forIns = "/reports/clinical_data/multiple_variable_ia";
        String forMeu = "/reports/clinical_data/multiple_variable_meu";
        String forMea = "/reports/clinical_data/multiple_variable_mea";
        String forClient = "";
        String noAction = "";
        String action = "";
        switch (webUserController.getLoggedUser().getWebUserRole()) {
            case Client:
                action = forClient;
                break;
            case Doctor:
            case Institution_Administrator:
            case Institution_Super_User:
            case Institution_User:
            case Nurse:
            case Midwife:
                action = forIns;
                break;
            case Me_Admin:
                action = forMea;
                break;
            case Me_Super_User:
                action = forMeu;
                break;
            case Me_User:
            case User:
                action = noAction;
                break;
            case Super_User:
            case System_Administrator:
                action = forSys;
                break;
        }
        userTransactionController.recordTransaction("To Single Variable Clinical Data");
        return action;
    }

    public String toFormsetClinicalData() {
        String forSys = "/national/reports/clinical_data_form";
        String forIns = "/national/reports/clinical_data_form";
        String forMeu = "/national/reports/clinical_data_form";
        String forMea = "/national/reports/clinical_data_form";
        String forClient = "";
        String noAction = "";
        String action = "";
        switch (webUserController.getLoggedUser().getWebUserRole()) {
            case Client:
                action = forClient;
                break;
            case Doctor:
            case Institution_Administrator:
            case Institution_Super_User:
            case Institution_User:
            case Nurse:
            case Midwife:
                action = forIns;
                break;
            case Me_Admin:
                action = forMea;
                break;
            case Me_Super_User:
                action = forMeu;
                break;
            case Me_User:
            case User:
                action = noAction;
                break;
            case Super_User:
            case System_Administrator:
                action = forSys;
                break;
        }
        userTransactionController.recordTransaction("To Single Variable Clinical Data");
        return action;
    }

    public String toFormsetClinicalDataRdhs() {
        String forSys = "/national/reports/clinical_data_form_rdhs";
        String forIns = "/national/reports/clinical_data_form_rdhs";
        String forMeu = "/national/reports/clinical_data_form_rdhs";
        String forMea = "/national/reports/clinical_data_form_rdhs";
        String forClient = "";
        String noAction = "";
        String action = "";
        switch (webUserController.getLoggedUser().getWebUserRole()) {
            case Client:
                action = forClient;
                break;
            case Doctor:
            case Institution_Administrator:
            case Institution_Super_User:
            case Institution_User:
            case Nurse:
            case Midwife:
                action = forIns;
                break;
            case Me_Admin:
                action = forMea;
                break;
            case Me_Super_User:
                action = forMeu;
                break;
            case Me_User:
            case User:
                action = noAction;
                break;
            case Super_User:
            case System_Administrator:
                action = forSys;
                break;
        }
        userTransactionController.recordTransaction("To Single Variable Clinical Data");
        return action;
    }

    public String toViewMySummeries() {

        listMyReports();
        userTransactionController.recordTransaction("To View My Summeries");
        return "/reports/summaries/my_summaries";
    }

    private List<Long> findEncounterIds(Date fromDate, Date toDate, Institution institution) {
        String j = "select e.id "
                + " from  ClientEncounterComponentFormSet f join f.encounter e"
                + " where e.retired<>:er"
                + " and f.retired<>:fr "
                + " and f.completed=:fc "
                + " and e.institution=:i "
                + " and e.encounterType=:t "
                + " and e.encounterDate between :fd and :td"
                + " group by e";

        Map m = new HashMap();
        m.put("i", institution);
        m.put("t", EncounterType.Clinic_Visit);
        m.put("er", true);
        m.put("fr", true);
        m.put("fc", true);
        m.put("fd", fromDate);
        m.put("td", toDate);
        List<Long> encounterIds = clientEncounterComponentFormSetFacade.findLongList(j, m);

        return encounterIds;

    }

    private List<Encounter> findEncounters(Date fromDate, Date toDate, Institution institution) {
        String j = "select e "
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

        List<Encounter> encs = encounterFacade.findByJpql(j, m);

        return encs;

    }

    private List<Client> findClients(Date fromDate, Date toDate, Institution institution) {
        String j = "select e.client "
                + " from  ClientEncounterComponentFormSet f join f.encounter e"
                + " where e.retired<>:er"
                + " and f.retired<>:fr "
                + " and f.completed=:fc "
                + " and e.institution=:i "
                + " and e.encounterType=:t "
                + " and e.encounterDate between :fd and :td"
                + " order by e.client.id";

        Map m = new HashMap();
        m.put("i", institution);
        m.put("t", EncounterType.Clinic_Visit);
        m.put("er", true);
        m.put("fr", true);
        m.put("fc", true);
        m.put("fd", fromDate);
        m.put("td", toDate);
        List<Client> encounterIds = clientFacade.findLongList(j, m);

        return encounterIds;

    }

    private List<ClientEncounterComponentItem> findClientEncounterComponentItems(Date fromDate, Date toDate, Institution institution, String sex) {
        Map m = new HashMap();
        String j = "select i "
                + " from  ClientEncounterComponentItem i join i.parentComponent.parentComponent f join f.encounter e"
                + " where e.retired<>:er"
                + " and f.retired<>:fr "
                + " and f.completed=:fc "
                + " and e.institution=:i "
                + " and e.encounterType=:t "
                + " and e.encounterDate between :fd and :td";

        if (sex != null) {
            j += " and e.client.person.sex.code=:sex";
            m.put("sex", sex);
        }
        j += " order by e.client.id";

        m.put("i", institution);
        m.put("t", EncounterType.Clinic_Visit);
        m.put("er", true);
        m.put("fr", true);
        m.put("fc", true);
        m.put("fd", fromDate);
        m.put("td", toDate);
        List<ClientEncounterComponentItem> encounterIds = clientEncounterComponentItemFacade.findLongList(j, m);

        return encounterIds;

    }

    private List<ClientEncounterComponentItem> ClientEncounterComponentFormItems(Encounter e) {
        Map m = new HashMap();
        String j = "select i "
                + " from  ClientEncounterComponentItem i "
                + " join i.parentComponent.parentComponent f "
                + " join f.encounter e"
                + " where e=:e";
        m.put("e", e);

        List<ClientEncounterComponentItem> encounterIds = clientEncounterComponentItemFacade.findLongList(j, m);

        return encounterIds;

    }

    public void downloadAllData() {

        //1. Create an Excel file
        WritableWorkbook myFirstWbook = null;
        File newFile = new File("/tmp/" + getWebUserController().getLoggedUser().getInstitution().getName() + (new Date()).getTime() + ".xls");
        try {

            myFirstWbook = Workbook.createWorkbook(newFile);

            // create an Excel sheet
            WritableSheet excelSheet = myFirstWbook.createSheet("Sheet 1", 0);

            int colNo = 0;
            int rowNo = 0;
            int writeStartRow = 1;

            Label label = new Label(colNo, rowNo, "Test MDGPHM");
            excelSheet.addCell(label);
            colNo++;

            rowNo = 1;
            label = new Label(colNo, rowNo, "MDGPHM");
            excelSheet.addCell(label);
            colNo++;

            mergingMessage = "Writing to file ";
            myFirstWbook.write();

        } catch (IOException | WriteException e) {
            mergingMessage = "Error - " + e.getMessage();
        } finally {

            if (myFirstWbook != null) {
                try {
                    myFirstWbook.close();
                    mergingMessage = "Closing File.";
                } catch (IOException | WriteException e) {
                    mergingMessage = "Error - " + e.getMessage();
                }
            }

        }

        mergingMessage = "Ready for Download";
        InputStream stream;
        try {
            stream = new FileInputStream(newFile);
//            file = new DefaultStreamedContent(stream, "application/xls", newFile.getAbsolutePath());
        } catch (FileNotFoundException ex) {
            mergingMessage = "Error - " + ex.getMessage();
        }
        mergingMessage = "";

    }

    public void toDownloadNcdReportThisMonthInstitution() {
        ReportTimePeriod rtp = new ReportTimePeriod();
        rtp.setTimePeriodType(TimePeriodType.Monthly);
        rtp.setFrom(CommonController.startOfTheMonth());
        rtp.setTo(CommonController.endOfTheMonth());
        rtp.setYear(CommonController.getYear());
        rtp.setMonth(CommonController.getMonth());
        toDownloadNcdReport(institution, rtp);
    }

    public void toDownloadNcdReport(Institution ins, ReportTimePeriod rtp) {

        if (queryComponent == null) {
            JsfUtil.addErrorMessage("Please select the report");
            return;
        }

        if (queryComponent.getQueryType() == null) {
            JsfUtil.addErrorMessage("No type for the Query.");
            return;
        }

        String j = "select u from Upload u "
                + " where u.component=:c";
        Map m = new HashMap();
        m.put("c", queryComponent);

        Upload upload = getUploadFacade().findFirstByJpql(j, m);
        if (upload == null) {
            JsfUtil.addErrorMessage("No file is available for seelcted summery");
            return;
        }

        List<Encounter> encs = null;
        List<Client> clnts = null;

        switch (queryComponent.getQueryType()) {
            case Encounter_Count:
                encs = findEncounters(rtp.getFrom(), rtp.getTo(), ins);
                break;
            case Client_Count:
                JsfUtil.addErrorMessage("Under Development");
                return;
            default:
                JsfUtil.addErrorMessage("Under Development");
                return;
        }

        if (encs == null) {
            JsfUtil.addErrorMessage("No results");
            return;
        } else if (encs.size() < 1) {

            JsfUtil.addErrorMessage("No results");
            return;
        }

        String FILE_NAME = upload.getFileName() + "_" + (new Date()) + ".xlsx";

        File newFile = new File(FILE_NAME);

        try {
            FileUtils.writeByteArrayToFile(newFile, upload.getBaImage());
        } catch (IOException ex) {
        }

        XSSFWorkbook workbook;
        XSSFSheet sheet;

        try {

            FileInputStream excelFile = new FileInputStream(newFile);
            workbook = new XSSFWorkbook(excelFile);
            sheet = workbook.getSheetAt(0);
            XSSFSheet sheet2 = workbook.createSheet("Test Sheet CHIMS");

            Iterator<Row> iterator = sheet.iterator();

            while (iterator.hasNext()) {

                Row currentRow = iterator.next();
                Iterator<Cell> cellIterator = currentRow.iterator();

                while (cellIterator.hasNext()) {

                    Cell currentCell = cellIterator.next();

                    String cellString = "";

                    switch (currentCell.getCellType()) {
                        case STRING:
                            cellString = currentCell.getStringCellValue();
                            break;
                        case BLANK:
                        case BOOLEAN:
                        case ERROR:
                        case FORMULA:
                        case NUMERIC:
                        case _NONE:

                            continue;
                    }

                    if (cellString.contains("#{")) {
                        Long temLong = findReplaceblesInCalculationString(cellString, encs);
                        if (temLong != null) {
                            currentCell.setCellValue(temLong);
                        } else {

                        }
                    }

                }

                excelFile.close();

                FileOutputStream out = new FileOutputStream(FILE_NAME);
                workbook.write(out);
                out.close();

                InputStream stream;
                stream = new FileInputStream(newFile);
//                file = new DefaultStreamedContent(stream, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", FILE_NAME);

            }
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }

    }

    public Long findReplaceblesInCalculationString(String text, List<Encounter> ens) {

        Long l = 0l;

        if (ens == null) {

            return l;
        }
        if (ens.isEmpty()) {

            l = 0l;
            return l;
        }

        List<Replaceable> ss = new ArrayList<>();

        String patternStart = "#{";
        String patternEnd = "}";
        String regexString = Pattern.quote(patternStart) + "(.*?)" + Pattern.quote(patternEnd);

        Pattern p = Pattern.compile(regexString);
        Matcher m = p.matcher(text);

        while (m.find()) {
            String block = m.group(1);

            QueryComponent qc = getQueryComponentController().findByCode(block);
            if (qc == null) {

                l = null;
                return l;

            } else {

                if (qc.getQueryType() == QueryType.Encounter_Count) {
                    List<QueryComponent> criteria = getQueryComponentController().criteria(qc);

                    if (criteria == null || criteria.isEmpty()) {
                        l = Long.valueOf(ens.size());
                        return l;
                    } else {
                        l = findMatchingCount(ens, criteria);
                    }

                } else {
                    l = null;
                    return l;
                }
            }

        }

        return l;

    }

    public boolean matchQuery(QueryComponent q, ClientEncounterComponentItem qi) {
        if (q.getItem() == null) {
            return false;
        }
        if (qi.getItem() == null) {
            return false;
        }
        if (!qi.getItem().getCode().equalsIgnoreCase(q.getItem().getCode())) {
            return false;
        }

        boolean m = false;
        Integer int1 = null;
        Integer int2 = null;
        Double real1 = null;
        Double real2 = null;
        Long lng1 = null;
        Long lng2 = null;
        Item itemVariable = null;
        Item itemValue = null;

        if (q.getMatchType() == QueryCriteriaMatchType.Variable_Value_Check || q.getMatchType() == QueryCriteriaMatchType.Variable_Range_check) {

            switch (q.getQueryDataType()) {
                case integer:
                    int1 = q.getIntegerNumberValue();
                    int2 = q.getIntegerNumberValue2();
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

            }
            switch (q.getEvaluationType()) {
                case Equal:
                    if (int1 != null) {
                        m = int1.equals(qi.getIntegerNumberValue());
                    }
                    if (lng1 != null) {
                        m = lng1.equals(qi.getLongNumberValue());
                    }
                    if (real1 != null) {
                        m = real1.equals(qi.getRealNumberValue());
                    }

                    if (itemValue != null && itemVariable != null) {

                        if (itemValue.getCode().equals(qi.getItemValue().getCode())) {
                            m = true;
                        }
                    }
                    break;
                case Less_than:
                    if (int1 != null && qi.getIntegerNumberValue() != null) {
                        m = qi.getIntegerNumberValue() < int1;
                    }
                    if (lng1 != null && qi.getLongNumberValue() != null) {
                        m = qi.getLongNumberValue() < lng1;
                    }
                    if (real1 != null && qi.getRealNumberValue() != null) {
                        m = qi.getRealNumberValue() < real1;
                    }

                case Between:
                    if (int1 != null && int2 != null && qi.getIntegerNumberValue() != null) {
                        if (int1 > int2) {
                            Integer intTem = int1;
                            intTem = int1;
                            int1 = int2;
                            int2 = intTem;
                        }
                        if (qi.getIntegerNumberValue() > int1 && qi.getIntegerNumberValue() < int2) {
                            m = true;
                        }
                    }
                    if (lng1 != null && lng2 != null && qi.getLongNumberValue() != null) {
                        if (lng1 > lng2) {
                            Long intTem = lng1;
                            intTem = lng1;
                            lng1 = lng2;
                            lng2 = intTem;
                        }
                        if (qi.getLongNumberValue() > lng1 && qi.getLongNumberValue() < lng2) {
                            m = true;
                        }
                    }
                    if (real1 != null && real2 != null && qi.getRealNumberValue() != null) {
                        if (real1 > real2) {
                            Double realTem = real1;
                            realTem = real1;
                            real1 = real2;
                            real2 = realTem;
                        }
                        if (qi.getRealNumberValue() > real1 && qi.getRealNumberValue() < real2) {
                            m = true;
                        }
                    }

                case Grater_than:
                    if (int1 != null && qi.getIntegerNumberValue() != null) {
                        m = qi.getIntegerNumberValue() > int1;
                    }
                    if (real1 != null && qi.getRealNumberValue() != null) {
                        m = qi.getRealNumberValue() > real1;
                    }

                case Grater_than_or_equal:
                    if (int1 != null && qi.getIntegerNumberValue() != null) {
                        m = qi.getIntegerNumberValue() < int1;
                    }
                    if (real1 != null && qi.getRealNumberValue() != null) {
                        m = qi.getRealNumberValue() < real1;
                    }
                case Less_than_or_equal:
                    if (int1 != null && qi.getIntegerNumberValue() != null) {
                        m = qi.getIntegerNumberValue() >= int1;
                    }
                    if (real1 != null && qi.getRealNumberValue() != null) {
                        m = qi.getRealNumberValue() >= real1;
                    }
            }
        }

        return m;
    }

    public Long findMatchingCount(List<Encounter> encs, List<QueryComponent> qrys) {

        Long c = 0l;
        for (Encounter e : encs) {
            List<ClientEncounterComponentItem> is = clientEncounterComponentItemController.findClientEncounterComponentItems(e);
            boolean suitableForInclusion = true;
            for (QueryComponent q : qrys) {
                boolean thisMatchOk = false;
                for (ClientEncounterComponentItem i : is) {
                    if (matchQuery(q, i)) {
                        thisMatchOk = true;
                    }
                }
                if (!thisMatchOk) {
                    suitableForInclusion = false;
                }
            }
            if (suitableForInclusion) {
                c++;
            }
        }
        return c;
    }

    public void toDownloadNcdReportLastMonthInstitution() {
        ReportTimePeriod rtp = new ReportTimePeriod();
        rtp.setTimePeriodType(TimePeriodType.Monthly);
        rtp.setFrom(CommonController.startOfTheLastMonth());
        rtp.setTo(CommonController.endOfTheLastMonth());
        rtp.setYear(CommonController.getYear(CommonController.endOfTheLastMonth()));
        rtp.setMonth(CommonController.getMonth(CommonController.endOfTheLastMonth()));
        toDownloadNcdReport(institution, rtp);
    }

    public void toDownloadNcdReportThisQuarterInstitution() {
        ReportTimePeriod rtp = new ReportTimePeriod();
        rtp.setTimePeriodType(TimePeriodType.Quarterly);
        rtp.setFrom(CommonController.startOfQuarter());
        rtp.setTo(CommonController.endOfQuarter());
        rtp.setYear(CommonController.getYear());
        rtp.setQuarter(CommonController.getQuarter());
        toDownloadNcdReport(institution, rtp);
    }

    public void toDownloadNcdReportLastQuarterInstitution() {
        ReportTimePeriod rtp = new ReportTimePeriod();
        rtp.setTimePeriodType(TimePeriodType.Quarterly);
        rtp.setFrom(CommonController.startOfTheLastQuarter());

        rtp.setTo(CommonController.endOfTheLastQuarter());
        rtp.setYear(CommonController.getYear(CommonController.startOfTheLastQuarter()));
        rtp.setQuarter(CommonController.getQuarter(CommonController.startOfTheLastQuarter()));

        toDownloadNcdReport(institution, rtp);
    }

    public String toExcelReports() {
        return "/reports/excel/index";
    }

    public String toViewClientRegistrationsByInstitution() {
        encounters = new ArrayList<>();
        String forSys = "/reports/client_registrations/for_system_by_ins";
        String forIns = "/reports/client_registrations/for_ins_by_ins";
        String forMe = "/reports/client_registrations/for_me_by_ins";
        String forClient = "/reports/client_registrations/for_clients";
        String noAction = "";
        String action = "";
        switch (webUserController.getLoggedUser().getWebUserRole()) {
            case Client:
                action = forClient;
                break;
            case Doctor:
            case Institution_Administrator:
            case Institution_Super_User:
            case Institution_User:
            case Nurse:
            case Midwife:
                action = forIns;
                break;
            case Me_Admin:
            case Me_Super_User:
                action = forMe;
                break;
            case Me_User:
            case User:
                action = noAction;
                break;
            case Super_User:
            case System_Administrator:
                action = forSys;
                break;
        }
        userTransactionController.recordTransaction("To View Client Registrations By Institution");
        return action;
    }

    public String toViewClinicVisitsByInstitution() {
        encounters = new ArrayList<>();
        String forSys = "/reports/clinic_visits/for_ins_by_ins";
        String forIns = "/reports/clinic_visits/for_ins_by_ins";
        String forMe = "/reports/clinic_visits/for_ins_by_ins";
        String forClient = "/reports/clinic_visits/for_ins_by_ins";
        String noAction = "";
        String action = "";
        switch (webUserController.getLoggedUser().getWebUserRole()) {
            case Client:
                action = forClient;
                break;
            case Doctor:
            case Institution_Administrator:
            case Institution_Super_User:
            case Institution_User:
            case Nurse:
            case Midwife:
                action = forIns;
                break;
            case Me_Admin:
            case Me_Super_User:
                action = forMe;
                break;
            case Me_User:
            case User:
                action = noAction;
                break;
            case Super_User:
            case System_Administrator:
                action = forSys;
                break;
        }
        userTransactionController.recordTransaction("To View Clinic Visits By Institution");
        return action;
    }

    public String toViewFormsetCountsByInstitution() {
        encounters = new ArrayList<>();
        String forSys = "/national/reports/institution_vice_formset_counts";
        String forIns = "/hospital/reports/formset_counts";
        String forMe = "/national/reports/institution_vice_formset_counts";
        String forClient = "";
        String noAction = "";
        String action = "";
        switch (webUserController.getLoggedUser().getWebUserRole()) {
            case Client:
                action = forClient;
                break;
            case Doctor:
            case Institution_Administrator:
            case Institution_Super_User:
            case Institution_User:
            case Nurse:
            case Midwife:
                action = forIns;
                break;
            case Me_Admin:
            case Me_Super_User:
                action = forMe;
                break;
            case Me_User:
            case User:
                action = noAction;
                break;
            case Super_User:
            case System_Administrator:
                action = forSys;
                break;
        }
        return action;
    }

    public String toViewDailyClinicsVisitCounts() {
        encounters = new ArrayList<>();
        String forSys = "/reports/clinic_visits/for_sa_daily";
        String forIns = "/reports/clinic_visits/for_ia_daily";
        String forMe = "/reports/clinic_visits/for_me_daily";
        String forClient = "/reports/index";
        String noAction = "";
        String action = "";
        switch (webUserController.getLoggedUser().getWebUserRole()) {
            case Client:
                action = forClient;
                break;
            case Doctor:
            case Institution_Administrator:
            case Institution_Super_User:
            case Institution_User:
            case Nurse:
            case Midwife:
                action = forIns;
                break;
            case Me_Admin:
            case Me_Super_User:
                action = forMe;
                break;
            case Me_User:
            case User:
                action = noAction;
                break;
            case Super_User:
            case System_Administrator:
                action = forSys;
                break;
        }
        userTransactionController.recordTransaction("To View Daily Clinic Visits");
        return action;
    }

    public String toFunctioningHLcs() {
        encounters = new ArrayList<>();
        String forSys = "/reports/clinic_visits/for_sa_functioning_hlcs";
        String forIns = "/reports/clinic_visits/for_ia_functioning_hlcs";
        String forMe = "/reports/clinic_visits/for_me_functioning_hlcs";
        String forClient = "";
        String noAction = "";
        String action = "";
        switch (webUserController.getLoggedUser().getWebUserRole()) {
            case Client:
                action = forClient;
                break;
            case Doctor:
            case Institution_Administrator:
            case Institution_Super_User:
            case Institution_User:
            case Nurse:
            case Midwife:
                action = forIns;
                break;
            case Me_Admin:
            case Me_Super_User:
                action = forMe;
                break;
            case Me_User:
            case User:
                action = noAction;
                break;
            case Super_User:
            case System_Administrator:
                action = forSys;
                break;
        }
        return action;
    }

    public String toViewDailyClinicsRegistrationCounts() {
        encounters = new ArrayList<>();
        String forSys = "/reports/client_registrations/for_sa_daily";
        String forIns = "/reports/client_registrations/for_ia_daily";
        String forMe = "/reports/client_registrations/for_me_daily";
        String forClient = "/reports/index";
        String noAction = "";
        String action = "";
        switch (webUserController.getLoggedUser().getWebUserRole()) {
            case Client:
                action = forClient;
                break;
            case Doctor:
            case Institution_Administrator:
            case Institution_Super_User:
            case Institution_User:
            case Nurse:
            case Midwife:
                action = forIns;
                break;
            case Me_Admin:
            case Me_Super_User:
                action = forMe;
                break;
            case Me_User:
            case User:
                action = noAction;
                break;
            case Super_User:
            case System_Administrator:
                action = forSys;
                break;
        }
        userTransactionController.recordTransaction("To View Daily Clinic Visits");
        return action;
    }

    public String toViewClientRegistrationsByDistrict() {
        areaCounts = null;
        areaRepCount = null;
        return "/reports/client_registrations/for_system_by_dis";
    }

    public String toViewClientRegistrationsByProvince() {
        areaCounts = null;
        areaRepCount = null;
        return "/reports/client_registrations/for_system_by_pro";
    }

    public String toViewClientRegistrations() {
        encounters = new ArrayList<>();
        String forSys = "/reports/client_registrations/for_system";
        String forIns = "/reports/client_registrations/for_ins";
        String forMe = "/reports/client_registrations/for_me";
        String forClient = "/reports/client_registrations/for_clients";
        String noAction = "";
        String action = "";
        switch (webUserController.getLoggedUser().getWebUserRole()) {
            case Client:
                action = forClient;
                break;
            case Doctor:
            case Institution_Administrator:
            case Institution_Super_User:
            case Institution_User:
            case Nurse:
            case Midwife:
                action = forIns;
                break;
            case Me_Admin:
            case Me_Super_User:
                action = forMe;
                break;
            case Me_User:
            case User:
                action = noAction;
                break;
            case Super_User:
            case System_Administrator:
                action = forSys;
                break;
        }
        userTransactionController.recordTransaction("To View Client Registrations");
        return action;
    }

    public String toViewClinicEnrollments() {
        encounters = new ArrayList<>();
        String forSys = "/reports/clinic_enrollments/for_system";
        String forIns = "/reports/clinic_enrollments/for_ins";
        String forMe = "/reports/clinic_enrollments/for_me";
        String forClient = "/reports/clinic_enrollments/for_clients";
        String noAction = "";
        String action = "";
        switch (webUserController.getLoggedUser().getWebUserRole()) {
            case Client:
                action = forClient;
                break;
            case Doctor:
            case Institution_Administrator:
            case Institution_Super_User:
            case Institution_User:
            case Nurse:
            case Midwife:
                action = forIns;
                break;
            case Me_Admin:
            case Me_Super_User:
                action = forMe;
                break;
            case Me_User:
            case User:
                action = noAction;
                break;
            case Super_User:
            case System_Administrator:
                action = forSys;
                break;
        }
        userTransactionController.recordTransaction("To View Clinic Enrollments");
        return action;
    }

    public String toViewClinicVisits() {
        encounters = new ArrayList<>();
        String forSys = "/reports/clinic_visits/for_system";
        String forIns = "/reports/clinic_visits/for_ins";
        String forMe = "/reports/clinic_visits/for_me";
        String forClient = "/reports/clinic_visits/for_clients";
        String noAction = "";
        String action = "";
        switch (webUserController.getLoggedUser().getWebUserRole()) {
            case Client:
                action = forClient;
                break;
            case Doctor:
            case Institution_Administrator:
            case Institution_Super_User:
            case Institution_User:
            case Nurse:
            case Midwife:
                action = forIns;
                break;
            case Me_Admin:
            case Me_Super_User:
                action = forMe;
                break;
            case Me_User:
            case User:
                action = noAction;
                break;
            case Super_User:
            case System_Administrator:
                action = forSys;
                break;
        }
        userTransactionController.recordTransaction("To View Clinic Visits");
        return action;
    }

    public String toViewformsetList() {
        encounters = new ArrayList<>();
        String forSys = "/national/reports/institution_vice_formset_list";
        String forIns = "/hospital/reports/formset_list";
        String forMe = "/national/reports/institution_vice_formset_list";
        String forClient = "";
        String noAction = "";
        String action = "";
        switch (webUserController.getLoggedUser().getWebUserRole()) {
            case Client:
                action = forClient;
                break;
            case Doctor:
            case Institution_Administrator:
            case Institution_Super_User:
            case Institution_User:
            case Nurse:
            case Midwife:
                action = forIns;
                break;
            case Me_Admin:
            case Me_Super_User:
                action = forMe;
                break;
            case Me_User:
            case User:
                action = noAction;
                break;
            case Super_User:
            case System_Administrator:
                action = forSys;
                break;
        }
        return action;
    }

    public String toViewLongitudinalClinicVisits() {
        encounters = new ArrayList<>();
        String forSys = "/reports/clinic_visits/for_system_longitidunal_clinic_visits";
        String forIns = "/reports/data_forms/for_ins";
        String forMe = "/reports/data_forms/for_me";
        String forClient = "/reports/data_forms/for_clients";
        String noAction = "";
        String action = "";
        switch (webUserController.getLoggedUser().getWebUserRole()) {
            case Client:
                action = forClient;
                break;
            case Doctor:
            case Institution_Administrator:
            case Institution_Super_User:
            case Institution_User:
            case Nurse:
            case Midwife:
                action = forIns;
                break;
            case Me_Admin:
            case Me_Super_User:
                action = forMe;
                break;
            case Me_User:
            case User:
                action = noAction;
                break;
            case Super_User:
            case System_Administrator:
                action = forSys;
                break;
        }
        userTransactionController.recordTransaction("To View Longitidunal Clinic Visits");
        return action;
    }

    public String toViewAllClientsAndAllClinicVisits() {
        encounters = new ArrayList<>();
        String forSys = "/reports/clinic_visits/for_system_all_clients_and_all_clinic_visits";
        String forIns = "/reports/data_forms/for_ins";
        String forMe = "/reports/data_forms/for_me";
        String forClient = "/reports/data_forms/for_clients";
        String noAction = "";
        String action = "";
        switch (webUserController.getLoggedUser().getWebUserRole()) {
            case Client:
                action = forClient;
                break;
            case Doctor:
            case Institution_Administrator:
            case Institution_Super_User:
            case Institution_User:
            case Nurse:
            case Midwife:
                action = forIns;
                break;
            case Me_Admin:
            case Me_Super_User:
                action = forMe;
                break;
            case Me_User:
            case User:
                action = noAction;
                break;
            case Super_User:
            case System_Administrator:
                action = forSys;
                break;
        }
        userTransactionController.recordTransaction("To All Clients and all Clinic Visits");
        return action;
    }

    public String toViewDataForms() {
        encounters = new ArrayList<>();
        String forSys = "/reports/data_forms/for_system";
        String forIns = "/reports/data_forms/for_ins";
        String forMe = "/reports/data_forms/for_me";
        String forClient = "/reports/data_forms/for_clients";
        String noAction = "";
        String action = "";
        switch (webUserController.getLoggedUser().getWebUserRole()) {
            case Client:
                action = forClient;
                break;
            case Doctor:
            case Institution_Administrator:
            case Institution_Super_User:
            case Institution_User:
            case Nurse:
            case Midwife:
                action = forIns;
                break;
            case Me_Admin:
            case Me_Super_User:
                action = forMe;
                break;
            case Me_User:
            case User:
                action = noAction;
                break;
            case Super_User:
            case System_Administrator:
                action = forSys;
                break;
        }
        userTransactionController.recordTransaction("To View Data Forms");
        return action;
    }

// </editor-fold>   
// <editor-fold defaultstate="collapsed" desc="Functions">
    public void fillClientRegistrationForSysAdmin() {
        String j;
        Map m = new HashMap();
        j = "select c from Client c "
                + " where c.retired=:ret "
                + " and c.reservedClient<>:res "
                + " and c.createdAt between :fd and :td ";
        m.put("ret", false);
        m.put("res", true);
        m.put("fd", fromDate);
        m.put("td", toDate);
        if (institution != null) {
            j += " and c.createInstitution in :ins ";
            List<Institution> ins = institutionApplicationController.findChildrenInstitutions(institution);
            ins.add(institution);
            m.put("ins", ins);
        }

        clients = clientController.getItems(j, m);
    }

    public void downloadClientRegistrations() {
        String j;
        Map m = new HashMap();

        j = "select c "
                + " from Client c "
                + " where (c.retired=:ret or c.retired is null) "
                + " and (c.reservedClient=:res or c.reservedClient is null) "
                + " and c.createdAt between :fd and :td ";
        m.put("ret", false);
        m.put("res", false);
        m.put("fd", getFromDate());
        m.put("td", getToDate());

        if (institution != null) {
            j += " and c.createInstitution in :ins ";
            List<Institution> ins = institutionApplicationController.findChildrenInstitutions(institution);
            ins.add(institution);
            m.put("ins", ins);
        } else {
            if (webUserController.getLoggedUser().isRestrictedToInstitution()) {
                j += " and c.createInstitution in :ins ";
                List<Institution> ins = webUserController.getLoggableInstitutions();
                m.put("ins", ins);
            }
        }

        List<Client> tmpClients = getClientFacade().findByJpql(j, m);

        String FILE_NAME = "client_registrations" + "_" + (new Date()) + ".xlsx";
        String mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        String folder = "/tmp/";

        File newFile = new File(folder + FILE_NAME);

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Data");

        int rowCount = 0;

        Row t1 = sheet.createRow(rowCount++);
        Cell th1_lbl = t1.createCell(0);
        th1_lbl.setCellValue("Report");
        Cell th1_val = t1.createCell(1);
        th1_val.setCellValue("List of Clients");

        Row t2 = sheet.createRow(rowCount++);
        Cell th2_lbl = t2.createCell(0);
        th2_lbl.setCellValue("From");
        Cell th2_val = t2.createCell(1);
        th2_val.setCellValue(CommonController.dateTimeToString(fromDate, "dd MMMM yyyy"));

        Row t3 = sheet.createRow(rowCount++);
        Cell th3_lbl = t3.createCell(0);
        th3_lbl.setCellValue("To");
        Cell th3_val = t3.createCell(1);
        th3_val.setCellValue(CommonController.dateTimeToString(toDate, "dd MMMM yyyy"));

        if (institution != null) {
            Row t4 = sheet.createRow(rowCount++);
            Cell th4_lbl = t4.createCell(0);
            th4_lbl.setCellValue("Institution");
            Cell th4_val = t4.createCell(1);
            th4_val.setCellValue(institution.getName());
        }

        rowCount++;

        Row t5 = sheet.createRow(rowCount);
        Cell th5_1 = t5.createCell(0);
        th5_1.setCellValue("Serial");

        Cell th5_2 = t5.createCell(1);
        th5_2.setCellValue("PHN");

        Cell th5_3 = t5.createCell(2);
        th5_3.setCellValue("Name");

        Cell th5_4 = t5.createCell(3);
        th5_4.setCellValue("NIC");

        Cell th5_5 = t5.createCell(4);
        th5_5.setCellValue("Birthday");

        Cell th5_6 = t5.createCell(5);
        th5_6.setCellValue("Age(yrs)");

        Cell th5_7 = t5.createCell(6);
        th5_7.setCellValue("Sex");

        Cell th5_8 = t5.createCell(7);
        th5_8.setCellValue("Address");

        Cell th5_9 = t5.createCell(8);
        th5_9.setCellValue("GN Areas");

        Cell th5_10 = t5.createCell(9);
        th5_10.setCellValue("Phone");

        if (institution == null) {
            Cell th5_11 = t5.createCell(10);
            th5_11.setCellValue("Institution");
        }

        Cell th5_12 = t5.createCell(11);
        th5_12.setCellValue("Created date");

        int serial = 1;

        CellStyle cellStyle = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/MMMM/yyyy hh:mm"));

        for (Client o : tmpClients) {

            Row row = sheet.createRow(++rowCount);

            Cell c1 = row.createCell(0);
            c1.setCellValue(serial);

            Cell c2 = row.createCell(1);
            c2.setCellValue(o.getPhn());

            if (o.getPerson() == null) {
                continue;
            }

            Cell c3 = row.createCell(2);
            c3.setCellValue(o.getPerson().getName());

            Cell c4 = row.createCell(3);
            c4.setCellValue(o.getPerson().getNic());

            Cell c5 = row.createCell(4);
            c5.setCellValue(o.getPerson().getDateOfBirth());

            Cell c6 = row.createCell(5);
            c6.setCellValue(o.getPerson().getAge());

            Cell c7 = row.createCell(6);
            if (o.getPerson().getSex() != null) {
                c7.setCellValue(o.getPerson().getSex().getName());
            }

            Cell c8 = row.createCell(7);
            c8.setCellValue(o.getPerson().getAddress());

            Cell c9 = row.createCell(8);
            if (o.getPerson().getGnArea() != null) {
                c9.setCellValue(o.getPerson().getGnArea().getName());
            }

            Cell c10 = row.createCell(9);
            c10.setCellValue(o.getPerson().getPhone1());

            if (institution == null) {
                Cell c11 = row.createCell(10);
                c11.setCellValue(o.getCreateInstitution().getName());
            }

            Cell c11 = row.createCell(11);
            c11.setCellValue(o.getCreatedAt());
            c11.setCellStyle(cellStyle);

            serial++;

        }

        try (FileOutputStream outputStream = new FileOutputStream(newFile)) {
            workbook.write(outputStream);
        } catch (Exception e) {

        }

        InputStream stream;
        try {
            stream = new FileInputStream(newFile);
            resultExcelFile = streamedContentController.generateStreamedContent(mimeType, FILE_NAME, stream);
        } catch (FileNotFoundException ex) {
            // //System.out.println("File not found exception -->" + ex.getMessage());
        }
    }

    public void fillRegistrationsOfClientsByInstitution() {

        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.createInstitution, count(c)) "
                + " from Client c "
                + " where c.retired<>:ret "
                + " and c.reservedClient<>:res ";
        Map m = new HashMap();
        m.put("ret", true);
        m.put("res", true);
        j = j + " and c.createdAt between :fd and :td ";

        if (webUserController.getLoggedUser().isRestrictedToInstitution()) {
            j = j + " and c.createInstitution in :ins ";
            m.put("ins", webUserController.getLoggableInstitutions());
        }

        j = j + " group by c.createInstitution ";
        j = j + " order by c.createInstitution.name ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        List<Object> objs = getClientFacade().findAggregates(j, m);
        institutionCounts = new ArrayList<>();
        reportCount = 0l;
        for (Object o : objs) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                institutionCounts.add(ic);
                reportCount += ic.getCount();
            }
        }
        userTransactionController.recordTransaction("Fill Registrations Of Clients By Institution");
    }

    public void fillClinicVisitsByInstitution() {

        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(e.institution, count(e)) "
                + " from Encounter e "
                + " where e.retired<>:ret "
                + " and e.encounterType=:et ";
        Map m = new HashMap();
        m.put("ret", true);
        m.put("et", EncounterType.Clinic_Visit);
        j = j + " and e.encounterDate between :fd and :td ";

        j = j + " group by e.institution ";
        j = j + " order by e.institution.name ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        List<Object> objs = getClientFacade().findAggregates(j, m);
        institutionCounts = new ArrayList<>();
        reportCount = 0l;
        for (Object o : objs) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                institutionCounts.add(ic);
                reportCount += ic.getCount();
            }
        }
        userTransactionController.recordTransaction("Fill Clinic Visits By Institution");
    }

    public void fillFormSetCountsByInstitution() {

        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.institution, count(c)) "
                + " from ClientEncounterComponentFormSet c "
                + " where c.retired<>:ret "
                + " and c.referenceComponent=:et ";
        Map m = new HashMap();
        m.put("ret", true);
        m.put("et", formset);
        j = j + " and c.createdAt between :fd and :td ";

        j = j + " group by c.institution ";
        j = j + " order by c.institution.name ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        List<Object> objs = getClientFacade().findAggregates(j, m);
        institutionCounts = new ArrayList<>();
        reportCount = 0l;
        for (Object o : objs) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                institutionCounts.add(ic);
                reportCount += ic.getCount();
            }
        }
        userTransactionController.recordTransaction("Fill Clinic Visits By Institution");
    }

    public void fillFormSetCountsByInstitutionList() {
        String j = "select c "
                + " from ClientEncounterComponentFormSet c "
                + " where c.retired<>:ret "
                + " and c.referenceComponent=:et ";
        Map m = new HashMap();
        m.put("ret", true);
        m.put("et", formset);
        j = j + " and c.createdAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        if (institution != null) {
            j = j + " and c.institution=:ins ";
            m.put("ins", institution);
        }
        clientEncounterComponentFormSets = clientEncounterComponentFormSetFacade.findByJpql(j, m, TemporalType.TIMESTAMP);

    }

    public void fillRegistrationsOfClientsByDistrict() {

        String j = "select new lk.gov.health.phsp.pojcs.AreaCount(c.createInstitution.district, count(c)) "
                + " from Client c "
                + " where c.retired<>:ret "
                + " and c.reservedClient<>:res ";
        Map m = new HashMap();
        m.put("ret", true);
        m.put("res", true);
        j = j + " and c.createdAt between :fd and :td ";

        j = j + " group by c.createInstitution.district ";
        j = j + " order by c.createInstitution.district.name ";

        m.put("fd", getFromDate());
        m.put("td", getToDate());
        List<Object> objs = getClientFacade().findAggregates(j, m);
        areaCounts = new ArrayList<>();
        areaRepCount = 0l;
        for (Object o : objs) {
            if (o instanceof AreaCount) {
                AreaCount ic = (AreaCount) o;
                areaCounts.add(ic);
                areaRepCount += ic.getCount();
            }
        }
    }

    public void fillRegistrationsOfClientsByProvince() {

        String j = "select new lk.gov.health.phsp.pojcs.AreaCount(c.createInstitution.province, count(c)) "
                + " from Client c "
                + " where c.retired<>:ret "
                + " and c.reservedClient<>:res ";
        Map m = new HashMap();
        m.put("ret", true);
        m.put("res", true);
        j = j + " and c.createdAt between :fd and :td ";

        j = j + " group by c.createInstitution.province ";
        j = j + " order by c.createInstitution.province.name ";

        m.put("fd", getFromDate());
        m.put("td", getToDate());
        List<Object> objs = getClientFacade().findAggregates(j, m);
        areaCounts = new ArrayList<>();
        areaRepCount = 0l;
        for (Object o : objs) {
            if (o instanceof AreaCount) {
                AreaCount ic = (AreaCount) o;
                areaCounts.add(ic);
                areaRepCount += ic.getCount();
            }
        }
    }

    public void fillClinicEnrollments() {
        String j;
        Map m = new HashMap();
        j = "select c from Encounter c "
                + " where c.retired=:ret "
                + " c.encounterType=:type "
                + " and c.encounterDate between :fd and :td ";
        m.put("ret", false);
        m.put("fd", fromDate);
        m.put("td", toDate);
        m.put("type", EncounterType.Clinic_Enroll);
        if (institution != null) {
            j += " and c.institution in :ins ";
            List<Institution> ins = institutionApplicationController.findChildrenInstitutions(institution);
            ins.add(institution);
            m.put("ins", ins);
        } else {
            if (webUserController.getLoggedUser().isRestrictedToInstitution()) {
                j += " and c.institution in :ins ";
                List<Institution> ins = webUserController.getLoggableInstitutions();
                ins.add(institution);
                m.put("ins", ins);
            }
        }
        encounters = encounterController.getItems(j, m);
        userTransactionController.recordTransaction("Fill Clinic Enrollments");
    }

    public void downloadClinicEnrollments() {
        String j;
        Map m = new HashMap();

        j = "select new lk.gov.health.phsp.pojcs.EncounterBasicData("
                + "e.client.phn, "
                + "e.client.person.gnArea.name, "
                + "e.institution.name, "
                + "e.client.person.dateOfBirth, "
                + "e.encounterDate, "
                + "e.client.person.sex.name "
                + ") "
                + " from Encounter e "
                + " where e.retired=:ret "
                + " and e.encounterType=:type "
                + " and e.encounterDate between :fd and :td ";

        m.put("ret", false);
        m.put("fd", fromDate);
        m.put("td", toDate);
        m.put("type", EncounterType.Clinic_Enroll);

        if (institution != null) {
            j += " and e.institution in :ins ";
            List<Institution> ins = institutionApplicationController.findChildrenInstitutions(institution);
            ins.add(institution);
            m.put("ins", ins);
        } else {
            if (webUserController.getLoggedUser().isRestrictedToInstitution()) {
                j += " and e.institution in :ins ";
                List<Institution> ins = webUserController.getLoggableInstitutions();
                ins.add(institution);
                m.put("ins", ins);
            }
        }
        //String phn, String gnArea, String institution, Date dataOfBirth, Date encounterAt, String sex
        List<Object> objs = getClientFacade().findAggregates(j, m);

        String FILE_NAME = "client_clinic_enrolments" + "_" + (new Date()) + ".xlsx";
        String mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        String folder = "/tmp/";

        File newFile = new File(folder + FILE_NAME);

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Data");

        int rowCount = 0;

        Row t1 = sheet.createRow(rowCount++);
        Cell th1_lbl = t1.createCell(0);
        th1_lbl.setCellValue("Report");
        Cell th1_val = t1.createCell(1);
        th1_val.setCellValue("List of Clinic Enrolments");

        Row t2 = sheet.createRow(rowCount++);
        Cell th2_lbl = t2.createCell(0);
        th2_lbl.setCellValue("From");
        Cell th2_val = t2.createCell(1);
        th2_val.setCellValue(CommonController.dateTimeToString(fromDate, "dd MMMM yyyy"));

        Row t3 = sheet.createRow(rowCount++);
        Cell th3_lbl = t3.createCell(0);
        th3_lbl.setCellValue("To");
        Cell th3_val = t3.createCell(1);
        th3_val.setCellValue(CommonController.dateTimeToString(toDate, "dd MMMM yyyy"));

        if (institution != null) {
            Row t4 = sheet.createRow(rowCount++);
            Cell th4_lbl = t4.createCell(0);
            th4_lbl.setCellValue("Institution");
            Cell th4_val = t4.createCell(1);
            th4_val.setCellValue(institution.getName());
        }

        rowCount++;

        Row t5 = sheet.createRow(rowCount);
        Cell th5_1 = t5.createCell(0);
        th5_1.setCellValue("Serial");
        Cell th5_2 = t5.createCell(1);
        th5_2.setCellValue("PHN");
        Cell th5_3 = t5.createCell(2);
        th5_3.setCellValue("Sex");
        Cell th5_4 = t5.createCell(3);
        th5_4.setCellValue("Age in Years at Encounter");
        Cell th5_5 = t5.createCell(4);
        th5_5.setCellValue("Encounter at");
        Cell th5_6 = t5.createCell(5);
        th5_6.setCellValue("GN Areas");
        if (institution == null) {
            Cell th5_7 = t5.createCell(6);
            th5_7.setCellValue("Institution");
        }

        int serial = 1;

        CellStyle cellStyle = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        cellStyle.setDataFormat(
                createHelper.createDataFormat().getFormat("dd/MMMM/yyyy hh:mm"));

        for (Object o : objs) {
            if (o instanceof EncounterBasicData) {
                EncounterBasicData cbd = (EncounterBasicData) o;
                Row row = sheet.createRow(++rowCount);

                Cell c1 = row.createCell(0);
                c1.setCellValue(serial);

                Cell c2 = row.createCell(1);
                c2.setCellValue(cbd.getPhn());

                Cell c3 = row.createCell(2);
                c3.setCellValue(cbd.getSex());

                Cell c4 = row.createCell(3);
                c4.setCellValue(cbd.getAgeInYears());

                Cell c5 = row.createCell(4);
                c5.setCellValue(cbd.getEncounterAt());
                c5.setCellStyle(cellStyle);

                Cell c6 = row.createCell(5);
                c6.setCellValue(cbd.getGnArea());
                if (institution == null) {
                    Cell c7 = row.createCell(6);
                    c7.setCellValue(cbd.getInstitution());
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
            resultExcelFile = streamedContentController.generateStreamedContent(mimeType, FILE_NAME, stream);
        } catch (FileNotFoundException ex) {

        }

    }

    public void downloadClinicVisits() {
        String j;
        Map m = new HashMap();

        j = "select new lk.gov.health.phsp.pojcs.EncounterBasicData("
                + "e.client.phn, "
                + "e.client.person.gnArea.name, "
                + "e.institution.name, "
                + "e.client.person.dateOfBirth, "
                + "e.encounterDate, "
                + "e.client.person.sex.name "
                + ") "
                + " from Encounter e "
                + " where e.retired=:ret "
                + " and e.encounterType=:type "
                + " and e.encounterDate between :fd and :td ";

        m.put("ret", false);
        m.put("fd", fromDate);
        m.put("td", toDate);
        m.put("type", EncounterType.Clinic_Visit);
        if (institution != null) {
            j += " and e.institution in :ins ";
            List<Institution> ins = institutionApplicationController.findChildrenInstitutions(institution);
            ins.add(institution);
            m.put("ins", ins);
        } else {
            if (webUserController.getLoggedUser().isRestrictedToInstitution()) {
                j += " and e.institution in :ins ";
                List<Institution> ins = webUserController.getLoggableInstitutions();
                ins.add(institution);
                m.put("ins", ins);
            }
        }

        //String phn, String gnArea, String institution, Date dataOfBirth, Date encounterAt, String sex
        List<Object> objs = getClientFacade().findAggregates(j, m);

        String FILE_NAME = "client_clinic_visits" + "_" + (new Date()) + ".xlsx";
        String mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        String folder = "/tmp/";

        File newFile = new File(folder + FILE_NAME);

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Data");

        int rowCount = 0;

        Row t1 = sheet.createRow(rowCount++);
        Cell th1_lbl = t1.createCell(0);
        th1_lbl.setCellValue("Report");
        Cell th1_val = t1.createCell(1);
        th1_val.setCellValue("List of Clinic Visits");

        Row t2 = sheet.createRow(rowCount++);
        Cell th2_lbl = t2.createCell(0);
        th2_lbl.setCellValue("From");
        Cell th2_val = t2.createCell(1);
        th2_val.setCellValue(CommonController.dateTimeToString(fromDate, "dd MMMM yyyy"));

        Row t3 = sheet.createRow(rowCount++);
        Cell th3_lbl = t3.createCell(0);
        th3_lbl.setCellValue("To");
        Cell th3_val = t3.createCell(1);
        th3_val.setCellValue(CommonController.dateTimeToString(toDate, "dd MMMM yyyy"));

        if (institution != null) {
            Row t4 = sheet.createRow(rowCount++);
            Cell th4_lbl = t4.createCell(0);
            th4_lbl.setCellValue("Institution");
            Cell th4_val = t4.createCell(1);
            th4_val.setCellValue(institution.getName());
        }

        rowCount++;

        Row t5 = sheet.createRow(rowCount);
        Cell th5_1 = t5.createCell(0);
        th5_1.setCellValue("Serial");
        Cell th5_2 = t5.createCell(1);
        th5_2.setCellValue("PHN");

        Cell th5_2a = t5.createCell(2);
        th5_2a.setCellValue("Name");
        Cell th5_2b = t5.createCell(3);
        th5_2b.setCellValue("Address");
        Cell th5_2c = t5.createCell(4);
        th5_2c.setCellValue("Phone 1");
        Cell th5_2d = t5.createCell(5);
        th5_2d.setCellValue("Phone 2");
        Cell th5_2e = t5.createCell(6);
        th5_2e.setCellValue("GN");

        Cell th5_3 = t5.createCell(7);
        th5_3.setCellValue("Sex");
        Cell th5_4 = t5.createCell(8);
        th5_4.setCellValue("Age in Years at Encounter");
        Cell th5_5 = t5.createCell(9);
        th5_5.setCellValue("Encounter at");
        Cell th5_6 = t5.createCell(10);
        th5_6.setCellValue("GN Areas");
        if (institution == null) {
            Cell th5_7 = t5.createCell(11);
            th5_7.setCellValue("Institution");
        }

        int serial = 1;

        CellStyle cellStyle = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        cellStyle.setDataFormat(
                createHelper.createDataFormat().getFormat("dd/MMMM/yyyy hh:mm"));

        for (Object o : objs) {
            if (o instanceof EncounterBasicData) {
                EncounterBasicData cbd = (EncounterBasicData) o;
                Row row = sheet.createRow(++rowCount);

                Cell c1 = row.createCell(0);
                c1.setCellValue(serial);

                Cell c2 = row.createCell(1);
                c2.setCellValue(cbd.getPhn());

                Cell c2a = row.createCell(2);
                c2a.setCellValue(cbd.getName());

                Cell c2c = row.createCell(4);
                c2c.setCellValue(cbd.getPhone());

                Cell c3 = row.createCell(2);
                c3.setCellValue(cbd.getSex());

                Cell c4 = row.createCell(3);
                c4.setCellValue(cbd.getAgeInYears());

                Cell c5 = row.createCell(4);
                c5.setCellValue(cbd.getEncounterAt());
                c5.setCellStyle(cellStyle);

                Cell c6 = row.createCell(5);
                c6.setCellValue(cbd.getGnArea());
                if (institution == null) {
                    Cell c7 = row.createCell(6);
                    c7.setCellValue(cbd.getInstitution());
                }

                serial++;
            }
        }

        objs = null;
        System.gc();

        try (FileOutputStream outputStream = new FileOutputStream(newFile)) {
            workbook.write(outputStream);
        } catch (Exception e) {

        }

        InputStream stream;
        try {
            stream = new FileInputStream(newFile);
            resultExcelFile = streamedContentController.generateStreamedContent(mimeType, FILE_NAME, stream);
        } catch (FileNotFoundException ex) {

        }

    }

    private Long numberOfInstitutions(InstitutionType type) {
        String j = "select count(i) "
                + " from Institution i "
                + " where (i.retired=:f or i.retired is null) "
                + " and i.institutionType=:t";
        Map m = new HashMap();
        m.put("t", type);
        m.put("f", false);
        Long n = encounterFacade.countByJpql(j, m);
        if (n == null) {
            return 0l;
        }
        return n;
    }

    private Long numberOfHlcs(InstitutionType type) {
        String j = "select count(i) "
                + " from Institution i "
                + " where (i.retired=:f or i.retired is null) "
                + " and (i.institutionType=:hlc and i.parent.institutionType=:t)";
        Map m = new HashMap();
        m.put("t", type);
        m.put("f", false);
        m.put("hlc", InstitutionType.Clinic);
        Long n = encounterFacade.countByJpql(j, m);
        if (n == null) {
            return 0l;
        }
        return n;
    }

    private Long numberOfFunctioningHlcs(InstitutionType type) {
        String j = "select e.institution "
                + " from Encounter e "
                + " where e.retired=:ret "
                + " and e.encounterDate between :fd and :td "
                + " group by e.institution";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("fd", fromDate);
        m.put("td", toDate);
        //System.out.println("m = " + m);
        //System.out.println("j = " + j);
        List<Institution> ins = institutionFacade.findByJpql(j, m);
        //System.out.println("ins = " + ins);
        Long n = 0l;
        for (Institution i : ins) {
            //System.out.println("i = " + i.getName());
            boolean canInclude = false;
            if (i.getInstitutionType() != null) {
                if (i.getInstitutionType().equals(type)) {
                    canInclude = true;
                }
            }
            if (i.getParent() != null && i.getParent().getInstitutionType() != null) {
                if (i.getParent().getInstitutionType().equals(type)) {
                    canInclude = true;
                }
                if (i.getParent().getParent() != null && i.getParent().getParent().getInstitutionType() != null) {
                    if (i.getParent().getParent().getInstitutionType().equals(type)) {
                        canInclude = true;
                    }
                }
            }
            if (canInclude) {
                n++;
            }

        }
        return n;
    }

    public void downloadFunctioningHlcs() {

        List<InstituteTypeCounts> itCounts = new ArrayList<>();
        InstituteTypeCounts pgh = new InstituteTypeCounts();
        InstituteTypeCounts dgh = new InstituteTypeCounts();
        InstituteTypeCounts bh = new InstituteTypeCounts();
        InstituteTypeCounts dh = new InstituteTypeCounts();
        InstituteTypeCounts pmcu = new InstituteTypeCounts();
        InstituteTypeCounts eh = new InstituteTypeCounts();
        InstituteTypeCounts moh = new InstituteTypeCounts();

        pgh.setType(InstitutionType.Provincial_General_Hospital);
        pgh.setSerial(1);
        pgh.setNumber(numberOfInstitutions(InstitutionType.Provincial_General_Hospital));
        pgh.setHlcs(numberOfHlcs(InstitutionType.Provincial_General_Hospital));
        pgh.setFunctioningHlcs(numberOfFunctioningHlcs(InstitutionType.Provincial_General_Hospital));
        itCounts.add(pgh);

        dgh.setType(InstitutionType.District_General_Hospital);
        dgh.setSerial(2);
        dgh.setNumber(numberOfInstitutions(InstitutionType.District_General_Hospital));
        dgh.setHlcs(numberOfHlcs(InstitutionType.District_General_Hospital));
        dgh.setFunctioningHlcs(numberOfFunctioningHlcs(InstitutionType.District_General_Hospital));
        itCounts.add(dgh);

        bh.setType(InstitutionType.Base_Hospital);
        bh.setSerial(3);
        bh.setNumber(numberOfInstitutions(InstitutionType.Base_Hospital));
        bh.setHlcs(numberOfHlcs(InstitutionType.Base_Hospital));
        bh.setFunctioningHlcs(numberOfFunctioningHlcs(InstitutionType.Base_Hospital));
        itCounts.add(bh);

        dh.setType(InstitutionType.Divisional_Hospital);
        dh.setSerial(4);
        dh.setNumber(numberOfInstitutions(InstitutionType.Divisional_Hospital));
        dh.setHlcs(numberOfHlcs(InstitutionType.Divisional_Hospital));
        dh.setFunctioningHlcs(numberOfFunctioningHlcs(InstitutionType.Divisional_Hospital));
        itCounts.add(dh);

        pmcu.setType(InstitutionType.Primary_Medical_Care_Unit);
        pmcu.setSerial(5);
        pmcu.setNumber(numberOfInstitutions(InstitutionType.Primary_Medical_Care_Unit));
        pmcu.setHlcs(numberOfHlcs(InstitutionType.Primary_Medical_Care_Unit));
        pmcu.setFunctioningHlcs(numberOfFunctioningHlcs(InstitutionType.Primary_Medical_Care_Unit));
        itCounts.add(pmcu);

        eh.setType(InstitutionType.Estate_Hospital);
        eh.setSerial(6);
        eh.setNumber(numberOfInstitutions(InstitutionType.Estate_Hospital));
        eh.setHlcs(numberOfHlcs(InstitutionType.Estate_Hospital));
        eh.setFunctioningHlcs(numberOfFunctioningHlcs(InstitutionType.Estate_Hospital));
        itCounts.add(eh);

        moh.setType(InstitutionType.MOH_Office);
        moh.setSerial(7);
        moh.setNumber(numberOfInstitutions(InstitutionType.MOH_Office));
        moh.setHlcs(numberOfHlcs(InstitutionType.MOH_Office));
        moh.setFunctioningHlcs(numberOfFunctioningHlcs(InstitutionType.MOH_Office));
        itCounts.add(moh);

        String FILE_NAME = "functioning_clinics" + "_" + (new Date()) + ".xlsx";
        String mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        String folder = "/tmp/";

        File newFile = new File(folder + FILE_NAME);

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Data");

        int rowCount = 0;

        Row t1 = sheet.createRow(rowCount++);
        Cell th1_lbl = t1.createCell(0);
        th1_lbl.setCellValue("Report");
        Cell th1_val = t1.createCell(1);
        th1_val.setCellValue("Functioning Clinics");

        Row t2 = sheet.createRow(rowCount++);
        Cell th2_lbl = t2.createCell(0);
        th2_lbl.setCellValue("From");
        Cell th2_val = t2.createCell(1);
        th2_val.setCellValue(CommonController.dateTimeToString(fromDate, "dd MMMM yyyy"));

        Row t3 = sheet.createRow(rowCount++);
        Cell th3_lbl = t3.createCell(0);
        th3_lbl.setCellValue("To");
        Cell th3_val = t3.createCell(1);
        th3_val.setCellValue(CommonController.dateTimeToString(toDate, "dd MMMM yyyy"));

//        if (institution != null) {
//            Row t4 = sheet.createRow(rowCount++);
//            Cell th4_lbl = t4.createCell(0);
//            th4_lbl.setCellValue("Institution");
//            Cell th4_val = t4.createCell(1);
//            th4_val.setCellValue(institution.getName());
//        }
        rowCount++;

        Row t5 = sheet.createRow(rowCount);
        Cell th5_1 = t5.createCell(0);
        th5_1.setCellValue("Type");
        Cell th5_2 = t5.createCell(1);
        th5_2.setCellValue("Number of Institutions");
        Cell th5_3 = t5.createCell(2);
        th5_3.setCellValue("Number of HLCs");
        Cell th5_4 = t5.createCell(3);
        th5_4.setCellValue("Number of Functioning HLCs");

        int serial = 1;

        CellStyle cellStyle = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        cellStyle.setDataFormat(
                createHelper.createDataFormat().getFormat("dd/MMMM/yyyy hh:mm"));

        for (InstituteTypeCounts cbd : itCounts) {

            Row row = sheet.createRow(++rowCount);

            Cell c1 = row.createCell(0);
            c1.setCellValue(cbd.getType().getLabel());

            Cell c2 = row.createCell(1);
            c2.setCellValue(cbd.getNumber());

            Cell c3 = row.createCell(2);
            c3.setCellValue(cbd.getHlcs());

            Cell c4 = row.createCell(3);
            c4.setCellValue(cbd.getFunctioningHlcs());

            serial++;

        }

        itCounts = null;
        System.gc();

        try (FileOutputStream outputStream = new FileOutputStream(newFile)) {
            workbook.write(outputStream);
        } catch (Exception e) {

        }

        InputStream stream;
        try {
            stream = new FileInputStream(newFile);
            resultExcelFile = streamedContentController.generateStreamedContent(mimeType, FILE_NAME, stream);
        } catch (FileNotFoundException ex) {

        }

    }

    public void downloadDailyClinicVisitCounts() {
        String j;
        Map m = new HashMap();

        j = "select new lk.gov.health.phsp.pojcs.DateInstitutionCount("
                + "e.encounterDate, count(e)"
                + ") "
                + " from Encounter e "
                + " where e.retired=:ret "
                + " and e.encounterType=:type "
                + " and e.encounterDate between :fd and :td ";

        m.put("ret", false);
        m.put("fd", fromDate);
        m.put("td", toDate);
        m.put("type", EncounterType.Clinic_Visit);
        if (institution != null) {
            j += " and e.institution in :ins ";
            List<Institution> ins = institutionApplicationController.findChildrenInstitutions(institution);
            ins.add(institution);
            m.put("ins", ins);
        } else {
            if (webUserController.getLoggedUser().isRestrictedToInstitution()) {
                j += " and e.institution in :ins ";
                List<Institution> ins = webUserController.getLoggableInstitutions();
                ins.add(institution);
                m.put("ins", ins);
            }
        }

        j += " group by e.encounterDate "
                + " order by e.encounterDate";

        //String phn, String gnArea, String institution, Date dataOfBirth, Date encounterAt, String sex
        List<Object> objs = getClientFacade().findAggregates(j, m);

        String FILE_NAME = "clinic_visits_by_date" + "_" + (new Date()) + ".xlsx";
        String mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        String folder = "/tmp/";

        File newFile = new File(folder + FILE_NAME);

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("ClinicVisitsByDate");

        int rowCount = 0;

        Row t1 = sheet.createRow(rowCount++);
        Cell th1_lbl = t1.createCell(0);
        th1_lbl.setCellValue("Report");
        Cell th1_val = t1.createCell(1);
        th1_val.setCellValue("Clinic Visits by Date");

        Row t2 = sheet.createRow(rowCount++);
        Cell th2_lbl = t2.createCell(0);
        th2_lbl.setCellValue("From");
        Cell th2_val = t2.createCell(1);
        th2_val.setCellValue(CommonController.dateTimeToString(getFromDate(), "dd MMMM yyyy"));

        Row t3 = sheet.createRow(rowCount++);
        Cell th3_lbl = t3.createCell(0);
        th3_lbl.setCellValue("To");
        Cell th3_val = t3.createCell(1);
        th3_val.setCellValue(CommonController.dateTimeToString(getToDate(), "dd MMMM yyyy"));

        if (institution != null) {
            Row t4 = sheet.createRow(rowCount++);
            Cell th4_lbl = t4.createCell(0);
            th4_lbl.setCellValue("Institution");
            Cell th4_val = t4.createCell(1);
            th4_val.setCellValue(institution.getName());
        }

        rowCount++;

        Row t5 = sheet.createRow(rowCount);
        Cell th5_1 = t5.createCell(0);
        th5_1.setCellValue("Serial");
        Cell th5_2 = t5.createCell(1);
        th5_2.setCellValue("Date");
        Cell th5_3 = t5.createCell(2);
        th5_3.setCellValue("Clinic Visit Count");

        int serial = 1;

        CellStyle cellStyle = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        cellStyle.setDataFormat(
                createHelper.createDataFormat().getFormat("dd/MMMM/yyyy"));

        for (Object o : objs) {
            if (o instanceof DateInstitutionCount) {
                DateInstitutionCount cbd = (DateInstitutionCount) o;
                Row row = sheet.createRow(++rowCount);

                Cell c1 = row.createCell(0);
                c1.setCellValue(serial);

                Cell c2 = row.createCell(1);
                c2.setCellStyle(cellStyle);
                c2.setCellValue(cbd.getDate());

                Cell c3 = row.createCell(2);
                c3.setCellValue(cbd.getCount());

                serial++;
            }
        }

        objs = null;
        System.gc();

        try (FileOutputStream outputStream = new FileOutputStream(newFile)) {
            workbook.write(outputStream);
        } catch (Exception e) {

        }

        InputStream stream;
        try {
            stream = new FileInputStream(newFile);
            resultExcelFile = streamedContentController.generateStreamedContent(mimeType, FILE_NAME, stream);
        } catch (FileNotFoundException ex) {

        }

    }

    public void downloadDailyClientRegistrationCounts() {
        String j;
        Map m = new HashMap();

        j = "select new lk.gov.health.phsp.pojcs.DateInstitutionCount("
                + "cast(e.createdAt as LocalDate) as createdDate, count(e)"
                + ") "
                + " from Client e "
                + " where e.retired=:ret "
                + " and e.createdAt between :fd and :td ";

        m.put("ret", false);
        m.put("fd", fromDate);
        m.put("td", toDate);
        if (institution != null) {
            j += " and e.createInstitution in :ins ";
            List<Institution> ins = institutionApplicationController.findChildrenInstitutions(institution);
            ins.add(institution);
            m.put("ins", ins);
        } else {
            if (webUserController.getLoggedUser().isRestrictedToInstitution()) {
                j += " and e.createInstitution in :ins ";
                List<Institution> ins = webUserController.getLoggableInstitutions();
                ins.add(institution);
                m.put("ins", ins);
            }
        }

        j += " group by cast(e.createdAt as LocalDate)  "
                + " order by cast(e.createdAt as LocalDate)";

        //String phn, String gnArea, String institution, Date dataOfBirth, Date encounterAt, String sex
        List<Object> objs = getClientFacade().findAggregates(j, m);

        String FILE_NAME = "client_registrations_by_date" + "_" + (new Date()) + ".xlsx";
        String mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        String folder = "/tmp/";

        File newFile = new File(folder + FILE_NAME);

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("ClientRegistrationsByDate");

        int rowCount = 0;

        Row t1 = sheet.createRow(rowCount++);
        Cell th1_lbl = t1.createCell(0);
        th1_lbl.setCellValue("Report");
        Cell th1_val = t1.createCell(1);
        th1_val.setCellValue("Client Registrations by Date");

        Row t2 = sheet.createRow(rowCount++);
        Cell th2_lbl = t2.createCell(0);
        th2_lbl.setCellValue("From");
        Cell th2_val = t2.createCell(1);
        th2_val.setCellValue(CommonController.dateTimeToString(getFromDate(), "dd MMMM yyyy"));

        Row t3 = sheet.createRow(rowCount++);
        Cell th3_lbl = t3.createCell(0);
        th3_lbl.setCellValue("To");
        Cell th3_val = t3.createCell(1);
        th3_val.setCellValue(CommonController.dateTimeToString(getToDate(), "dd MMMM yyyy"));

        if (institution != null) {
            Row t4 = sheet.createRow(rowCount++);
            Cell th4_lbl = t4.createCell(0);
            th4_lbl.setCellValue("Institution");
            Cell th4_val = t4.createCell(1);
            th4_val.setCellValue(institution.getName());
        }

        rowCount++;

        Row t5 = sheet.createRow(rowCount);
        Cell th5_1 = t5.createCell(0);
        th5_1.setCellValue("Serial");
        Cell th5_2 = t5.createCell(1);
        th5_2.setCellValue("Date");
        Cell th5_3 = t5.createCell(2);
        th5_3.setCellValue("Count");

        int serial = 1;

        CellStyle cellStyle = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        cellStyle.setDataFormat(
                createHelper.createDataFormat().getFormat("dd/MMMM/yyyy"));

        for (Object o : objs) {
            if (o instanceof DateInstitutionCount) {
                DateInstitutionCount cbd = (DateInstitutionCount) o;
                Row row = sheet.createRow(++rowCount);

                Cell c1 = row.createCell(0);
                c1.setCellValue(serial);

                Cell c2 = row.createCell(1);
                c2.setCellStyle(cellStyle);
                c2.setCellValue(cbd.getDate());

                Cell c3 = row.createCell(2);
                c3.setCellValue(cbd.getCount());

                serial++;
            }
        }

        objs = null;
        System.gc();

        try (FileOutputStream outputStream = new FileOutputStream(newFile)) {
            workbook.write(outputStream);
        } catch (Exception e) {

        }

        InputStream stream;
        try {
            stream = new FileInputStream(newFile);
            resultExcelFile = streamedContentController.generateStreamedContent(mimeType, FILE_NAME, stream);
        } catch (FileNotFoundException ex) {

        }

    }

    public void downloadLongitidinalClinicVisits() {
        if (institution == null) {
            JsfUtil.addErrorMessage("Select Institution");
            return;
        }
        analysisBean.createLongitudinalVisitDates(institution,
                fromDate,
                toDate,
                webUserController.getLoggedUser());
        JsfUtil.addSuccessMessage("Process started. Check under my reports.");
    }

    public void downloadAllClientsAndAllClinicVisits() {
        if (institution == null) {
            JsfUtil.addErrorMessage("Select Institution");
            return;
        }
        analysisBean.createAllClientsAndAllClinicVisits(institution,
                webUserController.getLoggedUser());
        JsfUtil.addSuccessMessage("Process started. Check under my reports.");
    }

    public void downloadFormsetDataEntries() {
        if (institution == null) {
            JsfUtil.addErrorMessage("Select Institution");
            return;
        }
        if (designingComponentFormSet == null) {
            JsfUtil.addErrorMessage("Select Form Set");
            return;
        }
        analysisBean.createFormsetDataEntriesAndSubsequentVisitDates(institution,
                designingComponentFormSet,
                fromDate,
                toDate,
                webUserController.getLoggedUser());
        JsfUtil.addSuccessMessage("Process started. Check under my reports.");
    }

    public void createExcelFile(String fileName, List<ReportColumn> cols, List<ReportRow> rows, List<ReportCell> cells) {
        String mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        String folder = "/tmp/";
        File newFile = new File(folder + fileName);

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Data");

        int rowCount = 0;

        for (ReportRow rr : rows) {
            Row r = sheet.createRow(rowCount++);
            int colCount = 0;
            for (ReportColumn rc : cols) {
                Cell c = r.createCell(colCount++);
                for (ReportCell rcel : cells) {
                    if (rcel.getColumn().equals(rc) && rcel.getRow().equals(rr)) {
                        if (rcel.isContainsDateValue()) {
                            CellStyle cellStyle = workbook.createCellStyle();
                            CreationHelper createHelper = workbook.getCreationHelper();
                            if (rc.getDateFormat() == null || rc.getDateFormat().trim().equals("")) {
                                cellStyle.setDataFormat(
                                        createHelper.createDataFormat().getFormat("dd/MMMM/yyyy hh:mm"));
                            } else {
                                cellStyle.setDataFormat(
                                        createHelper.createDataFormat().getFormat(rc.getDateFormat()));
                            }
                            c.setCellStyle(cellStyle);
                            c.setCellValue(rcel.getDateValue());
                        } else if (rcel.isContainsDoubleValue()) {
                            c.setCellValue(rcel.getDblValue());
                        } else if (rcel.isContainsLongValue()) {
                            c.setCellValue(rcel.getLongValue().doubleValue());
                        } else if (rcel.isContainsStringValue()) {
                            c.setCellValue(rcel.getStringValue());
                        } else {
                            c.setCellValue("");
                        }
                    }
                }
            }
        }

        try (FileOutputStream outputStream = new FileOutputStream(newFile)) {
            workbook.write(outputStream);
        } catch (Exception e) {

        }

        InputStream stream;
        try {
            stream = new FileInputStream(newFile);
            resultExcelFile = streamedContentController.generateStreamedContent(mimeType, fileName, stream);
        } catch (FileNotFoundException ex) {

        }

    }

    public StreamedContent getDownloadCreatedFromReport() {
        if (downloadingFile == null) {
            JsfUtil.addErrorMessage("No Download file");
            return null;
        }
        return downloadingFile;
    }

    public void createExcelFileForSelectedStoredQueryResult() {
        if (selectedStoredQueryResult == null) {
            JsfUtil.addErrorMessage("No fiel selected");
            return;
        }
        String fileName;
        if (selectedStoredQueryResult.getName() == null || selectedStoredQueryResult.getName().trim().equals("")) {
            fileName = "downloading file.xlsx";
        } else {
            fileName = selectedStoredQueryResult.getName();
        }

        Map m;
        String j;

        j = "select col "
                + " from ReportColumn col "
                + " where col.storedQueryResult=:sqr "
                + " order by col.columnNumber";
        m = new HashMap();
        m.put("sqr", selectedStoredQueryResult);
        List<ReportColumn> cols = reportColumnFacade.findByJpql(j, m);
        if (cols == null || cols.isEmpty()) {
            JsfUtil.addErrorMessage("No Columns for report");
            return;
        }

        j = "select row "
                + " from ReportRow row "
                + " where row.storedQueryResult=:sqr "
                + " order by row.rowNumber";
        m = new HashMap();
        m.put("sqr", selectedStoredQueryResult);
        List<ReportRow> rows = reportRowFacade.findByJpql(j, m);
        if (rows == null || rows.isEmpty()) {
            JsfUtil.addErrorMessage("No Rows for report");
            return;
        }

        j = "select cell "
                + " from ReportCell cell "
                + " where cell.storedQueryResult=:sqr ";
        m = new HashMap();
        m.put("sqr", selectedStoredQueryResult);
        List<ReportCell> cells = reportCellFacade.findByJpql(j, m);
        if (cells == null || rows.isEmpty()) {
            JsfUtil.addErrorMessage("No Cells for report");
            return;
        }

        String mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        String folder = "/tmp/";
        File newFile = new File(folder + fileName);

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Data");

        int rowCount = 0;

        for (ReportRow rr : rows) {
            Row r = sheet.createRow(rowCount++);
            int colCount = 0;
            for (ReportColumn rc : cols) {
                Cell c = r.createCell(colCount++);
                for (ReportCell rcel : cells) {
                    if (rcel.getColumn() == null || rcel.getRow() == null) {
                        continue;
                    }
                    if (rcel.getColumn().equals(rc) && rcel.getRow().equals(rr)) {
                        if (rcel.isContainsDateValue()) {
                            CellStyle cellStyle = workbook.createCellStyle();
                            CreationHelper createHelper = workbook.getCreationHelper();
                            if (rc.getDateFormat() == null || rc.getDateFormat().trim().equals("")) {
                                cellStyle.setDataFormat(
                                        createHelper.createDataFormat().getFormat("dd/MMMM/yyyy hh:mm"));
                            } else {
                                cellStyle.setDataFormat(
                                        createHelper.createDataFormat().getFormat(rc.getDateFormat()));
                            }
                            c.setCellStyle(cellStyle);
                            c.setCellValue(rcel.getDateValue());
                        } else if (rcel.isContainsDoubleValue()) {
                            c.setCellValue(rcel.getDblValue());
                        } else if (rcel.isContainsLongValue()) {
                            if (rcel != null & rcel.getLongValue() != null) {
                                c.setCellValue(rcel.getLongValue());
                            }
                        } else if (rcel.isContainsStringValue()) {
                            c.setCellValue(rcel.getStringValue());
                        } else {
                            c.setCellValue("");
                        }
                    }
                }
            }
        }

        try (FileOutputStream outputStream = new FileOutputStream(newFile)) {
            workbook.write(outputStream);
        } catch (Exception e) {

        }

        InputStream stream;
        try {
            stream = new FileInputStream(newFile);
//            downloadingFile = new DefaultStreamedContent(stream, mimeType, fileName);
        } catch (FileNotFoundException ex) {

        }

    }

    public void fillClinicVisitsForSysAdmin() {
        String j;
        Map m = new HashMap();
        j = "select c from Encounter c "
                + " where c.retired=:ret "
                + " c.encounterType=:type "
                + " and c.encounterDate between :fd and :td ";
        m.put("ret", false);
        m.put("fd", fromDate);
        m.put("td", toDate);
        m.put("type", EncounterType.Clinic_Visit);
        if (institution != null) {
            j += " and c.institution in :ins ";
            List<Institution> ins = institutionApplicationController.findChildrenInstitutions(institution);
            ins.add(institution);
            m.put("ins", ins);
        }
        encounters = encounterController.getItems(j, m);
    }

    public void fillClinicEnrollmentsForInstitution() {
        String j;
        Map m = new HashMap();
        j = "select c from Encounter c "
                + " where c.retired=:ret "
                + " c.encounterType=:type "
                + " and c.encounterDate between :fd and :td ";
        m.put("ret", false);
        m.put("fd", fromDate);
        m.put("td", toDate);
        m.put("type", EncounterType.Clinic_Enroll);
        if (institution != null) {
            j += " and c.institution in :ins ";
            List<Institution> ins = institutionApplicationController.findChildrenInstitutions(institution);
            ins.add(institution);
            m.put("ins", ins);
        } else {
            m.put("ins", webUserController.getLoggableInstitutions());
        }
        encounters = encounterController.getItems(j, m);
    }

    public void fillClinicVisitsForInstitution() {
        String j;
        Map m = new HashMap();
        j = "select c from Encounter c "
                + " where c.retired=:ret "
                + " c.encounterType=:type "
                + " and c.encounterDate between :fd and :td ";
        m.put("ret", false);
        m.put("fd", fromDate);
        m.put("td", toDate);
        m.put("type", EncounterType.Clinic_Visit);
        if (institution != null) {
            j += " and c.institution in :ins ";
            List<Institution> ins = institutionApplicationController.findChildrenInstitutions(institution);
            ins.add(institution);
            m.put("ins", ins);
        } else {
            m.put("ins", webUserController.getLoggableInstitutions());
        }
        encounters = encounterController.getItems(j, m);
    }

    public void fillEncountersForSysAdmin() {
        String j;
        Map m = new HashMap();
        j = "select c from Encounter c "
                + " where c.retired=:ret "
                + " c.encounterType=:type "
                + " and c.encounterDate between :fd and :td ";
        m.put("ret", false);
        m.put("fd", fromDate);
        m.put("td", toDate);
        m.put("type", EncounterType.Clinic_Enroll);
        if (institution != null) {
            j += " and c.institution in :ins ";
            List<Institution> ins = institutionApplicationController.findChildrenInstitutions(institution);
            ins.add(institution);
            m.put("ins", ins);
        }
        encounters = encounterController.getItems(j, m);
    }

// </editor-fold>   
// <editor-fold defaultstate="collapsed" desc="Getters and Setters">
    public EncounterController getEncounterController() {
        return encounterController;
    }

    public ClientController getClientController() {
        return clientController;
    }

    public ComponentController getComponentController() {
        return componentController;
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public List<Encounter> getEncounters() {
        return encounters;
    }

    public void setEncounters(List<Encounter> encounters) {
        this.encounters = encounters;
    }

    public List<Client> getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = CommonController.startOfTheMonth();
        }
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = new Date();
        }
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

// </editor-fold> 
    public InstitutionController getInstitutionController() {
        return institutionController;
    }

    public NcdReportTem getNcdReportTem() {
        return ncdReportTem;
    }

    public void setNcdReportTem(NcdReportTem ncdReportTem) {
        this.ncdReportTem = ncdReportTem;
    }

    public ClientEncounterComponentFormSetFacade getClientEncounterComponentFormSetFacade() {
        return clientEncounterComponentFormSetFacade;
    }

    public StreamedContent getFile() {
        return file;
    }

    public DesignComponentFormItemFacade getDesignComponentFormItemFacade() {
        return designComponentFormItemFacade;
    }

    public void setDesignComponentFormItemFacade(DesignComponentFormItemFacade designComponentFormItemFacade) {
        this.designComponentFormItemFacade = designComponentFormItemFacade;
    }

    public String getMergingMessage() {
        return mergingMessage;
    }

    public void setMergingMessage(String mergingMessage) {
        this.mergingMessage = mergingMessage;
    }

    public ClientFacade getClientFacade() {
        return clientFacade;
    }

    public void setClientFacade(ClientFacade clientFacade) {
        this.clientFacade = clientFacade;
    }

    public EncounterFacade getEncounterFacade() {
        return encounterFacade;
    }

    public void setEncounterFacade(EncounterFacade encounterFacade) {
        this.encounterFacade = encounterFacade;
    }

    public ClientEncounterComponentItemFacade getClientEncounterComponentItemFacade() {
        return clientEncounterComponentItemFacade;
    }

    public QueryComponent getQueryComponent() {
        return queryComponent;
    }

    public void setQueryComponent(QueryComponent queryComponent) {
        this.queryComponent = queryComponent;
    }

    public QueryComponentFacade getQueryComponentFacade() {
        return queryComponentFacade;
    }

    public UploadFacade getUploadFacade() {
        return uploadFacade;
    }

    public QueryComponentController getQueryComponentController() {
        return queryComponentController;
    }

    public ClientEncounterComponentItemController getClientEncounterComponentItemController() {
        return clientEncounterComponentItemController;
    }

    public void setClientEncounterComponentItemController(ClientEncounterComponentItemController clientEncounterComponentItemController) {
        this.clientEncounterComponentItemController = clientEncounterComponentItemController;
    }

    public List<StoredQueryResult> getMyResults() {
        return myResults;
    }

    public void setMyResults(List<StoredQueryResult> myResults) {
        this.myResults = myResults;
    }

    public List<StoredQueryResult> getReportResults() {
        return reportResults;
    }

    public void setReportResults(List<StoredQueryResult> reportResults) {
        this.reportResults = reportResults;
    }

    public StoredQueryResult getRemovingResult() {
        return removingResult;
    }

    public void setRemovingResult(StoredQueryResult removingResult) {
        this.removingResult = removingResult;
    }

    public StoredQueryResult getDownloadingResult() {
        return downloadingResult;
    }

    public void setDownloadingResult(StoredQueryResult downloadingResult) {
        this.downloadingResult = downloadingResult;
    }

    public Upload getCurrentUpload() {
        return currentUpload;
    }

    public void setCurrentUpload(Upload currentUpload) {
        this.currentUpload = currentUpload;
    }

    public List<InstitutionCount> getInstitutionCounts() {
        return institutionCounts;
    }

    public void setInstitutionCounts(List<InstitutionCount> institutionCounts) {
        this.institutionCounts = institutionCounts;
    }

    public Long getReportCount() {
        return reportCount;
    }

    public void setReportCount(Long reportCount) {
        this.reportCount = reportCount;
    }

    public StreamedContent getResultExcelFile() {
        return resultExcelFile;
    }

    public void setResultExcelFile(StreamedContent resultExcelFile) {
        this.resultExcelFile = resultExcelFile;
    }

    public ConsolidatedQueryResultFacade getConsolidatedQueryResultFacade() {
        return consolidatedQueryResultFacade;
    }

    public IndividualQueryResultFacade getIndividualQueryResultFacade() {
        return individualQueryResultFacade;
    }

    public ExcelReportController getExcelReportController() {
        return excelReportController;
    }

    public void setExcelReportController(ExcelReportController excelReportController) {
        this.excelReportController = excelReportController;
    }

    public DesignComponentFormSet getDesigningComponentFormSet() {
        return designingComponentFormSet;
    }

    public void setDesigningComponentFormSet(DesignComponentFormSet designingComponentFormSet) {
        this.designingComponentFormSet = designingComponentFormSet;
    }

    public List<DesignComponentFormItem> getDesignComponentFormItems() {
        return designComponentFormItems;
    }

    public void setDesignComponentFormItems(List<DesignComponentFormItem> designComponentFormItems) {
        this.designComponentFormItems = designComponentFormItems;
    }

    public DesignComponentFormItem getDesignComponentFormItem() {
        return designComponentFormItem;
    }

    public void setDesignComponentFormItem(DesignComponentFormItem designComponentFormItem) {
        this.designComponentFormItem = designComponentFormItem;
    }

    public List<AreaCount> getAreaCounts() {
        return areaCounts;
    }

    public void setAreaCounts(List<AreaCount> areaCounts) {
        this.areaCounts = areaCounts;
    }

    public Long getAreaRepCount() {
        return areaRepCount;
    }

    public void setAreaRepCount(Long areaRepCount) {
        this.areaRepCount = areaRepCount;
    }

    public boolean isRecalculate() {
        return recalculate;
    }

    public void setRecalculate(boolean recalculate) {
        this.recalculate = recalculate;
    }

    public DesignComponentFormSet getFromSet() {
        return fromSet;
    }

    public void setFromSet(DesignComponentFormSet fromSet) {
        this.fromSet = fromSet;
    }

    public StoredQueryResult getSelectedStoredQueryResult() {
        return selectedStoredQueryResult;
    }

    public void setSelectedStoredQueryResult(StoredQueryResult selectedStoredQueryResult) {
        this.selectedStoredQueryResult = selectedStoredQueryResult;
    }

    public DesignComponentFormSet getFormset() {
        return formset;
    }

    public void setFormset(DesignComponentFormSet formset) {
        this.formset = formset;
    }

    public List<ClientEncounterComponentFormSet> getClientEncounterComponentFormSets() {
        return clientEncounterComponentFormSets;
    }

    public void setClientEncounterComponentFormSets(List<ClientEncounterComponentFormSet> clientEncounterComponentFormSets) {
        this.clientEncounterComponentFormSets = clientEncounterComponentFormSets;
    }

}
