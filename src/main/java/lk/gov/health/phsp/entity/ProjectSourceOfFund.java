/*
 * To change this license header, choose License Headers in Project Properties.
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
public class ProjectSourceOfFund implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    private Project project;
    @ManyToOne
    private Item sourceOfFund;
    private Double fundValue;
    @ManyToOne
    private Item fundUnit;
    @Lob
    private String comments;
    

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
        if (!(object instanceof ProjectSourceOfFund)) {
            return false;
        }
        ProjectSourceOfFund other = (ProjectSourceOfFund) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.ProjectInstitution[ id=" + id + " ]";
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Item getFundUnit() {
        return fundUnit;
    }

    public void setFundUnit(Item fundUnit) {
        this.fundUnit = fundUnit;
    }

    public Item getSourceOfFund() {
        return sourceOfFund;
    }

    public void setSourceOfFund(Item sourceOfFund) {
        this.sourceOfFund = sourceOfFund;
    }

    public Double getFundValue() {
        return fundValue;
    }

    public void setFundValue(Double fundValue) {
        this.fundValue = fundValue;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

   

}
