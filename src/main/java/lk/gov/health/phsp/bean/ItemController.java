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
    List<Item> titles;
    List<Item> ethinicities;
    List<Item> religions;
    List<Item> sexes;
    List<Item> marietalStatus;
    

    public ItemController() {
    }

    // <editor-fold defaultstate="collapsed" desc="Navigation">
    // </editor-fold>    
    // <editor-fold defaultstate="collapsed" desc="Functions">
    
    public void addInitialMetadata() {
        addTitles();
        addMarietalStatus();
        addReligions();
        addEthinicGroups();
        addSexes();
    }
    
    public void addSexes() {
        String initialData = "Dictionary_Category::Sex:sex:0" + System.lineSeparator()
                + "Dictionary_Item:sex:Male:sex_male:0" + System.lineSeparator()
                + "Dictionary_Item:sex:Female:sex_female:1" + System.lineSeparator()
                + "Dictionary_Item:sex:Other:sex_other:2" + System.lineSeparator()
                + "Dictionary_Item:sex:Unknown:sex_unknown:3" + System.lineSeparator();
        addInitialMetadata(initialData);
    }
    
    public void addEthinicGroups() {
        String initialData = "Dictionary_Category::Ethnic Group:ethnic_group:0" + System.lineSeparator()
                + "Dictionary_Item:ethnic_group:Sinhalese:sinhalese:0" + System.lineSeparator()
                + "Dictionary_Item:ethnic_group:Tamil:tamil:1" + System.lineSeparator()
                + "Dictionary_Item:ethnic_group:Moors:moors:2" + System.lineSeparator()
                + "Dictionary_Item:ethnic_group:Malays:malays:2" + System.lineSeparator()
                + "Dictionary_Item:ethnic_group:Burghers:burghers:3" + System.lineSeparator()
                + "Dictionary_Item:ethnic_group:Other:ethnic_group_other:4" + System.lineSeparator();
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
                + "Dictionary_Category:title:Title used for Males:male_title:0" + System.lineSeparator()
                + "Dictionary_Category:title:Title used for Females:female_title:1" + System.lineSeparator()
                + "Dictionary_Category:title:Title used for Males or Females:male_or_female_title:2" + System.lineSeparator()
                + "Dictionary_Item:male_title:Mr:mr:0" + System.lineSeparator()
                + "Dictionary_Item:female_title:Mrs:mrs:1" + System.lineSeparator()
                + "Dictionary_Item:female_title:Miss:miss:2" + System.lineSeparator()
                + "Dictionary_Item:male_or_female_title:Rev:rev:3" + System.lineSeparator()
                + "Dictionary_Item:female_title:Ms:ms:4" + System.lineSeparator()
                + "Dictionary_Item:male_or_female_title:Dr:dr:5" + System.lineSeparator()
                + "Dictionary_Item:female_title:Dr(Mrs):drmrs:6" + System.lineSeparator()
                + "Dictionary_Item:female_title:Dr(Miss):drmiss:7" + System.lineSeparator()
                + "Dictionary_Item:female_title:Dr(Ms):drms:8" + System.lineSeparator()
                + "Dictionary_Item:male_or_female_title:Rt Rev:rtrev:9" + System.lineSeparator()
                + "Dictionary_Item:male_or_female_title:Baby of:bany_of:10" + System.lineSeparator()
                + "Dictionary_Item:male_or_female_title:Other:title_other:11" + System.lineSeparator();
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
                + "Dictionary_Item:male_or_female_title:Rev:rev:3" + System.lineSeparator()
                + "Dictionary_Item:female_title:Ms:ms:4" + System.lineSeparator()
                + "Dictionary_Item:male_or_female_title:Dr:dr:5" + System.lineSeparator()
                + "Dictionary_Item:female_title:Dr(Mrs):drmrs:6" + System.lineSeparator()
                + "Dictionary_Item:female_title:Dr(Miss):drmiss:7" + System.lineSeparator()
                + "Dictionary_Item:female_title:Dr(Ms):drms:8" + System.lineSeparator()
                + "Dictionary_Item:male_or_female_title:Rt Rev:rtrev:9" + System.lineSeparator()
                + "Dictionary_Item:male_or_female_title:Baby of:bany_of:10" + System.lineSeparator()
                + "Dictionary_Item:male_or_female_title:Other:title_other:11" + System.lineSeparator();
        addInitialMetadata(initialData);
    }
    
    

    public void addInitialMetadata(String str) {
        System.out.println("Adding initial metadata for " + str);
        String[] lines = str.split("\\r?\\n|\\r");
        for (String oneLines : lines) {
            String[] components = oneLines.split("\\:", -1);
            if (components.length == 5) {
                String itemTypeStr = components[0];
                ItemType itemType;
                try {
                    itemType = ItemType.valueOf(itemTypeStr);
                } catch (Exception e) {
                    System.out.println("Wrong Item Type = " + itemTypeStr);
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
                    System.out.println("Wrong Item Type = " + itemTypeStr);
                    continue;
                }
                Item parent = findItemByCode(itemCategory);
                Item item = createItem(itemType, parent, itemName, itemCode, itemOrderNo);
                System.out.println("item Created " + item.getName());
            } else {
                System.out.println("Format mismatch in components = " + components.toString());
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
        String j = "select i from Item i "
                + " where i.retired=false "
                + " and lower(i.code)=:code "
                + " order by i.id";
        Map m = new HashMap();
        m.put("code", code.trim().toLowerCase());
        item = getFacade().findFirstByJpql(j, m);
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
