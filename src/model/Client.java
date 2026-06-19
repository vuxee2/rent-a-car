package model;

import model.enums.ClientCategory;
import model.enums.Gender;
import model.enums.UserRole;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Client extends User {

    private LocalDate licenseIssueDate;
    private ClientCategory category; // moze biti null

    public Client() {
        super();
        this.role = UserRole.CLIENT;
    }

    public Client(String id, String firstName, String lastName, Gender gender,
                  LocalDate birthDate, String phone, String address,
                  String username, String password,
                  LocalDate licenseIssueDate, ClientCategory category) {
        super(id, firstName, lastName, gender, birthDate, phone, address, username, password, UserRole.CLIENT);
        this.licenseIssueDate = licenseIssueDate;
        this.category = category;
    }

    public boolean hasLicenseAtLeastTwoYears() {
        if (licenseIssueDate == null) return false;
        return ChronoUnit.YEARS.between(licenseIssueDate, LocalDate.now()) >= 2;
    }

    public LocalDate getLicenseIssueDate() { return licenseIssueDate; }
    public void setLicenseIssueDate(LocalDate licenseIssueDate) { this.licenseIssueDate = licenseIssueDate; }

    public ClientCategory getCategory() { return category; }
    public void setCategory(ClientCategory category) { this.category = category; }
}
