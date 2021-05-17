/*
 * The MIT License
 *
 * Copyright 2021 user.
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
package lk.gov.health.phsp.bean;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import lk.gov.health.phsp.entity.ExternalSync;
import lk.gov.health.phsp.facade.ExternalSyncFacade;

/**
 *
 * @author user
 */
@Named
@SessionScoped
public class ExternalSyncController implements Serializable {

    @EJB
    private ExternalSyncFacade extSyncFacade;
    
    private ExternalSync selected = new ExternalSync();

    public void ManageHash(String hashOwner) {  
        Timestamp timestamp = new Timestamp(System.currentTimeMillis()); 
        if(isExists(hashOwner)){
            this.setSelected(this.getObject(hashOwner));
            this.selected.setHashValue(timestamp.toString());
            
            extSyncFacade.edit(selected);     
        }else{
            this.selected.setHashOwner(hashOwner);
            this.selected.setHashValue(timestamp.toString());
            
            extSyncFacade.create(selected);
        }
    }

    public boolean isExists(String owner) {
        Map m = new HashMap();

        String q = "SELECT es FROM ExternalSync es WHERE es.hashOwner = :owner";
        m.put("owner", owner);
        return extSyncFacade.findByJpql(q, m).get(0) != null;
    }
    
    public ExternalSync getObject(String owner) {
        Map m = new HashMap();

        String q = "SELECT es FROM ExternalSync es WHERE es.hashOwner = :owner";
        m.put("owner", owner);
        return extSyncFacade.findFirstByJpql(q, m);
    }
    
    public String getHash(String hashOwner){
        Map m = new HashMap();

        String q = "SELECT es FROM ExternalSync es WHERE es.hashOwner = :owner";
        m.put("owner", hashOwner);
        return extSyncFacade.findFirstByJpql(q, m).getHashValue();
    }

    public ExternalSyncFacade getExtSyncFacade() {
        return extSyncFacade;
    }

    public void setExtSyncFacade(ExternalSyncFacade extSyncFacade) {
        this.extSyncFacade = extSyncFacade;
    }

    public ExternalSync getSelected() {
        return selected;
    }

    public void setSelected(ExternalSync selected) {
        this.selected = selected;
    }

}
