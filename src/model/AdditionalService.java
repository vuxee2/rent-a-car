package model;

import model.enums.ChargeType;
import model.enums.ServiceType;

public class AdditionalService {

    private String id;
    private ServiceType type;
    private double price;
    private ChargeType chargeType;

    public AdditionalService() {
    }

    public AdditionalService(String id, ServiceType type, double price, ChargeType chargeType) {
        this.id = id;
        this.type = type;
        this.price = price;
        this.chargeType = chargeType;
    }

    public String getDisplayName() {
        return switch (type) {
            case GPS -> "GPS navigacija";
            case CHILD_SEAT -> "Dečije sedište";
            case EXTRA_INSURANCE -> "Dodatno osiguranje";
            case EXTENDED_USE -> "Produženo korišćenje (po danu)";
        };
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public ServiceType getType() { return type; }
    public void setType(ServiceType type) { this.type = type; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public ChargeType getChargeType() { return chargeType; }
    public void setChargeType(ChargeType chargeType) { this.chargeType = chargeType; }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
