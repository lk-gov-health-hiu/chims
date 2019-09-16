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
    Client_Management,
    Encounter_Management,
    Appointment_Management,
    Lab_Management,
    Pharmacy_Management,
    User,
    Institution_Administration,
    System_Administration,
    //Client Management
    Add_Client,
    Search_any_Client_by_IDs,
    Search_any_Client_by_Details,
    Search_any_client_by_ID_of_Authorised_Areas,
    Search_any_client_by_Details_of_Authorised_Areas,
    Search_any_client_by_ID_of_Authorised_Institutions,
    Search_any_client_by_Details_of_Authorised_Institutions,
    //Institution Administration
    Manage_Institution_Users,
    Manage_Institution_Metadata,
    Manage_Authorised_Areas,
    Manage_Authorised_Institutions,
    //System Administration
    Manage_Users,
    Manage_Metadata,
    Manage_Area,
    Manage_Institutions,
    Manage_Forms,
    

}
