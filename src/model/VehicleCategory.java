package model;

import model.enums.VehicleCategoryType;

public class VehicleCategory {

    private String id;
    private VehicleCategoryType type;
    private double dailyPrice;

    public VehicleCategory() {
    }

    public VehicleCategory(String id, VehicleCategoryType type, double dailyPrice) {
        this.id = id;
        this.type = type;
        this.dailyPrice = dailyPrice;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public VehicleCategoryType getType() { return type; }
    public void setType(VehicleCategoryType type) { this.type = type; }

    public double getDailyPrice() { return dailyPrice; }
    public void setDailyPrice(double dailyPrice) { this.dailyPrice = dailyPrice; }

    @Override
    public String toString() {
        return type.name();
    }
}
