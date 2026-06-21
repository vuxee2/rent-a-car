package manager;

import model.*;
import repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserManager {

    private final UserRepository userRepository;

    public UserManager(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findByUsernameAndPassword(String username, String password) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent() && user.get().getPassword().equals(password)) {
            return user.get();
        }
        return null;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<Agent> getAllAgents() {
        return userRepository.findAll().stream()
                .filter(u -> u instanceof Agent)
                .map(u -> (Agent) u)
                .collect(Collectors.toList());
    }

    public List<Administrator> getAllAdministrators() {
        return userRepository.findAll().stream()
                .filter(u -> u instanceof Administrator)
                .map(u -> (Administrator) u)
                .collect(Collectors.toList());
    }

    public List<Client> getAllClients() {
        return userRepository.findAll().stream()
                .filter(u -> u instanceof Client)
                .map(u -> (Client) u)
                .collect(Collectors.toList());
    }

    public Optional<User> getById(String id) {
        return userRepository.findById(id);
    }

    public boolean usernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }


    public void registerEmployee(Employee employee) {
        if (usernameExists(employee.getUsername())) {
            throw new IllegalStateException("Korisničko ime već postoji.");
        }
        if (employee.getId() == null || employee.getId().isBlank()) {
            employee.setId(UUID.randomUUID().toString());
        }
        userRepository.save(employee);
    }


    public void registerClient(Client client) {
        if (usernameExists(client.getUsername())) {
            throw new IllegalStateException("Klijent sa tim e-mailom već postoji.");
        }
        if (client.getId() == null || client.getId().isBlank()) {
            client.setId(UUID.randomUUID().toString());
        }
        userRepository.save(client);
    }

    public void updateUser(User user) {
        userRepository.save(user);
    }

    public void deleteUser(String id) {
        userRepository.delete(id);
    }
}
