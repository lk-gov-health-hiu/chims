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

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.parser.IParser;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.Dependent;
import javax.faces.context.FacesContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PathParam;
import lk.gov.health.phsp.entity.ApiKey;
import lk.gov.health.phsp.entity.Client;
import lk.gov.health.phsp.entity.Encounter;
import lk.gov.health.phsp.enums.EncounterType;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.facade.ApiKeyFacade;
import lk.gov.health.phsp.facade.ClientFacade;
import lk.gov.health.phsp.facade.EncounterFacade;
import org.hl7.fhir.r4.model.BackboneElement;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.Base;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementDocumentComponent;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
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
    @EJB
    ClientFacade clientFacade;
    @EJB
    EncounterFacade encounterFacade;

    /**
     * Creates a new instance of GenericResource
     */
    public FhirResource() {
    }

    @GET
    @Path("/capability_statement")
    @Produces("application/json")
    public String getCapabilityStatement(@Context HttpServletRequest requestContext) {
        CapabilityStatement cs = new CapabilityStatement();
        CapabilityStatementDocumentComponent uc = new CapabilityStatementDocumentComponent();
        cs.getSoftware()
                .setName("cloud HIMS FHIR Server")
                .setVersion("1.0")
                .setReleaseDateElement(new DateTimeType("2022-10-06"));

        cs.addDocument(uc);
        cs.setName("cloud HIMS");
        FhirContext ctx = FhirContext.forR4();
        IParser parser = ctx.newJsonParser();

        String serialized = parser.encodeResourceToString(cs);

        return serialized;

    }

    @GET
    @Path("/Patient/{phn}")
    @Produces("application/json")
    public String getClientByPhn(@Context HttpServletRequest requestContext,
            @PathParam("phn") String phn) {

        String key = requestContext.getHeader("FHIR");
        if (!isValidKey(key)) {
            return errorMessageKey();
        }

        Long tid = null;
        try {
            tid = Long.valueOf(phn);
        } catch (Exception e) {

        }
        Client c = null;
        if (tid != null) {
            c = findPatientsById(tid);
        }
        if (c != null) {
            return patientToJson(c);
        }
        List<Client> cs = findPatientsByPhn(phn);
        if (cs != null && !cs.isEmpty()) {
            return patientToJson(cs);
        }
        cs = findPatientsByNic(phn);
        if (cs != null && !cs.isEmpty()) {
            return patientToJson(cs);
        }
        cs = findPatientsByPhone(phn);
        if (cs != null && !cs.isEmpty()) {
            return patientToJson(cs);
        }
        return errorMessageNoData();
    }

    public String patientToJson(Client c) {
        Patient patient = fhirPatientFromClient(c);

        FhirContext ctx = FhirContext.forR4();
        IParser parser = ctx.newJsonParser();

        String serialized = parser.encodeResourceToString(patient);

        return serialized;
    }

    public String getBaseUrl() {
        return "https://" + context.getBaseUri().getHost() + context.getBaseUri().getPath();
    }

    public String noData() {
        OperationOutcome oo = new OperationOutcome();
        OperationOutcome.OperationOutcomeIssueComponent c = oo.addIssue();
        c.setSeverity(OperationOutcome.IssueSeverity.ERROR);
        c.addExpression("No Data");
        FhirContext ctx = FhirContext.forR4();
        IParser parser = ctx.newJsonParser();
        String serialized = parser.encodeResourceToString(oo);
        return serialized;
    }

    public String patientToJson(List<Client> cs) {
        if (cs == null || cs.isEmpty()) {
            return noData();
        }
        if(cs.size()==1){
            return patientToJson(cs.get(0));
        }
        
        Bundle b = new Bundle();

        b.setId(cs.toString());
        b.setType(BundleType.SEARCHSET);
        b.setTotal(cs.size());

        for (Client c : cs) {
            Bundle.BundleEntryComponent e = b.addEntry();
            String temId = getBaseUrl() + "fhir/client/" + c.getId();
            e.setFullUrl(temId);
            Patient p = fhirPatientFromClient(c);
            e.setResource(p);
        }

//        Patient patient = fhirPatientFromClient(c);
        FhirContext ctx = FhirContext.forR4();
        IParser parser = ctx.newJsonParser();

        String serialized = parser.encodeResourceToString(b);

        return serialized;
    }

    @GET
    @Path("/Patient/{phn}/encounter/")
    @Produces("application/json")
    public String getClientVisitByPhn(@Context HttpServletRequest requestContext,
            @PathParam("phn") String phn) {

        String key = requestContext.getHeader("FHIR");
        if (!isValidKey(key)) {
            return errorMessageKey();
        }

        Client c = findFirstPatientsByPhn(phn);
        if (c == null) {
            return errorMessageNoData();
        }

        Patient patient = fhirPatientFromClient(c);
        List<Encounter> encounters = findEncounters(c, null, null, false, null, false);

        if (encounters == null || encounters.isEmpty()) {
            return errorMessageNoData();
        }

        org.hl7.fhir.r4.model.Encounter encounter = fhirEcnounterFromEncounter(encounters.get(0));
        FhirContext ctx = FhirContext.forR4();
        IParser parser = ctx.newJsonParser();

        String serialized = parser.encodeResourceToString(encounter);

        return serialized;
    }

    @GET
    @Path("/institution_client/{institution_code}")
    @Produces("application/json")
    public String getInstitutionClientByPhn(@Context HttpServletRequest requestContext,
            @PathParam("ins") String ins) {

        String key = requestContext.getHeader("FHIR");
        if (!isValidKey(key)) {
            return errorMessageKey();
        }

        Client c = findFirstPatientsByPhn(ins);
        if (c == null) {
            return errorMessageNoData();
        }

        Bundle b = new Bundle();
        b.setType(BundleType.COLLECTION);

        Patient patient = fhirPatientFromClient(c);

        FhirContext ctx = FhirContext.forR4();
        IParser parser = ctx.newJsonParser();

        String serialized = parser.encodeResourceToString(patient);

        return serialized;
    }

    public List<Client> listPatientsByPhn(String phn) {
        String j = "select c from Client c where c.retired=false and c.phn=:q order by c.phn";
        Map m = new HashMap();
        m.put("q", phn.trim().toUpperCase());
        return clientFacade.findByJpql(j, m);
    }

    public Client findFirstPatientsByPhn(String phn) {
        String j = "select c from Client c where c.retired=false and c.phn=:q order by c.id desc";
        Map m = new HashMap();
        m.put("q", phn.trim().toUpperCase());
        return clientFacade.findFirstByJpql(j, m);
    }

    public List<Client> findPatientsByPhn(String phn) {
        String j = "select c from Client c where c.phn=:q";
        Map m = new HashMap();
        m.put("q", phn.trim().toUpperCase());
        return clientFacade.findByJpql(j, m);
    }

    public Client findPatientsById(Long id) {
        String j = "select c from Client c where c.id=:q";
        Map m = new HashMap();
        m.put("q", id);
        return clientFacade.findFirstByJpql(j, m);
    }

    public List<Client> findPatientsByNic(String nic) {
        String j = "select c from Client c where c.person.nic=:q";
        Map m = new HashMap();
        m.put("q", nic);
        return clientFacade.findByJpql(j, m);
    }

    public List<Client> findPatientsByPhone(String nic) {
        String j = "select c from Client c where (c.person.phone1=:q or c.person.phone2=:q) ";
        Map m = new HashMap();
        m.put("q", nic);
        return clientFacade.findByJpql(j, m);
    }

    public org.hl7.fhir.r4.model.Encounter fhirEcnounterFromEncounter(Encounter e) {
        if (e == null) {
            return null;
        }
        org.hl7.fhir.r4.model.Encounter fe = new org.hl7.fhir.r4.model.Encounter();
        fe.setId(e.getId().toString());
        if (e.getCompleted()) {
            fe.setStatus(org.hl7.fhir.r4.model.Encounter.EncounterStatus.FINISHED);
        }
        fe.getPeriod().setStart(e.getCreatedAt());
        fe.getPeriod().setEnd(e.getCompletedAt());

        return fe;
    }

    public Patient fhirPatientFromClient(Client client) {
        if (client == null) {
            return null;
        }
        if (client.getPerson() == null) {
            return null;
        }
        Patient patient = new Patient();
        patient.setId(client.getId().toString());
        patient.addIdentifier()
                .setSystem("https://nic.gov.lk/")
                .setValue(client.getPerson().getNic());
        patient.addIdentifier()
                .setSystem("https://passport.gov.lk/")
                .setValue(client.getPerson().getPassportNumber());
        patient.addIdentifier()
                .setSystem("https://seniorcitizen.gov.lk/")
                .setValue(client.getPerson().getSsNumber());
        patient.addIdentifier()
                .setSystem("https://drivinglicense.gov.lk/")
                .setValue(client.getPerson().getDrivingLicenseNumber());
        patient.addIdentifier()
                .setSystem("https://phn.health.gov.lk/")
                .setValue(client.getPhn());
        patient.setActive(!client.isRetired());
        patient.addAddress().addLine(client.getPerson().getAddress());

        ContactPoint homePhone = patient.addTelecom();
        homePhone.setUse(ContactPoint.ContactPointUse.HOME);
        homePhone.setSystem(ContactPoint.ContactPointSystem.PHONE);
        homePhone.setValue(client.getPerson().getPhone2());

        ContactPoint mobile = patient.addTelecom();
        mobile.setUse(ContactPoint.ContactPointUse.MOBILE);
        mobile.setSystem(ContactPoint.ContactPointSystem.PHONE);
        mobile.setValue(client.getPerson().getPhone1());

        ContactPoint email = patient.addTelecom();
        email.setUse(ContactPoint.ContactPointUse.HOME);
        email.setSystem(ContactPoint.ContactPointSystem.EMAIL);
        email.setValue(client.getPerson().getEmail());

        patient.addName().setText(client.getPerson().getName());
        patient.addName()
                .setFamily(client.getPerson().getName())
                .addGiven(client.getPerson().getName());
        if (client.getPerson().getSex() == null || client.getPerson().getSex().getCode() == null) {
            patient.setGender(Enumerations.AdministrativeGender.NULL);
        } else if (client.getPerson().getSex().getCode().contains("female")) {
            patient.setGender(Enumerations.AdministrativeGender.FEMALE);
        } else if (client.getPerson().getSex().getCode().contains("male")) {
            patient.setGender(Enumerations.AdministrativeGender.MALE);
        } else {
            patient.setGender(Enumerations.AdministrativeGender.OTHER);
        }

        return patient;
    }

    private JSONObject errorMessage() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 400);
        jSONObjectOut.put("type", "error");
        String e = "Error.";
        jSONObjectOut.put("message", e);
        return jSONObjectOut;
    }

    private String errorMessageNoData() {
        FhirContext ctx = FhirContext.forR4();
        IParser parser = ctx.newJsonParser();

        OperationOutcome oo = new OperationOutcome();
        Narrative n = new Narrative();
        n.setId("Error - No Data");
        oo.setText(n);

        return parser.encodeResourceToString(oo);
    }

    private String errorMessageKey() {
        FhirContext ctx = FhirContext.forR4();
        IParser parser = ctx.newJsonParser();

        OperationOutcome oo = new OperationOutcome();
        Narrative n = new Narrative();
        n.setId("Error - Key is Invalid");
        oo.setText(n);

        return parser.encodeResourceToString(oo);
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

    public List<Encounter> findEncounters(Client client, List<InstitutionType> insTypes, EncounterType encType, boolean excludeCompleted, Integer maxRecordCount, boolean descending) {
        String j = "select e from Encounter e where e.retired=false ";
        Map m = new HashMap();
        if (client != null) {
            j += " and e.client=:c ";
            m.put("c", client);
        }
        if (insTypes != null) {
            j += " and e.institution.institutionType in :it ";
            m.put("it", insTypes);
        }
        if (insTypes != null) {
            j += " and e.encounterType=:et ";
            m.put("et", encType);
        }
        if (excludeCompleted) {
            j += " and e.completed=:com ";
            m.put("com", false);
        }
        if (descending) {
            j += " order by e.id desc";
        }
        if (maxRecordCount == null) {
            return encounterFacade.findByJpql(j, m);
        } else {
            return encounterFacade.findByJpql(j, m, maxRecordCount);
        }

    }

}
