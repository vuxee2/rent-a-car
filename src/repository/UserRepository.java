package repository;

import model.*;
import model.enums.*;
import util.CSVParser;
import util.DateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository za sve tipove korisnika (Administrator, Agent, Client).
 * Format reda: id;firstName;lastName;gender;birthDate;phone;address;username;
 *              password;role;educationLevel;yearsOfService;baseSalary;
 *              coefficient;licenseIssueDate;clientCategory
 */
public class UserRepository {

    private static final String FILE_PATH = "data/users.csv";
    private static final String DELIMITER = ";";

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        for (String[] row : CSVParser.read(FILE_PATH, DELIMITER)) {
            User user = mapRowToUser(row);
            if (user != null) users.add(user);
        }
        return users;
    }

    public Optional<User> findById(String id) {
        return findAll().stream().filter(u -> u.getId().equals(id)).findFirst();
    }

    public Optional<User> findByUsername(String username) {
        return findAll().stream().filter(u -> u.getUsername().equals(username)).findFirst();
    }

    public void save(User user) {
        List<User> all = findAll();
        boolean exists = all.stream().anyMatch(u -> u.getId().equals(user.getId()));

        if (exists) {
            List<String[]> rows = all.stream()
                    .map(u -> u.getId().equals(user.getId()) ? mapUserToRow(user) : mapUserToRow(u))
                    .collect(Collectors.toList());
            CSVParser.writeAll(FILE_PATH, rows, DELIMITER);
        } else {
            CSVParser.append(FILE_PATH, mapUserToRow(user), DELIMITER);
        }
    }

    public void delete(String id) {
        List<String[]> rows = findAll().stream()
                .filter(u -> !u.getId().equals(id))
                .map(this::mapUserToRow)
                .collect(Collectors.toList());
        CSVParser.writeAll(FILE_PATH, rows, DELIMITER);
    }

    // ---------------- mapiranje ----------------

    private User mapRowToUser(String[] row) {
        try {
            UserRole role = UserRole.valueOf(row[9]);
            switch (role) {
                case ADMINISTRATOR -> {
                    Administrator admin = new Administrator();
                    fillBase(admin, row);
                    fillEmployee(admin, row);
                    return admin;
                }
                case AGENT -> {
                    Agent agent = new Agent();
                    fillBase(agent, row);
                    fillEmployee(agent, row);
                    return agent;
                }
                case CLIENT -> {
                    Client client = new Client();
                    fillBase(client, row);
                    client.setLicenseIssueDate(DateUtil.parse(row[14]));
                    if (row.length > 15 && !row[15].isBlank()) {
                        client.setCategory(ClientCategory.valueOf(row[15]));
                    }
                    return client;
                }
                default -> {
                    return null;
                }
            }
        } catch (Exception e) {
            System.err.println("Greska pri parsiranju korisnika: " + String.join(DELIMITER, row));
            return null;
        }
    }

    private void fillBase(User user, String[] row) {
        user.setId(row[0]);
        user.setFirstName(row[1]);
        user.setLastName(row[2]);
        user.setGender(Gender.valueOf(row[3]));
        user.setBirthDate(DateUtil.parse(row[4]));
        user.setPhone(row[5]);
        user.setAddress(row[6]);
        user.setUsername(row[7]);
        user.setPassword(row[8]);
        user.setRole(UserRole.valueOf(row[9]));
    }

    private void fillEmployee(Employee emp, String[] row) {
        emp.setEducationLevel(row[10]);
        emp.setYearsOfService(Integer.parseInt(row[11]));
        emp.setBaseSalary(Double.parseDouble(row[12]));
        emp.setCoefficient(Double.parseDouble(row[13]));
    }

    private String[] mapUserToRow(User user) {
        String educationLevel = "", yearsOfService = "", baseSalary = "", coefficient = "";
        String licenseIssueDate = "", clientCategory = "";

        if (user instanceof Employee emp) {
            educationLevel = emp.getEducationLevel();
            yearsOfService = String.valueOf(emp.getYearsOfService());
            baseSalary = String.valueOf(emp.getBaseSalary());
            coefficient = String.valueOf(emp.getCoefficient());
        }
        if (user instanceof Client client) {
            licenseIssueDate = DateUtil.format(client.getLicenseIssueDate());
            clientCategory = client.getCategory() != null ? client.getCategory().name() : "";
        }

        return new String[]{
                user.getId(), user.getFirstName(), user.getLastName(),
                user.getGender().name(), DateUtil.format(user.getBirthDate()),
                user.getPhone(), user.getAddress(), user.getUsername(), user.getPassword(),
                user.getRole().name(), educationLevel, yearsOfService, baseSalary, coefficient,
                licenseIssueDate, clientCategory
        };
    }
}
