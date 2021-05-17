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
public enum SelectionDataType {
    Short_Text("Short Text"),
    Long_Text("Long Text"),
    Byte_Array("Byte Array"),
    Integer_Number("Integer Number"),
    Real_Number("Real Number"),
    Long_Number("Long Number"),
    DateTime("Date Time"),
    Boolean("Boolean"),
    Item_Reference("Item Reference"),
    Client_Reference("Client Reference"),
    Area_Reference("Area Reference"),
    Prescreption_Reference("Prescreption"),
    Procedure_Request("Procedure Request"),
    @Deprecated
    Institution_Reference("Institution Reference"),
    @Deprecated
    Item_List_Reference("Item List Reference"),
    @Deprecated
    Client_List_Reference("Client List Reference"),
    @Deprecated
    Area_List_Reference("Area List Reference"),
    @Deprecated
    Institution_List_Reference("Institution List Reference"),
    @Deprecated
    Free_Entry("Free Entry"),
    @Deprecated
    Calculation("Calculation");
    
    public final String label;    
    private SelectionDataType(String label){
        this.label = label;
    }
    
    public String getLabel(){
        return label;
    }
}
