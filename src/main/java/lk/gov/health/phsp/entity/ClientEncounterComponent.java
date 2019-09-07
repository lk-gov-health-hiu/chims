/*
 * To change this license header, choose License Headers in Client Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lk.gov.health.phsp.entity;

import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author buddhika
 */
@Entity
@XmlRootElement
public class ClientEncounterComponent extends Component {

    @ManyToOne
    private Encounter encounter;

    @ManyToOne
    private Client client;

    @Lob
    private String longTextValue;
    private String shortTextValue;
    private byte[] byteArrayValue;
    private Integer integerNumberValue;
    private Double realNumberValue;
    private Boolean booleanValue;
    @ManyToOne
    private Item itemValue;
    @ManyToOne
    private Area areaValue;
    @ManyToOne
    private Institution institutionValue;
    @ManyToOne
    private Client clientValue;
    
    @OneToMany
    private List<Item> itemValues;
    @OneToMany
    private List<Area> areaValues;
    @OneToMany
    private List<Institution> institutionValues;
    @OneToMany
    private List<Client> clientValues;

    public Encounter getEncounter() {
        return encounter;
    }

    public void setEncounter(Encounter encounter) {
        this.encounter = encounter;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getLongTextValue() {
        return longTextValue;
    }

    public void setLongTextValue(String longTextValue) {
        this.longTextValue = longTextValue;
    }

    public String getShortTextValue() {
        return shortTextValue;
    }

    public void setShortTextValue(String shortTextValue) {
        this.shortTextValue = shortTextValue;
    }

    public byte[] getByteArrayValue() {
        return byteArrayValue;
    }

    public void setByteArrayValue(byte[] byteArrayValue) {
        this.byteArrayValue = byteArrayValue;
    }

    public Integer getIntegerNumberValue() {
        return integerNumberValue;
    }

    public void setIntegerNumberValue(Integer integerNumberValue) {
        this.integerNumberValue = integerNumberValue;
    }

    public Double getRealNumberValue() {
        return realNumberValue;
    }

    public void setRealNumberValue(Double realNumberValue) {
        this.realNumberValue = realNumberValue;
    }

    public Boolean getBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(Boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    public Item getItemValue() {
        return itemValue;
    }

    public void setItemValue(Item itemValue) {
        this.itemValue = itemValue;
    }

    public Area getAreaValue() {
        return areaValue;
    }

    public void setAreaValue(Area areaValue) {
        this.areaValue = areaValue;
    }

    public Institution getInstitutionValue() {
        return institutionValue;
    }

    public void setInstitutionValue(Institution institutionValue) {
        this.institutionValue = institutionValue;
    }

    public Client getClientValue() {
        return clientValue;
    }

    public void setClientValue(Client clientValue) {
        this.clientValue = clientValue;
    }

    public List<Item> getItemValues() {
        return itemValues;
    }

    public void setItemValues(List<Item> itemValues) {
        this.itemValues = itemValues;
    }

    public List<Area> getAreaValues() {
        return areaValues;
    }

    public void setAreaValues(List<Area> areaValues) {
        this.areaValues = areaValues;
    }

    public List<Institution> getInstitutionValues() {
        return institutionValues;
    }

    public void setInstitutionValues(List<Institution> institutionValues) {
        this.institutionValues = institutionValues;
    }

    public List<Client> getClientValues() {
        return clientValues;
    }

    public void setClientValues(List<Client> clientValues) {
        this.clientValues = clientValues;
    }
    

    
    
}
