package lk.gov.health.phsp.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import lk.gov.health.phsp.entity.ApiRequest;
import lk.gov.health.phsp.entity.Client;
import lk.gov.health.phsp.entity.ClientEncounterComponentFormSet;
import lk.gov.health.phsp.entity.Encounter;
import lk.gov.health.phsp.entity.Prescription;
import lk.gov.health.phsp.facade.ApiRequestFacade;
import lk.gov.health.phsp.pojcs.PrescriptionItemPojo;
import lk.gov.health.phsp.pojcs.PrescriptionPojo;

@Named
@SessionScoped
public class ApiRequestApplicationController implements Serializable {

    @EJB
    private ApiRequestFacade ejbFacade;

    @Inject
    private WebUserController webUserController;
    @Inject
    private CommonController commonController;
    @Inject
    private UserTransactionController userTransactionController;

    // <editor-fold defaultstate="collapsed" desc="Getters and Setters">
    private ApiRequestFacade getFacade() {
        return ejbFacade;
    }

    public boolean markRequestAsReceived(String id) {
        Long lngId = CommonController.stringToLong(id);
        if (lngId == null || lngId == 0) {
            return false;
        }
        ApiRequest r = getFacade().find(lngId);
        if (r == null) {
            return false;
        }
        // //// System.out.println("r.isConvaied() = " + r.isConvaied());
        r.setConvaied(true);
        r.setConvaiedAt(new Date());
        ejbFacade.edit(r);
        // //// System.out.println("r.isConvaied() = " + r.isConvaied());
        return true;
    }

    public ApiRequest getApiRequest(String id) {
        Long lid = CommonController.getLongValue(id);
        if (lid == null) {
            return null;
        }
        return getFacade().find(lid);
    }

    public List<ApiRequest> getPendingProcedure(String id) {
        // //// System.out.println("getPendingProcedure");
        // //// System.out.println("id = " + id);
        Map m = new HashMap();
        m.put("ret", false);
        m.put("con", false);
        m.put("name", "procedure_request");
        String j = "select a "
                + " from ApiRequest a "
                + " where a.retired=:ret "
                + " and a.convaied=:con"
                + " and a.name=:name ";
        j += " order by a.id";
        List<ApiRequest> rs = getFacade().findByJpql(j, m);
        if (id != null && !id.trim().equals("")) {
            Long tid = CommonController.stringToLong(id);
            List<ApiRequest> irs = new ArrayList<>();
            for (ApiRequest ar : rs) {
                if (ar.getRequestCeci() != null && ar.getRequestCeci().getInstitutionValue() != null) {
                    if (ar.getRequestCeci().getInstitutionValue().getId().equals(tid)) {
                        irs.add(ar);
                    }
                }
            }
            return irs;
        }
        return rs;
    }

    public  List<PrescriptionPojo> getPendingPrescriptions() {
        // //// System.out.println("getPendingPrescreptions");
        Map m = new HashMap();
        m.put("ret", false);
        m.put("con", false);
        m.put("name", "prescription_request");
        String j = "select a "
                + " from ApiRequest a "
                + " where a.retired=:ret "
                + " and a.convaied=:con "
                + " and a.name=:name "
                + " and a.requestCefs is not null ";
        j += " order by a.id";
        List<ApiRequest> precrips = getFacade().findByJpql(j, m);
        List<PrescriptionPojo> ps = new ArrayList<>();
            List<ApiRequest> irs = new ArrayList<>();
            for (ApiRequest presc : precrips) {
                if (presc.getRequestCefs() == null) {
                    continue;
                }
                ClientEncounterComponentFormSet cefs = presc.getRequestCefs();
                if (cefs.getEncounter() == null) {
                    continue;
                }
                Encounter e = cefs.getEncounter();
                if (e.getClient() == null) {
                    continue;
                }
                Client c = e.getClient();
                PrescriptionPojo p = new PrescriptionPojo();
                p.setName(c.getPerson().getNameWithTitle());
                p.setAge(c.getPerson().getAge());
                p.setAgeInDays(c.getPerson().getAgeInDays());
                p.setInstitutionId(e.getInstitution().getId());

                m = new HashMap();
                m.put("ret", false);
                m.put("con", false);
                m.put("name", "prescription_request");
                m.put("p", presc);
                j = "select a "
                        + " from ApiRequest a "
                        + " where a.retired=:ret "
                        + " and a.convaied=:con "
                        + " and a.name=:name "
                        + " and a.parent=:p ";
                j += " order by a.id";

                List<ApiRequest> pis = getFacade().findByJpql(j, m);

                for (ApiRequest pi : pis) {
                    if (pi.getRequestCeci() != null && pi.getRequestCeci().getPrescriptionValue() != null) {
                        PrescriptionItemPojo i = new PrescriptionItemPojo();
                        Prescription pres = pi.getRequestCeci().getPrescriptionValue();
                        if(pres.getMedicine()!=null){
                            i.setMedicine(pres.getMedicine().getName());
                            i.setMedicineId(pres.getMedicine().getId());
                        }
                        if(pres.getMedicine().getItemType()!=null){
                            i.setMedicineType(pres.getMedicine().getItemType().name());
                        }
                        if(pres.getDose()!=null){
                            i.setDose(pres.getDose());
                        }
                        if(pres.getDoseUnit()!=null){
                            i.setDoseUnitId(pres.getDoseUnit().getId());
                        }
                        if(pres.getFrequency()!=null){
                            i.setFrequencyUnitId(pres.getFrequency().getId());
                        }
                        if(pres.getDuration()!=null){
                            i.setDuration(pres.getDuration());
                        }
                        if(pres.getDurationUnit()!=null){
                            i.setDurationUnitId(pres.getDurationUnit().getId());
                        }
                        if(pres.getDescription()!=null){
                            i.setComments(pres.getDescription());
                        }
                        if(pres.getIssueQuantity()!=null){
                            i.setIssueQty(pres.getIssueQuantity());
                        }
                        if(pres.getIssueUnit()!=null){
                            i.setIssueUnitId(pres.getIssueUnit().getId());
                        }
                        p.getItems().add(i);
                    }
                    ps.add(p);
                }

            }
        
        return ps;
    }

    public void saveApiRequests(ApiRequest p) {
        if (p == null) {
            return;
        }
        if (p.getId() == null) {
            p.setCreatedAt(new Date());
            p.setCreatedBy(webUserController.getLoggedUser());
            getFacade().create(p);
        } else {
            getFacade().edit(p);
        }
    }

    public ApiRequestFacade getEjbFacade() {
        return ejbFacade;
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public void setWebUserController(WebUserController webUserController) {
        this.webUserController = webUserController;
    }

    public CommonController getCommonController() {
        return commonController;
    }

    public void setCommonController(CommonController commonController) {
        this.commonController = commonController;
    }

    public UserTransactionController getUserTransactionController() {
        return userTransactionController;
    }

    public void setUserTransactionController(UserTransactionController userTransactionController) {
        this.userTransactionController = userTransactionController;
    }

}
