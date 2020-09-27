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

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
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

}
