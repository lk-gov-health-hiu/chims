package lk.gov.health.phsp.bean;

import ca.uhn.fhir.context.FhirContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.bean.util.JsfUtil.PersistAction;
import lk.gov.health.phsp.facade.InstitutionFacade;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
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
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.FhirOperationResult;
import lk.gov.health.phsp.entity.IntegrationEndpoint;
import lk.gov.health.phsp.enums.AreaType;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.facade.AreaFacade;
import lk.gov.health.phsp.pojcs.FhirConverters;
import org.hl7.fhir.r4.model.Bundle;
import org.json.JSONArray;
import org.json.JSONObject;
import org.primefaces.model.file.UploadedFile;

@Named
@SessionScoped
public class InstitutionController implements Serializable {

    @EJB
    private InstitutionFacade ejbFacade;

    @EJB
    private AreaFacade areaFacade;

    @Inject
    private WebUserController webUserController;

    @Inject
    private ApplicationController applicationController;
    @Inject
    InstitutionApplicationController institutionApplicationController;
    @Inject
    private UserTransactionController userTransactionController;
    @Inject
    IntegrationTriggerController integrationTriggerController;
    @Inject
    FhirController fhirController;
    @Inject
    FhirR4Controller fhirR4Controller;

    
    private List<Institution> items = null;

    private List<Institution> selectedItems = null;

    private Institution selected;
    private Institution deleting;
    private List<Institution> myClinics;
    private List<Area> gnAreasOfSelected;
    private Area area;
    private Area removingArea;

    private InstitutionType institutionType;
    private Institution parent;
    private Area province;
    private Area pdhsArea;
    private Area district;
    private Area rdhsArea;

    private String successMessage;
    private String failureMessage;
    private String startMessage;

    private UploadedFile file;
    private List<FhirOperationResult> fhirOperationResults;
    private boolean pushComplete = false;

    IntegrationEndpoint integrationEndpoint;
    String responseMessage;
    

    public String navigateToTestSendingOrganizationToEndPoint() {
        if (selected == null) {
            JsfUtil.addErrorMessage("No Institution is selected");
            return "";
        }
        return "/systemAdmin/integrationEndpoint/test_with_institution";
    }

    public void postOrganizationBundleToMediators() {
        Bundle bundle = fhirController.createTransactionalBundleWithUUID(UUID.randomUUID().toString());
        Bundle.BundleEntryComponent patientEntry = fhirController.createOrganizationEntry(selected);
        fhirController.addEntryToBundle(bundle, patientEntry);
        FhirOperationResult result = fhirR4Controller.createResourcesInFhirServer(bundle, integrationEndpoint);
        responseMessage = "Operation Result: " + (result.isSuccess() ? "Success" : "Failure") + "\nMessage: " + result.getMessage();
        FhirContext ctx = FhirContext.forR4();
        String serializedBundle = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
        responseMessage += "\nBundle: " + serializedBundle;
    }
    
    public void postLocationBundleToMediators() {
        Bundle bundle = fhirController.createTransactionalBundleWithUUID(UUID.randomUUID().toString());
        Bundle.BundleEntryComponent patientEntry = fhirController.createLocationEntry(selected);
        fhirController.addEntryToBundle(bundle, patientEntry);
        FhirOperationResult result = fhirR4Controller.createResourcesInFhirServer(bundle, integrationEndpoint);
        responseMessage = "Operation Result: " + (result.isSuccess() ? "Success" : "Failure") + "\nMessage: " + result.getMessage();
        FhirContext ctx = FhirContext.forR4();
        String serializedBundle = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
        responseMessage += "\nBundle: " + serializedBundle;
    }

    public String pushSelectedOrganizationsToFhirServers() {
        pushComplete = false;
        String jsonPlayLoad = FhirConverters.createOrganizationJsonPayload(selectedItems);
        List<FhirOperationResult> results = integrationTriggerController.postToMediators(jsonPlayLoad);
        fhirOperationResults = results;
        pushComplete = true; // Mark the operation as complete
        return "/institution/push_result?faces-redirect=true"; // Navigate to the push_result page
    }

    public String pushSelectedLocationsToFhirServers() {
        String jsonPlayLoad = FhirConverters.createLocationJsonPayload(selectedItems);
        List<FhirOperationResult> results = integrationTriggerController.postToMediators(jsonPlayLoad);
        fhirOperationResults = results;
        pushComplete = true; // Mark the operation as complete
        return "/institution/push_result?faces-redirect=true"; // Navigate to the push_result page
    }

    public String checkPushComplete() {
        if (pushComplete) {
            return navigateToPushInstitutions();
        }
        return null;
    }

    // Modified by Dr M H B Ariyaratne with assistance from ChatGPT from OpenAI.
    public Institution getInstitutionById(Long id) {
        return getFacade().find(id);
    }

    public Institution findHospital(Institution unit) {
        if (unit == null) {
            return null;
        }
        switch (unit.getInstitutionType()) {
            case Ministry_of_Health:
                return institutionApplicationController.findMinistryOfHealth();
            case Base_Hospital:
            case District_General_Hospital:
            case Divisional_Hospital:
            case National_Hospital:
            case Teaching_Hospital:
            case Primary_Medical_Care_Unit:
                return unit;
            case Clinic:
            case MOH_Office:

            case Other:
            case Partner:

            case Private_Sector_Institute:
            case Provincial_Department_of_Health_Services:
            case Regional_Department_of_Health_Department:
            case Stake_Holder:
            case Unit:
            case Ward:
            default:
                if (unit.getParent() != null) {
                    return findHospital(unit.getParent());
                } else {
                    return null;
                }
        }
    }

    public String toAddInstitution() {
        selected = new Institution();
        userTransactionController.recordTransaction("To Add Institution");
        fillItems();
        return "/institution/institution";
    }

    public String toImportInstitution() {
        selected = new Institution();
        userTransactionController.recordTransaction("To Add Institution");
        return "/institution/import";
    }

    public String toEditInstitution() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Please select");
            return "";
        }
        return "/institution/institution";
    }

    public boolean thisIsAParentInstitution(Institution checkingInstitution) {
        boolean flag = false;
        if (checkingInstitution == null) {
            return false;
        }
        for (Institution i : getItems()) {
            if (i.getParent() != null && i.getParent().equals(checkingInstitution)) {
                flag = true;
                return flag;
            }
        }
        return flag;
    }

    public String deleteInstitution() {
        if (deleting == null) {
            JsfUtil.addErrorMessage("Please select");
            return "";
        }
        if (thisIsAParentInstitution(deleting)) {
            JsfUtil.addErrorMessage("Can't delete. This has child institutions.");
            return "";
        }
        deleting.setRetired(true);
        deleting.setRetiredAt(new Date());
        deleting.setRetirer(webUserController.getLoggedUser());
        getFacade().edit(deleting);
        JsfUtil.addSuccessMessage("Deleted");
        institutionApplicationController.getInstitutions().remove(deleting);
        fillItems();
        return "/institution/list";
    }

    public String toListInstitutions() {
        userTransactionController.recordTransaction("To List Institutions");
        return "/institution/list";
    }

    public String navigateToPushInstitutions() {
        userTransactionController.recordTransaction("To Push Institutions");
        selectedItems = null;
        return "/institution/push";
    }

    public String toSearchInstitutions() {
        return "/institution/search";
    }

    public InstitutionController() {
    }

    public Institution getSelected() {
        return selected;
    }

    public void setSelected(Institution selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private InstitutionFacade getFacade() {
        return ejbFacade;
    }

    public List<Institution> findChildrenPmcis(Institution ins) {
        List<Institution> allIns = institutionApplicationController.getInstitutions();
        List<Institution> cins = new ArrayList<>();
        for (Institution i : allIns) {
            if (i.getParent() == null) {
                continue;
            }
            if (i.getParent().equals(ins) && i.isPmci()) {
                cins.add(i);
            }
        }
        List<Institution> tins = new ArrayList<>();
        tins.addAll(cins);
        if (cins.isEmpty()) {
            return tins;
        } else {
            for (Institution i : cins) {
                tins.addAll(findChildrenPmcis(i));
            }
        }
        return tins;
    }

    public List<Institution> findChildrenInstitutions(Institution ins) {
        List<Institution> allIns = institutionApplicationController.getInstitutions();
        List<Institution> cins = new ArrayList<>();
        for (Institution i : allIns) {
            if (i.getParent() == null) {
                continue;
            }
            if (i.getParent().equals(ins)) {
                cins.add(i);
            }
        }
        List<Institution> tins = new ArrayList<>();
        tins.addAll(cins);
        if (cins.isEmpty()) {
            return tins;
        } else {
            for (Institution i : cins) {
                tins.addAll(findChildrenInstitutions(i));
            }
        }
        return tins;
    }

    public List<Institution> findInstitutions(InstitutionType type) {
        List<Institution> cins = institutionApplicationController.getInstitutions();
        List<Institution> tins = new ArrayList<>();
        for (Institution i : cins) {
            if (i.getInstitutionType() == null) {
                continue;
            }
            if (i.getInstitutionType().equals(type)) {
                tins.add(i);
            }
        }
        return tins;
    }

    public List<Institution> findInstitutions(Area area, InstitutionType type) {
        List<Institution> cins = institutionApplicationController.getInstitutions();
        List<Institution> tins = new ArrayList<>();
        for (Institution i : cins) {
            if (type != null) {
                if (i.getInstitutionType() == null) {
                    continue;
                }
                if (!i.getInstitutionType().equals(type)) {
                    continue;
                }
            }
            if (area.getType() == AreaType.District) {
                if (i.getDistrict() == null) {
                    continue;
                }
                if (i.getDistrict().equals(area)) {
                    tins.add(i);
                }
            } else if (area.getType() == AreaType.Province) {
                if (i.getProvince() == null) {
                    continue;
                }
                if (i.getProvince().equals(area)) {
                    tins.add(i);
                }
            }

        }
        return tins;
    }

    public List<Institution> completeInstitutions(String nameQry) {
        List<InstitutionType> ts = Arrays.asList(InstitutionType.values());
        if (ts == null) {
            ts = new ArrayList<>();
        }
        return fillInstitutions(ts, nameQry, null);
    }

    public List<Institution> completeHlClinics(String nameQry) {
        return fillInstitutions(InstitutionType.Clinic, nameQry, null);
    }

    public List<Institution> completeClinics(String qry) {
        List<InstitutionType> its = new ArrayList<>();
        its.add(InstitutionType.Clinic);
        its.add(InstitutionType.Cardiology_Clinic);
        its.add(InstitutionType.Medical_Clinic);
        its.add(InstitutionType.Other_Clinic);
        its.add(InstitutionType.Surgical_Clinic);
        return fillInstitutions(its, qry, null);
    }

    public List<InstitutionType> hospitalInstitutionTypes() {
        List<InstitutionType> ts = new ArrayList<>();
        InstitutionType[] ta = InstitutionType.values();
        for (InstitutionType t : ta) {
            switch (t) {
                case Base_Hospital:
                case District_General_Hospital:
                case National_Hospital:
                case Primary_Medical_Care_Unit:
                case Private_Sector_Institute:
                case Teaching_Hospital:
                case Divisional_Hospital:
                    ts.add(t);
                    break;
            }
        }
        return ts;
    }

    public List<Institution> completeHospitals(String nameQry) {
        return fillInstitutions(hospitalInstitutionTypes(), nameQry, null);
    }

    public List<Institution> completeRdhs(String nameQry) {
        return fillInstitutions(InstitutionType.Regional_Department_of_Health_Department, nameQry, null);
    }

    public List<Institution> completePdhs(String nameQry) {
        return fillInstitutions(InstitutionType.Provincial_Department_of_Health_Services, nameQry, null);
    }

    public List<Institution> completeProcedureRooms(String nameQry) {
        return fillInstitutions(InstitutionType.Procedure_Room, nameQry, null);
    }

    public Institution findInstitutionByName(String name) {
        if (name == null || name.trim().equals("")) {
            return null;
        }
        Institution ni = null;
        for (Institution i : institutionApplicationController.getInstitutions()) {
            if (i.getName() != null && i.getName().equalsIgnoreCase(name)) {
                if (ni != null) {
                    // //// System.out.println("Duplicate Institution Name : " + name);
                }
                ni = i;
            }
        }
        return ni;
//        String j = "Select i from Institution i where i.retired=:ret ";
//        Map m = new HashMap();
//        if (name != null) {
//            j += " and lower(i.name)=:n ";
//            m.put("n", name.trim().toLowerCase());
//        }
//        m.put("ret", false);
//        return getFacade().findFirstByJpql(j, m);
    }

//    public Institution findInstitutionById(Long id) {
//        String j = "Select i from Institution i where i.retired=:ret ";
//        Map m = new HashMap();
//        if (id != null) {
//            j += " and i.id=:n ";
//            m.put("n", id);
//        }
//        m.put("ret", false);
//        return getFacade().findFirstByJpql(j, m);
//    }
//
//    public List<Institution> completePmcis(String nameQry) {
//        String j = "Select i from Institution i where i.retired=false and i.pmci=true ";
//        Map m = new HashMap();
//        if (nameQry != null) {
//            j += " and lower(i.name) like :n ";
//            m.put("n", "%" + nameQry.trim().toLowerCase() + "%");
//        }
//        j += " order by i.name";
//        return getFacade().findByJpql(j, m);
//    }
    public void fillItems() {
        if (institutionApplicationController.getInstitutions() != null) {
            items = institutionApplicationController.getInstitutions();
            return;
        }
    }

    public void resetAllInstitutions() {
        items = null;
        institutionApplicationController.resetAllInstitutions();
        items = institutionApplicationController.getInstitutions();
    }

    public List<Institution> fillInstitutions(InstitutionType type, String nameQry, Institution parent) {
        List<Institution> resIns = new ArrayList<>();
        if (nameQry == null) {
            return resIns;
        }
        if (nameQry.trim().equals("")) {
            return resIns;
        }
        List<Institution> allIns = institutionApplicationController.getInstitutions();

        for (Institution i : allIns) {
            boolean canInclude = true;
            if (parent != null) {
                if (i.getParent() == null) {
                    canInclude = false;
                } else {
                    if (!i.getParent().equals(parent)) {
                        canInclude = false;
                    }
                }
            }
            if (type != null) {
                if (i.getInstitutionType() == null) {
                    canInclude = false;
                } else {
                    if (!i.getInstitutionType().equals(type)) {
                        canInclude = false;
                    }
                }
            }
            if (i.getName() == null || i.getName().trim().equals("")) {
                canInclude = false;
            } else {
                if (!i.getName().toLowerCase().contains(nameQry.trim().toLowerCase())) {
                    canInclude = false;
                }
            }
            if (canInclude) {
                resIns.add(i);
            }
        }
        return resIns;
    }

    public List<Institution> fillInstitutions(List<InstitutionType> types, String nameQry, Institution parent) {
        List<Institution> resIns = new ArrayList<>();
        if (nameQry == null) {
            return resIns;
        }
        if (nameQry.trim().equals("")) {
            return resIns;
        }
        List<Institution> allIns = institutionApplicationController.getInstitutions();

        for (Institution i : allIns) {
            boolean canInclude = true;
            if (parent != null) {
                if (i.getParent() == null) {
                    canInclude = false;
                } else {
                    if (!i.getParent().equals(parent)) {
                        canInclude = false;
                    }
                }
            }
            boolean typeFound = false;
            for (InstitutionType type : types) {
                if (type != null) {
                    if (i.getInstitutionType() != null && i.getInstitutionType().equals(type)) {
                        typeFound = true;
                    }
                }
            }
            if (!typeFound) {
                canInclude = false;
            }
            if (i.getName() == null || i.getName().trim().equals("")) {
                canInclude = false;
            } else {
                if (!i.getName().toLowerCase().contains(nameQry.trim().toLowerCase())) {
                    canInclude = false;
                }
            }
            if (canInclude) {
                resIns.add(i);
            }
        }
        return resIns;
    }

    public Institution prepareCreate() {
        selected = new Institution();
        initializeEmbeddableKey();
        return selected;
    }

    public String importInstitutions() {
        successMessage = "";
        failureMessage = "";

        String newLine = "<br/>";

        try {

            File inputWorkbook;
            Workbook w;
            Cell cell;
            InputStream in;

            lk.gov.health.phsp.facade.util.JsfUtil.addSuccessMessage(file.getFileName());

            try {
                lk.gov.health.phsp.facade.util.JsfUtil.addSuccessMessage(file.getFileName());
                in = file.getInputStream();
                File f;
                f = new File(Calendar.getInstance().getTimeInMillis() + file.getFileName());
                FileOutputStream out = new FileOutputStream(f);
                Integer read = 0;
                byte[] bytes = new byte[1024];
                while ((read = in.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
                in.close();
                out.flush();
                out.close();

                inputWorkbook = new File(f.getAbsolutePath());

                successMessage += "File Uploaded Successfully." + newLine;

                w = Workbook.getWorkbook(inputWorkbook);
                Sheet sheet = w.getSheet(0);
                int startRow = 1;

                for (Integer i = startRow; i < sheet.getRows(); i++) {

                    Institution newIns = new Institution();
                    Institution newClinic = new Institution();
                    String insName;
                    String poi;

                    cell = sheet.getCell(0, i);
                    insName = cell.getContents();

                    cell = sheet.getCell(1, i);
                    poi = cell.getContents();

                    newIns.setPoiNumber(poi);
                    newIns.setName(institutionType.getLabel() + " " + insName);
                    newIns.setInstitutionType(institutionType);
                    newIns.setCreatedAt(new Date());
                    newIns.setCreater(webUserController.getLoggedUser());
                    newIns.setDistrict(district);
                    newIns.setLastHin(0l);

                    newIns.setParent(parent);
                    newIns.setPdhsArea(pdhsArea);
                    newIns.setProvince(province);
                    newIns.setRdhsArea(rdhsArea);
                    getFacade().create(newIns);

                    newClinic.setName("HLC " + insName);
                    newClinic.setInstitutionType(InstitutionType.Clinic);
                    newClinic.setCreatedAt(new Date());
                    newClinic.setCreater(webUserController.getLoggedUser());
                    newClinic.setDistrict(district);
                    newClinic.setLastHin(0l);
                    newClinic.setPoiInstitution(newIns);
                    newClinic.setParent(newIns);
                    newClinic.setPdhsArea(pdhsArea);
                    newClinic.setProvince(province);
                    newClinic.setRdhsArea(rdhsArea);
                    getFacade().create(newClinic);

                    institutionApplicationController.setInstitutions(null);

                }
                lk.gov.health.phsp.facade.util.JsfUtil.addSuccessMessage("Completed. Please check success and failure messages.");
                return "";
            } catch (IOException | BiffException ex) {
                lk.gov.health.phsp.facade.util.JsfUtil.addErrorMessage(ex.getMessage());
                failureMessage += "Error. " + ex.getMessage() + ". Aborting the process." + newLine;
                return "";
            }
        } catch (IndexOutOfBoundsException e) {
            failureMessage += "Error. " + e.getMessage() + ". Aborting the process." + newLine;
            return "";
        }

    }

    public void saveOrUpdateInstitution() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing to select");
            return;
        }
        saveOrUpdateInstitution(selected);
    }

    public void saveOrUpdateInstitution(Institution saving) {
        if (saving == null) {
            JsfUtil.addErrorMessage("Nothing to select");
            return;
        }
        if (saving.getId() == null) {
            saving.setCreatedAt(new Date());
            saving.setCreater(webUserController.getLoggedUser());
            getFacade().create(saving);

            institutionApplicationController.getInstitutions().add(saving);
            items = null;
            JsfUtil.addSuccessMessage("Saved");
        } else {
            saving.setEditedAt(new Date());
            saving.setEditer(webUserController.getLoggedUser());
            getFacade().edit(saving);
            items = null;
            JsfUtil.addSuccessMessage("Updates");
        }
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/BundleClinical").getString("InstitutionCreated"));
        if (!JsfUtil.isValidationFailed()) {
            institutionApplicationController.getInstitutions().add(selected);
            fillItems();
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/BundleClinical").getString("InstitutionUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/BundleClinical").getString("InstitutionDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<Institution> getItems() {
        if (items == null) {
            fillItems();
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

    public Institution getInstitution(java.lang.Long id) {
        Institution ni = null;
        for (Institution i : institutionApplicationController.getInstitutions()) {
            if (i.getId() != null && i.getId().equals(id)) {
                ni = i;
            }
        }
        return ni;
    }

    public void refreshMyInstitutions() {
        userTransactionController.recordTransaction("refresh My Institutions");
        myClinics = null;
    }

    public List<Institution> getMyClinics() {
        if (myClinics == null) {
            myClinics = new ArrayList<>();
            int count = 0;
            for (Institution i : webUserController.getLoggableInstitutions()) {
                if (i.getInstitutionType().equals(InstitutionType.Clinic)
                        || i.getInstitutionType().equals(InstitutionType.Medical_Clinic)
                        || i.getInstitutionType().equals(InstitutionType.Surgical_Clinic)
                        || i.getInstitutionType().equals(InstitutionType.Other_Clinic)
                        || i.getInstitutionType().equals(InstitutionType.Cardiology_Clinic)
                        || i.getInstitutionType().equals(InstitutionType.Ward_Clinic)) {
                    myClinics.add(i);
                    count++;
                }
                if (count > 50) {
                    return myClinics;
                }
            }
        }
        return myClinics;
    }

    public lk.gov.health.phsp.facade.InstitutionFacade getEjbFacade() {
        return ejbFacade;
    }

    public AreaFacade getAreaFacade() {
        return areaFacade;
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public List<Area> getGnAreasOfSelected() {
        if (gnAreasOfSelected == null) {
            gnAreasOfSelected = new ArrayList<>();
        }
        return gnAreasOfSelected;
    }

    public void setGnAreasOfSelected(List<Area> gnAreasOfSelected) {
        this.gnAreasOfSelected = gnAreasOfSelected;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public Area getRemovingArea() {
        return removingArea;
    }

    public void setRemovingArea(Area removingArea) {
        this.removingArea = removingArea;

    }

    public void setMyClinics(List<Institution> myClinics) {
        this.myClinics = myClinics;
    }

    public Institution getDeleting() {
        return deleting;
    }

    public void setDeleting(Institution deleting) {
        this.deleting = deleting;
    }

    public ApplicationController getApplicationController() {
        return applicationController;
    }

    public void setApplicationController(ApplicationController applicationController) {
        this.applicationController = applicationController;
    }

    public UserTransactionController getUserTransactionController() {
        return userTransactionController;
    }

    public void setUserTransactionController(UserTransactionController userTransactionController) {
        this.userTransactionController = userTransactionController;
    }

    public InstitutionType getInstitutionType() {
        return institutionType;
    }

    public void setInstitutionType(InstitutionType institutionType) {
        this.institutionType = institutionType;
    }

    public Institution getParent() {
        return parent;
    }

    public void setParent(Institution parent) {
        this.parent = parent;
    }

    public Area getProvince() {
        return province;
    }

    public void setProvince(Area province) {
        this.province = province;
    }

    public Area getPdhsArea() {
        return pdhsArea;
    }

    public void setPdhsArea(Area pdhsArea) {
        this.pdhsArea = pdhsArea;
    }

    public Area getDistrict() {
        return district;
    }

    public void setDistrict(Area district) {
        this.district = district;
    }

    public Area getRdhsArea() {
        return rdhsArea;
    }

    public void setRdhsArea(Area rdhsArea) {
        this.rdhsArea = rdhsArea;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public void setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
    }

    public String getStartMessage() {
        return startMessage;
    }

    public void setStartMessage(String startMessage) {
        this.startMessage = startMessage;
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public List<FhirOperationResult> getFhirOperationResults() {
        return fhirOperationResults;
    }

    public void setFhirOperationResults(List<FhirOperationResult> fhirOperationResults) {
        this.fhirOperationResults = fhirOperationResults;
    }

    public boolean isPushComplete() {
        return pushComplete;
    }

    public void setPushComplete(boolean pushComplete) {
        this.pushComplete = pushComplete;
    }

    public List<Institution> getSelectedItems() {
        return selectedItems;
    }

    public void setSelectedItems(List<Institution> selectedItems) {
        this.selectedItems = selectedItems;
    }

    public IntegrationEndpoint getIntegrationEndpoint() {
        return integrationEndpoint;
    }

    public void setIntegrationEndpoint(IntegrationEndpoint integrationEndpoint) {
        this.integrationEndpoint = integrationEndpoint;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    
    
    @FacesConverter(forClass = Institution.class)
    public static class InstitutionControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            InstitutionController controller = (InstitutionController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "institutionController");
            return controller.getInstitution(getKey(value));
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
            if (object instanceof Institution) {
                Institution o = (Institution) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Institution.class.getName()});
                return null;
            }
        }

    }

}
