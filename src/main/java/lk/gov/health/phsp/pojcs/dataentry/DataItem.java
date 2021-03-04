/*
 * The MIT License
 *
 * Copyright 2021 Dr M H Buddhika Ariyaratne <buddhika.ari@gmail.com>.
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
import lk.gov.health.phsp.entity.ClientEncounterComponentItem;
import lk.gov.health.phsp.entity.DesignComponentFormItem;

/**
 *
 * @author Dr M H Buddhika Ariyaratne <buddhika.ari@gmail.com>
 */
public class DataItem {

    public int id;
    public double orderNo;
    public DesignComponentFormItem di;
    public ClientEncounterComponentItem ci;
    private List<ClientEncounterComponentItem> cis;
    public DataForm form;

    public List<ClientEncounterComponentItem> getCis() {
        if(cis==null){
            cis = new ArrayList<>();
        }
        return cis;
    }

    public void setCis(List<ClientEncounterComponentItem> cis) {
        this.cis = cis;
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

    public DesignComponentFormItem getDi() {
        return di;
    }

    public void setDi(DesignComponentFormItem di) {
        this.di = di;
    }

    public ClientEncounterComponentItem getCi() {
        return ci;
    }

    public void setCi(ClientEncounterComponentItem ci) {
        this.ci = ci;
    }

    public DataForm getForm() {
        return form;
    }

    public void setForm(DataForm form) {
        this.form = form;
    }

    
    
}