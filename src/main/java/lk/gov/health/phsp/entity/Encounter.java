/*
 * To change this license header, choose License Headers in Client Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.xml.bind.annotation.XmlRootElement;
import lk.gov.health.phsp.enums.EncounterType;

/**
 *
 * @author buddhika
 */
@Entity
@XmlRootElement
public class Encounter implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Client project;
    @ManyToOne
    private Area area;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date encounterDate;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date encounterFrom;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date encounterTo;

    @Enumerated(EnumType.STRING)
    private EncounterType encounterType;

    @ManyToOne
    private Encounter parentEncounter;

    @ManyToOne
    Institution institution;
    
    @ManyToOne
    Institution referalInstitution;

    @ManyToOne
    private WebUser createdBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createAt;

    private boolean retired;
    @ManyToOne
    private WebUser retiredBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date retiredAt;

    @ManyToOne
    private WebUser retiredReversedBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date retiredReversedAt;

    private Boolean completed;
    @ManyToOne
    private WebUser completedBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date completedAt;

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
        if (!(object instanceof Encounter)) {
            return false;
        }
        Encounter other = (Encounter) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "id=" + id + "";
    }

    public Client getProject() {
        return project;
    }

    public void setProject(Client project) {
        this.project = project;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public Date getEncounterDate() {
        return encounterDate;
    }

    public void setEncounterDate(Date encounterDate) {
        this.encounterDate = encounterDate;
    }

    public Date getEncounterFrom() {
        return encounterFrom;
    }

    public void setEncounterFrom(Date encounterFrom) {
        this.encounterFrom = encounterFrom;
    }

    public Date getEncounterTo() {
        return encounterTo;
    }

    public void setEncounterTo(Date encounterTo) {
        this.encounterTo = encounterTo;
    }

    public EncounterType getEncounterType() {
        return encounterType;
    }

    public void setEncounterType(EncounterType encounterType) {
        this.encounterType = encounterType;
    }

    public Encounter getParentEncounter() {
        return parentEncounter;
    }

    public void setParentEncounter(Encounter parentEncounter) {
        this.parentEncounter = parentEncounter;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public WebUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(WebUser createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
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

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Institution getReferalInstitution() {
        return referalInstitution;
    }

    public void setReferalInstitution(Institution referalInstitution) {
        this.referalInstitution = referalInstitution;
    }

    public WebUser getCompletedBy() {
        return completedBy;
    }

    public void setCompletedBy(WebUser completedBy) {
        this.completedBy = completedBy;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }
    
    

}
