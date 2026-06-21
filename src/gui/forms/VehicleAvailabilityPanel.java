package gui.forms;

import manager.VehicleManager;
import model.Vehicle;
import model.VehicleModel;
import model.enums.VehicleStatus;
import util.AppContext;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;


public class VehicleAvailabilityPanel extends JPanel {

    private final VehicleManager vehicleManager;
    private DefaultTableModel tableModel;

    public VehicleAvailabilityPanel() {
        this.vehicleManager = AppContext.getInstance().getVehicleManager();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(buildTable(), BorderLayout.CENTER);
        refresh();
    }

    private JPanel buildTable() {
        tableModel = new DefaultTableModel(
                new Object[]{"Proizvođač", "Model", "Registracija", "Kilometraža", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(26);

        JButton refreshButton = new JButton("Osveži");
        refreshButton.addActionListener(e -> refresh());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(refreshButton);
        wrapper.add(bottom, BorderLayout.SOUTH);

        return wrapper;
    }

    private void refresh() {
        tableModel.setRowCount(0);
        for (VehicleModel model : vehicleManager.getAllModels()) {
            for (Vehicle v : vehicleManager.getVehiclesForModel(model.getId())) {
                tableModel.addRow(new Object[]{
                        model.getManufacturer(),
                        model.getName(),
                        v.getLicensePlate(),
                        v.getMileage(),
                        v.getStatus() == VehicleStatus.AVAILABLE ? "DOSTUPNO" : "IZDATO"
                });
            }
        }
    }
}
