package model;

import java.time.LocalDate;


public class Rental {

    private String id;
    private String reservationId;
    private String vehicleId;
    private String agentId;
    private int mileageAtPickup;
    private int mileageAtReturn; // -1 dok vozilo nije vraceno
    private LocalDate expectedReturnDate;
    private LocalDate actualReturnDate; // null dok nije vraceno
    private double lateFee;

    public Rental() {
    }

    public Rental(String id, String reservationId, String vehicleId, String agentId,
                  int mileageAtPickup, int mileageAtReturn, LocalDate expectedReturnDate,
                  LocalDate actualReturnDate, double lateFee) {
        this.id = id;
        this.reservationId = reservationId;
        this.vehicleId = vehicleId;
        this.agentId = agentId;
        this.mileageAtPickup = mileageAtPickup;
        this.mileageAtReturn = mileageAtReturn;
        this.expectedReturnDate = expectedReturnDate;
        this.actualReturnDate = actualReturnDate;
        this.lateFee = lateFee;
    }

    public boolean isReturned() {
        return actualReturnDate != null;
    }

    public boolean isLate() {
        return actualReturnDate != null && expectedReturnDate != null && actualReturnDate.isAfter(expectedReturnDate);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getReservationId() { return reservationId; }
    public void setReservationId(String reservationId) { this.reservationId = reservationId; }

    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }

    public int getMileageAtPickup() { return mileageAtPickup; }
    public void setMileageAtPickup(int mileageAtPickup) { this.mileageAtPickup = mileageAtPickup; }

    public int getMileageAtReturn() { return mileageAtReturn; }
    public void setMileageAtReturn(int mileageAtReturn) { this.mileageAtReturn = mileageAtReturn; }

    public LocalDate getExpectedReturnDate() { return expectedReturnDate; }
    public void setExpectedReturnDate(LocalDate expectedReturnDate) { this.expectedReturnDate = expectedReturnDate; }

    public LocalDate getActualReturnDate() { return actualReturnDate; }
    public void setActualReturnDate(LocalDate actualReturnDate) { this.actualReturnDate = actualReturnDate; }

    public double getLateFee() { return lateFee; }
    public void setLateFee(double lateFee) { this.lateFee = lateFee; }
}
