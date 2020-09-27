/*
 * The MIT License
 *
 * Copyright 2020 chims.
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
import javax.persistence.TemporalType;
import lk.gov.health.phsp.entity.ClientEncounterComponentFormSet;
import lk.gov.health.phsp.facade.ClientFacade;
import lk.gov.health.phsp.facade.EncounterFacade;

/**
 *
 * @author chims
 */
@Named(value = "analysisController")
@ApplicationScoped
public class AnalysisController {

    @EJB
    private ClientFacade clientFacade;
    @EJB
    private EncounterFacade encounterFacade;

    private Long clientCount;
    private Long encounterCount;
    private Date from;
    private Date to;

    /**
     * Creates a new instance of AnalysisController
     */
    public AnalysisController() {
    }

    public String toCountsForSystemAdmin() {
        findClientCount();
        findEncounterCount();
        return "/systemAdmin/all_counts";
    }

    public void findEncounterCount() {
        Long fs;
        Map m = new HashMap();
        String j = "select count(s) from ClientEncounterComponentFormSet s ";
        j += " where s.retired<>:ret ";

        if (from != null && to != null) {
            j += " and s.encounter.encounterFrom between :fd and :td ";
            m.put("fd", getFrom());
            m.put("td", getTo());
        }

       
        m.put("ret", true);

        fs = getEncounterFacade().findLongByJpql(j, m);

        encounterCount = fs;
    }

    public void findClientCount() {
        String j = "select count(c) from Client c "
                + " where c.retired<>:ret ";
        Map m = new HashMap();
        m.put("ret", true);
        if (from != null && to != null) {
            j = j + " and c.createdAt between :fd and :td ";
            m.put("fd", getFrom());
            m.put("td", getTo());
        }
        clientCount = getClientFacade().findLongByJpql(j, m);
    }

    public ClientFacade getClientFacade() {
        return clientFacade;
    }

    public void setClientFacade(ClientFacade clientFacade) {
        this.clientFacade = clientFacade;
    }

    public EncounterFacade getEncounterFacade() {
        return encounterFacade;
    }

    public void setEncounterFacade(EncounterFacade encounterFacade) {
        this.encounterFacade = encounterFacade;
    }

    public Long getClientCount() {
        return clientCount;
    }

    public void setClientCount(Long clientCount) {
        this.clientCount = clientCount;
    }

    public Long getEncounterCount() {
        return encounterCount;
    }

    public void setEncounterCount(Long encounterCount) {
        this.encounterCount = encounterCount;
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Date getTo() {
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }

}
