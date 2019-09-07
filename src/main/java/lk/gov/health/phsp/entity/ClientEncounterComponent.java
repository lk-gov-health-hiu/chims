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
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author buddhika
 */
@Entity
@XmlRootElement
public class ClientEncounterComponent extends Component {

    @ManyToOne
    Encounter encounter;

    @ManyToOne
    Client client;

    @Lob
    String longTextValue;
    String shortTextValue;
    byte[] byteArrayValue;
    Integer integerNumberValue;
    Double realNumberValue;
    Boolean booleanValue;
    @ManyToOne
    Item itemValue;
    @ManyToOne
    Area areaValue;
    @ManyToOne
    Institution institutionValue;
    @ManyToOne
    Client clientValue;
    
    List<Item> itemValues;
    List<Area> areaValues;
    List<Institution> institutionValues;
    List<Client> clientValues;
    

}
