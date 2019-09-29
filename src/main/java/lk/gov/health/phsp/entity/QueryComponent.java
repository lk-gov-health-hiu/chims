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
import javax.persistence.Lob;

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

    public void setFilterQuery(String filterQuery) {
        this.filterQuery = filterQuery;
    }

    public String getOrderQuery() {
        return orderQuery;
    }

    public void setOrderQuery(String orderQuery) {
        this.orderQuery = orderQuery;
    }

}
