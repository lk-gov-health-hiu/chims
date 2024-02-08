package lk.gov.health.phsp.bean;

// <editor-fold defaultstate="collapsed" desc="Imports">
import lk.gov.health.phsp.entity.DesignComponentFormItem;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.bean.util.JsfUtil.PersistAction;
import lk.gov.health.phsp.facade.DesignComponentFormItemFacade;
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
import lk.gov.health.phsp.entity.Component;
import lk.gov.health.phsp.entity.DesignComponentForm;
import lk.gov.health.phsp.entity.DesignComponentFormSet;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.enums.SelectionDataType;
import lk.gov.health.phsp.facade.ItemFacade;
import org.apache.commons.lang3.SerializationUtils;
// </editor-fold>

@Named("designComponentFormItemController")
@SessionScoped
public class DesignComponentFormItemController implements Serializable {
// <editor-fold defaultstate="collapsed" desc="EJBs">

    @EJB
    private lk.gov.health.phsp.facade.DesignComponentFormItemFacade ejbFacade;
    @EJB
    private ItemFacade itemFacade;
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Controllers">
    @Inject
    UserTransactionController userTransactionController;
    // </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Main Functions">
    @Inject
    private WebUserController webUserController;

    public void fillDuplicateItemsInAFormSet(DesignComponentFormSet s) {
        String j = "select di from DesignComponentFormItem di "
                + "  where di.retired=false "
                + "  and di.parentComponent.parentComponent=:s ";
        Map m = new HashMap();
        m.put("s", s);
        items = getFacade().findByJpql(j, m);
    }

    
    public List<Item> completeItemsInDesignFormItems(String qry){
        String j = "select distinct(di.item) "
                + " from DesignComponentFormItem di "
                + " where di.retired<>:ret "
                + " and (di.item.name like :qry or di.item.code like :qry) "
                + " order by di.item.name  ";
        Map m = new HashMap();
        m.put("ret", true);
        m.put("qry", "%" + qry.trim().toLowerCase() + "%");
        return getItemFacade().findByJpql(j, m);
    }
    
    
    
    
    
    
    public void addDataTypesToDictionaryItemsFromFormItems() {
        String j = "select di from DesignComponentFormItem di "
                + "  where di.retired=:ret ";
        Map m = new HashMap();
        m.put("ret", false);
        items = getFacade().findByJpql(j, m);
        for(DesignComponentFormItem fi:items){
            if(fi.getItem()!=null){
                Item i = fi.getItem();
                if(fi.getSelectionDataType()!=null){
                    i.setDataType(fi.getSelectionDataType());
                }
                getItemFacade().edit(i);
            }
        }
        userTransactionController.recordTransaction("Add Data Types To Dictionary Items From Form Items");
    }

// </editor-fold>    
// <editor-fold defaultstate="collapsed" desc="Class Variables">
    private List<DesignComponentFormItem> items = null;
    private DesignComponentFormItem selected;
    private DesignComponentForm designComponentForm;
    private List<DesignComponentFormItem> designComponentFormItems = null;
    private DesignComponentFormItem addingItem;
    private DesignComponentFormItem removingItem;
    private DesignComponentFormItem movingItem;
    private Long searchId;

    Component searchComponent;

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Constructors">
    public DesignComponentFormItemController() {
    }
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Navigation Functions">

    public String toEditDesignComponentFromItem() {
        if (selected == null) {
            JsfUtil.addErrorMessage("No item selected.");
            return "";
        }
        return "/designComponentFormItem/item";
    }

    public String toDesignDesignComponentFromItem() {
        if (selected == null) {
            JsfUtil.addErrorMessage("No item selected.");
            return "";
        }
        return "/designComponentFormItem/design";
    }
    
    public void fixItemDataTypeToMatchSelectionDataType() {
        if (selected == null) {
            JsfUtil.addErrorMessage("No form item selected.");
            return ;
        }
        if(selected.getItem()==null){
            JsfUtil.addErrorMessage("No item available for form item.");
            return ;
        }
        if(selected.getSelectionDataType() ==null){
            JsfUtil.addErrorMessage("No selection data type available for form item.");
            return ;
        }
        selected.getItem().setDataType(selected.getSelectionDataType());
        getItemFacade().edit(selected.getItem());
        JsfUtil.addSuccessMessage("Updated");
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Main Functions">
    public void searchById() {
        selected = getFacade().find(searchId);
    }

    public void saveItem() {
        if (selected == null) {
            JsfUtil.addErrorMessage("No item selected.");
            return;
        }
        if (selected.getId() == null) {
            selected.setCreatedAt(new Date());
            selected.setCreatedBy(webUserController.getLoggedUser());
            getFacade().create(selected);
            JsfUtil.addSuccessMessage("Saved Successfully.");
        } else {
//            selected.setLastEditBy(webUserController.getLoggedUser());
//            selected.setLastEditeAt(new Date());
            getFacade().edit(selected);
            JsfUtil.addSuccessMessage("Updated Successfully.");
        }
    }

    public void saveItem(DesignComponentFormItem i) {
        if (i == null) {
            return;
        }
        if (i.getId() == null) {
            i.setCreatedAt(new Date());
            i.setCreatedBy(webUserController.getLoggedUser());
            getFacade().create(i);
        } else {
//            i.setLastEditBy(webUserController.getLoggedUser());
//            i.setLastEditeAt(new Date());
            getFacade().edit(i);
        }
    }

    public void fillItemsOfTheForm() {
        designComponentFormItems = fillItemsOfTheForm(designComponentForm);
    }

    public List<DesignComponentFormItem> fillItemsOfTheForm(DesignComponentForm form) {
        List<DesignComponentFormItem> is;
        if (form == null) {
            is = new ArrayList<>();
            return is;
        }
        String j = "Select i from DesignComponentFormItem i where i.retired=false "
                + " and i.parentComponent=:p "
                + " order by i.orderNo";
        Map m = new HashMap();
        m.put("p", form);
        return getFacade().findByJpql(j, m);
    }
    
    public List<DesignComponentFormItem> fillItemsOfTheFormSet(DesignComponentFormSet formset) {
        List<DesignComponentFormItem> is;
        if (formset == null) {
            is = new ArrayList<>();
            return is;
        }
        String j = "Select i from DesignComponentFormItem i where i.retired=false "
                + " and i.parentComponent.parentComponent=:p "
                + " order by i.parentComponent.orderNo, i.orderNo ";
        Map m = new HashMap();
        m.put("p", formset);
        return getFacade().findByJpql(j, m);
    }

    public void addNewItemToForm() {
        if (designComponentForm == null) {
            JsfUtil.addErrorMessage("No Form Selected");
            return;
        }
        if (addingItem == null) {
            JsfUtil.addErrorMessage("No item to add");
            return;
        }
        addingItem.setParentComponent(designComponentForm);
        addingItem.setCreatedAt(new Date());
        addingItem.setCreatedBy(webUserController.getLoggedUser());
        getFacade().create(addingItem);
        addingItem = null;
        getAddingItem();
        fillItemsOfTheForm();
        createNewAddingItem();
        JsfUtil.addSuccessMessage("New Item added.");
    }

    public void removeItemFromFrom() {
        if (designComponentForm == null) {
            JsfUtil.addErrorMessage("No Form Selected");
            return;
        }
        if (removingItem == null) {
            JsfUtil.addErrorMessage("No item to remove");
            return;
        }
        removingItem.setRetired(true);
//        removingItem.setRetiredAt(new Date());
//        removingItem.setRetiredBy(webUserController.getLoggedUser());
        getFacade().edit(removingItem);
        fillItemsOfTheForm();
        JsfUtil.addSuccessMessage("Item removed.");
    }

    public void moveItemUpInForm() {
        if (designComponentForm == null) {
            JsfUtil.addErrorMessage("No Form Selected");
            return;
        }
        if (movingItem == null) {
            JsfUtil.addErrorMessage("No item to move");
            return;
        }
        movingItem.setOrderNo(movingItem.getOrderNo() - 1.5);
        getFacade().edit(movingItem);
        double d = 0.0;
        fillItemsOfTheForm();
        for (DesignComponentFormItem i : designComponentFormItems) {
            d = d + 1.0;
            i.setOrderNo(d);
            getFacade().edit(i);
        }
        fillItemsOfTheForm();
    }

    public void moveItemDownInForm() {
        if (designComponentForm == null) {
            JsfUtil.addErrorMessage("No Form Selected");
            return;
        }
        if (movingItem == null) {
            JsfUtil.addErrorMessage("No item to move");
            return;
        }
        movingItem.setOrderNo(movingItem.getOrderNo() + 1.5);
        getFacade().edit(movingItem);
        double d = 0.0;
        fillItemsOfTheForm();
        for (DesignComponentFormItem i : designComponentFormItems) {
            d = d + 1.0;
            i.setOrderNo(d);
            getFacade().edit(i);
        }
        fillItemsOfTheForm();
    }

    public void createNewAddingItem() {
        if (designComponentForm == null) {
            JsfUtil.addErrorMessage("No Form Selected");
            return;
        }
        addingItem = new DesignComponentFormItem();
        addingItem.setParentComponent(designComponentForm);
        addingItem.setComponentSex(designComponentForm.getComponentSex());
        addingItem.setOrderNo(getDesignComponentFormItems().size() + 1.0);

    }

    public void saveSelectedItem() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing Selected");
            return;
        }
        if (selected.getId() == null) {
            selected.setCreatedAt(new Date());
            selected.setCreatedBy(webUserController.getLoggedUser());
            getFacade().create(selected);
            JsfUtil.addSuccessMessage("Saved Successfully.");
        } else {
//            selected.setLastEditBy(webUserController.getLoggedUser());
//            selected.setLastEditeAt(new Date());
            getFacade().edit(selected);
            JsfUtil.addSuccessMessage("Updated Successfully.");
        }

    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Default Functions">
    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    public DesignComponentFormItem prepareCreate() {
        selected = new DesignComponentFormItem();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/BundleClinical").getString("DesignComponentFormItemCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/BundleClinical").getString("DesignComponentFormItemUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/BundleClinical").getString("DesignComponentFormItemDeleted"));
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

    public DesignComponentFormItem getSelected() {
        return selected;
    }

    public void setSelected(DesignComponentFormItem selected) {
        this.selected = selected;
    }

    private DesignComponentFormItemFacade getFacade() {
        return ejbFacade;
    }

    public List<DesignComponentFormItem> getItems() {
//        if (items == null) {
//            items = getFacade().findAll();
//        }
        return items;
    }

    
    
    public DesignComponentFormItem getDesignComponentFormItem(java.lang.Long id) {
        return getFacade().find(id);
    }

    public List<DesignComponentFormItem> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<DesignComponentFormItem> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    public lk.gov.health.phsp.facade.DesignComponentFormItemFacade getEjbFacade() {
        return ejbFacade;
    }

    public DesignComponentForm getDesignComponentForm() {
        return designComponentForm;
    }

    public void setDesignComponentForm(DesignComponentForm designComponentForm) {
        this.designComponentForm = designComponentForm;
    }

    public List<DesignComponentFormItem> getDesignComponentFormItems() {
        if (designComponentFormItems == null) {
            fillItemsOfTheForm();
        }
        return designComponentFormItems;
    }

    public void setDesignComponentFormItems(List<DesignComponentFormItem> designComponentFormItems) {
        this.designComponentFormItems = designComponentFormItems;
    }

    public DesignComponentFormItem getAddingItem() {
        if (addingItem == null) {
            createNewAddingItem();
        }
        return addingItem;
    }

    public void setAddingItem(DesignComponentFormItem addingItem) {
        this.addingItem = addingItem;
    }

    public DesignComponentFormItem getRemovingItem() {
        return removingItem;
    }

    public void setRemovingItem(DesignComponentFormItem removingItem) {
        this.removingItem = removingItem;
    }

    public DesignComponentFormItem getMovingItem() {
        return movingItem;
    }

    public void setMovingItem(DesignComponentFormItem movingItem) {
        this.movingItem = movingItem;
    }
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Converters">

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public Long getSearchId() {
        return searchId;
    }

    public void setSearchId(Long searchId) {
        this.searchId = searchId;
    }

    public ItemFacade getItemFacade() {
        return itemFacade;
    }

    @FacesConverter(forClass = DesignComponentFormItem.class)
    public static class DesignComponentFormItemControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            DesignComponentFormItemController controller = (DesignComponentFormItemController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "designComponentFormItemController");
            return controller.getDesignComponentFormItem(getKey(value));
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
            if (object instanceof DesignComponentFormItem) {
                DesignComponentFormItem o = (DesignComponentFormItem) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), DesignComponentFormItem.class.getName()});
                return null;
            }
        }

    }
// </editor-fold>

}
