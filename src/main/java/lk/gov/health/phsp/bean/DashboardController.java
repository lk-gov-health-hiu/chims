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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.enums.EncounterType;
import lk.gov.health.phsp.facade.ClientEncounterComponentItemFacade;
import lk.gov.health.phsp.facade.ClientFacade;
import lk.gov.health.phsp.facade.EncounterFacade;

/**
 *
 * @author buddhika
 */
@Named
@ApplicationScoped
public class DashboardController {

    @EJB
    ClientFacade clientFacade;
    @EJB
    EncounterFacade encounterFacade;
    @EJB
    ClientEncounterComponentItemFacade clientEncounterComponentItemFacade;

    private Long totalNumberOfRegisteredClients;
    private Long totalNumberOfClinicVisits;
    private Long totalNumberOfClinicEnrolments;
    private Long totalNumberOfCvsRiskClients;

    private String riskVariable = "cvs_risk_factor";
    private String riskVal1 = "30-40%";
    private String riskVal2 = ">40%";
    private List<String> riskVals;

    /**
     * Creates a new instance of DashboardController
     */
    public DashboardController() {
    }

    public void prepareSystemDashboard() {

        if (totalNumberOfRegisteredClients == null) {
            totalNumberOfRegisteredClients = findTotalNumberOfRegisteredClientsForAdmin();
        }
        if (totalNumberOfClinicEnrolments == null) {
            totalNumberOfClinicEnrolments = findTotalNumberOfClinicEnrolmentsForAdmin();
        }
        if (totalNumberOfClinicVisits == null) {
            totalNumberOfClinicVisits = findTotalNumberOfClinicVisitsForAdmin();
        }
        if (totalNumberOfCvsRiskClients == null) {
            riskVals = new ArrayList<>();
            riskVals.add(riskVal1);
            riskVals.add(riskVal2);
            totalNumberOfCvsRiskClients = findTotalNumberOfCvsRiskClientsForAdmin();
        }
    }

    public Long findTotalNumberOfRegisteredClientsForAdmin() {
        return (countOfRegistedClients(null, null));
    }

    public Long findTotalNumberOfClinicEnrolmentsForAdmin() {
        return (countOfEncounters(null, EncounterType.Clinic_Enroll));
    }

    public Long findTotalNumberOfClinicVisitsForAdmin() {
        return (countOfEncounters(null, EncounterType.Clinic_Visit));
    }

    public Long findTotalNumberOfCvsRiskClientsForAdmin() {
        riskVals = new ArrayList<>();
        riskVals.add(riskVal1);
        riskVals.add(riskVal2);
        return (findClientCountEncounterComponentItemMatchCount(
                null, CommonController.startOfTheYear(), new Date(), riskVariable, riskVals));
    }

    public Long countOfRegistedClients(Institution ins, Area gn) {
        String j = "select count(c) from Client c "
                + " where c.retired=:ret "
                + " and c.reservedClient<>:res ";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("res", true);
        if (ins != null) {
            j += " and c.createInstitution=:ins ";
            m.put("ins", ins);
        }
        if (gn != null) {
            j += " and c.person.gnArea=:gn ";
            m.put("gn", gn);
        }
        return clientFacade.countByJpql(j, m);
    }

    public Long countOfEncounters(List<Institution> clinics, EncounterType ec) {
        String j = "select count(e) from Encounter e "
                + " where e.retired=:ret "
                + " and e.encounterType=:ec "
                + " and e.createdAt>:d";
        Map m = new HashMap();
        m.put("d", CommonController.startOfTheYear());
        m.put("ec", ec);
        m.put("ret", false);
        if (clinics != null && !clinics.isEmpty()) {
            m.put("ins", clinics);
            j += " and e.institution in :ins ";
        }
        return encounterFacade.findLongByJpql(j, m);
    }

    public long findClientCountEncounterComponentItemMatchCount(
            List<Institution> ins,
            Date fromDate,
            Date toDate,
            String itemCode,
            List<String> valueStrings) {

        String j;
        Map m = new HashMap();

        j = "select count(f) "
                + " from ClientEncounterComponentItem f "
                + " where f.retired<>:ret ";
        j += " and f.item.code=:ic ";
        j += " and f.shortTextValue in :ivs";
        m.put("ic", itemCode);
        m.put("ret", true);
        m.put("ivs", valueStrings);
        if (fromDate != null && toDate != null) {
            m.put("fd", fromDate);
            m.put("td", toDate);
            j += " and f.createdAt between :fd and :td ";
        }
        return clientEncounterComponentItemFacade.findLongByJpql(j, m);
    }

    public Long getTotalNumberOfRegisteredClients() {
        if (totalNumberOfRegisteredClients == null) {
            prepareSystemDashboard();
        }
        return totalNumberOfRegisteredClients;
    }

    public void setTotalNumberOfRegisteredClients(Long totalNumberOfRegisteredClients) {
        this.totalNumberOfRegisteredClients = totalNumberOfRegisteredClients;
    }

    public Long getTotalNumberOfClinicVisits() {
        if (totalNumberOfClinicVisits == null) {
            prepareSystemDashboard();
        }
        return totalNumberOfClinicVisits;
    }

    public void setTotalNumberOfClinicVisits(Long totalNumberOfClinicVisits) {
        this.totalNumberOfClinicVisits = totalNumberOfClinicVisits;
    }

    public Long getTotalNumberOfClinicEnrolments() {
        if (totalNumberOfClinicEnrolments == null) {
            prepareSystemDashboard();
        }
        return totalNumberOfClinicEnrolments;
    }

    public void setTotalNumberOfClinicEnrolments(Long totalNumberOfClinicEnrolments) {
        this.totalNumberOfClinicEnrolments = totalNumberOfClinicEnrolments;
    }

    public Long getTotalNumberOfCvsRiskClients() {
        if (totalNumberOfCvsRiskClients == null) {
            prepareSystemDashboard();
        }
        return totalNumberOfCvsRiskClients;
    }

    public void setTotalNumberOfCvsRiskClients(Long totalNumberOfCvsRiskClients) {
        this.totalNumberOfCvsRiskClients = totalNumberOfCvsRiskClients;
    }

    public String getRiskVariable() {
        return riskVariable;
    }

    public void setRiskVariable(String riskVariable) {
        this.riskVariable = riskVariable;
    }

    public String getRiskVal1() {
        return riskVal1;
    }

    public void setRiskVal1(String riskVal1) {
        this.riskVal1 = riskVal1;
    }

    public String getRiskVal2() {
        return riskVal2;
    }

    public void setRiskVal2(String riskVal2) {
        this.riskVal2 = riskVal2;
    }

    public List<String> getRiskVals() {
        return riskVals;
    }

    public void setRiskVals(List<String> riskVals) {
        this.riskVals = riskVals;
    }

}
