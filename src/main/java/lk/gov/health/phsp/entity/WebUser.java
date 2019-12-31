/*
 * Author : Dr. M H B Ariyaratne
 *
 * MO(Health Information), Department of Health Services, Southern Province
 * and
 * Email : buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.entity;

import lk.gov.health.phsp.enums.WebUserRole;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 * Informatics)
 */
@Entity
@XmlRootElement
public class WebUser implements Serializable {

    private static final long serialVersionUID = 1L;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    String webUserPassword;
    @OneToOne(cascade = CascadeType.ALL)
    Person person;
    //Main Properties
    @Column(length = 50, nullable = false, unique = true)
    String name;
    String description;
    //Created Properties
    @ManyToOne
    WebUser creater;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date createdAt;
    //Retairing properties
    boolean retired;
    @ManyToOne
    WebUser retirer;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date retiredAt;
    String retireComments;
    //Activation properties
    boolean activated;
    @ManyToOne
    WebUser activator;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date activatedAt;
    String activateComments;
    @Enumerated(EnumType.STRING)
    WebUserRole webUserRole;
    String primeTheme;
    String defLocale;
    String email;
    String telNo;
    @ManyToOne
    Institution institution;
    @ManyToOne
    private Area area;

    String code;

    /*
    Last Edit Properties
     */
    @ManyToOne
    private WebUser lastEditBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date lastEditeAt;

    @Transient
    private boolean systemAdministrator;
    @Transient
    private boolean superUser;
    @Transient
    private boolean user;
    @Transient
    private boolean institutionUser;
    @Transient
    private boolean institutionSuperUser;
    @Transient
    private boolean institutionAdministrator;
    @Transient
    private boolean authorityUser;
    @Transient
    private boolean meAdministrator;
    @Transient
    private boolean meSuperUser;
    @Transient
    private boolean meUser;
    @Transient
    private boolean doctor;
    @Transient
    private boolean nurse;
    @Transient
    private boolean client;
    @Transient
    private boolean midwife;
    @Transient
    private WebUserRole assumedRole;
    @Transient
    private Institution assumedInstitution;
    @Transient
    private Area assumedArea;

    public WebUser() {
    }

    public Institution getInstitution() {
        if (getAssumedInstitution() != null) {
            return assumedInstitution;
        }
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelNo() {
        return telNo;
    }

    public void setTelNo(String telNo) {
        this.telNo = telNo;
    }

    public String getDefLocale() {
        return defLocale;
    }

    public void setDefLocale(String defLocale) {
        this.defLocale = defLocale;
    }

    public String getPrimeTheme() {
        return primeTheme;
    }

    public void setPrimeTheme(String primeTheme) {
        this.primeTheme = primeTheme;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public WebUser getCreater() {
        return creater;
    }

    public void setCreater(WebUser creater) {
        this.creater = creater;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRetireComments() {
        return retireComments;
    }

    public void setRetireComments(String retireComments) {
        this.retireComments = retireComments;
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

    public WebUser getRetirer() {
        return retirer;
    }

    public void setRetirer(WebUser retirer) {
        this.retirer = retirer;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWebUserPassword() {
        return webUserPassword;
    }

    public void setWebUserPassword(String webUserPassword) {
        this.webUserPassword = webUserPassword;
    }

    public Person getPerson() {
        if (person == null) {
            person = new Person();
        }
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public String getActivateComments() {
        return activateComments;
    }

    public void setActivateComments(String activateComments) {
        this.activateComments = activateComments;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public Date getActivatedAt() {
        return activatedAt;
    }

    public void setActivatedAt(Date activatedAt) {
        this.activatedAt = activatedAt;
    }

    public WebUser getActivator() {
        return activator;
    }

    public void setActivator(WebUser activator) {
        this.activator = activator;
    }

    public WebUserRole getWebUserRole() {
        if (assumedRole != null) {
            return assumedRole;
        }
        return webUserRole;
    }

    public void setWebUserRole(WebUserRole webUserRole) {
        this.webUserRole = webUserRole;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof WebUser)) {
            return false;
        }
        WebUser other = (WebUser) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        if (person != null) {
            return person.getNameWithTitle();
        } else {
            return name;
        }
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isSystemAdministrator() {
        systemAdministrator = getWebUserRole() == WebUserRole.System_Administrator;
        return systemAdministrator;
    }

    public boolean isSuperUser() {
        superUser = getWebUserRole() == WebUserRole.Super_User;
        return superUser;
    }

    public boolean isUser() {
        user = getWebUserRole() == WebUserRole.User;
        return user;
    }

    public boolean isInstitutionUser() {
        institutionUser = getWebUserRole() == WebUserRole.Institution_User;
        return institutionUser;
    }

    public boolean isInstitutionAdministrator() {
        institutionAdministrator = getWebUserRole() == WebUserRole.Institution_Administrator;
        return institutionAdministrator;
    }

    public boolean isAuthorityUser() {
        authorityUser = getWebUserRole() == WebUserRole.Me_User;
        return authorityUser;
    }

    public boolean isMeAdministrator() {
        meAdministrator = getWebUserRole() == WebUserRole.Me_Admin;
        return meAdministrator;
    }

    public Area getArea() {
        if (assumedArea != null) {
            return assumedArea;
        }
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public boolean isInstitutionSuperUser() {
        if(getWebUserRole()==WebUserRole.Institution_Super_User){
            institutionSuperUser=true;
        }else{
            institutionSuperUser=false;
        }
        return institutionSuperUser;
    }

    public boolean isMeSuperUser() {
        meSuperUser = getWebUserRole() == WebUserRole.Me_Super_User;
        return meSuperUser;
    }

    public boolean isMeUser() {
        meUser = getWebUserRole() == WebUserRole.Me_User;
        return meUser;
    }

    public boolean isDoctor() {
        doctor = getWebUserRole() == WebUserRole.Doctor;
        return doctor;
    }

    public boolean isNurse() {
        nurse = getWebUserRole() == WebUserRole.Nurse;
        return nurse;
    }

    public boolean isClient() {
        client = getWebUserRole() == WebUserRole.Client;
        return client;
    }

    public boolean isMidwife() {
        midwife = getWebUserRole() == WebUserRole.Midwife;
        return midwife;
    }

    public WebUser getLastEditBy() {
        return lastEditBy;
    }

    public void setLastEditBy(WebUser lastEditBy) {
        this.lastEditBy = lastEditBy;
    }

    public Date getLastEditeAt() {
        return lastEditeAt;
    }

    public void setLastEditeAt(Date lastEditeAt) {
        this.lastEditeAt = lastEditeAt;
    }

    public WebUserRole getAssumedRole() {
        return assumedRole;
    }

    public void setAssumedRole(WebUserRole assumedRole) {
        this.assumedRole = assumedRole;
    }

    public Institution getAssumedInstitution() {
        return assumedInstitution;
    }

    public void setAssumedInstitution(Institution assumedInstitution) {
        this.assumedInstitution = assumedInstitution;
    }

    public Area getAssumedArea() {
        return assumedArea;
    }

    public void setAssumedArea(Area assumedArea) {
        this.assumedArea = assumedArea;
    }

}
