package manager;

import model.Reservation;
import model.Rental;
import model.Vehicle;
import model.enums.ReservationStatus;
import model.enums.VehicleStatus;
import repository.PricelistRepository;
import repository.RentalRepository;
import repository.ReservationRepository;
import util.AppContext;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class RentalManager {

    private static final int DEFAULT_RENTAL_DAYS = 3;

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

        // Status vozila -> RENTED, ne moze se ponovo izdati dok ne bude DOSTUPNO
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

        AppContext.getInstance().getReservationRepository().delete(rental.getReservationId()); // ako se vrate kola brise se rezervacija

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

    public static int getDefaultRentalDays() {
        return DEFAULT_RENTAL_DAYS;
    }
}
