/*
 * To change this license header, choose License Headers in Client Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lk.gov.health.phsp.entity;

import javax.jdo.annotations.Index;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

/**
 *
 * @author buddhika
 */
@Entity
public class ClientEncounterComponent extends Component  {

    @Index
    @ManyToOne(fetch = FetchType.EAGER)
    private Encounter encounter;

    @Index
    @ManyToOne(fetch = FetchType.EAGER)
    private Client client;

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

}
