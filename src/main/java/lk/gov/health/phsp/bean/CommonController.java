/*
 * Author : Dr. M H B Ariyaratne
 *
 * MO(Health Information), Department of Health Services, Southern Province
 * and
 * Email : buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.bean;

import lk.gov.health.phsp.enums.AreaType;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.enums.ItemType;
import lk.gov.health.phsp.enums.WebUserRole;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import lk.gov.health.phsp.entity.SecurityProtocol;
import lk.gov.health.phsp.enums.ComponentSetType;
import lk.gov.health.phsp.enums.ComponentSex;
import lk.gov.health.phsp.enums.DataCompletionStrategy;
import lk.gov.health.phsp.enums.DataModificationStrategy;
import lk.gov.health.phsp.enums.DataPopulationStrategy;
import lk.gov.health.phsp.enums.ItemArrangementStrategy;
import lk.gov.health.phsp.enums.Month;
import lk.gov.health.phsp.enums.PanelType;
import lk.gov.health.phsp.enums.Quarter;
import lk.gov.health.phsp.enums.QueryCriteriaMatchType;
import lk.gov.health.phsp.enums.QueryDataType;
import lk.gov.health.phsp.enums.QueryFilterAreaType;
import lk.gov.health.phsp.enums.QueryFilterPeriodType;
import lk.gov.health.phsp.enums.QueryLevel;
import lk.gov.health.phsp.enums.QueryOutputType;
import lk.gov.health.phsp.enums.QueryType;
import lk.gov.health.phsp.enums.QueryVariableEvaluationType;
import lk.gov.health.phsp.enums.RelationshipType;
import lk.gov.health.phsp.enums.RenderType;
import lk.gov.health.phsp.enums.SearchCriteria;
import lk.gov.health.phsp.enums.SelectionDataType;
import lk.gov.health.phsp.enums.TimePeriodType;
import lk.gov.health.phsp.pojcs.TimePeriod;
import org.jasypt.util.password.BasicPasswordEncryptor;
import org.jasypt.util.text.BasicTextEncryptor;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 * Informatics)
 */
@Named
@SessionScoped
public class CommonController implements Serializable {

    private static final long serialVersionUID = 1L;

    static SelectionDataType selectionDataTypeFromString(String strDataType) {
        SelectionDataType[] dds = SelectionDataType.values();
        for(SelectionDataType sdt:dds){
            if(sdt.getLabel().equals(strDataType)){
                return sdt;
            }
        }
        return null;
    }

    /**
     * Creates a new instance of HOSecurity
     */
    public CommonController() {
    }

    public List<String> getExcelColumns() {
        List<String> cs = new ArrayList<>();
        cs.add("A");
        cs.add("B");
        cs.add("C");
        cs.add("D");
        cs.add("E");
        cs.add("F");
        cs.add("G");
        cs.add("H");
        cs.add("I");
        cs.add("J");
        cs.add("K");
        cs.add("L");
        cs.add("M");
        cs.add("N");
        cs.add("O");
        cs.add("P");
        cs.add("Q");
        cs.add("R");
        cs.add("S");
        cs.add("T");
        cs.add("U");
        cs.add("V");
        cs.add("W");
        cs.add("X");
        cs.add("Y");
        cs.add("Z");
        return cs;
    }

    public static Integer excelColFromHeader(String columnName) {
        if (columnName == null) {
            return null;
        }
        columnName = columnName.toUpperCase();
        switch (columnName) {
            case "A":
                return 0;
            case "B":
                return 1;
            case "C":
                return 2;
            case "D":
                return 3;
            case "E":
                return 4;
            case "F":
                return 5;
            case "G":
                return 6;
            case "H":
                return 7;
            case "I":
                return 8;
            case "J":
                return 9;
            case "K":
                return 10;
            case "L":
                return 11;
            case "M":
                return 12;
            case "N":
                return 13;
            case "O":
                return 14;
            case "P":
                return 15;
            case "Q":
                return 16;
            case "R":
                return 17;
            case "S":
                return 18;
            case "T":
                return 19;
            case "U":
                return 20;
        }
        return null;
    }

    public static String formatDate() {
        Date date = Calendar.getInstance().getTime();
        return formatDate(date);
    }

    public static String formatDate(Date date, String formatString) {
        DateFormat dateFormat = new SimpleDateFormat(formatString);
        String strDate = dateFormat.format(date);
        return strDate;
    }

    public static String formatDate(String formatString) {
        Date date = Calendar.getInstance().getTime();
        return formatDate(date, formatString);
    }

    public static String formatDate(Date date) {
        String formatString = "dd MMMM yyyy";
        return formatDate(date, formatString);
    }

    public static Integer stringToInteger(String str) {
        Integer outInt;
        if (str == null) {
            outInt = null;
            return outInt;
        }
        str = removeNonNumericCharactors(str);

        try {
            outInt = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            outInt = null;
        }
        return outInt;
    }

    public static Long stringToLong(String str) {
        Long outLong;
        if (str == null) {
            outLong = null;
            return outLong;
        }
        str = removeNonNumericCharactors(str);
        try {
            outLong = Long.parseLong(str);
        } catch (NumberFormatException e) {
            outLong = null;
        }
        return outLong;
    }

    public static Double stringToDouble(String str) {
        Double outDbl;
        if (str == null) {
            outDbl = null;
            return outDbl;
        }
        str = removeNonNumericCharactors(str);

        try {
            outDbl = Double.parseDouble(str);
        } catch (NumberFormatException e) {
            outDbl = null;
        }
        return outDbl;
    }

    private static String removeNonNumericCharactors(String str) {
        if (str == null) {
            return "";
        }
        if (str.contains("-")) {
            String[] parts = str.split("-");
            str = parts[0];
        }
        return str.replaceAll("[^\\d.]", "");
    }

    public Date dateFromString(String dateString, String format) {
        if (format == null || format.trim().equals("")) {
            format = "dd/MM/yyyy";
        }
        SimpleDateFormat formatter1 = new SimpleDateFormat(format);
        try {
            return formatter1.parse(dateString);
        } catch (ParseException ex) {
            return null;
        }
    }

    public Date startOfTheDay() {
        return startOfTheDay(new Date());
    }

    public Date startOfTheDay(Date date) {
        Calendar d = Calendar.getInstance();
        d.setTime(date);
        d.set(Calendar.HOUR_OF_DAY, 0);
        d.set(Calendar.MINUTE, 0);
        d.set(Calendar.SECOND, 0);
        d.set(Calendar.MILLISECOND, 0);
        return d.getTime();
    }

    public Date endOfTheDay() {
        return endOfTheDay(new Date());
    }

    public static int ageFromDob(Date dob) {
        if (dob == null) {
            return 0;
        }
        Calendar cDob = Calendar.getInstance();
        cDob.setTime(dob);
        Calendar today = Calendar.getInstance();
        int yearsInBetween = today.get(Calendar.YEAR)
                - cDob.get(Calendar.YEAR);
        return yearsInBetween;
    }

    public static int differenceInYears(Date fromDate, Date toDate) {
        if (fromDate == null || toDate == null) {
            return 0;
        }
        Calendar cFromDate = Calendar.getInstance();
        cFromDate.setTime(fromDate);
        Calendar cTodate = Calendar.getInstance();
        cTodate.setTime(toDate);
        int yearsInBetween = cTodate.get(Calendar.YEAR)
                - cFromDate.get(Calendar.YEAR);
        return yearsInBetween;
    }

    public static int calculateAge(Date dateOfBirth, Date toWhichDate) {
        if (dateOfBirth == null) {
            return 0;
        }
        if (toWhichDate == null) {
            toWhichDate = new Date();
        }
        LocalDate dob = new LocalDate(dateOfBirth);
        LocalDate date = new LocalDate(toWhichDate);
        Period period = new Period(dob, date, PeriodType.yearMonthDay());
        int age = period.getYears();
        return age;
    }

    public Date endOfTheDay(Date date) {
        Calendar d = Calendar.getInstance();
        d.setTime(startOfTheDay(date));
        d.add(Calendar.DATE, 1);
        d.add(Calendar.MILLISECOND, -1);
        return d.getTime();
    }

    public String dateToString() {
        return dateToString(Calendar.getInstance().getTime());
    }

    public String dateToString(Date date) {
        return dateToString(date, "dd MMMM yyyy");
    }

    public String dateToString(Date date, String format) {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        String strDate = dateFormat.format(date);
        return strDate;
    }

    public String encrypt(String word) {
        BasicTextEncryptor en = new BasicTextEncryptor();
        en.setPassword("health");
        try {
            return en.encrypt(word);
        } catch (Exception ex) {
            return null;
        }
    }

    public String hash(String word) {
        try {
            BasicPasswordEncryptor en = new BasicPasswordEncryptor();
            return en.encryptPassword(word);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean matchPassword(String planePassword, String encryptedPassword) {
        BasicPasswordEncryptor en = new BasicPasswordEncryptor();
        return en.checkPassword(planePassword, encryptedPassword);
    }

    public String decrypt(String word) {
        BasicTextEncryptor en = new BasicTextEncryptor();
        en.setPassword("health");
        try {
            return en.decrypt(word);
        } catch (Exception ex) {
            return null;
        }
    }

    public String decrypt(String word, String encryptKey) {
        BasicTextEncryptor en = new BasicTextEncryptor();
        en.setPassword("health");
        try {
            return en.decrypt(word);
        } catch (Exception ex) {
            return null;
        }
    }

    public List<SearchCriteria> getSearchCriteriae() {
        return Arrays.asList(SearchCriteria.values());
    }
    
    public WebUserRole[] getWebUserRoles() {
        return WebUserRole.values();
    }

    public InstitutionType[] getInstitutionTypes() {
        return InstitutionType.values();
    }

    public AreaType[] getAreaTypes() {
        return AreaType.values();
    }

    public ComponentSetType[] getComponentSetTypes() {
        ComponentSetType[] rts = new ComponentSetType[]{
            ComponentSetType.Encounter_Pages,
            ComponentSetType.Patient_Pages};
        return rts;
    }

    public ComponentSex[] getComponentSex() {
        return ComponentSex.values();
    }

    public RenderType[] getRenderTypes() {
        RenderType[] rts = new RenderType[]{
            RenderType.Autocomplete,
            RenderType.Boolean_Button,
            RenderType.Boolean_Checkbox,
            RenderType.Calendar,
            RenderType.Date_Picker,
            RenderType.Drop_Down_Menu,
            RenderType.Input_Text_Area,
            RenderType.Input_Text,
            RenderType.List_Box,
            RenderType.Observation,
            RenderType.Order,
            RenderType.PlanOfAction,
            RenderType.Prescreption,
            RenderType.Procedure_room,
            RenderType.Referral,
            RenderType.Text_Editor,};
        return rts;
    }

    public TimePeriodType[] getTimePeriodTypes() {
        return TimePeriodType.values();
    }

    public RelationshipType[] getRelationshipTypes() {
        return RelationshipType.values();
    }

    public RelationshipType[] getPopulationTypes() {
        RelationshipType[] ps = new RelationshipType[]{
            RelationshipType.Empanelled_Female_Population,
            RelationshipType.Empanelled_Male_Population,
            RelationshipType.Empanelled_Population,
            RelationshipType.Estimated_Midyear_Female_Population,
            RelationshipType.Estimated_Midyear_Male_Population,
            RelationshipType.Estimated_Midyear_Population,
            RelationshipType.Over_35_Female_Population,
            RelationshipType.Over_35_Male_Population,
            RelationshipType.Over_35_Population,};
        return ps;
    }

    public SelectionDataType[] getSelectionDataTypes() {
        SelectionDataType[] sdts = new SelectionDataType[]{
            SelectionDataType.Short_Text,
            SelectionDataType.Long_Text,
            SelectionDataType.Byte_Array,
            SelectionDataType.Integer_Number,
            SelectionDataType.Real_Number,
            SelectionDataType.Boolean,
            SelectionDataType.DateTime,
            SelectionDataType.Item_Reference,
            SelectionDataType.Client_Reference,
            SelectionDataType.Area_Reference,
            SelectionDataType.Prescreption_Reference,
            SelectionDataType.Procedure_Request,
            SelectionDataType.Prescreption_Request,};

        return sdts;
    }

    public DataPopulationStrategy[] getDataPopulationStrategies() {
        DataPopulationStrategy[] d = new DataPopulationStrategy[]{DataPopulationStrategy.None, DataPopulationStrategy.From_Client_Value, DataPopulationStrategy.From_Last_Encounter, DataPopulationStrategy.From_Last_Encounter_of_same_formset, DataPopulationStrategy.From_Last_Encounter_of_same_clinic};
        return d;
    }

    public DataCompletionStrategy[] getDataCompletionStrategies() {
        return DataCompletionStrategy.values();
    }

    public DataModificationStrategy[] getDataModificationStrategies() {
        return DataModificationStrategy.values();
    }

    public ItemArrangementStrategy[] getItemArrangementStrategies() {
        return ItemArrangementStrategy.values();
    }

    public ItemType[] getItemTypes() {
        return ItemType.values();
    }

    public PanelType[] getPanelTypes() {
        return PanelType.values();
    }

    public static Date startOfTheYear() {
        return startOfTheYear(new Date());
    }

    public static Date startOfTheMonth() {
        return startOfTheMonth(new Date());
    }

    public static Date startOfTheDate() {
        return startOfTheDate(new Date());
    }

    public static Date startOfTheLastMonth() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -1);
        return startOfTheMonth(c.getTime());
    }

    public static Date endOfTheLastMonth() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -1);
        return endOfTheMonth(c.getTime());
    }

    public static Date startOfTheLastQuarter() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -3);
        return startOfQuarter(c.getTime());
    }

    public static Date endOfTheLastQuarter() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -3);
        return endOfQuarter(c.getTime());
    }

    public static Date startOfTheMonth(Date d) {
//        // //// System.out.println("startOfTheMonth from date");
//        // //// System.out.println("d = " + d);
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.MILLISECOND, 1);
//        // //// System.out.println("c = " + c);
//        // //// System.out.println("c.getTime() = " + c.getTime());
        return c.getTime();
    }

    public static Date startOfTheDate(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.MILLISECOND, 1);
        return c.getTime();
    }

    public static Date startOfTheMonth(Integer year, Integer month) {
//        // //// System.out.println("startOfTheMonth from year and month");
//        // //// System.out.println("year = " + year);
//        // //// System.out.println("month = " + month);
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month - 1);
//        // //// System.out.println("c = " + c);
        return startOfTheMonth(c.getTime());
    }

    public static Date startOfTheMonth(Integer year, Integer month, boolean zeroIndexForMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        if (zeroIndexForMonth) {
            c.set(Calendar.MONTH, month);
        } else {
            c.set(Calendar.MONTH, month - 1);
        }

        return startOfTheMonth(c.getTime());
    }

    public static Integer monthIntFromString(String month) {
        if (month == null) {
            return null;
        }
        month = month.trim().toUpperCase();
        switch (month) {
            case "JANUARY":
                return 1;
            case "FEBRUARY":
                return 2;
            case "MARCH":
                return 3;
            case "APRIL":
                return 4;
            case "MAY":
                return 5;
            case "JUNE":
                return 6;
            case "JULY":
                return 7;
            case "AUGUST":
                return 8;
            case "SEPTEMBER":
                return 9;
            case "OCTOBER":
                return 10;
            case "NOVEMBER":
                return 11;
            case "DECEMBER":
                return 12;

        }
        return null;
    }

    public static Date endOfTheMonth(Integer year, Integer month) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month - 1);
        return endOfTheMonth(c.getTime());
    }

    public static Date endOfTheMonth(Integer year, Integer month, boolean zeroBasedMonthIndex) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        if (zeroBasedMonthIndex) {
            c.set(Calendar.MONTH, month);
        } else {
            c.set(Calendar.MONTH, month - 1);
        }
        return endOfTheMonth(c.getTime());
    }

    public static Date startOfQuarter() {
        return startOfQuarter(new Date());
    }

    public static Date startOfQuarter(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int month = c.get(Calendar.MONTH);
        switch (month) {
            case Calendar.JANUARY:
            case Calendar.FEBRUARY:
            case Calendar.MARCH:
                c.set(Calendar.MONTH, Calendar.JANUARY);
                break;
            case Calendar.APRIL:
            case Calendar.MAY:
            case Calendar.JUNE:
                c.set(Calendar.MONTH, Calendar.APRIL);
                break;
            case Calendar.JULY:
            case Calendar.AUGUST:
            case Calendar.SEPTEMBER:
                c.set(Calendar.MONTH, Calendar.JULY);
                break;
            case Calendar.OCTOBER:
            case Calendar.NOVEMBER:
            case Calendar.DECEMBER:
                c.set(Calendar.MONTH, Calendar.OCTOBER);
                break;
        }
        return startOfTheMonth(c.getTime());
    }

    public static Date startOfQuarter(Integer year, Integer quarter) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        switch (quarter) {
            case 1:
                c.set(Calendar.MONTH, Calendar.JANUARY);
                break;
            case 2:
                c.set(Calendar.MONTH, Calendar.APRIL);
                break;
            case 3:
                c.set(Calendar.MONTH, Calendar.JULY);
                break;
            case 4:
                c.set(Calendar.MONTH, Calendar.OCTOBER);
                break;
            default:
                c.set(Calendar.MONTH, Calendar.JANUARY);
        }
        return startOfTheMonth(c.getTime());
    }

    public static Date endOfQuarter(Integer year, Integer quarter) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        switch (quarter) {
            case 1:
                c.set(Calendar.MONTH, Calendar.MARCH);
                break;
            case 2:
                c.set(Calendar.MONTH, Calendar.JUNE);
                break;
            case 3:
                c.set(Calendar.MONTH, Calendar.SEPTEMBER);
                break;
            case 4:
                c.set(Calendar.MONTH, Calendar.DECEMBER);
                break;
            default:
                c.set(Calendar.MONTH, Calendar.MARCH);
        }
        return endOfTheMonth(c.getTime());
    }

    public static Date endOfQuarter() {
        return endOfQuarter(new Date());
    }

    public static Date endOfQuarter(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int month = c.get(Calendar.MONTH);
        switch (month) {
            case Calendar.JANUARY:
            case Calendar.FEBRUARY:
            case Calendar.MARCH:
                c.set(Calendar.MONTH, Calendar.MARCH);
                break;
            case Calendar.APRIL:
            case Calendar.MAY:
            case Calendar.JUNE:
                c.set(Calendar.MONTH, Calendar.JUNE);
                break;
            case Calendar.JULY:
            case Calendar.AUGUST:
            case Calendar.SEPTEMBER:
                c.set(Calendar.MONTH, Calendar.SEPTEMBER);
                break;
            case Calendar.OCTOBER:
            case Calendar.NOVEMBER:
            case Calendar.DECEMBER:
                c.set(Calendar.MONTH, Calendar.DECEMBER);
                break;
        }
        return endOfTheMonth(c.getTime());
    }

    public static Date endOfYear() {
        return endOfYear(new Date());
    }

    public static Date endOfYear(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.MONTH, Calendar.DECEMBER);
        return endOfTheMonth(c.getTime());
    }

    public static Date endOfYear(Integer year) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, Calendar.DECEMBER);
        return endOfTheMonth(c.getTime());
    }

    public static Date endOfTheMonth() {
        return endOfTheMonth(new Date());
    }

    public static Date endOfTheMonth(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        c.set(Calendar.HOUR, c.getActualMaximum(Calendar.HOUR));
        c.set(Calendar.MINUTE, c.getActualMaximum(Calendar.MINUTE));
        c.set(Calendar.MILLISECOND, c.getActualMaximum(Calendar.MILLISECOND));
        return c.getTime();
    }

    public static Date endOfTheDate() {
        return endOfTheDate(new Date());
    }

    public static Date endOfTheDate(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.HOUR, c.getActualMaximum(Calendar.HOUR));
        c.set(Calendar.MINUTE, c.getActualMaximum(Calendar.MINUTE));
        c.set(Calendar.MILLISECOND, c.getActualMaximum(Calendar.MILLISECOND));
        return c.getTime();
    }

    public static Date startOfTheYear(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.MONTH, Calendar.JANUARY);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.MILLISECOND, 1);
        return c.getTime();
    }

    public static Date startOfTheLastYear(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.MONTH, Calendar.JANUARY);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.MILLISECOND, 1);
        c.set(Calendar.YEAR, c.get(Calendar.YEAR) - 1);
        return c.getTime();
    }

    public static Date startOfTheLastYear() {
        return startOfTheLastYear(new Date());
    }

    public static Date startOfTheYear(Integer year) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, Calendar.JANUARY);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.MILLISECOND, 1);
        return c.getTime();
    }

    public static Double getDoubleValue(String result) {
        Double d = null;
        try {
            d = Double.parseDouble(result);
        } catch (Exception e) {
            d = null;
        }
        return d;
    }

    public static Long getDifferenceInYears(Date from, Date to) {
        if (from == null || to == null) {
            return 0l;
        }
        Long difInMiliSeconds = to.getTime() - from.getTime();
        difInMiliSeconds = Math.abs(difInMiliSeconds);

        Long diffInSec = difInMiliSeconds / 1000;

        Long diffInMin = diffInSec / 60;

        Long diffInHrs = diffInMin / 60;

        Long diffInDLong = diffInHrs / 24;

        Long diffInYrs = diffInDLong / 365;

        return diffInYrs;

    }

    public static Long getLongValue(String result) {
        Long l = null;
        try {
            l = Long.parseLong(result);
        } catch (Exception e) {
            l = null;
        }
        return l;
    }

    public static String formatDouble(Double variable) {
        String out;
        if ((variable == Math.floor(variable)) && !Double.isInfinite(variable)) {
            int oi = variable.intValue();
            out = oi + "";
        } else {
            out = variable.toString();
        }
        return out;
    }

    public static Integer getIntegerValue(String result) {
        Integer d = null;
        try {
            d = Integer.parseInt(result);
        } catch (Exception e) {
            d = null;
        }
        return d;//To change body of generated methods, choose Tools | Templates.
    }

    public static Integer getMonth(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        return c.get(Calendar.MONTH);
    }

    public static Integer getYear() {
        return getYear(new Date());
    }

    public static Integer getMonth() {
        return getMonth(new Date());
    }

    public static Integer getDateOfMonth() {
        return getDateOfMonth(new Date());
    }

    public static Integer getDateOfMonth(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        return c.get(Calendar.DATE);
    }

    public static String dateTimeToString(Date date, String format) {
        DateFormat dateFormat = new SimpleDateFormat(format);
        String strDate = dateFormat.format(date);
        return strDate;
    }

    public static String dateTimeToString(Date date) {
        if (date == null) {
            return "";
        }
        return dateTimeToString(date, "dd MMMM yyyy");
    }

    public static String anonymizeData(String input) {
        if (input == null) {
            return "";
        }
        if ("".equals(input)) {
            return "";
        }
        String output = "";

        ArrayList<Character> chars = new ArrayList<Character>();
        StringBuilder builder = new StringBuilder();
        for (char c : input.toCharArray()) {
            char nc;
            if (Character.isUpperCase(c)) {
                nc = (char) (Math.random() * 26 + 'A');
            } else if (Character.isLowerCase(c)) {
                nc = (char) (Math.random() * 26 + 'a');
            } else if (Character.isDigit(c)) {
                nc = (char) (Math.random() * 10 + '0');
            } else {
                nc = c;
            }
            builder.append(nc);
        }
        output = builder.toString();

        return output;
    }

    public static List<TimePeriod> getMonthPeriodsForQuarter(Integer year, Integer q) {
        List<lk.gov.health.phsp.pojcs.TimePeriod> ps = new ArrayList<>();
        if (year == null) {
            return ps;
        }
        if (q == null) {
            return ps;
        }
        TimePeriod first = new TimePeriod();
        TimePeriod second = new TimePeriod();
        TimePeriod thired = new TimePeriod();
        int firstMonth = 1;
        int secondMonth = 2;
        int thiredMonth = 3;
        switch (q) {
            case 1:
                firstMonth = 1;
                secondMonth = 2;
                thiredMonth = 3;
                break;
            case 2:
                firstMonth = 4;
                secondMonth = 5;
                thiredMonth = 6;
                break;
            case 3:
                firstMonth = 7;
                secondMonth = 8;
                thiredMonth = 9;
                break;
            case 4:
                firstMonth = 10;
                secondMonth = 11;
                thiredMonth = 12;
                break;
        }
        first.setFromDate(startOfTheMonth(year, firstMonth));
        first.setToDate(endOfTheMonth(year, firstMonth));
        second.setFromDate(startOfTheMonth(year, secondMonth));
        second.setToDate(endOfTheMonth(year, secondMonth));
        thired.setFromDate(startOfTheMonth(year, thiredMonth));
        thired.setToDate(endOfTheMonth(year, thiredMonth));
        ps.add(first);
        ps.add(second);
        ps.add(thired);
        return ps;
    }

    public static String quarterAsString(Integer q) {
        String qs = "";
        if (q == null) {
            return qs;
        }
        switch (q) {
            case 1:
                qs = "First";
                break;
            case 2:
                qs = "Second";
                break;
            case 3:
                qs = "Thired";
                break;
            case 4:
                qs = "Forth";
                break;
        }
        return qs;
    }

    public static String monthAsString(Integer q) {
        String qs = "";
        if (q == null) {
            return qs;
        }
        switch (q) {
            case 0:
                qs = "January";
                break;
            case 1:
                qs = "February";
                break;
            case 2:
                qs = "March";
                break;
            case 3:
                qs = "April";
                break;
            case 4:
                qs = "May";
                break;
            case 5:
                qs = "June";
                break;
            case 6:
                qs = "July";
                break;
            case 7:
                qs = "Aujust";
                break;
            case 8:
                qs = "September";
                break;
            case 9:
                qs = "Ocober";
                break;
            case 10:
                qs = "November";
                break;
            case 11:
                qs = "December";
                break;
        }
        return qs;
    }

    public static String monthString(Integer month) {
        if (month == null) {
            return "";
        }
        switch (month) {
            case Calendar.JANUARY:
                return "January";
            case Calendar.FEBRUARY:
                return "February";
            case Calendar.MARCH:
                return "March";
            case Calendar.APRIL:
                return "April";
            case Calendar.MAY:
                return "May";
            case Calendar.JUNE:
                return "June";
            case Calendar.JULY:
                return "July";
            case Calendar.AUGUST:
                return "August";
            case Calendar.SEPTEMBER:
                return "September";
            case Calendar.OCTOBER:
                return "October";
            case Calendar.NOVEMBER:
                return "November";
            case Calendar.DECEMBER:
                return "December";
        }
        return "";
    }

    public static String quarterString(Integer month) {
        if (month == null) {
            return "";
        }
        switch (month) {
            case Calendar.JANUARY:
            case Calendar.FEBRUARY:
            case Calendar.MARCH:
                return "First";
            case Calendar.APRIL:
            case Calendar.MAY:
            case Calendar.JUNE:
                return "Second";
            case Calendar.JULY:
            case Calendar.AUGUST:
            case Calendar.SEPTEMBER:
                return "Third";
            case Calendar.OCTOBER:
            case Calendar.NOVEMBER:
            case Calendar.DECEMBER:
                return "Fourth";
        }
        return "";
    }

    public static String quarterString(Quarter quarter) {
        if (quarter == null) {
            return "";
        }
        switch (quarter) {
            case First:
                return "First";
            case Second:
                return "Second";
            case Third:
                return "Third";
            case Fourth:
                return "Fourth";
        }
        return "";
    }

    public static Integer getYear(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        return c.get(Calendar.YEAR);
    }

    public static Integer getQuarter() {
        return getQuarter(new Date());
    }

    public static Integer getQuarter(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        int month = c.get(Calendar.MONTH);
        return (month / 3) + 1;
    }

    public Month[] getMonths() {
        return Month.values();
    }

    public Quarter[] getQuarters() {
        return Quarter.values();
    }

    public QueryOutputType[] getQueryOutputTypes() {
        return QueryOutputType.values();
    }

    public QueryCriteriaMatchType[] getQueryCriteriaMatchTypes() {
        return QueryCriteriaMatchType.values();
    }

    public QueryType[] getQueryType() {
        return QueryType.values();
    }

    public QueryType[] getQueryTypeForCounts() {
        QueryType[] qtfcs = {QueryType.Encounter_Count, QueryType.Client_Count, QueryType.Population};
        return qtfcs;
    }

    public QueryLevel[] getQueryLevels() {
        return QueryLevel.values();
    }

    public QueryDataType[] getQueryDataTypes() {
        return QueryDataType.values();
    }

    public QueryVariableEvaluationType[] getQueryVariableEvaluationType() {
        return QueryVariableEvaluationType.values();
    }

    public QueryFilterPeriodType[] getQueryFilterPeriodTypes() {
        return QueryFilterPeriodType.values();
    }

    public QueryFilterPeriodType[] getQueryFilterPeriodTypesWithoutYearAndQuarter() {
        QueryFilterPeriodType[] ts = new QueryFilterPeriodType[]{QueryFilterPeriodType.All, QueryFilterPeriodType.Period, QueryFilterPeriodType.After, QueryFilterPeriodType.Before};
        return ts;
    }

    public QueryFilterAreaType[] getQueryFilterAreaType() {
        return QueryFilterAreaType.values();
    }

    public QueryFilterAreaType[] getQueryFilterAreaTypeUpToDistrictLevel() {
        QueryFilterAreaType[] ts = new QueryFilterAreaType[]{QueryFilterAreaType.National, QueryFilterAreaType.Province_List,
            QueryFilterAreaType.District_List, QueryFilterAreaType.Province, QueryFilterAreaType.Distirct, QueryFilterAreaType.Province_District_list};
        return ts;
    }

    public static String stringToHtml(String str) {
        if (str == null) {
            str = "";
        }
        str = str.replace("\n", "<br/>");
        return str;
    }

    public static String prepareAsCode(String str) {
        String after = str.trim().replaceAll(" +", "_");
        return after.toLowerCase();
    }

}
