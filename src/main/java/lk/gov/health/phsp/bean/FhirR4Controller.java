package lk.gov.health.phsp.bean;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import lk.gov.health.phsp.entity.Client;
import lk.gov.health.phsp.entity.IntegrationEndpoint;
import lk.gov.health.phsp.entity.SecurityProtocol;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Bundle;
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor;
import java.util.Date;
import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.AuditEvent;
import lk.gov.health.phsp.entity.FhirOperationResult;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.entity.Person;
import lk.gov.health.phsp.enums.AreaType;
import lk.gov.health.phsp.facade.AuditEventFacade;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointUse;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import lk.gov.health.phsp.entity.FhirResourceLink;
import lk.gov.health.phsp.facade.FhirResourceLinkFacade;
import lk.gov.health.phsp.pojcs.SearchQueryData;

/**
 *
 * @author buddh
 */
@Named
@ApplicationScoped
public class FhirR4Controller implements Serializable {

    @Resource
    private ManagedExecutorService executorService;

    @EJB
    AuditEventFacade auditEventFacade;
    @EJB
    FhirResourceLinkFacade fhirResourceLinkFacade;

    @Inject
    ItemApplicationController itemApplicationController;
    @Inject
    AreaApplicationController areaApplicationController;

    /**
     * Creates a new instance of FhirR5Controller
     */
    public FhirR4Controller() {
    }

    public CompletableFuture<FhirOperationResult> createPatientInFhirServerAsync(Client client, IntegrationEndpoint endPoint) {
        System.out.println("Creating patient in FHIR server...");
        SecurityProtocol sp = endPoint.getSecurityProtocol();
        String username = endPoint.getUserName();
        String password = endPoint.getPassword();
        Patient patient = convertToFhirPatient(client);

        return CompletableFuture.supplyAsync(() -> {
            FhirOperationResult result = new FhirOperationResult();

            FhirContext ctx = FhirContext.forR4();
            String serverBase = endPoint.getEndPointUrl();
            IGenericClient fhirClient = ctx.newRestfulGenericClient(serverBase);

            if (sp == SecurityProtocol.BASIC_AUTHENTICATION) {
                fhirClient.registerInterceptor(new BasicAuthInterceptor(username, password));
            } else if (sp == SecurityProtocol.API_KEY) {
                String apiKeyName = endPoint.getApiKeyName(); // Assuming this is the name of the API key header
                String apiKeyValue = endPoint.getApiKeyValue();
                // Add the API key to the client's headers
                fhirClient.registerInterceptor(new IClientInterceptor() {
                    @Override
                    public void interceptRequest(IHttpRequest theRequest) {
                        theRequest.addHeader(apiKeyName, apiKeyValue);
                    }

                    @Override
                    public void interceptResponse(IHttpResponse theResponse) {
                        // You can add response handling here if needed
                    }
                });
            }
            // Add other authentication methods as needed

            MethodOutcome outcome = fhirClient.create().resource(patient).execute();

            if (outcome.getCreated()) {
                IdType id = (IdType) outcome.getId();
                result.setSuccess(true);
                result.setMessage("Created new Patient with ID: " + id.getIdPart());
                result.setResourceId(id);

                // Call updateFhirResourceLink method
                updateFhirResourceLink(client, endPoint, id.getIdPart());

            } else {
                result.setSuccess(false);
                result.setMessage("Failed to create new Patient");
            }
            return result;
        });
    }

    public CompletableFuture<FhirOperationResult> updatePatientInFhirServerAsync(Client client, IntegrationEndpoint endPoint, String resourceId) {
        System.out.println("Updating patient in FHIR server...");
        SecurityProtocol sp = endPoint.getSecurityProtocol();
        String username = endPoint.getUserName();
        String password = endPoint.getPassword();
        Patient patient = convertToFhirPatient(client);

        return CompletableFuture.supplyAsync(() -> {
            FhirOperationResult result = new FhirOperationResult();

            FhirContext ctx = FhirContext.forR4();
            String serverBase = endPoint.getEndPointUrl();
            IGenericClient fhirClient = ctx.newRestfulGenericClient(serverBase);

            if (sp == SecurityProtocol.BASIC_AUTHENTICATION) {
                fhirClient.registerInterceptor(new BasicAuthInterceptor(username, password));
            } else if (sp == SecurityProtocol.API_KEY) {
                String apiKeyName = endPoint.getApiKeyName();
                String apiKeyValue = endPoint.getApiKeyValue();
                fhirClient.registerInterceptor(new IClientInterceptor() {
                    @Override
                    public void interceptRequest(IHttpRequest theRequest) {
                        theRequest.addHeader(apiKeyName, apiKeyValue);
                    }

                    @Override
                    public void interceptResponse(IHttpResponse theResponse) {
                        // You can add response handling here if needed
                    }
                });
            }

            // Set the resource ID for the update operation
            patient.setId(resourceId);

            // Perform the update operation
            MethodOutcome outcome = fhirClient.update().resource(patient).execute();

            if (outcome.getCreated()) {
                result.setSuccess(false);
                result.setMessage("Unexpectedly created a new Patient instead of updating");
            } else if (outcome.getResource() != null) {
                IdType id = (IdType) outcome.getId();
                result.setSuccess(true);
                result.setMessage("Updated Patient with ID: " + id.getIdPart());
                result.setResourceId(id);
            } else {
                result.setSuccess(false);
                result.setMessage("Failed to update Patient");
            }
            return result;
        });
    }

    public void updateFhirResourceLink(Object object, IntegrationEndpoint endPoint, String fhirResourceId) {
        // Search first for a record, if exists, nothing is needed, if not, create a new record
        String jpql = "select l "
                + " from FhirResourceLink l "
                + " where l.integrationEndpoint = :ep "
                + " and l.objectType = :objectType "
                + " and l.objectId = :objectId"; // Completed JPQL
        Map<String, Object> m = new HashMap<>();
        m.put("ep", endPoint);
        m.put("objectType", object.getClass().getName()); // Assuming you want to search by object's class name
        m.put("objectId", getObjectID(object)); // Assuming you have a method to get the object's ID
        FhirResourceLink l = fhirResourceLinkFacade.findFirstByJpql(jpql, m);
        if (l == null) {
            l = new FhirResourceLink();
            l.setFhirResourceId(fhirResourceId);
            l.setIntegrationEndpoint(endPoint);
            l.setObjectType(object.getClass().getName());
            l.setObjectId(getObjectID(object)); // Assuming you have a method to get the object's ID
            // Added other fields
            fhirResourceLinkFacade.create(l);
        }
    }

    private Long getObjectID(Object object) {
        try {
            Method getIdMethod = object.getClass().getMethod("getId");
            return (Long) getIdMethod.invoke(object);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    public CompletableFuture<List<Client>> fetchClientsFromEndpoints(SearchQueryData sqd, IntegrationEndpoint endPoint) {
        System.out.println("fetchClientsFromEndpoints");
        return CompletableFuture.supplyAsync(() -> {
            List<Client> clients = new ArrayList<>();

            // Create a FHIR client
            FhirContext ctx = FhirContext.forR4();
            String serverBase = endPoint.getEndPointUrl(); // Assuming this is the URL of the FHIR server
            IGenericClient client = ctx.newRestfulGenericClient(serverBase);

            // Apply security if needed
            if (endPoint.getSecurityProtocol() == SecurityProtocol.BASIC_AUTHENTICATION) {
                String username = endPoint.getUserName();
                String password = endPoint.getPassword();
                client.registerInterceptor(new BasicAuthInterceptor(username, password));
            } else if (endPoint.getSecurityProtocol() == SecurityProtocol.API_KEY) {
                String apiKeyName = endPoint.getApiKeyName(); // Assuming this is the name of the API key header
                String apiKeyValue = endPoint.getApiKeyValue();
                // Add the API key to the client's headers
                client.registerInterceptor(new IClientInterceptor() {
                    @Override
                    public void interceptRequest(IHttpRequest theRequest) {
                        theRequest.addHeader(apiKeyName, apiKeyValue);
                    }

                    @Override
                    public void interceptResponse(IHttpResponse theResponse) {
                        // You can add response handling here if needed
                    }
                });
            }

            String status = "success"; // Assume success by default
            Bundle results = null;
            try {
                System.out.println("sqd.getSearchCriteria() = " + sqd.getSearchCriteria());
                switch (sqd.getSearchCriteria()) {
                    case NIC_ONLY:
                        results = searchByIdentifier("https://fhir.health.gov.lk/id/nic", sqd.getNic(), client);
                        break;
                    case PHN_ONLY:
                        results = searchByIdentifier("https://fhir.health.gov.lk/id/phn", sqd.getPhn(), client);
                        break;
                    case PASSPORT_ONLY:
                        results = searchByIdentifier("https://fhir.health.gov.lk/id/passport", sqd.getPassport(), client);
                        break;
                    case PART_OF_NAME_ONLY:
                        results = client.search()
                                .forResource(Patient.class)
                                .where(Patient.NAME.matches().value(sqd.getName()))
                                .and(Patient.NAME.matches().values("text", sqd.getName())) // Matching the 'text' field inside the 'name' array
                                .returnBundle(Bundle.class)
                                .execute();
                        break;
                    case PART_OF_NAME_AND_BIRTH_YEAR:
                        String startDateOfYear = String.format("%04d-01-01", sqd.getBirthYear());
                        String endDateOfYear = String.format("%04d-12-31", sqd.getBirthYear());
                        results = client.search()
                                .forResource(Patient.class)
                                .where(Patient.NAME.matches().value(sqd.getName()))
                                .where(Patient.BIRTHDATE.afterOrEquals().day(startDateOfYear))
                                .where(Patient.BIRTHDATE.beforeOrEquals().day(endDateOfYear))
                                .returnBundle(Bundle.class)
                                .execute();
                        break;

                    case PART_OF_NAME_AND_BIRTH_YEAR_AND_MONTH:
                        String startDate = String.format("%04d-%02d-01", sqd.getBirthYear(), sqd.getBirthMonth());
                        String endDate = String.format("%04d-%02d-%02d", sqd.getBirthYear(), sqd.getBirthMonth(), LocalDate.of(sqd.getBirthYear(), sqd.getBirthMonth(), 1).lengthOfMonth());
                        results = client.search()
                                .forResource(Patient.class)
                                .where(Patient.NAME.matches().value(sqd.getName()))
                                .where(Patient.BIRTHDATE.afterOrEquals().day(startDate))
                                .where(Patient.BIRTHDATE.beforeOrEquals().day(endDate))
                                .returnBundle(Bundle.class)
                                .execute();
                        break;

                    case DL_ONLY:
                        results = client.search()
                                .forResource(Patient.class)
                                .where(Patient.IDENTIFIER.exactly().systemAndIdentifier("https://fhir.health.gov.lk/id/dl", sqd.getDl()))
                                .returnBundle(Bundle.class)
                                .execute();
                        break;
                    case PART_OF_NAME_AND_AGE_IN_YEARS:
                        LocalDate birthDateFromAge = LocalDate.now().minusYears(sqd.getAgeInYears());
                        results = client.search()
                                .forResource(Patient.class)
                                .where(Patient.NAME.matches().value(sqd.getName()))
                                .where(Patient.BIRTHDATE.beforeOrEquals().day(birthDateFromAge.toString()))
                                .returnBundle(Bundle.class)
                                .execute();
                        break;
                    case PART_OF_NAME_AND_DATE_OF_BIRTH:
                        results = client.search()
                                .forResource(Patient.class)
                                .where(Patient.NAME.matches().value(sqd.getName()))
                                .where(Patient.BIRTHDATE.exactly().day(sqd.getDateOfBirth().toString()))
                                .returnBundle(Bundle.class)
                                .execute();
                        break;
                    case SCN_ONLY:
                        results = client.search()
                                .forResource(Patient.class)
                                .where(Patient.IDENTIFIER.exactly().systemAndIdentifier("https://fhir.health.gov.lk/id/scn", sqd.getScn()))
                                .returnBundle(Bundle.class)
                                .execute();
                        break;
                    case TELEPHONE_NUMBER_ONLY:
                        results = client.search()
                                .forResource(Patient.class)
                                .where(Patient.TELECOM.exactly().systemAndIdentifier("phone", sqd.getPhone()))
                                .returnBundle(Bundle.class)
                                .execute();
                        break;
                    default:

                }
                // Check if results are as expected (e.g., non-empty)
                if (results.getEntry().isEmpty()) {
                    status = "failure"; // No results found, consider this a failure if that's unexpected
                }

                // Convert the results to Client objects
                for (Bundle.BundleEntryComponent entry : results.getEntry()) {
                    Patient patient = (Patient) entry.getResource();
                    Client clientObj = convertFromFhirPatient(patient);
                    clients.add(clientObj);
                }
            } catch (Exception e) {
                status = "failure"; // An exception occurred, so mark the operation as a failure
                // Optionally log the exception
            }

//            AuditEvent auditEvent = new AuditEvent();
//            auditEvent.setStatus(status);
//            auditEvent.setAuditEventAction("read"); // Set the action (e.g., read, create, update)
//            auditEvent.setAuditEventTimestamp(new Date()); // Set the timestamp
//            auditEvent.setAgent("Practitioner/123"); // Set the agent (who performed the action)
//            auditEvent.setEntity("Patient/" + sqd); // Set the entity (what was acted upon)
//
//// Serialize the results to a string (e.g., JSON) if you want to store the full details
//// You can use a library like Jackson or Gson to handle the serialization
////            String fullAuditEventJson = serializeResultsToJson(results);
////            auditEvent.setFullAuditEvent(fullAuditEventJson);
//
//            auditEventFacade.create(auditEvent);
            return clients;
        }, executorService);
    }

    private Bundle searchByIdentifier(String system, String identifier, IGenericClient client) {
        System.out.println("client = " + client);
        System.out.println("system = " + system);
        System.out.println("identifier = " + identifier);
        return client.search()
                .forResource(Patient.class)
                .where(Patient.IDENTIFIER.exactly().systemAndIdentifier(system, identifier))
                .returnBundle(Bundle.class)
                .execute();
    }

    private String serializeResultsToJson(Bundle results) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(results);
        } catch (Exception e) {
            // Handle the exception as appropriate for your application
            e.printStackTrace();
            return null;
        }
    }

    public Client convertFromFhirPatient(Patient patient) {
        Client client = new Client();
        Person person = new Person(); // Assuming Person is a class you can instantiate

        // Name
        if (patient.getName() != null && !patient.getName().isEmpty()) {
            String nameText = patient.getName().get(0).getText();
            if (nameText != null) {
                person.setName(nameText);
            }
        }

        // Date of Birth
        person.setDateOfBirth(patient.getBirthDate());

        // Gender
        if (patient.getGender() != null) {
            System.out.println("patient.getGender() = " + patient.getGender());
            System.out.println("patient.getGender().toCode() = " + patient.getGender().toCode());
            // Assuming you have a way to map the FHIR gender to your custom Sex object
            Item sex = itemApplicationController.getSex(patient.getGender().toCode());
            System.out.println("sex = " + sex);
            person.setSex(sex);
        }

        // Identifiers
        for (Identifier identifier : patient.getIdentifier()) {
            if ("https://fhir.health.gov.lk/id/phn".equals(identifier.getSystem())) {
                client.setPhn(identifier.getValue());
            } else if ("https://fhir.health.gov.lk/id/nic".equals(identifier.getSystem())) {
                person.setNic(identifier.getValue());
            } else if ("https://fhir.health.gov.lk/id/ppn".equals(identifier.getSystem())) {
                person.setPassportNumber(identifier.getValue());
            } else if ("https://fhir.health.gov.lk/id/scn".equals(identifier.getSystem())) {
                person.setSsNumber(identifier.getValue());
            } else if ("https://fhir.health.gov.lk/id/dl".equals(identifier.getSystem())) {
                person.setDrivingLicenseNumber(identifier.getValue());
            }
            // Handle other identifiers as needed
        }

        // Address
        if (patient.getAddressFirstRep() != null) {
            Address address = patient.getAddressFirstRep();

            // Set the address line
            person.setAddress(address.getLine().toString());

            // Set the GN Area
            Area gnArea = areaApplicationController.getAreaByName(address.getCity(), AreaType.GN);
            person.setGnArea(gnArea);

            // Handle the MOH Area extension
//            Extension mohExtension = address.getExtensionByUrl("http://fhir.health.gov.lk/StructureDefinition/lk-core-moh-area-ex");
//            if (mohExtension != null) {
//                CodeableConcept mohCodeableConcept = (CodeableConcept) mohExtension.getValue();
//                // Assuming you have a way to map the MOH CodeableConcept to your custom MOH Area object
//                MohArea mohArea = convertMohCodeableConceptToMohArea(mohCodeableConcept);
//                person.setMohArea(mohArea);
//            }
        }

        // Contact Information
        for (ContactPoint contact : patient.getTelecom()) {
            if (ContactPointSystem.PHONE.equals(contact.getSystem())) {
                if (ContactPointUse.MOBILE.equals(contact.getUse())) {
                    person.setPhone1(contact.getValue());
                } else if (ContactPointUse.HOME.equals(contact.getUse())) {
                    person.setPhone2(contact.getValue());
                }
            } else if (ContactPointSystem.EMAIL.equals(contact.getSystem())) {
                person.setEmail(contact.getValue());
            }
        }

        client.setPerson(person);

        return client;
    }

    public Patient convertToFhirPatient(Client client) {
        Patient patient = new Patient();
        Person person = client.getPerson();

        // Identifiers
        if (client.getPhn() != null) {
            patient.addIdentifier(new Identifier().setSystem("https://fhir.health.gov.lk/id/phn").setValue(client.getPhn()));
        }
        if (person.getNic() != null) {
            patient.addIdentifier(new Identifier().setSystem("https://fhir.health.gov.lk/id/nic").setValue(person.getNic()));
        }
        if (person.getPassportNumber() != null) {
            patient.addIdentifier(new Identifier().setSystem("https://fhir.health.gov.lk/id/ppn").setValue(person.getPassportNumber()));
        }
        if (person.getSsNumber() != null) {
            patient.addIdentifier(new Identifier().setSystem("https://fhir.health.gov.lk/id/scn").setValue(person.getSsNumber()));
        }
        if (person.getDrivingLicenseNumber() != null) {
            patient.addIdentifier(new Identifier().setSystem("https://fhir.health.gov.lk/id/dl").setValue(person.getDrivingLicenseNumber()));
        }
        // Add other identifiers as needed

        // Name
        if (person.getName() != null) {
            HumanName name = new HumanName().setText(person.getName());
            patient.addName(name);
        }

        // Gender
        if (person.getSex() != null && person.getSex().getName() != null) {
            patient.setGender(AdministrativeGender.fromCode(person.getSex().getName().toLowerCase()));
        }

        // Date of Birth
        patient.setBirthDate(person.getDateOfBirth());

        // Address
        if (person.getAddress() != null) {
            Address address = new Address();
            address.addLine(person.getAddress());
            if (person.getGnArea() != null) {
                address.setCity(person.getGnArea().getName());
            }
            patient.addAddress(address);
        }

        // Contact Information (assuming phone1 is mobile and phone2 is resident phone number)
        if (person.getPhone1() != null || person.getPhone2() != null || person.getEmail() != null) {
            ContactPoint mobileContact = new ContactPoint().setSystem(ContactPointSystem.PHONE).setUse(ContactPointUse.MOBILE).setValue(person.getPhone1());
            ContactPoint homeContact = new ContactPoint().setSystem(ContactPointSystem.PHONE).setUse(ContactPointUse.HOME).setValue(person.getPhone2());
            ContactPoint emailContact = new ContactPoint().setSystem(ContactPointSystem.EMAIL).setValue(person.getEmail());
            patient.addTelecom(mobileContact);
            patient.addTelecom(homeContact);
            patient.addTelecom(emailContact);
        }

        return patient;
    }

}
