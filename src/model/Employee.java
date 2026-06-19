package model;

import model.enums.Gender;
import model.enums.UserRole;

import java.time.LocalDate;

public abstract class Employee extends User {

    protected String educationLevel;
    protected int yearsOfService;
    protected double baseSalary;
    protected double coefficient;

    public Employee() {
        super();
    }

    public Employee(String id, String firstName, String lastName, Gender gender,
                     LocalDate birthDate, String phone, String address,
                     String username, String password, UserRole role,
                     String educationLevel, int yearsOfService,
                     double baseSalary, double coefficient) {
        super(id, firstName, lastName, gender, birthDate, phone, address, username, password, role);
        this.educationLevel = educationLevel;
        this.yearsOfService = yearsOfService;
        this.baseSalary = baseSalary;
        this.coefficient = coefficient;
    }

    public double calculateSalary() {
        return baseSalary * (coefficient + 0.004 * yearsOfService);
    }

    public String getEducationLevel() { return educationLevel; }
    public void setEducationLevel(String educationLevel) { this.educationLevel = educationLevel; }

    public int getYearsOfService() { return yearsOfService; }
    public void setYearsOfService(int yearsOfService) { this.yearsOfService = yearsOfService; }

    public double getBaseSalary() { return baseSalary; }
    public void setBaseSalary(double baseSalary) { this.baseSalary = baseSalary; }

    public double getCoefficient() { return coefficient; }
    public void setCoefficient(double coefficient) { this.coefficient = coefficient; }
}
