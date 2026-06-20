package repository;

import model.Vehicle;
import model.enums.VehicleStatus;
import util.CSVParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Format reda: id;modelId;licensePlate;status;mileage
 */
public class VehicleRepository {

    private static final String FILE_PATH = "data/vehicles.csv";
    private static final String DELIMITER = ";";

    public List<Vehicle> findAll() {
        List<Vehicle> result = new ArrayList<>();
        for (String[] row : CSVParser.read(FILE_PATH, DELIMITER)) {
            try {
                result.add(new Vehicle(
                        row[0], row[1], row[2],
                        VehicleStatus.valueOf(row[3]),
                        Integer.parseInt(row[4])
                ));
            } catch (Exception e) {
                System.err.println("Greska pri parsiranju vozila: " + String.join(DELIMITER, row));
            }
        }
        return result;
    }

    public Optional<Vehicle> findById(String id) {
        return findAll().stream().filter(v -> v.getId().equals(id)).findFirst();
    }

    public List<Vehicle> findByModelId(String modelId) {
        return findAll().stream().filter(v -> v.getModelId().equals(modelId)).collect(Collectors.toList());
    }

    public void save(Vehicle vehicle) {
        List<Vehicle> all = findAll();
        boolean exists = all.stream().anyMatch(v -> v.getId().equals(vehicle.getId()));

        if (exists) {
            List<String[]> rows = all.stream()
                    .map(v -> v.getId().equals(vehicle.getId()) ? toRow(vehicle) : toRow(v))
                    .collect(Collectors.toList());
            CSVParser.writeAll(FILE_PATH, rows, DELIMITER);
        } else {
            CSVParser.append(FILE_PATH, toRow(vehicle), DELIMITER);
        }
    }

    public void delete(String id) {
        List<String[]> rows = findAll().stream()
                .filter(v -> !v.getId().equals(id))
                .map(this::toRow)
                .collect(Collectors.toList());
        CSVParser.writeAll(FILE_PATH, rows, DELIMITER);
    }

    private String[] toRow(Vehicle v) {
        return new String[]{
                v.getId(), v.getModelId(), v.getLicensePlate(),
                v.getStatus().name(), String.valueOf(v.getMileage())
        };
    }
}
