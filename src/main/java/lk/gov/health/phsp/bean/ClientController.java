package lk.gov.health.phsp.bean;

// <editor-fold defaultstate="collapsed" desc="Import">
import lk.gov.health.phsp.entity.Client;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.bean.util.JsfUtil.PersistAction;
import lk.gov.health.phsp.facade.ClientFacade;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import lk.gov.health.phsp.entity.Encounter;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.enums.EncounterType;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.facade.EncounterFacade;
import lk.gov.health.phsp.pojcs.YearMonthDay;
import org.bouncycastle.jcajce.provider.digest.GOST3411;
import org.primefaces.component.tabview.TabView;
import org.primefaces.event.TabChangeEvent;
// </editor-fold>

@Named("clientController")
@SessionScoped
public class ClientController implements Serializable {

    // <editor-fold defaultstate="collapsed" desc="EJBs">
    @EJB
    private lk.gov.health.phsp.facade.ClientFacade ejbFacade;
    @EJB
    private EncounterFacade encounterFacade;
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Controllers">
    @Inject
    ApplicationController applicationController;
    @Inject
    private WebUserController webUserController;
    @Inject
    private EncounterController encounterController;
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Variables">
    private List<Client> items = null;
    private List<Client> selectedClients = null;
    private Client selected;
    private List<Encounter> selectedClientsClinics;
    private String searchingId;
    private String searchingPhn;
    private String searchingPassportNo;
    private String searchingDrivingLicenceNo;
    private String searchingNicNo;
    private String searchingName;
    private String searchingPhoneNumber;
    private YearMonthDay yearMonthDay;
    private Institution selectedClinic;
    private int profileTabActiveIndex;

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Constructors">
    public ClientController() {
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Navigation">
    public String toSearchClientById() {
        return "/client/search_by_id";
    }

    public String toSearchClientByDetails() {
        return "/client/search_by_details";

    }

    public String toSelectClient() {
        return "/client/select";
    }

    public String toClient() {
        return "/client/client";
    }

    public String toClientProfile() {
        return "/client/profile";
    }

    public String toAddNewClient() {
        selected = new Client();
        yearMonthDay = new YearMonthDay();
        return "/client/client";
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Functions">
    public void onTabChange(TabChangeEvent event) {

        System.out.println("profileTabActiveIndex = " + profileTabActiveIndex);

        TabView tabView = (TabView) event.getComponent();

        profileTabActiveIndex = tabView.getChildren().indexOf(event.getTab());

        System.out.println("profileTabActiveIndex = " + profileTabActiveIndex);

    }

    public List<Encounter> fillEncounters(Client client, InstitutionType insType, EncounterType encType, boolean excludeCompleted) {
        System.out.println("fillEncounters");
        String j = "select e from Encounter e where e.retired=false ";
        Map m = new HashMap();
        if (client != null) {
            j += " and e.client=:c ";
            m.put("c", client);
        }
        if (insType != null) {
            j += " and e.institution.institutionType=:it ";
            m.put("it", insType);
        }
        if (insType != null) {
            j += " and e.encounterType=:et ";
            m.put("et", encType);
        }
        if (excludeCompleted) {
            j += " and e.completed=:com ";
            m.put("com", false);
        }
        System.out.println("m = " + m);
        System.out.println("j = " + j);
        return encounterFacade.findByJpql(j, m);
    }

    public void enrollInClinic() {
        if (selectedClinic == null) {
            JsfUtil.addErrorMessage("Please select an HLC clinic to enroll.");
            return;
        }
        if (selected == null) {
            JsfUtil.addErrorMessage("Please select a client to enroll.");
            return;
        }
        if (encounterController.clinicEnrolmentExists(selectedClinic, selected)) {
            JsfUtil.addErrorMessage("This client is already enrolled.");
            return;
        }
        Encounter encounter = new Encounter();
        encounter.setClient(selected);
        encounter.setEncounterType(EncounterType.Clinic_Enroll);
        encounter.setCreatedAt(new Date());
        encounter.setCreatedBy(webUserController.getLoggedUser());
        encounter.setInstitution(selectedClinic);
        encounter.setEncounterDate(new Date());
        encounter.setEncounterNumber(encounterController.createClinicEnrollNumber(selectedClinic));
        encounter.setCompleted(false);
        encounterFacade.create(encounter);
        JsfUtil.addSuccessMessage(selected.getPerson().getNameWithTitle() + " was Successfully Enrolled in " + selectedClinic.getName() + "\nThe Clinic number is " + encounter.getEncounterNumber());
        selectedClientsClinics = null;
    }

    

    public void generateAndAssignNewPhn() {
        if (selected == null) {
            return;
        }
        Institution poiIns;
        if (webUserController.getLoggedUser().getInstitution() == null) {
            JsfUtil.addErrorMessage("You do not have an Institution. Please contact support.");
            return;
        }
        if (webUserController.getLoggedUser().getInstitution().getPoiInstitution() != null) {
            poiIns = webUserController.getLoggedUser().getInstitution().getPoiInstitution();
        } else {
            poiIns = webUserController.getLoggedUser().getInstitution();
        }
        if (poiIns.getPoiNumber() == null || poiIns.getPoiNumber().trim().equals("")) {
            JsfUtil.addErrorMessage("A Point of Issue is NOT assigned to your Institution. Please discuss with the System Administrator.");
            return;
        }
        selected.setPhn(applicationController.createNewPersonalHealthNumber(webUserController.getLoggedUser().getInstitution()));
    }

    public void gnAreaChanged() {
        if (selected == null) {
            return;
        }
        if (selected.getPerson().getGnArea() != null) {
            selected.getPerson().setDsArea(selected.getPerson().getGnArea().getDsd());
            selected.getPerson().setMohArea(selected.getPerson().getGnArea().getMoh());
            selected.getPerson().setPhmArea(selected.getPerson().getGnArea().getPhm());
            selected.getPerson().setDistrict(selected.getPerson().getGnArea().getDistrict());
            selected.getPerson().setProvince(selected.getPerson().getGnArea().getProvince());
        }
    }

    public void updateYearDateMonth() {
        getYearMonthDay();
        if (selected != null) {
            yearMonthDay.setYear(selected.getPerson().getAgeYears() + "");
            yearMonthDay.setMonth(selected.getPerson().getAgeMonths() + "");
            yearMonthDay.setDay(selected.getPerson().getAgeDays() + "");
        } else {
            yearMonthDay = new YearMonthDay();
        }
    }

    public void yearMonthDateChanged() {
        if (selected == null) {
            return;
        }
        selected.getPerson().setDateOfBirth(guessDob(yearMonthDay));
    }

    public Date guessDob(YearMonthDay yearMonthDay) {
        // ////System.out.println("year string is " + docStr);
        int years = 0;
        int month = 0;
        int day = 0;
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("IST"));
        try {
            if (yearMonthDay.getYear() != null && !yearMonthDay.getYear().isEmpty()) {
                years = Integer.valueOf(yearMonthDay.getYear());
                now.add(Calendar.YEAR, -years);
            }

            if (yearMonthDay.getMonth() != null && !yearMonthDay.getMonth().isEmpty()) {
                month = Integer.valueOf(yearMonthDay.getMonth());
                now.add(Calendar.MONTH, -month);
            }

            if (yearMonthDay.getDay() != null && !yearMonthDay.getDay().isEmpty()) {
                day = Integer.valueOf(yearMonthDay.getDay());
                now.add(Calendar.DATE, -day);
            }

            return now.getTime();
        } catch (Exception e) {
            ////System.out.println("Error is " + e.getMessage());
            return new Date();

        }
    }

    public void addNewPhnNumberToSelectedClient() {
        if (selected == null) {
            JsfUtil.addErrorMessage("No Client is Selected");
            return;
        }
        if (webUserController.getLoggedUser().getInstitution().getPoiNumber().trim().equals("")) {
            JsfUtil.addErrorMessage("No POI is configured for your institution. Please contact support.");
            return;
        }
        selected.setPhn(applicationController.createNewPersonalHealthNumber(webUserController.getLoggedUser().getInstitution()));
    }

    public String searchById() {
        if (searchingPhn != null && !searchingPhn.trim().equals("")) {
            selectedClients = listPatientsByPhn(searchingPhn);
        } else if (searchingNicNo != null && !searchingNicNo.trim().equals("")) {
            selectedClients = listPatientsByNic(searchingNicNo);
        } else if (searchingPhoneNumber != null && !searchingPhoneNumber.trim().equals("")) {
            selectedClients = listPatientsByPhone(searchingPhoneNumber);
        }
        if (selectedClients == null || selectedClients.isEmpty()) {
            JsfUtil.addErrorMessage("No Results Found. Try different search criteria.");
            return "";
        }
        if (selectedClients.size() == 1) {
            selected = selectedClients.get(0);
            selectedClients = null;
            clearSearchById();
            return toClientProfile();
        } else {
            selected = null;
            clearSearchById();
            return toSelectClient();
        }
    }

    public void clearSearchById() {
        searchingId = "";
        searchingPhn = "";
        searchingPassportNo = "";
        searchingDrivingLicenceNo = "";
        searchingNicNo = "";
        searchingName = "";
        searchingPhoneNumber = "";
    }

    public List<Client> listPatientsByPhn(String phn) {
        String j = "select c from Client c where c.retired=false and upper(c.phn)=:q order by c.phn";
        Map m = new HashMap();
        m.put("q", phn.trim().toUpperCase());
        return getFacade().findByJpql(j, m);
    }

    public List<Client> listPatientsByNic(String phn) {
        String j = "select c from Client c where c.retired=false and upper(c.person.nic)=:q order by c.phn";
        Map m = new HashMap();
        m.put("q", phn.trim().toUpperCase());
        return getFacade().findByJpql(j, m);
    }

    public List<Client> listPatientsByPhone(String phn) {
        String j = "select c from Client c where c.retired=false and (upper(c.person.phone1)=:q or upper(c.person.phone2)=:q) order by c.phn";
        Map m = new HashMap();
        m.put("q", phn.trim().toUpperCase());
        return getFacade().findByJpql(j, m);
    }

    public Client prepareCreate() {
        selected = new Client();
        return selected;
    }

    public String saveClient() {
        if (selected == null) {
            JsfUtil.addErrorMessage("No Client Selected to save.");
            return "";
        }
        if (selected.getId() == null) {
            selected.setCreatedBy(webUserController.getLoggedUser());
            selected.setCreatedAt(new Date());
            getFacade().create(selected);
            JsfUtil.addSuccessMessage("New Client Saved.");
        } else {
            selected.setLastEditBy(webUserController.getLoggedUser());
            selected.setLastEditeAt(new Date());
            getFacade().edit(selected);
            JsfUtil.addSuccessMessage("Client Details Updated.");
        }
        return toClientProfile();
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/BundleClinical").getString("ClientCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/BundleClinical").getString("ClientUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/BundleClinical").getString("ClientDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    private void persist(PersistAction persistAction, String successMessage) {
        if (selected != null) {
            try {
                if (persistAction != PersistAction.DELETE) {
                    getFacade().edit(selected);
                } else {
                    getFacade().remove(selected);
                }
                JsfUtil.addSuccessMessage(successMessage);
            } catch (EJBException ex) {
                String msg = "";
                Throwable cause = ex.getCause();
                if (cause != null) {
                    msg = cause.getLocalizedMessage();
                }
                if (msg.length() > 0) {
                    JsfUtil.addErrorMessage(msg);
                } else {
                    JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/BundleClinical").getString("PersistenceErrorOccured"));
                }
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/BundleClinical").getString("PersistenceErrorOccured"));
            }
        }
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Getters & Setters">
    public String getSearchingId() {
        return searchingId;
    }

    public void setSearchingId(String searchingId) {
        this.searchingId = searchingId;
    }

    public String getSearchingPhn() {
        return searchingPhn;
    }

    public void setSearchingPhn(String searchingPhn) {
        this.searchingPhn = searchingPhn;
    }

    public String getSearchingPassportNo() {
        return searchingPassportNo;
    }

    public void setSearchingPassportNo(String searchingPassportNo) {
        this.searchingPassportNo = searchingPassportNo;
    }

    public String getSearchingDrivingLicenceNo() {
        return searchingDrivingLicenceNo;
    }

    public void setSearchingDrivingLicenceNo(String searchingDrivingLicenceNo) {
        this.searchingDrivingLicenceNo = searchingDrivingLicenceNo;
    }

    public String getSearchingNicNo() {
        return searchingNicNo;
    }

    public void setSearchingNicNo(String searchingNicNo) {
        this.searchingNicNo = searchingNicNo;
    }

    public String getSearchingName() {
        return searchingName;
    }

    public void setSearchingName(String searchingName) {
        this.searchingName = searchingName;
    }

    public ClientFacade getEjbFacade() {
        return ejbFacade;
    }

    public ApplicationController getApplicationController() {
        return applicationController;
    }

    public Client getSelected() {
        return selected;
    }

    public void setSelected(Client selected) {
        this.selected = selected;
        updateYearDateMonth();
        selectedClientsClinics = null;
    }

    private ClientFacade getFacade() {
        return ejbFacade;
    }

    public List<Client> getItems() {
        if (items == null) {
            items = getFacade().findAll();
        }
        return items;
    }

    public Client getClient(java.lang.Long id) {
        return getFacade().find(id);
    }

    public List<Client> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<Client> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public String getSearchingPhoneNumber() {
        return searchingPhoneNumber;
    }

    public void setSearchingPhoneNumber(String searchingPhoneNumber) {
        this.searchingPhoneNumber = searchingPhoneNumber;
    }

    public List<Client> getSelectedClients() {
        return selectedClients;
    }

    public void setSelectedClients(List<Client> selectedClients) {
        this.selectedClients = selectedClients;
    }

    public YearMonthDay getYearMonthDay() {
        if (yearMonthDay == null) {
            yearMonthDay = new YearMonthDay();
        }
        return yearMonthDay;
    }

    public void setYearMonthDay(YearMonthDay yearMonthDay) {
        this.yearMonthDay = yearMonthDay;
    }

    public Institution getSelectedClinic() {
        return selectedClinic;
    }

    public void setSelectedClinic(Institution selectedClinic) {
        this.selectedClinic = selectedClinic;
    }

    public List<Encounter> getSelectedClientsClinics() {
        if (selectedClientsClinics == null) {
            selectedClientsClinics = fillEncounters(selected, InstitutionType.Ward_Clinic, EncounterType.Clinic_Enroll, true);
        }
        return selectedClientsClinics;
    }

    public void setSelectedClientsClinics(List<Encounter> selectedClientsClinics) {
        this.selectedClientsClinics = selectedClientsClinics;
    }

    public int getProfileTabActiveIndex() {
        return profileTabActiveIndex;
    }

    public void setProfileTabActiveIndex(int profileTabActiveIndex) {
        this.profileTabActiveIndex = profileTabActiveIndex;
    }

    public EncounterFacade getEncounterFacade() {
        return encounterFacade;
    }

    public EncounterController getEncounterController() {
        return encounterController;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Inner Classes">
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Converters">
    @FacesConverter(forClass = Client.class)
    public static class ClientControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ClientController controller = (ClientController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "clientController");
            return controller.getClient(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Client) {
                Client o = (Client) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Client.class.getName()});
                return null;
            }
        }

    }

    // </editor-fold>
}
