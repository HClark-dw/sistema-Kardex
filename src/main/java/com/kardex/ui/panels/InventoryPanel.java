package com.kardex.ui.panels;

import com.kardex.dao.CategoryDAO;
import com.kardex.dao.ProductDAO;
import com.kardex.model.Category;
import com.kardex.model.Product;
import com.kardex.util.Exporter;
import com.kardex.util.FormatUtils;
import com.kardex.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.util.List;

public class InventoryPanel extends JPanel {

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    private final JTable tableProducts;
    private final DefaultTableModel tableModel;
    private final JTextField txtSearch;
    private final JLabel lblSummary;

    private List<Product> currentProductList;

    public InventoryPanel() {
        setLayout(new BorderLayout(14, 14));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        // 1. Encabezado y Barra de Herramientas
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);
        JLabel lblTitle = new JLabel("Catálogo de Productos e Inventario Valorado");
        lblTitle.setFont(UITheme.FONT_TITLE);
        titleBar.add(lblTitle, BorderLayout.WEST);

        // Barra de búsqueda y botones
        JPanel toolbar = new JPanel(new BorderLayout(10, 10));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("Buscar:");
        lblSearch.setFont(UITheme.FONT_BOLD);
        txtSearch = new JTextField(22);
        txtSearch.putClientProperty("JTextField.placeholderText", "Código, nombre o categoría...");
        txtSearch.addActionListener(e -> loadProducts());

        JButton btnSearch = new JButton("🔍 Buscar");
        btnSearch.addActionListener(e -> loadProducts());

        JButton btnReset = new JButton("Limpiar");
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            loadProducts();
        });

        searchPanel.add(lblSearch);
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnReset);

        // Acciones
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionsPanel.setOpaque(false);

        JButton btnNew = UITheme.createPrimaryButton("＋ Nuevo Producto");
        btnNew.addActionListener(e -> openProductDialog(null));

        JButton btnEdit = new JButton("✏ Editar");
        btnEdit.setFont(UITheme.FONT_BOLD);
        btnEdit.addActionListener(e -> editSelectedProduct());

        JButton btnDelete = UITheme.createDangerButton("🗑 Eliminar");
        btnDelete.addActionListener(e -> deleteSelectedProduct());

        JButton btnExport = new JButton("📥 Exportar CSV");
        btnExport.setFont(UITheme.FONT_BOLD);
        btnExport.addActionListener(e -> exportToCSV());

        actionsPanel.add(btnNew);
        actionsPanel.add(btnEdit);
        actionsPanel.add(btnDelete);
        actionsPanel.add(btnExport);

        toolbar.add(searchPanel, BorderLayout.WEST);
        toolbar.add(actionsPanel, BorderLayout.EAST);

        topContainer.add(titleBar);
        topContainer.add(toolbar);
        add(topContainer, BorderLayout.NORTH);

        // 2. Tabla de Inventario
        String[] columns = {
            "ID", "Código / SKU", "Descripción del Producto", "Categoría",
            "U.M.", "Stock Actual", "Stock Mínimo", "Costo Promedio",
            "Precio Venta", "Valor Total en Inventario", "Estado Alerta"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tableProducts = new JTable(tableModel);
        tableProducts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableProducts.setAutoCreateRowSorter(true);

        // Renderer de alertas para Stock
        tableProducts.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String alerta = (String) table.getValueAt(row, 10);

                if ("CRÍTICO".equals(alerta)) {
                    if (column == 5 || column == 10) {
                        setForeground(UITheme.DANGER);
                        setFont(UITheme.FONT_BOLD);
                    } else {
                        setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                        setFont(UITheme.FONT_REGULAR);
                    }
                } else {
                    if (column == 10) {
                        setForeground(UITheme.SUCCESS);
                        setFont(UITheme.FONT_BOLD);
                    } else {
                        setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                        setFont(UITheme.FONT_REGULAR);
                    }
                }
                return c;
            }
        });

        JScrollPane scrollTable = new JScrollPane(tableProducts);
        UITheme.styleCard(scrollTable);
        add(scrollTable, BorderLayout.CENTER);

        // 3. Barra de Resumen en Pie de Página
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(6, 4, 4, 4));

        lblSummary = new JLabel("Cargando inventario...");
        lblSummary.setFont(UITheme.FONT_BOLD);
        footerPanel.add(lblSummary, BorderLayout.WEST);

        add(footerPanel, BorderLayout.SOUTH);

        // Cargar
        loadProducts();
    }

    public void loadProducts() {
        String filter = txtSearch.getText().trim();
        SwingWorker<List<Product>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Product> doInBackground() throws Exception {
                return productDAO.listAll(filter);
            }

            @Override
            protected void done() {
                try {
                    currentProductList = get();
                    tableModel.setRowCount(0);

                    BigDecimal totalValuation = BigDecimal.ZERO;
                    BigDecimal totalUnits = BigDecimal.ZERO;
                    int criticalCount = 0;

                    for (Product p : currentProductList) {
                        BigDecimal val = p.getValorTotalInventario();
                        totalValuation = totalValuation.add(val);
                        totalUnits = totalUnits.add(p.getStockActual());

                        boolean isCritical = p.isLowStock();
                        if (isCritical) criticalCount++;

                        tableModel.addRow(new Object[]{
                            p.getIdProducto(),
                            p.getCodigo(),
                            p.getNombre(),
                            p.getCategoriaNombre(),
                            p.getUnidadMedida(),
                            FormatUtils.formatQuantity(p.getStockActual()),
                            FormatUtils.formatQuantity(p.getStockMinimo()),
                            FormatUtils.formatUnitCost(p.getCostoPromedio()),
                            FormatUtils.formatMoney(p.getPrecioVenta()),
                            FormatUtils.formatMoney(val),
                            isCritical ? "CRÍTICO" : "NORMAL"
                        });
                    }

                    lblSummary.setText(String.format(
                        "Total: %d productos | Unidades en Almacén: %s | Valorización Total: %s | Alertas Stock Bajo: %d",
                        currentProductList.size(),
                        FormatUtils.formatQuantity(totalUnits),
                        FormatUtils.formatMoney(totalValuation),
                        criticalCount
                    ));

                } catch (Exception e) {
                    lblSummary.setText("Error al consultar productos: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void editSelectedProduct() {
        int selectedRow = tableProducts.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto de la tabla para editar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = tableProducts.convertRowIndexToModel(selectedRow);
        Product p = currentProductList.get(modelRow);
        openProductDialog(p);
    }

    private void deleteSelectedProduct() {
        int selectedRow = tableProducts.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto para eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = tableProducts.convertRowIndexToModel(selectedRow);
        Product p = currentProductList.get(modelRow);

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro de desactivar el producto: " + p.getNombre() + "?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (productDAO.delete(p.getIdProducto())) {
                    JOptionPane.showMessageDialog(this, "Producto desactivado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    loadProducts();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar producto: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openProductDialog(Product existing) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), existing == null ? "Nuevo Producto" : "Editar Producto", true);
        dlg.setLayout(new BorderLayout(14, 14));
        dlg.setSize(480, 520);
        dlg.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        JTextField txtCode = new JTextField();
        JTextField txtName = new JTextField();
        JComboBox<Category> cmbCategory = new JComboBox<>();
        JTextField txtUnit = new JTextField("UND");
        JTextField txtStockMin = new JTextField("5.00");
        JTextField txtStockInit = new JTextField("0.00");
        JTextField txtCostInit = new JTextField("0.00");
        JTextField txtPrice = new JTextField("0.00");

        // Cargar Categorías
        try {
            List<Category> cats = categoryDAO.listAll();
            for (Category c : cats) cmbCategory.addItem(c);
        } catch (Exception ignored) {}

        if (existing != null) {
            txtCode.setText(existing.getCodigo());
            txtName.setText(existing.getNombre());
            txtUnit.setText(existing.getUnidadMedida());
            txtStockMin.setText(existing.getStockMinimo().toString());
            txtPrice.setText(existing.getPrecioVenta().toString());
            txtStockInit.setText(existing.getStockActual().toString());
            txtStockInit.setEnabled(false); // El stock se modifica solo por compras/ventas
            txtCostInit.setText(existing.getCostoPromedio().toString());
            txtCostInit.setEnabled(false); // El costo se calcula por Kardex
            for (int i = 0; i < cmbCategory.getItemCount(); i++) {
                if (cmbCategory.getItemAt(i).getIdCategoria() == existing.getIdCategoria()) {
                    cmbCategory.setSelectedIndex(i);
                    break;
                }
            }
        }

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; form.add(new JLabel("Código / SKU:"), gbc);
        gbc.gridx = 1; form.add(txtCode, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row; form.add(new JLabel("Nombre / Descripción:"), gbc);
        gbc.gridx = 1; form.add(txtName, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row; form.add(new JLabel("Categoría:"), gbc);
        gbc.gridx = 1; form.add(cmbCategory, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row; form.add(new JLabel("Unidad de Medida:"), gbc);
        gbc.gridx = 1; form.add(txtUnit, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row; form.add(new JLabel("Stock Mínimo Alerta:"), gbc);
        gbc.gridx = 1; form.add(txtStockMin, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row; form.add(new JLabel(existing == null ? "Stock Inicial:" : "Stock Actual:"), gbc);
        gbc.gridx = 1; form.add(txtStockInit, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row; form.add(new JLabel(existing == null ? "Costo Unit. Inicial:" : "Costo Promedio:"), gbc);
        gbc.gridx = 1; form.add(txtCostInit, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row; form.add(new JLabel("Precio Venta Público:"), gbc);
        gbc.gridx = 1; form.add(txtPrice, gbc); row++;

        // Botones guardar / cancelar
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnSave = UITheme.createPrimaryButton("Guardar Producto");
        JButton btnCancel = new JButton("Cancelar");

        btnCancel.addActionListener(e -> dlg.dispose());
        btnSave.addActionListener(e -> {
            String code = txtCode.getText().trim();
            String name = txtName.getText().trim();
            Category cat = (Category) cmbCategory.getSelectedItem();

            if (code.isEmpty() || name.isEmpty() || cat == null) {
                JOptionPane.showMessageDialog(dlg, "Código, Nombre y Categoría son obligatorios.", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            BigDecimal stockMin = FormatUtils.parseBigDecimal(txtStockMin.getText(), new BigDecimal("5.00"));
            BigDecimal price = FormatUtils.parseBigDecimal(txtPrice.getText(), BigDecimal.ZERO);

            try {
                if (existing == null) {
                    BigDecimal stockInit = FormatUtils.parseBigDecimal(txtStockInit.getText(), BigDecimal.ZERO);
                    BigDecimal costInit = FormatUtils.parseBigDecimal(txtCostInit.getText(), BigDecimal.ZERO);

                    Product newP = new Product();
                    newP.setCodigo(code);
                    newP.setNombre(name);
                    newP.setIdCategoria(cat.getIdCategoria());
                    newP.setUnidadMedida(txtUnit.getText().trim().toUpperCase());
                    newP.setStockMinimo(stockMin);
                    newP.setStockActual(stockInit);
                    newP.setCostoPromedio(costInit);
                    newP.setPrecioVenta(price);

                    productDAO.insert(newP);
                    JOptionPane.showMessageDialog(dlg, "Producto registrado correctamente con saldo inicial en Kardex.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    existing.setCodigo(code);
                    existing.setNombre(name);
                    existing.setIdCategoria(cat.getIdCategoria());
                    existing.setUnidadMedida(txtUnit.getText().trim().toUpperCase());
                    existing.setStockMinimo(stockMin);
                    existing.setPrecioVenta(price);

                    productDAO.update(existing);
                    JOptionPane.showMessageDialog(dlg, "Producto actualizado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                }
                dlg.dispose();
                loadProducts();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Error al guardar producto: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void exportToCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("reporte_inventario_kardex.csv"));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            try {
                Exporter.exportTableToCSV(tableProducts, chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Inventario exportado con éxito en: " + chooser.getSelectedFile().getName(), "Exportación Exitosa", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al exportar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
