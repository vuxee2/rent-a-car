package gui.panels;

import gui.forms.AdditionalServiceManagementPanel;
import gui.forms.EmployeeManagementPanel;
import gui.forms.PricelistPanel;
import gui.forms.ReportPanel;
import gui.forms.VehicleManagementPanel;
import manager.ReportManager;
import model.User;
import util.AppContext;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;


public class AdminDashboardPanel extends JPanel {

    private final User admin;

    public AdminDashboardPanel(User currentUser) {
        this.admin = currentUser;
        setLayout(new BorderLayout());
        initTabs();
    }

    private void initTabs() {
        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Pregled", buildOverviewTab());
        tabbedPane.addTab("Zaposleni", new EmployeeManagementPanel());
        tabbedPane.addTab("Vozila", new VehicleManagementPanel());
        tabbedPane.addTab("Cenovnik", new PricelistPanel(admin));
        tabbedPane.addTab("Dodatne usluge", new AdditionalServiceManagementPanel());
        tabbedPane.addTab("Izveštaji", new ReportPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel buildOverviewTab() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        ReportManager reportManager = AppContext.getInstance().getReportManager();
        long employeeCount = AppContext.getInstance().getUserManager().getAllAgents().size()
                + AppContext.getInstance().getUserManager().getAllAdministrators().size();
        long vehicleCount = AppContext.getInstance().getVehicleManager().getAllModels().stream()
                .mapToLong(m -> AppContext.getInstance().getVehicleManager().getVehiclesForModel(m.getId()).size())
                .sum();
        long pendingReservations = AppContext.getInstance().getReservationManager().getPendingReservations().size();
        ReportManager.FinancialReport monthReport = reportManager.getFinancialReport(
                LocalDate.now().withDayOfMonth(1), LocalDate.now());

        panel.add(buildStatCard("Ukupno zaposlenih", String.valueOf(employeeCount)));
        panel.add(buildStatCard("Ukupno vozila", String.valueOf(vehicleCount)));
        panel.add(buildStatCard("Rezervacije na čekanju", String.valueOf(pendingReservations)));
        panel.add(buildStatCard("Prihod ovog meseca", String.format("%.2f RSD", monthReport.totalIncome())));

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
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 22f));

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);

        return card;
    }
}
