package gui;

import gui.panels.AdminDashboardPanel;
import gui.panels.AgentDashboardPanel;
import gui.panels.ClientDashboardPanel;
import model.User;
import model.enums.UserRole;
import util.AppContext;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private final User currentUser;

    public MainFrame(User currentUser) {
        this.currentUser = currentUser;

        setTitle("Rent-A-Car | " + currentUser.getFullName() + " (" + currentUser.getRole() + ")");
        setSize(1000, 650);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initLayout();
    }

    private void initLayout() {
        JPanel root = new JPanel(new BorderLayout());

        root.add(buildTopBar(), BorderLayout.NORTH);
        root.add(buildDashboardForRole(), BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        topBar.setBackground(new Color(40, 44, 52));

        JLabel welcomeLabel = new JLabel("Dobrodošli, " + currentUser.getFullName());
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(welcomeLabel.getFont().deriveFont(Font.BOLD, 14f));

        JButton logoutButton = new JButton("Odjavi se");
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> handleLogout());

        topBar.add(welcomeLabel, BorderLayout.WEST);
        topBar.add(logoutButton, BorderLayout.EAST);

        return topBar;
    }

    private JComponent buildDashboardForRole() {
        UserRole role = currentUser.getRole();

        return switch (role) {
            case ADMINISTRATOR -> new AdminDashboardPanel(currentUser);
            case AGENT -> new AgentDashboardPanel(currentUser);
            case CLIENT -> new ClientDashboardPanel(currentUser);
        };
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Da li ste sigurni da želite da se odjavite?",
                "Potvrda odjave",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            AppContext.getInstance().getAuthService().logout();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }
}
