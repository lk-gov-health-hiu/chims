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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.Dependent;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import lk.gov.health.phsp.bean.AnalysisController;
import lk.gov.health.phsp.bean.ApiRequestApplicationController;
import lk.gov.health.phsp.bean.ApplicationController;
import lk.gov.health.phsp.bean.AreaApplicationController;
import lk.gov.health.phsp.bean.CommonController;
import lk.gov.health.phsp.bean.InstitutionApplicationController;
import lk.gov.health.phsp.bean.ItemApplicationController;
import lk.gov.health.phsp.bean.RelationshipController;
import lk.gov.health.phsp.bean.StoredQueryResultController;
import lk.gov.health.phsp.bean.WebUserController;
import lk.gov.health.phsp.entity.ApiKey;
import lk.gov.health.phsp.entity.ApiRequest;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.Client;
import lk.gov.health.phsp.entity.ClientEncounterComponentItem;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.entity.QueryComponent;
import lk.gov.health.phsp.entity.Relationship;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.enums.AreaType;
import lk.gov.health.phsp.enums.EncounterType;
import lk.gov.health.phsp.enums.ItemType;
import lk.gov.health.phsp.enums.RelationshipType;
import lk.gov.health.phsp.enums.WebUserRole;
import lk.gov.health.phsp.facade.ApiKeyFacade;
import lk.gov.health.phsp.pojcs.PrescriptionPojo;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * REST Web Service
 *
 * @author buddhika
 */
@Path("fhir")
@Dependent
public class FhirResource {

    @Context
    private UriInfo context;

    @EJB
    ApiKeyFacade apiKeyFacade;

    /**
     * Creates a new instance of GenericResource
     */
    public FhirResource() {
    }

    @GET
    @Path("/clint/{institution_code}/{last_invoice_id}")
    @Produces("application/json")
    public String getCashInvoice(@Context HttpServletRequest requestContext,
            @PathParam("last_invoice_id") String strLastIdInRequest,
            @PathParam("institution_code") String strInstitutionCode) {
        JSONArray array;
        JSONObject jSONObjectOut = new JSONObject();
        String key = requestContext.getHeader("FHIR");
        if (!isValidKey(key)) {
            jSONObjectOut = errorMessageNotValidKey();
            String json = jSONObjectOut.toString();
            return json;
        }

        Long lastIdInRequest;
        try {
            lastIdInRequest = Long.valueOf(strLastIdInRequest);
        } catch (Exception e) {
            jSONObjectOut = errorMessageNotValidPathParameter();
            String json = jSONObjectOut.toString();
            System.out.println("e = " + e);
            return json;
        }

        return errorMessageNotValidInstitution().toString();
    }

    private JSONObject errorMessage() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 400);
        jSONObjectOut.put("type", "error");
        String e = "Error.";
        jSONObjectOut.put("message", e);
        return jSONObjectOut;
    }

    private JSONObject errorMessageNoData() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 400);
        jSONObjectOut.put("type", "error");
        String e = "No Data.";
        jSONObjectOut.put("message", e);
        return jSONObjectOut;
    }

    private JSONObject errorMessageNotValidKey() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 401);
        jSONObjectOut.put("type", "error");
        String e = "Not a valid key.";
        jSONObjectOut.put("message", e);
        return jSONObjectOut;
    }

    private JSONObject errorMessageNotValidPathParameter() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 401);
        jSONObjectOut.put("type", "error");
        String e = "Not a valid path parameter.";
        jSONObjectOut.put("message", e);
        return jSONObjectOut;
    }

    private JSONObject errorMessageNotValidInstitution() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 401);
        jSONObjectOut.put("type", "error");
        String e = "Not a valid institution code.";
        jSONObjectOut.put("message", e);
        return jSONObjectOut;
    }

    public ApiKey findApiKey(String keyValue) {
        String j;
        j = "select a "
                + " from ApiKey a "
                + " where a.keyValue=:kv";
        Map m = new HashMap();
        m.put("kv", keyValue);
        return apiKeyFacade.findFirstByJpql(j, m);
    }

    private boolean isValidKey(String key) {
        System.out.println("key = " + key);
        if (key == null || key.trim().equals("")) {
            System.out.println("No key given");
            return false;
        }
        ApiKey k = findApiKey(key);
        if (k == null) {
            System.out.println("No key found");
            return false;
        }
        if (k.getWebUser() == null) {
            System.out.println("No user for the key");
            return false;
        }
        if (k.getWebUser().isRetired()) {
            System.out.println("User Retired");
            return false;
        }
        if (k.getDateOfExpiary().before(new Date())) {
            System.out.println("Key Expired");
            return false;
        }
        return true;
    }

}
