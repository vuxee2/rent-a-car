package model;

public class VehicleModel {

    private String id;
    private String manufacturer;
    private String name;
    private String categoryId;

    public VehicleModel() {
    }

    public VehicleModel(String id, String manufacturer, String name, String categoryId) {
        this.id = id;
        this.manufacturer = manufacturer;
        this.name = name;
        this.categoryId = categoryId;
    }

    public String getFullName() {
        return manufacturer + " " + name;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    @Override
    public String toString() {
        return getFullName();
    }
}
