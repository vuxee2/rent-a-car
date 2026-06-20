package manager;

import model.Client;
import model.Reservation;
import model.Vehicle;
import model.enums.ReservationStatus;
import repository.ReservationRepository;
import util.DateUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ReservationManager {

    private final ReservationRepository reservationRepository;
    private final VehicleManager vehicleManager;

    public ReservationManager(ReservationRepository reservationRepository,
                               VehicleManager vehicleManager) {
        this.reservationRepository = reservationRepository;
        this.vehicleManager = vehicleManager;
    }

    /**
     * Kreira zahtev za rezervaciju. Baca IllegalStateException sa porukom
     * objasnjenja ako neko od poslovnih pravila nije zadovoljeno — GUI sloj
     * hvata ovaj izuzetak i prikazuje poruku korisniku.
     */
    public Reservation createReservation(Client client, String vehicleModelId,
                                          LocalDate startDate, LocalDate endDate,
                                          String additionalServiceIds, double totalPrice) {

        if (!client.hasLicenseAtLeastTwoYears()) {
            throw new IllegalStateException("Ne možete rezervisati vozilo — vozačka dozvola mora biti starija od 2 godine.");
        }

        if (!canClientReserveNow(client.getId())) {
            throw new IllegalStateException("Ne možete kreirati novu rezervaciju u naredna 24h od poslednjeg otkazivanja.");
        }

        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalStateException("Datum preuzimanja ne može biti u prošlosti.");
        }

        if (endDate.isBefore(startDate)) {
            throw new IllegalStateException("Datum vraćanja mora biti posle datuma preuzimanja.");
        }

        if (!isModelAvailableInPeriod(vehicleModelId, startDate, endDate)) {
            throw new IllegalStateException("Nema slobodnih vozila ovog modela u izabranom periodu.");
        }

        Reservation reservation = new Reservation(
                UUID.randomUUID().toString(),
                client.getId(),
                vehicleModelId,
                startDate,
                endDate,
                ReservationStatus.PENDING,
                totalPrice,
                additionalServiceIds,
                LocalDateTime.now(),
                null
        );

        reservationRepository.save(reservation);
        return reservation;
    }

    /**
     * Postoji bar jedan primerak modela koji nije zauzet drugom aktivnom
     * (PENDING ili CONFIRMED) rezervacijom u trazenom periodu.
     */
    public boolean isModelAvailableInPeriod(String modelId, LocalDate start, LocalDate end) {
        List<Vehicle> vehiclesOfModel = vehicleManager.getVehiclesForModel(modelId);
        if (vehiclesOfModel.isEmpty()) return false;

        List<Reservation> activeReservations = reservationRepository.findAll().stream()
                .filter(r -> r.getVehicleModelId().equals(modelId))
                .filter(r -> r.getStatus() == ReservationStatus.PENDING || r.getStatus() == ReservationStatus.CONFIRMED)
                .filter(r -> DateUtil.isOverlapping(r.getStartDate(), r.getEndDate(), start, end))
                .collect(Collectors.toList());

        // Ako je broj fizickih vozila veci od broja preklapajucih rezervacija, ima slobodnog mesta
        return vehiclesOfModel.size() > activeReservations.size();
    }

    /**
     * Klijent ne sme da napravi novu rezervaciju u naredna 24h nakon sto je
     * otkazao rezervaciju (status CANCELLED), niti nakon sto se nije pojavio
     * (sto se takodje markira kao CANCELLED).
     */
    public boolean canClientReserveNow(String clientId) {
        return reservationRepository.findByClientId(clientId).stream()
                .filter(r -> r.getStatus() == ReservationStatus.CANCELLED)
                .noneMatch(r -> r.getCreatedAt() != null
                        && ChronoUnit.HOURS.between(r.getCreatedAt(), LocalDateTime.now()) < 24);
    }

    public List<Reservation> getReservationsForClient(String clientId) {
        return reservationRepository.findByClientId(clientId);
    }

    public List<Reservation> getPendingReservations() {
        return reservationRepository.findByStatus(ReservationStatus.PENDING);
    }

    public void cancelReservation(String reservationId) {
        reservationRepository.findById(reservationId).ifPresent(r -> {
            r.setStatus(ReservationStatus.CANCELLED);
            reservationRepository.save(r);
        });
    }

    public void confirmReservation(String reservationId, String agentId) {
        reservationRepository.findById(reservationId).ifPresent(r -> {
            if (!isModelAvailableInPeriod(r.getVehicleModelId(), r.getStartDate(), r.getEndDate())) {
                throw new IllegalStateException("Nema dostupnih vozila ovog modela za potvrdu rezervacije.");
            }
            r.setStatus(ReservationStatus.CONFIRMED);
            r.setAgentId(agentId);
            reservationRepository.save(r);
        });
    }

    public void rejectReservation(String reservationId, String agentId) {
        reservationRepository.findById(reservationId).ifPresent(r -> {
            r.setStatus(ReservationStatus.REJECTED);
            r.setAgentId(agentId);
            reservationRepository.save(r);
        });
    }

    /**
     * Rezervacije kojima je prosao datum pocetka, a jos su PENDING,
     * automatski se odbijaju. Poziva se pri startu aplikacije.
     */
    public void expireOverdueReservations() {
        reservationRepository.findByStatus(ReservationStatus.PENDING).stream()
                .filter(r -> r.getStartDate().isBefore(LocalDate.now()))
                .forEach(r -> {
                    r.setStatus(ReservationStatus.REJECTED);
                    reservationRepository.save(r);
                });
    }
}
