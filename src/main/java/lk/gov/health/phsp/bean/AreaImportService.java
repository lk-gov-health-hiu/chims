package lk.gov.health.phsp.bean;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.List;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.enums.AreaType;

@Stateless
public class AreaImportService {

    @PersistenceContext(unitName = "hmisPU")
    private EntityManager em;

    @Inject
    private AreaApplicationController areaApplicationController;

    @Asynchronous
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void runImport(byte[] fileData,
                          int startRow,
                          int typeCol, int nameCol, int codeCol,
                          int uidCol, int parentCol, int districtCol,
                          Long createdById,
                          int[] progress,    // [0]=processed [1]=total [2]=created [3]=skipped
                          boolean[] running,
                          List<String> warnings) {
        try {
            WebUser createdBy = em.getReference(WebUser.class, createdById);
            Workbook w = Workbook.getWorkbook(new ByteArrayInputStream(fileData));
            Sheet sheet = w.getSheet(0);
            int totalRows = Math.max(0, sheet.getRows() - startRow);
            progress[1] = totalRows;

            int batch = 0;
            for (int i = startRow; i < sheet.getRows(); i++) {
                progress[0]++;

                String typStr       = getCellValue(sheet, typeCol, i);
                String name         = getCellValue(sheet, nameCol, i);
                String code         = getCellValue(sheet, codeCol, i);
                String uidStr       = getCellValue(sheet, uidCol, i);
                String parentName   = getCellValue(sheet, parentCol, i);
                String districtName = getCellValue(sheet, districtCol, i);

                if (name.isEmpty()) {
                    progress[3]++;
                    continue;
                }

                AreaType areaType = null;
                for (AreaType at : AreaType.values()) {
                    if (at.name().equalsIgnoreCase(typStr)) {
                        areaType = at;
                        break;
                    }
                }
                if (areaType == null) {
                    warnings.add("Row " + i + ": unknown type '" + typStr + "' — skipped.");
                    progress[3]++;
                    continue;
                }

                if (areaApplicationController.getAreaByName(name, areaType) != null) {
                    progress[3]++;
                    continue;
                }

                Area area = new Area();
                area.setName(name);
                area.setType(areaType);
                area.setCode(code.isEmpty() ? null : code);

                if (!uidStr.isEmpty()) {
                    try {
                        area.setAreauid(Long.parseLong(uidStr));
                    } catch (NumberFormatException e) {
                        warnings.add("Row " + i + ": non-numeric UID '" + uidStr + "' ignored.");
                    }
                }

                if (!parentName.isEmpty()) {
                    Area parent = areaApplicationController.getAreaByName(parentName, null);
                    area.setParentArea(parent);
                }

                if (!districtName.isEmpty()) {
                    Area district = areaApplicationController.getAreaByName(districtName, AreaType.District);
                    area.setDistrict(district);
                }

                area.setCreatedAt(new Date());
                area.setCreatedBy(createdBy);
                em.persist(area);
                progress[2]++;

                if (++batch % 100 == 0) {
                    em.flush();
                    em.clear();
                    createdBy = em.getReference(WebUser.class, createdById);
                }
            }
            em.flush();

        } catch (Exception ex) {
            warnings.add("Fatal error: " + ex.getMessage());
        } finally {
            running[0] = false;
            areaApplicationController.reloadAreas();
        }
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
