/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package lk.gov.health.phsp.enums;

/**
 *
 * @author buddh
 */
public enum IntegrationEndpointType {
    REST_SERVER("RestServer"),
    REST_CLIENT("RestClient");

    private final String type;

    IntegrationEndpointType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
