/*
 * The MIT License
 *
 * Copyright 2021 buddhika.
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
package lk.gov.health.phsp.ws;

import java.util.List;
import javax.enterprise.context.Dependent;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import lk.gov.health.phsp.bean.AreaApplicationController;
import lk.gov.health.phsp.bean.InstitutionApplicationController;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.enums.AreaType;
import lk.gov.health.phsp.enums.RelationshipType;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * REST Web Service
 *
 * @author buddhika
 */
@Path("")
@Dependent
public class ApiResource {

    @Context
    private UriInfo context;

    @Inject
    AreaApplicationController areaApplicationController;
    @Inject
    InstitutionApplicationController institutionApplicationController;

    /**
     * Creates a new instance of GenericResource
     */
    public ApiResource() {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getJson(@QueryParam("name") String name, @QueryParam("year") String year) {
        JSONObject jSONObjectOut;
        if (name == null || name.trim().equals("")) {
            jSONObjectOut = errorMessageInstruction();
        } else {
            switch (name) {
                case "get_province_list":
                    jSONObjectOut = provinceList();
                    break;
                case "get_district_list":
                    jSONObjectOut = districtList();
                    break;
                case "get_institutes_list":
                    jSONObjectOut = instituteList();
                    break;
                case "get_module_institutes_list":
                    jSONObjectOut = moduleInstituteList();
                    break;
                case "get_institutes_total_population_list":
                    jSONObjectOut = institutePopulationList(year);
                    break;
                default:
                    jSONObjectOut = errorMessage();
            }
        }
        String json = jSONObjectOut.toString();
        return json;
    }

    private JSONObject districtList() {
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();
        List<Area> ds = areaApplicationController.getAllAreas(AreaType.District);
        for (Area a : ds) {
            JSONObject ja = new JSONObject();
            ja.put("district_id", a.getId());
            ja.put("district_code", a.getCode());
            ja.put("district_name", a.getName());
            array.put(ja);
        }
        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject instituteList() {
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();
        List<Institution> ds = institutionApplicationController.getInstitutions();
        for (Institution a : ds) {
            JSONObject ja = new JSONObject();
            ja.put("institute_id", a.getId()); 
            ja.put("institute_code", a.getCode()); 
            ja.put("name", a.getName());
            ja.put("hin", a.getPoiNumber());
            ja.put("latitude", a.getCoordinate().getLatitude());
            ja.put("longitude", a.getCoordinate().getLongitude());
            ja.put("address", a.getAddress());
            if (a.getProvince() != null) {
                ja.put("province_id", a.getProvince().getCode());
            }
            if (a.getDistrict() != null) {
                ja.put("district_id", a.getDistrict().getCode());
            }
            array.put(ja);
        }
        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }
    
    private JSONObject institutePopulationList(String year) {
        if(year==null || year.trim().equals("")){
            return errorMessageNoYear();
        }
        Integer intYear;
        try{
            intYear = Integer.parseInt(year);
        }catch (Exception e){
            System.out.println("e = " + e);
            return errorMessageNoYear();
        }
        if(intYear==null||intYear<2000||intYear>2030){
            return errorMessageNoYear();
        }
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();
        List<Institution> ds = institutionApplicationController.getInstitutions();
        for (Institution a : ds) {
            JSONObject ja = new JSONObject();
            ja.put("institute_id", a.getCode());  
            ja.put("institute_code", a.getCode());  
            
            ja.put("year", a.getName());
            
            ja.put("male",institutionApplicationController.findInstitutionPopulationData(a, RelationshipType.Male_Population, intYear));
            ja.put("female", institutionApplicationController.findInstitutionPopulationData(a, RelationshipType.Female_Population, intYear));
            ja.put("over_35_male", institutionApplicationController.findInstitutionPopulationData(a, RelationshipType.Over_35_Male_Population, intYear));
            ja.put("over_35_female", institutionApplicationController.findInstitutionPopulationData(a, RelationshipType.Over_35_Female_Population, intYear));
            ja.put("target_over_35_male", institutionApplicationController.findInstitutionPopulationData(a, RelationshipType.Over_35_Male_Population, intYear));
            ja.put("target_over_35_female", institutionApplicationController.findInstitutionPopulationData(a, RelationshipType.Over_35_Female_Population, intYear));
            
            array.put(ja);
        }
        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }
    
    private JSONObject moduleInstituteList() {
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();
        List<Institution> ds = institutionApplicationController.getInstitutions();
        for (Institution a : ds) {
            JSONObject ja = new JSONObject();
            ja.put("institute_id", a.getId()); 
            ja.put("institute_code", a.getCode()); 
            ja.put("institute_type_db",a.getInstitutionType());
            ja.put("institute_type",a.getInstitutionType().getLabel());
            ja.put("name", a.getName());
            ja.put("hin", a.getPoiNumber());
            ja.put("address", a.getAddress());
            ja.put("province_id", a.getProvince().getCode());
            ja.put("district_id", a.getDistrict().getCode());
            array.put(ja);
        }
        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject provinceList() {
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();
        List<Area> ds = areaApplicationController.getAllAreas(AreaType.Province);
        for (Area a : ds) {
            JSONObject ja = new JSONObject();
            ja.put("province_id", a.getCode());
            ja.put("province_code", a.getCode());
            ja.put("province_name", a.getName());
            array.put(ja);
        }
        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject successMessage() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 200);
        jSONObjectOut.put("type", "success");
        return jSONObjectOut;
    }

    private JSONObject errorMessage() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 400);
        jSONObjectOut.put("type", "error");
        jSONObjectOut.put("message", "Parameter name is not recognized.");
        return jSONObjectOut;
    }
    
    private JSONObject errorMessageNoYear() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 400);
        jSONObjectOut.put("type", "error");
        jSONObjectOut.put("message", "Parameter year is provided or not recognized.");
        return jSONObjectOut;
    }

    private JSONObject errorMessageInstruction() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 400);
        jSONObjectOut.put("type", "error");
        jSONObjectOut.put("message", "You must provide a value for the parameter name.");
        return jSONObjectOut;
    }

}
