package repository;

import model.Subscription;
import model.enums.SubscriptionStatus;
import util.CSVParser;
import util.DateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Format reda: id;clientId;startDate;endDate;status;paidAmount;agentId
 */
public class SubscriptionRepository {

    private static final String FILE_PATH = "data/subscriptions.csv";
    private static final String DELIMITER = ";";

    public List<Subscription> findAll() {
        List<Subscription> result = new ArrayList<>();
        for (String[] row : CSVParser.read(FILE_PATH, DELIMITER)) {
            try {
                result.add(new Subscription(
                        row[0],
                        row[1],
                        DateUtil.parse(row[2]),
                        DateUtil.parse(row[3]),
                        SubscriptionStatus.valueOf(row[4]),
                        Double.parseDouble(row[5]),
                        row.length > 6 ? row[6] : ""
                ));
            } catch (Exception e) {
                System.err.println("Greska pri parsiranju pretplate: " + String.join(DELIMITER, row));
            }
        }
        return result;
    }

    public Optional<Subscription> findById(String id) {
        return findAll().stream().filter(s -> s.getId().equals(id)).findFirst();
    }

    public List<Subscription> findByClientId(String clientId) {
        return findAll().stream().filter(s -> s.getClientId().equals(clientId)).collect(Collectors.toList());
    }

    public void save(Subscription subscription) {
        List<Subscription> all = findAll();
        boolean exists = all.stream().anyMatch(s -> s.getId().equals(subscription.getId()));

        if (exists) {
            List<String[]> rows = all.stream()
                    .map(s -> s.getId().equals(subscription.getId()) ? toRow(subscription) : toRow(s))
                    .collect(Collectors.toList());
            CSVParser.writeAll(FILE_PATH, rows, DELIMITER);
        } else {
            CSVParser.append(FILE_PATH, toRow(subscription), DELIMITER);
        }
    }

    public void delete(String id) {
        List<String[]> rows = findAll().stream()
                .filter(s -> !s.getId().equals(id))
                .map(this::toRow)
                .collect(Collectors.toList());
        CSVParser.writeAll(FILE_PATH, rows, DELIMITER);
    }

    private String[] toRow(Subscription s) {
        return new String[]{
                s.getId(), s.getClientId(),
                DateUtil.format(s.getStartDate()), DateUtil.format(s.getEndDate()),
                s.getStatus().name(), String.valueOf(s.getPaidAmount()),
                s.getAgentId() == null ? "" : s.getAgentId()
        };
    }
}
