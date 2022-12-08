/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lk.gov.health.phsp.enums;

/**
 *
 * @author www.divudi.com
 */
public enum Privilege {
    //Main Menu Privileges
    Client_Management("Client Management"),
    Encounter_Management("Clinic Management"),
    Analytics("Analytics"),
    @Deprecated
    Appointment_Management("Appointment Management"),
    @Deprecated
    Lab_Management("Lab Management"),
    @Deprecated
    Pharmacy_Management("Pharmacy Management"),
    User("User"),
    Institution_Administration("Institution Administration"),
    System_Administration("System Administration"),
    //Client Management
    Search_client("Search Client"),
    Add_Client("Add Client"),
    Edit_client("Edit Client"),
    Delete_client("Delete Client"),
    Reserve_Phn("Reserver PHN"),
    //Clinic Management
    Add_to_clinic("Add to Clinic"),
    Remove_from_clinic("Remove from Clinic"),
    Add_clinic_visit("Clinic Visit"),
    Complete_clinic_visit("Complete Clinic Visit"),
    Incomplete_clinic_visit("Incomplete Clinic Visit"),
    //Analytics
    Counts("Counts"),
    Indicators("Indicators"),
    Templates("Templates"),
    Named_Lists("Named Lists"),
    Anonymous_Lists("Anonymous Lists"),
    @Deprecated
    Search_any_Client_by_IDs("Search any Client by IDs"),
    @Deprecated
    Search_any_Client_by_Details("Search any Client by Details"),
    @Deprecated
    Search_any_client_by_ID_of_Authorised_Areas("Search any client by ID of Authorised Areas"),
    @Deprecated
    Search_any_client_by_Details_of_Authorised_Areas("Search any client by Details of Authorised Areas"),
    @Deprecated
    Search_any_client_by_ID_of_Authorised_Institutions("Search any client by ID of Authorised Institutions"),
    @Deprecated
    Search_any_client_by_Details_of_Authorised_Institutions("Search any client by Details of Authorised Institutions"),
    //Institution Administration
    Manage_Institution_Users("Manage Institution Users"),
    @Deprecated
    Manage_Institution_Metadata("Manage Institution Metadata"),
    Manage_Authorised_Areas("Manage Authorised Areas"),
    Manage_Authorised_Institutions("Manage Authorised Institutions"),
    //System Administration
    Manage_Users("Manage Users"),
    Manage_Metadata("Manage Metadata"),
    Manage_Area("Manage Area"),
    Manage_Institutions("Manage Institutions"),
    Manage_Forms("Manage Forms"),
    //Monitoring and Evaluation
    Monitoring_and_evaluation("Monitoring & Evaluation"),
    Monitoring_and_evaluation_reports("Monitoring & Evaluation Reports");
    
    public final String label;    
    private Privilege(String label){
        this.label = label;
    }
    
    public String getLabel(){
        return label;
    }

}
