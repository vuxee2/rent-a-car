package manager;

import model.Vehicle;
import model.VehicleCategory;
import model.VehicleModel;
import model.enums.VehicleStatus;
import repository.VehicleCategoryRepository;
import repository.VehicleModelRepository;
import repository.VehicleRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class VehicleManager {

    private final VehicleRepository vehicleRepository;
    private final VehicleModelRepository vehicleModelRepository;
    private final VehicleCategoryRepository vehicleCategoryRepository;

    public VehicleManager(VehicleRepository vehicleRepository,
                           VehicleModelRepository vehicleModelRepository,
                           VehicleCategoryRepository vehicleCategoryRepository) {
        this.vehicleRepository = vehicleRepository;
        this.vehicleModelRepository = vehicleModelRepository;
        this.vehicleCategoryRepository = vehicleCategoryRepository;
    }

    public List<VehicleModel> getAllModels() {
        return vehicleModelRepository.findAll();
    }

    public List<VehicleCategory> getAllCategories() {
        return vehicleCategoryRepository.findAll();
    }

    public Optional<VehicleModel> getModelById(String modelId) {
        return vehicleModelRepository.findById(modelId);
    }

    public Optional<VehicleCategory> getCategoryById(String categoryId) {
        return vehicleCategoryRepository.findById(categoryId);
    }

    public List<Vehicle> getVehiclesForModel(String modelId) {
        return vehicleRepository.findByModelId(modelId);
    }

    public boolean hasAnyVehicleOfModel(String modelId) {
        return !vehicleRepository.findByModelId(modelId).isEmpty();
    }

    public List<Vehicle> getAvailableVehiclesForModel(String modelId) {
        return vehicleRepository.findByModelId(modelId).stream()
                .filter(v -> v.getStatus() == VehicleStatus.AVAILABLE)
                .collect(Collectors.toList());
    }

    public List<VehicleModel> searchModels(String manufacturerOrName, String categoryId) {
        return vehicleModelRepository.findAll().stream()
                .filter(m -> manufacturerOrName == null || manufacturerOrName.isBlank()
                        || m.getFullName().toLowerCase().contains(manufacturerOrName.toLowerCase()))
                .filter(m -> categoryId == null || categoryId.isBlank() || m.getCategoryId().equals(categoryId))
                .collect(Collectors.toList());
    }

    public void setVehicleStatus(String vehicleId, VehicleStatus status) {
        vehicleRepository.findById(vehicleId).ifPresent(v -> {
            v.setStatus(status);
            vehicleRepository.save(v);
        });
    }

    public void addVehicle(Vehicle vehicle) {
        vehicleRepository.save(vehicle);
    }

    public void addVehicleModel(VehicleModel model) {
        vehicleModelRepository.save(model);
    }

    public void addVehicleCategory(VehicleCategory category) {
        vehicleCategoryRepository.save(category);
    }

    public void setVehicleMileage(String id, int mileage) {
        Optional<Vehicle> vehicle = vehicleRepository.findById(id);
        if(vehicle.isPresent()) {
            vehicle.get().setMileage(mileage);
            vehicleRepository.save(vehicle.get());
        }
    }
}
