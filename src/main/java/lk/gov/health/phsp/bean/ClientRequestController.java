/*
 * The MIT License
 *
 * Copyright 2020 buddhika.
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

//<editor-fold desc="imports" defaultstate="collapsed">
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import lk.gov.health.phsp.entity.Client;
import lk.gov.health.phsp.facade.ClientFacade;
//</editor-fold>

/**
 *
 * @author buddhika
 */
@Named
@RequestScoped
public class ClientRequestController {

    //<editor-fold desc="constructors" defaultstate="collapsed">
    /**
     * Creates a new instance of ClientRequestController
     */
    public ClientRequestController() {
    }
    //</editor-fold>

    //<editor-fold desc="class variables" defaultstate="collapsed">
    List<Client> clientsWithSameNic;
    //</editor-fold>
    
    //<editor-fold desc="EJBs" defaultstate="collapsed">
    @EJB
    ClientFacade clientFacade;
    //</editor-fold>
    
    //<editor-fold desc="controllers" defaultstate="collapsed">
    
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Functions">
    public List<Client> clientsWithSameNic(String nic) {
        if(nic==null || nic.trim().equals("")){
            return null;
        }
        Map m = new HashMap();
        String j = "select c "
                + " from Client c "
                + " where c.retired<>:ret "
                + " and c.reservedClient<>:res "
                + " and c.person.nic=:nic ";
        m.put("ret", true);
        m.put("res", true);
        m.put("nic", nic.toLowerCase());
        return clientFacade.findByJpql(j, m);
    }
    
    public List<Client> clientsWithSamePhn(String phn) {
        if(phn==null || phn.trim().equals("")){
            return null;
        }
        Map m = new HashMap();
        String j = "select c "
                + " from Client c "
                + " where c.retired<>:ret "
                + " and c.reservedClient<>:res "
                + " and c.phn=:phn ";
        m.put("ret", true);
        m.put("res", true);
        m.put("phn", phn);
        return clientFacade.findByJpql(j, m);
    }
  
    public List<Client> clientsWithSameEmail(String email) {
        if(email==null || email.trim().equals("")){
            return null;
        }
        Map m = new HashMap();
        String j = "select c "
                + " from Client c "
                + " where c.retired<>:ret "
                + " and c.reservedClient<>:res "
                + " and c.person.email=:email ";
        m.put("ret", true);
        m.put("res", true);
        m.put("email", email.toLowerCase());
        return clientFacade.findByJpql(j, m);
    }
    
    public List<Client> clientsWithSameSSNumber(String ssNumber) {
        if(ssNumber==null || ssNumber.trim().equals("")){
            return null;
        }
        Map m = new HashMap();
        String j = "select c "
                + " from Client c "
                + " where c.retired<>:ret "
                + " and c.reservedClient<>:res "
                + " and c.person.ssNumber=:ssNumber ";
        m.put("ret", true);
        m.put("res", true);
        m.put("ssNumber", ssNumber);
        return clientFacade.findByJpql(j, m);
    } 
      
    public List<Client> clientsWithSameMobileNo(String phone1) {
        if(phone1==null || phone1.trim().equals("")){
            return null;
        }
        Map m = new HashMap();
        String j = "select c "
                + " from Client c "
                + " where c.retired<>:ret "
                + " and c.reservedClient<>:res "
                + " and c.person.phone1=:phone1 ";
        m.put("ret", true);
        m.put("res", true);
        m.put("phone1", phone1);
        return clientFacade.findByJpql(j, m);
    } 
      
    //</editor-fold>

}