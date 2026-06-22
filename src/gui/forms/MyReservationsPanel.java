package gui.forms;

import manager.ReservationManager;
import manager.VehicleManager;
import model.Client;
import model.Reservation;
import model.VehicleModel;
import model.enums.ReservationStatus;
import util.AppContext;
import util.DateUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MyReservationsPanel extends JPanel {

    private final Client client;
    private final ReservationManager reservationManager;
    private final VehicleManager vehicleManager;

    private DefaultTableModel tableModel;
    private JTable table;
    private List<Reservation> currentReservations;
    private final List<model.AdditionalService> allServices;

    public MyReservationsPanel(Client client) {
        this.client = client;
        this.reservationManager = AppContext.getInstance().getReservationManager();
        this.vehicleManager = AppContext.getInstance().getVehicleManager();
        this.allServices = AppContext.getInstance().getAdditionalServiceRepository().findAll();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(buildTable(), BorderLayout.CENTER);
        refresh();
    }

    private JPanel buildTable() {
        tableModel = new DefaultTableModel(
                new Object[]{"Model vozila", "Od", "Do", "Status", "Dodatne usluge", "Cena"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton refreshButton = new JButton("Osveži");
        refreshButton.addActionListener(e -> refresh());

        JButton cancelButton = new JButton("Otkaži izabranu rezervaciju");
        cancelButton.addActionListener(e -> handleCancel());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(refreshButton);
        buttons.add(cancelButton);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(new JScrollPane(table), BorderLayout.CENTER);
        wrapper.add(buttons, BorderLayout.SOUTH);

        return wrapper;
    }

    public void refresh() {
        currentReservations = reservationManager.getReservationsForClient(client.getId());
        tableModel.setRowCount(0);

        for (Reservation r : currentReservations) {
            VehicleModel model = vehicleManager.getModelById(r.getVehicleModelId()).orElse(null);
            tableModel.addRow(new Object[]{
                    model != null ? model.getFullName() : r.getVehicleModelId(),
                    DateUtil.format(r.getStartDate()),
                    DateUtil.format(r.getEndDate()),
                    translateStatus(r.getStatus()),
                    formatServiceNames(r.getAdditionalServiceIds()),
                    String.format("%.2f RSD", r.getTotalPrice())
            });
        }
    }

    private String formatServiceNames(String serviceIdsCsv) {
        if (serviceIdsCsv == null || serviceIdsCsv.isBlank()) return "-";
        return java.util.Arrays.stream(serviceIdsCsv.split(","))
                .map(id -> allServices.stream()
                        .filter(s -> s.getId().equals(id))
                        .findFirst()
                        .map(model.AdditionalService::getDisplayName)
                        .orElse(id))
                .collect(java.util.stream.Collectors.joining(", "));
    }

    private void handleCancel() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Izaberite rezervaciju iz tabele.", "Napomena", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Reservation selected = currentReservations.get(selectedRow);

        if (selected.getStatus() != ReservationStatus.PENDING && selected.getStatus() != ReservationStatus.CONFIRMED) {
            JOptionPane.showMessageDialog(this,
                    "Samo rezervacije sa statusom NA ČEKANJU ili POTVRĐENA mogu biti otkazane.",
                    "Nije moguće otkazati", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Otkazivanjem rezervacije nećete moći da napravite novu u naredna 24h. Nastaviti?",
                "Potvrda otkazivanja", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            reservationManager.cancelReservation(selected.getId());
            refresh();
        }
    }

    private String translateStatus(ReservationStatus status) {
        return switch (status) {
            case PENDING -> "NA ČEKANJU";
            case CONFIRMED -> "POTVRĐENA";
            case REJECTED -> "ODBIJENA";
            case CANCELLED -> "OTKAZANA";
        };
    }
}
