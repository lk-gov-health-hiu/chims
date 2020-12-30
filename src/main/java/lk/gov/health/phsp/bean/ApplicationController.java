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
import java.util.Random;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.entity.Phn;
import lk.gov.health.phsp.entity.QueryComponent;
import lk.gov.health.phsp.enums.AreaType;
import lk.gov.health.phsp.facade.AreaFacade;
import lk.gov.health.phsp.enums.EncounterType;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.enums.WebUserRole;
import lk.gov.health.phsp.facade.ClientEncounterComponentItemFacade;
import lk.gov.health.phsp.facade.ClientFacade;
import lk.gov.health.phsp.facade.EncounterFacade;
import lk.gov.health.phsp.facade.InstitutionFacade;
import lk.gov.health.phsp.facade.PhnFacade;
import lk.gov.health.phsp.facade.QueryComponentFacade;
// </editor-fold>

/**
 *
 * @author Dr M H B Ariyaratne<buddhika.ari@gmail.com>
 */
@Named(value = "applicationController")
@ApplicationScoped
public class ApplicationController {

// <editor-fold defaultstate="collapsed" desc="EJBs">
    @EJB
    private AreaFacade areaFacade;
    @EJB
    private InstitutionFacade institutionFacade;
    @EJB
    private QueryComponentFacade queryComponentFacade;    
    @EJB
    private ClientFacade clientFacade;
    @EJB
    private EncounterFacade encounterFacade;
    @EJB
    private ClientEncounterComponentItemFacade clientEncounterComponentItemFacade;    
    @EJB
    PhnFacade phnFacade;
// </editor-fold>    
    @Inject
    private UserTransactionController userTransactionController;

// <editor-fold defaultstate="collapsed" desc="Class Variables">
    private boolean demoSetup = false;
    private boolean production = true;
    private String versionNo = "1.1.4";
    private List<QueryComponent> queryComponents;
    private List<Item> items;
    private List<String> userTransactionTypes;
    private List<Institution> institutions;
    private List<Area> gnAreas = new ArrayList<>();
    private final boolean logActivity = true;
    private Long totalNumberOfRegisteredClientsForAdmin = null;
    private Long totalNumberOfClinicEnrolmentsForAdmin = null;
    private Long totalNumberOfClinicVisitsForAdmin = null;
    private Long totalNumberOfCvsRiskClientsForAdmin = null;

    String riskVariable = "cvs_risk_factor";
    String riskVal1 = "30-40%";
    String riskVal2 = ">40%";
    List<String> riskVals;    
    // </editor-fold>
    public ApplicationController() {
    }

    // <editor-fold defaultstate="collapsed" desc="Functions">
    public List<Area> getGnAreas(String qry) {
        if (gnAreas == null) {
            this.setGnAreas(getAllGnAreas(qry));
        }
        return gnAreas;        
    }

    public String createNewPersonalHealthNumber(Institution pins) {
        if (pins == null) {
            return null;
        }
        Institution ins = getInstitutionFacade().find(pins.getId());
        if (ins == null) {
            return null;
        }
        Long lastHinIssued = ins.getLastHin();
        if (lastHinIssued == null) {
            lastHinIssued = 0l;
        }
        Long thisHin = lastHinIssued + 1;
        String poi = ins.getPoiNumber();
        String num = String.format("%06d", thisHin);
        String checkDigit = calculateCheckDigit(poi + num);
        String phn = poi + num + checkDigit;
        ins.setLastHin(thisHin);
        getInstitutionFacade().edit(ins);

        return phn;
    }


    public String createNewPersonalHealthNumberformat(Institution pins) {
        if (pins == null) {
            return null;
        }
        Institution ins = getInstitutionFacade().find(pins.getId());
        if (ins == null) {
            return null;
        }
        String alpha = "BCDFGHJKMPQRTVWXY";
        String numeric = "23456789";
        String alphanum = alpha + numeric;
        
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
   
        int length = 6;
        for(int i = 0; i < length; i++) {
        int index = random.nextInt(alphanum.length());
        char randomChar = alphanum.charAt(index);
        sb.append(randomChar);
        }
        
        String randomString = sb.toString();
        
        String poi = ins.getPoiNumber();
        String checkDigit = calculateCheckDigit(poi + randomString);
        String phn = poi + randomString + checkDigit;

        return phn;
    }
     

    public String createNewPersonalHealthNumberRandomly(Institution pins) {
        if (pins == null) {
            return null;
        }
        Institution ins = getInstitutionFacade().find(pins.getId());
        if (ins == null) {
            return null;
        }
        
        
        String hex;
        Double maxDbl = Math.pow(16, 7);
        long leftLimit = 1L;
        long rightLimit = maxDbl.longValue();
        long generatedLong = leftLimit + (long) (Math.random() * (rightLimit - leftLimit));
        hex = Long.toHexString(generatedLong);

        hex = "0000000" + hex; 
        hex = hex.substring(hex.length()-7, hex.length());

        
        
        String poi = ins.getPoiNumber();
        String checkDigit = calculateCheckDigit(poi + hex);
        String phn = poi + hex + checkDigit;
        

        boolean creationFailed;
        do{
            creationFailed = !savePhn(phn, ins);
        }while(creationFailed);
        
        return phn;

    }


    private boolean savePhn(String phn, Institution poi) {
        try {
            Phn p = new Phn(phn, poi);
            phnFacade.create(p);
            return true;
        } catch (Exception e) {
            return false;
        }

    }


    public static boolean validateHin(String validatingHin) {
        if (validatingHin == null) {
            return false;
        }
        char checkDigit = validatingHin.charAt(validatingHin.length() - 1);
        String digit = calculateCheckDigit(validatingHin.substring(0, validatingHin.length() - 1));
        return checkDigit == digit.charAt(0);
    }

    public static String calculateCheckDigit(String card) {
        if (card == null) {
            return null;
        }
        String digit;
        /* convert to array of int for simplicity */
        int[] digits = new int[card.length()];
        for (int i = 0; i < card.length(); i++) {
            digits[i] = Character.getNumericValue(card.charAt(i));
        }

        /* double every other starting from right - jumping from 2 in 2 */
        for (int i = digits.length - 1; i >= 0; i -= 2) {
            digits[i] += digits[i];

            /* taking the sum of digits grater than 10 - simple trick by substract 9 */
            if (digits[i] >= 10) {
                digits[i] = digits[i] - 9;
            }
        }
        int sum = 0;
        for (int i = 0; i < digits.length; i++) {
            sum += digits[i];
        }
        /* multiply by 9 step */
        sum = sum * 9;

        /* convert to string to be easier to take the last digit */
        digit = sum + "";
        return digit.substring(digit.length() - 1);
    }
    
    public List<Area> getAllGnAreas(String qry) {
        String j;
        Map m = new HashMap();
        j = "select a "
                + " from Area a "
                + " where a.name is not null "
                + " and a.type=:t";
            
        m.put("t", AreaType.GN);
        
        if (qry != null) {
            j += " and lower(a.name) like :qry ";
            m.put("qry", "%" + qry.toLowerCase() + "%");
        }
        
        j += " order by a.name";
        return getAreaFacade().findByJpql(j, m);
    }

    private List<QueryComponent> findQueryComponents() {
        String j = "select q from QueryComponent q "
                + " where q.retired=false "
                + " order by q.orderNo, q.name";
        Map m = new HashMap();
        return queryComponents = getQueryComponentFacade().findByJpql(j, m);

    }
    
    public Long countOfRegistedClients(Institution ins, Area gn) {
        String j = "select count(c) from Client c "
                + " where c.retired=:ret ";
        Map m = new HashMap();
        m.put("ret", false);
        if (ins != null) {
            j += " and c.createInstitution=:ins ";
            m.put("ins", ins);
        }
        if (gn != null) {
            j += " and c.person.gnArea=:gn ";
            m.put("gn", gn);
        }
        return getClientFacade().countByJpql(j, m);
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
        return getEncounterFacade().findLongByJpql(j, m);
    }
    
    public long findClientCountEncounterComponentItemMatchCount(
            List<Institution> ins,
            Date fromDate,
            Date toDate,
            String itemCode,
            List<String> valueStrings) {
        
        if (logActivity) {

        }
        String j;
        Map m = new HashMap();
        
        j = "select count(f.encounter) "
                + " from ClientEncounterComponentItem f "
                + " where f.retired<>:ret "
                + " and f.encounter.retired<>:ret ";
        j += " and f.item.code=:ic ";
        j += " and f.shortTextValue in :ivs";
        m.put("ic", itemCode);
        m.put("ret", true);
        m.put("ivs", valueStrings);
        if (ins != null && !ins.isEmpty()) {
            m.put("ins", ins);
            j += " and f.encounter.institution in :ins ";
        }
        if (fromDate != null && toDate != null) {
            m.put("fd", fromDate);
            m.put("td", toDate);
            j += " and f.encounter.encounterDate between :fd and :td ";
        }
//        j += " group by e";
        return getClientEncounterComponentItemFacade().findLongByJpql(j, m);
    }

    public void reloadQueryComponents() {
        queryComponents = null;
        userTransactionController.recordTransaction("Reload Query Components");
    }

    // </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Enums">
    public InstitutionType[] getInstitutionTypes() {
        return InstitutionType.values();
    }

    public WebUserRole[] getWebUserRoles() {
        return WebUserRole.values();
    }

    // <editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Getters & Setters">
    private InstitutionFacade getInstitutionFacade() {
        return institutionFacade;
    }

// </editor-fold>
    public boolean isDemoSetup() {
        return demoSetup;
    }

    

    public void setDemoSetup(boolean demoSetup) {
        this.demoSetup = demoSetup;
    }

    public String getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(String versionNo) {
        this.versionNo = versionNo;
    }

    public List<QueryComponent> getQueryComponents() {
        if (queryComponents == null) {
            queryComponents = findQueryComponents();
        }
        return queryComponents;
    }

    public QueryComponentFacade getQueryComponentFacade() {
        return queryComponentFacade;
    }

    public void setQueryComponentFacade(QueryComponentFacade queryComponentFacade) {
        this.queryComponentFacade = queryComponentFacade;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public List<String> getUserTransactionTypes() {
        return userTransactionTypes;
    }

    public void setUserTransactionTypes(List<String> userTransactionTypes) {
        this.userTransactionTypes = userTransactionTypes;
    }

    public List<Institution> getInstitutions() {
        return institutions;
    }

    public void setInstitutions(List<Institution> institutions) {
        this.institutions = institutions;
    }

    public boolean isProduction() {
        return production;
    }

    public void setProduction(boolean production) {
        this.production = production;
    }

    public List<Area> getGnAreas() {
        return gnAreas;
    }

    public void setGnAreas(List<Area> gnAreas) {
        this.gnAreas = gnAreas;
    }

    public AreaFacade getAreaFacade() {
        return areaFacade;
    }

    public void setAreaFacade(AreaFacade areaFacade) {
        this.areaFacade = areaFacade;
    }
    public Long getTotalNumberOfRegisteredClientsForAdmin() {
        if (totalNumberOfRegisteredClientsForAdmin == null) {
            setTotalNumberOfRegisteredClientsForAdmin(countOfRegistedClients(null, null));
        }
        return totalNumberOfRegisteredClientsForAdmin;
    }

    public void setTotalNumberOfRegisteredClientsForAdmin(Long totalNumberOfRegisteredClientsForAdmin) {
        this.totalNumberOfRegisteredClientsForAdmin = totalNumberOfRegisteredClientsForAdmin;
    }

    public Long getTotalNumberOfClinicEnrolmentsForAdmin() {
        if (totalNumberOfClinicEnrolmentsForAdmin == null) {
            setTotalNumberOfClinicEnrolmentsForAdmin(countOfEncounters(null, EncounterType.Clinic_Enroll));
        }
        return totalNumberOfClinicEnrolmentsForAdmin;
    }

    public void setTotalNumberOfClinicEnrolmentsForAdmin(Long totalNumberOfClinicEnrolmentsForAdmin) {
        this.totalNumberOfClinicEnrolmentsForAdmin = totalNumberOfClinicEnrolmentsForAdmin;
    }

    public Long getTotalNumberOfClinicVisitsForAdmin() {
        if (totalNumberOfClinicVisitsForAdmin == null) {
            setTotalNumberOfClinicVisitsForAdmin(countOfEncounters(null, EncounterType.Clinic_Visit));
        }
        return totalNumberOfClinicVisitsForAdmin;
    }

    public void setTotalNumberOfClinicVisitsForAdmin(Long totalNumberOfClinicVisitsForAdmin) {
        this.totalNumberOfClinicVisitsForAdmin = totalNumberOfClinicVisitsForAdmin;
    }

    public Long getTotalNumberOfCvsRiskClientsForAdmin() {
        riskVals = new ArrayList<>();
        riskVals.add(riskVal1);
        riskVals.add(riskVal2);
        if (totalNumberOfCvsRiskClientsForAdmin == null) {
            setTotalNumberOfCvsRiskClientsForAdmin(findClientCountEncounterComponentItemMatchCount(
                    null, CommonController.startOfTheYear(), new Date(), riskVariable, riskVals));
        }
        return totalNumberOfCvsRiskClientsForAdmin;
    }

    public void setTotalNumberOfCvsRiskClientsForAdmin(Long totalNumberOfCvsRiskClientsForAdmin) {
        this.totalNumberOfCvsRiskClientsForAdmin = totalNumberOfCvsRiskClientsForAdmin;
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

    public ClientEncounterComponentItemFacade getClientEncounterComponentItemFacade() {
        return clientEncounterComponentItemFacade;
    }

    public void setClientEncounterComponentItemFacade(ClientEncounterComponentItemFacade clientEncounterComponentItemFacade) {
        this.clientEncounterComponentItemFacade = clientEncounterComponentItemFacade;
    }
}
