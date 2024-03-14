package lk.gov.health.phsp.bean;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.context.support.ValidationSupportContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.IInstanceValidatorModule;
import ca.uhn.fhir.validation.IValidatorModule;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.ejb.EJB;
import javax.inject.Inject;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.Client;
import lk.gov.health.phsp.entity.FhirOperationResult;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.entity.Person;
import lk.gov.health.phsp.entity.Preference;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.facade.ClientFacade;
import lk.gov.health.phsp.facade.InstitutionFacade;
import lk.gov.health.phsp.facade.WebUserFacade;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointUse;
import org.hl7.fhir.r4.model.Device;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StructureDefinition;

/**
 *
 * @author Dr M H B Ariyaratne <buddhika.ari at gmail.com>
 */
@Named(value = "fhirController")
@SessionScoped
public class FhirController implements Serializable {

    @EJB
    InstitutionFacade institutionFacade;
    @EJB
    ClientFacade clientFacade;
    @EJB
    WebUserFacade webUserFacade;

    @Inject
    InstitutionController institutionController;
    @Inject
    PreferenceController preferenceController;

    /**
     * Creates a new instance of FhirController
     */
    public FhirController() {
    }

    public String bundleToJson(IBaseResource bundleResource) {
        FhirContext fhirContext = FhirContext.forR4();
        IParser parser = fhirContext.newJsonParser();
        return parser.encodeResourceToString(bundleResource);
    }

    public String serializeResourceToJson(Resource resource) {
        FhirContext ctx = FhirContext.forR4();
        return ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(resource);
    }

    public Bundle.BundleEntryComponent createEncounterEntry(
            Patient patient,
            List<Practitioner> practitioners,
            List<Location> locations,
            List<String> reasonCodes,
            Boolean finished,
            Date start,
            Date end) {

        Encounter encounter = new Encounter();
        encounter.setId(UUID.randomUUID().toString()); // Use UUID or some unique identifier for Encounter itself
        encounter.getMeta().addProfile("http://fhir.health.gov.lk/ips/StructureDefinition/target-facility-encounter");

        encounter.setStatus((finished != null && finished) ? Encounter.EncounterStatus.FINISHED : Encounter.EncounterStatus.INPROGRESS);
        encounter.setClass_(new Coding("http://terminology.hl7.org/CodeSystem/v3-ActCode", "AMB", null));

        if (patient != null) {
            encounter.setSubject(new Reference("Patient/" + patient.getIdElement().getIdPart()));
        }

        if (practitioners != null && !practitioners.isEmpty()) {
            for (Practitioner practitioner : practitioners) {
                Encounter.EncounterParticipantComponent participant = new Encounter.EncounterParticipantComponent();
                participant.setIndividual(new Reference("Practitioner/" + practitioner.getIdElement().getIdPart()));
                encounter.addParticipant(participant);
            }
        }

        Period period = new Period();
        period.setStart(start);
        period.setEnd(end);
        encounter.setPeriod(period);

        if (reasonCodes != null && !reasonCodes.isEmpty()) {
            for (String code : reasonCodes) {
                CodeableConcept reason = new CodeableConcept();
                reason.addCoding(new Coding("http://snomed.info/sct", code, null));
                encounter.addReasonCode(reason);
            }
        }

        if (locations != null && !locations.isEmpty()) {
            for (Location location : locations) {
                // Check if the location or its ID element is null
                if (location == null || location.getIdElement() == null || location.getIdElement().getIdPart() == null) {
                    System.out.println("Warning: A location is null or does not have an ID.");
                    continue; // Skip this location
                }

                Encounter.EncounterLocationComponent locationComponent = new Encounter.EncounterLocationComponent();
                locationComponent.setLocation(new Reference("Location/" + location.getIdElement().getIdPart()));
                encounter.addLocation(locationComponent);
            }
        }

        // Generate a simple narrative text for the Encounter
        String narrativeText = "Encounter details...";
        // Here you would dynamically generate the narrative text as needed
        Narrative narrative = new Narrative();
        narrative.setStatus(Narrative.NarrativeStatus.GENERATED);
        narrative.setDivAsString("<div xmlns=\"http://www.w3.org/1999/xhtml\">" + narrativeText + "</div>");
        encounter.setText(narrative);

        Bundle.BundleEntryComponent entryComponent = new Bundle.BundleEntryComponent();
        entryComponent.setFullUrl("http://hapi-fhir:8080/fhir/Encounter/" + encounter.getId());
        entryComponent.setResource(encounter);
        entryComponent.getRequest().setMethod(Bundle.HTTPVerb.PUT).setUrl("Encounter/" + encounter.getId());

        return entryComponent;
    }

    public boolean validate(String structureDefinitionString, String fhirJsonMessageString) {
        //todo
        return true;
    }

    public Bundle createTransactionalBundleWithUUID(String uuid) {
        Bundle bundle = new Bundle();

        // Set the bundle type to transaction
        bundle.setType(Bundle.BundleType.TRANSACTION);

        // Set the bundle ID and Meta.profile using the provided UUID
        bundle.setId(uuid);
        Meta meta = new Meta();
        meta.addProfile("http://fhir.health.gov.lk/ips/StructureDefinition/hims-transactional-bundle");
        bundle.setMeta(meta);

        // Set the identifier
        Identifier identifier = new Identifier();
        identifier.setSystem("http://fhir.health.gov.lk/ips/identifier/hims-transactional");
        identifier.setValue(uuid);
        bundle.setIdentifier(identifier);

        // Set the timestamp to the current system time
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        String formattedTimestamp = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(now);
        bundle.setTimestamp(java.sql.Timestamp.valueOf(now.toLocalDateTime()));

        return bundle;
    }

    public void addEntryToBundle(Bundle bundle, Bundle.BundleEntryComponent entry) {
        bundle.addEntry(entry);
    }

    public Patient extractPatientFromEntry(Bundle.BundleEntryComponent entry) {
        if (entry != null && entry.getResource() instanceof Patient) {
            return (Patient) entry.getResource();
        }
        return null; // or throw an exception if you prefer
    }

    public Organization extractOrganizationFromEntry(Bundle.BundleEntryComponent entry) {
        if (entry != null && entry.getResource() instanceof Organization) {
            return (Organization) entry.getResource();
        }
        return null; // or throw an exception if you prefer
    }

    public Location extractLocationFromEntry(Bundle.BundleEntryComponent entry) {
        if (entry != null && entry.getResource() instanceof Location) {
            return (Location) entry.getResource();
        }
        return null; // or throw an exception if you prefer
    }

    public Device extractDeviceFromEntry(Bundle.BundleEntryComponent entry) {
        if (entry != null && entry.getResource() instanceof Device) {
            return (Device) entry.getResource();
        }
        return null; // or throw an exception if you prefer
    }

    public Practitioner extractPractitionerFromEntry(Bundle.BundleEntryComponent entry) {
        if (entry != null && entry.getResource() instanceof Practitioner) {
            return (Practitioner) entry.getResource();
        }
        return null; // or throw an exception if you prefer
    }

    public Bundle.BundleEntryComponent createPatientEntry(Client chimsPatient) {
        if (chimsPatient.getUuid() == null) {
            chimsPatient.setUuid(UUID.randomUUID().toString());
            if (chimsPatient.getId() == null) {
                clientFacade.create(chimsPatient);
            } else {
                clientFacade.edit(chimsPatient);
            }
        }

        String patientId = chimsPatient.getUuid();
        String deviceInformation = null;
        List<String> phnList = new ArrayList<>();
        if (chimsPatient.getPhn() != null && !chimsPatient.getPhn().trim().isEmpty()) {
            phnList.add(chimsPatient.getPhn().trim());
        }

        List<String> ppnList = chimsPatient.getPerson().getPassportNumber() != null && !chimsPatient.getPerson().getPassportNumber().trim().isEmpty()
                ? Collections.singletonList(chimsPatient.getPerson().getPassportNumber().trim())
                : new ArrayList<>();

        List<String> dlList = chimsPatient.getPerson().getDrivingLicenseNumber() != null && !chimsPatient.getPerson().getDrivingLicenseNumber().trim().isEmpty()
                ? Collections.singletonList(chimsPatient.getPerson().getDrivingLicenseNumber().trim())
                : new ArrayList<>();

        List<String> nicList = chimsPatient.getPerson().getNic() != null && !chimsPatient.getPerson().getNic().trim().isEmpty()
                ? Collections.singletonList(chimsPatient.getPerson().getNic().trim())
                : new ArrayList<>();

        List<String> scnList = chimsPatient.getPerson().getSsNumber() != null && !chimsPatient.getPerson().getSsNumber().trim().isEmpty()
                ? Collections.singletonList(chimsPatient.getPerson().getSsNumber().trim())
                : new ArrayList<>();

        List<String> givenNames = chimsPatient.getPerson().getName() != null && !chimsPatient.getPerson().getName().trim().isEmpty()
                ? Collections.singletonList(chimsPatient.getPerson().getName().trim())
                : new ArrayList<>();

// Handling multiple phone numbers
        List<String> phoneNumbers = new ArrayList<>();
        if (chimsPatient.getPerson().getPhone1() != null && !chimsPatient.getPerson().getPhone1().trim().isEmpty()) {
            phoneNumbers.add(chimsPatient.getPerson().getPhone1().trim());
        }
        if (chimsPatient.getPerson().getPhone2() != null && !chimsPatient.getPerson().getPhone2().trim().isEmpty()) {
            phoneNumbers.add(chimsPatient.getPerson().getPhone2().trim());
        }

        List<String> emails = chimsPatient.getPerson().getEmail() != null && !chimsPatient.getPerson().getEmail().trim().isEmpty()
                ? Collections.singletonList(chimsPatient.getPerson().getEmail().trim())
                : new ArrayList<>();

        List<String> addressLines = chimsPatient.getPerson().getAddress() != null && !chimsPatient.getPerson().getAddress().trim().isEmpty()
                ? Collections.singletonList(chimsPatient.getPerson().getAddress().trim())
                : new ArrayList<>();

        Person person = chimsPatient.getPerson();
        String lastName = person.getName() != null ? person.getName() : "";
        Item title = person.getTitle();
        String namePrefix = title != null && title.getName() != null ? title.getName() : "";
        Item sex = person.getSex();
        String gender = sex != null && sex.getName() != null ? sex.getName() : "";
        Area gnArea = person.getGnArea();
        String city = gnArea != null && gnArea.getName() != null ? gnArea.getName() : "";
        Area districtObj = person.getDistrict();
        String district = districtObj != null && districtObj.getName() != null ? districtObj.getName() : "";
        String state = null;
        String postalCode = null;
        String country = "lk";

        Date dateOfBirth = chimsPatient.getPerson().getDateOfBirth();
        String birthDate = "";

        if (dateOfBirth != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            birthDate = sdf.format(dateOfBirth);
        }
        return createPatientEntry(patientId, deviceInformation, phnList, ppnList, dlList, nicList, scnList, lastName, givenNames, namePrefix, phoneNumbers, emails, gender, birthDate, addressLines, city, district, state, postalCode, country);

    }

    public Bundle.BundleEntryComponent createPatientEntry(
            String patientId,
            String deviceInformation,
            List<String> phnList,
            List<String> ppnList,
            List<String> dlList,
            List<String> nicList,
            List<String> scnList,
            String lastName,
            List<String> givenNames,
            String namePrefix,
            List<String> phoneNumbers,
            List<String> emails,
            String gender,
            String birthDate,
            List<String> addressLines,
            String city,
            String district,
            String state,
            String postalCode,
            String country) {

        Patient patient = new Patient();
        patient.setId(patientId);

        // Set Meta
        Meta meta = new Meta();
        meta.addProfile("http://fhir.health.gov.lk/ips/StructureDefinition/ips-patient");
        patient.setMeta(meta);

        // Set extension if deviceInformation is provided
        if (deviceInformation != null) {
            Extension extension = new Extension("http://fhir.health.gov.lk/ips/StructureDefinition/patient-registration-system");
            extension.setValue(new Reference("Device/" + deviceInformation));
            patient.addExtension(extension);
        }

        // Helper method to add identifiers
        addIdentifiers(patient, phnList, "http://fhir.health.gov.lk/ips/CodeSystem/cs-identifier-types", "PHN", "http://fhir.health.gov.lk/ips/identifier/phn");
        addIdentifiers(patient, ppnList, "http://terminology.hl7.org/CodeSystem/v2-0203", "PPN", "http://fhir.health.gov.lk/ips/identifier/passport");
        addIdentifiers(patient, dlList, "http://terminology.hl7.org/CodeSystem/v2-0203", "DL", "http://fhir.health.gov.lk/ips/identifier/drivers");
        addIdentifiers(patient, nicList, "http://fhir.health.gov.lk/ips/CodeSystem/cs-identifier-types", "NIC", "http://fhir.health.gov.lk/ips/identifier/nic");
        addIdentifiers(patient, scnList, "http://fhir.health.gov.lk/ips/CodeSystem/cs-identifier-types", "SCN", "http://fhir.health.gov.lk/ips/identifier/scn");

        // Set the patient's name
        HumanName name = new HumanName();
        name.setUse(HumanName.NameUse.OFFICIAL);
        name.setFamily(lastName);
        givenNames.forEach(name::addGiven);
        if (namePrefix != null) {
            name.addPrefix(namePrefix);
        }
        patient.addName(name);

        // Set telecom (phone and email)
        phoneNumbers.forEach(phone -> patient.addTelecom(new ContactPoint().setSystem(ContactPoint.ContactPointSystem.PHONE).setValue(phone)));
        emails.forEach(email -> patient.addTelecom(new ContactPoint().setSystem(ContactPoint.ContactPointSystem.EMAIL).setValue(email)));

        // Set gender
        if (gender != null) {
            patient.setGender(Enumerations.AdministrativeGender.fromCode(gender.toLowerCase()));
        }

        // Set birthDate
        if (birthDate != null) {
            patient.setBirthDate(java.sql.Date.valueOf(birthDate));
        }

        // Set address
        Address address = new Address();
        address.setType(Address.AddressType.POSTAL);
        addressLines.forEach(address::addLine);
        address.setCity(city);
        address.setDistrict(district);
        address.setState(state);
        address.setPostalCode(postalCode);
        address.setCountry(country);
        patient.addAddress(address);

        // Creating Bundle Entry
        Bundle.BundleEntryComponent entryComponent = new Bundle.BundleEntryComponent();
        entryComponent.setFullUrl("http://hapi-fhir:8080/fhir/Patient/" + patientId);
        entryComponent.setResource(patient);

        // Set request part of the entry (for transaction type Bundle)
        entryComponent.getRequest().setMethod(Bundle.HTTPVerb.PUT).setUrl("Patient/" + patientId);

        return entryComponent;
    }

    private void addIdentifiers(Patient patient, List<String> values, String systemCode, String code, String system) {
        if (values != null) {
            for (String value : values) {
                if (value != null) {
                    Identifier identifier = new Identifier();
                    identifier.setType(new CodeableConcept(new Coding(systemCode, code, null)));
                    identifier.setSystem(system);
                    identifier.setValue(value);
                    patient.addIdentifier(identifier);
                }
            }
        }
    }

    public Bundle.BundleEntryComponent createOrganizationEntry(Institution ins) {
        if (ins.getUuid() == null || ins.getUuid().trim().equals("")) {
            ins.setUuid(UUID.randomUUID().toString());
            if (ins.getId() == null) {
                institutionFacade.create(ins);
            } else {
                institutionFacade.edit(ins);
            }
        }
        String organizationId = ins.getUuid();

        String hinId = ins.getHin();
        String organizationName = ins.getName();
        return createOrganizationEntry(organizationId, hinId, organizationName);
    }

    public Bundle.BundleEntryComponent createOrganizationEntry(String organizationId, String facilityId, String organizationName) {
        Organization organization = new Organization();
        organization.setId(organizationId);

        Meta meta = new Meta();
        meta.addProfile("http://fhir.health.gov.lk/ips/StructureDefinition/organization");
        organization.setMeta(meta);

        // Identifier for the organization with HIN
        Identifier hinIdentifier = new Identifier()
                .setType(new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v2-0203", "XX", "Organization identifier")))
                .setSystem("http://fhir.health.gov.lk/ips/identifier/hin")
                .setValue(facilityId);
        organization.addIdentifier(hinIdentifier);

        if (organizationName != null && !organizationName.trim().isEmpty()) {
            organization.setName(organizationName.trim());
        }

        // Construct narrative text
        String narrativeText = "Organization: " + organizationName + ". Facility ID: " + facilityId + ". Summary or description of the organization.";

        Narrative narrative = new Narrative();
        narrative.setStatus(Narrative.NarrativeStatus.GENERATED);
        String narrativeDiv = "<div xmlns=\"http://www.w3.org/1999/xhtml\">" + narrativeText + "</div>";
        narrative.setDivAsString(narrativeDiv);
        organization.setText(narrative);

        Bundle.BundleEntryComponent entryComponent = new Bundle.BundleEntryComponent();
        entryComponent.setFullUrl("http://hapi-fhir:8080/fhir/Organization/" + organizationId);
        entryComponent.setResource(organization);
        entryComponent.getRequest().setMethod(Bundle.HTTPVerb.PUT).setUrl("Organization/" + organizationId);
        System.out.println("entryComponent = " + entryComponent.toString());
        return entryComponent;
    }

    public Organization createOrganization(
            String organizationId,
            String facilityId,
            String organizationName) {

        Organization organization = new Organization();
        organization.setId(organizationId);

        // Set Meta
        Meta meta = new Meta();
        meta.addProfile("http://fhir.health.gov.lk/ips/StructureDefinition/organization");
        organization.setMeta(meta);

        // Set Identifier
        if (facilityId != null && !facilityId.trim().isEmpty()) {
            Identifier identifier = new Identifier();
            identifier.setType(new CodeableConcept(new Coding("http://terminology.hl7.org/CodeSystem/v2-0203", "XX", "Organization identifier")));
            identifier.setSystem("http://fhir.health.gov.lk/ips/identifier/organization");
            identifier.setValue(facilityId.trim());
            organization.addIdentifier(identifier);
        }

        // Set Name
        if (organizationName != null && !organizationName.trim().isEmpty()) {
            organization.setName(organizationName.trim());
        }

        return organization;
    }

    public Bundle.BundleEntryComponent createLocationEntry(Institution ins) {
        List<ContactPoint> telecoms = new ArrayList<>();

        if (ins.getUuid() == null || ins.getUuid().trim().equals("")) {
            ins.setUuid(UUID.randomUUID().toString());
            if (ins.getId() == null) {
                institutionFacade.create(ins);
            } else {
                institutionFacade.edit(ins);
            }
        }

        if (ins.getPhone() != null) {
            ContactPoint phoneContact = new ContactPoint();
            phoneContact.setSystem(ContactPointSystem.PHONE);
            phoneContact.setValue(ins.getPhone());
            phoneContact.setUse(ContactPointUse.WORK);
            telecoms.add(phoneContact);
        }

        if (ins.getMobile() != null) {
            ContactPoint mobileContact = new ContactPoint();
            mobileContact.setSystem(ContactPointSystem.PHONE);
            mobileContact.setValue(ins.getMobile());
            mobileContact.setUse(ContactPointUse.MOBILE);
            telecoms.add(mobileContact);
        }

        if (ins.getEmail() != null) {
            ContactPoint emailContact = new ContactPoint();
            emailContact.setSystem(ContactPointSystem.EMAIL);
            emailContact.setValue(ins.getEmail());
            emailContact.setUse(ContactPointUse.HOME);
            telecoms.add(emailContact);
        }

        String locationId = ins.getUuid();
        List<String> identifierValues = new ArrayList<>();
        if (ins.getHin() != null) {
            identifierValues.add(ins.getHin());
        }
        String locationStatus = "active";
        String locationName = ins.getName();
        Address address = stringToAddress(ins.getAddress());

        String managingOrganizationId = "";

        if (ins.getParent() != null) {
            if (ins.getParent().getUuid() == null) {
                ins.getParent().setUuid(UUID.randomUUID().toString());
                if (ins.getParent().getId() == null) {
                    institutionFacade.create(ins.getParent());
                } else {
                    institutionFacade.edit(ins.getParent());
                }
            }
            managingOrganizationId = ins.getParent().getUuid();
        }
        return createLocationEntry(locationId, identifierValues, locationStatus, locationName, telecoms, address, managingOrganizationId);
    }

    public Address stringToAddress(String addressString) {
        Address address = new Address();
        if (addressString != null && !addressString.trim().isEmpty()) {
            String[] parts = addressString.split(",\\s*");

            if (parts.length > 0) {
                // Assuming the first part is the street address
                address.addLine(parts[0].trim());
            }
            if (parts.length > 1) {
                // Assuming the second part is the city
                address.setCity(parts[1].trim());
            }
            if (parts.length > 2) {
                // Assuming the third part is the state or district
                address.setState(parts[2].trim());
            }
            if (parts.length > 3) {
                // Assuming the fourth part is the postal code
                address.setPostalCode(parts[3].trim());
            }
            // Set the country as Sri Lanka
            address.setCountry("Sri Lanka");
        }

        return address;
    }

    public Bundle.BundleEntryComponent createLocationEntry(
            String locationId,
            List<String> identifierValues,
            String locationStatus,
            String locationName,
            List<ContactPoint> telecoms,
            Address address,
            String managingOrganizationId) {

        Location location = new Location();
        location.setId(locationId);

        // Set Meta
        Meta meta = new Meta();
        meta.addProfile("http://fhir.health.gov.lk/ips/StructureDefinition/providers-location");
        location.setMeta(meta);

        // Set Identifiers
        identifierValues.forEach(value -> {
            Identifier identifier = new Identifier();
            identifier.setType(new CodeableConcept(new Coding("http://fhir.health.gov.lk/ips/CodeSystem/cs-identifier-types", "PLOC", "Provider location identifier")));
            identifier.setSystem("http://fhir.health.gov.lk/ips/identifier/provider-location");
            identifier.setValue(value);
            location.addIdentifier(identifier);
        });

        // Set Status
        if (locationStatus != null && !locationStatus.trim().isEmpty()) {
            location.setStatus(Location.LocationStatus.fromCode(locationStatus.toLowerCase()));
        }

        // Set Name
        if (locationName != null && !locationName.trim().isEmpty()) {
            location.setName(locationName.trim());
        }

        // Set Telecoms
        if (telecoms != null) {
            location.setTelecom(telecoms);
        }

        // Set Address
        if (address != null) {
            location.setAddress(address);
        }

        // Set Managing Organization
        if (managingOrganizationId != null && !managingOrganizationId.trim().isEmpty()) {
            location.setManagingOrganization(new Reference("Organization/" + managingOrganizationId.trim()));
        }

        // Creating Bundle Entry
        Bundle.BundleEntryComponent entryComponent = new Bundle.BundleEntryComponent();
        entryComponent.setFullUrl("http://hapi-fhir:8080/fhir/Location/" + locationId);
        entryComponent.setResource(location);

        // Set request part of the entry (for transaction type Bundle)
        entryComponent.getRequest().setMethod(Bundle.HTTPVerb.PUT).setUrl("Location/" + locationId);

        return entryComponent;
    }

    public Location createLocation(
            String locationId,
            List<String> identifierValues,
            String locationStatus,
            String locationName,
            List<ContactPoint> telecoms,
            Address address,
            String managingOrganizationId) {

        Location location = new Location();
        location.setId(locationId);

        // Set Meta
        Meta meta = new Meta();
        meta.addProfile("http://fhir.health.gov.lk/ips/StructureDefinition/providers-location");
        location.setMeta(meta);

        // Set Identifiers
        identifierValues.forEach(value -> {
            Identifier identifier = new Identifier();
            identifier.setType(new CodeableConcept(new Coding("http://fhir.health.gov.lk/ips/CodeSystem/cs-identifier-types", "PLOC", "Provider location identifier")));
            identifier.setSystem("http://fhir.health.gov.lk/ips/identifier/provider-location");
            identifier.setValue(value);
            location.addIdentifier(identifier);
        });

        // Set Status
        if (locationStatus != null && !locationStatus.trim().isEmpty()) {
            location.setStatus(Location.LocationStatus.fromCode(locationStatus.toLowerCase()));
        }

        // Set Name
        if (locationName != null && !locationName.trim().isEmpty()) {
            location.setName(locationName.trim());
        }

        // Set Telecoms
        if (telecoms != null) {
            location.setTelecom(telecoms);
        }

        // Set Address
        if (address != null) {
            location.setAddress(address);
        }

        // Set Managing Organization
        if (managingOrganizationId != null && !managingOrganizationId.trim().isEmpty()) {
            location.setManagingOrganization(new Reference("Organization/" + managingOrganizationId.trim()));
        }

        return location;
    }

    public Bundle.BundleEntryComponent createPractitionerEntry(WebUser user) {
        if (user == null) {
            return null;
        }
        if (user.getPerson() == null) {
            return null;
        }
        if (user.getUuid() == null) {
            user.setUuid(UUID.randomUUID().toString());
            if (user.getId() == null) {
                webUserFacade.create(user);
            } else {
                webUserFacade.edit(user);
            }
        }
        List<ContactPoint> telecoms = new ArrayList<>();
        ContactPoint p1 = new ContactPoint();

        if (user.getPerson().getPhone1() != null) {
            ContactPoint phoneContact = new ContactPoint();
            phoneContact.setSystem(ContactPointSystem.PHONE);
            phoneContact.setValue(user.getPerson().getPhone1());
            phoneContact.setUse(ContactPointUse.WORK);
            telecoms.add(phoneContact);
        }

        if (user.getPerson().getPhone2() != null) {
            ContactPoint phoneContact = new ContactPoint();
            phoneContact.setSystem(ContactPointSystem.PHONE);
            phoneContact.setValue(user.getPerson().getPhone2());
            phoneContact.setUse(ContactPointUse.HOME);
            telecoms.add(phoneContact);
        }

        if (user.getEmail() != null) {
            ContactPoint emailContact = new ContactPoint();
            emailContact.setSystem(ContactPointSystem.EMAIL);
            emailContact.setValue(user.getEmail());
            emailContact.setUse(ContactPointUse.HOME);
            telecoms.add(emailContact);
        }

        String id = user.getUuid();

        String title = null;
        if (user.getPerson().getTitle() != null) {
            title = user.getPerson().getTitle().getName();
        }
        String name = null;
        name = user.getPerson().getName();
        return createPractitionerEntry(telecoms, id, title, name);
    }

    public Bundle.BundleEntryComponent createPractitionerEntry(
            List<ContactPoint> telecoms,
            String id,
            String title,
            String name) {

        Practitioner practitioner = new Practitioner();
        practitioner.setId(id);

        // Set Meta
        Meta meta = new Meta();
        meta.addProfile("http://fhir.health.gov.lk/ips/StructureDefinition/practitioner");
        practitioner.setMeta(meta);

        // Set name
        HumanName humanName = new HumanName();
        if (title != null && !title.isEmpty()) {
            humanName.addPrefix(title);
        }
        humanName.setFamily(name); // Set the same name for the family
        humanName.addGiven(name); // Set the same name for given
        humanName.setText(name); // Also set the name as text
        practitioner.addName(humanName); // Correctly add the HumanName after setting all its fields

        // Set telecoms
        if (telecoms != null) {
            practitioner.setTelecom(telecoms);
        }

        // Generate a simple narrative
        String narrativeText = String.format("Practitioner: %s", name);
        Narrative narrative = new Narrative();
        narrative.setStatus(Narrative.NarrativeStatus.GENERATED);
        narrative.setDivAsString("<div xmlns=\"http://www.w3.org/1999/xhtml\">" + narrativeText + "</div>");
        practitioner.setText(narrative);

        // Creating Bundle Entry
        Bundle.BundleEntryComponent entryComponent = new Bundle.BundleEntryComponent();
        entryComponent.setFullUrl("http://hapi-fhir:8080/fhir/Practitioner/" + id);
        entryComponent.setResource(practitioner);
        entryComponent.getRequest().setMethod(Bundle.HTTPVerb.PUT).setUrl("Practitioner/" + id);

        return entryComponent;
    }

    public Bundle.BundleEntryComponent createDeviceEntry() {
        Institution moh = institutionController.findInstitutionByName("Ministry of Health");
        Preference device = preferenceController.findApplicationPreferance("device_id");
        String deviceId = device.getLongTextValue();
        if (deviceId == null || deviceId.trim().equals("")) {
            device.setLongTextValue(UUID.randomUUID().toString());
            preferenceController.savePreference(device);
        }

        if (moh == null) {
            moh = new Institution();
            moh.setName("Ministry of Health");
            moh.setInstitutionType(InstitutionType.Ministry_of_Health);
            institutionController.saveOrUpdateInstitution(moh);
        }
        Institution hiu = institutionController.findInstitutionByName("Health Information Unit");
        if (hiu == null) {
            hiu = new Institution();
            hiu.setName("Health Information Unit");
            hiu.setParent(moh);
            hiu.setInstitutionType(InstitutionType.Other);
            institutionController.saveOrUpdateInstitution(hiu);
        }

        Bundle.BundleEntryComponent orgEntry = createOrganizationEntry(moh);
        System.out.println("orgEntry = " + orgEntry);
        Bundle.BundleEntryComponent locationEntry = createLocationEntry(hiu);
        System.out.println("locationEntry = " + locationEntry);

        Organization owner = extractOrganizationFromEntry(orgEntry);
        System.out.println("owner = " + owner);
        Location location = extractLocationFromEntry(locationEntry);
        System.out.println("location = " + location);

        Coding typeCoding = new Coding();
        typeCoding.setSystem("http://snomed.info/sct");
        typeCoding.setCode("706690007");

        boolean active = true;
        String identifierValue = "Version";
        String versionNumber = "2.0.1";
        Coding versionCoding = new Coding();
        versionCoding.setSystem("http://snomed.info/sct");
        versionCoding.setCode("22303008");

        return createDeviceEntry(deviceId,
                owner,
                location,
                typeCoding,
                active,
                identifierValue,
                versionNumber,
                versionCoding);
    }

    public Bundle.BundleEntryComponent createDeviceEntry(
            String deviceId,
            Organization owner,
            Location location,
            Coding typeCoding,
            boolean active,
            String identifierValue,
            String versionNumber,
            Coding versionCoding) {

        Device device = new Device();
        // Set ID and Meta
        device.setId(deviceId);
        device.getMeta().addProfile("http://fhir.health.gov.lk/ips/StructureDefinition/device-information");

        // Set Identifier
        device.addIdentifier(new Identifier().setSystem("http://fhir.health.gov.lk/ips/identifier/system-id").setValue(identifierValue));

        // Set Status
        device.setStatus(active ? Device.FHIRDeviceStatus.ACTIVE : Device.FHIRDeviceStatus.INACTIVE);

        // Set Type
        device.setType(new CodeableConcept().addCoding(typeCoding));

        // Set Version
        Device.DeviceVersionComponent versionComponent = new Device.DeviceVersionComponent();
        versionComponent.setType(new CodeableConcept().addCoding(versionCoding));
        versionComponent.setValue(versionNumber);
        device.addVersion(versionComponent);

        // Set Owner and Location References
        System.out.println("owner = " + owner);
        if (owner != null) {
            device.setOwner(new Reference("Organization/" + owner.getIdElement().getIdPart()));
            System.out.println("owner.getIdElement().getIdPart() = " + owner.getIdElement().getIdPart());
        }
        System.out.println("location = " + location);
        if (location != null) {
            device.setLocation(new Reference("Location/" + location.getIdElement().getIdPart()));
            System.out.println("location.getIdElement().getIdPart() = " + location.getIdElement().getIdPart());
        }

        // Generate Narrative
        String narrativeText = "Device: " + identifierValue + (active ? " (Active)" : " (Inactive)")
                + ". Version: " + versionNumber + " - " + versionCoding.getCode();

        Narrative narrative = new Narrative();
        narrative.setStatus(Narrative.NarrativeStatus.GENERATED);
        narrative.setDivAsString("<div xmlns=\"http://www.w3.org/1999/xhtml\">" + narrativeText + "</div>");
        device.setText(narrative);

        // Bundle Entry Component
        Bundle.BundleEntryComponent entryComponent = new Bundle.BundleEntryComponent();
        entryComponent.setFullUrl("http://hapi-fhir:8080/fhir/Device/" + device.getId());
        entryComponent.setResource(device);
        entryComponent.getRequest().setMethod(Bundle.HTTPVerb.PUT).setUrl("Device/" + device.getId());

        return entryComponent;
    }

}
