package lk.gov.health.phsp.enums;

/**
 *
 * @author Dr M H B Ariyaratne
 */
public enum ProjectStageType {
    Awaiting_PEC_Approval,
    Incomplete_Pcp,
    PEC_Rejected,
    Awaiting_DNP_Submission,
    Awaiting_DNP_Approval,
    DNP_Revision,
    DNP_Rejected,
    Awaiting_Cabinet_Submission,
    Awaiting_Cabinet_Approval,
    Cabinet_Approved,
    Cabinet_Rejected,
    Funds_Allocated,
    Ongoing,
    Completed;

    public String getLabel() {
        switch (this) {
            case Awaiting_PEC_Approval:
                return "Awaiting PEC Approval";
            case Incomplete_Pcp:
                return "Incomplete PCP";
            case PEC_Rejected:
                return "Rejected by PEC";
            case Awaiting_DNP_Submission:
                return "Awaiting NDP Submission";
            case Awaiting_DNP_Approval:
                return "Awaiting NDP Approval";
            case DNP_Revision:
                return "Under NDP Revision";
            case DNP_Rejected:
                return "Rejected by NDP";
            case Awaiting_Cabinet_Submission:
                return "Awaiting Cabinet Submission";
            case Awaiting_Cabinet_Approval:
                return "Awaiting Cabinet Approval";
            case Cabinet_Approved:
                return "Approved by the Cabinet";
            case Cabinet_Rejected:
                return "Rejected by the Cabinet";
            case Funds_Allocated:
                return "Funds Allocated";
            case Ongoing:
                return "Ongoing";
            case Completed:
                return "Completed";
            default:
                return this.toString();
        }
    }
}
