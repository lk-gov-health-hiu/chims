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
package lk.gov.health.phsp.ejb;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;
import lk.gov.health.phsp.entity.ApiRequest;
import lk.gov.health.phsp.facade.ApiRequestFacade;

/**
 *
 * @author buddhika
 */
@Singleton
public class ApiRequestBean {

    @Inject
    ApiRequestFacade apiRequestFacade;

    @Schedule(dayOfMonth = "*", year = "*", minute = "*", second = "0", persistent = false)
    public void myTimer() {
        // System.out.println("Timer event: " + new Date());
        sendApiRequest(null);
    }

    public void sendApiRequests() {
        String j = "select a "
                + " from ApiRequest a "
                + " where a.success=:s"
                + " and a.failed=:f";
        Map m = new HashMap();
        m.put("s", false);
        m.put("f", false);
        List<ApiRequest> requests = apiRequestFacade.findByJpql(j, m);
        for (ApiRequest a : requests) {
            if (sendApiRequest(a)) {
                a.setSuccess(true);
                a.setFailed(false);
            } else {
                a.setSuccess(false);
                if (a.getFailedAttempts() > 5) {
                    a.setFailed(true);
                } else {
                    a.setFailedAttempts(a.getFailedAttempts() + 1);
                }
            }

            saveApiRequest(a);
        }
    }

    public boolean sendApiRequest(ApiRequest a) {
        //TO : Dinidu
        String phn;
        String procedureOd;
        String InsId;
        String CreterId;
        
        if(a==null){
            
//        // System.out.println("PHN = " + a.getEncounter().getClient().getPhn());
//        // System.out.println("Procedure ID = " + a.getClientEncounterComponentItem().getItemValue().getId());
//        // System.out.println("Institute ID = " + a.getEncounter().getInstitution().getId());
//        // System.out.println("Institute ID = " + a.getEncounter().getInstitution().getId());
        }else{
            
        }
        /*
        PHN
        Procedure ID taken from API
        Institute ID
        Creater By - User ID
        Created At - User
        */
       
        return false;
    }

    private void saveApiRequest(ApiRequest a) {
        if (a == null) {
            return;
        }
        if (a.getId() == null) {
            apiRequestFacade.create(a);
        } else {
            apiRequestFacade.edit(a);
        }
    }

}
