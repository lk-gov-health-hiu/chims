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
import javax.faces.context.FacesContext;
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
import lk.gov.health.phsp.enums.TimePeriodType;
import lk.gov.health.phsp.facade.ClientEncounterComponentItemFacade;
import lk.gov.health.phsp.facade.ClientFacade;
import lk.gov.health.phsp.facade.DesignComponentFormItemFacade;
import lk.gov.health.phsp.facade.EncounterFacade;
import lk.gov.health.phsp.facade.QueryComponentFacade;
import lk.gov.health.phsp.facade.UploadFacade;
import lk.gov.health.phsp.facade.util.JsfUtil;
import lk.gov.health.phsp.pojcs.ReportTimePeriod;
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
// <editor-fold defaultstate="collapsed" desc="Navigation">
    public String toViewReports() {
        return "/reports/index";
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
        List<Encounter> encounterIds = encounterFacade.findLongList(j, m);

        return encounterIds;

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
                    System.out.println("e2 = " + e.getMessage());
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
        toDownloadNcdReport(getWebUserController().getLoggedUser().getInstitution(), rtp);
    }

    public void toDownloadNcdReport(Institution ins, ReportTimePeriod rtp) {
        
        
        
        String FILE_NAME = "NCD_Report_.xlsx";

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("NCD_Report");

        int rowNum = 0;
        int firstRow = 0;
        int lastRow = 0;
        int firstCol = 0;
        int lastCol = 2;

        System.out.println("Creating excel");

        String txt;

        Row row;
        Cell cell;

        row = sheet.createRow(0);
        txt = "Monthly Summary of the NCD Screening Activities ( HLC/ Office/Mobile) H1239";
        cell = row.createCell(1);
        cell.setCellValue(txt);
        sheet.addMergedRegion(CellRangeAddress.valueOf("A1:X1"));

        row = sheet.createRow(1);

        txt = "Institution";
        cell = row.createCell(0);
        cell.setCellValue(txt);

        txt = ins.getName();
        cell = row.createCell(1);
        cell.setCellValue(txt);

        sheet.addMergedRegion(CellRangeAddress.valueOf("B2:D2"));

        row = sheet.createRow(2);

        txt = rtp.getLabel();
        cell = row.createCell(0);
        cell.setCellValue(txt);

        txt = rtp.getValue();
        cell = row.createCell(1);
        cell.setCellValue(txt);
        sheet.addMergedRegion(CellRangeAddress.valueOf("B3:D3"));

        row = sheet.createRow(3);

        firstRow = 3;
        lastRow = 3;
        firstCol = 3;
        lastCol = 5;
        txt = "Age in Years";
        cell = row.createCell(3);
        cell.setCellValue(txt);
        sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));

        row = sheet.createRow(4);
        txt = "Total Participants";
        cell = row.createCell(2);
        cell.setCellValue(txt);

        txt = "Less than 35";
        cell = row.createCell(3);
        cell.setCellValue(txt);
        txt = "35-65";
        cell = row.createCell(4);
        cell.setCellValue(txt);
        txt = "More than 35";
        cell = row.createCell(5);
        cell.setCellValue(txt);

        List<Institution> allIns = institutionController.findChildrenInstitutions(ins);

        System.out.println("allIns = " + allIns.size());

        int rowCount = 5;

        for (Institution i : allIns) {

            System.out.println("i = " + i.getName());

            List<Encounter> encs = findEncounters(rtp.getFrom(), rtp.getTo(), i);

            if (encs == null) {
                System.out.println(i.getName() + " No Result");
                continue;
            } else if (encs.size() < 1) {
                System.out.println(i.getName() + " No Encounters");
                continue;
            } else {
                System.out.println(i.getName() + " No Encounters");
            }

            NcdReportTem mri = new NcdReportTem();
            NcdReportTem fri = new NcdReportTem();
            NcdReportTem tri = new NcdReportTem();

            if (i == null) {
                continue;
            }

            Row mrow = sheet.createRow(rowCount);
            Row frow = sheet.createRow(rowCount + 1);
            Row trow = sheet.createRow(rowCount + 2);

            txt = i.getName();
            cell = mrow.createCell(0);
            cell.setCellValue(txt);

            txt = "Male";
            cell = mrow.createCell(1);
            cell.setCellValue(txt);

            txt = "Female";
            cell = frow.createCell(1);
            cell.setCellValue(txt);

            txt = "Total";
            cell = trow.createCell(1);
            cell.setCellValue(txt);

            for (Encounter e : encs) {

                Long age = CommonController.getDifferenceInYears(e.getClient().getPerson().getDateOfBirth(), e.getCreatedAt());
                System.out.println("e.getClient().getPerson().getDateOfBirth() = " + e.getClient().getPerson().getDateOfBirth());
                System.out.println("e.getCreatedAt() = " + e.getCreatedAt());
                System.out.println("age = " + age);

                if (e.getClient().getPerson().getSex().getCode().equalsIgnoreCase("sex_male")) {
                    mri.setTotalNoOfParticipants(mri.getTotalNoOfParticipants() + 1);
                    if (age < 35) {
                        mri.setAge20To34(mri.getAge20To34() + 1);
                    } else if (age < 65) {
                        mri.setAge35To65(mri.getAge35To65() + 1);
                    } else {
                        mri.setAgeGt65(mri.getAgeGt65() + 1);
                    }
                } else if (e.getClient().getPerson().getSex().getCode().equalsIgnoreCase("sex_female")) {
                    fri.setTotalNoOfParticipants(fri.getTotalNoOfParticipants() + 1);
                    if (age < 35) {
                        fri.setAge20To34(fri.getAge20To34() + 1);
                    } else if (age < 65) {
                        fri.setAge35To65(fri.getAge35To65() + 1);
                    } else {
                        fri.setAgeGt65(fri.getAgeGt65() + 1);
                    }
                }
                tri.setTotalNoOfParticipants(tri.getTotalNoOfParticipants() + 1);

                if (age < 35) {
                    tri.setAge20To34(tri.getAge20To34() + 1);
                } else if (age < 65) {
                    tri.setAge35To65(tri.getAge35To65() + 1);
                } else {
                    tri.setAgeGt65(tri.getAgeGt65() + 1);
                }
                List<ClientEncounterComponentItem> its = ClientEncounterComponentFormItems(e);
            }

            //Totals
            txt = "" + mri.getTotalNoOfParticipants();
            cell = mrow.createCell(2);
            cell.setCellValue(txt);

            txt = "" + fri.getTotalNoOfParticipants();
            cell = frow.createCell(2);
            cell.setCellValue(txt);

            txt = "" + tri.getTotalNoOfParticipants();
            cell = trow.createCell(2);
            cell.setCellValue(txt);

            //Ages
            txt = "" + mri.getAge20To34();
            cell = mrow.createCell(3);
            cell.setCellValue(txt);
            txt = "" + mri.getAge35To65();
            cell = mrow.createCell(4);
            cell.setCellValue(txt);
            txt = "" + mri.getAgeGt65();
            cell = mrow.createCell(5);
            cell.setCellValue(txt);

            txt = "" + fri.getAge20To34();
            cell = frow.createCell(3);
            cell.setCellValue(txt);
            txt = "" + fri.getAge35To65();
            cell = frow.createCell(4);
            cell.setCellValue(txt);
            txt = "" + fri.getAgeGt65();
            cell = frow.createCell(5);
            cell.setCellValue(txt);

            txt = "" + tri.getAge20To34();
            cell = trow.createCell(3);
            cell.setCellValue(txt);
            txt = "" + tri.getAge35To65();
            cell = trow.createCell(4);
            cell.setCellValue(txt);
            txt = "" + tri.getAgeGt65();
            cell = trow.createCell(5);
            cell.setCellValue(txt);

            rowCount = rowCount + 3;

        }

//        for (Object[] datatype : datatypes) {
//            Row row = sheet.createRow(rowNum++);
//            int colNum = 0;
//            for (Object field : datatype) {
//                Cell cell = row.createCell(colNum++);
//                if (field instanceof String) {
//                    cell.setCellValue((String) field);
//                } else if (field instanceof Integer) {
//                    cell.setCellValue((Integer) field);
//                }
//            }
//        }
        try {
            FileOutputStream outputStream = new FileOutputStream(FILE_NAME);
            workbook.write(outputStream);
            workbook.close();
        } catch (FileNotFoundException e) {
            System.out.println("e.printStackTrace() = " + e.getMessage());
        } catch (IOException e) {
            System.out.println("e.printStackTrace() = " + e.getMessage());
        }

        InputStream stream;
        try {
            stream = new FileInputStream(FILE_NAME);
            file = new DefaultStreamedContent(stream, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", FILE_NAME);
        } catch (FileNotFoundException ex) {
            System.out.println("ex3 = " + ex.getMessage());
            mergingMessage = "Error - " + ex.getMessage();
        }
    }

    public void toDownloadNcdReportLastMonthInstitution() {
        ReportTimePeriod rtp = new ReportTimePeriod();
        rtp.setTimePeriodType(TimePeriodType.Monthly);
        rtp.setFrom(CommonController.startOfTheLastMonth());
        rtp.setTo(CommonController.endOfTheLastMonth());
        rtp.setYear(CommonController.getYear(CommonController.endOfTheLastMonth()));
        rtp.setMonth(CommonController.getMonth(CommonController.endOfTheLastMonth()));
        toDownloadNcdReport(getWebUserController().getLoggedUser().getInstitution(), rtp);
    }

    public void toDownloadNcdReportThisQuarterInstitution() {
        ReportTimePeriod rtp = new ReportTimePeriod();
        rtp.setTimePeriodType(TimePeriodType.Quarterly);
        rtp.setFrom(CommonController.startOfQuarter());
        rtp.setTo(CommonController.endOfQuarter());
        rtp.setYear(CommonController.getYear());
        rtp.setQuarter(CommonController.getQuarter());
        toDownloadNcdReport(getWebUserController().getLoggedUser().getInstitution(), rtp);
    }

    public void toDownloadNcdReportLastQuarterInstitution() {
        ReportTimePeriod rtp = new ReportTimePeriod();
        rtp.setTimePeriodType(TimePeriodType.Quarterly);
        rtp.setFrom(CommonController.startOfTheLastQuarter());
        rtp.setTo(CommonController.endOfTheLastQuarter());
        rtp.setYear(CommonController.getYear(CommonController.startOfTheLastQuarter()));
        rtp.setQuarter(CommonController.getQuarter(CommonController.startOfTheLastQuarter()));
        toDownloadNcdReport(getWebUserController().getLoggedUser().getInstitution(), rtp);
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
    
    

}
