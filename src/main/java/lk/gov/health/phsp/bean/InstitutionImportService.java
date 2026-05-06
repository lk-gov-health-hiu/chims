package lk.gov.health.phsp.bean;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.facade.AreaFacade;
import lk.gov.health.phsp.facade.InstitutionFacade;
import lk.gov.health.phsp.facade.WebUserFacade;

@Stateless
public class InstitutionImportService {

    @EJB
    private InstitutionFacade institutionFacade;

    @EJB
    private AreaFacade areaFacade;

    @EJB
    private WebUserFacade webUserFacade;

    @Inject
    private InstitutionApplicationController institutionApplicationController;

    /**
     * Async import — no outer transaction; each facade.create() is its own transaction.
     * Areas and WebUser are looked up by ID inside each create via the facade (REQUIRED).
     */
    @Asynchronous
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void runImport(byte[] fileData,
                          Long createdById,
                          InstitutionType institutionType,
                          Long parentId,
                          Long provinceId,
                          Long pdhsAreaId,
                          Long districtId,
                          Long rdhsAreaId,
                          int[] progress,
                          boolean[] running,
                          List<String> warnings) {
        try {
            // Fetch managed references once (each call is REQUIRED — own transaction)
            WebUser   createdBy = webUserFacade.find(createdById);
            Institution parent  = parentId  != null ? institutionFacade.find(parentId)  : null;
            Area province       = provinceId != null ? areaFacade.find(provinceId)  : null;
            Area pdhsArea       = pdhsAreaId != null ? areaFacade.find(pdhsAreaId)  : null;
            Area district       = districtId != null ? areaFacade.find(districtId)  : null;
            Area rdhsArea       = rdhsAreaId != null ? areaFacade.find(rdhsAreaId)  : null;

            // O(1) duplicate check
            Set<String> existingNames = new HashSet<>();
            for (Institution ins : institutionApplicationController.getInstitutions()) {
                if (ins.getName() != null) {
                    existingNames.add(ins.getName().toLowerCase());
                }
            }

            Workbook w = Workbook.getWorkbook(new ByteArrayInputStream(fileData));
            Sheet sheet = w.getSheet(0);
            progress[1] = Math.max(0, sheet.getRows() - 1);

            for (int i = 1; i < sheet.getRows(); i++) {
                progress[0]++;

                String insName = getCellValue(sheet, 0, i);
                String poi     = getCellValue(sheet, 1, i);

                if (insName.isEmpty()) {
                    progress[3]++;
                    continue;
                }

                String fullName = institutionType.getLabel() + " " + insName;
                String hlcName  = "HLC " + insName;

                if (existingNames.contains(fullName.toLowerCase())) {
                    progress[3]++;
                    continue;
                }

                try {
                    Institution newIns = buildInstitution(
                            fullName, poi.isEmpty() ? null : poi,
                            institutionType, createdBy,
                            parent, province, pdhsArea, district, rdhsArea);
                    institutionFacade.create(newIns);
                    existingNames.add(fullName.toLowerCase());

                    Institution newClinic = buildInstitution(
                            hlcName, null,
                            InstitutionType.Clinic, createdBy,
                            newIns, province, pdhsArea, district, rdhsArea);
                    newClinic.setPoiInstitution(newIns);
                    institutionFacade.create(newClinic);
                    existingNames.add(hlcName.toLowerCase());

                    progress[2]++;
                } catch (Exception ex) {
                    warnings.add("Row " + i + " (" + insName + "): save failed — " + ex.getMessage());
                    progress[3]++;
                }
            }

        } catch (Exception ex) {
            warnings.add("Fatal error: " + ex.getMessage());
        } finally {
            running[0] = false;
            try {
                institutionApplicationController.resetAllInstitutions();
            } catch (Exception ignored) {
            }
        }
    }

    private Institution buildInstitution(String name, String poi, InstitutionType type,
                                          WebUser createdBy, Institution parent,
                                          Area province, Area pdhsArea, Area district, Area rdhsArea) {
        Institution ins = new Institution();
        ins.setName(name);
        ins.setPoiNumber(poi);
        ins.setInstitutionType(type);
        ins.setCreatedAt(new Date());
        ins.setCreater(createdBy);
        ins.setLastHin(0L);
        ins.setParent(parent);
        ins.setProvince(province);
        ins.setPdhsArea(pdhsArea);
        ins.setDistrict(district);
        ins.setRdhsArea(rdhsArea);
        return ins;
    }

    private String getCellValue(Sheet sheet, int col, int row) {
        try {
            Cell cell = sheet.getCell(col, row);
            return cell == null ? "" : cell.getContents().trim();
        } catch (Exception e) {
            return "";
        }
    }
}
