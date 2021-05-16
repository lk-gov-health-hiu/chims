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
package lk.gov.health.phsp.bean;

import java.util.List;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.facade.WebUserFacade;

/**
 *
 * @author buddhika
 */
@Named
@ApplicationScoped
public class WebUserApplicationController {

    /*
    EJBs
     */
    @EJB
    private WebUserFacade facade;

     @Inject
    private UserTransactionController userTransactionController;
    private List<WebUser> items = null;

    /**
     * Creates a new instance of WebUserApplicationController
     */
    public WebUserApplicationController() {
    }

    public List<WebUser> getItems() {
        if(items==null){
            fillWebUsers();
        }
        return items;
    }

    public void setItems(List<WebUser> items) {
        this.items = items;
    }

    private void fillWebUsers(){
        String j = "select u from WebUser u "
                + " where u.retired=false ";
        items = facade.findByJpql(j);
        userTransactionController.recordTransaction("To List All Users");
    }
    
    public void resetWebUsers(){
        items=null;
    }
    
}
