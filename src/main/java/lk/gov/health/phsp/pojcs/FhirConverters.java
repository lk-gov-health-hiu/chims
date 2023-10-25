package lk.gov.health.phsp.pojcs;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import lk.gov.health.phsp.entity.Client;
import lk.gov.health.phsp.entity.Institution;

import org.json.JSONArray;
import org.json.JSONObject;
import lk.gov.health.phsp.entity.Encounter;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Dr M H B Ariyaratne <buddhika.ari at gmail.com>
 */
public class FhirConverters {

    public static String replacePatientAndEncounterInBundle(JSONObject bundleObject, JSONObject newPatient, JSONObject newEncounter) {
        JSONArray entryArray = bundleObject.getJSONArray("entry");

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
            updateResourceReferences(resource, "Patient", newPatientId);
            updateResourceReferences(resource, "Encounter", newEncounterId);
        }

        return bundleObject.toString();
    }

// Helper method to update references in resources
    private static void updateResourceReferences(JSONObject resource, String resourceType, String newId) {
        for (String key : resource.keySet()) {
            Object value = resource.get(key);
            if (value instanceof JSONObject) {
                JSONObject subObject = (JSONObject) value;
                if (subObject.has("reference") && subObject.getString("reference").startsWith(resourceType + "/")) {
                    subObject.put("reference", resourceType + "/" + newId);
                }
                updateResourceReferences(subObject, resourceType, newId);
            } else if (value instanceof JSONArray) {
                JSONArray array = (JSONArray) value;
                for (int i = 0; i < array.length(); i++) {
                    Object item = array.get(i);
                    if (item instanceof JSONObject) {
                        updateResourceReferences((JSONObject) item, resourceType, newId);
                    }
                }
            }
        }
    }

    public static JSONObject convertMyEncounterToFhirEncounter(Encounter encounter, JSONObject patient) {
        JSONObject encounterJson = new JSONObject();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        // Set resource type
        encounterJson.put("resourceType", "Encounter");

        String prefix = "Encounter";
        // Set ID
        if (encounter.getId() != null) {
            encounterJson.put("id", prefix + encounter.getId());
        }

        if (encounter.getEncounterNumber() != null) {
            JSONArray identifierArray = new JSONArray();
            identifierArray.put(new JSONObject()
                    .put("system", "https://fhir.health.gov.lk/id/encounterNumber")
                    .put("value", encounter.getEncounterNumber()));
            encounterJson.put("identifier", identifierArray);
        }

        // Set Encounter Period (From and To)
        JSONObject period = new JSONObject();
        if (encounter.getEncounterFrom() != null) {
            period.put("start", sdf.format(encounter.getEncounterFrom()));
        }
        if (encounter.getEncounterTo() != null) {
            period.put("end", sdf.format(encounter.getEncounterTo()));
        }
        encounterJson.put("period", period);

        // Set Encounter Type (assuming it can be mapped directly)
        if (encounter.getEncounterType() != null) {
            JSONArray typeArray = new JSONArray();
            typeArray.put(new JSONObject().put("text", encounter.getEncounterType().toString()));
            encounterJson.put("type", typeArray);
        }

        // Set Institution (assuming it's the service provider)
        if (encounter.getInstitution() != null) {
            JSONArray serviceProviderArray = new JSONArray();
            serviceProviderArray.put(new JSONObject()
                    .put("reference", "Organization/" + encounter.getInstitution().getId()) // Assuming the ID of the institution can be used as a reference
                    .put("display", encounter.getInstitution().getName())); // Assuming the Institution entity has a getName() method
            encounterJson.put("serviceProvider", serviceProviderArray);
        }

        // Add Patient reference
        if (patient != null && patient.has("id")) {
            JSONObject subject = new JSONObject();
            subject.put("reference", "Patient/" + patient.getString("id"));
            encounterJson.put("subject", subject);
        }

        // More fields can be added as needed. The structure above can be used as a template for additional fields.
        return encounterJson;
    }

    public static JSONObject convertInstitutionToOrganization(Institution institution) {
        JSONObject resource = new JSONObject();
        resource.put("resourceType", "Organization");
        String idPrefix = "Organization";
        resource.put("id", idPrefix + institution.getId());
        JSONObject meta = new JSONObject();
        meta.put("profile", Arrays.asList("http://openhie.org/fhir/sri-lanka/StructureDefinition/organization"));
        resource.put("meta", meta);

        JSONArray identifierArray = new JSONArray();
        JSONObject identifier = new JSONObject();
        JSONObject type = new JSONObject();
        JSONArray codingArray = new JSONArray();
        JSONObject coding = new JSONObject();
        coding.put("system", "http://terminology.hl7.org/CodeSystem/v2-0203");
        coding.put("code", "XX");
        coding.put("display", "Organization identifier");
        codingArray.put(coding);
        type.put("coding", codingArray);
        type.put("text", "Organization identifier");
        identifier.put("type", type);
        identifier.put("system", "http://openhie.org/fhir/sri-lanka/identifier/organization");
        identifier.put("value", idPrefix + institution.getId());
        identifierArray.put(identifier);
        resource.put("identifier", identifierArray);

        resource.put("name", institution.getName());

        JSONArray addressArray = new JSONArray();
        JSONObject address = new JSONObject();
        address.put("city", institution.getGnArea());
        address.put("district", institution.getDistrict());
        address.put("state", institution.getProvince());
        address.put("postalCode", institution.getAddress());
        address.put("country", institution.getDsDivision());
        addressArray.put(address);
        resource.put("address", addressArray);

        return resource;
    }

    // Modified by Dr M H B Ariyaratne with assistance from ChatGPT from OpenAI.
    public static String createPatientBundleJsonPayload(List<Client> clients) {
        JSONObject bundleJson = new JSONObject();

        bundleJson.put("resourceType", "Bundle");
        bundleJson.put("id", "HIMS-Transactional-Bundle");
        bundleJson.put("type", "transaction");

        JSONArray entryArray = new JSONArray();

        for (Client client : clients) {
            // Creating Patient Entry
            JSONObject patientEntry = new JSONObject();
            JSONObject patientResource = convertMyClientToFhirPatient(client);

            patientEntry.put("fullUrl", "Patient/" + client.getPhn()); // Assuming PHN can be used as PatientID
            patientEntry.put("resource", patientResource);

            JSONObject patientRequest = new JSONObject();
            patientRequest.put("method", "PUT");
            String idPrefix = "Patient";
            patientRequest.put("url", "Patient/" + idPrefix + client.getId());
            patientEntry.put("request", patientRequest);

            entryArray.put(patientEntry);
        }

        bundleJson.put("entry", entryArray);

        return bundleJson.toString(4); // '4' denotes 4 spaces indentation
    }

    // Modified by Dr M H B Ariyaratne with assistance from ChatGPT from OpenAI.
    public static JSONArray convertMyClientsToFhirPatients(List<Client> clients) {
        JSONArray patientsArray = new JSONArray();

        for (Client client : clients) {
            JSONObject patientJson = new JSONObject();

            patientJson.put("resourceType", "Patient");
            String idPrefix = "Patient";
            patientJson.put("id", idPrefix + client.getPhn()); // Assuming PHN can be used as PatientID

            JSONObject meta = new JSONObject();
            meta.put("profile", Arrays.asList("http://openhie.org/fhir/sri-lanka/StructureDefinition/patient"));
            patientJson.put("meta", meta);

            JSONArray identifierArray = new JSONArray();

            // Identifiers
            if (client.getPhn() != null) {
                patientJson.append("identifier", new JSONObject()
                        .put("system", "https://fhir.health.gov.lk/id/phn")
                        .put("value", client.getPhn()));
            }
            if (client.getPerson().getNic() != null) {
                patientJson.append("identifier", new JSONObject()
                        .put("system", "https://fhir.health.gov.lk/id/nic")
                        .put("value", client.getPerson().getNic()));
            }
            if (client.getPerson().getPassportNumber() != null) {
                patientJson.append("identifier", new JSONObject()
                        .put("system", "https://fhir.health.gov.lk/id/ppn")
                        .put("value", client.getPerson().getPassportNumber()));
            }
            if (client.getPerson().getSsNumber() != null) {
                patientJson.append("identifier", new JSONObject()
                        .put("system", "https://fhir.health.gov.lk/id/scn")
                        .put("value", client.getPerson().getSsNumber()));
            }
            if (client.getPerson().getDrivingLicenseNumber() != null) {
                patientJson.append("identifier", new JSONObject()
                        .put("system", "https://fhir.health.gov.lk/id/dl")
                        .put("value", client.getPerson().getDrivingLicenseNumber()));
            }

            patientJson.put("identifier", identifierArray);

            // Name
            if (client.getPerson().getName() != null) {
                JSONArray nameArray = new JSONArray();
                nameArray.put(new JSONObject().put("text", client.getPerson().getName()));
                patientJson.put("name", nameArray);
            }

            // Gender
            if (client.getPerson().getSex() != null && client.getPerson().getSex().getName() != null) {
                patientJson.put("gender", client.getPerson().getSex().getName().toLowerCase());
            }

            // Date of Birth
            if (client.getPerson().getDateOfBirth() != null) {
                patientJson.put("birthDate", client.getPerson().getDateOfBirth().toString());
            }

            // Address
            if (client.getPerson().getAddress() != null) {
                JSONArray addressArray = new JSONArray();
                JSONObject addressJson = new JSONObject()
                        .put("line", new JSONArray().put(client.getPerson().getAddress()))
                        .put("city", client.getPerson().getGnArea().getName());
                addressArray.put(addressJson);
                patientJson.put("address", addressArray);
            }

            // Contact Information
            if (client.getPerson().getPhone1() != null || client.getPerson().getPhone2() != null || client.getPerson().getEmail() != null) {
                JSONArray telecomArray = new JSONArray();
                telecomArray.put(new JSONObject()
                        .put("system", "phone")
                        .put("use", "mobile")
                        .put("value", client.getPerson().getPhone1()));
                telecomArray.put(new JSONObject()
                        .put("system", "phone")
                        .put("use", "home")
                        .put("value", client.getPerson().getPhone2()));
                telecomArray.put(new JSONObject()
                        .put("system", "email")
                        .put("value", client.getPerson().getEmail()));
                patientJson.put("telecom", telecomArray);
            }

            patientsArray.put(patientJson);
        }

        return patientsArray;
    }

    // Modified by Dr M H B Ariyaratne with assistance from ChatGPT from OpenAI.
    public static JSONObject convertMyClientToFhirPatient(Client client) {
        JSONObject patientJson = new JSONObject();

        patientJson.put("resourceType", "Patient");
        String idPrefix = "Patient";
        patientJson.put("id", idPrefix + client.getId()); // Assuming PHN can be used as PatientID

        JSONObject meta = new JSONObject();
        meta.put("profile", Arrays.asList("http://openhie.org/fhir/sri-lanka/StructureDefinition/patient"));
        patientJson.put("meta", meta);

        JSONArray identifierArray = new JSONArray();

        // Identifiers
        if (client.getPhn() != null) {
            patientJson.append("identifier", new JSONObject()
                    .put("system", "https://fhir.health.gov.lk/id/phn")
                    .put("value", client.getPhn()));
        }
        if (client.getPerson().getNic() != null) {
            patientJson.append("identifier", new JSONObject()
                    .put("system", "https://fhir.health.gov.lk/id/nic")
                    .put("value", client.getPerson().getNic()));
        }
        if (client.getPerson().getPassportNumber() != null) {
            patientJson.append("identifier", new JSONObject()
                    .put("system", "https://fhir.health.gov.lk/id/ppn")
                    .put("value", client.getPerson().getPassportNumber()));
        }
        if (client.getPerson().getSsNumber() != null) {
            patientJson.append("identifier", new JSONObject()
                    .put("system", "https://fhir.health.gov.lk/id/scn")
                    .put("value", client.getPerson().getSsNumber()));
        }
        if (client.getPerson().getDrivingLicenseNumber() != null) {
            patientJson.append("identifier", new JSONObject()
                    .put("system", "https://fhir.health.gov.lk/id/dl")
                    .put("value", client.getPerson().getDrivingLicenseNumber()));
        }

        patientJson.put("identifier", identifierArray);

        // Name
        if (client.getPerson().getName() != null) {
            JSONArray nameArray = new JSONArray();
            nameArray.put(new JSONObject().put("text", client.getPerson().getName()));
            patientJson.put("name", nameArray);
        }

        // Gender
        if (client.getPerson().getSex() != null && client.getPerson().getSex().getName() != null) {
            patientJson.put("gender", client.getPerson().getSex().getName().toLowerCase());
        }

        // Modified by Dr M H B Ariyaratne with assistance from ChatGPT from OpenAI.
// Date of Birth
        if (client.getPerson().getDateOfBirth() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = sdf.format(client.getPerson().getDateOfBirth());
            patientJson.put("birthDate", formattedDate);
        }
        JSONArray addressArray = new JSONArray();
        JSONObject addressJson = new JSONObject();

        if (client.getPerson().getAddress() != null) {
            addressJson.put("line", new JSONArray().put(client.getPerson().getAddress()));
        }

        if (client.getPerson().getGnArea() != null) {
            addressJson.put("city", client.getPerson().getGnArea().getName());
        }

        if (!addressJson.isEmpty()) {  // Check if we've added anything to the addressJson
            addressArray.put(addressJson);
            patientJson.put("address", addressArray);
        }

        // Contact Information
        if (client.getPerson().getPhone1() != null || client.getPerson().getPhone2() != null || client.getPerson().getEmail() != null) {
            JSONArray telecomArray = new JSONArray();
            telecomArray.put(new JSONObject()
                    .put("system", "phone")
                    .put("use", "mobile")
                    .put("value", client.getPerson().getPhone1()));
            telecomArray.put(new JSONObject()
                    .put("system", "phone")
                    .put("use", "home")
                    .put("value", client.getPerson().getPhone2()));
            telecomArray.put(new JSONObject()
                    .put("system", "email")
                    .put("value", client.getPerson().getEmail()));
            patientJson.put("telecom", telecomArray);
        }

        return patientJson;
    }

    public static JSONObject convertInstitutionToLocation(Institution institution) {
        JSONObject resource = new JSONObject();
        resource.put("resourceType", "Location");
        String idPrefix = "Location";
        resource.put("id", idPrefix + institution.getId());

        // Set managingOrganization if parent institution exists
        if (institution.getParent() != null) {
            JSONObject managingOrg = new JSONObject();
            managingOrg.put("reference", "Organization/" + institution.getParent().getId());
            resource.put("managingOrganization", managingOrg);
        }

        JSONArray identifierArray = new JSONArray();
        JSONObject identifier = new JSONObject();
        JSONObject type = new JSONObject();
        JSONArray codingArray = new JSONArray();
        JSONObject coding = new JSONObject();
        coding.put("system", "http://terminology.hl7.org/CodeSystem/v2-0203");
        coding.put("code", "XX");
        coding.put("display", "Location identifier");
        codingArray.put(coding);
        type.put("coding", codingArray);
        type.put("text", "Location identifier");
        identifier.put("type", type);
        identifier.put("system", "http://openhie.org/fhir/sri-lanka/identifier/location");
        identifier.put("value", idPrefix + institution.getId());
        identifierArray.put(identifier);
        resource.put("identifier", identifierArray);

        resource.put("name", institution.getName());

        JSONArray addressArray = new JSONArray();
        JSONObject address = new JSONObject();
        address.put("city", institution.getGnArea());
        address.put("district", institution.getDistrict());
        address.put("state", institution.getProvince());
        address.put("postalCode", institution.getAddress());
        address.put("country", institution.getDsDivision());
        addressArray.put(address);
        resource.put("address", addressArray);

        return resource;
    }

    public static String createLocationJsonPayload(List<Institution> institutions) {
        JSONObject bundle = new JSONObject();
        bundle.put("resourceType", "Bundle");
        bundle.put("type", "transaction");
        JSONArray entryArray = new JSONArray();
        for (Institution institution : institutions) {
            JSONObject entry = new JSONObject();
            JSONObject resource = FhirConverters.convertInstitutionToLocation(institution);
            entry.put("resource", resource);
            JSONObject request = new JSONObject();
            request.put("method", "PUT");
            request.put("url", "Location/" + "Location" + institution.getId());
            entry.put("request", request);
            entryArray.put(entry);
        }
        bundle.put("entry", entryArray);
        return bundle.toString(4);  // '4' denotes 4 spaces indentation
    }

    public static String createOrganizationJsonPayload(List<Institution> institutions) {
        JSONObject bundle = new JSONObject();
        bundle.put("resourceType", "Bundle");
        bundle.put("type", "transaction");
        JSONArray entryArray = new JSONArray();
        for (Institution institution : institutions) {
            JSONObject entry = new JSONObject();
            JSONObject resource = FhirConverters.convertInstitutionToOrganization(institution);
            entry.put("resource", resource);
            JSONObject request = new JSONObject();
            request.put("method", "PUT");
            request.put("url", "Organization/" + "Organization" + institution.getId());
            entry.put("request", request);
            entryArray.put(entry);
        }
        bundle.put("entry", entryArray);
        return bundle.toString(4);  // '4' denotes 4 spaces indentation
    }

}
