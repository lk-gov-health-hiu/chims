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
import javax.jdo.annotations.Index;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;

import lk.gov.health.phsp.pojcs.Identifiable;

/**
 *
 * @author buddhika
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Component implements Serializable, Identifiable {

    private static final long serialVersionUID = 1L;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Index
    String name;

    @Index
    private String code;

    @Index
    @ManyToOne(fetch = FetchType.EAGER)
    private Item item;

    @Lob
    private String descreption;

    @Index
    private Double orderNo;

    @Index
    @ManyToOne(fetch = FetchType.EAGER)
    private Institution institution;

    @Index
    @ManyToOne(fetch = FetchType.EAGER)
    private Component parentComponent;

    @Index
    @ManyToOne(fetch = FetchType.EAGER)
    private Component referenceComponent;

    @Lob
    private String css;

    @Transient
    private String generatedCss;
    @Transient
    private DesignComponentFormSet referanceDesignComponentFormSet;
    @Transient
    private DesignComponentForm referanceDesignComponentForm;
    @Transient
    private DesignComponentFormItem referanceDesignComponentFormItem;
    @Transient
    private DesignComponentFormSet parentDesignComponentFormSet;
    @Transient
    private DesignComponentForm parentDesignComponentForm;
    @Transient
    private DesignComponentFormItem parentDesignComponentFormItem;

    @Transient
    private ClientEncounterComponentFormSet referanceClientEncounterComponentFormSet;
    @Transient
    private ClientEncounterComponentForm referanceClientEncounterComponentForm;
    @Transient
    private ClientEncounterComponentItem referanceClientEncounterComponentItem;
    @Transient
    private ClientEncounterComponentFormSet parentClientEncounterComponentFormSet;
    @Transient
    private ClientEncounterComponentForm parentClientEncounterComponentForm;
    @Transient
    private ClientEncounterComponentForm parentClientEncounterComponentItem;


    /*
    Create Properties
     */
    @ManyToOne(fetch = FetchType.EAGER)
    private WebUser createdBy;
    @Index
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdAt;
    /*
    Last Edit Properties
     */
//    @ManyToOne(fetch = FetchType.EAGER)
//    private WebUser lastEditBy;
//    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
//    private Date lastEditeAt;
    /*
    Retire Reversal Properties
     */
//    @ManyToOne(fetch = FetchType.EAGER)
//    private WebUser retiredReversedBy;
//    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
//    private Date retiredReversedAt;
    /*
    Retire Properties
     */
    @Index
    private boolean retired;
//    @ManyToOne(fetch = FetchType.EAGER)
//    private WebUser retiredBy;
//    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
//    private Date retiredAt;
//    private String retireComments;
    @Index
    private boolean completed;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    @Index
    private Date completedAt;
    @ManyToOne(fetch = FetchType.EAGER)
    private WebUser completedBy;

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
        return name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Component getParentComponent() {
        return parentComponent;
    }

    public void setParentComponent(Component parentComponent) {
        this.parentComponent = parentComponent;
    }

//    public SelectionDataType getSelectionDataType() {
//        if (selectionDataType == null) {
//            if (item != null) {
//                selectionDataType = item.getDataType();
//            }else{
//                selectionDataType = SelectionDataType.Short_Text;
//            }
//        }
//        return selectionDataType;
//    }
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

//    public WebUser getLastEditBy() {
//        return lastEditBy;
//    }
//
//    public void setLastEditBy(WebUser lastEditBy) {
//        this.lastEditBy = lastEditBy;
//    }
//
//    public Date getLastEditeAt() {
//        return lastEditeAt;
//    }
//
//    public void setLastEditeAt(Date lastEditeAt) {
//        this.lastEditeAt = lastEditeAt;
//    }
//    public WebUser getRetiredReversedBy() {
//        return retiredReversedBy;
//    }
//
//    public void setRetiredReversedBy(WebUser retiredReversedBy) {
//        this.retiredReversedBy = retiredReversedBy;
//    }
//
//    public Date getRetiredReversedAt() {
//        return retiredReversedAt;
//    }
//
//    public void setRetiredReversedAt(Date retiredReversedAt) {
//        this.retiredReversedAt = retiredReversedAt;
//    }
    public boolean isRetired() {
        return retired;
    }

    public void setRetired(boolean retired) {
        this.retired = retired;
    }

//    public WebUser getRetiredBy() {
//        return retiredBy;
//    }
//
//    public void setRetiredBy(WebUser retiredBy) {
//        this.retiredBy = retiredBy;
//    }
//    public Date getRetiredAt() {
//        return retiredAt;
//    }
//
//    public void setRetiredAt(Date retiredAt) {
//        this.retiredAt = retiredAt;
//    }
//
//    public String getRetireComments() {
//        return retireComments;
//    }
//
//    public void setRetireComments(String retireComments) {
//        this.retireComments = retireComments;
//    }
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

    public Component getReferenceComponent() {
        return referenceComponent;
    }

    public void setReferenceComponent(Component referenceComponent) {
        this.referenceComponent = referenceComponent;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public String getDescreption() {
        return descreption;
    }

    public void setDescreption(String descreption) {
        this.descreption = descreption;
    }

    public String getGeneratedCss() {
        generatedCss = css;
        return generatedCss;
    }

//    public String getGeneratedCss() {
//        generatedCss = "";
//        if (css != null) {
//            generatedCss += css;
//        }
//        if (heightPercent != null) {
//            generatedCss += "; height:" + heightPercent + "%; ";
//        }
//        if (leftPercent != null) {
//            generatedCss += "; left:" + leftPercent + "%; ";
//        }
//        if (widthPercent != null) {
//            generatedCss += "; width:" + widthPercent + "%; ";
//        }
//        if (heightPercent != null) {
//            generatedCss += "; height:" + heightPercent + "%; ";
//        }
//        if (backgroundColour != null && !backgroundColour.equals("")) {
//            generatedCss += "; background-color:#" + backgroundColour + "; ";
//        }
//        if (foregroundColour != null && !foregroundColour.equals("")) {
//            generatedCss += "; color:#" + foregroundColour + "; ";
//        }
//        if (borderColour != null && !borderColour.equals("")) {
//            generatedCss += "; border-color: #" + foregroundColour + "; ";
//        }
//        return generatedCss;
//    }
    public void setGeneratedCss(String generatedCss) {
        this.generatedCss = generatedCss;
    }

    public String getCss() {
        return css;
    }

    public void setCss(String css) {
        this.css = css;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }

    public WebUser getCompletedBy() {
        return completedBy;
    }

    public void setCompletedBy(WebUser completedBy) {
        this.completedBy = completedBy;
    }

    public DesignComponentFormSet getReferanceDesignComponentFormSet() {
        if (referenceComponent instanceof DesignComponentFormSet) {
            referanceDesignComponentFormSet = (DesignComponentFormSet) referenceComponent;
        }
        return referanceDesignComponentFormSet;
    }

    public DesignComponentForm getReferanceDesignComponentForm() {
        if (referenceComponent instanceof DesignComponentForm) {
            referanceDesignComponentForm = (DesignComponentForm) referenceComponent;
        }
        return referanceDesignComponentForm;
    }

    public DesignComponentFormItem getReferanceDesignComponentFormItem() {
        if (referenceComponent instanceof DesignComponentFormItem) {
            referanceDesignComponentFormItem = (DesignComponentFormItem) referenceComponent;
        }
        return referanceDesignComponentFormItem;
    }

    public DesignComponentFormSet getParentDesignComponentFormSet() {
        if (parentComponent instanceof DesignComponentFormSet) {
            parentDesignComponentFormSet = (DesignComponentFormSet) parentComponent;
        }
        return parentDesignComponentFormSet;
    }

    public DesignComponentForm getParentDesignComponentForm() {
        if (parentComponent instanceof DesignComponentForm) {
            parentDesignComponentForm = (DesignComponentForm) parentComponent;
        }
        return parentDesignComponentForm;
    }

    public DesignComponentFormItem getParentDesignComponentFormItem() {
        if (parentComponent instanceof DesignComponentFormItem) {
            parentDesignComponentFormItem = (DesignComponentFormItem) parentComponent;
        }
        return parentDesignComponentFormItem;
    }

    public ClientEncounterComponentFormSet getReferanceClientEncounterComponentFormSet() {
        if (referenceComponent instanceof ClientEncounterComponentFormSet) {
            referanceClientEncounterComponentFormSet = (ClientEncounterComponentFormSet) referenceComponent;
        }
        return referanceClientEncounterComponentFormSet;
    }

    public ClientEncounterComponentForm getReferanceClientEncounterComponentForm() {
        if (referenceComponent instanceof ClientEncounterComponentForm) {
            referanceClientEncounterComponentForm = (ClientEncounterComponentForm) referenceComponent;
        }
        return referanceClientEncounterComponentForm;
    }

    public ClientEncounterComponentItem getReferanceClientEncounterComponentItem() {
        if (referenceComponent instanceof ClientEncounterComponentItem) {
            referanceClientEncounterComponentItem = (ClientEncounterComponentItem) referenceComponent;
        }
        return referanceClientEncounterComponentItem;
    }

    public ClientEncounterComponentFormSet getParentClientEncounterComponentFormSet() {
        if (parentComponent instanceof ClientEncounterComponentFormSet) {
            parentClientEncounterComponentFormSet = (ClientEncounterComponentFormSet) parentComponent;
        }
        return parentClientEncounterComponentFormSet;
    }

    public ClientEncounterComponentForm getParentClientEncounterComponentForm() {
        if (parentComponent instanceof ClientEncounterComponentForm) {
            parentClientEncounterComponentForm = (ClientEncounterComponentForm) parentComponent;
        }
        return parentClientEncounterComponentForm;
    }

    public ClientEncounterComponentForm getParentClientEncounterComponentItem() {
        if (parentComponent instanceof ClientEncounterComponentForm) {
            parentClientEncounterComponentItem = (ClientEncounterComponentForm) parentComponent;
        }
        return parentClientEncounterComponentItem;
    }

}
