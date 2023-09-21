package lk.gov.health.phsp.enums;

/**
 * Enum to define the search criteria for finding patients or other entities.
 * Each value represents a specific type of search that can be performed.
 *
 * @author buddh thanks to ChatGPT
 */
public enum SearchCriteria {
    NIC_ONLY("Search by National Identity Card number"),
    PHN_ONLY("Search by Personal Health Number"),
    PASSPORT_ONLY("Search by Passport number"),
    SCN_ONLY("Search by Senior Citizen Number"),
    DL_ONLY("Search by Driver's License number"),
    TELEPHONE_NUMBER_ONLY("Search by Telephone number"),
    PART_OF_NAME_ONLY("Search by part of the name"),
    PART_OF_NAME_AND_AGE_IN_YEARS("Search by part of the name and age in years"),
    PART_OF_NAME_AND_BIRTH_YEAR("Search by part of the name and birth year"),
    PART_OF_NAME_AND_BIRTH_YEAR_AND_MONTH("Search by part of the name, birth year, and birth month"),
    PART_OF_NAME_AND_DATE_OF_BIRTH("Search by part of the name and full date of birth");

    private final String description;

    SearchCriteria(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
