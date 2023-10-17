/*
 * The MIT License
 *
 * Copyright 2019 Dr M H B Ariyaratne<buddhika.ari@gmail.com>.
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
package lk.gov.health.phsp.entity;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlRootElement;
import lk.gov.health.phsp.enums.AvailableDataType;
import lk.gov.health.phsp.enums.ComponentSetType;
import lk.gov.health.phsp.enums.DataCompletionStrategy;
import lk.gov.health.phsp.enums.DataModificationStrategy;
import lk.gov.health.phsp.enums.DataPopulationStrategy;
import lk.gov.health.phsp.enums.ItemArrangementStrategy;
import lk.gov.health.phsp.enums.PanelType;
import lk.gov.health.phsp.enums.RenderType;
import lk.gov.health.phsp.enums.SelectionDataType;

/**
 *
 * @author sunila_soft
 */
@Entity
@XmlRootElement
public class DesignComponentFormItem extends DesignComponent {

    private boolean required;

    private boolean calculateOnFocus;

    private boolean calculateButton;
    @Lob
    private String calculationScriptForColour;

    @Lob
    private String calculationScriptForBackgroundColour;

    private boolean displayDetailsBox;
    private boolean discreptionAsAToolTip;
    private boolean discreptionAsASideLabel;
    private boolean displayLastResult;
    private boolean displayLinkToResultList;
    private boolean displayLinkToClientValues;

    private boolean multipleEntiesPerForm;

    @Lob
    private String calculationScript;

    @Lob
    private String requiredErrorMessage;

    private String regexValidationString;

    @Lob
    private String regexValidationFailedMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    private Item mimeType;

    @Enumerated(EnumType.STRING)
    private RenderType renderType;
    
    @Enumerated(EnumType.STRING)
    private ComponentSetType componentSetType;

    @Enumerated(EnumType.STRING)
    private PanelType panelType;

    @Enumerated(EnumType.STRING)
    private SelectionDataType selectionDataType;

    @Enumerated(EnumType.STRING)
    private AvailableDataType availableDataType;

    @Enumerated(EnumType.STRING)
    private DataPopulationStrategy dataPopulationStrategy;

    @Enumerated(EnumType.STRING)
    private DataPopulationStrategy resultDisplayStrategy;

    @Enumerated(EnumType.STRING)
    private DataCompletionStrategy dataCompletionStrategy;

    @Enumerated(EnumType.STRING)
    private DataModificationStrategy dataModificationStrategy;

    @ManyToOne(fetch = FetchType.LAZY)
    private Area parentAreaOfAvailableAreas;
    @ManyToOne(fetch = FetchType.LAZY)
    private Item categoryOfAvailableItems;
    @ManyToOne(fetch = FetchType.LAZY)
    private Institution parentInstitutionOfAvailableInstitutions;

    

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isCalculateOnFocus() {
        return calculateOnFocus;
    }

    public void setCalculateOnFocus(boolean calculateOnFocus) {
        this.calculateOnFocus = calculateOnFocus;
    }

    public boolean isCalculateButton() {
        return calculateButton;
    }

    public void setCalculateButton(boolean calculateButton) {
        this.calculateButton = calculateButton;
    }

    public String getCalculationScriptForColour() {
        return calculationScriptForColour;
    }

    public void setCalculationScriptForColour(String calculationScriptForColour) {
        this.calculationScriptForColour = calculationScriptForColour;
    }

    public String getCalculationScriptForBackgroundColour() {
        return calculationScriptForBackgroundColour;
    }

    public void setCalculationScriptForBackgroundColour(String calculationScriptForBackgroundColour) {
        this.calculationScriptForBackgroundColour = calculationScriptForBackgroundColour;
    }

    public boolean isDisplayDetailsBox() {
        return displayDetailsBox;
    }

    public void setDisplayDetailsBox(boolean displayDetailsBox) {
        this.displayDetailsBox = displayDetailsBox;
    }

    public boolean isDiscreptionAsAToolTip() {
        return discreptionAsAToolTip;
    }

    public void setDiscreptionAsAToolTip(boolean discreptionAsAToolTip) {
        this.discreptionAsAToolTip = discreptionAsAToolTip;
    }

    public boolean isDiscreptionAsASideLabel() {
        return discreptionAsASideLabel;
    }

    public void setDiscreptionAsASideLabel(boolean discreptionAsASideLabel) {
        this.discreptionAsASideLabel = discreptionAsASideLabel;
    }

    public boolean isDisplayLastResult() {
        return displayLastResult;
    }

    public void setDisplayLastResult(boolean displayLastResult) {
        this.displayLastResult = displayLastResult;
    }

    public boolean isDisplayLinkToResultList() {
        return displayLinkToResultList;
    }

    public void setDisplayLinkToResultList(boolean displayLinkToResultList) {
        this.displayLinkToResultList = displayLinkToResultList;
    }

    public boolean isDisplayLinkToClientValues() {
        return displayLinkToClientValues;
    }

    public void setDisplayLinkToClientValues(boolean displayLinkToClientValues) {
        this.displayLinkToClientValues = displayLinkToClientValues;
    }

    public boolean isMultipleEntiesPerForm() {
        return multipleEntiesPerForm;
    }

    public void setMultipleEntiesPerForm(boolean multipleEntiesPerForm) {
        this.multipleEntiesPerForm = multipleEntiesPerForm;
    }

    public String getCalculationScript() {
        return calculationScript;
    }

    public void setCalculationScript(String calculationScript) {
        this.calculationScript = calculationScript;
    }

    public String getRequiredErrorMessage() {
        return requiredErrorMessage;
    }

    public void setRequiredErrorMessage(String requiredErrorMessage) {
        this.requiredErrorMessage = requiredErrorMessage;
    }

    public String getRegexValidationString() {
        return regexValidationString;
    }

    public void setRegexValidationString(String regexValidationString) {
        this.regexValidationString = regexValidationString;
    }

    public String getRegexValidationFailedMessage() {
        return regexValidationFailedMessage;
    }

    public void setRegexValidationFailedMessage(String regexValidationFailedMessage) {
        this.regexValidationFailedMessage = regexValidationFailedMessage;
    }

    public Item getMimeType() {
        return mimeType;
    }

    public void setMimeType(Item mimeType) {
        this.mimeType = mimeType;
    }

    public ComponentSetType getComponentSetType() {
        return componentSetType;
    }

    public void setComponentSetType(ComponentSetType componentSetType) {
        this.componentSetType = componentSetType;
    }

    public PanelType getPanelType() {
        return panelType;
    }

    public void setPanelType(PanelType panelType) {
        this.panelType = panelType;
    }

    public SelectionDataType getSelectionDataType() {
        return selectionDataType;
    }

    public void setSelectionDataType(SelectionDataType selectionDataType) {
        this.selectionDataType = selectionDataType;
    }

    public AvailableDataType getAvailableDataType() {
        return availableDataType;
    }

    public void setAvailableDataType(AvailableDataType availableDataType) {
        this.availableDataType = availableDataType;
    }

    public DataPopulationStrategy getDataPopulationStrategy() {
        return dataPopulationStrategy;
    }

    public void setDataPopulationStrategy(DataPopulationStrategy dataPopulationStrategy) {
        this.dataPopulationStrategy = dataPopulationStrategy;
    }

    public DataPopulationStrategy getResultDisplayStrategy() {
        return resultDisplayStrategy;
    }

    public void setResultDisplayStrategy(DataPopulationStrategy resultDisplayStrategy) {
        this.resultDisplayStrategy = resultDisplayStrategy;
    }

    public DataCompletionStrategy getDataCompletionStrategy() {
        return dataCompletionStrategy;
    }

    public void setDataCompletionStrategy(DataCompletionStrategy dataCompletionStrategy) {
        this.dataCompletionStrategy = dataCompletionStrategy;
    }

    public DataModificationStrategy getDataModificationStrategy() {
        return dataModificationStrategy;
    }

    public void setDataModificationStrategy(DataModificationStrategy dataModificationStrategy) {
        this.dataModificationStrategy = dataModificationStrategy;
    }

    public Area getParentAreaOfAvailableAreas() {
        return parentAreaOfAvailableAreas;
    }

    public void setParentAreaOfAvailableAreas(Area parentAreaOfAvailableAreas) {
        this.parentAreaOfAvailableAreas = parentAreaOfAvailableAreas;
    }

    public Item getCategoryOfAvailableItems() {
        return categoryOfAvailableItems;
    }

    public void setCategoryOfAvailableItems(Item categoryOfAvailableItems) {
        this.categoryOfAvailableItems = categoryOfAvailableItems;
    }

    public Institution getParentInstitutionOfAvailableInstitutions() {
        return parentInstitutionOfAvailableInstitutions;
    }

    public void setParentInstitutionOfAvailableInstitutions(Institution parentInstitutionOfAvailableInstitutions) {
        this.parentInstitutionOfAvailableInstitutions = parentInstitutionOfAvailableInstitutions;
    }

    public RenderType getRenderType() {
        return renderType;
    }

    public void setRenderType(RenderType renderType) {
        this.renderType = renderType;
    }

    

}
