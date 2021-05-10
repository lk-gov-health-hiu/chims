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
public enum RenderType {
    Input_Text("Input Text"),
    Input_Text_Area("Input Text Area"),
    Drop_Down_Menu("Drop Down Menu"),
    List_Box("List Box"),
    Boolean_Button("Boolean Button"),
    Boolean_Checkbox("Boolean Checkbox"),
    Short_text_input("Short Text Input"),
    Long_text_input("Long Text Input"),
    Real_number_input("Real Number Input"),
    Integer_number_input("Integer Number Input"),
    Label("Label"),
    HTML("HTML"),
    Autocomplete_to_select_one_item("Autocomplete to Select One Item"),
    Autocomplete_to_select_many_items("Autocomplete to Select Many Items"),
    Prescreption_pad("Prescreption Pad"),
    Observation("Observation"),
    Referral("Referral"),
    Transfer("Transfer"),
    PlanOfAction("Plan of action"),
    Order("Orders"),
    Table("Table"),
    Grid("Grid"),
    Row("Row"),
    Column("Column"),
    Data_Table("Data Table"),
    Panel("Panel"),
    Panel_Tab("Panel_Tab"),
    Autocomplete("Autocomplete"),
    Select_Boolean_Button("Select Boolean Button"),
    Select_Boolean_Checkbox("Select Boolean Checkbox"),
    Select_One_Button("Select One Button"),
    Select_One_Radio("Select One Radio"),
    Select_Checkbox_Menu("Select Checkbox Menu"),
    Select_One_Menu("Select One Menu"),
    Select_One_Listbox("Select One Listbox"),
    Select_Many_Button("Select Many Button"),
    Select_Many_Menu("Select Many Menu"),
    Select_Many_Checkbox("Select Many Checkbox"),
    Select_Many_List_Box("Select Many List Box"),
    Date_Picker("Date Picker"),
    Calendar("Calendar"),
    Signature("Signature"),    
    Input_Switch("Input Switch"),
    Password("Password"),
    Keyboard("Keyboard"),
    Rating("Rating"),
    Color_Picker("Color Picker"),
    Inplace("Inplace"),
    Key_Filter("Key Filter"),
    Knob("Knob"),
    Text_Editor("Text_Editor"),
    Chips("Chips"),
    Button("Button"),
    Link("Link"),
    @Deprecated
    Item_select_and_add_to_List("Item select & add to List"),
    Prescreption("Prescreption"),
    Procedure_room("Procedure Room");
    
    
    
    private final String label;
    
    private RenderType(String label){
        this.label = label;
    }
    
    public String getLabel(){
        return label;
    }  
}
