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
package lk.gov.health.phsp.bean;

// <editor-fold defaultstate="collapsed" desc="Import">
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.entity.QueryComponent;
import lk.gov.health.phsp.enums.EncounterType;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.enums.WebUserRole;
import lk.gov.health.phsp.facade.ClientEncounterComponentItemFacade;
import lk.gov.health.phsp.facade.ClientFacade;
import lk.gov.health.phsp.facade.EncounterFacade;
import lk.gov.health.phsp.facade.InstitutionFacade;
import lk.gov.health.phsp.facade.QueryComponentFacade;
// </editor-fold>

/**
 *
 * @author Dr M H B Ariyaratne<buddhika.ari@gmail.com>
 */
@Named
@ApplicationScoped
public class InstitutionApplicationController {

// <editor-fold defaultstate="collapsed" desc="EJBs">
    @EJB
    private InstitutionFacade institutionFacade;
// </editor-fold>    

// <editor-fold defaultstate="collapsed" desc="Class Variables">
    private List<Institution> institutions;
    // </editor-fold>

    public InstitutionApplicationController() {
    }

    // <editor-fold defaultstate="collapsed" desc="Enums">
    public InstitutionType[] getInstitutionTypes() {
        return InstitutionType.values();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters & Setters">
    private List<Institution> fillAllInstitutions() {
        String j;
        Map m = new HashMap();
        j = "select i from Institution i where i.retired=:ret "
                + " order by i.name ";
        m.put("ret", false);
        return institutionFacade.findByJpql(j, m);
    }

    public void resetAllInstitutions() {
        institutions = null;
    }

// </editor-fold>
    public List<Institution> getInstitutions() {
        if (institutions == null) {
            institutions = fillAllInstitutions();
        }
        return institutions;
    }

    public void setInstitutions(List<Institution> institutions) {
        this.institutions = institutions;
    }

}
