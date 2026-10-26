package com.kardex.ui.panels;

import com.kardex.dao.KardexDAO;
import com.kardex.dao.ProductDAO;
import com.kardex.model.KardexMovement;
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
import java.time.LocalDate;
import java.util.List;

public class KardexPanel extends JPanel {

    private final ProductDAO productDAO = new ProductDAO();
    private final KardexDAO kardexDAO = new KardexDAO();

    private JComboBox<Product> cmbProducts;
    private JTextField txtDateFrom;
    private JTextField txtDateTo;
    private JTable tableKardex;
    private DefaultTableModel modelKardex;

    private JLabel lblCurrentStock;
    private JLabel lblCurrentAvgCost;
    private JLabel lblCurrentValuation;

    public KardexPanel() {
        setLayout(new BorderLayout(14, 14));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        // 1. Encabezado y Filtros
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        JLabel lblTitle = new JLabel("Kardex Físico y Valorado (Método Promedio Ponderado Móvil)");
        lblTitle.setFont(UITheme.FONT_TITLE);

        // Barra de selección
        JPanel filterCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        UITheme.styleCard(filterCard);

        cmbProducts = new JComboBox<>();
        cmbProducts.setPreferredSize(new Dimension(380, 32));
        cmbProducts.addActionListener(e -> queryKardex());

        txtDateFrom = new JTextField("", 8);
        txtDateFrom.putClientProperty("JTextField.placeholderText", "yyyy-MM-dd");

        txtDateTo = new JTextField("", 8);
        txtDateTo.putClientProperty("JTextField.placeholderText", "yyyy-MM-dd");

        JButton btnFilter = UITheme.createPrimaryButton("🔍 Consultar");
        btnFilter.addActionListener(e -> queryKardex());

        JButton btnExport = new JButton("📥 Exportar Kardex a CSV");
        btnExport.setFont(UITheme.FONT_BOLD);
        btnExport.addActionListener(e -> exportKardexCSV());

        filterCard.add(new JLabel("Seleccionar Producto:"));
        filterCard.add(cmbProducts);
        filterCard.add(new JLabel("Desde:"));
        filterCard.add(txtDateFrom);
        filterCard.add(new JLabel("Hasta:"));
        filterCard.add(txtDateTo);
        filterCard.add(btnFilter);
        filterCard.add(btnExport);

        // Panel de Resumen del Producto
        JPanel summaryCard = new JPanel(new GridLayout(1, 3, 12, 0));
        UITheme.styleCard(summaryCard);

        lblCurrentStock = new JLabel("Existencias Actuales: 0.00 UND");
        lblCurrentStock.setFont(UITheme.FONT_SUBTITLE);
        lblCurrentStock.setForeground(UITheme.PRIMARY);

        lblCurrentAvgCost = new JLabel("Costo Promedio Móvil: $ 0.0000");
        lblCurrentAvgCost.setFont(UITheme.FONT_SUBTITLE);
        lblCurrentAvgCost.setForeground(UITheme.WARNING);

        lblCurrentValuation = new JLabel("Saldo Total Valorado: $ 0.00");
        lblCurrentValuation.setFont(UITheme.FONT_SUBTITLE);
        lblCurrentValuation.setForeground(UITheme.SUCCESS);

        summaryCard.add(lblCurrentStock);
        summaryCard.add(lblCurrentAvgCost);
        summaryCard.add(lblCurrentValuation);

        topContainer.add(lblTitle);
        topContainer.add(Box.createRigidArea(new Dimension(0, 10)));
        topContainer.add(filterCard);
        topContainer.add(Box.createRigidArea(new Dimension(0, 8)));
        topContainer.add(summaryCard);

        add(topContainer, BorderLayout.NORTH);

        // 2. Tabla de Kardex Multicolumna
        String[] columns = {
            "Fecha / Hora", "Tipo Movimiento", "Documento", "Motivo",
            "Entrada: Cant", "Entrada: C.Unit", "Entrada: C.Total",
            "Salida: Cant", "Salida: C.Unit", "Salida: C.Total",
            "Saldo: Cant", "Saldo: C.Unit (Prom)", "Saldo: C.Total"
        };

        modelKardex = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tableKardex = new JTable(modelKardex);
        tableKardex.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableKardex.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Renderer de colores para columnas de Kardex
        tableKardex.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    if (column >= 4 && column <= 6) {
                        // Entradas (Verde sutil)
                        setForeground(new Color(27, 94, 32));
                    } else if (column >= 7 && column <= 9) {
                        // Salidas (Rojo sutil)
                        setForeground(new Color(183, 28, 28));
                    } else if (column >= 10 && column <= 12) {
                        // Saldos (Azul sutil)
                        setForeground(new Color(13, 71, 161));
                        setFont(UITheme.FONT_BOLD);
                    } else {
                        setForeground(table.getForeground());
                        setFont(UITheme.FONT_REGULAR);
                    }
                }
                return c;
            }
        });

        JScrollPane scrollTable = new JScrollPane(tableKardex);
        UITheme.styleCard(scrollTable);
        add(scrollTable, BorderLayout.CENTER);

        loadProducts();
    }

    public void loadProducts() {
        try {
            cmbProducts.removeAllItems();
            List<Product> prods = productDAO.listAll("");
            for (Product p : prods) {
                cmbProducts.addItem(p);
            }
            if (!prods.isEmpty()) {
                queryKardex();
            }
        } catch (Exception e) {
            System.err.println("Error al cargar productos en Kardex: " + e.getMessage());
        }
    }

    public void queryKardex() {
        Product p = (Product) cmbProducts.getSelectedItem();
        if (p == null) return;

        LocalDate start = null;
        LocalDate end = null;
        try {
            if (!txtDateFrom.getText().trim().isEmpty()) {
                start = LocalDate.parse(txtDateFrom.getText().trim(), FormatUtils.SQL_DATE_FORMATTER);
            }
            if (!txtDateTo.getText().trim().isEmpty()) {
                end = LocalDate.parse(txtDateTo.getText().trim(), FormatUtils.SQL_DATE_FORMATTER);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Formato de fecha inválido. Utilice: yyyy-MM-dd (Ej: 2026-10-25)", "Error de Fecha", JOptionPane.WARNING_MESSAGE);
            return;
        }

        final LocalDate fStart = start;
        final LocalDate fEnd = end;

        SwingWorker<List<KardexMovement>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<KardexMovement> doInBackground() throws Exception {
                // Actualizar datos del producto para métricas del header
                Product fresh = productDAO.findById(p.getIdProducto());
                if (fresh != null) {
                    p.setStockActual(fresh.getStockActual());
                    p.setCostoPromedio(fresh.getCostoPromedio());
                }
                return kardexDAO.getKardexByProduct(p.getIdProducto(), fStart, fEnd);
            }

            @Override
            protected void done() {
                try {
                    List<KardexMovement> movements = get();
                    modelKardex.setRowCount(0);

                    lblCurrentStock.setText(String.format("Existencias Actuales: %s %s", FormatUtils.formatQuantity(p.getStockActual()), p.getUnidadMedida()));
                    lblCurrentAvgCost.setText(String.format("Costo Promedio Móvil: %s", FormatUtils.formatUnitCost(p.getCostoPromedio())));
                    lblCurrentValuation.setText(String.format("Saldo Total Valorado: %s", FormatUtils.formatMoney(p.getValorTotalInventario())));

                    for (KardexMovement m : movements) {
                        modelKardex.addRow(new Object[]{
                            FormatUtils.formatDateTime(m.getFecha()),
                            m.getTipoMovimiento(),
                            m.getDocumentoReferencia(),
                            m.getMotivo(),
                            // Entradas
                            m.getEntradaCantidad().compareTo(BigDecimal.ZERO) > 0 ? FormatUtils.formatQuantity(m.getEntradaCantidad()) : "-",
                            m.getEntradaCantidad().compareTo(BigDecimal.ZERO) > 0 ? FormatUtils.formatUnitCost(m.getEntradaCostoUnitario()) : "-",
                            m.getEntradaCantidad().compareTo(BigDecimal.ZERO) > 0 ? FormatUtils.formatMoney(m.getEntradaCostoTotal()) : "-",
                            // Salidas
                            m.getSalidaCantidad().compareTo(BigDecimal.ZERO) > 0 ? FormatUtils.formatQuantity(m.getSalidaCantidad()) : "-",
                            m.getSalidaCantidad().compareTo(BigDecimal.ZERO) > 0 ? FormatUtils.formatUnitCost(m.getSalidaCostoUnitario()) : "-",
                            m.getSalidaCantidad().compareTo(BigDecimal.ZERO) > 0 ? FormatUtils.formatMoney(m.getSalidaCostoTotal()) : "-",
                            // Saldos
                            FormatUtils.formatQuantity(m.getSaldoCantidad()),
                            FormatUtils.formatUnitCost(m.getSaldoCostoUnitario()),
                            FormatUtils.formatMoney(m.getSaldoCostoTotal())
                        });
                    }

                } catch (Exception ex) {
                    System.err.println("Error al cargar kardex: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void exportKardexCSV() {
        Product p = (Product) cmbProducts.getSelectedItem();
        String prodName = p != null ? p.getCodigo() : "producto";
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("kardex_" + prodName + ".csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Exporter.exportTableToCSV(tableKardex, chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Kardex exportado exitosamente a CSV.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al exportar Kardex: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
