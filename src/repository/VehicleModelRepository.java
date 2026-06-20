package repository;

import model.VehicleModel;
import util.CSVParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Format reda: id;manufacturer;name;categoryId */
public class VehicleModelRepository {

    private static final String FILE_PATH = "data/vehicle_models.csv";
    private static final String DELIMITER = ";";

    public List<VehicleModel> findAll() {
        List<VehicleModel> result = new ArrayList<>();
        for (String[] row : CSVParser.read(FILE_PATH, DELIMITER)) {
            try {
                result.add(new VehicleModel(row[0], row[1], row[2], row[3]));
            } catch (Exception e) {
                System.err.println("Greska pri parsiranju modela vozila: " + String.join(DELIMITER, row));
            }
        }
        return result;
    }

    public Optional<VehicleModel> findById(String id) {
        return findAll().stream().filter(m -> m.getId().equals(id)).findFirst();
    }

    public void save(VehicleModel model) {
        List<VehicleModel> all = findAll();
        boolean exists = all.stream().anyMatch(m -> m.getId().equals(model.getId()));

        if (exists) {
            List<String[]> rows = all.stream()
                    .map(m -> m.getId().equals(model.getId()) ? toRow(model) : toRow(m))
                    .collect(Collectors.toList());
            CSVParser.writeAll(FILE_PATH, rows, DELIMITER);
        } else {
            CSVParser.append(FILE_PATH, toRow(model), DELIMITER);
        }
    }

    public void delete(String id) {
        List<String[]> rows = findAll().stream()
                .filter(m -> !m.getId().equals(id))
                .map(this::toRow)
                .collect(Collectors.toList());
        CSVParser.writeAll(FILE_PATH, rows, DELIMITER);
    }

    private String[] toRow(VehicleModel m) {
        return new String[]{ m.getId(), m.getManufacturer(), m.getName(), m.getCategoryId() };
    }
}
