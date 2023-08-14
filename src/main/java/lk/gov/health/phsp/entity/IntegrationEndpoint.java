/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.gov.health.phsp.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import lk.gov.health.phsp.enums.CommunicationProtocol;
import lk.gov.health.phsp.enums.IntegrationEndpointType;

/**
 *
 * @author buddh
 */
@Entity
public class IntegrationEndpoint implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private IntegrationEndpointType endpointType;
    @Enumerated(EnumType.STRING)
    private CommunicationProtocol communicationProtocol;
    @Enumerated(EnumType.STRING)
    private SecurityProtocol securityProtocol;
    private String endPointUrl;
    private String name;
    private String description;
    private String userName;
    private String password;
    private String apiKey;

    /*
    Create Properties
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser createdBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdAt;
    /*
    Retire Properties
     */
    private boolean retired;
    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser retiredBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date retiredAt;
    private String retireComments;

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
        if (!(object instanceof IntegrationEndpoint)) {
            return false;
        }
        IntegrationEndpoint other = (IntegrationEndpoint) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "lk.gov.health.phsp.entity.IntegrationEndpoint[ id=" + id + " ]";
    }

    public IntegrationEndpointType getEndpointType() {
        return endpointType;
    }

    public void setEndpointType(IntegrationEndpointType endpointType) {
        this.endpointType = endpointType;
    }

    public CommunicationProtocol getCommunicationProtocol() {
        return communicationProtocol;
    }

    public void setCommunicationProtocol(CommunicationProtocol communicationProtocol) {
        this.communicationProtocol = communicationProtocol;
    }

    public SecurityProtocol getSecurityProtocol() {
        return securityProtocol;
    }

    public void setSecurityProtocol(SecurityProtocol securityProtocol) {
        this.securityProtocol = securityProtocol;
    }

    public String getEndPointUrl() {
        return endPointUrl;
    }

    public void setEndPointUrl(String endPointUrl) {
        this.endPointUrl = endPointUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public WebUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(WebUser createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRetired() {
        return retired;
    }

    public void setRetired(boolean retired) {
        this.retired = retired;
    }

    public WebUser getRetiredBy() {
        return retiredBy;
    }

    public void setRetiredBy(WebUser retiredBy) {
        this.retiredBy = retiredBy;
    }

    public Date getRetiredAt() {
        return retiredAt;
    }

    public void setRetiredAt(Date retiredAt) {
        this.retiredAt = retiredAt;
    }

    public String getRetireComments() {
        return retireComments;
    }

    public void setRetireComments(String retireComments) {
        this.retireComments = retireComments;
    }

}