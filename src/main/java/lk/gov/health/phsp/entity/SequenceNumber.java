/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.gov.health.phsp.entity;

import java.io.Serializable;
import javax.jdo.annotations.Index;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 *
 * @author Dr M H B Ariyaratne <buddhika.ari at gmail.com>
 */
@Entity
public class SequenceNumber implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    private Long id = 1L; // Always 1 for the single row
    @Index
    private Long lastUsedId;

    // Getters and setters
    public Long getLastUsedId() {
        return lastUsedId;
    }

    public void setLastUsedId(Long lastUsedId) {
        this.lastUsedId = lastUsedId;
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
        if (!(object instanceof SequenceNumber)) {
            return false;
        }
        SequenceNumber other = (SequenceNumber) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "lk.gov.health.phsp.entity.SequenceNumber[ id=" + id + " ]";
    }
    
}
