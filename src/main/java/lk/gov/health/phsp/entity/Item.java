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

import lk.gov.health.phsp.enums.ItemType;
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
import javax.xml.bind.annotation.XmlRootElement;
import lk.gov.health.phsp.enums.SelectionDataType;

/**
 *
 * @author User
 */
@Entity
@XmlRootElement
public class Item implements Serializable {

    private static final long serialVersionUID = 1L;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Enumerated(EnumType.STRING)
    ItemType itemType;
    String name;
    private String displayName;
    private String code;
    @ManyToOne
    private Item parent;

    @Lob
    private String descreption;
    private SelectionDataType dataType;
    private Double absoluteMinimumDbl;
    private Double absoluteMaximumDbl;
    private Integer absoluteMinimumInt;
    private Integer absoluteMaximumInt;
    private Long absoluteMinimumLong;
    private Long absoluteMaximumLong;
    private Boolean multipleEntiesPerClientStatus;

    private int orderNo;

    @ManyToOne
    private WebUser createdBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdAt;
    @ManyToOne
    private WebUser editedBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date editedAt;
    //Retairing properties
    private boolean retired;
    @ManyToOne
    private WebUser retiredBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date retiredAt;
    private String retireComments;

    @Transient
    private boolean dataTypeReal;
    @Transient
    private boolean dataTypeLong;
    @Transient
    private boolean dataTypeItem;
    @Transient
    private boolean dataTypeShortText;
    @Transient
    private boolean dataTypeLongText;
    @Transient
    private boolean dataTypeInteger;
    @Transient
    private boolean dataTypeDateTime;
    @Transient
    private boolean dataTypeArea;
    @Transient
    private boolean dataTypeClient;
    @Transient
    private boolean dataTypeInstitution;
    @Transient
    private boolean dataTypeBoolean;
    @Transient
    private boolean dataTypeByteArray;
    @Transient
    private boolean dataTypePrescreption;

    public ItemType getItemType() {
        return itemType;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public WebUser getEditedBy() {
        return editedBy;
    }

    public void setEditedBy(WebUser editedBy) {
        this.editedBy = editedBy;
    }

    public Date getEditedAt() {
        return editedAt;
    }

    public void setEditedAt(Date editedAt) {
        this.editedAt = editedAt;
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

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {

        if (!(object instanceof Item)) {
            return false;
        }
        Item other = (Item) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ID = " + id + ", Name =" + name + ".";
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Item getParent() {
        return parent;
    }

    public void setParent(Item parent) {
        this.parent = parent;
    }

    public int getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(int orderNo) {
        this.orderNo = orderNo;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescreption() {
        return descreption;
    }

    public void setDescreption(String descreption) {
        this.descreption = descreption;
    }

    public SelectionDataType getDataType() {
        if(dataType==null){
            dataType = SelectionDataType.Short_Text;
        }
        return dataType;
    }

    public void setDataType(SelectionDataType dataType) {
        this.dataType = dataType;
    }

    public Double getAbsoluteMinimumDbl() {
        return absoluteMinimumDbl;
    }

    public void setAbsoluteMinimumDbl(Double absoluteMinimumDbl) {
        this.absoluteMinimumDbl = absoluteMinimumDbl;
    }

    public Double getAbsoluteMaximumDbl() {
        return absoluteMaximumDbl;
    }

    public void setAbsoluteMaximumDbl(Double absoluteMaximumDbl) {
        this.absoluteMaximumDbl = absoluteMaximumDbl;
    }

    public Integer getAbsoluteMinimumInt() {
        return absoluteMinimumInt;
    }

    public void setAbsoluteMinimumInt(Integer absoluteMinimumInt) {
        this.absoluteMinimumInt = absoluteMinimumInt;
    }

    public Integer getAbsoluteMaximumInt() {
        return absoluteMaximumInt;
    }

    public void setAbsoluteMaximumInt(Integer absoluteMaximumInt) {
        this.absoluteMaximumInt = absoluteMaximumInt;
    }

    public Boolean getMultipleEntiesPerClientStatus() {
        return multipleEntiesPerClientStatus;
    }

    public void setMultipleEntiesPerClientStatus(Boolean multipleEntiesPerClientStatus) {
        this.multipleEntiesPerClientStatus = multipleEntiesPerClientStatus;
    }

    public void classifyDataTypes() {

        dataTypeReal = false;
        dataTypeLong = false;
        dataTypeItem = false;
        dataTypeShortText = false;
        dataTypeLongText = false;
        dataTypeInteger = false;
        dataTypeDateTime = false;
        dataTypeArea = false;
        dataTypeClient = false;
        dataTypeInstitution = false;
        dataTypeBoolean = false;
        dataTypeByteArray = false;
        dataTypePrescreption = false;

        switch (this.dataType) {
            case Area_Reference:
                dataTypeArea = true;
                return;
            case Boolean:
                dataTypeBoolean = true;
                return;
            case Byte_Array:
                dataTypeByteArray = true;
                return;
            case Client_Reference:
                dataTypeClient = true;
                return;
            case DateTime:
                dataTypeDateTime = true;
                return;
            case Integer_Number:
                dataTypeInteger = true;
                return;
            case Item_Reference:
                dataTypeItem = true;
                return;
            case Long_Number:
                dataTypeLong = true;
                return;
            case Long_Text:
                dataTypeLongText = true;
                return;
            case Prescreption_Reference:
                dataTypePrescreption = true;
                return;
            case Real_Number:
                dataTypeReal = true;
                return;
            case Short_Text:
                dataTypeShortText = true;
                return;

        }
    }

    public boolean isDataTypeItem() {
        classifyDataTypes();
        return dataTypeItem;
    }

    public boolean isDataTypeShortText() {
        classifyDataTypes();
        return dataTypeShortText;
    }

    public boolean isDataTypeLongText() {
        classifyDataTypes();
        return dataTypeLongText;
    }

    public boolean isDataTypeDateTime() {
        classifyDataTypes();
        return dataTypeDateTime;
    }

    public boolean isDataTypeArea() {
        classifyDataTypes();
        return dataTypeArea;
    }

    public boolean isDataTypeClient() {
        classifyDataTypes();
        return dataTypeClient;
    }

    public boolean isDataTypeInstitution() {
        classifyDataTypes();
        return dataTypeInstitution;
    }

    public boolean isDataTypeReal() {
        classifyDataTypes();
        return dataTypeReal;
    }

    public boolean isDataTypeLong() {
        classifyDataTypes();
        return dataTypeLong;
    }

    public boolean isDataTypeInteger() {
        classifyDataTypes();
        return dataTypeInteger;
    }

    public boolean isDataTypeBoolean() {
        classifyDataTypes();
        return dataTypeBoolean;
    }

    public boolean isDataTypeByteArray() {
        classifyDataTypes();
        return dataTypeByteArray;
    }

    public boolean isDataTypePrescreption() {
        classifyDataTypes();
        return dataTypePrescreption;
    }

    public Long getAbsoluteMinimumLong() {
        return absoluteMinimumLong;
    }

    public void setAbsoluteMinimumLong(Long absoluteMinimumLong) {
        this.absoluteMinimumLong = absoluteMinimumLong;
    }

    public Long getAbsoluteMaximumLong() {
        return absoluteMaximumLong;
    }

    public void setAbsoluteMaximumLong(Long absoluteMaximumLong) {
        this.absoluteMaximumLong = absoluteMaximumLong;
    }
    
    

}
