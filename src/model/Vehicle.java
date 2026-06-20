package model;

import model.enums.VehicleStatus;

public class Vehicle {

    private String id;
    private String modelId;
    private String licensePlate;
    private VehicleStatus status;
    private int mileage;

    public Vehicle() {
    }

    public Vehicle(String id, String modelId, String licensePlate, VehicleStatus status, int mileage) {
        this.id = id;
        this.modelId = modelId;
        this.licensePlate = licensePlate;
        this.status = status;
        this.mileage = mileage;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public VehicleStatus getStatus() { return status; }
    public void setStatus(VehicleStatus status) { this.status = status; }

    public int getMileage() { return mileage; }
    public void setMileage(int mileage) { this.mileage = mileage; }
}
