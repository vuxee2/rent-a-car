package gui.panels;

import model.User;

import javax.swing.*;
import java.awt.*;

/**
 * Dashboard za Agenta.
 * Tabovi: Rezervacije na čekanju, Izdavanje/Vraćanje, Klijenti, Vozila (dostupnost).
 */
public class AgentDashboardPanel extends JPanel {

    private final User currentUser;

    public AgentDashboardPanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout());
        initTabs();
    }

    private void initTabs() {
        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Rezervacije na čekanju", buildPlaceholderTab(
                "Potvrda / odbijanje rezervacija",
                "Ovde ide ReservationManagementPanel — lista rezervacija sa statusom NA_CEKANJU, "
                        + "dugmad Potvrdi / Odbij. Prilikom potvrde proverava se dostupnost slobodnog primerka."));

        tabbedPane.addTab("Izdavanje vozila", buildPlaceholderTab(
                "Izdavanje rezervisanog vozila",
                "Ovde ide IssueVehicleForm — agent bira slobodan primerak modela, unosi kilometražu "
                        + "pri preuzimanju, status vozila prelazi u IZDATO."));

        tabbedPane.addTab("Vraćanje vozila", buildPlaceholderTab(
                "Prijem vraćenog vozila",
                "Ovde ide ReturnVehicleForm — agent unosi kilometražu pri vraćanju, sistem obračunava "
                        + "eventualnu kaznu za kašnjenje, status vozila prelazi u DOSTUPNO."));

        tabbedPane.addTab("Klijenti", buildPlaceholderTab(
                "Registracija klijenata i pretplate",
                "Ovde ide panel za registraciju novih klijenata i odobravanje zahteva za obnovu pretplate."));

        tabbedPane.addTab("Dostupnost vozila", buildPlaceholderTab(
                "Pregled dostupnosti",
                "Ovde ide pregled svih vozila sa statusom DOSTUPNO / IZDATO po modelu i kategoriji."));

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel buildPlaceholderTab(String title, String description) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));

        JLabel descLabel = new JLabel("<html><body style='width: 380px'>" + description + "</body></html>");
        descLabel.setForeground(Color.GRAY);
        descLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(titleLabel);
        textPanel.add(descLabel);

        panel.add(textPanel, BorderLayout.NORTH);
        return panel;
    }
}
