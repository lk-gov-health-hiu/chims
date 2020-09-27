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

import java.io.Serializable;
import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import lk.gov.health.phsp.enums.AvailableDataType;
import lk.gov.health.phsp.enums.ComponentSetType;
import lk.gov.health.phsp.enums.DataCompletionStrategy;
import lk.gov.health.phsp.enums.DataModificationStrategy;
import lk.gov.health.phsp.enums.DataPopulationStrategy;
import lk.gov.health.phsp.enums.ItemArrangementStrategy;
import lk.gov.health.phsp.enums.PanelType;
import lk.gov.health.phsp.enums.SelectionDataType;
import lk.gov.health.phsp.enums.RenderType;

/**
 *
 * @author buddhika
 */
@Entity
public class Component implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    String name;

    private String code;

    @ManyToOne(fetch = FetchType.EAGER)
    private Item item;

    @Lob
    private String descreption;

    private Double orderNo;

    @ManyToOne
    private Institution institution;

    @ManyToOne(fetch = FetchType.EAGER)
    private Component parentComponent;

    @ManyToOne(fetch = FetchType.EAGER)
    private Component referenceComponent;

    @Enumerated(EnumType.STRING)
    RenderType renderType;

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

    @ManyToOne
    private Item mimeType;

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

    @Enumerated(EnumType.STRING)
    private ItemArrangementStrategy itemArrangementStrategy;

    @ManyToOne
    private Area parentAreaOfAvailableAreas;
    @ManyToOne
    private Item categoryOfAvailableItems;
    @ManyToOne
    private Institution parentInstitutionOfAvailableInstitutions;

    private Double topPercent;
    private Double leftPercent;
    private Double widthPercent;
    private Double heightPercent;

    private Integer intHtmlColor;
    @Transient
    private String hexHtmlColour;
    @Lob
    private String html;

    private String backgroundColour;
    private String foregroundColour;
    private String borderColour;

    @Lob
    private String css;

    @Transient
    private String generatedCss;

    /*
    Create Properties
     */
    @ManyToOne
    private WebUser createdBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdAt;
    /*
    Last Edit Properties
     */
    @ManyToOne
    private WebUser lastEditBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date lastEditeAt;
    /*
    Retire Reversal Properties
     */
    @ManyToOne
    private WebUser retiredReversedBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date retiredReversedAt;
    /*
    Retire Properties
     */
    private boolean retired;
    @ManyToOne
    private WebUser retiredBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date retiredAt;
    private String retireComments;

    @ManyToOne(fetch = FetchType.EAGER)
    private Encounter encounter;

    @ManyToOne(fetch = FetchType.EAGER)
    private Client client;

    @Lob
    private String longTextValue;
    @Lob
    private String descreptionValue;
    private String shortTextValue;
    private byte[] byteArrayValue;
    private Integer integerNumberValue;
    private Long longNumberValue;
    private Double realNumberValue;
    private Boolean booleanValue;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date dateValue;
    @ManyToOne(fetch = FetchType.EAGER)
    private Item itemValue;
    @ManyToOne(fetch = FetchType.EAGER)
    private Area areaValue;
    @ManyToOne(fetch = FetchType.EAGER)
    private Institution institutionValue;
    @ManyToOne(fetch = FetchType.EAGER)
    private Client clientValue;
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Prescription prescriptionValue;
    
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Observation observationValue;
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Procedure procedureValue;
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Movement movementValue;
    

    private boolean completed;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date completedAt;
    @ManyToOne(fetch = FetchType.EAGER)
    private WebUser completedBy;

    private Integer integerNumberValue2;
    private Long longNumberValue2;
    private Double realNumberValue2;

    public Encounter getEncounter() {
        return encounter;
    }

    public void setEncounter(Encounter encounter) {
        this.encounter = encounter;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getLongTextValue() {
        return longTextValue;
    }

    public void setLongTextValue(String longTextValue) {
        this.longTextValue = longTextValue;
    }

    public String getShortTextValue() {
        return shortTextValue;
    }

    public void setShortTextValue(String shortTextValue) {
        this.shortTextValue = shortTextValue;
    }

    public byte[] getByteArrayValue() {
        return byteArrayValue;
    }

    public void setByteArrayValue(byte[] byteArrayValue) {
        this.byteArrayValue = byteArrayValue;
    }

    public Integer getIntegerNumberValue() {
        return integerNumberValue;
    }

    public void setIntegerNumberValue(Integer integerNumberValue) {
        this.integerNumberValue = integerNumberValue;
    }

    public Double getRealNumberValue() {
        return realNumberValue;
    }

    public void setRealNumberValue(Double realNumberValue) {
        this.realNumberValue = realNumberValue;
    }

    public Boolean getBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(Boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    public Item getItemValue() {
        return itemValue;
    }

    public void setItemValue(Item itemValue) {
        this.itemValue = itemValue;
    }

    public Area getAreaValue() {
        return areaValue;
    }

    public void setAreaValue(Area areaValue) {
        this.areaValue = areaValue;
    }

    public Institution getInstitutionValue() {
        return institutionValue;
    }

    public void setInstitutionValue(Institution institutionValue) {
        this.institutionValue = institutionValue;
    }

    public Client getClientValue() {
        return clientValue;
    }

    public void setClientValue(Client clientValue) {
        this.clientValue = clientValue;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }

    public WebUser getCompletedBy() {
        return completedBy;
    }

    public void setCompletedBy(WebUser completedBy) {
        this.completedBy = completedBy;
    }

    public Long getLongNumberValue() {
        return longNumberValue;
    }

    public void setLongNumberValue(Long longNumberValue) {
        this.longNumberValue = longNumberValue;
    }

    public Date getDateValue() {
        return dateValue;
    }

    public void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }

    public Prescription getPrescriptionValue() {
        if (prescriptionValue == null) {
            prescriptionValue = new Prescription();
        }
        return prescriptionValue;
    }

    public void setPrescriptionValue(Prescription prescriptionValue) {
        this.prescriptionValue = prescriptionValue;
    }

    public String getDescreptionValue() {
        return descreptionValue;
    }

    public void setDescreptionValue(String descreptionValue) {
        this.descreptionValue = descreptionValue;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {

        if (!(object instanceof Component)) {
            return false;
        }
        Component other = (Component) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return name ;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RenderType getRenderType() {
        return renderType;
    }

    public void setRenderType(RenderType renderType) {
        this.renderType = renderType;
    }

    public Component getParentComponent() {
        return parentComponent;
    }

    public void setParentComponent(Component parentComponent) {
        this.parentComponent = parentComponent;
    }

    public Item getMimeType() {
        return mimeType;
    }

    public void setMimeType(Item mimeType) {
        this.mimeType = mimeType;
    }

    public SelectionDataType getSelectionDataType() {
        if (selectionDataType == null) {
            if (item != null) {
                selectionDataType = item.getDataType();
            }else{
                selectionDataType = SelectionDataType.Short_Text;
            }
        }
        return selectionDataType;
    }

    public void setSelectionDataType(SelectionDataType selectionDataType) {
        this.selectionDataType = selectionDataType;
    }

    public Double getTopPercent() {
        return topPercent;
    }

    public void setTopPercent(Double topPercent) {
        this.topPercent = topPercent;
    }

    public Double getLeftPercent() {
        return leftPercent;
    }

    public void setLeftPercent(Double leftPercent) {
        this.leftPercent = leftPercent;
    }

    public Double getWidthPercent() {
        return widthPercent;
    }

    public void setWidthPercent(Double widthPercent) {
        this.widthPercent = widthPercent;
    }

    public Double getHeightPercent() {
        return heightPercent;
    }

    public void setHeightPercent(Double heightPercent) {
        this.heightPercent = heightPercent;
    }

    public Integer getIntHtmlColor() {
        return intHtmlColor;
    }

    public void setIntHtmlColor(Integer intHtmlColor) {
        this.intHtmlColor = intHtmlColor;
    }

    public String getHexHtmlColour() {
        return hexHtmlColour;
    }

    public void setHexHtmlColour(String hexHtmlColour) {
        this.hexHtmlColour = hexHtmlColour;
    }

    public WebUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(WebUser createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public WebUser getLastEditBy() {
        return lastEditBy;
    }

    public void setLastEditBy(WebUser lastEditBy) {
        this.lastEditBy = lastEditBy;
    }

    public Date getLastEditeAt() {
        return lastEditeAt;
    }

    public void setLastEditeAt(Date lastEditeAt) {
        this.lastEditeAt = lastEditeAt;
    }

    public WebUser getRetiredReversedBy() {
        return retiredReversedBy;
    }

    public void setRetiredReversedBy(WebUser retiredReversedBy) {
        this.retiredReversedBy = retiredReversedBy;
    }

    public Date getRetiredReversedAt() {
        return retiredReversedAt;
    }

    public void setRetiredReversedAt(Date retiredReversedAt) {
        this.retiredReversedAt = retiredReversedAt;
    }

    public boolean isRetired() {
        return retired;
    }

    public void setRetired(boolean retired) {
        this.retired = retired;
    }

    public WebUser getRetiredBy() {
        return retiredBy;
    }

    public void setRetiredBy(WebUser retiredBy) {
        this.retiredBy = retiredBy;
    }

    public Date getRetiredAt() {
        return retiredAt;
    }

    public void setRetiredAt(Date retiredAt) {
        this.retiredAt = retiredAt;
    }

    public String getRetireComments() {
        return retireComments;
    }

    public void setRetireComments(String retireComments) {
        this.retireComments = retireComments;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public AvailableDataType getAvailableDataType() {
        return availableDataType;
    }

    public void setAvailableDataType(AvailableDataType availableDataType) {
        this.availableDataType = availableDataType;
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

    public Double getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Double orderNo) {
        this.orderNo = orderNo;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public DataPopulationStrategy getDataPopulationStrategy() {
        return dataPopulationStrategy;
    }

    public void setDataPopulationStrategy(DataPopulationStrategy dataPopulationStrategy) {
        this.dataPopulationStrategy = dataPopulationStrategy;
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

    public ItemArrangementStrategy getItemArrangementStrategy() {
        return itemArrangementStrategy;
    }

    public void setItemArrangementStrategy(ItemArrangementStrategy itemArrangementStrategy) {
        this.itemArrangementStrategy = itemArrangementStrategy;
    }

    public Component getReferenceComponent() {
        return referenceComponent;
    }

    public void setReferenceComponent(Component referenceComponent) {
        this.referenceComponent = referenceComponent;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
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

    public String getRequiredErrorMessage() {
        return requiredErrorMessage;
    }

    public void setRequiredErrorMessage(String requiredErrorMessage) {
        this.requiredErrorMessage = requiredErrorMessage;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public String getDescreption() {
        return descreption;
    }

    public void setDescreption(String descreption) {
        this.descreption = descreption;
    }

    public boolean isCalculateOnFocus() {
        return calculateOnFocus;
    }

    public void setCalculateOnFocus(boolean calculateOnFocus) {
        this.calculateOnFocus = calculateOnFocus;
    }

    public String getCalculationScript() {
        return calculationScript;
    }

    public void setCalculationScript(String calculationScript) {
        this.calculationScript = calculationScript;
    }

    public String getBackgroundColour() {
        return backgroundColour;
    }

    public void setBackgroundColour(String backgroundColour) {
        this.backgroundColour = backgroundColour;
    }

    public String getForegroundColour() {
        return foregroundColour;
    }

    public void setForegroundColour(String foregroundColour) {
        this.foregroundColour = foregroundColour;
    }

    public String getGeneratedCss() {
        generatedCss = "";
        if (css != null) {
            generatedCss += css;
        }
        if (heightPercent != null) {
            generatedCss += "; height:" + heightPercent + "%; ";
        }
        if (leftPercent != null) {
            generatedCss += "; left:" + leftPercent + "%; ";
        }
        if (widthPercent != null) {
            generatedCss += "; width:" + widthPercent + "%; ";
        }
        if (heightPercent != null) {
            generatedCss += "; height:" + heightPercent + "%; ";
        }
        if (backgroundColour != null && !backgroundColour.equals("")) {
            generatedCss += "; background-color:#" + backgroundColour + "; ";
        }
        if (foregroundColour != null && !foregroundColour.equals("")) {
            generatedCss += "; color:#" + foregroundColour + "; ";
        }
        if (borderColour != null && !borderColour.equals("")) {
            generatedCss += "; border-color: #" + foregroundColour + "; ";
        }
        return generatedCss;
    }

    public void setGeneratedCss(String generatedCss) {

        this.generatedCss = generatedCss;
    }

    public String getCss() {
        return css;
    }

    public void setCss(String css) {
        this.css = css;
    }

    public String getBorderColour() {
        return borderColour;
    }

    public void setBorderColour(String borderColour) {
        this.borderColour = borderColour;
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

    public String getCalculationScriptForBackgroundColour() {
        return calculationScriptForBackgroundColour;
    }

    public void setCalculationScriptForBackgroundColour(String calculationScriptForBackgroundColour) {
        this.calculationScriptForBackgroundColour = calculationScriptForBackgroundColour;
    }

    public boolean isMultipleEntiesPerForm() {
        return multipleEntiesPerForm;
    }

    public void setMultipleEntiesPerForm(boolean multipleEntiesPerForm) {
        this.multipleEntiesPerForm = multipleEntiesPerForm;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getIntegerNumberValue2() {
        return integerNumberValue2;
    }

    public void setIntegerNumberValue2(Integer integerNumberValue2) {
        this.integerNumberValue2 = integerNumberValue2;
    }

    public Long getLongNumberValue2() {
        return longNumberValue2;
    }

    public void setLongNumberValue2(Long longNumberValue2) {
        this.longNumberValue2 = longNumberValue2;
    }

    public Double getRealNumberValue2() {
        return realNumberValue2;
    }

    public void setRealNumberValue2(Double realNumberValue2) {
        this.realNumberValue2 = realNumberValue2;
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

    public DataPopulationStrategy getResultDisplayStrategy() {
        return resultDisplayStrategy;
    }

    public void setResultDisplayStrategy(DataPopulationStrategy resultDisplayStrategy) {
        this.resultDisplayStrategy = resultDisplayStrategy;
    }

    public Observation getObservationValue() {
        return observationValue;
    }

    public void setObservationValue(Observation observationValue) {
        this.observationValue = observationValue;
    }

    public Procedure getProcedureValue() {
        return procedureValue;
    }

    public void setProcedureValue(Procedure procedureValue) {
        this.procedureValue = procedureValue;
    }

    public Movement getMovementValue() {
        return movementValue;
    }

    public void setMovementValue(Movement movementValue) {
        this.movementValue = movementValue;
    }

    
    
}
