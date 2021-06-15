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
package lk.gov.health.phsp.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;

/**
 *
 * @author buddhika
 */
@Entity
public class ReportCell implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    private ReportRow row;
    @ManyToOne
    private ReportColumn column;
    private Double dblValue;
    private String stringValue;
    private Long longValue;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date dateValue;
    private boolean containsDoubleValue;
    private boolean containsLongValue;
    private boolean containsStringValue;
    private boolean containsDateValue;

    @ManyToOne
    private StoredQueryResult storedQueryResult;
    
    
    
    
    
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
        if (!(object instanceof ReportCell)) {
            return false;
        }
        ReportCell other = (ReportCell) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "lk.gov.health.phsp.entity.ReportCell[ id=" + id + " ]";
    }

    public ReportRow getRow() {
        return row;
    }

    public void setRow(ReportRow row) {
        this.row = row;
    }

    public ReportColumn getColumn() {
        return column;
    }

    public void setColumn(ReportColumn column) {
        this.column = column;
    }

    public Double getDblValue() {
        return dblValue;
    }

    public void setDblValue(Double dblValue) {
        this.dblValue = dblValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public Long getLongValue() {
        return longValue;
    }

    public void setLongValue(Long longValue) {
        this.longValue = longValue;
    }

    public Date getDateValue() {
        return dateValue;
    }

    public void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }

    public boolean isContainsDoubleValue() {
        return containsDoubleValue;
    }

    public void setContainsDoubleValue(boolean containsDoubleValue) {
        this.containsDoubleValue = containsDoubleValue;
    }

    public boolean isContainsLongValue() {
        return containsLongValue;
    }

    public void setContainsLongValue(boolean containsLongValue) {
        this.containsLongValue = containsLongValue;
    }

    public boolean isContainsStringValue() {
        return containsStringValue;
    }

    public void setContainsStringValue(boolean containsStringValue) {
        this.containsStringValue = containsStringValue;
    }

    public boolean isContainsDateValue() {
        return containsDateValue;
    }

    public void setContainsDateValue(boolean containsDateValue) {
        this.containsDateValue = containsDateValue;
    }

    public StoredQueryResult getStoredQueryResult() {
        return storedQueryResult;
    }

    public void setStoredQueryResult(StoredQueryResult storedQueryResult) {
        this.storedQueryResult = storedQueryResult;
    }
    
}
