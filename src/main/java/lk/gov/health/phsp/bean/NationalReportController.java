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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.persistence.TemporalType;
import lk.gov.health.phsp.entity.Client;
import lk.gov.health.phsp.entity.ClientEncounterComponentForm;
import lk.gov.health.phsp.entity.ClientEncounterComponentFormSet;
import lk.gov.health.phsp.entity.ClientEncounterComponentItem;
import lk.gov.health.phsp.entity.DesignComponentForm;
import lk.gov.health.phsp.entity.DesignComponentFormItem;
import lk.gov.health.phsp.entity.DesignComponentFormSet;
import lk.gov.health.phsp.entity.Encounter;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.entity.Person;
import lk.gov.health.phsp.enums.ComponentSex;
import lk.gov.health.phsp.enums.DataRepresentationType;
import lk.gov.health.phsp.enums.EncounterType;
import lk.gov.health.phsp.facade.ClientEncounterComponentFormSetFacade;
import lk.gov.health.phsp.facade.ClientEncounterComponentItemFacade;
import lk.gov.health.phsp.facade.ClientFacade;
import lk.gov.health.phsp.facade.EncounterFacade;
import lk.gov.health.phsp.facade.util.JsfUtil;
import lk.gov.health.phsp.pojcs.InstitutionCount;
import lk.gov.health.phsp.pojcs.ObservationValueCount;
import lk.gov.health.phsp.pojcs.dataentry.DataForm;
import lk.gov.health.phsp.pojcs.dataentry.DataFormset;
import lk.gov.health.phsp.pojcs.dataentry.DataItem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author buddhika
 */
@Named
@SessionScoped
public class NationalReportController implements Serializable {

    private Date fromDate;
    private Date toDate;
    private Long count;
    private Item queryItem;
    private Item sex;

    private List<ObservationValueCount> observationValueCounts;
    private DesignComponentFormItem designComponentFormItem;

    private StreamedContent resultExcelFile;

    private Institution institution;
    private DesignComponentFormSet designingComponentFormSet;

    @EJB
    private ClientEncounterComponentItemFacade clientEncounterComponentItemFacade;

    @Inject
    WebUserController webUserController;
    @Inject
    StreamedContentController streamedContentController;
    @Inject
    private DesignComponentFormItemController designComponentFormItemController;
    @Inject
    DesignComponentFormController designComponentFormController;
    @Inject
    ClientEncounterComponentFormController clientEncounterComponentFormController;
    @Inject
    ClientEncounterComponentItemController clientEncounterComponentItemController;

    @EJB
    ClientFacade clientFacade;
    @EJB
    EncounterFacade encounterFacade;
    @EJB
    ClientEncounterComponentFormSetFacade clientEncounterComponentFormSetFacade;

    private List<DesignComponentFormItem> designComponentFormItems;

    /**
     * Creates a new instance of HospitalController
     */
    public NationalReportController() {
    }

    public String toNationalReportsCounts() {
        count = null;
        return "/national/counts";
    }

    public String toObservationValueCount() {
        count = null;
        return "/national/observation_values";
    }

    public String toRegistrationCount() {
        count = null;
        return "/national/registration_counts";
    }

    public String toClinicVisitCount() {
        count = null;
        return "/national/clinic_visit_counts";
    }

    public String toObservationValueCountInt() {
        count = null;
        return "/national/observation_values_int";
    }

    public String toObservationValueCountLong() {
        count = null;
        return "/national/observation_values_long";
    }

    public String toObservationValueCountDbl() {
        count = null;
        return "/national/observation_values_dbl";
    }

    public void fillItemsofTheSelectedSet() {
        designComponentFormItems = designComponentFormItemController.fillItemsOfTheFormSet(designingComponentFormSet);
    }

    public void createDataSingleCounts() {
        if (designComponentFormItem == null) {
            JsfUtil.addErrorMessage("Please select a variable");
            return;
        }
        if (designComponentFormItem.getItem() == null
                || designComponentFormItem.getItem().getCode() == null) {
            JsfUtil.addErrorMessage("Error in selected variable.");
            return;
        }
        if (designComponentFormItem.getItem().getDataType() == null) {
            JsfUtil.addErrorMessage("Error in item data type.");
            return;
        }
        String select = "";
        String groupBy = "";
        switch (designComponentFormItem.getItem().getDataType()) {
            case Real_Number:
                select = "select new lk.gov.health.phsp.pojcs.ObservationValueCount(c.realNumberValue, count(c)) ";
                groupBy = " group by c.realNumberValue";
                break;
            case Short_Text:
                select = "select new lk.gov.health.phsp.pojcs.ObservationValueCount(c.shortTextValue, count(c)) ";
                groupBy = " group by c.shortTextValue";
                break;
            case Integer_Number:
                select = "select new lk.gov.health.phsp.pojcs.ObservationValueCount(c.integerNumberValue, count(c)) ";
                groupBy = " group by c.integerNumberValue";
                break;
            case Item_Reference:
                select = "select new lk.gov.health.phsp.pojcs.ObservationValueCount(c.itemValue.name, count(c)) ";
                groupBy = " group by c.itemValue";
                break;
            case Long_Number:
                select = "select new lk.gov.health.phsp.pojcs.ObservationValueCount(c.longNumberValue, count(c)) ";
                groupBy = " group by c.longNumberValue";
                break;
            case Long_Text:
            case Area_Reference:
            case Boolean:
            case Byte_Array:
            case Client_Reference:
            case DateTime:
            case Prescreption_Reference:
            case Prescreption_Request:
            case Procedure_Request:
                JsfUtil.addErrorMessage("Not supported.");
                return;
            default:
                JsfUtil.addErrorMessage("Error in item data type.");
                return;
        }

        String j = select
                + " from  ClientEncounterComponentItem c join c.itemEncounter e"
                + " where c.retired<>:fr "
                + " and c.item.code=:ic ";
        j += "  and e.retired<>:er "
                + " and e.encounterDate between :fd and :td";
        j += groupBy;
        Map m = new HashMap();
        m.put("fr", true);
        m.put("ic", designComponentFormItem.getItem().getCode().toLowerCase());
        m.put("er", true);

        m.put("fd", fromDate);
        m.put("td", toDate);

        List<Object> cis = clientEncounterComponentItemFacade.findObjectByJpql(j, m, TemporalType.TIMESTAMP);

        observationValueCounts = new ArrayList<>();

        for (Object o : cis) {
            if (o instanceof ObservationValueCount) {
                observationValueCounts.add((ObservationValueCount) o);
            }
        }

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

        Row t5 = sheet.createRow(rowCount++);
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
        th5_6.setCellValue("Short-text Value");
        Cell th5_7 = t5.createCell(11);
        th5_7.setCellValue("Long Value");
        Cell th5_8 = t5.createCell(12);
        th5_8.setCellValue("Int Value");
        Cell th5_9 = t5.createCell(13);
        th5_9.setCellValue("Real Value");
        Cell th5_10 = t5.createCell(14);
        th5_10.setCellValue("Item Value");
        Cell th5_11 = t5.createCell(15);
        th5_11.setCellValue("Item Value");
        Cell th5_12 = t5.createCell(16);
        th5_12.setCellValue("Completed");

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

                Cell c2a = row.createCell(2);
                c2a.setCellValue(c.getPerson().getName());
                Cell c2b = row.createCell(3);
                c2b.setCellValue(c.getPerson().getAddress());
                Cell c2c = row.createCell(4);
                c2c.setCellValue(c.getPerson().getPhone1());
                Cell c2d = row.createCell(5);
                c2d.setCellValue(c.getPerson().getPhone2());

                if (p.getGnArea() != null) {
                    Cell c2e = row.createCell(6);
                    c2e.setCellValue(c.getPerson().getGnArea().getName());
                }

                Cell c3 = row.createCell(7);
                if (p.getSex() != null) {
                    c3.setCellValue(p.getSex().getName());
                }

                Cell c4 = row.createCell(8);
                int ageInYears = CommonController.calculateAge(p.getDateOfBirth(), e.getEncounterDate());
                c4.setCellValue(ageInYears);

                Cell c5 = row.createCell(9);
                if (e.getEncounterDate() != null) {
                    c5.setCellValue(e.getEncounterDate());
                }
                c5.setCellStyle(cellStyle);

                Cell c6 = row.createCell(10);
                if (i.getShortTextValue() != null) {
                    c6.setCellValue(i.getShortTextValue());
                }

                Cell c7 = row.createCell(11);
                if (i.getLongNumberValue() != null) {
                    c7.setCellValue(i.getLongNumberValue());
                }

                Cell c8 = row.createCell(12);
                if (i.getIntegerNumberValue() != null) {
                    c8.setCellValue(i.getIntegerNumberValue());
                }

                Cell c9 = row.createCell(13);
                if (i.getRealNumberValue() != null) {
                    c9.setCellValue(i.getRealNumberValue());
                }

                Cell c10 = row.createCell(14);
                if (i.getItemValue() != null && i.getItemValue().getName() != null) {
                    c10.setCellValue(i.getItemValue().getName());
                }

                Cell c11 = row.createCell(15);
                if (i.getBooleanValue() != null) {
                    c11.setCellValue(i.getBooleanValue() ? "True" : "False");
                }

                Cell c12 = row.createCell(16);

                if (i.getParentComponent() != null && i.getParentComponent().getParentComponent() != null) {
                    c12.setCellValue(i.getParentComponent().getParentComponent().isCompleted() ? "Complete" : "Not Completed");
                }
                serial++;
            }
        }

        cis = null;

        try ( FileOutputStream outputStream = new FileOutputStream(newFile)) {
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

    public void createExcelFileOfFromsetDataForSelectedEncounters() {
        if (institution == null) {
            JsfUtil.addErrorMessage("Please select an institutions");
            return;
        }
        if (designingComponentFormSet == null) {
            JsfUtil.addErrorMessage("Please select a Formset");
            return;
        }

        String j = "select f "
                + " from  ClientEncounterComponentFormSet f join f.encounter e"
                + " where f.retired<>:fr "
                + " and f.referenceComponent=:ic ";
        j += " and e.institution=:i "
                + " and e.retired<>:er "
                + " and e.encounterType=:t "
                + " and e.encounterDate between :fd and :td"
                + " order by e.id";
        Map m = new HashMap();
        m.put("fr", true);
        m.put("ic", designingComponentFormSet);
        m.put("i", institution);
        m.put("er", true);

        m.put("t", EncounterType.Clinic_Visit);
        m.put("fd", fromDate);
        m.put("td", toDate);

        List<ClientEncounterComponentFormSet> cis = clientEncounterComponentFormSetFacade.findByJpql(j, m);

        String FILE_NAME = designingComponentFormSet.getName() + "_data_of_" + institution.getName() + "_from_" + CommonController.formatDate(fromDate) + "_to_" + CommonController.formatDate(toDate) + ".xlsx";
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
        th1_val.setCellValue("Fromset Data");

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
        th5a_lbl.setCellValue("Formset");
        Cell th5a_val = t5a.createCell(1);
        th5a_val.setCellValue(designingComponentFormSet.getName());

        CellStyle cellStyle = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        cellStyle.setDataFormat(
                createHelper.createDataFormat().getFormat("dd/MMMM/yyyy hh:mm"));

        DataFormset titleFormset = fillDesignComponantFormset(designingComponentFormSet);

        Row formNameRow = sheet.createRow(rowCount++);
        Row itemNameRow = sheet.createRow(rowCount++);
        int colCount = 0;
        for (DataForm tdf : titleFormset.getForms()) {

            for (DataItem tdi : tdf.getItems()) {
                Cell formNameCell = formNameRow.createCell(colCount);
                formNameCell.setCellValue(tdf.getDf().getName());
                Cell itemNameCell = itemNameRow.createCell(colCount);
                itemNameCell.setCellValue(tdi.getDi().getName());
                colCount++;
            }

        }

        for (ClientEncounterComponentFormSet c : cis) {
            DataFormset tdfs = fillClinicalDataFormset(c);
            Row dataRow = sheet.createRow(rowCount++);
            colCount = 0;
            for (DataForm tdf : tdfs.getForms()) {

                for (DataItem tdi : tdf.getItems()) {
                    Cell formNameCell = dataRow.createCell(colCount);
                    if (tdi.getCi() == null) {
                        continue;
                    }
                    if (tdi.getDi() == null) {
                        continue;
                    }
                    if (tdi.getDi().getSelectionDataType() == null) {
                        continue;
                    }
                    switch (tdi.getDi().getSelectionDataType()) {
                        case Area_Reference:
                        case Boolean:
                            if (tdi.getCi().getBooleanValue() == null) {
                                continue;
                            }
                            formNameCell.setCellValue(tdi.getCi().getBooleanValue());
                            break;
                        case Byte_Array:
                        case Client_Reference:
                        case DateTime:
                            if (tdi.getCi().getDateValue() == null) {
                                continue;
                            }
                            formNameCell.setCellStyle(cellStyle);
                            formNameCell.setCellValue(tdi.getCi().getDateValue());
                            break;
                        case Integer_Number:
                            if (tdi.getCi().getIntegerNumberValue() == null) {
                                continue;
                            }
                            formNameCell.setCellValue(tdi.getCi().getIntegerNumberValue());
                            break;
                        case Item_Reference:
                            if (tdi.getCi().getItemValue() == null) {
                                continue;
                            }
                            formNameCell.setCellValue(tdi.getCi().getItemValue().getName());
                            break;
                        case Long_Number:
                            if (tdi.getCi().getLongNumberValue() == null) {
                                continue;
                            }
                            formNameCell.setCellValue(tdi.getCi().getLongNumberValue());
                            break;
                        case Long_Text:
                            if (tdi.getCi().getShortTextValue() == null) {
                                continue;
                            }
                            formNameCell.setCellValue(tdi.getCi().getShortTextValue());
                            break;
                        case Prescreption_Reference:
                        case Prescreption_Request:
                        case Procedure_Request:
                        case Real_Number:
                            if (tdi.getCi().getRealNumberValue() == null) {
                                continue;
                            }
                            formNameCell.setCellValue(tdi.getCi().getShortTextValue());
                            break;
                        case Short_Text:
                            if (tdi.getCi().getShortTextValue() == null) {
                                continue;
                            }
                            formNameCell.setCellValue(tdi.getCi().getShortTextValue());
                            break;
                    }

                    colCount++;
                }

            }

        }

        cis = null;

        try ( FileOutputStream outputStream = new FileOutputStream(newFile)) {
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

    public void fillObservationValues() {
        String j;
        Map m = new HashMap();

        j = "select new lk.gov.health.phsp.pojcs.ObservationValueCount(c.shortTextValue, count(c)) "
                + " from ClientEncounterComponentItem c "
                + " where (c.retired=:ret or c.retired is null) "
                + " and (c.item=:qi)";
        if (sex != null) {
            j += " and c.encounter.client.person.sex.code=:s ";
            m.put("s", sex.getCode());
        }
        if (institution != null) {
            j += " and c.createInstitution=:ins ";
            m.put("ins", institution);
        }
        j += " and c.createdAt between :fd and :td "
                + " group by c.shortTextValue";
        m.put("ret", false);
        m.put("qi", queryItem);
        m.put("fd", getFromDate());
        m.put("td", getToDate());

//        ClientEncounterComponentItem c = new ClientEncounterComponentItem();
//        c.getEncounter().getClient().getPerson().getSex().getCode();
        observationValueCounts = new ArrayList<>();
        //System.out.println("m = " + m);
        //System.out.println("j = " + j);
        List<Object> objs = clientFacade.findAggregates(j, m);
        if (objs == null) {
            return;
        }
        for (Object o : objs) {
            if (o instanceof ObservationValueCount) {
                ObservationValueCount ic = (ObservationValueCount) o;
                observationValueCounts.add(ic);
            }
        }

    }

    public void fillRegistrationCounts() {
        String j;
        Map m = new HashMap();

        j = "select new lk.gov.health.phsp.pojcs.ObservationValueCount(count(c)) "
                + " from Client c "
                + " where (c.retired=:ret or c.retired is null) ";
        if (sex != null) {
            j += " and c.person.sex.code=:s ";
            m.put("s", sex.getCode());
        }
        if (institution != null) {
            j += " and c.createInstitution=:ins ";
            m.put("ins", institution);
        }
        j += " and c.createdAt between :fd and :td ";
        m.put("ret", false);
        m.put("fd", getFromDate());
        m.put("td", getToDate());

        observationValueCounts = new ArrayList<>();
        //System.out.println("m = " + m);
        //System.out.println("j = " + j);
        List<Object> objs = clientFacade.findAggregates(j, m, TemporalType.TIMESTAMP);
        if (objs == null) {
            return;
        }
        for (Object o : objs) {
            if (o instanceof ObservationValueCount) {
                ObservationValueCount ic = (ObservationValueCount) o;
                observationValueCounts.add(ic);
            }
        }

    }

    public void fillClinicVisitCounts() {
        String j;
        Map m = new HashMap();

        j = "select new lk.gov.health.phsp.pojcs.ObservationValueCount(count(c)) "
                + " from Encounter c "
                + " where (c.retired=:ret or c.retired is null) "
                + " and c.encounterType=:et ";
        m.put("et", EncounterType.Clinic_Visit);
        if (sex != null) {
            j += " and c.client.person.sex.code=:s ";
            m.put("s", sex.getCode());
        }
        j += " and c.createdAt between :fd and :td ";
        m.put("ret", false);
        m.put("fd", getFromDate());
        m.put("td", getToDate());

        observationValueCounts = new ArrayList<>();
        //System.out.println("m = " + m);
        //System.out.println("j = " + j);
        List<Object> objs = clientFacade.findAggregates(j, m, TemporalType.TIMESTAMP);
        if (objs == null) {
            return;
        }
        for (Object o : objs) {
            if (o instanceof ObservationValueCount) {
                ObservationValueCount ic = (ObservationValueCount) o;
                observationValueCounts.add(ic);
            }
        }

    }

    public void fillObservationValuesInt() {
        String j;
        Map m = new HashMap();

        j = "select new lk.gov.health.phsp.pojcs.ObservationValueCount(c.integerNumberValue, count(c)) "
                + " from ClientEncounterComponentItem c "
                + " where (c.retired=:ret or c.retired is null) "
                + " and (c.item=:qi) ";
        if (sex != null) {
            j += " and c.encounter.client.person.sex.code=:s ";
            m.put("s", sex.getCode());
        }
        if (institution != null) {
            j += " and c.createInstitution=:ins ";
            m.put("ins", institution);
        }
        j += " and c.createdAt between :fd and :td "
                + " group by c.integerNumberValue";
        m.put("ret", false);
        m.put("qi", queryItem);
        m.put("fd", getFromDate());
        m.put("td", getToDate());

        observationValueCounts = new ArrayList<>();
        //System.out.println("m = " + m);
        //System.out.println("j = " + j);
        List<Object> objs = clientFacade.findAggregates(j, m);
        if (objs == null) {
            return;
        }
        for (Object o : objs) {
            if (o instanceof ObservationValueCount) {
                ObservationValueCount ic = (ObservationValueCount) o;
                observationValueCounts.add(ic);
            }
        }

    }

    public void fillObservationValuesLong() {
        String j;
        Map m = new HashMap();

        j = "select new lk.gov.health.phsp.pojcs.ObservationValueCount(c.longNumberValue, count(c)) "
                + " from ClientEncounterComponentItem c "
                + " where (c.retired=:ret or c.retired is null) "
                + " and (c.item=:qi) ";
        if (sex != null) {
            j += " and c.encounter.client.person.sex.code=:s ";
            m.put("s", sex.getCode());
        }
        if (institution != null) {
            j += " and c.createInstitution=:ins ";
            m.put("ins", institution);
        }
        j += " and c.createdAt between :fd and :td "
                + " group by c.longNumberValue";
        m.put("ret", false);
        m.put("qi", queryItem);
        m.put("fd", getFromDate());
        m.put("td", getToDate());

        observationValueCounts = new ArrayList<>();
        //System.out.println("m = " + m);
        //System.out.println("j = " + j);
        List<Object> objs = clientFacade.findAggregates(j, m);
        if (objs == null) {
            return;
        }
        for (Object o : objs) {
            if (o instanceof ObservationValueCount) {
                ObservationValueCount ic = (ObservationValueCount) o;
                observationValueCounts.add(ic);
            }
        }

    }

    public void fillObservationValuesDbl() {
        String j;
        Map m = new HashMap();

        j = "select new lk.gov.health.phsp.pojcs.ObservationValueCount(c.realNumberValue, count(c)) "
                + " from ClientEncounterComponentItem c "
                + " where (c.retired=:ret or c.retired is null) "
                + " and (c.item=:qi) ";
        if (sex != null) {
            j += " and c.encounter.client.person.sex.code=:s ";
            m.put("s", sex.getCode());
        }
        if (institution != null) {
            j += " and c.createInstitution=:ins ";
            m.put("ins", institution);
        }
        j += " and c.createdAt between :fd and :td "
                + " group by c.realNumberValue";
        m.put("ret", false);
        m.put("qi", queryItem);
        m.put("fd", getFromDate());
        m.put("td", getToDate());

        observationValueCounts = new ArrayList<>();
        //System.out.println("m = " + m);
        //System.out.println("j = " + j);
        List<Object> objs = clientFacade.findAggregates(j, m);
        if (objs == null) {
            return;
        }
        for (Object o : objs) {
            if (o instanceof ObservationValueCount) {
                ObservationValueCount ic = (ObservationValueCount) o;
                observationValueCounts.add(ic);
            }
        }

    }

    public DataFormset fillClinicalDataFormset(ClientEncounterComponentFormSet cfs) {
        //System.out.println("loadOldNavigateToDataEntry");
        if (cfs == null) {
            return null;
        }
        //System.out.println("cfs = " + cfs.getId());
        DesignComponentFormSet dfs = cfs.getReferanceDesignComponentFormSet();
        //System.out.println("dfs = " + dfs.getId());

        DataFormset fs = new DataFormset();

        Encounter e = cfs.getEncounter();

        fs.setDfs(dfs);
        fs.setEfs(cfs);

        List<DesignComponentForm> dfList = designComponentFormController.fillFormsofTheSelectedSet(dfs);

        int formCounter = 0;

        for (DesignComponentForm df : dfList) {
            // //System.out.println("df = " + df.getName());

            boolean skipThisForm = false;

            // //System.out.println("skipThisForm = " + skipThisForm);
            if (!skipThisForm) {
                formCounter++;
                String j = "select cf "
                        + " from ClientEncounterComponentForm cf "
                        + " where cf.referenceComponent=:rf "
                        + " and cf.parentComponent=:cfs "
                        + "order by cf.id desc";
                Map m = new HashMap();
                m.put("rf", df);
                m.put("cfs", cfs);
// // //System.out.println("df = " + df.getId());

                ClientEncounterComponentForm cf = clientEncounterComponentFormController.getClientEncounterComponentForm(j, m);

                // //System.out.println("cf = " + cf);
                if (cf == null) {
                    cf = new ClientEncounterComponentForm();

                    cf.setEncounter(e);
                    cf.setInstitution(dfs.getCurrentlyUsedIn());
                    cf.setItem(df.getItem());

                    cf.setReferenceComponent(df);
                    cf.setName(df.getName());
                    cf.setOrderNo(df.getOrderNo());
                    cf.setParentComponent(cfs);
                    cf.setCss(df.getCss());

                    clientEncounterComponentFormController.save(cf);
                }

                DataForm f = new DataForm();
                f.cf = cf;
                f.df = df;
                f.formset = fs;
                f.id = formCounter;
                f.orderNo = formCounter;

                List<DesignComponentFormItem> diList = designComponentFormItemController.fillItemsOfTheForm(df);

                int itemCounter = 0;

                for (DesignComponentFormItem dis : diList) {

                    // //System.out.println("dis = " + dis.getName());
                    boolean disSkipThisItem = false;

                    // //System.out.println("disSkipThisItem = " + disSkipThisItem);
                    if (!disSkipThisItem) {

                        if (dis.isMultipleEntiesPerForm()) {

                            // //System.out.println("dis.isMultipleEntiesPerForm() = " + dis.isMultipleEntiesPerForm());
                            j = "Select ci "
                                    + " from ClientEncounterComponentItem ci "
                                    + " where ci.retired=:ret "
                                    + " and ci.parentComponent=:cf "
                                    + " and ci.referenceComponent=:dis "
                                    + " order by ci.orderNo";
                            m = new HashMap();
                            m.put("ret", false);
                            m.put("cf", cf);
                            m.put("dis", dis);
                            // //System.out.println("cf = " + cf.getId());
                            // //System.out.println("dis = " + dis.getId());
                            List<ClientEncounterComponentItem> cis = clientEncounterComponentItemController.getItems(j, m);
                            // //System.out.println("cis = " + cis);

                            itemCounter++;
                            ClientEncounterComponentItem ci = new ClientEncounterComponentItem();

                            ci.setEncounter(e);
                            ci.setInstitution(dfs.getCurrentlyUsedIn());

                            ci.setItemFormset(cfs);
                            ci.setItemEncounter(e);
                            ci.setItemClient(e.getClient());

                            ci.setItem(dis.getItem());
                            ci.setDescreption(dis.getDescreption());

                            ci.setReferenceComponent(dis);
                            ci.setParentComponent(cf);
                            ci.setName(dis.getName());
                            ci.setCss(dis.getCss());
                            ci.setOrderNo(dis.getOrderNo());
                            ci.setDataRepresentationType(DataRepresentationType.Encounter);
                            DataItem i = new DataItem();
                            i.setMultipleEntries(true);
                            i.setCi(ci);
                            i.di = dis;
                            i.id = itemCounter;
                            i.orderNo = itemCounter;
                            i.form = f;

                            if (cis != null && !cis.isEmpty()) {
                                for (ClientEncounterComponentItem tci : cis) {
                                    DataItem di = new DataItem();
                                    di.setMultipleEntries(true);
                                    di.setCi(tci);
                                    di.di = dis;
                                    di.id = itemCounter;
                                    di.orderNo = tci.getOrderNo();
                                    di.form = f;
                                    i.getAddedItems().add(di);
                                }
                            }

                            f.getItems().add(i);

                        } else {

                            j = "Select ci "
                                    + " from ClientEncounterComponentItem ci "
                                    + " where ci.retired=:ret "
                                    + " and ci.parentComponent=:cf "
                                    + " and ci.referenceComponent=:dis "
                                    + " order by ci.orderNo";
                            m = new HashMap();
                            m.put("ret", false);
                            m.put("cf", cf);
                            m.put("dis", dis);
                            // //System.out.println("cf = " + cf.getId());
                            // //System.out.println("dis = " + dis.getId());
                            ClientEncounterComponentItem ci;
                            ci = clientEncounterComponentItemController.getItem(j, m);
                            // //System.out.println("ci = " + ci);
                            if (ci != null) {
                                DataItem i = new DataItem();
                                i.setMultipleEntries(false);
                                i.setCi(ci);
                                i.di = dis;
                                i.id = itemCounter;
                                i.orderNo = itemCounter;
                                i.form = f;

                                f.getItems().add(i);
                            } else {
                                itemCounter++;
                                ci = new ClientEncounterComponentItem();
                                ci.setEncounter(e);
                                ci.setInstitution(dfs.getCurrentlyUsedIn());
                                ci.setItemFormset(cfs);
                                ci.setItemEncounter(e);
                                ci.setItemClient(e.getClient());
                                ci.setItem(dis.getItem());
                                ci.setDescreption(dis.getDescreption());
                                ci.setReferenceComponent(dis);
                                ci.setParentComponent(cf);
                                ci.setName(dis.getName());
                                ci.setCss(dis.getCss());
                                ci.setOrderNo(dis.getOrderNo());
                                ci.setDataRepresentationType(DataRepresentationType.Encounter);

                                DataItem i = new DataItem();
                                i.setMultipleEntries(false);
                                i.setCi(ci);
                                i.di = dis;
                                i.id = itemCounter;
                                i.orderNo = itemCounter;
                                i.form = f;

                                f.getItems().add(i);
                            }

                        }

                    }

                }
                fs.getForms().add(f);
            }

        }
        return fs;
    }

    public DataFormset fillDesignComponantFormset(DesignComponentFormSet dfs) {
        DataFormset fs = new DataFormset();
        List<DesignComponentForm> dfList = designComponentFormController.fillFormsofTheSelectedSet(dfs);
        int formCounter = 0;
        for (DesignComponentForm df : dfList) {
            formCounter++;
            DataForm f = new DataForm();
            f.df = df;
            f.formset = fs;
            f.id = formCounter;
            f.orderNo = formCounter;
            List<DesignComponentFormItem> diList = designComponentFormItemController.fillItemsOfTheForm(df);
            int itemCounter = 0;
            for (DesignComponentFormItem dis : diList) {
                itemCounter++;
                DataItem i = new DataItem();
                i.setMultipleEntries(false);
                i.di = dis;
                i.id = itemCounter;
                i.orderNo = itemCounter;
                i.form = f;
                f.getItems().add(i);
            }
            fs.getForms().add(f);
        }
        return fs;
    }

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = CommonController.startOfTheDate();
        }
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = CommonController.endOfTheDate();
        }
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Item getQueryItem() {
        return queryItem;
    }

    public void setQueryItem(Item queryItem) {
        this.queryItem = queryItem;
    }

    public List<ObservationValueCount> getObservationValueCounts() {
        return observationValueCounts;
    }

    public void setObservationValueCounts(List<ObservationValueCount> observationValueCounts) {
        this.observationValueCounts = observationValueCounts;
    }

    public Item getSex() {
        return sex;
    }

    public void setSex(Item sex) {
        this.sex = sex;
    }

    public DesignComponentFormItem getDesignComponentFormItem() {
        return designComponentFormItem;
    }

    public void setDesignComponentFormItem(DesignComponentFormItem designComponentFormItem) {
        this.designComponentFormItem = designComponentFormItem;
    }

    public StreamedContent getResultExcelFile() {
        return resultExcelFile;
    }

    public void setResultExcelFile(StreamedContent resultExcelFile) {
        this.resultExcelFile = resultExcelFile;
    }

    public String toViewClinicalDataSingle() {
        String action = "/national/reports/clinical_data_single";
        return action;
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

}
