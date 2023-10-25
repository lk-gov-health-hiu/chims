/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package lk.gov.health.phsp.enums;

/**
 *
 * @author buddh
 */
public enum IntegrationEvent {
    PATIENT_SEARCH("Patient Search"),
    PATIENT_SAVE("Patient Save"),
    ENCOUNTER_SAVE("Encounter Save"),
    ORGANIZATION_SAVE("Organization Save"),
    LOCATION_SAVE("Location Save"),
    MEDIATORS("Mediators");

    private final String event;

    IntegrationEvent(String event) {
        this.event = event;
    }

    public String getEvent() {
        return event;
    }
}
