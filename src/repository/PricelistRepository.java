package repository;

import model.Pricelist;
import util.CSVParser;
import util.DateUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Format reda:
 * id;validFrom;validTo;defaultRentalDays;annualSubscriptionPrice;lateReturnFeePerDay;
 * studentDiscountPercent;pensionerDiscountPercent;companyDiscountPercent;createdByAdminId
 */
public class PricelistRepository {

    private static final String FILE_PATH = "data/pricelists.csv";
    private static final String DELIMITER = ";";

    public List<Pricelist> findAll() {
        List<Pricelist> result = new ArrayList<>();
        for (String[] row : CSVParser.read(FILE_PATH, DELIMITER)) {
            try {
                result.add(new Pricelist(
                        row[0],
                        DateUtil.parse(row[1]),
                        row[2].isBlank() ? null : DateUtil.parse(row[2]),
                        Integer.parseInt(row[3]),
                        Double.parseDouble(row[4]),
                        Double.parseDouble(row[5]),
                        Double.parseDouble(row[6]),
                        Double.parseDouble(row[7]),
                        Double.parseDouble(row[8]),
                        row[9]
                ));
            } catch (Exception e) {
                System.err.println("Greska pri parsiranju cenovnika: " + String.join(DELIMITER, row));
            }
        }
        return result;
    }

    public Optional<Pricelist> findById(String id) {
        return findAll().stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    /** Trenutno vazeci cenovnik na dati datum (najnoviji koji pokriva taj datum). */
    public Optional<Pricelist> findActiveOn(LocalDate date) {
        return findAll().stream()
                .filter(p -> p.isActiveOn(date))
                .sorted((a, b) -> b.getValidFrom().compareTo(a.getValidFrom()))
                .findFirst();
    }

    public void save(Pricelist pricelist) {
        List<Pricelist> all = findAll();
        boolean exists = all.stream().anyMatch(p -> p.getId().equals(pricelist.getId()));

        if (exists) {
            List<String[]> rows = all.stream()
                    .map(p -> p.getId().equals(pricelist.getId()) ? toRow(pricelist) : toRow(p))
                    .collect(Collectors.toList());
            CSVParser.writeAll(FILE_PATH, rows, DELIMITER);
        } else {
            CSVParser.append(FILE_PATH, toRow(pricelist), DELIMITER);
        }
    }

    private String[] toRow(Pricelist p) {
        return new String[]{
                p.getId(),
                DateUtil.format(p.getValidFrom()),
                p.getValidTo() == null ? "" : DateUtil.format(p.getValidTo()),
                String.valueOf(p.getDefaultRentalDays()),
                String.valueOf(p.getAnnualSubscriptionPrice()),
                String.valueOf(p.getLateReturnFeePerDay()),
                String.valueOf(p.getStudentDiscountPercent()),
                String.valueOf(p.getPensionerDiscountPercent()),
                String.valueOf(p.getCompanyDiscountPercent()),
                p.getCreatedByAdminId() == null ? "" : p.getCreatedByAdminId()
        };
    }
}
