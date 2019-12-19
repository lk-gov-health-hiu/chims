/*
 * The MIT License
 *
 * Copyright 2019 buddhika.ari@gmail.com
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

// <editor-fold defaultstate="collapsed" desc="Imports">
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.Client;
import lk.gov.health.phsp.entity.Encounter;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.enums.EncounterType;
// </editor-fold>   

/**
 *
 * @author hiu_pdhs_sp
 */
@Named(value = "reportController")
@SessionScoped
public class ReportController implements Serializable {
// <editor-fold defaultstate="collapsed" desc="EJBs">
// </editor-fold>     
// <editor-fold defaultstate="collapsed" desc="Controllers">

    @Inject
    private EncounterController encounterController;
    @Inject
    private ClientController clientController;
    @Inject
    private ComponentController componentController;
    @Inject
    private WebUserController webUserController;
    @Inject
    private InstitutionController institutionController;
// </editor-fold>  
// <editor-fold defaultstate="collapsed" desc="Class Variables">
    private List<Encounter> encounters;
    private List<Client> clients;
    private Date fromDate;
    private Date toDate;
    private Institution institution;
    private Area area;
    
// </editor-fold> 

// <editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Creates a new instance of ReportController
     */
    public ReportController() {
    }

// </editor-fold> 
// <editor-fold defaultstate="collapsed" desc="Navigation">
    public String toViewClientRegistrations() {
        encounters = new ArrayList<>();
        String forSys = "/reports/client_registrations/for_system";
        String forIns = "/reports/client_registrations/for_ins";
        String forMe = "/reports/client_registrations/for_me";
        String forClient = "/reports/client_registrations/for_clients";
        String noAction = "";
        String action = "";
        switch (webUserController.getLoggedUser().getWebUserRole()) {
            case Client:
                action = forClient;
                break;
            case Doctor:
            case Institution_Administrator:
            case Institution_Super_User:
            case Institution_User:
            case Nurse:
            case Midwife:
                action = forIns;
                break;
            case Me_Admin:
            case Me_Super_User:
                action = forMe;
                break;
            case Me_User:
            case User:
                action = noAction;
                break;
            case Super_User:
            case System_Administrator:
                action = forSys;
                break;
        }
        return action;
    }
    
    public String toViewClinicEnrollments() {
        encounters = new ArrayList<>();
        String forSys = "/reports/clinic_enrollments/for_system";
        String forIns = "/reports/clinic_enrollments/for_ins";
        String forMe = "/reports/clinic_enrollments/for_me";
        String forClient = "/reports/clinic_enrollments/for_clients";
        String noAction = "";
        String action = "";
        switch (webUserController.getLoggedUser().getWebUserRole()) {
            case Client:
                action = forClient;
                break;
            case Doctor:
            case Institution_Administrator:
            case Institution_Super_User:
            case Institution_User:
            case Nurse:
            case Midwife:
                action = forIns;
                break;
            case Me_Admin:
            case Me_Super_User:
                action = forMe;
                break;
            case Me_User:
            case User:
                action = noAction;
                break;
            case Super_User:
            case System_Administrator:
                action = forSys;
                break;
        }
        return action;
    }

// </editor-fold>   
// <editor-fold defaultstate="collapsed" desc="Functions">
    
    public void fillClientRegistrationForSysAdmin(){
        String j;
        Map m = new HashMap();
        j = "select c from Client c "
                + " where c.retired=:ret "
                + " and c.createdAt between :fd and :td ";
        m.put("ret", false);
        m.put("fd", fromDate);
        m.put("td", toDate);
        if(institution!=null){
            j += " and c.createInstitution in :ins ";
            List<Institution> ins = institutionController.findChildrenInstitutions(institution);
            ins.add(institution);
            m.put("ins", ins);
        }
        
        clients = clientController.getItems(j,m);
    }
    
    public void fillClinicEnrollmentsForSysAdmin(){
        String j;
        Map m = new HashMap();
        j = "select c from Encounter c "
                + " where c.retired=:ret "
                + " c.encounterType=:type "
                + " and c.encounterDate between :fd and :td ";
        m.put("ret", false);
        m.put("fd", fromDate);
        m.put("td", toDate);
        m.put("type", EncounterType.Clinic_Enroll);
        if(institution!=null){
            j += " and c.institution in :ins ";
            List<Institution> ins = institutionController.findChildrenInstitutions(institution);
            ins.add(institution);
            m.put("ins", ins);
        }
        encounters = encounterController.getItems(j,m);
    }
    
    public void fillEncountersForSysAdmin(){
        String j;
        Map m = new HashMap();
        j = "select c from Encounter c "
                + " where c.retired=:ret "
                + " c.encounterType=:type "
                + " and c.encounterDate between :fd and :td ";
        m.put("ret", false);
        m.put("fd", fromDate);
        m.put("td", toDate);
        m.put("type", EncounterType.Clinic_Enroll);
        if(institution!=null){
            j += " and c.institution in :ins ";
            List<Institution> ins = institutionController.findChildrenInstitutions(institution);
            ins.add(institution);
            m.put("ins", ins);
        }
        encounters = encounterController.getItems(j,m);
    }
    
    
// </editor-fold>   
// <editor-fold defaultstate="collapsed" desc="Getters and Setters">

    

    public EncounterController getEncounterController() {
        return encounterController;
    }

    public ClientController getClientController() {
        return clientController;
    }

    public ComponentController getComponentController() {
        return componentController;
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public List<Encounter> getEncounters() {
        return encounters;
    }

    public void setEncounters(List<Encounter> encounters) {
        this.encounters = encounters;
    }

    public List<Client> getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }
    
    public Date getFromDate() {
        if(fromDate==null){
            fromDate = CommonController.startOfTheYear();
        }
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        if(toDate==null){
            toDate = new Date();
        }
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }
    
// </editor-fold> 

    public InstitutionController getInstitutionController() {
        return institutionController;
    }

    

}
