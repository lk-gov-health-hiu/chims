/*
 * The MIT License
 *
 * Copyright 2021 buddhika.
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
package lk.gov.health.phsp.bean;

import javax.inject.Named;
import javax.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import lk.gov.health.phsp.entity.ClientEncounterComponentItem;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.enums.EncounterType;
import lk.gov.health.phsp.facade.ClientFacade;
import lk.gov.health.phsp.facade.EncounterFacade;
import lk.gov.health.phsp.pojcs.InstitutionCount;
import lk.gov.health.phsp.pojcs.ObservationValueCount;

/**
 *
 * @author buddhika
 */
@Named
@SessionScoped
public class NationalReportController implements Serializable {

    private Date fromDate;
    private Date toDate;
    private Long count;
    private Item queryItem;
    private Item sex;

    private List<ObservationValueCount> observationValueCounts;

    private Institution institution;

    @Inject
    WebUserController webUserController;

    @EJB
    ClientFacade clientFacade;
    @EJB
    EncounterFacade encounterFacade;

    /**
     * Creates a new instance of HospitalController
     */
    public NationalReportController() {
    }

    public String toNationalReportsCounts() {
        count = null;
        return "/national/counts";
    }

    public String toObservationValueCount() {
        count = null;
        return "/national/observation_values";
    }

    public String toObservationValueCountInt() {
        count = null;
        return "/national/observation_values_int";
    }

    public String toObservationValueCountLong() {
        count = null;
        return "/national/observation_values_long";
    }

    public String toObservationValueCountDbl() {
        count = null;
        return "/national/observation_values_dbl";
    }

    public void fillObservationValues() {
        String j;
        Map m = new HashMap();

        j = "select new lk.gov.health.phsp.pojcs.ObservationValueCount(c.shortTextValue, count(c)) "
                + " from ClientEncounterComponentItem c "
                + " where (c.retired=:ret or c.retired is null) "
                + " and (c.item=:qi)";
        if (sex != null) {
            j += " and c.encounter.client.person.sex.code=:s ";
            m.put("s", sex.getCode());
        }
        j += " and c.createdAt between :fd and :td "
                + " group by c.shortTextValue";
        m.put("ret", false);
        m.put("qi", queryItem);
        m.put("fd", getFromDate());
        m.put("td", getToDate());

        ClientEncounterComponentItem c = new ClientEncounterComponentItem();
        c.getEncounter().getClient().getPerson().getSex().getCode();

        if (institution != null) {
            j += " and c.createInstitution=:ins ";
            m.put("ins", institution);
        }
        observationValueCounts = new ArrayList<>();
        System.out.println("m = " + m);
        System.out.println("j = " + j);
        List<Object> objs = clientFacade.findAggregates(j, m);
        if (objs == null) {
            return;
        }
        for (Object o : objs) {
            if (o instanceof ObservationValueCount) {
                ObservationValueCount ic = (ObservationValueCount) o;
                observationValueCounts.add(ic);
            }
        }

    }

    public void fillObservationValuesInt() {
        String j;
        Map m = new HashMap();

        j = "select new lk.gov.health.phsp.pojcs.ObservationValueCount(c.integerNumberValue, count(c)) "
                + " from ClientEncounterComponentItem c "
                + " where (c.retired=:ret or c.retired is null) "
                + " and (c.item=:qi) "
                + " and c.createdAt between :fd and :td "
                + " group by c.integerNumberValue";
        m.put("ret", false);
        m.put("qi", queryItem);
        m.put("fd", getFromDate());
        m.put("td", getToDate());

        if (institution != null) {
            j += " and c.createInstitution=:ins ";
            m.put("ins", institution);
        }
        observationValueCounts = new ArrayList<>();
        System.out.println("m = " + m);
        System.out.println("j = " + j);
        List<Object> objs = clientFacade.findAggregates(j, m);
        if (objs == null) {
            return;
        }
        for (Object o : objs) {
            if (o instanceof ObservationValueCount) {
                ObservationValueCount ic = (ObservationValueCount) o;
                observationValueCounts.add(ic);
            }
        }

    }

    public void fillObservationValuesLong() {
        String j;
        Map m = new HashMap();

        j = "select new lk.gov.health.phsp.pojcs.ObservationValueCount(c.longNumberValue, count(c)) "
                + " from ClientEncounterComponentItem c "
                + " where (c.retired=:ret or c.retired is null) "
                + " and (c.item=:qi) "
                + " and c.createdAt between :fd and :td "
                + " group by c.longNumberValue";
        m.put("ret", false);
        m.put("qi", queryItem);
        m.put("fd", getFromDate());
        m.put("td", getToDate());

        if (institution != null) {
            j += " and c.createInstitution=:ins ";
            m.put("ins", institution);
        }
        observationValueCounts = new ArrayList<>();
        System.out.println("m = " + m);
        System.out.println("j = " + j);
        List<Object> objs = clientFacade.findAggregates(j, m);
        if (objs == null) {
            return;
        }
        for (Object o : objs) {
            if (o instanceof ObservationValueCount) {
                ObservationValueCount ic = (ObservationValueCount) o;
                observationValueCounts.add(ic);
            }
        }

    }

    public void fillObservationValuesDbl() {
        String j;
        Map m = new HashMap();

        j = "select new lk.gov.health.phsp.pojcs.ObservationValueCount(c.realNumberValue, count(c)) "
                + " from ClientEncounterComponentItem c "
                + " where (c.retired=:ret or c.retired is null) "
                + " and (c.item=:qi) "
                + " and c.createdAt between :fd and :td "
                + " group by c.realNumberValue";
        m.put("ret", false);
        m.put("qi", queryItem);
        m.put("fd", getFromDate());
        m.put("td", getToDate());

        if (institution != null) {
            j += " and c.createInstitution=:ins ";
            m.put("ins", institution);
        }
        observationValueCounts = new ArrayList<>();
        System.out.println("m = " + m);
        System.out.println("j = " + j);
        List<Object> objs = clientFacade.findAggregates(j, m);
        if (objs == null) {
            return;
        }
        for (Object o : objs) {
            if (o instanceof ObservationValueCount) {
                ObservationValueCount ic = (ObservationValueCount) o;
                observationValueCounts.add(ic);
            }
        }

    }

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = CommonController.startOfTheDate();
        }
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = CommonController.endOfTheDate();
        }
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Item getQueryItem() {
        return queryItem;
    }

    public void setQueryItem(Item queryItem) {
        this.queryItem = queryItem;
    }

    public List<ObservationValueCount> getObservationValueCounts() {
        return observationValueCounts;
    }

    public void setObservationValueCounts(List<ObservationValueCount> observationValueCounts) {
        this.observationValueCounts = observationValueCounts;
    }

    public Item getSex() {
        return sex;
    }

    public void setSex(Item sex) {
        this.sex = sex;
    }

}
