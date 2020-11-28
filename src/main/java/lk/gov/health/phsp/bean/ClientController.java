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
import javax.persistence.TemporalType;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.Encounter;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.entity.Person;
import lk.gov.health.phsp.enums.AreaType;
import lk.gov.health.phsp.enums.EncounterType;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.facade.EncounterFacade;
import lk.gov.health.phsp.pojcs.ClientBasicData;
import lk.gov.health.phsp.pojcs.YearMonthDay;
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
    private InstitutionController institutionController;
    @Inject
    private CommonController commonController;
    @Inject
    private AreaController areaController;
    @Inject
    private ClientEncounterComponentFormSetController clientEncounterComponentFormSetController;
    @Inject
    UserTransactionController userTransactionController;
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Variables">
    private List<Client> items = null;
    private List<ClientBasicData> clients = null;
    private List<Client> selectedClients = null;
    private List<Client> importedClients = null;

    private List<ClientBasicData> selectedClientsBasic = null;

    private Client selected;
    private Long selectedId;
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
    private String searchingLocalReferanceNo;
    private String searchingSsNumber;
    private String uploadDetails;
    private String errorCode;
    private YearMonthDay yearMonthDay;
    private Institution selectedClinic;
    private int profileTabActiveIndex;
    private boolean goingToCaptureWebCamPhoto;
    private UploadedFile file;
    private Date clinicDate;
    private Date from;
    private Date to;

    private Boolean nicExists;
    private Boolean phnExists;
    private Boolean passportExists;
    private Boolean dlExists;
    private Boolean localReferanceExists;
    private Boolean ssNumberExists;
    private String dateTimeFormat;
    private String dateFormat;

    private int intNo;

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
        setSelected(new Client());
        selectedClientsClinics = null;
        selectedClinic = null;
        yearMonthDay = new YearMonthDay();
        userTransactionController.recordTransaction("to add a new client");
        return "/client/client";
    }

    public String toViewCorrectedDuplicates() {
        String j;
        j = "select c"
                + " from Client c "
                + " where c.comments is not null";
        items = getFacade().findByJpql(j);
        return "/systemAdmin/clients_with_corrected_duplicate_phn";
    }

    public String toDetectPhnDuplicates() {
        String j;
        Map m = new HashMap();
        j = "SELECT c.phn "
                + " FROM Client c "
                + " "
                + " GROUP BY c.phn"
                + " HAVING COUNT(c.phn) > 1 ";
        List<String> duplicatedPhnNumbers = getFacade().findString(j);
        items = new ArrayList<>();
        for (String dupPhn : duplicatedPhnNumbers) {
            j = "select c"
                    + " from Client c "
                    + " where c.phn=:phn";
            m = new HashMap();
            m.put("phn", dupPhn);
            List<Client> temClients = getFacade().findByJpql(j, m);
            items.addAll(temClients);
        }
        return "/systemAdmin/clients_with_phn_duplication";
    }

    public String correctPhnDuplicates() {
        String j;
        Map m = new HashMap();
        j = "SELECT c.phn "
                + " FROM Client c "
                + " "
                + " GROUP BY c.phn"
                + " HAVING COUNT(c.phn) > 1 ";
        List<String> duplicatedPhnNumbers = getFacade().findString(j, intNo);
        items = new ArrayList<>();
        for (String dupPhn : duplicatedPhnNumbers) {
            j = "select c"
                    + " from Client c "
                    + " where c.phn=:phn";
            m = new HashMap();
            m.put("phn", dupPhn);
            List<Client> temClients = getFacade().findByJpql(j, m);
            int n = 0;
            for (Client c : temClients) {
                if (n == 0) {

                } else {
                    if (c.getPerson().getLocalReferanceNo() == null || c.getPerson().getLocalReferanceNo().trim().equals("")) {
                        c.setComments("Duplicate PHN. Old PHN Stored as Local Ref");
                        System.out.println("Duplicate PHN. Old PHN Stored as Local Ref");
                        System.out.println("c.getPhn()");
                        c.getPerson().setLocalReferanceNo(c.getPhn());
                        c.setPhn(generateNewPhn(c.getCreateInstitution()));
                    } else if (c.getPerson().getSsNumber() == null || c.getPerson().getSsNumber().trim().equals("")) {
                        c.setComments("Duplicate PHN. Old PHN Stored as SC No");
                        System.out.println("Duplicate PHN. Old PHN Stored as SC No");
                        System.out.println("c.getPhn()");
                        c.getPerson().setSsNumber(c.getPhn());
                        c.setPhn(generateNewPhn(c.getCreateInstitution()));
                    } else {
                    }
                    getFacade().edit(c);
                }
                n++;
            }
            items.addAll(temClients);
        }
        return "/systemAdmin/clients_with_corrected_duplicate_phn";
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Functions">
    public List<Area> getGnAreasForTheSelectedClient(String qry) {
        List<Area> areas = new ArrayList<>();
        if (selected == null) {
            return areas;
        }
        if (selected.getPerson().getDsArea() == null) {
            return areaController.getAreas(AreaType.GN, null, null, qry);
        } else {
            return areaController.getAreas(AreaType.GN, selected.getPerson().getDsArea(), null, qry);
        }
    }

    public void clearExistsValues() {
        phnExists = false;
        nicExists = false;
        passportExists = false;
        dlExists = false;
    }

    public void checkPhnExists() {
        phnExists = null;
        if (selected == null || selected.getPhn() == null || selected.getPhn().trim().equals("")) {
            return;
        }

        phnExists = checkPhnExists(selected.getPhn(), selected);
    }

    public Boolean checkPhnExists(String phn, Client c) {
        Map m = new HashMap();

        String jpql = "select count(c) from Client c "
                + " where c.retired=:ret "
                + " and c.phn=:phn ";

        m.put("ret", false);
        m.put("phn", phn);

        if (c != null && c.getId() != null) {
            jpql += " and c <> :client";
            m.put("client", c);
        }
        Long count = getFacade().countByJpql(jpql, m);
        return !(count == null || count == 0l);
    }

    public void checkNicExists() {
        nicExists = null;
        if (selected == null || selected.getPerson() == null
                || selected.getPerson().getNic() == null
                || selected.getPerson().getNic().trim().equals("")) {
            return;
        }
        nicExists = checkNicExists(selected.getPerson().getNic(), selected);
    }

    public Boolean checkNicExists(String nic, Client c) {
        Map m = new HashMap();

        String jpql = "select count(c) from Client c "
                + " where c.retired=:ret "
                + " and c.person.nic=:nic ";

        m.put("ret", false);
        m.put("nic", nic);

        if (c != null && c.getPerson() != null && c.getPerson().getId() != null) {
            jpql += " and c.person <> :person";
            m.put("person", c.getPerson());
        }
        Long count = getFacade().countByJpql(jpql, m);
        return !(count == null || count == 0l);
    }

    public void fixClientPersonCreatedAt() {
        Map m = new HashMap();

        String j = "select c from Client c where c.retired=:ret ";

        m.put("ret", false);

        List<Client> cs = getFacade().findByJpql(j, m);

        for (Client c : cs) {
            if (c.getCreatedAt() == null && c.getPerson().getCreatedAt() != null) {
                c.setCreatedAt(c.getPerson().getCreatedAt());
                getFacade().edit(c);
            } else if (c.getCreatedAt() != null && c.getPerson().getCreatedAt() == null) {
                c.getPerson().setCreatedAt(c.getCreatedAt());
                getFacade().edit(c);
            } else if (c.getCreatedAt() == null && c.getPerson().getCreatedAt() == null) {
                c.getPerson().setCreatedAt(new Date());
                c.setCreatedAt(new Date());
                getFacade().edit(c);
            }
        }
    }

    public void updateClientCreatedInstitution() {
        Map m = new HashMap();

        if (institution == null) {
            JsfUtil.addErrorMessage("Institution ?");
            return;
        }
        String j = "select c from Client c "
                + " where c.retired=:ret "
                + " and c.id > :idf "
                + " and c.id < :idt ";

        m.put("ret", false);
        m.put("idf", idFrom);
        m.put("idt", idTo);

        List<Client> cs = getFacade().findByJpql(j, m);

        for (Client c : cs) {
            c.setCreateInstitution(institution);
            getFacade().edit(c);
        }
    }

    public void updateClientDateOfBirth() {
        Map m = new HashMap();

        String j = "select c from Client c "
                + " where c.retired=:ret "
                + " and c.id > :idf "
                + " and c.id < :idt ";

        m.put("ret", false);
        m.put("idf", idFrom);
        m.put("idt", idTo);

        List<Client> cs = getFacade().findByJpql(j, m);

        for (Client c : cs) {
            Calendar cd = Calendar.getInstance();

            if (c.getPerson().getDateOfBirth() != null) {
                cd.setTime(c.getPerson().getDateOfBirth());
                int dobYear = cd.get(Calendar.YEAR);

                if (dobYear < 1800) {
                    cd.add(Calendar.YEAR, 2000);
                    c.getPerson().setDateOfBirth(cd.getTime());
                    getFacade().edit(c);
                }
            }
        }
    }

    public Long countOfRegistedClients(Institution ins, Area gn) {
        Map m = new HashMap();

        String j = "select count(c) from Client c "
                + " where c.retired=:ret ";

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
        Map m = new HashMap();

        String j = "select c from Client c "
                + " where c.retired=:ret ";

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

    public String toRegisterdClientsWithDates() {
        Map m = new HashMap();

        String j = "select c from Client c "
                + " where c.retired=:ret ";

        m.put("ret", false);

        if (webUserController.getLoggedUser().getInstitution() != null) {
            j += " and c.createInstitution=:ins ";
            m.put("ins", webUserController.getLoggedUser().getInstitution());
        }
        j = j + " and c.createdAt between :fd and :td ";
        j = j + " order by c.id desc";

        m.put("fd", getFrom());
        m.put("td", getTo());

        items = getFacade().findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/insAdmin/registered_clients";
    }

    public String toRegisterdClientsWithDatesForSystemAdmin() {
        return "/systemAdmin/all_clients";
    }

    public void fillClients() {
        Map m = new HashMap();

        String jpql = "select new lk.gov.health.phsp.pojcs.ClientBasicData("
                + "c.id, "
                + "c.phn, "
                + "c.person.gnArea.name, "
                + "c.createInstitution.name, "
                + "c.person.dateOfBirth, "
                + "c.createdAt, "
                + "c.person.sex.name, "
                + "c.person.nic,"
                + "c.person.name "
                + ") "
                + "from Client c "
                + " where c.retired=:ret "
                + " and c.createdAt between :fd and :td "
                + " order by c.id desc";

        m.put("ret", false);
        m.put("fd", getFrom());
        m.put("td", getTo());

        selectedClientsBasic = null;
        clients = getFacade().findByJpql(jpql, m, TemporalType.TIMESTAMP);
    }

    public void fillRegisterdClientsWithDatesForInstitution() {
        Map m = new HashMap();

        String j = "select c from Client c "
                + " where c.retired<>:ret "
                + " and c.createdAt between :fd and :td ";

        m.put("ret", true);

        if (institution != null) {
            j = j + " and c.createInstitution =:ins ";
            m.put("ins", institution);
        } else {
            j = j + " and c.createInstitution in :ins ";
            m.put("ins", webUserController.getLoggableInstitutions());
        }

        j = j + " order by c.id desc";
        m.put("fd", getFrom());
        m.put("td", getTo());

        selectedClients = null;
        items = getFacade().findByJpql(j, m, TemporalType.TIMESTAMP);
    }

    public void saveSelectedImports() {
        if (institution == null) {
            JsfUtil.addErrorMessage("Institution ?");
            return;
        }
        for (Client c : selectedClients) {
            c.setCreateInstitution(institution);
            if (!checkPhnExists(c.getPhn(), null)) {
                c.setId(null);
                saveClient(c);
            }
        }
    }

    public void fillClientsWithWrongPhnLength() {
        String j = "select c from Client c where length(c.phn) <>11 order by c.id";
        items = getFacade().findByJpql(j);
    }

    public void fillRetiredClients() {
        Map m = new HashMap();

        String j = "select new lk.gov.health.phsp.pojcs.ClientBasicData("
                + "c.id, "
                + "c.phn, "
                + "c.person.gnArea.name, "
                + "c.createInstitution.name, "
                + "c.person.dateOfBirth, "
                + "c.createdAt, "
                + "c.person.sex.name, "
                + "c.person.nic,"
                + "c.person.name "
                + ") "
                + "from Client c "
                + " where c.retired=:ret "
                + " and c.createdAt between :fd and :td "
                + " order by c.id desc";

        m.put("ret", true);
        m.put("fd", getFrom());
        m.put("td", getTo());

        selectedClientsBasic = null;
        clients = getFacade().findByJpql(j, m, TemporalType.TIMESTAMP);
    }

    public String retireSelectedClients() {
        for (ClientBasicData cb : selectedClientsBasic) {

            Client c = getFacade().find(cb.getId());
            if (c == null) {
                continue;
            }

            c.setRetired(true);
            c.setRetireComments("Bulk Delete");
            c.setRetiredAt(new Date());
            c.setRetiredBy(webUserController.getLoggedUser());

            c.getPerson().setRetired(true);
            c.getPerson().setRetireComments("Bulk Delete");
            c.getPerson().setRetiredAt(new Date());
            c.getPerson().setRetiredBy(webUserController.getLoggedUser());

            getFacade().edit(c);
        }
        selectedClients = null;
        return toRegisterdClientsWithDatesForSystemAdmin();
    }

    public String unretireSelectedClients() {
        for (ClientBasicData cb : selectedClientsBasic) {
            Client c = getFacade().find(cb.getId());

            if (c == null) {
                continue;
            };
            
            c.setRetired(false);
            c.setRetireComments("Bulk Un Delete");
            c.setLastEditBy(webUserController.getLoggedUser());
            c.setLastEditeAt(new Date());

            c.getPerson().setRetired(false);
            c.getPerson().setRetireComments("Bulk Un Delete");
            c.getPerson().setEditedAt(new Date());
            c.getPerson().setEditer(webUserController.getLoggedUser());

            getFacade().edit(c);
        }
        selectedClients = null;
        return toRegisterdClientsWithDatesForSystemAdmin();
    }

    public void retireSelectedClient() {
        Client c = selected;
        if (c != null) {
            c.setRetired(true);
            c.setRetiredBy(webUserController.getLoggedUser());
            c.setRetiredAt(new Date());

            c.getPerson().setRetired(true);
            c.getPerson().setRetiredBy(webUserController.getLoggedUser());
            c.getPerson().setRetiredAt(new Date());

            getFacade().edit(c);
        }
    }

    public void saveAllImports() {
        if (institution == null) {
            JsfUtil.addErrorMessage("Institution ?");
            return;
        }
        for (Client c : importedClients) {
            c.setCreateInstitution(institution);
            if (!checkPhnExists(c.getPhn(), null)) {
                c.setId(null);
                saveClient(c);
            }
        }
    }

//    public boolean phnExists(String phn) {
//        String j = "select c from Client c where c.retired=:ret "
//                + " and c.phn=:phn";
//        Map m = new HashMap();
//        m.put("ret", false);
//        m.put("phn", phn);
//        Client c = getFacade().findFirstByJpql(j, m);
//        if (c == null) {
//            return false;
//        }
//        return true;
//    }
    
    public String importClientsFromExcel() {
        importedClients = new ArrayList<>();
        File inputWorkbook;
        Workbook w;
        Cell cell;
        InputStream in;
        String gnAreaName;
        String gnAreaCode;
        Long temId = 0L;
        String[] cols = uploadDetails.split("\\r?\\n");

        if (!Validate_Parameters()) {
            return "";
        }

        try {
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

                for (int i = 1; i < sheet.getRows(); i++) {
                    gnAreaName = null;
                    gnAreaCode = null;
                    Area gnArea = null;

                    Client c = new Client();
                    Person p = new Person();
                    c.setPerson(p);

                    for (String colName : cols) {
                        cell = sheet.getCell(0, i);

                        switch (colName) {
                            case "client_gn_area_name":
                                gnAreaName = cell.getContents();
                                break;
                            case "client_gn_area_code":
                                gnAreaCode = cell.getContents();
                                break;
                        }
                    }

                    if (gnAreaName != null && gnAreaCode != null) {
                        gnArea = areaController.getGnAreaByNameAndCode(gnAreaName, gnAreaCode);
                    } else if (gnAreaName != null) {
                        gnArea = areaController.getGnAreaByName(gnAreaName);
                    } else if (gnAreaCode != null) {
                        gnArea = areaController.getGnAreaByCode(gnAreaCode);
                    }

                    for (String colName : cols) {
                        Item item = null;
                        Date tdob = null;
                        Date today = new Date();
                        int ageInYears = 0;
                        int birthYear;
                        int thisYear;

                        cell = sheet.getCell(0, i);
                        String cellString = cell.getContents();

                        switch (colName) {
                            case "client_name":
                                c.getPerson().setName(cellString);
                                break;

                            case "client_phn_number":
                                c.setPhn(cellString);
                                break;

                            case "client_sex":
                                if (cellString.toLowerCase().contains("f")) {
                                    item = itemController.findItemByCode("sex_female");
                                } else {
                                    item = itemController.findItemByCode("sex_male");
                                }
                                c.getPerson().setSex(item);
                                break;

                            case "client_citizenship":
                                if (cellString == null) {
                                    item = null;
                                } else if (cellString.toLowerCase().contains("sri")) {
                                    item = itemController.findItemByCode("citizenship_local");
                                } else {
                                    item = itemController.findItemByCode("citizenship_foreign");
                                }
                                c.getPerson().setCitizenship(item);
                                break;

                            case "client_ethnic_group":
                                if (cellString == null || cellString.trim().equals("")) {
                                    item = null;
                                } else if (cellString.equalsIgnoreCase("Sinhala")) {
                                    item = itemController.findItemByCode("sinhalese");
                                } else if (cellString.equalsIgnoreCase("moors")) {
                                    item = itemController.findItemByCode("citizenship_local");
                                } else if (cellString.equalsIgnoreCase("SriLankanTamil")) {
                                    item = itemController.findItemByCode("tamil");
                                } else {
                                    item = itemController.findItemByCode("ethnic_group_other");
                                }
                                c.getPerson().setEthinicGroup(item);
                                break;

                            case "client_religion":
                                if (cellString == null || cellString.trim().equals("")) {
                                    item = null;
                                } else if (cellString.equalsIgnoreCase("Buddhist")) {
                                    item = itemController.findItemByCode("buddhist");
                                } else if (cellString.equalsIgnoreCase("Christian")) {
                                    item = itemController.findItemByCode("christian");
                                } else if (cellString.equalsIgnoreCase("Hindu")) {
                                    item = itemController.findItemByCode("hindu");
                                } else {
                                    item = itemController.findItemByCode("religion_other");
                                }
                                c.getPerson().setReligion(item);
                                break;

                            case "client_marital_status":
                                if (cellString == null || cellString.trim().equals("")) {
                                    item = null;
                                } else if (cellString.equalsIgnoreCase("Married")) {
                                    item = itemController.findItemByCode("married");
                                } else if (cellString.equalsIgnoreCase("Separated")) {
                                    item = itemController.findItemByCode("seperated");
                                } else if (cellString.equalsIgnoreCase("Single")) {
                                    item = itemController.findItemByCode("unmarried");
                                } else {
                                    item = itemController.findItemByCode("marital_status_other");
                                }
                                c.getPerson().setMariatalStatus(item);
                                break;

                            case "client_title":
                                switch (cellString) {
                                    case "Baby":
                                        item = itemController.findItemByCode("baby");
                                        break;
                                    case "Babyof":
                                        item = itemController.findItemByCode("baby_of");
                                        break;
                                    case "Mr":
                                        item = itemController.findItemByCode("mr");
                                        break;
                                    case "Mrs":
                                        item = itemController.findItemByCode("mrs");
                                        break;
                                    case "Ms":
                                        item = itemController.findItemByCode("item");
                                        break;
                                    case "Prof":
                                        item = itemController.findItemByCode("prof");
                                        break;
                                    case "Rev":
                                    case "Thero":
                                        item = itemController.findItemByCode("rev");
                                        break;
                                }
                                c.getPerson().setTitle(item);
                                break;

                            case "client_nic_number":
                                c.getPerson().setNic(cellString);
                                break;

                            case "client_data_of_birth":
                                try {
                                tdob = commonController.dateFromString(cellString, dateFormat);
                                Calendar bc = Calendar.getInstance();
                                bc.setTime(tdob);
                                birthYear = bc.get(Calendar.YEAR);
                                Calendar tc = Calendar.getInstance();
                                thisYear = tc.get(Calendar.YEAR);
                                ageInYears = thisYear - birthYear;

                            } catch (Exception e) {

                            }
                            if (ageInYears < 0) {
                                tdob = today;
                            } else if (ageInYears > 200) {
                                tdob = today;
                            }

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
                                Date reg = commonController.dateFromString(cellString, dateTimeFormat);
                                c.getPerson().setCreatedAt(reg);
                                c.setCreatedAt(reg);
                                break;
                                
                            case "client_gn_area":
                                if (gnArea == null) {
                                    gnArea = areaController.getAreaByCodeIfNotName(cellString, AreaType.GN);
                                }
                                break;
                        }
                    }

                    if (gnArea != null) {
                        c.getPerson().setGnArea(gnArea);
                        c.getPerson().setDsArea(gnArea.getDsd());
                        c.getPerson().setMohArea(gnArea.getMoh());
                        c.getPerson().setPhmArea(gnArea.getPhm());
                        c.getPerson().setDistrict(gnArea.getDistrict());
                        c.getPerson().setProvince(gnArea.getProvince());
                    }
                    c.setCreateInstitution(institution);
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
        TabView tabView = (TabView) event.getComponent();
        profileTabActiveIndex = tabView.getChildren().indexOf(event.getTab());
    }

    public List<Encounter> fillEncounters(Client client, InstitutionType insType, EncounterType encType, boolean excludeCompleted) {
        Map m = new HashMap();
        
        String j = "select e from Encounter e where e.retired=false ";
        
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

        return encounterFacade.findByJpql(j, m);
    }

    public void enrollInClinic() {
        if (selectedClinic == null) {
            JsfUtil.addErrorMessage("Please select an clinic to enroll.");
            return;
        }
        if (selected == null) {
            JsfUtil.addErrorMessage("Please select a client to enroll.");
            return;
        }
        if (selectedClinic.getId() == null) {
            JsfUtil.addErrorMessage("Please select a valid clinic to enroll.");
            return;
        }
        if (selected.getId() == null) {
            JsfUtil.addErrorMessage("Please save the client first before enrolling.");
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
        
        if (clinicDate != null) {
            encounter.setEncounterDate(clinicDate);
        } else {
            encounter.setEncounterDate(new Date());
        }
        
        encounter.setEncounterNumber(encounterController.createClinicEnrollNumber(selectedClinic));
        encounter.setCompleted(false);
        encounterFacade.create(encounter);
        
        JsfUtil.addSuccessMessage(selected.getPerson().getNameWithTitle() + " was Successfully Enrolled in " + selectedClinic.getName() + "\nThe Clinic number is " + encounter.getEncounterNumber());
        selectedClientsClinics = null;
    }

    public void generateAndAssignNewPhn() {
        Institution poiIns;
        
        if (selected == null) {
            return;
        }        
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
        selected.setPhn(applicationController.createNewPersonalHealthNumber(poiIns));

        if (webUserController.getLoggedUser().getInstitution().getPoiInstitution() != null) {
            webUserController.getLoggedUser().getInstitution().setPoiInstitution(institutionController.getInstitutionById(webUserController.getLoggedUser().getInstitution().getPoiInstitution().getId()));
        } else {
            webUserController.getLoggedUser().setInstitution(institutionController.getInstitutionById(webUserController.getLoggedUser().getInstitution().getId()));
        }
    }

    public String generateNewPhn(Institution ins) {
        Institution poiIns;
        
        if (ins == null) {
            return null;
        }        
        if (ins.getPoiInstitution() != null) {
            poiIns = ins.getPoiInstitution();
        } else {
            poiIns = ins;
        }
        if (poiIns.getPoiNumber() == null || poiIns.getPoiNumber().trim().equals("")) {
            return null;
        }
        return applicationController.createNewPersonalHealthNumber(poiIns);
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

    public void selectedClientChanged() {
        clientEncounterComponentFormSetController.setLastFiveClinicVisits(null);
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
        int years;
        int month;
        int day;
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
        } catch (NumberFormatException e) {
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

    public String searchByPhn() {
        selectedClients = listPatientsByPhn(searchingPhn);
        
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

    public String searchByNic() {
        selectedClients = listPatientsByNic(searchingNicNo);
        
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

    public String searchByPhoneNumber() {
        selectedClients = listPatientsByPhone(searchingPhoneNumber);
        
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

    public String searchByPassportNo() {
        selectedClients = listPatientsByPassportNo(searchingPassportNo);
        
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

    public String searchByDrivingLicenseNo() {
        selectedClients = listPatientsByDrivingLicenseNo(searchingDrivingLicenceNo);
        
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

    public String searchByLocalReferanceNo() {
        if (webUserController.getLoggedUser().isSystemAdministrator()) {
            selectedClients = listPatientsByLocalReferanceNoForSystemAdmin(searchingLocalReferanceNo);
        } else {
            selectedClients = listPatientsByLocalReferanceNo(searchingLocalReferanceNo);
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

    public String searchBySsNo() {
        selectedClients = listPatientsBySsNo(searchingSsNumber);
        
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

    public String searchByAllId() {
        selectedClients = new ArrayList<>();
        
        if (searchingPhn != null && !searchingPhn.trim().equals("")) {
            selectedClients.addAll(listPatientsByPhn(searchingPhn));
        }
        if (searchingNicNo != null && !searchingNicNo.trim().equals("")) {
            selectedClients.addAll(listPatientsByNic(searchingNicNo));
        }
        if (searchingPhoneNumber != null && !searchingPhoneNumber.trim().equals("")) {
            selectedClients.addAll(listPatientsByPhone(searchingPhoneNumber));
        }
        if (searchingPassportNo != null && !searchingPassportNo.trim().equals("")) {
            selectedClients.addAll(listPatientsByPassportNo(searchingPassportNo));
        }
        if (searchingDrivingLicenceNo != null && !searchingDrivingLicenceNo.trim().equals("")) {
            selectedClients.addAll(listPatientsByDrivingLicenseNo(searchingDrivingLicenceNo));
        }
        if (searchingLocalReferanceNo != null && !searchingLocalReferanceNo.trim().equals("")) {
            selectedClients.addAll(listPatientsByLocalReferanceNo(searchingLocalReferanceNo));
        }
        if (searchingSsNumber != null && !searchingSsNumber.trim().equals("")) {
            selectedClients.addAll(listPatientsBySsNo(searchingSsNumber));
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

    public String searchById() {
        clearExistsValues();
        
        if (searchingPhn != null && !searchingPhn.trim().equals("")) {
            selectedClients = listPatientsByPhn(searchingPhn);
        } else if (searchingName != null && !searchingName.trim().equals("")) {
            selectedClients = listPatientsByName(searchingName);
        } else if (searchingNicNo != null && !searchingNicNo.trim().equals("")) {
            selectedClients = listPatientsByNic(searchingNicNo);
        } else if (searchingPhoneNumber != null && !searchingPhoneNumber.trim().equals("")) {
            selectedClients = listPatientsByPhone(searchingPhoneNumber);
        } else if (searchingPassportNo != null && !searchingPassportNo.trim().equals("")) {
            selectedClients = listPatientsByPassportNo(searchingPassportNo);
        } else if (searchingDrivingLicenceNo != null && !searchingDrivingLicenceNo.trim().equals("")) {
            selectedClients = listPatientsByDrivingLicenseNo(searchingDrivingLicenceNo);
        } else if (searchingLocalReferanceNo != null && !searchingLocalReferanceNo.trim().equals("")) {
            selectedClients = listPatientsByLocalReferanceNo(searchingLocalReferanceNo);
        } else if (searchingSsNumber != null && !searchingSsNumber.trim().equals("")) {
            selectedClients = listPatientsBySsNo(searchingSsNumber);
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
        clearExistsValues();
        if (searchingId == null) {
            searchingId = "";
        }

        selectedClients = listPatientsByIDsStepvice(searchingId.trim().toUpperCase());

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
        searchingLocalReferanceNo = "";
        searchingSsNumber = "";
    }

    public List<Client> listPatientsByPhn(String phn) {
        String j = "select c from Client c where c.retired=false and upper(c.phn)=:q order by c.phn";
        Map m = new HashMap();
        m.put("q", phn.trim().toUpperCase());
        return getFacade().findByJpql(j, m);
    }

    public List<Client> listPatientsByName(String phn) {
        String j = "select c from Client c where c.retired=false and upper(c.person.name)=:q order by c.phn";
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

    public List<Client> listPatientsByLocalReferanceNo(String refNo) {
        String j = "select c from Client c "
                + " where c.retired=false "
                + " and lower(c.person.localReferanceNo)=:q "
                + " and c.createInstitution=:ins "
                + " order by c.phn";
        Map m = new HashMap();
        m.put("q", refNo.trim().toLowerCase());
        m.put("ins", webUserController.getLoggedUser().getInstitution());
        return getFacade().findByJpql(j, m);
    }

    public List<Client> listPatientsByLocalReferanceNoForSystemAdmin(String refNo) {
        String j = "select c from Client c "
                + " where c.retired=false "
                + " and lower(c.person.localReferanceNo)=:q "
                + " order by c.phn";
        Map m = new HashMap();
        m.put("q", refNo.trim().toLowerCase());
        return getFacade().findByJpql(j, m);
    }

    public List<Client> listPatientsBySsNo(String ssNo) {
        String j = "select c from Client c "
                + " where c.retired=false "
                + " and lower(c.person.ssNumber)=:q "
                + " order by c.phn";
        Map m = new HashMap();
        m.put("q", ssNo.trim().toLowerCase());
        return getFacade().findByJpql(j, m);
    }

    public List<Client> listPatientsByDrivingLicenseNo(String dlNo) {
        String j = "select c from Client c "
                + " where c.retired=false "
                + " and lower(c.person.drivingLicenseNumber)=:q "
                + " order by c.phn";
        Map m = new HashMap();
        m.put("q", dlNo.trim().toLowerCase());
        return getFacade().findByJpql(j, m);
    }

    public List<Client> listPatientsByPassportNo(String passportNo) {
        String j = "select c from Client c "
                + " where c.retired=false "
                + " and lower(c.person.passportNumber)=:q "
                + " order by c.phn";
        Map m = new HashMap();
        m.put("q", passportNo.trim().toLowerCase());
        return getFacade().findByJpql(j, m);
    }

    public List<Client> listPatientsByIDsStepvice(String ids) {
        List<Client> cs;
        String j;
        Map m = new HashMap();
        
        if (ids == null || ids.trim().equals("")) {
            return null;
        } 
        // Need to evaluate contradictory check
        if (ids == null || ids.trim().equals("")) {
            cs = new ArrayList<>();
            return cs;
        }
        
        j = "select c from Client c "
                + " where c.retired=false "
                + " and upper(c.phn)=:q "
                + " order by c.phn";
        
        m.put("q", ids.trim().toUpperCase());

        cs = getFacade().findByJpql(j, m);

        if (cs != null && !cs.isEmpty()) { 
            return cs;
        }

        j = "select c from Client c "
                + " where c.retired=false "
                + " and ("
                + " upper(c.person.phone1)=:q "
                + " or "
                + " upper(c.person.phone2)=:q "
                + " or "
                + " upper(c.person.nic)=:q "
                + " or "
                + " upper(c.person.name)=:q "
                + " ) "
                + " order by c.phn";
        cs = getFacade().findByJpql(j, m);

        if (cs != null && !cs.isEmpty()) {
            return cs;
        }

        j = "select c from Client c "
                + " where c.retired=false "
                + " and ("
                + " c.person.localReferanceNo=:q "
                + " or "
                + " c.person.ssNumber=:q "
                + " ) "
                + " order by c.phn";

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
                + " upper(c.person.name)=:q "
                + " or "
                + " upper(c.phn)=:q "
                + " or "
                + " c.person.localReferanceNo=:q "
                + " or "
                + " c.person.ssNumber=:q "
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
            if (c.getPerson().getCreatedAt() == null) {
                c.getPerson().setCreatedAt(new Date());
            }
            if (c.getPerson().getCreatedBy() == null) {
                c.getPerson().setCreatedBy(webUserController.getLoggedUser());
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
            items = null;    // Invalidate list of items to trigger item-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/BundleClinical").getString("ClientUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/BundleClinical").getString("ClientDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger item-query.
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
        selectedClientChanged();
        selectedClientsClinics = null;
    }

    private ClientFacade getFacade() {
        return ejbFacade;
    }

    public List<Client> getItems() {
//        if (items == null) {
//            items = getFacade().findAll();
//        }
        return items;
    }

    public List<Client> getItems(String jpql, Map m) {
        return getFacade().findByJpql(jpql, m);
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
                    + "client_gn_area_name" + "\n"
                    + "client_gn_area_code" + "\n"
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

    public Boolean getNicExists() {
        return nicExists;
    }

    public void setNicExists(Boolean nicExists) {
        this.nicExists = nicExists;
    }

    public Boolean getPhnExists() {
        return phnExists;
    }

    public void setPhnExists(Boolean phnExists) {
        this.phnExists = phnExists;
    }

    public Boolean getPassportExists() {
        return passportExists;
    }

    public void setPassportExists(Boolean passportExists) {
        this.passportExists = passportExists;
    }

    public Boolean getDlExists() {
        return dlExists;
    }

    public void setDlExists(Boolean dlExists) {
        this.dlExists = dlExists;
    }

    public InstitutionController getInstitutionController() {
        return institutionController;
    }

    public void setInstitutionController(InstitutionController institutionController) {
        this.institutionController = institutionController;
    }

    public Date getFrom() {
        if (from == null) {
            from = commonController.startOfTheDay();
        }
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Date getTo() {
        if (to == null) {
            to = commonController.endOfTheDay();
        }
        return to;
    }

    public void setTo(Date to) {
        this.to = to;

    }

    public String getDateTimeFormat() {
        if (dateTimeFormat == null) {
            dateTimeFormat = "yyyy-MM-dd hh:mm:ss";
        }
        return dateTimeFormat;
    }

    public void setDateTimeFormat(String dateTimeFormat) {
        this.dateTimeFormat = dateTimeFormat;
    }

    public String getDateFormat() {
        if (dateFormat == null) {
            dateFormat = "yyyy/MM/dd";
        }
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getSearchingLocalReferanceNo() {
        return searchingLocalReferanceNo;
    }

    public void setSearchingLocalReferanceNo(String searchingLocalReferanceNo) {
        this.searchingLocalReferanceNo = searchingLocalReferanceNo;
    }

    public String getSearchingSsNumber() {
        return searchingSsNumber;
    }

    public void setSearchingSsNumber(String searchingSsNumber) {
        this.searchingSsNumber = searchingSsNumber;
    }

    public Boolean getLocalReferanceExists() {
        return localReferanceExists;
    }

    public void setLocalReferanceExists(Boolean localReferanceExists) {
        this.localReferanceExists = localReferanceExists;
    }

    public Boolean getSsNumberExists() {
        return ssNumberExists;
    }

    public void setSsNumberExists(Boolean ssNumberExists) {
        this.ssNumberExists = ssNumberExists;
    }

    public ClientEncounterComponentFormSetController getClientEncounterComponentFormSetController() {
        return clientEncounterComponentFormSetController;
    }

    public void setClientEncounterComponentFormSetController(ClientEncounterComponentFormSetController clientEncounterComponentFormSetController) {
        this.clientEncounterComponentFormSetController = clientEncounterComponentFormSetController;
    }

    public Long getSelectedId() {
        return selectedId;
    }

    public void setSelectedId(Long selectedId) {
        selected = getFacade().find(selectedId);
        this.selectedId = selectedId;
    }

    public List<ClientBasicData> getClients() {
        return clients;
    }

    public void setClients(List<ClientBasicData> clients) {
        this.clients = clients;
    }

    public List<ClientBasicData> getSelectedClientsBasic() {
        return selectedClientsBasic;
    }

    public void setSelectedClientsBasic(List<ClientBasicData> selectedClientsBasic) {
        this.selectedClientsBasic = selectedClientsBasic;
    }

    public int getIntNo() {
        return intNo;
    }

    public void setIntNo(int intNo) {
        this.intNo = intNo;
    }

    private boolean Validate_Parameters() {
        if (institution == null) {
            JsfUtil.addErrorMessage("Add Institution");
            return false;
        }

        if (uploadDetails == null || uploadDetails.trim().equals("")) {
            JsfUtil.addErrorMessage("Add Column Names");
            return false;
        }

        String[] cols = uploadDetails.split("\\r?\\n");
        if (cols == null || cols.length < 5) {
            JsfUtil.addErrorMessage("No SUfficient Columns");
            return false;
        }
        return true;
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
