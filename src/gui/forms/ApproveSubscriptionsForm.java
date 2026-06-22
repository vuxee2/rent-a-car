package gui.forms;

import manager.RentalManager;
import manager.SubscriptionManager;
import manager.UserManager;
import model.Subscription;
import model.User;
import util.AppContext;
import util.DateUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;


public class ApproveSubscriptionsForm extends JPanel {

    private final User agent;
    private final SubscriptionManager subscriptionManager;
    private final RentalManager rentalManager;
    private final UserManager userManager;

    private DefaultTableModel tableModel;
    private JTable table;
    private List<Subscription> pendingSubscriptions;

    public ApproveSubscriptionsForm(User agent) {
        this.agent = agent;
        this.subscriptionManager = AppContext.getInstance().getSubscriptionManager();
        this.rentalManager = AppContext.getInstance().getRentalManager();
        this.userManager = AppContext.getInstance().getUserManager();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(buildTable(), BorderLayout.CENTER);
        refresh();
    }

    private JPanel buildTable() {
        tableModel = new DefaultTableModel(
                new Object[]{"Klijent", "Datum zahteva", "Iznos"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton refreshButton = new JButton("Osveži");
        refreshButton.addActionListener(e -> refresh());

        JButton approveButton = new JButton("Odobri pretplatu");
        approveButton.setForeground(new Color(0, 110, 0));
        approveButton.addActionListener(e -> handleApprove());

        JButton rejectButton = new JButton("Odbij zahtev");
        rejectButton.setForeground(Color.RED);
        rejectButton.addActionListener(e -> handleReject());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(refreshButton);
        buttons.add(approveButton);
        buttons.add(rejectButton);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(new JScrollPane(table), BorderLayout.CENTER);
        wrapper.add(buttons, BorderLayout.SOUTH);

        return wrapper;
    }

    private void refresh() {
        pendingSubscriptions = subscriptionManager.getPendingSubscriptions();
        tableModel.setRowCount(0);

        for (Subscription s : pendingSubscriptions) {
            tableModel.addRow(new Object[]{
                    userManager.getById(s.getClientId()).get().getFullName(),
                    DateUtil.format(s.getStartDate()),
                    String.format("%.2f RSD", s.getPaidAmount())
            });
        }
    }

    private void handleApprove() {
        Subscription selected = getSelected();
        if (selected == null) return;

        int lateReturnCount = rentalManager.countLateReturnsForClient(
                selected.getClientId(), AppContext.getInstance().getReservationRepository());

        try {
            subscriptionManager.approveSubscription(selected.getId(), agent.getId(), lateReturnCount);
            JOptionPane.showMessageDialog(this, "Pretplata je odobrena.", "Uspešno", JOptionPane.INFORMATION_MESSAGE);
            refresh();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Zahtev automatski odbijen", JOptionPane.WARNING_MESSAGE);
            refresh();
        }
    }

    private void handleReject() {
        Subscription selected = getSelected();
        if (selected == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Da li ste sigurni da želite da odbijete ovaj zahtev?",
                "Potvrda odbijanja", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            subscriptionManager.rejectSubscription(selected.getId(), agent.getId());
            refresh();
        }
    }

    private Subscription getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Izaberite zahtev iz tabele.", "Napomena", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return pendingSubscriptions.get(row);
    }
}
