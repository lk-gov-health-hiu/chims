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

import java.util.HashMap;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.facade.ItemFacade;

/**
 *
 * @author buddhika
 */
@Named(value = "itemApplicationController")
@ApplicationScoped
public class ItemApplicationController {

    @EJB
    private ItemFacade facade;
    /**
     * Creates a new instance of ItemApplicationController
     */
    public ItemApplicationController() {
    }
    
    public Item findItemByCode(String code) {
        Item item;
        String j;
        Map m = new HashMap();
        if (code != null) {
            j = "select i from Item i "
                    + " where i.retired=false "
                    + " and lower(i.code)=:code "
                    + " order by i.id";
            m = new HashMap();
            m.put("code", code.trim().toLowerCase());
            item = facade.findFirstByJpql(j, m);
        } else {
            item = null;
        }
        return item;
    }
    
    public Item getMale(){
        return findItemByCode("sex_male");
    }
    
    public Item getFemale(){
        return findItemByCode("sex_female");
    }
    
    
    
    
}
