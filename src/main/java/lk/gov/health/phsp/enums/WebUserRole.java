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
    System_Administrator,
    Super_User,
    User,
    Institution_User,
    Institution_Super_User,
    Institution_Administrator,
    Me_User,
    Me_Super_User,
    Me_Admin,
    Doctor,
    Nurse,
    Midwife,
    Client;

    public String getLabel() {
        switch (this) {
            case Client:
                return "Client";
            case Doctor:
                return "Doctor";
            case Institution_Administrator:
                return "Institution Administrator";
            case Institution_Super_User:
                return "Institution Super User";
            case Institution_User:
                return "Institution User";
            case Me_Admin:
                return "Monitoring & Evaluation Administrator";
            case Me_Super_User:
                return "Monitoring & Evaluation Super User";
            case Me_User:
                return "Monitoring & Evaluation User";
            case Midwife:
                return "Widwife";
            case Nurse:
                return "Nurse";
            case Super_User:
                return "Super User";
            case System_Administrator:
                return "Syetem Administrator";
            case User:
                return "User";
            default:
                return "None";
        }
    }

}
