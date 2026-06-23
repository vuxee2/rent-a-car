package model;

import model.enums.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Reservation {

    private String id;
    private String clientId;
    private String vehicleModelId;
    private LocalDate startDate;
    private LocalDate endDate;
    private ReservationStatus status;
    private double totalPrice;
    private String additionalServiceIds;
    private LocalDateTime createdAt;
    private String agentId;
    private LocalDateTime cancelledAt; // null dok nije otkazana

    public Reservation() {
    }

    public Reservation(String id, String clientId, String vehicleModelId,
                        LocalDate startDate, LocalDate endDate, ReservationStatus status,
                        double totalPrice, String additionalServiceIds,
                        LocalDateTime createdAt, String agentId) {
        this(id, clientId, vehicleModelId, startDate, endDate, status, totalPrice,
                additionalServiceIds, createdAt, agentId, null);
    }

    public Reservation(String id, String clientId, String vehicleModelId,
                        LocalDate startDate, LocalDate endDate, ReservationStatus status,
                        double totalPrice, String additionalServiceIds,
                        LocalDateTime createdAt, String agentId, LocalDateTime cancelledAt) {
        this.id = id;
        this.clientId = clientId;
        this.vehicleModelId = vehicleModelId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.totalPrice = totalPrice;
        this.additionalServiceIds = additionalServiceIds;
        this.createdAt = createdAt;
        this.agentId = agentId;
        this.cancelledAt = cancelledAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getVehicleModelId() { return vehicleModelId; }
    public void setVehicleModelId(String vehicleModelId) { this.vehicleModelId = vehicleModelId; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getAdditionalServiceIds() { return additionalServiceIds; }
    public void setAdditionalServiceIds(String additionalServiceIds) { this.additionalServiceIds = additionalServiceIds; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }

    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
}
