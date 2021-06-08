package lk.gov.health.phsp.bean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.facade.ItemFacade;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
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
import lk.gov.health.phsp.entity.Relationship;
import lk.gov.health.phsp.enums.ItemType;
import lk.gov.health.phsp.enums.RelationshipType;
import org.primefaces.model.UploadedFile;

@Named
@SessionScoped
public class ItemController implements Serializable {

    @EJB
    private lk.gov.health.phsp.facade.ItemFacade ejbFacade;

    @Inject
    private WebUserController webUserController;
    @Inject
    ApplicationController applicationController;
    @Inject
    private UserTransactionController userTransactionController;
    @Inject
    ItemApplicationController itemApplicationController;
    @Inject
    RelationshipController relationshipController;

    private List<Item> items = null;
    private Item selected;
    private Item selectedParent;
    private Item removingItem;
    private List<Item> titles;
    private List<Item> ethinicities;
    private List<Item> religions;
    private List<Item> sexes;
    private List<Item> marietalStatus;
    private List<Item> educationalStatus;
    private List<Item> citizenships;
    private List<Item> mimeTypes;
    private List<Item> categories;
    private List<Item> procedures;
    private List<Item> vtms;
    private List<Item> atms;
    private List<Item> amps;
    private List<Item> vmps;
    private List<Item> units;

    private Item vtm;
    private Item atm;
    private Item vmp;
    private Item amp;
    private Item unit;

    private UploadedFile file;

    private int itemTypeColumnNumber;
    private int itemNameColumnNumber;
    private int itemCodeColumnNumber;
    private int parentCodeColumnNumber;
    private int startRow = 1;

    public ItemController() {
    }

    // <editor-fold defaultstate="collapsed" desc="Navigation">
    public String toManageVtms() {
        vtms = itemApplicationController.findVtms();
        return "/item/vtms";
    }

    public String toManageVmps() {
        vmps = itemApplicationController.findVmps();
        return "/item/vmps";
    }

    public String toManageAmps() {
        amps = itemApplicationController.findAmps();
        return "/item/amps";
    }

    public String toManageUnits() {
        units = itemApplicationController.findUnits();
        return "/item/units";
    }

    public String toManageDictionary() {
        items = itemApplicationController.getDictionaryItemsAndCategories();
        return "/item/List";
    }

    public String toEditVtm() {
        if (vtm == null) {
            JsfUtil.addErrorMessage("Nothing to Edit");
            return "";
        }
        return "/item/vtm";
    }

    public String toEditVmp() {
        if (vmp == null) {
            JsfUtil.addErrorMessage("Nothing to Edit");
            return "";
        }
        return "/item/vmp";
    }

    public String toEditAmp() {
        if (amp == null) {
            JsfUtil.addErrorMessage("Nothing to Edit");
            return "";
        }
        return "/item/amp";
    }

    public String toEditUnit() {
        if (unit == null) {
            JsfUtil.addErrorMessage("Nothing to Edit");
            return "";
        }
        return "/item/unit";
    }

    public String toAddVtm() {
        vtm = new Item();
        vtm.setItemType(ItemType.Vtm);
        return "/item/vtm";
    }

    public String toAddVmp() {
        vmp = new Item();
        vmp.setItemType(ItemType.Vmp);
        return "/item/vmp";
    }

    public String toAddAmp() {
        amp = new Item();
        amp.setItemType(ItemType.Amp);
        return "/item/amp";
    }

    public String toAddUnit() {
        unit = new Item();
        return "/item/unit";
    }

    public void saveVtm() {
        save(vtm);
        vtms = null;
        getVtms();
    }

    public void saveVmp() {
        save(vmp);
        vmps = null;
        getVmps();
    }

    public void saveAmp() {
        save(amp);
        amps = null;
        getAmps();
    }

    public void saveUnit() {
        save(unit);
        units = null;
        getUnits();
    }

    // </editor-fold>    
    // <editor-fold defaultstate="collapsed" desc="Functions">
    public String importToExcel() {
        String dosageFormName;
        String ampName;
        String ampLocalCode;
        String ampBarcode;
        String vtmName;
        String strStrength;
        String strengthUnitName;
        String strPackSize;
        String issueUnitName;
        String packUnitName;

        int dosageFormCol = 0;

        int ampCol = 1;
        int ampLocalCol = 2;
        int ampBarcodeCol = 3;

        int vtmCol = 4;

        int strengthOfIssueUnitCol = 5;
        int strengthUnitCol = 6;
        int issueUnitsPerPackCol = 7;

        int issueUnitCol = 8;
        int packUnitCol = 9;

        /**
         * <h:outputLabel value ="0. Dosage Form"></h:outputLabel>
         * <h:outputLabel value ="1. Product " ></h:outputLabel>
         * <h:outputLabel value ="2. Code" ></h:outputLabel>
         * <h:outputLabel value ="3. Bar Code" ></h:outputLabel>
         * <h:outputLabel value ="4. Generic Name" ></h:outputLabel>
         * <h:outputLabel value ="6. Strength" ></h:outputLabel>
         * <h:outputLabel value ="6. Strength Unit" ></h:outputLabel>
         * <h:outputLabel value ="7. Pack Size" ></h:outputLabel>
         * <h:outputLabel value ="8. Issue Unit" ></h:outputLabel>
         * <h:outputLabel value ="9. Pack Unit" ></h:outputLabel>
         * <h:outputLabel value ="10. Manufacturer" ></h:outputLabel>
         * <h:outputLabel value ="11. Importer" ></h:outputLabel>
         */
        Item cat;
        Item ivtm;
        Item ivmp;
        Item iamp;
        Item issueUnit;
        Item strengthUnit;
        Item packUnit;
        double strengthUnitsPerIssueUnit;
        double issueUnitsPerPack;

        File inputWorkbook;
        Workbook w;
        Cell cell;
        InputStream in;
        JsfUtil.addSuccessMessage(file.getFileName());
        try {
            JsfUtil.addSuccessMessage(file.getFileName());
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

            JsfUtil.addSuccessMessage("Excel File Opened");
            w = Workbook.getWorkbook(inputWorkbook);
            Sheet sheet = w.getSheet(0);

            for (int i = startRow; i < sheet.getRows(); i++) {

                Map m = new HashMap();

                //Category
                cell = sheet.getCell(dosageFormCol, i);
                dosageFormName = cell.getContents();
                if (dosageFormName == null || dosageFormName.trim().equals("")) {
                    System.err.println("No Dosage Form Specified in line no " + i);
                    continue;
                }
                String dosageFormCode = CommonController.prepareAsCode(
                        "dosage_form_" + dosageFormName.trim().toLowerCase());
                cat = findItemByCode(dosageFormCode, ItemType.Dosage_Form);
                if (cat == null) {
                    cat = new Item();
                    cat.setItemType(ItemType.Dosage_Form);
                    cat.setName(dosageFormName);
                    cat.setCode(dosageFormCode);
                    save(cat);
                }

                //Strength Unit
                cell = sheet.getCell(strengthUnitCol, i);
                strengthUnitName = cell.getContents();

                if (strengthUnitName == null || strengthUnitName.trim().equals("")) {
                    System.err.println("No Strength Unit in line no " + i);
                    continue;
                }

                String strengthUnitCode = CommonController.prepareAsCode("strength_unit_" + strengthUnitName);
                strengthUnit = findItemByCode(strengthUnitCode, ItemType.Strength_Unit);
                if (strengthUnit == null) {
                    strengthUnit = new Item();
                    strengthUnit.setName(strengthUnitName);
                    strengthUnit.setCode(strengthUnitCode);
                    strengthUnit.setItemType(ItemType.Strength_Unit);
                    save(strengthUnit);
                }
                // //System.out.println("strengthUnit = " + strengthUnit.getName());
                //Pack Unit
                cell = sheet.getCell(packUnitCol, i);
                packUnitName = cell.getContents();

                if (packUnitName == null || packUnitName.trim().equals("")) {
                    System.out.println("No pack unit for line number " + i);
                    continue;
                }
                String packUnitCode = CommonController.prepareAsCode("pack_unit_" + packUnitName);
                packUnit = findItemByCode(packUnitCode, ItemType.Pack_Unit);
                if (packUnit == null) {
                    packUnit = new Item();
                    packUnit.setName(packUnitName);
                    packUnit.setCode(packUnitCode);
                    packUnit.setItemType(ItemType.Pack_Unit);
                    save(packUnit);
                }

                //Issue Unit
                cell = sheet.getCell(issueUnitCol, i);
                issueUnitName = cell.getContents();
                if (issueUnitName == null || issueUnitName.trim().equals("")) {
                    System.out.println("Issue Unit is not found in line no " + i);
                    continue;
                }
                String issueUnitCode = CommonController.prepareAsCode("issue_unit_" + issueUnitName);
                issueUnit = findItemByCode(issueUnitCode, ItemType.Issue_Unit);
                if (issueUnit == null) {
                    issueUnit = new Item();
                    issueUnit.setName(issueUnitName);
                    issueUnit.setCode(issueUnitCode);
                    issueUnit.setItemType(ItemType.Issue_Unit);
                    save(issueUnit);
                    continue;
                }
                //StrengthOfAnMeasurementUnit
                cell = sheet.getCell(strengthOfIssueUnitCol, i);
                strStrength = cell.getContents();
                // //System.out.println("strStrength = " + strStrength);
                if (!strStrength.equals("")) {
                    try {
                        strengthUnitsPerIssueUnit = Double.parseDouble(strStrength);
                    } catch (NumberFormatException e) {
                        strengthUnitsPerIssueUnit = 0.0;
                    }
                } else {
                    strengthUnitsPerIssueUnit = 0.0;
                }

                //Issue Units Per Pack
                cell = sheet.getCell(issueUnitsPerPackCol, i);
                strPackSize = cell.getContents();
                // //System.out.println("strPackSize = " + strPackSize);
                if (!strPackSize.equals("")) {
                    try {
                        issueUnitsPerPack = Double.parseDouble(strPackSize);
                    } catch (NumberFormatException e) {
                        issueUnitsPerPack = 0.0;
                    }
                } else {
                    issueUnitsPerPack = 0.0;
                }

                //Vtm
                cell = sheet.getCell(vtmCol, i);
                vtmName = cell.getContents();
                // //System.out.println("strGenericName = " + strGenericName);
                if (vtmName == null || vtmName.trim().equals("")) {
                    System.out.println("VTM is not given in line no " + i);
                }

                String vtmCode = CommonController.prepareAsCode("vtm_" + vtmName);

                ivtm = findItemByCode(vtmCode, ItemType.Vtm);

                if (ivtm == null) {
                    ivtm = new Item();
                    ivtm.setName(vtmName);
                    ivtm.setCode(vtmCode);
                    ivtm.setItemType(ItemType.Vtm);
                    save(ivtm);
                }

                String strengthUnitsPerIssueUnitString = CommonController.formatDouble(strengthUnitsPerIssueUnit);

                String vmpName = vtmName + " " + strengthUnitsPerIssueUnitString
                        + strengthUnitName + " " + dosageFormName;

                //Vmp
                String vmpCode = CommonController.prepareAsCode("vmp_" + vmpName);
                ivmp = findItemByCode(vmpCode, ItemType.Vmp);

                if (ivmp == null) {
                    ivmp = new Item();
                    ivmp.setName(vmpName);
                    ivmp.setCode(vmpCode);
                    ivmp.setItemType(ItemType.Vmp);
                    ivmp.setParent(cat);
                    save(ivmp);
                }

                //Amp
                cell = sheet.getCell(ampCol, i);
                ampName = cell.getContents();
                if (ampName == null || ampName.trim().equals("")) {
                    System.err.println("Amp is not given in line number " + i);
                }

                String ampCode = CommonController.prepareAsCode("amp_" + ampName);
                iamp = findItemByCode(ampCode, ItemType.Amp);
                if (iamp == null) {
                    iamp = new Item();
                    iamp.setName(ampName);
                    iamp.setCode(ampCode);
                    iamp.setItemType(ItemType.Amp);
                    iamp.setParent(cat);
                    save(iamp);
                }

                cell = sheet.getCell(ampLocalCol, i);
                ampLocalCode = cell.getContents();
                if (ampLocalCode != null && !ampLocalCode.trim().equals("")) {
                    iamp.setLocalCode(ampLocalCode);
                }

                cell = sheet.getCell(ampBarcodeCol, i);
                ampBarcode = cell.getContents();
                if (ampBarcode != null && !ampBarcode.trim().equals("")) {
                    iamp.setBarcode(ampBarcode);
                }

                Relationship vtmsForVmp
                        = relationshipController.findRelationship(ivmp,
                                issueUnit,
                                ivtm,
                                strengthUnitsPerIssueUnit,
                                strengthUnit,
                                RelationshipType.VtmsForVmp);
                if (vtmsForVmp == null) {
                    vtmsForVmp = new Relationship();

                    vtmsForVmp.setItem(ivmp);
                    vtmsForVmp.setItemUnit(issueUnit);

                    vtmsForVmp.setToItem(ivtm);
                    vtmsForVmp.setDblValue(strengthUnitsPerIssueUnit);
                    vtmsForVmp.setToItemUnit(strengthUnit);

                    vtmsForVmp.setRelationshipType(RelationshipType.VtmsForVmp);

                    relationshipController.save(vtmsForVmp);
                }

                Relationship vmpForAmp = relationshipController.findRelationship(iamp, ivmp, RelationshipType.VmpForAmp);
                if (vmpForAmp == null) {
                    vmpForAmp = new Relationship();
                    vmpForAmp.setItem(iamp);
                    vmpForAmp.setToItem(ivmp);
                    vmpForAmp.setRelationshipType(RelationshipType.VmpForAmp);
                    relationshipController.save(vmpForAmp);
                }

                //TODO: AMPP, VMPP, Importer, Manufacturer, Suplier
            }

            JsfUtil.addSuccessMessage("Succesful. All the data in Excel File Impoted to the database");
            return "";
        } catch (IOException ex) {
            JsfUtil.addErrorMessage(ex.getMessage());
            return "";
        } catch (BiffException e) {
            JsfUtil.addErrorMessage(e.getMessage());
            return "";
        }
    }

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
                userTransactionController.recordTransaction("Import Item");
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
        return findChildrenAndGrandchildrenItemList(selectedParent, null, qry);
    }

    public List<Item> completeItemsofParent(Item parent, String qry) {
        return findChildrenAndGrandchildrenItemList(parent, null, qry);
    }

    public List<Item> completeItemsofParentWithFIlter(String qry) {
        FacesContext context = FacesContext.getCurrentInstance();
        String o = (String) UIComponent.getCurrentComponent(context).getAttributes().get("filter");
        Item ti = findItemByCode(o);
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
        userTransactionController.recordTransaction("Generate Display Names");
    }

    public void addInitialMetadata() {
        addTitles();
        addMarietalStatus();
        addEducationalStatus();
        addReligions();
        addEthinicGroups();
        addSexes();
        addCitizenship();
        addClientData();
        addMedicines();
        userTransactionController.recordTransaction("Add Initial Metadata");
    }

    public void addMedicines() {
        String initialData = "Dictionary_Item::Medicine:medicine:0" + System.lineSeparator()
                + "Dictionary_Item:medicine:VTM:vtm:1" + System.lineSeparator()
                + "Dictionary_Item:medicine:ATM:atm:0" + System.lineSeparator()
                + "Dictionary_Item:medicine:VMP:vmp:2" + System.lineSeparator()
                + "Dictionary_Item:medicine:AMP:amp:3" + System.lineSeparator()
                + "Dictionary_Item:medicine:VMPP:vmpp:4" + System.lineSeparator()
                + "Dictionary_Item:medicine:AMPP:ampp:4" + System.lineSeparator()
                + "Dictionary_Item::Dose:medicine_dose:0" + System.lineSeparator()
                + "Dictionary_Item::Dose Unit:medicine_dose_unit:0" + System.lineSeparator()
                + "Dictionary_Item::Duration:medicine_duration:0" + System.lineSeparator()
                + "Dictionary_Item::Duration Unit:medicine_dutaion_unit:0" + System.lineSeparator()
                + "Dictionary_Item::Frequency:medicine_frequency:0" + System.lineSeparator()
                + "Dictionary_Item::Issue Quantity:medicine_issue_quantity :0" + System.lineSeparator()
                + "Dictionary_Item::Issue Unit:medicine_issue_unit :0" + System.lineSeparator()
                + "Dictionary_Item::Instructions:medicine_issue_instruction :0" + System.lineSeparator()
                + "Dictionary_Item::Measurement Unit:measurement_unit:0" + System.lineSeparator()
                + "Dictionary_Item:measurement_unit:Dose Unit:measurement_unit_dose:0" + System.lineSeparator()
                + "Dictionary_Item:measurement_unit:Dose Unit:measurement_unit_frequency:1" + System.lineSeparator()
                + "Dictionary_Item:measurement_unit:Dose Unit:measurement_unit_duration:2" + System.lineSeparator()
                + "Dictionary_Item:measurement_unit:Dose Unit:measurement_unit_issue_quantity:3" + System.lineSeparator()
                + "Dictionary_Item:measurement_unit_dose:mg:measurement_unit_dose_mg:0" + System.lineSeparator()
                + "Dictionary_Item:measurement_unit_dose:ml:measurement_unit_dose_ml:1" + System.lineSeparator()
                + "Dictionary_Item:measurement_unit_dose:microgram:measurement_unit_dose_microgram:2" + System.lineSeparator()
                + "Dictionary_Item:measurement_unit_dose:#:measurement_unit_dose_unit:3" + System.lineSeparator()
                + "Dictionary_Item:measurement_unit_frequency:bid:measurement_unit_frequency_bid:0" + System.lineSeparator()
                + "Dictionary_Item:measurement_unit_frequency:tds:measurement_unit_frequency_tds:1" + System.lineSeparator()
                + "Dictionary_Item:measurement_unit_frequency:qds:measurement_unit_frequency_qds:2" + System.lineSeparator()
                + "Dictionary_Item:measurement_unit_frequency:od:measurement_unit_frequency_od:3" + System.lineSeparator()
                + "Dictionary_Item:measurement_unit_frequency:mane:measurement_unit_frequency_mane:4" + System.lineSeparator()
                + "Dictionary_Item:measurement_unit_frequency:nocte:measurement_unit_frequency_nocte:5" + System.lineSeparator()
                + "Dictionary_Item:measurement_unit_frequency:sos:measurement_unit_frequency_sos:6" + System.lineSeparator()
                + "Dictionary_Item:measurement_unit_frequency:stat:measurement_unit_frequency_stat:7" + System.lineSeparator()
                + "Dictionary_Item:measurement_unit_duration:doses:measurement_unit_duration_doses:1" + System.lineSeparator()
                + "Dictionary_Item:measurement_unit_duration:days:measurement_unit_duration_days:0" + System.lineSeparator()
                + "Dictionary_Item:measurement_unit_duration:weeks:measurement_unit_duration_weekes:2" + System.lineSeparator()
                + "Dictionary_Item:measurement_unit_duration:months:measurement_unit_duration_months:3" + System.lineSeparator()
                + "Dictionary_Item:measurement_unit_issue_quantity:#:measurement_unit_issue_quantity_units:0" + System.lineSeparator()
                + "Dictionary_Item:measurement_unit_issue_quantity:g:measurement_unit_issue_quantity_g:1" + System.lineSeparator()
                + "Dictionary_Item:measurement_unit_issue_quantity:mg:measurement_unit_issue_quantity_mg:2" + System.lineSeparator()
                + "Dictionary_Item:measurement_unit_issue_quantity:ml:measurement_unit_issue_quantity_ml:3" + System.lineSeparator();
        addInitialMetadata(initialData);
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
                + "Dictionary_Item::Occupation:client_occupation:3" + System.lineSeparator()
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

    public void addEducationalStatus() {
        String initialData = "Dictionary_Category::Educational Status:education_levels:0" + System.lineSeparator()
                + "Dictionary_Item:education_levels:No Formal Education:no_formal_education:0" + System.lineSeparator()
                + "Dictionary_Item:education_levels:1st Degree Education (Grade 1 -5 ):education_levels_1st_degree:1" + System.lineSeparator()
                + "Dictionary_Item:education_levels:Secondary Education (Grade 6 - 10):education_levels_secondary_education:2" + System.lineSeparator()
                + "Dictionary_Item:education_levels:Secondary Education (Grade 6 - 10):education_levels_secondary_education:2" + System.lineSeparator();
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
        Map m = new HashMap();
        String j = "select i from Item i "
                + " where i.retired=false "
                + " and i.itemType=:it ";
        if (parent != null) {
            j += " and i.parent=:p ";
            m.put("p", parent);
        }
        j += " and i.name=:name "
                + " and i.code=:code "
                + " order by i.id";

        m.put("it", itemType);

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
        Item item = null;
        if (code == null || code.trim().equals("")) {
            return item;
        }
        code = code.trim();
        for (Item i : itemApplicationController.getItems()) {
            if (i.getCode() != null) {
                if (i.getCode().trim().equalsIgnoreCase(code)) {
                    return i;
                }
            }
        }
        return item;
    }

    public Item findItemByCode(String code, ItemType type) {
        Item item = null;
        if (code == null || code.trim().equals("")) {
            return item;
        }
        code = code.trim();
        for (Item i : itemApplicationController.getItems()) {
            if (i.getCode() != null) {
                if (i.getItemType() != null) {
                    if (i.getCode().trim().equalsIgnoreCase(code) && i.getItemType().equals(type)) {
                        return i;
                    }
                }
            }
        }
        return item;
    }

    // </editor-fold>   
    public void removeItem() {
        if (removingItem == null) {
            JsfUtil.addErrorMessage("Nothing Selected");
            return;
        }
        removingItem.setRetired(true);
        removingItem.setRetiredAt(new Date());
        removingItem.setRetiredBy(webUserController.getLoggedUser());
        save(removingItem);

        try {
            itemApplicationController.getItems().remove(removingItem);
        } catch (Exception e) {
            itemApplicationController.invalidateItems();
        }
        removingItem = null;
        JsfUtil.addErrorMessage("Removed");
    }

    public Item getSelected() {
        return selected;
    }

    public void setSelected(Item selected) {
        this.selected = selected;
    }

    public void saveDictionatyItemsAndCategories() {
        boolean needReload = false;
        if (selected.getId() == null) {
            needReload = true;
        }
        save(selected);
        if (needReload) {
            itemApplicationController.invalidateDictionaryItemsAndCategories();
            items = itemApplicationController.getDictionaryItemsAndCategories();
        }
    }

    public void save() {
        save(selected);
        JsfUtil.addSuccessMessage("Saved");
    }

    public void save(Item i) {
        if (i.getId() == null) {
            i.setCreatedAt(new Date());
            i.setCreatedBy(webUserController.getLoggedUser());
            getFacade().create(i);
            try {
                itemApplicationController.getItems().add(i);
            } catch (Exception e) {
                itemApplicationController.invalidateItems();
            }
        } else {
            i.setEditedAt(new Date());
            i.setEditedBy(webUserController.getLoggedUser());
            getFacade().edit(i);
        }
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

    public List<Item> getItems() {
        if (items == null) {
            items = itemApplicationController.getItems();
        }
        return items;
    }

    public void reloadItems() {
        itemApplicationController.invalidateItems();
        items = null;
        getItems();
    }

    public Item getItem(java.lang.Long id) {
        return getFacade().find(id);
    }

    public List<Item> getTitles() {
        if (titles == null) {
            titles = itemApplicationController.findChildDictionaryItems("title");
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

    public List<Item> findChildren(String parentCode) {
        if (parentCode == null || parentCode.trim().equals("")) {
            return null;
        }
        Map<Long, Item> mits = new HashMap();
        for (Item i : itemApplicationController.getItems()) {
            if (i.getParent() != null
                    && i.getParent().getCode() != null
                    && i.getParent().getCode().equalsIgnoreCase(parentCode)) {
                mits.put(i.getId(), i);
                List<Item> gcs = findChildren(i.getCode());
                for (Item gc : gcs) {
                    mits.put(gc.getId(), gc);
                }
            }
        }
        List<Item> tis = new ArrayList<>(mits.values());
        return tis;
    }

    public List<Item> findChildrenAndGrandchildrenItemList(Item parent, ItemType t, String qry) {

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
        List<Item> tis = getFacade().findByJpql(j, m);
        return tis;
    }

    public List<Item> findItemList(String parentCode, ItemType t, String qry) {
        List<Item> nis = new ArrayList<>();
        for (Item i : itemApplicationController.getItems()) {
            boolean canInclude = true;

            if (parentCode == null || parentCode.trim().equalsIgnoreCase("")) {

            }

            if (canInclude) {
                nis.add(i);
            }
        }

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
//        String j = "select t from Item t where t.retired=false ";
//        Map m = new HashMap();
//
//        if (parent != null) {
//            m.put("p", parent);
//            j += " and t.parent=:p ";
//        }
//        j += " order by t.name";
        if (parent == null || parent.getCode() == null) {
            return new ArrayList<>();
        }
        return itemApplicationController.findChildren(parent.getCode());
    }

    public List<Item> findItemListByCode(String parentCode) {
        return itemApplicationController.findChildren(parentCode);
    }

    public List<Item> completeItemstByCode(String parentCode, String qry) {
        return itemApplicationController.findChildren(parentCode, qry);
    }

    public void setTitles(List<Item> titles) {
        this.titles = titles;
    }

    public List<Item> getEthinicities() {
        if (ethinicities == null) {
//            ethinicities = findItemList("ethnic_group", ItemType.Dictionary_Item);
            ethinicities = itemApplicationController.findChildDictionaryItems("ethnic_group");
        }
        return ethinicities;
    }

    public void setEthinicities(List<Item> ethinicities) {
        this.ethinicities = ethinicities;
    }

    public List<Item> getReligions() {
        if (religions == null) {
//            religions = findItemList("religion", ItemType.Dictionary_Item);
            religions = itemApplicationController.findChildDictionaryItems("religion");
        }
        return religions;
    }

    public void setReligions(List<Item> religions) {
        this.religions = religions;
    }

    public List<Item> getSexes() {
        if (sexes == null) {
//            sexes = findItemList("sex", ItemType.Dictionary_Item);
            sexes = itemApplicationController.findChildDictionaryItems("sex");
        }
        return sexes;
    }

    public void setSexes(List<Item> sexes) {
        this.sexes = sexes;
    }

    public List<Item> getMarietalStatus() {
        if (marietalStatus == null) {
//            marietalStatus = findItemList("marital_status", ItemType.Dictionary_Item);
            marietalStatus = itemApplicationController.findChildDictionaryItems("marital_status");
        }
        return marietalStatus;
    }

    public void setMarietalStatus(List<Item> marietalStatus) {
        this.marietalStatus = marietalStatus;
    }

    public List<Item> getCitizenships() {
        if (citizenships == null) {
//            citizenships = findItemList("citizenship", ItemType.Dictionary_Item);
            citizenships = itemApplicationController.findChildDictionaryItems("citizenship");
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

    public List<Item> getEducationalStatus() {
        if (educationalStatus == null) {
            educationalStatus = findItemList("education_levels", ItemType.Dictionary_Item);
        }
        return educationalStatus;
    }

    public void setEducationalStatus(List<Item> educationalStatus) {
        this.educationalStatus = educationalStatus;
    }

    public List<Item> getProcedures() {
        if (procedures == null) {
            procedures = findChildren("procedure");
        }
        return procedures;
    }

    public List<Item> completeProcedures(String qry) {
        List<Item> tps = new ArrayList<>();
        if (qry == null || qry.trim().equals("")) {
            return tps;
        }
        qry = qry.trim().toLowerCase();
        for (Item p : getProcedures()) {
            boolean canInclude = false;
            if (p.getName() != null && p.getName().toLowerCase().contains(qry)) {
                canInclude = true;
            }
            if (p.getDisplayName() != null && p.getDisplayName().toLowerCase().contains(qry)) {
                canInclude = true;
            }
            if (p.getCode() != null && p.getCode().toLowerCase().contains(qry)) {
                canInclude = true;
            }
            if (canInclude) {
                tps.add(p);
            }
        }
        return tps;
    }

    public List<Item> getVtms() {
        if (vtms == null) {
            vtms = itemApplicationController.findVtms();
        }
        return vtms;
    }

    public void setVtms(List<Item> vtms) {
        this.vtms = vtms;
    }

    public List<Item> getAtms() {
        return atms;
    }

    public void setAtms(List<Item> atms) {
        this.atms = atms;
    }

    public List<Item> getAmps() {
        return amps;
    }

    public void setAmps(List<Item> amps) {
        this.amps = amps;
    }

    public List<Item> getVmps() {
        return vmps;
    }

    public void setVmps(List<Item> vmps) {
        this.vmps = vmps;
    }

    public Item getVtm() {
        return vtm;
    }

    public void setVtm(Item vtm) {
        this.vtm = vtm;
    }

    public Item getAtm() {
        return atm;
    }

    public void setAtm(Item atm) {
        this.atm = atm;
    }

    public Item getVmp() {
        return vmp;
    }

    public void setVmp(Item vmp) {
        this.vmp = vmp;
    }

    public Item getAmp() {
        return amp;
    }

    public void setAmp(Item amp) {
        this.amp = amp;
    }

    public Item getRemovingItem() {
        return removingItem;
    }

    public void setRemovingItem(Item removingItem) {
        this.removingItem = removingItem;
    }

    public List<Item> getUnits() {
        if (units == null) {
            units = itemApplicationController.findUnits();
        }
        return units;
    }

    public void setUnits(List<Item> units) {
        this.units = units;
    }

    public Item getUnit() {
        return unit;
    }

    public void setUnit(Item unit) {
        this.unit = unit;
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
