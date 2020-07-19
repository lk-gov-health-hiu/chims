/*
 * The MIT License
 *
 * Copyright 2020 buddhika.
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
package lk.gov.health.phsp.ejbs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import lk.gov.health.phsp.entity.Client;
import lk.gov.health.phsp.entity.ClientEncounterComponentItem;
import lk.gov.health.phsp.entity.Encounter;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.entity.QueryComponent;
import lk.gov.health.phsp.entity.Upload;
import lk.gov.health.phsp.enums.EncounterType;
import lk.gov.health.phsp.enums.QueryCriteriaMatchType;
import lk.gov.health.phsp.enums.QueryLevel;
import lk.gov.health.phsp.enums.QueryType;
import lk.gov.health.phsp.entity.StoredQueryResult;
import lk.gov.health.phsp.facade.ClientEncounterComponentItemFacade;
import lk.gov.health.phsp.facade.EncounterFacade;
import lk.gov.health.phsp.facade.QueryComponentFacade;
import lk.gov.health.phsp.facade.StoredQueryResultFacade;
import lk.gov.health.phsp.facade.UploadFacade;
import lk.gov.health.phsp.pojcs.Replaceable;
import lk.gov.health.phsp.pojcs.ReportTimePeriod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author buddhika
 */
@Stateless
public class ReportTimerSessionBean {

    private boolean processingReport = false;

    @EJB
    private StoredQueryResultFacade storeQueryResultFacade;
    @EJB
    private UploadFacade uploadFacade;
    @EJB
    private EncounterFacade encounterFacade;
    @EJB
    private QueryComponentFacade queryComponentFacade;
    @EJB
    private ClientEncounterComponentItemFacade clientEncounterComponentItemFacade;

    @Schedule(
            hour = "*",
            minute = "*",
            second = "10",
            persistent = false)
    public void runEveryMinute() {
        System.out.println("runEveryMinute = " + new Date());
        System.out.println("processingReport = " + processingReport);
        if (!processingReport) {
            runReports();
        }
    }

    public void runReports() {
        System.out.println("runReports");
        processingReport = true;
        String j;
        Map m = new HashMap();
        j = "select q from StoredQueryResult q "
                + " where q.retired=false "
                + " and q.processFailed=false "
                + " and q.processCompleted=false "
                + " and q.processStarted=false "
                + " order by q.id";
        List<StoredQueryResult> qs = getStoreQueryResultFacade().findByJpql(j);
        System.out.println("qs = " + qs);
        if (qs == null) {
            processingReport = false;
            return;
        }
        if (qs.isEmpty()) {
            processingReport = false;
            return;
        }

        for (StoredQueryResult q : qs) {
            q.setProcessStarted(true);
            q.setProcessStartedAt(new Date());
            q.setProcessFailed(false);
            q.setProcessCompleted(false);
            getStoreQueryResultFacade().edit(q);

            if (processReport(q)) {
                q.setProcessCompleted(true);
                q.setProcessCompletedAt(new Date());
                getStoreQueryResultFacade().edit(q);
            } else {
                q.setProcessFailed(true);
                q.setProcessFailedAt(new Date());
                getStoreQueryResultFacade().edit(q);
            }

        }
        processingReport = false;
    }

    private boolean processReport(StoredQueryResult sqr) {
        System.out.println("sqr = " + sqr);
        boolean success = false;

        QueryComponent queryComponent = sqr.getQueryComponent();
        Institution ins = sqr.getInstitution();
        ReportTimePeriod rtp = new ReportTimePeriod();
        rtp.setTimePeriodType(sqr.getTimePeriodType());
        rtp.setFrom(sqr.getResultFrom());
        rtp.setTo(sqr.getResultTo());
        rtp.setYear(sqr.getResultYear());
        rtp.setMonth(sqr.getResultMonth());
        rtp.setQuarter(sqr.getResultQuarter());
        rtp.setDateOfMonth(sqr.getResultDateOfMonth());

        if (queryComponent == null) {
            sqr.setErrorMessage("No report available.");
            getStoreQueryResultFacade().edit(sqr);
            return success;
        }

        if (queryComponent.getQueryType() == null) {
            sqr.setErrorMessage("No query type specified.");
            getStoreQueryResultFacade().edit(sqr);
            return success;
        }

        String j = "select u from Upload u "
                + " where u.component=:c";
        Map m = new HashMap();
        m.put("c", queryComponent);

        Upload upload = getUploadFacade().findFirstByJpql(j, m);
        if (upload == null) {
            sqr.setErrorMessage("No excel template uploaded.");
            getStoreQueryResultFacade().edit(sqr);
            return success;
        }

        List<Encounter> encs = null;
        List<Client> clnts = null;

        switch (queryComponent.getQueryType()) {
            case Encounter_Count:
                encs = findEncounters(rtp.getFrom(), rtp.getTo(), ins);
                break;
            case Client_Count:
                sqr.setErrorMessage("Client Queries not yet supported.");
                getStoreQueryResultFacade().edit(sqr);
                return success;
            default:
                sqr.setErrorMessage("This type of query not yet supported.");
                getStoreQueryResultFacade().edit(sqr);
                return success;
        }

        if (encs == null) {
            sqr.setErrorMessage("No Data.");
            getStoreQueryResultFacade().edit(sqr);
            return success;
        } else if (encs.size() < 1) {
            sqr.setErrorMessage("No Data.");
            getStoreQueryResultFacade().edit(sqr);
            return success;
        }

        String FILE_NAME = upload.getFileName() + "_" + (new Date()) + ".xlsx";

        File newFile = new File(FILE_NAME);

        try {
            FileUtils.writeByteArrayToFile(newFile, upload.getBaImage());
        } catch (IOException ex) {
            sqr.setErrorMessage("IO Exception. " + ex.getMessage());
            getStoreQueryResultFacade().edit(sqr);
        }

        XSSFWorkbook workbook;
        XSSFSheet sheet;

        try {

            FileInputStream excelFile = new FileInputStream(newFile);
            workbook = new XSSFWorkbook(excelFile);
            sheet = workbook.getSheetAt(0);
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

                InputStream is;

                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    workbook.write(bos);
                    byte[] barray = bos.toByteArray();
                    is = new ByteArrayInputStream(barray);
                } catch (IOException e) {
                    sqr.setErrorMessage("IO Exception. " + e.getMessage());
                    getStoreQueryResultFacade().edit(sqr);
                    return success;
                }

                System.out.println("1 = " + 1);
                excelFile.close();
                System.out.println("2 = " + 2);

                Upload u = new Upload();
                u.setFileName(newFile.getName());
                u.setCreatedAt(new Date());
                u.setBaImage(IOUtils.toByteArray(is));

                getUploadFacade().create(u);

                System.out.println("5 = " + 5);

                sqr.setUpload(upload);
                getStoreQueryResultFacade().edit(sqr);
                System.out.println("6 = " + 6);

            }
        } catch (FileNotFoundException e) {
            sqr.setErrorMessage("IO Exception. " + e.getMessage());
            getStoreQueryResultFacade().edit(sqr);
            return success;
        } catch (IOException e) {
            sqr.setErrorMessage("IO Exception. " + e.getMessage());
            getStoreQueryResultFacade().edit(sqr);
            return success;
        }

        success = true;
        return success;

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
            QueryComponent qc = findQueryComponentByCode(block);
            if (qc == null) {
                System.out.println("No Such Query = ");
                l = null;
                return l;

            } else {
                System.out.println("qc.getQueryType() = " + qc.getQueryType());
                if (qc.getQueryType() == QueryType.Encounter_Count) {
                    List<QueryComponent> criteria = findCriteriaForQueryComponent(qc);
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

    public QueryComponent findQueryComponentByCode(String code) {
        String j = "select q from QueryComponent q "
                + " where q.retired <> :ret "
                + " and q.code=:code";
        Map m = new HashMap();
        m.put("ret", true);
        m.put("code", code);
        return getQueryComponentFacade().findFirstByJpql(j, m);
    }

    public Long findMatchingCount(List<Encounter> encs, List<QueryComponent> qrys) {
        System.out.println("findMatchingCount");
        Long c = 0l;
        for (Encounter e : encs) {
            List<ClientEncounterComponentItem> is = findClientEncounterComponentItems(e);
            boolean suitableForInclusion = true;
            for (QueryComponent q : qrys) {
                System.out.println("query = " + q.getName());
                boolean thisMatchOk = false;
                for (ClientEncounterComponentItem i : is) {
                    if (i.getItem() == null || q.getItem() == null) {
                        continue;
                    }
                    if (i.getItem().getCode().trim().equalsIgnoreCase(q.getItem().getCode().trim())) {
                        System.out.println("i.getItem().getCode() = " + i.getItem().getCode());
                        if (matchQuery(q, i)) {
                            thisMatchOk = true;
                        }
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

    public boolean matchQuery(QueryComponent q, ClientEncounterComponentItem clientValue) {
        boolean m = false;
        Integer qInt1 = null;
        Integer qInt2 = null;
        Double real1 = null;
        Double real2 = null;
        Long lng1 = null;
        Long lng2 = null;
        Item itemVariable = null;
        Item itemValue = null;

        if (q.getMatchType() == QueryCriteriaMatchType.Variable_Value_Check) {
            switch (q.getQueryDataType()) {
                case integer:
                    qInt1 = q.getIntegerNumberValue();
                    qInt2 = q.getIntegerNumberValue2();
                    System.out.println("Query int1 = " + qInt1);
                    System.out.println("Query int2 = " + qInt2);
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
                    System.out.println("Equal");

                    if (qInt1 != null) {
                        m = qInt1.equals(clientValue.getIntegerNumberValue());
                    }
                    if (lng1 != null) {
                        m = lng1.equals(clientValue.getLongNumberValue());
                    }
                    if (real1 != null) {
                        m = real1.equals(clientValue.getRealNumberValue());
                    }

                    if (itemValue != null && itemVariable != null) {
                        if (clientValue != null
                                && itemValue.getCode() != null
                                && clientValue.getItemValue() != null
                                && clientValue.getItemValue().getCode() != null) {

                            if (itemValue.getCode().equals(clientValue.getItemValue().getCode())) {
                                m = true;
                            }
                        }
                    }
                    break;
                case Less_than:
                    System.out.println("Less than");
                    System.out.println("Client Value = " + clientValue.getIntegerNumberValue());
                    if (qInt1 != null && clientValue.getIntegerNumberValue() != null) {
                        m = clientValue.getIntegerNumberValue() < qInt1;
                    }
                    if (lng1 != null && clientValue.getLongNumberValue() != null) {
                        m = clientValue.getLongNumberValue() < lng1;
                    }
                    if (real1 != null && clientValue.getRealNumberValue() != null) {
                        m = clientValue.getRealNumberValue() < real1;
                    }
                    System.out.println("Included = " + m);
                    break;
                case Between:
                    System.out.println("Between");
                    System.out.println("Client Value = " + clientValue.getIntegerNumberValue());
                    if (qInt1 != null && qInt2 != null && clientValue.getIntegerNumberValue() != null) {
                        if (qInt1 > qInt2) {
                            Integer intTem = qInt1;
                            qInt1 = qInt2;
                            qInt2 = intTem;
                        }
                        if (clientValue.getIntegerNumberValue() > qInt1 && clientValue.getIntegerNumberValue() < qInt2) {
                            m = true;
                        }
                    }
                    if (lng1 != null && lng2 != null && clientValue.getLongNumberValue() != null) {
                        if (lng1 > lng2) {
                            Long intTem = lng1;
                            intTem = lng1;
                            lng1 = lng2;
                            lng2 = intTem;
                        }
                        if (clientValue.getLongNumberValue() > lng1 && clientValue.getLongNumberValue() < lng2) {
                            m = true;
                        }
                    }
                    if (real1 != null && real2 != null && clientValue.getRealNumberValue() != null) {
                        if (real1 > real2) {
                            Double realTem = real1;
                            realTem = real1;
                            real1 = real2;
                            real2 = realTem;
                        }
                        if (clientValue.getRealNumberValue() > real1 && clientValue.getRealNumberValue() < real2) {
                            m = true;
                        }
                    }
                    break;
                case Grater_than:
                    System.out.println("Grater than");
                    System.out.println("Client Value = " + clientValue.getIntegerNumberValue());
                    if (qInt1 != null && clientValue.getIntegerNumberValue() != null) {
                        m = clientValue.getIntegerNumberValue() > qInt1;
                    }
                    if (real1 != null && clientValue.getRealNumberValue() != null) {
                        m = clientValue.getRealNumberValue() > real1;
                    }
                    break;
                case Grater_than_or_equal:
                    if (qInt1 != null && clientValue.getIntegerNumberValue() != null) {
                        m = clientValue.getIntegerNumberValue() < qInt1;
                    }
                    if (real1 != null && clientValue.getRealNumberValue() != null) {
                        m = clientValue.getRealNumberValue() < real1;
                    }
                case Less_than_or_equal:
                    if (qInt1 != null && clientValue.getIntegerNumberValue() != null) {
                        m = clientValue.getIntegerNumberValue() >= qInt1;
                    }
                    if (real1 != null && clientValue.getRealNumberValue() != null) {
                        m = clientValue.getRealNumberValue() >= real1;
                    }
                    break;
            }
        }
        System.out.println("Included= " + m);
        return m;
    }

    public List<ClientEncounterComponentItem> findClientEncounterComponentItems(Encounter enc) {
        String j = "select f from ClientEncounterComponentItem f "
                + " where f.retired=false "
                + " and f.encounter=:e";
        Map m = new HashMap();
        m.put("e", enc);
        List<ClientEncounterComponentItem> t = getClientEncounterComponentItemFacade().findByJpql(j, m);
        if (t == null) {
            t = new ArrayList<>();
        }
        return t;
    }

    public List<QueryComponent> findCriteriaForQueryComponent(QueryComponent p) {
        //System.out.println("finding criteria");
        //System.out.println("p = " + p);
        String j = "select q from QueryComponent q "
                + " where q.retired=false "
                + " and q.queryLevel =:l "
                + " and q.parentComponent =:p "
                + " order by q.name";
        Map m = new HashMap();
        m.put("p", p);
        m.put("l", QueryLevel.Criterian);
        List<QueryComponent> c = getQueryComponentFacade().findByJpql(j, m);
        //System.out.println("c = " + c);
        return c;
    }

    public StoredQueryResultFacade getStoreQueryResultFacade() {
        return storeQueryResultFacade;
    }

    public boolean isProcessingReport() {
        return processingReport;
    }

    public void setProcessingReport(boolean processingReport) {
        this.processingReport = processingReport;
    }

    public UploadFacade getUploadFacade() {
        return uploadFacade;
    }

    public EncounterFacade getEncounterFacade() {
        return encounterFacade;
    }

    public QueryComponentFacade getQueryComponentFacade() {
        return queryComponentFacade;
    }

    public ClientEncounterComponentItemFacade getClientEncounterComponentItemFacade() {
        return clientEncounterComponentItemFacade;
    }

}
