import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class UserForm extends JFrame {
    public JPanel userPanel;
    private JTable userTable;
    private JTextField tfUsername, tfName, tfLastName, tfEmail, tfPhone, tfPassword;
    private JButton btnAdd, btnUpdate, btnDelete, btnRefresh;

    private final String DB_URL = "jdbc:mysql://localhost:3306/MyStore?serverTimezone=UTC";
    private final String USERNAME = "root";
    private final String PASSWORD = "";

    public UserForm() {
        setTitle("User Management");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        userPanel = new JPanel(new BorderLayout(10, 10));
        setContentPane(userPanel);

        // Table setup
        String[] columns = {"Username", "Name", "Last Name", "Email", "Phone"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false; // no editing directly in table
            }
        };
        userTable = new JTable(model);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userPanel.add(new JScrollPane(userTable), BorderLayout.CENTER);

        // Form fields panel below table
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));

        tfUsername = new JTextField();
        tfName = new JTextField();
        tfLastName = new JTextField();
        tfEmail = new JTextField();
        tfPhone = new JTextField();
        tfPassword = new JTextField();

        formPanel.add(new JLabel("Username:"));
        formPanel.add(tfUsername);
        formPanel.add(new JLabel("Name:"));
        formPanel.add(tfName);
        formPanel.add(new JLabel("Last Name:"));
        formPanel.add(tfLastName);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(tfEmail);
        formPanel.add(new JLabel("Phone:"));
        formPanel.add(tfPhone);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(tfPassword);

        userPanel.add(formPanel, BorderLayout.SOUTH);

        // Buttons panel on the right
        JPanel buttonsPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        btnAdd = new JButton("Add");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Delete");
        btnRefresh = new JButton("Refresh");

        buttonsPanel.add(btnAdd);
        buttonsPanel.add(btnUpdate);
        buttonsPanel.add(btnDelete);
        buttonsPanel.add(btnRefresh);

        userPanel.add(buttonsPanel, BorderLayout.EAST);

        // cargar usuarios
        loadUsers();

        // cargar data al seleccionar row
        userTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && userTable.getSelectedRow() != -1) {
                int row = userTable.getSelectedRow();
                tfUsername.setText(userTable.getValueAt(row, 0).toString());
                tfName.setText(userTable.getValueAt(row, 1).toString());
                tfLastName.setText(userTable.getValueAt(row, 2).toString());
                tfEmail.setText(userTable.getValueAt(row, 3).toString());
                tfPhone.setText(userTable.getValueAt(row, 4).toString());
                tfPassword.setText(""); // never show password
                tfUsername.setEnabled(false); // username not editable after selection
            }
        });

        // Button actions
        btnAdd.addActionListener(e -> addUser());
        btnUpdate.addActionListener(e -> updateUser());
        btnDelete.addActionListener(e -> deleteUser());
        btnRefresh.addActionListener(e -> {
            clearFields();
            loadUsers();
        });
    }

    private void loadUsers() {
        DefaultTableModel model = (DefaultTableModel) userTable.getModel();
        model.setRowCount(0);

        try (Connection con = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT username, name, lastName, email, phone FROM users";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("username"),
                        rs.getString("name"),
                        rs.getString("lastName"),
                        rs.getString("email"),
                        rs.getString("phone")
                });
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading users.");
        }
    }

    private void addUser() {
        String username = tfUsername.getText().trim();
        String name = tfName.getText().trim();
        String lastName = tfLastName.getText().trim();
        String email = tfEmail.getText().trim();
        String phone = tfPhone.getText().trim();
        String password = tfPassword.getText().trim();

        if (username.isEmpty() || name.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields.");
            return;
        }

        try (Connection con = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
            String sql = "INSERT INTO users (username, name, lastName, email, phone, password) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, name);
            ps.setString(3, lastName);
            ps.setString(4, email);
            ps.setString(5, phone);
            ps.setString(6, password);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "User added successfully!");
            clearFields();
            loadUsers();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding user. Username or email might be already used.");
        }
    }

    private void updateUser() {
        String username = tfUsername.getText().trim();
        String name = tfName.getText().trim();
        String lastName = tfLastName.getText().trim();
        String email = tfEmail.getText().trim();
        String phone = tfPhone.getText().trim();
        String password = tfPassword.getText().trim();

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a user to update.");
            return;
        }

        try (Connection con = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
            String sql;
            PreparedStatement ps;

            if (password.isEmpty()) {
                sql = "UPDATE users SET name=?, lastName=?, email=?, phone=? WHERE username=?";
                ps = con.prepareStatement(sql);
                ps.setString(1, name);
                ps.setString(2, lastName);
                ps.setString(3, email);
                ps.setString(4, phone);
                ps.setString(5, username);
            } else {
                sql = "UPDATE users SET name=?, lastName=?, email=?, phone=?, password=? WHERE username=?";
                ps = con.prepareStatement(sql);
                ps.setString(1, name);
                ps.setString(2, lastName);
                ps.setString(3, email);
                ps.setString(4, phone);
                ps.setString(5, password);
                ps.setString(6, username);
            }

            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "User updated successfully!");
                clearFields();
                loadUsers();
                tfUsername.setEnabled(true);
            } else {
                JOptionPane.showMessageDialog(this, "Update failed, user not found.");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating user.");
        }
    }

    private void deleteUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a user to delete.");
            return;
        }

        String username = userTable.getValueAt(selectedRow, 0).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete user: " + username + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection con = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
                String sql = "DELETE FROM users WHERE username=?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, username);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "User deleted successfully!");
                clearFields();
                loadUsers();

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting user.");
            }
        }
    }

    private void clearFields() {
        tfUsername.setText("");
        tfName.setText("");
        tfLastName.setText("");
        tfEmail.setText("");
        tfPhone.setText("");
        tfPassword.setText("");
        userTable.clearSelection();
        tfUsername.setEnabled(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UserForm uf = new UserForm();
            uf.setVisible(true);
        });
    }
}
