package lk.gov.health.phsp.enums;

/**
 *
 * @author Dr M H B Ariyaratne
 */
public enum InstitutionType {
    Ministry_of_Health("Ministry of Health"),   
    Provincial_Department_of_Health_Services("Provincial Department of Health Services"),
    Regional_Department_of_Health_Department("Regional Department of Health Department"),
    National_Hospital("National Hospital"),
    Teaching_Hospital("Teaching Hospital"),
    District_General_Hospital("District General Hospital"),
    Base_Hospital("Base Hospital"),
    Divisional_Hospital("Divisional Hospital"),
    Primary_Medical_Care_Unit("Primary Medical Care Unit"),
    MOH_Office("MOH Office"),
    Clinic("Clinic"),
    Unit("Unit"),
    Ward("Ward"),
    Stake_Holder("Stake Holder"),
    Partner("Partner"),
    Private_Sector_Institute("Private Sector Institute"),
    Other("Other");
    
    private final String label;
    
    private InstitutionType(String label){
        this.label = label;
    }
    
    public String getLabel(){
        return label;
    }
}
