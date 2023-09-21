package lk.gov.health.phsp.bean;

import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.entity.Upload;
import lk.gov.health.phsp.enums.WebUserRole;
import lk.gov.health.phsp.facade.InstitutionFacade;
import lk.gov.health.phsp.facade.UploadFacade;
import lk.gov.health.phsp.facade.WebUserFacade;
import lk.gov.health.phsp.facade.util.JsfUtil;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import lk.gov.health.phsp.entity.Person;
import lk.gov.health.phsp.entity.Relationship;
import lk.gov.health.phsp.entity.UserPrivilege;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.enums.Privilege;
import lk.gov.health.phsp.enums.PrivilegeTreeNode;
import lk.gov.health.phsp.enums.RelationshipType;
import lk.gov.health.phsp.facade.UserPrivilegeFacade;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;
import org.primefaces.model.file.UploadedFile;
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
    InstitutionApplicationController institutionApplicationController;
    @Inject
    WebUserApplicationController webUserApplicationController;
    @Inject
    RelationshipController relationshipController;
    @Inject
    HospitalDashboardController hospitalDashboardController;
    /*
    Variables
     */
    private boolean highSecurity = false;

    private List<WebUser> items = null;
    private List<Upload> companyUploads;

    private List<Institution> loggableInstitutions;
    private List<Institution> loggableClinics;
    private List<Institution> loggablePmcis;
    private List<Institution> loggableHospitals;
    private List<Institution> loggableProcedureRooms;

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
    private String selectedIp;
    private String selectedUsername;

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
    private String currentPassword;
    private MapModel emptyModel;
    List<UserPrivilege> loggedUserPrivileges;

    private UploadedFile file;
    private String comments;
    private Boolean ipBlocked;
    boolean logged;

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
    private int metadataTabIndex;

    private String ipAddress;

    private WebUser passwordChangingUser;

    /**
     *
     * Privileges
     *
     */
    private TreeNode allPrivilegeRoot;
    private TreeNode myPrivilegeRoot;
    private TreeNode[] selectedNodes;

    private String institutionName;

    @PostConstruct
    public void init() {
        emptyModel = new DefaultMapModel();
        createAllPrivilege();
        findIpAddress();
    }

    @PreDestroy
    public void sessionDestroy() {
        webUserApplicationController.removeFromLoggedUsers(userName);
        userTransactionController.recordTransaction("Invalidating the Session", this.toString());
    }

    private void findIpAddress() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }

    }

//    public boolean ipBlocked() {
//        return webUserApplicationController.ipBlocked(getIpAddress());
//    }
//    public String assumeUser() {
//        if (current == null) {
//            JsfUtil.addErrorMessage("Please select a User");
//            return "";
//        }
//        assumedArea = current.getArea();
//        assumedInstitution = current.getInstitution();
//        assumedRole = current.getWebUserRole();
//        assumedPrivileges = userPrivilegeList(current);
//        userTransactionController.recordTransaction("assume User");
//        return assumeRoles();
//
//    }
//    public String assumeRoles() {
//        if (assumedRole == null) {
//            JsfUtil.addErrorMessage("Please select a Role");
//            userTransactionController.recordTransaction("Assume Roles");
//            return "";
//        }
//
//        if (assumedInstitution == null) {
//            JsfUtil.addErrorMessage("Please lsect an Institution");
//            return "";
//        }
////        if (assumedArea == null) {
////            JsfUtil.addErrorMessage("Please select an area");
////            return "";
////        }
//        if (assumedPrivileges == null) {
//            assumedPrivileges = generateAssumedPrivileges(loggedUser, getInitialPrivileges(assumedRole));
//        }
//        WebUser twu = loggedUser;
//        logOut();
//        userName = twu.getName();
//        loggedUser = twu;
//        loggedUser.setAssumedArea(assumedArea);
//        loggedUser.setAssumedInstitution(assumedInstitution);
//        loggedUser.setAssumedRole(assumedRole);
//        loggedUserPrivileges = assumedPrivileges;
//        return WebUserController.this.login(true);
//    }
    public void assumedInstitutionChanged() {
        if (assumedInstitution != null) {
            assumedArea = assumedInstitution.getDistrict();
        }
    }

//    public String endAssumingRoles() {
//        assumedRole = null;
//        assumedInstitution = null;
//        assumedArea = null;
//        assumedPrivileges = null;
//        logOut();
//        userTransactionController.recordTransaction("End Assuming Roles");
//        return WebUserController.this.login(true);
//    }
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

    public List<Institution> findAutherizedClinics(InstitutionType t) {
        List<Institution> ins = new ArrayList<>();
        if (loggedUser == null) {
            return ins;
        }
        if (loggedUser.getInstitution() == null) {
            return ins;
        }
        ins.add(loggedUser.getInstitution());
        ins.addAll(institutionApplicationController.findChildrenInstitutions(loggedUser.getInstitution()));
        List<Institution> rins = new ArrayList<>();
        for (Institution i : ins) {
            if (i.getInstitutionType().equals(t)) {
                rins.add(i);
            }
        }

        return rins;

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

    public String toManageAllUsers() {
        items = webUserApplicationController.getItems();
        return "/systemAdmin/manage_users";
    }

    public String toManageBlockedIps() {
        return "/systemAdmin/blocked_ips";
    }

    public String toManageBlockedUsers() {
        return "/systemAdmin/blocked_users";
    }

    public String toManageLoggedUsers() {
        return "/systemAdmin/logged_users";
    }

    public void removeBlockedIp() {
        if (selectedIp == null || selectedIp.trim().equals("")) {
            JsfUtil.addErrorMessage("No IP Selected");
            return;
        }
        try {
            webUserApplicationController.getSuspiciousIps().remove(selectedIp);
        } catch (Exception e) {
            JsfUtil.addErrorMessage("Error in removing.");
        }
    }

    public void removeBlockedUser() {
        if (selectedUsername == null || selectedUsername.trim().equals("")) {
            JsfUtil.addErrorMessage("No User Selected");
            return;
        }
        try {
            webUserApplicationController.getSuspiciousUsers().remove(selectedUsername);
        } catch (Exception e) {
            JsfUtil.addErrorMessage("Error in removing.");
        }
    }

    public void removeLoggedUser() {
        if (selectedUsername == null || selectedUsername.trim().equals("")) {
            JsfUtil.addErrorMessage("No User Selected");
            return;
        }
        try {
            webUserApplicationController.getLoggedUsers().remove(selectedUsername);
        } catch (Exception e) {
            JsfUtil.addErrorMessage("Error in removing.");
        }
    }

    public String toManageUserIndexForSystemAdmin() {
        return "/systemAdmin/manage_user_index";
    }

    public String toManagePrivileges() {

        if (current == null) {
            JsfUtil.addErrorMessage("Nothing Selected");
            return "";
        }
        selectedNodes = new TreeNode[0];
        List<UserPrivilege> userps = userPrivilegeList(current);

        for (Object o : allPrivilegeRoot.getChildren()) {
            TreeNode n;
            if (o instanceof TreeNode) {
                n = (TreeNode) o;
            } else {
                continue;
            }
            n.setSelected(false);
            for (Object o1 : n.getChildren()) {
                TreeNode n1;
                if (o1 instanceof TreeNode) {
                    n1 = (TreeNode) o1;
                } else {
                    continue;
                }
                n1.setSelected(false);
                for (Object o2 : n1.getChildren()) {
                    TreeNode n2;
                    if (o2 instanceof TreeNode) {
                        n2 = (TreeNode) o2;
                    } else {
                        continue;
                    }
                    n2.setSelected(false);
                }
            }
        }
        List<TreeNode> temSelected = new ArrayList<>();
        for (UserPrivilege wup : userps) {
            for (Object o : allPrivilegeRoot.getChildren()) {
                TreeNode n;
                if (o instanceof TreeNode) {
                    n = (TreeNode) o;
                } else {
                    continue;
                }
                if (wup.getPrivilege().equals(((PrivilegeTreeNode) n).getP())) {
                    n.setSelected(true);

                    temSelected.add(n);
                }
                for (Object o1 : n.getChildren()) {
                    TreeNode n1;
                    if (o1 instanceof TreeNode) {
                        n1 = (TreeNode) o1;
                    } else {
                        continue;
                    }
                    if (wup.getPrivilege().equals(((PrivilegeTreeNode) n1).getP())) {
                        n1.setSelected(true);

                        temSelected.add(n1);
                    }
                    for (Object o2 : n1.getChildren()) {
                        TreeNode n2;
                        if (o2 instanceof TreeNode) {
                            n2 = (TreeNode) o2;
                        } else {
                            continue;
                        }
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

    public String toManagePrivilegesBySysAdmin() {

        if (current == null) {
            JsfUtil.addErrorMessage("Nothing Selected");
            return "";
        }
        selectedNodes = new TreeNode[0];
        List<UserPrivilege> userps = userPrivilegeList(current);

        for (Object o : allPrivilegeRoot.getChildren()) {
            TreeNode n;
            if (o instanceof TreeNode) {
                n = (TreeNode) o;
            } else {
                continue;
            }
            n.setSelected(false);
            for (Object o1 : n.getChildren()) {
                TreeNode n1;
                if (o1 instanceof TreeNode) {
                    n1 = (TreeNode) o1;
                } else {
                    continue;
                }
                n1.setSelected(false);
                for (Object o2 : n1.getChildren()) {
                    TreeNode n2;
                    if (o2 instanceof TreeNode) {
                        n2 = (TreeNode) o2;
                    } else {
                        continue;
                    }
                    n2.setSelected(false);
                }
            }
        }
        List<TreeNode> temSelected = new ArrayList<>();
        for (UserPrivilege wup : userps) {
            for (Object o : allPrivilegeRoot.getChildren()) {
                TreeNode n;
                if (o instanceof TreeNode) {
                    n = (TreeNode) o;
                } else {
                    continue;
                }
                if (wup.getPrivilege().equals(((PrivilegeTreeNode) n).getP())) {
                    n.setSelected(true);

                    temSelected.add(n);
                }
                for (Object o1 : n.getChildren()) {
                    TreeNode n1;
                    if (o1 instanceof TreeNode) {
                        n1 = (TreeNode) o1;
                    } else {
                        continue;
                    }
                    if (wup.getPrivilege().equals(((PrivilegeTreeNode) n1).getP())) {
                        n1.setSelected(true);

                        temSelected.add(n1);
                    }
                    for (Object o2 : n1.getChildren()) {
                        TreeNode n2;
                        if (o2 instanceof TreeNode) {
                            n2 = (TreeNode) o2;
                        } else {
                            continue;
                        }
                        if (wup.getPrivilege().equals(((PrivilegeTreeNode) n2).getP())) {
                            n2.setSelected(true);

                            temSelected.add(n2);
                        }
                    }
                }
            }
        }
        selectedNodes = temSelected.toArray(new TreeNode[temSelected.size()]);
        userTransactionController.recordTransaction("Manage Privileges in user list By SysAdmin");
        return "/systemAdmin/privileges";
    }

    public String toOpdModule() {
        userTransactionController.recordTransaction("To Opd Module");
        return "/opd/index_opd";
    }

    private void createAllPrivilege() {
        allPrivilegeRoot = new PrivilegeTreeNode("Root", null);

        TreeNode clientManagement = new PrivilegeTreeNode("Client Management", allPrivilegeRoot, Privilege.Client_Management);
        TreeNode encounterManagement = new PrivilegeTreeNode("Clinic Management", allPrivilegeRoot, Privilege.Encounter_Management);
        TreeNode analytics = new PrivilegeTreeNode("Analytics", allPrivilegeRoot, Privilege.Analytics);
        TreeNode institutionAdministration = new PrivilegeTreeNode("Institution Administration", allPrivilegeRoot, Privilege.Institution_Administration);
        TreeNode systemAdministration = new PrivilegeTreeNode("System Administration", allPrivilegeRoot, Privilege.System_Administration);

        //Client Management
        TreeNode add_Client = new PrivilegeTreeNode("Add Client", clientManagement, Privilege.Add_Client);
        TreeNode edit_client = new PrivilegeTreeNode("Edit Client", clientManagement, Privilege.Edit_client);
        TreeNode delete_client = new PrivilegeTreeNode("Delete Client", clientManagement, Privilege.Delete_client);
        TreeNode reserve_phn = new PrivilegeTreeNode("Reserve PHN", clientManagement, Privilege.Reserve_Phn);
        TreeNode search_client = new PrivilegeTreeNode("Search Client", clientManagement, Privilege.Search_client);
        //Clinic Management
        TreeNode add_to_clinic = new PrivilegeTreeNode("Add to Clinic", encounterManagement, Privilege.Add_to_clinic);
        TreeNode remove_from_clinic = new PrivilegeTreeNode("Remove from Clinic", encounterManagement, Privilege.Remove_from_clinic);
        TreeNode add_clinic_visit = new PrivilegeTreeNode("Add Clinic Visit", encounterManagement, Privilege.Add_clinic_visit);
        TreeNode complete_clinic_visit = new PrivilegeTreeNode("Complete Clinic Visits", encounterManagement, Privilege.Complete_clinic_visit);
        TreeNode incomplete_clinic_visit = new PrivilegeTreeNode("Incomplete Clinic Visits", encounterManagement, Privilege.Incomplete_clinic_visit);
        // Analytics
        TreeNode counts = new PrivilegeTreeNode("Counts", analytics, Privilege.Counts);
        TreeNode indicators = new PrivilegeTreeNode("Indicators", analytics, Privilege.Indicators);
        TreeNode templates = new PrivilegeTreeNode("Template", analytics, Privilege.Templates);
        TreeNode named_Lists = new PrivilegeTreeNode("Named Lists", analytics, Privilege.Named_Lists);
        TreeNode anonymous_Lists = new PrivilegeTreeNode("Anonymous Lists", analytics, Privilege.Anonymous_Lists);
        //Institution Administration
        TreeNode manage_Institution_Users = new PrivilegeTreeNode("Manage Users", institutionAdministration, Privilege.Manage_Institution_Users);
        TreeNode manage_Authorised_Areas = new PrivilegeTreeNode("Manage Areas", institutionAdministration, Privilege.Manage_Authorised_Areas);
        TreeNode manage_Authorised_Institutions = new PrivilegeTreeNode("Manage Institutions", institutionAdministration, Privilege.Manage_Authorised_Institutions);
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
//        downloadingFile = new DefaultStreamedContent(stream, currentUpload.getFileType(), currentUpload.getFileName());
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
        webUserApplicationController.removeFromLoggedUsers(userName);
        userTransactionController.recordTransaction("Logout");
        loggedUser = null;
        logged = false;
        return "/index";
    }

    public String login() {
        loggableInstitutions = null;
        loggableClinics = null;
        loggableHospitals = null;

        loggablePmcis = null;
        loggableGnAreas = null;
        institutionController.setMyClinics(null);
        if (userName == null || userName.trim().equals("")) {
            JsfUtil.addErrorMessage("Please enter a Username");
            return "";
        }
        userName = userName.toLowerCase().trim();

        if (highSecurity) {

            if (webUserApplicationController.userBlocked(userName)) {
                JsfUtil.addErrorMessage("This user is blocked due to multiple failed login attempts. Please contact the hotline.");
                return "";
            }
            if (webUserApplicationController.userAlreadyLogged(userName)) {
                JsfUtil.addErrorMessage("This user is already logged to the system. If you have any concerns, please contact the hotline.");
                return "";
            }
        }
        if (password == null || password.trim().equals("")) {
            JsfUtil.addErrorMessage("Please enter the Password");
            return "";
        }
        //System.out.println("password = " + password);
        if (!checkLogin()) {
            JsfUtil.addErrorMessage("Username/Password Error. Please retry.");
            userTransactionController.recordTransaction("Failed Login Attempt", userName);
            webUserApplicationController.addFailedAttempt(getIpAddress(), getUserName());
            return "";
        }
        logged = true;
        loggedUserPrivileges = userPrivilegeList(loggedUser);
        clientController.setClientDcfs(null);

        userTransactionController.recordTransaction("Successful Login");
        webUserApplicationController.addToLoggedUsers(userName);
        if (!passwordStrengthCheck(password)) {
            passwordChangingUser = loggedUser;
            loggedUser = null;
            return "/webUser/change_password_at_login";
        } else {
            passwordChangingUser = null;
            prepareDashboards();
            JsfUtil.addSuccessMessage("Successfully Logged");
            return "/index";
        }
    }

    private void prepareDashboards() {
        switch (getLoggedUser().getWebUserRoleLevel()) {
            case Hospital:
            case Provincial:
            case Regional:
                hospitalDashboardController.prepareDashboard();
                break;
            case National:
            case National_Me:
            case Client:
            case Moh:
        }
    }

    private String usersExists;

    public void checkUsersExists() {
        if (thereAreUsersInTheSystem()) {
            usersExists = "Users Exists";
        } else {
            usersExists = "Users Do Not Exists";
        }
    }

    private boolean thereAreUsersInTheSystem() {
        String jpql = "select w from WebUser w";
        WebUser u = getFacade().findFirstByJpql(jpql);
        if (u == null) {
            return false;
        }
        return true;
    }

    private boolean checkLogin() {
        //System.out.println("checkLogin");
        if (getFacade() == null) {
            JsfUtil.addErrorMessage("Server Error");
            return false;
        }

        String temSQL;
        temSQL = "SELECT u FROM WebUser u WHERE u.name=:userName and u.retired =:ret";
        Map m = new HashMap();
        m.put("userName", userName.trim().toLowerCase());
        m.put("ret", false);
        //System.out.println("m = " + m);
        //System.out.println("temSQL = " + temSQL);
        loggedUser = getFacade().findFirstByJpql(temSQL, m);
        //System.out.println("loggedUser = " + loggedUser);
        if (loggedUser == null) {
            return false;
        }
        if (commonController.matchPassword(password, loggedUser.getWebUserPassword())) {
            //System.out.println("Password matching" );
            return true;
        } else {
            //System.out.println("Password mismatch ");
            loggedUser = null;
            return false;
        }
    }

    public String toHome() {
        userTransactionController.recordTransaction("To Home");
        return "/index";
    }

    public List<WebUser> completeUsers(String qry) {
        String temSQL;
        temSQL = "SELECT u FROM WebUser u WHERE u.name like :userName and u.retired =:ret";
        Map m = new HashMap();
        m.put("userName", "%" + qry.trim().toLowerCase() + "%");
        m.put("ret", false);
        return getFacade().findByJpql(temSQL, m);
    }

    public void createDemoUser() {
        Institution i = new Institution();
        i.setName(userName);
        institutionController.saveOrUpdateInstitution(i);

        Person p = new Person();
        p.setName("Administrator");

        WebUser u = new WebUser();
        u.setName("admin");
        u.setWebUserPassword(commonController.hash(password));
        u.setWebUserRole(WebUserRole.System_Administrator);

        save(u);

    }

    List<Privilege> getInitialPrivileges(WebUserRole role) {
        List<Privilege> wups = new ArrayList<>();
        if (role == null) {
            return wups;
        }
        switch (role) {

            case Client:
                break;
            case System_Administrator:
                wups.add(Privilege.Manage_Users);
            case Super_User:
                wups.add(Privilege.Manage_Metadata);
                wups.add(Privilege.Manage_Area);
                wups.add(Privilege.Manage_Institutions);
            case User:
                wups.add(Privilege.Manage_Forms);
                wups.add(Privilege.System_Administration);
            case Institution_Administrator:
                wups.add(Privilege.Manage_Institution_Users);
            case Institution_Super_User:
                wups.add(Privilege.Institution_Administration);
                wups.add(Privilege.Manage_Authorised_Areas);
                wups.add(Privilege.Manage_Authorised_Institutions);
            case Midwife:
            case Nurse:
            case Doctor:
            case Institution_User:
                //Menu
                wups.add(Privilege.Client_Management);
                wups.add(Privilege.Encounter_Management);
                wups.add(Privilege.Analytics);
                //Clinic Management
                wups.add(Privilege.Add_to_clinic);
                wups.add(Privilege.Remove_from_clinic);
                wups.add(Privilege.Add_clinic_visit);
                wups.add(Privilege.Complete_clinic_visit);
                wups.add(Privilege.Incomplete_clinic_visit);
                //Client Management
                wups.add(Privilege.Add_Client);
                wups.add(Privilege.Search_client);
                wups.add(Privilege.Edit_client);
                wups.add(Privilege.Delete_client);
                wups.add(Privilege.Reserve_Phn);
                //Analytics
                wups.add(Privilege.Counts);
                wups.add(Privilege.Indicators);
                wups.add(Privilege.Templates);
                wups.add(Privilege.Named_Lists);
                wups.add(Privilege.Anonymous_Lists);
                break;

            case Me_Admin:
                wups.add(Privilege.Manage_Institution_Users);
            case Me_Super_User:
            case Me_User:
                wups.add(Privilege.Analytics);
                wups.add(Privilege.Counts);
                wups.add(Privilege.Indicators);
                wups.add(Privilege.Templates);
                wups.add(Privilege.Named_Lists);
                break;
            case Moh:
            case Student:

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
        return "/systemAdmin/create_new_user";
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
        if (getSelected() == null) {
            JsfUtil.addErrorMessage("Noting to save");
            return "";
        }

        if (getSelected().getName() == null || getSelected().getName().equals("")) {
            JsfUtil.addErrorMessage("Please enter a Username");
            return "";
        }
        if (getSelected().getName() == null || getSelected().getName().equals("")) {
            JsfUtil.addErrorMessage("Please enter a Username");
            return "";
        }
        if (password == null || password.trim().equals("")) {
            JsfUtil.addErrorMessage("Please enter a Password");
            return "";
        }
        if (passwordReenter == null || passwordReenter.trim().equals("")) {
            JsfUtil.addErrorMessage("Please enter the Password Again");
            return "";
        }
        if (!password.equals(passwordReenter)) {
            JsfUtil.addErrorMessage("Passwords do NOT match");
            return "";
        }
        if (!passwordStrengthCheck(password)) {
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
            userTransactionController.recordTransaction("Save NewWebUser By InsAdmin-User Details Updated");
            return "";
        }
        try {
            getSelected().setWebUserPassword(commonController.hash(password));
            getSelected().setCreatedAt(new Date());
            getSelected().setCreater(loggedUser);
            getFacade().create(getSelected());
            addWebUserPrivileges(getSelected(), getInitialPrivileges(current.getWebUserRole()));
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
        String j = "select u from WebUser u where u.name=:un order by u.id desc";
        Map m = new HashMap();
        m.put("un", un.toLowerCase());
        WebUser u = getFacade().findFirstByJpql(j, m);
        return u != null;
    }

    public boolean passwordStrengthCheck(String password) {
        int passwordLength = 8, upChars = 0, lowChars = 0;
        int special = 0, digits = 0;
        char ch;

        int total = password.length();
        if (total < passwordLength) {
            JsfUtil.addErrorMessage("Length of the Password is not enough. Need at least " + passwordLength + " charactors.");
            return false;
        } else {
            for (int i = 0; i < total; i++) {
                ch = password.charAt(i);
                if (Character.isUpperCase(ch)) {
                    upChars = 1;
                } else if (Character.isLowerCase(ch)) {
                    lowChars = 1;
                } else if (Character.isDigit(ch)) {
                    digits = 1;
                } else {
                    special = 1;
                }
            }
        }
        if (upChars == 1 && lowChars == 1 && digits == 1 && special == 1) {
            return true;
        } else {
            JsfUtil.addErrorMessage("The Password is week. Need at least one loweser case letter, upper case letter, a number and a special charactors.");
            return false;
        }
    }

    public String saveNewWebUserBySysAdmin() {
        if (getSelected() == null) {
            JsfUtil.addErrorMessage("Noting to save");
            return "";
        }

        if (getSelected().getName() == null || getSelected().getName().equals("")) {
            JsfUtil.addErrorMessage("Please enter a Username");
            return "";
        }
        if (getSelected().getName() == null || getSelected().getName().equals("")) {
            JsfUtil.addErrorMessage("Please enter a Username");
            return "";
        }
        if (password == null || password.trim().equals("")) {
            JsfUtil.addErrorMessage("Please enter a Password");
            return "";
        }
        if (passwordReenter == null || passwordReenter.trim().equals("")) {
            JsfUtil.addErrorMessage("Please enter the Password Again");
            return "";
        }
        if (!password.equals(passwordReenter)) {
            JsfUtil.addErrorMessage("Passwords do NOT match");
            return "";
        }
        if (!passwordStrengthCheck(password)) {
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
            getSelected().setWebUserPassword(commonController.hash(password));
            getSelected().setCreatedAt(new Date());
            getSelected().setCreater(loggedUser);
            getFacade().create(getSelected());
            addWebUserPrivileges(getSelected(), getInitialPrivileges(getSelected().getWebUserRole()));
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

    public String prepareEditBySysAdmin() {
        userTransactionController.recordTransaction("Edit user list By SysAdmin or InsAdmin");
        return "/systemAdmin/Edit";
    }

    public String prepareEditPassword() {
        password = "";
        passwordReenter = "";
        return "/webUser/Password";
    }

    public String prepareEditPasswordBySysAdmin() {
        password = "";
        passwordReenter = "";
        return "/systemAdmin/Password";
    }

    public String deleteUser() {
        if (current == null) {
            JsfUtil.addErrorMessage("Nothing to delete");
            return "";
        }
        current.setRetired(true);
        current.setRetirer(getLoggedUser());
        current.setRetiredAt(new Date());
        save(current);
        webUserApplicationController.getItems().remove(current);
        getItems().remove(current);
        return "";
    }

    public void save(WebUser u) {
        if (u == null) {
            return;
        }
        if (u.getId() == null) {
            u.setCreatedAt(new Date());
            u.setCreater(getLoggedUser());
            getFacade().create(u);
        } else {
            u.setLastEditBy(getLoggedUser());
            u.setLastEditeAt(new Date());
            getFacade().edit(u);
        }
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

    public String updateBySysAdmin() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(("Updated"));
            userTransactionController.recordTransaction("webUser Update");
            return toManageUserIndexForSystemAdmin();
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

    public String updateUserPrivilegesBySysAdmin() {

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
        return toManageUserIndexForSystemAdmin();
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
        if (loggedUser == null) {
            JsfUtil.addSuccessMessage(("Error. No Logged User"));
            return "";
        }
        if (currentPassword == null || currentPassword.trim().equals("")) {
            JsfUtil.addSuccessMessage(("Error. Enter the current password."));
            return "";
        }
        if (!commonController.matchPassword(currentPassword, loggedUser.getWebUserPassword())) {
            JsfUtil.addSuccessMessage(("Error. Current Password is wrong. Please call hotline to reset."));
            return "";
        }
        if (password == null || password.trim().equals("")) {
            JsfUtil.addSuccessMessage(("Error. Enter a new password"));
            return "";
        }
        if (passwordReenter == null || passwordReenter.trim().equals("")) {
            JsfUtil.addSuccessMessage(("Error. Enter a new password again."));
            return "";
        }
        if (!password.equals(passwordReenter)) {
            JsfUtil.addSuccessMessage(("Password Mismatch."));
            return "";
        }
        if (!passwordStrengthCheck(password)) {
            JsfUtil.addSuccessMessage(("The strength of the password is NOT enough. Please include at least one letter, one number and one special charactor. Password length should be at least 8 charactors."));
            return "";
        }
        loggedUser.setWebUserPassword(commonController.hash(password));
        try {
            getFacade().edit(loggedUser);
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

    public String updateMyPasswordAtLogin() {
        if (passwordChangingUser == null) {
            JsfUtil.addSuccessMessage(("Error. No Logged User"));
            return "";
        }
        if (currentPassword == null || currentPassword.trim().equals("")) {
            JsfUtil.addSuccessMessage(("Error. Enter the current password."));
            return "";
        }
        if (!commonController.matchPassword(currentPassword, passwordChangingUser.getWebUserPassword())) {
            JsfUtil.addSuccessMessage(("Error. Current Password is wrong. Please call hotline to reset."));
            return "";
        }
        if (password == null || password.trim().equals("")) {
            JsfUtil.addSuccessMessage(("Error. Enter a new password"));
            return "";
        }
        if (passwordReenter == null || passwordReenter.trim().equals("")) {
            JsfUtil.addSuccessMessage(("Error. Enter a new password again."));
            return "";
        }
        if (!password.equals(passwordReenter)) {
            JsfUtil.addSuccessMessage(("Password Mismatch."));
            return "";
        }
        if (!passwordStrengthCheck(password)) {
            JsfUtil.addSuccessMessage(("The strength of the password is NOT enough. Please include at least one letter, one number and one special charactor. Password length should be at least 8 charactors."));
            return "";
        }
        passwordChangingUser.setWebUserPassword(commonController.hash(password));
        try {
            getFacade().edit(passwordChangingUser);
            JsfUtil.addSuccessMessage(("Password Updated"));
            password = "";
            passwordReenter = "";
            userTransactionController.recordTransaction("My Password Updated");
            loggedUser = passwordChangingUser;
            passwordChangingUser = null;
            JsfUtil.addSuccessMessage("Password Changed. Successfully Logged to the appliation.");
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

    public String updatePasswordBySysAdmin() {
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
            return toManageUserIndexForSystemAdmin();
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
        return items;
    }

    private void recreateModel() {
        items = null;
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

    public List<Institution> getLoggableClinics() {
        if (loggableClinics == null) {
            loggableClinics = findAutherizedClinics(InstitutionType.Clinic);
        }
        return loggableClinics;
    }

    public List<Institution> getLoggableHospitals() {
        if (loggableHospitals == null) {
            loggableHospitals = findAutherizedClinics(InstitutionType.Base_Hospital);
        }
        return loggableHospitals;
    }

    public boolean isLogged() {
        return logged;
    }

    public void setLogged(boolean logged) {
        this.logged = logged;
    }

    public List<Institution> getLoggableProcedureRooms() {
        if (loggableProcedureRooms == null) {
            Map<Long, Institution> mapPrs = new HashMap<>();
            List<Institution> prs = new ArrayList<>();
            for (Institution ins : getLoggableInstitutions()) {
                List<Relationship> rs = relationshipController.findRelationships(ins, RelationshipType.Procedure_Room);
                for (Relationship r : rs) {
                    if (r.getToInstitution() != null) {
                        mapPrs.put(r.getToInstitution().getId(), r.getToInstitution());
                    }
                }
            }
            prs.addAll(mapPrs.values());
            loggableProcedureRooms = prs;
        }
        return loggableProcedureRooms;
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

    public int getMetadataTabIndex() {
        return metadataTabIndex;
    }

    public void setMetadataTabIndex(int metadataTabIndex) {
        this.metadataTabIndex = metadataTabIndex;
    }

    public Boolean getIpBlocked() {
        ipBlocked = webUserApplicationController.ipBlocked(getIpAddress());
        return ipBlocked;
    }

    public void setIpBlocked(Boolean ipBlocked) {
        this.ipBlocked = ipBlocked;
    }

    public String getSelectedIp() {
        return selectedIp;
    }

    public void setSelectedIp(String selectedIp) {
        this.selectedIp = selectedIp;
    }

    public String getSelectedUsername() {
        return selectedUsername;
    }

    public void setSelectedUsername(String selectedUsername) {
        this.selectedUsername = selectedUsername;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public WebUser getPasswordChangingUser() {
        return passwordChangingUser;
    }

    public void setPasswordChangingUser(WebUser passwordChangingUser) {
        this.passwordChangingUser = passwordChangingUser;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public String getUsersExists() {
        return usersExists;
    }

    public void setUsersExists(String usersExists) {
        this.usersExists = usersExists;
    }

    public boolean isHighSecurity() {
        return highSecurity;
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
