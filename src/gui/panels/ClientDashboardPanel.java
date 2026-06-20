package gui.panels;

import gui.forms.MyReservationsPanel;
import gui.forms.NewReservationForm;
import gui.forms.SubscriptionPanel;
import model.Client;
import model.User;

import javax.swing.*;
import java.awt.*;

public class ClientDashboardPanel extends JPanel {

    private final Client client;

    public ClientDashboardPanel(User currentUser) {
        this.client = (Client) currentUser;

        setLayout(new BorderLayout());
        initTabs();
    }

    private void initTabs() {
        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Dostupna vozila", new NewReservationForm(client));
        tabbedPane.addTab("Moje rezervacije", new MyReservationsPanel(client));
        tabbedPane.addTab("Moja pretplata", new SubscriptionPanel(client));

        add(tabbedPane, BorderLayout.CENTER);
    }
}
