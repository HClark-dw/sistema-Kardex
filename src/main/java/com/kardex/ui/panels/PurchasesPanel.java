package com.kardex.ui.panels;

import com.kardex.dao.ProductDAO;
import com.kardex.dao.PurchaseDAO;
import com.kardex.dao.SupplierDAO;
import com.kardex.model.Product;
import com.kardex.model.Purchase;
import com.kardex.model.PurchaseDetail;
import com.kardex.model.Supplier;
import com.kardex.model.User;
import com.kardex.util.Exporter;
import com.kardex.util.FormatUtils;
import com.kardex.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PurchasesPanel extends JPanel {

    private final User currentUser;
    private final PurchaseDAO purchaseDAO = new PurchaseDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final ProductDAO productDAO = new ProductDAO();

    // Componentes Formulario Nueva Compra
    private JComboBox<Supplier> cmbSuppliers;
    private JComboBox<String> cmbDocType;
    private JTextField txtDocNumber;
    private JComboBox<Product> cmbProducts;
    private JTextField txtQuantity;
    private JTextField txtCost;
    private JTable tableDetails;
    private DefaultTableModel modelDetails;
    private JLabel lblTotal;

    private final List<PurchaseDetail> currentDetails = new ArrayList<>();

    // Componentes Historial
    private JTable tableHistory;
    private DefaultTableModel modelHistory;
    private List<Purchase> currentPurchasesList;

    public PurchasesPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(10, 10));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BOLD);

        tabbedPane.addTab("📥 Registrar Nueva Compra (Entrada Kardex)", createNewPurchasePanel());
        tabbedPane.addTab("📋 Historial de Compras", createHistoryPanel());

        add(tabbedPane, BorderLayout.CENTER);

        loadSuppliersAndProducts();
        loadHistory();
    }

    private JPanel createNewPurchasePanel() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        // 1. Datos del Comprobante y Proveedor
        JPanel topCard = new JPanel(new GridBagLayout());
        UITheme.styleCard(topCard);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 6, 5, 6);

        cmbSuppliers = new JComboBox<>();
        cmbDocType = new JComboBox<>(new String[]{"Factura", "Boleta de Compra", "Guía de Remisión", "Nota de Entrada"});
        txtDocNumber = new JTextField("F001-");

        gbc.gridx = 0; gbc.gridy = 0; topCard.add(new JLabel("Proveedor:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; topCard.add(cmbSuppliers, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0; topCard.add(new JLabel("Tipo Doc:"), gbc);
        gbc.gridx = 3; topCard.add(cmbDocType, gbc);

        gbc.gridx = 4; gbc.gridy = 0; topCard.add(new JLabel("N° Serie-Doc:"), gbc);
        gbc.gridx = 5; gbc.weightx = 0.5; topCard.add(txtDocNumber, gbc);

        // 2. Selector de Producto y Cantidad
        JPanel itemCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        UITheme.styleCard(itemCard);

        cmbProducts = new JComboBox<>();
        cmbProducts.setPreferredSize(new Dimension(340, 32));
        cmbProducts.addActionListener(e -> {
            Product sel = (Product) cmbProducts.getSelectedItem();
            if (sel != null && sel.getCostoPromedio() != null) {
                txtCost.setText(sel.getCostoPromedio().toString());
            }
        });

        txtQuantity = new JTextField("1.00", 6);
        txtCost = new JTextField("0.00", 8);

        JButton btnAddItem = UITheme.createPrimaryButton("＋ Agregar a Lista");
        btnAddItem.addActionListener(e -> addItemToPurchase());

        itemCard.add(new JLabel("Producto:"));
        itemCard.add(cmbProducts);
        itemCard.add(new JLabel("Cantidad:"));
        itemCard.add(txtQuantity);
        itemCard.add(new JLabel("Costo Unitario ($):"));
        itemCard.add(txtCost);
        itemCard.add(btnAddItem);

        JPanel headerContainer = new JPanel();
        headerContainer.setLayout(new BoxLayout(headerContainer, BoxLayout.Y_AXIS));
        headerContainer.setOpaque(false);
        headerContainer.add(topCard);
        headerContainer.add(Box.createRigidArea(new Dimension(0, 8)));
        headerContainer.add(itemCard);

        panel.add(headerContainer, BorderLayout.NORTH);

        // 3. Tabla de Items Agregados
        String[] cols = {"ID Prod", "Código", "Descripción del Producto", "Cantidad", "Costo Unitario", "Subtotal"};
        modelDetails = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableDetails = new JTable(modelDetails);
        JScrollPane scrollTable = new JScrollPane(tableDetails);
        UITheme.styleCard(scrollTable);
        panel.add(scrollTable, BorderLayout.CENTER);

        // 4. Pie con Total y Botón de Registro
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JButton btnRemoveItem = UITheme.createDangerButton("Quitar Item Seleccionado");
        btnRemoveItem.addActionListener(e -> removeSelectedItem());

        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        totalPanel.setOpaque(false);

        lblTotal = new JLabel("TOTAL COMPRA: $ 0.00");
        lblTotal.setFont(UITheme.FONT_TITLE);
        lblTotal.setForeground(UITheme.DANGER);

        JButton btnSubmit = UITheme.createSuccessButton("💾 Procesar y Registrar Compra");
        btnSubmit.setPreferredSize(new Dimension(240, 38));
        btnSubmit.addActionListener(e -> submitPurchase());

        totalPanel.add(lblTotal);
        totalPanel.add(btnSubmit);

        footer.add(btnRemoveItem, BorderLayout.WEST);
        footer.add(totalPanel, BorderLayout.EAST);

        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        // Toolbar
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setOpaque(false);

        JButton btnReload = new JButton("↻ Actualizar Historial");
        btnReload.addActionListener(e -> loadHistory());

        JButton btnViewDetails = UITheme.createPrimaryButton("👁 Ver Detalle de Compra");
        btnViewDetails.addActionListener(e -> showPurchaseDetailsModal());

        JButton btnExport = new JButton("📥 Exportar CSV");
        btnExport.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("historial_compras.csv"));
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    Exporter.exportTableToCSV(tableHistory, chooser.getSelectedFile());
                    JOptionPane.showMessageDialog(this, "Historial exportado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error al exportar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        bar.add(btnReload);
        bar.add(btnViewDetails);
        bar.add(btnExport);

        panel.add(bar, BorderLayout.NORTH);

        String[] cols = {"ID Compra", "Fecha y Hora", "Tipo Comprobante", "N° Serie/Doc", "Proveedor", "Registrado Por", "Total Compra"};
        modelHistory = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableHistory = new JTable(modelHistory);
        tableHistory.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(tableHistory);
        UITheme.styleCard(scroll);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    public void loadSuppliersAndProducts() {
        try {
            cmbSuppliers.removeAllItems();
            for (Supplier s : supplierDAO.listAll()) {
                cmbSuppliers.addItem(s);
            }

            cmbProducts.removeAllItems();
            for (Product p : productDAO.listAll("")) {
                cmbProducts.addItem(p);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar proveedores o productos: " + e.getMessage());
        }
    }

    private void addItemToPurchase() {
        Product p = (Product) cmbProducts.getSelectedItem();
        if (p == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal qty = FormatUtils.parseBigDecimal(txtQuantity.getText(), BigDecimal.ZERO);
        BigDecimal cost = FormatUtils.parseBigDecimal(txtCost.getText(), BigDecimal.ZERO);

        if (qty.compareTo(BigDecimal.ZERO) <= 0 || cost.compareTo(BigDecimal.ZERO) < 0) {
            JOptionPane.showMessageDialog(this, "Ingrese una cantidad válida mayor a 0 y un costo válido.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        PurchaseDetail detail = new PurchaseDetail(p.getIdProducto(), p.getCodigo(), p.getNombre(), qty, cost);
        currentDetails.add(detail);
        refreshDetailsTable();

        txtQuantity.setText("1.00");
    }

    private void removeSelectedItem() {
        int sel = tableDetails.getSelectedRow();
        if (sel >= 0 && sel < currentDetails.size()) {
            currentDetails.remove(sel);
            refreshDetailsTable();
        }
    }

    private void refreshDetailsTable() {
        modelDetails.setRowCount(0);
        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseDetail d : currentDetails) {
            modelDetails.addRow(new Object[]{
                d.getIdProducto(),
                d.getProductoCodigo(),
                d.getProductoNombre(),
                FormatUtils.formatQuantity(d.getCantidad()),
                FormatUtils.formatUnitCost(d.getCostoUnitario()),
                FormatUtils.formatMoney(d.getSubtotal())
            });
            total = total.add(d.getSubtotal());
        }
        lblTotal.setText("TOTAL COMPRA: " + FormatUtils.formatMoney(total));
    }

    private void submitPurchase() {
        Supplier supp = (Supplier) cmbSuppliers.getSelectedItem();
        String docNum = txtDocNumber.getText().trim();
        if (supp == null || docNum.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un proveedor e ingrese el número de comprobante.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (currentDetails.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Agregue al menos un producto a la compra.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Purchase purchase = new Purchase();
        purchase.setIdProveedor(supp.getIdProveedor());
        purchase.setIdUsuario(currentUser != null ? currentUser.getIdUsuario() : 1);
        purchase.setTipoComprobante((String) cmbDocType.getSelectedItem());
        purchase.setSerieNumero(docNum);
        purchase.setFecha(LocalDateTime.now());
        purchase.setDetalles(new ArrayList<>(currentDetails));
        purchase.setObservaciones("Compra registrada vía Desktop");

        try {
            if (purchaseDAO.registerPurchase(purchase)) {
                JOptionPane.showMessageDialog(
                    this,
                    "¡Compra registrada exitosamente!\n" +
                    "Los stocks y los costos promedio en el Kardex han sido actualizados con el método promedio ponderado móvil.",
                    "Compra Exitosa",
                    JOptionPane.INFORMATION_MESSAGE
                );

                // Limpiar formulario
                currentDetails.clear();
                refreshDetailsTable();
                txtDocNumber.setText("F001-");

                // Recargar historial y productos
                loadHistory();
                loadSuppliersAndProducts();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al registrar la compra: " + ex.getMessage(), "Error Transaccional", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadHistory() {
        SwingWorker<List<Purchase>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Purchase> doInBackground() throws Exception {
                return purchaseDAO.listAll(null, null);
            }

            @Override
            protected void done() {
                try {
                    currentPurchasesList = get();
                    modelHistory.setRowCount(0);
                    for (Purchase p : currentPurchasesList) {
                        modelHistory.addRow(new Object[]{
                            p.getIdCompra(),
                            FormatUtils.formatDateTime(p.getFecha()),
                            p.getTipoComprobante(),
                            p.getSerieNumero(),
                            p.getProveedorNombre(),
                            p.getUsuarioNombre(),
                            FormatUtils.formatMoney(p.getTotal())
                        });
                    }
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    private void showPurchaseDetailsModal() {
        int sel = tableHistory.getSelectedRow();
        if (sel == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una compra del historial para ver su detalle.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Purchase p = currentPurchasesList.get(sel);
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Detalle de Compra #" + p.getIdCompra() + " - " + p.getSerieNumero(), true);
        dlg.setLayout(new BorderLayout(12, 12));
        dlg.setSize(620, 400);
        dlg.setLocationRelativeTo(this);

        String[] cols = {"Código", "Producto", "Cantidad", "Costo Unitario", "Subtotal"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        try {
            List<PurchaseDetail> details = purchaseDAO.getDetails(p.getIdCompra());
            for (PurchaseDetail d : details) {
                model.addRow(new Object[]{
                    d.getProductoCodigo(),
                    d.getProductoNombre(),
                    FormatUtils.formatQuantity(d.getCantidad()),
                    FormatUtils.formatUnitCost(d.getCostoUnitario()),
                    FormatUtils.formatMoney(d.getSubtotal())
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(dlg, "Error al consultar detalles: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);
        dlg.add(scroll, BorderLayout.CENTER);

        JPanel pnlTotal = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 10));
        JLabel lblTot = new JLabel("TOTAL COMPRA: " + FormatUtils.formatMoney(p.getTotal()));
        lblTot.setFont(UITheme.FONT_TITLE);
        lblTot.setForeground(UITheme.DANGER);
        pnlTotal.add(lblTot);
        dlg.add(pnlTotal, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }
}
