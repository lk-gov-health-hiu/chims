/*
 * The MIT License
 *
 * Copyright 2021 buddhika.
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
package lk.gov.health.phsp.pojcs;

/**
 *
 * @author buddhika
 */
public class PrescriptionItemPojo {
    private Long medicineId;
    private String medicine;
    private String medicineType;
    private Double dose;
    private Long doseUnitId;
    private Long frequencyUnitId;
    private Double duration;
    private Long durationUnitId;
    private String comments;
    private Double issueQty;
    private Long issueUnitId;
    private String prescriptionItemString;

    public Long getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Long medicineId) {
        this.medicineId = medicineId;
    }

    public String getMedicine() {
        return medicine;
    }

    public void setMedicine(String medicine) {
        this.medicine = medicine;
    }

    public String getMedicineType() {
        return medicineType;
    }

    public void setMedicineType(String medicineType) {
        this.medicineType = medicineType;
    }

    public Double getDose() {
        return dose;
    }

    public void setDose(Double dose) {
        this.dose = dose;
    }

    public Long getDoseUnitId() {
        return doseUnitId;
    }

    public void setDoseUnitId(Long doseUnitId) {
        this.doseUnitId = doseUnitId;
    }

    public Long getFrequencyUnitId() {
        return frequencyUnitId;
    }

    public void setFrequencyUnitId(Long frequencyUnitId) {
        this.frequencyUnitId = frequencyUnitId;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public Long getDurationUnitId() {
        return durationUnitId;
    }

    public void setDurationUnitId(Long durationUnitId) {
        this.durationUnitId = durationUnitId;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Double getIssueQty() {
        return issueQty;
    }

    public void setIssueQty(Double issueQty) {
        this.issueQty = issueQty;
    }

    public Long getIssueUnitId() {
        return issueUnitId;
    }

    public void setIssueUnitId(Long issueUnitId) {
        this.issueUnitId = issueUnitId;
    }

    public String getPrescriptionItemString() {
        return prescriptionItemString;
    }

    public void setPrescriptionItemString(String prescriptionItemString) {
        this.prescriptionItemString = prescriptionItemString;
    }
    
    
    
}
