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
import javax.xml.bind.annotation.XmlRootElement;
import lk.gov.health.phsp.enums.DataRepresentationType;

/**
 *
 * @author buddhika
 */
@Entity
@XmlRootElement
public class ClientEncounterComponentItem extends ClientEncounterComponent {

    @ManyToOne(fetch = FetchType.LAZY)
    private Client itemClient;
    @ManyToOne(fetch = FetchType.LAZY)
    private Encounter itemEncounter;
    @ManyToOne(fetch = FetchType.LAZY)
    private ClientEncounterComponentFormSet itemFormset;
    @Enumerated(EnumType.STRING)
    private DataRepresentationType dataRepresentationType;

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
    @ManyToOne(fetch = FetchType.LAZY)
    private Item itemValue;
    @ManyToOne(fetch = FetchType.LAZY)
    private Area areaValue;
    @ManyToOne(fetch = FetchType.LAZY)
    private Institution institutionValue;
    @ManyToOne(fetch = FetchType.LAZY)
    private Client clientValue;
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Prescription prescriptionValue;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Observation observationValue;
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Procedure procedureValue;
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Movement movementValue;

    private Integer integerNumberValue2;
    private Long longNumberValue2;
    private Double realNumberValue2;

    public Client getItemClient() {
        return itemClient;
    }

    public void setItemClient(Client itemClient) {
        this.itemClient = itemClient;
    }

    public Encounter getItemEncounter() {
        return itemEncounter;
    }

    public void setItemEncounter(Encounter itemEncounter) {
        this.itemEncounter = itemEncounter;
    }

    public ClientEncounterComponentFormSet getItemFormset() {
        return itemFormset;
    }

    public void setItemFormset(ClientEncounterComponentFormSet itemFormset) {
        this.itemFormset = itemFormset;
    }

    public DataRepresentationType getDataRepresentationType() {
        return dataRepresentationType;
    }

    public void setDataRepresentationType(DataRepresentationType dataRepresentationType) {
        this.dataRepresentationType = dataRepresentationType;
    }

    public String getLongTextValue() {
        return longTextValue;
    }

    public void setLongTextValue(String longTextValue) {
        this.longTextValue = longTextValue;
    }

    public String getDescreptionValue() {
        return descreptionValue;
    }

    public void setDescreptionValue(String descreptionValue) {
        this.descreptionValue = descreptionValue;
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

    public Long getLongNumberValue() {
        return longNumberValue;
    }

    public void setLongNumberValue(Long longNumberValue) {
        this.longNumberValue = longNumberValue;
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

    public Date getDateValue() {
        return dateValue;
    }

    public void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }

    public Item getItemValue() {
        if (itemValue == null && prescriptionValue != null) {
            itemValue = prescriptionValue.getMedicine();
        }
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

    public Prescription getPrescriptionValue() {
        return prescriptionValue;
    }

    public void setPrescriptionValue(Prescription prescriptionValue) {
//        if (prescriptionValue == null) {
//            prescriptionValue = new Prescription();
//        }
        this.prescriptionValue = prescriptionValue;
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

}
