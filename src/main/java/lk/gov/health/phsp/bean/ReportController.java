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
import java.io.OutputStream;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.faces.context.FacesContext;
import javax.persistence.TemporalType;
import javax.servlet.ServletContext;
import jxl.CellType;
import jxl.DateCell;
import jxl.NumberCell;
import jxl.Workbook;
import jxl.write.DateTime;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import lk.gov.health.phsp.entity.ClientEncounterComponentItem;
import lk.gov.health.phsp.entity.DesignComponentFormItem;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.entity.QueryComponent;
import lk.gov.health.phsp.entity.StoredQueryResult;
import lk.gov.health.phsp.entity.Upload;
import lk.gov.health.phsp.enums.Quarter;
import lk.gov.health.phsp.enums.QueryCriteriaMatchType;
import lk.gov.health.phsp.enums.QueryType;
import lk.gov.health.phsp.enums.TimePeriodType;
import lk.gov.health.phsp.facade.ClientEncounterComponentItemFacade;
import lk.gov.health.phsp.facade.ClientFacade;
import lk.gov.health.phsp.facade.DesignComponentFormItemFacade;
import lk.gov.health.phsp.facade.EncounterFacade;
import lk.gov.health.phsp.facade.QueryComponentFacade;
import lk.gov.health.phsp.facade.StoredQueryResultFacade;
import lk.gov.health.phsp.facade.UploadFacade;
import lk.gov.health.phsp.facade.util.JsfUtil;
import lk.gov.health.phsp.pojcs.ClientBasicData;
import lk.gov.health.phsp.pojcs.InstitutionCount;
import lk.gov.health.phsp.pojcs.Replaceable;
import lk.gov.health.phsp.pojcs.ReportTimePeriod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.util.CellRangeAddress;
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
    private QueryComponentFacade queryComponentFacade;
    @EJB
    private UploadFacade uploadFacade;
    @EJB
    private StoredQueryResultFacade storedQueryResultFacade;

// </editor-fold>     
// <editor-fold defaultstate="collapsed" desc="Controllers">
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
    private QueryComponentController queryComponentController;
    @Inject
    private ClientEncounterComponentItemController clientEncounterComponentItemController;
// </editor-fold>  
// <editor-fold defaultstate="collapsed" desc="Class Variables">
    private List<Encounter> encounters;
    private List<Client> clients;
    private Date fromDate;
    private Date toDate;
    private Institution institution;
    private Area area;
    private NcdReportTem ncdReportTem;
    private StreamedContent file;
    private String mergingMessage;
    private QueryComponent queryComponent;
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
    private List<InstitutionCount> institutionCounts;
    private Long reportCount;

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

    public StreamedContent getDownloadingFile() {
        if (getDownloadingResult() == null) {
            JsfUtil.addErrorMessage("No Download file");
            return null;
        }
        if (getDownloadingResult().getUpload() == null) {
            JsfUtil.addErrorMessage("No Excel file");
            return null;
        }
        InputStream stream = new ByteArrayInputStream(downloadingResult.getUpload().getBaImage());
        if (downloadingResult.getUpload().getFileType() == null || downloadingResult.getUpload().getFileType().trim().equals("")) {
            downloadingResult.getUpload().setFileType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            getStoredQueryResultFacade().edit(downloadingResult);
        }
        downloadingFile = new DefaultStreamedContent(stream, downloadingResult.getUpload().getFileType(), downloadingResult.getUpload().getFileName());
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

        listExistingReports();

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
        return "/reports/index";
    }

    public String toViewInstitutionExcelReports() {
        String forSys = "/reports/excel/institution_excel_reports_sa";
        String forIns = "/reports/excel/institution_excel_reports_ia";
        String forMe = "/reports/excel/institution_excel_reports_sa";
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

    public String toViewMyExcelReports() {
        listMyReports();
        return "/reports/excel/my_excel_reports";
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

//        System.out.println("m = " + m);
//        System.out.println("j = " + j);
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
            System.out.println("e1 = " + e.getMessage());
            mergingMessage = "Error - " + e.getMessage();
        } finally {

            if (myFirstWbook != null) {
                try {
                    myFirstWbook.close();
                    mergingMessage = "Closing File.";
                } catch (IOException | WriteException e) {
//                    System.out.println("e2 = " + e.getMessage());
                    mergingMessage = "Error - " + e.getMessage();
                }
            }

        }

        mergingMessage = "Ready for Download";
        InputStream stream;
        try {
            stream = new FileInputStream(newFile);
            file = new DefaultStreamedContent(stream, "application/xls", newFile.getAbsolutePath());
        } catch (FileNotFoundException ex) {
            System.out.println("ex3 = " + ex.getMessage());
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
                System.out.println("clnts = " + clnts);
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
            System.out.println("ex = " + ex);
        }

        XSSFWorkbook workbook;
        XSSFSheet sheet;

        try {

            FileInputStream excelFile = new FileInputStream(newFile);
            workbook = new XSSFWorkbook(excelFile);
            sheet = workbook.getSheetAt(0);
            XSSFSheet sheet2 = workbook.createSheet("Test Sheet CHIMS");

            Iterator<Row> iterator = sheet.iterator();

            System.out.println("sheet.getSheetName() = " + sheet.getSheetName());

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
                System.out.println();

                excelFile.close();

                FileOutputStream out = new FileOutputStream(FILE_NAME);
                workbook.write(out);
                out.close();

                InputStream stream;
                stream = new FileInputStream(newFile);
                file = new DefaultStreamedContent(stream, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", FILE_NAME);

            }
        } catch (FileNotFoundException e) {
            System.out.println("e = " + e);
        } catch (IOException e) {
            System.out.println("e = " + e);
        }

    }

    public Long findReplaceblesInCalculationString(String text, List<Encounter> ens) {
        System.out.println("findReplaceblesInCalculationString");

        Long l = 0l;

        if (ens == null) {
            System.out.println("No encounters");
            return l;
        }
        if (ens.isEmpty()) {
            System.out.println("Empty encounter list");
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
            System.out.println("block = " + block);
            QueryComponent qc = getQueryComponentController().findByCode(block);
            if (qc == null) {
                System.out.println("No Such Query = ");
                l = null;
                return l;

            } else {
                System.out.println("qc.getQueryType() = " + qc.getQueryType());
                if (qc.getQueryType() == QueryType.Encounter_Count) {
                    List<QueryComponent> criteria = getQueryComponentController().criteria(qc);
                    System.out.println("criteria = " + criteria);
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

        System.out.println("End of while");
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
        System.out.println("qi.getItem().getCode() = " + qi.getItem().getCode());
        System.out.println("q.getItem().getCode() = " + q.getItem().getCode());

        boolean m = false;
        Integer int1 = null;
        Integer int2 = null;
        Double real1 = null;
        Double real2 = null;
        Long lng1 = null;
        Long lng2 = null;
        Item itemVariable = null;
        Item itemValue = null;
        System.out.println("q.getMatchType() = " + q.getMatchType());
        if (q.getMatchType() == QueryCriteriaMatchType.Variable_Value_Check) {
            System.out.println("q.getQueryDataType() = " + q.getQueryDataType());
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

                        System.out.println("itemValue = " + itemValue.getCode());
                        System.out.println("itemVariable = " + itemVariable.getCode());
                        System.out.println("qi Item Name " + qi.getItem().getCode());
                        System.out.println("qi Item Value = " + qi.getItemValue().getCode());

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
        System.out.println("m = " + m);
        return m;
    }

    public Long findMatchingCount(List<Encounter> encs, List<QueryComponent> qrys) {
        System.out.println("findMatchingCount");
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

        System.out.println("rtp.getFrom() = " + rtp.getFrom());
        System.out.println("rtp.getTo() = " + rtp.getTo());
        System.out.println("rtp.getYear() = " + rtp.getYear());
        System.out.println("rtp.getQuarter() = " + rtp.getQuarter());

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
        return action;
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
        return action;
    }

// </editor-fold>   
// <editor-fold defaultstate="collapsed" desc="Functions">
    public void fillClientRegistrationForSysAdmin() {
        String j;
        Map m = new HashMap();
        j = "select c from Client c "
                + " where c.retired=:ret "
                + " and c.createdAt between :fd and :td ";
        m.put("ret", false);
        m.put("fd", fromDate);
        m.put("td", toDate);
        if (institution != null) {
            j += " and c.createInstitution in :ins ";
            List<Institution> ins = institutionController.findChildrenInstitutions(institution);
            ins.add(institution);
            m.put("ins", ins);
        }

        clients = clientController.getItems(j, m);
    }

    public void downloadClientRegistrationsForSysAdmin() {
        String j;
        Map m = new HashMap();
        j = "select new lk.gov.health.phsp.pojcs.ClientBasicData("
                + "c.phn, "
                + "c.person.gnArea.name, "
                + "c.createInstitution.name, "
                + "c.person.dateOfBirth, "
                + "c.createdAt, "
                + "c.person.sex.name "
                + ") "
                + " from Client c "
                + " where c.retired=:ret "
                + " and c.createdAt between :fd and :td ";
        m.put("ret", false);
        m.put("fd", fromDate);
        m.put("td", toDate);

        //phn, String gnArea, String createdInstitution, Date dataOfBirth, String sex
        if (institution != null) {
            j += " and c.createInstitution in :ins ";
            List<Institution> ins = institutionController.findChildrenInstitutions(institution);
            ins.add(institution);
            m.put("ins", ins);
        }

        List<Object> objs = getClientFacade().findAggregates(j, m);

        String FILE_NAME = "client_registrations" + "_" + (new Date()) + ".xlsx";
        String mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        String folder = "/tmp/";

        File newFile = new File(folder + FILE_NAME);

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Java Books");

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

        Row t5 = sheet.createRow(rowCount++);
        Cell th5_1 = t5.createCell(0);
        th5_1.setCellValue("Serial");
        Cell th5_2 = t5.createCell(1);
        th5_2.setCellValue("PHN");
        Cell th5_3 = t5.createCell(2);
        th5_3.setCellValue("Sex");
        Cell th5_4 = t5.createCell(3);
        th5_4.setCellValue("Age in Years");
        Cell th5_5 = t5.createCell(4);
        th5_5.setCellValue("Registered on");
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
            if (o instanceof ClientBasicData) {
                ClientBasicData cbd = (ClientBasicData) o;
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
                c5.setCellValue(cbd.getCreatedAt());
                c5.setCellStyle(cellStyle);
                
                Cell c6 = row.createCell(5);
                c6.setCellValue(cbd.getGnArea());
                if (institution == null) {
                    Cell c7 = row.createCell(6);
                    c7.setCellValue(cbd.getCreatedInstitution());
                }

                serial++;
            }
        }

        try (FileOutputStream outputStream = new FileOutputStream(newFile)) {
            workbook.write(outputStream);
        } catch (Exception e) {
            System.out.println("e = " + e);
        }

        InputStream stream;
        try {
            stream = new FileInputStream(newFile);
            resultExcelFile = new DefaultStreamedContent(stream, mimeType, FILE_NAME);
        } catch (FileNotFoundException ex) {
            System.out.println("ex = " + ex);
        }

    }

    public void fillClientRegistrationForSysAdminByInstitution() {
        String j;
        Map m = new HashMap();
        j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.createInstitution, count(c)) "
                + " from Client c "
                + " where c.retired=:ret "
                + " and c.createdAt between :fd and :td "
                + " group by c.createInstitution "
                + " order by c.createInstitution.name";
        m.put("ret", false);
        m.put("fd", fromDate);
        m.put("td", toDate);
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
    }

    public void fillRegistrationsOfClientsByInstitution() {

        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.createInstitution, count(c)) "
                + " from Client c "
                + " where c.retired<>:ret ";
        Map m = new HashMap();
        m.put("ret", true);
        j = j + " and c.createdAt between :fd and :td ";

        j = j + " and c.createInstitution in :ins ";
        m.put("ins", webUserController.getLoggableInstitutions());

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

    }

    public void fillClinicEnrollmentsForSysAdmin() {
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
            List<Institution> ins = institutionController.findChildrenInstitutions(institution);
            ins.add(institution);
            m.put("ins", ins);
        }
        encounters = encounterController.getItems(j, m);
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
            List<Institution> ins = institutionController.findChildrenInstitutions(institution);
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
            List<Institution> ins = institutionController.findChildrenInstitutions(institution);
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
            List<Institution> ins = institutionController.findChildrenInstitutions(institution);
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
            List<Institution> ins = institutionController.findChildrenInstitutions(institution);
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
            fromDate = CommonController.startOfTheYear();
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

}
