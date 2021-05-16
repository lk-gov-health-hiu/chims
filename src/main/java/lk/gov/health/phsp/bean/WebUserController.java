package lk.gov.health.phsp.bean;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.entity.Upload;
import lk.gov.health.phsp.enums.WebUserRole;
import lk.gov.health.phsp.facade.InstitutionFacade;
import lk.gov.health.phsp.facade.ProjectInstitutionFacade;
import lk.gov.health.phsp.facade.ProjectSourceOfFundFacade;
import lk.gov.health.phsp.facade.UploadFacade;
import lk.gov.health.phsp.facade.WebUserFacade;
import lk.gov.health.phsp.facade.util.JsfUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import lk.gov.health.phsp.entity.UserPrivilege;
import lk.gov.health.phsp.enums.Privilege;
import lk.gov.health.phsp.enums.PrivilegeTreeNode;
import lk.gov.health.phsp.facade.UserPrivilegeFacade;
import org.glassfish.jersey.client.ClientResponse;
import org.json.simple.parser.JSONParser;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;
import org.primefaces.model.UploadedFile;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;

@Named
@SessionScoped
public class WebUserController implements Serializable {

    /*
    EJBs
     */
    @EJB
    private WebUserFacade ejbFacade;
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
    @Inject
    private ClientController clientController;
    @Inject
    private EncounterController encounterController;
    @Inject
    ExcelReportController reportController;
    @Inject
    private UserTransactionController userTransactionController;
    @Inject
    private ApplicationController applicationController;
    @Inject
    InstitutionApplicationController institutionApplicationController;
    @Inject
    WebUserApplicationController webUserApplicationController;
    /*
    Variables
     */
    private List<WebUser> items = null;
    private List<Upload> currentProjectUploads;
    private List<Upload> clientUploads;
    private List<Upload> companyUploads;

    private List<Institution> loggableInstitutions;
    private List<Institution> loggablePmcis;
    private List<Area> loggableGnAreas;

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

    private WebUserRole assumedRole;
    private Institution assumedInstitution;
    private Area assumedArea;
    private List<UserPrivilege> assumedPrivileges;

    int reportTabIndex;
    private int indicatorTabIndex;

    private String ipAddress;

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
        findIpAddress();
    }

    @PreDestroy
    public void sessionDestroy() {
        userTransactionController.recordTransaction("Invalidating the Session", this.toString());
    }

    private void findIpAddress() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }

    }

    public String assumeUser() {
        if (current == null) {
            JsfUtil.addErrorMessage("Please select a User");
            return "";
        }
        assumedArea = current.getArea();
        assumedInstitution = current.getInstitution();
        assumedRole = current.getWebUserRole();
        assumedPrivileges = userPrivilegeList(current);
        userTransactionController.recordTransaction("assume User");
        return assumeRoles();

    }

    public String assumeRoles() {
        if (assumedRole == null) {
            JsfUtil.addErrorMessage("Please select a Role");
            userTransactionController.recordTransaction("Assume Roles");
            return "";
        }

        if (assumedInstitution == null) {
            JsfUtil.addErrorMessage("Please lsect an Institution");
            return "";
        }
//        if (assumedArea == null) {
//            JsfUtil.addErrorMessage("Please select an area");
//            return "";
//        }
        if (assumedPrivileges == null) {
            assumedPrivileges = generateAssumedPrivileges(loggedUser, getInitialPrivileges(assumedRole));
        }
        WebUser twu = loggedUser;
        logOut();
        userName = twu.getName();
        loggedUser = twu;
        loggedUser.setAssumedArea(assumedArea);
        loggedUser.setAssumedInstitution(assumedInstitution);
        loggedUser.setAssumedRole(assumedRole);
        loggedUserPrivileges = assumedPrivileges;
        return login(true);
    }

    public void assumedInstitutionChanged() {
        if (assumedInstitution != null) {
            assumedArea = assumedInstitution.getDistrict();
        }
    }

    public String endAssumingRoles() {
        assumedRole = null;
        assumedInstitution = null;
        assumedArea = null;
        assumedPrivileges = null;
        logOut();
        userTransactionController.recordTransaction("End Assuming Roles");
        return login(true);
    }

    public List<Area> findAutherizedGnAreas() {
        List<Area> gns = new ArrayList<>();
        if (loggedUser == null) {
            return gns;
        }
        if (getLoggablePmcis() == null) {
            return gns;
        }
        for (Institution i : getLoggablePmcis()) {
            gns.addAll(institutionController.findDrainingGnAreas(i));
        }
        return gns;
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
        ins.addAll(institutionApplicationController.findChildrenInstitutions(loggedUser.getInstitution()));
        return ins;
    }

    public List<Institution> findAutherizedPmcis() {
        List<Institution> ins = new ArrayList<>();
        if (loggedUser == null) {
            return ins;
        }
        if (loggedUser.getInstitution() == null) {
            return ins;
        }
        if (loggedUser.getInstitution().isPmci()) {
            ins.add(loggedUser.getInstitution());
        }
        ins.addAll(institutionController.findChildrenPmcis(loggedUser.getInstitution()));
        return ins;
    }

    public String toManageInstitutionUsers() {
        String j = "select u from WebUser u "
                + " where u.retired=false "
                + " and u.institution in :inss ";
        Map m = new HashMap();
        m.put("inss", getLoggableInstitutions());
        items = getFacade().findByJpql(j, m);
        userTransactionController.recordTransaction("To Manage Institution Users");
        return "/insAdmin/manage_users";
    }

    public String toAddNewUserByInsAdmin() {
        current = new WebUser();
        password = "";
        passwordReenter = "";
        userTransactionController.recordTransaction("To Add New User By InsAdmin");
        return "/insAdmin/create_new_user";
    }
    
    public void toProcedureRoom() {
        String privilageStr = null;
        String baseUrl = "http://localhost:8080/ProcedureRoomService/resources/redirect";
        String urlVals = "?API_KEY=EF16A5D4EF8AA6AA0580AF1390CF0600";
        urlVals += "&User_Name="+loggedUser.getName();
        urlVals += "&User_Role="+loggedUser.getWebUserRole();
        
        for(UserPrivilege up:loggedUserPrivileges){
            if(up.getPrivilege() != null){
                if(privilageStr==null){
                    privilageStr = up.getPrivilege().toString();
                }else{
                    privilageStr += "^"+up.getPrivilege().toString();
                }
            }
        }
        urlVals += "&Privileges="+"TEST";
        urlVals += "&Institution="+loggedUser.getInstitution().getCode();
        urlVals += "&UserID="+loggedUser.getName();
        
        Client client = Client.create();
        WebResource webResource1 = client.resource(baseUrl + urlVals);
        com.sun.jersey.api.client.ClientResponse cr = webResource1.accept("text/plain").get(com.sun.jersey.api.client.ClientResponse.class);
        String outpt = cr.getEntity(String.class);
        
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        try {
            externalContext.redirect(outpt);
        } catch (IOException ex) {
            Logger.getLogger(WebUserController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String toManageAllUsers() {
        items = webUserApplicationController.getItems();
        return "/webUser/manage_users";
    }

    public String toManageUserIndexForSystemAdmin() {
        return "/webUser/index";
    }

    public String toManagePrivileges() {

        if (current == null) {
            JsfUtil.addErrorMessage("Nothing Selected");
            return "";
        }
        selectedNodes = new TreeNode[0];
        List<UserPrivilege> userps = userPrivilegeList(current);

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
            for (TreeNode n : allPrivilegeRoot.getChildren()) {
                if (wup.getPrivilege().equals(((PrivilegeTreeNode) n).getP())) {
                    n.setSelected(true);

                    temSelected.add(n);
                }
                for (TreeNode n1 : n.getChildren()) {
                    if (wup.getPrivilege().equals(((PrivilegeTreeNode) n1).getP())) {
                        n1.setSelected(true);

                        temSelected.add(n1);
                    }
                    for (TreeNode n2 : n1.getChildren()) {
                        if (wup.getPrivilege().equals(((PrivilegeTreeNode) n2).getP())) {
                            n2.setSelected(true);

                            temSelected.add(n2);
                        }
                    }
                }
            }
        }
        selectedNodes = temSelected.toArray(new TreeNode[temSelected.size()]);
        userTransactionController.recordTransaction("Manage Privileges in user list By SysAdmin or InsAdmin");
        return "/webUser/privileges";
    }

    public String toOpdModule() {
        userTransactionController.recordTransaction("To Opd Module");
        return "/opd/index_opd";
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
        TreeNode me = new PrivilegeTreeNode("Monitoring and Evaluation", allPrivilegeRoot, Privilege.Monitoring_and_evaluation);

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

        //Monitoring and Evaluation
        TreeNode me_Users = new PrivilegeTreeNode("View Reports", me, Privilege.Monitoring_and_evaluation_reports);

    }

    public String toChangeMyDetails() {
        if (loggedUser == null) {
            return "";
        }
        current = loggedUser;
        userTransactionController.recordTransaction("To Change My Details");
        return "/change_my_details";
    }

    public String toChangeMyPassword() {
        if (loggedUser == null) {
            return "";
        }
        password = "";
        passwordReenter = "";
        current = loggedUser;
        userTransactionController.recordTransaction("To Change My Password");
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

    public List<Institution> completeLoggableInstitutions(String qry) {
        List<Institution> ins = new ArrayList<>();
        if (qry == null) {
            return ins;
        }
        if (qry.trim().equals("")) {
            return ins;
        }
        qry = qry.trim().toLowerCase();
        for (Institution i : getLoggableInstitutions()) {
            if (i.getName() == null) {
                continue;
            }
            if (i.getName().toLowerCase().contains(qry)) {
                ins.add(i);
            }
        }
        return ins;
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
        userTransactionController.recordTransaction("Logout");
        loggedUser = null;
        return "/index";
    }

    public String login() {
        userTransactionController.recordTransaction("login");
        return login(false);
    }

    public String login(boolean withoutPassword) {
        loggableInstitutions = null;
        loggablePmcis = null;
        loggableGnAreas = null;
        institutionController.setMyClinics(null);
        if (userName == null || userName.trim().equals("")) {
            JsfUtil.addErrorMessage("Please enter a Username");
            return "";
        }
        if (!withoutPassword) {
            if (password == null || password.trim().equals("")) {
                JsfUtil.addErrorMessage("Please enter the Password");
                return "";
            }
        }

        if (!checkLogin(withoutPassword)) {
            JsfUtil.addErrorMessage("Username/Password Error. Please retry.");
            userTransactionController.recordTransaction("Failed Login Attempt", userName);
            return "";
        }

        if (assumedPrivileges == null) {
            loggedUserPrivileges = userPrivilegeList(loggedUser);
        }
        JsfUtil.addSuccessMessage("Successfully Logged");
        userTransactionController.recordTransaction("Successful Login");
        return "/index";
    }

    public String loginNew() {
        System.out.println("loginNew");
        loggableInstitutions = null;
        loggablePmcis = null;
        loggableGnAreas = null;
        institutionController.setMyClinics(null);
        if (userName == null || userName.trim().equals("")) {
            JsfUtil.addErrorMessage("Please enter a Username");
            return "";
        }

        if (password == null || password.trim().equals("")) {
            JsfUtil.addErrorMessage("Please enter the Password");
            return "";
        }

        if (!checkLoginNew()) {
            JsfUtil.addErrorMessage("Username/Password Error. Please retry.");
            userTransactionController.recordTransaction("Failed Login Attempt", userName);
            return "";
        }

        System.out.println("username & password correct");
        
        loggedUserPrivileges = userPrivilegeList(loggedUser);

        JsfUtil.addSuccessMessage("Successfully Logged");
        userTransactionController.recordTransaction("Successful Login");
        return "/index";
    }

    private boolean checkLoginNew() {
        System.out.println("checkLoginNew");
        if (getFacade() == null) {
            JsfUtil.addErrorMessage("Server Error");
            return false;
        }

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

    public String toHome() {
        userTransactionController.recordTransaction("To Home");
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
        if (!checkLogin(false)) {
            loginRequestResponse += "Wrong Isername. Please go back to settings and update.";
            return "/mobile/login_failure";
        }
        return "/mobile/index";
    }

    public List<WebUser> completeUsers(String qry) {
        String temSQL;
        temSQL = "SELECT u FROM WebUser u WHERE lower(u.name) like :userName and u.retired =:ret";
        Map m = new HashMap();
        m.put("userName", "%" + qry.trim().toLowerCase() + "%");
        m.put("ret", false);
        return getFacade().findByJpql(temSQL, m);
    }

    private boolean checkLogin(boolean withoutPassword) {
        if (loggedUser != null && withoutPassword) {
            return true;
        }

        if (getFacade() == null) {
            JsfUtil.addErrorMessage("Server Error");
            return false;
        }

        String temSQL;
        temSQL = "SELECT u FROM WebUser u WHERE lower(u.name)=:userName and u.retired =:ret";
        Map m = new HashMap();
        m.put("userName", userName.trim().toLowerCase());
        m.put("ret", false);
        loggedUser = getFacade().findFirstByJpql(temSQL, m);
        if (loggedUser == null) {
            return false;
        }
        if (withoutPassword) {
            return true;
        }
        if (commonController.matchPassword(password, loggedUser.getWebUserPassword())) {
            return true;
        } else {
            loggedUser = null;
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
                wups.add(Privilege.Monitoring_and_evaluation);
                wups.add(Privilege.Monitoring_and_evaluation_reports);
                break;
            case Me_Super_User:
                wups.add(Privilege.User);
                wups.add(Privilege.Monitoring_and_evaluation);
                wups.add(Privilege.Monitoring_and_evaluation_reports);
                break;
            case Me_User:
                wups.add(Privilege.User);
                wups.add(Privilege.Monitoring_and_evaluation);
                wups.add(Privilege.Monitoring_and_evaluation_reports);
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

    public String toCreateNewUserBySysAdmin() {
        current = new WebUser();
        password = "";
        passwordReenter = "";
        userTransactionController.recordTransaction("Create New User By SysAdmin");
        return "/webUser/create_new_user";
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

    public String saveNewWebUserByInsAdmin() {
        if (current == null) {
            JsfUtil.addErrorMessage("Noting to save");
            return "";
        }
        if (!password.equals(passwordReenter)) {
            JsfUtil.addErrorMessage("Passwords do NOT match");
            userTransactionController.recordTransaction("Save NewWebUser By InsAdmin-Passwords do NOT match");
            return "";
        }
        if (userNameExsists(current.getName())) {
            JsfUtil.addErrorMessage("Username already exists. Please try another.");
            userTransactionController.recordTransaction("Save NewWebUser By InsAdmin-Username already exists");
            return "";
        }
        if (current.getId() != null) {
            current.setLastEditBy(loggedUser);
            current.setLastEditeAt(new Date());
            getFacade().edit(current);
            JsfUtil.addSuccessMessage("User Details Updated");
            userTransactionController.recordTransaction("Save NewWebUser By InsAdmin-User Details Updated");
            return "";
        }
        try {
            current.setWebUserPassword(commonController.hash(password));
            current.setCreatedAt(new Date());
            current.setCreater(loggedUser);
            getFacade().create(current);
            addWebUserPrivileges(current, getInitialPrivileges(current.getWebUserRole()));
            JsfUtil.addSuccessMessage(("A new User Created Successfully."));
            userTransactionController.recordTransaction("Save NewWebUser By InsAdmin-Successfully");
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ("Error Occured. Please change username and try again."));
            return "";
        }
        userTransactionController.recordTransaction("Save NewWebUser By InsAdmin");
        return "/insAdmin/user_index";
    }

    public boolean userNameExsists() {
        if (getSelected() == null) {
            return false;
        }

        boolean une = userNameExsists(getSelected().getName());
        return une;
    }

    public boolean userNameExsists(String un) {
        if (un == null) {
            return false;
        }
        String j = "select u from WebUser u where lower(u.name)=:un order by u.id desc";
        Map m = new HashMap();
        m.put("un", un.toLowerCase());
        WebUser u = getFacade().findFirstByJpql(j, m);
        return u != null;
    }

    public String saveNewWebUserBySysAdmin() {
        if (getSelected() == null) {
            JsfUtil.addErrorMessage("Noting to save");
            return "";
        }

        if (!password.equals(passwordReenter)) {
            JsfUtil.addErrorMessage("Passwords do NOT match");
            return "";
        }
        if (userNameExsists(getSelected().getName())) {
            JsfUtil.addErrorMessage("Username already exists. Please try another.");
            return "";
        }
        if (getSelected().getId() != null) {
            getSelected().setLastEditBy(loggedUser);
            getSelected().setLastEditeAt(new Date());
            getFacade().edit(getSelected());
            JsfUtil.addSuccessMessage("User Details Updated");
            return "/webUser/index";
        }
        try {
            current.setWebUserPassword(commonController.hash(password));
            current.setCreatedAt(new Date());
            current.setCreater(loggedUser);
            getFacade().create(current);
            addWebUserPrivileges(current, getInitialPrivileges(current.getWebUserRole()));
            JsfUtil.addSuccessMessage(("A new User Created Successfully."));
            userTransactionController.recordTransaction("NEW webUser Created");
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ("Error Occured. Please change username and try again."));
            return "";
        }
        webUserApplicationController.resetWebUsers();
        userTransactionController.recordTransaction("New WebUser save BySysAdmin");
        return "/webUser/index";
    }

    public String prepareEdit() {
        userTransactionController.recordTransaction("Edit user list By SysAdmin or InsAdmin");
        return "/webUser/Edit";
    }

    public String prepareEditPassword() {
        password = "";
        passwordReenter="";
        userTransactionController.recordTransaction("Edit Password user list By SysAdmin or InsAdmin");
        return "/webUser/Password";
    }

    public String update() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(("Updated"));
            userTransactionController.recordTransaction("webUser Update");
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
        List<UserPrivilege> userps = userPrivilegeList(current);
        List<Privilege> tps = new ArrayList<>();
        if (selectedNodes != null && selectedNodes.length > 0) {
            for (TreeNode node : selectedNodes) {
                Privilege p;
                p = ((PrivilegeTreeNode) node).getP();
                if (p != null) {
                    tps.add(p);
                }
            }
        }
        for (Privilege p : tps) {
            boolean found = false;
            for (UserPrivilege tup : userps) {

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
        userTransactionController.recordTransaction("update User Privileges By SysAdmin or InsAdmin");
        return "/webUser/manage_users";
    }

    public String updateMyDetails() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(("Your details Updated."));
            userTransactionController.recordTransaction("update My Details");
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
            userTransactionController.recordTransaction("My Password Mismatch");
            return "";
        }
        current.setWebUserPassword(commonController.hash(password));
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(("Password Updated"));
            password = "";
            passwordReenter = "";
            userTransactionController.recordTransaction("My Password Updated");
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
            userTransactionController.recordTransaction("webUser Password not match");
            return "";
        }
        try {
            String hashedPassword = commonController.hash(password);
            current.setWebUserPassword(hashedPassword);
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(("Password Changed."));
            userTransactionController.recordTransaction("webUser Password Changed");
            return "index";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ("PersistenceErrorOccured"));
            userTransactionController.recordTransaction("webUser Password error");
            return null;
        }
    }

    public WebUserRole[] getWebUserRolesForInsAdmin() {
        WebUserRole[] rs = {WebUserRole.Institution_Administrator, WebUserRole.Institution_Super_User, WebUserRole.Institution_User,
            WebUserRole.Doctor, WebUserRole.Nurse, WebUserRole.Midwife};
        return rs;
    }

    public List<WebUser> getItems() {
//        if (items == null) {
//            items = getFacade().findAll();
//        }
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
        userTransactionController.recordTransaction("All Privilege Root");
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

    public List<Institution> getLoggablePmcis() {
        if (loggablePmcis == null) {
            loggablePmcis = findAutherizedPmcis();
        }
        return loggablePmcis;
    }

    public void setLoggablePmcis(List<Institution> loggablePmcis) {
        this.loggablePmcis = loggablePmcis;
    }

    public List<Area> getLoggableGnAreas() {
        if (loggableGnAreas == null) {
            loggableGnAreas = findAutherizedGnAreas();
        }
        return loggableGnAreas;
    }

    public void setLoggableGnAreas(List<Area> loggableGnAreas) {
        this.loggableGnAreas = loggableGnAreas;
    }

    public int getReportTabIndex() {
        return reportTabIndex;
    }

    public void setReportTabIndex(int reportTabIndex) {
        this.reportTabIndex = reportTabIndex;
    }

    public ClientController getClientController() {
        return clientController;
    }

    public EncounterController getEncounterController() {
        return encounterController;
    }

    public WebUserRole getAssumedRole() {
        return assumedRole;
    }

    public void setAssumedRole(WebUserRole assumedRole) {
        this.assumedRole = assumedRole;
    }

    public Institution getAssumedInstitution() {
        return assumedInstitution;
    }

    public void setAssumedInstitution(Institution assumedInstitution) {
        this.assumedInstitution = assumedInstitution;
    }

    public Area getAssumedArea() {
        return assumedArea;
    }

    public void setAssumedArea(Area assumedArea) {
        this.assumedArea = assumedArea;
    }

    public List<UserPrivilege> getAssumedPrivileges() {
        return assumedPrivileges;
    }

    public void setAssumedPrivileges(List<UserPrivilege> assumedPrivileges) {
        this.assumedPrivileges = assumedPrivileges;
    }

    private List<UserPrivilege> generateAssumedPrivileges(WebUser wu, List<Privilege> ps) {
        List<UserPrivilege> ups = new ArrayList<>();
        for (Privilege p : ps) {
            UserPrivilege up = new UserPrivilege();
            up.setPrivilege(p);
            up.setWebUser(wu);
            ups.add(up);
        }
        return ups;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    private UserTransactionController getUserTransactionController() {
        return userTransactionController;
    }

    public int getIndicatorTabIndex() {
        return indicatorTabIndex;
    }

    public void setIndicatorTabIndex(int indicatorTabIndex) {
        this.indicatorTabIndex = indicatorTabIndex;
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
