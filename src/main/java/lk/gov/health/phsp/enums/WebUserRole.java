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
 * @author Dr M H B Ariyaratne
 */
public enum WebUserRole {
    System_Administrator("System Administrator"),
    Super_User("Super User"),
    User("User"),
    Institution_User("Institution User"),
    Institution_Super_User("Institution Super User"),
    Institution_Administrator("Institution Administrator"),
    Me_User("Monitoring & Evaluation Administrator"),
    Me_Super_User("Monitoring & Evaluation Super User"),
    Me_Admin("Monitoring & Evaluation User"),
    Doctor("Doctor"),
    Nurse("Nurse"),
    Midwife("Midwife"),
    Client("Client");

    private final String label;
    
    private WebUserRole(String label){
        this.label = label;
    }
    
    public String getLabel(){
        return label;
    } 

}
