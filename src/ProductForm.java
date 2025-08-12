import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ProductForm extends JFrame {
    public JPanel productPanel;
    private JTable productTable;
    private JTextField tfNombre, tfMarca, tfCategoria, tfPrecio, tfCantidad;
    private JButton btnNew, btnSave, btnDelete;

    private final String DB_URL = "jdbc:mysql://localhost:3306/MyStore?serverTimezone=UTC";
    private final String USERNAME = "root";
    private final String PASSWORD = "";

    private int selectedProductId = -1;

    public ProductForm() {
        setTitle("Product Management");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        productPanel = new JPanel(new BorderLayout(10,10));
        setContentPane(productPanel);


        String[] columns = {"ID", "Name", "Brand", "Category", "Price", "Quantity"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        productTable = new JTable(model);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productPanel.add(new JScrollPane(productTable), BorderLayout.CENTER);


        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        tfNombre = new JTextField();
        tfMarca = new JTextField();
        tfCategoria = new JTextField();
        tfPrecio = new JTextField();
        tfCantidad = new JTextField();

        formPanel.add(new JLabel("Name:"));
        formPanel.add(tfNombre);
        formPanel.add(new JLabel("Brand:"));
        formPanel.add(tfMarca);
        formPanel.add(new JLabel("Category:"));
        formPanel.add(tfCategoria);
        formPanel.add(new JLabel("Price:"));
        formPanel.add(tfPrecio);
        formPanel.add(new JLabel("Quantity:"));
        formPanel.add(tfCantidad);

        productPanel.add(formPanel, BorderLayout.SOUTH);


        JPanel buttonsPanel = new JPanel(new GridLayout(3,1, 10, 10));
        btnNew = new JButton("New");
        btnSave = new JButton("Save");
        btnDelete = new JButton("Delete");
        buttonsPanel.add(btnNew);
        buttonsPanel.add(btnSave);
        buttonsPanel.add(btnDelete);

        productPanel.add(buttonsPanel, BorderLayout.EAST);

        loadProducts();


        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && productTable.getSelectedRow() != -1) {
                int row = productTable.getSelectedRow();
                selectedProductId = Integer.parseInt(productTable.getValueAt(row, 0).toString());
                tfNombre.setText(productTable.getValueAt(row, 1).toString());
                tfMarca.setText(productTable.getValueAt(row, 2).toString());
                tfCategoria.setText(productTable.getValueAt(row, 3).toString());
                tfPrecio.setText(productTable.getValueAt(row, 4).toString());
                tfCantidad.setText(productTable.getValueAt(row, 5).toString());
            }
        });


        btnNew.addActionListener(e -> {
            NewProductDialog dialog = new NewProductDialog(this);
            dialog.setVisible(true);

            if (dialog.isSucceeded()) {
                try (Connection con = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
                    String sql = "INSERT INTO productos (nombre, marca, categoria, precio, cantidad) VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setString(1, dialog.getNombre());
                    ps.setString(2, dialog.getMarca());
                    ps.setString(3, dialog.getCategoria());
                    ps.setDouble(4, dialog.getPrecio());
                    ps.setInt(5, dialog.getCantidad());
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Product added successfully!");
                    loadProducts();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error adding product.");
                }
            }
        });


        btnSave.addActionListener(e -> {
            if (selectedProductId == -1) {
                JOptionPane.showMessageDialog(this, "Select a product to save changes.");
                return;
            }
            String nombre = tfNombre.getText().trim();
            String marca = tfMarca.getText().trim();
            String categoria = tfCategoria.getText().trim();
            String precioStr = tfPrecio.getText().trim();
            String cantidadStr = tfCantidad.getText().trim();

            if (nombre.isEmpty() || precioStr.isEmpty() || cantidadStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name, Price and Quantity are required.");
                return;
            }

            double precio;
            int cantidad;
            try {
                precio = Double.parseDouble(precioStr);
                cantidad = Integer.parseInt(cantidadStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Price must be a number and Quantity must be an integer.");
                return;
            }

            try (Connection con = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
                String sql = "UPDATE productos SET nombre=?, marca=?, categoria=?, precio=?, cantidad=? WHERE id=?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, nombre);
                ps.setString(2, marca);
                ps.setString(3, categoria);
                ps.setDouble(4, precio);
                ps.setInt(5, cantidad);
                ps.setInt(6, selectedProductId);
                int updated = ps.executeUpdate();
                if (updated > 0) {
                    JOptionPane.showMessageDialog(this, "Product updated successfully!");
                    loadProducts();
                } else {
                    JOptionPane.showMessageDialog(this, "Update failed.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating product.");
            }
        });


        btnDelete.addActionListener(e -> {
            if (selectedProductId == -1) {
                JOptionPane.showMessageDialog(this, "Select a product to delete.");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this product?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection con = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
                    String sql = "DELETE FROM productos WHERE id=?";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setInt(1, selectedProductId);
                    int deleted = ps.executeUpdate();
                    if (deleted > 0) {
                        JOptionPane.showMessageDialog(this, "Product deleted successfully!");
                        clearFields();
                        loadProducts();
                        selectedProductId = -1;
                    } else {
                        JOptionPane.showMessageDialog(this, "Delete failed.");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error deleting product.");
                }
            }
        });
    }

    private void loadProducts() {
        DefaultTableModel model = (DefaultTableModel) productTable.getModel();
        model.setRowCount(0);

        try (Connection con = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT * FROM productos";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("marca"),
                        rs.getString("categoria"),
                        rs.getBigDecimal("precio"),
                        rs.getInt("cantidad")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading products.");
        }
    }

    private void clearFields() {
        tfNombre.setText("");
        tfMarca.setText("");
        tfCategoria.setText("");
        tfPrecio.setText("");
        tfCantidad.setText("");
    }

    // nuevo producto popup
    private static class NewProductDialog extends JDialog {
        private JTextField tfNombre, tfMarca, tfCategoria, tfPrecio, tfCantidad;
        private boolean succeeded;

        public NewProductDialog(JFrame parent) {
            super(parent, "Add New Product", true);
            setLayout(new GridLayout(6, 2, 10, 10));

            add(new JLabel("Name:"));
            tfNombre = new JTextField();
            add(tfNombre);

            add(new JLabel("Brand:"));
            tfMarca = new JTextField();
            add(tfMarca);

            add(new JLabel("Category:"));
            tfCategoria = new JTextField();
            add(tfCategoria);

            add(new JLabel("Price:"));
            tfPrecio = new JTextField();
            add(tfPrecio);

            add(new JLabel("Quantity:"));
            tfCantidad = new JTextField();
            add(tfCantidad);

            JButton btnAdd = new JButton("Add");
            JButton btnCancel = new JButton("Cancel");
            add(btnAdd);
            add(btnCancel);

            btnAdd.addActionListener(e -> {
                if (validateFields()) {
                    succeeded = true;
                    setVisible(false);
                }
            });

            btnCancel.addActionListener(e -> {
                succeeded = false;
                setVisible(false);
            });

            pack();
            setLocationRelativeTo(parent);
        }

        private boolean validateFields() {
            if (tfNombre.getText().trim().isEmpty() ||
                    tfPrecio.getText().trim().isEmpty() ||
                    tfCantidad.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name, Price and Quantity are required.");
                return false;
            }
            try {
                Double.parseDouble(tfPrecio.getText().trim());
                Integer.parseInt(tfCantidad.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Price must be a number and Quantity must be an integer.");
                return false;
            }
            return true;
        }

        public String getNombre() { return tfNombre.getText().trim(); }
        public String getMarca() { return tfMarca.getText().trim(); }
        public String getCategoria() { return tfCategoria.getText().trim(); }
        public double getPrecio() { return Double.parseDouble(tfPrecio.getText().trim()); }
        public int getCantidad() { return Integer.parseInt(tfCantidad.getText().trim()); }
        public boolean isSucceeded() { return succeeded; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ProductForm pf = new ProductForm();
            pf.setVisible(true);
        });
    }
}
