package util;

import manager.UserManager;
import repository.UserRepository;
import service.AuthService;

public class AppContext {

    private static AppContext instance;

    private final UserRepository userRepository;
    private final UserManager userManager;
    private final AuthService authService;

    private AppContext() {
        userRepository = new UserRepository();
        userManager = new UserManager(userRepository);
        authService = new AuthService(userManager);
    }

    public static synchronized AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }

    public UserManager getUserManager() { return userManager; }
    public AuthService getAuthService() { return authService; }
}
