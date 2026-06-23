package util;

import manager.PricelistManager;
import manager.RentalManager;
import manager.ReportManager;
import manager.ReservationManager;
import manager.SubscriptionManager;
import manager.UserManager;
import manager.VehicleManager;
import repository.AdditionalServiceRepository;
import repository.PricelistRepository;
import repository.RentalRepository;
import repository.ReservationRepository;
import repository.SubscriptionRepository;
import repository.UserRepository;
import repository.VehicleCategoryRepository;
import repository.VehicleModelRepository;
import repository.VehicleRepository;
import service.AuthService;

public class AppContext {

    private static AppContext instance;

    // --- Repositories ---
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleModelRepository vehicleModelRepository;
    private final VehicleCategoryRepository vehicleCategoryRepository;
    private final ReservationRepository reservationRepository;
    private final AdditionalServiceRepository additionalServiceRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final RentalRepository rentalRepository;
    private final PricelistRepository pricelistRepository;

    // --- Managers ---
    private final UserManager userManager;
    private final VehicleManager vehicleManager;
    private final ReservationManager reservationManager;
    private final SubscriptionManager subscriptionManager;
    private final RentalManager rentalManager;
    private final PricelistManager pricelistManager;
    private final ReportManager reportManager;

    // --- Services ---
    private final AuthService authService;

    private AppContext() {
        userRepository = new UserRepository();
        vehicleRepository = new VehicleRepository();
        vehicleModelRepository = new VehicleModelRepository();
        vehicleCategoryRepository = new VehicleCategoryRepository();
        reservationRepository = new ReservationRepository();
        additionalServiceRepository = new AdditionalServiceRepository();
        subscriptionRepository = new SubscriptionRepository();
        rentalRepository = new RentalRepository();
        pricelistRepository = new PricelistRepository();

        userManager = new UserManager(userRepository);
        vehicleManager = new VehicleManager(vehicleRepository, vehicleModelRepository, vehicleCategoryRepository);
        pricelistManager = new PricelistManager(pricelistRepository);
        subscriptionManager = new SubscriptionManager(subscriptionRepository, pricelistManager);
        reservationManager = new ReservationManager(reservationRepository, vehicleManager, subscriptionManager);
        rentalManager = new RentalManager(rentalRepository, reservationRepository, vehicleManager, pricelistRepository);
        reportManager = new ReportManager(reservationRepository, rentalRepository, userRepository, vehicleModelRepository, subscriptionRepository);

        authService = new AuthService(userManager);

        // Automatsko odbijanje isteklih i otkazivanje nepreuzetih rezervacija pri pokretanju
        reservationManager.expireOverdueReservations();
        rentalManager.expireNoShowReservations();
    }

    // Samo jedan thread moze da executuje
    public static synchronized AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }

    public UserManager getUserManager() { return userManager; }
    public VehicleManager getVehicleManager() { return vehicleManager; }
    public ReservationManager getReservationManager() { return reservationManager; }
    public SubscriptionManager getSubscriptionManager() { return subscriptionManager; }
    public RentalManager getRentalManager() { return rentalManager; }
    public PricelistManager getPricelistManager() { return pricelistManager; }
    public ReportManager getReportManager() { return reportManager; }
    public AuthService getAuthService() { return authService; }
    public AdditionalServiceRepository getAdditionalServiceRepository() { return additionalServiceRepository; }
    public ReservationRepository getReservationRepository() { return reservationRepository; }
}
