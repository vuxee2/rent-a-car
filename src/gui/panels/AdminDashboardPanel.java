package gui.panels;

import model.User;

import javax.swing.*;
import java.awt.*;

/**
 * Dashboard za Administratora.
 * Tabovi: Pregled, Zaposleni, Vozila, Cenovnik, Izvestaji.
 * Svaki tab je za sada placeholder panel — popuni ga konkretnom
 * tabelom/formom kad budes implementirao odgovarajuci deo (CRUD, izvestaji...).
 */
public class AdminDashboardPanel extends JPanel {

    private final User currentUser;

    public AdminDashboardPanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout());
        initTabs();
    }

    private void initTabs() {
        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Pregled", buildOverviewTab());
        tabbedPane.addTab("Zaposleni", buildPlaceholderTab(
                "Upravljanje zaposlenima",
                "Ovde ide UserManagementPanel — CRUD za agente i administratore."));
        tabbedPane.addTab("Vozila", buildPlaceholderTab(
                "Upravljanje vozilima",
                "Ovde ide VehicleManagementPanel — kategorije, modeli, primerci vozila."));
        tabbedPane.addTab("Cenovnik", buildPlaceholderTab(
                "Cenovnik agencije",
                "Ovde ide PricelistPanel — cene najma, popusti, kazne, trajanje najma."));
        tabbedPane.addTab("Izveštaji", buildPlaceholderTab(
                "Izveštaji i statistika",
                "Ovde ide ReportPanel — izdavanja, rezervacije, prihodi/rashodi, XChart grafici."));

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel buildOverviewTab() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(buildStatCard("Ukupno zaposlenih", "—"));
        panel.add(buildStatCard("Ukupno vozila", "—"));
        panel.add(buildStatCard("Aktivne rezervacije", "—"));
        panel.add(buildStatCard("Prihod ovog meseca", "—"));

        return panel;
    }

    private JPanel buildStatCard(String title, String value) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN, 12f));
        titleLabel.setForeground(Color.GRAY);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 26f));

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);

        return card;
    }

    private JPanel buildPlaceholderTab(String title, String description) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));

        JLabel descLabel = new JLabel("<html><body style='width: 350px'>" + description + "</body></html>");
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
