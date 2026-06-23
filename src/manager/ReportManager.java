package manager;

import model.*;
import model.enums.ReservationStatus;
import repository.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

public class ReportManager {

    private final ReservationRepository reservationRepository;
    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;
    private final VehicleModelRepository vehicleModelRepository;
    private final SubscriptionRepository subscriptionRepository;

    public ReportManager(ReservationRepository reservationRepository,
                          RentalRepository rentalRepository,
                          UserRepository userRepository,
                          VehicleModelRepository vehicleModelRepository,
                          SubscriptionRepository subscriptionRepository) {
        this.reservationRepository = reservationRepository;
        this.rentalRepository = rentalRepository;
        this.userRepository = userRepository;
        this.vehicleModelRepository = vehicleModelRepository;
        this.subscriptionRepository = subscriptionRepository;
    }
    
    public Map<String, Long> getAgentIssuedVehiclesReport(LocalDate start, LocalDate end) {
        List<Rental> rentalsInPeriod = rentalRepository.findAll().stream()
                .filter(r -> isWithinRange(r.getExpectedReturnDate(), start, end)
                        || isWithinRange(r.getActualReturnDate(), start, end))
                .toList();

        Map<String, Long> countByAgentId = rentalsInPeriod.stream()
                .collect(Collectors.groupingBy(Rental::getAgentId, Collectors.counting()));

        Map<String, Long> result = new LinkedHashMap<>();
        for (Map.Entry<String, Long> entry : countByAgentId.entrySet()) {
            String agentName = userRepository.findById(entry.getKey())
                    .map(User::getFullName)
                    .orElse(entry.getKey());
            result.put(agentName, entry.getValue());
        }
        return result;
    }

    public Map<ReservationStatus, Long> getReservationStatusReport(LocalDate start, LocalDate end) {
        List<Reservation> reservationsInPeriod = reservationRepository.findAll().stream()
                .filter(r -> r.getCreatedAt() != null)
                .filter(r -> !r.getCreatedAt().toLocalDate().isBefore(start) && !r.getCreatedAt().toLocalDate().isAfter(end))
                .toList();

        Map<ReservationStatus, Long> result = new EnumMap<>(ReservationStatus.class);
        for (ReservationStatus status : ReservationStatus.values()) {
            result.put(status, reservationsInPeriod.stream().filter(r -> r.getStatus() == status).count());
        }
        return result;
    }

    public List<VehicleModelReportRow> getVehicleModelReport(LocalDate start, LocalDate end) {
        List<VehicleModel> allModels = vehicleModelRepository.findAll();
        List<Reservation> reservationsInPeriod = reservationRepository.findAll().stream()
                .filter(r -> r.getCreatedAt() != null)
                .filter(r -> !r.getCreatedAt().toLocalDate().isBefore(start) && !r.getCreatedAt().toLocalDate().isAfter(end))
                .toList();

        List<VehicleModelReportRow> result = new ArrayList<>();
        for (VehicleModel model : allModels) {
            long reservationCount = reservationsInPeriod.stream()
                    .filter(r -> r.getVehicleModelId().equals(model.getId()))
                    .count();

            long rentalCount = reservationsInPeriod.stream()
                    .filter(r -> r.getVehicleModelId().equals(model.getId()))
                    .filter(r -> rentalRepository.findByReservationId(r.getId()).isPresent())
                    .count();

            result.add(new VehicleModelReportRow(model.getFullName(), model.getCategoryId(), reservationCount, rentalCount));
        }
        return result;
    }

    public FinancialReport getFinancialReport(LocalDate start, LocalDate end) {
        double subscriptionIncome = subscriptionRepository.findAll().stream()
                .filter(s -> isWithinRange(s.getStartDate(), start, end))
                .mapToDouble(Subscription::getPaidAmount)
                .sum();

        double rentalIncome = reservationRepository.findAll().stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
                .filter(r -> r.getCreatedAt() != null)
                .filter(r -> !r.getCreatedAt().toLocalDate().isBefore(start) && !r.getCreatedAt().toLocalDate().isAfter(end))
                .mapToDouble(Reservation::getTotalPrice)
                .sum();

        double lateFeesIncome = rentalRepository.findAll().stream()
                .filter(r -> isWithinRange(r.getActualReturnDate(), start, end))
                .mapToDouble(Rental::getLateFee)
                .sum();

        double totalIncome = subscriptionIncome + rentalIncome + lateFeesIncome;

        double salaryExpense = userRepository.findAll().stream()
                .filter(u -> u instanceof Employee)
                .map(u -> (Employee) u)
                .mapToDouble(Employee::calculateSalary)
                .sum();

        return new FinancialReport(subscriptionIncome, rentalIncome, lateFeesIncome, totalIncome, salaryExpense);
    }

    public MonthlyRevenueReport getMonthlyRevenueByCategory(int months) {
        List<YearMonth> timeline = new ArrayList<>();
        YearMonth current = YearMonth.now();
        for (int i = months - 1; i >= 0; i--) {
            timeline.add(current.minusMonths(i));
        }
        Map<YearMonth, Integer> indexOf = new HashMap<>();
        for (int i = 0; i < timeline.size(); i++) {
            indexOf.put(timeline.get(i), i);
        }
        List<String> monthLabels = timeline.stream().map(YearMonth::toString).collect(Collectors.toList());

        List<String> categories = List.of("STUDENT", "PENSIONER", "COMPANY", "BEZ KATEGORIJE");
        Map<String, List<Double>> revenuePerCategory = new LinkedHashMap<>();
        for (String c : categories) {
            revenuePerCategory.put(c, new ArrayList<>(Collections.nCopies(months, 0.0)));
        }
        List<Double> totalPerMonth = new ArrayList<>(Collections.nCopies(months, 0.0));

        // 1) Najmovi + dodatne usluge iz potvrđenih rezervacija
        for (Reservation r : reservationRepository.findAll()) {
            if (r.getStatus() != ReservationStatus.CONFIRMED || r.getCreatedAt() == null) continue;
            Integer idx = indexOf.get(YearMonth.from(r.getCreatedAt().toLocalDate()));
            if (idx == null) continue;
            accumulate(revenuePerCategory, totalPerMonth, categoryOfClient(r.getClientId()), idx, r.getTotalPrice());
        }

        // 2) Uplacene pretplate
        for (Subscription s : subscriptionRepository.findAll()) {
            if (s.getStartDate() == null) continue;
            Integer idx = indexOf.get(YearMonth.from(s.getStartDate()));
            if (idx == null) continue;
            accumulate(revenuePerCategory, totalPerMonth, categoryOfClient(s.getClientId()), idx, s.getPaidAmount());
        }

        // 3) Naplacene kazne
        for (Rental rental : rentalRepository.findAll()) {
            if (rental.getActualReturnDate() == null || rental.getLateFee() <= 0) continue;
            Integer idx = indexOf.get(YearMonth.from(rental.getActualReturnDate()));
            if (idx == null) continue;
            String category = reservationRepository.findById(rental.getReservationId())
                    .map(res -> categoryOfClient(res.getClientId()))
                    .orElse("BEZ KATEGORIJE");
            accumulate(revenuePerCategory, totalPerMonth, category, idx, rental.getLateFee());
        }

        return new MonthlyRevenueReport(monthLabels, revenuePerCategory, totalPerMonth);
    }

    private void accumulate(Map<String, List<Double>> perCategory, List<Double> total,
                             String category, int idx, double amount) {
        List<Double> series = perCategory.get(category);
        if (series != null) {
            series.set(idx, series.get(idx) + amount);
        }
        total.set(idx, total.get(idx) + amount);
    }

    private String categoryOfClient(String clientId) {
        User u = userRepository.findById(clientId).orElse(null);
        if (u instanceof Client c && c.getCategory() != null) {
            return c.getCategory().name();
        }
        return "BEZ KATEGORIJE";
    }

    public Map<String, Long> getAgentWorkloadLast30Days() {
        LocalDate from = LocalDate.now().minusDays(30);
        List<Reservation> recentlyHandled = reservationRepository.findAll().stream()
                .filter(r -> r.getAgentId() != null && !r.getAgentId().isBlank())
                .filter(r -> r.getCreatedAt() != null && !r.getCreatedAt().toLocalDate().isBefore(from))
                .toList();

        Map<String, Long> countByAgentId = recentlyHandled.stream()
                .collect(Collectors.groupingBy(Reservation::getAgentId, Collectors.counting()));

        Map<String, Long> result = new LinkedHashMap<>();
        for (Map.Entry<String, Long> entry : countByAgentId.entrySet()) {
            String agentName = userRepository.findById(entry.getKey()).map(User::getFullName).orElse(entry.getKey());
            result.put(agentName, entry.getValue());
        }
        return result;
    }

    public Map<ReservationStatus, Long> getReservationStatusLast30Days() {
        LocalDate from = LocalDate.now().minusDays(30);
        return getReservationStatusReport(from, LocalDate.now());
    }

    private boolean isWithinRange(LocalDate date, LocalDate start, LocalDate end) {
        return date != null && !date.isBefore(start) && !date.isAfter(end);
    }

    // ---------------- pomocne klase za rezultate izvestaja ----------------

    public record VehicleModelReportRow(String modelName, String categoryId, long reservationCount, long rentalCount) {}

    public record MonthlyRevenueReport(List<String> monthLabels,
                                        Map<String, List<Double>> revenuePerCategory,
                                        List<Double> totalPerMonth) {}

    public record FinancialReport(double subscriptionIncome, double rentalIncome, double lateFeesIncome,
                                   double totalIncome, double salaryExpense) {
        public double getNetResult() {
            return totalIncome - salaryExpense;
        }
    }
}
