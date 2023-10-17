package lk.gov.health.phsp.entity;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import lk.gov.health.phsp.pojcs.Identifiable;

/**
 *
 * @author buddhika.ari@gmail.com & ChatGPT
 */
@Entity
public class FhirResourceLink implements Serializable, Identifiable  {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String objectType; // Type of object in your application (e.g., "Client", "Encounter")

    private Long objectId; // ID of the object in your application

    private String fhirResourceId; // ID of the corresponding FHIR resource

    @ManyToOne
    private IntegrationEndpoint integrationEndpoint; // Reference to the endpoint where the FHIR resource is located

    
    
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof FhirResourceLink)) {
            return false;
        }
        FhirResourceLink other = (FhirResourceLink) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "lk.gov.health.phsp.entity.FhirResourceLink[ id=" + id + " ]";
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public String getFhirResourceId() {
        return fhirResourceId;
    }

    public void setFhirResourceId(String fhirResourceId) {
        this.fhirResourceId = fhirResourceId;
    }

    public IntegrationEndpoint getIntegrationEndpoint() {
        return integrationEndpoint;
    }

    public void setIntegrationEndpoint(IntegrationEndpoint integrationEndpoint) {
        this.integrationEndpoint = integrationEndpoint;
    }

}
