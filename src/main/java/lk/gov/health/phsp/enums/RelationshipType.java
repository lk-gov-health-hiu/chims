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
public enum RelationshipType {
    Primary_Care_Institute("Primary Care Institute"),
    GN("GN"),
    PHM("PHM"),
    PHI("PHI"),
    MOH("MOH"),
    DsArea("Ds Area"),
    District("District"),
    Province("Province"),
    National("National"),
    Male_Population("Male Population"),
    Female_Population("Female Population"),
    Total_Population("Total Population"),
    Estimated_Midyear_Population("Estimated Midyear Population"),
    Estimated_Midyear_Male_Population("Estimated Midyear Male Population"),
    Estimated_Midyear_Female_Population("Estimated Midyear Female Population"),
    Empanelled_Male_Population("Empanelled Male Population"),
    Empanelled_Female_Population("Empanelled Female Population"),
    Empanelled_Population("Empanelled Population"),
    Registerd_Male_Population("Registerd Male Population"),
    Registered_Female_Population("Registered Female Population"),
    Registered_Population("Registered_Population"),
    Screened_Male_Population("Screened Male Population"),
    Screened_Female_Population("Screened Female Population"),
    Screened_Population("Screened Population"),
    Over_35_Male_Population("Over 35 Male Population"),
    Over_35_Female_Population("Over 35 Female Population"),
    Over_35_Population("Over 35 Population"),
    High_Risk_Male_Population("High Risk Male Population"),
    High_Risk_Female_Population("High Risk Female Population"),
    High_Risk_Population("High_Risk_Population"),
    Annual_Target_Male_Population("Registerd Male Population"),
    Annual_Target_Female_Population("Registered Female Population"),
    Annual_Target_Population("Registered_Population"),
    Procedure_Room("Procedure Room"),
    Dispensary("Dispensary"),
    Laboratory("Laboratory"),
    Procedure_for_institution("Procedure performed at the institution"),
    Formsets_for_institution("Formsets Assigned For Institutions"),
    VtmsForVmp("VTM for VMP"),
    VmpForAmp("VMP for AMP");
    private final String label;
    
    private RelationshipType(String label){
        this.label = label;
    }
    
    public String getLabel(){
        return label;
    }    
}
