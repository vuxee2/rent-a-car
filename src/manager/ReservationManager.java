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
    private final SubscriptionManager subscriptionManager;

    public ReservationManager(ReservationRepository reservationRepository,
                                VehicleManager vehicleManager,
                                SubscriptionManager subscriptionManager) {
        this.reservationRepository = reservationRepository;
        this.vehicleManager = vehicleManager;
        this.subscriptionManager = subscriptionManager;
    }

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

        if (!subscriptionManager.hasActiveSubscription(client.getId()))
        {
            throw new IllegalStateException("Niste pretplaćeni na servis.");
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

    /** Da li postoji bar jedan dostupan model. */
    public boolean isModelAvailableInPeriod(String modelId, LocalDate start, LocalDate end) {
        List<Vehicle> vehiclesOfModel = vehicleManager.getVehiclesForModel(modelId);
        if (vehiclesOfModel.isEmpty()) return false;

        List<Reservation> activeReservations = reservationRepository.findAll().stream()
                .filter(r -> r.getVehicleModelId().equals(modelId))
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
                .filter(r -> DateUtil.isOverlapping(r.getStartDate(), r.getEndDate(), start, end))
                .collect(Collectors.toList());

        // Ako je broj fizickih vozila veci od broja preklapajucih rezervacija, ima slobodnog mesta
        return vehiclesOfModel.size() > activeReservations.size();
    }

    /** Klijent koji je otkazao rezervaciju (ili nije preuzeo vozilo) ne sme da rezervise u naredna 24h. */
    public boolean canClientReserveNow(String clientId) {
        return reservationRepository.findByClientId(clientId).stream()
                .filter(r -> r.getStatus() == ReservationStatus.CANCELLED)
                .filter(r -> r.getCancelledAt() != null)
                .noneMatch(r -> ChronoUnit.HOURS.between(r.getCancelledAt(), LocalDateTime.now()) < 24);
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
            r.setCancelledAt(LocalDateTime.now());
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
