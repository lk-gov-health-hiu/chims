/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package lk.gov.health.phsp.enums;

/**
 *
 * @author buddh
 */
public enum CommunicationProtocol {
    FHIR_R4("FHIR R4"),
    FHIR_R5("FHIR R5"),
    HL7_2_5("HL7 2.5");

    private final String protocol;

    CommunicationProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getProtocol() {
        return protocol;
    }
}
