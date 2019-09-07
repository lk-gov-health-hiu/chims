/*
 * To change this license header, choose License Headers in Project Properties.
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
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import lk.gov.health.phsp.enums.DataStorageType;
import lk.gov.health.phsp.enums.RenderType;

/**
 *
 * @author buddhika
 */
@Entity
public class Component implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    String name;

    @ManyToOne
    private Component parentComponent;
    
    @Enumerated(EnumType.STRING)
    RenderType renderType;

    @ManyToOne
    private Item mimeType;

    @Enumerated(EnumType.STRING)
    private DataStorageType dataStorageType;

    private Double topPercent;
    private Double leftPercent;
    private Double widthPercent;
    private Double heightPercent;

    private Integer intHtmlColor;
    @Transient
    private String hexHtmlColour;
    /*
    Create Properties
     */
    @ManyToOne
    private WebUser createdBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdAt;
    /*
    Last Edit Properties
     */
    @ManyToOne
    private WebUser lastEditBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date lastEditeAt;
    /*
    Retire Reversal Properties
     */
    @ManyToOne
    private WebUser retiredReversedBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date retiredReversedAt;
    /*
    Retire Properties
     */
    private boolean retired;
    @ManyToOne
    private WebUser retiredBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date retiredAt;
    private String retireComments;
    @Lob
    private String html;
    

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
        if (!(object instanceof Component)) {
            return false;
        }
        Component other = (Component) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "lk.gov.health.phsp.entity.EncounterItem[ id=" + id + " ]";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RenderType getRenderType() {
        return renderType;
    }

    public void setRenderType(RenderType renderType) {
        this.renderType = renderType;
    }

    
    
    public Component getParentComponent() {
        return parentComponent;
    }

    public void setParentComponent(Component parentComponent) {
        this.parentComponent = parentComponent;
    }

    public Item getMimeType() {
        return mimeType;
    }

    public void setMimeType(Item mimeType) {
        this.mimeType = mimeType;
    }

    public DataStorageType getDataStorageType() {
        return dataStorageType;
    }

    public void setDataStorageType(DataStorageType dataStorageType) {
        this.dataStorageType = dataStorageType;
    }

    public Double getTopPercent() {
        return topPercent;
    }

    public void setTopPercent(Double topPercent) {
        this.topPercent = topPercent;
    }

    public Double getLeftPercent() {
        return leftPercent;
    }

    public void setLeftPercent(Double leftPercent) {
        this.leftPercent = leftPercent;
    }

    public Double getWidthPercent() {
        return widthPercent;
    }

    public void setWidthPercent(Double widthPercent) {
        this.widthPercent = widthPercent;
    }

    public Double getHeightPercent() {
        return heightPercent;
    }

    public void setHeightPercent(Double heightPercent) {
        this.heightPercent = heightPercent;
    }

    public Integer getIntHtmlColor() {
        return intHtmlColor;
    }

    public void setIntHtmlColor(Integer intHtmlColor) {
        this.intHtmlColor = intHtmlColor;
    }

    public String getHexHtmlColour() {
        return hexHtmlColour;
    }

    public void setHexHtmlColour(String hexHtmlColour) {
        this.hexHtmlColour = hexHtmlColour;
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

    public WebUser getLastEditBy() {
        return lastEditBy;
    }

    public void setLastEditBy(WebUser lastEditBy) {
        this.lastEditBy = lastEditBy;
    }

    public Date getLastEditeAt() {
        return lastEditeAt;
    }

    public void setLastEditeAt(Date lastEditeAt) {
        this.lastEditeAt = lastEditeAt;
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

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }
    
    

}
