package model;

import java.time.LocalDate;


public class Pricelist {

    private String id;
    private LocalDate validFrom;
    private LocalDate validTo; // null = jos uvek vazi
    private int defaultRentalDays;
    private double annualSubscriptionPrice;
    private double lateReturnFeePerDay;
    private double studentDiscountPercent;
    private double pensionerDiscountPercent;
    private double companyDiscountPercent;
    private String createdByAdminId;

    public Pricelist() {
    }

    public Pricelist(String id, LocalDate validFrom, LocalDate validTo, int defaultRentalDays,
                      double annualSubscriptionPrice, double lateReturnFeePerDay,
                      double studentDiscountPercent, double pensionerDiscountPercent,
                      double companyDiscountPercent, String createdByAdminId) {
        this.id = id;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.defaultRentalDays = defaultRentalDays;
        this.annualSubscriptionPrice = annualSubscriptionPrice;
        this.lateReturnFeePerDay = lateReturnFeePerDay;
        this.studentDiscountPercent = studentDiscountPercent;
        this.pensionerDiscountPercent = pensionerDiscountPercent;
        this.companyDiscountPercent = companyDiscountPercent;
        this.createdByAdminId = createdByAdminId;
    }

    public boolean isActiveOn(LocalDate date) {
        boolean afterStart = !date.isBefore(validFrom);
        boolean beforeEnd = (validTo == null) || !date.isAfter(validTo);
        return afterStart && beforeEnd;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LocalDate getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDate validFrom) { this.validFrom = validFrom; }

    public LocalDate getValidTo() { return validTo; }
    public void setValidTo(LocalDate validTo) { this.validTo = validTo; }

    public int getDefaultRentalDays() { return defaultRentalDays; }
    public void setDefaultRentalDays(int defaultRentalDays) { this.defaultRentalDays = defaultRentalDays; }

    public double getAnnualSubscriptionPrice() { return annualSubscriptionPrice; }
    public void setAnnualSubscriptionPrice(double annualSubscriptionPrice) { this.annualSubscriptionPrice = annualSubscriptionPrice; }

    public double getLateReturnFeePerDay() { return lateReturnFeePerDay; }
    public void setLateReturnFeePerDay(double lateReturnFeePerDay) { this.lateReturnFeePerDay = lateReturnFeePerDay; }

    public double getStudentDiscountPercent() { return studentDiscountPercent; }
    public void setStudentDiscountPercent(double studentDiscountPercent) { this.studentDiscountPercent = studentDiscountPercent; }

    public double getPensionerDiscountPercent() { return pensionerDiscountPercent; }
    public void setPensionerDiscountPercent(double pensionerDiscountPercent) { this.pensionerDiscountPercent = pensionerDiscountPercent; }

    public double getCompanyDiscountPercent() { return companyDiscountPercent; }
    public void setCompanyDiscountPercent(double companyDiscountPercent) { this.companyDiscountPercent = companyDiscountPercent; }

    public String getCreatedByAdminId() { return createdByAdminId; }
    public void setCreatedByAdminId(String createdByAdminId) { this.createdByAdminId = createdByAdminId; }
}
