package lk.gov.health.phsp.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table
@XmlRootElement
public class Client implements Serializable {

// <editor-fold defaultstate="collapsed" desc="Attributes">
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private static final long serialVersionUID = 1L;

    @OneToOne(cascade = CascadeType.ALL)
    private Person person;

    private String phn;

    private String comments;
    /*
    Create Properties
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser createdBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdAt;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date createdOn;
    @ManyToOne(fetch = FetchType.LAZY)
    private Institution createInstitution;
    /*
    Last Edit Properties
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser lastEditBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date lastEditeAt;
    /*
    Retire Reversal Properties
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser retiredReversedBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date retiredReversedAt;
    /*
    Retire Properties
     */
    private boolean retired;
    private boolean reservedClient;
    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser retiredBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date retiredAt;
    private String retireComments;

    // </editor-fold>   
// <editor-fold defaultstate="collapsed" desc="Overrides">
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {

        if (!(object instanceof Client)) {
            return false;
        }
        Client other = (Client) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Client{" + "phn=" + phn + '}';
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Getters & Setters">
    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public WebUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(WebUser createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
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

    public boolean isRetired() {
        return retired;
    }

    public void setRetired(boolean retired) {
        this.retired = retired;
    }

    public WebUser getRetiredBy() {
        return retiredBy;
    }

    public void setRetiredBy(WebUser retiredBy) {
        this.retiredBy = retiredBy;
    }

    public Date getRetiredAt() {
        return retiredAt;
    }

    public void setRetiredAt(Date retiredAt) {
        this.retiredAt = retiredAt;
    }

    public String getRetireComments() {
        return retireComments;
    }

    public void setRetireComments(String retireComments) {
        this.retireComments = retireComments;
    }

    public WebUser getRetiredReversedBy() {
        return retiredReversedBy;
    }

    public void setRetiredReversedBy(WebUser retiredReversedBy) {
        this.retiredReversedBy = retiredReversedBy;
    }

    public Date getRetiredReversedAt() {
        return retiredReversedAt;
    }

    public void setRetiredReversedAt(Date retiredReversedAt) {
        this.retiredReversedAt = retiredReversedAt;
    }

    public String getPhn() {
        return phn;
    }

    public void setPhn(String phn) {
        this.phn = phn;
    }

// </editor-fold>

    public Institution getCreateInstitution() {
        return createInstitution;
    }

    public void setCreateInstitution(Institution createInstitution) {
        this.createInstitution = createInstitution;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public boolean isReservedClient() {
        return reservedClient;
    }

    public void setReservedClient(boolean reservedClient) {
        this.reservedClient = reservedClient;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    
    
}
