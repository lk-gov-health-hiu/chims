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
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * REST Web Service
 *
 * @author buddhika
 */
@Path("data")
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
    public String getJson(@QueryParam("name") String name) {
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
            ja.put("district_id", a.getCode());
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
            ja.put("institute_id", a.getCode());
            ja.put("name", a.getName());
            ja.put("hin", a.getPoiNumber());
            ja.put("latitude", a.getCoordinate().getLatitude());
            ja.put("longitude", a.getCoordinate().getLongitude());
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

    private JSONObject errorMessageInstruction() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 400);
        jSONObjectOut.put("type", "error");
        jSONObjectOut.put("message", "You must provide a value for the parameter name.");
        return jSONObjectOut;
    }

}
