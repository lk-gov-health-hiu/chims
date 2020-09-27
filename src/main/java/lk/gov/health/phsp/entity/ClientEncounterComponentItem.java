/*
 * The MIT License
 *
 * Copyright 2019 Dr M H B Ariyaratne<buddhika.ari@gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lk.gov.health.phsp.entity;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import lk.gov.health.phsp.enums.DataRepresentationType;

/**
 *
 * @author buddhika
 */
@Entity
public class ClientEncounterComponentItem extends ClientEncounterComponent {

    @ManyToOne
    private Client itemClient;
    @ManyToOne
    private Encounter itemEncounter;
    @ManyToOne
    private ClientEncounterComponentFormSet itemFormset;
    @Enumerated(EnumType.STRING)
    private DataRepresentationType dataRepresentationType;

    
    
    public Client getItemClient() {
        return itemClient;
    }

    public void setItemClient(Client itemClient) {
        this.itemClient = itemClient;
    }

    public Encounter getItemEncounter() {
        return itemEncounter;
    }

    public void setItemEncounter(Encounter itemEncounter) {
        this.itemEncounter = itemEncounter;
    }

    public ClientEncounterComponentFormSet getItemFormset() {
        return itemFormset;
    }

    public void setItemFormset(ClientEncounterComponentFormSet itemFormset) {
        this.itemFormset = itemFormset;
    }

    public DataRepresentationType getDataRepresentationType() {
        return dataRepresentationType;
    }

    public void setDataRepresentationType(DataRepresentationType dataRepresentationType) {
        this.dataRepresentationType = dataRepresentationType;
    }

    
    
}
