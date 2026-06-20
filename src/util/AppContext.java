package util;

import manager.ReservationManager;
import manager.SubscriptionManager;
import manager.UserManager;
import manager.VehicleManager;
import repository.AdditionalServiceRepository;
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

    // --- Managers ---
    private final UserManager userManager;
    private final VehicleManager vehicleManager;
    private final ReservationManager reservationManager;
    private final SubscriptionManager subscriptionManager;

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

        userManager = new UserManager(userRepository);
        vehicleManager = new VehicleManager(vehicleRepository, vehicleModelRepository, vehicleCategoryRepository);
        reservationManager = new ReservationManager(reservationRepository, vehicleManager);
        subscriptionManager = new SubscriptionManager(subscriptionRepository);

        authService = new AuthService(userManager);

        // Automatsko odbijanje isteklih rezervacija pri pokretanju aplikacije
        reservationManager.expireOverdueReservations();
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
    public AuthService getAuthService() { return authService; }
    public AdditionalServiceRepository getAdditionalServiceRepository() { return additionalServiceRepository; }
}
