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
package lk.gov.health.phsp.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 * Informatics)
 */
@Entity
@XmlRootElement
public class Person implements Serializable {

// <editor-fold defaultstate="collapsed" desc="Persistant Attributes">
    static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

   
    @ManyToOne
    Item title;
    String name;

 
    @ManyToOne
    Item sex;


    @ManyToOne
    private Item citizenship;

    @ManyToOne
    private Item ethinicGroup;

    @ManyToOne
    private Item religion;

    @ManyToOne
    private Item mariatalStatus;
    
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date dateOfBirth;

    String address;

    String phone1;
    String phone2;
    String email;

    String nic;

    String passportNumber;

    String website;
    String drivingLicenseNumber;

    //Created Properties
    @ManyToOne
    WebUser createdBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date createdAt;
    @ManyToOne
    WebUser editer;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date editedAt;

    //Retairing properties
    boolean retired;
    @ManyToOne
    WebUser retiredBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date retiredAt;
    String retireComments;

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Transient Attributes">
    @Transient
    int ageMonths;
    @Transient
    int ageDays;
    @Transient
    int ageYears;
    @Transient
    String age;
    @Transient
    long ageInDays;
    @Transient
    int serealNumber;
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Functions">
    public void calAgeFromDob() {
        age = "";
        ageInDays = 0l;
        ageMonths = 0;
        ageDays = 0;
        ageYears = 0;
        if (getDateOfBirth() == null) {
            return;
        }

        LocalDate dob = new LocalDate(getDateOfBirth());
        LocalDate date = new LocalDate(new Date());

        Period period = new Period(dob, date, PeriodType.yearMonthDay());
        ageYears = period.getYears();
        ageMonths = period.getMonths();
        ageDays = period.getDays();
        if (ageYears > 12) {
            age = period.getYears() + " years.";
        } else if (ageYears > 0) {
            age = period.getYears() + " years and " + period.getMonths() + " months.";
        } else {
            age = period.getMonths() + " months and " + period.getDays() + " days.";
        }
        period = new Period(dob, date, PeriodType.days());
        ageInDays = (long) period.getDays();
    }

    public String getAge() {
        calAgeFromDob();
        return age;
    }

    public Long getAgeInDays() {
        calAgeFromDob();
        return ageInDays;
    }

    public int getAgeMonths() {
        calAgeFromDob();
        return ageMonths;
    }

    public int getAgeDays() {
        calAgeFromDob();
        return ageDays;
    }

    public int getAgeYears() {
        calAgeFromDob();
        return ageYears;
    }

    public String getNameWithTitle() {
        String temT = title.name + " " + name;
        return temT;
    }
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Getters & Setters">

    public Item getSex() {
        return sex;
    }

    public void setSex(Item sex) {
        this.sex = sex;
    }

    public String getRetireComments() {
        return retireComments;
    }

    public void setRetireComments(String retireComments) {
        this.retireComments = retireComments;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public WebUser getEditer() {
        return editer;
    }

    public void setEditer(WebUser editer) {
        this.editer = editer;
    }

    public Date getEditedAt() {
        return editedAt;
    }

    public void setEditedAt(Date editedAt) {
        this.editedAt = editedAt;
    }

    public WebUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(WebUser createdBy) {
        this.createdBy = createdBy;
    }

    public String getPhone2() {
        return phone2;
    }

    public void setPhone2(String phone2) {
        this.phone2 = phone2;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.toUpperCase();
    }

    public boolean isRetired() {
        return retired;
    }

    public void setRetired(boolean retired) {
        this.retired = retired;
    }

    public Date getRetiredAt() {
        return retiredAt;
    }

    public void setRetiredAt(Date retiredAt) {
        this.retiredAt = retiredAt;
    }

    public WebUser getRetiredBy() {
        return retiredBy;
    }

    public void setRetiredBy(WebUser retiredBy) {
        this.retiredBy = retiredBy;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address.toUpperCase();
    }

    public String getPassportNumber() {
        return passportNumber;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getDrivingLicenseNumber() {
        return drivingLicenseNumber;
    }

    public void setDrivingLicenseNumber(String drivingLicenseNumber) {
        this.drivingLicenseNumber = drivingLicenseNumber;
    }

    public Item getTitle() {
        return title;
    }

    public void setTitle(Item title) {
        this.title = title;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPhone1() {
        return phone1;
    }

    public void setPhone1(String phone1) {
        this.phone1 = phone1;
    }

    public String getNic() {
        return nic;
    }

    public void setNic(String nic) {
        this.nic = nic;
    }

    public int getSerealNumber() {
        return serealNumber;
    }

    public void setSerealNumber(int serealNumber) {
        this.serealNumber = serealNumber;
    }

    public Item getCitizenship() {
        return citizenship;
    }

    public void setCitizenship(Item citizenship) {
        this.citizenship = citizenship;
    }

    public Item getEthinicGroup() {
        return ethinicGroup;
    }

    public void setEthinicGroup(Item ethinicGroup) {
        this.ethinicGroup = ethinicGroup;
    }

    public Item getReligion() {
        return religion;
    }

    public void setReligion(Item religion) {
        this.religion = religion;
    }

    public Item getMariatalStatus() {
        return mariatalStatus;
    }

    public void setMariatalStatus(Item mariatalStatus) {
        this.mariatalStatus = mariatalStatus;
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Over-rides">
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Person)) {
            return false;
        }
        Person other = (Person) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return name;
    }
// </editor-fold>

}
