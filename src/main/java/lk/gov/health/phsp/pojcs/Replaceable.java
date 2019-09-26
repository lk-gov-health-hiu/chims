/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lk.gov.health.phsp.pojcs;

import java.util.ArrayList;
import java.util.List;
import lk.gov.health.phsp.entity.ClientEncounterComponentItem;

/**
 *
 * @author buddhika_ari
 */
public class Replaceable {
    String variableCode;
    private String valueCode;
    private String pef;
    private String fl;
    private String defaultValue;
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
        if(selectedValue==null){
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
    
    
    
    
}
