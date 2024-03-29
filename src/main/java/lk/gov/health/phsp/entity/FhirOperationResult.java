/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.gov.health.phsp.entity;

import org.hl7.fhir.r4.model.IdType;

/**
 *
 * @author buddh
 */
public class FhirOperationResult {

    private boolean success;
    private String message;
    private IdType resourceId;

    public FhirOperationResult() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public IdType getResourceId() {
        return resourceId;
    }

    public void setResourceId(IdType resourceId) {
        this.resourceId = resourceId;
    }

    
    
}
