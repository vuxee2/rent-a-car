package gui.forms;

import manager.PricelistManager;
import model.Pricelist;
import model.User;
import util.AppContext;
import util.DateUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;


public class PricelistPanel extends JPanel {

    private final User admin;
    private final PricelistManager pricelistManager;

    private JLabel currentSummaryLabel;
    private DefaultTableModel historyTableModel;

    public PricelistPanel(User admin) {
        this.admin = admin;
        this.pricelistManager = AppContext.getInstance().getPricelistManager();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(buildCurrentCard(), BorderLayout.NORTH);
        add(buildHistoryTable(), BorderLayout.CENTER);
        add(buildNewPricelistForm(), BorderLayout.SOUTH);

        refresh();
    }

    private JPanel buildCurrentCard() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Trenutno važeći cenovnik"));
        currentSummaryLabel = new JLabel();
        panel.add(currentSummaryLabel, BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane buildHistoryTable() {
        historyTableModel = new DefaultTableModel(
                new Object[]{"Važi od", "Važi do", "Trajanje najma", "Pretplata", "Kazna/dan",
                        "Popust STUDENT", "Popust PENZIONER", "Popust FIRMA"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(historyTableModel);
        table.setRowHeight(26);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createTitledBorder("Istorija cenovnika"));
        wrapper.add(table.getTableHeader(), BorderLayout.NORTH);
        wrapper.add(table, BorderLayout.CENTER);

        return new JScrollPane(wrapper);
    }

    private JPanel buildNewPricelistForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Definisanje novog cenovnika"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField defaultDaysField = new JTextField("3", 6);
        JTextField subscriptionPriceField = new JTextField("6000", 8);
        JTextField lateFeeField = new JTextField("5000", 8);
        JTextField studentDiscountField = new JTextField("10", 5);
        JTextField pensionerDiscountField = new JTextField("15", 5);
        JTextField companyDiscountField = new JTextField("5", 5);

        int col = 0;
        gbc.gridx = col++; gbc.gridy = 0;
        panel.add(new JLabel("Trajanje najma (dani):"), gbc);
        gbc.gridx = col++;
        panel.add(defaultDaysField, gbc);

        gbc.gridx = col++;
        panel.add(new JLabel("Cena pretplate:"), gbc);
        gbc.gridx = col++;
        panel.add(subscriptionPriceField, gbc);

        gbc.gridx = col++;
        panel.add(new JLabel("Kazna/dan:"), gbc);
        gbc.gridx = col++;
        panel.add(lateFeeField, gbc);

        col = 0;
        gbc.gridx = col++; gbc.gridy = 1;
        panel.add(new JLabel("Popust STUDENT (%):"), gbc);
        gbc.gridx = col++;
        panel.add(studentDiscountField, gbc);

        gbc.gridx = col++;
        panel.add(new JLabel("Popust PENSIONER (%):"), gbc);
        gbc.gridx = col++;
        panel.add(pensionerDiscountField, gbc);

        gbc.gridx = col++;
        panel.add(new JLabel("Popust COMPANY (%):"), gbc);
        gbc.gridx = col++;
        panel.add(companyDiscountField, gbc);

        JButton createButton = new JButton("Aktiviraj novi cenovnik");
        createButton.addActionListener(e -> {
            try {
                int defaultDays = Integer.parseInt(defaultDaysField.getText().trim());
                double subscriptionPrice = Double.parseDouble(subscriptionPriceField.getText().trim());
                double lateFee = Double.parseDouble(lateFeeField.getText().trim());
                double studentDiscount = Double.parseDouble(studentDiscountField.getText().trim());
                double pensionerDiscount = Double.parseDouble(pensionerDiscountField.getText().trim());
                double companyDiscount = Double.parseDouble(companyDiscountField.getText().trim());

                pricelistManager.createPricelist(defaultDays, subscriptionPrice, lateFee,
                        studentDiscount, pensionerDiscount, companyDiscount, admin.getId());

                JOptionPane.showMessageDialog(this, "Novi cenovnik je aktiviran.", "Uspešno", JOptionPane.INFORMATION_MESSAGE);
                refresh();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Sva polja moraju biti brojevi.", "Greška", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 6; gbc.anchor = GridBagConstraints.CENTER;
        panel.add(createButton, gbc);

        return panel;
    }

    private void refresh() {
        pricelistManager.getActivePricelist().ifPresentOrElse(p -> {
            currentSummaryLabel.setText(String.format(
                    "<html>Trajanje najma: <b>%d dana</b> &nbsp;|&nbsp; Pretplata: <b>%.2f RSD</b> "
                            + "&nbsp;|&nbsp; Kazna za kašnjenje: <b>%.2f RSD/dan</b><br>"
                            + "Popusti - Student: <b>%.0f%%</b>, Penzioner: <b>%.0f%%</b>, Firma: <b>%.0f%%</b></html>",
                    p.getDefaultRentalDays(), p.getAnnualSubscriptionPrice(), p.getLateReturnFeePerDay(),
                    p.getStudentDiscountPercent(), p.getPensionerDiscountPercent(), p.getCompanyDiscountPercent()
            ));
        }, () -> currentSummaryLabel.setText("Nema definisanog cenovnika. Kreirajte prvi cenovnik ispod."));

        historyTableModel.setRowCount(0);
        List<Pricelist> all = pricelistManager.getAllPricelists();
        for (Pricelist p : all) {
            historyTableModel.addRow(new Object[]{
                    DateUtil.format(p.getValidFrom()),
                    p.getValidTo() == null ? "i dalje" : DateUtil.format(p.getValidTo()),
                    p.getDefaultRentalDays() + " dana",
                    String.format("%.2f RSD", p.getAnnualSubscriptionPrice()),
                    String.format("%.2f RSD", p.getLateReturnFeePerDay()),
                    p.getStudentDiscountPercent() + "%",
                    p.getPensionerDiscountPercent() + "%",
                    p.getCompanyDiscountPercent() + "%"
            });
        }
    }
}
