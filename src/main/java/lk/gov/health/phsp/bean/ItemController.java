package lk.gov.health.phsp.bean;

import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.bean.util.JsfUtil.PersistAction;
import lk.gov.health.phsp.facade.ItemFacade;

import java.io.Serializable;
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
import lk.gov.health.phsp.enums.ItemType;

@Named("itemController")
@SessionScoped
public class ItemController implements Serializable {

    @EJB
    private lk.gov.health.phsp.facade.ItemFacade ejbFacade;
    private List<Item> items = null;
    private Item selected;
    private List<Item> titles;
    private List<Item> ethinicities;
    private List<Item> religions;
    private List<Item> sexes;
    private List<Item> marietalStatus;
    private List<Item> citizenships;
    private List<Item> mimeTypes;
    private List<Item> categories;

    public ItemController() {
    }

    // <editor-fold defaultstate="collapsed" desc="Navigation">
    // </editor-fold>    
    // <editor-fold defaultstate="collapsed" desc="Functions">
    public List<Item> completeDictionaryItems(String qry) {
        return findItemList(null, ItemType.Dictionary_Item, qry);
    }

    public List<Item> completeItems(String qry) {
        return findItemList(null, null, qry);
    }

    public void addInitialMetadata() {
        addTitles();
        addMarietalStatus();
        addReligions();
        addEthinicGroups();
        addSexes();
        addCitizenship();

    }

    public void addMimeTypes() {
        String initialData = "Dictionary_Category::MIME type:mime_type:0" + System.lineSeparator()
                + "Dictionary_Item:mime_type:Plain Text:text/plain:2" + System.lineSeparator()
                + "Dictionary_Item:mime_type:CSS:text/css:1" + System.lineSeparator()
                + "Dictionary_Item:mime_type:CSV:text/csv:2" + System.lineSeparator()
                + "Dictionary_Item:mime_type:HTML:text/html:0" + System.lineSeparator()
                + "Dictionary_Item:mime_type:javascript:text/javascript:2" + System.lineSeparator()
                + "Dictionary_Item:mime_type:BMP Image:image/bmp:0" + System.lineSeparator()
                + "Dictionary_Item:mime_type:JPEG:image/jpeg:1" + System.lineSeparator()
                + "Dictionary_Item:mime_type:GIF:image/gif:3" + System.lineSeparator()
                + "Dictionary_Item:mime_type:SVG:image/svg+xml:1" + System.lineSeparator()
                + "Dictionary_Item:mime_type:PNG:image/png:3" + System.lineSeparator()
                + "Dictionary_Item:mime_type:PDF Document:application/pdf:0" + System.lineSeparator();
        addInitialMetadata(initialData);
    }

    public void addSexes() {
        String initialData = "Dictionary_Category::Sex:sex:0" + System.lineSeparator()
                + "Dictionary_Item:sex:Male:sex_male:0" + System.lineSeparator()
                + "Dictionary_Item:sex:Female:sex_female:1" + System.lineSeparator()
                + "Dictionary_Item:sex:Other:sex_other:2" + System.lineSeparator()
                + "Dictionary_Item:sex:Unknown:sex_unknown:3" + System.lineSeparator();
        addInitialMetadata(initialData);
    }

    public void addCitizenship() {
        String initialData = "Dictionary_Category::Citizenship:citizenship:0" + System.lineSeparator()
                + "Dictionary_Item:citizenship:Local:citizenship_local:0" + System.lineSeparator()
                + "Dictionary_Item:citizenship:Foreign:citizenship_foreign:1" + System.lineSeparator()
                + "Dictionary_Item:citizenship:Unknown:citizenship_other:2" + System.lineSeparator();
        addInitialMetadata(initialData);
    }

    public void addEthinicGroups() {
        String initialData = "Dictionary_Category::Ethnic Group:ethnic_group:0" + System.lineSeparator()
                + "Dictionary_Item:ethnic_group:Sinhalese:sinhalese:0" + System.lineSeparator()
                + "Dictionary_Item:ethnic_group:Tamil:tamil:1" + System.lineSeparator()
                + "Dictionary_Item:ethnic_group:Moors:moors:2" + System.lineSeparator()
                + "Dictionary_Item:ethnic_group:Malays:malays:3" + System.lineSeparator()
                + "Dictionary_Item:ethnic_group:Burghers:burghers:4" + System.lineSeparator()
                + "Dictionary_Item:ethnic_group:Other:ethnic_group_other:5" + System.lineSeparator();
        addInitialMetadata(initialData);
    }

    public void addReligions() {
        String initialData = "Dictionary_Category::Religion:religion:0" + System.lineSeparator()
                + "Dictionary_Item:religion:Buddhist:buddhist:0" + System.lineSeparator()
                + "Dictionary_Item:religion:Hindu:hindu:1" + System.lineSeparator()
                + "Dictionary_Item:religion:Muslim:muslim:2" + System.lineSeparator()
                + "Dictionary_Item:religion:Christian:christian:3" + System.lineSeparator()
                + "Dictionary_Item:religion:Other:religion_other:4" + System.lineSeparator();
        addInitialMetadata(initialData);
    }

    public void addMarietalStatus() {
        String initialData = "Dictionary_Category::Marital Status:marital_status:0" + System.lineSeparator()
                + "Dictionary_Item:marital_status:Married:married:0" + System.lineSeparator()
                + "Dictionary_Item:marital_status:Unmarried:unmarried:1" + System.lineSeparator()
                + "Dictionary_Item:marital_status:Widowed:widowed:2" + System.lineSeparator()
                + "Dictionary_Item:marital_status:Divorsed:divorsed:3" + System.lineSeparator()
                + "Dictionary_Item:marital_status:Seperated:seperated:4" + System.lineSeparator()
                + "Dictionary_Item:marital_status:Other:marital_status_other:4" + System.lineSeparator();
        addInitialMetadata(initialData);
    }

    public void addTitles() {
        String initialData = "Dictionary_Category::Title:title:0" + System.lineSeparator()
                + "Dictionary_Category:title:Title used for Males:male_title:0" + System.lineSeparator()
                + "Dictionary_Category:title:Title used for Females:female_title:1" + System.lineSeparator()
                + "Dictionary_Category:title:Title used for Males or Females:male_or_female_title:2" + System.lineSeparator()
                + "Dictionary_Item:male_title:Mr:mr:0" + System.lineSeparator()
                + "Dictionary_Item:female_title:Mrs:mrs:1" + System.lineSeparator()
                + "Dictionary_Item:female_title:Miss:miss:2" + System.lineSeparator()
                + "Dictionary_Item:male_title:Master:master:3" + System.lineSeparator()
                + "Dictionary_Item:male_or_female_title:Baby:baby:4" + System.lineSeparator()
                + "Dictionary_Item:male_or_female_title:Rev:rev:5" + System.lineSeparator()
                + "Dictionary_Item:female_title:Ms:ms:6" + System.lineSeparator()
                + "Dictionary_Item:male_or_female_title:Dr:dr:7" + System.lineSeparator()
                + "Dictionary_Item:female_title:Dr(Mrs):drmrs:8" + System.lineSeparator()
                + "Dictionary_Item:female_title:Dr(Miss):drmiss:9" + System.lineSeparator()
                + "Dictionary_Item:female_title:Dr(Ms):drms:10" + System.lineSeparator()
                + "Dictionary_Item:male_or_female_title:Rt Rev:rtrev:11" + System.lineSeparator()
                + "Dictionary_Item:male_or_female_title:Baby of:bany_of:12" + System.lineSeparator()
                + "Dictionary_Item:male_or_female_title:Other:title_other:13" + System.lineSeparator();
        addInitialMetadata(initialData);
    }

    public void addInitialMetadata(String str) {
        String[] lines = str.split("\\r?\\n|\\r");
        for (String oneLines : lines) {
            String[] components = oneLines.split("\\:", -1);
            if (components.length == 5) {
                String itemTypeStr = components[0];
                ItemType itemType;
                try {
                    itemType = ItemType.valueOf(itemTypeStr);
                } catch (Exception e) {
                    continue;
                }
                String itemCategory = components[1];
                String itemName = components[2];
                String itemCode = components[3];
                String itemOrderNoStr = components[4];
                int itemOrderNo = 0;
                try {
                    itemOrderNo = Integer.parseInt(itemOrderNoStr);
                } catch (Exception e) {
                    continue;
                }
                Item parent = findItemByCode(itemCategory);
                Item item = createItem(itemType, parent, itemName, itemCode, itemOrderNo);
            } else {
            }
        }
    }

    public Item createItem(ItemType itemType, Item parent, String name, String code, int orderNo) {
        Item item;
        String j = "select i from Item i "
                + " where i.retired=false "
                + " and i.itemType=:it "
                + " and i.parent=:p "
                + " and i.name=:name "
                + " and i.code=:code "
                + " order by i.id";
        Map m = new HashMap();
        m.put("it", itemType);
        m.put("p", parent);
        m.put("name", name);
        m.put("code", code);
        item = getFacade().findFirstByJpql(j, m);
        if (item == null) {
            item = new Item();
            item.setItemType(itemType);
            item.setName(name);
            item.setCode(code.trim().toLowerCase());
            item.setParent(parent);
            item.setOrderNo(orderNo);
            getFacade().create(item);
        }
        return item;
    }

    public Item findItemByCode(String code) {
        Item item;
        String j;
        Map m = new HashMap();
        if (code != null) {
            j = "select i from Item i "
                    + " where i.retired=false "
                    + " and lower(i.code)=:code "
                    + " order by i.id";
            m = new HashMap();
            m.put("code", code.trim().toLowerCase());
            item = getFacade().findFirstByJpql(j, m);
        } else {
            item = null;
        }
        return item;
    }

    // </editor-fold>    
    public Item getSelected() {
        return selected;
    }

    public void setSelected(Item selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private ItemFacade getFacade() {
        return ejbFacade;
    }

    public Item prepareCreate() {
        selected = new Item();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/BundleClinical").getString("ItemCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/BundleClinical").getString("ItemUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/BundleClinical").getString("ItemDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<Item> getItems() {
        if (items == null) {
            items = getFacade().findAll();
        }
        return items;
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

    public Item getItem(java.lang.Long id) {
        return getFacade().find(id);
    }

    public List<Item> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<Item> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    public List<Item> getTitles() {
        if (titles == null) {
            String j = "select t from Item t where t.retired=false and t.parent.parent=:p order by t.orderNo";
            Map m = new HashMap();
            m.put("p", findItemByCode("title"));
            titles = getFacade().findByJpql(j, m);
        }
        return titles;
    }

    public List<Item> findItemList(String parentCode, ItemType t) {
        return findItemList(parentCode, t, null);
    }

    public List<Item> findItemList(String parentCode, ItemType t, String qry) {
        String j = "select t from Item t where t.retired=false ";
        Map m = new HashMap();

        Item parent = findItemByCode(parentCode);
        if (parentCode != null) {
            m.put("t", t);
            j += " and t.itemType=:t  ";
        }
        if (parent != null) {
            m.put("p", parent);
            j += " and t.parent=:p ";
        }
        if (qry != null) {
            m.put("n", "%" + qry.trim().toLowerCase() + "%");
            j += " and lower(t.name) like :n ";
        }
        j += " order by t.orderNo";
        return getFacade().findByJpql(j, m);
    }


        public List<Item> findItemList(Item parent) {
        String j = "select t from Item t where t.retired=false ";
        Map m = new HashMap();

        if (parent != null) {
            m.put("p", parent);
            j += " and t.parent=:p ";
        }
        j += " order by t.name";
        return getFacade().findByJpql(j, m);
    }



    public void setTitles(List<Item> titles) {
        this.titles = titles;
    }

    public List<Item> getEthinicities() {
        if (ethinicities == null) {
            ethinicities = findItemList("ethnic_group", ItemType.Dictionary_Item);
        }
        return ethinicities;
    }

    public void setEthinicities(List<Item> ethinicities) {
        this.ethinicities = ethinicities;
    }

    public List<Item> getReligions() {
        if (religions == null) {
            religions = findItemList("religion", ItemType.Dictionary_Item);
        }
        return religions;
    }

    public void setReligions(List<Item> religions) {
        this.religions = religions;
    }

    public List<Item> getSexes() {
        if (sexes == null) {
            sexes = findItemList("sex", ItemType.Dictionary_Item);
        }
        return sexes;
    }

    public void setSexes(List<Item> sexes) {
        this.sexes = sexes;
    }

    public List<Item> getMarietalStatus() {
        if (marietalStatus == null) {
            marietalStatus = findItemList("marital_status", ItemType.Dictionary_Item);
        }
        return marietalStatus;
    }

    public void setMarietalStatus(List<Item> marietalStatus) {
        this.marietalStatus = marietalStatus;
    }

    public List<Item> getCitizenships() {
        if (citizenships == null) {
            citizenships = findItemList("citizenship", ItemType.Dictionary_Item);
        }
        return citizenships;
    }

    public void setCitizenships(List<Item> citizenships) {
        this.citizenships = citizenships;
    }

    public List<Item> getMimeTypes() {
        if (mimeTypes == null) {
            mimeTypes = findItemList("mime_type", ItemType.Dictionary_Item);
        }
        return mimeTypes;
    }

    public void setMimeTypes(List<Item> mimeTypes) {
        this.mimeTypes = mimeTypes;
    }

    public List<Item> getCategories() {
        if (categories == null) {
            categories = findItemList(null, ItemType.Dictionary_Category);
        }
        return categories;
    }

    public void setCategories(List<Item> categories) {
        this.categories = categories;
    }

    public lk.gov.health.phsp.facade.ItemFacade getEjbFacade() {
        return ejbFacade;
    }

    @FacesConverter(forClass = Item.class)
    public static class ItemControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ItemController controller = (ItemController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "itemController");
            return controller.getItem(getKey(value));
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
            if (object instanceof Item) {
                Item o = (Item) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Item.class.getName()});
                return null;
            }
        }

    }

}
