/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.gov.health.phsp.entity;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Resource;

/**
 *
 * @author buddh
 */
public class FhirOperationResult {

    private boolean success;
    private String message;
    private IdType resourceId;
    private Resource resource;
    private Bundle responseBundle; 
    private OperationOutcome operationOutcome;

    // Getter and setter for responseBundle
    public Bundle getResponseBundle() {
        return responseBundle;
    }

    public void setResponseBundle(Bundle responseBundle) {
        this.responseBundle = responseBundle;
    }

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

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public OperationOutcome getOperationOutcome() {
        return operationOutcome;
    }

    public void setOperationOutcome(OperationOutcome operationOutcome) {
        this.operationOutcome = operationOutcome;
    }
    
    

    
    
    
}
