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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.SessionScoped;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Relationship;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.enums.RelationshipType;
import lk.gov.health.phsp.facade.InstitutionFacade;
import lk.gov.health.phsp.facade.RelationshipFacade;
import org.apache.commons.codec.digest.DigestUtils;
// </editor-fold>

/**
 *
 * @author Dr M H B Ariyaratne<buddhika.ari@gmail.com>
 */
@Named
@SessionScoped
public class InstitutionApplicationController implements Serializable{

// <editor-fold defaultstate="collapsed" desc="EJBs">
    @EJB
    private InstitutionFacade institutionFacade;
    @EJB
    RelationshipFacade relationshipFacade;
// </editor-fold>    

// <editor-fold defaultstate="collapsed" desc="Class Variables">
    private List<Institution> institutions;
    List<Institution> hospitals;
    private List<InstitutionType> hospitalTypes;
    private List<InstitutionType> clinicTypes;
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
    
    
    
    public String getInstitutionHash(){
         return  DigestUtils.md5Hex(getInstitutions().toString()).toUpperCase();
    }

    public List<Institution> getHospitals() {
        fillHospitals();
//        // //System.out.println("hospitals = " + hospitals.size());
        return hospitals;
    }

    public void fillHospitals() {
        hospitals = new ArrayList<>();
        for (Institution i : getInstitutions()) {
            if (institutionTypeCorrect(getHospitalTypes(), i.getInstitutionType())) {
                hospitals.add(i);
            }
        }
    }

    public boolean institutionTypeCorrect(List<InstitutionType> its, InstitutionType it) {
        
        boolean correct = false;
        if (its == null || it == null) {
            return correct;
        }
        for (InstitutionType tit : its) {
            if (tit.equals(it)) {
                correct = true;
            }
        }
        return correct;
    }

    public void setInstitutions(List<Institution> institutions) {
        this.institutions = institutions;
    }

    public Long findInstitutionPopulationData(Institution tins, RelationshipType ttr, Integer ty) {

        if (ty == null) {
            // //System.out.println("No Year");
            return 0l;
        }
        if (tins == null) {
            // //System.out.println("No Institution");
            return 0l;
        }
        if (ttr == null) {
            // //System.out.println("No Relationship Type");
            return 0l;
        }

        String j = "select r from Relationship r "
                + " where r.retired<>:ret "
                + " and r.yearInt=:y";

        Map m = new HashMap();

        j += " and r.institution=:ins  ";
        j += " and r.relationshipType=:rt ";

        m.put("ins", tins);
        m.put("rt", ttr);
        m.put("y", ty);
        m.put("ret", true);

        // //System.out.println("m = " + m);
        // //System.out.println("j = " + j);
        Relationship tr = relationshipFacade.findFirstByJpql(j, m);
        if (tr == null) {
            return 0l;
        }
        return tr.getLongValue1();
    }

    public List<Relationship> findInstitutionPopulationData(Institution tins, Integer ty) {

        if (ty == null) {
            // //System.out.println("No Year");
            return null;
        }
        if (tins == null) {
            // //System.out.println("No Institution");
            return null;
        }

        String j = "select r from Relationship r "
                + " where r.retired<>:ret "
                + " and r.yearInt=:y";

        Map m = new HashMap();

        j += " and r.institution=:ins  ";

        m.put("ins", tins);
        m.put("y", ty);
        m.put("ret", true);

        // //System.out.println("m = " + m);
        // //System.out.println("j = " + j);
        List<Relationship> tr = relationshipFacade.findByJpql(j, m);
        return tr;
    }

    public List<InstitutionType> getHospitalTypes() {
        if (hospitalTypes == null || hospitalTypes.isEmpty()) {
            hospitalTypes = new ArrayList<>();
            hospitalTypes.add(InstitutionType.Base_Hospital);
            hospitalTypes.add(InstitutionType.District_General_Hospital);
            hospitalTypes.add(InstitutionType.Divisional_Hospital);
            hospitalTypes.add(InstitutionType.National_Hospital);
            hospitalTypes.add(InstitutionType.Primary_Medical_Care_Unit);
            hospitalTypes.add(InstitutionType.Teaching_Hospital);
        }
        return hospitalTypes;
    }

    
    
    public Institution findInstitution(Long insId) {
        Institution ri = null;
        for (Institution i : getInstitutions()) {
            if (i.getId().equals(insId)) {
                ri = i;
            }
        }
        return ri;
    }
    
    public Institution findMinistryOfHealth() {
        //System.out.println("find MoH");
        Institution ri = null;
        for (Institution i : getInstitutions()) {
            if (i.getInstitutionType().equals(InstitutionType.Ministry_of_Health)) {
                ri = i;
            }
        }
        //System.out.println("ri = " + ri);
        return ri;
    }

    public List<Institution> findChildrenInstitutions(Institution ins) {
        List<Institution> allIns = getInstitutions();
        List<Institution> cins = new ArrayList<>();
        for (Institution i : allIns) {
            if (i.getParent() == null) {
                continue;
            }
            if (i.getParent().equals(ins)) {
                cins.add(i);
            }
        }
        List<Institution> tins = new ArrayList<>();
        tins.addAll(cins);
        if (cins.isEmpty()) {
            return tins;
        } else {
            for (Institution i : cins) {
                tins.addAll(findChildrenInstitutions(i));
            }
        }
        return tins;
    }

    public List<Institution> findChildrenInstitutions(Institution ins, InstitutionType type) {
        List<Institution> cins = findChildrenInstitutions(ins);
        List<Institution> tins = new ArrayList<>();
        for (Institution i : cins) {
            if (i.getParent() == null) {
                continue;
            }
            if (i.getInstitutionType() == null) {
                continue;
            }
            if (i.getInstitutionType().equals(type)) {
                tins.add(i);
            }
        }
        return tins;
    }

    public List<InstitutionType> getClinicTypes() {
        if (clinicTypes == null || clinicTypes.isEmpty()) {
            clinicTypes = new ArrayList<>();
            clinicTypes.add(InstitutionType.Clinic);
            clinicTypes.add(InstitutionType.Cardiology_Clinic);
            clinicTypes.add(InstitutionType.Medical_Clinic);
            clinicTypes.add(InstitutionType.Other_Clinic);
            clinicTypes.add(InstitutionType.Surgical_Clinic);
            clinicTypes.add(InstitutionType.Ward_Clinic);
        }
        return clinicTypes;
    }

}
