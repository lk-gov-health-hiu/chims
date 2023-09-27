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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
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
import lk.gov.health.phsp.entity.ClientEncounterComponentForm;
import lk.gov.health.phsp.entity.ClientEncounterComponentFormSet;
import lk.gov.health.phsp.entity.ClientEncounterComponentItem;
import lk.gov.health.phsp.entity.DesignComponentForm;
import lk.gov.health.phsp.entity.DesignComponentFormItem;
import lk.gov.health.phsp.entity.DesignComponentFormSet;
import lk.gov.health.phsp.entity.Encounter;
import lk.gov.health.phsp.entity.FhirOperationResult;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.IntegrationEndpoint;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.entity.Person;
import lk.gov.health.phsp.enums.AreaType;
import lk.gov.health.phsp.enums.ComponentSex;
import lk.gov.health.phsp.enums.DataRepresentationType;
import lk.gov.health.phsp.enums.InstitutionType;

import lk.gov.health.phsp.facade.EncounterFacade;
import lk.gov.health.phsp.pojcs.ClientBasicData;
import lk.gov.health.phsp.pojcs.YearMonthDay;
import lk.gov.health.phsp.pojcs.dataentry.DataForm;
import lk.gov.health.phsp.pojcs.dataentry.DataFormset;
import lk.gov.health.phsp.pojcs.dataentry.DataItem;
import lk.gov.health.phsp.enums.EncounterType;
import static lk.gov.health.phsp.enums.EncounterType.Client_Data;
import lk.gov.health.phsp.enums.SearchCriteria;
import lk.gov.health.phsp.facade.PersonFacade;
import lk.gov.health.phsp.pojcs.SearchQueryData;
import org.primefaces.component.tabview.TabView;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.file.UploadedFile;

// </editor-fold>
@Named("clientController")
@SessionScoped
public class ClientController implements Serializable {

    // <editor-fold defaultstate="collapsed" desc="EJBs">
    @EJB
    private lk.gov.health.phsp.facade.ClientFacade ejbFacade;
    @EJB
    PersonFacade personFacade;
    @EJB
    private EncounterFacade encounterFacade;
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Controllers">
    @Inject
    ApplicationController applicationController;
    @Inject
    AreaApplicationController areaApplicationController;
    @Inject
    InstitutionApplicationController institutionApplicationController;
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
    @Inject
    DesignComponentFormSetController designComponentFormSetController;
    @Inject
    DesignComponentFormController designComponentFormController;
    @Inject
    ClientEncounterComponentFormController clientEncounterComponentFormController;
    @Inject
    DesignComponentFormItemController designComponentFormItemController;
    @Inject
    ClientEncounterComponentItemController clientEncounterComponentItemController;
    @Inject
    IntegrationTriggerController integrationTriggerController;
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Variables">
    private List<Client> items = null;
    private List<ClientBasicData> clients = null;
    private List<Client> selectedClients = null;
    private List<Client> selectedClientsFromIntegrations = new ArrayList<>();
    private List<ClientBasicData> selectedClientsWithBasicData = null;
    private List<Client> importedClients = null;

    private List<ClientBasicData> selectedClientsBasic = null;

    private Client selected;
    private Long selectedId;
    private Long idFrom;
    private Long idTo;
    private Institution institution;
    private List<Encounter> selectedClientsClinics;
    private List<Encounter> selectedClientsLastFiveClinicVisits;
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
    private Integer numberOfPhnToReserve;
    private boolean goingToCaptureWebCamPhoto;
    private UploadedFile file;
    private Date clinicDate;
    private Date from;
    private Date to;

    private Boolean nicExists;
    private Boolean phnExists;
    private Boolean emailExists;
    private Boolean phone1Exists;
    private Boolean passportExists;
    private Boolean dlExists;
    private Boolean localReferanceExists;
    private Boolean ssNumberExists;
    private String dateTimeFormat;
    private String dateFormat;
    private List<String> reservePhnList;
    private int intNo;

    private Encounter unregisteringClinic;

    private DataFormset dataFormset;
    ClientEncounterComponentFormSet clientCefs;
    private DesignComponentFormSet clientDcfs;

    private SearchQueryData searchQueryData;

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Constructors">
    public ClientController() {
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Navigation">
    public String toSearchClientById() {
        userTransactionController.recordTransaction("To Search Client By Id");
        return "/client/search_by_id";
    }

    public String toSearchClientByDetails() {
        return "/client/search_by_details";

    }

    public String toSelectClient() {
        return "/client/select";
    }

    public String toSelectClientBasic() {
        return "/client/select_basic";
    }

    public String toClient() {
//        loadClientFormDataEntry();
        return "/client/client";
    }

    private List<FhirOperationResult> fhirOperationResults;
    private boolean pushComplete = false;

    public String pushToFhirServers() {
        CompletableFuture<List<FhirOperationResult>> futureResults
                = integrationTriggerController.createNewClientsToEndpoints(selected);
        futureResults.thenAccept(results -> {
            fhirOperationResults = results;
            pushComplete = true; // Mark the operation as complete
        });
        return "/client/push_result?faces-redirect=true"; // Navigate to the push_result page
    }

    public String checkPushComplete() {
        if (pushComplete) {
            return toClientProfile(); // Navigate to the client profile page
        }
        return null; // Stay on the current page
    }

    public boolean isPushComplete() {
        return pushComplete;
    }

    public void setPushComplete(boolean pushComplete) {
        this.pushComplete = pushComplete;
    }

    public String toRetireClient() {
        if (selected == null) {
            JsfUtil.addErrorMessage("No client is selected");
            return "";
        }
        selected.getPerson().setRetired(true);
        selected.getPerson().setRetiredAt(new Date());
        selected.getPerson().setRetiredBy(webUserController.getLoggedUser());
        selected.setRetired(true);
        selected.setRetiredAt(new Date());
        selected.setRetiredBy(webUserController.getLoggedUser());
        getFacade().edit(selected);
        return "/index";
    }

    public String toClientProfile() {
        selectedClientsLastFiveClinicVisits = null;
        updateYearDateMonth();
        selectedClientChanged();
        selectedClientsClinics = null;
        selectedClientsLastFiveClinicVisits = null;
//        loadClientFormDataEntry();
        userTransactionController.recordTransaction("To Client Profile");
        return "/client/profile";
    }

    public String toClientProfileById() {
        selected = getFacade().find(selectedId);
        if (selected == null) {
            JsfUtil.addErrorMessage("No such client");
            return "";
        }
        if (selected.getPerson() == null) {
            return "";
        }
        if (selected.getPerson().getGnArea() == null) {
        } else {
            if (selected.getPerson().getGnArea().getName() == null) {
            }else{
            }
        }
        if (selected.getPerson().getSex() == null) {
        } else {
            if(selected.getPerson().getSex().getName()==null){
            }else{
            }
            
        }
        selectedClientsClinics = null;
        selectedClientsLastFiveClinicVisits = null;
        userTransactionController.recordTransaction("To Client Profile");
        return "/client/profile";
    }

    public String toReserverPhn() {
        numberOfPhnToReserve = 0;
        return "/client/reserve_phn";
    }

    public String toAddNewClient() {
        setSelected(new Client());
        clearRegisterNewExistsValues();
        selectedClientsClinics = null;
        selectedClientsLastFiveClinicVisits = null;
        selectedClinic = null;
        yearMonthDay = new YearMonthDay();
        userTransactionController.recordTransaction("to add a new client");
        return "/client/client";
    }

    public String toViewCorrectedDuplicates() {
        String j;
        j = "select c"
                + " from Client c "
                + " where c.reservedClient <> true and c.comments is not null";
        items = getFacade().findByJpql(j);
        userTransactionController.recordTransaction("To View Corrected Duplicates");
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
        userTransactionController.recordTransaction("To Detect PHN Duplicates");
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
            // //System.out.println("dupPhn = " + dupPhn);
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
                        // //System.out.println("Duplicate PHN. Old PHN Stored as Local Ref");
                        // //System.out.println("c.getPhn()");
                        c.getPerson().setLocalReferanceNo(c.getPhn());
                        // //System.out.println("c.getPerson().getLocalReferanceNo() = " + c.getPerson().getLocalReferanceNo());
                        c.setPhn(generateNewPhn(c.getCreateInstitution()));
                        // //System.out.println("c.getPhn()");
                    } else if (c.getPerson().getSsNumber() == null || c.getPerson().getSsNumber().trim().equals("")) {
                        c.setComments("Duplicate PHN. Old PHN Stored as SC No");
                        // //System.out.println("Duplicate PHN. Old PHN Stored as SC No");
                        // //System.out.println("c.getPhn()");
                        c.getPerson().setSsNumber(c.getPhn());
                        // //System.out.println("c.getPerson().getSsNumber() = " + c.getPerson().getSsNumber());
                        c.setPhn(generateNewPhn(c.getCreateInstitution()));
                        // //System.out.println("c.getPhn()");
                    } else {
                        // //System.out.println("No Space to Store Old PHN");
                    }
                    getFacade().edit(c);
                }
                n++;
            }
            items.addAll(temClients);
        }
        userTransactionController.recordTransaction("Correct PHN Duplicates");
        return "/systemAdmin/clients_with_corrected_duplicate_phn";
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Functions">
    public void loadClientFormDataEntry() {
        if (selected == null) {
            return;
        }
        DesignComponentFormSet dfs = designComponentFormSetController.getClintFormSet(webUserController.getLoggedUser().getInstitution());
        if (dfs == null) {
            return;
        }
        ClientEncounterComponentFormSet cfs = clientEncounterComponentFormSetController.findClientEncounterFromset(
                getClientDcfs(),
                selected,
                webUserController.getLoggedUser().getInstitution(),
                Client_Data);

        if (cfs == null) {
            Encounter e = new Encounter();
            e.setClient(getSelected());
            e.setInstitution(webUserController.getLoggedUser().getInstitution());
            e.setEncounterType(EncounterType.Client_Data);
            encounterController.save(e);
            cfs = new ClientEncounterComponentFormSet();
            cfs.setCreatedAt(new Date());
            cfs.setCreatedBy(webUserController.getLoggedUser());
            cfs.setEncounter(e);
            cfs.setInstitution(dfs.getCurrentlyUsedIn());
            cfs.setReferenceComponent(dfs);
            cfs.setName(dfs.getName());
            cfs.setDescreption(dfs.getDescreption());
            cfs.setCss(dfs.getCss());
            clientEncounterComponentFormSetController.saveCfs(cfs);
        }
        DataFormset fs = new DataFormset();
        Encounter e = cfs.getEncounter();
        fs.setDfs(dfs);
        fs.setEfs(cfs);

        List<DesignComponentForm> dfList = designComponentFormController.fillFormsofTheSelectedSet(dfs);

        int formCounter = 0;

        for (DesignComponentForm df : dfList) {
            // //System.out.println("df = " + df.getName());

            boolean skipThisForm = false;
            if (df.getComponentSex() == ComponentSex.For_Females && getSelected().getPerson().getSex().getCode().equalsIgnoreCase("sex_male")) {
                skipThisForm = true;
            }
            if (df.getComponentSex() == ComponentSex.For_Males && getSelected().getPerson().getSex().getCode().equalsIgnoreCase("sex_female")) {
                skipThisForm = true;
            }

            if (!skipThisForm) {
                formCounter++;
                String j = "select cf "
                        + " from ClientEncounterComponentForm cf "
                        + " where cf.referenceComponent=:rf "
                        + " and cf.parentComponent=:cfs "
                        + "order by cf.id desc";
                Map m = new HashMap();
                m.put("rf", df);
                m.put("cfs", cfs);

                ClientEncounterComponentForm cf = clientEncounterComponentFormController.getClientEncounterComponentForm(j, m);

                // //System.out.println("cf = " + cf);
                if (cf == null) {
                    cf = new ClientEncounterComponentForm();

                    cf.setEncounter(e);
                    cf.setInstitution(dfs.getCurrentlyUsedIn());
                    cf.setItem(df.getItem());

                    cf.setReferenceComponent(df);
                    cf.setName(df.getName());
                    cf.setOrderNo(df.getOrderNo());
                    cf.setParentComponent(cfs);
                    cf.setCss(df.getCss());

                    clientEncounterComponentFormController.save(cf);
                }

                DataForm f = new DataForm();
                f.cf = cf;
                f.df = df;
                f.formset = fs;
                f.id = formCounter;
                f.orderNo = formCounter;

                List<DesignComponentFormItem> diList = designComponentFormItemController.fillItemsOfTheForm(df);

                int itemCounter = 0;

                for (DesignComponentFormItem dis : diList) {

                    // //System.out.println("dis = " + dis.getName());
                    boolean disSkipThisItem = false;
                    if (dis.getComponentSex() == ComponentSex.For_Females && getSelected().getPerson().getSex().getCode().equalsIgnoreCase("sex_male")) {
                        disSkipThisItem = true;
                    }
                    if (dis.getComponentSex() == ComponentSex.For_Males && getSelected().getPerson().getSex().getCode().equalsIgnoreCase("sex_female")) {
                        disSkipThisItem = true;
                    }

                    // //System.out.println("disSkipThisItem = " + disSkipThisItem);
                    if (!disSkipThisItem) {

                        if (dis.isMultipleEntiesPerForm()) {

                            // //System.out.println("dis.isMultipleEntiesPerForm() = " + dis.isMultipleEntiesPerForm());
                            j = "Select ci "
                                    + " from ClientEncounterComponentItem ci "
                                    + " where ci.retired=:ret "
                                    + " and ci.parentComponent=:cf "
                                    + " and ci.referenceComponent=:dis "
                                    + " order by ci.orderNo";
                            m = new HashMap();
                            m.put("ret", false);
                            m.put("cf", cf);
                            m.put("dis", dis);
                            // //System.out.println("cf = " + cf.getId());
                            // //System.out.println("dis = " + dis.getId());
                            List<ClientEncounterComponentItem> cis = clientEncounterComponentItemController.getItems(j, m);
                            // //System.out.println("cis = " + cis);

                            itemCounter++;
                            ClientEncounterComponentItem ci = new ClientEncounterComponentItem();

                            ci.setEncounter(e);
                            ci.setInstitution(dfs.getCurrentlyUsedIn());

                            ci.setItemFormset(cfs);
                            ci.setItemEncounter(e);
                            ci.setItemClient(e.getClient());

                            ci.setItem(dis.getItem());
                            ci.setDescreption(dis.getDescreption());

                            ci.setReferenceComponent(dis);
                            ci.setParentComponent(cf);
                            ci.setName(dis.getName());
                            ci.setCss(dis.getCss());
                            ci.setOrderNo(dis.getOrderNo());
                            ci.setDataRepresentationType(DataRepresentationType.Encounter);
                            DataItem i = new DataItem();
                            i.setMultipleEntries(true);
                            i.setCi(ci);
                            i.di = dis;
                            i.id = itemCounter;
                            i.orderNo = itemCounter;
                            i.form = f;
                            i.setAvailableItemsForSelection(itemController.findItemList(dis.getCategoryOfAvailableItems()));

                            if (cis != null && !cis.isEmpty()) {
                                for (ClientEncounterComponentItem tci : cis) {
                                    DataItem di = new DataItem();
                                    di.setMultipleEntries(true);
                                    di.setCi(tci);
                                    di.di = dis;
                                    di.id = itemCounter;
                                    di.orderNo = tci.getOrderNo();
                                    di.form = f;
                                    di.setAvailableItemsForSelection(itemController.findItemList(dis.getCategoryOfAvailableItems()));
                                    i.getAddedItems().add(di);
                                }
                            }

                            f.getItems().add(i);

                        } else {

                            j = "Select ci "
                                    + " from ClientEncounterComponentItem ci "
                                    + " where ci.retired=:ret "
                                    + " and ci.parentComponent=:cf "
                                    + " and ci.referenceComponent=:dis "
                                    + " order by ci.orderNo";
                            m = new HashMap();
                            m.put("ret", false);
                            m.put("cf", cf);
                            m.put("dis", dis);
                            // //System.out.println("cf = " + cf.getId());
                            // //System.out.println("dis = " + dis.getId());
                            ClientEncounterComponentItem ci;
                            ci = clientEncounterComponentItemController.getItem(j, m);
                            // //System.out.println("ci = " + ci);
                            if (ci != null) {
                                DataItem i = new DataItem();
                                i.setMultipleEntries(false);
                                i.setCi(ci);
                                i.di = dis;
                                i.id = itemCounter;
                                i.orderNo = itemCounter;
                                i.form = f;
                                i.setAvailableItemsForSelection(itemController.findItemList(dis.getCategoryOfAvailableItems()));

                                f.getItems().add(i);
                            } else {
                                itemCounter++;
                                ci = new ClientEncounterComponentItem();
                                ci.setEncounter(e);
                                ci.setInstitution(dfs.getCurrentlyUsedIn());
                                ci.setItemFormset(cfs);
                                ci.setItemEncounter(e);
                                ci.setItemClient(e.getClient());
                                ci.setItem(dis.getItem());
                                ci.setDescreption(dis.getDescreption());
                                ci.setReferenceComponent(dis);
                                ci.setParentComponent(cf);
                                ci.setName(dis.getName());
                                ci.setCss(dis.getCss());
                                ci.setOrderNo(dis.getOrderNo());
                                ci.setDataRepresentationType(DataRepresentationType.Encounter);

                                DataItem i = new DataItem();
                                i.setMultipleEntries(false);
                                i.setCi(ci);
                                i.di = dis;
                                i.id = itemCounter;
                                i.orderNo = itemCounter;
                                i.form = f;
                                i.setAvailableItemsForSelection(itemController.findItemList(dis.getCategoryOfAvailableItems()));

                                f.getItems().add(i);
                            }

                        }

                    }

                }
                fs.getForms().add(f);
            }

        }

        dataFormset = fs;
        clientCefs = cfs;
        clientDcfs = dfs;

    }

    public List<Area> completeClientsGnArea(String qry) {
        List<Area> areas = new ArrayList<>();
        if (selected == null) {
            return areas;
        }
        if (selected.getPerson().getDsArea() == null) {
            return areaApplicationController.completeGnAreas(qry);
        } else {
            return areaApplicationController.completeGnAreas(qry, selected.getPerson().getDsArea());
        }
    }

    public void clearRegisterNewExistsValues() {
        phnExists = false;
        nicExists = false;
        emailExists = false;
        phone1Exists = false;
        ssNumberExists = false;
    }

    public void clearExistsValues() {
        phnExists = false;
        nicExists = false;
        passportExists = false;
        dlExists = false;
    }

    public void checkPhnExists() {
        phnExists = null;
        if (selected == null) {
            return;
        }
        if (selected.getPhn() == null) {
            return;
        }
        if (selected.getPhn().trim().equals("")) {
            return;
        }
        phnExists = checkPhnExists(selected.getPhn(), selected);
    }

    public Boolean checkPhnExists(String phn, Client c) {
        String jpql = "select count(c) from Client c "
                + " where c.retired=:ret "
                + " and c.phn=:phn ";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("phn", phn);
        if (c != null && c.getId() != null) {
            jpql += " and c <> :client";
            m.put("client", c);
        }
        Long count = getFacade().countByJpql(jpql, m);
        if (count == null || count == 0l) {
            return false;
        } else {
            return true;
        }
    }

    public void checkNicExists() {
        nicExists = null;
        if (selected == null) {
            return;
        }
        if (selected.getPerson() == null) {
            return;
        }
        if (selected.getPerson().getNic() == null) {
            return;
        }
        if (selected.getPerson().getNic().trim().equals("")) {
            return;
        }
        nicExists = checkNicExists(selected.getPerson().getNic(), selected);
    }

    public Boolean checkNicExists(String nic, Client c) {
        String jpql = "select count(c) from Client c "
                + " where c.retired=:ret "
                + " and c.reservedClient<>:res "
                + " and c.person.nic=:nic ";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("res", true);
        m.put("nic", nic);
        if (c != null && c.getPerson() != null && c.getPerson().getId() != null) {
            jpql += " and c.person <> :person";
            m.put("person", c.getPerson());
        }
        Long count = getFacade().countByJpql(jpql, m);
        if (count == null || count == 0l) {
            return false;
        } else {
            return true;
        }

    }

    public void addCreatedDateFromCreatedAt() {
        String j = "select c from Client c where c.createdOn is null";
        List<Client> cs = getFacade().findByJpql(j, 1000);
        // //System.out.println("cs.getSize() = " + cs.size());
        for (Client c : cs) {
            if (c.getCreatedOn() == null) {
                c.setCreatedOn(c.getCreatedAt());
                getFacade().edit(c);
            }
        }
    }

    public void checkEmailExists() {
        emailExists = null;
        if (selected == null) {
            return;
        }
        if (selected.getPerson() == null) {
            return;
        }
        if (selected.getPerson().getEmail() == null) {
            return;
        }
        if (selected.getPerson().getEmail().trim().equals("")) {
            return;
        }
        emailExists = checkEmailExists(selected.getPerson().getEmail(), selected);
    }

    public Boolean checkEmailExists(String email, Client c) {
        String jpql = "select count(c) from Client c "
                + " where c.retired=:ret "
                + " and c.reservedClient<>:res "
                + " and c.person.email=:email ";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("res", true);
        m.put("email", email);
        if (c != null && c.getPerson() != null && c.getPerson().getId() != null) {
            jpql += " and c.person <> :person";
            m.put("person", c.getPerson());
        }
        Long count = getFacade().countByJpql(jpql, m);
        if (count == null || count == 0l) {
            return false;
        } else {
            return true;
        }

    }

    public void checkPhone1Exists() {
        phone1Exists = null;
        if (selected == null) {
            return;
        }
        if (selected.getPerson() == null) {
            return;
        }
        if (selected.getPerson().getPhone1() == null) {
            return;
        }
        if (selected.getPerson().getPhone1().trim().equals("")) {
            return;
        }
        phone1Exists = checkPhone1Exists(selected.getPerson().getPhone1(), selected);
    }

    public Boolean checkPhone1Exists(String phone1, Client c) {
        String jpql = "select count(c) from Client c "
                + " where c.retired=:ret "
                + " and c.reservedClient<>:res "
                + " and c.person.phone1=:phone1 ";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("res", true);
        m.put("phone1", phone1);
        if (c != null && c.getPerson() != null && c.getPerson().getId() != null) {
            jpql += " and c.person <> :person";
            m.put("person", c.getPerson());
        }
        Long count = getFacade().countByJpql(jpql, m);
        if (count == null || count == 0l) {
            return false;
        } else {
            return true;
        }

    }

    public void checkSsNumberExists() {
        ssNumberExists = null;
        if (selected == null) {
            return;
        }
        if (selected.getPerson() == null) {
            return;
        }
        if (selected.getPerson().getSsNumber() == null) {
            return;
        }
        if (selected.getPerson().getSsNumber().trim().equals("")) {
            return;
        }
        ssNumberExists = checkSsNumberExists(selected.getPerson().getSsNumber(), selected);
    }

    public Boolean checkSsNumberExists(String ssNumber, Client c) {
        String jpql = "select count(c) from Client c "
                + " where c.retired=:ret "
                + " and c.reservedClient<>:res "
                + " and c.person.ssNumber=:ssNumber ";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("res", true);
        m.put("ssNumber", ssNumber);
        if (c != null && c.getPerson() != null && c.getPerson().getId() != null) {
            jpql += " and c.person <> :person";
            m.put("person", c.getPerson());
        }
        Long count = getFacade().countByJpql(jpql, m);
        if (count == null || count == 0l) {
            return false;
        } else {
            return true;
        }

    }

    public void fixClientPersonCreatedAt() {
        String j = "select c from Client c "
                + " where c.retired=:ret "
                + " and c.reservedClient<>:res ";

        Map m = new HashMap();
        m.put("ret", false);
        m.put("res", true);
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
        userTransactionController.recordTransaction("Fix Client Person Created At");
    }

    public void updateClientCreatedInstitution() {
        if (institution == null) {
            JsfUtil.addErrorMessage("Institution ?");
            return;
        }
        String j = "select c from Client c "
                + " where c.retired=:ret "
                + " and c.createInstitution=:ins";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("ins", institution);
        List<Client> cs = getFacade().findByJpql(j, m);
        for (Client c : cs) {
            if (c.getCreatedBy() != null && c.getCreatedBy().getInstitution() != null) {
                c.setCreateInstitution(c.getCreatedBy().getInstitution());
                c.setPoiInstitution(institution);
                getFacade().edit(c);
            }

        }
        userTransactionController.recordTransaction("Update Client Created Institution");
    }

    public void updateClientDateOfBirth() {
        String j = "select c from Client c "
                + " where c.retired=:ret "
                + " and c.reservedClient<>:res "
                + " and c.id > :idf "
                + " and c.id < :idt ";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("res", true);
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
        userTransactionController.recordTransaction("Update Client Date Of Birth");
    }

    public Long countOfRegistedClients(Institution ins, Area gn) {
        String j = "select count(c) from Client c "
                + " where c.retired=:ret "
                + " and c.reservedClient<>:res ";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("res", true);
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
                + " where c.retired=:ret "
                + " and c.reservedClient<>:res ";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("res", true);
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
        String j = "select c from Client c "
                + " where c.retired=:ret "
                + " and c.reservedClient<>:res ";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("res", true);
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
        userTransactionController.recordTransaction("To Registerd Clients With Dates For SystemAdmin");
        return "/systemAdmin/all_clients";
    }

    public void fillClients() {
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
                + " and c.reservedClient<>:res ";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("res", true);
        j = j + " and c.createdAt between :fd and :td ";
        j = j + " order by c.id desc";
        m.put("fd", getFrom());
        m.put("td", getTo());
        selectedClientsBasic = null;
        clients = getFacade().findByJpql(j, m, TemporalType.TIMESTAMP);
        userTransactionController.recordTransaction("Fill Clients - SysAdmin");
    }

    public void fillRegisterdClientsWithDatesForInstitution() {
        String j = "select c from Client c "
                + " where c.retired<>:ret "
                + " and c.reservedClient<>:res ";
        Map m = new HashMap();
        m.put("ret", true);
        m.put("res", true);
        j = j + " and c.createdAt between :fd and :td ";

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
        String j = "select c from Client c where length(c.phn) <>11 and reservedClient<>true order by c.id";
        items = getFacade().findByJpql(j);
        userTransactionController.recordTransaction("Fill Clients With Wrong PHN Length");
    }

    public void fillRetiredClients() {
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
                + " and c.reservedClient<>:res ";
        Map m = new HashMap();
        m.put("ret", true);
        m.put("res", true);
        j = j + " and c.createdAt between :fd and :td ";
        j = j + " order by c.id desc";
        m.put("fd", getFrom());
        m.put("td", getTo());
        selectedClientsBasic = null;
        clients = getFacade().findByJpql(j, m, TemporalType.TIMESTAMP);
        userTransactionController.recordTransaction("Fill Retired Clients - SysAdmin");
    }

    public String retireSelectedClients() {
        for (ClientBasicData cb : selectedClientsBasic) {

            Client c = getFacade().find(cb.getId());

            if (c == null) {
                continue;
            };

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
        userTransactionController.recordTransaction("Retire Selected Clients - SysAdmin");
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
        userTransactionController.recordTransaction("Unretire Selected Clients - SysAdmin");
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
//    
//    public String importClientsFromExcel() {
//
//        importedClients = new ArrayList<>();
//
//        if (institution == null) {
//            JsfUtil.addErrorMessage("Add Institution");
//            return "";
//        }
//
//        if (uploadDetails == null || uploadDetails.trim().equals("")) {
//            JsfUtil.addErrorMessage("Add Column Names");
//            return "";
//        }
//
//        String[] cols = uploadDetails.split("\\r?\\n");
//        if (cols == null || cols.length < 5) {
//            JsfUtil.addErrorMessage("No SUfficient Columns");
//            return "";
//        }
//
//        try {
//            File inputWorkbook;
//            Workbook w;
//            Cell cell;
//            InputStream in;
//            try {
//                in = file.getInputStream();
//                File f;
//                f = new File(Calendar.getInstance().getTimeInMillis() + file.getFileName());
//                FileOutputStream out = new FileOutputStream(f);
//                int read = 0;
//                byte[] bytes = new byte[1024];
//                while ((read = in.read(bytes)) != -1) {
//                    out.write(bytes, 0, read);
//                }
//                in.close();
//                out.flush();
//                out.close();
//
//                inputWorkbook = new File(f.getAbsolutePath());
//
//                JsfUtil.addSuccessMessage("Excel File Opened");
//                w = Workbook.getWorkbook(inputWorkbook);
//                Sheet sheet = w.getSheet(0);
//
//                errorCode = "";
//
//                int startRow = 1;
//
//                Long temId = 0L;
//
//                for (int i = startRow; i < sheet.getRows(); i++) {
//
//                    Map m = new HashMap();
//
//                    Client c = new Client();
//                    Person p = new Person();
//                    c.setPerson(p);
//
//                    int colNo = 0;
//
//                    String gnAreaName = null;
//                    String gnAreaCode = null;
//                    for (String colName : cols) {
//                        cell = sheet.getCell(colNo, i);
//                        String cellString = cell.getContents();
//                        switch (colName) {
//                            case "client_gn_area_name":
//                                gnAreaName = cellString;
//                                break;
//                            case "client_gn_area_code":
//                                gnAreaCode = cellString;
//                                break;
//                        }
//                        colNo++;
//                    }
//                    Area gnArea = null;
////                    //// //System.out.println("gnAreaName = " + gnAreaName);
////                    //// //System.out.println("gnAreaCode = " + gnAreaCode);
//                    if (gnAreaName != null && gnAreaCode != null) {
////                        gnArea = areaController.getGnAreaByNameAndCode(gnAreaName, gnAreaCode);
//                    } else if (gnAreaName != null) {
//                        gnArea = areaController.getGnAreaByName(gnAreaName);
//                    } else if (gnAreaCode != null) {
//                        gnArea = areaController.getGnAreaByCode(gnAreaCode);
//                    }
//                    if (gnArea != null) {
////                        //// //System.out.println("gnArea = " + gnArea.getName());
//                    }
//
//                    colNo = 0;
//
//                    for (String colName : cols) {
//                        cell = sheet.getCell(colNo, i);
//                        String cellString = cell.getContents();
//                        switch (colName) {
//                            case "client_name":
//                                c.getPerson().setName(cellString);
//                                break;
//                            case "client_phn_number":
//                                c.setPhn(cellString);
//                                break;
//                            case "client_sex":
//                                Item sex;
//                                if (cellString.toLowerCase().contains("f")) {
//                                    sex = itemController.findItemByCode("sex_female");
//                                } else {
//                                    sex = itemController.findItemByCode("sex_male");
//                                }
//                                c.getPerson().setSex(sex);
//                                break;
//                            case "client_citizenship":
//                                Item cs;
//                                if (cellString == null) {
//                                    cs = null;
//                                } else if (cellString.toLowerCase().contains("sri")) {
//                                    cs = itemController.findItemByCode("citizenship_local");
//                                } else {
//                                    cs = itemController.findItemByCode("citizenship_foreign");
//                                }
//                                c.getPerson().setCitizenship(cs);
//                                break;
//
//                            case "client_ethnic_group":
//                                Item eg = null;
//                                if (cellString == null || cellString.trim().equals("")) {
//                                    eg = null;
//                                } else if (cellString.equalsIgnoreCase("Sinhala")) {
//                                    eg = itemController.findItemByCode("sinhalese");
//                                } else if (cellString.equalsIgnoreCase("moors")) {
//                                    eg = itemController.findItemByCode("citizenship_local");
//                                } else if (cellString.equalsIgnoreCase("SriLankanTamil")) {
//                                    eg = itemController.findItemByCode("tamil");
//                                } else {
//                                    eg = itemController.findItemByCode("ethnic_group_other");;
//                                }
//                                c.getPerson().setEthinicGroup(eg);
//                                break;
//                            case "client_religion":
//                                Item re = null;
//                                if (cellString == null || cellString.trim().equals("")) {
//                                    re = null;
//                                } else if (cellString.equalsIgnoreCase("Buddhist")) {
//                                    re = itemController.findItemByCode("buddhist");
//                                } else if (cellString.equalsIgnoreCase("Christian")) {
//                                    re = itemController.findItemByCode("christian");
//                                } else if (cellString.equalsIgnoreCase("Hindu")) {
//                                    re = itemController.findItemByCode("hindu");
//                                } else {
//                                    re = itemController.findItemByCode("religion_other");;
//                                }
//                                c.getPerson().setReligion(re);
//                                break;
//                            case "client_marital_status":
//                                Item ms = null;
//                                if (cellString == null || cellString.trim().equals("")) {
//                                    ms = null;
//                                } else if (cellString.equalsIgnoreCase("Married")) {
//                                    ms = itemController.findItemByCode("married");
//                                } else if (cellString.equalsIgnoreCase("Separated")) {
//                                    ms = itemController.findItemByCode("seperated");
//                                } else if (cellString.equalsIgnoreCase("Single")) {
//                                    ms = itemController.findItemByCode("unmarried");
//                                } else {
//                                    ms = itemController.findItemByCode("marital_status_other");;
//                                }
//                                c.getPerson().setMariatalStatus(ms);
//                                break;
//                            case "client_title":
//                                Item title = null;
//                                String ts = cellString;
//                                switch (ts) {
//                                    case "Baby":
//                                        title = itemController.findItemByCode("baby");
//                                        break;
//                                    case "Babyof":
//                                        title = itemController.findItemByCode("baby_of");
//                                        break;
//                                    case "Mr":
//                                        title = itemController.findItemByCode("mr");
//                                        break;
//                                    case "Mrs":
//                                        title = itemController.findItemByCode("mrs");
//                                        break;
//                                    case "Ms":
//                                        title = itemController.findItemByCode("ms");
//                                        break;
//                                    case "Prof":
//                                        title = itemController.findItemByCode("prof");
//                                        break;
//                                    case "Rev":
//                                    case "Thero":
//                                        title = itemController.findItemByCode("rev");
//                                        break;
//                                }
//                                c.getPerson().setTitle(title);
//                                break;
//                            case "client_nic_number":
//                                c.getPerson().setNic(cellString);
//                                break;
//                            case "client_data_of_birth":
//                                Date tdob = null;
//                                Date today = new Date();
//                                int ageInYears = 0;
//                                int birthYear;
//                                int thisYear;
//
//                                try {
//                                    tdob = commonController.dateFromString(cellString, dateFormat);
//                                    Calendar bc = Calendar.getInstance();
//                                    bc.setTime(tdob);
//                                    birthYear = bc.get(Calendar.YEAR);
//                                    Calendar tc = Calendar.getInstance();
//                                    thisYear = tc.get(Calendar.YEAR);
//                                    ageInYears = thisYear - birthYear;
////                                    //// //System.out.println("ageInYears = " + ageInYears);
//                                } catch (Exception e) {
////                                    //// //System.out.println("e = " + e);
//                                }
//                                if (ageInYears < 0) {
//                                    tdob = today;
//                                } else if (ageInYears > 200) {
//                                    tdob = today;
//                                }
//
//                                c.getPerson().setDateOfBirth(tdob);
//                                break;
//                            case "client_permanent_address":
//                                c.getPerson().setAddress(cellString);
//                                break;
//                            case "client_current_address":
//                                c.getPerson().setAddress(cellString);
//                                break;
//                            case "client_mobile_number":
//                                c.getPerson().setPhone1(cellString);
//                                break;
//                            case "client_home_number":
//                                c.getPerson().setPhone2(cellString);
//                                break;
//                            case "client_registered_at":
//                                Date reg = commonController.dateFromString(cellString, dateTimeFormat);
//                                c.getPerson().setCreatedAt(reg);
//                                c.setCreatedAt(reg);
//                                break;
//                            case "client_gn_area":
//                                //// //System.out.println("GN");
//                                //// //System.out.println("cellString = " + cellString);
//
//                                Area tgn;
//                                if (gnArea == null) {
////                                    gnArea = areaController.getAreaByCodeIfNotName(cellString, AreaType.GN);
//                                }
//
//                                break;
//                        }
//
//                        colNo++;
//                    }
//
//                    //// //System.out.println("tgn = " + gnArea);
//                    if (gnArea != null) {
//                        c.getPerson().setGnArea(gnArea);
//                        c.getPerson().setDsArea(gnArea.getDsd());
//                        c.getPerson().setMohArea(gnArea.getMoh());
//                        c.getPerson().setPhmArea(gnArea.getPhm());
//                        c.getPerson().setDistrict(gnArea.getDistrict());
//                        c.getPerson().setProvince(gnArea.getProvince());
//                    }
//                    c.setCreateInstitution(institution);
//
//                    c.setId(temId);
//                    temId++;
//
//                    importedClients.add(c);
//
//                }
//
//                lk.gov.health.phsp.facade.util.JsfUtil.addSuccessMessage("Succesful. All the data in Excel File Impoted to the database");
//                errorCode = "";
//                return "save_imported_clients";
//            } catch (IOException ex) {
//                errorCode = ex.getMessage();
//                lk.gov.health.phsp.facade.util.JsfUtil.addErrorMessage(ex.getMessage());
//                return "";
//            } catch (BiffException ex) {
//                lk.gov.health.phsp.facade.util.JsfUtil.addErrorMessage(ex.getMessage());
//                errorCode = ex.getMessage();
//                return "";
//            }
//        } catch (IndexOutOfBoundsException e) {
//            errorCode = e.getMessage();
//            return "";
//        }
//    }
    public void prepareToCapturePhotoWithWebCam() {
        goingToCaptureWebCamPhoto = true;
    }

    public void finishCapturingPhotoWithWebCam() {
        goingToCaptureWebCamPhoto = false;
    }

    public void onTabChange(TabChangeEvent event) {

        // ////// //System.out.println("profileTabActiveIndex = " + profileTabActiveIndex);
        TabView tabView = (TabView) event.getComponent();

        profileTabActiveIndex = tabView.getChildren().indexOf(event.getTab());

    }

    public List<Encounter> fillEncounters(Client client, InstitutionType insType, EncounterType encType, boolean excludeCompleted, Integer maxRecordCount) {
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
        if (maxRecordCount == null) {
            return encounterFacade.findByJpql(j, m);
        } else {
            return encounterFacade.findByJpql(j, m, maxRecordCount);
        }

    }

    public List<Encounter> fillEncounters(Client client, List<InstitutionType> insTypes, EncounterType encType, boolean excludeCompleted, Integer maxRecordCount) {
        String j = "select e from Encounter e where e.retired=false ";
        Map m = new HashMap();
        if (client != null) {
            j += " and e.client=:c ";
            m.put("c", client);
        }
        if (insTypes != null) {
            j += " and e.institution.institutionType in :it ";
            m.put("it", insTypes);
        }
        if (insTypes != null) {
            j += " and e.encounterType=:et ";
            m.put("et", encType);
        }
        if (excludeCompleted) {
            j += " and e.completed=:com ";
            m.put("com", false);
        }
        if (maxRecordCount == null) {
            return encounterFacade.findByJpql(j, m);
        } else {
            return encounterFacade.findByJpql(j, m, maxRecordCount);
        }

    }

    public List<Encounter> fillEncounters(Client client, List<InstitutionType> insTypes, EncounterType encType, boolean excludeCompleted, Integer maxRecordCount, boolean descending) {
        String j = "select e from Encounter e where e.retired=false ";
        Map m = new HashMap();
        if (client != null) {
            j += " and e.client=:c ";
            m.put("c", client);
        }
        if (insTypes != null) {
            j += " and e.institution.institutionType in :it ";
            m.put("it", insTypes);
        }
        if (insTypes != null) {
            j += " and e.encounterType=:et ";
            m.put("et", encType);
        }
        if (excludeCompleted) {
            j += " and e.completed=:com ";
            m.put("com", false);
        }
        if (descending) {
            j += " order by e.id desc";
        }
        if (maxRecordCount == null) {
            return encounterFacade.findByJpql(j, m);
        } else {
            return encounterFacade.findByJpql(j, m, maxRecordCount);
        }

    }

    public List<Encounter> fillEncounters(Client client, InstitutionType insType, EncounterType encType, boolean excludeCompleted) {
        return fillEncounters(client, insType, encType, true, null);
    }

    public List<Encounter> fillEncounters(Client client, List<InstitutionType> insTypes, EncounterType encType, boolean excludeCompleted) {
        return fillEncounters(client, insTypes, encType, true, null);
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
        if (selected == null) {
            return;
        }
        Institution poiIns;
        if (webUserController.getLoggedUser().getInstitution() == null) {
            JsfUtil.addErrorMessage("You do not have an Institution. Please contact support.");
            return;
        }
        //// //System.out.println("webUserController.getLoggedUser().getInstitution() = " + webUserController.getLoggedUser().getInstitution().getLastHin());
        if (webUserController.getLoggedUser().getInstitution().getPoiInstitution() != null) {
            poiIns = webUserController.getLoggedUser().getInstitution().getPoiInstitution();
        } else {
            poiIns = webUserController.getLoggedUser().getInstitution();
        }
        if (poiIns.getPoiNumber() == null || poiIns.getPoiNumber().trim().equals("")) {
            Institution moh = institutionApplicationController.findMinistryOfHealth();
            poiIns = moh;
            if (poiIns.getPoiNumber() == null || poiIns.getPoiNumber().trim().equals("")) {
                poiIns.setPoiNumber("385C");
            }
            return;
        }
        selected.setPhn(applicationController.createNewPersonalHealthNumberformat(poiIns));
    }

    public String generateNewPhn(Institution ins) {
        Institution poiIns;
        if (ins == null) {
            // //System.out.println("Ins is null");
            return null;
        }
        if (ins.getPoiInstitution() != null) {
            poiIns = ins.getPoiInstitution();
        } else {
            poiIns = ins;
        }
        if (poiIns.getPoiNumber() == null || poiIns.getPoiNumber().trim().equals("")) {
            // //System.out.println("A Point of Issue is NOT assigned to the Institution. Please discuss with the System Administrator.");
            return null;
        }
        return applicationController.createNewPersonalHealthNumberformat(poiIns);
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
        // ////// ////// //System.out.println("year string is " + docStr);
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
            ////// ////// //System.out.println("Error is " + e.getMessage());
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
            setSelected(selectedClients.get(0));
            selectedClients = null;
            clearSearchById();
            if (selected.isReservedClient()) {
                return "/client/client";
            }
            return toClientProfile();
        } else {
            selected = null;
            clearSearchById();
            return toSelectClient();
        }
    }

    public String searchByNic() {
        if (searchingNicNo == null || searchingNicNo.trim().equals("")) {
            JsfUtil.addErrorMessage("Please enter a NIC to Search");
            return "";
        }
        selectedClients = listPatientsByNic(searchingNicNo);
        selectedClientsFromIntegrations = new ArrayList<>();
        fhirOperationResults = new ArrayList<>(); // Initialize the list to store FhirOperationResult objects

        searchQueryData = new SearchQueryData();
        searchQueryData.setSearchCriteria(SearchCriteria.NIC_ONLY);
        searchQueryData.setNic(searchingNicNo);

        CompletableFuture<List<Client>> futureClients = integrationTriggerController.fetchClientsFromEndpoints(searchQueryData);
        futureClients.thenAccept(clients -> {
            if (clients != null && !clients.isEmpty()) {
                selectedClientsFromIntegrations.addAll(clients);
            } else {
            }
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });

        // Do something with the fhirOperationResults list, if needed
        return toSelectClient();
    }

    public String searchBySearchQueryData() {
        if (searchQueryData == null) {
            JsfUtil.addErrorMessage("Error");
            return null;
        }
        if (searchQueryData.getSearchCriteria() == null) {
            JsfUtil.addErrorMessage("Please select a search criteria");
            return null;
        }

        selectedClientsFromIntegrations = new ArrayList<>();
        selectedClients = new ArrayList<>();

        switch (searchQueryData.getSearchCriteria()) {
            case NIC_ONLY:
                selectedClients = listPatientsByNic(searchQueryData.getNic());
                break;
            case DL_ONLY:
                selectedClients = listPatientsByDrivingLicenseNo(searchQueryData.getDl());
                break;
            case PASSPORT_ONLY:
                selectedClients = listPatientsByPassportNo(searchQueryData.getPassport());
                break;
            case PHN_ONLY:
                selectedClients = listPatientsByPhn(searchQueryData.getPhn());
                break;
            case SCN_ONLY:
                selectedClients = listPatientsByScn(searchQueryData.getScn());
                break;
            case TELEPHONE_NUMBER_ONLY:
                selectedClients = listPatientsByPhone(searchQueryData.getPhone());
                break;
            case PART_OF_NAME_AND_DATE_OF_BIRTH:
                selectedClients = listPatientsByNameAndDateOfBirth(searchQueryData.getName(), searchQueryData.getDateOfBirth());
                break;
            case PART_OF_NAME_AND_AGE_IN_YEARS:

                break;
            case PART_OF_NAME_AND_BIRTH_YEAR:
                selectedClients = listPatientsByNameAndYearOfBirth(searchQueryData.getName(), searchQueryData.getBirthYear());
                break;
            case PART_OF_NAME_AND_BIRTH_YEAR_AND_MONTH:
                selectedClients = listPatientsByNameAndYearOfBirthAndMonth(searchQueryData.getName(), searchQueryData.getBirthYear(), searchQueryData.getBirthMonth());
                break;

        }

        fhirOperationResults = new ArrayList<>(); // Initialize the list to store FhirOperationResult objects

        CompletableFuture<List<Client>> futureClients = integrationTriggerController.fetchClientsFromEndpoints(searchQueryData);
        futureClients.thenAccept(clients -> {
            if (clients != null && !clients.isEmpty()) {
                selectedClientsFromIntegrations.addAll(clients);
            } else {
            }
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });

        // Do something with the fhirOperationResults list, if needed
        return toSelectClient();
    }

    public String searchByPhoneNumber() {
        selectedClients = listPatientsByPhone(searchingPhoneNumber);
        if (selectedClients.size() == 1) {
            setSelected(selectedClients.get(0));
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
            setSelected(selectedClients.get(0));
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
            setSelected(selectedClients.get(0));
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
            setSelected(selectedClients.get(0));
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
        selectedClients = listPatientsByScn(searchingSsNumber);
        if (selectedClients.size() == 1) {
            setSelected(selectedClients.get(0));
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
            selectedClients.addAll(listPatientsByScn(searchingSsNumber));
        }

        if (selectedClients == null || selectedClients.isEmpty()) {
            JsfUtil.addErrorMessage("No Results Found. Try different search criteria.");
            return "";
        }
        if (selectedClients.size() == 1) {
            setSelected(selectedClients.get(0));
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
            selectedClients = listPatientsByScn(searchingSsNumber);
        }
        if (selectedClients == null || selectedClients.isEmpty()) {
            JsfUtil.addErrorMessage("No Results Found. Try different search criteria.");
            return "";
        }
        if (selectedClients.size() == 1) {
            setSelected(selectedClients.get(0));
            selectedClients = null;
            clearSearchById();
            return toClientProfile();
        } else {
            selected = null;
            clearSearchById();
            return toSelectClient();
        }
    }

    public String searchByAnyIdWithBasicData() {
//        System.out.println("searchByAnyIdWithBasicData 1 = " + new Date().getTime());
        userTransactionController.recordTransaction("Search By Any Id");
        clearExistsValues();
        if (searchingId == null) {
            searchingId = "";
        }
//        System.out.println("searchByAnyIdWithBasicData 2 = " + new Date().getTime());
        selectedClientsWithBasicData = listPatientsByIDsWithBasicData(searchingId.trim().toUpperCase());
//        System.out.println("searchByAnyIdWithBasicData 3 = " + new Date().getTime());
        if (selectedClientsWithBasicData == null || selectedClientsWithBasicData.isEmpty()) {
            JsfUtil.addErrorMessage("No Results Found. Try different search criteria.");
            userTransactionController.recordTransaction("Search By Any Id Failed as no match");
//            System.out.println("searchByAnyIdWithBasicData 4 = " + new Date().getTime());
            return "/client/search_by_id";
        }
        if (selectedClientsWithBasicData.size() == 1) {
            selected = getFacade().find(selectedClientsWithBasicData.get(0).getId());
            selectedClients = null;
            searchingId = "";
            userTransactionController.recordTransaction("Search By Any Id returend single match");
//            System.out.println("searchByAnyIdWithBasicData 5 = " + new Date().getTime());
            return toClientProfile();
        } else {
            selected = null;
            searchingId = "";
            userTransactionController.recordTransaction("Search By Any Id returned multiple matches");
//            System.out.println("searchByAnyIdWithBasicData 6 = " + new Date().getTime());
            return toSelectClientBasic();
        }
    }

    public String searchByPhnWithBasicData() {
        // //System.out.println("searchByPhnWithBasicData");
        userTransactionController.recordTransaction("Search By PHN");
        clearExistsValues();
        if (searchingId == null) {
            searchingId = "";
        }

        selectedClientsWithBasicData = listPatientsByPhnWithBasicData(searchingId.trim().toUpperCase());

        if (selectedClientsWithBasicData == null || selectedClientsWithBasicData.isEmpty()) {
            JsfUtil.addErrorMessage("No Results Found. Try different search criteria.");
            userTransactionController.recordTransaction("Search By Any Id Failed as no match");
            return "/client/search_by_id";
        }
        if (selectedClientsWithBasicData.size() == 1) {
            selected = getFacade().find(selectedClientsWithBasicData.get(0).getId());
            selectedClients = null;
            searchingId = "";
            userTransactionController.recordTransaction("Search By Any Id returend single match");
            return toClientProfile();
        } else {
            selected = null;
            searchingId = "";
            userTransactionController.recordTransaction("Search By Any Id returned multiple matches");
            return toSelectClientBasic();
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
            userTransactionController.recordTransaction("Search By Any Id");
            return "/client/search_by_id";
        }
        if (selectedClients.size() == 1) {
            setSelected(selectedClients.get(0));
            selectedClients = null;
            searchingId = "";
            userTransactionController.recordTransaction("Search By Any Id");
            return toClientProfile();
        } else {
            selected = null;
            searchingId = "";
            userTransactionController.recordTransaction("Search By Any Id");
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
        String j = "select c from Client c where c.retired=false and lower(c.phn)=:q";
        Map m = new HashMap();
        m.put("q", phn.trim().toLowerCase());
        return getFacade().findByJpql(j, m);
    }

    public List<Client> listPatientsByNic(String phn) {
        String j = "select c "
                + " from Client c "
                + " where c.retired=false "
                + " and c.reservedClient<>:res "
                + " and lower(c.person.nic)=:q ";
        Map m = new HashMap();
        m.put("res", true);
        m.put("q", phn.trim().toLowerCase());
        return getFacade().findByJpql(j, m);
    }

    public List<Client> listPatientsByPhone(String phn) {
        String j = "select c from Client c where c.retired=false and c.reservedClient<>:res and (c.person.phone1=:q or c.person.phone2=:q) order by c.phn";
        Map m = new HashMap();
        m.put("res", true);
        m.put("q", phn.trim());
        return getFacade().findByJpql(j, m);
    }

    public List<Client> listPatientsByNameAndDateOfBirth(String name, Date dob) {
        String j = "select c "
                + " from Client c "
                + " where c.retired=false "
                + " and (c.reservedClient is null or c.reservedClient<>:res) "
                + " and lower(c.person.name) like :n "
                + " and c.person.dateOfBirth=:dob"; // Changed ordering
        Map m = new HashMap();
        m.put("res", true);
        m.put("n", "%" + name.trim().toLowerCase() + "%");
        m.put("dob", dob);
        return getFacade().findByJpql(j, m);
    }

    public List<Client> listPatientsByNameAndYearOfBirth(String name, Integer yob) {
        String j = "select c "
                + " from Client c "
                + " where c.retired=false "
                + " and (c.reservedClient is null or c.reservedClient<>:res) "
                + " and lower(c.person.name) like :n "
                + " and FUNCTION('YEAR', c.person.dateOfBirth) = :yob" // Extracting the year from dateOfBirth
                + " order by c.person.name"; // Changed ordering
        Map m = new HashMap();
        m.put("res", true);
        m.put("n", "%" + name.trim().toLowerCase() + "%");
        m.put("yob", yob); // Using yob for the year of birth
        return getFacade().findByJpql(j, m);
    }

    public List<Client> listPatientsByNameAndYearOfBirthAndMonth(String name, Integer yob, Integer mob) {
        String j = "select c "
                + " from Client c "
                + " where c.retired=false "
                + " and (c.reservedClient is null or c.reservedClient<>:res) "
                + " and lower(c.person.name) like :n "
                + " and FUNCTION('YEAR', c.person.dateOfBirth) = :yob" // Extracting the year from dateOfBirth
                + " and FUNCTION('MONTH', c.person.dateOfBirth) = :mob" // Extracting the month from dateOfBirth
                + " order by c.person.name"; // Changed ordering
        Map m = new HashMap();
        m.put("res", true);
        m.put("n", "%" + name.trim().toLowerCase() + "%");
        m.put("yob", yob); // Using yob for the year of birth
        m.put("mob", mob); // Using mob for the month of birth
        return getFacade().findByJpql(j, m);
    }

    public List<Client> listPatientsByLocalReferanceNo(String refNo) {
        String j = "select c from Client c "
                + " where c.retired=false "
                + " and c.reservedClient<>:res "
                + " and lower(c.person.localReferanceNo)=:q "
                + " and c.createInstitution=:ins "
                + " order by c.phn";
        Map m = new HashMap();
        m.put("res", true);
        m.put("q", refNo.trim().toLowerCase());
        m.put("ins", webUserController.getLoggedUser().getInstitution());
        return getFacade().findByJpql(j, m);
    }

    public List<Client> listPatientsByLocalReferanceNoForSystemAdmin(String refNo) {
        String j = "select c from Client c "
                + " where c.retired=false "
                + " and c.reservedClient<>:res "
                + " and lower(c.person.localReferanceNo)=:q "
                + " order by c.phn";
        Map m = new HashMap();
        m.put("res", true);
        m.put("q", refNo.trim().toLowerCase());
        return getFacade().findByJpql(j, m);
    }

    public List<Client> listPatientsByScn(String ssNo) {
        String j = "select c from Client c "
                + " where c.retired=false "
                + " and c.reservedClient<>:res "
                + " and lower(c.person.ssNumber)=:q "
                + " order by c.phn";
        Map m = new HashMap();
        m.put("res", true);
        m.put("q", ssNo.trim().toLowerCase());
        return getFacade().findByJpql(j, m);
    }

    public List<Client> listPatientsByDrivingLicenseNo(String dlNo) {
        String j = "select c from Client c "
                + " where c.retired=false "
                + " and c.reservedClient<>:res "
                + " and lower(c.person.drivingLicenseNumber)=:q "
                + " order by c.phn";
        Map m = new HashMap();
        m.put("res", true);
        m.put("q", dlNo.trim().toLowerCase());
        return getFacade().findByJpql(j, m);
    }

    public List<Client> listPatientsByPassportNo(String passportNo) {
        String j = "select c from Client c "
                + " where c.retired=false "
                + " and c.reservedClient<>:res "
                + " and lower(c.person.passportNumber)=:q "
                + " order by c.phn";
        Map m = new HashMap();
        m.put("res", true);
        m.put("q", passportNo.trim().toLowerCase());
        return getFacade().findByJpql(j, m);
    }

    public List<Client> listPatientsByIDsStepvice(String ids) {
        //// //System.out.println("ids = " + ids);
        if (ids == null || ids.trim().equals("")) {
            return null;
        }
        List<Client> cs;
        if (ids == null || ids.trim().equals("")) {
            cs = new ArrayList<>();
            return cs;
        }
        String j;
        Map m;
        m = new HashMap();
        j = "select c from Client c "
                + " where c.retired=false "
                + " and c.phn=:q "
                + " order by c.phn";
        m.put("q", ids.trim());
        //// //System.out.println("m = " + m);
        //// //System.out.println("j = " + j);
        cs = getFacade().findByJpql(j, m);

        if (cs != null && !cs.isEmpty()) {
            //// //System.out.println("cs.size() = " + cs.size());
            return cs;
        }
        
        List<Person> ps;

        j = "select c from Person c "
                + " where c.retired=false "
                + " and ("
                + " c.phone1=:q "
                + " or "
                + " c.phone2=:q "
                + " or "
                + " c.nic=:q "
                + " ) ";
        ps = personFacade.findByJpql(j, m);
        if (ps != null && !ps.isEmpty()) {
            cs =new ArrayList<>();
            for(Person p:ps){
                String j1="Select c "
                        + " from Client c "
                        + " where c.person=:p";
                Map m1 = new HashMap();
                Client c = getFacade().findFirstByJpql(j1, m1);
                if(c!=null){
                    cs.add(c);
                }
            }
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

    public List<ClientBasicData> listPatientsByIDsStepviceWithBasicData(String ids) {
        if (ids == null || ids.trim().equals("")) {
            return null;
        }
        List<ClientBasicData> cs;
        List<Object> objs;
        if (ids.trim().equals("")) {
            cs = new ArrayList<>();
            return cs;
        }
        String j;
        Map m;
        m = new HashMap();
        m.put("q", ids.trim().toLowerCase());
        j = "select new lk.gov.health.phsp.pojcs.ClientBasicData("
                + "c.id, "
                + "c.phn, "
                + "c.person.name, "
                + "c.person.nic, "
                + "c.person.phone1, "
                + "c.person.address "
                + ") "
                + " from Client c "
                + " where c.retired=false "
                + " and ("
                + " c.person.phone1=:q "
                + " or "
                + " c.person.phone2=:q "
                + " or "
                + " lower(c.person.nic)=:q "
                + " or "
                + " lower(c.phn)=:q"
                + " ) ";
        objs = getFacade().findByJpql(j, m);
        if (objs != null && !objs.isEmpty()) {
            cs = objectsToClientBasicDataObjects(objs);
            return cs;
        }

        j = "select new lk.gov.health.phsp.pojcs.ClientBasicData("
                + "c.id, "
                + "c.phn, "
                + "c.person.name, "
                + "c.person.nic, "
                + "c.person.phone1, "
                + "c.person.address "
                + ") "
                + " from Client c "
                + " where c.retired=false "
                + " and ("
                + " lower(c.person.localReferanceNo)=:q "
                + " or "
                + " lower(c.person.ssNumber)=:q"
                + " ) ";

        objs = getFacade().findByJpql(j, m);
        if (objs != null && !objs.isEmpty()) {
            cs = objectsToClientBasicDataObjects(objs);
            return cs;
        }
        cs = new ArrayList<>();
        return cs;
    }

    public List<ClientBasicData> listPatientsByIDsWithBasicData(String ids) {
        Long st = new Date().getTime();
        List<ClientBasicData> cs = new ArrayList<>();
        if (ids == null || ids.trim().equals("")) {
            return cs;
        }
        String jpql;
        Map m;
        m = new HashMap();
        jpql = "select new lk.gov.health.phsp.pojcs.ClientBasicData("
                + "c.id, "
                + "c.phn, "
                + "c.person.name, "
                + "c.person.nic, "
                + "c.person.phone1, "
                + "c.person.address "
                + ") "
                + " from Client c "
                + " where c.retired=false "
                + " and "
                + " ("
                + " lower(c.phn)=:q "
                + " or "
                + " lower(c.person.localReferanceNo)=:q "
                + " or "
                + " lower(c.person.ssNumber)=:q "
                + " or "
                + " c.person.phone1=:q "
                + " or "
                + " c.person.phone2=:q "
                + " or "
                + " lower(c.person.nic)=:q "
                + " ) ";
        m.put("q", ids.trim().toLowerCase());
        cs = (List<ClientBasicData>) getFacade().findLightsByJpql(jpql, m);
        Long ed = new Date().getTime();
        return cs;
    }

    public List<ClientBasicData> listPatientsByPhnWithBasicData(String ids) {
        if (ids == null || ids.trim().equals("")) {
            return null;
        }
        List<ClientBasicData> cs;
        List<Object> objs;
        if (ids.trim().equals("")) {
            cs = new ArrayList<>();
            return cs;
        }
        String j;
        Map m;
        m = new HashMap();
        j = "select new lk.gov.health.phsp.pojcs.ClientBasicData("
                + "c.id, "
                + "c.phn, "
                + "c.person.name, "
                + "c.person.sex.name,"
                + "c.person.nic, "
                + "c.person.phone1, "
                + "c.person.address "
                + ") ";
        j += " from Client c "
                + " where c.retired=false "
                + " and c.phn=:q "
                + " order by c.phn";
        m.put("q", ids.trim());
        objs = getFacade().findByJpql(j, m);

        if (objs != null && !objs.isEmpty()) {
            cs = objectsToClientBasicDataObjects(objs);
            return cs;
        }
        cs = new ArrayList<>();
        return cs;
    }

    public List<ClientBasicData> objectsToClientBasicDataObjects(List<Object> objs) {
        List<ClientBasicData> cbds = new ArrayList<>();
        if (objs == null || objs.isEmpty()) {
            return cbds;
        }
        for (Object o : objs) {
            if (o instanceof ClientBasicData) {
                ClientBasicData c = (ClientBasicData) o;
                cbds.add(c);
            }
        }
        return cbds;
    }

    public List<Client> listPatientsByIDs(String ids) {
        if (ids == null || ids.trim().equals("")) {
            return null;
        }
        String j = "select c from Client c "
                + " where c.retired=false "
                + " and c.reservedClient<>:res "
                + " and ("
                + " c.person.phone1=:q "
                + " or "
                + " c.person.phone2=:q "
                + " or "
                + " c.person.nic=:q "
                + " or "
                + " c.phn=:q "
                + " or "
                + " c.person.localReferanceNo=:q "
                + " or "
                + " c.person.ssNumber=:q "
                + " ) "
                + " order by c.phn";
        Map m = new HashMap();
        m.put("res", true);
        m.put("q", ids.trim());
        return getFacade().findByJpql(j, m);
    }

    public Client prepareCreate() {
        selected = new Client();
        return selected;
    }

    public String saveClient() {

        Institution poiIns;

        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing to save");
            return "";
        }

        Institution createdIns = null;

        if (selected.getCreateInstitution() == null) {
            if (webUserController.getLoggedUser().getInstitution().getPoiInstitution() != null) {
                createdIns = webUserController.getLoggedUser().getInstitution().getPoiInstitution();
                poiIns = webUserController.getLoggedUser().getInstitution().getPoiInstitution();;
            } else {
                createdIns = webUserController.getLoggedUser().getInstitution();
                poiIns = webUserController.getLoggedUser().getInstitution();
            }
            selected.setCreateInstitution(createdIns);
        } else {
            createdIns = selected.getCreateInstitution();
            poiIns = selected.getCreateInstitution();
        }

        if (poiIns == null) {
            poiIns = institutionApplicationController.findMinistryOfHealth();
        }

        if (poiIns.getPoiNumber() == null || poiIns.getPoiNumber().trim().equals("")) {
            poiIns.setPoiNumber("385C");
        }

        if (selected.getPhn() == null || selected.getPhn().trim().equals("")) {
            String newPhn = applicationController.createNewPersonalHealthNumberformat(poiIns);

            int count = 0;
            while (checkPhnExists(newPhn, null)) {
                newPhn = applicationController.createNewPersonalHealthNumberformat(poiIns);
                count++;
                if (count > 100) {
                    JsfUtil.addErrorMessage("Generating New PHN Failed. Client NOT saved. Please contact System Administrator.");
                    return "";
                }
            }
            selected.setPhn(newPhn);
        }

        if (selected.getId() == null) {
            if (checkPhnExists(selected.getPhn(), null)) {
                JsfUtil.addErrorMessage("PHN already exists.");
                return null;
            }
            if (selected.getPerson().getNic() != null && !selected.getPerson().getNic().trim().equals("")) {

                if (checkNicExists(selected.getPerson().getNic(), null)) {
                    JsfUtil.addErrorMessage("NIC already exists.");
                    return null;
                }
            }
        } else {
            if (checkPhnExists(selected.getPhn(), selected)) {
                JsfUtil.addErrorMessage("PHN already exists.");
                return null;
            }
            if (selected.getPerson().getNic() != null && !selected.getPerson().getNic().trim().equals("")) {
                if (checkNicExists(selected.getPerson().getNic(), selected)) {
                    JsfUtil.addErrorMessage("NIC already exists.");
                    return null;
                }
            }
        }
        selected.setReservedClient(false);

        saveClient(selected);
        JsfUtil.addSuccessMessage("Saved.");
        return toClientProfile();
    }

    public void reserverPhn() {
        Institution poiInstitution;
        int i = 0;

        if (webUserController.getLoggedUser().getInstitution().getPoiInstitution() != null) {
            poiInstitution = webUserController.getLoggedUser().getInstitution().getPoiInstitution();
        } else {
            poiInstitution = webUserController.getLoggedUser().getInstitution();
        }

        if (poiInstitution == null) {
            JsfUtil.addErrorMessage("No POI");
            return;
        }

        if (numberOfPhnToReserve == null) {
            JsfUtil.addErrorMessage("No Numner of PHN to add");
            return;
        }

        if (numberOfPhnToReserve > 100) {
            JsfUtil.addErrorMessage("Only upto 100 PHNs can reserve at a time.");
            return;
        }
        reservePhnList = new ArrayList<>();

        while (i < numberOfPhnToReserve) {
            String newPhn = generateNewPhn(poiInstitution);

            if (!checkPhnExists(newPhn, null)) {
                reservePhnList.add(newPhn);

                Client rc = new Client();

                rc.setPhn(newPhn);
                rc.setCreatedBy(webUserController.getLoggedUser());
                rc.setCreatedAt(new Date());
                rc.setCreatedOn(new Date());
                rc.setCreateInstitution(webUserController.getLoggedUser().getInstitution());
                if (rc.getPerson().getCreatedAt() == null) {
                    rc.getPerson().setCreatedAt(new Date());
                }
                if (rc.getPerson().getCreatedBy() == null) {
                    rc.getPerson().setCreatedBy(webUserController.getLoggedUser());
                }
                rc.setReservedClient(true);

                getFacade().create(rc);
                i = i + 1;
            }
        }
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
            if (c.getCreatedOn() == null) {
                c.setCreatedOn(new Date());
            }
            if (c.getCreateInstitution() == null) {
                if (webUserController.getLoggedUser() != null && webUserController.getLoggedUser().getInstitution() != null) {
                    c.setCreateInstitution(webUserController.getLoggedUser().getInstitution());
                }
            }
            if (c.getPerson().getCreatedAt() == null) {
                c.getPerson().setCreatedAt(new Date());
            }
            if (c.getPerson().getCreatedBy() == null) {
                c.getPerson().setCreatedBy(webUserController.getLoggedUser());
            }
            personFacade.create(c.getPerson());
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
//        updateYearDateMonth();
//        selectedClientChanged();
//        selectedClientsClinics = null;
//        selectedClientsLastFiveClinicVisits = null;
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
            selectedClientsClinics = fillEncounters(selected,
                    institutionApplicationController.getClinicTypes(),
                    EncounterType.Clinic_Enroll, true);
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

    // Comment by Dr M H B Ariyaratne with assistance from ChatGPT from OpenAI
    public void setSelectedId(Long selectedId) {
        try {
            selected = getFacade().find(selectedId);
            this.selectedId = selectedId;
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public List<Encounter> getSelectedClientsLastFiveClinicVisits() {
        if (selectedClientsLastFiveClinicVisits == null) {
            selectedClientsLastFiveClinicVisits = fillEncounters(selected,
                    institutionApplicationController.getClinicTypes(),
                    EncounterType.Clinic_Visit, true, 5, true);

        }
        return selectedClientsLastFiveClinicVisits;
    }

    public String removeFromClinic() {
        if (unregisteringClinic == null) {
            JsfUtil.addErrorMessage("No Clinic Selected");
            return "";
        }
        unregisteringClinic.setRetired(true);
        unregisteringClinic.setRetiredAt(new Date());
        unregisteringClinic.setRetiredBy(webUserController.getLoggedUser());
        encounterFacade.edit(unregisteringClinic);
        JsfUtil.addSuccessMessage("Unregistered from the clinic");
        return toClientProfile();
    }

    public void setSelectedClientsLastFiveClinicVisits(List<Encounter> selectedClientsLastFiveClinicVisits) {
        this.selectedClientsLastFiveClinicVisits = selectedClientsLastFiveClinicVisits;
    }

    public Boolean getEmailExists() {
        return emailExists;
    }

    public void setEmailExists(Boolean emailExists) {
        this.emailExists = emailExists;
    }

    public Boolean getPhone1Exists() {
        return phone1Exists;
    }

    public void setPhone1Exists(Boolean phone1Exists) {
        this.phone1Exists = phone1Exists;
    }

    public Integer getNumberOfPhnToReserve() {
        return numberOfPhnToReserve;
    }

    public void setNumberOfPhnToReserve(Integer numberOfPhnToReserve) {
        this.numberOfPhnToReserve = numberOfPhnToReserve;
    }

    public List<ClientBasicData> getSelectedClientsWithBasicData() {
        return selectedClientsWithBasicData;
    }

    public void setSelectedClientsWithBasicData(List<ClientBasicData> selectedClientsWithBasicData) {
        this.selectedClientsWithBasicData = selectedClientsWithBasicData;
    }

    public List<String> getReservePhnList() {
        return reservePhnList;
    }

    public void setReservePhnList(List<String> reservePhnList) {
        this.reservePhnList = reservePhnList;
    }

    public DataFormset getDataFormset() {
        return dataFormset;
    }

    public void setDataFormset(DataFormset dataFormset) {
        this.dataFormset = dataFormset;
    }

    public DesignComponentFormSet getClientDcfs() {
        if (clientDcfs == null) {
            clientDcfs = designComponentFormSetController.getClintFormSet(webUserController.getLoggedUser().getInstitution());
        }
        return clientDcfs;
    }

    public void setClientDcfs(DesignComponentFormSet clientDcfs) {
        this.clientDcfs = clientDcfs;
    }

    public Encounter getUnregisteringClinic() {
        return unregisteringClinic;
    }

    public void setUnregisteringClinic(Encounter unregisteringClinic) {
        this.unregisteringClinic = unregisteringClinic;
    }

    public List<Client> getSelectedClientsFromIntegrations() {
        return selectedClientsFromIntegrations;
    }

    public void setSelectedClientsFromIntegrations(List<Client> selectedClientsFromIntegrations) {
        this.selectedClientsFromIntegrations = selectedClientsFromIntegrations;
    }

    public List<FhirOperationResult> getFhirOperationResults() {
        return fhirOperationResults;
    }

    public void setFhirOperationResults(List<FhirOperationResult> fhirOperationResults) {
        this.fhirOperationResults = fhirOperationResults;
    }

    public SearchQueryData getSearchQueryData() {
        if (searchQueryData == null) {
            searchQueryData = new SearchQueryData();
        }
        return searchQueryData;
    }

    public void setSearchQueryData(SearchQueryData searchQueryData) {
        this.searchQueryData = searchQueryData;
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
