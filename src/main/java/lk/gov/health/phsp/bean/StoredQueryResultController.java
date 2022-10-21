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
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.QueryComponent;
import lk.gov.health.phsp.entity.StoredQueryResult;
import lk.gov.health.phsp.enums.RelationshipType;
import lk.gov.health.phsp.facade.QueryComponentFacade;
import lk.gov.health.phsp.facade.StoredQueryResultFacade;
import lk.gov.health.phsp.pojcs.InstitutionDataQuery;
import lk.gov.health.phsp.pojcs.Replaceable;

/**
 *
 * @author buddhika
 */
@Named
@ApplicationScoped
public class StoredQueryResultController implements Serializable {

    @Inject
    WebUserController webUserController;
    @Inject
    RelationshipController relationshipController;

    @EJB
    StoredQueryResultFacade facade;
    @EJB
    QueryComponentFacade queryComponentFacade;

    /**
     * Creates a new instance of StoredQueryResultController
     */
    public StoredQueryResultController() {
    }

    public Long findStoredLongValue(QueryComponent qc, Date fromDate, Date toDate, Institution institution) {
        StoredQueryResult s;
        s = findStoredQueryResult(qc, fromDate, toDate, institution);
        if (s == null) {
            return null;
        }
        return s.getLongValue();
    }

    public Long findStoredLongValue(QueryComponent qc, Date fromDate, Date toDate, List<Institution> institutions) {
        // //System.out.println("findStoredLongValue");
        // //System.out.println("fromDate = " + CommonController.dateTimeToString(fromDate));
        // //System.out.println("toDate = " + CommonController.dateTimeToString(toDate));
        Long c = 0l;
        Map<Long, Institution> mis = new HashMap<>();
        for (Institution institution : institutions) {
            mis.put(institution.getId(), institution);
        }
        for (Institution institution : mis.values()) {
            StoredQueryResult s;
            s = findStoredQueryResult(qc, fromDate, toDate, institution);
            // //System.out.println("institution.getName() = " + institution.getName());
            if (s != null && s.getLongValue() != null) {
                // //System.out.println("s.getLongValue() = " + s.getLongValue());
                c += s.getLongValue();
            }
        }
        // //System.out.println("c = " + c);
        return c;
    }

    public Long findStoredLongValue(QueryComponent qc, Date fromDate, Date toDate, Institution institution, Replaceable re) {
        //System.out.println("findStoredLongValue");
        StoredQueryResult s;
        s = findStoredQueryResult(qc, fromDate, toDate, institution);
        Long r;
        if (s == null) {
            r = null;
        } else {
            if (s.getLongValue() == null) {
                r = null;
            } else {
                r = s.getLongValue();
            }
        }
        return r;
    }

    public Long findStoredLongValue(QueryComponent qc, Date fromDate, Date toDate, List<Institution> institutions, Replaceable re) {
        //System.out.println("findStoredLongValue");
        //System.out.println("qc = " + qc);
        //System.out.println("fromDate = " + fromDate);
        //System.out.println("toDate = " + toDate);
        Long insSum = 0L;
        for (Institution i : institutions) {
            //System.out.println("i = " + i);
            Long ic = findStoredLongValue(qc, fromDate, toDate, i, re);
            //System.out.println("qc = " + qc.getName() + ", Ins = " + i.getName() + ", count = " + ic);
            if (ic != null) {
                insSum += ic;
            }
        }
        return insSum;
    }

    public List<InstitutionDataQuery> findStoredQueryData(QueryComponent qc, Date fromDate, Date toDate, List<Institution> institutions, Replaceable re, Integer year, Integer month) {
        //System.out.println("findStoredQueryData");
        List<InstitutionDataQuery> qds = new ArrayList<>();
        Long insSum = 0L;
        for (Institution i : institutions) {
            InstitutionDataQuery q = new InstitutionDataQuery();
            q.setInstitution(i);
            q.setQuery(qc);
            q.setYear(year);
            q.setMonth(month);
            Long ic = findStoredLongValue(qc, fromDate, toDate, i, re);
            q.setValue(ic);
//            // //System.out.println("qc = " + qc.getName() + ", Ins = " + i.getName() + ", count = " + ic);
            if (ic != null) {
                insSum += ic;
            }
            qds.add(q);
        }
        return qds;
    }

    public List<InstitutionDataQuery> findPopulationData(QueryComponent qc, List<Institution> institutions, Integer year) {
        RelationshipType pt = qc.getPopulationType();
        List<InstitutionDataQuery> qds = new ArrayList<>();
        for (Institution i : institutions) {
            InstitutionDataQuery q = new InstitutionDataQuery();
            q.setInstitution(i);
            q.setYear(year);
            Long ic = relationshipController.findPopulationValue(year, i, pt);
            q.setValue(ic);
            q.setQuery(qc);
            qds.add(q);
        }
        return qds;
    }

    public StoredQueryResult findStoredQueryResult(QueryComponent qc, Date fromDate, Date toDate, Institution institution) {
        //System.out.println("findStoredQueryResult");
        String j;
        Map m;
        m = new HashMap();
        m.put("fd", fromDate);
        m.put("td", toDate);
        m.put("ins", institution);
        m.put("qc", qc);
        j = "select s "
                + " from StoredQueryResult s "
                + " where s.institution=:ins "
                + " and s.resultFrom=:fd "
                + " and s.resultTo=:td "
                + " and s.queryComponent=:qc "
                + " order by s.id desc";
        return facade.findFirstByJpql(j, m);
    }

    public StoredQueryResult findStoredQueryResult(QueryComponent qc, Date fromDate, Date toDate, Area area) {
        String j;
        Map m;
        m = new HashMap();
        m.put("fd", fromDate);
        m.put("td", toDate);
        m.put("area", area);
        m.put("qc", qc);
        j = "select s "
                + " from StoredQueryResult s "
                + " where s.area=:area "
                + " and s.resultFrom=:fd "
                + " and s.resultTo=:td "
                + " and s.queryComponent=:qc "
                + " order by s.id desc";
        return facade.findFirstByJpql(j, m);

    }

    public void saveValue(QueryComponent qc, Date fromDate, Date toDate, Institution institution, Long value) {
        StoredQueryResult s;
        //System.out.println("SaveValue for Stored Query Result");
        //System.out.println("qc.getName() = " + qc.getName());
        //System.out.println("fromDate = " + fromDate);
        //System.out.println("toDate = " + toDate);
        //System.out.println("institution = " + institution);
        //System.out.println("value = " + value);
        s = findStoredQueryResult(qc, fromDate, toDate, institution);
        //System.out.println("s = " + s);
        if (s == null) {
            s = new StoredQueryResult();
            s.setCreatedAt(new Date());
            s.setCreater(webUserController.getLoggedUser());
            s.setInstitution(institution);
            s.setResultFrom(fromDate);
            s.setResultTo(toDate);
            s.setQueryComponent(qc);
            s.setLongValue(value);
            facade.create(s);
            //System.out.println("saved as new");
            //System.out.println("s = " + s);
            //System.out.println("s.getLongValue() = " + s.getLongValue());
            facade.edit(s);
        } else {
            //System.out.println("s = " + s);
            //System.out.println("updating old");
            s.setLongValue(value);
            facade.edit(s);
        }

    }

    public void saveValue(QueryComponent qc, Date fromDate, Date toDate, Institution institution, Long value, Double blVal) {
        StoredQueryResult s;
        //System.out.println("SaveValue for Stored Query Result");
        //System.out.println("qc.getName() = " + qc.getName());
        //System.out.println("fromDate = " + fromDate);
        //System.out.println("toDate = " + toDate);
        //System.out.println("institution = " + institution);
        //System.out.println("value = " + value);
        s = findStoredQueryResult(qc, fromDate, toDate, institution);
        //System.out.println("s = " + s);
        if (s == null) {
            s = new StoredQueryResult();
            s.setCreatedAt(new Date());
            s.setCreater(webUserController.getLoggedUser());
            s.setInstitution(institution);
            s.setResultFrom(fromDate);
            s.setResultTo(toDate);
            s.setQueryComponent(qc);
            s.setLongValue(value);
            s.setDoubleValue(blVal);
            facade.create(s);
            //System.out.println("saved as new");
            //System.out.println("s = " + s);
            //System.out.println("s.getLongValue() = " + s.getLongValue());
            facade.edit(s);
        } else {
            //System.out.println("s = " + s);
            //System.out.println("updating old");
            s.setLongValue(value);
            facade.edit(s);
        }

    }

    public void saveValue(QueryComponent qc, Date fromDate, Date toDate, Area area, Long value) {
        StoredQueryResult s;
        s = findStoredQueryResult(qc, fromDate, toDate, area);
        if (s == null) {
            s = new StoredQueryResult();
            s.setArea(area);
            s.setResultFrom(fromDate);
            s.setResultTo(toDate);
            s.setQueryComponent(qc);
            s.setLongValue(value);
            facade.create(s);
        } else {
            s.setLongValue(value);
            facade.edit(s);
        }

    }

}
