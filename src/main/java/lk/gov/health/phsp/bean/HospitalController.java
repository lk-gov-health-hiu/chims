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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Inject;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.enums.EncounterType;
import lk.gov.health.phsp.facade.ClientFacade;
import lk.gov.health.phsp.facade.EncounterFacade;

/**
 *
 * @author buddhika
 */
@Named(value = "hospitalController")
@ViewScoped
public class HospitalController implements Serializable {

    private Date fromDate;
    private Date toDate;
    private Long count;

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
    public HospitalController() {
    }

    public String toHospitalReportsCounts() {
        count = null;
        return "/reports/hospital/counts";
    }

    public String toHospitalReportsLists() {
        count = null;
        return "/reports/hospital/lists";
    }

    public String toHospitalRegistrationCount() {
        count = null;
        return "/reports/hospital/registrations";
    }

    public String toClinicVisitCount() {
        count = null;
        return "/reports/hospital/clinic_visits";
    }

    public void calculateClientRegistrations() {
        String j;
        Map m = new HashMap();

        j = "select count(c) "
                + " from Client c "
                + " where (c.retired=:ret or c.retired is null) "
                + " and (c.reservedClient=:res or c.reservedClient is null) "
                + " and c.createdAt between :fd and :td ";
        m.put("ret", false);
        m.put("res", false);
        m.put("fd", getFromDate());
        m.put("td", getToDate());

        if (institution != null) {
            j += " and c.createInstitution=:ins ";
            m.put("ins", institution);
        } else {
            if (webUserController.getLoggedUser().isRestrictedToInstitution()) {
                j += " and c.createInstitution in :inss ";
                List<Institution> ins = webUserController.getLoggableInstitutions();
                m.put("inss", ins);
            }
        }

        count = clientFacade.findLongByJpql(j, m);

    }

    public void calculateClinicVisits() {
        String j;
        Map m = new HashMap();

        j = "select count(e) "
                + " from Encounter e "
                + " where e.retired=:ret "
                + " and e.encounterType=:type "
                + " and e.encounterDate between :fd and :td ";

        m.put("ret", false);
        m.put("fd", fromDate);
        m.put("td", toDate);
        m.put("type", EncounterType.Clinic_Visit);

        if (institution != null) {
            j += " and e.institution=:ins ";
            m.put("ins", institution);
        } else {
            if (webUserController.getLoggedUser().isRestrictedToInstitution()) {
                j += " and e.institution in :inss ";
                List<Institution> ins = webUserController.getLoggableInstitutions();
                m.put("inss", ins);
            }
        }

        count = encounterFacade.findLongByJpql(j, m);

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

}
