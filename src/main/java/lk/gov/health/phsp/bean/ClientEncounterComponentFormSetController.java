package lk.gov.health.phsp.bean;

// <editor-fold defaultstate="collapsed" desc="Import">
import lk.gov.health.phsp.entity.ClientEncounterComponentFormSet;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.bean.util.JsfUtil.PersistAction;
import lk.gov.health.phsp.facade.ClientEncounterComponentFormSetFacade;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
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
import lk.gov.health.phsp.entity.ClientEncounterComponentForm;
import lk.gov.health.phsp.entity.ClientEncounterComponentItem;
import lk.gov.health.phsp.entity.DesignComponentForm;
import lk.gov.health.phsp.entity.DesignComponentFormItem;
import lk.gov.health.phsp.entity.DesignComponentFormSet;
import lk.gov.health.phsp.entity.Encounter;
import lk.gov.health.phsp.enums.ComponentSetType;
import lk.gov.health.phsp.enums.ComponentSex;
import lk.gov.health.phsp.enums.EncounterType;
// </editor-fold>

@Named("clientEncounterComponentFormSetController")
@SessionScoped
public class ClientEncounterComponentFormSetController implements Serializable {
// <editor-fold defaultstate="collapsed" desc="EJBs">

    @EJB
    private lk.gov.health.phsp.facade.ClientEncounterComponentFormSetFacade ejbFacade;
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

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Class Variables">
    private List<ClientEncounterComponentFormSet> items = null;
    private ClientEncounterComponentFormSet selected;
    private DesignComponentFormSet designFormSet;
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Constructors">

    public ClientEncounterComponentFormSetController() {
    }
// </editor-fold>    
// <editor-fold defaultstate="collapsed" desc="User Functions">

    public String createAndNavigateToClinicalEncounterComponentFormSetFromDesignComponentFormSet() {
        return createAndNavigateToClinicalEncounterComponentFormSetFromDesignComponentFormSetForClinicVisit(designFormSet);
    }

    public String createAndNavigateToClinicalEncounterComponentFormSetFromDesignComponentFormSetForClinicVisit(DesignComponentFormSet dfs) {
        String navigationLink = "/clientEncounterComponentFormSet/Formset";

        Encounter e = new Encounter();
        e.setClient(clientController.getSelected());
        e.setInstitution(dfs.getInstitution());
        e.setEncounterDate(new Date());
        e.setEncounterFrom(new Date());
        e.setEncounterType(EncounterType.Clinic_Visit);
        encounterController.save(e);

        ClientEncounterComponentFormSet cfs = new ClientEncounterComponentFormSet();

        cfs.setEncounter(e);
        cfs.setInstitution(dfs.getInstitution());

        cfs.setReferenceComponent(dfs);
        cfs.setComponentSetType(dfs.getComponentSetType());
        cfs.setPanelType(dfs.getPanelType());
        cfs.setName(dfs.getName());

        getFacade().create(cfs);

        List<DesignComponentForm> dfList = designComponentFormController.fillFormsofTheSelectedSet(dfs);

        for (DesignComponentForm df : dfList) {
            boolean skipThisForm = false;
            if (df.getComponentSex() == ComponentSex.For_Females && clientController.getSelected().getPerson().getSex().getCode().equalsIgnoreCase("male")) {
                skipThisForm = true;
            }
            if (df.getComponentSex() == ComponentSex.For_Males && clientController.getSelected().getPerson().getSex().getCode().equalsIgnoreCase("female")) {
                skipThisForm = true;
            }
            if (!skipThisForm) {

                ClientEncounterComponentForm cf = new ClientEncounterComponentForm();

                cf.setEncounter(e);
                cf.setInstitution(dfs.getInstitution());

                cf.setReferenceComponent(df);
                cf.setName(df.getName());
                cf.setOrderNo(df.getOrderNo());
                cf.setItemArrangementStrategy(df.getItemArrangementStrategy());
                cf.setParentComponent(cfs);

                clientEncounterComponentFormController.save(cf);

                List<DesignComponentFormItem> diList = designComponentFormItemController.fillItemsOfTheForm(df);

                for (DesignComponentFormItem di : diList) {

                    boolean skipThisItem = false;
                    if (di.getComponentSex() == ComponentSex.For_Females && clientController.getSelected().getPerson().getSex().getCode().equalsIgnoreCase("male")) {
                        skipThisItem = true;
                    }
                    if (di.getComponentSex() == ComponentSex.For_Males && clientController.getSelected().getPerson().getSex().getCode().equalsIgnoreCase("female")) {
                        skipThisItem = true;
                    }
                    if (!skipThisItem) {
                        ClientEncounterComponentItem ci = new ClientEncounterComponentItem();

                        ci.setEncounter(e);
                        ci.setInstitution(dfs.getInstitution());

                        ci.setReferenceComponent(di);
                        ci.setParentComponent(cf);
                        ci.setName(di.getName());
                        ci.setRenderType(di.getRenderType());
                        ci.setMimeType(di.getMimeType());
                        ci.setSelectionDataType(di.getSelectionDataType());
                        ci.setTopPercent(di.getTopPercent());
                        ci.setLeftPercent(di.getLeftPercent());
                        ci.setWidthPercent(di.getWidthPercent());
                        ci.setHeightPercent(di.getHeightPercent());
                        ci.setCategoryOfAvailableItems(di.getCategoryOfAvailableItems());
                        ci.setOrderNo(di.getOrderNo());
                        ci.setDataPopulationStrategy(di.getDataPopulationStrategy());
                        ci.setDataModificationStrategy(di.getDataModificationStrategy());
                        ci.setDataCompletionStrategy(di.getDataCompletionStrategy());
                        ci.setIntHtmlColor(di.getIntHtmlColor());
                        ci.setHexHtmlColour(di.getHexHtmlColour());

                        clientEncounterComponentItemController.save(ci);
                    }
                }

            }
        }

        selected = cfs;
        return navigationLink;
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
        if (items == null) {
            items = getFacade().findAll();
        }
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
