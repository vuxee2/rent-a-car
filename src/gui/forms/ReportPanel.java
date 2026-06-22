package gui.forms;

import manager.ReportManager;
import model.enums.ReservationStatus;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.XChartPanel;
import util.AppContext;
import util.DateUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ReportPanel extends JPanel {

    private final ReportManager reportManager;

    private JTextField fromField;
    private JTextField toField;
    private JTabbedPane innerTabs;

    public ReportPanel() {
        this.reportManager = AppContext.getInstance().getReportManager();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(buildDateRangePanel(), BorderLayout.NORTH);

        innerTabs = new JTabbedPane();
        innerTabs.addTab("Izdavanja po agentu", new JPanel());
        innerTabs.addTab("Status rezervacija", new JPanel());
        innerTabs.addTab("Modeli vozila", new JPanel());
        innerTabs.addTab("Prihodi i rashodi", new JPanel());
        innerTabs.addTab("Grafikon: prihod po kategoriji", new JPanel());
        innerTabs.addTab("Grafikon: opterećenje agenata", new JPanel());
        innerTabs.addTab("Grafikon: status rezervacija (30d)", new JPanel());

        add(innerTabs, BorderLayout.CENTER);

        refreshAllReports();
    }

    private JPanel buildDateRangePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Opseg datuma za tekstualne izveštaje"));

        fromField = new JTextField(LocalDate.now().minusMonths(1).toString(), 10);
        toField = new JTextField(LocalDate.now().toString(), 10);

        JButton applyButton = new JButton("Primeni");
        applyButton.addActionListener(e -> refreshAllReports());

        panel.add(new JLabel("Od:"));
        panel.add(fromField);
        panel.add(new JLabel("Do:"));
        panel.add(toField);
        panel.add(applyButton);

        return panel;
    }

    private void refreshAllReports() {
        LocalDate from = parseDateSafely(fromField.getText());
        LocalDate to = parseDateSafely(toField.getText());
        if (from == null || to == null) {
            JOptionPane.showMessageDialog(this, "Datumi moraju biti u formatu yyyy-MM-dd.", "Greška", JOptionPane.ERROR_MESSAGE);
            return;
        }

        innerTabs.setComponentAt(0, buildAgentIssuedReport(from, to));
        innerTabs.setComponentAt(1, buildReservationStatusReport(from, to));
        innerTabs.setComponentAt(2, buildVehicleModelReport(from, to));
        innerTabs.setComponentAt(3, buildFinancialReport(from, to));
        innerTabs.setComponentAt(4, buildRevenueByCategoryChart());
        innerTabs.setComponentAt(5, buildAgentWorkloadChart());
        innerTabs.setComponentAt(6, buildReservationStatusChart());
    }

    // ==================== TEKSTUALNI IZVESTAJI ====================

    // Izdavanja po agentu
    private JPanel buildAgentIssuedReport(LocalDate from, LocalDate to) {
        DefaultTableModel model = new DefaultTableModel(new Object[]{"Agent", "Broj izdatih vozila"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        Map<String, Long> report = reportManager.getAgentIssuedVehiclesReport(from, to);
        report.forEach((agent, count) -> model.addRow(new Object[]{ agent, count }));

        return wrapInTable(model, "Izdavanja vozila po agentu (" + DateUtil.format(from) + " — " + DateUtil.format(to) + ")");
    }

    // Status rezervacija
    private JPanel buildReservationStatusReport(LocalDate from, LocalDate to) {
        DefaultTableModel model = new DefaultTableModel(new Object[]{"Status", "Broj rezervacija"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        Map<ReservationStatus, Long> report = reportManager.getReservationStatusReport(from, to);
        report.forEach((status, count) -> model.addRow(new Object[]{ translateStatus(status), count }));

        return wrapInTable(model, "Status rezervacija (" + DateUtil.format(from) + " — " + DateUtil.format(to) + ")");
    }

    // Modeli vozila
    private JPanel buildVehicleModelReport(LocalDate from, LocalDate to) {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Model", "Kategorija", "Broj rezervacija", "Broj iznajmljivanja"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        List<ReportManager.VehicleModelReportRow> rows = reportManager.getVehicleModelReport(from, to);
        for (ReportManager.VehicleModelReportRow row : rows) {
            String categoryName = AppContext.getInstance().getVehicleManager().getCategoryById(row.categoryId())
                    .map(c -> c.getType().name()).orElse("-");
            model.addRow(new Object[]{ row.modelName(), categoryName, row.reservationCount(), row.rentalCount() });
        }

        return wrapInTable(model, "Statistika po modelu vozila (" + DateUtil.format(from) + " — " + DateUtil.format(to) + ")");
    }

    // Prihodi i rashodi
    private JPanel buildFinancialReport(LocalDate from, LocalDate to) {
        ReportManager.FinancialReport report = reportManager.getFinancialReport(from, to);

        DefaultTableModel model = new DefaultTableModel(new Object[]{"Stavka", "Iznos"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        model.addRow(new Object[]{ "Prihod od pretplata", String.format("%.2f RSD", report.subscriptionIncome()) });
        model.addRow(new Object[]{ "Prihod od najmova", String.format("%.2f RSD", report.rentalIncome()) });
        model.addRow(new Object[]{ "Prihod od kazni", String.format("%.2f RSD", report.lateFeesIncome()) });
        model.addRow(new Object[]{ "UKUPAN PRIHOD", String.format("%.2f RSD", report.totalIncome()) });
        model.addRow(new Object[]{ "Rashodi (plate)", String.format("%.2f RSD", report.salaryExpense()) });
        model.addRow(new Object[]{ "NETO REZULTAT", String.format("%.2f RSD", report.getNetResult()) });

        return wrapInTable(model, "Prihodi i rashodi (" + DateUtil.format(from) + " — " + DateUtil.format(to) + ")");
    }

    private JPanel wrapInTable(DefaultTableModel model, String title) {
        JTable table = new JTable(model);
        table.setRowHeight(26);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // ==================== GRAFIKONI (XChart) ====================

    // Prihod po kategoriji
    private JPanel buildRevenueByCategoryChart() {
        Map<String, Double> data = reportManager.getRevenueByClientCategoryLastMonths(12);

        PieChart chart = new PieChartBuilder()
                .width(700).height(500)
                .title("Prihod po kategoriji klijenta - poslednjih 12 meseci")
                .build();

        chart.getStyler().setLegendVisible(true);
        chart.getStyler().setPlotContentSize(0.8);

        if (data.isEmpty()) {
            chart.addSeries("Nema podataka", 1);
        } else {
            data.forEach(chart::addSeries);
        }

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new XChartPanel<>(chart), BorderLayout.CENTER);
        return panel;
    }

    // Opterecenje agenta u prethodnih 30 dana
    private JPanel buildAgentWorkloadChart() {
        Map<String, Long> data = reportManager.getAgentWorkloadLast30Days();

        CategoryChart chart = new CategoryChartBuilder()
                .width(700).height(500)
                .title("Opterećenje agenata - poslednjih 30 dana")
                .xAxisTitle("Agent")
                .yAxisTitle("Broj obrađenih rezervacija")
                .build();

        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setXAxisLabelRotation(20);

        if (data.isEmpty()) {
            chart.addSeries("Rezervacije", List.of("Nema podataka"), List.of(0));
        } else {
            chart.addSeries("Rezervacije", List.copyOf(data.keySet()), List.copyOf(data.values()));
        }

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new XChartPanel<>(chart), BorderLayout.CENTER);
        return panel;
    }

    // Status rezervacije u prethodnih 30 dana
    private JPanel buildReservationStatusChart() {
        Map<ReservationStatus, Long> data = reportManager.getReservationStatusLast30Days();

        PieChart chart = new PieChartBuilder()
                .width(700).height(500)
                .title("Status rezervacija - poslednjih 30 dana")
                .build();

        chart.getStyler().setLegendVisible(true);
        chart.getStyler().setPlotContentSize(0.8);

        boolean anyData = false;
        for (Map.Entry<ReservationStatus, Long> entry : data.entrySet()) {
            if (entry.getValue() > 0) {
                chart.addSeries(translateStatus(entry.getKey()), entry.getValue());
                anyData = true;
            }
        }
        if (!anyData) {
            chart.addSeries("Nema podataka", 1);
        }

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new XChartPanel<>(chart), BorderLayout.CENTER);
        return panel;
    }

    private String translateStatus(ReservationStatus status) {
        return switch (status) {
            case PENDING -> "NA ČEKANJU";
            case CONFIRMED -> "POTVRĐENA";
            case REJECTED -> "ODBIJENA";
            case CANCELLED -> "OTKAZANA";
        };
    }

    private LocalDate parseDateSafely(String text) {
        try {
            return DateUtil.parse(text.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
