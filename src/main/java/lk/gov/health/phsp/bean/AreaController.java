/*
 * The MIT License
 *
 * Copyright 2019 Dr M H B Ariyaratne<buddhika.ari@gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
    CoordinateFacade coordinateFacade;
    private List<Area> items = null;
    List<Area> mohAreas = null;
    List<Area> phiAreas = null;
    List<Area> rdhsAreas = null;
    List<Area> pdhsAreas = null;
    private List<Area> dsAreas = null;
    private List<Area> provinces = null;
    private Area selected;

    @Inject
    WebUserController webUserController;

    private MapModel polygonModel;

    public List<Area> getMohAreas() {
        if (mohAreas == null) {
            mohAreas = getAreas(AreaType.MOH, null);
        }
        return mohAreas;
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

        j += " and a.mohArea=:moh ";
        m.put("moh", mohArea);
        j += " order by a.name";
        System.out.println("m = " + m);
        System.out.println("j = " + j);
        List<Area> areas = getFacade().findBySQL(j, m);
        System.out.println("areas = " + areas);
        return areas;
    }

    public String drawArea() {
        polygonModel = new DefaultMapModel();

        //Polygon
        Polygon polygon = new Polygon();

        String j = "select c from Coordinate c where c.area=:a";
        Map m = new HashMap();
        m.put("a", selected);
        List<Coordinate> cs = coordinateFacade.findBySQL(j, m);
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

    private UploadedFile file;

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public String saveMohCoordinates() {
        System.out.println("saveMohCoordinates");
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
            int read = 0;
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

            for (int gnCount = 0; gnCount < nList.getLength(); gnCount++) {
                Node gnNode = nList.item(gnCount);
                NodeList gnNodes = gnNode.getChildNodes();
                for (int gnElemantCount = 0; gnElemantCount < gnNodes.getLength(); gnElemantCount++) {

                    Node gnDataNode = gnNodes.item(gnElemantCount);

                    if (gnElemantCount == 4) {
                        NodeList gnEdNodes = gnDataNode.getChildNodes();
                        for (int gnEdCount = 0; gnEdCount < gnEdNodes.getLength(); gnEdCount++) {
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
                        for (int gnEdCount = 0; gnEdCount < gnEdNodes.getLength(); gnEdCount++) {
                            Node gnEdNode = gnEdNodes.item(gnEdCount);

                            if (gnEdCount == 2) {
                                coordinatesText = gnEdNode.getTextContent().trim();
//                                System.out.println("coordinatesText = " + coordinatesText);
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
                        System.out.println("gnDataNode = " + gnDataNode.getTextContent());
                    }

                }

                province = getArea(provinceName, AreaType.Province,false,null);
                if (province == null) {
                    System.out.println("province = " + province);
                    JsfUtil.addErrorMessage("Add " + provinceName);
                    return "";
                }

                district = getArea(districtName, AreaType.District,false,null);
                if (district == null) {
                    System.out.println("district = " + district);
                    JsfUtil.addErrorMessage("Add " + districtName);
                    return "";
                }

                moh = getArea(mohAreaName, AreaType.MOH,false,null);
                if (moh == null) {
                    System.out.println("moh = " + moh);
                    moh = new Area();
                    moh.setType(AreaType.MOH);
                    moh.setCentreLatitude(Double.parseDouble(centreLat));
                    moh.setCentreLongitude(Double.parseDouble(centreLon));
                    moh.setZoomLavel(12);
                    moh.setName(mohAreaName);
                    moh.setParentArea(district);
                    getFacade().create(moh);
                    System.out.println("moh = " + moh);
                    coordinatesText = coordinatesText.replaceAll("[\\t\\n\\r]", " ");
                    addCoordinates(moh, coordinatesText);
                } else {
                    JsfUtil.addErrorMessage("MOH Exists");
                }
            }
        } catch (IOException ex) {
            System.out.println("ex.getMessage() = " + ex.getMessage());
            JsfUtil.addErrorMessage(ex.getMessage());
            return "";
        } catch (ParserConfigurationException ex) {
            System.out.println("ex.getMessage() = " + ex.getMessage());
            Logger.getLogger(AreaController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            System.out.println("ex.getMessage() = " + ex.getMessage());
            Logger.getLogger(AreaController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            System.out.println("ex.getMessage() = " + ex.getMessage());
        }
        return "";
    }

    public String saveGnCoordinates() {
        System.out.println("saveMohCoordinates");
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
            int read = 0;
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

            for (int gnCount = 0; gnCount < nList.getLength(); gnCount++) {
//            for (int gnCount = 0; gnCount < 3; gnCount++) {
                Node gnNode = nList.item(gnCount);
                NodeList gnNodes = gnNode.getChildNodes();
                for (int gnElemantCount = 0; gnElemantCount < gnNodes.getLength(); gnElemantCount++) {

                    Node gnDataNode = gnNodes.item(gnElemantCount);

                    if (gnElemantCount == 4) {
                        NodeList gnEdNodes = gnDataNode.getChildNodes();
                        for (int gnEdCount = 0; gnEdCount < gnEdNodes.getLength(); gnEdCount++) {
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
                        for (int gnEdCount = 0; gnEdCount < gnEdNodes.getLength(); gnEdCount++) {
                            Node gnEdNode = gnEdNodes.item(gnEdCount);

                            if (gnEdCount == 2) {
                                coordinatesText = gnEdNode.getTextContent().trim();
//                                System.out.println("coordinatesText = " + coordinatesText);
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
                                    System.out.println("mohAreaName = " + mohAreaName);
                                }
                                if (gnEdNode.getFirstChild().getTextContent().equals("GND_NO")) {
                                    gnAreaCode = gnEdNode.getLastChild().getTextContent();
                                    System.out.println("gnAreaCode = " + gnAreaCode);
                                }
                                if (gnEdNode.getFirstChild().getTextContent().equals("GND_N")) {
                                    gnAreaName = gnEdNode.getLastChild().getTextContent();
                                    System.out.println("gnAreaName = " + gnAreaName);
                                }

                            }
                        }
                    }

                    if (gnElemantCount == 8) {
                        System.out.println("gnDataNode = " + gnDataNode.getTextContent());
                    }

                }

                province = getArea(provinceName, AreaType.Province, false , null);
                if (province == null) {
                    System.out.println("province = " + province);
                    JsfUtil.addErrorMessage("Add " + provinceName);
                    return "";
                }

                district = getArea(districtName, AreaType.District, false , null);
                if (district == null) {
                    System.out.println("district = " + district);
                    JsfUtil.addErrorMessage("Add " + districtName);
                    return "";
                }

                moh = getArea(mohAreaName, AreaType.MOH, false , null);
                if (moh == null) {
                    System.out.println("MOH = " + mohAreaName);
                    JsfUtil.addErrorMessage("Add " + mohAreaName);
                    return "";
                }

                gn = getArea(gnAreaCode, AreaType.GN, false , null);
                System.out.println("gnAreaCode = " + gnAreaCode);
                System.out.println("gnAreaName = " + gnAreaName);
                if (gn == null) {
                    System.out.println("GN = " + gn);
                    gn = new Area();
                    gn.setType(AreaType.GN);
                    gn.setCentreLatitude(Double.parseDouble(centreLat));
                    gn.setCentreLongitude(Double.parseDouble(centreLon));
                    gn.setZoomLavel(16);
                    gn.setName(gnAreaName);
                    gn.setCode(gnAreaCode);
                    gn.setParentArea(moh);
                    getFacade().create(gn);
                    System.out.println("gn = " + gn);
                    System.out.println("to add coords");
                    coordinatesText = coordinatesText.replaceAll("[\\t\\n\\r]", " ");
                    addCoordinates(gn, coordinatesText);
                    System.out.println("adter add codes = ");
                } else {
                    JsfUtil.addErrorMessage("GN Exists");
                }
            }

        } catch (IOException ex) {
            System.out.println("ex.getMessage() = " + ex.getMessage());
            JsfUtil.addErrorMessage(ex.getMessage());
            return "";
        } catch (ParserConfigurationException ex) {
            System.out.println("ex.getMessage() = " + ex.getMessage());
            Logger.getLogger(AreaController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            System.out.println("ex.getMessage() = " + ex.getMessage());
            Logger.getLogger(AreaController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            System.out.println("ex.getMessage() = " + ex.getMessage());
        }
        return "";
    }

    public void addCoordinates(Area area, String s) {
        System.out.println("adding codes = ");
        String j = "select c from Coordinate c where c.area=:a";
        Map m = new HashMap();
        m.put("a", area);
        List<Coordinate> cs = coordinateFacade.findBySQL(j, m);
        for (Coordinate c : cs) {
            coordinateFacade.remove(c);
        }
        String cvsSplitBy = ",";
        String[] coords = s.split(" ");
        for (String a : coords) {
            System.out.println("a = " + a);
            String[] country = a.split(cvsSplitBy);
            if (country.length > 1) {
                System.out.println("Coordinates [Longitude= " + country[0] + " , Latitude=" + country[1] + "]");
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
        List<Coordinate> cs = coordinateFacade.findBySQL(j, m);
        for (Coordinate c : cs) {
            coordinateFacade.remove(c);
        }

        try {
            String line = "";
            String cvsSplitBy = ",";
            BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputstream(), "UTF-8"));

            int i = 0;
            while ((line = br.readLine()) != null) {
                String[] country = line.split(cvsSplitBy);

                if (i > 0) {
                    if (country.length > 2) {
                        System.out.println("Coordinates [Longitude= " + country[1] + " , Latitude=" + country[2] + "]");
                        Coordinate c = new Coordinate();
                        c.setArea(selected);

                        String strLon = country[1].replace("\"", "");
                        String strLat = country[2].replace("\"", "");

                        double lon = Double.parseDouble(strLon);
                        System.out.println("lon = " + lon);

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
            System.out.println("e = " + e);
            return "";
        }

    }

    public String saveCentreCoordinates() {
        System.out.println("saveCentreCoordinates = ");
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

            int i = 0;
            while ((line = br.readLine()) != null) {
                String[] country = line.split(cvsSplitBy);
                System.out.println("i = " + i);
                if (i > 0) {
                    System.out.println("country.length = " + country.length);
                    if (country.length > 3) {
                        System.out.println(country[3] + "Coordinates [Longitude= " + country[1] + " , Latitude=" + country[2] + "]");

                        String areName = country[3].replace("\"", "");
                        String j = "select c from Area c where upper(c.name) like :a order by c.id desc";
                        Map m = new HashMap();
                        m.put("a", areName.toUpperCase() + "%");
                        Area a = getFacade().findFirstBySQL(j, m);

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
                        System.out.println("lon = " + lon);

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
            System.out.println("e = " + e);
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
        return "/area/add_area_index";
    }

    public String saveNewDistrict() {
        selected.setCreatedAt(new Date());
        getFacade().create(selected);
        selected = null;
        items = null;
        JsfUtil.addSuccessMessage("New District Saved");
        return "/area/add_area_index";
    }

    public String saveNewMoh() {
        selected.setCreatedAt(new Date());
        getFacade().create(selected);
        selected = null;
        items = null;

        JsfUtil.addSuccessMessage("New MOH Area Saved");
        return "/area/add_area_index";
    }

    public String saveNewEducationalZone() {
        selected.setCreatedAt(new Date());
        getFacade().create(selected);
        selected = null;
        items = null;
        JsfUtil.addSuccessMessage("New Educational Zone Saved");
        return "/area/add_area_index";
    }

    public String saveNewPhi() {
        selected.setCreatedAt(new Date());
        getFacade().create(selected);
        selected = null;
        items = null;
        JsfUtil.addSuccessMessage("New PHI Area Saved");
        return "/area/add_area_index";
    }

    public String saveNewGn() {
        selected.setCreatedAt(new Date());
        getFacade().create(selected);
        selected = null;
        items = null;
        JsfUtil.addSuccessMessage("New GN Area Saved");
        return "/area/add_area_index";
    }

    public List<Area> getAreas(AreaType areaType, Area superArea) {
        String j;
        Map m = new HashMap();
        j = "select a "
                + " from Area a "
                + " where a.name is not null ";
        if (areaType != null) {
            j += " and a.type=:t";
            m.put("t", areaType);
        }
        if (superArea != null) {
            j += " and a.parentArea=:pa ";
            m.put("pa", superArea);
        }
        j += " order by a.name";
        System.out.println("m = " + m);
        System.out.println("j = " + j);
        List<Area> areas = getFacade().findBySQL(j, m);
        System.out.println("areas = " + areas);
        return areas;
    }

    public List<Area> completeProvinces(String qry) {
        return getAreas(qry, AreaType.Province);
    }

    public List<Area> completeDistricts(String qry) {
        return getAreas(qry, AreaType.District);
    }

    public List<Area> completeDsAreas(String qry) {
        return getAreas(qry, AreaType.DsArea);
    }

    public List<Area> completeGnAreas(String qry) {
        return getAreas(qry, AreaType.GN);
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
        System.out.println("m = " + m);
        System.out.println("j = " + j);
        return getFacade().findBySQL(j, m);
    }

    public Area getArea(String nameOrCode, AreaType areaType, boolean createNew, Area parentArea) {
        if(nameOrCode.trim().equals("")){
            return null;
        }
        String j;
        Map m = new HashMap();
        j = "select a "
                + " from Area a "
                + " where (upper(a.name) =:n or upper(a.code) =:n)  ";
        m.put("n", nameOrCode.toUpperCase());
        if (areaType != null) {
            j += " and a.type=:t";
            m.put("t", areaType);
        }
        j += " order by a.code";
        System.out.println("m = " + m);
        System.out.println("j = " + j);
        Area ta = getFacade().findFirstBySQL(j, m);
        if(ta==null && createNew){
            ta = new Area();
            ta.setName(nameOrCode);
            ta.setType(areaType);
            ta.setCreatedAt(new Date());
            ta.setParentArea(parentArea);
            getFacade().create(ta);
        }
        return ta;
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

}
