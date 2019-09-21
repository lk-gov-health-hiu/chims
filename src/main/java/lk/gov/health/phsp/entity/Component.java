/*
 * The MIT License
 *
 * Copyright 2019 Dr M H B Ariyaratne<buddhika.ari@gmail.com>.
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
import lk.gov.health.phsp.enums.AvailableDataType;
import lk.gov.health.phsp.enums.DataCompletionStrategy;
import lk.gov.health.phsp.enums.DataModificationStrategy;
import lk.gov.health.phsp.enums.DataPopulationStrategy;
import lk.gov.health.phsp.enums.ItemArrangementStrategy;
import lk.gov.health.phsp.enums.SelectionDataType;
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

    private Double orderNo;

    @ManyToOne
    private Institution institution;

    @ManyToOne
    private Component parentComponent;
    
    @ManyToOne
    private Component referenceComponent;
    

    @Enumerated(EnumType.STRING)
    RenderType renderType;

    @ManyToOne
    private Item mimeType;

    @Enumerated(EnumType.STRING)
    private SelectionDataType selectionDataType;

    @Enumerated(EnumType.STRING)
    private AvailableDataType availableDataType;

    @Enumerated(EnumType.STRING)
    private DataPopulationStrategy dataPopulationStrategy;

    @Enumerated(EnumType.STRING)
    private DataCompletionStrategy dataCompletionStrategy;

    @Enumerated(EnumType.STRING)
    private DataModificationStrategy dataModificationStrategy;

    @Enumerated(EnumType.STRING)
    private ItemArrangementStrategy itemArrangementStrategy;

    @ManyToOne
    private Area parentAreaOfAvailableAreas;
    @ManyToOne
    private Item categoryOfAvailableItems;
    @ManyToOne
    private Institution parentInstitutionOfAvailableInstitutions;

    private Double topPercent;
    private Double leftPercent;
    private Double widthPercent;
    private Double heightPercent;

    private Integer intHtmlColor;
    @Transient
    private String hexHtmlColour;
    @Lob
    private String html;
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

    public SelectionDataType getSelectionDataType() {
        return selectionDataType;
    }

    public void setSelectionDataType(SelectionDataType selectionDataType) {
        this.selectionDataType = selectionDataType;
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

    public AvailableDataType getAvailableDataType() {
        return availableDataType;
    }

    public void setAvailableDataType(AvailableDataType availableDataType) {
        this.availableDataType = availableDataType;
    }

    public Area getParentAreaOfAvailableAreas() {
        return parentAreaOfAvailableAreas;
    }

    public void setParentAreaOfAvailableAreas(Area parentAreaOfAvailableAreas) {
        this.parentAreaOfAvailableAreas = parentAreaOfAvailableAreas;
    }

    public Item getCategoryOfAvailableItems() {
        return categoryOfAvailableItems;
    }

    public void setCategoryOfAvailableItems(Item categoryOfAvailableItems) {
        this.categoryOfAvailableItems = categoryOfAvailableItems;
    }

    public Institution getParentInstitutionOfAvailableInstitutions() {
        return parentInstitutionOfAvailableInstitutions;
    }

    public void setParentInstitutionOfAvailableInstitutions(Institution parentInstitutionOfAvailableInstitutions) {
        this.parentInstitutionOfAvailableInstitutions = parentInstitutionOfAvailableInstitutions;
    }

    public Double getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Double orderNo) {
        this.orderNo = orderNo;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public DataPopulationStrategy getDataPopulationStrategy() {
        return dataPopulationStrategy;
    }

    public void setDataPopulationStrategy(DataPopulationStrategy dataPopulationStrategy) {
        this.dataPopulationStrategy = dataPopulationStrategy;
    }

    public DataCompletionStrategy getDataCompletionStrategy() {
        return dataCompletionStrategy;
    }

    public void setDataCompletionStrategy(DataCompletionStrategy dataCompletionStrategy) {
        this.dataCompletionStrategy = dataCompletionStrategy;
    }

    public DataModificationStrategy getDataModificationStrategy() {
        return dataModificationStrategy;
    }

    public void setDataModificationStrategy(DataModificationStrategy dataModificationStrategy) {
        this.dataModificationStrategy = dataModificationStrategy;
    }

    public ItemArrangementStrategy getItemArrangementStrategy() {
        return itemArrangementStrategy;
    }

    public void setItemArrangementStrategy(ItemArrangementStrategy itemArrangementStrategy) {
        this.itemArrangementStrategy = itemArrangementStrategy;
    }

    public Component getReferenceComponent() {
        return referenceComponent;
    }

    public void setReferenceComponent(Component referenceComponent) {
        this.referenceComponent = referenceComponent;
    }
    
    

}
