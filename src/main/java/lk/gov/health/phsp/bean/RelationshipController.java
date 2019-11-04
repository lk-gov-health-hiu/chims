package lk.gov.health.phsp.bean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import lk.gov.health.phsp.entity.Relationship;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.bean.util.JsfUtil.PersistAction;
import lk.gov.health.phsp.facade.RelationshipFacade;

import java.io.Serializable;
import java.util.ArrayList;
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
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.enums.AreaType;
import lk.gov.health.phsp.enums.ItemType;
import lk.gov.health.phsp.enums.RelationshipType;
import lk.gov.health.phsp.facade.AreaFacade;
import org.primefaces.model.UploadedFile;

@Named("relationshipController")
@SessionScoped
public class RelationshipController implements Serializable {

    @EJB
    private lk.gov.health.phsp.facade.RelationshipFacade ejbFacade;
    @EJB
    private AreaFacade areaFacade;

    @Inject
    private WebUserController webUserController;
    @Inject
    private AreaController areaController;

    private List<Relationship> items = null;
    private Relationship selected;

    private Area area;
    private int year;
    private int month;

    private Relationship adding;
    private Relationship removing;

    private int districtColumnNumber;
    private int estimatedMidyearPopulationColumnNumber;
    private int targetPopulationColumnNumber;
    private int parentCodeColumnNumber;
    private int startRow = 1;

    private UploadedFile file;
    private String errorCode;

    public void fillAll(){
        items = getFacade().findAll();
    }
    
    public String importDistrictPopulationDataFromExcel() {
        try {
            String strDistrict;
            String strEstimatedMidYearPopulation;
            String strEstimatedTargetPopulation;
            Long midyearPopulation;
            Long targetPopulation;

            Area district = null;

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

                errorCode = "";

                for (int i = startRow; i < sheet.getRows(); i++) {

                    Map m = new HashMap();

                    cell = sheet.getCell(districtColumnNumber, i);
                    strDistrict = cell.getContents();

                    district = areaController.getAreaByName(strDistrict, AreaType.District, false, null);

                    if (district == null) {
                        errorCode += strDistrict + " NOT Found";
                        continue;
                    }

                    cell = sheet.getCell(estimatedMidyearPopulationColumnNumber, i);
                    strEstimatedMidYearPopulation = cell.getContents();
                    midyearPopulation = CommonController.getLongValue(strEstimatedMidYearPopulation);

                    cell = sheet.getCell(targetPopulationColumnNumber, i);
                    strEstimatedTargetPopulation = cell.getContents();
                    targetPopulation = CommonController.getLongValue(strEstimatedTargetPopulation);

                    Relationship myp = findRelationship(district, RelationshipType.Estimated_Midyear_Population, year);
                    if (myp == null) {
                        myp = new Relationship();
                        myp.setArea(district);
                        myp.setRelationshipType(RelationshipType.Estimated_Midyear_Population);
                        myp.setYearInt(year);
                        myp.setCreatedAt(new Date());
                        myp.setCreatedBy(webUserController.getLoggedUser());
                        getFacade().create(myp);
                    } else {
                        myp.setLastEditBy(webUserController.getLoggedUser());
                        myp.setLastEditeAt(new Date());
                    }
                    myp.setLongValue1(midyearPopulation);
                    getFacade().edit(myp);

                    Relationship tp = findRelationship(district, RelationshipType.Over_35_Population, year);
                    if (tp == null) {
                        tp = new Relationship();
                        tp.setArea(district);
                        tp.setRelationshipType(RelationshipType.Over_35_Population);
                        tp.setYearInt(year);
                        tp.setCreatedAt(new Date());
                        tp.setCreatedBy(webUserController.getLoggedUser());
                        getFacade().create(tp);
                    } else {
                        tp.setLastEditBy(webUserController.getLoggedUser());
                        tp.setLastEditeAt(new Date());
                    }
                    tp.setLongValue1(targetPopulation);
                    getFacade().edit(tp);

                    district.setTotalPopulation(midyearPopulation);
                    district.setTotalTargetPopulation(targetPopulation);
                    getAreaFacade().edit(district);

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

    public void addEmpowerementData() {
        if (adding == null) {
            JsfUtil.addErrorMessage("Select");
            return;
        }
        if (adding.getArea() == null) {
            JsfUtil.addErrorMessage("Select GN Area");
            return;
        }
        if (adding.getLongValue1() == null) {
            JsfUtil.addErrorMessage("Please enter the number empanelled");
            return;
        }
        if (adding.getYearInt() == 0) {
            JsfUtil.addErrorMessage("Please enter the nyear");
            return;
        }
        if (findRelationship(adding.getArea(), adding.getRelationshipType(), adding.getYearInt()) != null) {
            JsfUtil.addErrorMessage("Already data added.");
            return;
        }
        if (adding.getRelationshipType() == null) {
            JsfUtil.addErrorMessage("Type ?");
            return;
        }
        save(adding);
        fillRelationshipData();
        adding = null;
        JsfUtil.addSuccessMessage("Updated");
    }

    public void removeRelationship() {
        if (removing == null) {
            JsfUtil.addErrorMessage("Nothing to remove");
            return;
        }
        removing.setRetired(true);
        removing.setRetiredAt(new Date());
        removing.setRetiredBy(webUserController.getLoggedUser());
        getFacade().edit(removing);
        removing = null;
        items = null;
    }

    public void save() {
        save(selected);
        JsfUtil.addSuccessMessage("Saved");
    }

    public void save(Relationship r) {
        if (r == null) {
            JsfUtil.addErrorMessage("Nothing selected");
            return;
        }
        if (r.getId() == null) {
            r.setCreatedAt(new Date());
            r.setCreatedBy(webUserController.getLoggedUser());
            getFacade().edit(r);
        } else {
            r.setLastEditBy(webUserController.getLoggedUser());
            r.setLastEditeAt(new Date());
            getFacade().edit(r);
        }
    }

    public void fillRelationshipData() {
        if (area == null) {
            return;
        }
        String j = "select r from Relationship r "
                + " where (r.area=:a or r.area.parentArea=:a or r.area.parentArea.parentArea=:a or r.area.parentArea.parentArea.parentArea=:a "
                + " or r.area.phm=:a or r.area.phi=:a or r.area.dsd=:a  or r.area.moh=:a  or  r.area.district=:a  or  r.area.province=:a  or r.area.rdhsArea=:a  or r.area.pdhsArea=:a)  "
                + " and r.retired=false "
                + " and r.yearInt=:y";
        j = "select r from Relationship r "
                + " where r.area=:a  "
                + " and r.retired=false "
                + " and r.yearInt=:y";

        Map m = new HashMap();
        m.put("a", area);
        m.put("y", year);
        items = getFacade().findByJpql(j, m);
    }
    
    public Relationship findRelationship(Area a, RelationshipType type, Integer year) {
        return findRelationship(a, type, year, false);
    }

    public Relationship findRelationship(Area relArea, RelationshipType relType, Integer relYear, boolean create) {
        String j = "select r from Relationship r "
                + " where r.area.id=:a "
                + " and r.relationshipType=:t "
                + " and r.retired=:f ";
        Map m = new HashMap();
        m.put("f", false);
        if (relYear!=null && relYear != 0) {
            j += " and r.yearInt=:y";
            m.put("y", relYear);
        }
        m.put("a", relArea.getId());
        m.put("t", relType);
        j += " order by r.id desc";
        //System.out.println("j = " + j);
        //System.out.println("m = " + m);
        
        Relationship r = getFacade().findFirstByJpql(j, m);
        if(r==null && create){
            r = new Relationship();
            r.setArea(relArea);
            r.setRelationshipType(relType);
            r.setYearInt(relYear);
            getFacade().create(r);
        }
        return r;
    }

    public Relationship findRelationship(Area a, RelationshipType type) {
        return findRelationship(a, type, 0);
    }

    public RelationshipController() {
    }

    public Relationship getSelected() {
        return selected;
    }

    public void setSelected(Relationship selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private RelationshipFacade getFacade() {
        return ejbFacade;
    }

    public Relationship prepareCreate() {
        selected = new Relationship();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/BundleClinical").getString("RelationshipCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/BundleClinical").getString("RelationshipUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/BundleClinical").getString("RelationshipDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<Relationship> getItems() {
        if (items == null) {
            items = new ArrayList<>();
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

    public Relationship getRelationship(java.lang.Long id) {
        return getFacade().find(id);
    }

    public List<Relationship> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<Relationship> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public lk.gov.health.phsp.facade.RelationshipFacade getEjbFacade() {
        return ejbFacade;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public Relationship getAdding() {
        if (adding == null) {
            adding = new Relationship();
        }
        return adding;
    }

    public void setAdding(Relationship adding) {
        this.adding = adding;
    }

    public Relationship getRemoving() {
        return removing;
    }

    public void setRemoving(Relationship removing) {
        this.removing = removing;
    }

    public int getYear() {
        if (year == 0) {
            Calendar c = Calendar.getInstance();
            year = c.get(Calendar.YEAR);
        }
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDistrictColumnNumber() {
        return districtColumnNumber;
    }

    public void setDistrictColumnNumber(int districtColumnNumber) {
        this.districtColumnNumber = districtColumnNumber;
    }

    public int getEstimatedMidyearPopulationColumnNumber() {
        return estimatedMidyearPopulationColumnNumber;
    }

    public void setEstimatedMidyearPopulationColumnNumber(int estimatedMidyearPopulationColumnNumber) {
        this.estimatedMidyearPopulationColumnNumber = estimatedMidyearPopulationColumnNumber;
    }

    public int getTargetPopulationColumnNumber() {
        return targetPopulationColumnNumber;
    }

    public void setTargetPopulationColumnNumber(int targetPopulationColumnNumber) {
        this.targetPopulationColumnNumber = targetPopulationColumnNumber;
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

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public AreaController getAreaController() {
        return areaController;
    }

    public AreaFacade getAreaFacade() {
        return areaFacade;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    @FacesConverter(forClass = Relationship.class)
    public static class RelationshipControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            RelationshipController controller = (RelationshipController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "relationshipController");
            return controller.getRelationship(getKey(value));
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
            if (object instanceof Relationship) {
                Relationship o = (Relationship) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Relationship.class.getName()});
                return null;
            }
        }

    }

}
