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
import java.util.List;
import java.util.UUID;

/**
 * Admin upravlja kategorijama, modelima i primercima vozila — kroz tri
 * pod-taba, svaki sa svojom tabelom i CRUD dugmadima.
 */
public class VehicleManagementPanel extends JPanel {

    private final VehicleManager vehicleManager;

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
        DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"Kategorija", "Cena/dan"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(26);

        Runnable[] refreshHolder = new Runnable[1];
        refreshHolder[0] = () -> {
            tableModel.setRowCount(0);
            for (VehicleCategory c : vehicleManager.getAllCategories()) {
                tableModel.addRow(new Object[]{ c.getType().name(), String.format("%.2f RSD", c.getDailyPrice()) });
            }
        };

        JButton addButton = new JButton("Dodaj kategoriju");
        addButton.addActionListener(e -> openCategoryDialog(null, refreshHolder[0]));

        JButton editButton = new JButton("Izmeni");
        editButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { warnNoSelection(); return; }
            openCategoryDialog(vehicleManager.getAllCategories().get(row), refreshHolder[0]);
        });

        JButton refreshButton = new JButton("Osveži");
        refreshButton.addActionListener(e -> refreshHolder[0].run());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(refreshButton);
        buttons.add(addButton);
        buttons.add(editButton);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);

        refreshHolder[0].run();
        return panel;
    }

    private void openCategoryDialog(VehicleCategory existing, Runnable onSave) {
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
        typeCombo.setEnabled(existing == null); // tip kategorije se ne menja, samo cena

        JTextField priceField = new JTextField(existing != null ? String.valueOf(existing.getDailyPrice()) : "3000", 12);

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
                onSave.run();
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
        DefaultTableModel tableModel = new DefaultTableModel(
                new Object[]{"Proizvođač", "Model", "Kategorija"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(26);

        Runnable[] refreshHolder = new Runnable[1];
        refreshHolder[0] = () -> {
            tableModel.setRowCount(0);
            for (VehicleModel m : vehicleManager.getAllModels()) {
                String categoryName = vehicleManager.getCategoryById(m.getCategoryId())
                        .map(c -> c.getType().name()).orElse("-");
                tableModel.addRow(new Object[]{ m.getManufacturer(), m.getName(), categoryName });
            }
        };

        JButton addButton = new JButton("Dodaj model");
        addButton.addActionListener(e -> openModelDialog(null, refreshHolder[0]));

        JButton editButton = new JButton("Izmeni");
        editButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { warnNoSelection(); return; }
            openModelDialog(vehicleManager.getAllModels().get(row), refreshHolder[0]);
        });

        JButton refreshButton = new JButton("Osveži");
        refreshButton.addActionListener(e -> refreshHolder[0].run());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(refreshButton);
        buttons.add(addButton);
        buttons.add(editButton);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);

        refreshHolder[0].run();
        return panel;
    }

    private void openModelDialog(VehicleModel existing, Runnable onSave) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                existing == null ? "Novi model" : "Izmena modela", true);
        dialog.setSize(380, 250);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField manufacturerField = new JTextField(existing != null ? existing.getManufacturer() : "", 16);
        JTextField nameField = new JTextField(existing != null ? existing.getName() : "", 16);

        List<VehicleCategory> categories = vehicleManager.getAllCategories();
        JComboBox<VehicleCategory> categoryCombo = new JComboBox<>(categories.toArray(new VehicleCategory[0]));
        if (existing != null) {
            categories.stream()
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
                JOptionPane.showMessageDialog(dialog, "Prvo dodajte bar jednu kategoriju vozila.", "Greška", JOptionPane.ERROR_MESSAGE);
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
            onSave.run();
        });

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        form.add(saveButton, gbc);

        dialog.setContentPane(form);
        dialog.setVisible(true);
    }

    // ==================== PRIMERCI VOZILA ====================

    private JPanel buildVehiclesTab() {
        DefaultTableModel tableModel = new DefaultTableModel(
                new Object[]{"Model", "Registracija", "Kilometraža", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(26);

        Runnable[] refreshHolder = new Runnable[1];
        List<Vehicle> allVehicles = new java.util.ArrayList<>();

        refreshHolder[0] = () -> {
            tableModel.setRowCount(0);
            allVehicles.clear();
            for (VehicleModel m : vehicleManager.getAllModels()) {
                for (Vehicle v : vehicleManager.getVehiclesForModel(m.getId())) {
                    allVehicles.add(v);
                    tableModel.addRow(new Object[]{
                            m.getFullName(),
                            v.getLicensePlate(),
                            v.getMileage(),
                            v.getStatus() == VehicleStatus.AVAILABLE ? "DOSTUPNO" : "IZDATO"
                    });
                }
            }
        };

        JButton addButton = new JButton("Dodaj vozilo");
        addButton.addActionListener(e -> openVehicleDialog(null, refreshHolder[0]));

        JButton editButton = new JButton("Izmeni");
        editButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { warnNoSelection(); return; }
            openVehicleDialog(allVehicles.get(row), refreshHolder[0]);
        });

        JButton refreshButton = new JButton("Osveži");
        refreshButton.addActionListener(e -> refreshHolder[0].run());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(refreshButton);
        buttons.add(addButton);
        buttons.add(editButton);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);

        refreshHolder[0].run();
        return panel;
    }

    private void openVehicleDialog(Vehicle existing, Runnable onSave) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                existing == null ? "Novo vozilo" : "Izmena vozila", true);
        dialog.setSize(380, 250);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        List<VehicleModel> models = vehicleManager.getAllModels();
        JComboBox<VehicleModel> modelCombo = new JComboBox<>(models.toArray(new VehicleModel[0]));
        if (existing != null) {
            models.stream().filter(m -> m.getId().equals(existing.getModelId()))
                    .findFirst().ifPresent(modelCombo::setSelectedItem);
        }

        JTextField plateField = new JTextField(existing != null ? existing.getLicensePlate() : "", 16);
        JTextField mileageField = new JTextField(existing != null ? String.valueOf(existing.getMileage()) : "0", 16);
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
                JOptionPane.showMessageDialog(dialog, "Prvo dodajte bar jedan model vozila.", "Greška", JOptionPane.ERROR_MESSAGE);
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
                onSave.run();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Kilometraža mora biti ceo broj.", "Greška", JOptionPane.ERROR_MESSAGE);
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
