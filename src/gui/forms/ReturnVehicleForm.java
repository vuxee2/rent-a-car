package gui.forms;

import manager.RentalManager;
import manager.VehicleManager;
import model.Rental;
import model.Vehicle;
import util.AppContext;
import util.DateUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;


public class ReturnVehicleForm extends JPanel {

    private final RentalManager rentalManager;
    private final VehicleManager vehicleManager;

    private DefaultTableModel tableModel;
    private JTable table;
    private List<Rental> activeRentals;

    private JTextField mileageField;
    private JTextField returnDateField;
    private JLabel lateFeePreviewLabel;

    public ReturnVehicleForm() {
        this.rentalManager = AppContext.getInstance().getRentalManager();
        this.vehicleManager = AppContext.getInstance().getVehicleManager();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(buildTable(), BorderLayout.CENTER);
        add(buildReturnPanel(), BorderLayout.SOUTH);

        refresh();
    }

    private JScrollPane buildTable() {
        tableModel = new DefaultTableModel(
                new Object[]{"Registracija", "Km pri preuzimanju", "Očekivani povratak"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        return new JScrollPane(table);
    }

    private JPanel buildReturnPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Prijem vraćenog vozila"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        mileageField = new JTextField(10);
        returnDateField = new JTextField(LocalDate.now().toString(), 10);
        lateFeePreviewLabel = new JLabel("Kazna za kašnjenje: —");

        JButton returnButton = new JButton("Evidentiraj vraćanje");
        returnButton.setForeground(new Color(0, 110, 0));
        returnButton.addActionListener(e -> handleReturn());

        JButton refreshButton = new JButton("Osveži");
        refreshButton.addActionListener(e -> refresh());

        int col = 0;
        gbc.gridx = col++; gbc.gridy = 0;
        panel.add(new JLabel("Kilometraža pri vraćanju:"), gbc);
        gbc.gridx = col++;
        panel.add(mileageField, gbc);

        gbc.gridx = col++;
        panel.add(new JLabel("Datum vraćanja (yyyy-MM-dd):"), gbc);
        gbc.gridx = col++;
        panel.add(returnDateField, gbc);

        gbc.gridx = col++;
        panel.add(returnButton, gbc);
        gbc.gridy ++;
        panel.add(refreshButton, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 5;
        panel.add(lateFeePreviewLabel, gbc);

        return panel;
    }

    private void refresh() {
        activeRentals = rentalManager.getActiveRentals();
        tableModel.setRowCount(0);

        for (Rental r : activeRentals) {
            Vehicle vehicle = findVehicle(r.getVehicleId());
            String plate = vehicle != null ? vehicle.getLicensePlate() : r.getVehicleId();
            tableModel.addRow(new Object[]{
                    plate,
                    r.getMileageAtPickup(),
                    DateUtil.format(r.getExpectedReturnDate())
            });
        }
        lateFeePreviewLabel.setText("Kazna za kašnjenje: —");
    }

    private Vehicle findVehicle(String vehicleId) {
        return vehicleManager.getAllModels().stream()
                .flatMap(m -> vehicleManager.getVehiclesForModel(m.getId()).stream())
                .filter(v -> v.getId().equals(vehicleId))
                .findFirst()
                .orElse(null);
    }

    private void handleReturn() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Izaberite vozilo iz tabele.", "Napomena", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int mileage;
        LocalDate returnDate;
        try {
            mileage = Integer.parseInt(mileageField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Kilometraža mora biti ceo broj.", "Greška", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            returnDate = DateUtil.parse(returnDateField.getText().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Datum mora biti u formatu yyyy-MM-dd.", "Greška", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Rental rental = activeRentals.get(row);

        try {
            Rental updated = rentalManager.returnVehicle(rental.getId(), mileage, returnDate);
            String feeMsg = updated.getLateFee() > 0
                    ? String.format("\nNaplaćena kazna za kašnjenje: %.2f RSD", updated.getLateFee())
                    : "";
            JOptionPane.showMessageDialog(this,
                    "Vozilo je uspešno vraćeno." + feeMsg,
                    "Uspešno", JOptionPane.INFORMATION_MESSAGE);
            mileageField.setText("");
            refresh();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Greška", JOptionPane.ERROR_MESSAGE);
        }
    }
}
