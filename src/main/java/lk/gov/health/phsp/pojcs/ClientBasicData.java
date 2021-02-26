/*
 * The MIT License
 *
 * Copyright 2020 buddhika.
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
package lk.gov.health.phsp.pojcs;

import java.io.Serializable;
import java.util.Date;
import lk.gov.health.phsp.bean.CommonController;

/**
 *
 * @author buddhika
 */
public class ClientBasicData implements Serializable {

    private String phn;
    private String gnArea;
    private String createdInstitution;
    private String phone;
    private String name;
    private Date dataOfBirth;
    private Date createdAt;
    private String sex;
    private int ageInYears;
    private Long id;
    private String nic;
    private String address;

    public ClientBasicData() {
    }

    public ClientBasicData(String phn, String gnArea, String createdInstitution, Date dataOfBirth, String sex) {
        this.phn = phn;
        this.gnArea = gnArea;
        this.createdInstitution = createdInstitution;
        this.dataOfBirth = dataOfBirth;
        this.sex = sex;
    }

    public ClientBasicData(String phn, String gnArea, String createdInstitution, Date dataOfBirth, Date createdAt, String sex) {
        this.phn = phn;
        this.gnArea = gnArea;
        this.createdInstitution = createdInstitution;
        this.dataOfBirth = dataOfBirth;
        this.createdAt = createdAt;
        this.sex = sex;
    }
    
    public ClientBasicData(String phn,String name, String nic, String address, String phone, String gnArea, String createdInstitution, Date dataOfBirth, Date createdAt, String sex) {
        this.phn = phn;
        this.name = name;
        this.nic = nic;
        this.address = address;
        this.phone = phone;
        this.gnArea = gnArea;
        this.createdInstitution = createdInstitution;
        this.dataOfBirth = dataOfBirth;
        this.createdAt = createdAt;
        this.sex = sex;
    }

    public ClientBasicData(Long id, String phn, String gnArea, String createdInstitution, Date dataOfBirth, Date createdAt, String sex, String nic) {
        this.id = id;
        this.phn = phn;
        this.gnArea = gnArea;
        this.createdInstitution = createdInstitution;
        this.dataOfBirth = dataOfBirth;
        this.createdAt = createdAt;
        this.sex = sex;
        this.nic = nic;
    }
    
    public ClientBasicData(Long id, String phn, String gnArea, String createdInstitution, Date dataOfBirth, Date createdAt, String sex, String nic, String name) {
        this.id = id;
        this.phn = phn;
        this.gnArea = gnArea;
        this.createdInstitution = createdInstitution;
        this.dataOfBirth = dataOfBirth;
        this.createdAt = createdAt;
        this.sex = sex;
        this.nic = nic;
        this.name = name;
    }

    public ClientBasicData(Long id, String phn, String name, String sex, String nic, String phone, String address) {
        this.phn = phn;
        this.phone = phone;
        this.name = name;
        this.sex = sex;
        this.id = id;
        this.nic = nic;
        this.address = address;
    }

    
    public ClientBasicData(Long id, String phn, String name, String nic, String phone, String address) {
        this.phn = phn;
        this.phone = phone;
        this.name = name;
        this.id = id;
        this.nic = nic;
        this.address = address;
    }
    
    
    public String getPhn() {
        return phn;
    }

    public void setPhn(String phn) {
        this.phn = phn;
    }

    public String getGnArea() {
        return gnArea;
    }

    public void setGnArea(String gnArea) {
        this.gnArea = gnArea;
    }

    public String getCreatedInstitution() {
        return createdInstitution;
    }

    public void setCreatedInstitution(String createdInstitution) {
        this.createdInstitution = createdInstitution;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDataOfBirth() {
        return dataOfBirth;
    }

    public void setDataOfBirth(Date dataOfBirth) {
        this.dataOfBirth = dataOfBirth;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public int getAgeInYears() {
        ageInYears = CommonController.ageFromDob(dataOfBirth);
        return ageInYears;
    }

    public void setAgeInYears(int ageInYears) {
        this.ageInYears = ageInYears;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNic() {
        return nic;
    }

    public void setNic(String nic) {
        this.nic = nic;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}
