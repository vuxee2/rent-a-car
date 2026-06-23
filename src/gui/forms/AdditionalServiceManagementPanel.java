package gui.forms;

import model.AdditionalService;
import model.enums.ChargeType;
import model.enums.ServiceType;
import repository.AdditionalServiceRepository;
import util.AppContext;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.UUID;


public class AdditionalServiceManagementPanel extends JPanel {

    private final AdditionalServiceRepository repo;

    private DefaultTableModel tableModel;
    private JTable table;
    private List<AdditionalService> currentServices;

    public AdditionalServiceManagementPanel() {
        this.repo = AppContext.getInstance().getAdditionalServiceRepository();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        add(buildTable(), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);
        refresh();
    }

    private JScrollPane buildTable() {
        tableModel = new DefaultTableModel(
                new Object[]{"Usluga", "Cena", "Način naplate"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return new JScrollPane(table);
    }

    private JPanel buildButtons() {
        JButton addButton = new JButton("Dodaj uslugu");
        addButton.addActionListener(e -> openDialog(null));

        JButton editButton = new JButton("Izmeni cenu");
        editButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Izaberite uslugu iz tabele.", "Napomena", JOptionPane.WARNING_MESSAGE);
                return;
            }
            openDialog(currentServices.get(row));
        });

        JButton deleteButton = new JButton("Obriši");
        deleteButton.setForeground(Color.RED);
        deleteButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Izaberite uslugu iz tabele.", "Napomena", JOptionPane.WARNING_MESSAGE);
                return;
            }
            AdditionalService selected = currentServices.get(row);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Da li ste sigurni da želite da obrišete uslugu \"" + selected.getDisplayName() + "\"?",
                    "Potvrda brisanja", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                repo.delete(selected.getId());
                refresh();
            }
        });

        JButton refreshButton = new JButton("Osveži");
        refreshButton.addActionListener(e -> refresh());

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.add(refreshButton);
        panel.add(addButton);
        panel.add(editButton);
        panel.add(deleteButton);
        return panel;
    }

    private void refresh() {
        currentServices = repo.findAll();
        tableModel.setRowCount(0);
        for (AdditionalService s : currentServices) {
            String chargeLabel = s.getChargeType() == ChargeType.PER_DAY ? "Po danu" : "Jednokratno";
            tableModel.addRow(new Object[]{
                    s.getDisplayName(),
                    String.format("%.2f RSD", s.getPrice()),
                    chargeLabel
            });
        }
    }

    private void openDialog(AdditionalService existing) {
        boolean isNew = existing == null;
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                isNew ? "Nova dodatna usluga" : "Izmena cene usluge", true);
        dialog.setSize(380, 230);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 7, 7, 7);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Tip usluge
        JComboBox<ServiceType> typeCombo = new JComboBox<>(ServiceType.values());
        if (existing != null) typeCombo.setSelectedItem(existing.getType());
        typeCombo.setEnabled(isNew);

        // Cena
        JTextField priceField = new JTextField(
                existing != null ? String.format("%.2f", existing.getPrice()) : "0", 10);

        // Nacin naplate
        JComboBox<ChargeType> chargeCombo = new JComboBox<>(ChargeType.values());
        if (existing != null) chargeCombo.setSelectedItem(existing.getChargeType());
        chargeCombo.setEnabled(isNew);
        chargeCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == ChargeType.ONE_TIME) setText("Jednokratno");
                else if (value == ChargeType.PER_DAY) setText("Po danu");
                return this;
            }
        });

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        form.add(new JLabel("Tip usluge:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        form.add(typeCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        form.add(new JLabel("Cena (RSD):"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        form.add(priceField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        form.add(new JLabel("Način naplate:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        form.add(chargeCombo, gbc);

        JButton saveButton = new JButton("Sačuvaj");
        saveButton.addActionListener(e -> {
            try {
                double price = Double.parseDouble(priceField.getText().trim().replace(",", "."));
                if (price < 0) {
                    JOptionPane.showMessageDialog(dialog, "Cena ne može biti negativna.", "Greška", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                AdditionalService service = new AdditionalService(
                        isNew ? UUID.randomUUID().toString() : existing.getId(),
                        (ServiceType) typeCombo.getSelectedItem(),
                        price,
                        (ChargeType) chargeCombo.getSelectedItem()
                );
                repo.save(service);
                dialog.dispose();
                refresh();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Cena mora biti broj.",
                        "Greška", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        form.add(saveButton, gbc);

        dialog.setContentPane(form);
        dialog.setVisible(true);
    }
}
