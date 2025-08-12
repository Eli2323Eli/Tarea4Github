import javax.swing.*;
import java.awt.*;

public class DashboardForm extends JFrame {
    private JPanel dashboardPanel;
    private JLabel lbAdmin;
    private JButton btnUser, btnProduct, btnLogOff;

    public DashboardForm(User loggedInUser) {
        setTitle("Dashboard");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(800, 600);
        setMinimumSize(new Dimension(500, 429));
        setLocationRelativeTo(null);


        dashboardPanel = new JPanel(new BorderLayout(20, 20));
        dashboardPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setContentPane(dashboardPanel);


        lbAdmin = new JLabel("User: " + loggedInUser.name);
        lbAdmin.setFont(new Font("Segoe UI", Font.BOLD, 18));
        dashboardPanel.add(lbAdmin, BorderLayout.NORTH);


        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        dashboardPanel.add(buttonsPanel, BorderLayout.CENTER);

        btnUser = new JButton("Manage Users");
        btnUser.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnUser.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btnUser.setMaximumSize(new Dimension(200, 50));

        btnProduct = new JButton("Manage Products");
        btnProduct.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnProduct.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btnProduct.setMaximumSize(new Dimension(200, 50));

        btnLogOff = new JButton("Log Off");
        btnLogOff.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogOff.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btnLogOff.setMaximumSize(new Dimension(200, 50));
        btnLogOff.setForeground(Color.RED);


        buttonsPanel.add(Box.createVerticalGlue());
        buttonsPanel.add(btnUser);
        buttonsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        buttonsPanel.add(btnProduct);
        buttonsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        buttonsPanel.add(btnLogOff);
        buttonsPanel.add(Box.createVerticalGlue());

        // Actions
        btnUser.addActionListener(e -> {
            UserForm userForm = new UserForm();
            userForm.setVisible(true);
        });

        btnProduct.addActionListener(e -> {
            ProductForm productForm = new ProductForm();
            productForm.setVisible(true);
        });

        btnLogOff.addActionListener(e -> {
            dispose();
            LoginForm loginForm = new LoginForm(null); // Pass null since LoginForm needs a JFrame
            loginForm.setVisible(true);
        });
    }
}
