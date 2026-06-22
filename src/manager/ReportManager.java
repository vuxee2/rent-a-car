package manager;

import model.*;
import model.enums.ReservationStatus;
import repository.*;

import java.time.LocalDate;
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

    public Map<String, Double> getRevenueByClientCategoryLastMonths(int months) {
        LocalDate from = LocalDate.now().minusMonths(months);
        Map<String, Double> result = new LinkedHashMap<>();

        List<Reservation> reservations = reservationRepository.findAll().stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
                .filter(r -> r.getCreatedAt() != null && !r.getCreatedAt().toLocalDate().isBefore(from))
                .toList();

        for (Reservation r : reservations) {
            User client = userRepository.findById(r.getClientId()).orElse(null);
            String category = "BEZ KATEGORIJE";
            if (client instanceof Client c && c.getCategory() != null) {
                category = c.getCategory().name();
            }
            result.merge(category, r.getTotalPrice(), Double::sum);
        }
        return result;
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

    public record FinancialReport(double subscriptionIncome, double rentalIncome, double lateFeesIncome,
                                   double totalIncome, double salaryExpense) {
        public double getNetResult() {
            return totalIncome - salaryExpense;
        }
    }
}
