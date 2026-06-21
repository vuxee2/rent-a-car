package gui.panels;

import gui.forms.ApproveSubscriptionsForm;
import gui.forms.IssueVehicleForm;
import gui.forms.PendingReservationsPanel;
import gui.forms.RegisterClientForm;
import gui.forms.ReturnVehicleForm;
import gui.forms.VehicleAvailabilityPanel;
import model.User;

import javax.swing.*;
import java.awt.*;


public class AgentDashboardPanel extends JPanel {

    private final User agent;

    public AgentDashboardPanel(User currentUser) {
        this.agent = currentUser;
        setLayout(new BorderLayout());
        initTabs();
    }

    private void initTabs() {
        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Rezervacije na čekanju", new PendingReservationsPanel(agent));
        tabbedPane.addTab("Izdavanje vozila", new IssueVehicleForm(agent));
        tabbedPane.addTab("Vraćanje vozila", new ReturnVehicleForm());
        tabbedPane.addTab("Registracija klijenta", new RegisterClientForm());
        tabbedPane.addTab("Zahtevi za pretplatu", new ApproveSubscriptionsForm(agent));
        tabbedPane.addTab("Dostupnost vozila", new VehicleAvailabilityPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }
}
