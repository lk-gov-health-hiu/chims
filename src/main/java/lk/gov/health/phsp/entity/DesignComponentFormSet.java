
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

import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
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

/**
 *
 * @author sunila_soft
 */
@Entity
public class DesignComponentFormSet extends DesignComponent  {
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
}
