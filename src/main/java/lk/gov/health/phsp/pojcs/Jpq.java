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
package lk.gov.health.phsp.pojcs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lk.gov.health.phsp.entity.Client;
import lk.gov.health.phsp.entity.ClientEncounterComponentForm;
import lk.gov.health.phsp.entity.Encounter;
import lk.gov.health.phsp.entity.QueryComponent;
import lk.gov.health.phsp.entity.Relationship;
import lk.gov.health.phsp.enums.QueryFilterAreaType;
import lk.gov.health.phsp.enums.QueryFilterPeriodType;

/**
 *
 * @author Dr M H B Ariyaratne<buddhika.ari@gmail.com>
 */
public class Jpq {

    private String jfrom = "";
    private String jselect = "";
    private String jwhere = "";
    private String jgroupby = "";
    private String jhaving = "";
    private String jorderBy = "";
    private String filter = "";
    private String jpql = "";
    private Map m;

    private Long longResult;
    private Double dblResult;
    private QueryComponent qc;
    private List<Client> clientList;
    private List<Encounter> encounterList;
    private List<ClientEncounterComponentForm> formList;
    private List<Relationship> relationshipList;

    private QueryFilterAreaType areType;
    private QueryFilterPeriodType periodType;

    /**
     *
     * SELECT c.currency, SUM(c.population) FROM Country c WHERE 'Europe' MEMBER
     * OF c.continents GROUP BY c.currency HAVING COUNT(c) > 1 ORDER BY
     * c.currency
     */
    /**
     *
     * @return
     */
    public String getJfrom() {
        return jfrom;
    }

    public void setJfrom(String jfrom) {
        this.jfrom = jfrom;
    }

    public String getJselect() {
        return jselect;
    }

    public void setJselect(String jselect) {
        this.jselect = jselect;
    }

    public String getJwhere() {
        return jwhere;
    }

    public void setJwhere(String jwhere) {
        this.jwhere = jwhere;
    }

    public String getJgroupby() {
        return jgroupby;
    }

    public void setJgroupby(String jgroupby) {
        this.jgroupby = jgroupby;
    }

    public String getJhaving() {
        return jhaving;
    }

    public void setJhaving(String jhaving) {
        this.jhaving = jhaving;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getJpql() {
        jpql = jselect + " " + jfrom + " " + jwhere + " " + jgroupby + " " + jhaving + " " + jorderBy;
        return jpql;
    }

    public void setJpql(String jpql) {
        this.jpql = jpql;
    }

    public Map getM() {
        if (m == null) {
            m = new HashMap<>();
        }
        return m;
    }

    public void setM(Map m) {
        this.m = m;
    }

    public Long getLongResult() {
        return longResult;
    }

    public void setLongResult(Long longResult) {
        this.longResult = longResult;
    }

    public String getJorderBy() {
        return jorderBy;
    }

    public void setJorderBy(String jorderBy) {
        this.jorderBy = jorderBy;
    }

    public QueryComponent getQc() {
        return qc;
    }

    public void setQc(QueryComponent qc) {
        this.qc = qc;
    }

    @Override
    public String toString() {
        return "Jpq{" + "jpql=" + getJpql() + ", m=" + getM() + '}';
    }

    public List<Client> getClientList() {
        return clientList;
    }

    public void setClientList(List<Client> clientList) {
        this.clientList = clientList;
    }

    public List<Encounter> getEncounterList() {
        return encounterList;
    }

    public void setEncounterList(List<Encounter> encounterList) {
        this.encounterList = encounterList;
    }

    public List<ClientEncounterComponentForm> getFormList() {
        return formList;
    }

    public void setFormList(List<ClientEncounterComponentForm> formList) {
        this.formList = formList;
    }

    public List<Relationship> getRelationshipList() {
        return relationshipList;
    }

    public void setRelationshipList(List<Relationship> relationshipList) {
        this.relationshipList = relationshipList;
    }

    public Double getDblResult() {
        return dblResult;
    }

    public void setDblResult(Double dblResult) {
        this.dblResult = dblResult;
    }

    public QueryFilterAreaType getAreType() {
        return areType;
    }

    public void setAreType(QueryFilterAreaType areType) {
        this.areType = areType;
    }

    public QueryFilterPeriodType getPeriodType() {
        return periodType;
    }

    public void setPeriodType(QueryFilterPeriodType periodType) {
        this.periodType = periodType;
    }

    
}
