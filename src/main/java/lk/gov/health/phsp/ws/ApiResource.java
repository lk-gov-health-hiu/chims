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

import java.util.Date;
import java.util.List;
import javax.enterprise.context.Dependent;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.inject.Inject;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import lk.gov.health.phsp.bean.AnalysisController;
import lk.gov.health.phsp.bean.ApiRequestApplicationController;
import lk.gov.health.phsp.bean.ApplicationController;
import lk.gov.health.phsp.bean.AreaApplicationController;
import lk.gov.health.phsp.bean.CommonController;
import lk.gov.health.phsp.bean.InstitutionApplicationController;
import lk.gov.health.phsp.bean.ItemApplicationController;
import lk.gov.health.phsp.bean.StoredQueryResultController;
import lk.gov.health.phsp.entity.ApiRequest;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.Client;
import lk.gov.health.phsp.entity.ClientEncounterComponentItem;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.entity.QueryComponent;
import lk.gov.health.phsp.entity.Relationship;
import lk.gov.health.phsp.enums.AreaType;
import lk.gov.health.phsp.enums.EncounterType;
import lk.gov.health.phsp.enums.RelationshipType;
import lk.gov.health.phsp.enums.WebUserRole;
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
    @Inject
    AnalysisController analysisController;
    @Inject
    ItemApplicationController itemApplicationController;
    @Inject
    ApplicationController applicationController;
    @Inject
    StoredQueryResultController storedQueryResultController;
    @Inject
    ApiRequestApplicationController apiRequestApplicationController;

    /**
     * Creates a new instance of GenericResource
     */
    public ApiResource() {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getJson(@QueryParam("name") String name,
            @QueryParam("year") String year,
            @QueryParam("month") String month,
            @QueryParam("institute_id") String instituteId,
            @QueryParam("id") String id) {
        JSONObject jSONObjectOut;
        if (name == null || name.trim().equals("")) {
            jSONObjectOut = errorMessageInstruction();
        } else {
            switch (name) {
                case "get_procedure_list":
                    jSONObjectOut = procedureList();
                    break;
                case "get_procedures_pending":
                    jSONObjectOut = proceduresPending();
                    break;
                case "mark_request_as_received":
                    jSONObjectOut = markRequestAsReceived(id);
                    break;
                case "get_province_list":
                    jSONObjectOut = provinceList();
                    break;
                case "get_district_list":
                    jSONObjectOut = districtList();
                    break;
                case "get_institutes_list":
                case "get_institute_list":
                    jSONObjectOut = instituteList();
                    break;
                case "get_institutes_list_hash":
                    jSONObjectOut = instituteListHash();
                    break;
                case "get_institutes_total_population_list":
                    jSONObjectOut = instituteListWithPopulations(year);
                    break;

                case "get_institutes_registered_list":
                    jSONObjectOut = instituteListWithregistrationCounts(year, month);
                    break;
                case "get_institutes_screened_list":
                    jSONObjectOut = instituteListWithScreenedCounts(year, month);
                    break;
                case "get_patients_with_cvd_risk_list":
                    jSONObjectOut = instituteListWithCvdRiskCounts(year, month);
                    break;
                case "get_institute_populations":
                    jSONObjectOut = institutePopulations(year, instituteId);
                    break;
                case "get_institute_screened_counts":
                    jSONObjectOut = instituteScreened(year, month, instituteId);
                    break;

                case "get_institute_registered_counts":
                    jSONObjectOut = instituteScreened(year, month, instituteId);
                    break;
                default:
                    jSONObjectOut = errorMessage();
            }
        }
        String json = jSONObjectOut.toString();
        return json;
    }

    @GET
    @Path("/get_role_name/{roleId}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getRoleName(@PathParam("roleId") String roleId) {
        return WebUserRole.valueOf(roleId).getLabel();
    }

    @GET
    @Path("/get_ins_name/{insCode}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getInstituteName(@PathParam("insCode") String insCode) {
        return null;
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
        List<Institution> ds = institutionApplicationController.getHospitals();
        for (Institution a : ds) {
            JSONObject ja = new JSONObject();
            ja.put("institute_id", a.getId());
            ja.put("institute_code", a.getCode());
            ja.put("name", a.getName());
            ja.put("hin", a.getPoiNumber());
            ja.put("latitude", a.getCoordinate().getLatitude());
            ja.put("longitude", a.getCoordinate().getLongitude());
            ja.put("address", a.getAddress());
            ja.put("type", a.getInstitutionType());
            ja.put("type_label", a.getInstitutionType().getLabel());
            if (a.getEditedAt() != null) {
                ja.put("edited_at", a.getEditedAt());
            } else {
                ja.put("edited_at", a.getCreatedAt());
            }
            if (a.getProvince() != null) {
                ja.put("province_id", a.getProvince().getId());
            }
            if (a.getDistrict() != null) {
                ja.put("district_id", a.getDistrict().getId());
            }
            ja.put("child_institutions", Get_Child_Institutions(a));
            array.put(ja);
        }
        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject instituteListWithPopulations(String year) {
        if (year == null || year.trim().equals("")) {
            return errorMessageNoYear();
        }
        Integer intYear;
        try {
            intYear = Integer.parseInt(year);
        } catch (NumberFormatException e) {
            System.out.println("e = " + e);
            return errorMessageNoYear();
        }
        if (intYear == null || intYear < 2000 || intYear > 2030) {
            return errorMessageNoYear();
        }
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();
        List<Institution> ds = institutionApplicationController.getHospitals();
        for (Institution a : ds) {
            JSONObject ja = new JSONObject();
            ja.put("institute_id", a.getId());
            ja.put("institute_code", a.getCode());

            ja.put("year", year);

            ja.put("male", institutionApplicationController.findInstitutionPopulationData(a, RelationshipType.Male_Population, intYear));
            ja.put("female", institutionApplicationController.findInstitutionPopulationData(a, RelationshipType.Female_Population, intYear));
            ja.put("over_35_male", institutionApplicationController.findInstitutionPopulationData(a, RelationshipType.Over_35_Male_Population, intYear));
            ja.put("over_35_female", institutionApplicationController.findInstitutionPopulationData(a, RelationshipType.Over_35_Female_Population, intYear));
            ja.put("target_over_35_male", institutionApplicationController.findInstitutionPopulationData(a, RelationshipType.Annual_Target_Male_Population, intYear));
            ja.put("target_over_35_female", institutionApplicationController.findInstitutionPopulationData(a, RelationshipType.Annual_Target_Female_Population, intYear));

            array.put(ja);
        }
        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject instituteListWithScreenedCounts(String year, String month) {
        if (year == null || year.trim().equals("")) {
            return errorMessageNoYear();
        }
        Integer intYear;
        try {
            intYear = Integer.parseInt(year);
        } catch (NumberFormatException e) {
            System.out.println("e = " + e);
            return errorMessageNoYear();
        }
        if (intYear < 2000 || intYear > 2030) {
            return errorMessageNoYear();
        }

        //Month
        if (month == null || month.trim().equals("")) {
            return errorMessageNoYear();
        }
        Integer intMonth;
        intMonth = CommonController.monthIntFromString(month);
        if (intMonth == null) {
            try {
                intMonth = Integer.parseInt(month);
            } catch (NumberFormatException e) {
                System.out.println("e = " + e);
                return errorMessageNoMonth();
            }
        }

        if (intMonth < 0 || intMonth > 12) {
            return errorMessageNoMonth();
        }

        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();
        List<Institution> ds = institutionApplicationController.getHospitals();
        for (Institution a : ds) {

            Date from = CommonController.startOfTheMonth(intYear, intMonth);
            Date to = CommonController.endOfTheMonth(intYear, intMonth);

            List<Institution> pIns = institutionApplicationController.findChildrenInstitutions(a);

            Long maleCount = analysisController.findEncounterCount(from, to, pIns, EncounterType.Clinic_Visit, itemApplicationController.getMale());
            Long femaleCount = analysisController.findEncounterCount(from, to, pIns, EncounterType.Clinic_Visit, itemApplicationController.getFemale());
            Long totalCount = analysisController.findEncounterCount(from, to, pIns, EncounterType.Clinic_Visit, null);

            if (totalCount != null && totalCount > 0) {
                JSONObject ja = new JSONObject();
                ja.put("institute_id", a.getId());
                ja.put("institute_code", a.getCode());
                ja.put("year", year);
                ja.put("month", month);
                ja.put("male", maleCount);
                ja.put("female", femaleCount);
                ja.put("total", totalCount);
                array.put(ja);
            }

        }
        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject instituteListWithCvdRiskCounts(String year, String month) {
        if (year == null || year.trim().equals("")) {
            return errorMessageNoYear();
        }
        Integer intYear;
        try {
            intYear = Integer.parseInt(year);
        } catch (NumberFormatException e) {
            System.out.println("e = " + e);
            return errorMessageNoYear();
        }
        if (intYear < 2000 || intYear > 2030) {
            return errorMessageNoYear();
        }

        //Month
        if (month == null || month.trim().equals("")) {
            return errorMessageNoYear();
        }
        Integer intMonth;
        intMonth = CommonController.monthIntFromString(month);
        if (intMonth == null) {
            try {
                intMonth = Integer.parseInt(month);
            } catch (NumberFormatException e) {
                System.out.println("e = " + e);
                return errorMessageNoMonth();
            }
        }

        if (intMonth < 0 || intMonth > 12) {
            return errorMessageNoMonth();
        }

        QueryComponent mc = applicationController.findQueryComponent("encounter_count_Number_of_female_participants_with_CVD_risk_greater_20presentage");
        if (mc == null) {
            return errorMessageNoIndicator();
        }
        QueryComponent fc = applicationController.findQueryComponent("encounter_count_Number_of_male_participants_with_CVD_risk_greater_20presentage");
        if (fc == null) {
            return errorMessageNoIndicator();
        }
        QueryComponent tc = applicationController.findQueryComponent("encounter_count_of_total_CVD_risk_greater_20presentage");
        if (tc == null) {
            return errorMessageNoIndicator();
        }

        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();
        List<Institution> ds = institutionApplicationController.getHospitals();
        for (Institution a : ds) {

            Date from = CommonController.startOfTheMonth(intYear, intMonth);
            Date to = CommonController.endOfTheMonth(intYear, intMonth);

            List<Institution> pIns = institutionApplicationController.findChildrenInstitutions(a);
            pIns.add(a);

            Long maleCount = storedQueryResultController.findStoredLongValue(mc, from, to, pIns);
            Long femaleCount = storedQueryResultController.findStoredLongValue(fc, from, to, pIns);
            Long totalCount = storedQueryResultController.findStoredLongValue(tc, from, to, pIns);

            if (totalCount != null && totalCount > 0) {
                JSONObject ja = new JSONObject();
                ja.put("institute_id", a.getId());
                ja.put("institute_code", a.getCode());
                ja.put("year", year);
                ja.put("month", month);
                ja.put("male", maleCount);
                ja.put("female", femaleCount);
                ja.put("total", totalCount);
                array.put(ja);
            }

        }
        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject instituteListWithregistrationCounts(String year, String month) {
        if (year == null || year.trim().equals("")) {
            return errorMessageNoYear();
        }
        Integer intYear;
        try {
            intYear = Integer.parseInt(year);
        } catch (NumberFormatException e) {
            System.out.println("e = " + e);
            return errorMessageNoYear();
        }
        if (intYear < 2000 || intYear > 2050) {
            return errorMessageNoYear();
        }

        //Month
        if (month == null || month.trim().equals("")) {
            return errorMessageNoMonth();
        }
        Integer intMonth;
        intMonth = CommonController.monthIntFromString(month);
        if (intMonth == null) {
            try {
                intMonth = Integer.parseInt(month);
            } catch (NumberFormatException e) {
                System.out.println("e = " + e);
                return errorMessageNoMonth();
            }
        }
        if (intMonth < 1 || intMonth > 12) {
            return errorMessageNoMonth();
        }

        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();
        List<Institution> ds = institutionApplicationController.getHospitals();
        for (Institution a : ds) {

            Date from = CommonController.startOfTheMonth(intYear, intMonth);
            Date to = CommonController.endOfTheMonth(intYear, intMonth);

            List<Institution> pIns = institutionApplicationController.findChildrenInstitutions(a);
            pIns.add(a);

            Long maleCount = analysisController.findRegistrationCount(from, to, pIns, itemApplicationController.getMale());
            Long femaleCount = analysisController.findRegistrationCount(from, to, pIns, itemApplicationController.getFemale());
            Long totalCount = analysisController.findRegistrationCount(from, to, pIns, null);

//            System.out.println("Institution = " + a.getName());
//            System.out.println("maleCount = " + maleCount);
//            System.out.println("femaleCount = " + femaleCount);
//            System.out.println("totalCount = " + totalCount);
//            System.out.println("intYear = " + intYear);
//            System.out.println("intMonth = " + intMonth);
//            System.out.println("from = " + CommonController.dateTimeToString(from, "dd MMMM yyyy hh:mm"));
//            System.out.println("to = " + CommonController.dateTimeToString(to, "dd MMMM yyyy hh:mm"));
//            
            if (totalCount != null && totalCount > 0) {
                JSONObject ja = new JSONObject();
                ja.put("institute_id", a.getId());
                ja.put("institute_code", a.getCode());
                ja.put("year", year);
                ja.put("month", month);
                ja.put("male", maleCount);
                ja.put("female", femaleCount);
                ja.put("total", totalCount);
                array.put(ja);
            }

        }
        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject institutePopulations(String year, String insId) {

        //Year
        if (year == null || year.trim().equals("")) {
            return errorMessageNoYear();
        }
        Integer intYear;

        try {
            intYear = Integer.parseInt(year);
        } catch (Exception e) {
            System.out.println("e = " + e);
            return errorMessageNoYear();
        }
        if (intYear == null || intYear < 2000 || intYear > 2030) {
            return errorMessageNoYear();
        }

        //Institution
        if (insId == null || insId.trim().equals("")) {
            return errorMessageNoInstituteId();
        }
        Long lngInsId = null;

        try {
            lngInsId = Long.parseLong(insId);
        } catch (Exception e) {
            System.out.println("e = " + e);
            return errorMessageNoInstituteId();
        }
        if (lngInsId == null || lngInsId < 1) {
            return errorMessageNoInstituteId();
        }
        Institution a = institutionApplicationController.findInstitution(lngInsId);

        if (a == null) {
            return errorMessageNoInstituteFound();
        }

        JSONObject jSONObjectOut = new JSONObject();

        JSONObject io = new JSONObject();
        io.put("institute_id", a.getCode());
        io.put("institute_code", a.getCode());
        io.put("institute_name", a.getName());
        io.put("year", year);

        JSONArray array = new JSONArray();
        List<Relationship> ds = institutionApplicationController.findInstitutionPopulationData(a, intYear);

        for (Relationship r : ds) {
            JSONObject ja = new JSONObject();
            ja.put("population_type", r.getRelationshipType());
            ja.put("value", r.getLongValue1());
            array.put(ja);
        }

        io.put("populations", array);

        jSONObjectOut.put("data", io);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject instituteScreened(String year, String month, String insId) {
        //Year
        if (year == null || year.trim().equals("")) {
            return errorMessageNoYear();
        }
        Integer intYear;
        try {
            intYear = Integer.parseInt(year);
        } catch (Exception e) {
            System.out.println("e = " + e);
            return errorMessageNoYear();
        }
        if (intYear == null || intYear < 2000 || intYear > 2030) {
            return errorMessageNoYear();
        }

        //Month
        if (month == null || month.trim().equals("")) {
            return errorMessageNoYear();
        }
        Integer intMonth;
        try {
            intMonth = Integer.parseInt(month);
        } catch (NumberFormatException e) {
            System.out.println("e = " + e);
            return errorMessageNoMonth();
        }
        if (intMonth < 1 || intMonth > 12) {
            return errorMessageNoMonth();
        }

        //Institution
        Long lngInsId = null;
        if (insId == null || insId.trim().equals("")) {
            return errorMessageNoInstituteId();
        }
        try {
            lngInsId = Long.parseLong(insId);
        } catch (Exception e) {
            System.out.println("e = " + e);
            return errorMessageNoInstituteId();
        }
        if (lngInsId == null || lngInsId < 1) {
            return errorMessageNoInstituteId();
        }

        Date from = CommonController.startOfTheMonth(intYear, intMonth);
        Date to = CommonController.endOfTheMonth(intYear, intMonth);

        Institution a = institutionApplicationController.findInstitution(lngInsId);

        if (a == null) {
            return errorMessageNoInstituteFound();
        }
        List<Institution> pIns = institutionApplicationController.findChildrenInstitutions(a);

        JSONObject jSONObjectOut = new JSONObject();

        JSONObject io = new JSONObject();
        io.put("institute_id", a.getCode());
        io.put("institute_code", a.getCode());
        io.put("institute_name", a.getName());
        io.put("year", year);

        JSONArray array = new JSONArray();
        List<Relationship> ds = institutionApplicationController.findInstitutionPopulationData(a, intYear);

        Long maleCount = analysisController.findEncounterCount(from, to, pIns, EncounterType.Clinic_Visit, itemApplicationController.getMale());
        Long femaleCount = analysisController.findEncounterCount(from, to, pIns, EncounterType.Clinic_Visit, itemApplicationController.getFemale());
        Long totalCount = analysisController.findEncounterCount(from, to, pIns, EncounterType.Clinic_Visit, null);

        JSONObject jam = new JSONObject();
        jam.put("name", "Males");
        jam.put("value", maleCount);
        array.put(jam);

        JSONObject jaf = new JSONObject();
        jaf.put("name", "Females");
        jaf.put("value", femaleCount);
        array.put(jaf);

        JSONObject jat = new JSONObject();
        jat.put("name", "Total");
        jat.put("value", maleCount);
        array.put(jaf);

        io.put("populations", array);

        jSONObjectOut.put("data", io);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject instituteListHash() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("data", institutionApplicationController.getInstitutionHash());
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject provinceList() {
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();
        List<Area> ds = areaApplicationController.getAllAreas(AreaType.Province);
        for (Area a : ds) {
            JSONObject ja = new JSONObject();
            ja.put("province_id", a.getId());
            ja.put("province_code", a.getCode());
            ja.put("province_name", a.getName());
            array.put(ja);
        }
        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject procedureList() {
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();
        List<Item> ds = itemApplicationController.findChildren("procedure");
        for (Item a : ds) {
            JSONObject ja = new JSONObject();
            ja.put("procedure_id", a.getId());
            ja.put("procedure_code", a.getCode());
            ja.put("procedure_name", a.getName());
            ja.put("procedure_descreption", a.getDescreption());
            array.put(ja);
        }
        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject markRequestAsReceived(String id){
        boolean f = apiRequestApplicationController.markRequestAsReceived(id);
        if(!f){
            return errorMessageNoId();
        }
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }
    
    private JSONObject proceduresPending() {
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();
        List<ApiRequest> ds = apiRequestApplicationController.getPendingProcedure();
        for (ApiRequest a : ds) {
            JSONObject ja = new JSONObject();

            if (a.getRequestCeci() == null) {
                System.err.println("a.getRequestCeci() is null");
                continue;
            }

            ClientEncounterComponentItem ci = a.getRequestCeci();
            Client c = null;
            Item i = null;
            Institution ins = null;

            if (ci.getItemValue() != null) {
                i = ci.getItemValue();
            } else {
                System.err.println("ci.getItemValue() is null");
                continue;
            }

            if (ci.getEncounter() != null) {
                if (ci.getEncounter().getClient() != null) {
                    c = ci.getEncounter().getClient();
                } else {
                    System.err.println("ci.getEncounter().getClient() is null");
                    continue;
                }
                if (ci.getEncounter().getInstitution() != null) {
                    ins = ci.getEncounter().getInstitution();
                } else {
                    System.err.println("ci.getEncounter().getInstitution() is null");
                    continue;
                }
            } else {
                System.out.println("ci.getEncounter() is null");
                continue;
            }

            ja.put("procedure_request_id", a.getId());
            ja.put("procedure_id", i.getId());
            ja.put("procedure_code", i.getCode());
            ja.put("procedure_name", i.getName());
            ja.put("client_phn", c.getPhn());
            ja.put("client_id", c.getId());
            ja.put("client_name", c.getPerson().getName());
            ja.put("institute_id", ins.getId());
            ja.put("institute_code", ins.getCode());
            ja.put("institute_name", ins.getName());
            if (ins.getParent() != null) {
                ja.put("parent_institute_id", ins.getParent().getId());
                ja.put("parent_institute_code", ins.getParent().getCode());
                ja.put("parent_institute_name", ins.getParent().getName());
            }
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
        String e = "Parameter name is not recognized.";
        jSONObjectOut.put("message", "Parameter name is not recognized.");
        return jSONObjectOut;
    }

    private JSONObject errorMessageTime() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 400);
        jSONObjectOut.put("type", "error");
        String e = "This requested is supported only during 9pm to 6am next day.";
        jSONObjectOut.put("message", "Parameter name is not recognized.");
        return jSONObjectOut;
    }

    private JSONObject errorMessageNoYear() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 401);
        jSONObjectOut.put("type", "error");
        jSONObjectOut.put("message", "Parameter year is not provided or not recognized.");
        return jSONObjectOut;
    }

    private JSONObject errorMessageNoIndicator() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 411);
        jSONObjectOut.put("type", "error");
        jSONObjectOut.put("message", "Indicator NOT recognized.");
        return jSONObjectOut;
    }

    private JSONObject errorMessageNoMonth() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 402);
        jSONObjectOut.put("type", "error");
        jSONObjectOut.put("message", "Parameter month is not provided or not recognized.");
        return jSONObjectOut;
    }

    private JSONObject errorMessageNoInstituteId() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 402);
        jSONObjectOut.put("type", "error");
        jSONObjectOut.put("message", "Parameter institute_id is not provided or not recognized.");
        return jSONObjectOut;
    }

    private JSONObject errorMessageNoInstituteFound() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 403);
        jSONObjectOut.put("type", "error");
        jSONObjectOut.put("message", "Parameter institute_id is not found.");
        return jSONObjectOut;
    }

    private JSONObject errorMessageInstruction() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 400);
        jSONObjectOut.put("type", "error");
        jSONObjectOut.put("message", "You must provide a value for the parameter name.");
        return jSONObjectOut;
    }
    
    private JSONObject errorMessageNoId() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 410);
        jSONObjectOut.put("type", "error");
        jSONObjectOut.put("message", "The ID provided is not found.");
        return jSONObjectOut;
    }

    private String Get_Child_Institutions(Institution institution) {
        String childInstitions = null;

        if (institution != null) {
            List<Institution> instList = institutionApplicationController.findChildrenInstitutions(institution);

            for (Institution i_ : instList) {
                if (childInstitions == null) {
                    childInstitions = institution.getCode() + ":" + i_.getCode();
                } else {
                    childInstitions += "^" + institution.getCode() + ":" + i_.getCode();
                }
            }
        }
        return childInstitions;
    }
}
