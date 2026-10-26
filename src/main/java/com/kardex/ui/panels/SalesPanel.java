package com.kardex.ui.panels;

import com.kardex.dao.CustomerDAO;
import com.kardex.dao.ProductDAO;
import com.kardex.dao.SaleDAO;
import com.kardex.model.Customer;
import com.kardex.model.Product;
import com.kardex.model.Sale;
import com.kardex.model.SaleDetail;
import com.kardex.model.User;
import com.kardex.util.Exporter;
import com.kardex.util.FormatUtils;
import com.kardex.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SalesPanel extends JPanel {

    private final User currentUser;
    private final SaleDAO saleDAO = new SaleDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final ProductDAO productDAO = new ProductDAO();

    // Componentes Formulario Nueva Venta
    private JComboBox<Customer> cmbCustomers;
    private JComboBox<String> cmbDocType;
    private JTextField txtDocNumber;
    private JComboBox<Product> cmbProducts;
    private JLabel lblAvailableStock;
    private JTextField txtQuantity;
    private JTextField txtPrice;
    private JTable tableDetails;
    private DefaultTableModel modelDetails;
    private JLabel lblTotal;

    private final List<SaleDetail> currentDetails = new ArrayList<>();

    // Componentes Historial
    private JTable tableHistory;
    private DefaultTableModel modelHistory;
    private List<Sale> currentSalesList;

    public SalesPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(10, 10));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BOLD);

        tabbedPane.addTab("🛒 Registrar Nueva Venta (Salida Kardex)", createNewSalePanel());
        tabbedPane.addTab("📋 Historial de Ventas", createHistoryPanel());

        add(tabbedPane, BorderLayout.CENTER);

        loadCustomersAndProducts();
        loadHistory();
    }

    private JPanel createNewSalePanel() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        // 1. Datos del Comprobante y Cliente
        JPanel topCard = new JPanel(new GridBagLayout());
        UITheme.styleCard(topCard);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 6, 5, 6);

        cmbCustomers = new JComboBox<>();
        cmbDocType = new JComboBox<>(new String[]{"Boleta de Venta", "Factura", "Ticket", "Nota de Salida"});
        txtDocNumber = new JTextField("B001-");

        gbc.gridx = 0; gbc.gridy = 0; topCard.add(new JLabel("Cliente:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; topCard.add(cmbCustomers, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0; topCard.add(new JLabel("Tipo Doc:"), gbc);
        gbc.gridx = 3; topCard.add(cmbDocType, gbc);

        gbc.gridx = 4; gbc.gridy = 0; topCard.add(new JLabel("N° Serie-Doc:"), gbc);
        gbc.gridx = 5; gbc.weightx = 0.5; topCard.add(txtDocNumber, gbc);

        // 2. Selector de Producto, Stock y Precio
        JPanel itemCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        UITheme.styleCard(itemCard);

        cmbProducts = new JComboBox<>();
        cmbProducts.setPreferredSize(new Dimension(320, 32));
        lblAvailableStock = new JLabel("Stock: 0.00");
        lblAvailableStock.setFont(UITheme.FONT_BOLD);
        lblAvailableStock.setForeground(UITheme.PRIMARY);

        cmbProducts.addActionListener(e -> updateProductStockPreview());

        txtQuantity = new JTextField("1.00", 5);
        txtPrice = new JTextField("0.00", 7);

        JButton btnAddItem = UITheme.createPrimaryButton("＋ Agregar a Venta");
        btnAddItem.addActionListener(e -> addItemToSale());

        itemCard.add(new JLabel("Producto:"));
        itemCard.add(cmbProducts);
        itemCard.add(lblAvailableStock);
        itemCard.add(new JLabel("Cantidad:"));
        itemCard.add(txtQuantity);
        itemCard.add(new JLabel("Precio Venta ($):"));
        itemCard.add(txtPrice);
        itemCard.add(btnAddItem);

        JPanel headerContainer = new JPanel();
        headerContainer.setLayout(new BoxLayout(headerContainer, BoxLayout.Y_AXIS));
        headerContainer.setOpaque(false);
        headerContainer.add(topCard);
        headerContainer.add(Box.createRigidArea(new Dimension(0, 8)));
        headerContainer.add(itemCard);

        panel.add(headerContainer, BorderLayout.NORTH);

        // 3. Tabla de Items
        String[] cols = {"ID Prod", "Código", "Descripción del Producto", "Cantidad", "Precio Unitario", "Subtotal"};
        modelDetails = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableDetails = new JTable(modelDetails);
        JScrollPane scrollTable = new JScrollPane(tableDetails);
        UITheme.styleCard(scrollTable);
        panel.add(scrollTable, BorderLayout.CENTER);

        // 4. Pie con Total y Botón de Venta
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JButton btnRemoveItem = UITheme.createDangerButton("Quitar Item Seleccionado");
        btnRemoveItem.addActionListener(e -> removeSelectedItem());

        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        totalPanel.setOpaque(false);

        lblTotal = new JLabel("TOTAL VENTA: $ 0.00");
        lblTotal.setFont(UITheme.FONT_TITLE);
        lblTotal.setForeground(UITheme.SUCCESS);

        JButton btnSubmit = UITheme.createSuccessButton("💵 Procesar y Facturar Venta");
        btnSubmit.setPreferredSize(new Dimension(240, 38));
        btnSubmit.addActionListener(e -> submitSale());

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

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setOpaque(false);

        JButton btnReload = new JButton("↻ Actualizar Historial");
        btnReload.addActionListener(e -> loadHistory());

        JButton btnViewDetails = UITheme.createPrimaryButton("👁 Ver Detalle de Venta");
        btnViewDetails.addActionListener(e -> showSaleDetailsModal());

        JButton btnExport = new JButton("📥 Exportar CSV");
        btnExport.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("historial_ventas.csv"));
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

        String[] cols = {"ID Venta", "Fecha y Hora", "Tipo Comprobante", "N° Serie/Doc", "Cliente", "Total Venta", "Costo Mercadería", "Utilidad Bruta"};
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

    public void loadCustomersAndProducts() {
        try {
            cmbCustomers.removeAllItems();
            for (Customer c : customerDAO.listAll()) {
                cmbCustomers.addItem(c);
            }

            cmbProducts.removeAllItems();
            for (Product p : productDAO.listAll("")) {
                cmbProducts.addItem(p);
            }
            updateProductStockPreview();
        } catch (Exception e) {
            System.err.println("Error al cargar clientes o productos: " + e.getMessage());
        }
    }

    private void updateProductStockPreview() {
        Product sel = (Product) cmbProducts.getSelectedItem();
        if (sel != null) {
            lblAvailableStock.setText("Stock: " + FormatUtils.formatQuantity(sel.getStockActual()) + " " + sel.getUnidadMedida());
            if (sel.getPrecioVenta() != null) {
                txtPrice.setText(sel.getPrecioVenta().toString());
            }
        }
    }

    private void addItemToSale() {
        Product p = (Product) cmbProducts.getSelectedItem();
        if (p == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto para vender.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal qty = FormatUtils.parseBigDecimal(txtQuantity.getText(), BigDecimal.ZERO);
        BigDecimal price = FormatUtils.parseBigDecimal(txtPrice.getText(), BigDecimal.ZERO);

        if (qty.compareTo(BigDecimal.ZERO) <= 0 || price.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this, "Ingrese una cantidad y un precio mayores a cero.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validar stock disponible acumulado en la venta
        BigDecimal alreadyInCart = BigDecimal.ZERO;
        for (SaleDetail d : currentDetails) {
            if (d.getIdProducto() == p.getIdProducto()) {
                alreadyInCart = alreadyInCart.add(d.getCantidad());
            }
        }

        if (alreadyInCart.add(qty).compareTo(p.getStockActual()) > 0) {
            JOptionPane.showMessageDialog(
                this,
                String.format("¡Stock insuficiente! Disponible: %s %s. Ya agregados al carrito: %s %s.",
                        p.getStockActual(), p.getUnidadMedida(), alreadyInCart, p.getUnidadMedida()),
                "Stock Insuficiente",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        SaleDetail detail = new SaleDetail(p.getIdProducto(), p.getCodigo(), p.getNombre(), qty, price, p.getCostoPromedio());
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
        for (SaleDetail d : currentDetails) {
            modelDetails.addRow(new Object[]{
                d.getIdProducto(),
                d.getProductoCodigo(),
                d.getProductoNombre(),
                FormatUtils.formatQuantity(d.getCantidad()),
                FormatUtils.formatMoney(d.getPrecioUnitario()),
                FormatUtils.formatMoney(d.getSubtotal())
            });
            total = total.add(d.getSubtotal());
        }
        lblTotal.setText("TOTAL VENTA: " + FormatUtils.formatMoney(total));
    }

    private void submitSale() {
        Customer cust = (Customer) cmbCustomers.getSelectedItem();
        String docNum = txtDocNumber.getText().trim();
        if (cust == null || docNum.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente e ingrese el número de comprobante.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (currentDetails.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Agregue al menos un producto a la venta.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Sale sale = new Sale();
        sale.setIdCliente(cust.getIdCliente());
        sale.setIdUsuario(currentUser != null ? currentUser.getIdUsuario() : 1);
        sale.setTipoComprobante((String) cmbDocType.getSelectedItem());
        sale.setSerieNumero(docNum);
        sale.setFecha(LocalDateTime.now());
        sale.setDetalles(new ArrayList<>(currentDetails));
        sale.setObservaciones("Venta registrada vía Desktop");

        try {
            if (saleDAO.registerSale(sale)) {
                JOptionPane.showMessageDialog(
                    this,
                    "¡Venta registrada exitosamente!\n" +
                    "Se ha descontado el inventario y generado la salida correspondiente en el Kardex.",
                    "Venta Exitosa",
                    JOptionPane.INFORMATION_MESSAGE
                );

                // Limpiar formulario
                currentDetails.clear();
                refreshDetailsTable();
                txtDocNumber.setText("B001-");

                // Recargar historial y productos
                loadHistory();
                loadCustomersAndProducts();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al procesar la venta: " + ex.getMessage(), "Error Transaccional", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadHistory() {
        SwingWorker<List<Sale>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Sale> doInBackground() throws Exception {
                return saleDAO.listAll(null, null);
            }

            @Override
            protected void done() {
                try {
                    currentSalesList = get();
                    modelHistory.setRowCount(0);
                    for (Sale s : currentSalesList) {
                        modelHistory.addRow(new Object[]{
                            s.getIdVenta(),
                            FormatUtils.formatDateTime(s.getFecha()),
                            s.getTipoComprobante(),
                            s.getSerieNumero(),
                            s.getClienteNombre(),
                            FormatUtils.formatMoney(s.getTotal()),
                            FormatUtils.formatMoney(s.getCostoTotalEstimado()),
                            FormatUtils.formatMoney(s.getUtilidadBruta())
                        });
                    }
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    private void showSaleDetailsModal() {
        int sel = tableHistory.getSelectedRow();
        if (sel == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una venta del historial para ver su detalle.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Sale s = currentSalesList.get(sel);
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Detalle de Venta #" + s.getIdVenta() + " - " + s.getSerieNumero(), true);
        dlg.setLayout(new BorderLayout(12, 12));
        dlg.setSize(620, 400);
        dlg.setLocationRelativeTo(this);

        String[] cols = {"Código", "Producto", "Cantidad", "Precio Venta", "Costo Histórico", "Subtotal"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        try {
            List<SaleDetail> details = saleDAO.getDetails(s.getIdVenta());
            for (SaleDetail d : details) {
                model.addRow(new Object[]{
                    d.getProductoCodigo(),
                    d.getProductoNombre(),
                    FormatUtils.formatQuantity(d.getCantidad()),
                    FormatUtils.formatMoney(d.getPrecioUnitario()),
                    FormatUtils.formatUnitCost(d.getCostoUnitarioHistorico()),
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
        JLabel lblTot = new JLabel("TOTAL VENTA: " + FormatUtils.formatMoney(s.getTotal()));
        lblTot.setFont(UITheme.FONT_TITLE);
        lblTot.setForeground(UITheme.SUCCESS);
        pnlTotal.add(lblTot);
        dlg.add(pnlTotal, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }
}
