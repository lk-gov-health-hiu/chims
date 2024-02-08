package lk.gov.health.phsp.entity;

import java.io.Serializable;
import java.util.Date;
import javax.jdo.annotations.Index;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import lk.gov.health.phsp.enums.IntegrationEvent;
import lk.gov.health.phsp.pojcs.Identifiable;

/**
 *
 * @author buddh
 */
@Entity
public class IntegrationTrigger implements Serializable, Identifiable  {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Index
    @Enumerated(EnumType.STRING)
    private IntegrationEvent integrationEvent;
    @Index
    @ManyToOne
    private IntegrationEndpoint integrationEndpoint;

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
        if (!(object instanceof IntegrationTrigger)) {
            return false;
        }
        IntegrationTrigger other = (IntegrationTrigger) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "lk.gov.health.phsp.entity.IntegrationTrigger[ id=" + id + " ]";
    }

    public IntegrationEvent getIntegrationEvent() {
        return integrationEvent;
    }

    public void setIntegrationEvent(IntegrationEvent integrationEvent) {
        this.integrationEvent = integrationEvent;
    }

    public IntegrationEndpoint getIntegrationEndpoint() {
        return integrationEndpoint;
    }

    public void setIntegrationEndpoint(IntegrationEndpoint integrationEndpoint) {
        this.integrationEndpoint = integrationEndpoint;
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
    
}
