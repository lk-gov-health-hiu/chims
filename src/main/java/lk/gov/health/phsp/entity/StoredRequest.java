/*
 * The MIT License
 *
 * Copyright 2022 buddhika.
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
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import lk.gov.health.phsp.pojcs.Identifiable;

/**
 *
 * @author buddhika
 */
@Entity
public class StoredRequest implements Serializable, Identifiable  {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    private boolean pending;
    @ManyToOne
    private Institution institution;
    private Integer ryear;
    private Integer rmonth;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date requestCreatedAt;
    
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date processStartedAt;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date processCompletedAt;
    private boolean processFailed;
    private boolean processSuccess;
    
    
    
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
        if (!(object instanceof StoredRequest)) {
            return false;
        }
        StoredRequest other = (StoredRequest) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "lk.gov.health.phsp.entity.StoredRequest[ id=" + id + " ]";
    }

    public boolean isPending() {
        return pending;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Integer getRyear() {
        return ryear;
    }

    public void setRyear(Integer ryear) {
        this.ryear = ryear;
    }

    public Integer getRmonth() {
        return rmonth;
    }

    public void setRmonth(Integer rmonth) {
        this.rmonth = rmonth;
    }

    public Date getProcessStartedAt() {
        return processStartedAt;
    }

    public void setProcessStartedAt(Date processStartedAt) {
        this.processStartedAt = processStartedAt;
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

    public boolean isProcessSuccess() {
        return processSuccess;
    }

    public void setProcessSuccess(boolean processSuccess) {
        this.processSuccess = processSuccess;
    }

    public Date getRequestCreatedAt() {
        return requestCreatedAt;
    }

    public void setRequestCreatedAt(Date requestCreatedAt) {
        this.requestCreatedAt = requestCreatedAt;
    }

  
    
    
    
}
