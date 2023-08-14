/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package lk.gov.health.phsp.entity;

/**
 *
 * @author buddh
 */
public enum SecurityProtocol {
    NO_AUTHENTICATION("No Authentication"),
    BASIC_AUTHENTICATION("Basic Authentication"),
    API_KEY("API Key"),
    KEYCLOAK("KeyCloak"),
    OAUTH2("OAuth2");

    private final String protocol;

    SecurityProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getProtocol() {
        return protocol;
    }
}
