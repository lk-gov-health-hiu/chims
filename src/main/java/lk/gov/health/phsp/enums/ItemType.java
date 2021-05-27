/*
 * The MIT License
 *
 * Copyright 2019 Dr M H B Ariyaratne<buddhika.ari@gmail.com>.
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

package lk.gov.health.phsp.enums;

/**
 *
 * @author User
 */
public enum ItemType {
    Dictionary_Item("Dictionary Item"),
    Dictionary_Category("Dictionary Category"),
    Mime_Type("Mime Type"),
    Atm("ATM"),
    Vtm("VTM"),
    Amp("AMP"),
    Vmp("VMP"),
    Ampp("AMPP"),
    Vmpp("VMPP"),
    Strength_Unit("Strength Unit"),
    Issue_Unit("Issue Unit"),
    Dosage_Form("Dosage Form"),
    Pack_Unit("Pack Unit"),
    Frequency_Unit("Frequency Unit"),
    Duration_Unit("Duration Unit"),
    Other("Other");
    
    public final String label;
    
    private ItemType(String label){
        this.label = label;
    }
    
    public String getLabel(){
        return label;
    }
}
