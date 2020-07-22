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
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import lk.gov.health.phsp.bean.CommonController;
import lk.gov.health.phsp.enums.TimePeriodType;

/**
 *
 * @author buddhika
 */
@Entity
public class StoredQueryResult implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Institution institution;
    @ManyToOne
    private Area area;
    @ManyToOne
    private WebUser webUser;
    @ManyToOne
    private QueryComponent queryComponent;
    @Enumerated(EnumType.STRING)
    private TimePeriodType timePeriodType;

    private Integer resultYear;
    private Integer resultQuarter;
    private Integer resultMonth;
    private Integer resultDateOfMonth;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date resultFrom;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date resultTo;

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date requestCreatedAt;

    private boolean processStarted;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date processStartedAt;

    private boolean processCompleted;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date processCompletedAt;

    private boolean processFailed;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date processFailedAt;

    @ManyToOne
    private Upload upload;

    private Long longValue;
    private Double doubleValue;
    @Lob
    private String errorMessage;

    @ManyToOne
    private WebUser creater;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdAt;
    //Retairing properties
    private boolean retired;
    @ManyToOne
    private WebUser retirer;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date retiredAt;
    private String retireComments;

    @Transient
    private String processStatus;
    @Transient
    private String periodString;

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
        if (!(object instanceof StoredQueryResult)) {
            return false;
        }
        StoredQueryResult other = (StoredQueryResult) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "lk.gov.health.phsp.enums.StoreQueryResults[ id=" + id + " ]";
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public WebUser getWebUser() {
        return webUser;
    }

    public void setWebUser(WebUser webUser) {
        this.webUser = webUser;
    }

    public QueryComponent getQueryComponent() {
        return queryComponent;
    }

    public void setQueryComponent(QueryComponent queryComponent) {
        this.queryComponent = queryComponent;
    }

    public Date getRequestCreatedAt() {
        return requestCreatedAt;
    }

    public void setRequestCreatedAt(Date requestCreatedAt) {
        this.requestCreatedAt = requestCreatedAt;
    }

    public boolean isProcessStarted() {
        return processStarted;
    }

    public void setProcessStarted(boolean processStarted) {
        this.processStarted = processStarted;
    }

    public Date getProcessStartedAt() {
        return processStartedAt;
    }

    public void setProcessStartedAt(Date processStartedAt) {
        this.processStartedAt = processStartedAt;
    }

    public boolean isProcessCompleted() {
        return processCompleted;
    }

    public void setProcessCompleted(boolean processCompleted) {
        this.processCompleted = processCompleted;
    }

    public Date getProcessCompletedAt() {
        return processCompletedAt;
    }

    public void setProcessCompletedAt(Date processCompletedAt) {
        this.processCompletedAt = processCompletedAt;
    }

    public boolean isProcessFailed() {
        return processFailed;
    }

    public void setProcessFailed(boolean processFailed) {
        this.processFailed = processFailed;
    }

    public Date getProcessFailedAt() {
        return processFailedAt;
    }

    public void setProcessFailedAt(Date processFailedAt) {
        this.processFailedAt = processFailedAt;
    }

    public Upload getUpload() {
        return upload;
    }

    public void setUpload(Upload upload) {
        this.upload = upload;
    }

    public Long getLongValue() {
        return longValue;
    }

    public void setLongValue(Long longValue) {
        this.longValue = longValue;
    }

    public Double getDoubleValue() {
        return doubleValue;
    }

    public void setDoubleValue(Double doubleValue) {
        this.doubleValue = doubleValue;
    }

    public WebUser getCreater() {
        return creater;
    }

    public void setCreater(WebUser creater) {
        this.creater = creater;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRetired() {
        return retired;
    }

    public void setRetired(boolean retired) {
        this.retired = retired;
    }

    public WebUser getRetirer() {
        return retirer;
    }

    public void setRetirer(WebUser retirer) {
        this.retirer = retirer;
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

    public TimePeriodType getTimePeriodType() {
        return timePeriodType;
    }

    public void setTimePeriodType(TimePeriodType timePeriodType) {
        this.timePeriodType = timePeriodType;
    }

    public Integer getResultYear() {
        return resultYear;
    }

    public void setResultYear(Integer resultYear) {
        this.resultYear = resultYear;
    }

    public Integer getResultQuarter() {
        return resultQuarter;
    }

    public void setResultQuarter(Integer resultQuarter) {
        this.resultQuarter = resultQuarter;
    }

    public Integer getResultMonth() {
        return resultMonth;
    }

    public void setResultMonth(Integer resultMonth) {
        this.resultMonth = resultMonth;
    }

    public Integer getResultDateOfMonth() {
        return resultDateOfMonth;
    }

    public void setResultDateOfMonth(Integer resultDateOfMonth) {
        this.resultDateOfMonth = resultDateOfMonth;
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

    public String getProcessStatus() {
        processStatus = "Unknown";
        if (processStarted == false) {
            return "Awaiting in the queue for Processing";
        } else if (processFailed) {
            return "Processing failed";
        } else if (processCompleted) {
            return "Processing completed";
        }
        return processStatus;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getPeriodString() {
        periodString = "";
        if (this.timePeriodType == null) {
            return periodString;
        }
        switch (this.timePeriodType) {
            case Yearley:
                periodString = "Year : " + this.resultYear;
                break;
            case Quarterly:
                ;
                periodString = "Year : " + 
                        this.resultYear + 
                        ", Quarter : " +
                        CommonController.quarterAsString(this.resultQuarter) ;
                break;
            case Monthly:
                periodString = "Year : " + 
                        this.resultYear + 
                        ", Month : " +
                        CommonController.monthAsString(this.resultMonth) ;
                break;
            case Dates:
                periodString = "From : " + 
                        CommonController.dateTimeToString(this.resultFrom) + 
                        ", To : " + 
                        CommonController.dateTimeToString(this.resultTo);
                break;
        }
        return periodString;
    }

    public void setPeriodString(String periodString) {
        this.periodString = periodString;
    }

}
