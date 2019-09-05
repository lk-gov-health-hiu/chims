package lk.gov.health.phsp.entity;

import lk.gov.health.phsp.enums.ProjectStageType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
public class Client implements Serializable {

    private static final long serialVersionUID = 1L;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Integer projectYear;

    @ManyToOne
    private Area province;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<FormSet> projectProvinces;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<FormItem> projectDistricts;

    private String fileNumber;

    @ManyToOne
    private Area district;

    @ManyToOne
    private Institution projectLocation;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<EncounterFormItem> projectLocations;

    private String projectTitle;

    @Lob
    private String projectDescription;

    private Double projectCost;

    @ManyToOne
    private Item projectCostUnit;

    @ManyToOne
    private Item sourceOfFunds;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Form> sourcesOfFunds;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date proposalDate;
    
    @Lob
    private String proposalDateComments;

    @ManyToOne
    private Institution pcpReceivedBy;
    
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date pcpFirstReceivedDate;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date pcpFinalizedDate;

    private Boolean engineersEstimateAvailable;

    private Boolean masterPlanAvailable;

    private Boolean buildingPlanAvailable;

    @Lob
    private String pecRecommendation;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date pcpFirstSendToNdpDate;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date pcpLastSendToNdpDate;

    Boolean incompletePcp;
    @Temporal(javax.persistence.TemporalType.DATE)
    Date incompletePcpDecidedDate;

    private Boolean pecRecomended;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date pecRecommendedOn;

    private Boolean pecRevision;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date pecRevisionStartedOn;

    private Boolean pecRejected;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date pecRejectedOn;

    private Boolean ndpSubmitted;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date ndpSubmittedOn;

    private Boolean ndpRecommended;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date ndpRecommendedOn;

    Boolean ndpRevision;
    @Temporal(javax.persistence.TemporalType.DATE)
    Date ndpRevisionStartedOn;

    private Boolean ndpRejected;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date ndpRejectedOn;

    private Boolean cabinetSubmitted;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date cabinetSubmittedOn;

    private Boolean cabinetApproved;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date cabinetApprovalOn;

    private Boolean cabinetRejected;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date cabinetRejectedOn;

    Boolean fundsAllocated;
    @Temporal(javax.persistence.TemporalType.DATE)
    Date fundsAllocatedOn;

    private Boolean onoing;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date ongoingStartedOn;

    private Boolean completed;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date completedOn;

    @Lob
    private String remarks;

    @ManyToOne
    private Item sector;

    private Boolean allIsland;

    @Enumerated(EnumType.STRING)
    private ProjectStageType currentStageType;

    //Created Properties
    @ManyToOne
    private WebUser creater;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdAt;

    //Editor Properties
    @ManyToOne
    private WebUser lastEditor;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date lastEditAt;

    //Retairing properties
    private boolean retired;
    @ManyToOne
    private WebUser retirer;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date retiredAt;
    @Lob
    private String retireComments;

    //Incomplete PCP
    @ManyToOne
    WebUser pcpMarkedAsIncompleteBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date pcpMarkedAsIncompleteAt;
    @Lob
    String pcpIncompleteComments;

    //PCE Approval
    @ManyToOne
    private WebUser pecReviewRecordedBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date pecReviewRecordedAt;
    @Lob
    private String pecReviewComments;

    //PCE Approval
    @ManyToOne
    private WebUser pecRecommendationRecordedBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date pecRecommendationRecordedAt;
    @Lob
    private String pecRecommendationComments;

    //PCE Rejection
    @ManyToOne
    private WebUser pecRejectionRecordedBy;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date pecRejectionRecordedAt;
    @Lob
    private String pecRejectionComments;

    //Sento to DNP
    @ManyToOne
    private WebUser ndpSubmissionRecordedUser;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date ndpSubmissionRecordedAt;
    @Lob
    private String ndpSubmissionComments;

    //NDP Revision
    @ManyToOne
    private WebUser ndpRevisionRecordedBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date ndpRevisionRecordedAt;
    @Lob
    private String ndpRevisionComments;

    //DNP Approval
    @ManyToOne
    private WebUser ndpRecommendationRecordedBy;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date ndpApprovalRecordedAt;
    @Lob
    private String ndpRecommendationComments;

    //DNP Rejection
    @ManyToOne
    private WebUser ndpRejectionRecordedBy;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date ndpRejectionRecorderAt;
    @Lob
    private String ndpRejectionComments;

    //Sento to Cabinet
    @ManyToOne
    private WebUser cabinetSubmissionRecordedBy;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date cabinetSubmissionRecordedAt;
    @Lob
    private String cabinetSubmissionComments;

    //Cabinet Approval
    @ManyToOne
    private WebUser cabinetApprovalRecordedBy;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date cabinetApprovalRecordedAt;
    @Lob
    private String cabinetApprovalComments;

    //Cabinet Rejection
    @ManyToOne
    private WebUser cabinetRejectionRecordedBy;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date cabinetRejectionRecordedAt;
    @Lob
    private String cabinetRejectionComments;

    //Fund Allocation
    @ManyToOne
    private WebUser fundAllocationDoneRecordedBy;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date fundAllocationRecordedAt;
    @Lob
    private String fundAllocationComments;

    //Ongoing
    @ManyToOne
    private WebUser ongoingMarkedUser;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date ongoingMarkedAt;
    @Lob
    private String ongoingRecommendation;

    //Completed
    @ManyToOne
    private WebUser completedMarkedUser;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date completeMarkedAt;
    @Lob
    private String completeRecommendation;

    @Transient
    private boolean canApproveAtPec = false;
    @Transient
    private boolean canRejectAtPec = false;
    @Transient
    private boolean canReviewAtPec = false;

    @Transient
    private boolean canSubmitToNdp;
    @Transient
    private boolean canApproveAtNdp = false;
    @Transient
    private boolean canRejectAtNdp = false;
    @Transient
    private boolean canReviewAtNdp = false;

    @Transient
    private boolean canSubmitToCabinet;
    @Transient
    private boolean canApproveAtCabinet = false;
    @Transient
    private boolean canRejectAtCabinet = false;

    @Transient
    private boolean canAllocateFunds;
    @Transient
    private boolean canMarkAsOngoing;
    @Transient
    private boolean canMarkAsCompleted = false;

    @Transient
    private String provincesStr;
    @Transient
    private String districtsStr;
    @Transient
    private String locationsStr;
    @Transient
    private String sourcesOfFundsStr;

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
        return "Ref No " + fileNumber + " ";
    }

    public Integer getProjectYear() {
        return projectYear;
    }

    public void setProjectYear(Integer projectYear) {
        this.projectYear = projectYear;
    }

    public Institution getProjectLocation() {
        return projectLocation;
    }

    public void setProjectLocation(Institution projectLocation) {
        this.projectLocation = projectLocation;
    }

    public Date getPcpFirstReceivedDate() {
        return pcpFirstReceivedDate;
    }

    public void setPcpFirstReceivedDate(Date pcpFirstReceivedDate) {
        this.pcpFirstReceivedDate = pcpFirstReceivedDate;
    }

    public Date getPcpFinalizedDate() {
        return pcpFinalizedDate;
    }

    public void setPcpFinalizedDate(Date pcpFinalizedDate) {
        this.pcpFinalizedDate = pcpFinalizedDate;
    }

    public Boolean getEngineersEstimateAvailable() {
        return engineersEstimateAvailable;
    }

    public void setEngineersEstimateAvailable(Boolean engineersEstimateAvailable) {
        this.engineersEstimateAvailable = engineersEstimateAvailable;
    }

    public Boolean getMasterPlanAvailable() {
        return masterPlanAvailable;
    }

    public void setMasterPlanAvailable(Boolean masterPlanAvailable) {
        this.masterPlanAvailable = masterPlanAvailable;
    }

    public Boolean getBuildingPlanAvailable() {
        return buildingPlanAvailable;
    }

    public void setBuildingPlanAvailable(Boolean buildingPlanAvailable) {
        this.buildingPlanAvailable = buildingPlanAvailable;
    }

    public String getPecRecommendation() {
        return pecRecommendation;
    }

    public void setPecRecommendation(String pecRecommendation) {
        this.pecRecommendation = pecRecommendation;
    }

    public Date getPcpFirstSendToNdpDate() {
        return pcpFirstSendToNdpDate;
    }

    public void setPcpFirstSendToNdpDate(Date pcpFirstSendToNdpDate) {
        this.pcpFirstSendToNdpDate = pcpFirstSendToNdpDate;
    }

    public Date getPcpLastSendToNdpDate() {
        return pcpLastSendToNdpDate;
    }

    public void setPcpLastSendToNdpDate(Date pcpLastSendToNdpDate) {
        this.pcpLastSendToNdpDate = pcpLastSendToNdpDate;
    }

    public Boolean getNdpRecommended() {
        return ndpRecommended;
    }

    public void setNdpRecommended(Boolean ndpRecommended) {
        this.ndpRecommended = ndpRecommended;
    }

    public Date getNdpRecommendedOn() {
        return ndpRecommendedOn;
    }

    public void setNdpRecommendedOn(Date ndpRecommendedOn) {
        this.ndpRecommendedOn = ndpRecommendedOn;
    }

    public Boolean getCabinetApproved() {
        return cabinetApproved;
    }

    public void setCabinetApproved(Boolean cabinetApproved) {
        this.cabinetApproved = cabinetApproved;
    }

    public Date getCabinetApprovalOn() {
        return cabinetApprovalOn;
    }

    public void setCabinetApprovalOn(Date cabinetApprovalOn) {
        this.cabinetApprovalOn = cabinetApprovalOn;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Area getProvince() {
        return province;
    }

    public void setProvince(Area province) {
        this.province = province;
    }

    public String getFileNumber() {
        return fileNumber;
    }

    public void setFileNumber(String fileNumber) {
        this.fileNumber = fileNumber;
    }

    public Area getDistrict() {
        return district;
    }

    public void setDistrict(Area district) {
        this.district = district;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    public Double getProjectCost() {
        return projectCost;
    }

    public void setProjectCost(Double projectCost) {
        this.projectCost = projectCost;
    }

    public Item getProjectCostUnit() {
        return projectCostUnit;
    }

    public void setProjectCostUnit(Item projectCostUnit) {
        this.projectCostUnit = projectCostUnit;
    }

    public Item getSourceOfFunds() {
        return sourceOfFunds;
    }

    public void setSourceOfFunds(Item sourceOfFunds) {
        this.sourceOfFunds = sourceOfFunds;
    }

    public Date getProposalDate() {
        return proposalDate;
    }

    public void setProposalDate(Date proposalDate) {
        this.proposalDate = proposalDate;
    }

    public Item getSector() {
        return sector;
    }

    public void setSector(Item sector) {
        this.sector = sector;
    }

    public Boolean getAllIsland() {
        return allIsland;
    }

    public void setAllIsland(Boolean allIsland) {
        this.allIsland = allIsland;
    }

    public ProjectStageType getCurrentStageType() {
        return currentStageType;
    }

    public void setCurrentStageType(ProjectStageType currentStageType) {
        this.currentStageType = currentStageType;
    }

    public WebUser getCreater() {
        return creater;
    }

    public void setCreater(WebUser creater) {
        this.creater = creater;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRetired() {
        return retired;
    }

    public void setRetired(boolean retired) {
        this.retired = retired;
    }

    public WebUser getRetirer() {
        return retirer;
    }

    public void setRetirer(WebUser retirer) {
        this.retirer = retirer;
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

    public WebUser getLastEditor() {
        return lastEditor;
    }

    public void setLastEditor(WebUser lastEditor) {
        this.lastEditor = lastEditor;
    }

    public Date getLastEditAt() {
        return lastEditAt;
    }

    public void setLastEditAt(Date lastEditAt) {
        this.lastEditAt = lastEditAt;
    }

    public void setPecRecommendationRecordedBy(WebUser pecRecommendationRecordedBy) {
        this.pecRecommendationRecordedBy = pecRecommendationRecordedBy;
    }

    public Date getPecRecommendationRecordedAt() {
        return pecRecommendationRecordedAt;
    }

    public void setPecRecommendationRecordedAt(Date pecRecommendationRecordedAt) {
        this.pecRecommendationRecordedAt = pecRecommendationRecordedAt;
    }

    public String getPecRecommendationComments() {
        return pecRecommendationComments;
    }

    public void setPecRecommendationComments(String pecRecommendationComments) {
        this.pecRecommendationComments = pecRecommendationComments;
    }

    public WebUser getPecRejectionRecordedBy() {
        return pecRejectionRecordedBy;
    }

    public void setPecRejectionRecordedBy(WebUser pecRejectionRecordedBy) {
        this.pecRejectionRecordedBy = pecRejectionRecordedBy;
    }

    public Date getPecRejectionRecordedAt() {
        return pecRejectionRecordedAt;
    }

    public void setPecRejectionRecordedAt(Date pecRejectionRecordedAt) {
        this.pecRejectionRecordedAt = pecRejectionRecordedAt;
    }

    public String getPecRejectionComments() {
        return pecRejectionComments;
    }

    public void setPecRejectionComments(String pecRejectionComments) {
        this.pecRejectionComments = pecRejectionComments;
    }

    public WebUser getNdpSubmissionRecordedUser() {
        return ndpSubmissionRecordedUser;
    }

    public void setNdpSubmissionRecordedUser(WebUser ndpSubmissionRecordedUser) {
        this.ndpSubmissionRecordedUser = ndpSubmissionRecordedUser;
    }

    public Date getNdpSubmissionRecordedAt() {
        return ndpSubmissionRecordedAt;
    }

    public void setNdpSubmissionRecordedAt(Date ndpSubmissionRecordedAt) {
        this.ndpSubmissionRecordedAt = ndpSubmissionRecordedAt;
    }

    public String getNdpSubmissionComments() {
        return ndpSubmissionComments;
    }

    public void setNdpSubmissionComments(String ndpSubmissionComments) {
        this.ndpSubmissionComments = ndpSubmissionComments;
    }

    public WebUser getNdpRecommendationRecordedBy() {
        return ndpRecommendationRecordedBy;
    }

    public void setNdpRecommendationRecordedBy(WebUser ndpRecommendationRecordedBy) {
        this.ndpRecommendationRecordedBy = ndpRecommendationRecordedBy;
    }

    public Date getNdpApprovalRecordedAt() {
        return ndpApprovalRecordedAt;
    }

    public void setNdpApprovalRecordedAt(Date ndpApprovalRecordedAt) {
        this.ndpApprovalRecordedAt = ndpApprovalRecordedAt;
    }

    public String getNdpRecommendationComments() {
        return ndpRecommendationComments;
    }

    public void setNdpRecommendationComments(String ndpRecommendationComments) {
        this.ndpRecommendationComments = ndpRecommendationComments;
    }

    public WebUser getNdpRejectionRecordedBy() {
        return ndpRejectionRecordedBy;
    }

    public void setNdpRejectionRecordedBy(WebUser ndpRejectionRecordedBy) {
        this.ndpRejectionRecordedBy = ndpRejectionRecordedBy;
    }

    public Date getNdpRejectionRecorderAt() {
        return ndpRejectionRecorderAt;
    }

    public void setNdpRejectionRecorderAt(Date ndpRejectionRecorderAt) {
        this.ndpRejectionRecorderAt = ndpRejectionRecorderAt;
    }

    public String getNdpRejectionComments() {
        return ndpRejectionComments;
    }

    public void setNdpRejectionComments(String ndpRejectionComments) {
        this.ndpRejectionComments = ndpRejectionComments;
    }

    public WebUser getCabinetSubmissionRecordedBy() {
        return cabinetSubmissionRecordedBy;
    }

    public void setCabinetSubmissionRecordedBy(WebUser cabinetSubmissionRecordedBy) {
        this.cabinetSubmissionRecordedBy = cabinetSubmissionRecordedBy;
    }

    public Date getCabinetSubmissionRecordedAt() {
        return cabinetSubmissionRecordedAt;
    }

    public void setCabinetSubmissionRecordedAt(Date cabinetSubmissionRecordedAt) {
        this.cabinetSubmissionRecordedAt = cabinetSubmissionRecordedAt;
    }

    public String getCabinetSubmissionComments() {
        return cabinetSubmissionComments;
    }

    public void setCabinetSubmissionComments(String cabinetSubmissionComments) {
        this.cabinetSubmissionComments = cabinetSubmissionComments;
    }

    public WebUser getCabinetApprovalRecordedBy() {
        return cabinetApprovalRecordedBy;
    }

    public void setCabinetApprovalRecordedBy(WebUser cabinetApprovalRecordedBy) {
        this.cabinetApprovalRecordedBy = cabinetApprovalRecordedBy;
    }

    public Date getCabinetApprovalRecordedAt() {
        return cabinetApprovalRecordedAt;
    }

    public void setCabinetApprovalRecordedAt(Date cabinetApprovalRecordedAt) {
        this.cabinetApprovalRecordedAt = cabinetApprovalRecordedAt;
    }

    public String getCabinetApprovalComments() {
        return cabinetApprovalComments;
    }

    public void setCabinetApprovalComments(String cabinetApprovalComments) {
        this.cabinetApprovalComments = cabinetApprovalComments;
    }

    public Boolean getPecRecomended() {
        return pecRecomended;
    }

    public void setPecRecomended(Boolean pecRecomended) {
        this.pecRecomended = pecRecomended;
    }

    public Date getPecRecommendedOn() {
        return pecRecommendedOn;
    }

    public void setPecRecommendedOn(Date pecRecommendedOn) {
        this.pecRecommendedOn = pecRecommendedOn;
    }

    public Boolean getPecRejected() {
        return pecRejected;
    }

    public void setPecRejected(Boolean pecRejected) {
        this.pecRejected = pecRejected;
    }

    public Date getPecRejectedOn() {
        return pecRejectedOn;
    }

    public void setPecRejectedOn(Date pecRejectedOn) {
        this.pecRejectedOn = pecRejectedOn;
    }

    public Boolean getNdpRejected() {
        return ndpRejected;
    }

    public void setNdpRejected(Boolean ndpRejected) {
        this.ndpRejected = ndpRejected;
    }

    public Date getNdpRejectedOn() {
        return ndpRejectedOn;
    }

    public void setNdpRejectedOn(Date ndpRejectedOn) {
        this.ndpRejectedOn = ndpRejectedOn;
    }

    public Boolean getNdpSubmitted() {
        return ndpSubmitted;
    }

    public void setNdpSubmitted(Boolean ndpSubmitted) {
        this.ndpSubmitted = ndpSubmitted;
    }

    public Date getNdpSubmittedOn() {
        return ndpSubmittedOn;
    }

    public void setNdpSubmittedOn(Date ndpSubmittedOn) {
        this.ndpSubmittedOn = ndpSubmittedOn;
    }

    public WebUser getPecRecommendationRecordedBy() {
        return pecRecommendationRecordedBy;
    }

    public Boolean getCabinetSubmitted() {
        return cabinetSubmitted;
    }

    public void setCabinetSubmitted(Boolean cabinetSubmitted) {
        this.cabinetSubmitted = cabinetSubmitted;
    }

    public Date getCabinetSubmittedOn() {
        return cabinetSubmittedOn;
    }

    public void setCabinetSubmittedOn(Date cabinetSubmittedOn) {
        this.cabinetSubmittedOn = cabinetSubmittedOn;
    }

    public WebUser getCabinetRejectionRecordedBy() {
        return cabinetRejectionRecordedBy;
    }

    public void setCabinetRejectionRecordedBy(WebUser cabinetRejectionRecordedBy) {
        this.cabinetRejectionRecordedBy = cabinetRejectionRecordedBy;
    }

    public Date getCabinetRejectionRecordedAt() {
        return cabinetRejectionRecordedAt;
    }

    public void setCabinetRejectionRecordedAt(Date cabinetRejectionRecordedAt) {
        this.cabinetRejectionRecordedAt = cabinetRejectionRecordedAt;
    }

    public String getCabinetRejectionComments() {
        return cabinetRejectionComments;
    }

    public void setCabinetRejectionComments(String cabinetRejectionComments) {
        this.cabinetRejectionComments = cabinetRejectionComments;
    }

    public boolean isCanApproveAtPec() {
        if (currentStageType == ProjectStageType.Awaiting_PEC_Approval || currentStageType == ProjectStageType.PEC_Rejected) {
            canApproveAtPec = true;
        } else {
            canApproveAtPec = false;
        }
        return canApproveAtPec;
    }

    public boolean isCanRejectAtPec() {
        if (currentStageType == ProjectStageType.Awaiting_PEC_Approval) {
            canRejectAtPec = true;
        } else {
            canRejectAtPec = false;
        }
        return canRejectAtPec;
    }

    public boolean isCanSubmitToNdp() {
        if (currentStageType == ProjectStageType.Awaiting_DNP_Submission || currentStageType == ProjectStageType.DNP_Rejected) {
            canSubmitToNdp = true;
        } else {
            canSubmitToNdp = false;
        }
        return canSubmitToNdp;
    }

    public boolean isCanApproveAtNdp() {
        if (currentStageType == ProjectStageType.Awaiting_DNP_Approval||currentStageType == ProjectStageType.DNP_Rejected||currentStageType == ProjectStageType.DNP_Revision) {
            canApproveAtNdp = true;
        } else {
            canApproveAtNdp = false;
        }
        return canApproveAtNdp;
    }

    public boolean isCanRejectAtNdp() {
        if (currentStageType == ProjectStageType.Awaiting_DNP_Approval||currentStageType == ProjectStageType.DNP_Revision) {
            canRejectAtNdp = true;
        } else {
            canRejectAtNdp = false;
        }
        return canRejectAtNdp;
    }

    public boolean isCanSubmitToCabinet() {
        if (currentStageType == ProjectStageType.Awaiting_Cabinet_Submission || currentStageType == ProjectStageType.Cabinet_Rejected) {
            canSubmitToCabinet = true;
        } else {
            canSubmitToCabinet = false;
        }
        return canSubmitToCabinet;
    }

    public boolean isCanApproveAtCabinet() {
        if (currentStageType == ProjectStageType.Awaiting_Cabinet_Approval) {
            canApproveAtCabinet = true;
        } else {
            canApproveAtCabinet = false;
        }
        return canApproveAtCabinet;
    }

    public boolean isCanRejectAtCabinet() {
        if (currentStageType == ProjectStageType.Awaiting_Cabinet_Approval) {
            canRejectAtCabinet = true;
        } else {
            canRejectAtCabinet = false;
        }
        return canRejectAtCabinet;
    }

    public boolean isCanMarkAsOngoing() {
        if (currentStageType == ProjectStageType.Cabinet_Approved) {
            canMarkAsOngoing = true;
        } else {
            canMarkAsOngoing = false;
        }
        return canMarkAsOngoing;
    }

    public boolean isCanMarkAsCompleted() {
        if (currentStageType == ProjectStageType.Ongoing) {
            canMarkAsCompleted = true;
        } else {
            canMarkAsCompleted = false;
        }
        return canMarkAsCompleted;
    }

    public Boolean getCabinetRejected() {
        return cabinetRejected;
    }

    public Boolean getIncompletePcp() {
        return incompletePcp;
    }

    public void setIncompletePcp(Boolean incompletePcp) {
        this.incompletePcp = incompletePcp;
    }

    public Date getIncompletePcpDecidedDate() {
        return incompletePcpDecidedDate;
    }

    public void setIncompletePcpDecidedDate(Date incompletePcpDecidedDate) {
        this.incompletePcpDecidedDate = incompletePcpDecidedDate;
    }

    public Boolean getPecRevision() {
        return pecRevision;
    }

    public void setPecRevision(Boolean pecRevision) {
        this.pecRevision = pecRevision;
    }

    public Date getPecRevisionStartedOn() {
        return pecRevisionStartedOn;
    }

    public void setPecRevisionStartedOn(Date pecRevisionStartedOn) {
        this.pecRevisionStartedOn = pecRevisionStartedOn;
    }

    public Boolean getNdpRevision() {
        return ndpRevision;
    }

    public void setNdpRevision(Boolean ndpRevision) {
        this.ndpRevision = ndpRevision;
    }

    public Date getNdpRevisionStartedOn() {
        return ndpRevisionStartedOn;
    }

    public void setNdpRevisionStartedOn(Date ndpRevisionStartedOn) {
        this.ndpRevisionStartedOn = ndpRevisionStartedOn;
    }

    public Boolean getFundsAllocated() {
        return fundsAllocated;
    }

    public void setFundsAllocated(Boolean fundsAllocated) {
        this.fundsAllocated = fundsAllocated;
    }

    public Date getFundsAllocatedOn() {
        return fundsAllocatedOn;
    }

    public void setFundsAllocatedOn(Date fundsAllocatedOn) {
        this.fundsAllocatedOn = fundsAllocatedOn;
    }

    public WebUser getPcpMarkedAsIncompleteBy() {
        return pcpMarkedAsIncompleteBy;
    }

    public void setPcpMarkedAsIncompleteBy(WebUser pcpMarkedAsIncompleteBy) {
        this.pcpMarkedAsIncompleteBy = pcpMarkedAsIncompleteBy;
    }

    public Date getPcpMarkedAsIncompleteAt() {
        return pcpMarkedAsIncompleteAt;
    }

    public void setPcpMarkedAsIncompleteAt(Date pcpMarkedAsIncompleteAt) {
        this.pcpMarkedAsIncompleteAt = pcpMarkedAsIncompleteAt;
    }

    public String getPcpIncompleteComments() {
        return pcpIncompleteComments;
    }

    public void setPcpIncompleteComments(String pcpIncompleteComments) {
        this.pcpIncompleteComments = pcpIncompleteComments;
    }

    public WebUser getPecReviewRecordedBy() {
        return pecReviewRecordedBy;
    }

    public void setPecReviewRecordedBy(WebUser pecReviewRecordedBy) {
        this.pecReviewRecordedBy = pecReviewRecordedBy;
    }

    public Date getPecReviewRecordedAt() {
        return pecReviewRecordedAt;
    }

    public void setPecReviewRecordedAt(Date pecReviewRecordedAt) {
        this.pecReviewRecordedAt = pecReviewRecordedAt;
    }

    public String getPecReviewComments() {
        return pecReviewComments;
    }

    public void setPecReviewComments(String pecReviewComments) {
        this.pecReviewComments = pecReviewComments;
    }

    public WebUser getNdpRevisionRecordedBy() {
        return ndpRevisionRecordedBy;
    }

    public void setNdpRevisionRecordedBy(WebUser ndpRevisionRecordedBy) {
        this.ndpRevisionRecordedBy = ndpRevisionRecordedBy;
    }

    public Date getNdpRevisionRecordedAt() {
        return ndpRevisionRecordedAt;
    }

    public void setNdpRevisionRecordedAt(Date ndpRevisionRecordedAt) {
        this.ndpRevisionRecordedAt = ndpRevisionRecordedAt;
    }

    public String getNdpRevisionComments() {
        return ndpRevisionComments;
    }

    public void setNdpRevisionComments(String ndpRevisionComments) {
        this.ndpRevisionComments = ndpRevisionComments;
    }

    public WebUser getFundAllocationDoneRecordedBy() {
        return fundAllocationDoneRecordedBy;
    }

    public void setFundAllocationDoneRecordedBy(WebUser fundAllocationDoneRecordedBy) {
        this.fundAllocationDoneRecordedBy = fundAllocationDoneRecordedBy;
    }

    public Date getFundAllocationRecordedAt() {
        return fundAllocationRecordedAt;
    }

    public void setFundAllocationRecordedAt(Date fundAllocationRecordedAt) {
        this.fundAllocationRecordedAt = fundAllocationRecordedAt;
    }

    public String getFundAllocationComments() {
        return fundAllocationComments;
    }

    public void setFundAllocationComments(String fundAllocationComments) {
        this.fundAllocationComments = fundAllocationComments;
    }

    public void setCabinetRejected(Boolean cabinetRejected) {
        this.cabinetRejected = cabinetRejected;
    }

    public Date getCabinetRejectedOn() {
        return cabinetRejectedOn;
    }

    public void setCabinetRejectedOn(Date cabinetRejectedOn) {
        this.cabinetRejectedOn = cabinetRejectedOn;
    }

    public Boolean getOnoing() {
        return onoing;
    }

    public void setOnoing(Boolean onoing) {
        this.onoing = onoing;
    }

    public Date getOngoingStartedOn() {
        return ongoingStartedOn;
    }

    public void setOngoingStartedOn(Date ongoingStartedOn) {
        this.ongoingStartedOn = ongoingStartedOn;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public Date getCompletedOn() {
        return completedOn;
    }

    public void setCompletedOn(Date completedOn) {
        this.completedOn = completedOn;
    }

    public WebUser getOngoingMarkedUser() {
        return ongoingMarkedUser;
    }

    public void setOngoingMarkedUser(WebUser ongoingMarkedUser) {
        this.ongoingMarkedUser = ongoingMarkedUser;
    }

    public Date getOngoingMarkedAt() {
        return ongoingMarkedAt;
    }

    public void setOngoingMarkedAt(Date ongoingMarkedAt) {
        this.ongoingMarkedAt = ongoingMarkedAt;
    }

    public String getOngoingRecommendation() {
        return ongoingRecommendation;
    }

    public void setOngoingRecommendation(String ongoingRecommendation) {
        this.ongoingRecommendation = ongoingRecommendation;
    }

    public WebUser getCompletedMarkedUser() {
        return completedMarkedUser;
    }

    public void setCompletedMarkedUser(WebUser completedMarkedUser) {
        this.completedMarkedUser = completedMarkedUser;
    }

    public Date getCompleteMarkedAt() {
        return completeMarkedAt;
    }

    public void setCompleteMarkedAt(Date completeMarkedAt) {
        this.completeMarkedAt = completeMarkedAt;
    }

    public String getCompleteRecommendation() {
        return completeRecommendation;
    }

    public void setCompleteRecommendation(String completeRecommendation) {
        this.completeRecommendation = completeRecommendation;
    }

    public boolean isCanReviewAtPec() {
        return canReviewAtPec;
    }

    public boolean isCanReviewAtNdp() {
        return canReviewAtNdp;
    }

    public boolean isCanAllocateFunds() {
        return canAllocateFunds;
    }

    public List<FormSet> getProjectProvinces() {
        if (projectProvinces == null) {
            projectProvinces = new ArrayList<>();
        }
        return projectProvinces;
    }

    public void setProjectProvinces(List<FormSet> projectProvinces) {
        this.projectProvinces = projectProvinces;
    }

    public List<FormItem> getProjectDistricts() {
        if (projectDistricts == null) {
            projectDistricts = new ArrayList<>();
        }
        return projectDistricts;
    }

    public void setProjectDistricts(List<FormItem> projectDistricts) {
        this.projectDistricts = projectDistricts;
    }

    public List<EncounterFormItem> getProjectLocations() {
        if (projectLocations == null) {
            projectLocations = new ArrayList<>();
        }
        return projectLocations;
    }

    public void setProjectLocations(List<EncounterFormItem> projectLocations) {
        this.projectLocations = projectLocations;
    }

    public List<Form> getSourcesOfFunds() {
        if (sourcesOfFunds == null) {
            sourcesOfFunds = new ArrayList<>();
        }
        return sourcesOfFunds;
    }

    public void setSourcesOfFunds(List<Form> sourcesOfFunds) {
        this.sourcesOfFunds = sourcesOfFunds;
    }

    public String getProvincesStr() {
        provincesStr = "";
        for (FormSet pa : getProjectProvinces()) {
            provincesStr += " " + pa.getArea().getName();
        }
        return provincesStr;
    }

    public String getDistrictsStr() {
        districtsStr = "";
        for (FormItem pa : getProjectDistricts()) {
            districtsStr += " " + pa.getArea().getName();
        }
        return districtsStr;
    }

    public String getLocationsStr() {
        locationsStr = "";
        for(EncounterFormItem pi: getProjectLocations()){
            locationsStr += " " + pi.getInstitution().getName();
        }
        return locationsStr;
    }

    public String getSourcesOfFundsStr() {
        return sourcesOfFundsStr;
    }

    public String getProposalDateComments() {
        return proposalDateComments;
    }

    public void setProposalDateComments(String proposalDateComments) {
        this.proposalDateComments = proposalDateComments;
    }

    public Institution getPcpReceivedBy() {
        return pcpReceivedBy;
    }

    public void setPcpReceivedBy(Institution pcpReceivedBy) {
        this.pcpReceivedBy = pcpReceivedBy;
    }

    
    
}
