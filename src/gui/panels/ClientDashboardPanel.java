package gui.panels;

import model.User;

import javax.swing.*;
import java.awt.*;

/**
 * Dashboard za Klijenta.
 * Tabovi: Dostupna vozila, Moje rezervacije, Moja pretplata.
 */
public class ClientDashboardPanel extends JPanel {

    private final User currentUser;

    public ClientDashboardPanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout());
        initTabs();
    }

    private void initTabs() {
        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Dostupna vozila", buildPlaceholderTab(
                "Pregled i rezervacija vozila",
                "Ovde ide NewReservationForm — pretraga modela vozila po datumima, kategoriji i "
                        + "proizvođaču, prikaz cene, dugme 'Rezerviši'."));

        tabbedPane.addTab("Moje rezervacije", buildPlaceholderTab(
                "Istorija i status rezervacija",
                "Ovde ide pregled svih rezervacija ovog klijenta sa statusima, potrošenim iznosima "
                        + "(dodatne usluge, kazne) i mogućnošću otkazivanja rezervacije na čekanju."));

        tabbedPane.addTab("Moja pretplata", buildPlaceholderTab(
                "Status pretplate",
                "Ovde ide prikaz statusa pretplate (aktivna/istekla) i dugme za podnošenje zahteva "
                        + "za obnovu, koji zatim odobrava agent."));

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
