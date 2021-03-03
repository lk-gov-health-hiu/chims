/*
 * The MIT License
 *
 * Copyright 2021 Dr M H B Ariyaratne
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
import java.util.List;
import lk.gov.health.phsp.entity.ClientEncounterComponentForm;
import lk.gov.health.phsp.entity.DesignComponentForm;

/**
 *
 * @author Dr M H B Ariyaratne
 */
public class DataForm {

    public int id;
    public double orderNo;
    public DesignComponentForm df;
    public ClientEncounterComponentForm cf;
    public DataFormset formset;
    List<DataItem> items;

   
    public List<DataItem> getItems() {
        if(items==null){
            items = new ArrayList<>();
        }
        return items;
    }

    public void setItems(List<DataItem> items) {
        this.items = items;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(double orderNo) {
        this.orderNo = orderNo;
    }

    public DesignComponentForm getDf() {
        return df;
    }

    public void setDf(DesignComponentForm df) {
        this.df = df;
    }

    public ClientEncounterComponentForm getCf() {
        return cf;
    }

    public void setCf(ClientEncounterComponentForm cf) {
        this.cf = cf;
    }

    public DataFormset getFormset() {
        return formset;
    }

    public void setFormset(DataFormset formset) {
        this.formset = formset;
    }

    
    
}
