package repository;

import model.VehicleCategory;
import model.enums.VehicleCategoryType;
import util.CSVParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Format reda: id;type;dailyPrice */
public class VehicleCategoryRepository {

    private static final String FILE_PATH = "data/vehicle_categories.csv";
    private static final String DELIMITER = ";";

    public List<VehicleCategory> findAll() {
        List<VehicleCategory> result = new ArrayList<>();
        for (String[] row : CSVParser.read(FILE_PATH, DELIMITER)) {
            try {
                result.add(new VehicleCategory(
                        row[0],
                        VehicleCategoryType.valueOf(row[1]),
                        Double.parseDouble(row[2])
                ));
            } catch (Exception e) {
                System.err.println("Greska pri parsiranju kategorije vozila: " + String.join(DELIMITER, row));
            }
        }
        return result;
    }

    public Optional<VehicleCategory> findById(String id) {
        return findAll().stream().filter(c -> c.getId().equals(id)).findFirst();
    }

    public void save(VehicleCategory category) {
        List<VehicleCategory> all = findAll();
        boolean exists = all.stream().anyMatch(c -> c.getId().equals(category.getId()));

        if (exists) {
            List<String[]> rows = all.stream()
                    .map(c -> c.getId().equals(category.getId()) ? toRow(category) : toRow(c))
                    .collect(Collectors.toList());
            CSVParser.writeAll(FILE_PATH, rows, DELIMITER);
        } else {
            CSVParser.append(FILE_PATH, toRow(category), DELIMITER);
        }
    }

    public void delete(String id) {
        List<String[]> rows = findAll().stream()
                .filter(c -> !c.getId().equals(id))
                .map(this::toRow)
                .collect(Collectors.toList());
        CSVParser.writeAll(FILE_PATH, rows, DELIMITER);
    }

    private String[] toRow(VehicleCategory c) {
        return new String[]{ c.getId(), c.getType().name(), String.valueOf(c.getDailyPrice()) };
    }
}
