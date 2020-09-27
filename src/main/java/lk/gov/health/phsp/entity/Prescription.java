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
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import lk.gov.health.phsp.enums.PrescriptionType;

/**
 *
 * @author Dr M H B Ariyaratne<buddhika.ari@gmail.com>
 */
@Entity
public class Prescription implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(EnumType.STRING)
    private PrescriptionType prescriptionType;

    @ManyToOne
    private Item medicine;
    @ManyToOne
    private Item medicineSuggested;
    private Double dose;
    @ManyToOne
    private Item doseUnit;
    @ManyToOne
    private Item frequency;
    private Double duration;
    @ManyToOne
    private Item durationUnit;
    private Double issueQuantity;
    @ManyToOne
    private Item issueUnit;
    @Lob
    private String description;
    private boolean repeatPrescription;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date startFrom;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date continuedUpTo;

    @ManyToOne
    private Client client;
    @ManyToOne
    private Encounter encounter;

    /*
    Omitting Properties
     */
    private boolean omitted;
    @ManyToOne
    private WebUser omittedBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date omittedAt;
    private String omitComments;
    private boolean omittedAsPrescribed;

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
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Prescription)) {
            return false;
        }
        Prescription other = (Prescription) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {

        String str = "";
        if (medicine == null) {
            return str;
        }
        str += medicine.getName() + " ";
        if (medicineSuggested != null) {
            str += " (" + medicineSuggested.getName() + ") ";
        }
        if (dose != null & doseUnit != null) {
            str += dose + " " + doseUnit.getName();
        }
        if (frequency != null) {
            str += frequency + " ";
        }
        if (duration != null && durationUnit != null) {
            str += duration + " " + durationUnit.getName() + " ";
        }
        if (issueQuantity != null && issueUnit != null) {
            str += issueQuantity +  " " + issueUnit.getName();
        }

        return str;
    }

    public Item getMedicine() {
        return medicine;
    }

    public void setMedicine(Item medicine) {
        this.medicine = medicine;
    }

    public Item getMedicineSuggested() {
        return medicineSuggested;
    }

    public void setMedicineSuggested(Item medicineSuggested) {
        this.medicineSuggested = medicineSuggested;
    }

    public Double getDose() {
        return dose;
    }

    public void setDose(Double dose) {
        this.dose = dose;
    }

    public Item getDoseUnit() {
        return doseUnit;
    }

    public void setDoseUnit(Item doseUnit) {
        this.doseUnit = doseUnit;
    }

    public Item getFrequency() {
        return frequency;
    }

    public void setFrequency(Item frequency) {
        this.frequency = frequency;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public Item getDurationUnit() {
        return durationUnit;
    }

    public void setDurationUnit(Item durationUnit) {
        this.durationUnit = durationUnit;
    }

    public Double getIssueQuantity() {
        return issueQuantity;
    }

    public void setIssueQuantity(Double issueQuantity) {
        this.issueQuantity = issueQuantity;
    }

    public Item getIssueUnit() {
        return issueUnit;
    }

    public void setIssueUnit(Item issueUnit) {
        this.issueUnit = issueUnit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isRepeatPrescription() {
        return repeatPrescription;
    }

    public void setRepeatPrescription(boolean repeatPrescription) {
        this.repeatPrescription = repeatPrescription;
    }

    public Date getStartFrom() {
        return startFrom;
    }

    public void setStartFrom(Date startFrom) {
        this.startFrom = startFrom;
    }

    public Date getContinuedUpTo() {
        return continuedUpTo;
    }

    public void setContinuedUpTo(Date continuedUpTo) {
        this.continuedUpTo = continuedUpTo;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Encounter getEncounter() {
        return encounter;
    }

    public void setEncounter(Encounter encounter) {
        this.encounter = encounter;
    }

    public boolean isOmittedAsPrescribed() {
        return omittedAsPrescribed;
    }

    public void setOmittedAsPrescribed(boolean omittedAsPrescribed) {
        this.omittedAsPrescribed = omittedAsPrescribed;
    }

    public boolean isOmitted() {
        return omitted;
    }

    public void setOmitted(boolean omitted) {
        this.omitted = omitted;
    }

    public WebUser getOmittedBy() {
        return omittedBy;
    }

    public void setOmittedBy(WebUser omittedBy) {
        this.omittedBy = omittedBy;
    }

    public Date getOmittedAt() {
        return omittedAt;
    }

    public void setOmittedAt(Date omittedAt) {
        this.omittedAt = omittedAt;
    }

    public String getOmitComments() {
        return omitComments;
    }

    public void setOmitComments(String omitComments) {
        this.omitComments = omitComments;
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

    public PrescriptionType getPrescriptionType() {
        return prescriptionType;
    }

    public void setPrescriptionType(PrescriptionType prescriptionType) {
        this.prescriptionType = prescriptionType;
    }

}
