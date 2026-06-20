package repository;

import model.AdditionalService;
import model.enums.ChargeType;
import model.enums.ServiceType;
import util.CSVParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Format reda: id;type;price;chargeType */
public class AdditionalServiceRepository {

    private static final String FILE_PATH = "data/additional_services.csv";
    private static final String DELIMITER = ";";

    public List<AdditionalService> findAll() {
        List<AdditionalService> result = new ArrayList<>();
        for (String[] row : CSVParser.read(FILE_PATH, DELIMITER)) {
            try {
                result.add(new AdditionalService(
                        row[0],
                        ServiceType.valueOf(row[1]),
                        Double.parseDouble(row[2]),
                        ChargeType.valueOf(row[3])
                ));
            } catch (Exception e) {
                System.err.println("Greska pri parsiranju dodatne usluge: " + String.join(DELIMITER, row));
            }
        }
        return result;
    }

    public Optional<AdditionalService> findById(String id) {
        return findAll().stream().filter(s -> s.getId().equals(id)).findFirst();
    }

    public void save(AdditionalService service) {
        List<AdditionalService> all = findAll();
        boolean exists = all.stream().anyMatch(s -> s.getId().equals(service.getId()));

        if (exists) {
            List<String[]> rows = all.stream()
                    .map(s -> s.getId().equals(service.getId()) ? toRow(service) : toRow(s))
                    .collect(Collectors.toList());
            CSVParser.writeAll(FILE_PATH, rows, DELIMITER);
        } else {
            CSVParser.append(FILE_PATH, toRow(service), DELIMITER);
        }
    }

    public void delete(String id) {
        List<String[]> rows = findAll().stream()
                .filter(s -> !s.getId().equals(id))
                .map(this::toRow)
                .collect(Collectors.toList());
        CSVParser.writeAll(FILE_PATH, rows, DELIMITER);
    }

    private String[] toRow(AdditionalService s) {
        return new String[]{ s.getId(), s.getType().name(), String.valueOf(s.getPrice()), s.getChargeType().name() };
    }
}
