package gui.forms;

import manager.VehicleManager;
import model.Vehicle;
import model.VehicleCategory;
import model.VehicleModel;
import model.enums.VehicleCategoryType;
import model.enums.VehicleStatus;
import util.AppContext;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class VehicleManagementPanel extends JPanel {

    private final VehicleManager vehicleManager;

    private List<VehicleCategory> currentCategories = new ArrayList<>();
    private List<VehicleModel> currentModels = new ArrayList<>();
    private List<Vehicle> currentVehicles = new ArrayList<>();

    private DefaultTableModel categoriesTableModel;
    private DefaultTableModel modelsTableModel;
    private DefaultTableModel vehiclesTableModel;

    public VehicleManagementPanel() {
        this.vehicleManager = AppContext.getInstance().getVehicleManager();

        setLayout(new BorderLayout());
        JTabbedPane innerTabs = new JTabbedPane();
        innerTabs.addTab("Kategorije", buildCategoriesTab());
        innerTabs.addTab("Modeli", buildModelsTab());
        innerTabs.addTab("Primerci vozila", buildVehiclesTab());
        add(innerTabs, BorderLayout.CENTER);
    }

    // ==================== KATEGORIJE ====================

    private JPanel buildCategoriesTab() {
        categoriesTableModel = new DefaultTableModel(
                new Object[]{"Kategorija", "Cena/dan"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(categoriesTableModel);
        table.setRowHeight(26);

        JButton addButton = new JButton("Dodaj kategoriju");
        addButton.addActionListener(e -> openCategoryDialog(null));

        JButton editButton = new JButton("Izmeni");
        editButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { warnNoSelection(); return; }
            openCategoryDialog(currentCategories.get(row));
        });

        JButton deleteButton = new JButton("Obriši");
        deleteButton.setForeground(Color.RED);
        deleteButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { warnNoSelection(); return; }
            VehicleCategory selected = currentCategories.get(row);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Da li ste sigurni da želite da obrišete kategoriju "
                            + selected.getType().toString() + " (" + selected.getDailyPrice() + ")?",
                    "Potvrda brisanja", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                vehicleManager.deleteVehicleCategory(selected.getId());
                refreshCategoriesTable();
            }
        });

        JButton refreshButton = new JButton("Osveži");
        refreshButton.addActionListener(e -> refreshCategoriesTable());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(refreshButton);
        buttons.add(addButton);
        buttons.add(editButton);
        buttons.add(deleteButton);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);

        refreshCategoriesTable();
        return panel;
    }

    private void refreshCategoriesTable() {
        currentCategories = vehicleManager.getAllCategories();
        categoriesTableModel.setRowCount(0);
        for (VehicleCategory c : currentCategories) {
            categoriesTableModel.addRow(new Object[]{
                    c.getType().name(),
                    String.format("%.2f RSD", c.getDailyPrice())
            });
        }
    }

    private void openCategoryDialog(VehicleCategory existing) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                existing == null ? "Nova kategorija" : "Izmena kategorije", true);
        dialog.setSize(350, 200);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<VehicleCategoryType> typeCombo = new JComboBox<>(VehicleCategoryType.values());
        if (existing != null) typeCombo.setSelectedItem(existing.getType());
        typeCombo.setEnabled(existing == null);

        JTextField priceField = new JTextField(
                existing != null ? String.valueOf(existing.getDailyPrice()) : "3000", 12);

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        form.add(new JLabel("Kategorija:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        form.add(typeCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        form.add(new JLabel("Cena po danu:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        form.add(priceField, gbc);

        JButton saveButton = new JButton("Sačuvaj");
        saveButton.addActionListener(e -> {
            try {
                double price = Double.parseDouble(priceField.getText().trim());
                VehicleCategory category = new VehicleCategory(
                        existing != null ? existing.getId() : UUID.randomUUID().toString(),
                        (VehicleCategoryType) typeCombo.getSelectedItem(),
                        price
                );
                vehicleManager.addVehicleCategory(category);
                dialog.dispose();
                refreshCategoriesTable();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Cena mora biti broj.", "Greška", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Greška", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        form.add(saveButton, gbc);

        dialog.setContentPane(form);
        dialog.setVisible(true);
    }

    // ==================== MODELI ====================

    private JPanel buildModelsTab() {
        modelsTableModel = new DefaultTableModel(
                new Object[]{"Proizvođač", "Model", "Kategorija"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(modelsTableModel);
        table.setRowHeight(26);

        JButton addButton = new JButton("Dodaj model");
        addButton.addActionListener(e -> openModelDialog(null));

        JButton editButton = new JButton("Izmeni");
        editButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { warnNoSelection(); return; }
            openModelDialog(currentModels.get(row));
        });

        JButton deleteButton = new JButton("Obriši");
        deleteButton.setForeground(Color.RED);
        deleteButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { warnNoSelection(); return; }
            VehicleModel selected = currentModels.get(row);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Da li ste sigurni da želite da obrišete model " + selected.getFullName() + "?",
                    "Potvrda brisanja", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                vehicleManager.deleteVehicleModel(selected.getId());
                refreshModelsTable();
            }
        });

        JButton refreshButton = new JButton("Osveži");
        refreshButton.addActionListener(e -> refreshModelsTable());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(refreshButton);
        buttons.add(addButton);
        buttons.add(editButton);
        buttons.add(deleteButton);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);

        refreshModelsTable();
        return panel;
    }

    private void refreshModelsTable() {
        currentModels = vehicleManager.getAllModels();
        modelsTableModel.setRowCount(0);
        for (VehicleModel m : currentModels) {
            String categoryName = currentCategories.stream()
                    .filter(c -> c.getId().equals(m.getCategoryId()))
                    .map(c -> c.getType().name())
                    .findFirst()
                    .orElse("?");
            modelsTableModel.addRow(new Object[]{
                    m.getManufacturer(),
                    m.getName(),
                    categoryName
            });
        }
    }

    private void openModelDialog(VehicleModel existing) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                existing == null ? "Novi model" : "Izmena modela", true);
        dialog.setSize(380, 250);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField manufacturerField = new JTextField(
                existing != null ? existing.getManufacturer() : "", 16);
        JTextField nameField = new JTextField(
                existing != null ? existing.getName() : "", 16);

        JComboBox<VehicleCategory> categoryCombo = new JComboBox<>(
                currentCategories.toArray(new VehicleCategory[0]));
        if (existing != null) {
            currentCategories.stream()
                    .filter(c -> c.getId().equals(existing.getCategoryId()))
                    .findFirst()
                    .ifPresent(categoryCombo::setSelectedItem);
        }

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        form.add(new JLabel("Proizvođač:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        form.add(manufacturerField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        form.add(new JLabel("Model:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        form.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        form.add(new JLabel("Kategorija:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        form.add(categoryCombo, gbc);

        JButton saveButton = new JButton("Sačuvaj");
        saveButton.addActionListener(e -> {
            VehicleCategory selectedCategory = (VehicleCategory) categoryCombo.getSelectedItem();
            if (selectedCategory == null) {
                JOptionPane.showMessageDialog(dialog, "Prvo dodajte bar jednu kategoriju vozila.",
                        "Greška", JOptionPane.ERROR_MESSAGE);
                return;
            }
            VehicleModel model = new VehicleModel(
                    existing != null ? existing.getId() : UUID.randomUUID().toString(),
                    manufacturerField.getText().trim(),
                    nameField.getText().trim(),
                    selectedCategory.getId()
            );
            vehicleManager.addVehicleModel(model);
            dialog.dispose();
            refreshModelsTable();
        });

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        form.add(saveButton, gbc);

        dialog.setContentPane(form);
        dialog.setVisible(true);
    }

    // ==================== PRIMERCI VOZILA ====================

    private JPanel buildVehiclesTab() {
        vehiclesTableModel = new DefaultTableModel(
                new Object[]{"Model", "Registracija", "Kilometraža", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(vehiclesTableModel);
        table.setRowHeight(26);

        JButton addButton = new JButton("Dodaj vozilo");
        addButton.addActionListener(e -> openVehicleDialog(null));

        JButton editButton = new JButton("Izmeni");
        editButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { warnNoSelection(); return; }
            openVehicleDialog(currentVehicles.get(row));
        });

        JButton deleteButton = new JButton("Obriši");
        deleteButton.setForeground(Color.RED);
        deleteButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { warnNoSelection(); return; }
            Vehicle selected = currentVehicles.get(row);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Da li ste sigurni da želite da obrišete vozilo " + selected.getLicensePlate() + "?",
                    "Potvrda brisanja", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                vehicleManager.deleteVehicle(selected.getId());
                refreshVehiclesTable();
            }
        });

        JButton refreshButton = new JButton("Osveži");
        refreshButton.addActionListener(e -> refreshVehiclesTable());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(refreshButton);
        buttons.add(addButton);
        buttons.add(editButton);
        buttons.add(deleteButton);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);

        refreshVehiclesTable();
        return panel;
    }

    private void refreshVehiclesTable() {
        currentVehicles.clear();
        vehiclesTableModel.setRowCount(0);
        for (VehicleModel m : vehicleManager.getAllModels()) {
            for (Vehicle v : vehicleManager.getVehiclesForModel(m.getId())) {
                currentVehicles.add(v);
                vehiclesTableModel.addRow(new Object[]{
                        m.getFullName(),
                        v.getLicensePlate(),
                        v.getMileage(),
                        v.getStatus() == VehicleStatus.AVAILABLE ? "DOSTUPNO" : "IZDATO"
                });
            }
        }
    }

    private void openVehicleDialog(Vehicle existing) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                existing == null ? "Novo vozilo" : "Izmena vozila", true);
        dialog.setSize(380, 280);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<VehicleModel> modelCombo = new JComboBox<>(
                currentModels.toArray(new VehicleModel[0]));
        if (existing != null) {
            currentModels.stream()
                    .filter(m -> m.getId().equals(existing.getModelId()))
                    .findFirst()
                    .ifPresent(modelCombo::setSelectedItem);
        }

        JTextField plateField = new JTextField(
                existing != null ? existing.getLicensePlate() : "", 16);
        JTextField mileageField = new JTextField(
                existing != null ? String.valueOf(existing.getMileage()) : "0", 16);
        JComboBox<VehicleStatus> statusCombo = new JComboBox<>(VehicleStatus.values());
        if (existing != null) statusCombo.setSelectedItem(existing.getStatus());

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        form.add(new JLabel("Model:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        form.add(modelCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        form.add(new JLabel("Registarska oznaka:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        form.add(plateField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        form.add(new JLabel("Kilometraža:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        form.add(mileageField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST;
        form.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        form.add(statusCombo, gbc);

        JButton saveButton = new JButton("Sačuvaj");
        saveButton.addActionListener(e -> {
            VehicleModel selectedModel = (VehicleModel) modelCombo.getSelectedItem();
            if (selectedModel == null) {
                JOptionPane.showMessageDialog(dialog, "Prvo dodajte bar jedan model vozila.",
                        "Greška", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                int mileage = Integer.parseInt(mileageField.getText().trim());
                Vehicle vehicle = new Vehicle(
                        existing != null ? existing.getId() : UUID.randomUUID().toString(),
                        selectedModel.getId(),
                        plateField.getText().trim(),
                        (VehicleStatus) statusCombo.getSelectedItem(),
                        mileage
                );
                vehicleManager.addVehicle(vehicle);
                dialog.dispose();
                refreshVehiclesTable();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Kilometraža mora biti ceo broj.",
                        "Greška", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        form.add(saveButton, gbc);

        dialog.setContentPane(form);
        dialog.setVisible(true);
    }

    private void warnNoSelection() {
        JOptionPane.showMessageDialog(this, "Izaberite red iz tabele.", "Napomena", JOptionPane.WARNING_MESSAGE);
    }
}