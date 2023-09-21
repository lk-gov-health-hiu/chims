package lk.gov.health.phsp.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Dr M H B Ariyaratne thanks to CharGPT V 4.0
 */
@Entity
public class AuditEvent implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String status;
    private String auditEventAction;
    @Temporal(TemporalType.TIMESTAMP)
    private Date AuditEventTimestamp;
    private String agent;
    private String entity;
    @Lob
    private String fullAuditEvent;

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
        if (!(object instanceof AuditEvent)) {
            return false;
        }
        AuditEvent other = (AuditEvent) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "lk.gov.health.phsp.entity.AuditEvent[ id=" + id + " ]";
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAuditEventAction() {
        return auditEventAction;
    }

    public void setAuditEventAction(String auditEventAction) {
        this.auditEventAction = auditEventAction;
    }

    public Date getAuditEventTimestamp() {
        return AuditEventTimestamp;
    }

    public void setAuditEventTimestamp(Date AuditEventTimestamp) {
        this.AuditEventTimestamp = AuditEventTimestamp;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getFullAuditEvent() {
        return fullAuditEvent;
    }

    public void setFullAuditEvent(String fullAuditEvent) {
        this.fullAuditEvent = fullAuditEvent;
    }
    
}
