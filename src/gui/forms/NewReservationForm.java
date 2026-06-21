package gui.forms;

import manager.ReservationManager;
import manager.VehicleManager;
import model.AdditionalService;
import model.Client;
import model.VehicleCategory;
import model.VehicleModel;
import model.enums.ChargeType;
import util.AppContext;
import util.DateUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NewReservationForm extends JPanel {

    private final Client client;
    private double discount;
    private final VehicleManager vehicleManager;
    private final ReservationManager reservationManager;
    private final List<AdditionalService> allServices;
    private int additionalDays;

    private JTextField searchField;
    private JComboBox<CategoryItem> categoryCombo;
    private JTextField startDateField;
    private JTextField endDateField;
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private List<VehicleModel> currentResults;
    private List<JCheckBox> serviceCheckboxes;
    private JLabel estimatedPriceLabel;

    public NewReservationForm(Client client) {
        this.client = client;
        this.vehicleManager = AppContext.getInstance().getVehicleManager();
        this.reservationManager = AppContext.getInstance().getReservationManager();
        this.allServices = AppContext.getInstance().getAdditionalServiceRepository().findAll();
        this.discount = getDiscount();
        this.additionalDays = 0;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(buildFilterPanel(), BorderLayout.NORTH);
        add(buildResultsTable(), BorderLayout.CENTER);
        add(buildServicesPanel(), BorderLayout.SOUTH);

        runSearch();
    }

    private double getDiscount() {
        switch (client.getCategory()) {
            case STUDENT:
                return AppContext.getInstance().getPricelistManager().getActivePricelist().get().getStudentDiscountPercent();
            case PENSIONER:
                return AppContext.getInstance().getPricelistManager().getActivePricelist().get().getPensionerDiscountPercent();
            case COMPANY:
                return AppContext.getInstance().getPricelistManager().getActivePricelist().get().getCompanyDiscountPercent();
            default:
                return 0;
        }
    }

    private JPanel buildFilterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Pretraga dostupnih vozila"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        searchField = new JTextField(15);
        categoryCombo = new JComboBox<>();
        categoryCombo.addItem(new CategoryItem(null, "Sve kategorije"));
        for (VehicleCategory cat : vehicleManager.getAllCategories()) {
            categoryCombo.addItem(new CategoryItem(cat.getId(), cat.getType().name()));
        }

        startDateField = new JTextField(LocalDate.now().plusDays(1).toString(), 10);

        int defaultRentalDays = AppContext.getInstance().getPricelistManager().getDefaultRentalDays();
        endDateField = new JTextField(LocalDate.now().plusDays(defaultRentalDays).toString(), 10);
        endDateField.setEditable(false);

        JButton searchButton = new JButton("Pretraži");
        searchButton.addActionListener(e -> runSearch());

        int col = 0;
        gbc.gridx = col++; gbc.gridy = 0;
        panel.add(new JLabel("Model / proizvođač:"), gbc);
        gbc.gridx = col++;
        panel.add(searchField, gbc);

        gbc.gridx = col++;
        panel.add(new JLabel("Kategorija:"), gbc);
        gbc.gridx = col++;
        panel.add(categoryCombo, gbc);

        col = 0;
        gbc.gridx = col++; gbc.gridy = 1;
        panel.add(new JLabel("Od datuma (yyyy-MM-dd):"), gbc);
        gbc.gridx = col++;
        panel.add(startDateField, gbc);

        gbc.gridx = col++;
        panel.add(new JLabel("Do datuma (yyyy-MM-dd):"), gbc);
        gbc.gridx = col++;
        panel.add(endDateField, gbc);

        gbc.gridx = col++;
        panel.add(searchButton, gbc);

        return panel;
    }

    private JScrollPane buildResultsTable() {
        tableModel = new DefaultTableModel(
                new Object[]{"Proizvođač", "Model", "Kategorija", "Cena/dan", "Dostupnost"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        resultsTable = new JTable(tableModel);
        resultsTable.setRowHeight(26);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsTable.getSelectionModel().addListSelectionListener(e -> updateEstimatedPrice());

        return new JScrollPane(resultsTable);
    }

    private JPanel buildServicesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Dodatne usluge"));

        JPanel checkboxPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        serviceCheckboxes = new ArrayList<>();

        for (AdditionalService service : allServices) {
            
            String priceLabel = service.getChargeType() == ChargeType.PER_DAY
                    ? String.format("%.2f RSD/dan", service.getPrice())
                    : String.format("%.2f RSD jednokratno", service.getPrice());

            JCheckBox checkBox = new JCheckBox(service.getDisplayName() + " — " + priceLabel);
            checkBox.putClientProperty("service", service);

            checkBox.addActionListener(e -> updateEstimatedPrice());

            // ako je per day, dodaj spinner za broj dana
            JSpinner daysSpinner = null;

            if (service.getChargeType() == ChargeType.PER_DAY) {
                daysSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 365, 1));

                daysSpinner.addChangeListener(e -> updateEstimatedPrice());

                checkBox.putClientProperty("daysSpinner", daysSpinner);

                JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                wrapper.add(checkBox);
                wrapper.add(daysSpinner);

                serviceCheckboxes.add(checkBox);
                checkboxPanel.add(wrapper);
            } else {
                serviceCheckboxes.add(checkBox);
                checkboxPanel.add(checkBox);
            }
        }
        estimatedPriceLabel = new JLabel("");                                
        estimatedPriceLabel.setFont(estimatedPriceLabel.getFont().deriveFont(Font.BOLD, 14f));

        JButton reserveButton = new JButton("Pošalji zahtev za rezervaciju");
        reserveButton.addActionListener(e -> handleReserve());

        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.add(estimatedPriceLabel, BorderLayout.WEST);

        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonWrapper.add(reserveButton);
        bottomRow.add(buttonWrapper, BorderLayout.EAST);

        panel.add(checkboxPanel, BorderLayout.CENTER);
        panel.add(bottomRow, BorderLayout.SOUTH);

        return panel;
    }

    private void runSearch() {
        int defaultRentalDays = AppContext.getInstance().getPricelistManager().getDefaultRentalDays();
        LocalDate endDate = LocalDate.parse(startDateField.getText()).plusDays(defaultRentalDays);
        endDateField.setText(endDate.toString());

        String query = searchField.getText().trim();
        CategoryItem selectedCategory = (CategoryItem) categoryCombo.getSelectedItem();
        String categoryId = selectedCategory != null ? selectedCategory.id : null;

        currentResults = vehicleManager.searchModels(query, categoryId);
        tableModel.setRowCount(0);

        LocalDate start = parseDateSafely(startDateField.getText());
        LocalDate end = parseDateSafely(endDateField.getText());

        for (VehicleModel model : currentResults) {
            VehicleCategory cat = vehicleManager.getCategoryById(model.getCategoryId()).orElse(null);
            String priceText = cat != null ? String.format("%.2f", cat.getDailyPrice()) : "-";
            boolean available = (start != null && end != null)
                    && reservationManager.isModelAvailableInPeriod(model.getId(), start, end);

            tableModel.addRow(new Object[]{
                    model.getManufacturer(),
                    model.getName(),
                    cat != null ? cat.getType().name() : "-",
                    priceText,
                    available ? "Dostupno" : "Nije dostupno"
            });
        }

        updateEstimatedPrice();
    }


    private void updateEstimatedPrice() {
        int selectedRow = resultsTable.getSelectedRow();
        LocalDate start = parseDateSafely(startDateField.getText());
        LocalDate end = parseDateSafely(endDateField.getText());

        String priceText = discount == 0
        ? "Ukupna cena: "
        : "Ukupna cena (sa " + discount + "% popusta): ";

        if (selectedRow < 0 || start == null || end == null || end.isBefore(start)) {
            estimatedPriceLabel.setText(priceText);
            return;
        }

        double total = calculateTotalPrice(selectedRow, start, end);
        estimatedPriceLabel.setText(priceText + total + "RSD");
    }

    private double calculateTotalPrice(int modelRow, LocalDate start, LocalDate end) {
        VehicleModel model = currentResults.get(modelRow);
        VehicleCategory cat = vehicleManager.getCategoryById(model.getCategoryId()).orElse(null);
        long days = ChronoUnit.DAYS.between(start, end);

        double total = (cat != null ? cat.getDailyPrice() : 0) * days;

        for (JCheckBox checkBox : serviceCheckboxes) {
            if (!checkBox.isSelected()) continue;
            AdditionalService service = (AdditionalService) checkBox.getClientProperty("service");
            if (service.getChargeType() == ChargeType.PER_DAY) {
                JSpinner spinner = (JSpinner) checkBox.getClientProperty("daysSpinner");
                if (spinner != null) {
                    int daysSpinner = (int) spinner.getValue();
                    total += service.getPrice() * daysSpinner;
                    additionalDays = daysSpinner;
                }
            } else {
                total += service.getPrice();
            }
        }

        if (discount != 0){
            total -= discount / 100 * total;
        }

        return total;
    }

    private String getSelectedServiceIds() {
        return serviceCheckboxes.stream()
                .filter(JCheckBox::isSelected)
                .map(cb -> ((AdditionalService) cb.getClientProperty("service")).getId())
                .collect(Collectors.joining(","));
    }

    private void handleReserve() {
        int selectedRow = resultsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Izaberite vozilo iz tabele.", "Napomena", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate start = parseDateSafely(startDateField.getText());
        LocalDate end = parseDateSafely(endDateField.getText());
        

        if (start == null || end == null) {
            JOptionPane.showMessageDialog(this, "Datumi moraju biti u formatu yyyy-MM-dd.", "Greška", JOptionPane.ERROR_MESSAGE);
            return;
        }

        VehicleModel selectedModel = currentResults.get(selectedRow);
        double totalPrice = calculateTotalPrice(selectedRow, start, end);

        end = end.plusDays(additionalDays);

        String serviceIds = getSelectedServiceIds();

        try {
            reservationManager.createReservation(client, selectedModel.getId(), start, end, serviceIds, totalPrice);
            JOptionPane.showMessageDialog(this,
                    "Zahtev za rezervaciju je poslat. Status: NA ČEKANJU.\nUkupna cena: " + String.format("%.2f", totalPrice) + " RSD",
                    "Uspešno", JOptionPane.INFORMATION_MESSAGE);
            runSearch();
            for (JCheckBox checkBox : serviceCheckboxes) checkBox.setSelected(false);
            updateEstimatedPrice();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Rezervacija nije moguća", JOptionPane.ERROR_MESSAGE);
        }
    }

    private LocalDate parseDateSafely(String text) {
        try {
            return DateUtil.parse(text.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static class CategoryItem {
        final String id;
        final String label;

        CategoryItem(String id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
