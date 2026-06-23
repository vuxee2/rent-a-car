package manager;

import model.Reservation;
import model.Rental;
import model.Vehicle;
import model.enums.ReservationStatus;
import model.enums.VehicleStatus;
import repository.PricelistRepository;
import repository.RentalRepository;
import repository.ReservationRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class RentalManager {
    private final RentalRepository rentalRepository;
    private final ReservationRepository reservationRepository;
    private final VehicleManager vehicleManager;
    private final PricelistRepository pricelistRepository;

    public RentalManager(RentalRepository rentalRepository,
                          ReservationRepository reservationRepository,
                          VehicleManager vehicleManager,
                          PricelistRepository pricelistRepository) {
        this.rentalRepository = rentalRepository;
        this.reservationRepository = reservationRepository;
        this.vehicleManager = vehicleManager;
        this.pricelistRepository = pricelistRepository;
    }

    
    public Rental issueVehicle(String reservationId, String vehicleId, String agentId, int mileageAtPickup) {
        return issueVehicle(reservationId, vehicleId, agentId, mileageAtPickup, "", 0.0, 0);
    }

    public Rental issueVehicle(String reservationId, String vehicleId, String agentId, int mileageAtPickup,
                                String addedServiceIdsCsv, double addedServicesCost) {
        return issueVehicle(reservationId, vehicleId, agentId, mileageAtPickup, addedServiceIdsCsv, addedServicesCost, 0);
    }

    /** Izdavanje vozila uz mogucnost da agent doda dodatne usluge u trenutku izdavanja. */
    public Rental issueVehicle(String reservationId, String vehicleId, String agentId, int mileageAtPickup,
                                String addedServiceIdsCsv, double addedServicesCost, int additionalDays) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalStateException("Rezervacija ne postoji."));

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Samo potvrđene rezervacije mogu biti izdate.");
        }

        Vehicle vehicle = vehicleManager.getVehiclesForModel(reservation.getVehicleModelId()).stream()
                .filter(v -> v.getId().equals(vehicleId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Izabrano vozilo ne odgovara rezervisanom modelu."));

        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            throw new IllegalStateException("Izabrano vozilo trenutno nije dostupno (status: " + vehicle.getStatus() + ").");
        }

        boolean reservationChanged = false;

        // Dodatne usluge koje agent dodaje pri izdavanju
        if (addedServiceIdsCsv != null && !addedServiceIdsCsv.isBlank()) {
            String existing = reservation.getAdditionalServiceIds();
            String merged = (existing == null || existing.isBlank())
                    ? addedServiceIdsCsv
                    : existing + "," + addedServiceIdsCsv;
            reservation.setAdditionalServiceIds(merged);
            reservation.setTotalPrice(reservation.getTotalPrice() + addedServicesCost);
            reservationChanged = true;
        }

        // Produzenje trajanja rezervacije (pomera datum vracanja)
        if (additionalDays > 0) {
            reservation.setEndDate(reservation.getEndDate().plusDays(additionalDays));
            reservationChanged = true;
        }

        if (reservationChanged) {
            reservationRepository.save(reservation);
        }

        vehicleManager.setVehicleStatus(vehicle.getId(), VehicleStatus.RENTED);

        Rental rental = new Rental(
                UUID.randomUUID().toString(),
                reservationId,
                vehicleId,
                agentId,
                mileageAtPickup,
                -1,
                reservation.getEndDate(),
                null,
                0.0
        );
        rentalRepository.save(rental);
        return rental;
    }

    public Rental returnVehicle(String rentalId, int mileageAtReturn, LocalDate actualReturnDate) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalStateException("Izdavanje ne postoji."));

        if (rental.isReturned()) {
            throw new IllegalStateException("Ovo vozilo je već vraćeno.");
        }

        if (mileageAtReturn < rental.getMileageAtPickup()) {
            throw new IllegalStateException("Kilometraža pri vraćanju ne može biti manja od kilometraže pri preuzimanju.");
        }

        double lateFee = calculateLateFee(rental, actualReturnDate);

        rental.setMileageAtReturn(mileageAtReturn);
        rental.setActualReturnDate(actualReturnDate);
        rental.setLateFee(lateFee);
        rentalRepository.save(rental);

        vehicleManager.setVehicleMileage(rental.getVehicleId(), mileageAtReturn);
        vehicleManager.setVehicleStatus(rental.getVehicleId(), VehicleStatus.AVAILABLE);

        return rental;
    }

    public double calculateLateFee(Rental rental, LocalDate actualReturnDate) {
        if (rental.getExpectedReturnDate() == null || !actualReturnDate.isAfter(rental.getExpectedReturnDate())) {
            return 0.0;
        }
        long lateDays = ChronoUnit.DAYS.between(rental.getExpectedReturnDate(), actualReturnDate);
        double feePerDay = pricelistRepository.findActiveOn(LocalDate.now())
                .map(p -> p.getLateReturnFeePerDay())
                .orElse(500.0); // podrazumevana kazna ako cenovnik nije definisan
        return lateDays * feePerDay;
    }

    public List<Rental> getActiveRentals() {
        return rentalRepository.findActive();
    }

    public List<Rental> getRentalsForAgent(String agentId) {
        return rentalRepository.findByAgentId(agentId);
    }

    public List<Rental> getRentalsInPeriod(LocalDate start, LocalDate end) {
        return rentalRepository.findAll().stream()
                .filter(r -> r.getActualReturnDate() != null || r.getExpectedReturnDate() != null)
                .filter(r -> {
                    LocalDate ref = r.getActualReturnDate() != null ? r.getActualReturnDate() : r.getExpectedReturnDate();
                    return !ref.isBefore(start) && !ref.isAfter(end);
                })
                .collect(Collectors.toList());
    }


    public int countLateReturnsForClient(String clientId, ReservationRepository reservationRepository) {
        List<String> clientReservationIds = reservationRepository.findByClientId(clientId).stream()
                .map(Reservation::getId)
                .collect(Collectors.toList());

        return (int) rentalRepository.findAll().stream()
                .filter(r -> clientReservationIds.contains(r.getReservationId()))
                .filter(Rental::isLate)
                .count();
    }

    public Optional<Rental> getRentalByReservationId(String reservationId) {
        return rentalRepository.findByReservationId(reservationId);
    }

    /** Rezervacije za koje se nije pojavio klijent postaju CANCELLED i klijentu se daje zabrana rezervisanja 24h */
    public int expireNoShowReservations() {
        int count = 0;
        for (Reservation r : reservationRepository.findByStatus(ReservationStatus.CONFIRMED)) {
            boolean notPickedUp = rentalRepository.findByReservationId(r.getId()).isEmpty();
            if (notPickedUp && r.getStartDate() != null && r.getStartDate().isBefore(LocalDate.now())) {
                r.setStatus(ReservationStatus.CANCELLED);
                r.setCancelledAt(LocalDateTime.now());
                reservationRepository.save(r);
                count++;
            }
        }
        return count;
    }
}
