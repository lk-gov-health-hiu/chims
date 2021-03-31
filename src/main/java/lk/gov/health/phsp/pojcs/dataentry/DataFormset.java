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
package lk.gov.health.phsp.pojcs.dataentry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lk.gov.health.phsp.entity.ClientEncounterComponentFormSet;
import lk.gov.health.phsp.entity.ClientEncounterComponentItem;
import lk.gov.health.phsp.entity.DesignComponentFormSet;

/**
 *
 * @author buddhika
 */
public class DataFormset {

    private DesignComponentFormSet dfs;
    private ClientEncounterComponentFormSet efs;
    public List<DataForm> forms;
    private Map<String, ClientEncounterComponentItem> mapOfClientValues;

    public List<DataForm> getForms() {
        if (forms == null) {
//            *74 Mobitel Voicemail
            forms = new ArrayList<>();
        }
        return forms;
    }

    
    
    public void setForms(List<DataForm> forms) {
        this.forms = forms;
    }

    public DesignComponentFormSet getDfs() {
        return dfs;
    }

    public void setDfs(DesignComponentFormSet dfs) {
        this.dfs = dfs;
    }

    public ClientEncounterComponentFormSet getEfs() {
        return efs;
    }

    public void setEfs(ClientEncounterComponentFormSet efs) {
        this.efs = efs;
    }

    public Map<String, ClientEncounterComponentItem> getMapOfClientValues() {
        if(mapOfClientValues==null){
            mapOfClientValues = new HashMap<>();
        }
        return mapOfClientValues;
    }

    public void setMapOfClientValues(Map<String, ClientEncounterComponentItem> mapOfClientValues) {
        this.mapOfClientValues = mapOfClientValues;
    }
    
    

}
