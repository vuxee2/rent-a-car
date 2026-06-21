package gui.forms;

import manager.UserManager;
import model.Client;
import model.enums.ClientCategory;
import model.enums.Gender;
import util.AppContext;
import util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.UUID;

public class RegisterClientForm extends JPanel {

    private final UserManager userManager;

    private JTextField firstNameField;
    private JTextField lastNameField;
    private JComboBox<Gender> genderCombo;
    private JTextField birthDateField;
    private JTextField phoneField;
    private JTextField addressField;
    private JTextField emailField; // postaje username
    private JPasswordField passwordField;
    private JTextField licenseDateField;
    private JComboBox<String> categoryCombo;

    public RegisterClientForm() {
        this.userManager = AppContext.getInstance().getUserManager();

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(buildForm(), BorderLayout.NORTH);
    }

    private JPanel buildForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Registracija novog klijenta"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        firstNameField = new JTextField(18);
        lastNameField = new JTextField(18);
        genderCombo = new JComboBox<>(Gender.values());
        birthDateField = new JTextField("2000-01-01", 18);
        phoneField = new JTextField(18);
        addressField = new JTextField(18);
        emailField = new JTextField(18);
        passwordField = new JPasswordField(18);
        licenseDateField = new JTextField(LocalDate.now().minusYears(3).toString(), 18);

        categoryCombo = new JComboBox<>(new String[]{"BEZ KATEGORIJE", "STUDENT", "PENSIONER", "COMPANY"});

        int row = 0;
        addRow(panel, gbc, row++, "Ime:", firstNameField);
        addRow(panel, gbc, row++, "Prezime:", lastNameField);
        addRow(panel, gbc, row++, "Pol:", genderCombo);
        addRow(panel, gbc, row++, "Datum rođenja (yyyy-MM-dd):", birthDateField);
        addRow(panel, gbc, row++, "Telefon:", phoneField);
        addRow(panel, gbc, row++, "Adresa:", addressField);
        addRow(panel, gbc, row++, "E-mail (postaje korisničko ime):", emailField);
        addRow(panel, gbc, row++, "Lozinka:", passwordField);
        addRow(panel, gbc, row++, "Datum izdavanja vozačke dozvole:", licenseDateField);
        addRow(panel, gbc, row++, "Kategorija:", categoryCombo);

        JButton registerButton = new JButton("Registruj klijenta");
        registerButton.addActionListener(e -> handleRegister());

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(registerButton, gbc);

        return panel;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(field, gbc);
    }

    private void handleRegister() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Popunite sva obavezna polja.", "Nepotpuni podaci", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate birthDate;
        LocalDate licenseDate;
        try {
            birthDate = DateUtil.parse(birthDateField.getText().trim());
            licenseDate = DateUtil.parse(licenseDateField.getText().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Datumi moraju biti u formatu yyyy-MM-dd.", "Greška", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String categorySelection = (String) categoryCombo.getSelectedItem();
        ClientCategory category = "BEZ KATEGORIJE".equals(categorySelection) ? null : ClientCategory.valueOf(categorySelection);

        Client client = new Client(
                UUID.randomUUID().toString(),
                firstName,
                lastName,
                (Gender) genderCombo.getSelectedItem(),
                birthDate,
                phoneField.getText().trim(),
                addressField.getText().trim(),
                email,
                password,
                licenseDate,
                category
        );

        try {
            userManager.registerClient(client);
            JOptionPane.showMessageDialog(this,
                    "Klijent je uspešno registrovan.\nKorisničko ime: " + email,
                    "Uspešno", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Greška", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        firstNameField.setText("");
        lastNameField.setText("");
        phoneField.setText("");
        addressField.setText("");
        emailField.setText("");
        passwordField.setText("");
        categoryCombo.setSelectedIndex(0);
    }
}
