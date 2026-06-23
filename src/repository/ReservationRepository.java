package repository;

import model.Reservation;
import model.enums.ReservationStatus;
import util.CSVParser;
import util.DateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Format reda: id;clientId;vehicleModelId;startDate;endDate;status;totalPrice;additionalServiceIds;createdAt;agentId;cancelledAt */
public class ReservationRepository {

    private static final String FILE_PATH = "data/reservations.csv";
    private static final String DELIMITER = ";";

    public List<Reservation> findAll() {
        List<Reservation> result = new ArrayList<>();
        for (String[] row : CSVParser.read(FILE_PATH, DELIMITER)) {
            try {
                result.add(new Reservation(
                        row[0],
                        row[1],
                        row[2],
                        DateUtil.parse(row[3]),
                        DateUtil.parse(row[4]),
                        ReservationStatus.valueOf(row[5]),
                        Double.parseDouble(row[6]),
                        row[7],
                        DateUtil.parseDateTime(row[8]),
                        row.length > 9 ? row[9] : "",
                        row.length > 10 ? DateUtil.parseDateTime(row[10]) : null
                ));
            } catch (Exception e) {
                System.err.println("Greska pri parsiranju rezervacije: " + String.join(DELIMITER, row));
            }
        }
        return result;
    }

    public Optional<Reservation> findById(String id) {
        return findAll().stream().filter(r -> r.getId().equals(id)).findFirst();
    }

    public List<Reservation> findByClientId(String clientId) {
        return findAll().stream().filter(r -> r.getClientId().equals(clientId)).collect(Collectors.toList());
    }

    public List<Reservation> findByStatus(ReservationStatus status) {
        return findAll().stream().filter(r -> r.getStatus() == status).collect(Collectors.toList());
    }

    public void save(Reservation reservation) {
        List<Reservation> all = findAll();
        boolean exists = all.stream().anyMatch(r -> r.getId().equals(reservation.getId()));

        if (exists) {
            List<String[]> rows = all.stream()
                    .map(r -> r.getId().equals(reservation.getId()) ? toRow(reservation) : toRow(r))
                    .collect(Collectors.toList());
            CSVParser.writeAll(FILE_PATH, rows, DELIMITER);
        } else {
            CSVParser.append(FILE_PATH, toRow(reservation), DELIMITER);
        }
    }

    public void delete(String id) {
        List<String[]> rows = findAll().stream()
                .filter(r -> !r.getId().equals(id))
                .map(this::toRow) // (r -> this.toRow(r))
                .collect(Collectors.toList());
        CSVParser.writeAll(FILE_PATH, rows, DELIMITER);
    }

    private String[] toRow(Reservation r) {
        return new String[]{
                r.getId(),
                r.getClientId(),
                r.getVehicleModelId(),
                DateUtil.format(r.getStartDate()),
                DateUtil.format(r.getEndDate()),
                r.getStatus().name(),
                String.valueOf(r.getTotalPrice()),
                r.getAdditionalServiceIds() == null ? "" : r.getAdditionalServiceIds(),
                DateUtil.format(r.getCreatedAt()),
                r.getAgentId() == null ? "" : r.getAgentId(),
                DateUtil.format(r.getCancelledAt())
        };
    }
}
