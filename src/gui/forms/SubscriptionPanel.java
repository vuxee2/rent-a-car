package gui.forms;

import manager.SubscriptionManager;
import model.Client;
import model.Subscription;
import model.enums.SubscriptionStatus;
import util.AppContext;
import util.DateUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Optional;

public class SubscriptionPanel extends JPanel {

    private final Client client;
    private final SubscriptionManager subscriptionManager;

    private JLabel statusLabel;
    private JLabel detailsLabel;
    private JButton requestButton;
    private DefaultTableModel historyTableModel;

    public SubscriptionPanel(Client client) {
        this.client = client;
        this.subscriptionManager = AppContext.getInstance().getSubscriptionManager();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(buildStatusCard(), BorderLayout.NORTH);
        add(buildHistoryTable(), BorderLayout.CENTER);

        refresh();
    }

    private JPanel buildStatusCard() {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Status pretplate"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        statusLabel = new JLabel();
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 16f));

        detailsLabel = new JLabel();
        detailsLabel.setForeground(Color.GRAY);

        textPanel.add(statusLabel);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(detailsLabel);

        requestButton = new JButton("Podnesi zahtev za pretplatu");
        requestButton.addActionListener(e -> handleRequestSubscription());

        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonWrapper.add(requestButton);

        card.add(textPanel, BorderLayout.WEST);
        card.add(buttonWrapper, BorderLayout.EAST);

        return card;
    }

    private JScrollPane buildHistoryTable() {
        historyTableModel = new DefaultTableModel(
                new Object[]{"Datum početka", "Datum isteka", "Status", "Uplaćeno"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(historyTableModel);
        table.setRowHeight(26);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createTitledBorder("Istorija pretplata"));
        wrapper.add(table.getTableHeader(), BorderLayout.NORTH);
        wrapper.add(table, BorderLayout.CENTER);

        return new JScrollPane(wrapper);
    }

    private void refresh() {
        Optional<Subscription> active = subscriptionManager.getActiveSubscription(client.getId());
        boolean hasPending = subscriptionManager.getSubscriptionsForClient(client.getId()).stream()
                .anyMatch(s -> s.getStatus() == SubscriptionStatus.PENDING);

        if (active.isPresent()) {
            Subscription s = active.get();
            statusLabel.setText("Pretplata je AKTIVNA");
            statusLabel.setForeground(new Color(0, 130, 0));
            detailsLabel.setText("Važi do: " + DateUtil.format(s.getEndDate())
                    + "  |  Uplaćeno: " + String.format("%.2f RSD", s.getPaidAmount()));
            requestButton.setEnabled(false);
        } else if (hasPending) {
            statusLabel.setText("Zahtev za pretplatu je NA ČEKANJU");
            statusLabel.setForeground(new Color(200, 130, 0));
            detailsLabel.setText("Sačekajte odobrenje agenta.");
            requestButton.setEnabled(false);
        } else {
            statusLabel.setText("Nemate aktivnu pretplatu");
            statusLabel.setForeground(Color.RED);
            detailsLabel.setText("Bez aktivne pretplate ne možete rezervisati vozilo. Podnesite zahtev za pretplatu.");
            requestButton.setEnabled(true);
        }

        historyTableModel.setRowCount(0);
        List<Subscription> all = subscriptionManager.getSubscriptionsForClient(client.getId());
        for (Subscription s : all) {
            historyTableModel.addRow(new Object[]{
                    DateUtil.format(s.getStartDate()),
                    DateUtil.format(s.getEndDate()),
                    translateStatus(s.getStatus()),
                    String.format("%.2f RSD", s.getPaidAmount())
            });
        }
    }

    private void handleRequestSubscription() {
        try {
            subscriptionManager.requestSubscription(client.getId());
            JOptionPane.showMessageDialog(this,
                    "Zahtev za pretplatu je poslat. Status: NA ČEKANJU.",
                    "Uspešno", JOptionPane.INFORMATION_MESSAGE);
            refresh();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Zahtev nije moguć", JOptionPane.WARNING_MESSAGE);
        }
    }

    private String translateStatus(SubscriptionStatus status) {
        return switch (status) {
            case ACTIVE -> "AKTIVNA";
            case EXPIRED -> "ISTEKLA";
            case PENDING -> "NA ČEKANJU";
            case REJECTED -> "ODBIJENA";
        };
    }
}
