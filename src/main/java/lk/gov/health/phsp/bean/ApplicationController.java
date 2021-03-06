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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.QueryComponent;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.enums.WebUserRole;
import lk.gov.health.phsp.facade.InstitutionFacade;
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
    private InstitutionFacade institutionFacade;
       @EJB
    private QueryComponentFacade queryComponentFacade;
    
// </editor-fold>    

// <editor-fold defaultstate="collapsed" desc="Class Variables">
    private boolean demoSetup = false;
    private String versionNo = "1.1.4";
    private List<QueryComponent> queryComponents;

// </editor-fold>
    public ApplicationController() {
    }

    // <editor-fold defaultstate="collapsed" desc="Functions">
    public String createNewPersonalHealthNumber(Institution pins) {
        if (pins == null) {
            return null;
        }
        Institution ins = getInstitutionFacade().find(pins.getId());
        if(ins==null){
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

    
    public List<QueryComponent> findQueryComponents() {
        String j = "select q from QueryComponent q "
                + " where q.retired=false "
                + " order by q.name";
        Map m = new HashMap();
        return getQueryComponentFacade().findByJpql(j, m);
    }    
    
    public void reloadQueryComponents(){
        queryComponents = null;
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
        if(queryComponents==null){
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

    
    
}
