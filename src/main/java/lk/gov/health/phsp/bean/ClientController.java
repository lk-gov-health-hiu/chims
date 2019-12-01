package lk.gov.health.phsp.bean;

// <editor-fold defaultstate="collapsed" desc="Import">
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import lk.gov.health.phsp.entity.Client;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.bean.util.JsfUtil.PersistAction;
import lk.gov.health.phsp.facade.ClientFacade;
import java.io.Serializable;
import java.text.ParseException;
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
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.Encounter;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.entity.Person;
import lk.gov.health.phsp.entity.Relationship;
import lk.gov.health.phsp.enums.AreaType;
import lk.gov.health.phsp.enums.EncounterType;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.enums.RelationshipType;
import lk.gov.health.phsp.facade.EncounterFacade;
import lk.gov.health.phsp.pojcs.YearMonthDay;
import org.bouncycastle.jcajce.provider.digest.GOST3411;
import org.primefaces.component.tabview.TabView;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.UploadedFile;
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
    @Inject
    private ItemController itemController;
    @Inject
    private CommonController commonController;
    @Inject
    private AreaController areaController;
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Variables">
    private List<Client> items = null;
    private List<Client> selectedClients = null;
    private List<Client> importedClients = null;
    private Client selected;
    private Long idFrom;
    private Long idTo;
    private Institution institution;
    private List<Encounter> selectedClientsClinics;
    private String searchingId;
    private String searchingPhn;
    private String searchingPassportNo;
    private String searchingDrivingLicenceNo;
    private String searchingNicNo;
    private String searchingName;
    private String searchingPhoneNumber;
    private String uploadDetails;
    private String errorCode;
    private YearMonthDay yearMonthDay;
    private Institution selectedClinic;
    private int profileTabActiveIndex;
    private boolean goingToCaptureWebCamPhoto;
    private UploadedFile file;
    private Date clinicDate;

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
        selectedClientsClinics = null;
        return "/client/profile";
    }

    public String toAddNewClient() {
        selected = new Client();
        selectedClientsClinics = null;
        selectedClinic = null;
        yearMonthDay = new YearMonthDay();
        return "/client/client";
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Functions">
    public void updateClientCreatedIdFromPersonId() {
        String j = "select c from Client c "
                + " where c.retired=:ret "
                + " and c.createInstitution is null ";
        Map m = new HashMap();
        m.put("ret", false);
        List<Client> cs = getFacade().findByJpql(j, m);
        for (Client c : cs) {
            c.setCreatedAt(c.getPerson().getCreatedAt());
            getFacade().edit(c);
        }

    }

    public void updateClientCreatedInstitution() {
        if (institution == null) {
            JsfUtil.addErrorMessage("Institution ?");
            return;
        }
        String j = "select c from Client c "
                + " where c.retired=:ret "
                + " and c.id > :idf "
                + " and c.id < :idt ";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("idf", idFrom);
        m.put("idt", idTo);
        List<Client> cs = getFacade().findByJpql(j, m);
        for (Client c : cs) {
            c.setCreateInstitution(institution);
            getFacade().edit(c);
        }

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
        return getFacade().countByJpql(j, m);
    }

    public String toRegisterdClientsDemo() {
        String j = "select c from Client c "
                + " where c.retired=:ret ";
        Map m = new HashMap();
        m.put("ret", false);
        if (webUserController.getLoggedUser().getInstitution() != null) {
            j += " and c.createInstitution=:ins ";
            m.put("ins", webUserController.getLoggedUser().getInstitution());
        } else {
            items = new ArrayList<>();
        }

        System.out.println("m = " + m);
        System.out.println("j = " + j);
        items = getFacade().findByJpql(j, m);
        return "/insAdmin/registered_clients";
    }

    public String toRegisterdClients() {
        String j = "select c from Client c "
                + " where c.retired=:ret ";
        Map m = new HashMap();
        m.put("ret", false);
        if (webUserController.getLoggedUser().getInstitution() != null) {
            j += " and c.createInstitution=:ins ";
            m.put("ins", webUserController.getLoggedUser().getInstitution());
        } else {
            items = new ArrayList<>();
        }
        items = getFacade().findByJpql(j, m);
        return "/insAdmin/registered_clients";
    }

    public void saveSelectedImports() {
        if (institution == null) {
            JsfUtil.addErrorMessage("Institution ?");
            return;
        }
        for (Client c : selectedClients) {
            c.setCreateInstitution(institution);
            if (!phnExists(c.getPhn())) {
                c.setId(null);
                saveClient(c);
            }
        }
    }

    public void saveAllImports() {
        if (institution == null) {
            JsfUtil.addErrorMessage("Institution ?");
            return;
        }
        for (Client c : importedClients) {
            c.setCreateInstitution(institution);
            if (!phnExists(c.getPhn())) {
                c.setId(null);
                saveClient(c);
            }
        }
    }

    public boolean phnExists(String phn) {
        String j = "select c from Client c where c.retired=:ret "
                + " and c.phn=:phn";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("phn", phn);
        Client c = getFacade().findFirstByJpql(j, m);
        if (c == null) {
            return false;
        }
        return true;
    }

    public String importClientsFromExcel() {


        importedClients = new ArrayList<>();

        if (uploadDetails == null || uploadDetails.trim().equals("")) {
            JsfUtil.addErrorMessage("Add Column Names");
            return "";
        }

        String[] cols = uploadDetails.split("\\r?\\n");
        if (cols == null || cols.length < 5) {
            JsfUtil.addErrorMessage("No SUfficient Columns");
            return "";
        }

        try {
            File inputWorkbook;
            Workbook w;
            Cell cell;
            InputStream in;
            try {
                in = file.getInputstream();
                File f;
                f = new File(Calendar.getInstance().getTimeInMillis() + file.getFileName());
                FileOutputStream out = new FileOutputStream(f);
                int read = 0;
                byte[] bytes = new byte[1024];
                while ((read = in.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
                in.close();
                out.flush();
                out.close();

                inputWorkbook = new File(f.getAbsolutePath());

                JsfUtil.addSuccessMessage("Excel File Opened");
                w = Workbook.getWorkbook(inputWorkbook);
                Sheet sheet = w.getSheet(0);

                errorCode = "";

                int startRow = 1;

                Long temId = 0L;

                for (int i = startRow; i < sheet.getRows(); i++) {

                    Map m = new HashMap();

                    Client c = new Client();
                    Person p = new Person();
                    c.setPerson(p);

                    int colNo = 0;

                    for (String colName : cols) {
                        cell = sheet.getCell(colNo, i);
                        String cellString = cell.getContents();
                        System.out.println("colName = " + colName);
                        switch (colName) {
                            case "client_name":
                                c.getPerson().setName(cellString);
                                break;
                            case "client_phn_number":
                                c.setPhn(cellString);
                                break;
                            case "client_sex":
                                Item sex;
                                if (cellString.toLowerCase().contains("f")) {
                                    sex = itemController.findItemByCode("sex_female");
                                } else {
                                    sex = itemController.findItemByCode("sex_male");
                                }
                                c.getPerson().setSex(sex);
                                break;
                            case "client_citizenship":
                                Item cs;
                                if (cellString == null) {
                                    cs = null;
                                } else if (cellString.toLowerCase().contains("sri")) {
                                    cs = itemController.findItemByCode("citizenship_local");
                                } else {
                                    cs = itemController.findItemByCode("citizenship_foreign");
                                }
                                c.getPerson().setCitizenship(cs);
                                break;

                            case "client_ethnic_group":
                                Item eg = null;
                                if (cellString == null || cellString.trim().equals("")) {
                                    eg = null;
                                } else if (cellString.equalsIgnoreCase("Sinhala")) {
                                    eg = itemController.findItemByCode("sinhalese");
                                } else if (cellString.equalsIgnoreCase("moors")) {
                                    eg = itemController.findItemByCode("citizenship_local");
                                } else if (cellString.equalsIgnoreCase("SriLankanTamil")) {
                                    eg = itemController.findItemByCode("tamil");
                                } else {
                                    eg = itemController.findItemByCode("ethnic_group_other");;
                                }
                                c.getPerson().setEthinicGroup(eg);
                                break;
                            case "client_religion":
                                Item re = null;
                                if (cellString == null || cellString.trim().equals("")) {
                                    re = null;
                                } else if (cellString.equalsIgnoreCase("Buddhist")) {
                                    re = itemController.findItemByCode("buddhist");
                                } else if (cellString.equalsIgnoreCase("Christian")) {
                                    re = itemController.findItemByCode("christian");
                                } else if (cellString.equalsIgnoreCase("Hindu")) {
                                    re = itemController.findItemByCode("hindu");
                                } else {
                                    re = itemController.findItemByCode("religion_other");;
                                }
                                c.getPerson().setReligion(re);
                                break;
                            case "client_marital_status":
                                Item ms = null;
                                if (cellString == null || cellString.trim().equals("")) {
                                    ms = null;
                                } else if (cellString.equalsIgnoreCase("Married")) {
                                    ms = itemController.findItemByCode("married");
                                } else if (cellString.equalsIgnoreCase("Separated")) {
                                    ms = itemController.findItemByCode("seperated");
                                } else if (cellString.equalsIgnoreCase("Single")) {
                                    ms = itemController.findItemByCode("unmarried");
                                } else {
                                    ms = itemController.findItemByCode("marital_status_other");;
                                }
                                c.getPerson().setMariatalStatus(ms);
                                break;
                            case "client_title":
                                Item title = null;
                                String ts = cellString;
                                switch (ts) {
                                    case "Baby":
                                        title = itemController.findItemByCode("baby");
                                        break;
                                    case "Babyof":
                                        title = itemController.findItemByCode("baby_of");
                                        break;
                                    case "Mr":
                                        title = itemController.findItemByCode("mr");
                                        break;
                                    case "Mrs":
                                        title = itemController.findItemByCode("mrs");
                                        break;
                                    case "Ms":
                                        title = itemController.findItemByCode("ms");
                                        break;
                                    case "Prof":
                                        title = itemController.findItemByCode("prof");
                                        break;
                                    case "Rev":
                                    case "Thero":
                                        title = itemController.findItemByCode("rev");
                                        break;
                                }
                                c.getPerson().setTitle(title);
                                break;
                            case "client_nic_number":
                                c.getPerson().setNic(cellString);
                                break;
                            case "client_data_of_birth":
                                Date tdob = commonController.dateFromString(cellString, "yyyy/MM/dd");
                                c.getPerson().setDateOfBirth(tdob);
                                break;
                            case "client_permanent_address":
                                c.getPerson().setAddress(cellString);
                                break;
                            case "client_current_address":
                                c.getPerson().setAddress(cellString);
                                break;
                            case "client_mobile_number":
                                c.getPerson().setPhone1(cellString);
                                break;
                            case "client_home_number":
                                c.getPerson().setPhone2(cellString);
                                break;
                            case "client_registered_at":
                                Date reg = commonController.dateFromString(cellString, "MM/dd/yyyy hh:mm:ss");
                                c.getPerson().setCreatedAt(reg);
                                c.setCreatedAt(reg);
                                break;
                            case "client_gn_area":
                                Area tgn = areaController.getAreaByName(cellString, AreaType.GN, false, null);
                                if (tgn != null) {
                                    c.getPerson().setGnArea(tgn);
                                    c.getPerson().setDsArea(tgn.getDsd());
                                    c.getPerson().setMohArea(tgn.getMoh());
                                    c.getPerson().setPhmArea(tgn.getPhm());
                                    c.getPerson().setDistrict(tgn.getDistrict());
                                    c.getPerson().setProvince(tgn.getProvince());
                                }
                                break;
                        }

                        colNo++;
                    }

                    c.setId(temId);
                    temId++;

                    importedClients.add(c);

                }

                lk.gov.health.phsp.facade.util.JsfUtil.addSuccessMessage("Succesful. All the data in Excel File Impoted to the database");
                errorCode = "";
                return "save_imported_clients";
            } catch (IOException ex) {
                errorCode = ex.getMessage();
                lk.gov.health.phsp.facade.util.JsfUtil.addErrorMessage(ex.getMessage());
                return "";
            } catch (BiffException ex) {
                lk.gov.health.phsp.facade.util.JsfUtil.addErrorMessage(ex.getMessage());
                errorCode = ex.getMessage();
                return "";
            }
        } catch (IndexOutOfBoundsException e) {
            errorCode = e.getMessage();
            return "";
        }
    }

    public void prepareToCapturePhotoWithWebCam() {
        goingToCaptureWebCamPhoto = true;
    }

    public void finishCapturingPhotoWithWebCam() {
        goingToCaptureWebCamPhoto = false;
    }

    public void onTabChange(TabChangeEvent event) {

        // //System.out.println("profileTabActiveIndex = " + profileTabActiveIndex);
        TabView tabView = (TabView) event.getComponent();

        profileTabActiveIndex = tabView.getChildren().indexOf(event.getTab());

    }

    public List<Encounter> fillEncounters(Client client, InstitutionType insType, EncounterType encType, boolean excludeCompleted) {
        // //System.out.println("fillEncounters");
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
        // //System.out.println("m = " + m);
        return encounterFacade.findByJpql(j, m);
    }

    public void enrollInClinic() {
        System.out.println("selectedClinic = " + selectedClinic);
        if (selectedClinic == null) {
            JsfUtil.addErrorMessage("Please select an clinic to enroll.");
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
        if(clinicDate!=null){
            encounter.setEncounterDate(clinicDate);
        }else{
            encounter.setEncounterDate(new Date());
        }
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
            selected.getPerson().setDobIsAnApproximation(false);
        } else {
            yearMonthDay = new YearMonthDay();
        }
    }

    public void yearMonthDateChanged() {
        if (selected == null) {
            return;
        }
        selected.getPerson().setDobIsAnApproximation(true);
        selected.getPerson().setDateOfBirth(guessDob(yearMonthDay));
    }

    public Date guessDob(YearMonthDay yearMonthDay) {
        // ////// //System.out.println("year string is " + docStr);
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
            ////// //System.out.println("Error is " + e.getMessage());
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

    public String searchByAnyId() {
        if (searchingId == null) {
            searchingId = "";
        }

        selectedClients = listPatientsByIDs(searchingId.trim().toUpperCase());

        if (selectedClients == null || selectedClients.isEmpty()) {
            JsfUtil.addErrorMessage("No Results Found. Try different search criteria.");
            return "/client/search_by_id";
        }
        if (selectedClients.size() == 1) {
            selected = selectedClients.get(0);
            selectedClients = null;
            searchingId = "";
            return toClientProfile();
        } else {
            selected = null;
            searchingId = "";
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

    public List<Client> listPatientsByIDs(String ids) {
        if (ids == null || ids.trim().equals("")) {
            return null;
        }
        String j = "select c from Client c "
                + " where c.retired=false "
                + " and ("
                + " upper(c.person.phone1)=:q "
                + " or "
                + " upper(c.person.phone2)=:q "
                + " or "
                + " upper(c.person.nic)=:q "
                + " or "
                + " upper(c.phn)=:q "
                + " ) "
                + " order by c.phn";
        Map m = new HashMap();
        m.put("q", ids.trim().toUpperCase());
        return getFacade().findByJpql(j, m);
    }

    public Client prepareCreate() {
        selected = new Client();
        return selected;
    }

    public String saveClient() {
        Institution createdIns;
        if (selected.getCreateInstitution() == null) {
            if (webUserController.getLoggedUser().getInstitution().getPoiInstitution() != null) {
                createdIns = webUserController.getLoggedUser().getInstitution().getPoiInstitution();
            } else {
                createdIns = webUserController.getLoggedUser().getInstitution();
            }
            selected.setCreateInstitution(createdIns);
        }
        saveClient(selected);
        JsfUtil.addSuccessMessage("Saved.");
        return toClientProfile();
    }

    public String saveClient(Client c) {
        if (c == null) {
            JsfUtil.addErrorMessage("No Client Selected to save.");
            return "";
        }
        if (c.getId() == null) {
            c.setCreatedBy(webUserController.getLoggedUser());
            if (c.getCreatedAt() == null) {
                c.setCreatedAt(new Date());
            }
            if (c.getCreateInstitution() == null) {
                if (webUserController.getLoggedUser().getInstitution().getPoiInstitution() != null) {
                    c.setCreateInstitution(webUserController.getLoggedUser().getInstitution().getPoiInstitution());
                } else if (webUserController.getLoggedUser().getInstitution() != null) {
                    c.setCreateInstitution(webUserController.getLoggedUser().getInstitution());
                }
            }

            getFacade().create(c);

        } else {
            c.setLastEditBy(webUserController.getLoggedUser());
            c.setLastEditeAt(new Date());
            getFacade().edit(c);
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
            selectedClientsClinics = fillEncounters(selected, InstitutionType.Clinic, EncounterType.Clinic_Enroll, true);
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

    public boolean isGoingToCaptureWebCamPhoto() {
        return goingToCaptureWebCamPhoto;
    }

    public void setGoingToCaptureWebCamPhoto(boolean goingToCaptureWebCamPhoto) {
        this.goingToCaptureWebCamPhoto = goingToCaptureWebCamPhoto;
    }

    public String getUploadDetails() {
        if (uploadDetails == null || uploadDetails.trim().equals("")) {
            uploadDetails
                    = "client_phn_number" + "\n"
                    + "client_nic_number" + "\n"
                    + "client_title" + "\n"
                    + "client_name" + "\n"
                    + "client_sex" + "\n"
                    + "client_data_of_birth" + "\n"
                    + "client_citizenship" + "\n"
                    + "client_ethnic_group" + "\n"
                    + "client_religion" + "\n"
                    + "client_marital_status" + "\n"
                    + "client_permanent_address" + "\n"
                    + "client_gn_area" + "\n"
                    + "client_mobile_number" + "\n"
                    + "client_home_number" + "\n"
                    + "client_email" + "\n"
                    + "client_registered_at" + "\n";
        }

        return uploadDetails;
    }

    public void setUploadDetails(String uploadDetails) {
        this.uploadDetails = uploadDetails;
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public List<Client> getImportedClients() {
        return importedClients;
    }

    public void setImportedClients(List<Client> importedClients) {
        this.importedClients = importedClients;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public ItemController getItemController() {
        return itemController;
    }

    public CommonController getCommonController() {
        return commonController;
    }

    public AreaController getAreaController() {
        return areaController;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Long getIdFrom() {
        return idFrom;
    }

    public void setIdFrom(Long idFrom) {
        this.idFrom = idFrom;
    }
    
    

    public Long getIdTo() {
        return idTo;
    }

    public void setIdTo(Long idTo) {
        this.idTo = idTo;
    }

    public Date getClinicDate() {
        return clinicDate;
    }

    public void setClinicDate(Date clinicDate) {
        this.clinicDate = clinicDate;
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
