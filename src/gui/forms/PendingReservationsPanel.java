package gui.forms;

import manager.ReservationManager;
import manager.VehicleManager;
import model.Reservation;
import model.User;
import model.VehicleModel;
import util.AppContext;
import util.DateUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PendingReservationsPanel extends JPanel {

    private final User agent;
    private final ReservationManager reservationManager;
    private final VehicleManager vehicleManager;

    private DefaultTableModel tableModel;
    private JTable table;
    private List<Reservation> currentReservations;

    public PendingReservationsPanel(User agent) {
        this.agent = agent;
        this.reservationManager = AppContext.getInstance().getReservationManager();
        this.vehicleManager = AppContext.getInstance().getVehicleManager();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(buildTable(), BorderLayout.CENTER);
        refresh();
    }

    private JPanel buildTable() {
        tableModel = new DefaultTableModel(
                new Object[]{"Klijent ID", "Model vozila", "Od", "Do", "Cena"}, 0) {
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

        JButton confirmButton = new JButton("Potvrdi rezervaciju");
        confirmButton.setForeground(new Color(0, 110, 0));
        confirmButton.addActionListener(e -> handleConfirm());

        JButton rejectButton = new JButton("Odbij rezervaciju");
        rejectButton.setForeground(Color.RED);
        rejectButton.addActionListener(e -> handleReject());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(refreshButton);
        buttons.add(confirmButton);
        buttons.add(rejectButton);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(new JScrollPane(table), BorderLayout.CENTER);
        wrapper.add(buttons, BorderLayout.SOUTH);

        return wrapper;
    }

    public void refresh() {
        currentReservations = reservationManager.getPendingReservations();
        tableModel.setRowCount(0);

        for (Reservation r : currentReservations) {
            VehicleModel model = vehicleManager.getModelById(r.getVehicleModelId()).orElse(null);
            tableModel.addRow(new Object[]{
                    r.getClientId(),
                    model != null ? model.getFullName() : r.getVehicleModelId(),
                    DateUtil.format(r.getStartDate()),
                    DateUtil.format(r.getEndDate()),
                    String.format("%.2f RSD", r.getTotalPrice())
            });
        }
    }

    private void handleConfirm() {
        Reservation selected = getSelectedReservation();
        if (selected == null) return;

        try {
            reservationManager.confirmReservation(selected.getId(), agent.getId());
            JOptionPane.showMessageDialog(this, "Rezervacija je potvrđena.", "Uspešno", JOptionPane.INFORMATION_MESSAGE);
            refresh();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Nije moguće potvrditi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleReject() {
        Reservation selected = getSelectedReservation();
        if (selected == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Da li ste sigurni da želite da odbijete ovu rezervaciju?",
                "Potvrda odbijanja", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            reservationManager.rejectReservation(selected.getId(), agent.getId());
            JOptionPane.showMessageDialog(this, "Rezervacija je odbijena.", "Uspešno", JOptionPane.INFORMATION_MESSAGE);
            refresh();
        }
    }

    private Reservation getSelectedReservation() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Izaberite rezervaciju iz tabele.", "Napomena", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return currentReservations.get(row);
    }
}
