package lk.gov.health.phsp.bean;

// <editor-fold defaultstate="collapsed" desc="Import">
import lk.gov.health.phsp.entity.ClientEncounterComponentFormSet;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.bean.util.JsfUtil.PersistAction;
import lk.gov.health.phsp.facade.ClientEncounterComponentFormSetFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
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
import lk.gov.health.phsp.entity.Client;
import lk.gov.health.phsp.entity.ClientEncounterComponentForm;
import lk.gov.health.phsp.entity.ClientEncounterComponentItem;
import lk.gov.health.phsp.entity.DesignComponentForm;
import lk.gov.health.phsp.entity.DesignComponentFormItem;
import lk.gov.health.phsp.entity.DesignComponentFormSet;
import lk.gov.health.phsp.entity.Encounter;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.enums.ComponentSex;
import lk.gov.health.phsp.enums.DataCompletionStrategy;
import lk.gov.health.phsp.enums.DataPopulationStrategy;
import lk.gov.health.phsp.enums.DataRepresentationType;
import lk.gov.health.phsp.enums.EncounterType;
import lk.gov.health.phsp.enums.SelectionDataType;
import lk.gov.health.phsp.facade.ClientEncounterComponentItemFacade;
import lk.gov.health.phsp.facade.ClientFacade;
import lk.gov.health.phsp.facade.EncounterFacade;
import lk.gov.health.phsp.facade.PersonFacade;
import org.apache.commons.lang3.SerializationUtils;
// </editor-fold>

@Named("clientEncounterComponentFormSetController")
@SessionScoped
public class ClientEncounterComponentFormSetController implements Serializable {
// <editor-fold defaultstate="collapsed" desc="EJBs">

    @EJB
    private lk.gov.health.phsp.facade.ClientEncounterComponentFormSetFacade ejbFacade;
    @EJB
    private ClientEncounterComponentItemFacade itemFacade;
    @EJB
    private ClientFacade clientFacade;
    @EJB
    private PersonFacade personFacade;
    @EJB
    private EncounterFacade encounterFacade;

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Controllers">
    @Inject
    private DesignComponentFormSetController designComponentFormSetController;
    @Inject
    private DesignComponentFormController designComponentFormController;
    @Inject
    private DesignComponentFormItemController designComponentFormItemController;
    @Inject
    private ClientEncounterComponentFormController clientEncounterComponentFormController;
    @Inject
    private ClientEncounterComponentItemController clientEncounterComponentItemController;
    @Inject
    private ClientController clientController;
    @Inject
    private WebUserController webUserController;
    @Inject
    private EncounterController encounterController;
    @Inject
    private ItemController itemController;
    @Inject
    private CommonController commonController;
    @Inject
    UserTransactionController userTransactionController;

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Class Variables">
    private List<ClientEncounterComponentFormSet> items = null;
    private List<ClientEncounterComponentFormSet> selectedItems = null;
    private ClientEncounterComponentFormSet selected;
    private DesignComponentFormSet designFormSet;
    private boolean formEditable;
    private Date encounterDate;
    private Integer selectedTabIndex;
    private Date from;
    private Date to;

    private List<ClientEncounterComponentFormSet> lastFiveClinicVisits;

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Constructors">
    public ClientEncounterComponentFormSetController() {
    }
// </editor-fold>    
// <editor-fold defaultstate="collapsed" desc="Navigation Functions">

    public String toClientProfileFromEncounter() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing selected.");
            return "";
        }
        clientController.setSelected(selected.getEncounter().getClient());
        return "/client/profile";
    }

    public String toViewOrEditFormset() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing selected.");
            return "";
        }
        if (selected.isCompleted()) {
            return toViewFormset();
        } else {
            return toEditFormset();
        }
    }

    public String toViewFormset() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing selected.");
            return "";
        }
        String navigationLink = "/clientEncounterComponentFormSet/Formset_view";
        formEditable = false;
        return navigationLink;
    }

    public String toEditFormset() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing selected.");
            return "";
        }
        String navigationLink = "/clientEncounterComponentFormSet/Formset";

        formEditable = !selected.isCompleted();
        return navigationLink;
    }
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="User Functions">

    public void retireSelectedItems() {
        if (selectedItems == null) {
            return;
        }
        for (ClientEncounterComponentFormSet s : selectedItems) {
            Encounter e = s.getEncounter();
            if (e != null) {
                e.setRetired(true);
                e.setRetiredAt(new Date());
                e.setRetiredBy(webUserController.getLoggedUser());
                getEncounterFacade().edit(e);
            }
            s.setRetired(true);
            s.setRetiredAt(new Date());
            s.setRetiredBy(webUserController.getLoggedUser());
            getFacade().edit(s);
        }
        selectedItems = null;
        items = null;
    }

    public void retireSelectedItemsAsUncomplete() {
        if (selectedItems == null) {
            return;
        }
        for (ClientEncounterComponentFormSet s : selectedItems) {
            if (s == null) {
                continue;
            }
            Encounter e = s.getEncounter();
            if (e != null) {
                e.setCompleted(false);
                e.setLastEditeAt(new Date());
                e.setLastEditBy(webUserController.getLoggedUser());
                getEncounterFacade().edit(e);
            }
            s.setCompleted(false);
            s.setLastEditeAt(new Date());
            s.setLastEditBy(webUserController.getLoggedUser());
            getFacade().edit(s);
        }
        selectedItems = null;
        items = null;
    }

    public void retireSelectedItemsAsComplete() {
        if (selectedItems == null) {
            return;
        }
        for (ClientEncounterComponentFormSet s : selectedItems) {
            if (s == null) {
                continue;
            }
            Encounter e = s.getEncounter();
            if (e != null) {
                e.setCompleted(true);
                e.setCompletedAt(new Date());
                e.setCompletedBy(webUserController.getLoggedUser());
                getEncounterFacade().edit(e);
            }
            s.setCompleted(false);
            s.setCompletedAt(new Date());
            s.setCompletedBy(webUserController.getLoggedUser());
            getFacade().edit(s);
        }
        selectedItems = null;
        items = null;
    }

    public String completeFormset() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing to Complete.");
            userTransactionController.recordTransaction("Nothing to Complete in formset");
            return "";
        }
        save(selected);
        selected.setCompleted(true);
        selected.setCompletedAt(new Date());
        selected.setCompletedBy(webUserController.getLoggedUser());
        getFacade().edit(selected);
        executePostCompletionStrategies(selected);
        formEditable = false;
        JsfUtil.addSuccessMessage("Completed");
        userTransactionController.recordTransaction("Formset Completed");
        return toViewFormset();
    }

    public void executePostCompletionStrategies(ClientEncounterComponentFormSet s) {
        String j = "select f from ClientEncounterComponentItem f "
                + " where f.retired=false "
                + " and f.parentComponent.parentComponent=:s ";
        Map m = new HashMap();
        m.put("s", s);
        List<ClientEncounterComponentItem> is = getItemFacade().findByJpql(j, m);
        for (ClientEncounterComponentItem i : is) {
            if (i.getDataCompletionStrategy() == DataCompletionStrategy.Replace_Values_of_client) {
                updateToClientValue(i);
            }
        }

    }

    //TODO:Save Values to client Component
    public void updateToClientValue(ClientEncounterComponentItem vi) {

        if (vi == null) {

            return;
        }
        if (vi.getParentComponent() == null) {

            return;
        }
        if (vi.getParentComponent().getParentComponent() == null) {

            return;
        }
        ClientEncounterComponentFormSet s;
        Client c;
        if (vi.getParentComponent().getParentComponent() instanceof ClientEncounterComponentFormSet) {
            s = (ClientEncounterComponentFormSet) vi.getParentComponent().getParentComponent();

        } else {

            return;
        }

        c = s.getEncounter().getClient();

        ClientEncounterComponentItem ti;
        String j = "select vi from ClientEncounterComponentItem vi where vi.retired=false "
                + " and vi.client=:c "
                + " and vi.item=:i "
                + " and vi.dataRepresentationType=:r "
                + " order by vi.id desc";
        Map m = new HashMap();
        m.put("r", DataRepresentationType.Client);
        m.put("c", c);
        m.put("i", vi.getItem());

        ti = getItemFacade().findFirstByJpql(j, m);

        if (ti == null) {
            ti = new ClientEncounterComponentItem();
            ti.setItem(vi.getItem());
            ti.setCreatedAt(new Date());
            ti.setCreatedBy(webUserController.getLoggedUser());
            ti.setClient(c);
//            ti.setSelectionDataType(vi.getSelectionDataType());
            ti.setDataRepresentationType(DataRepresentationType.Client);
            getItemFacade().create(ti);
        } else {
            ti.setLastEditBy(webUserController.getLoggedUser());
            ti.setLastEditeAt(new Date());
        }

//        if (ti.getSelectionDataType() == null) {
//            ti.setSelectionDataType(vi.getSelectionDataType());
//        }
        ti.setClient(c);

        ti.setDateValue(vi.getDateValue());
        ti.setShortTextValue(vi.getShortTextValue());
        ti.setLongTextValue(vi.getLongTextValue());
        ti.setItemValue(vi.getItemValue());
        ti.setAreaValue(vi.getAreaValue());
        ti.setItemValue(vi.getItemValue());
        ti.setInstitution(vi.getInstitutionValue());
        ti.setPrescriptionValue(vi.getPrescriptionValue());

        getItemFacade().edit(ti);

        if (ti.getItem() == null || ti.getItem().getCode() == null) {
            return;
        }

        String code = ti.getItem().getCode();

        switch (code) {
            case "client_name":
                c.getPerson().setName(ti.getShortTextValue());
                return;
            case "client_occupation":
                c.getPerson().setOccupation(ti.getShortTextValue());
                return;
            case "client_phn_number":
                c.setPhn(ti.getShortTextValue());
                return;
            case "client_sex":
                c.getPerson().setSex(ti.getItemValue());
                return;
            case "client_nic_number":
                c.getPerson().setNic(ti.getShortTextValue());
                return;
            case "client_data_of_birth":
                c.getPerson().setDateOfBirth(ti.getDateValue());
                return;
            case "client_permanent_address":
                c.getPerson().setAddress(ti.getLongTextValue());
                return;
            case "client_current_address":
                ti.setLongTextValue(c.getPerson().getAddress());
                return;
            case "client_mobile_number":
                c.getPerson().setPhone1(ti.getShortTextValue());
                return;
            case "client_home_number":
                c.getPerson().setPhone2(ti.getShortTextValue());
                return;
            case "client_permanent_moh_area":
                c.getPerson().setGnArea(ti.getAreaValue());
                return;
            case "client_permanent_phm_area":
                c.getPerson().getGnArea().setPhm(ti.getAreaValue());
                return;
            case "client_permanent_phi_area":
                c.getPerson().getGnArea().setPhi(ti.getAreaValue());
                return;
            case "client_gn_area":
                c.getPerson().setGnArea(ti.getAreaValue());
                return;
            case "client_ds_division":
                c.getPerson().getGnArea().setDsd(ti.getAreaValue());
                return;
        }

        getPersonFacade().edit(c.getPerson());
        getClientFacade().edit(c);

    }

    public void save() {
        save(selected);
    }

    public void save(ClientEncounterComponentFormSet s) {
        if (s == null) {
            return;
        }
        if (s.getId() == null) {
            s.setCreatedAt(new Date());
            s.setCreatedBy(webUserController.getLoggedUser());
            getFacade().create(s);
        } else {
            s.setLastEditBy(webUserController.getLoggedUser());
            s.setLastEditeAt(new Date());
            getFacade().edit(s);
        }
    }

    public String createAndNavigateToClinicalEncounterComponentFormSetFromDesignComponentFormSet() {
        return createAndNavigateToClinicalEncounterComponentFormSetFromDesignComponentFormSetForClinicVisit(designFormSet);
    }

    public List<ClientEncounterComponentFormSet> fillLastFiveCompletedEncountersFormSets(String type) {
        return ClientEncounterComponentFormSetController.this.fillEncountersFormSets(type, 5);
    }

    public List<ClientEncounterComponentFormSet> filluncompletedEncountersFormSets(String type) {
        return ClientEncounterComponentFormSetController.this.fillEncountersFormSets(type, false);
    }

    public List<ClientEncounterComponentFormSet> filluncompletedEncountersFormSets(String type, int count) {
        return ClientEncounterComponentFormSetController.this.fillEncountersFormSets(type, count, false);
    }

    public List<ClientEncounterComponentFormSet> fillEncountersFormSets(EncounterType type) {
        Client c = getClientController().getSelected();
        if (c == null) {
            return new ArrayList<>();
        }
        return ClientEncounterComponentFormSetController.this.fillEncountersFormSets(c, type, 0, null);
    }

//    public List<ClientEncounterComponentFormSet> fillLastFiveEncountersFormSets(EncounterType type) {
//        Client c = getClientController().getSelected();
//        if (c == null) {
//            return new ArrayList<>();
//        }
//        return fillEncountersFormSetsForSysadmin(c, type, 5, null);
//    }
    public List<ClientEncounterComponentFormSet> fillEncountersFormSets(String type) {
        return ClientEncounterComponentFormSetController.this.fillEncountersFormSets(type, true);
    }

    public List<ClientEncounterComponentFormSet> fillEncountersFormSets(String type, boolean completedOnly) {
        EncounterType ec = null;
        try {
            ec = EncounterType.valueOf(type);
            Client c = getClientController().getSelected();
            if (c == null) {
                return new ArrayList<>();
            }
            return ClientEncounterComponentFormSetController.this.fillEncountersFormSets(c, ec, 0, completedOnly);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<ClientEncounterComponentFormSet> fillEncountersFormSets(String type, int count) {
        return ClientEncounterComponentFormSetController.this.fillEncountersFormSets(type, count, true);
    }

    public List<ClientEncounterComponentFormSet> fillEncountersFormSets(String type, int count, boolean completedOnly) {

        EncounterType ec = null;
        try {
            ec = EncounterType.valueOf(type);
            Client c = getClientController().getSelected();
            if (c == null) {
                return new ArrayList<>();
            }
            return ClientEncounterComponentFormSetController.this.fillEncountersFormSets(c, ec, count, completedOnly);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<ClientEncounterComponentFormSet> fillEncountersFormSets(Client c, String type) {
        EncounterType ec = null;
        try {
            ec = EncounterType.valueOf(type);
            return ClientEncounterComponentFormSetController.this.fillEncountersFormSets(c, ec);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<ClientEncounterComponentFormSet> fillEncountersFormSets(Client c, EncounterType type) {
        return ClientEncounterComponentFormSetController.this.fillEncountersFormSets(c, type, 0);
    }

    public List<ClientEncounterComponentFormSet> fillEncountersFormSets(Client c, EncounterType type, int count) {
        return ClientEncounterComponentFormSetController.this.fillEncountersFormSets(c, type, count, true);
    }

    public List<ClientEncounterComponentFormSet> fillEncountersFormSets(Client c, String type, int count) {
        EncounterType ec = null;
        try {
            ec = EncounterType.valueOf(type);
            return ClientEncounterComponentFormSetController.this.fillEncountersFormSets(c, ec, count, true);
        } catch (Exception e) {
            return new ArrayList<>();
        }

    }

    public List<ClientEncounterComponentFormSet> fillEncountersFormSets(Client c, EncounterType type, int count, Boolean completeOnly) {

        List<ClientEncounterComponentFormSet> fs;
        Map m = new HashMap();
        String j = "select s from ClientEncounterComponentFormSet s where "
                + " s.retired=false "
                + " and s.encounter.encounterType=:t "
                + " and s.encounter.client=:c ";
        if (completeOnly == null) {

        } else if (completeOnly == true) {
            j += " and s.completed=:com ";
            m.put("com", true);

        } else if (completeOnly == false) {
            j += " and s.completed=:com ";
            m.put("com", false);
        }
        j += " order by s.encounter.encounterFrom desc";

        m.put("c", c);
        m.put("t", type);
        if (count == 0) {
            fs = getFacade().findByJpql(j, m);
        } else {
            fs = getFacade().findByJpql(j, m, count);
        }
        if (fs == null) {
            fs = new ArrayList<>();
        }
        return fs;
    }

    public void fillLastFiveVisits() {
        Map m = new HashMap();
        String j = "select s from ClientEncounterComponentFormSet s where "
                + " s.retired=false "
                + " and s.encounter.encounterType=:t "
                + " and s.encounter.client=:c ";
        j += " order by s.encounter.encounterFrom desc";
        m.put("c", getClientController().getSelected());
        m.put("t", EncounterType.Clinic_Visit);
        lastFiveClinicVisits = getFacade().findByJpql(j, m, 5);
        if (lastFiveClinicVisits == null) {
            lastFiveClinicVisits = new ArrayList<>();
        }
    }

    public List<ClientEncounterComponentFormSet> fillLastFiveEncountersFormSets(EncounterType type) {

        List<ClientEncounterComponentFormSet> fs;
        Map m = new HashMap();
        String j = "select s from ClientEncounterComponentFormSet s where "
                + " s.retired=false "
                + " and s.encounter.encounterType=:t "
                + " and s.encounter.client=:c ";
        j += " order by s.encounter.encounterFrom desc";
        m.put("c", getClientController().getSelected());
        m.put("t", type);

        fs = getFacade().findByJpql(j, m, 5);

        if (fs == null) {
            fs = new ArrayList<>();
        }
        return fs;
    }

    public String fillAllEncountersFormSetsOfSelectedClient(EncounterType type) {
        List<ClientEncounterComponentFormSet> fs;
        Map m = new HashMap();
        String j = "select s from ClientEncounterComponentFormSet s where "
                + " s.retired=false "
                + " and s.encounter.encounterType=:t "
                + " and s.encounter.client=:c ";
        j += " order by s.encounter.encounterFrom desc";
        m.put("c", getClientController().getSelected());
        m.put("t", type);
        fs = getFacade().findByJpql(j, m);
        if (fs == null) {
            fs = new ArrayList<>();
        }
        items = fs;
        userTransactionController.recordTransaction("fill All Encounters Form Sets Of Selected Client");
        return "/client/client_encounters";
    }

    public String fillRetiredEncountersFormSets() {
        return fillEncountersFormSetsForSysadmin(true);
    }

    public String fillEncountersFormSetsForSysadmin() {
        return fillEncountersFormSetsForSysadmin(false);
    }

    public String fillEncountersFormSetsForSysadmin(boolean retired) {
        List<ClientEncounterComponentFormSet> fs;
        Map m = new HashMap();
        String j = "select s from ClientEncounterComponentFormSet s ";
        j += " where s.retired=:ret ";
        j += " and s.encounter.encounterFrom between :fd and :td ";
        j += " order by s.encounter.encounterFrom desc";
        m.put("ret", retired);
        m.put("fd", getFrom());
        m.put("td", getTo());
        fs = getFacade().findByJpql(j, m);
        if (fs == null) {
            fs = new ArrayList<>();
        }
        items = fs;
        return "/systemAdmin/all_encounters";
    }

    public ClientEncounterComponentFormSet findLastUncompletedEncounterOfThatType(DesignComponentFormSet dfs, Client c, Institution i, EncounterType t) {
        String j = "select f from  ClientEncounterComponentFormSet f join f.encounter e"
                + " where "
                + " e.retired<>:er"
                + " and f.retired<>:fr "
                + " and f.completed<>:fc "
                + " and f.referenceComponent=:dfs "
                + " and e.client=:c "
                + " and e.institution=:i "
                + " and e.encounterType=:t"
                + " order by f.id desc";
//        j = "select f from  ClientEncounterComponentFormSet f join f.encounter e"
//                + " where f.referenceComponent=:dfs "
//                + " and e.client=:c "
//                + " and e.institution=:i "
//                + " and e.encounterType=:t"
//                + " order by f.id desc";
        Map m = new HashMap();
        m.put("c", c);
        m.put("i", i);
        m.put("t", t);
        m.put("dfs", dfs);
        m.put("er", true);
        m.put("fr", true);
        m.put("fc", true);
        ClientEncounterComponentFormSet f = getFacade().findFirstByJpql(j, m);
        return f;
    }

    public boolean isFirstEncounterOfThatType(Client c, Institution i, EncounterType t) {
        String j = "select count(e) from Encounter e where "
                + " e.retired=false "
                + " and e.client=:c "
                + " and e.institution=:i "
                + " and e.encounterType=:t";
        Map m = new HashMap();
        m.put("c", c);
        m.put("i", i);
        m.put("t", t);

        Long count = getFacade().countByJpql(j, m);
        if (count == null) {
            return true;
        }
        if (count == 0) {
            return true;
        }
        return false;
    }

    public String createAndNavigateToClinicalEncounterComponentFormSetFromDesignComponentFormSetForClinicVisit(DesignComponentFormSet dfs) {
        ClientEncounterComponentFormSet efs = findLastUncompletedEncounterOfThatType(dfs, clientController.getSelected(), dfs.getInstitution(), EncounterType.Clinic_Visit);
        selectedTabIndex = 0;
        if (efs == null) {
            return createNewAndNavigateToClinicalEncounterComponentFormSetFromDesignComponentFormSetForClinicVisit(dfs);
        } else {
            selected = efs;
            return toEditFormset();
        }
    }

    public String createNewAndNavigateToClinicalEncounterComponentFormSetFromDesignComponentFormSetForClinicVisit(DesignComponentFormSet dfs) {

        String navigationLink = "/clientEncounterComponentFormSet/Formset";
        formEditable = true;

        if (clientController.getSelected() == null) {
            JsfUtil.addErrorMessage("Please select a client");
            return "";
        }

        Map<String, ClientEncounterComponentItem> mapOfClientValues = getClientValues(clientController.getSelected());

        Date d = new Date();
        Encounter e = new Encounter();
        e.setClient(clientController.getSelected());
        e.setInstitution(dfs.getInstitution());

        if (encounterDate != null) {
            e.setEncounterDate(encounterDate);
        } else {
            e.setEncounterDate(d);
        }

        e.setEncounterFrom(d);
        e.setEncounterType(EncounterType.Clinic_Visit);

        e.setFirstEncounter(isFirstEncounterOfThatType(clientController.getSelected(), dfs.getInstitution(), EncounterType.Clinic_Visit));

        e.setEncounterMonth(CommonController.getMonth(d));
        e.setEncounterQuarter(CommonController.getQuarter(d));
        e.setEncounterYear(CommonController.getYear(d));

        encounterController.save(e);

        ClientEncounterComponentFormSet cfs = new ClientEncounterComponentFormSet();

        cfs.setEncounter(e);
        cfs.setInstitution(dfs.getInstitution());

        cfs.setReferenceComponent(dfs);
        cfs.setComponentSetType(dfs.getComponentSetType());
        cfs.setPanelType(dfs.getPanelType());
        cfs.setName(dfs.getName());
        cfs.setDescreption(dfs.getDescreption());
        cfs.setForegroundColour(dfs.getForegroundColour());
        cfs.setBackgroundColour(dfs.getBackgroundColour());
        cfs.setBorderColour(dfs.getBorderColour());

        getFacade().create(cfs);

        List<DesignComponentForm> dfList = designComponentFormController.fillFormsofTheSelectedSet(dfs);

        for (DesignComponentForm df : dfList) {

            boolean skipThisForm = false;
            if (df.getComponentSex() == ComponentSex.For_Females && clientController.getSelected().getPerson().getSex().getCode().equalsIgnoreCase("sex_male")) {
                skipThisForm = true;
            }
            if (df.getComponentSex() == ComponentSex.For_Males && clientController.getSelected().getPerson().getSex().getCode().equalsIgnoreCase("sex_female")) {
                skipThisForm = true;
            }

            if (!skipThisForm) {

                ClientEncounterComponentForm cf = new ClientEncounterComponentForm();

                cf.setEncounter(e);
                cf.setInstitution(dfs.getInstitution());
                cf.setItem(df.getItem());

                cf.setReferenceComponent(df);
                cf.setName(df.getName());
                cf.setOrderNo(df.getOrderNo());
                cf.setItemArrangementStrategy(df.getItemArrangementStrategy());
                cf.setParentComponent(cfs);

                cf.setBackgroundColour(df.getBackgroundColour());
                cf.setForegroundColour(df.getForegroundColour());
                cf.setBorderColour(df.getBorderColour());

                clientEncounterComponentFormController.save(cf);

                List<DesignComponentFormItem> diList = designComponentFormItemController.fillItemsOfTheForm(df);

                for (DesignComponentFormItem dis : diList) {

                    boolean disSkipThisItem = false;
                    if (dis.getComponentSex() == ComponentSex.For_Females && clientController.getSelected().getPerson().getSex().getCode().equalsIgnoreCase("sex_male")) {
                        disSkipThisItem = true;
                    }
                    if (dis.getComponentSex() == ComponentSex.For_Males && clientController.getSelected().getPerson().getSex().getCode().equalsIgnoreCase("sex_female")) {
                        disSkipThisItem = true;
                    }

                    if (!disSkipThisItem) {
                        ClientEncounterComponentItem ci = new ClientEncounterComponentItem();

                        ci.setEncounter(e);
                        ci.setInstitution(dfs.getInstitution());

                        ci.setItemFormset(cfs);
                        ci.setItemEncounter(e);
                        ci.setItemClient(e.getClient());

                        ci.setItem(dis.getItem());
                        ci.setDescreption(dis.getDescreption());

                        ci.setRequired(dis.isRequired());
                        ci.setRequiredErrorMessage(dis.getRequiredErrorMessage());
                        ci.setRegexValidationString(dis.getRegexValidationString());
                        ci.setRegexValidationFailedMessage(dis.getRegexValidationFailedMessage());

                        ci.setReferenceComponent(dis);
                        ci.setParentComponent(cf);
                        ci.setName(dis.getName());
                        ci.setRenderType(dis.getRenderType());
                        ci.setMimeType(dis.getMimeType());
                        ci.setSelectionDataType(dis.getSelectionDataType());
                        ci.setTopPercent(dis.getTopPercent());
                        ci.setLeftPercent(dis.getLeftPercent());
                        ci.setWidthPercent(dis.getWidthPercent());
                        ci.setHeightPercent(dis.getHeightPercent());
                        ci.setCategoryOfAvailableItems(dis.getCategoryOfAvailableItems());
                        ci.setOrderNo(dis.getOrderNo());
                        ci.setDataPopulationStrategy(dis.getDataPopulationStrategy());
                        ci.setDataModificationStrategy(dis.getDataModificationStrategy());
                        ci.setDataCompletionStrategy(dis.getDataCompletionStrategy());
                        ci.setIntHtmlColor(dis.getIntHtmlColor());
                        ci.setHexHtmlColour(dis.getHexHtmlColour());

                        ci.setForegroundColour(dis.getForegroundColour());
                        ci.setBackgroundColour(dis.getBackgroundColour());
                        ci.setBorderColour(dis.getBorderColour());

                        ci.setCalculateOnFocus(dis.isCalculateOnFocus());
                        ci.setCalculationScript(dis.getCalculationScript());

                        ci.setCalculateButton(dis.isCalculateButton());
                        ci.setCalculationScriptForColour(dis.getCalculationScriptForColour());
                        ci.setDisplayDetailsBox(dis.isDisplayDetailsBox());
                        ci.setDiscreptionAsAToolTip(dis.isDiscreptionAsAToolTip());

                        ci.setDisplayLastResult(dis.isDisplayLastResult());
                        ci.setDisplayLinkToClientValues(dis.isDisplayLastResult());
                        ci.setResultDisplayStrategy(dis.getResultDisplayStrategy());
                        ci.setDisplayLinkToResultList(dis.isDisplayLinkToResultList());

                        ci.setDiscreptionAsASideLabel(dis.isDiscreptionAsASideLabel());

                        ci.setCalculationScriptForBackgroundColour(dis.getCalculationScriptForBackgroundColour());
                        ci.setMultipleEntiesPerForm(dis.isMultipleEntiesPerForm());

                        ci.setDataRepresentationType(DataRepresentationType.Encounter);

                        clientEncounterComponentItemController.save(ci);

                        if (ci.getDataPopulationStrategy() == DataPopulationStrategy.From_Client_Value) {
                            updateFromClientValueSingle(ci, e.getClient(), mapOfClientValues);
                        } else if (ci.getDataPopulationStrategy() == DataPopulationStrategy.From_Last_Encounter) {
                            updateFromLastEncounter(ci);
                        }

                        clientEncounterComponentItemController.save(ci);

                    }

                }

            }

        }

        selected = cfs;
        return navigationLink;
    }

    public ClientEncounterComponentItem fillClientValue(Client c, String code) {
        if (c == null || code == null) {
            return null;
        }
        Item i = itemController.findItemByCode(code);
        if (i == null) {
            return null;
        }

        String j = "select vi from ClientEncounterComponentItem vi where vi.retired=false "
                + " and vi.client=:c "
                + " and vi.item.code=:i "
                + " and vi.dataRepresentationType=:r "
                + " order by vi.id desc";
        Map m = new HashMap();
        m.put("c", c);
        m.put("i", i.getCode());
        m.put("r", DataRepresentationType.Client);
        return getItemFacade().findFirstByJpql(j, m);
    }

    public List<ClientEncounterComponentItem> fillClientValues(Client c, String code) {

        Item i = itemController.findItemByCode(code);
        if (i == null) {
            return new ArrayList<>();
        }
        String j = "select vi from ClientEncounterComponentItem vi where vi.retired=false "
                + " and vi.client=:c "
                + " and vi.item.code=:i "
                + " and vi.dataRepresentationType=:r "
                + " order by vi.id desc";
        Map m = new HashMap();
        m.put("c", c);
        m.put("i", i.getCode());
        m.put("r", DataRepresentationType.Client);
        return getItemFacade().findByJpql(j, m);
    }

    public List<ClientEncounterComponentItem> updateFromClientValueMultiple(ClientEncounterComponentItem ti, Client c) {

        List<ClientEncounterComponentItem> listOfClientItems = new ArrayList<>();

        String code = ti.getItem().getCode();

        ClientEncounterComponentItem vi;
        List<ClientEncounterComponentItem> vis;
        String j = "select vi from ClientEncounterComponentItem vi where vi.retired=false "
                + " and vi.client=:c "
                + " and vi.item.code=:i "
                + " and vi.dataRepresentationType=:r "
                + " order by vi.id desc";
        Map m = new HashMap();
        m.put("c", ti.getEncounter().getClient());
        m.put("i", ti.getItem().getCode());
        m.put("r", DataRepresentationType.Client);
        vis = getItemFacade().findByJpql(j, m);
        if (vis == null || vis.isEmpty()) {
            vis = new ArrayList<>();
        }
        Double positionIncrement = ti.getOrderNo();
        int temIndex = 0;
        for (ClientEncounterComponentItem tvi : vis) {
            if (temIndex == 0) {
                ti.setDateValue(tvi.getDateValue());
                ti.setShortTextValue(tvi.getShortTextValue());
                ti.setLongTextValue(tvi.getLongTextValue());
                ti.setItemValue(tvi.getItemValue());
                ti.setAreaValue(tvi.getAreaValue());
                ti.setItemValue(tvi.getItemValue());
                ti.setInstitution(tvi.getInstitutionValue());
                ti.setPrescriptionValue(tvi.getPrescriptionValue());
                ti.setOrderNo(positionIncrement);
                getItemFacade().edit(ti);
                listOfClientItems.add(ti);
            } else {
                ClientEncounterComponentItem nti = SerializationUtils.clone(ti);
                nti.setDateValue(tvi.getDateValue());
                nti.setShortTextValue(tvi.getShortTextValue());
                nti.setLongTextValue(tvi.getLongTextValue());
                nti.setItemValue(tvi.getItemValue());
                nti.setAreaValue(tvi.getAreaValue());
                nti.setItemValue(tvi.getItemValue());
                nti.setInstitution(tvi.getInstitutionValue());
                nti.setPrescriptionValue(tvi.getPrescriptionValue());
                nti.setId(null);
                nti.setOrderNo(positionIncrement);
                getItemFacade().create(nti);
                listOfClientItems.add(nti);
            }
            positionIncrement += 0.0001;
            temIndex++;
        }

        if (listOfClientItems.isEmpty()) {
            listOfClientItems.add(ti);
        }

        return listOfClientItems;
    }

    public Map<String, ClientEncounterComponentItem> getClientValues(Client c) {
        List<ClientEncounterComponentItem> vis;
        String j = "select vi from ClientEncounterComponentItem vi where vi.retired=false "
                + " and vi.client=:c "
                + " and vi.dataRepresentationType=:r ";
        Map m = new HashMap();
        m.put("c", c);
        m.put("r", DataRepresentationType.Client);
        vis = getItemFacade().findByJpql(j, m);
        Map<String, ClientEncounterComponentItem> map = new HashMap();
        for (ClientEncounterComponentItem vi : vis) {
            map.put(vi.getCode(), vi);
        }
        return map;
    }

    public void updateFromClientValueSingle(ClientEncounterComponentItem ti, Client c, Map<String, ClientEncounterComponentItem> cvs) {

        String code = ti.getItem().getCode();
        switch (code) {
            case "client_name":
                ti.setShortTextValue(c.getPerson().getName());
                return;
            case "client_phn_number":
                ti.setShortTextValue(c.getPhn());
                return;
            case "client_occupation":
                ti.setShortTextValue(c.getPerson().getOccupation());
                return;
            case "client_sex":
                ti.setItemValue(c.getPerson().getSex());
                return;
            case "client_nic_number":
                ti.setShortTextValue(c.getPerson().getNic());
                return;
            case "client_data_of_birth":
                ti.setDateValue(c.getPerson().getDateOfBirth());
                return;
            case "client_current_age":
                ti.setShortTextValue(c.getPerson().getAge());
                return;
            case "client_age_at_encounter_in_years":
                ti.setIntegerNumberValue(c.getPerson().getAgeYears());
                ti.setShortTextValue(c.getPerson().getAgeYears() + "");
                ti.setRealNumberValue(Double.valueOf(c.getPerson().getAgeYears()));
                return;
            case "client_age_at_encounter":
                ti.setShortTextValue(c.getPerson().getAge());
                return;
            case "client_permanent_address":
                ti.setLongTextValue(c.getPerson().getAddress());
                return;
            case "client_current_address":
                ti.setLongTextValue(c.getPerson().getAddress());
                return;
            case "client_mobile_number":
                ti.setShortTextValue(c.getPerson().getPhone1());
                return;
            case "client_home_number":
                ti.setShortTextValue(c.getPerson().getPhone2());
                return;
            case "client_permanent_moh_area":
                ti.setAreaValue(c.getPerson().getGnArea());
                return;
            case "client_permanent_phm_area":
                ti.setAreaValue(c.getPerson().getGnArea().getPhm());
                return;
            case "client_permanent_phi_area":
                ti.setAreaValue(c.getPerson().getGnArea().getPhi());
                return;
            case "client_gn_area":
                ti.setAreaValue(c.getPerson().getGnArea());
                return;
            case "client_ds_division":
                ti.setAreaValue(c.getPerson().getGnArea().getDsd());
                return;
        }

        ClientEncounterComponentItem vi;
        vi = cvs.get(ti.getItem().getCode());

        if (vi == null) {
            return;
        }
        ti.setDateValue(vi.getDateValue());
        ti.setShortTextValue(vi.getShortTextValue());
        ti.setLongTextValue(vi.getLongTextValue());
        ti.setItemValue(vi.getItemValue());
        ti.setAreaValue(vi.getAreaValue());
        ti.setItemValue(vi.getItemValue());
        ti.setInstitution(vi.getInstitutionValue());
        ti.setPrescriptionValue(vi.getPrescriptionValue());
        getItemFacade().edit(ti);

    }

    public String lastData(ClientEncounterComponentItem ci) {
        String lr = "";
        if (ci == null) {
            return lr;
        }
        List<ClientEncounterComponentItem> lcis = null;
        if (ci.getResultDisplayStrategy() == DataPopulationStrategy.From_Client_Value) {
            lcis = dataFromClientValue(ci);
        } else if (ci.getResultDisplayStrategy() == DataPopulationStrategy.From_Last_Encounter) {
            lcis = dataFromLastEncounter(ci);
        }
        if (ci.getResultDisplayStrategy() == DataPopulationStrategy.From_Last_Encounter_of_same_clinic) {

        }
        if (ci.getResultDisplayStrategy() == DataPopulationStrategy.From_Last_Encounter_of_same_formset) {

        }
        if (lcis == null) {
            return lr;
        }

        switch (ci.getSelectionDataType()) {
            case Area_Reference:
                lr = areaValueToString(lcis);
                break;
            case Boolean:
                lr = booleanValueToString(lcis);
                break;
            case Client_Reference:
                lr = clientValueToString(lcis);
                break;
            case DateTime:
                lr = dateValueToString(lcis);
                break;
            case Integer_Number:
                lr = integerValueToString(lcis);
                break;
            case Item_Reference:
                lr = itemValueToString(lcis);
                break;
            case Long_Text:
                lr = longTextValueToString(lcis);
                break;
            case Prescreption_Reference:
                lr = prescreptionValueToString(lcis);
                break;
            case Long_Number:
                lr = longNumberValueToString(lcis);
                break;
            case Real_Number:
                lr = realNumberValueToString(lcis);
                break;
            case Short_Text:
                lr = shortTextValueToString(lcis);
                break;
            default:

        }
        return lr;
    }

    private String prescreptionValueToString(List<ClientEncounterComponentItem> is) {
        String s = "";
        if (is == null) {
            return s;
        }
        if (is.isEmpty()) {
            return s;
        }
        if (is.size() == 1) {
            if (is.get(0).getPrescriptionValue() == null) {
                return s;
            }
            return is.get(0).getPrescriptionValue().toString();
        }
        for (ClientEncounterComponentItem i : is) {
            if (i.getCreatedAt() != null) {
                s += commonController.dateToString(i.getCreatedAt()) + " ";
            }
            if (i.getPrescriptionValue() != null) {
                s += i.getPrescriptionValue().toString() + "\n";
            }

        }
        return s;
    }

    private String realNumberValueToString(List<ClientEncounterComponentItem> is) {
        String s = "";
        if (is == null) {
            return s;
        }
        if (is.isEmpty()) {
            return s;
        }
        if (is.size() == 1) {
            if (is.get(0) == null) {
                return s;
            }
            if (is.get(0).getLongNumberValue() == null) {
                return s;
            }
            return is.get(0).getLongNumberValue().toString();
        }
        for (ClientEncounterComponentItem i : is) {
            if (i.getCreatedAt() != null) {
                s += commonController.dateToString(i.getCreatedAt()) + " ";
            }
            if (i.getLongNumberValue() != null) {
                s += i.getLongNumberValue().toString() + "\n";
            }
        }
        return s;
    }

    private String longNumberValueToString(List<ClientEncounterComponentItem> is) {
        String s = "";
        if (is == null) {
            return s;
        }
        if (is.isEmpty()) {
            return s;
        }
        if (is.size() == 1) {
            if (is.get(0).getLongNumberValue() == null) {
                return s;
            }
            return is.get(0).getLongNumberValue().toString();
        }
        for (ClientEncounterComponentItem i : is) {
            if (i.getCreatedAt() != null) {
                s += commonController.dateToString(i.getCreatedAt()) + " ";
            }
            if (i.getLongNumberValue() != null) {
                s += i.getLongNumberValue().toString() + "\n";
            }
        }
        return s;
    }

    private String shortTextValueToString(List<ClientEncounterComponentItem> is) {
        String s = "";
        if (is == null) {
            return s;
        }
        if (is.isEmpty()) {
            return s;
        }
        if (is.size() == 1) {
            if (is.get(0).getShortTextValue() == null) {
                return s;
            }
            return is.get(0).getShortTextValue();
        }
        for (ClientEncounterComponentItem i : is) {
            if (i.getCreatedAt() != null) {
                s += commonController.dateToString(i.getCreatedAt()) + " ";
            }
            if (i.getShortTextValue() != null) {
                s += i.getShortTextValue() + "\n";
            }
        }
        return s;
    }

    private String longTextValueToString(List<ClientEncounterComponentItem> is) {
        String s = "";
        if (is == null) {
            return s;
        }
        if (is.isEmpty()) {
            return s;
        }
        if (is.size() == 1) {
            if (is.get(0).getLongTextValue() == null) {
                return s;
            }
            return is.get(0).getLongTextValue();
        }
        for (ClientEncounterComponentItem i : is) {
            if (i.getCreatedAt() != null) {
                s += commonController.dateToString(i.getCreatedAt()) + " ";
            }
            if (i.getLongTextValue() != null) {
                s += i.getLongTextValue() + "\n";
            }
        }
        return s;
    }

    private String itemValueToString(List<ClientEncounterComponentItem> is) {
        String s = "";
        if (is == null) {
            return s;
        }
        if (is.isEmpty()) {
            return s;
        }
        if (is.size() == 1) {
            if (is.get(0).getItemValue() == null) {
                return s;
            }
            return is.get(0).getItemValue().getName();
        }
        for (ClientEncounterComponentItem i : is) {
            if (i.getCreatedAt() != null) {
                s += commonController.dateToString(i.getCreatedAt()) + " ";
            }
            if (i.getItemValue() != null) {
                s += i.getItemValue().getName() + "\n";
            }
        }
        return s;
    }

    private String integerValueToString(List<ClientEncounterComponentItem> is) {
        String s = "";
        if (is == null) {
            return s;
        }
        if (is.isEmpty()) {
            return s;
        }
        if (is.size() == 1) {
            if (is.get(0).getIntegerNumberValue() == null) {
                return s;
            }
            return is.get(0).getIntegerNumberValue().toString();
        }
        for (ClientEncounterComponentItem i : is) {
            if (i.getCreatedAt() != null) {
                s += commonController.dateToString(i.getCreatedAt()) + " ";
            }
            if (i.getIntegerNumberValue() != null) {
                s += i.getIntegerNumberValue().toString() + "\n";
            }
        }
        return s;
    }

    private String booleanValueToString(List<ClientEncounterComponentItem> is) {
        String s = "";
        if (is == null) {
            return s;
        }
        if (is.isEmpty()) {
            return s;
        }
        if (is.size() == 1) {
            if (is.get(0).getBooleanValue() == null) {
                return s;
            }
            return is.get(0).getBooleanValue().toString();
        }
        for (ClientEncounterComponentItem i : is) {
            if (i.getCreatedAt() != null) {
                s += commonController.dateToString(i.getCreatedAt()) + " " + i.getBooleanValue().toString() + "\n";
            }
            if (i.getBooleanValue() != null) {
                s += i.getBooleanValue().toString() + "\n";
            }
        }
        return s;
    }

    private String areaValueToString(List<ClientEncounterComponentItem> is) {
        String s = "";
        if (is == null) {
            return s;
        }
        if (is.isEmpty()) {
            return s;
        }
        if (is.size() == 1) {
            if (is.get(0).getAreaValue() == null) {
                return s;
            }
            return is.get(0).getAreaValue().getName();
        }
        for (ClientEncounterComponentItem i : is) {
            if (i.getCreatedAt() != null) {
                s += commonController.dateToString(i.getCreatedAt()) + " ";
            }
            if (i.getAreaValue() != null) {
                s += i.getAreaValue().getName() + "\n";
            }
        }
        return s;
    }

    private String clientValueToString(List<ClientEncounterComponentItem> is) {
        String s = "";
        if (is == null) {
            return s;
        }
        if (is.isEmpty()) {
            return s;
        }
        if (is.size() == 1) {
            if (is.get(0).getClientValue() == null || is.get(0).getClientValue().getPerson() == null) {
                return s;
            }
            return is.get(0).getClientValue().getPerson().getNameWithTitle();
        }
        for (ClientEncounterComponentItem i : is) {
            if (i.getCreatedAt() != null) {
                s += commonController.dateToString(i.getCreatedAt()) + " ";
            }
            if (i.getClientValue() != null && i.getClientValue().getPerson() != null) {
                s += i.getClientValue().getPerson().getNameWithTitle() + "\n";
            }
        }
        return s;
    }

    private String dateValueToString(List<ClientEncounterComponentItem> is) {
        String s = "";
        if (is == null) {
            return s;
        }
        if (is.isEmpty()) {
            return s;
        }

        if (is.size() == 1) {
            if (is.get(0).getDateValue() == null) {
                return s;
            }
            return commonController.dateToString(is.get(0).getDateValue());
        }
        for (ClientEncounterComponentItem i : is) {
            if (i.getCreatedAt() != null) {
                s += commonController.dateToString(i.getCreatedAt()) + " ";
            }
            if (i.getDateValue() != null) {
                s += commonController.dateToString(i.getDateValue()) + "\n";
            }
        }
        return s;
    }

    public List<ClientEncounterComponentItem> dataFromClientValue(ClientEncounterComponentItem ti) {
        String j = "select vi from ClientEncounterComponentItem vi where vi.retired=false "
                + " and vi.client=:c "
                + " and vi.item.code=:i "
                + " and vi.dataRepresentationType=:r "
                + " order by vi.id desc";
        Map m = new HashMap();
        Client c;
        if (ti.getEncounter() == null && ti.getClient() == null) {
            return null;
        } else if (ti.getEncounter() != null && ti.getClient() == null) {
            if (ti.getEncounter().getClient() == null) {
                return null;
            } else {
                c = ti.getEncounter().getClient();
            }
        } else {
            c = ti.getClient();
        }
        m.put("r", DataRepresentationType.Client);
        m.put("c", c);
        m.put("i", ti.getItem().getCode());
        List<ClientEncounterComponentItem> tis = getItemFacade().findByJpql(j, m);
        return tis;

    }

    public List<ClientEncounterComponentItem> dataFromLastEncounter(ClientEncounterComponentItem ti) {
        if (ti == null) {
            return null;
        }
        Client c;
        if (ti.getEncounter() == null && ti.getClient() == null) {
            return null;
        } else if (ti.getEncounter() != null && ti.getClient() == null) {
            if (ti.getEncounter().getClient() == null) {
                return null;
            } else {
                c = ti.getEncounter().getClient();
            }
        } else {
            c = ti.getClient();
        }
        Encounter lastEncounter;
        String j;
        Map m;

        m = new HashMap();
        m.put("c", ti.getEncounter().getClient());
        m.put("r", DataRepresentationType.Encounter);
        m.put("te", ti.getEncounter());
        j = "select vi.encounter from ClientEncounterComponentItem vi where vi.retired=false "
                + " and vi.encounter.client=:c "
                + " and vi.encounter <> :te "
                + " and vi.dataRepresentationType=:r ";
        switch (ti.getSelectionDataType()) {
            case Area_Reference:
                j += "and vi.areaValue is not null ";
                break;
            case Boolean:
                j += "and vi.booleanValue is not null ";
                break;
            case Byte_Array:
                j += "and vi.byteArrayValue is not null ";
                break;
            case Client_Reference:
                j += "and vi.clientValue is not null";
                break;
            case DateTime:
                j += "and vi.dateValue is not null ";
                break;
            case Integer_Number:
                j += "and vi.integerNumberValue is not null ";
                break;
            case Item_Reference:
                j += "and vi.itemValue is not null ";
                break;
            case Long_Number:
                j += "and vi.longNumberValue is not null ";
                break;
            case Long_Text:
                j += "and vi.longTextValue is not null ";
                break;
            case Prescreption_Reference:
                j += "and vi.prescriptionValue.medicine is not null ";
                break;
            case Real_Number:
                j += "and vi.realNumberValue is not null ";
                break;
            case Short_Text:
                j += "and vi.shortTextValue is not null";
                break;
        }
        j += " order by vi.id desc";
        lastEncounter = getEncounterFacade().findFirstByJpql(j, m);
        j = "select vi from ClientEncounterComponentItem vi "
                + " where vi.retired=false "
                + " and vi.item.code=:ic"
                + " and vi.encounter=:e "
                + " and vi.dataRepresentationType=:r ";
        j += " order by vi.id desc";
        m = new HashMap();
        m.put("r", DataRepresentationType.Encounter);
        m.put("e", lastEncounter);
        m.put("ic", ti.getItem().getCode());
        List<ClientEncounterComponentItem> temLastResult = getItemFacade().findByJpql(j, m);
        return temLastResult;
    }

    public void updateFromLastEncounter(ClientEncounterComponentItem ti) {
        if (ti == null) {
            return;
        }
        Client c;
        if (ti.getEncounter() == null && ti.getClient() == null) {
            return;
        } else if (ti.getEncounter() != null && ti.getClient() == null) {
            if (ti.getEncounter().getClient() == null) {
                return;
            } else {
                c = ti.getEncounter().getClient();
            }
        } else {
            c = ti.getClient();
        }

        ClientEncounterComponentItem vi;
        String j = "select vi from ClientEncounterComponentItem vi where vi.retired=false "
                + " and vi.encounter.client=:c "
                + " and vi.dataRepresentationType=:r "
                + " order by vi.id desc";
        Map m = new HashMap();
        m.put("c", ti.getEncounter().getClient());
        m.put("r", DataRepresentationType.Encounter);
        vi = getItemFacade().findFirstByJpql(j, m);

        if (vi == null) {
            return;
        }

        ti.setDateValue(vi.getDateValue());
        ti.setShortTextValue(vi.getShortTextValue());
        ti.setLongTextValue(vi.getLongTextValue());
        ti.setItemValue(vi.getItemValue());
        ti.setAreaValue(vi.getAreaValue());
        ti.setItemValue(vi.getItemValue());
        ti.setInstitution(vi.getInstitutionValue());
        ti.setPrescriptionValue(vi.getPrescriptionValue());

    }

// </editor-fold>    
// <editor-fold defaultstate="collapsed" desc="Default Functions">
    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    public ClientEncounterComponentFormSet prepareCreate() {
        selected = new ClientEncounterComponentFormSet();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/BundleClinical").getString("ClientEncounterComponentFormSetCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/BundleClinical").getString("ClientEncounterComponentFormSetUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/BundleClinical").getString("ClientEncounterComponentFormSetDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    private void persist(PersistAction persistAction, String successMessage) {
        if (selected != null) {
            setEmbeddableKeys();
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
    
    
    
    public ClientEncounterComponentFormSet getSelected() {
        return selected;
    }

    public void setSelected(ClientEncounterComponentFormSet selected) {
        this.selected = selected;
    }

    private ClientEncounterComponentFormSetFacade getFacade() {
        return ejbFacade;
    }

    public List<ClientEncounterComponentFormSet> getItems() {
//        if (items == null) {
//            items = getFacade().findAll();
//        }
        return items;
    }

    public ClientEncounterComponentFormSet getClientEncounterComponentFormSet(java.lang.Long id) {
        return getFacade().find(id);
    }

    public List<ClientEncounterComponentFormSet> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<ClientEncounterComponentFormSet> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    public DesignComponentFormSet getDesignFormSet() {
        return designFormSet;
    }

    public void setDesignFormSet(DesignComponentFormSet designFormSet) {
        this.designFormSet = designFormSet;
    }

    public lk.gov.health.phsp.facade.ClientEncounterComponentFormSetFacade getEjbFacade() {
        return ejbFacade;
    }

    public DesignComponentFormSetController getDesignComponentFormSetController() {
        return designComponentFormSetController;
    }

    public DesignComponentFormController getDesignComponentFormController() {
        return designComponentFormController;
    }

    public DesignComponentFormItemController getDesignComponentFormItemController() {
        return designComponentFormItemController;
    }

    public ClientEncounterComponentFormController getClientEncounterComponentFormController() {
        return clientEncounterComponentFormController;
    }

    public ClientEncounterComponentItemController getClientEncounterComponentItemController() {
        return clientEncounterComponentItemController;
    }

    public ClientController getClientController() {
        return clientController;
    }

    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public void setWebUserController(WebUserController webUserController) {
        this.webUserController = webUserController;
    }

    public EncounterController getEncounterController() {
        return encounterController;
    }

    public void setEncounterController(EncounterController encounterController) {
        this.encounterController = encounterController;
    }

    public ClientEncounterComponentItemFacade getItemFacade() {
        return itemFacade;
    }

    public boolean isFormEditable() {
        return formEditable;
    }

    public void setFormEditable(boolean formEditable) {
        this.formEditable = formEditable;
    }

    public ClientFacade getClientFacade() {
        return clientFacade;
    }

    public PersonFacade getPersonFacade() {
        return personFacade;
    }

    public ItemController getItemController() {
        return itemController;
    }

    public Date getEncounterDate() {
        return encounterDate;
    }

    public void setEncounterDate(Date encounterDate) {
        this.encounterDate = encounterDate;
    }

    public EncounterFacade getEncounterFacade() {
        return encounterFacade;
    }

    public CommonController getCommonController() {
        return commonController;
    }

    public List<ClientEncounterComponentFormSet> getSelectedItems() {
        return selectedItems;
    }

    public void setSelectedItems(List<ClientEncounterComponentFormSet> selectedItems) {
        this.selectedItems = selectedItems;
    }

    public Integer getSelectedTabIndex() {
        if (selectedTabIndex == null) {
            selectedTabIndex = 0;
        }
        return selectedTabIndex;
    }

    public void setSelectedTabIndex(Integer selectedTabIndex) {
        this.selectedTabIndex = selectedTabIndex;
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

    public List<ClientEncounterComponentFormSet> getLastFiveClinicVisits() {
        fillLastFiveVisits();
        return lastFiveClinicVisits;
    }

    public void setLastFiveClinicVisits(List<ClientEncounterComponentFormSet> lastFiveClinicVisits) {
        this.lastFiveClinicVisits = lastFiveClinicVisits;
    }

// </editor-fold>    
// <editor-fold defaultstate="collapsed" desc="Converter">
    @FacesConverter(forClass = ClientEncounterComponentFormSet.class)
    public static class ClientEncounterComponentFormSetControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ClientEncounterComponentFormSetController controller = (ClientEncounterComponentFormSetController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "clientEncounterComponentFormSetController");
            return controller.getClientEncounterComponentFormSet(getKey(value));
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
            if (object instanceof ClientEncounterComponentFormSet) {
                ClientEncounterComponentFormSet o = (ClientEncounterComponentFormSet) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), ClientEncounterComponentFormSet.class.getName()});
                return null;
            }
        }

    }

// </editor-fold>    
}
