package com.kardex.ui.panels;

import com.kardex.dao.ProductDAO;
import com.kardex.dao.PurchaseDAO;
import com.kardex.dao.SaleDAO;
import com.kardex.model.Product;
import com.kardex.model.Purchase;
import com.kardex.model.Sale;
import com.kardex.util.Exporter;
import com.kardex.util.FormatUtils;
import com.kardex.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ReportsPanel extends JPanel {

    private final ProductDAO productDAO = new ProductDAO();
    private final SaleDAO saleDAO = new SaleDAO();
    private final PurchaseDAO purchaseDAO = new PurchaseDAO();

    private JComboBox<String> cmbReportType;
    private JTextField txtFrom;
    private JTextField txtTo;
    private JTable tableReport;
    private DefaultTableModel modelReport;
    private JLabel lblReportSummary;

    public ReportsPanel() {
        setLayout(new BorderLayout(14, 14));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        // 1. Cabecera y Selector de Reporte
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        JLabel lblTitle = new JLabel("Centro de Reportes Financieros y de Inventario");
        lblTitle.setFont(UITheme.FONT_TITLE);

        JPanel filterCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        UITheme.styleCard(filterCard);

        cmbReportType = new JComboBox<>(new String[]{
            "Reporte de Inventario Valorado General",
            "Reporte de Ventas y Utilidad Bruta",
            "Reporte de Compras y Egresos de Almacén",
            "Reporte de Productos en Stock Crítico"
        });
        cmbReportType.setFont(UITheme.FONT_BOLD);
        cmbReportType.addActionListener(e -> generateReport());

        txtFrom = new JTextField("", 8);
        txtFrom.putClientProperty("JTextField.placeholderText", "yyyy-MM-dd");

        txtTo = new JTextField("", 8);
        txtTo.putClientProperty("JTextField.placeholderText", "yyyy-MM-dd");

        JButton btnGenerate = UITheme.createPrimaryButton("📊 Generar Reporte");
        btnGenerate.addActionListener(e -> generateReport());

        JButton btnExport = new JButton("📥 Exportar CSV");
        btnExport.setFont(UITheme.FONT_BOLD);
        btnExport.addActionListener(e -> exportCSV());

        JButton btnPrint = new JButton("🖨 Imprimir / Guardar PDF");
        btnPrint.setFont(UITheme.FONT_BOLD);
        btnPrint.addActionListener(e -> printReport());

        filterCard.add(new JLabel("Tipo de Reporte:"));
        filterCard.add(cmbReportType);
        filterCard.add(new JLabel("Desde:"));
        filterCard.add(txtFrom);
        filterCard.add(new JLabel("Hasta:"));
        filterCard.add(txtTo);
        filterCard.add(btnGenerate);
        filterCard.add(btnExport);
        filterCard.add(btnPrint);

        topContainer.add(lblTitle);
        topContainer.add(Box.createRigidArea(new Dimension(0, 10)));
        topContainer.add(filterCard);

        add(topContainer, BorderLayout.NORTH);

        // 2. Tabla del Reporte
        modelReport = new DefaultTableModel();
        tableReport = new JTable(modelReport);
        tableReport.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollTable = new JScrollPane(tableReport);
        UITheme.styleCard(scrollTable);
        add(scrollTable, BorderLayout.CENTER);

        // 3. Resumen al pie
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(6, 4, 4, 4));

        lblReportSummary = new JLabel("Listo para generar reporte.");
        lblReportSummary.setFont(UITheme.FONT_BOLD);
        footer.add(lblReportSummary, BorderLayout.WEST);

        add(footer, BorderLayout.SOUTH);

        generateReport();
    }

    public void generateReport() {
        int index = cmbReportType.getSelectedIndex();
        LocalDate start = null;
        LocalDate end = null;
        try {
            if (!txtFrom.getText().trim().isEmpty()) {
                start = LocalDate.parse(txtFrom.getText().trim(), FormatUtils.SQL_DATE_FORMATTER);
            }
            if (!txtTo.getText().trim().isEmpty()) {
                end = LocalDate.parse(txtTo.getText().trim(), FormatUtils.SQL_DATE_FORMATTER);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Formato de fecha inválido. Utilice yyyy-MM-dd.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        final LocalDate fStart = start;
        final LocalDate fEnd = end;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                switch (index) {
                    case 0 -> loadValuedInventoryReport();
                    case 1 -> loadSalesReport(fStart, fEnd);
                    case 2 -> loadPurchasesReport(fStart, fEnd);
                    case 3 -> loadCriticalStockReport();
                }
                return null;
            }
        };
        worker.execute();
    }

    private void loadValuedInventoryReport() {
        try {
            List<Product> list = productDAO.listAll("");
            String[] cols = {"Código", "Producto", "Categoría", "U.M.", "Stock", "Costo Promedio", "Valor Total Inventario", "Precio Venta", "Margen Potencial"};
            DefaultTableModel model = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };

            final BigDecimal[] sumValuation = {BigDecimal.ZERO};
            final BigDecimal[] sumPotentialSales = {BigDecimal.ZERO};

            for (Product p : list) {
                BigDecimal val = p.getValorTotalInventario();
                BigDecimal potentialSale = p.getStockActual().multiply(p.getPrecioVenta());
                BigDecimal margin = potentialSale.subtract(val);

                sumValuation[0] = sumValuation[0].add(val);
                sumPotentialSales[0] = sumPotentialSales[0].add(potentialSale);

                model.addRow(new Object[]{
                    p.getCodigo(),
                    p.getNombre(),
                    p.getCategoriaNombre(),
                    p.getUnidadMedida(),
                    FormatUtils.formatQuantity(p.getStockActual()),
                    FormatUtils.formatUnitCost(p.getCostoPromedio()),
                    FormatUtils.formatMoney(val),
                    FormatUtils.formatMoney(p.getPrecioVenta()),
                    FormatUtils.formatMoney(margin)
                });
            }

            SwingUtilities.invokeLater(() -> {
                tableReport.setModel(model);
                lblReportSummary.setText(String.format(
                    "Total Productos: %d | Valorización Total del Inventario: %s | Venta Potencial: %s",
                    list.size(), FormatUtils.formatMoney(sumValuation[0]), FormatUtils.formatMoney(sumPotentialSales[0])
                ));
            });

        } catch (Exception ex) {
            System.err.println("Error reporte inventario: " + ex.getMessage());
        }
    }

    private void loadSalesReport(LocalDate start, LocalDate end) {
        try {
            List<Sale> list = saleDAO.listAll(start, end);
            String[] cols = {"ID Venta", "Fecha y Hora", "Comprobante", "N° Serie/Doc", "Cliente", "Total Venta (Ingreso)", "Costo de Venta (Egreso)", "Utilidad Bruta", "Margen %"};
            DefaultTableModel model = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };

            BigDecimal totalVentas = BigDecimal.ZERO;
            BigDecimal totalCostos = BigDecimal.ZERO;
            BigDecimal totalUtilidad = BigDecimal.ZERO;

            for (Sale s : list) {
                totalVentas = totalVentas.add(s.getTotal());
                totalCostos = totalCostos.add(s.getCostoTotalEstimado());
                BigDecimal util = s.getUtilidadBruta();
                totalUtilidad = totalUtilidad.add(util);

                double marginPct = (s.getTotal().compareTo(BigDecimal.ZERO) > 0)
                        ? util.doubleValue() / s.getTotal().doubleValue() * 100.0 : 0.0;

                model.addRow(new Object[]{
                    s.getIdVenta(),
                    FormatUtils.formatDateTime(s.getFecha()),
                    s.getTipoComprobante(),
                    s.getSerieNumero(),
                    s.getClienteNombre(),
                    FormatUtils.formatMoney(s.getTotal()),
                    FormatUtils.formatMoney(s.getCostoTotalEstimado()),
                    FormatUtils.formatMoney(util),
                    String.format("%.1f%%", marginPct)
                });
            }

            final BigDecimal fTotVentas = totalVentas;
            final BigDecimal fTotCostos = totalCostos;
            final BigDecimal fTotUtilidad = totalUtilidad;

            SwingUtilities.invokeLater(() -> {
                tableReport.setModel(model);
                lblReportSummary.setText(String.format(
                    "Ventas Totales: %s | Costo de Mercadería Vendida: %s | Utilidad Bruta Total: %s",
                    FormatUtils.formatMoney(fTotVentas), FormatUtils.formatMoney(fTotCostos), FormatUtils.formatMoney(fTotUtilidad)
                ));
            });

        } catch (Exception ex) {
            System.err.println("Error reporte ventas: " + ex.getMessage());
        }
    }

    private void loadPurchasesReport(LocalDate start, LocalDate end) {
        try {
            List<Purchase> list = purchaseDAO.listAll(start, end);
            String[] cols = {"ID Compra", "Fecha y Hora", "Comprobante", "N° Serie/Doc", "Proveedor", "Registrado Por", "Total Compra (Egreso)"};
            DefaultTableModel model = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };

            BigDecimal totalCompras = BigDecimal.ZERO;

            for (Purchase p : list) {
                totalCompras = totalCompras.add(p.getTotal());
                model.addRow(new Object[]{
                    p.getIdCompra(),
                    FormatUtils.formatDateTime(p.getFecha()),
                    p.getTipoComprobante(),
                    p.getSerieNumero(),
                    p.getProveedorNombre(),
                    p.getUsuarioNombre(),
                    FormatUtils.formatMoney(p.getTotal())
                });
            }

            final BigDecimal fTotCompras = totalCompras;

            SwingUtilities.invokeLater(() -> {
                tableReport.setModel(model);
                lblReportSummary.setText(String.format(
                    "Total Compras Registradas: %d transacciones | Total Egresos por Compra: %s",
                    list.size(), FormatUtils.formatMoney(fTotCompras)
                ));
            });

        } catch (Exception ex) {
            System.err.println("Error reporte compras: " + ex.getMessage());
        }
    }

    private void loadCriticalStockReport() {
        try {
            List<Product> list = productDAO.listAll("");
            String[] cols = {"Código", "Producto", "Categoría", "Stock Actual", "Stock Mínimo", "Unidad", "Déficit / Faltante", "Costo Reposición Est."};
            DefaultTableModel model = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };

            int count = 0;
            BigDecimal totalReposition = BigDecimal.ZERO;

            for (Product p : list) {
                if (p.isLowStock()) {
                    count++;
                    BigDecimal def = p.getStockMinimo().subtract(p.getStockActual());
                    if (def.compareTo(BigDecimal.ZERO) < 0) def = BigDecimal.ZERO;
                    BigDecimal costRep = def.multiply(p.getCostoPromedio());
                    totalReposition = totalReposition.add(costRep);

                    model.addRow(new Object[]{
                        p.getCodigo(),
                        p.getNombre(),
                        p.getCategoriaNombre(),
                        FormatUtils.formatQuantity(p.getStockActual()),
                        FormatUtils.formatQuantity(p.getStockMinimo()),
                        p.getUnidadMedida(),
                        FormatUtils.formatQuantity(def),
                        FormatUtils.formatMoney(costRep)
                    });
                }
            }

            final int fCount = count;
            final BigDecimal fTotRep = totalReposition;

            SwingUtilities.invokeLater(() -> {
                tableReport.setModel(model);
                lblReportSummary.setText(String.format(
                    "⚠️ Productos en Alerta Crítica: %d | Costo Estimado de Reposición a Stock Mínimo: %s",
                    fCount, FormatUtils.formatMoney(fTotRep)
                ));
            });

        } catch (Exception ex) {
            System.err.println("Error reporte stock crítico: " + ex.getMessage());
        }
    }

    private void exportCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("reporte_" + System.currentTimeMillis() + ".csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Exporter.exportTableToCSV(tableReport, chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Reporte exportado exitosamente a CSV.", "Exportación Completa", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al exportar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void printReport() {
        try {
            boolean complete = tableReport.print(
                JTable.PrintMode.FIT_WIDTH,
                new java.text.MessageFormat((String) cmbReportType.getSelectedItem()),
                new java.text.MessageFormat("Página - {0}")
            );
            if (complete) {
                JOptionPane.showMessageDialog(this, "Impresión completada.", "Impresión", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error de impresión: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
