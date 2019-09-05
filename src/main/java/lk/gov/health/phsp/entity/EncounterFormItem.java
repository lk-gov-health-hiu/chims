/*
 * To change this license header, choose License Headers in Client Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lk.gov.health.phsp.entity;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author buddhika
 */
@Entity
@XmlRootElement
public class EncounterFormItem implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    private Client project;
    @ManyToOne
    private Institution institution;
    @Lob
    private String natureOfIntervention;
    @Lob
    private String currentStatusOfIntervention;
    private Boolean isIncludeDevelopmentActivity;

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
        if (!(object instanceof EncounterFormItem)) {
            return false;
        }
        EncounterFormItem other = (EncounterFormItem) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.ProjectInstitution[ id=" + id + " ]";
    }

    public Client getProject() {
        return project;
    }

    public void setProject(Client project) {
        this.project = project;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public String getNatureOfIntervention() {
        return natureOfIntervention;
    }

    public void setNatureOfIntervention(String natureOfIntervention) {
        this.natureOfIntervention = natureOfIntervention;
    }

    public String getCurrentStatusOfIntervention() {
        return currentStatusOfIntervention;
    }

    public void setCurrentStatusOfIntervention(String currentStatusOfIntervention) {
        this.currentStatusOfIntervention = currentStatusOfIntervention;
    }

    public Boolean getIsIncludeDevelopmentActivity() {
        return isIncludeDevelopmentActivity;
    }

    public void setIsIncludeDevelopmentActivity(Boolean isIncludeDevelopmentActivity) {
        this.isIncludeDevelopmentActivity = isIncludeDevelopmentActivity;
    }

}
