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
import javax.jdo.annotations.Index;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import lk.gov.health.phsp.pojcs.Identifiable;

/**
 *
 * @author buddhika
 */
@Entity
public class ApiRequest implements Serializable , Identifiable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Index
    @Column(length = 100)
    private String name;

    @Index
    @ManyToOne(fetch = FetchType.EAGER)
    private Encounter encounter;

    @Index
    @ManyToOne(fetch = FetchType.EAGER)
    private ClientEncounterComponentItem requestCeci;

    @Index
    @ManyToOne(fetch = FetchType.EAGER)
    private DesignComponentFormItem requestDcfi;

    @Index
    @ManyToOne(fetch = FetchType.EAGER)
    private ApiRequest parent;

    @Lob
    private String requestMessage;

    @Lob
    private String response;

    private boolean success;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date successedAt;
    private boolean failed;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date failedAt;
    private int failedAttempts;

    /*
    Convey Properties
     */
    private boolean convaied;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date convaiedAt;

    private boolean responseReceived;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date responseReceivedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    private ClientEncounterComponentItem responseCeci;

    @ManyToOne(fetch = FetchType.EAGER)
    private ClientEncounterComponentFormSet requestCefs;

    
    /*
    Create Properties
     */
    @ManyToOne(fetch = FetchType.EAGER)
    private WebUser createdBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdAt;

    /*
    Retire Properties
     */
    @Index
    private boolean retired;
    @ManyToOne(fetch = FetchType.EAGER)
    private WebUser retiredBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date retiredAt;

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
        if (!(object instanceof ApiRequest)) {
            return false;
        }
        ApiRequest other = (ApiRequest) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "lk.gov.health.phsp.entity.ApiRequest[ id=" + id + " ]";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Encounter getEncounter() {
        return encounter;
    }

    public void setEncounter(Encounter encounter) {
        this.encounter = encounter;
    }

    public ClientEncounterComponentItem getRequestCeci() {
        return requestCeci;
    }

    public void setRequestCeci(ClientEncounterComponentItem requestCeci) {
        this.requestCeci = requestCeci;
    }

    public String getRequestMessage() {
        return requestMessage;
    }

    public void setRequestMessage(String requestMessage) {
        this.requestMessage = requestMessage;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public Date getSuccessedAt() {
        return successedAt;
    }

    public void setSuccessedAt(Date successedAt) {
        this.successedAt = successedAt;
    }

    public Date getFailedAt() {
        return failedAt;
    }

    public void setFailedAt(Date failedAt) {
        this.failedAt = failedAt;
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

    public boolean isConvaied() {
        return convaied;
    }

    public void setConvaied(boolean convaied) {
        this.convaied = convaied;
    }

    public boolean isResponseReceived() {
        return responseReceived;
    }

    public void setResponseReceived(boolean responseReceived) {
        this.responseReceived = responseReceived;
    }

    public boolean isRetired() {
        return retired;
    }

    public void setRetired(boolean retired) {
        this.retired = retired;
    }

    public Date getConvaiedAt() {
        return convaiedAt;
    }

    public void setConvaiedAt(Date convaiedAt) {
        this.convaiedAt = convaiedAt;
    }

    public Date getResponseReceivedAt() {
        return responseReceivedAt;
    }

    public void setResponseReceivedAt(Date responseReceivedAt) {
        this.responseReceivedAt = responseReceivedAt;
    }

    public ClientEncounterComponentItem getResponseCeci() {
        return responseCeci;
    }

    public void setResponseCeci(ClientEncounterComponentItem responseCeci) {
        this.responseCeci = responseCeci;
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

    public ApiRequest getParent() {
        return parent;
    }

    public void setParent(ApiRequest parent) {
        this.parent = parent;
    }

    public DesignComponentFormItem getRequestDcfi() {
        return requestDcfi;
    }

    public void setRequestDcfi(DesignComponentFormItem requestDcfi) {
        this.requestDcfi = requestDcfi;
    }

    public ClientEncounterComponentFormSet getRequestCefs() {
        return requestCefs;
    }

    public void setRequestCefs(ClientEncounterComponentFormSet requestCefs) {
        this.requestCefs = requestCefs;
    }

}
