package lk.gov.health.phsp.bean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.bean.util.JsfUtil.PersistAction;
import lk.gov.health.phsp.facade.ItemFacade;

import java.io.Serializable;
import java.util.Calendar;
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
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import lk.gov.health.phsp.entity.DesignComponentFormSet;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.enums.ItemType;
import org.primefaces.model.UploadedFile;

@Named("itemController")
@SessionScoped
public class ItemController implements Serializable {

    @EJB
    private lk.gov.health.phsp.facade.ItemFacade ejbFacade;

    @Inject
    private WebUserController webUserController;

    private List<Item> items = null;
    private Item selected;
    private Item selectedParent;
    private List<Item> titles;
    private List<Item> ethinicities;
    private List<Item> religions;
    private List<Item> sexes;
    private List<Item> marietalStatus;
    private List<Item> citizenships;
    private List<Item> mimeTypes;
    private List<Item> categories;
    private UploadedFile file;

    private int itemTypeColumnNumber;
    private int itemNameColumnNumber;
    private int itemCodeColumnNumber;
    private int parentCodeColumnNumber;
    private int startRow = 1;

    public ItemController() {
    }

    // <editor-fold defaultstate="collapsed" desc="Navigation">
    // </editor-fold>    
    // <editor-fold defaultstate="collapsed" desc="Functions">
    public void fillDuplicateItemsInAFormSet(DesignComponentFormSet s) {
        String j = "select di.item from DesignComponentFormItem di "
                + "  where di.retired=false "
                + "  and di.parentComponent.parentComponent=:s "
                + "  group by di.item "
                + " having count(*)>1 "
                + "  ";
        Map m = new HashMap();
        m.put("s", s);
        items = getFacade().findByJpql(j, m);
    }

    public void makeAsSelectedParent(Item pi) {
        selectedParent = pi;
    }

    public void makeAsSelectedParentByCode(String strPi) {
        Item pi = findItemByCode(strPi);
        selectedParent = pi;
    }

    public List<String> completeItemCodes(String qry) {
        String j = "select i.code from Item i "
                + " where lower(i.code) like :q "
                + "  and i.retired=false "
                + " order by i.code";
        Map m = new HashMap();
        m.put("q", "%" + qry.trim().toLowerCase() + "%");
        List<String> ss = getFacade().findString(j, m);
        return ss;
    }

    public String importItemsFromExcel() {
        try {
            String strParentCode;
            String strItemName;
            String strItemType;
            String strItemCode;

            Item parent = null;

            File inputWorkbook;
            Workbook w;
            Cell cell;
            InputStream in;

            lk.gov.health.phsp.facade.util.JsfUtil.addSuccessMessage(file.getFileName());

            try {
                lk.gov.health.phsp.facade.util.JsfUtil.addSuccessMessage(file.getFileName());
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

                lk.gov.health.phsp.facade.util.JsfUtil.addSuccessMessage("Excel File Opened");
                w = Workbook.getWorkbook(inputWorkbook);
                Sheet sheet = w.getSheet(0);

                for (int i = startRow; i < sheet.getRows(); i++) {

                    Map m = new HashMap();

                    cell = sheet.getCell(parentCodeColumnNumber, i);
                    strParentCode = cell.getContents();

                    parent = findItemByCode(strParentCode);

                    cell = sheet.getCell(itemNameColumnNumber, i);
                    strItemName = cell.getContents();

                    cell = sheet.getCell(itemCodeColumnNumber, i);
                    strItemCode = cell.getContents();
                    strItemCode = strItemCode.trim().toLowerCase().replaceAll(" ", "_");

                    cell = sheet.getCell(itemTypeColumnNumber, i);
                    strItemType = cell.getContents();

                    ItemType itemType;
                    try {
                        itemType = ItemType.valueOf(strItemType);
                    } catch (Exception e) {
                        continue;
                    }

                    Item item = createItem(itemType, parent, strItemName, strItemCode, i);

                    getFacade().edit(item);

                }

                lk.gov.health.phsp.facade.util.JsfUtil.addSuccessMessage("Succesful. All the data in Excel File Impoted to the database");
                return "";
            } catch (IOException ex) {
                lk.gov.health.phsp.facade.util.JsfUtil.addErrorMessage(ex.getMessage());
                return "";
            } catch (BiffException e) {
                lk.gov.health.phsp.facade.util.JsfUtil.addErrorMessage(e.getMessage());
                return "";
            }
        } catch (Exception e) {
            return "";
        }
    }

    public List<Item> completeDictionaryItems(String qry) {
        return findItemList(null, ItemType.Dictionary_Item, qry);
    }

    public List<Item> completeItems(String qry) {
        return findItemList(null, null, qry);
    }

    public List<Item> completeItemsofParent(String qry) {
        //System.out.println("completeItemsofParent");
        //System.out.println("qry = " + qry);
        return findChildrenAndGrandchildrenItemList(selectedParent, null, qry);
    }

    public List<Item> completeItemsofParentWithFIlter(String qry) {
        FacesContext context = FacesContext.getCurrentInstance();
        String o = (String) UIComponent.getCurrentComponent(context).getAttributes().get("filter");
        System.out.println("o = " + o);
        Item ti = findItemByCode(o);
        System.out.println("ti = " + ti);
        return findChildrenAndGrandchildrenItemList(ti, null, qry);
    }

    public void generateDisplayNames() {
        List<Item> tis = getFacade().findAll();
        for (Item i : tis) {
            if (i.getDisplayName() == null || i.getDisplayName().trim().equals("")) {
                i.setDisplayName(i.getName());
                getFacade().edit(i);
            }
        }

    }

    public void addInitialMetadata() {
        addTitles();
        addMarietalStatus();
        addReligions();
        addEthinicGroups();
        addSexes();
        addCitizenship();
        addClientData();

    }

    public void addClientData() {
//        client_age_at_encounter_as_string
//client_age_at_encounter_in_days
//client_age_at_encounter_in_years

        String initialData = "Dictionary_Item::Name:client_name:0" + System.lineSeparator()
                + "Dictionary_Item::Sex:client_sex:0" + System.lineSeparator()
                + "Dictionary_Item::Religion:client_religion:0" + System.lineSeparator()
                + "Dictionary_Item::Ethnic Group:client_ethnic_group:0" + System.lineSeparator()
                + "Dictionary_Item::Marital Status:client_marital_status:0" + System.lineSeparator()
                + "Dictionary_Item::Title:client_title:0" + System.lineSeparator()
                + "Dictionary_Item::Citizenship:client_citizenship:0" + System.lineSeparator()
                + "Dictionary_Item::PHN Number:client_phn_number:0" + System.lineSeparator()
                + "Dictionary_Item::NIC No.:client_nic_number:1" + System.lineSeparator()
                + "Dictionary_Item::Date of Birth:client_data_of_birth:2" + System.lineSeparator()
                + "Dictionary_Item::Age:client_current_age_as_string:3" + System.lineSeparator()
                + "Dictionary_Item::Age in days:client_current_age_in_days:3" + System.lineSeparator()
                + "Dictionary_Item::Age in years:client_current_age_in_years:3" + System.lineSeparator()
                + "Dictionary_Item::Age at Encounter:client_age_at_encounter_as_string:3" + System.lineSeparator()
                + "Dictionary_Item::Age at Encounter (Days):client_age_at_encounter_in_days:3" + System.lineSeparator()
                + "Dictionary_Item::Age at Encounter (Years):client_age_at_encounter_in_years:3" + System.lineSeparator()
                + "Dictionary_Item::Permanent Age:client_permanent_address:3" + System.lineSeparator()
                + "Dictionary_Item::Current Address:client_current_address:3" + System.lineSeparator()
                + "Dictionary_Item::Mobile Number:client_mobile_number:3" + System.lineSeparator()
                + "Dictionary_Item::Home Number:client_home_number:3" + System.lineSeparator()
                + "Dictionary_Item::Email:client_email:3" + System.lineSeparator()
                + "Dictionary_Item::Guardian Details:guardian_details:3" + System.lineSeparator()
                + "Dictionary_Item::MOH Area:client_current_moh_area:3" + System.lineSeparator()
                + "Dictionary_Item::PHM Area:client_current_phm_area:3" + System.lineSeparator()
                + "Dictionary_Item::PHI Area:client_current_phi_area:3" + System.lineSeparator()
                + "Dictionary_Item::MOH Area:client_permanent_moh_area:3" + System.lineSeparator()
                + "Dictionary_Item::PHM Area:client_permanent_phm_area:3" + System.lineSeparator()
                + "Dictionary_Item::PHI Area:client_permanent_phi_area:3" + System.lineSeparator()
                + "Dictionary_Item::GN Division:client_gn_area:3" + System.lineSeparator()
                + "Dictionary_Item::DS Division:client_ds_division:3" + System.lineSeparator()
                + "Dictionary_Item::Date of Registration:client_date_of_first_phc_registration:3" + System.lineSeparator()
                + "Dictionary_Item::Person to be contact in an Emergency:next_of_kin_name:3" + System.lineSeparator()
                + "Dictionary_Item::Details of Person to contact in Emergency:next_of_kin_contact_details:3" + System.lineSeparator()
                + "Dictionary_Item::Has Drug Allergy:client_drug_allergy_exists:3" + System.lineSeparator()
                + "Dictionary_Item::Is allergic to:client_allergic_to_medicine:3" + System.lineSeparator()
                + "Dictionary_Item::Has Other Allergy:client_food_allergy_exists:3" + System.lineSeparator()
                + "Dictionary_Item::Is allergic to:client_allergic_to:3" + System.lineSeparator()
                + "Dictionary_Item::Client's Default Photo:client_default_photo:3" + System.lineSeparator()
                + "Dictionary_Item::Client's Photo:client_photo:3" + System.lineSeparator()
                + "Dictionary_Item::Client's Registered at:client_registered_at:3" + System.lineSeparator();

        addInitialMetadata(initialData);
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
                + "Dictionary_Item:male_or_female_title:Baby of:baby_of:12" + System.lineSeparator()
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
            item.setCreatedAt(new Date());
            item.setCreatedBy(webUserController.getLoggedUser());
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
            // //System.out.println("m = " + m);
            // //System.out.println("j = " + j);
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

    public List<Item> findChildrenAndGrandchildrenItemList(Item parent) {
        return findChildrenAndGrandchildrenItemList(parent, ItemType.Dictionary_Item, null);
    }

    public List<Item> findChildrenAndGrandchildrenItemList(Item parent, String qry) {
        return findChildrenAndGrandchildrenItemList(parent, ItemType.Dictionary_Item, qry);
    }

    public List<Item> findChildrenAndGrandchildrenItemList(Item parent, ItemType t) {
        return findChildrenAndGrandchildrenItemList(parent, t, null);
    }

    public List<Item> findChildrenAndGrandchildrenItemList(Item parent, ItemType t, String qry) {
        //System.out.println("findChildrenAndGrandchildrenItemList");
        //System.out.println("qry = " + qry);
        //System.out.println("parent = " + parent);
        //System.out.println("t = " + t);
        String j = "select t from Item t where t.retired=false ";
        Map m = new HashMap();

        if (t != null) {
            m.put("t", t);
            j += " and t.itemType=:t  ";
        }
        if (parent != null) {
            m.put("p", parent);
            j += " and (t.parent=:p or t.parent.parent=:p or t.parent.parent.parent=:p)";
        }
        if (qry != null) {
            m.put("n", "%" + qry.trim().toLowerCase() + "%");
            j += " and lower(t.name) like :n ";
        }
        j += " order by t.orderNo";
        //System.out.println("m = " + m);
        //System.out.println("j = " + j);
        List<Item> tis = getFacade().findByJpql(j, m);
        return tis;
    }

    public List<Item> findItemList(String parentCode, ItemType t, String qry) {
        String j = "select t from Item t where t.retired=false ";
        Map m = new HashMap();

        Item parent = findItemByCode(parentCode);
        if (t != null) {
            m.put("t", t);
            j += " and t.itemType=:t  ";
        }
        if (parent != null) {
            m.put("p", parent);
            j += " and t.parent=:p ";
        }
        if (qry != null) {
            m.put("n", "%" + qry.trim().toLowerCase() + "%");
            j += " and (lower(t.name) like :n or lower(t.code) like :n) ";
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

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public int getItemTypeColumnNumber() {
        return itemTypeColumnNumber;
    }

    public void setItemTypeColumnNumber(int itemTypeColumnNumber) {
        this.itemTypeColumnNumber = itemTypeColumnNumber;
    }

    public int getItemNameColumnNumber() {
        return itemNameColumnNumber;
    }

    public void setItemNameColumnNumber(int itemNameColumnNumber) {
        this.itemNameColumnNumber = itemNameColumnNumber;
    }

    public int getItemCodeColumnNumber() {
        return itemCodeColumnNumber;
    }

    public void setItemCodeColumnNumber(int itemCodeColumnNumber) {
        this.itemCodeColumnNumber = itemCodeColumnNumber;
    }

    public int getParentCodeColumnNumber() {
        return parentCodeColumnNumber;
    }

    public void setParentCodeColumnNumber(int parentCodeColumnNumber) {
        this.parentCodeColumnNumber = parentCodeColumnNumber;
    }

    public int getStartRow() {
        return startRow;
    }

    public void setStartRow(int startRow) {
        this.startRow = startRow;
    }

    public Item getSelectedParent() {
        return selectedParent;
    }

    public void setSelectedParent(Item selectedParent) {
        this.selectedParent = selectedParent;
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
            try {
                key = Long.valueOf(value);
            } catch (NumberFormatException e) {
                key = 0l;
            }
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
