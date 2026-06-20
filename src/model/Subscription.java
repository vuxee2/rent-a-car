package model;

import model.enums.SubscriptionStatus;

import java.time.LocalDate;

public class Subscription {

    private String id;
    private String clientId;
    private LocalDate startDate;
    private LocalDate endDate;
    private SubscriptionStatus status;
    private double paidAmount;
    private String agentId;

    public Subscription() {
    }

    public Subscription(String id, String clientId, LocalDate startDate, LocalDate endDate,
                         SubscriptionStatus status, double paidAmount, String agentId) {
        this.id = id;
        this.clientId = clientId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.paidAmount = paidAmount;
        this.agentId = agentId;
    }

    public boolean isCurrentlyActive() {
        return status == SubscriptionStatus.ACTIVE
                && endDate != null
                && !endDate.isBefore(LocalDate.now());
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public SubscriptionStatus getStatus() { return status; }
    public void setStatus(SubscriptionStatus status) { this.status = status; }

    public double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(double paidAmount) { this.paidAmount = paidAmount; }

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
}
