package service;

import manager.UserManager;
import model.User;

// Servis zaduzen za prijavu/odjavu i cuvanje trenutno ulogovanog korisnika.
public class AuthService {

    private final UserManager userManager;
    private User currentUser;

    public AuthService(UserManager userManager) {
        this.userManager = userManager;
    }

    // Pokusava prijavu. Vraca User ako su podaci ispravni, inace null.
    public User login(String username, String password) {
        User user = userManager.findByUsernameAndPassword(username, password);
        if (user != null) {
            this.currentUser = user;
        }
        return user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
