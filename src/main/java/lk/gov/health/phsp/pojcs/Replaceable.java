/*
 * To change this license header, choose License Headers institution Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template institution the editor.
 */
package lk.gov.health.phsp.pojcs;

import java.util.ArrayList;
import java.util.List;
import lk.gov.health.phsp.entity.ClientEncounterComponentItem;
import lk.gov.health.phsp.enums.Evaluation;
import lk.gov.health.phsp.enums.QueryDataType;

/**
 *
 * @author buddhika_ari
 */
public class Replaceable {

    private String variableCode;
    private String valueCode;
    private Evaluation evaluation;
    private QueryDataType queryDataType;

    private boolean forClient;
    private boolean forEncounter;
    private boolean forForm;

    private String pef;
    private String fl;
    private String sm;
    private String defaultValue;
    private String strEvaluation;
    private String strQueryDataType;
    
    
    private String qryCode;
    
    
    private ClientEncounterComponentItem clientEncounterComponentItem;
    List<String> options;
    String selectedOption;
    boolean inputText;
    boolean formulaEvaluation;
    String selectedValue;
    String fullText;

    public String getVariableCode() {
        return variableCode;
    }

    public void setVariableCode(String variableCode) {
        this.variableCode = variableCode;
    }

    public List<String> getOptions() {
        if (options == null) {
            options = new ArrayList<>();
        }
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(String selectedOption) {
        this.selectedOption = selectedOption;
    }

    public boolean isInputText() {
        return inputText;
    }

    public void setInputText(boolean inputText) {
        this.inputText = inputText;
    }

    public boolean isFormulaEvaluation() {
        return formulaEvaluation;
    }

    public void setFormulaEvaluation(boolean formulaEvaluation) {
        this.formulaEvaluation = formulaEvaluation;
    }

    public String getSelectedValue() {
        if (selectedValue == null) {
            selectedValue = "";
        }
        return selectedValue;
    }

    public void setSelectedValue(String selectedValue) {
        this.selectedValue = selectedValue;
    }

    public String getFullText() {
        return fullText;
    }

    public void setFullText(String fullText) {
        this.fullText = fullText;
    }

    public String getPef() {
        return pef;
    }

    public void setPef(String pef) {
        this.pef = pef;
        forClient = false;
        forEncounter = false;
        forForm = false;
        if (pef.equalsIgnoreCase("p")) {
            forClient = true;
        } else if (pef.equalsIgnoreCase("f")) {
            forForm = true;
        } else if (pef.equalsIgnoreCase("e")) {
            forEncounter = true;
        }
    }

    public String getFl() {
        return fl;
    }

    public void setFl(String fl) {
        this.fl = fl;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public ClientEncounterComponentItem getClientEncounterComponentItem() {
        return clientEncounterComponentItem;
    }

    public void setClientEncounterComponentItem(ClientEncounterComponentItem clientEncounterComponentItem) {
        this.clientEncounterComponentItem = clientEncounterComponentItem;
    }

    public String getValueCode() {
        return valueCode;
    }

    public void setValueCode(String valueCode) {
        this.valueCode = valueCode;
    }

    public String getSm() {
        return sm;
    }

    public void setSm(String sm) {
        this.sm = sm;
    }

    public Evaluation getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(Evaluation evaluation) {
        this.evaluation = evaluation;
    }

    public boolean isForClient() {
        return forClient;
    }

    public void setForClient(boolean forClient) {
        this.forClient = forClient;
    }

    public boolean isForEncounter() {
        return forEncounter;
    }

    public void setForEncounter(boolean forEncounter) {
        this.forEncounter = forEncounter;
    }

    public boolean isForForm() {
        return forForm;
    }

    public void setForForm(boolean forForm) {
        this.forForm = forForm;
    }

    public String getStrEvaluation() {
        return strEvaluation;
    }

    public void setStrEvaluation(String strEvaluation) {
        this.strEvaluation = strEvaluation.trim().toLowerCase();
        switch (this.strEvaluation) {
            case "":
                evaluation = null;
                break;
            case "eq":
                evaluation = Evaluation.eq;
                break;
            case "ne":
                evaluation = Evaluation.ne;
                break;
            case "gt":
                evaluation = Evaluation.gt;
                break;
            case "lt":
                evaluation = Evaluation.lt;
                break;
            case "ge":
                evaluation = Evaluation.ge;
                break;
            case "le":
                evaluation = Evaluation.le;
                break;
            case "in":
                evaluation = Evaluation.in;
                break;
            case "nn":
                evaluation = Evaluation.nn;
                break;
        }
    }

    public String getStrQueryDataType() {
        return strQueryDataType;
    }

    public void setStrQueryDataType(String strQueryDataType) {
        this.strQueryDataType = strQueryDataType.trim().toLowerCase();
        switch (this.strQueryDataType) {
            case "":
                queryDataType = null;
                break;
            case "it":
                queryDataType = QueryDataType.integer;
                break;
            case "db":
                queryDataType = QueryDataType.real;
                break;
            case "in":
                queryDataType = QueryDataType.institution;
                break;
            case "ac":
                queryDataType = QueryDataType.area;
                break;
            case "ic":
                queryDataType = QueryDataType.item;
                break;
            case "sr":
                queryDataType = QueryDataType.String;
                break;
            case "bo":
                queryDataType = QueryDataType.Boolean;
                break;
            case "dt":
                queryDataType = QueryDataType.DateTime;
                break;
        }
    }

    public QueryDataType getQueryDataType() {
        return queryDataType;
    }

    public void setQueryDataType(QueryDataType queryDataType) {
        this.queryDataType = queryDataType;
    }

    public String getQryCode() {
        return qryCode;
    }

    public void setQryCode(String qryCode) {
        this.qryCode = qryCode;
    }

    
    
}
