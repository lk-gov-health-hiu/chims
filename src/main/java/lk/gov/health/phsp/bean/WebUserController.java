package lk.gov.health.phsp.bean;

import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.entity.Upload;
import lk.gov.health.phsp.enums.UploadType;
import lk.gov.health.phsp.enums.WebUserRole;
import lk.gov.health.phsp.facade.InstitutionFacade;
import lk.gov.health.phsp.facade.ProjectInstitutionFacade;
import lk.gov.health.phsp.facade.ProjectSourceOfFundFacade;
import lk.gov.health.phsp.facade.UploadFacade;
import lk.gov.health.phsp.facade.WebUserFacade;
import lk.gov.health.phsp.facade.util.JsfUtil;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import lk.gov.health.phsp.entity.UserPrivilege;
import lk.gov.health.phsp.enums.Privilege;
import lk.gov.health.phsp.enums.PrivilegeTreeNode;
import lk.gov.health.phsp.facade.UserPrivilegeFacade;
import org.apache.commons.io.IOUtils;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;
import org.primefaces.model.UploadedFile;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;

@Named("webUserController")
@SessionScoped
public class WebUserController implements Serializable {

    /*
    EJBs
     */
    @EJB
    private lk.gov.health.phsp.facade.WebUserFacade ejbFacade;
    @EJB
    private InstitutionFacade institutionFacade;
    @EJB
    private UploadFacade uploadFacade;
    @EJB
    private ProjectInstitutionFacade projectInstitutionFacade;
    @EJB
    private ProjectSourceOfFundFacade projectSourceOfFundFacade;
    @EJB
    private UserPrivilegeFacade userPrivilegeFacade;
    /*
    Controllers
     */
    @Inject
    private CommonController commonController;
    @Inject
    private AreaController areaController;
    @Inject
    private InstitutionController institutionController;
    @Inject
    private ItemController itemController;

    /*
    Variables
     */
    private List<WebUser> items = null;
    private List<Upload> currentProjectUploads;
    private List<Upload> clientUploads;
    private List<Upload> companyUploads;

    private List<Institution> loggableInstitutions;

    private Area selectedProvince;
    private Area selectedDistrict;
    private Area selectedDsArea;
    private Area selectedGnArea;
    private Institution selectedLocation;
    private Item selectedSourceOfFund;
    private Double selectedFundValue;
    private Item selectedFundUnit;
    private String selectedFundComments;

    private List<Area> districtsAvailableForSelection;

    private List<Area> selectedDsAreas;
    private List<Area> selectedGnAreas;
    private Area[] selectedProvinces;

    private WebUser current;
    private Upload currentUpload;
    private Institution institution;

    private WebUser loggedUser;
    private String userName;
    private String password;
    private String passwordReenter;
    private MapModel emptyModel;
    List<UserPrivilege> loggedUserPrivileges;

    private UploadedFile file;
    private String comments;

    private StreamedContent downloadingFile;

    private Date fromDate;
    private Date toDate;

    private Integer year;
    private Area province;
    private Area district;
    private Institution location;
    private Boolean allIslandProjects;
    private String searchKeyword;

    private String loginRequestResponse;

    private String locale;

    /**
     *
     * Privileges
     *
     */
    private TreeNode allPrivilegeRoot;
    private TreeNode myPrivilegeRoot;
    private TreeNode[] selectedNodes;

    @PostConstruct
    public void init() {
        emptyModel = new DefaultMapModel();
        createAllPrivilege();
    }

    public List<Institution> findAutherizedInstitutions() {
        List<Institution> ins = new ArrayList<>();
        if (loggedUser == null) {
            return ins;
        }
        if (loggedUser.getInstitution() == null) {
            return ins;
        }
        ins.add(loggedUser.getInstitution());
        ins.addAll(institutionController.findChildrenInstitutions(loggedUser.getInstitution()));
        return ins;
    }

    public String toManagePrivileges() {
        System.out.println("toManagePrivileges = " + this);
        if (current == null) {
            JsfUtil.addErrorMessage("Nothing Selected");
            return "";
        }
        selectedNodes = new TreeNode[0];
        List<UserPrivilege> userps = userPrivilegeList(current);
        System.out.println("userps = " + userps);
        for (TreeNode n : allPrivilegeRoot.getChildren()) {
            n.setSelected(false);
            for (TreeNode n1 : n.getChildren()) {
                n1.setSelected(false);
                for (TreeNode n2 : n1.getChildren()) {
                    n2.setSelected(false);
                }
            }
        }
        List<TreeNode> temSelected = new ArrayList<>();
        for (UserPrivilege wup : userps) {
            System.out.println("wup = " + wup.getPrivilege());
            for (TreeNode n : allPrivilegeRoot.getChildren()) {
                if (wup.getPrivilege().equals(((PrivilegeTreeNode) n).getP())) {
                    n.setSelected(true);
                    System.out.println("n = " + n);
                    System.out.println("wup.getPrivilege() = " + wup.getPrivilege());
                    temSelected.add(n);
                }
                for (TreeNode n1 : n.getChildren()) {
                    if (wup.getPrivilege().equals(((PrivilegeTreeNode) n1).getP())) {
                        n1.setSelected(true);
                        System.out.println("n1 = " + n1);
                        System.out.println("wup.getPrivilege() = " + wup.getPrivilege());
                        temSelected.add(n1);
                    }
                    for (TreeNode n2 : n1.getChildren()) {
                        if (wup.getPrivilege().equals(((PrivilegeTreeNode) n2).getP())) {
                            n2.setSelected(true);
                            System.out.println("n2 = " + n2);
                            System.out.println("wup.getPrivilege() = " + wup.getPrivilege());
                            temSelected.add(n2);
                        }
                    }
                }
            }
        }
        selectedNodes = temSelected.toArray(new TreeNode[temSelected.size()]);
        System.out.println("temSelected = " + temSelected);
        System.out.println("selectedNodes = " + Arrays.toString(selectedNodes));
        return "/webUser/privileges";
    }

    private void createAllPrivilege() {
        allPrivilegeRoot = new PrivilegeTreeNode("Root", null);

        TreeNode clientManagement = new PrivilegeTreeNode("Client Management", allPrivilegeRoot, Privilege.Client_Management);
        TreeNode encounterManagement = new PrivilegeTreeNode("Encounter Management", allPrivilegeRoot, Privilege.Encounter_Management);
        TreeNode appointmentManagement = new PrivilegeTreeNode("Appointment Management", allPrivilegeRoot, Privilege.Appointment_Management);
        TreeNode labManagement = new PrivilegeTreeNode("Lab Management", allPrivilegeRoot, Privilege.Lab_Management);
        TreeNode pharmacyManagement = new PrivilegeTreeNode("Pharmacy Management", allPrivilegeRoot, Privilege.Pharmacy_Management);
        TreeNode user = new PrivilegeTreeNode("User", allPrivilegeRoot, Privilege.Manage_Users);
        TreeNode institutionAdministration = new PrivilegeTreeNode("Institution Administration", allPrivilegeRoot, Privilege.Institution_Administration);
        TreeNode systemAdministration = new PrivilegeTreeNode("System Administration", allPrivilegeRoot, Privilege.System_Administration);
        //Client Management

        TreeNode add_Client = new PrivilegeTreeNode("Add_Client", clientManagement, Privilege.Add_Client);
        TreeNode search_any_Client_by_IDs = new PrivilegeTreeNode("Search any Client by IDs", clientManagement, Privilege.Search_any_Client_by_IDs);
        TreeNode search_any_Client_by_Details = new PrivilegeTreeNode("Search any Client by Details", clientManagement, Privilege.Search_any_Client_by_Details);
        TreeNode search_any_client_by_ID_of_Authorised_Areas = new PrivilegeTreeNode("Search any client by ID of Authorised Areas", clientManagement, Privilege.Search_any_client_by_ID_of_Authorised_Areas);
        TreeNode search_any_client_by_Details_of_Authorised_Areas = new PrivilegeTreeNode("Search any client by Details of Authorised Areas", clientManagement, Privilege.Search_any_client_by_Details_of_Authorised_Areas);
        TreeNode search_any_client_by_ID_of_Authorised_Institutions = new PrivilegeTreeNode("Search any client by ID of Authorised Institutions", clientManagement, Privilege.Search_any_client_by_ID_of_Authorised_Institutions);
        TreeNode search_any_client_by_Details_of_Authorised_Institutions = new PrivilegeTreeNode("Search any client by Details of Authorised Institutions", clientManagement, Privilege.Search_any_client_by_Details_of_Authorised_Institutions);

        //Institution Administration
        TreeNode manage_Institution_Users = new PrivilegeTreeNode("Manage Institution Users", institutionAdministration, Privilege.Manage_Institution_Users);
        TreeNode manage_Institution_Metadata = new PrivilegeTreeNode("Manage Institution Metadata", institutionAdministration, Privilege.Manage_Institution_Metadata);
        TreeNode manage_Authorised_Areas = new PrivilegeTreeNode("Manage Authorised Areas", institutionAdministration, Privilege.Manage_Authorised_Areas);
        TreeNode manage_Authorised_Institutions = new PrivilegeTreeNode("Manage Authorised Institutions", institutionAdministration, Privilege.Manage_Authorised_Institutions);
        //System Administration
        TreeNode manage_Users = new PrivilegeTreeNode("Manage Users", systemAdministration, Privilege.Manage_Users);
        TreeNode manage_Metadata = new PrivilegeTreeNode("Manage Metadata", systemAdministration, Privilege.Manage_Metadata);
        TreeNode manage_Area = new PrivilegeTreeNode("Manage Area", systemAdministration, Privilege.Manage_Area);
        TreeNode manage_Institutions = new PrivilegeTreeNode("Manage Institutions", systemAdministration, Privilege.Manage_Institutions);
        TreeNode manage_Forms = new PrivilegeTreeNode("Manage Forms", systemAdministration, Privilege.Manage_Forms);

    }

    public String toChangeMyDetails() {
        if (loggedUser == null) {
            return "";
        }
        current = loggedUser;
        return "/change_my_details";
    }

    public String toChangeMyPassword() {
        if (loggedUser == null) {
            return "";
        }
        password = "";
        passwordReenter = "";
        current = loggedUser;
        return "/change_my_password";
    }

    public void markLocationOnMap() {
        emptyModel = new DefaultMapModel();
        if (current == null) {
            return;
        }
        LatLng coord1 = new LatLng(current.getInstitution().getCoordinate().getLatitude(), current.getInstitution().getCoordinate().getLongitude());
        emptyModel.addOverlay(new Marker(coord1, current.getInstitution().getAddress()));
    }

    public void markLocationOnMapForBidders() {
        emptyModel = new DefaultMapModel();
        if (current == null) {
            return;
        }
        LatLng coord1 = new LatLng(current.getInstitution().getCoordinate().getLatitude(), current.getInstitution().getCoordinate().getLongitude());
        emptyModel.addOverlay(new Marker(coord1, current.getInstitution().getAddress()));
    }

    public String viewMedia() {
        if (currentUpload == null) {
            JsfUtil.addErrorMessage("Nothing is selected to view");
            return "";
        }
        if (currentUpload.getFileType().contains("image")) {
            return "/view_image";
        } else if (currentUpload.getFileType().contains("pdf")) {
            return "/view_pdf";
        } else {
            JsfUtil.addErrorMessage("NOT an image of a pdf file. ");
            return "";
        }
    }

    public String toSubmitClientRequest() {
        return "/finalize_client_request";
    }

    public void sendSubmitClientRequestConfirmationEmail() {

    }

    public void downloadCurrentFile() {
        if (currentUpload == null) {
            return;
        }
        InputStream stream = new ByteArrayInputStream(currentUpload.getBaImage());
        downloadingFile = new DefaultStreamedContent(stream, currentUpload.getFileType(), currentUpload.getFileName());
    }

    public StreamedContent getDownloadingFile() {
        downloadCurrentFile();
        return downloadingFile;
    }

    public String addMarker() {
        Marker marker = new Marker(new LatLng(current.getInstitution().getCoordinate().getLatitude(), current.getInstitution().getCoordinate().getLongitude()), current.getName());
        emptyModel.addOverlay(marker);
        getInstitutionFacade().edit(getCurrent().getInstitution());
        JsfUtil.addSuccessMessage("Location Recorded");
        return "";
    }

    public String prepareRegisterAsClient() {
        current = new WebUser();
        current.setWebUserRole(WebUserRole.Institution_User);

        currentProjectUploads = null;
        companyUploads = null;
        clientUploads = null;
        currentUpload = null;

        return "/register";
    }

    public String registerUser() {
        if (!current.getWebUserPassword().equals(password)) {
            JsfUtil.addErrorMessage("Passwords are not matching. Please retry.");
            return "";
        }
        current.setWebUserRole(WebUserRole.Institution_User);
        try {
            getFacade().create(current);
        } catch (Exception e) {
            JsfUtil.addErrorMessage("Username already taken. Please enter a different username");
            return "";
        }

        setLoggedUser(current);
        JsfUtil.addSuccessMessage("Your Details Added as an institution user. Please contact us for changes");
        return "/index";
    }

    public String logOut() {
        loggedUser = null;
        return "/index";
    }

    public String login() {
        loggableInstitutions = null;
        if (userName == null || userName.trim().equals("")) {
            JsfUtil.addErrorMessage("Please enter a Username");
            return "";
        }
        if (password == null || password.trim().equals("")) {
            JsfUtil.addErrorMessage("Please enter the Password");
            return "";
        }
        if (!isFirstVisit()) {
            if (!checkLogin()) {
                JsfUtil.addErrorMessage("Username/Password Error. Please retry.");
                return "";
            }
        }
        loggedUserPrivileges = userPrivilegeList(loggedUser);
        JsfUtil.addSuccessMessage("Successfully Logged");
        return "/index";
    }

    public String loginForMobile() {
        loginRequestResponse = "";
        if (userName == null || userName.trim().equals("")) {
            loginRequestResponse += "Wrong Isername. Please go back to settings and update.";
            return "/mobile/login_failure";
        }
        if (password == null || password.trim().equals("")) {
            loginRequestResponse += "Wrong Isername. Please go back to settings and update.";
            return "/mobile/login_failure";
        }
        if (!checkLogin()) {
            loginRequestResponse += "Wrong Isername. Please go back to settings and update.";
            return "/mobile/login_failure";
        }
        return "/mobile/index";
    }

    private boolean checkLogin() {
        String temSQL;
        temSQL = "SELECT u FROM WebUser u WHERE lower(u.name)=:userName and u.retired =:ret";
        Map m = new HashMap();
        m.put("userName", userName.trim().toLowerCase());
        m.put("ret", false);
        loggedUser = getFacade().findFirstByJpql(temSQL, m);
        if (loggedUser == null) {
            return false;
        }
        if (commonController.matchPassword(password, loggedUser.getWebUserPassword())) {
            return true;
        } else {
            loggedUser = null;
            return false;
        }

    }

    private boolean isFirstVisit() {
        if (getFacade().count() <= 0) {
            JsfUtil.addSuccessMessage("First Visit");

            Institution ins = new Institution();
            ins.setName("Institution");
            ins.setInstitutionType(InstitutionType.Ministry_of_Health);
            getInstitutionFacade().create(ins);
            WebUser wu = new WebUser();
            wu.getPerson().setName(userName);
            wu.setName(userName);
            String tp = commonController.hash(password);
            wu.setWebUserPassword(tp);
            wu.setInstitution(ins);
            wu.setWebUserRole(WebUserRole.System_Administrator);
            getFacade().create(wu);
            loggedUser = wu;
            addAllWebUserPrivileges(wu);
            itemController.addInitialMetadata();
            return true;
        } else {
            return false;
        }

    }

    List<Privilege> getInitialPrivileges(WebUserRole role) {
        List<Privilege> wups = new ArrayList<>();
        if (role == null) {
            return wups;
        }
        switch (role) {

            case Client:
            case Midwife:
                //Menu
                wups.add(Privilege.Client_Management);
                wups.add(Privilege.Encounter_Management);
                wups.add(Privilege.Appointment_Management);
                wups.add(Privilege.Lab_Management);
                wups.add(Privilege.Pharmacy_Management);
                wups.add(Privilege.User);
                //Client Management
                wups.add(Privilege.Add_Client);
                wups.add(Privilege.Search_any_Client_by_IDs);
                wups.add(Privilege.Search_any_Client_by_Details);
                wups.add(Privilege.Search_any_client_by_ID_of_Authorised_Areas);
                wups.add(Privilege.Search_any_client_by_Details_of_Authorised_Areas);
                wups.add(Privilege.Search_any_client_by_ID_of_Authorised_Institutions);
                wups.add(Privilege.Search_any_client_by_Details_of_Authorised_Institutions);
                break;
            case Nurse:
                //Menu
                wups.add(Privilege.Client_Management);
                wups.add(Privilege.Encounter_Management);
                wups.add(Privilege.Appointment_Management);
                wups.add(Privilege.Lab_Management);
                wups.add(Privilege.Pharmacy_Management);
                wups.add(Privilege.User);
                //Client Management
                wups.add(Privilege.Add_Client);
                wups.add(Privilege.Search_any_Client_by_IDs);
                wups.add(Privilege.Search_any_Client_by_Details);
                wups.add(Privilege.Search_any_client_by_ID_of_Authorised_Areas);
                wups.add(Privilege.Search_any_client_by_Details_of_Authorised_Areas);
                wups.add(Privilege.Search_any_client_by_ID_of_Authorised_Institutions);
                wups.add(Privilege.Search_any_client_by_Details_of_Authorised_Institutions);
                break;
            case Doctor:
                //Menu
                wups.add(Privilege.Client_Management);
                wups.add(Privilege.Encounter_Management);
                wups.add(Privilege.Appointment_Management);
                wups.add(Privilege.Lab_Management);
                wups.add(Privilege.Pharmacy_Management);
                wups.add(Privilege.User);
                //Client Management
                wups.add(Privilege.Add_Client);
                wups.add(Privilege.Search_any_Client_by_IDs);
                wups.add(Privilege.Search_any_Client_by_Details);
                wups.add(Privilege.Search_any_client_by_ID_of_Authorised_Areas);
                wups.add(Privilege.Search_any_client_by_Details_of_Authorised_Areas);
                wups.add(Privilege.Search_any_client_by_ID_of_Authorised_Institutions);
                wups.add(Privilege.Search_any_client_by_Details_of_Authorised_Institutions);
                break;
            case User:

            case Institution_Administrator:
                //Menu
                wups.add(Privilege.User);
                wups.add(Privilege.Institution_Administration);
                //Institution Administration
                wups.add(Privilege.Manage_Institution_Users);
                wups.add(Privilege.Manage_Institution_Metadata);
                wups.add(Privilege.Manage_Authorised_Areas);
                wups.add(Privilege.Manage_Authorised_Institutions);
                break;

            case Institution_Super_User:
                //Menu
                wups.add(Privilege.User);
                wups.add(Privilege.Institution_Administration);
                //Institution Administration
                wups.add(Privilege.Manage_Institution_Metadata);
                wups.add(Privilege.Manage_Authorised_Areas);
                wups.add(Privilege.Manage_Authorised_Institutions);
                break;
            case Institution_User:
                //Menu
                wups.add(Privilege.Client_Management);
                wups.add(Privilege.Encounter_Management);
                wups.add(Privilege.Appointment_Management);
                wups.add(Privilege.Lab_Management);
                wups.add(Privilege.Pharmacy_Management);
                wups.add(Privilege.User);
                //Client Management
                wups.add(Privilege.Add_Client);
                wups.add(Privilege.Search_any_Client_by_IDs);
                wups.add(Privilege.Search_any_Client_by_Details);
                wups.add(Privilege.Search_any_client_by_ID_of_Authorised_Areas);
                wups.add(Privilege.Search_any_client_by_Details_of_Authorised_Areas);
                wups.add(Privilege.Search_any_client_by_ID_of_Authorised_Institutions);
                wups.add(Privilege.Search_any_client_by_Details_of_Authorised_Institutions);
                break;
            case Me_Admin:
                wups.add(Privilege.User);
                break;
            case Me_Super_User:
                wups.add(Privilege.User);
                break;
            case Me_User:
                wups.add(Privilege.User);
                break;
            case Super_User:
                wups.add(Privilege.User);
                wups.add(Privilege.System_Administration);
                //System Administration
                wups.add(Privilege.Manage_Metadata);
                wups.add(Privilege.Manage_Area);
                wups.add(Privilege.Manage_Institutions);
                wups.add(Privilege.Manage_Forms);
                break;
            case System_Administrator:
                //Menu
                wups.add(Privilege.Client_Management);
                wups.add(Privilege.Encounter_Management);
                wups.add(Privilege.Appointment_Management);
                wups.add(Privilege.Lab_Management);
                wups.add(Privilege.Pharmacy_Management);
                wups.add(Privilege.User);
                wups.add(Privilege.Institution_Administration);
                wups.add(Privilege.System_Administration);
                //Client Management
                wups.add(Privilege.Add_Client);
                wups.add(Privilege.Search_any_Client_by_IDs);
                wups.add(Privilege.Search_any_Client_by_Details);
                wups.add(Privilege.Search_any_client_by_ID_of_Authorised_Areas);
                wups.add(Privilege.Search_any_client_by_Details_of_Authorised_Areas);
                wups.add(Privilege.Search_any_client_by_ID_of_Authorised_Institutions);
                wups.add(Privilege.Search_any_client_by_Details_of_Authorised_Institutions);
                //Institution Administration
                wups.add(Privilege.Manage_Institution_Users);
                wups.add(Privilege.Manage_Institution_Metadata);
                wups.add(Privilege.Manage_Authorised_Areas);
                wups.add(Privilege.Manage_Authorised_Institutions);
                //System Administration
                wups.add(Privilege.Manage_Users);
                wups.add(Privilege.Manage_Metadata);
                wups.add(Privilege.Manage_Area);
                wups.add(Privilege.Manage_Institutions);
                wups.add(Privilege.Manage_Forms);
                break;
        }

        return wups;
    }

    public void addAllWebUserPrivileges(WebUser u) {
        List<Privilege> ps = Arrays.asList(Privilege.values());
        addWebUserPrivileges(u, ps);
    }

    public void addWebUserPrivileges(WebUser u, List<Privilege> ps) {
        for (Privilege p : ps) {
            addWebUserPrivileges(u, p);
        }
    }

    public void addWebUserPrivileges(WebUser u, Privilege p) {
        String j = "Select up from UserPrivilege up where "
                + " up.webUser=:u and up.privilege=:p "
                + " order by up.id desc";
        Map m = new HashMap();
        m.put("u", u);
        m.put("p", p);
        UserPrivilege up = getUserPrivilegeFacade().findFirstByJpql(j, m);
        if (up == null) {
            up = new UserPrivilege();
            up.setCreatedAt(new Date());
            up.setCreatedBy(loggedUser);
            up.setWebUser(u);
            up.setPrivilege(p);
            getUserPrivilegeFacade().create(up);
        } else {
            up.setRetired(false);
            up.setCreatedAt(new Date());
            up.setCreatedBy(loggedUser);
            up.setWebUser(u);
            up.setPrivilege(p);

            getUserPrivilegeFacade().edit(up);
        }
    }

    public boolean hasPrivilege(String privilege) {
        Privilege p;
        try {
            p = Privilege.valueOf(privilege);
            if (p != null) {
                return hasPrivilege(p);
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hasPrivilege(Privilege p) {
        return hasPrivilege(loggedUserPrivileges, p);
    }

    public boolean hasPrivilege(List<UserPrivilege> ups, Privilege p) {
        boolean f = false;
        for (UserPrivilege up : ups) {
            if (up.getPrivilege().equals(p)) {
                f = true;
            }
        }
        return f;
    }

    public List<UserPrivilege> userPrivilegeList(WebUser u) {
        return userPrivilegeList(u, null);
    }

    public List<UserPrivilege> userPrivilegeList(Item i) {
        return userPrivilegeList(null, i);
    }

    public List<UserPrivilege> userPrivilegeList(WebUser u, Item i) {
        String j = "select p from UserPrivilege p "
                + " where p.retired=false ";
        Map m = new HashMap();
        if (u != null) {
            j += " and p.webUser=:u ";
            m.put("u", u);
        }
        if (i != null) {
            j += " and p.item=:i ";
            m.put("i", i);
        }
        return getUserPrivilegeFacade().findByJpql(j, m);
    }

    public WebUserController() {
    }

    public WebUser getSelected() {
        return current;
    }

    private WebUserFacade getFacade() {
        return ejbFacade;
    }

    public String prepareList() {
        recreateModel();
        return "manage_users";
    }

    public String prepareView() {
        return "/webUser/View";
    }

    public String prepareCreate() {
        current = new WebUser();
        password = "";
        passwordReenter = "";
        return "/webUser/Create";
        //970224568

    }

    public String create() {
        if (!password.equals(passwordReenter)) {
            JsfUtil.addErrorMessage("Passwords do NOT match");
            return "";
        }
        try {
            current.setWebUserPassword(commonController.hash(password));
            current.setCreatedAt(new Date());
            current.setCreater(loggedUser);
            getFacade().create(current);
            addWebUserPrivileges(current, getInitialPrivileges(current.getWebUserRole()));
            JsfUtil.addSuccessMessage(("A new User Created Successfully."));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ("Error Occured. Please change username and try again."));
            return "";
        }
        return "index";
    }

    public String prepareEdit() {
        return "Edit";
    }

    public String prepareEditPassword() {
        return "Password";
    }

    public String update() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(("Updated"));
            return "manage_users";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, e.getMessage());
            return null;
        }
    }

    public String updateUserPrivileges() {

        if (current == null) {
            JsfUtil.addErrorMessage("Please select a user");
            return "";
        }
        System.out.println("selectedNodes = " + Arrays.toString(selectedNodes));
        System.out.println("selectedNodes.length = " + selectedNodes.length);
        List<UserPrivilege> userps = userPrivilegeList(current);
        List<Privilege> tps = new ArrayList<>();
        if (selectedNodes != null && selectedNodes.length > 0) {
            for (TreeNode node : selectedNodes) {
                Privilege p;
                p = ((PrivilegeTreeNode) node).getP();
                System.out.println("p = " + p);
                if (p != null) {
                    tps.add(p);
                }
            }
        }
        System.out.println("tps = " + tps);
        for (Privilege p : tps) {
            boolean found = false;
            for (UserPrivilege tup : userps) {
                System.out.println("tup = " + tup);
                System.out.println("p = " + p);
                if (p != null && tup.getPrivilege() != null && p.equals(tup.getPrivilege())) {
                    found = true;
                }
            }
            if (!found) {
                addWebUserPrivileges(current, p);
            }
        }

        userps = userPrivilegeList(current);

        for (UserPrivilege tup : userps) {
            boolean found = false;
            for (Privilege p : tps) {
                if (p != null && tup.getPrivilege() != null && p.equals(tup.getPrivilege())) {
                    found = true;
                }
            }
            if (!found) {
                tup.setRetired(true);
                tup.setRetiredAt(new Date());
                tup.setRetiredBy(loggedUser);
                getUserPrivilegeFacade().edit(tup);
            }
        }
        return "/webUser/manage_users";
    }

    public String updateMyDetails() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(("Your details Updated."));
            return "/index";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, e.getMessage());
            return null;
        }
    }

    public String updateMyPassword() {
        current = loggedUser;
        if (current == null) {
            JsfUtil.addSuccessMessage(("Error. No Logged User"));
            return "";
        }

        if (!password.equals(passwordReenter)) {
            JsfUtil.addSuccessMessage(("Password Mismatch."));
            return "";
        }
        current.setWebUserPassword(commonController.hash(password));
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(("Password Updated"));
            password = "";
            passwordReenter = "";
            return "/index";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, e.getMessage());
            return "";
        }
    }

    public void updateLoggedUser() {
        if (loggedUser == null) {
            return;
        }
        try {
            getFacade().edit(loggedUser);
            JsfUtil.addSuccessMessage(("Updated"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, e.getMessage());
        }
    }

    public String updatePassword() {
        if (!password.equals(passwordReenter)) {
            JsfUtil.addErrorMessage("Passwords do NOT match.");
            return "";
        }
        try {
            String hashedPassword = commonController.hash(password);
            current.setWebUserPassword(hashedPassword);
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(("Password Changed."));
            return "index";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        performDestroy();
        recreateModel();
        return "manage_users";
    }

    private void performDestroy() {
        try {
            getFacade().remove(current);
            JsfUtil.addSuccessMessage(("WebUserDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ("PersistenceErrorOccured"));
        }
    }

    public List<WebUser> getItems() {
        if (items == null) {
            items = getFacade().findAll();
        }
        return items;
    }

    private void recreateModel() {
        items = null;
    }

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), true);
    }

    public WebUser getWebUser(java.lang.Long id) {
        return ejbFacade.find(id);
    }

    public InstitutionFacade getInstitutionFacade() {
        return institutionFacade;
    }

    public void setInstitutionFacade(InstitutionFacade institutionFacade) {
        this.institutionFacade = institutionFacade;
    }

    public WebUser getLoggedUser() {
        return loggedUser;
    }

    public void setLoggedUser(WebUser loggedUser) {
        this.loggedUser = loggedUser;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public lk.gov.health.phsp.facade.WebUserFacade getEjbFacade() {
        return ejbFacade;
    }

    public void setEjbFacade(lk.gov.health.phsp.facade.WebUserFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    public WebUser getCurrent() {
        return current;
    }

    public void setCurrent(WebUser current) {
        this.current = current;
    }

    public CommonController getCommonController() {
        return commonController;
    }

    public MapModel getEmptyModel() {
        return emptyModel;
    }

    public void setEmptyModel(MapModel emptyModel) {
        this.emptyModel = emptyModel;
    }

    public UploadFacade getUploadFacade() {
        return uploadFacade;
    }

    public void setUploadFacade(UploadFacade uploadFacade) {
        this.uploadFacade = uploadFacade;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Upload getCurrentUpload() {
        return currentUpload;
    }

    public void setCurrentUpload(Upload currentUpload) {
        this.currentUpload = currentUpload;
    }

    public void setCurrentProjectUploads(List<Upload> currentProjectUploads) {
        this.currentProjectUploads = currentProjectUploads;
    }

    public Date getFromDate() {
        if (fromDate == null) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.MONTH, 0);
            c.set(Calendar.DATE, 1);
            fromDate = c.getTime();
        }
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = new Date();
        }
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public void setClientUploads(List<Upload> clientUploads) {
        this.clientUploads = clientUploads;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Area[] getSelectedProvinces() {
        return selectedProvinces;
    }

    public void setSelectedProvinces(Area[] selectedProvinces) {
        this.selectedProvinces = selectedProvinces;
    }

    public void setSelectedDsAreas(List<Area> selectedDsAreas) {
        this.selectedDsAreas = selectedDsAreas;
    }

    public List<Area> getSelectedGnAreas() {
        return selectedGnAreas;
    }

    public void setSelectedGnAreas(List<Area> selectedGnAreas) {
        if (selectedGnAreas == null) {
            selectedGnAreas = new ArrayList<>();
        }
        this.selectedGnAreas = selectedGnAreas;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Area getProvince() {
        return province;
    }

    public void setProvince(Area province) {
        this.province = province;
    }

    public Area getDistrict() {
        return district;
    }

    public void setDistrict(Area district) {
        this.district = district;
    }

    public Institution getLocation() {
        return location;
    }

    public void setLocation(Institution location) {
        this.location = location;
    }

    public Boolean getAllIslandProjects() {
        return allIslandProjects;
    }

    public void setAllIslandProjects(Boolean allIslandProjects) {
        this.allIslandProjects = allIslandProjects;
    }

    public String getSearchKeyword() {
        return searchKeyword;
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    public String getLoginRequestResponse() {
        return loginRequestResponse;
    }

    public void setLoginRequestResponse(String loginRequestResponse) {
        this.loginRequestResponse = loginRequestResponse;
    }

    public String getLocale() {
        if (loggedUser != null) {
            locale = loggedUser.getDefLocale();
        }
        if (locale == null || locale.trim().equals("")) {
            locale = "en";
        }
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public AreaController getAreaController() {
        return areaController;
    }

    public InstitutionController getInstitutionController() {
        return institutionController;
    }

    public ItemController getItemController() {
        return itemController;
    }

    public String getPasswordReenter() {
        return passwordReenter;
    }

    public void setPasswordReenter(String passwordReenter) {
        this.passwordReenter = passwordReenter;
    }

    public Area getSelectedProvince() {
        return selectedProvince;
    }

    public void setSelectedProvince(Area selectedProvince) {
        this.selectedProvince = selectedProvince;
    }

    public Area getSelectedDistrict() {
        return selectedDistrict;
    }

    public void setSelectedDistrict(Area selectedDistrict) {
        this.selectedDistrict = selectedDistrict;
    }

    public Area getSelectedDsArea() {
        return selectedDsArea;
    }

    public void setSelectedDsArea(Area selectedDsArea) {
        this.selectedDsArea = selectedDsArea;
    }

    public Area getSelectedGnArea() {
        return selectedGnArea;
    }

    public void setSelectedGnArea(Area selectedGnArea) {
        this.selectedGnArea = selectedGnArea;
    }

    public Institution getSelectedLocation() {
        return selectedLocation;
    }

    public void setSelectedLocation(Institution selectedLocation) {
        this.selectedLocation = selectedLocation;
    }

    public Item getSelectedSourceOfFund() {
        return selectedSourceOfFund;
    }

    public void setSelectedSourceOfFund(Item selectedSourceOfFund) {
        this.selectedSourceOfFund = selectedSourceOfFund;
    }

    public Double getSelectedFundValue() {
        return selectedFundValue;
    }

    public void setSelectedFundValue(Double selectedFundValue) {
        this.selectedFundValue = selectedFundValue;
    }

    public Item getSelectedFundUnit() {
        return selectedFundUnit;
    }

    public void setSelectedFundUnit(Item selectedFundUnit) {
        this.selectedFundUnit = selectedFundUnit;
    }

    public String getSelectedFundComments() {
        return selectedFundComments;
    }

    public void setSelectedFundComments(String selectedFundComments) {
        this.selectedFundComments = selectedFundComments;
    }

    public ProjectSourceOfFundFacade getProjectSourceOfFundFacade() {
        return projectSourceOfFundFacade;
    }

    public ProjectInstitutionFacade getProjectInstitutionFacade() {
        return projectInstitutionFacade;
    }

    public TreeNode getAllPrivilegeRoot() {
        return allPrivilegeRoot;
    }

    public void setAllPrivilegeRoot(TreeNode allPrivilegeRoot) {
        this.allPrivilegeRoot = allPrivilegeRoot;
    }

    public TreeNode[] getSelectedNodes() {
        return selectedNodes;
    }

    public void setSelectedNodes(TreeNode[] selectedNodes) {
        this.selectedNodes = selectedNodes;
    }

    public TreeNode getMyPrivilegeRoot() {
        return myPrivilegeRoot;
    }

    public void setMyPrivilegeRoot(TreeNode myPrivilegeRoot) {
        this.myPrivilegeRoot = myPrivilegeRoot;
    }

    public UserPrivilegeFacade getUserPrivilegeFacade() {
        return userPrivilegeFacade;
    }

    public List<Upload> getCompanyUploads() {
        return companyUploads;
    }

    public void setCompanyUploads(List<Upload> companyUploads) {
        this.companyUploads = companyUploads;
    }

    public List<Area> getDistrictsAvailableForSelection() {
        return districtsAvailableForSelection;
    }

    public void setDistrictsAvailableForSelection(List<Area> districtsAvailableForSelection) {
        this.districtsAvailableForSelection = districtsAvailableForSelection;
    }

    public List<UserPrivilege> getLoggedUserPrivileges() {
        return loggedUserPrivileges;
    }

    public void setLoggedUserPrivileges(List<UserPrivilege> loggedUserPrivileges) {
        this.loggedUserPrivileges = loggedUserPrivileges;
    }

    public List<Institution> getLoggableInstitutions() {
        if (loggableInstitutions == null) {
            loggableInstitutions = findAutherizedInstitutions();
        }
        return loggableInstitutions;
    }

    public void setLoggableInstitutions(List<Institution> loggableInstitutions) {
        this.loggableInstitutions = loggableInstitutions;
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    @FacesConverter(forClass = WebUser.class)
    public static class WebUserControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            WebUserController controller = (WebUserController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "webUserController");
            return controller.getWebUser(getKey(value));
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
            if (object instanceof WebUser) {
                WebUser o = (WebUser) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + WebUser.class.getName());
            }
        }

    }

}
