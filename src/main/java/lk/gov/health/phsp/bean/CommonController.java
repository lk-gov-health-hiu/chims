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
import java.util.Calendar;
import java.util.Date;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
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
import lk.gov.health.phsp.enums.SelectionDataType;
import org.jasypt.util.password.BasicPasswordEncryptor;
import org.jasypt.util.text.BasicTextEncryptor;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 * Informatics)
 */
@Named
@SessionScoped
public class CommonController implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of HOSecurity
     */
    public CommonController() {
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
        return ComponentSetType.values();
    }

    public ComponentSex[] getComponentSex() {
        return ComponentSex.values();
    }

    public RenderType[] getRenderTypes() {
        RenderType[] rts = new RenderType[]{
            RenderType.Autocomplete,
            RenderType.Calendar,
            RenderType.Date_Picker,
            RenderType.Input_Text,
            RenderType.Input_Text_Area,
            RenderType.List_Box,
            RenderType.Prescreption,
            RenderType.Boolean_Button,
            RenderType.Boolean_Checkbox,
            RenderType.Drop_Down_Menu};
        return rts;
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
            SelectionDataType.Area_Reference,};
        return sdts;
    }

    public DataPopulationStrategy[] getDataPopulationStrategies() {
        DataPopulationStrategy[] d = new DataPopulationStrategy[]{DataPopulationStrategy.None, DataPopulationStrategy.From_Client_Value, DataPopulationStrategy.From_Last_Encounter};
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

    public static Date startOfTheYear(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.MONTH, 0);
        c.set(Calendar.DAY_OF_MONTH, Calendar.JANUARY);
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.MILLISECOND, 0);
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

    public static Long getLongValue(String result) {
        Long l = null;
        try {
            l = Long.parseLong(result);
        } catch (Exception e) {
            l = null;
        }
        return l;
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

    public static Integer getYear(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        return c.get(Calendar.YEAR);
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
    
    public QueryLevel[] getQueryLevels() {
        return QueryLevel.values();
    }
    
     
    public QueryDataType[] getQueryDataTypes() {
        return QueryDataType.values();
    }

    public QueryVariableEvaluationType[] getQueryVariableEvaluationType() {
        return QueryVariableEvaluationType.values();
    }
    
    public QueryFilterPeriodType[] getQueryFilterPeriodTypes(){
        return QueryFilterPeriodType.values();
    }

    public QueryFilterAreaType[] getQueryFilterAreaType(){
        return QueryFilterAreaType.values();
    } 
    
}
