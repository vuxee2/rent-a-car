package repository;

import model.Rental;
import util.CSVParser;
import util.DateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Format reda:
 * id;reservationId;vehicleId;agentId;mileageAtPickup;mileageAtReturn;expectedReturnDate;actualReturnDate;lateFee
*/
public class RentalRepository {

    private static final String FILE_PATH = "data/rentals.csv";
    private static final String DELIMITER = ";";

    public List<Rental> findAll() {
        List<Rental> result = new ArrayList<>();
        for (String[] row : CSVParser.read(FILE_PATH, DELIMITER)) {
            try {
                result.add(new Rental(
                        row[0],
                        row[1],
                        row[2],
                        row[3],
                        Integer.parseInt(row[4]),
                        row[5].isBlank() ? -1 : Integer.parseInt(row[5]),
                        DateUtil.parse(row[6]),
                        DateUtil.parse(row[7]),
                        row[8].isBlank() ? 0.0 : Double.parseDouble(row[8])
                ));
            } catch (Exception e) {
                System.err.println("Greska pri parsiranju izdavanja: " + String.join(DELIMITER, row));
            }
        }
        return result;
    }

    public Optional<Rental> findById(String id) {
        return findAll().stream().filter(r -> r.getId().equals(id)).findFirst();
    }

    public Optional<Rental> findByReservationId(String reservationId) {
        return findAll().stream().filter(r -> r.getReservationId().equals(reservationId)).findFirst();
    }

    public List<Rental> findByVehicleId(String vehicleId) {
        return findAll().stream().filter(r -> r.getVehicleId().equals(vehicleId)).collect(Collectors.toList());
    }

    public List<Rental> findByAgentId(String agentId) {
        return findAll().stream().filter(r -> r.getAgentId().equals(agentId)).collect(Collectors.toList());
    }

    public List<Rental> findActive() {
        return findAll().stream().filter(r -> !r.isReturned()).collect(Collectors.toList());
    }

    public void save(Rental rental) {
        List<Rental> all = findAll();
        boolean exists = all.stream().anyMatch(r -> r.getId().equals(rental.getId()));

        if (exists) {
            List<String[]> rows = all.stream()
                    .map(r -> r.getId().equals(rental.getId()) ? toRow(rental) : toRow(r))
                    .collect(Collectors.toList());
            CSVParser.writeAll(FILE_PATH, rows, DELIMITER);
        } else {
            CSVParser.append(FILE_PATH, toRow(rental), DELIMITER);
        }
    }

    public void delete(String id) {
        List<String[]> rows = findAll().stream()
                .filter(r -> !r.getId().equals(id))
                .map(this::toRow)
                .collect(Collectors.toList());
        CSVParser.writeAll(FILE_PATH, rows, DELIMITER);
    }

    private String[] toRow(Rental r) {
        return new String[]{
                r.getId(),
                r.getReservationId(),
                r.getVehicleId(),
                r.getAgentId(),
                String.valueOf(r.getMileageAtPickup()),
                r.getMileageAtReturn() < 0 ? "" : String.valueOf(r.getMileageAtReturn()),
                DateUtil.format(r.getExpectedReturnDate()),
                DateUtil.format(r.getActualReturnDate()),
                String.valueOf(r.getLateFee())
        };
    }
}
