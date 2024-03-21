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
package lk.gov.health.phsp.bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
import lk.gov.health.phsp.entity.ClientEncounterComponentItem;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.entity.QueryComponent;
import lk.gov.health.phsp.entity.StoredQueryResult;
import lk.gov.health.phsp.entity.Upload;
import lk.gov.health.phsp.enums.EncounterType;
import lk.gov.health.phsp.enums.QueryCriteriaMatchType;
import lk.gov.health.phsp.enums.QueryLevel;
import lk.gov.health.phsp.facade.ClientEncounterComponentItemFacade;
import lk.gov.health.phsp.facade.ConsolidatedQueryResultFacade;
import lk.gov.health.phsp.facade.EncounterFacade;
import lk.gov.health.phsp.facade.QueryComponentFacade;
import lk.gov.health.phsp.facade.StoredQueryResultFacade;
import lk.gov.health.phsp.facade.UploadFacade;
import lk.gov.health.phsp.pojcs.EncounterWithBasicComponents;
import lk.gov.health.phsp.pojcs.EncounterWithComponents;
import lk.gov.health.phsp.pojcs.QueryWithCriteria;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author buddhika
 */
@Named
@SessionScoped
public class ExcelReportController implements Serializable {

    private final boolean logActivity = true;
    private List<EncounterWithComponents> encountersWithComponents;
    private List<QueryWithCriteria> queriesWithCriteria;
    StoredQueryResult storedQueryResult;
    private List<QueryComponent> queryComponents;

    String checkingString = "dsfsdfsdfds";
    boolean needCheckLogin = false;

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
    @EJB
    private ConsolidatedQueryResultFacade consolidatedQueryResultFacade;

    @Inject
    StoredQueryResultController storedQueryResultController;
    @Inject
    ApplicationController applicationController;

    /**
     * Creates a new instance of ExcelReportController
     */
    public ExcelReportController() {
    }

    public boolean processReport(StoredQueryResult storedQueryResult) {
        if (logActivity) {

        }

        if (storedQueryResult != null) {
            updateStoredQueryOnStartProcessing(storedQueryResult);
            List<Long> encounterIds = findEncounterIds(storedQueryResult.getResultFrom(),
                    storedQueryResult.getResultTo(),
                    storedQueryResult.getInstitution());
            encountersWithComponents = findEncountersWithComponents(encounterIds);
            if (encountersWithComponents == null) {
                updateOnNoData(storedQueryResult);
                return false;
            }
            queriesWithCriteria = findQueriesWithCriteria(storedQueryResult);
            if (queriesWithCriteria == null) {
                updateOnNoQueries(storedQueryResult);
                return false;
            }
            boolean success = generateRecordFileAfterConsolidation(storedQueryResult,
                    encountersWithComponents,
                    queriesWithCriteria);
            if (success) {
                updateOnSuccess(storedQueryResult);
//                JsfUtil.addSuccessMessage("Report Created. Download from Below");
                encountersWithComponents = null;
                queriesWithCriteria = null;
                return true;
            } else {
                JsfUtil.addErrorMessage("Report Error. Please check details.");
                encountersWithComponents = null;
                queriesWithCriteria = null;
                return false;
            }
        } else {
            JsfUtil.addErrorMessage("No Report Selected");
            encountersWithComponents = null;
            queriesWithCriteria = null;
            return false;
        }

    }

    public List<EncounterWithComponents> findEncountersWithComponents(List<Long> ids) {
        if (logActivity) {

        }
        if (ids == null) {
            if (logActivity) {

            }
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

    public void updateStoredQueryOnStartProcessing(StoredQueryResult q) {
        q.setProcessStarted(true);
        q.setProcessStartedAt(new Date());
        q.setProcessFailed(false);
        q.setProcessCompleted(false);
        getStoreQueryResultFacade().edit(q);
        q.setSubmittedForConsolidation(true);
        q.setSubmittedForConsolidationAt(new Date());
        getStoreQueryResultFacade().edit(q);
    }

    public void updateOnSuccess(StoredQueryResult q) {
        q.setProcessCompleted(true);
        q.setProcessCompletedAt(new Date());
        getStoreQueryResultFacade().edit(q);
    }

    public void updateOnFailure(StoredQueryResult q) {
        q.setProcessFailed(true);
        q.setProcessFailedAt(new Date());
        getStoreQueryResultFacade().edit(q);
    }

    public void updateOnNoData(StoredQueryResult q) {
        q.setErrorMessage("No Data");
        q.setProcessFailed(true);
        q.setProcessFailedAt(new Date());
        getStoreQueryResultFacade().edit(q);
    }

    public void updateOnNoQueries(StoredQueryResult q) {
        q.setErrorMessage("No Queries in report file.");
        q.setProcessFailed(true);
        q.setProcessFailedAt(new Date());
        getStoreQueryResultFacade().edit(q);
    }

    private List<QueryWithCriteria> findQueriesWithCriteria(StoredQueryResult sqr) {
        if (logActivity) {

        }
        if (sqr == null) {
            getStoreQueryResultFacade().edit(sqr);
            return null;
        }
        List<QueryWithCriteria> qs = new ArrayList<>();

        String j = "select u from Upload u "
                + " where u.component=:c";
        Map m = new HashMap();
        m.put("c", sqr.getQueryComponent());

        Upload upload = getUploadFacade().findFirstByJpql(j, m);
        if (upload == null) {
            sqr.setErrorMessage("No excel template uploaded.");
            getStoreQueryResultFacade().edit(sqr);
            return null;
        }

        String FILE_NAME = upload.getFileName() + "_" + (new Date()) + ".xlsx";

        String folder = "/tmp/";

        File newFile = new File(folder + FILE_NAME);

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

                    CellType ct = currentCell.getCellType();

                    if (ct == null) {
                        continue;
                    }

                    switch (ct) {
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
                        QueryComponent qc = findQueryComponentByCellString(cellString);
                        if (qc != null) {
                            QueryWithCriteria qwc = new QueryWithCriteria();
                            qwc.setQuery(qc);
                            qwc.setCriteria(findCriteriaForQueryComponent(qc.getCode()));
                            qs.add(qwc);
                        } else {
                            if (logActivity) {

                            }
                        }
                    }
                }
            }
            excelFile.close();
        } catch (FileNotFoundException e) {
            sqr.setErrorMessage("IO Exception. " + e.getMessage());
            getStoreQueryResultFacade().edit(sqr);
            return null;
        } catch (IOException e) {
            sqr.setErrorMessage("IO Exception. " + e.getMessage());
            getStoreQueryResultFacade().edit(sqr);
            return null;
        }
        return qs;
    }

    private boolean generateRecordFileAfterConsolidation(StoredQueryResult sqr,
            List<EncounterWithComponents> ewcs,
            List<QueryWithCriteria> qwcs) {

        if (logActivity) {

        }

        Boolean success = false;
        if (sqr == null) {
            return success;
        }

        String j = "select u from Upload u "
                + " where u.component=:c";
        Map m = new HashMap();
        m.put("c", sqr.getQueryComponent());

        Upload upload = getUploadFacade().findFirstByJpql(j, m);
        if (upload == null) {
            sqr.setErrorMessage("No excel template uploaded.");
            getStoreQueryResultFacade().edit(sqr);
            return success;
        }

        String FILE_NAME = upload.getFileName() + "_" + (new Date()) + ".xlsx";

        String folder = "/tmp/";

        File newFile = new File(folder + FILE_NAME);

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

                    CellType ct = currentCell.getCellType();

                    if (ct == null) {
                        continue;
                    }

                    switch (ct) {
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
                        if (cellString.equals("#{report_institute}")) {
                            if (sqr.getInstitution() != null) {
                                currentCell.setCellValue(sqr.getInstitution().getName());
                            }
                        } else if (cellString.equals("#{report_period}")) {
                            currentCell.setCellValue(sqr.getPeriodString());
                        } else if (cellString.equals("#{report_dates_count}")) {
                            Long tds = findCountOfEncounterDates(sqr.getResultFrom(),
                                    sqr.getResultTo(),
                                    sqr.getInstitution());
                            if (tds != null) {
                                currentCell.setCellValue(tds);
                            }
                        } else {
                            if (logActivity) {

                            }
                            String qryCode = findQueryComponentCodeByCellString(cellString);
                            if (logActivity) {

                            }
                            if (qryCode != null) {
                                QueryWithCriteria qwc = findQwcFromQwcs(qwcs, qryCode);
                                Long value = null;
                                boolean featchAgain = true;
                                if (sqr.isRecalculate()) {
                                    featchAgain = true;
                                } else {

                                    if (qwc == null) {
                                        // //// System.out.println("No Query with criteria");
                                    } else if (qwc.getQuery() == null) {
                                        // //// System.out.println("No Query for Query with criteria");
                                    } else if (sqr.getResultTo() == null) {
                                        // //// System.out.println("sqr.getResultTo() null");
                                    } else if (sqr.getResultFrom() == null) {
                                        // //// System.out.println("sqr.getResultFrom() null");
                                    } else if (sqr.getInstitution() == null) {
                                        // //// System.out.println("sqr.getResultFrom() null");
                                    } else {
                                        value = storedQueryResultController.findStoredLongValue(qwc.getQuery(), sqr.getResultFrom(), sqr.getResultTo(), sqr.getInstitution());
                                    }

                                    if (value == null) {
                                        featchAgain = true;
                                    }
                                }

                                if (featchAgain) {
                                    value = calculateIndividualQueryResult(ewcs, qwc);
                                    if (value != null) {
                                        if (qwc == null) {
                                            // //// System.out.println("No Query with criteria");
                                        } else if (qwc.getQuery() == null) {
                                            // //// System.out.println("No Query for Query with criteria");
                                        } else if (sqr.getResultTo() == null) {
                                            // //// System.out.println("sqr.getResultTo() null");
                                        } else if (sqr.getResultFrom() == null) {
                                            // //// System.out.println("sqr.getResultFrom() null");
                                        } else if (sqr.getInstitution() == null) {
                                            // //// System.out.println("sqr.getResultFrom() null");
                                        } else {
                                            storedQueryResultController.saveValue(qwc.getQuery(), sqr.getResultFrom(), sqr.getResultTo(), sqr.getInstitution(), value);
                                        }
                                    }
                                }
                                if (value != null) {
                                    currentCell.setCellValue(value);
                                }
                            } else {

                                currentCell.setCellValue("");
                            }

                        }
                    }

                }

            }

            excelFile.close();
            FileOutputStream out = new FileOutputStream(FILE_NAME);
            workbook.write(out);
            out.close();

            InputStream stream;
            stream = new FileInputStream(FILE_NAME);

            Upload u = new Upload();
            u.setFileName(FILE_NAME);
            u.setFileType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            u.setCreatedAt(new Date());

            getUploadFacade().create(u);

            byte[] byteArray = IOUtils.toByteArray(stream);
            u.setBaImage(byteArray);

            sqr.setUpload(u);
            getStoreQueryResultFacade().edit(sqr);
            getUploadFacade().edit(u);
            for (int i = 0; i < 10000; i++) {

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

    private Long calculateIndividualQueryResult(List<EncounterWithComponents> ewcs, QueryWithCriteria qwc) {
        Long result = 0l;
        if (ewcs == null) {
            if (logActivity) {

            }
            return result;
        }
        if (qwc == null) {
            if (logActivity) {

            }
            return result;
        }
        List<QueryComponent> criteria = qwc.getCriteria();

        if (qwc.getQuery() != null && qwc.getQuery().getCode() != null) {
            needCheckLogin = qwc.getQuery().getCode().equals(checkingString);
        } else {
            needCheckLogin = false;
        }

        if (criteria == null || criteria.isEmpty()) {
            Integer ti = ewcs.size();
            result = ti.longValue();
            if (needCheckLogin) {

            }
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

    private String evaluateScript(String script) {
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        try {
            return engine.eval(script) + "";
        } catch (ScriptException ex) {

            return null;
        }
    }

    private boolean findMatch(List<ClientEncounterComponentItem> ccs, QueryWithCriteria qrys) {
        if (needCheckLogin) {

        }
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

        if (needCheckLogin) {

        }

        boolean isComplexQuery = false;

        for (QueryComponent qc : qrys.getCriteria()) {
            if (qc.getMatchType() == null) {
                JsfUtil.addErrorMessage("No match type for the query " + qc.getName());
                continue;
            }
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
                        if (logActivity) {

                        }
                        continue;
                    }
                    if (qc.getItem().getCode() == null) {
                        if (logActivity) {

                        }
                        continue;
                    }
                    for (ClientEncounterComponentItem cei : ccs) {
                        if (needCheckLogin) {

                        }

                        if (cei.getItem() == null) {
                            if (logActivity) {

                            }
                            continue;
                        }
                        if (cei.getItem().getCode() == null) {
                            if (logActivity) {

                            }
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
                    if (logActivity) {

                    }
                    continue;
                }
                if (qc.getItem().getCode() == null) {
                    if (logActivity) {

                    }
                    continue;
                }

                boolean thisMatchOk = false;
                boolean componentFound = false;

                for (ClientEncounterComponentItem cei : ccs) {
                    if (needCheckLogin) {

                    }

                    if (cei.getItem() == null) {
                        if (logActivity) {

                        }
                        continue;
                    }
                    if (cei.getItem().getCode() == null) {
                        if (logActivity) {

                        }
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
                    if (logActivity) {

                        for (ClientEncounterComponentItem ci : ccs) {

                        }
                        for (QueryComponent tqc : qrys.getCriteria()) {

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

    private boolean findMatchOld(List<ClientEncounterComponentItem> ccs, QueryWithCriteria qrys) {
        if (needCheckLogin) {

        }
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

        if (needCheckLogin) {

        }

        for (QueryComponent qc : qrys.getCriteria()) {
            if (qc.getItem() == null) {
                if (logActivity) {

                }
                continue;
            }
            if (qc.getItem().getCode() == null) {
                if (logActivity) {

                }
                continue;
            }

            boolean thisMatchOk = false;
            boolean componentFound = false;

            for (ClientEncounterComponentItem cei : ccs) {
                if (needCheckLogin) {

                }

                if (cei.getItem() == null) {
                    if (logActivity) {

                    }
                    continue;
                }
                if (cei.getItem().getCode() == null) {
                    if (logActivity) {

                    }
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
                if (logActivity) {

                    for (ClientEncounterComponentItem ci : ccs) {

                    }
                    for (QueryComponent tqc : qrys.getCriteria()) {

                    }
                }
            }
            if (!thisMatchOk) {
                suitableForInclusion = false;
            }
        }

        return suitableForInclusion;
    }

    private boolean matchQuery(QueryComponent q, ClientEncounterComponentItem clientValue) {
        if (needCheckLogin) {

        }
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
            if (needCheckLogin) {

            }
            switch (q.getQueryDataType()) {
                case integer:
                    if (needCheckLogin) {

                    }

                    qInt1 = q.getIntegerNumberValue();
                    qInt2 = q.getIntegerNumberValue2();
                    if (needCheckLogin) {

                    }
                    if (needCheckLogin) {

                    }
                    break;
                case item:
                    if (needCheckLogin && clientValue.getItem() != null) {
//                        
                    }
                    if (needCheckLogin && clientValue.getItemValue() != null) {
//                        
                    } else {
//                        
                    }

                    itemValue = q.getItemValue();
                    itemVariable = q.getItem();
                    break;
                case real:
                    if (needCheckLogin) {

                    }

                    real1 = q.getRealNumberValue();
                    real2 = q.getRealNumberValue2();
                    break;
                case longNumber:
                    if (needCheckLogin) {

                    }
                    lng1 = q.getLongNumberValue();
                    lng2 = q.getLongNumberValue2();
                    break;
                case Boolean:
                    if (needCheckLogin) {

                    }
                    qBool = q.getBooleanValue();
                    break;
                case String:
                    qStr = q.getShortTextValue();
                    break;

            }
            if (needCheckLogin) {

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
        if (needCheckLogin) {

        }
        if (q.getMatchType() == QueryCriteriaMatchType.Variable_Value_Check) {
            switch (q.getQueryDataType()) {
                case integer:
                    if (needCheckLogin) {

                    }
                    if (clientValue.getIntegerNumberValue() != null) {
                        valueNotNull = true;
                    }
                    break;
                case item:
                    if (needCheckLogin) {

                    }
                    if (clientValue.getItemValue() != null) {
                        valueNotNull = true;
                    }
                    break;
                case real:
                    if (needCheckLogin) {

                    }
                    if (clientValue.getRealNumberValue() != null) {
                        valueNotNull = true;
                    }
                    break;
                case longNumber:
                    if (needCheckLogin) {

                    }
                    if (clientValue.getLongNumberValue() != null) {
                        valueNotNull = true;
                    }
                    break;
                case Boolean:
                    if (needCheckLogin) {

                    }
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

    private List<Long> findEncounterIds(Date fromDate, Date toDate, Institution institution) {
        if (logActivity) {

        }
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

    private Long findCountOfEncounterDates(Date fromDate, Date toDate, Institution institution) {
        if (logActivity) {

        }
        String j = "select count(e.encounterDate) "
                + " from  ClientEncounterComponentFormSet f join f.encounter e"
                + " where e.retired<>:er"
                + " and f.retired<>:fr ";
        j += " and f.completed=:fc ";
        j += " and e.institution=:i "
                + " and e.encounterType=:t "
                + " and e.encounterDate between :fd and :td "
                + " group by e.encounterDate";
        Map m = new HashMap();
        m.put("i", institution);
        m.put("t", EncounterType.Clinic_Visit);
        m.put("er", true);
        m.put("fr", true);
        m.put("fc", true);
        m.put("fd", fromDate);
        m.put("td", toDate);

        Long encs = encounterFacade.findLongByJpql(j, m);
        return encs;
    }

    private List<ClientEncounterComponentItem> findClientEncounterComponentItems(Long endId) {
        if (logActivity) {

        }
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

    public long findClientCountEncounterComponentItemMatchCount(
            List<Institution> ins,
            Date fromDate,
            Date toDate,
            String itemCode,
            List<String> valueStrings) {
        if (logActivity) {

        }
        String j;
        Map m;
        m = new HashMap();
        j = "select count(f.encounter) "
                + " from ClientEncounterComponentItem f "
                + " where f.retired<>:ret "
                + " and f.encounter.retired<>:ret ";
        j += " and f.item.code=:ic ";
        j += " and f.shortTextValue in :ivs";
        m.put("ic", itemCode);
        m.put("ret", true);
        m.put("ivs", valueStrings);
        if (ins != null && !ins.isEmpty()) {
            m.put("ins", ins);
            j += " and f.encounter.institution in :ins ";
        }
        if (fromDate != null && toDate != null) {
            m.put("fd", fromDate);
            m.put("td", toDate);
            j += " and f.encounter.encounterDate between :fd and :td ";
        }
//        j += " group by e";
        Long count = getClientEncounterComponentItemFacade().findLongByJpql(j, m);

        long val;
        if (count != null) {
            val = (long) count;
        } else {
            val = 0l;
        }
        return val;
    }

    private String currentTimeAsString() {
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
        String strDate = dateFormat.format(date);
        return strDate;
    }

    private QueryComponent findQueryComponentByCellString(String text) {
        String str;

        String patternStart = "#{";
        String patternEnd = "}";
        String regexString = Pattern.quote(patternStart) + "(.*?)" + Pattern.quote(patternEnd);

        Pattern p = Pattern.compile(regexString);

        Matcher m = p.matcher(text);
        QueryComponent qc = null;
        while (m.find()) {
            String block = m.group(1);
            str = block;
            QueryComponent tqc = findQueryComponentByCode(block);
            if (tqc != null) {
                qc = tqc;
            }
        }
        return qc;
    }

    private String findQueryComponentCodeByCellString(String text) {

        String str = null;
        String patternStart = "#{";
        String patternEnd = "}";
        String regexString = Pattern.quote(patternStart) + "(.*?)" + Pattern.quote(patternEnd);
        Pattern p = Pattern.compile(regexString);
        Matcher m = p.matcher(text);
        while (m.find()) {
            String block = m.group(1);
            str = block;

        }

        return str;
    }

    private QueryComponent findQueryComponentByCode(String code) {
        if (code == null) {
            return null;
        }
        for (QueryComponent qc : getQueryComponents()) {
            if (qc.getCode() == null) {
                continue;
            }
            if (qc.getCode().trim().equals(code.trim())) {
                return qc;
            }
        }
        return null;
    }

    private List<QueryComponent> findCriteriaForQueryComponent(String qryCode) {
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

    public boolean isLogActivity() {
        return logActivity;
    }

    public List<EncounterWithComponents> getEncountersWithComponents() {
        return encountersWithComponents;
    }

    public List<QueryWithCriteria> getQueriesWithCriteria() {
        return queriesWithCriteria;
    }

    public StoredQueryResultFacade getStoreQueryResultFacade() {
        return storeQueryResultFacade;
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

    public ConsolidatedQueryResultFacade getConsolidatedQueryResultFacade() {
        return consolidatedQueryResultFacade;
    }

    private List<QueryComponent> getQueryComponents() {
        if (queryComponents == null) {
            queryComponents = findAllQueryComponents();
        }

        return queryComponents;
    }

    private List<QueryComponent> findAllQueryComponents() {
        return applicationController.getQueryComponents();
    }

    private QueryWithCriteria findQwcFromQwcs(List<QueryWithCriteria> qwcs, String qryCode) {

        QueryWithCriteria q = null;

        for (QueryWithCriteria tq : qwcs) {
            if (tq.getQuery() != null) {
                if (tq.getQuery().getCode() != null) {
                    if (qryCode.equalsIgnoreCase(tq.getQuery().getCode())) {
                        q = tq;
                    }
                } else {
                    if (logActivity) {

                    }
                }
            } else {
                if (logActivity) {

                }
            }
        }
        return q;
    }

}
