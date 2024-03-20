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
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
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
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointUse;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.Device;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Range;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.RiskAssessment;
import org.hl7.fhir.r4.model.SimpleQuantity;
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
        return null;
    }

    public Organization extractOrganizationFromEntry(Bundle.BundleEntryComponent entry) {
        if (entry != null && entry.getResource() instanceof Organization) {
            return (Organization) entry.getResource();
        }
        return null;
    }

    public Location extractLocationFromEntry(Bundle.BundleEntryComponent entry) {
        if (entry != null && entry.getResource() instanceof Location) {
            return (Location) entry.getResource();
        }
        return null;
    }

    public Device extractDeviceFromEntry(Bundle.BundleEntryComponent entry) {
        if (entry != null && entry.getResource() instanceof Device) {
            return (Device) entry.getResource();
        }
        return null;
    }

    public Encounter extractEncounter(Bundle.BundleEntryComponent entry) {
        if (entry != null && entry.getResource() instanceof Encounter) {
            return (Encounter) entry.getResource();
        }
        return null;
    }

    public Observation extractObservation(Bundle.BundleEntryComponent entry) {
        if (entry != null && entry.getResource() instanceof Observation) {
            return (Observation) entry.getResource();
        }
        return null;
    }

    public Practitioner extractPractitionerFromEntry(Bundle.BundleEntryComponent entry) {
        if (entry != null && entry.getResource() instanceof Practitioner) {
            return (Practitioner) entry.getResource();
        }
        return null;
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

        // Generate a 5-digit numeric HIN if hinId is null or blank
        if (ins.getHin() == null || ins.getHin().trim().isEmpty()) {
            ins.setHin(generateRandomNumericHIN(5));
            institutionFacade.edit(ins);
        }
        String hinId = ins.getHin();

        String organizationName = ins.getName();
        return createOrganizationEntry(organizationId, hinId, organizationName);
    }

    public String generateRandomNumericHIN(int length) {
        Random random = new Random();
        StringBuilder hinId = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            hinId.append(random.nextInt(10)); // Append a random digit (0-9)
        }
        return hinId.toString();
    }

    public Bundle.BundleEntryComponent createOrganizationEntry(String organizationId, String hinId, String organizationName) {
        Organization organization = new Organization();
        organization.setId(organizationId);

        Meta meta = new Meta();
        meta.addProfile("http://fhir.health.gov.lk/ips/StructureDefinition/organization");
        organization.setMeta(meta);

        // Correctly adding HIN identifier with its value
        if (hinId != null && !hinId.isEmpty()) {
            Identifier hinIdentifier = new Identifier()
                    .setType(new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v2-0203", "XX", "Organization identifier")))
                    .setSystem("http://fhir.health.gov.lk/ips/identifier/hin")
                    .setValue(hinId); // Ensure this is the correct HIN value
            organization.addIdentifier(hinIdentifier);
        }

        if (organizationName != null && !organizationName.trim().isEmpty()) {
            organization.setName(organizationName.trim());
        }

        // Narrative construction remains the same
        String narrativeText = "Organization: " + organizationName + ". HIN: " + hinId + ". Summary or description of the organization.";
        Narrative narrative = new Narrative();
        narrative.setStatus(Narrative.NarrativeStatus.GENERATED);
        narrative.setDivAsString("<div xmlns=\"http://www.w3.org/1999/xhtml\">" + narrativeText + "</div>");
        organization.setText(narrative);

        Bundle.BundleEntryComponent entryComponent = new Bundle.BundleEntryComponent();
        entryComponent.setFullUrl("http://hapi-fhir:8080/fhir/Organization/" + organizationId);
        entryComponent.setResource(organization);
        entryComponent.getRequest().setMethod(Bundle.HTTPVerb.PUT).setUrl("Organization/" + organizationId);

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

        // Ensure at least one identifier is provided
        if (identifierValues != null && !identifierValues.isEmpty()) {
            identifierValues.forEach(value -> {
                if (value != null && !value.trim().isEmpty()) {
                    Identifier identifier = new Identifier();
                    identifier.setType(new CodeableConcept(new Coding("http://fhir.health.gov.lk/ips/CodeSystem/cs-identifier-types", "PLOC", "Provider Location")));
                    identifier.setSystem("http://fhir.health.gov.lk/ips/identifier/provider-location");
                    identifier.setValue(value.trim());
                    location.addIdentifier(identifier);
                }
            });
        }

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

        // Set Managing Organization Reference
        if (managingOrganizationId != null && !managingOrganizationId.trim().isEmpty()) {
            location.setManagingOrganization(new Reference("Organization/" + managingOrganizationId.trim()));
        }

        // Generate narrative text for robust management
        String narrativeText = "Location Name: " + (locationName != null ? locationName : "Not Specified");
        narrativeText += ". Status: " + (locationStatus != null ? locationStatus : "Not Specified");
        narrativeText += ". Status: " + locationStatus;

        Narrative narrative = new Narrative();
        narrative.setStatus(Narrative.NarrativeStatus.GENERATED);
        narrative.setDivAsString("<div xmlns=\"http://www.w3.org/1999/xhtml\">" + narrativeText + "</div>");
        location.setText(narrative);

        // Creating Bundle Entry
        Bundle.BundleEntryComponent entryComponent = new Bundle.BundleEntryComponent();
        entryComponent.setFullUrl("http://hapi-fhir:8080/fhir/Location/" + locationId);
        entryComponent.setResource(location);
        entryComponent.getRequest().setMethod(Bundle.HTTPVerb.PUT).setUrl("Location/" + locationId);

        return entryComponent;
    }

    public Bundle.BundleEntryComponent createBloodPressureObservationEntry(
            Patient patient,
            Encounter encounter,
            Organization organization,
            Location location,
            Device device,
            Practitioner practitioner,
            Date performedDate,
            Number systolicBP,
            Number diastolicBP,
            boolean active,
            String identifier) {

        Observation observation = new Observation();
        observation.setId(identifier);
        observation.setMeta(new Meta().addProfile("http://fhir.health.gov.lk/ips/StructureDefinition/blood-pressure"));
        observation.setStatus(active ? Observation.ObservationStatus.FINAL : Observation.ObservationStatus.AMENDED);
        observation.setCategory(Collections.singletonList(new CodeableConcept(new Coding("http://terminology.hl7.org/CodeSystem/observation-category", "vital-signs", "Vital Signs"))));
        observation.setCode(new CodeableConcept().addCoding(new Coding("http://loinc.org", "85354-9", "Blood Pressure")));
        observation.setSubject(new Reference("Patient/" + patient.getIdElement().getIdPart()));
        observation.setEncounter(new Reference("Encounter/" + encounter.getIdElement().getIdPart()));
        observation.setEffective(new DateTimeType(performedDate));
        observation.setPerformer(Arrays.asList(
                new Reference("Organization/" + organization.getIdElement().getIdPart()),
                new Reference("Practitioner/" + practitioner.getIdElement().getIdPart())
        ));
        observation.setDevice(new Reference("Device/" + device.getIdElement().getIdPart()));

        // Components for systolic and diastolic blood pressure
        Observation.ObservationComponentComponent systolicComponent = new Observation.ObservationComponentComponent()
                .setCode(new CodeableConcept().addCoding(new Coding("http://loinc.org", "8480-6", "Systolic Blood Pressure")))
                .setValue(new Quantity(systolicBP.doubleValue()).setUnit("mmHg").setSystem("http://unitsofmeasure.org").setCode("mm[Hg]"));
        Observation.ObservationComponentComponent diastolicComponent = new Observation.ObservationComponentComponent()
                .setCode(new CodeableConcept().addCoding(new Coding("http://loinc.org", "8462-4", "Diastolic Blood Pressure")))
                .setValue(new Quantity(diastolicBP.doubleValue()).setUnit("mmHg").setSystem("http://unitsofmeasure.org").setCode("mm[Hg]"));

        observation.setComponent(Arrays.asList(systolicComponent, diastolicComponent));

        // Constructing the narrative (text representation)
        Narrative narrative = new Narrative();
        narrative.setStatus(Narrative.NarrativeStatus.GENERATED);
        String div = "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p>Blood Pressure Observation for patient '"
                + patient.getName().stream().findFirst().orElse(new HumanName()).getText()
                + "'.</p><p>Systolic: " + systolicBP + " mmHg, Diastolic: " + diastolicBP + " mmHg.</p></div>";
        narrative.setDivAsString(div);
        observation.setText(narrative);

        Bundle.BundleEntryComponent entryComponent = new Bundle.BundleEntryComponent();
        entryComponent.setFullUrl("http://hapi-fhir:8080/fhir/Observation/" + identifier);
        entryComponent.setResource(observation);
        entryComponent.getRequest().setMethod(Bundle.HTTPVerb.PUT).setUrl("Observation/" + identifier);

        return entryComponent;
    }

    public Bundle.BundleEntryComponent createTotalCholesterolObservationEntry(
            Patient patient,
            Encounter encounter,
            Organization organization,
            Location location,
            Device device,
            Practitioner practitioner,
            Date performedDate,
            Number cholesterolValue,
            boolean active,
            String identifier) {

        Observation observation = new Observation();
        observation.setId(identifier);
        observation.setMeta(new Meta().addProfile("http://fhir.health.gov.lk/ips/StructureDefinition/total-cholesterol"));
        observation.setStatus(active ? Observation.ObservationStatus.FINAL : Observation.ObservationStatus.AMENDED);
        observation.setCategory(Collections.singletonList(new CodeableConcept(new Coding("http://terminology.hl7.org/CodeSystem/observation-category", "laboratory", "Laboratory"))));
        observation.setCode(new CodeableConcept().addCoding(new Coding("http://loinc.org", "2093-3", "Cholesterol")));
        observation.setSubject(new Reference("Patient/" + patient.getIdElement().getIdPart()));
        observation.setEncounter(new Reference("Encounter/" + encounter.getIdElement().getIdPart()));
        observation.setEffective(new DateTimeType(performedDate));
        observation.setPerformer(Arrays.asList(
                new Reference("Organization/" + organization.getIdElement().getIdPart()),
                new Reference("Practitioner/" + practitioner.getIdElement().getIdPart())
        ));
        observation.setDevice(new Reference("Device/" + device.getIdElement().getIdPart()));

        // Value for total cholesterol
        observation.setValue(new Quantity(cholesterolValue.doubleValue()).setUnit("mmol/L").setSystem("http://unitsofmeasure.org").setCode("mmol/L"));

        // Interpretation of cholesterol value
        CodeableConcept interpretation = new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation", "H", "High"));
        observation.setInterpretation(Collections.singletonList(interpretation));

        // Reference range for total cholesterol
        Observation.ObservationReferenceRangeComponent referenceRange = new Observation.ObservationReferenceRangeComponent()
                .setHigh(new SimpleQuantity().setValue(5).setUnit("mmol/L").setSystem("http://unitsofmeasure.org").setCode("mmol/L"));
        observation.setReferenceRange(Collections.singletonList(referenceRange));

        // Constructing the narrative (text representation)
        Narrative narrative = new Narrative();
        narrative.setStatus(Narrative.NarrativeStatus.GENERATED);
        String div = "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p>Total Cholesterol Observation for patient '"
                + patient.getName().stream().findFirst().orElse(new HumanName()).getText()
                + "'.</p><p>Value: " + cholesterolValue + " mmol/L. Interpretation: High. Reference range: Up to 5 mmol/L.</p></div>";
        narrative.setDivAsString(div);
        observation.setText(narrative);

        Bundle.BundleEntryComponent entryComponent = new Bundle.BundleEntryComponent();
        entryComponent.setFullUrl("http://hapi-fhir:8080/fhir/Observation/" + identifier);
        entryComponent.setResource(observation);
        entryComponent.getRequest().setMethod(Bundle.HTTPVerb.PUT).setUrl("Observation/" + identifier);

        return entryComponent;
    }

    public Bundle.BundleEntryComponent createFastingBloodSugarObservationEntry(
            Patient patient,
            Encounter encounter,
            Organization organization,
            Location location,
            Device device,
            Practitioner practitioner,
            Date performedDate,
            Number fastingBloodSugarValue,
            boolean active,
            String identifier) {

        Observation observation = new Observation();
        observation.setId(identifier);
        observation.setMeta(new Meta().addProfile("http://fhir.health.gov.lk/ips/StructureDefinition/fasting-blood-sugar"));
        observation.setStatus(active ? Observation.ObservationStatus.FINAL : Observation.ObservationStatus.AMENDED);
        observation.setCategory(Collections.singletonList(new CodeableConcept(new Coding("http://terminology.hl7.org/CodeSystem/observation-category", "laboratory", "Laboratory"))));
        observation.setCode(new CodeableConcept().addCoding(new Coding("http://loinc.org", "76629-5", "Fasting glucose")));
        observation.setSubject(new Reference("Patient/" + patient.getIdElement().getIdPart()));
        observation.setEncounter(new Reference("Encounter/" + encounter.getIdElement().getIdPart()));
        observation.setEffective(new DateTimeType(performedDate));
        observation.setPerformer(Arrays.asList(
                new Reference("Organization/" + organization.getIdElement().getIdPart()),
                new Reference("Practitioner/" + practitioner.getIdElement().getIdPart())
        ));
        observation.setDevice(new Reference("Device/" + device.getIdElement().getIdPart()));

        // Value for fasting blood sugar
        observation.setValue(new Quantity(fastingBloodSugarValue.doubleValue()).setUnit("mmol/L").setSystem("http://unitsofmeasure.org").setCode("mmol/L"));

        // Interpretation of fasting blood sugar value
        CodeableConcept interpretation = new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation", "N", "Normal"));
        observation.setInterpretation(Collections.singletonList(interpretation));

        // Reference range for fasting blood sugar
        Observation.ObservationReferenceRangeComponent referenceRange = new Observation.ObservationReferenceRangeComponent()
                .setLow(new SimpleQuantity().setValue(3.9).setUnit("mmol/L").setSystem("http://unitsofmeasure.org").setCode("mmol/L"))
                .setHigh(new SimpleQuantity().setValue(5.6).setUnit("mmol/L").setSystem("http://unitsofmeasure.org").setCode("mmol/L"));
        observation.setReferenceRange(Collections.singletonList(referenceRange));

        // Constructing the narrative (text representation)
        Narrative narrative = new Narrative();
        narrative.setStatus(Narrative.NarrativeStatus.GENERATED);
        String div = "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p>Fasting Blood Sugar Observation for patient '"
                + patient.getName().stream().findFirst().orElse(new HumanName()).getText()
                + "'.</p><p>Value: " + fastingBloodSugarValue + " mmol/L. Interpretation: Normal. Reference range: 3.9 to 5.6 mmol/L.</p></div>";
        narrative.setDivAsString(div);
        observation.setText(narrative);

        Bundle.BundleEntryComponent entryComponent = new Bundle.BundleEntryComponent();
        entryComponent.setFullUrl("http://hapi-fhir:8080/fhir/Observation/" + identifier);
        entryComponent.setResource(observation);
        entryComponent.getRequest().setMethod(Bundle.HTTPVerb.PUT).setUrl("Observation/" + identifier);

        return entryComponent;
    }

    public Bundle.BundleEntryComponent createBMIObservationEntry(
            Patient patient,
            Encounter encounter,
            Organization organization,
            Location location,
            Device device,
            Practitioner practitioner,
            Date performedDate,
            Number bmiValue,
            boolean active,
            String identifier,
            Observation weightObservation,
            Observation heightObservation) {

        Observation observation = new Observation();
        observation.setId(identifier);
        observation.setMeta(new Meta().addProfile("http://fhir.health.gov.lk/ips/StructureDefinition/bmi"));
        observation.setStatus(active ? Observation.ObservationStatus.FINAL : Observation.ObservationStatus.AMENDED);
        observation.setCategory(Collections.singletonList(new CodeableConcept(new Coding("http://terminology.hl7.org/CodeSystem/observation-category", "vital-signs", null))));
        observation.setCode(new CodeableConcept().addCoding(new Coding("http://loinc.org", "39156-5", "Body mass index (BMI)")));
        observation.setSubject(new Reference("Patient/" + patient.getIdElement().getIdPart()));
        observation.setEncounter(new Reference("Encounter/" + encounter.getIdElement().getIdPart()));
        observation.setEffective(new DateTimeType(performedDate)); // Ensure this date is in the correct format
        observation.setPerformer(Arrays.asList(
                new Reference("Organization/" + organization.getIdElement().getIdPart()),
                new Reference("Practitioner/" + practitioner.getIdElement().getIdPart())
        ));
        observation.setDevice(new Reference("Device/" + device.getIdElement().getIdPart()));
        observation.setValue(new Quantity(bmiValue.doubleValue()).setUnit("kg/m2").setSystem("http://unitsofmeasure.org").setCode("kg/m2"));
        observation.setDerivedFrom(Arrays.asList(
                new Reference("Observation/" + weightObservation.getIdElement().getIdPart()),
                new Reference("Observation/" + heightObservation.getIdElement().getIdPart())
        ));

        Narrative narrative = new Narrative().setStatus(Narrative.NarrativeStatus.GENERATED);
        String div = "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p>BMI Observation for patient '"
                + patient.getName().stream().findFirst().orElse(new HumanName()).getText()
                + "'.</p><p>BMI: " + bmiValue + " kg/m2.</p><p>Derived from weight and height observations.</p></div>";
        narrative.setDivAsString(div);
        observation.setText(narrative);

        Bundle.BundleEntryComponent entryComponent = new Bundle.BundleEntryComponent();
        entryComponent.setFullUrl("http://hapi-fhir:8080/fhir/Observation/" + identifier);
        entryComponent.setResource(observation);
        entryComponent.getRequest().setMethod(Bundle.HTTPVerb.PUT).setUrl("Observation/" + identifier);

        return entryComponent;
    }

    public Bundle.BundleEntryComponent createWeightObservationEntry(
            Patient patient,
            Encounter encounter,
            Organization organization,
            Location location,
            Device device,
            Practitioner practitioner,
            Date performedDate,
            Number weightValue,
            boolean active,
            String identifier) {

        Observation observation = new Observation();
        observation.setId(identifier);
        observation.setMeta(new Meta().addProfile("http://fhir.health.gov.lk/ips/StructureDefinition/weight"));
        observation.setStatus(active ? Observation.ObservationStatus.FINAL : Observation.ObservationStatus.AMENDED);
        observation.setCategory(Collections.singletonList(new CodeableConcept(new Coding("http://terminology.hl7.org/CodeSystem/observation-category", "vital-signs", "Vital Signs"))));
        observation.setCode(new CodeableConcept().addCoding(new Coding("http://loinc.org", "29463-7", "Body Weight")));
        observation.setSubject(new Reference("Patient/" + patient.getIdElement().getIdPart()));
        observation.setEncounter(new Reference("Encounter/" + encounter.getIdElement().getIdPart()));
        observation.setEffective(new DateTimeType(performedDate));
        observation.setPerformer(Arrays.asList(
                new Reference("Organization/" + organization.getIdElement().getIdPart()),
                new Reference("Practitioner/" + practitioner.getIdElement().getIdPart())
        ));
        observation.setDevice(new Reference("Device/" + device.getIdElement().getIdPart()));

        // Value for weight
        observation.setValue(new Quantity(weightValue.doubleValue()).setUnit("kg").setSystem("http://unitsofmeasure.org").setCode("kg"));

        // Constructing the narrative (text representation) directly
        Narrative narrative = new Narrative();
        narrative.setStatus(Narrative.NarrativeStatus.GENERATED);
        String div = "<div xmlns=\"http://www.w3.org/1999/xhtml\">"
                + "<p>Weight Observation for patient '"
                + patient.getName().stream().findFirst().orElse(new HumanName()).getText()
                + "'.</p><p>Weight: " + weightValue + " kg.</p></div>";
        narrative.setDivAsString(div);
        observation.setText(narrative);

        Bundle.BundleEntryComponent entryComponent = new Bundle.BundleEntryComponent();
        entryComponent.setFullUrl("http://hapi-fhir:8080/fhir/Observation/" + identifier);
        entryComponent.setResource(observation);
        entryComponent.getRequest().setMethod(Bundle.HTTPVerb.PUT).setUrl("Observation/" + identifier);

        return entryComponent;
    }

    public Bundle.BundleEntryComponent createHeightObservationEntry(
            Patient patient,
            Encounter encounter,
            Organization organization,
            Location location,
            Device device,
            Practitioner practitioner,
            Date performedDate,
            Number heightValue,
            boolean active,
            String identifier) {

        Observation observation = new Observation();
        observation.setId(identifier);
        observation.setMeta(new Meta().addProfile("http://fhir.health.gov.lk/ips/StructureDefinition/height"));
        observation.setStatus(active ? Observation.ObservationStatus.FINAL : Observation.ObservationStatus.AMENDED);
        observation.setCategory(Collections.singletonList(new CodeableConcept(new Coding("http://terminology.hl7.org/CodeSystem/observation-category", "vital-signs", "Vital Signs"))));
        observation.setCode(new CodeableConcept().addCoding(new Coding("http://loinc.org", "8302-2", "Body Height")));
        observation.setSubject(new Reference("Patient/" + patient.getIdElement().getIdPart()));
        observation.setEncounter(new Reference("Encounter/" + encounter.getIdElement().getIdPart()));
        observation.setEffective(new DateTimeType(performedDate));
        observation.setPerformer(Arrays.asList(
                new Reference("Organization/" + organization.getIdElement().getIdPart()),
                new Reference("Practitioner/" + practitioner.getIdElement().getIdPart())
        ));
        observation.setDevice(new Reference("Device/" + device.getIdElement().getIdPart()));

        // Value for height
        observation.setValue(new Quantity(heightValue.doubleValue()).setUnit("cm").setSystem("http://unitsofmeasure.org").setCode("cm"));

        // Constructing the narrative (text representation) with corrected div setting
        Narrative narrative = new Narrative();
        narrative.setStatus(Narrative.NarrativeStatus.GENERATED);
        String div = "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p>Height Observation for patient '"
                + patient.getName().stream().findFirst().orElse(new HumanName()).getText()
                + "'.</p><p>Height: " + heightValue + " cm.</p></div>";
        narrative.setDivAsString(div);
        observation.setText(narrative);

        Bundle.BundleEntryComponent entryComponent = new Bundle.BundleEntryComponent();
        entryComponent.setFullUrl("http://hapi-fhir:8080/fhir/Observation/" + identifier);
        entryComponent.setResource(observation);
        entryComponent.getRequest().setMethod(Bundle.HTTPVerb.PUT).setUrl("Observation/" + identifier);

        return entryComponent;
    }

    public Bundle.BundleEntryComponent createMedicalHistoryConditionEntry(
            Patient patient,
            Encounter encounter,
            Practitioner recorder,
            Practitioner asserter,
            Date recordedDate,
            Boolean active,
            Boolean confirmed,
            String system,
            String code,
            String name) {

        Condition condition = new Condition();
        condition.setId("MedicalHistoryExample");
        condition.setMeta(new Meta().addProfile("http://fhir.health.gov.lk/ips/StructureDefinition/medical-history"));
        condition.setClinicalStatus(new CodeableConcept(new Coding("http://terminology.hl7.org/CodeSystem/condition-clinical", active ? "active" : "inactive", null)));
        condition.setVerificationStatus(new CodeableConcept(new Coding("http://terminology.hl7.org/CodeSystem/condition-ver-status", confirmed ? "confirmed" : "unconfirmed", null)));
        condition.setCategory(Arrays.asList(new CodeableConcept(new Coding("http://terminology.hl7.org/CodeSystem/condition-category", "problem-list-item", null))));
        condition.setCode(new CodeableConcept().addCoding(new Coding(system, code, name)));
        condition.setSubject(new Reference("Patient/" + patient.getIdElement().getIdPart()));
        condition.setEncounter(new Reference("Encounter/" + encounter.getIdElement().getIdPart()));
        condition.setRecordedDate(recordedDate);
        condition.setRecorder(new Reference("Practitioner/" + recorder.getIdElement().getIdPart()));
        condition.setAsserter(new Reference("Practitioner/" + asserter.getIdElement().getIdPart()));

        // Constructing the narrative (text representation)
        Narrative narrative = new Narrative();
        narrative.setStatus(Narrative.NarrativeStatus.GENERATED);
        String div = String.format("<div xmlns=\"http://www.w3.org/1999/xhtml\"><p><b>Generated Narrative: Condition</b></p><p><b>clinicalStatus</b>: %s</p><p><b>verificationStatus</b>: %s</p><p><b>category</b>: Problem List Item</p><p><b>code</b>: %s (%s#%s)</p><p><b>subject</b>: %s</p><p><b>encounter</b>: %s</p><p><b>recordedDate</b>: %s</p><p><b>recorder</b>: %s</p><p><b>asserter</b>: %s</p></div>",
                active ? "Active" : "Inactive",
                confirmed ? "Confirmed" : "Unconfirmed",
                name, system, code,
                "Patient/PatientExample", // This and the following references should be dynamically generated based on actual data
                "Encounter/TargetFacilityEncounterExample",
                recordedDate.toString(),
                "Practitioner/GeneralPractitionerExample",
                "Practitioner/GeneralPractitionerExample");
        narrative.setDivAsString(div);
        condition.setText(narrative);

        Bundle.BundleEntryComponent entryComponent = new Bundle.BundleEntryComponent();
        entryComponent.setFullUrl("urn:uuid:" + condition.getId());
        entryComponent.setResource(condition);

        return entryComponent;
    }

    public Bundle.BundleEntryComponent createRandomBloodSugarObservationEntry(
            Patient patient,
            Encounter encounter,
            Organization organization,
            Location location,
            Device device,
            Practitioner practitioner,
            Date performedDate,
            Number randomBloodSugarValue,
            boolean active,
            String identifier) {

        Observation observation = new Observation();
        observation.setId(identifier);
        observation.setMeta(new Meta().addProfile("http://fhir.health.gov.lk/ips/StructureDefinition/random-blood-sugar"));
        observation.setStatus(active ? Observation.ObservationStatus.FINAL : Observation.ObservationStatus.AMENDED);
        observation.setCategory(Collections.singletonList(new CodeableConcept(new Coding("http://terminology.hl7.org/CodeSystem/observation-category", "laboratory", "Laboratory"))));
        observation.setCode(new CodeableConcept().addCoding(new Coding("http://loinc.org", "15074-8", "Glucose")));
        observation.setSubject(new Reference("Patient/" + patient.getIdElement().getIdPart()));
        observation.setEncounter(new Reference("Encounter/" + encounter.getIdElement().getIdPart()));
        observation.setEffective(new DateTimeType(performedDate));
        observation.setPerformer(Arrays.asList(
                new Reference("Organization/" + organization.getIdElement().getIdPart()),
                new Reference("Practitioner/" + practitioner.getIdElement().getIdPart())
        ));
        observation.setDevice(new Reference("Device/" + device.getIdElement().getIdPart()));

        // Value for random blood sugar
        observation.setValue(new Quantity(randomBloodSugarValue.doubleValue()).setUnit("mmol/L").setSystem("http://unitsofmeasure.org").setCode("mmol/L"));

        // Interpretation of random blood sugar value
        CodeableConcept interpretation = new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation", "H", "High"));
        observation.setInterpretation(Collections.singletonList(interpretation));

        // Reference range for random blood sugar
        Observation.ObservationReferenceRangeComponent referenceRange = new Observation.ObservationReferenceRangeComponent()
                .setLow(new SimpleQuantity().setValue(3.1).setUnit("mmol/L").setSystem("http://unitsofmeasure.org").setCode("mmol/L"))
                .setHigh(new SimpleQuantity().setValue(6.2).setUnit("mmol/L").setSystem("http://unitsofmeasure.org").setCode("mmol/L"));
        observation.setReferenceRange(Collections.singletonList(referenceRange));

        // Constructing the narrative (text representation)
        Narrative narrative = new Narrative();
        narrative.setStatus(Narrative.NarrativeStatus.GENERATED);
        String div = "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p>Random Blood Sugar Observation for patient '"
                + patient.getName().stream().findFirst().orElse(new HumanName()).getText()
                + "'.</p><p>Value: " + randomBloodSugarValue + " mmol/L. Interpretation: High. Reference range: 3.1 to 6.2 mmol/L.</p></div>";
        narrative.setDivAsString(div);
        observation.setText(narrative);

        Bundle.BundleEntryComponent entryComponent = new Bundle.BundleEntryComponent();
        entryComponent.setFullUrl("http://hapi-fhir:8080/fhir/Observation/" + identifier);
        entryComponent.setResource(observation);
        entryComponent.getRequest().setMethod(Bundle.HTTPVerb.PUT).setUrl("Observation/" + identifier);

        return entryComponent;
    }

    public Bundle.BundleEntryComponent createTobaccoSmokerObservationEntry(
            Patient patient,
            Encounter encounter,
            Organization organization,
            Location location,
            Device device,
            Practitioner practitioner,
            Date performedDate,
            boolean isSmoker,
            boolean active,
            String identifier) {

        Observation observation = new Observation();
        observation.setId(identifier);
        observation.setMeta(new Meta().addProfile("http://fhir.health.gov.lk/ips/StructureDefinition/risk-behaviour-tobacco-smoker"));
        observation.setStatus(active ? Observation.ObservationStatus.FINAL : Observation.ObservationStatus.AMENDED);
        observation.setCategory(Collections.singletonList(new CodeableConcept(new Coding("http://terminology.hl7.org/CodeSystem/observation-category", "social-history", "Social History"))));
        observation.setCode(new CodeableConcept().addCoding(new Coding("http://loinc.org", "81229-7", "Tobacco smoking status for tobacco smoker")));
        observation.setSubject(new Reference("Patient/" + patient.getIdElement().getIdPart()));
        observation.setEncounter(new Reference("Encounter/" + encounter.getIdElement().getIdPart()));
        observation.setEffective(new DateTimeType(performedDate));
        observation.setPerformer(Arrays.asList(
                new Reference("Organization/" + organization.getIdElement().getIdPart()),
                new Reference("Practitioner/" + practitioner.getIdElement().getIdPart())
        ));
        observation.setDevice(new Reference("Device/" + device.getIdElement().getIdPart()));

        // Set the value based on isSmoker boolean
        String smokingStatus = isSmoker ? "Current every day smoker" : "Never smoker";
        String smokingCode = isSmoker ? "LA15920-4" : "LA18976-3"; // Example codes, please verify against the actual coding system
        observation.setValue(new CodeableConcept().addCoding(new Coding("http://loinc.org", smokingCode, smokingStatus)));

        // Constructing the narrative (text representation)
        Narrative narrative = new Narrative();
        narrative.setStatus(Narrative.NarrativeStatus.GENERATED);
        String div = "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p>Tobacco Smoker Observation for patient '"
                + patient.getName().stream().findFirst().orElse(new HumanName()).getText()
                + "'.</p><p>Status: " + smokingStatus + ".</p></div>";
        observation.setText(narrative);

        Bundle.BundleEntryComponent entryComponent = new Bundle.BundleEntryComponent();
        entryComponent.setFullUrl("http://hapi-fhir:8080/fhir/Observation/" + identifier);
        entryComponent.setResource(observation);
        entryComponent.getRequest().setMethod(Bundle.HTTPVerb.PUT).setUrl("Observation/" + identifier);

        return entryComponent;
    }

    public Bundle.BundleEntryComponent createCVDRiskAssessmentEntry(
            Patient patient,
            Encounter encounter,
            Organization organization,
            Practitioner practitioner,
            Date performedDate,
            double probability,
            String cvdRiskCategoryText,
            List<Observation> basisObservations) {

        RiskAssessment riskAssessment = new RiskAssessment();
        riskAssessment.setId("CVDRiskCategoryExample");
        riskAssessment.setMeta(new Meta().addProfile("http://fhir.health.gov.lk/ips/StructureDefinition/cvd-risk-category"));
        riskAssessment.setStatus(RiskAssessment.RiskAssessmentStatus.FINAL);
        riskAssessment.setCode(new CodeableConcept(new Coding("http://snomed.info/sct", "441829007", "Assessment for risk of cardiovascular disease")));
        riskAssessment.setSubject(new Reference("Patient/" + patient.getIdElement().getIdPart()));
        riskAssessment.setEncounter(new Reference("Encounter/" + encounter.getIdElement().getIdPart()));
        riskAssessment.setOccurrence(new DateTimeType(performedDate));
        riskAssessment.setPerformer(new Reference("Practitioner/" + practitioner.getIdElement().getIdPart()));

        // Convert Observations to References safely
        List<Reference> basisReferences = new ArrayList<>();
        for (Observation obs : basisObservations) {
            if (obs != null && obs.getIdElement() != null && obs.getIdElement().getIdPart() != null) {
                basisReferences.add(new Reference("Observation/" + obs.getIdElement().getIdPart()));
            }
        }
        riskAssessment.setBasis(basisReferences);

        RiskAssessment.RiskAssessmentPredictionComponent prediction = new RiskAssessment.RiskAssessmentPredictionComponent();
        prediction.setOutcome(new CodeableConcept(new Coding("http://snomed.info/sct", "395112001", "Risk of heart attack")));
        prediction.setProbability(new DecimalType(probability));
        prediction.setQualitativeRisk(new CodeableConcept(new Coding("http://fhir.health.gov.lk/ips/CodeSystem/cs-cvd-risk-category", "Critical", cvdRiskCategoryText)));

        prediction.setWhen(new Range().setLow(new SimpleQuantity().setValue(39).setUnit("years").setSystem("http://unitsofmeasure.org").setCode("a"))
                .setHigh(new SimpleQuantity().setValue(49).setUnit("years").setSystem("http://unitsofmeasure.org").setCode("a")));

        riskAssessment.setPrediction(Collections.singletonList(prediction));

        // Setting narrative
        String narrativeText = String.format("<div xmlns=\"http://www.w3.org/1999/xhtml\"><p><b>Generated Narrative with Details</b></p><p><b>Status</b>: %s</p><p><b>Code</b>: %s</p><p><b>Subject</b>: %s</p><p><b>Encounter</b>: %s</p><p><b>Occurrence</b>: %s</p><p><b>Performer</b>: %s</p><p><b>Risk Assessment Detail</b>: Probability - %s%%; Age range - 39-49 years.</p></div>",
                riskAssessment.getStatus().getDisplay(),
                riskAssessment.getCode().getCodingFirstRep().getDisplay(),
                patient.getName().stream().findFirst().orElse(new HumanName()).getText(),
                "Encounter/" + encounter.getIdElement().getIdPart(),
                performedDate.toString(),
                "Practitioner/" + practitioner.getIdElement().getIdPart(),
                probability);

        riskAssessment.getText().setStatus(Narrative.NarrativeStatus.GENERATED);
        riskAssessment.getText().setDivAsString(narrativeText);

        Bundle.BundleEntryComponent entryComponent = new Bundle.BundleEntryComponent();
        entryComponent.setFullUrl("http://hapi-fhir:8080/fhir/RiskAssessment/" + riskAssessment.getId());
        entryComponent.setResource(riskAssessment);
        entryComponent.getRequest().setMethod(Bundle.HTTPVerb.PUT).setUrl("RiskAssessment/" + riskAssessment.getId());

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
        // System.out.println("orgEntry = " + orgEntry);
        Bundle.BundleEntryComponent locationEntry = createLocationEntry(hiu);
        // System.out.println("locationEntry = " + locationEntry);

        Organization owner = extractOrganizationFromEntry(orgEntry);
        // System.out.println("owner = " + owner);
        Location location = extractLocationFromEntry(locationEntry);

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

        // Set Owner and Location References
        // System.out.println("owner = " + owner);
        if (owner != null) {
            device.setOwner(new Reference("Organization/" + owner.getIdElement().getIdPart()));
        }
        if (location != null) {
            device.setLocation(new Reference("Location/" + location.getIdElement().getIdPart()));
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
