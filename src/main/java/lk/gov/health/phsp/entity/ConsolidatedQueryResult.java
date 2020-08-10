/*
 * The MIT License
 *
 * Copyright 2020 buddhika.
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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import lk.gov.health.phsp.enums.TimePeriodType;

/**
 *
 * @author buddhika
 */
@Entity
public class ConsolidatedQueryResult implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Institution institution;

    @ManyToOne(fetch = FetchType.LAZY)
    private Area area;

    @Column(length = 180)
    private String queryComponentCode;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date resultFrom;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date resultTo;

    private Long longValue;
    private Long lastIndividualQueryResultId;

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
        if (!(object instanceof ConsolidatedQueryResult)) {
            return false;
        }
        ConsolidatedQueryResult other = (ConsolidatedQueryResult) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "lk.gov.health.phsp.entity.QueryResult[ id=" + id + " ]";
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public String getQueryComponentCode() {
        return queryComponentCode;
    }

    public void setQueryComponentCode(String queryComponentCode) {
        this.queryComponentCode = queryComponentCode;
    }

    public Date getResultFrom() {
        return resultFrom;
    }

    public void setResultFrom(Date resultFrom) {
        this.resultFrom = resultFrom;
    }

    public Date getResultTo() {
        return resultTo;
    }

    public void setResultTo(Date resultTo) {
        this.resultTo = resultTo;
    }

    public Long getLongValue() {
        return longValue;
    }

    public void setLongValue(Long longValue) {
        this.longValue = longValue;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public Long getLastIndividualQueryResultId() {
        return lastIndividualQueryResultId;
    }

    public void setLastIndividualQueryResultId(Long lastIndividualQueryResultId) {
        this.lastIndividualQueryResultId = lastIndividualQueryResultId;
    }

}
