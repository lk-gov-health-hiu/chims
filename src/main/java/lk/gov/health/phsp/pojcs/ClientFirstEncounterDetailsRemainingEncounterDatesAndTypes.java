/*
 * The MIT License
 *
 * Copyright 2021 buddhika.
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
package lk.gov.health.phsp.pojcs;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lk.gov.health.phsp.entity.Client;
import lk.gov.health.phsp.entity.Encounter;

/**
 *
 * @author buddhika
 */
public class ClientFirstEncounterDetailsRemainingEncounterDatesAndTypes {
    private Client client;
    private Encounter firstEncounter;
    private List<Encounter> remainigEncounters;

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Encounter getFirstEncounter() {
        return firstEncounter;
    }

    public void setFirstEncounter(Encounter firstEncounter) {
        this.firstEncounter = firstEncounter;
    }

    public List<Encounter> getRemainigEncounters() {
        if(remainigEncounters==null){
            remainigEncounters = new ArrayList<>();
        }
        return remainigEncounters;
    }

    public void setRemainigEncounters(List<Encounter> remainigEncounters) {
        this.remainigEncounters = remainigEncounters;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.client);
        return hash;
    }

    

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ClientFirstEncounterDetailsRemainingEncounterDatesAndTypes other = (ClientFirstEncounterDetailsRemainingEncounterDatesAndTypes) obj;
        if (!Objects.equals(this.client, other.client)) {
            return false;
        }
        return true;
    }
    
    
}
