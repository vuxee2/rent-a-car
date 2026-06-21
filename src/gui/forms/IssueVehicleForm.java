package gui.forms;

// import manager.ReservationManager;
import manager.RentalManager;
import manager.VehicleManager;
import model.Reservation;
import model.User;
import model.Vehicle;
import model.VehicleModel;
import model.enums.ReservationStatus;
import util.AppContext;
import util.DateUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class IssueVehicleForm extends JPanel {

    private final User agent;
    // private final ReservationManager reservationManager;
    private final RentalManager rentalManager;
    private final VehicleManager vehicleManager;

    private DefaultTableModel reservationTableModel;
    private JTable reservationTable;
    private List<Reservation> confirmedReservations;

    private DefaultTableModel vehicleTableModel;
    private JTable vehicleTable;
    private List<Vehicle> availableVehicles;

    private JTextField mileageField;

    public IssueVehicleForm(User agent) {
        this.agent = agent;
        // this.reservationManager = AppContext.getInstance().getReservationManager();
        this.rentalManager = AppContext.getInstance().getRentalManager();
        this.vehicleManager = AppContext.getInstance().getVehicleManager();

        setLayout(new GridLayout(1, 2, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(buildReservationsPanel());
        add(buildIssuePanel());

        refreshReservations();
    }

    private JPanel buildReservationsPanel() {
        reservationTableModel = new DefaultTableModel(
                new Object[]{"Klijent ID", "Model", "Od", "Do"}, 0) {
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

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Slobodni primerci rezervisanog modela"));
        panel.add(new JScrollPane(vehicleTable), BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout());
        south.add(formPanel, BorderLayout.CENTER);
        JPanel buttonWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonWrap.add(issueButton);
        south.add(buttonWrap, BorderLayout.SOUTH);

        panel.add(south, BorderLayout.SOUTH);
        return panel;
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
                    r.getClientId(),
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
            return;
        }

        Reservation reservation = confirmedReservations.get(row);
        availableVehicles = vehicleManager.getAvailableVehiclesForModel(reservation.getVehicleModelId());

        for (Vehicle v : availableVehicles) {
            vehicleTableModel.addRow(new Object[]{ v.getLicensePlate(), v.getMileage() });
        }
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

        try {
            rentalManager.issueVehicle(reservation.getId(), vehicle.getId(), agent.getId(), mileage);
            vehicleManager.setVehicleMileage(vehicle.getId(), mileage);
            JOptionPane.showMessageDialog(this, "Vozilo je uspešno izdato.", "Uspešno", JOptionPane.INFORMATION_MESSAGE);
            mileageField.setText("");
            refreshReservations();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Izdavanje nije moguće", JOptionPane.ERROR_MESSAGE);
        }
    }
}
