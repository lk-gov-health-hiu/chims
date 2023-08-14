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
    PATIENT_SEARCH("PatientSearch"),
    PATIENT_SAVE("PatientSave");

    private final String event;

    IntegrationEvent(String event) {
        this.event = event;
    }

    public String getEvent() {
        return event;
    }
}
