package lk.gov.health.phsp.bean;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import ca.uhn.fhir.rest.client.impl.GenericClient;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.instance.model.api.IIdType;
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
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.SessionScoped;
import lk.gov.health.phsp.entity.ClientEncounterComponentForm;
import lk.gov.health.phsp.entity.ClientEncounterComponentFormSet;
import lk.gov.health.phsp.entity.ClientEncounterComponentItem;
import lk.gov.health.phsp.entity.FhirResourceLink;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.facade.FhirResourceLinkFacade;
import lk.gov.health.phsp.pojcs.SearchQueryData;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Encounter.EncounterStatus;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.hl7.fhir.r4.model.StringType;
import org.json.JSONArray;
import org.json.JSONObject;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.EncodingEnum;
import org.hl7.fhir.r4.model.Bundle;

/**
 *
 * @author buddh
 */
@Named
@SessionScoped
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
    @Inject
    ClientEncounterComponentItemController clientEncounterComponentItemController;

    /**
     * Creates a new instance of FhirR5Controller
     */
    public FhirR4Controller() {
    }

    public FhirOperationResult createPatientInFhirServer(Client client, IntegrationEndpoint endPoint) {
        System.out.println("Creating patient in FHIR server...");
        SecurityProtocol sp = endPoint.getSecurityProtocol();
        String username = endPoint.getUserName();
        String password = endPoint.getPassword();
        Patient patient = convertToFhirPatient(client);

        FhirOperationResult result = new FhirOperationResult();
        FhirContext ctx = FhirContext.forR4();
        String serverBase = endPoint.getEndPointUrl();
        IGenericClient fhirClient = ctx.newRestfulGenericClient(serverBase);

        // Log serialized patient JSON
        String serializedPatient = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(patient);
        System.out.println("Serialized Patient JSON: " + serializedPatient);

        // Custom interceptor for logging request details
        fhirClient.registerInterceptor(new IClientInterceptor() {
            @Override
            public void interceptRequest(IHttpRequest theRequest) {
                System.out.println("Request URL: " + theRequest.getUri());
                // This approach does not directly expose method and headers for logging here due to API constraints.
            }

            @Override
            public void interceptResponse(IHttpResponse theResponse) {
                // Optionally handle the response
            }
        });

        // Basic Authentication
        if (sp == SecurityProtocol.BASIC_AUTHENTICATION) {
            fhirClient.registerInterceptor(new BasicAuthInterceptor(username, password));
        } // Keycloak Authentication
        else if (sp == SecurityProtocol.KEYCLOAK) {
            String token = acquireToken(endPoint.getKeyCloackClientId(), endPoint.getKeyCloackClientSecret(), endPoint.getKeyCloakTokenAcquiringUrl());
            fhirClient.registerInterceptor(new BearerTokenAuthInterceptor(token));
        } // API Key Authentication
        else if (sp == SecurityProtocol.API_KEY) {
            fhirClient.registerInterceptor(new IClientInterceptor() {
                @Override
                public void interceptRequest(IHttpRequest theRequest) {
                    theRequest.removeHeaders("Accept");
                    theRequest.addHeader("Accept", "*/*");
                    theRequest.addHeader(endPoint.getApiKeyName(), endPoint.getApiKeyValue());
                }

                @Override
                public void interceptResponse(IHttpResponse theResponse) {
                    // Optionally handle the response
                }
            });
        }

        // Execute the create operation
        try {
            MethodOutcome outcome = fhirClient.create().resource(patient).execute();
            System.out.println("Request sent to: " + serverBase); // Log the server base URL

            if (outcome.getCreated()) {
                IdType id = (IdType) outcome.getId();
                result.setSuccess(true);
                result.setMessage("Created new Patient with ID: " + id.getIdPart());
                result.setResourceId(id);
                System.out.println("Created new Patient with ID: " + id.getIdPart());
            } else {
                result.setSuccess(false);
                result.setMessage("Failed to create new Patient");
            }
        } catch (Exception e) {
            System.err.println("Error during FHIR operation: " + e.getMessage());
            e.printStackTrace();
            result.setSuccess(false);
            result.setMessage("Exception occurred: " + e.getMessage());
        }

        return result;
    }

    public Patient getPatientFromFhirServer(String patientIdentifier, IntegrationEndpoint endPoint) {
        SecurityProtocol sp = endPoint.getSecurityProtocol();
        String username = endPoint.getUserName();
        String password = endPoint.getPassword();

        FhirContext ctx = FhirContext.forR4();
        String serverBase = endPoint.getEndPointUrl();
        IGenericClient fhirClient = ctx.newRestfulGenericClient(serverBase);
        fhirClient.setEncoding(EncodingEnum.JSON);

        // Basic Authentication
        if (sp == SecurityProtocol.BASIC_AUTHENTICATION) {
            fhirClient.registerInterceptor(new BasicAuthInterceptor(username, password));
        } // Keycloak Authentication
        else if (sp == SecurityProtocol.KEYCLOAK) {
            String token = acquireToken(endPoint.getKeyCloackClientId(), endPoint.getKeyCloackClientSecret(), endPoint.getKeyCloakTokenAcquiringUrl());
            fhirClient.registerInterceptor(new BearerTokenAuthInterceptor(token));
        } // API Key Authentication
        else if (sp == SecurityProtocol.API_KEY) {
            fhirClient.registerInterceptor(new IClientInterceptor() {
                @Override
                public void interceptRequest(IHttpRequest theRequest) {
                    theRequest.removeHeaders("Accept");
                    theRequest.addHeader("Accept", "*/*");
                    theRequest.addHeader(endPoint.getApiKeyName(), endPoint.getApiKeyValue());
                }

                @Override
                public void interceptResponse(IHttpResponse theResponse) {
                    // Optionally handle the response if needed
                }
            });
        }

        try {
            // Print out the constructed URL for debugging purposes
            String constructedUrl = serverBase + "/Patient?identifier=" + patientIdentifier;
            System.out.println("Constructed URL: " + constructedUrl);

            // Retrieve the Patient resource by identifier
            Bundle results = fhirClient
                    .search()
                    .forResource(Patient.class)
                    .where(Patient.IDENTIFIER.exactly().code(patientIdentifier))
                    .returnBundle(Bundle.class)
                    .execute();

            if (!results.getEntry().isEmpty()) {
                // Assuming the first entry is the desired patient
                return (Patient) results.getEntry().get(0).getResource();
            } else {
                System.out.println("Patient not found.");
                return null; // No patient found
            }
        } catch (Exception e) {
            System.err.println("Error retrieving patient: " + e.getMessage());
            e.printStackTrace();
            return null; // An error occurred
        }
    }

    public FhirOperationResult createResourcesInFhirServer(Bundle bundle, IntegrationEndpoint endPoint) {
        SecurityProtocol sp = endPoint.getSecurityProtocol();
        String username = endPoint.getUserName();
        String password = endPoint.getPassword();

        FhirOperationResult result = new FhirOperationResult();
        FhirContext ctx = FhirContext.forR4();
        String serverBase = endPoint.getEndPointUrl();
        IGenericClient fhirClient = ctx.newRestfulGenericClient(serverBase);
        fhirClient.setEncoding(EncodingEnum.JSON);

        // Handle Basic Authentication and Keycloak (Bearer Token) Authentication
        if (sp == SecurityProtocol.BASIC_AUTHENTICATION) {
            fhirClient.registerInterceptor(new BasicAuthInterceptor(username, password));
        } else if (sp == SecurityProtocol.KEYCLOAK) {
            String token = acquireToken(endPoint.getKeyCloackClientId(), endPoint.getKeyCloackClientSecret(), endPoint.getKeyCloakTokenAcquiringUrl());
            fhirClient.registerInterceptor(new BearerTokenAuthInterceptor(token));
        } else if (sp == SecurityProtocol.API_KEY) {
            fhirClient.registerInterceptor(new IClientInterceptor() {
                @Override
                public void interceptRequest(IHttpRequest theRequest) {
                    theRequest.removeHeaders("Accept");
                    theRequest.addHeader("Accept", "*/*");
                    if (sp == SecurityProtocol.API_KEY) {
                        theRequest.addHeader(endPoint.getApiKeyName(), endPoint.getApiKeyValue());
                    }
                }
                @Override
                public void interceptResponse(IHttpResponse theResponse) {
                }
            });
        }

        // Serialize and log the bundle for debugging purposes
        String serializedBundle = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
        System.out.println("Serialized Bundle JSON: " + serializedBundle);

        // Execute the transaction operation with the bundle
        try {
            Bundle responseBundle = fhirClient.transaction().withBundle(bundle).execute();
            System.out.println("Transaction executed successfully.");

            // Log the status of each operation in the transaction
            responseBundle.getEntry().forEach(entry -> System.out.println("Response: " + entry.getResponse().getStatus()));

            result.setSuccess(true);
            result.setMessage("Bundle processed by FHIR server.");
        } catch (Exception e) {
            System.err.println("Error during FHIR operation: " + e.getMessage());
            e.printStackTrace();

            result.setSuccess(false);
            result.setMessage("Exception occurred: " + e.getMessage());
        }

        return result;
    }

    public Bundle convertJsonToBundle(String jsonBundle) {
        FhirContext ctx = FhirContext.forR4();
        IParser parser = ctx.newJsonParser();

        // Assuming the JSON string is a valid FHIR Bundle representation
        Bundle bundle = parser.parseResource(Bundle.class, jsonBundle);

        return bundle;
    }

    public FhirOperationResult updateServiceRequestInFhirServer(ServiceRequest serviceRequest, IntegrationEndpoint endPoint) {
        System.out.println("Updating ServiceRequest in FHIR server...");
        SecurityProtocol sp = endPoint.getSecurityProtocol();
        String username = endPoint.getUserName();
        String password = endPoint.getPassword();

        FhirOperationResult result = new FhirOperationResult();

        FhirContext ctx = FhirContext.forR4();
        String serverBase = endPoint.getEndPointUrl();
        IGenericClient fhirClient = ctx.newRestfulGenericClient(serverBase);

        // Setup the authentication
        if (sp == SecurityProtocol.BASIC_AUTHENTICATION) {
            fhirClient.registerInterceptor(new BasicAuthInterceptor(username, password));
        } else if (sp == SecurityProtocol.KEYCLOAK) {
            String token = acquireToken(endPoint.getKeyCloackClientId(), endPoint.getKeyCloackClientSecret(), endPoint.getKeyCloakTokenAcquiringUrl());
            BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(token);
            fhirClient.registerInterceptor(authInterceptor);
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
                    // No action needed on the response for now
                }
            });
        }

        // Perform the update operation
        MethodOutcome outcome = fhirClient.update().resource(serviceRequest).execute();

        if (outcome != null && outcome.getId() != null) {
            IdType id = (IdType) outcome.getId();
            result.setSuccess(true);
            result.setMessage("ServiceRequest updated successfully with ID: " + id.getIdPart());
            result.setResourceId(id);

            // Here you might want to link the updated ServiceRequest with your local data, if needed
            // updateFhirResourceLink(serviceRequest, endPoint, id.getIdPart());
        } else {
            result.setSuccess(false);
            result.setMessage("Failed to update ServiceRequest");
        }
        return result;
    }

    // Modified by Dr M H B Ariyaratne with assistance from ChatGPT from OpenAI
    public FhirOperationResult createPatientInFhirServer1(Client client, IntegrationEndpoint endPoint) {
        System.out.println("Creating patient in FHIR server...");
        SecurityProtocol sp = endPoint.getSecurityProtocol();
        String username = endPoint.getUserName();
        String password = endPoint.getPassword();
        Patient patient = convertToFhirPatient(client);

        FhirOperationResult result = new FhirOperationResult();

        FhirContext ctx = FhirContext.forR4();
        String serverBase = endPoint.getEndPointUrl();
        IGenericClient fhirClient = ctx.newRestfulGenericClient(serverBase);

        if (sp == SecurityProtocol.BASIC_AUTHENTICATION) {
            fhirClient.registerInterceptor(new BasicAuthInterceptor(username, password));
        } else if (endPoint.getSecurityProtocol() == SecurityProtocol.KEYCLOAK) {
            String token = acquireToken(endPoint.getKeyCloackClientId(), endPoint.getKeyCloackClientSecret(), endPoint.getKeyCloakTokenAcquiringUrl());
            BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(token);
            fhirClient.registerInterceptor(authInterceptor);
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
    }

    public CompletableFuture<FhirOperationResult> createOrganizationInFhirServerAsync(Institution institution, IntegrationEndpoint endPoint) {
        System.out.println("Creating patient in FHIR server...");
        SecurityProtocol sp = endPoint.getSecurityProtocol();
        String username = endPoint.getUserName();
        String password = endPoint.getPassword();
        Organization organization = convertToFhirOrganization(institution);

        return CompletableFuture.supplyAsync(() -> {
            FhirOperationResult result = new FhirOperationResult();

            FhirContext ctx = FhirContext.forR4();
            String serverBase = endPoint.getEndPointUrl();
            IGenericClient fhirClient = ctx.newRestfulGenericClient(serverBase);

            if (sp == SecurityProtocol.BASIC_AUTHENTICATION) {
                fhirClient.registerInterceptor(new BasicAuthInterceptor(username, password));
            } else if (endPoint.getSecurityProtocol() == SecurityProtocol.KEYCLOAK) {
                String token = acquireToken(endPoint.getKeyCloackClientId(), endPoint.getKeyCloackClientSecret(), endPoint.getKeyCloakTokenAcquiringUrl());
                BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(token);
                fhirClient.registerInterceptor(authInterceptor);
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

            MethodOutcome outcome = fhirClient.create().resource(organization).execute();

            if (outcome.getCreated()) {
                IdType id = (IdType) outcome.getId();
                result.setSuccess(true);
                result.setMessage("Created new Patient with ID: " + id.getIdPart());
                result.setResourceId(id);

                // Call updateFhirResourceLink method
                updateFhirResourceLink(institution, endPoint, id.getIdPart());

            } else {
                result.setSuccess(false);
                result.setMessage("Failed to create new Patient");
            }
            return result;
        });
    }

    // Modified by Dr M H B Ariyaratne with assistance from ChatGPT from OpenAI.
    public List<FhirOperationResult> createOrganizationsInFhirServer(List<Institution> institutions, IntegrationEndpoint endPoint) {
        System.out.println("Creating organizations in FHIR server...");

        List<FhirOperationResult> results = new ArrayList<>();

        SecurityProtocol sp = endPoint.getSecurityProtocol();
        String username = endPoint.getUserName();
        String password = endPoint.getPassword();

        FhirContext ctx = FhirContext.forR4();
        String serverBase = endPoint.getEndPointUrl();
        IGenericClient fhirClient = ctx.newRestfulGenericClient(serverBase);

        if (sp == SecurityProtocol.BASIC_AUTHENTICATION) {
            fhirClient.registerInterceptor(new BasicAuthInterceptor(username, password));
        } else if (sp == SecurityProtocol.KEYCLOAK) {
            String token = acquireToken(endPoint.getKeyCloackClientId(), endPoint.getKeyCloackClientSecret(), endPoint.getKeyCloakTokenAcquiringUrl());
            BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(token);
            fhirClient.registerInterceptor(authInterceptor);
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
                    // Optional response handling
                }
            });
        }

        // Create a new bundle to hold the resources
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.TRANSACTION);

        for (Institution institution : institutions) {
            Organization organization = convertToFhirOrganization(institution);

            // Add each organization to the bundle as an entry
            Bundle.BundleEntryComponent entry = new Bundle.BundleEntryComponent();
            entry.setResource(organization);
            entry.getRequest()
                    .setMethod(Bundle.HTTPVerb.PUT)
                    .setUrl("Organization/" + organization.getId());
            bundle.addEntry(entry);
        }

        // Send the entire bundle to the FHIR server
        Bundle response = fhirClient.transaction().withBundle(bundle).execute();

        // Process the response to extract the results
        for (Bundle.BundleEntryComponent respEntry : response.getEntry()) {
            FhirOperationResult result = new FhirOperationResult();
            if (respEntry.getResponse().getStatus().startsWith("201")) {
                result.setSuccess(true);
                result.setMessage("Successfully created Organization with ID: " + respEntry.getResource().getIdElement().getIdPart());
                result.setResourceId(respEntry.getResource().getIdElement());
            } else {
                result.setSuccess(false);
                result.setMessage("Failed to create new Organization. Status: " + respEntry.getResponse().getStatus());
            }
            results.add(result);
        }

        return results;
    }

    // Modified by Dr M H B Ariyaratne with assistance from ChatGPT from OpenAI.
    public FhirOperationResult createOrganizationInFhirServer(Institution institution, IntegrationEndpoint endPoint) {
        System.out.println("Creating organization in FHIR server...");
        SecurityProtocol sp = endPoint.getSecurityProtocol();
        String username = endPoint.getUserName();
        String password = endPoint.getPassword();
        Organization organization = convertToFhirOrganization(institution);

        FhirOperationResult result = new FhirOperationResult();

        FhirContext ctx = FhirContext.forR4();
        String serverBase = endPoint.getEndPointUrl();
        IGenericClient fhirClient = ctx.newRestfulGenericClient(serverBase);

        if (sp == SecurityProtocol.BASIC_AUTHENTICATION) {
            fhirClient.registerInterceptor(new BasicAuthInterceptor(username, password));
        } else if (sp == SecurityProtocol.KEYCLOAK) {
            String token = acquireToken(endPoint.getKeyCloackClientId(), endPoint.getKeyCloackClientSecret(), endPoint.getKeyCloakTokenAcquiringUrl());
            BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(token);
            fhirClient.registerInterceptor(authInterceptor);
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
                    // Optional response handling
                }
            });
        }

        MethodOutcome outcome = fhirClient.create().resource(organization).execute();

        if (outcome.getCreated()) {
            IdType id = (IdType) outcome.getId();
            result.setSuccess(true);
            result.setMessage("Created new Organization with ID: " + id.getIdPart());
            result.setResourceId(id);

            // Call updateFhirResourceLink method
            updateFhirResourceLink(institution, endPoint, id.getIdPart());

        } else {
            result.setSuccess(false);
            result.setMessage("Failed to create new Organization");
        }

        return result;
    }

    // Modified by Dr M H B Ariyaratne with assistance from ChatGPT from OpenAI.
    public FhirOperationResult sendJsonPayloadToFhirR4(String jsonPayload, IntegrationEndpoint endPoint) {
        System.out.println("Sending JSON payload to FHIR R4 server...");

        SecurityProtocol sp = endPoint.getSecurityProtocol();
        String username = endPoint.getUserName();
        String password = endPoint.getPassword();

        FhirOperationResult result = new FhirOperationResult();

        FhirContext ctx = FhirContext.forR4();
        String serverBase = endPoint.getEndPointUrl();
        IGenericClient fhirClient = ctx.newRestfulGenericClient(serverBase);

        if (sp == SecurityProtocol.BASIC_AUTHENTICATION) {
            fhirClient.registerInterceptor(new BasicAuthInterceptor(username, password));
        } else if (sp == SecurityProtocol.KEYCLOAK) {
            String token = acquireToken(endPoint.getKeyCloackClientId(), endPoint.getKeyCloackClientSecret(), endPoint.getKeyCloakTokenAcquiringUrl());
            BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(token);
            fhirClient.registerInterceptor(authInterceptor);
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
                    // Optional response handling
                }
            });
        }

        // Convert the JSON payload string into a Bundle object
        Bundle bundle = (Bundle) ctx.newJsonParser().parseResource(jsonPayload);

        // Execute the transaction
        Bundle response = fhirClient.transaction().withBundle(bundle).execute();

        // Check the response to set the result (This is a basic check. You might need to adjust based on your needs)
        if (response != null) {
            result.setSuccess(true);
            result.setMessage("Successfully processed the bundle transaction");
        } else {
            result.setSuccess(false);
            result.setMessage("Failed to process the bundle transaction");
        }

        return result;
    }

    // Created by Dr M H B Ariyaratne with assistance from ChatGPT from OpenAI
    public FhirOperationResult createFormsetInFhirServer(ClientEncounterComponentFormSet cecfs, IntegrationEndpoint endPoint) {
        System.out.println("createFormsetInFhirServer...");
        System.out.println("cecfs = " + cecfs);
        FhirOperationResult result = new FhirOperationResult();

        SecurityProtocol sp = endPoint.getSecurityProtocol();
        String username = endPoint.getUserName();
        String password = endPoint.getPassword();
        Bundle bundle = convertToFhirBundle(cecfs);
        System.out.println("bundle.toString() = " + bundle.toString());

        FhirContext ctx = FhirContext.forR4();
        // Modified by Dr M H B Ariyaratne with assistance from ChatGPT from OpenAI
        String json = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
        System.err.println("FHIR JSON: " + json);

        String serverBase = endPoint.getEndPointUrl();
        IGenericClient fhirClient = ctx.newRestfulGenericClient(serverBase);

        if (sp == SecurityProtocol.BASIC_AUTHENTICATION) {
            fhirClient.registerInterceptor(new BasicAuthInterceptor(username, password));
        } else if (endPoint.getSecurityProtocol() == SecurityProtocol.KEYCLOAK) {
            String token = acquireToken(endPoint.getKeyCloackClientId(), endPoint.getKeyCloackClientSecret(), endPoint.getKeyCloakTokenAcquiringUrl());
            BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(token);
            fhirClient.registerInterceptor(authInterceptor);
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
                    // Optional: handle response
                }
            });
        }
        // Add other authentication methods as needed

        MethodOutcome outcome = fhirClient.create().resource(bundle).execute();

        if (outcome.getCreated()) {
            IdType id = (IdType) outcome.getId();
            result.setSuccess(true);
            result.setMessage("Created new Patient with ID: " + id.getIdPart());
            result.setResourceId(id);
            updateFhirResourceLink(cecfs, endPoint, id.getIdPart());
        } else {
            result.setSuccess(false);
            result.setMessage("Failed to create new Patient");
        }
        return result;
    }

    public FhirOperationResult updateFormsetInFhirServerAsync(ClientEncounterComponentFormSet cecfs, IntegrationEndpoint endPoint, String oldId) {
        System.out.println("createFormsetInFhirServer...");
        System.out.println("cecfs = " + cecfs);
        FhirOperationResult result = new FhirOperationResult();

        SecurityProtocol sp = endPoint.getSecurityProtocol();
        String username = endPoint.getUserName();
        String password = endPoint.getPassword();
        Bundle bundle = convertToFhirBundle(cecfs);
        System.out.println("bundle.toString() = " + bundle.toString());

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
                    // Optional: handle response
                }
            });
        }
        // Add other authentication methods as needed

        MethodOutcome outcome = fhirClient.create().resource(bundle).execute();

        if (outcome.getCreated()) {
            IdType id = (IdType) outcome.getId();
            result.setSuccess(true);
            result.setMessage("Created new Patient with ID: " + id.getIdPart());
            result.setResourceId(id);
            updateFhirResourceLink(cecfs, endPoint, id.getIdPart());
        } else {
            result.setSuccess(false);
            result.setMessage("Failed to create new Patient");
        }
        return result;
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

    public FhirOperationResult updatePatientInFhirServer(Client client, IntegrationEndpoint endPoint, String resourceId) {
        System.out.println("Updating patient in FHIR server...");
        SecurityProtocol sp = endPoint.getSecurityProtocol();
        String username = endPoint.getUserName();
        String password = endPoint.getPassword();
        Patient patient = convertToFhirPatient(client);

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
    }

    // Modified by Dr M H B Ariyaratne with assistance from ChatGPT from OpenAI.
    public CompletableFuture<FhirOperationResult> updateOrganizationInFhirServerAsync(Institution institution, IntegrationEndpoint endPoint, String resourceId) {
        System.out.println("Updating organization in FHIR server...");
        SecurityProtocol sp = endPoint.getSecurityProtocol();
        String username = endPoint.getUserName();
        String password = endPoint.getPassword();
        Organization organization = convertToFhirOrganization(institution);

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
            organization.setId(resourceId);

            // Perform the update operation
            MethodOutcome outcome = fhirClient.update().resource(organization).execute();

            if (outcome.getCreated()) {
                result.setSuccess(false);
                result.setMessage("Unexpectedly created a new Organization instead of updating");
            } else if (outcome.getResource() != null) {
                IdType id = (IdType) outcome.getId();
                result.setSuccess(true);
                result.setMessage("Updated Organization with ID: " + id.getIdPart());
                result.setResourceId(id);
            } else {
                result.setSuccess(false);
                result.setMessage("Failed to update Organization");
            }
            return result;
        });
    }

    // Modified by Dr M H B Ariyaratne with assistance from ChatGPT from OpenAI.
    public FhirOperationResult updateOrganizationInFhirServer(Institution institution, IntegrationEndpoint endPoint, String resourceId) {
        System.out.println("Updating organization in FHIR server...");
        SecurityProtocol sp = endPoint.getSecurityProtocol();
        String username = endPoint.getUserName();
        String password = endPoint.getPassword();
        Organization organization = convertToFhirOrganization(institution);

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
                    // Optional response handling
                }
            });
        }

        // Set the resource ID for the update operation
        organization.setId(resourceId);

        // Perform the update operation
        MethodOutcome outcome = fhirClient.update().resource(organization).execute();

        if (outcome.getCreated()) {
            result.setSuccess(false);
            result.setMessage("Unexpectedly created a new Organization instead of updating");
        } else if (outcome.getResource() != null) {
            IdType id = (IdType) outcome.getId();
            result.setSuccess(true);
            result.setMessage("Updated Organization with ID: " + id.getIdPart());
            result.setResourceId(id);
        } else {
            result.setSuccess(false);
            result.setMessage("Failed to update Organization");
        }

        return result;
    }

    public static String replacePatientAndEncounterInBundle(String bundleJson, String patientJson, String encounterJson) {
        JSONObject bundleObject = new JSONObject(bundleJson);
        JSONArray entryArray = bundleObject.getJSONArray("entry");

        JSONObject newPatient = new JSONObject(patientJson);
        JSONObject newEncounter = new JSONObject(encounterJson);

        String newPatientId = newPatient.getString("id");
        String newEncounterId = newEncounter.getString("id");

        for (int i = 0; i < entryArray.length(); i++) {
            JSONObject entry = entryArray.getJSONObject(i);
            JSONObject resource = entry.getJSONObject("resource");

            // Replace Patient resource and URIs
            if ("Patient".equals(resource.getString("resourceType"))) {
                entry.put("resource", newPatient);
                entry.put("fullUrl", "Patient/" + newPatientId);
                JSONObject request = entry.getJSONObject("request");
                request.put("url", "Patient/" + newPatientId);
            }

            // Replace Encounter resource and URIs
            if ("Encounter".equals(resource.getString("resourceType"))) {
                entry.put("resource", newEncounter);
                entry.put("fullUrl", "Encounter/" + newEncounterId);
                JSONObject request = entry.getJSONObject("request");
                request.put("url", "Encounter/" + newEncounterId);
            }

            // Update Patient and Encounter references in other resources
            replaceResourceReferences(resource, "Patient", newPatientId);
            replaceResourceReferences(resource, "Encounter", newEncounterId);
        }

        return bundleObject.toString();
    }

// Helper method to replace references in resources
    private static void replaceResourceReferences(JSONObject resource, String resourceType, String newId) {
        for (String key : resource.keySet()) {
            Object value = resource.get(key);
            if (value instanceof JSONObject) {
                JSONObject subObject = (JSONObject) value;
                if (subObject.has("reference") && subObject.getString("reference").startsWith(resourceType + "/")) {
                    subObject.put("reference", resourceType + "/" + newId);
                }
                replaceResourceReferences(subObject, resourceType, newId);
            } else if (value instanceof JSONArray) {
                JSONArray array = (JSONArray) value;
                for (int i = 0; i < array.length(); i++) {
                    Object item = array.get(i);
                    if (item instanceof JSONObject) {
                        replaceResourceReferences((JSONObject) item, resourceType, newId);
                    }
                }
            }
        }
    }

// Modified by Dr M H B Ariyaratne with assistance from ChatGPT from OpenAI.
    public FhirOperationResult postJsonPayloadToFhirServer(String jsonPlayLoad, IntegrationEndpoint endPoint) {
        System.out.println("Sending JSON payload to FHIR server...");
        SecurityProtocol sp = endPoint.getSecurityProtocol();
        String username = endPoint.getUserName();
        String password = endPoint.getPassword();
        FhirOperationResult result = new FhirOperationResult();
        FhirContext ctx = FhirContext.forR4();

        // Disable server validation on the FHIR context
        ctx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);

        String serverBase = endPoint.getEndPointUrl();
        System.out.println("Target Base URL: " + serverBase);
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
                    // Optional response handling
                }
            });
        }

        // Parsing the given JSON payload to a Bundle object
        Bundle bundle = (Bundle) ctx.newJsonParser().parseResource(jsonPlayLoad);

        System.out.println("Attempting to send bundle as a POST request...");
        Bundle response;
        try {
            response = fhirClient.transaction().withBundle(bundle).execute();
            System.out.println("Bundle sent via POST method.");
        } catch (Exception e) {
            System.err.println("Error occurred while sending the bundle: " + e.getMessage());
            e.printStackTrace();
            result.setSuccess(false);
            result.setMessage("Failed to send the bundle: " + e.getMessage());
            return result;
        }

        // Evaluating the response to set the result
        if (response != null && response.getType() == Bundle.BundleType.TRANSACTIONRESPONSE) {
            System.out.println("Response received: " + response.toString());
            result.setSuccess(true);
            result.setMessage("Successfully processed the bundle transaction");
        } else {
            result.setSuccess(false);
            result.setMessage("Failed to process the bundle transaction");
        }

        return result;
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

    public List<Client> fetchClientsFromEndpoints(SearchQueryData sqd, IntegrationEndpoint endPoint) {
        System.out.println("fetchClientsFromEndpoints");
        List<Client> clients = new ArrayList<>();

        // Create a FHIR client
        FhirContext ctx = FhirContext.forR4();
        String serverBase = endPoint.getEndPointUrl(); // Assuming this is the URL of the FHIR server
        IGenericClient client = ctx.newRestfulGenericClient(serverBase);

        String status = "success"; // Assume success by default
        Bundle results = null;
        try {
            System.out.println("sqd.getSearchCriteria() = " + sqd.getSearchCriteria());
            results = performSearchBasedOnCriteria(client, sqd);

            // Convert the results to Client objects
            for (Bundle.BundleEntryComponent entry : results.getEntry()) {
                Patient patient = (Patient) entry.getResource();
                Client clientObj = convertFromFhirPatient(patient);
                clients.add(clientObj);
            }
        } catch (Exception e) {
            status = "failure"; // An exception occurred, so mark the operation as a failure
        }
        return clients;
    }

    public List<ServiceRequest> fetchServiceRequestsFromEndpoints(IntegrationEndpoint endPoint) {
        System.out.println("fetchServiceRequestsFromEndpoints");

        // Create a FHIR context and client
        FhirContext ctx = FhirContext.forR4();
        String serverBase = endPoint.getEndPointUrl();
        IGenericClient client = ctx.newRestfulGenericClient(serverBase);

        // Apply the security configurations to the client
        applySecurity(client, endPoint);

        List<ServiceRequest> serviceRequests = new ArrayList<>();
        String status = "success"; // Assume success by default
        Bundle results;

        try {
            results = searchAllServiceRequests(client);
            for (Bundle.BundleEntryComponent entry : results.getEntry()) {
                ServiceRequest sr = (ServiceRequest) entry.getResource();
                serviceRequests.add(sr);
            }
        } catch (Exception e) {
            status = "failure"; // An exception occurred, so mark the operation as a failure
            System.out.println("Error fetching ServiceRequests: " + e.getMessage());
        }

        return serviceRequests;
    }

    private void applySecurity(IGenericClient client, IntegrationEndpoint endPoint) {
        SecurityProtocol sp = endPoint.getSecurityProtocol();
        String username = endPoint.getUserName();
        String password = endPoint.getPassword();

        if (sp == SecurityProtocol.BASIC_AUTHENTICATION) {
            client.registerInterceptor(new BasicAuthInterceptor(username, password));
        } else if (endPoint.getSecurityProtocol() == SecurityProtocol.KEYCLOAK) {
            String token = acquireToken(endPoint.getKeyCloackClientId(), endPoint.getKeyCloackClientSecret(), endPoint.getKeyCloakTokenAcquiringUrl());
            BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(token);
            client.registerInterceptor(authInterceptor);
        } else if (sp == SecurityProtocol.API_KEY) {
            String apiKeyName = endPoint.getApiKeyName();
            String apiKeyValue = endPoint.getApiKeyValue();
            client.registerInterceptor(new IClientInterceptor() {
                @Override
                public void interceptRequest(IHttpRequest theRequest) {
                    theRequest.addHeader(apiKeyName, apiKeyValue);
                }

                @Override
                public void interceptResponse(IHttpResponse theResponse) {
                    // Response handling can be implemented here if needed
                }
            });
        }
        // Add other security protocols as needed
    }

    private Bundle performSearchBasedOnCriteria(IGenericClient client, SearchQueryData sqd) {
        Bundle results = null;
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
                // Handle default case or throw an exception if the search criteria is unrecognized
                throw new IllegalArgumentException("Invalid search criteria");
        }
        return results;
    }

    public CompletableFuture<List<Client>> fetchClientsFromEndpoints1(SearchQueryData sqd, IntegrationEndpoint endPoint) {
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
            } else if (endPoint.getSecurityProtocol() == SecurityProtocol.KEYCLOAK) {
                String token = acquireToken(endPoint.getKeyCloackClientId(), endPoint.getKeyCloackClientSecret(), endPoint.getKeyCloakTokenAcquiringUrl());
                BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(token);
                client.registerInterceptor(authInterceptor);
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
            }
            return clients;
        }, executorService);
    }

    public String acquireToken(String clientId, String clientSecret, String tokenUrl) {
        System.out.println("acquireToken");
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(tokenUrl);
        System.out.println("tokenUrl = " + tokenUrl);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("grant_type", "client_credentials"));
        params.add(new BasicNameValuePair("client_id", clientId));
        System.out.println("clientId = " + clientId);
        params.add(new BasicNameValuePair("client_secret", clientSecret));
        System.out.println("clientSecret = " + clientSecret);
        CloseableHttpResponse response = null;
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(params));
            response = httpClient.execute(httpPost);
            String responseBody = EntityUtils.toString(response.getEntity());

            // Use Jackson to parse the JSON response
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            if (jsonNode.hasNonNull("access_token")) {
                String accessToken = jsonNode.get("access_token").asText();
                System.out.println("accessToken = " + accessToken);
                return accessToken;
            } else {
                System.out.println("Error: The JSON response does not contain an 'access_token' field.");
                return null;
            }
        } catch (IOException e) {
            System.out.println("Error acquiring token: " + e.getMessage());
            e.printStackTrace(); // This prints the full stack trace, including the line number where the exception occurred.
            return null; // or you can return an empty string, depending on how you want to handle it in calling methods
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
                httpClient.close();
            } catch (IOException ex) {
                System.out.println("Error closing resources: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    public CompletableFuture<List<Client>> fetchClientsFromEndpoints2(SearchQueryData sqd, IntegrationEndpoint endPoint) {
        return CompletableFuture.supplyAsync(() -> {
            List<Client> clients = new ArrayList<>();
            FhirContext ctx = FhirContext.forR4();
            String serverBase = endPoint.getEndPointUrl(); // Assuming this is the URL of the FHIR server
            IGenericClient client = ctx.newRestfulGenericClient(serverBase);
            if (endPoint.getSecurityProtocol() == SecurityProtocol.BASIC_AUTHENTICATION) {
                String username = endPoint.getUserName();
                String password = endPoint.getPassword();
                client.registerInterceptor(new BasicAuthInterceptor(username, password));
            } else if (endPoint.getSecurityProtocol() == SecurityProtocol.KEYCLOAK) {
                String token = acquireToken(endPoint.getKeyCloackClientId(), endPoint.getKeyCloackClientSecret(), endPoint.getKeyCloakTokenAcquiringUrl());
                BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(token);
                client.registerInterceptor(authInterceptor);
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
                results = searchByIdentifier("https://fhir.health.gov.lk/id/nic", sqd.getNic(), client);
                if (results.getEntry().isEmpty()) {
                    status = "failure"; // No results found, consider this a failure if that's unexpected
                }
                for (Bundle.BundleEntryComponent entry : results.getEntry()) {
                    Patient patient = (Patient) entry.getResource();
                    Client clientObj = convertFromFhirPatient(patient);
                    clients.add(clientObj);
                }
            } catch (Exception e) {
                status = "failure";
            }
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

    private Bundle searchAllServiceRequests(IGenericClient client) {
        System.out.println("client = " + client);

        return client.search()
                .forResource(ServiceRequest.class)
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
        if (person.getDateOfBirth() != null) {
            SimpleDateFormat fhirDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDateOfBirth = fhirDateFormat.format(person.getDateOfBirth());
            try {
                patient.setBirthDate(fhirDateFormat.parse(formattedDateOfBirth));
            } catch (ParseException e) {
                e.printStackTrace();
                // Handle the error appropriately
            }
        }

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

    // Newly created by Dr M H B Ariyaratne with assistance from ChatGPT from OpenAI.
    public Organization convertToFhirOrganization(Institution institution) {
        Organization organization = new Organization();

        // Identifiers
        if (institution.getCode() != null) {
            organization.addIdentifier(new Identifier().setSystem("https://fhir.health.gov.lk/id/institution-code").setValue(institution.getCode()));
        }
        if (institution.getPoiNumber() != null) {
            organization.addIdentifier(new Identifier().setSystem("https://fhir.health.gov.lk/id/institution-poi").setValue(institution.getPoiNumber()));
        }

        // Name
        if (institution.getName() != null) {
            organization.setName(institution.getName());
        }

        // Type
        if (institution.getInstitutionType() != null) {
            CodeableConcept typeConcept = new CodeableConcept();
            typeConcept.addCoding(new Coding().setCode(institution.getInstitutionType().name()));
            organization.addType(typeConcept);
        }

        // Address
        if (institution.getAddress() != null) {
            Address address = new Address();
            address.addLine(institution.getAddress());
            if (institution.getGnArea() != null) {
                address.setCity(institution.getGnArea().getName());
                address.setDistrict(institution.getDsDivision().getName());
                address.setState(institution.getDistrict().getName());
                address.setCountry(institution.getProvince().getName());
            }
            organization.addAddress(address);
        }

        // Contact Information
        if (institution.getPhone() != null || institution.getMobile() != null || institution.getEmail() != null || institution.getFax() != null) {
            ContactPoint phoneContact = new ContactPoint().setSystem(ContactPointSystem.PHONE).setValue(institution.getPhone());
            ContactPoint mobileContact = new ContactPoint().setSystem(ContactPointSystem.PHONE).setUse(ContactPointUse.MOBILE).setValue(institution.getMobile());
            ContactPoint faxContact = new ContactPoint().setSystem(ContactPointSystem.FAX).setValue(institution.getFax());
            ContactPoint emailContact = new ContactPoint().setSystem(ContactPointSystem.EMAIL).setValue(institution.getEmail());
            organization.addTelecom(phoneContact);
            organization.addTelecom(mobileContact);
            organization.addTelecom(faxContact);
            organization.addTelecom(emailContact);
        }

        // Web
        if (institution.getWeb() != null) {
            organization.addEndpoint(new Reference().setReference(institution.getWeb()));
        }

        // Parent Institution
        if (institution.getParent() != null) {
            organization.setPartOf(new Reference().setReference("Organization/" + institution.getParent().getId()));
        }

        return organization;
    }

    public Bundle convertToFhirBundle(ClientEncounterComponentFormSet formset) {
        System.out.println("convertToFhirBundle");
        System.out.println("formset = " + formset);
        Bundle bundle = new Bundle();
        bundle.setType(BundleType.COLLECTION);

        Client myPatient = formset.getEncounter().getClient();
        System.out.println("myPatient = " + myPatient);
        lk.gov.health.phsp.entity.Encounter myEncounter = formset.getEncounter();

        // Assuming you have separate methods to convert to FHIR resources
        Patient patientResource = convertToFhirPatient(myPatient);
        System.out.println("myPatient = " + myPatient);
        System.out.println("patientResource = " + patientResource);
        Encounter encounterResource = convertToEncounter(myEncounter);
        System.out.println("myEncounter = " + myEncounter);
        System.out.println("encounterResource = " + encounterResource);

        // Add Patient to the Bundle
        BundleEntryComponent patientEntry = bundle.addEntry();
        System.out.println("1 patientEntry = " + patientEntry);
        System.out.println("patientResource = " + patientResource);
        patientEntry.setResource(patientResource);
        System.out.println("2 patientEntry = " + patientEntry);

        // Add Encounter to the Bundle
        BundleEntryComponent encounterEntry = bundle.addEntry();
        System.out.println("1 encounterEntry = " + encounterEntry);
        System.out.println("encounterResource = " + encounterResource);
        encounterEntry.setResource(encounterResource);
        System.out.println("2 encounterEntry = " + encounterEntry);

        List<ClientEncounterComponentItem> cecis = clientEncounterComponentItemController.getClientEncounterComponentItemOfAFormset(formset);

        if (cecis == null) {
            return bundle;
        } else {
            System.out.println("cecis = " + cecis.size());
        }
        if (cecis.isEmpty()) {
            return bundle;
        }

        for (ClientEncounterComponentItem is : cecis) {
            System.out.println("is = " + is);
            Observation obsResource = convertToObservation(is);

            System.out.println("obsResource = " + obsResource);
            BundleEntryComponent obsEntry = bundle.addEntry();
            System.out.println("1. obsEntry = " + obsEntry);
            obsEntry.setResource(obsResource);
            System.out.println("2. obsEntry = " + obsEntry);
        }

        System.out.println("bundle = " + bundle.toString());
        return bundle;
    }

    public Encounter convertToEncounter(lk.gov.health.phsp.entity.Encounter myEncounter) {
        Encounter fhirEncounter = new Encounter();

        // Set Identifier
        if (myEncounter.getId() != null) {
            Identifier identifier = new Identifier();
            identifier.setValue(myEncounter.getId().toString());
            fhirEncounter.addIdentifier(identifier);
        }

        // Set Status
        if (myEncounter.getCompleted()) {
            fhirEncounter.setStatus(EncounterStatus.FINISHED);
        } else {
            fhirEncounter.setStatus(EncounterStatus.INPROGRESS); // Set to appropriate status if not completed
        }

        // Set Encounter Dates (Period)
        Period period = new Period();
        if (myEncounter.getEncounterFrom() != null) {
            period.setStart(myEncounter.getEncounterFrom());
        }
        if (myEncounter.getEncounterTo() != null) {
            period.setEnd(myEncounter.getEncounterTo());
        }
        fhirEncounter.setPeriod(period);

        // Set Patient Reference
        if (myEncounter.getClient() != null) {
            fhirEncounter.setSubject(new Reference("Patient/" + myEncounter.getClient().getId()));
        }

        // Set Location (Institution)
        if (myEncounter.getInstitution() != null) {
            Encounter.EncounterLocationComponent location = new Encounter.EncounterLocationComponent(
                    new Reference("Location/" + myEncounter.getInstitution().getId()));
            fhirEncounter.addLocation(location);
        }
        return fhirEncounter;
    }

    // Modified by Dr M H B Ariyaratne with assistance from ChatGPT from OpenAI
    public Observation convertToObservation(ClientEncounterComponentItem ceci) {
        Observation obs = new Observation();

        if (ceci.getReferanceDesignComponentFormItem() == null) {
            return obs;
        }
        String nameOfObservation = ceci.getReferanceDesignComponentFormItem().getName();
        String codeOfTheObservation = ceci.getReferanceDesignComponentFormItem().getCode();
        String codingSystem = "terminology.chims.health.gov.lk";

        CodeableConcept obsCode = new CodeableConcept();
        obsCode.addCoding(new Coding().setCode(codeOfTheObservation).setSystem(codingSystem).setDisplay(nameOfObservation));
        obs.setCode(obsCode);

        switch (ceci.getReferanceDesignComponentFormItem().getSelectionDataType()) {
            case Boolean:
                obs.setValue(new BooleanType(ceci.getBooleanValue()));
                break;

            case Integer_Number:
                if (ceci != null && ceci.getIntegerNumberValue() != null) {
                    Quantity quantityInt = new Quantity();
                    quantityInt.setValue(ceci.getIntegerNumberValue());
                    obs.setValue(quantityInt);
                }
                break;

            case Long_Number:
                Quantity quantityLong = new Quantity();
                quantityLong.setValue(ceci.getLongNumberValue());
                obs.setValue(quantityLong);
                break;

            case Real_Number:
                Quantity quantityReal = new Quantity();
                quantityReal.setValue(ceci.getRealNumberValue());
                obs.setValue(quantityReal);
                break;

            case Item_Reference:
                CodeableConcept cc = new CodeableConcept();
                if (ceci != null && ceci.getItemValue() != null) {
                    Coding coding = new Coding().setSystem(codingSystem);
                    if (ceci.getItemValue().getCode() != null) {
                        coding.setCode(ceci.getItemValue().getCode());
                    }
                    if (ceci.getItemValue().getName() != null) {
                        coding.setDisplay(ceci.getItemValue().getName());
                    }
                    cc.addCoding(coding);
                }
                obs.setValue(cc);
                break;

            case Short_Text:
                obs.setValue(new StringType(ceci.getShortTextValue()));
                break;

            case Long_Text:
                obs.getDataAbsentReason().setText(ceci.getLongTextValue());
                break;

            default:
                // Handle default case, if necessary
                break;
        }

        return obs;
    }

}
