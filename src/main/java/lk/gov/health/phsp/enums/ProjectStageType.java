package lk.gov.health.phsp.enums;

/**
 *
 * @author Dr M H B Ariyaratne
 */
public enum ProjectStageType {
    Awaiting_PEC_Approval("Awaiting PEC Approval"),
    Incomplete_Pcp("Incomplete PCP"),
    PEC_Rejected("Rejected by PEC"),
    Awaiting_DNP_Submission("Awaiting NDP Submission"),
    Awaiting_DNP_Approval("Awaiting NDP Approval"),
    DNP_Revision("Under NDP Revision"),
    DNP_Rejected("Rejected by NDP"),
    Awaiting_Cabinet_Submission("Awaiting Cabinet Submission"),
    Awaiting_Cabinet_Approval("Awaiting Cabinet Approval"),
    Cabinet_Approved("Approved by the Cabinet"),
    Cabinet_Rejected("Rejected by the Cabinet"),
    Funds_Allocated("Funds Allocated"),
    Ongoing("Ongoing"),
    Completed("Completed");

    public final String label;    
    private ProjectStageType(String label){
        this.label = label;
    }
    
    public String getLabel(){
        return label;
    }
}
