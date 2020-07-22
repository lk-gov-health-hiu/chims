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

import java.util.Date;
import java.util.List;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.Client;
import lk.gov.health.phsp.entity.ClientEncounterComponentForm;
import lk.gov.health.phsp.entity.Encounter;
import lk.gov.health.phsp.entity.Relationship;
import lk.gov.health.phsp.enums.QueryFilterAreaType;
import lk.gov.health.phsp.enums.QueryFilterPeriodType;

/**
 *
 * @author Dr M H B Ariyaratne<buddhika.ari@gmail.com>
 */
public class QueryResult {

    private Area area;
    private Jpq jpq;

    private Date tfrom = null;
    private Date tTo = null;
    private Integer tYear = null;
    private Integer tQuater = null;

    private QueryFilterAreaType areaType;
    private QueryFilterPeriodType periodType;

    private String resultString;
    private Long longResult;
    private Double dblResult;
    private List<Relationship> resultRelationshipList;
    private List<Client> resultClientList;
    private List<Encounter> resultEncounterList;
    private List<ClientEncounterComponentForm> resultFormList;

    private String chartNameSeries;
    private String chartDataSeries1;
    private String chartDataSeries2;
    private String chartName;
    private String values1Name;
    private String values2Name;

    private String chartString;

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public Jpq getJpq() {
        return jpq;
    }

    public void setJpq(Jpq jpq) {
        this.jpq = jpq;
    }

    public String getResultString() {
        return resultString;
    }

    public void setResultString(String resultString) {
        this.resultString = resultString;
    }

    public List<Relationship> getResultRelationshipList() {
        return resultRelationshipList;
    }

    public void setResultRelationshipList(List<Relationship> resultRelationshipList) {
        this.resultRelationshipList = resultRelationshipList;
    }

    public List<Client> getResultClientList() {
        return resultClientList;
    }

    public void setResultClientList(List<Client> resultClientList) {
        this.resultClientList = resultClientList;
    }

    public List<Encounter> getResultEncounterList() {
        return resultEncounterList;
    }

    public void setResultEncounterList(List<Encounter> resultEncounterList) {
        this.resultEncounterList = resultEncounterList;
    }

    public List<ClientEncounterComponentForm> getResultFormList() {
        return resultFormList;
    }

    public void setResultFormList(List<ClientEncounterComponentForm> resultFormList) {
        this.resultFormList = resultFormList;
    }

    public Date getTfrom() {
        return tfrom;
    }

    public void setTfrom(Date tfrom) {
        this.tfrom = tfrom;
    }

    public Date gettTo() {
        return tTo;
    }

    public void settTo(Date tTo) {
        this.tTo = tTo;
    }

    public Integer gettYear() {
        return tYear;
    }

    public void settYear(Integer tYear) {
        this.tYear = tYear;
    }

    public Integer gettQuater() {
        return tQuater;
    }

    public void settQuater(Integer tQuater) {
        this.tQuater = tQuater;
    }

    public QueryFilterAreaType getAreaType() {
        return areaType;
    }

    public void setAreaType(QueryFilterAreaType areaType) {
        this.areaType = areaType;
    }

    public QueryFilterPeriodType getPeriodType() {
        return periodType;
    }

    public void setPeriodType(QueryFilterPeriodType periodType) {
        this.periodType = periodType;
    }

    public String getChartNameSeries() {
        return chartNameSeries;
    }

    public void setChartNameSeries(String chartNameSeries) {
        this.chartNameSeries = chartNameSeries;
    }

    public String getChartDataSeries1() {
        return chartDataSeries1;
    }

    public void setChartDataSeries1(String chartDataSeries1) {
        this.chartDataSeries1 = chartDataSeries1;
    }

    public String getChartDataSeries2() {
        return chartDataSeries2;
    }

    public void setChartDataSeries2(String chartDataSeries2) {
        this.chartDataSeries2 = chartDataSeries2;
    }

    public String getChartName() {
        return chartName;
    }

    public void setChartName(String chartName) {
        this.chartName = chartName;
    }

    public String getValues1Name() {
        return values1Name;
    }

    public void setValues1Name(String values1Name) {
        this.values1Name = values1Name;
    }

    public String getValues2Name() {
        return values2Name;
    }

    public void setValues2Name(String values2Name) {
        this.values2Name = values2Name;
    }

    public String getChartString() {
        return chartString;
    }

    public void setChartString(String chartString) {
        this.chartString = chartString;
    }
    
    

    public String convertLongValuesToChartDataSeries(List<QueryResult> cqrs) {
        String s = "";
        int i = 0;
        if (cqrs == null) {
            return "";
        }
        for (QueryResult e : cqrs) {
            i++;
            s += e.getJpq().getLongResult();
            if (i != cqrs.size()) {
                s += ", ";
            }
        }
        return s;
    }

    public Long getLongResult() {
        return longResult;
    }

    public void setLongResult(Long longResult) {
        this.longResult = longResult;
    }

    public Double getDblResult() {
        return dblResult;
    }

    public void setDblResult(Double dblResult) {
        this.dblResult = dblResult;
    }

    
}
