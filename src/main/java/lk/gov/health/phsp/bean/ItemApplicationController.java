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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    private List<Item> items;
    

    /**
     * Creates a new instance of ItemApplicationController
     */
    public ItemApplicationController() {
    }

    public Item findItemByCode(String code) {
        Item item = null;
        int counter = 0;
        for (Item ti : getItems()) {
            if (ti.getCode() != null && ti.getCode().equalsIgnoreCase(code)) {
                counter++;
                item = ti;
            }
        }
        if (counter > 1) {
            System.err.println("This item code is duplicated = " + code);
        }
        return item;
    }

    public List<Item> findChildren(String code) {
        List<Item> tis = new ArrayList<>();
        for (Item ti : getItems()) {
            if (ti.getParent() != null && ti.getParent().getCode() != null && ti.getParent().getCode().equalsIgnoreCase(code)) {
                tis.add(ti);
            }
        }
        return tis;
    }

    public Item getMale() {
        return findItemByCode("sex_male");
    }

    public Item getFemale() {
        return findItemByCode("sex_female");
    }

    public List<Item> getItems() {
        if (items == null) {
            items = fillItems();
        }
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    private List<Item> fillItems() {
        String j = "select i "
                + "from Item i "
                + "where i.retired=:ret "
                + "order by i.name";
        Map m = new HashMap();
        m.put("ret", false);
        return facade.findByJpql(j, m);
    }

    public void invalidateItems() {
        items = null;
    }

}
