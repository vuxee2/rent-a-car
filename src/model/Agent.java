package model;

import model.enums.Gender;
import model.enums.UserRole;

import java.time.LocalDate;

public class Agent extends Employee {

    public Agent() {
        super();
        this.role = UserRole.AGENT;
    }

    public Agent(String id, String firstName, String lastName, Gender gender,
                 LocalDate birthDate, String phone, String address,
                 String username, String password,
                 String educationLevel, int yearsOfService,
                 double baseSalary, double coefficient) {
        super(id, firstName, lastName, gender, birthDate, phone, address, username, password,
                UserRole.AGENT, educationLevel, yearsOfService, baseSalary, coefficient);
    }
}
