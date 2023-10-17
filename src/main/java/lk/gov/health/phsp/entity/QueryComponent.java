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

import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.xml.bind.annotation.XmlRootElement;
import lk.gov.health.phsp.enums.QueryCriteriaMatchType;
import lk.gov.health.phsp.enums.QueryDataType;

import lk.gov.health.phsp.enums.QueryLevel;
import lk.gov.health.phsp.enums.QueryOutputType;
import lk.gov.health.phsp.enums.QueryType;
import lk.gov.health.phsp.enums.QueryVariableEvaluationType;
import lk.gov.health.phsp.enums.RelationshipType;

/**
 *
 * @author buddhika
 */
@Entity
@Table
@XmlRootElement
public class QueryComponent extends Component {

    @Lob
    private String indicatorQuery;
    @Lob
    private String selectQuery;
    @Lob
    private String fromQuery;
    @Lob
    private String whereQuery;
    @Lob
    private String groupQuery;
    @Lob
    private String havingQuery;
    @Lob
    private String filterQuery;
    @Lob
    private String orderQuery;

    @Enumerated(EnumType.STRING)
    private QueryType queryType;

    @Enumerated(EnumType.STRING)
    private QueryOutputType outputType;

    @Enumerated(EnumType.STRING)
    private QueryCriteriaMatchType matchType;

    @Enumerated(EnumType.STRING)
    private QueryVariableEvaluationType evaluationType;

    @Enumerated(EnumType.STRING)
    private RelationshipType populationType;

    @Enumerated(EnumType.STRING)
    private QueryLevel queryLevel;

    @Enumerated(EnumType.STRING)
    private QueryDataType queryDataType;

    private boolean filterByDistrict;
    private boolean filterByProvince;
    private boolean filterByRdhs;
    private boolean filterByMoh;
    private boolean filterByFrom;
    private boolean filterByDate;
    private boolean filterByTo;
    private boolean filterByInstitution;
    private boolean filterByGn;
    private boolean filterByYear;
    private boolean filterByMonth;
    private boolean filterByQuarter;
    
    private boolean required;


//    @ManyToOne(fetch = FetchType.LAZY)
//    private Client client;

    @Lob
    private String longTextValue;
    @Lob
    private String descreptionValue;
    private String shortTextValue;
    private byte[] byteArrayValue;
    private Integer integerNumberValue;
    private Long longNumberValue;
    private Double realNumberValue;
    private Boolean booleanValue;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date dateValue;
    @ManyToOne(fetch = FetchType.LAZY)
    private Item itemValue;
    @ManyToOne(fetch = FetchType.LAZY)
    private Area areaValue;
    @ManyToOne(fetch = FetchType.LAZY)
    private Institution institutionValue;
    @ManyToOne(fetch = FetchType.LAZY)
    private Client clientValue;
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Prescription prescriptionValue;
    
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Observation observationValue;
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Procedure procedureValue;
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Movement movementValue;
    
//
//    private boolean completed;
//    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
//    private Date completedAt;
//    @ManyToOne(fetch = FetchType.LAZY)
//    private WebUser completedBy;

    private Integer integerNumberValue2;
    private Long longNumberValue2;
    private Double realNumberValue2;

    public String getIndicatorQuery() {
        return indicatorQuery;
    }

    public void setIndicatorQuery(String indicatorQuery) {
        this.indicatorQuery = indicatorQuery;
    }

    public String getSelectQuery() {
        return selectQuery;
    }

    public void setSelectQuery(String selectQuery) {
        this.selectQuery = selectQuery;
    }

    public String getFromQuery() {
        return fromQuery;
    }

    public void setFromQuery(String fromQuery) {
        this.fromQuery = fromQuery;
    }

    public String getWhereQuery() {
        return whereQuery;
    }

    public void setWhereQuery(String whereQuery) {
        this.whereQuery = whereQuery;
    }

    public String getGroupQuery() {
        return groupQuery;
    }

    public void setGroupQuery(String groupQuery) {
        this.groupQuery = groupQuery;
    }

    public String getHavingQuery() {
        return havingQuery;
    }

    public void setHavingQuery(String havingQuery) {
        this.havingQuery = havingQuery;
    }

    public String getFilterQuery() {
        return filterQuery;
    }

    public String getOrderQuery() {
        return orderQuery;
    }

    public void setOrderQuery(String orderQuery) {
        this.orderQuery = orderQuery;
    }

    public void setFilterQuery(String filterQuery) {
        this.filterQuery = filterQuery;
    }

    public QueryType getQueryType() {
        return queryType;
    }

    public void setQueryType(QueryType queryType) {
        this.queryType = queryType;
    }

    public QueryCriteriaMatchType getMatchType() {
        return matchType;
    }

    public void setMatchType(QueryCriteriaMatchType matchType) {
        this.matchType = matchType;
    }

    public QueryVariableEvaluationType getEvaluationType() {
        return evaluationType;
    }

    public void setEvaluationType(QueryVariableEvaluationType evaluationType) {
        this.evaluationType = evaluationType;
    }

    public RelationshipType getPopulationType() {
        return populationType;
    }

    public void setPopulationType(RelationshipType populationType) {
        this.populationType = populationType;
    }

    public boolean isFilterByDistrict() {
        return filterByDistrict;
    }

    public void setFilterByDistrict(boolean filterByDistrict) {
        this.filterByDistrict = filterByDistrict;
    }

    public boolean isFilterByProvince() {
        return filterByProvince;
    }

    public void setFilterByProvince(boolean filterByProvince) {
        this.filterByProvince = filterByProvince;
    }

    public boolean isFilterByFrom() {
        return filterByFrom;
    }

    public void setFilterByFrom(boolean filterByFrom) {
        this.filterByFrom = filterByFrom;
    }

    public boolean isFilterByTo() {
        return filterByTo;
    }

    public void setFilterByTo(boolean filterByTo) {
        this.filterByTo = filterByTo;
    }

    public boolean isFilterByInstitution() {
        return filterByInstitution;
    }

    public void setFilterByInstitution(boolean filterByInstitution) {
        this.filterByInstitution = filterByInstitution;
    }

    public boolean isFilterByGn() {
        return filterByGn;
    }

    public void setFilterByGn(boolean filterByGn) {
        this.filterByGn = filterByGn;
    }

    public boolean isFilterByRdhs() {
        return filterByRdhs;
    }

    public void setFilterByRdhs(boolean filterByRdhs) {
        this.filterByRdhs = filterByRdhs;
    }

    public boolean isFilterByMoh() {
        return filterByMoh;
    }

    public void setFilterByMoh(boolean filterByMoh) {
        this.filterByMoh = filterByMoh;
    }

    public boolean isFilterByYear() {
        return filterByYear;
    }

    public void setFilterByYear(boolean filterByYear) {
        this.filterByYear = filterByYear;
    }

    public boolean isFilterByMonth() {
        return filterByMonth;
    }

    public void setFilterByMonth(boolean filterByMonth) {
        this.filterByMonth = filterByMonth;
    }

    public boolean isFilterByQuarter() {
        return filterByQuarter;
    }

    public void setFilterByQuarter(boolean filterByQuarter) {
        this.filterByQuarter = filterByQuarter;
    }

    public boolean isFilterByDate() {
        return filterByDate;
    }

    public void setFilterByDate(boolean filterByDate) {
        this.filterByDate = filterByDate;
    }

    public QueryOutputType getOutputType() {
        return outputType;
    }

    public void setOutputType(QueryOutputType outputType) {
        this.outputType = outputType;
    }

    public QueryLevel getQueryLevel() {
        return queryLevel;
    }

    public void setQueryLevel(QueryLevel queryLevel) {
        this.queryLevel = queryLevel;
    }

    public QueryDataType getQueryDataType() {
        return queryDataType;
    }

    public void setQueryDataType(QueryDataType queryDataType) {
        this.queryDataType = queryDataType;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    
//    public Client getClient() {
//        return client;
//    }
//
//    public void setClient(Client client) {
//        this.client = client;
//    }

    public String getLongTextValue() {
        return longTextValue;
    }

    public void setLongTextValue(String longTextValue) {
        this.longTextValue = longTextValue;
    }

    public String getDescreptionValue() {
        return descreptionValue;
    }

    public void setDescreptionValue(String descreptionValue) {
        this.descreptionValue = descreptionValue;
    }

    public String getShortTextValue() {
        return shortTextValue;
    }

    public void setShortTextValue(String shortTextValue) {
        this.shortTextValue = shortTextValue;
    }

    public byte[] getByteArrayValue() {
        return byteArrayValue;
    }

    public void setByteArrayValue(byte[] byteArrayValue) {
        this.byteArrayValue = byteArrayValue;
    }

    public Integer getIntegerNumberValue() {
        return integerNumberValue;
    }

    public void setIntegerNumberValue(Integer integerNumberValue) {
        this.integerNumberValue = integerNumberValue;
    }

    public Long getLongNumberValue() {
        return longNumberValue;
    }

    public void setLongNumberValue(Long longNumberValue) {
        this.longNumberValue = longNumberValue;
    }

    public Double getRealNumberValue() {
        return realNumberValue;
    }

    public void setRealNumberValue(Double realNumberValue) {
        this.realNumberValue = realNumberValue;
    }

    public Boolean getBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(Boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    public Date getDateValue() {
        return dateValue;
    }

    public void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }

    public Item getItemValue() {
        return itemValue;
    }

    public void setItemValue(Item itemValue) {
        this.itemValue = itemValue;
    }

    public Area getAreaValue() {
        return areaValue;
    }

    public void setAreaValue(Area areaValue) {
        this.areaValue = areaValue;
    }

    public Institution getInstitutionValue() {
        return institutionValue;
    }

    public void setInstitutionValue(Institution institutionValue) {
        this.institutionValue = institutionValue;
    }

    public Client getClientValue() {
        return clientValue;
    }

    public void setClientValue(Client clientValue) {
        this.clientValue = clientValue;
    }

    public Prescription getPrescriptionValue() {
        return prescriptionValue;
    }

    public void setPrescriptionValue(Prescription prescriptionValue) {
        this.prescriptionValue = prescriptionValue;
    }

    public Observation getObservationValue() {
        return observationValue;
    }

    public void setObservationValue(Observation observationValue) {
        this.observationValue = observationValue;
    }

    public Procedure getProcedureValue() {
        return procedureValue;
    }

    public void setProcedureValue(Procedure procedureValue) {
        this.procedureValue = procedureValue;
    }

    public Movement getMovementValue() {
        return movementValue;
    }

    public void setMovementValue(Movement movementValue) {
        this.movementValue = movementValue;
    }

//    public boolean isCompleted() {
//        return completed;
//    }
//
//    public void setCompleted(boolean completed) {
//        this.completed = completed;
//    }
//
//    public Date getCompletedAt() {
//        return completedAt;
//    }
//
//    public void setCompletedAt(Date completedAt) {
//        this.completedAt = completedAt;
//    }
//
//    public WebUser getCompletedBy() {
//        return completedBy;
//    }
//
//    public void setCompletedBy(WebUser completedBy) {
//        this.completedBy = completedBy;
//    }

    public Integer getIntegerNumberValue2() {
        return integerNumberValue2;
    }

    public void setIntegerNumberValue2(Integer integerNumberValue2) {
        this.integerNumberValue2 = integerNumberValue2;
    }

    public Long getLongNumberValue2() {
        return longNumberValue2;
    }

    public void setLongNumberValue2(Long longNumberValue2) {
        this.longNumberValue2 = longNumberValue2;
    }

    public Double getRealNumberValue2() {
        return realNumberValue2;
    }

    public void setRealNumberValue2(Double realNumberValue2) {
        this.realNumberValue2 = realNumberValue2;
    }

    
    
}
