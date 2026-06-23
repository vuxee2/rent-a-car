package gui.forms;

// import manager.ReservationManager;
import manager.RentalManager;
import manager.UserManager;
import manager.VehicleManager;
import model.AdditionalService;
import model.Client;
import model.Reservation;
import model.User;
import model.Vehicle;
import model.VehicleCategory;
import model.VehicleModel;
import model.enums.ChargeType;
import model.enums.ReservationStatus;
import util.AppContext;
import util.DateUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class IssueVehicleForm extends JPanel {

    private final User agent;
    private final RentalManager rentalManager;
    private final VehicleManager vehicleManager;
    private final UserManager userManager;
    private final List<AdditionalService> allServices;

    private DefaultTableModel reservationTableModel;
    private JTable reservationTable;
    private List<Reservation> confirmedReservations;

    private DefaultTableModel vehicleTableModel;
    private JTable vehicleTable;
    private List<Vehicle> availableVehicles;

    private JTextField mileageField;
    private List<JCheckBox> serviceCheckboxes;

    private JLabel finalPriceLabel;
    private double currentDailyPrice;   // dnevna cena auta za izabranu rezervaciju
    private double currentDiscount;     // popust klijenta
    private double currentBaseTotal;    // vec obracunata cena rezervacije

    public IssueVehicleForm(User agent) {
        this.agent = agent;
        this.rentalManager = AppContext.getInstance().getRentalManager();
        this.vehicleManager = AppContext.getInstance().getVehicleManager();
        this.userManager = AppContext.getInstance().getUserManager();
        this.allServices = AppContext.getInstance().getAdditionalServiceRepository().findAll();

        setLayout(new GridLayout(1, 2, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(buildReservationsPanel());
        add(buildIssuePanel());

        refreshReservations();
    }

    private JPanel buildReservationsPanel() {
        reservationTableModel = new DefaultTableModel(
                new Object[]{"Klijent", "Model", "Od", "Do"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        reservationTable = new JTable(reservationTableModel);
        reservationTable.setRowHeight(26);
        reservationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reservationTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadVehiclesForSelectedReservation();
        });

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Potvrđene rezervacije (spremne za izdavanje)"));
        panel.add(new JScrollPane(reservationTable), BorderLayout.CENTER);

        JButton refreshButton = new JButton("Osveži");
        refreshButton.addActionListener(e -> refreshReservations());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(refreshButton);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildIssuePanel() {
        vehicleTableModel = new DefaultTableModel(new Object[]{"Registracija", "Kilometraža"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        vehicleTable = new JTable(vehicleTableModel);
        vehicleTable.setRowHeight(26);
        vehicleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        vehicleTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadVehicleMileage();
        });

        mileageField = new JTextField(10);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Kilometraža pri preuzimanju:"), gbc);
        gbc.gridx = 1;
        formPanel.add(mileageField, gbc);

        JButton issueButton = new JButton("Izdaj izabrano vozilo");
        issueButton.setForeground(new Color(0, 110, 0));
        issueButton.addActionListener(e -> handleIssue());

        finalPriceLabel = new JLabel("Konačna cena: -");
        finalPriceLabel.setFont(finalPriceLabel.getFont().deriveFont(Font.BOLD, 14f));

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Slobodni primerci rezervisanog modela"));
        panel.add(new JScrollPane(vehicleTable), BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout());
        south.add(buildServicesPanel(), BorderLayout.NORTH);
        south.add(formPanel, BorderLayout.CENTER);

        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.add(finalPriceLabel, BorderLayout.WEST);
        JPanel buttonWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonWrap.add(issueButton);
        bottomRow.add(buttonWrap, BorderLayout.EAST);
        south.add(bottomRow, BorderLayout.SOUTH);

        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    /** Dodatne usluge */
    private JPanel buildServicesPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 3));
        panel.setBorder(BorderFactory.createTitledBorder("Dodatne usluge (već uzete su zaključane)"));

        serviceCheckboxes = new ArrayList<>();
        for (AdditionalService service : allServices) {
            String priceLabel = service.getChargeType() == ChargeType.PER_DAY
                    ? String.format("%.2f RSD/dan", service.getPrice())
                    : String.format("%.2f RSD jednokratno", service.getPrice());

            JCheckBox checkBox = new JCheckBox(service.getDisplayName() + " - " + priceLabel);
            checkBox.putClientProperty("service", service);
            checkBox.putClientProperty("locked", Boolean.FALSE);

            if (service.getChargeType() == ChargeType.PER_DAY) {
                JSpinner daysSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 365, 1));
                daysSpinner.setEnabled(false);
                daysSpinner.addChangeListener(e -> updateFinalPrice());
                checkBox.putClientProperty("daysSpinner", daysSpinner);
                checkBox.addActionListener(e -> {
                    boolean locked = Boolean.TRUE.equals(checkBox.getClientProperty("locked"));
                    daysSpinner.setEnabled(checkBox.isSelected() && !locked);
                    updateFinalPrice();
                });
                JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                wrapper.add(checkBox);
                wrapper.add(daysSpinner);
                panel.add(wrapper);
            } else {
                checkBox.addActionListener(e -> updateFinalPrice());
                panel.add(checkBox);
            }
            serviceCheckboxes.add(checkBox);
        }
        return panel;
    }

    /** Cekira i zakljucava usluge koje rezervacija vec sadrzi. */
    private void applyReservationServices(Reservation reservation) {
        Set<String> existing = new HashSet<>();
        String ids = reservation.getAdditionalServiceIds();
        if (ids != null && !ids.isBlank()) {
            existing.addAll(Arrays.asList(ids.split(",")));
        }

        for (JCheckBox cb : serviceCheckboxes) {
            AdditionalService service = (AdditionalService) cb.getClientProperty("service");
            boolean alreadyTaken = existing.contains(service.getId());

            cb.setSelected(alreadyTaken);
            cb.setEnabled(!alreadyTaken); 
            cb.putClientProperty("locked", alreadyTaken);

            JSpinner spinner = (JSpinner) cb.getClientProperty("daysSpinner");
            if (spinner != null) {
                spinner.setEnabled(false);
            }
        }
    }

    private void resetServices() {
        for (JCheckBox cb : serviceCheckboxes) {
            cb.setSelected(false);
            cb.setEnabled(true);
            cb.putClientProperty("locked", Boolean.FALSE);
            JSpinner spinner = (JSpinner) cb.getClientProperty("daysSpinner");
            if (spinner != null) {
                spinner.setValue(1);
                spinner.setEnabled(false);
            }
        }
        if (finalPriceLabel != null) updateFinalPrice();
    }

    // Cena samo NOVODODATIH usluga
    private double computeAddedServicesCost() {
        double cost = 0;
        for (JCheckBox cb : serviceCheckboxes) {
            if (!cb.isSelected() || Boolean.TRUE.equals(cb.getClientProperty("locked"))) continue;
            AdditionalService s = (AdditionalService) cb.getClientProperty("service");
            if (s.getChargeType() == ChargeType.PER_DAY) {
                JSpinner spinner = (JSpinner) cb.getClientProperty("daysSpinner");
                int days = spinner != null ? (int) spinner.getValue() : 1;
                cost += s.getPrice() * days;
            } else {
                cost += s.getPrice();
            }
        }
        return cost;
    }

    private String selectedServiceIds() {
        return serviceCheckboxes.stream()
                .filter(JCheckBox::isSelected)
                .filter(cb -> !Boolean.TRUE.equals(cb.getClientProperty("locked")))
                .map(cb -> ((AdditionalService) cb.getClientProperty("service")).getId())
                .collect(Collectors.joining(","));
    }

    // Dodatni dani = najveci broj dana medju novododatim uslugama po danu, produzava rezervaciju
    private int computeAdditionalDays() {
        int days = 0;
        for (JCheckBox cb : serviceCheckboxes) {
            if (!cb.isSelected() || Boolean.TRUE.equals(cb.getClientProperty("locked"))) continue;
            AdditionalService s = (AdditionalService) cb.getClientProperty("service");
            if (s.getChargeType() == ChargeType.PER_DAY) {
                JSpinner spinner = (JSpinner) cb.getClientProperty("daysSpinner");
                int d = spinner != null ? (int) spinner.getValue() : 0;
                days = Math.max(days, d);
            }
        }
        return days;
    }

    // Iznos koji se DODAJE na cenu rezervacije
    private double computeAddedCharge() {
        double carExtra = currentDailyPrice * computeAdditionalDays();
        double servicesExtra = computeAddedServicesCost();
        double subtotal = carExtra + servicesExtra;
        if (currentDiscount != 0) {
            subtotal -= currentDiscount / 100 * subtotal;
        }
        return subtotal;
    }

    private void updateFinalPrice() {
        if (reservationTable.getSelectedRow() < 0) {
            finalPriceLabel.setText("Konačna cena: -");
            return;
        }
        double added = computeAddedCharge();
        double total = currentBaseTotal + added;
        finalPriceLabel.setText(String.format("Konačna cena: %.2f RSD", total));
    }

    private double discountFor(String clientId) {
        User u = userManager.getById(clientId).orElse(null);
        if (!(u instanceof Client) || ((Client) u).getCategory() == null) return 0;
        return AppContext.getInstance().getPricelistManager().getActivePricelist()
                .map(pl -> {
                    switch (((Client) u).getCategory()) {
                        case STUDENT:   return pl.getStudentDiscountPercent();
                        case PENSIONER: return pl.getPensionerDiscountPercent();
                        case COMPANY:   return pl.getCompanyDiscountPercent();
                        default:        return 0.0;
                    }
                })
                .orElse(0.0);
    }

    private void loadVehicleMileage()
    {
        int row = vehicleTable.getSelectedRow();
        if(row < 0) return;
        Vehicle selectedVehicle = availableVehicles.get(row);
        mileageField.setText(Integer.toString(selectedVehicle.getMileage()));
    }

    private void refreshReservations() {
        confirmedReservations = AppContext.getInstance().getReservationRepository().findAll().stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
                .filter(r -> rentalManager.getRentalByReservationId(r.getId()).isEmpty()) // jos nije izdato
                .toList();

        reservationTableModel.setRowCount(0);
        for (Reservation r : confirmedReservations) {
            VehicleModel model = vehicleManager.getModelById(r.getVehicleModelId()).orElse(null);
            reservationTableModel.addRow(new Object[]{
                    userManager.getById(r.getClientId()).get().getFullName(),
                    model != null ? model.getFullName() : r.getVehicleModelId(),
                    DateUtil.format(r.getStartDate()),
                    DateUtil.format(r.getEndDate())
            });
        }
        vehicleTableModel.setRowCount(0);
        availableVehicles = List.of();
    }

    private void loadVehiclesForSelectedReservation() {
        int row = reservationTable.getSelectedRow();
        vehicleTableModel.setRowCount(0);
        if (row < 0) {
            availableVehicles = List.of();
            resetServices();
            return;
        }

        Reservation reservation = confirmedReservations.get(row);
        availableVehicles = vehicleManager.getAvailableVehiclesForModel(reservation.getVehicleModelId());

        for (Vehicle v : availableVehicles) {
            vehicleTableModel.addRow(new Object[]{ v.getLicensePlate(), v.getMileage() });
        }

        currentBaseTotal = reservation.getTotalPrice();
        currentDiscount = discountFor(reservation.getClientId());
        currentDailyPrice = vehicleManager.getModelById(reservation.getVehicleModelId())
                .flatMap(m -> vehicleManager.getCategoryById(m.getCategoryId()))
                .map(VehicleCategory::getDailyPrice)
                .orElse(0.0);

        applyReservationServices(reservation);
        updateFinalPrice();
    }

    private void handleIssue() {
        int reservationRow = reservationTable.getSelectedRow();
        int vehicleRow = vehicleTable.getSelectedRow();

        if (reservationRow < 0) {
            JOptionPane.showMessageDialog(this, "Izaberite rezervaciju.", "Napomena", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (vehicleRow < 0) {
            JOptionPane.showMessageDialog(this, "Izaberite slobodan primerak vozila.", "Napomena", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int mileage;
        try {
            mileage = Integer.parseInt(mileageField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Kilometraža mora biti ceo broj.", "Greška", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Reservation reservation = confirmedReservations.get(reservationRow);
        Vehicle vehicle = availableVehicles.get(vehicleRow);

        String addedServiceIds = selectedServiceIds();
        int additionalDays = computeAdditionalDays();
        double addedCharge = computeAddedCharge();   // cena auta za produžene dane + nove usluge (sa popustom)

        try {
            rentalManager.issueVehicle(reservation.getId(), vehicle.getId(), agent.getId(), mileage,
                    addedServiceIds, addedCharge, additionalDays);
            vehicleManager.setVehicleMileage(vehicle.getId(), mileage);

            StringBuilder msg = new StringBuilder("Vozilo je uspešno izdato.");
            double finalTotal = currentBaseTotal + addedCharge;
            msg.append(String.format("%nKonačna cena: %.2f RSD", finalTotal));
            if (additionalDays > 0) {
                msg.append(String.format("%nRezervacija produžena za %d dan(a) - novi povratak: %s",
                        additionalDays, DateUtil.format(reservation.getEndDate().plusDays(additionalDays))));
            }
            JOptionPane.showMessageDialog(this, msg.toString(), "Uspešno", JOptionPane.INFORMATION_MESSAGE);

            mileageField.setText("");
            resetServices();
            refreshReservations();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Izdavanje nije moguće", JOptionPane.ERROR_MESSAGE);
        }
    }
}
