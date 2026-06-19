package model;

import model.enums.Gender;
import model.enums.UserRole;

import java.time.LocalDate;

public class Administrator extends Employee {

    public Administrator() {
        super();
        this.role = UserRole.ADMINISTRATOR;
    }

    public Administrator(String id, String firstName, String lastName, Gender gender,
                          LocalDate birthDate, String phone, String address,
                          String username, String password,
                          String educationLevel, int yearsOfService,
                          double baseSalary, double coefficient) {
        super(id, firstName, lastName, gender, birthDate, phone, address, username, password,
                UserRole.ADMINISTRATOR, educationLevel, yearsOfService, baseSalary, coefficient);
    }
}
