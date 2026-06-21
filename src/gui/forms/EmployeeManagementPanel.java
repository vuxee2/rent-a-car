package gui.forms;

import manager.UserManager;
import model.Administrator;
import model.Agent;
import model.Employee;
import model.User;
import model.enums.Gender;
import model.enums.UserRole;
import util.AppContext;
import util.DateUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Admin upravlja zaposlenima (Agent, Administrator) — pregled, dodavanje,
 * izmena, brisanje. Plata se prikazuje izracunata po formuli
 * osnova * (koeficijent + 0.004 * godine_staza).
 */
public class EmployeeManagementPanel extends JPanel {

    private final UserManager userManager;
    private DefaultTableModel tableModel;
    private JTable table;
    private List<User> currentEmployees;

    public EmployeeManagementPanel() {
        this.userManager = AppContext.getInstance().getUserManager();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(buildTable(), BorderLayout.CENTER);
        refresh();
    }

    private JPanel buildTable() {
        tableModel = new DefaultTableModel(
                new Object[]{"Ime i prezime", "Uloga", "Korisničko ime", "Stručna sprema", "Staž", "Plata"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton addButton = new JButton("Dodaj zaposlenog");
        addButton.addActionListener(e -> openEmployeeDialog(null));

        JButton editButton = new JButton("Izmeni");
        editButton.addActionListener(e -> {
            User selected = getSelected();
            if (selected != null) openEmployeeDialog((Employee) selected);
        });

        JButton deleteButton = new JButton("Obriši");
        deleteButton.setForeground(Color.RED);
        deleteButton.addActionListener(e -> handleDelete());

        JButton refreshButton = new JButton("Osveži");
        refreshButton.addActionListener(e -> refresh());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(refreshButton);
        buttons.add(addButton);
        buttons.add(editButton);
        buttons.add(deleteButton);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(new JScrollPane(table), BorderLayout.CENTER);
        wrapper.add(buttons, BorderLayout.SOUTH);

        return wrapper;
    }

    private void refresh() {
        currentEmployees = userManager.getAllUsers().stream()
                .filter(u -> u instanceof Employee)
                .toList();

        tableModel.setRowCount(0);
        for (User u : currentEmployees) {
            Employee emp = (Employee) u;
            tableModel.addRow(new Object[]{
                    emp.getFullName(),
                    emp.getRole(),
                    emp.getUsername(),
                    emp.getEducationLevel(),
                    emp.getYearsOfService(),
                    String.format("%.2f RSD", emp.calculateSalary())
            });
        }
    }

    private User getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Izaberite zaposlenog iz tabele.", "Napomena", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return currentEmployees.get(row);
    }

    private void handleDelete() {
        User selected = getSelected();
        if (selected == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Da li ste sigurni da želite da obrišete zaposlenog " + selected.getFullName() + "?",
                "Potvrda brisanja", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            userManager.deleteUser(selected.getId());
            refresh();
        }
    }

    /**
     * Otvara dijalog za dodavanje (employee == null) ili izmenu postojeceg
     * zaposlenog.
     */
    private void openEmployeeDialog(Employee existing) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                existing == null ? "Novi zaposleni" : "Izmena zaposlenog", true);
        dialog.setSize(420, 480);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField firstNameField = new JTextField(existing != null ? existing.getFirstName() : "", 16);
        JTextField lastNameField = new JTextField(existing != null ? existing.getLastName() : "", 16);
        JComboBox<Gender> genderCombo = new JComboBox<>(Gender.values());
        if (existing != null) genderCombo.setSelectedItem(existing.getGender());
        JTextField birthDateField = new JTextField(existing != null ? DateUtil.format(existing.getBirthDate()) : "1990-01-01", 16);
        JTextField phoneField = new JTextField(existing != null ? existing.getPhone() : "", 16);
        JTextField addressField = new JTextField(existing != null ? existing.getAddress() : "", 16);
        JTextField usernameField = new JTextField(existing != null ? existing.getUsername() : "", 16);
        JPasswordField passwordField = new JPasswordField(existing != null ? existing.getPassword() : "", 16);

        JComboBox<UserRole> roleCombo = new JComboBox<>(new UserRole[]{UserRole.AGENT, UserRole.ADMINISTRATOR});
        if (existing != null) roleCombo.setSelectedItem(existing.getRole());

        JComboBox<String> eduCombo = new JComboBox<>(new String[]{"HIGH_SCHOOL", "BACHELOR", "MASTER", "PHD"});
        if (existing != null) eduCombo.setSelectedItem(existing.getEducationLevel());

        JTextField yearsField = new JTextField(existing != null ? String.valueOf(existing.getYearsOfService()) : "0", 16);
        JTextField baseSalaryField = new JTextField(existing != null ? String.valueOf(existing.getBaseSalary()) : "60000", 16);
        JTextField coefficientField = new JTextField(existing != null ? String.valueOf(existing.getCoefficient()) : "1.0", 16);

        int row = 0;
        addFormRow(form, gbc, row++, "Ime:", firstNameField);
        addFormRow(form, gbc, row++, "Prezime:", lastNameField);
        addFormRow(form, gbc, row++, "Pol:", genderCombo);
        addFormRow(form, gbc, row++, "Datum rođenja:", birthDateField);
        addFormRow(form, gbc, row++, "Telefon:", phoneField);
        addFormRow(form, gbc, row++, "Adresa:", addressField);
        addFormRow(form, gbc, row++, "Korisničko ime:", usernameField);
        addFormRow(form, gbc, row++, "Lozinka:", passwordField);
        addFormRow(form, gbc, row++, "Uloga:", roleCombo);
        addFormRow(form, gbc, row++, "Stručna sprema:", eduCombo);
        addFormRow(form, gbc, row++, "Godine staža:", yearsField);
        addFormRow(form, gbc, row++, "Osnova plate:", baseSalaryField);
        addFormRow(form, gbc, row++, "Koeficijent:", coefficientField);

        JButton saveButton = new JButton(existing == null ? "Dodaj" : "Sačuvaj izmene");
        saveButton.addActionListener(e -> {
            try {
                Gender gender = (Gender) genderCombo.getSelectedItem();
                LocalDate birthDate = DateUtil.parse(birthDateField.getText().trim());
                UserRole role = (UserRole) roleCombo.getSelectedItem();
                String education = (String) eduCombo.getSelectedItem();
                int years = Integer.parseInt(yearsField.getText().trim());
                double baseSalary = Double.parseDouble(baseSalaryField.getText().trim());
                double coefficient = Double.parseDouble(coefficientField.getText().trim());
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword());

                Employee employee = (role == UserRole.ADMINISTRATOR)
                        ? new Administrator()
                        : new Agent();

                employee.setId(existing != null ? existing.getId() : UUID.randomUUID().toString());
                employee.setFirstName(firstNameField.getText().trim());
                employee.setLastName(lastNameField.getText().trim());
                employee.setGender(gender);
                employee.setBirthDate(birthDate);
                employee.setPhone(phoneField.getText().trim());
                employee.setAddress(addressField.getText().trim());
                employee.setUsername(username);
                employee.setPassword(password);
                employee.setEducationLevel(education);
                employee.setYearsOfService(years);
                employee.setBaseSalary(baseSalary);
                employee.setCoefficient(coefficient);

                if (existing == null) {
                    userManager.registerEmployee(employee);
                } else {
                    userManager.updateUser(employee);
                }

                dialog.dispose();
                refresh();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Proverite da su staž, osnova i koeficijent ispravni brojevi.", "Greška", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Greška", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        form.add(saveButton, gbc);

        dialog.setContentPane(new JScrollPane(form));
        dialog.setVisible(true);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(field, gbc);
    }
}
