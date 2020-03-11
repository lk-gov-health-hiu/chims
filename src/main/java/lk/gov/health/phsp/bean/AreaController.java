package lk.gov.health.phsp.bean;

import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.enums.AreaType;
import lk.gov.health.phsp.entity.Coordinate;
import lk.gov.health.phsp.facade.AreaFacade;
import lk.gov.health.phsp.facade.CoordinateFacade;
import lk.gov.health.phsp.facade.util.JsfUtil;
import lk.gov.health.phsp.facade.util.JsfUtil.PersistAction;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
import javax.ejb.EJBException;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.entity.Relationship;
import lk.gov.health.phsp.enums.RelationshipType;
import org.primefaces.model.UploadedFile;
import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Polygon;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@Named
@SessionScoped
public class AreaController implements Serializable {

    @EJB
    private AreaFacade ejbFacade;
    @EJB
    private CoordinateFacade coordinateFacade;
    private List<Area> items = null;
    List<Area> mohAreas = null;
    List<Area> phiAreas = null;
    List<Area> rdhsAreas = null;
    List<Area> pdhsAreas = null;
    private List<Area> gnAreas = null;
    private List<Area> dsAreas = null;
    private List<Area> provinces = null;
    private List<Area> districts = null;
    private Area selected;
    private Area deleting;
    private UploadedFile file;

    @Inject
    private WebUserController webUserController;
    @Inject
    private CommonController commonController;
    @Inject
    private RelationshipController relationshipController;
    @Inject
    private InstitutionController institutionController;

    private MapModel polygonModel;

    private String successMessage;
    private String failureMessage;
    private String startMessage;

    private Integer gnNameColumnNumber;
    private Integer gnCodeColumnNumber;
    private Integer gnUidColumnNumber;
    private Integer institutionColumnNumber;
    private Integer dsdNameColumnNumber;
    private Integer districtNameColumnNumber;
    private Integer provinceNameColumnNumber;
    private Integer totalPopulationColumnNumber;
    private Integer malePopulationColumnNumber;
    private Integer femalePopulationColumnNumber;
    private Integer areaColumnNumber;
    private Integer startRow = 1;
    private Integer year;

    public String listGnAreas() {
        items = getAreas(AreaType.GN, null);
        return "/area/gn_list";
    }

    public String toAddArea() {
        selected = new Area();
        return "/area/area";
    }

    public String toEditAreaForSysAdmin() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Please select an Area to Edit");
            return "";
        }
        return "/area/area";
    }

    public String deleteAreaForSysAdmin() {
        if (deleting == null) {
            JsfUtil.addErrorMessage("Please select an Area to Delete");
            return "";
        }
        deleting.setRetired(true);
        deleting.setRetiredAt(new Date());
        deleting.setRetiredBy(webUserController.getLoggedUser());
        getFacade().edit(deleting);
        items = null;
        deleting = null;
        return toListAreasForSysAdmin();
    }

    public String saveOrUpdateAreaForSystemAdmin() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Please select an Area");
            return "";
        }
        if (selected.getId() == null) {
            selected.setCreatedAt(new Date());
            selected.setCreatedBy(webUserController.getLoggedUser());
            getFacade().create(selected);
            JsfUtil.addSuccessMessage("Saved");
        } else {
            selected.setLastEditBy(webUserController.getLoggedUser());
            selected.setLastEditeAt(new Date());
            getFacade().edit(deleting);
            JsfUtil.addSuccessMessage("Updated");
        }
        items = null;
        selected = null;
        return toListAreasForSysAdmin();
    }

    public String toListAreasForSysAdmin() {
        String j = "select a from Area a where a.retired=:ret order by a.name";
        Map m = new HashMap();
        m.put("ret", false);
        items = getFacade().findByJpql(j, m);
        return "/area/list";
    }

    public String toBrowseAreasForSysAdmin() {

        return "/area/browse";
    }

    public String toSearchAreasForSysAdmin() {

        return "/area/search";
    }

    public String importAreasFromExcel() {
        try {
            String strGnName;
            String strGNCode;
            String strDsName;
            String strDistrictName;
            String strProvinceName;
            String strTotalPopulationNumber;
            String strMalePopulationNumber;
            String strFemalePopulationNumber;
            String strArea;
            Long totalPopulation = null;
            Long malePopulation = null;
            Long femalePopulation = null;
            Double area = null;

            Area province;
            Area district;
            Area dsd;
            Area moh;
            Area phm;
            Area phi;
            Area gn;

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
                Integer read = 0;
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

                for (Integer i = startRow; i < sheet.getRows(); i++) {

                    Map m = new HashMap();

                    cell = sheet.getCell(gnCodeColumnNumber, i);
                    strGNCode = cell.getContents();

                    cell = sheet.getCell(gnNameColumnNumber, i);
                    strGnName = cell.getContents();

                    cell = sheet.getCell(districtNameColumnNumber, i);
                    strDistrictName = cell.getContents();

                    cell = sheet.getCell(provinceNameColumnNumber, i);
                    strProvinceName = cell.getContents();

                    cell = sheet.getCell(dsdNameColumnNumber, i);
                    strDsName = cell.getContents();

                    cell = sheet.getCell(totalPopulationColumnNumber, i);
                    strTotalPopulationNumber = cell.getContents();

                    cell = sheet.getCell(malePopulationColumnNumber, i);
                    strMalePopulationNumber = cell.getContents();

                    cell = sheet.getCell(femalePopulationColumnNumber, i);
                    strFemalePopulationNumber = cell.getContents();

                    cell = sheet.getCell(areaColumnNumber, i);
                    strArea = cell.getContents();

                    province = getAreaByName(strProvinceName, AreaType.Province, true, null);
                    district = getAreaByName(strDistrictName, AreaType.District, true, province);
                    dsd = getAreaByName(strDsName, AreaType.DsArea, true, district);
                    gn = getAreaByCodeAndName(strGNCode, strGnName, AreaType.GN, true, dsd);

                    try {
                        totalPopulation = Long.parseLong(strTotalPopulationNumber);
                        malePopulation = Long.parseLong(strMalePopulationNumber);
                        femalePopulation = Long.parseLong(strFemalePopulationNumber);
                        area = Double.parseDouble(strArea);
                    } catch (Exception e) {
                    }

                    gn.setName(strGnName);
                    gn.setProvince(province);
                    gn.setDistrict(district);
                    gn.setDsd(dsd);
                    gn.setTotalPopulation(totalPopulation);
                    gn.setMalePopulation(malePopulation);
                    gn.setFemalePopulation(femalePopulation);
                    gn.setSurfaceArea(area);
                    getFacade().edit(gn);

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
        } catch (Exception e) {
            return "";
        }
    }

    public String toImportInstitutionDrainingAreas() {
        successMessage = "";
        failureMessage = "";
        startMessage = "";
        startMessage += "This will search Institutions and add Areas as draining areas.<br/>";
        startMessage += "This Area can be searched by the code or UID. If search by areas code, leave UID column blank. If areas need to be search by UID, leave code column blank.<br/>";
        startMessage += "This Institutions are searched by the name. No new Institutions will be created if the name is not found. Names search is case insensitive.<br/>";
        startMessage += "Upload as an xls file. XLSX files are not currently supported. That feature will be added soon.<br/>";
        startMessage += "Column Numbers are Zero Based. For example, Column A is 0. Column B is 1.<br/>";
        startMessage += "Row Numbers are Zero Based. For example, Row 1 is 0. Row 2 is 1.<br/>";
        return "/area/import_draining_gn_areas_for_institutions";
    }
    
    
    public String toImportPopulationData() {
        successMessage = "";
        failureMessage = "";
        startMessage = "";
        startMessage += "This will search area and add population data.";
        startMessage += "This Area can be searched by the code or UID. If search by areas code, leave UID column blank. If areas need to be search by UID, leave code column blank.";
        startMessage += "Upload as an xls file. XLSX files are not currently supported. That feature will be added soon.";
        startMessage += "Column Numbers are Zero Based. For example, Column A is 0. Column B is 1.";
        startMessage += "Row Numbers are Zero Based. For example, Row 1 is 0. Row 2 is 1.";
        return "/area/import_draining_gn_areas_for_institutions";
    }

    public String uploadInstitutionDrainingAreas() {
        successMessage = "";
        failureMessage = "";
//        <br/>
        String newLine = "<br/>";
        try {

            String strGNCode;
            String strGnUid;
            String strIns;
            Long longGnUid = null;

            Area gn = null;
            Institution ins;

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

                for (Integer i = startRow; i < sheet.getRows(); i++) {

                    Map m = new HashMap();

                    cell = sheet.getCell(institutionColumnNumber, i);
                    strIns = cell.getContents();
                    if (strIns == null) {
                        failureMessage += "No Institution given for the line number " + i + "." + newLine;
                        continue;
                    }

                    ins = getInstitutionController().findInstitutionByName(strIns);
                    if (ins == null) {
                        failureMessage += "No Institution found for the name given for the line number " + i + "." + newLine;
                        continue;
                    }

                    if (gnCodeColumnNumber != null && gnUidColumnNumber != null) {
                        cell = sheet.getCell(gnUidColumnNumber, i);
                        strGnUid = cell.getContents();

                        cell = sheet.getCell(gnCodeColumnNumber, i);
                        strGNCode = cell.getContents();

                        if (strGnUid == null && strGNCode == null) {
                            failureMessage += "No Area UID or Area Code given for the line number " + i + "." + newLine;
                            continue;
                        } else if (strGnUid != null && strGNCode != null) {
                            try {
                                longGnUid = Long.parseLong(strGnUid);
                                gn = getAreaByUid(longGnUid, null);
                            } catch (NumberFormatException e) {

                            }
                            if (gn == null) {
                                gn = getAreaByCode(strGNCode, null);
                            }
                        } else if (strGnUid != null) {
                            try {
                                longGnUid = Long.parseLong(strGnUid);
                            } catch (NumberFormatException e) {

                            }
                            gn = getAreaByUid(longGnUid, null);
                        } else if (strGNCode != null) {
                            gn = getAreaByCode(strGNCode, null);
                        }
                    } else if (gnCodeColumnNumber != null) {
                        cell = sheet.getCell(gnCodeColumnNumber, i);
                        strGNCode = cell.getContents();

                        if (strGNCode == null || strGNCode.trim().equals("")) {
                            failureMessage += "No GN Code for the line number " + i + newLine;
                            continue;
                        }
                        gn = getAreaByCode(strGNCode, null);

                    } else if (gnUidColumnNumber != null) {
                        cell = sheet.getCell(gnUidColumnNumber, i);
                        strGnUid = cell.getContents();
                        if (strGnUid == null || strGnUid.trim().equals("")) {
                            failureMessage += "No GN UID for the line number " + i + "." + newLine;
                            continue;
                        }
                        try {
                            longGnUid = Long.parseLong(strGnUid);
                            gn = getAreaByUid(longGnUid, null);
                        } catch (NumberFormatException e) {

                        }
                    } else {
                        failureMessage += "Both Area UID and Area Code for the line number " + i + newLine + " is missing.";
                        continue;
                    }

                    if (gn == null) {
                        failureMessage += "No Matching area for Code or UID for the line number " + i + newLine;
                        continue;
                    }

                    if (gn.getPmci() == null) {
                        successMessage += "Successfully added " + gn.getName() + "(" + gn.getCode() + ") to the " + ins.getName() + " as a draining area." + newLine;
                        gn.setPmci(ins);
                        getFacade().edit(gn);
                    } else {
                        if (gn.getPmci().equals(ins)) {
                            successMessage += "The " + gn.getName() + "(" + gn.getCode() + ") is already have the " + ins.getName() + " as the draining area. No update was necessary." + newLine;
                        } else {
                            successMessage += "The " + gn.getName() + "(" + gn.getCode() + ") is already had " + gn.getPmci().getName() + " as the draining area. It was replaced with " + ins.getName() + " as the new draining area." + newLine;
                            getFacade().edit(gn);
                        }
                    }

                }
                JsfUtil.addSuccessMessage("Completed. Please check success and failure messages.");
                return "";
            } catch (IOException | BiffException ex) {
                JsfUtil.addErrorMessage(ex.getMessage());
                failureMessage += "Error. " + ex.getMessage() + ". Aborting the process." + newLine;
                return "";
            }
        } catch (IndexOutOfBoundsException e) {
            failureMessage += "Error. " + e.getMessage() + ". Aborting the process." + newLine;
            return "";
        }
    }

    public String importUpdateUidFromCodeOfAreasFromExcel() {
        successMessage = "";
        failureMessage = "";
//        <br/>
        String newLine = "<br/>";
        try {

            String strGNCode;
            String strGnUid;
            Long longGnUid;

            Area gn;

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

                for (Integer i = startRow; i < sheet.getRows(); i++) {

                    Map m = new HashMap();

                    cell = sheet.getCell(gnCodeColumnNumber, i);
                    strGNCode = cell.getContents();

                    if (strGNCode == null || strGNCode.trim().equals("")) {
                        failureMessage += "No GN Code for the line number " + i + newLine;
                        continue;
                    }

                    cell = sheet.getCell(gnUidColumnNumber, i);
                    strGnUid = cell.getContents();

                    if (strGnUid == null || strGnUid.trim().equals("")) {
                        failureMessage += "No GN UID for the line number " + i + "." + newLine;
                        continue;
                    }

                    try {
                        longGnUid = Long.parseLong(strGnUid);
                    } catch (NumberFormatException e) {
                        failureMessage += "The GN UID for the line number " + i + " is not a number." + newLine;
                        continue;
                    }

                    gn = getAreaByCode(strGNCode, null);

                    if (gn == null) {
                        failureMessage += "NO Areas could be found with the code for the line number " + i + " is not a number." + newLine;
                        continue;
                    }

                    gn.setAreauid(longGnUid);
                    getFacade().edit(gn);
                    successMessage += "Successfully added the UID for " + gn.getName() + "(" + gn.getCode() + ")." + newLine;

                }
                JsfUtil.addSuccessMessage("Succesful. All the data in Excel File Impoted to the database");
                return "";
            } catch (IOException | BiffException ex) {
                JsfUtil.addErrorMessage(ex.getMessage());
                failureMessage += "Error. " + ex.getMessage() + ". Aborting the process." + newLine;
                return "";
            }
        } catch (IndexOutOfBoundsException e) {
            failureMessage += "Error. " + e.getMessage() + ". Aborting the process." + newLine;
            return "";
        }
    }

    public void updateNationalAndProvincialPopulationFromDistrictPopulations() {
        RelationshipType[] rts = new RelationshipType[]{RelationshipType.Empanelled_Female_Population,
            RelationshipType.Empanelled_Male_Population,
            RelationshipType.Empanelled_Population,
            RelationshipType.Estimated_Midyear_Female_Population,
            RelationshipType.Estimated_Midyear_Male_Population,
            RelationshipType.Estimated_Midyear_Population,
            RelationshipType.Over_35_Female_Population,
            RelationshipType.Over_35_Male_Population,
            RelationshipType.Over_35_Population,};
        for (RelationshipType t : rts) {
            //System.out.prIntegerln("t = " + t);
            Area sl = getNationalArea();
            //System.out.prIntegerln("sl = " + sl);
            Relationship slr = getRelationshipController().findRelationship(sl, t, year, true);
            Long pop = 0l;
            for (Area d : getDistricts()) {
                //System.out.prIntegerln("d = " + d + " " + d.getId());
                Relationship dr = getRelationshipController().findRelationship(d, t, year, false);
                if (dr != null) {
                    if (dr.getLongValue1() != null) {
                        //System.out.prIntegerln("dr.getLongValue1() = " + dr.getLongValue1());
                        pop += dr.getLongValue1();
                    }
                }
            }
            slr.setLongValue1(pop);
            getRelationshipController().save(slr);
            for (Area p : getProvinces()) {
                //System.out.prIntegerln("p = " + p);
                List<Area> pds = getAreas(AreaType.District, p);
                Relationship pr = getRelationshipController().findRelationship(p, t, year, true);
                Long ppop = 0l;
                for (Area d : pds) {
                    //System.out.prIntegerln("d = " + d);
                    Relationship pdr = getRelationshipController().findRelationship(d, t, year, false);
                    if (pdr != null) {
                        if (pdr.getLongValue1() != null) {
                            ppop += pdr.getLongValue1();
                        }
                    }
                }
                pr.setLongValue1(ppop);
                getRelationshipController().save(pr);
            }
        }

    }

    public List<Area> getMohAreas() {
        if (mohAreas == null) {
            mohAreas = getAreas(AreaType.MOH, null);
        }
        return mohAreas;
    }

    public List<Area> getMohAreas(Area district) {
        mohAreas = getAreas(AreaType.MOH, district);

        return mohAreas;
    }

    public List<Area> getMohAreasOfADistrict(Area district) {
        String j;
        Map m = new HashMap();
        j = "select a "
                + " from Area a "
                + " where a.name is not null ";

        if (district != null) {
            j += " and a.district=:pa ";
            m.put("pa", district);
        }
        j += " order by a.name";

        List<Area> areas = getFacade().findByJpql(j, m);
        return areas;
    }

    public List<Area> getMohAreasOfRdhs(Area rdhs) {
        String j;
        Map m = new HashMap();
        j = "select a "
                + " from Area a "
                + " where a.name is not null ";
        if (rdhs != null) {
            j += " and a.rdhsArea=:pa ";
            m.put("pa", rdhs);
        }
        j += " order by a.name";

        List<Area> areas = getFacade().findByJpql(j, m);
        return areas;
    }

    public void setMohAreas(List<Area> mohAreas) {
        this.mohAreas = mohAreas;
    }

    public List<Area> getPhiAreas() {
        if (phiAreas == null) {
            phiAreas = getAreas(AreaType.PHI, null);
        }
        return phiAreas;
    }

    public void setPhiAreas(List<Area> phiAreas) {
        this.phiAreas = phiAreas;
    }

    public List<Area> getRdhsAreas() {
        if (rdhsAreas == null) {
            rdhsAreas = getAreas(AreaType.District, null);
        }
        return rdhsAreas;
    }

    public List<Area> rdhsAreas(Area province) {
        return getAreas(AreaType.District, province);
    }

    public void setRdhsAreas(List<Area> rdhsAreas) {
        this.rdhsAreas = rdhsAreas;
    }

    public List<Area> getPdhsAreas() {
        if (pdhsAreas == null) {
            pdhsAreas = getAreas(AreaType.Province, null);
        }
        return pdhsAreas;
    }

    public void setPdhsAreas(List<Area> pdhsAreas) {
        this.pdhsAreas = pdhsAreas;
    }

    public Area getAreaById(Long id) {
        return getFacade().find(id);
    }

    public List<Area> getGnAreasOfMoh(Area mohArea) {
        String j;
        Map m = new HashMap();
        j = "select a "
                + " from Area a "
                + " where a.name is not null ";
        j += " and a.type=:t";
        m.put("t", AreaType.GN);
        j += " and a.moh=:moh ";
        m.put("moh", mohArea);
        j += " order by a.name";
        List<Area> areas = getFacade().findByJpql(j, m);
        return areas;
    }

    public Area getNationalArea() {
        String j = "select a from Area a "
                + " where "
                + " a.type=:t "
                + " and a.retired=false"
                + " order by a.id desc";
        Map m = new HashMap();
        m.put("t", AreaType.National);
        Area a = getFacade().findFirstByJpql(j, m);
        if (a == null) {
            a = new Area();
            a.setName("Sri Lanka");
            a.setCode("LK");
            a.setType(AreaType.National);
            a.setCreatedAt(new Date());
            a.setCreatedBy(webUserController.getLoggedUser());
            getFacade().create(a);
            List<Area> ps = getAreas(AreaType.Province, null);
            for (Area p : ps) {
                p.setParentArea(a);
                getFacade().edit(p);
            }
        }
        return a;
    }

    public List<Area> getGnAreasOfPhm(Area mohArea) {
        String j;
        Map m = new HashMap();
        j = "select a "
                + " from Area a "
                + " where a.name is not null ";
        j += " and a.type=:t";
        m.put("t", AreaType.PHM);
        j += " and a.moh=:moh ";
        m.put("moh", mohArea);
        j += " order by a.name";
        List<Area> areas = getFacade().findByJpql(j, m);
        return areas;
    }

    public List<Area> getDistrictsOfAProvince(Area province) {
        String j;
        Map m = new HashMap();
        j = "select a "
                + " from Area a "
                + " where a.name is not null ";
        j += " and a.type=:t";
        m.put("t", AreaType.District);
        j += " and a.parentArea=:p ";
        m.put("p", province);
        j += " order by a.name";
        List<Area> areas = getFacade().findByJpql(j, m);
        return areas;
    }

    public List<Area> getPhmAreasOfMoh(Area mohArea) {
        String j;
        Map m = new HashMap();
        j = "select a "
                + " from Area a "
                + " where a.name is not null ";
        j += " and a.type=:t";
        m.put("t", AreaType.PHM);
        j += " and a.moh=:moh ";
        m.put("moh", mohArea);
        j += " order by a.name";
        List<Area> areas = getFacade().findByJpql(j, m);
        return areas;
    }

    public String drawArea() {
        polygonModel = new DefaultMapModel();

        //Polygon
        Polygon polygon = new Polygon();

        String j = "select c from Coordinate c where c.area=:a";
        Map m = new HashMap();
        m.put("a", selected);
        List<Coordinate> cs = coordinateFacade.findByJpql(j, m);
        for (Coordinate c : cs) {
            LatLng coord = new LatLng(c.getLatitude(), c.getLongitude());
            polygon.getPaths().add(coord);
        }

        polygon.setStrokeColor("#FF9900");
        polygon.setFillColor("#FF9900");
        polygon.setStrokeOpacity(0.7);
        polygon.setFillOpacity(0.7);

        polygonModel.addOverlay(polygon);

        return "/area/area_map";
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public String saveMohCoordinates() {
        if (file == null || "".equals(file.getFileName())) {
            return "";
        }
        if (file == null) {
            JsfUtil.addErrorMessage("Please select an KML File");
            return "";
        }

        Area province;
        Area district;
        Area moh;

        String text = "";
        String provinceName = "";
        String districtName = "";
        String mohAreaName = "";
        String centreLon = "";
        String centreLat = "";
        String centreLongLat = "";
        String coordinatesText = "";

        InputStream in;
        JsfUtil.addSuccessMessage(file.getFileName() + " file uploaded.");
        try {
            JsfUtil.addSuccessMessage(file.getFileName());
            in = file.getInputstream();
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

            File fXmlFile = new File(f.getAbsolutePath());
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("Placemark");

            for (Integer gnCount = 0; gnCount < nList.getLength(); gnCount++) {
                Node gnNode = nList.item(gnCount);
                NodeList gnNodes = gnNode.getChildNodes();
                for (Integer gnElemantCount = 0; gnElemantCount < gnNodes.getLength(); gnElemantCount++) {

                    Node gnDataNode = gnNodes.item(gnElemantCount);

                    if (gnElemantCount == 4) {
                        NodeList gnEdNodes = gnDataNode.getChildNodes();
                        for (Integer gnEdCount = 0; gnEdCount < gnEdNodes.getLength(); gnEdCount++) {
                            Node gnEdNode = gnEdNodes.item(gnEdCount);
                            if (gnEdNode.hasChildNodes()) {
                                if (gnEdNode.getFirstChild().getTextContent().equals("PROVINCE_N")) {
                                    provinceName = gnEdNode.getLastChild().getTextContent();
                                }
                                if (gnEdNode.getFirstChild().getTextContent().equals("DISTRICT_N")) {
                                    districtName = gnEdNode.getLastChild().getTextContent();
                                }
                                if (gnEdNode.getFirstChild().getTextContent().equals("MOH_N")) {
                                    mohAreaName = gnEdNode.getLastChild().getTextContent();
                                }
                            }
                        }
                    }

                    if (gnElemantCount == 6) {

                        NodeList gnEdNodes = gnDataNode.getChildNodes();
                        for (Integer gnEdCount = 0; gnEdCount < gnEdNodes.getLength(); gnEdCount++) {
                            Node gnEdNode = gnEdNodes.item(gnEdCount);

                            if (gnEdCount == 2) {
                                coordinatesText = gnEdNode.getTextContent().trim();
//                                // //System.out.prIntegerln("coordinatesText = " + coordinatesText);
                            }

                            if (gnEdNode.hasChildNodes()) {

                                centreLongLat = gnEdNode.getFirstChild().getTextContent();

                                if (centreLongLat.contains(",")) {
                                    String[] ll = centreLongLat.split(",");
                                    centreLat = ll[1].trim();
                                    centreLon = ll[0].trim();
                                }

                                if (gnEdNode.getFirstChild().getTextContent().equals("PROVINCE_N")) {
                                    provinceName = gnEdNode.getLastChild().getTextContent();
                                }
                                if (gnEdNode.getFirstChild().getTextContent().equals("DISTRICT_N")) {
                                    districtName = gnEdNode.getLastChild().getTextContent();
                                }

                                if (gnEdNode.getFirstChild().getTextContent().equals("MOH_N")) {
                                    mohAreaName = gnEdNode.getLastChild().getTextContent();
                                }
                            }
                        }
                    }

                    if (gnElemantCount == 8) {
                    }

                }

                province = getAreaByName(provinceName, AreaType.Province, false, null);
                if (province == null) {
                    JsfUtil.addErrorMessage("Add " + provinceName);
                    return "";
                }

                district = getAreaByName(districtName, AreaType.District, false, null);
                if (district == null) {
                    JsfUtil.addErrorMessage("Add " + districtName);
                    return "";
                }

                moh = getAreaByName(mohAreaName, AreaType.MOH, false, null);
                if (moh == null) {
                    moh = new Area();
                    moh.setType(AreaType.MOH);
                    moh.setCentreLatitude(Double.parseDouble(centreLat));
                    moh.setCentreLongitude(Double.parseDouble(centreLon));
                    moh.setZoomLavel(12);
                    moh.setName(mohAreaName);
                    moh.setParentArea(district);
                    getFacade().create(moh);
                    coordinatesText = coordinatesText.replaceAll("[\\t\\n\\r]", " ");
                    addCoordinates(moh, coordinatesText);
                } else {
                    JsfUtil.addErrorMessage("MOH Exists");
                }
            }
        } catch (IOException ex) {
            JsfUtil.addErrorMessage(ex.getMessage());
            return "";
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(AreaController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(AreaController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
        }
        return "";
    }

    public String saveGnCoordinates() {
        if (file == null || "".equals(file.getFileName())) {
            return "";
        }
        if (file == null) {
            JsfUtil.addErrorMessage("Please select an KML File");
            return "";
        }

        Area province;
        Area district;
        Area moh;
        Area gn;

        String text = "";
        String provinceName = "";
        String districtName = "";
        String mohAreaName = "";
        String gnAreaName = "";
        String gnAreaCode = "";
        String centreLon = "";
        String centreLat = "";
        String centreLongLat = "";
        String coordinatesText = "";

        InputStream in;
        JsfUtil.addSuccessMessage(file.getFileName() + " file uploaded.");
        try {
            JsfUtil.addSuccessMessage(file.getFileName());
            in = file.getInputstream();
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

            File fXmlFile = new File(f.getAbsolutePath());
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("Placemark");

            for (Integer gnCount = 0; gnCount < nList.getLength(); gnCount++) {
//            for (Integer gnCount = 0; gnCount < 3; gnCount++) {
                Node gnNode = nList.item(gnCount);
                NodeList gnNodes = gnNode.getChildNodes();
                for (Integer gnElemantCount = 0; gnElemantCount < gnNodes.getLength(); gnElemantCount++) {

                    Node gnDataNode = gnNodes.item(gnElemantCount);

                    if (gnElemantCount == 4) {
                        NodeList gnEdNodes = gnDataNode.getChildNodes();
                        for (Integer gnEdCount = 0; gnEdCount < gnEdNodes.getLength(); gnEdCount++) {
                            Node gnEdNode = gnEdNodes.item(gnEdCount);
                            if (gnEdNode.hasChildNodes()) {
                                if (gnEdNode.getFirstChild().getTextContent().equals("PROVINCE_N")) {
                                    provinceName = gnEdNode.getLastChild().getTextContent();
                                }
                                if (gnEdNode.getFirstChild().getTextContent().equals("DISTRICT_N")) {
                                    districtName = gnEdNode.getLastChild().getTextContent();
                                }
                                if (gnEdNode.getFirstChild().getTextContent().equals("MOH_N")) {
                                    mohAreaName = gnEdNode.getLastChild().getTextContent();
                                }
                                if (gnEdNode.getFirstChild().getTextContent().equals("GND_NO")) {
                                    gnAreaCode = gnEdNode.getLastChild().getTextContent();
                                }
                                if (gnEdNode.getFirstChild().getTextContent().equals("GND_N")) {
                                    gnAreaName = gnEdNode.getLastChild().getTextContent();
                                }

                            }
                        }
                    }

                    if (gnElemantCount == 6) {

                        NodeList gnEdNodes = gnDataNode.getChildNodes();
                        for (Integer gnEdCount = 0; gnEdCount < gnEdNodes.getLength(); gnEdCount++) {
                            Node gnEdNode = gnEdNodes.item(gnEdCount);

                            if (gnEdCount == 2) {
                                coordinatesText = gnEdNode.getTextContent().trim();
//                                // //System.out.prIntegerln("coordinatesText = " + coordinatesText);
                            }

                            if (gnEdNode.hasChildNodes()) {

                                centreLongLat = gnEdNode.getFirstChild().getTextContent();

                                if (centreLongLat.contains(",")) {
                                    String[] ll = centreLongLat.split(",");
                                    centreLat = ll[1].trim();
                                    centreLon = ll[0].trim();
                                }

                                if (gnEdNode.getFirstChild().getTextContent().equals("PROVINCE_N")) {
                                    provinceName = gnEdNode.getLastChild().getTextContent();
                                }
                                if (gnEdNode.getFirstChild().getTextContent().equals("DISTRICT_N")) {
                                    districtName = gnEdNode.getLastChild().getTextContent();
                                }
                                if (gnEdNode.getFirstChild().getTextContent().equals("MOH_N")) {
                                    mohAreaName = gnEdNode.getLastChild().getTextContent();
                                }
                                if (gnEdNode.getFirstChild().getTextContent().equals("GND_NO")) {
                                    gnAreaCode = gnEdNode.getLastChild().getTextContent();
                                }
                                if (gnEdNode.getFirstChild().getTextContent().equals("GND_N")) {
                                    gnAreaName = gnEdNode.getLastChild().getTextContent();
                                }

                            }
                        }
                    }

                    if (gnElemantCount == 8) {
                    }

                }

                province = getAreaByName(provinceName, AreaType.Province, false, null);
                if (province == null) {
                    JsfUtil.addErrorMessage("Add " + provinceName);
                    return "";
                }

                district = getAreaByName(districtName, AreaType.District, false, null);
                if (district == null) {
                    JsfUtil.addErrorMessage("Add " + districtName);
                    return "";
                }

                moh = getAreaByName(mohAreaName, AreaType.MOH, false, null);
                if (moh == null) {
                    JsfUtil.addErrorMessage("Add " + mohAreaName);
                    return "";
                }

                gn = getAreaByName(gnAreaCode, AreaType.GN, false, null);
                if (gn == null) {
                    // //System.out.prIntegerln("GN = " + gn);
                    gn = new Area();
                    gn.setType(AreaType.GN);
                    gn.setCentreLatitude(Double.parseDouble(centreLat));
                    gn.setCentreLongitude(Double.parseDouble(centreLon));
                    gn.setZoomLavel(16);
                    gn.setName(gnAreaName);
                    gn.setCode(gnAreaCode);
                    gn.setParentArea(moh);
                    getFacade().create(gn);
                    // //System.out.prIntegerln("gn = " + gn);
                    coordinatesText = coordinatesText.replaceAll("[\\t\\n\\r]", " ");
                    addCoordinates(gn, coordinatesText);
                } else {
                    JsfUtil.addErrorMessage("GN Exists");
                }
            }

        } catch (IOException ex) {
            JsfUtil.addErrorMessage(ex.getMessage());
            return "";
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(AreaController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(AreaController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
        }
        return "";
    }

    public void addCoordinates(Area area, String s) {
        String j = "select c from Coordinate c where c.area=:a";
        Map m = new HashMap();
        m.put("a", area);
        List<Coordinate> cs = coordinateFacade.findByJpql(j, m);
        for (Coordinate c : cs) {
            coordinateFacade.remove(c);
        }
        String cvsSplitBy = ",";
        String[] coords = s.split(" ");
        for (String a : coords) {
            String[] country = a.split(cvsSplitBy);
            if (country.length > 1) {
                Coordinate c = new Coordinate();
                c.setArea(area);
                String strLon = country[0].replace("\"", "");
                String strLat = country[1].replace("\"", "");
                double lon = Double.parseDouble(strLon);
                double lat = Double.parseDouble(strLat);
                c.setLongitude(lon);
                c.setLatitude(lat);
                coordinateFacade.create(c);
            }
        }
    }

    public String saveCoordinates() {
        if (selected == null || selected.getId() == null) {
            JsfUtil.addErrorMessage("Please select an Area");
            return "";
        }
        if (file == null || "".equals(file.getFileName())) {
            return "";
        }
        if (file == null) {
            JsfUtil.addErrorMessage("Please select an CSV File");
            return "";
        }

        String j = "select c from Coordinate c where c.area=:a";
        Map m = new HashMap();
        m.put("a", selected);
        List<Coordinate> cs = coordinateFacade.findByJpql(j, m);
        for (Coordinate c : cs) {
            coordinateFacade.remove(c);
        }

        try {
            String line = "";
            String cvsSplitBy = ",";
            BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputstream(), "UTF-8"));

            Integer i = 0;
            while ((line = br.readLine()) != null) {
                String[] country = line.split(cvsSplitBy);

                if (i > 0) {
                    if (country.length > 2) {
                        Coordinate c = new Coordinate();
                        c.setArea(selected);

                        String strLon = country[1].replace("\"", "");
                        String strLat = country[2].replace("\"", "");

                        double lon = Double.parseDouble(strLon);

                        double lat = Double.parseDouble(strLat);

                        c.setLongitude(lon);
                        c.setLatitude(lat);

                        coordinateFacade.create(c);
                    }
                }
                i++;
            }
            return "";
        } catch (IOException e) {
            return "";
        }

    }

    public String saveCentreCoordinates() {
        if (file == null || "".equals(file.getFileName())) {
            return "";
        }
        if (file == null) {
            JsfUtil.addErrorMessage("Please select an CSV File");
            return "";
        }

        try {
            String line = "";
            String cvsSplitBy = ",";
            BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputstream(), "UTF-8"));

            Integer i = 0;
            while ((line = br.readLine()) != null) {
                String[] country = line.split(cvsSplitBy);
                if (i > 0) {
                    if (country.length > 3) {

                        String areName = country[3].replace("\"", "");
                        String j = "select c from Area c where upper(c.name) like :a order by c.id desc";
                        Map m = new HashMap();
                        m.put("a", areName.toUpperCase() + "%");
                        Area a = getFacade().findFirstByJpql(j, m);

                        if (a == null) {
//                            a = new Area();
//                            a.setName(areName);
//                            a.setType(AreaType.MOH);
//                            getFacade().create(a);
                            break;
                        }

                        String strLon = country[1].replace("\"", "");
                        String strLat = country[2].replace("\"", "");

                        double lon = Double.parseDouble(strLon);

                        double lat = Double.parseDouble(strLat);

                        a.setCentreLatitude(lat);
                        a.setCentreLongitude(lon);
                        a.setZoomLavel(12);

                        getFacade().edit(a);
                    }
                }
                i++;
            }
            return "";
        } catch (IOException e) {
            return "";
        }

    }

    public String toAddProvince() {
        selected = new Area();
        selected.setType(AreaType.Province);
        return "/area/add_province";
    }

    public String toAddDistrict() {
        selected = new Area();
        selected.setType(AreaType.District);
        return "/area/add_district";
    }

    public String toAddMhoArea() {
        selected = new Area();
        selected.setType(AreaType.MOH);
        return "/area/add_moh";
    }

    public String toEducationalZones() {
        selected = new Area();
        return "/area/add_educational_zones";
    }

    public String toAddPhiArea() {
        selected = new Area();
        selected.setType(AreaType.PHI);
        return "/area/add_phi";
    }

    public String toAddGnArea() {
        selected = new Area();
        selected.setType(AreaType.GN);
        return "/area/add_gn";
    }

    public String saveNewProvince() {
        selected.setCreatedAt(new Date());
        getFacade().create(selected);
        selected = null;
        items = null;
        JsfUtil.addSuccessMessage("New Province Saved");
        return "/area/index";
    }

    public String saveNewDistrict() {
        selected.setCreatedAt(new Date());
        getFacade().create(selected);
        selected = null;
        items = null;
        JsfUtil.addSuccessMessage("New District Saved");
        return "/area/index";
    }

    public String saveNewMoh() {
        selected.setCreatedAt(new Date());
        getFacade().create(selected);
        selected = null;
        items = null;

        JsfUtil.addSuccessMessage("New MOH Area Saved");
        return "/area/index";
    }

    public String saveNewEducationalZone() {
        selected.setCreatedAt(new Date());
        getFacade().create(selected);
        selected = null;
        items = null;
        JsfUtil.addSuccessMessage("New Educational Zone Saved");
        return "/area/index";
    }

    public String saveNewPhi() {
        selected.setCreatedAt(new Date());
        getFacade().create(selected);
        selected = null;
        items = null;
        JsfUtil.addSuccessMessage("New PHI Area Saved");
        return "/area/index";
    }

    public String saveNewGn() {
        selected.setCreatedAt(new Date());
        getFacade().create(selected);
        selected = null;
        items = null;
        JsfUtil.addSuccessMessage("New GN Area Saved");
        return "/area/index";
    }

    public List<Area> getAreas(AreaType areaType, Area superArea) {
        return getAreas(areaType, superArea, null);
    }

    public List<Area> getAreas(AreaType areaType, Area parentArea, Area grandParentArea) {
        return getAreas(areaType, parentArea, grandParentArea, null);
    }

    public List<Area> getAreas(AreaType areaType, Area parentArea, Area grandParentArea, String qry) {
        String j;
        Map m = new HashMap();
        j = "select a "
                + " from Area a "
                + " where a.name is not null ";
        if (areaType != null) {
            j += " and a.type=:t";
            m.put("t", areaType);
        }
        if (parentArea != null) {
            j += " and a.parentArea=:pa ";
            m.put("pa", parentArea);
        }
        if (grandParentArea != null) {
            j += " and a.parentArea.parentArea=:gpa ";
            m.put("gpa", grandParentArea);
        }
        if (qry != null) {
            j += " and lower(a.name) like :qry ";
            m.put("qry", "%" + qry.toLowerCase() + "%");
        }
        j += " order by a.name";
        // //System.out.prIntegerln("m = " + m);
        List<Area> areas = getFacade().findByJpql(j, m);
        return areas;
    }

//    public List<Area> getAreas(AreaType areaType, Area parentArea, String qry) {
//        String j;
//        Map m = new HashMap();
//        j = "select a "
//                + " from Area a "
//                + " where a.name is not null ";
//        if (areaType != null) {
//            j += " and a.type=:t";
//            m.put("t", areaType);
//        }
//        if (parentArea != null) {
//            j += " and a.parentArea=:pa ";
//            m.put("pa", parentArea);
//        }
//        if (qry != null) {
//            j += " and lower(a.name) like :qry ";
//            m.put("qry", "%" + qry.toLowerCase() + "%");
//        }
//        j += " order by a.name";
//        // //System.out.prIntegerln("m = " + m);
//        List<Area> areas = getFacade().findByJpql(j, m);
//        return areas;
//    }
    public List<Area> completeProvinces(String qry) {
        return getAreas(qry, AreaType.Province);
    }

    public List<Area> completeDistricts(String qry) {
        return getAreas(qry, AreaType.District);
    }

    public List<Area> completeMoh(String qry) {
        return getAreas(qry, AreaType.MOH);
    }

    public List<Area> completePhm(String qry) {
        return getAreas(qry, AreaType.PHM);
    }

    public List<Area> completeGn(String qry) {
        return getAreas(qry, AreaType.GN);
    }

    public List<Area> completeAreas(String qry) {
        return getAreas(qry, null);
    }

    public List<Area> completeDsAreas(String qry) {
        return getAreas(qry, AreaType.DsArea);
    }

    public List<Area> completeGnAreas(String qry) {
        return getAreas(qry, AreaType.GN);
    }

    public List<Area> completePdhsAreas(String qry) {
        return getAreas(qry, AreaType.PdhsArea);
    }

    public List<Area> completeRdhsAreas(String qry) {
        return getAreas(qry, AreaType.RdhsAra);
    }

    public List<Area> completePhiAreas(String qry) {
        return getAreas(qry, AreaType.PHI);
    }

    public List<Area> completeMohAreas(String qry) {
        return getAreas(qry, AreaType.MOH);
    }

    public List<Area> completePhmAreas(String qry) {
        return getAreas(qry, AreaType.PHM);
    }

    public List<Area> getAreas(String qry, AreaType areaType) {
        String j;
        Map m = new HashMap();
        j = "select a "
                + " from Area a "
                + " where upper(a.name) like :n   ";
        m.put("n", "%" + qry.toUpperCase() + "%");
        if (areaType != null) {
            j += " and a.type=:t";
            m.put("t", areaType);
        }
        j += " order by a.code";
        return getFacade().findByJpql(j, m);
    }

    public Area getAreaByCode(String code, AreaType areaType) {
        if (code.trim().equals("")) {
            return null;
        }
        String j;
        Map m = new HashMap();
        j = "select a "
                + " from Area a "
                + " where a.retired=:ret "
                + " and upper(a.code)=:n  ";
        m.put("n", code.toUpperCase());
        m.put("ret", false);
        if (areaType != null) {
            j += " and a.type=:t";
            m.put("t", areaType);
        }
        j += " order by a.id desc";
        Area ta = getFacade().findFirstByJpql(j, m);
        return ta;
    }

    public Area getAreaByUid(Long code, AreaType areaType) {
        if (code == null) {
            return null;
        }
        String j;
        Map m = new HashMap();
        j = "select a "
                + " from Area a "
                + " where a.retired=:ret "
                + " and a.areauid=:n  ";
        m.put("n", code);
        m.put("ret", false);
        if (areaType != null) {
            j += " and a.type=:t";
            m.put("t", areaType);
        }
        j += " order by a.id desc";
        Area ta = getFacade().findFirstByJpql(j, m);
        return ta;
    }

    public Area getAreaByName(String nameOrCode, AreaType areaType, boolean createNew, Area parentArea) {
        if (nameOrCode.trim().equals("")) {
            return null;
        }
        String j;
        Map m = new HashMap();
        j = "select a "
                + " from Area a "
                + " where upper(a.name)=:n  ";
        m.put("n", nameOrCode.toUpperCase());
        if (areaType != null) {
            j += " and a.type=:t";
            m.put("t", areaType);
        }
        j += " order by a.code";
//        // //System.out.prIntegerln("m = " + m);
        Area ta = getFacade().findFirstByJpql(j, m);
        if (ta == null && createNew) {
            ta = new Area();
            ta.setName(nameOrCode);
            ta.setType(areaType);
            ta.setCreatedAt(new Date());
            ta.setCreatedBy(webUserController.getLoggedUser());
            ta.setParentArea(parentArea);
            getFacade().create(ta);
        }
        return ta;
    }

    public Area getAreaByCodeAndName(String code, String name, AreaType areaType, boolean createNew, Area parentArea) {
        try {
            if (code.trim().equals("")) {
                return null;
            }
            String j;
            Map m = new HashMap();
            j = "select a "
                    + " from Area a "
                    + " where upper(a.name) =:n and upper(a.code) =:c ";
            m.put("c", code.toUpperCase());
            m.put("n", name.toUpperCase());
            if (areaType != null) {
                j += " and a.type=:t";
                m.put("t", areaType);
            }
            j += " order by a.code";
//            // //System.out.prIntegerln("m = " + m);
            Area ta = getFacade().findFirstByJpql(j, m);
            if (ta == null && createNew) {
                ta = new Area();
                ta.setCode(code);
                ta.setType(areaType);
                ta.setCreatedAt(new Date());
                ta.setCreatedBy(webUserController.getLoggedUser());
                ta.setParentArea(parentArea);
                getFacade().create(ta);
            }
            return ta;
        } catch (Exception e) {
            // //System.out.prIntegerln("e = " + e);
            // //System.out.prIntegerln("code = " + code);
            return null;
        }
    }

    public AreaController() {
    }

    public Area getSelected() {
        return selected;
    }

    public void setSelected(Area selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private AreaFacade getFacade() {
        return ejbFacade;
    }

    public Area prepareCreate() {
        selected = new Area();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        persist(PersistAction.CREATE, "Created");
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
            provinces = null;
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, "Updated");
    }

    public void destroy() {
        persist(PersistAction.DELETE, "Deleted");
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
            provinces = null;
        }
    }

    public List<Area> getItems() {
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
                    JsfUtil.addErrorMessage(ex, "Error");
                }
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                JsfUtil.addErrorMessage(ex, "Error");
            }
        }
    }

    public List<Area> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<Area> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    public MapModel getPolygonModel() {
        return polygonModel;
    }

    public void onPolygonSelect(OverlaySelectEvent event) {
        JsfUtil.addSuccessMessage("Selected");
    }

    // <editor-fold defaultstate="collapsed" desc="Getters and Setters">
    public List<Area> getProvinces() {
        if (provinces == null) {
            provinces = getAreas(AreaType.Province, null);
        }
        return provinces;
    }

    public void setProvinces(List<Area> provinces) {
        this.provinces = provinces;
    }

    public List<Area> getDsAreas() {
        if (dsAreas == null) {
            dsAreas = getAreas(AreaType.DsArea, null);
        }
        return dsAreas;
    }

    public void setDsAreas(List<Area> dsAreas) {
        this.dsAreas = dsAreas;
    }

    public CoordinateFacade getCoordinateFacade() {
        return coordinateFacade;
    }

    public void setCoordinateFacade(CoordinateFacade coordinateFacade) {
        this.coordinateFacade = coordinateFacade;
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public void setWebUserController(WebUserController webUserController) {
        this.webUserController = webUserController;
    }

    public Integer getGnNameColumnNumber() {
        return gnNameColumnNumber;
    }

    public void setGnNameColumnNumber(Integer gnNameColumnNumber) {
        this.gnNameColumnNumber = gnNameColumnNumber;
    }

    public Integer getGnCodeColumnNumber() {
        return gnCodeColumnNumber;
    }

    public void setGnCodeColumnNumber(Integer gnCodeColumnNumber) {
        this.gnCodeColumnNumber = gnCodeColumnNumber;
    }

    public Integer getDsdNameColumnNumber() {
        return dsdNameColumnNumber;
    }

    public void setDsdNameColumnNumber(Integer dsdNameColumnNumber) {
        this.dsdNameColumnNumber = dsdNameColumnNumber;
    }

    public Integer getDistrictNameColumnNumber() {
        return districtNameColumnNumber;
    }

    public void setDistrictNameColumnNumber(Integer districtNameColumnNumber) {
        this.districtNameColumnNumber = districtNameColumnNumber;
    }

    public Integer getProvinceNameColumnNumber() {
        return provinceNameColumnNumber;
    }

    public void setProvinceNameColumnNumber(Integer provinceNameColumnNumber) {
        this.provinceNameColumnNumber = provinceNameColumnNumber;
    }

    public Integer getTotalPopulationColumnNumber() {
        return totalPopulationColumnNumber;
    }

    public void setTotalPopulationColumnNumber(Integer totalPopulationColumnNumber) {
        this.totalPopulationColumnNumber = totalPopulationColumnNumber;
    }

    public Integer getMalePopulationColumnNumber() {
        return malePopulationColumnNumber;
    }

    public void setMalePopulationColumnNumber(Integer malePopulationColumnNumber) {
        this.malePopulationColumnNumber = malePopulationColumnNumber;
    }

    public Integer getFemalePopulationColumnNumber() {
        return femalePopulationColumnNumber;
    }

    public void setFemalePopulationColumnNumber(Integer femalePopulationColumnNumber) {
        this.femalePopulationColumnNumber = femalePopulationColumnNumber;
    }

    public Integer getAreaColumnNumber() {
        return areaColumnNumber;
    }

    public void setAreaColumnNumber(Integer areaColumnNumber) {
        this.areaColumnNumber = areaColumnNumber;
    }

    public AreaFacade getEjbFacade() {
        return ejbFacade;
    }

    public void setEjbFacade(AreaFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    public Integer getStartRow() {
        return startRow;
    }

    public void setStartRow(Integer startRow) {
        this.startRow = startRow;
    }

    public List<Area> getGnAreas() {
        gnAreas = getAreas(AreaType.GN, null);
        return gnAreas;
    }

    public List<Area> getGnAreas(Area parentArea, AreaType type) {
        gnAreas = getAreas(AreaType.GN, null);
        return gnAreas;
    }

    public void setGnAreas(List<Area> gnAreas) {
        this.gnAreas = gnAreas;
    }

    public List<Area> getDistricts() {
        if (districts == null) {
            districts = getAreas(AreaType.District, null);
        }
        return districts;
    }

    public void setDistricts(List<Area> districts) {
        this.districts = districts;
    }

    public CommonController getCommonController() {
        return commonController;
    }

    public RelationshipController getRelationshipController() {
        return relationshipController;
    }

    public Integer getYear() {
        if (year == 0) {
            year = CommonController.getYear(new Date());
        }
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Area getDeleting() {
        return deleting;
    }

    public void setDeleting(Area deleting) {
        this.deleting = deleting;
    }

    public Integer getGnUidColumnNumber() {
        return gnUidColumnNumber;
    }

    public void setGnUidColumnNumber(Integer gnUidColumnNumber) {
        this.gnUidColumnNumber = gnUidColumnNumber;
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

    public Integer getInstitutionColumnNumber() {
        return institutionColumnNumber;
    }

    public void setInstitutionColumnNumber(Integer institutionColumnNumber) {
        this.institutionColumnNumber = institutionColumnNumber;
    }

    public InstitutionController getInstitutionController() {
        return institutionController;
    }

    public String getStartMessage() {
        return startMessage;
    }

    public void setStartMessage(String startMessage) {
        this.startMessage = startMessage;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Converters">
    @FacesConverter(forClass = Area.class)
    public static class AreaControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            AreaController controller = (AreaController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "areaController");
            return controller.getFacade().find(getKey(value));
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
            if (object instanceof Area) {
                Area o = (Area) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Area.class.getName()});
                return null;
            }
        }

    }

    @FacesConverter(value = "areaConverter")
    public static class AreaConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            AreaController controller = (AreaController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "areaController");
            return controller.getFacade().find(getKey(value));
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
            if (object instanceof Area) {
                Area o = (Area) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Area.class.getName()});
                return null;
            }
        }

    }

    // </editor-fold>
}
