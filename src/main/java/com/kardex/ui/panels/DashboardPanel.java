package com.kardex.ui.panels;

import com.kardex.dao.DashboardDAO;
import com.kardex.model.DashboardMetrics;
import com.kardex.model.Product;
import com.kardex.model.Purchase;
import com.kardex.model.Sale;
import com.kardex.ui.components.MetricCard;
import com.kardex.ui.components.SimpleBarChart;
import com.kardex.util.FormatUtils;
import com.kardex.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class DashboardPanel extends JPanel {

    private final MetricCard cardIngresos;
    private final MetricCard cardEgresos;
    private final MetricCard cardUtilidad;
    private final MetricCard cardInventario;

    private final SimpleBarChart barChart;
    private final JTable tableCriticos;
    private final DefaultTableModel modelCriticos;

    private final JTable tableVentas;
    private final DefaultTableModel modelVentas;

    private final JTable tableCompras;
    private final DefaultTableModel modelCompras;

    private final DashboardDAO dashboardDAO = new DashboardDAO();

    public DashboardPanel() {
        setLayout(new BorderLayout(16, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        // 1. Barra Superior con Título y Botón de Refresco
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Panel de Control y Métricas Financieras");
        lblTitle.setFont(UITheme.FONT_TITLE);

        JButton btnRefresh = new JButton("↻ Actualizar Métricas");
        btnRefresh.setFont(UITheme.FONT_BOLD);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> reloadMetrics());

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(btnRefresh, BorderLayout.EAST);

        // 2. Panel Superior: 4 Tarjetas KPI
        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 14, 0));
        cardsPanel.setOpaque(false);

        cardIngresos = new MetricCard("Ingresos Totales", "$ 0.00", "Ventas acumuladas", "📈", UITheme.SUCCESS);
        cardEgresos = new MetricCard("Egresos Totales", "$ 0.00", "Compras e inventario", "📉", UITheme.DANGER);
        cardUtilidad = new MetricCard("Utilidad Bruta", "$ 0.00", "Margen comercial", "💰", UITheme.PRIMARY);
        cardInventario = new MetricCard("Valor Inventario", "$ 0.00", "Stock valorado", "📦", UITheme.WARNING);

        cardsPanel.add(cardIngresos);
        cardsPanel.add(cardEgresos);
        cardsPanel.add(cardUtilidad);
        cardsPanel.add(cardInventario);

        // 3. Panel Central: Gráfica de Ingresos/Egresos + Tabla de Stock Crítico
        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 14, 0));
        centerGrid.setOpaque(false);

        barChart = new SimpleBarChart();

        // Card Stock Crítico
        JPanel criticosContainer = new JPanel(new BorderLayout(8, 8));
        UITheme.styleCard(criticosContainer);
        JLabel lblCriticosTitle = new JLabel("⚠️ Alerta: Productos con Stock Crítico");
        lblCriticosTitle.setFont(UITheme.FONT_SUBTITLE);
        lblCriticosTitle.setForeground(UITheme.DANGER);

        String[] colsCriticos = {"Código", "Producto", "Stock Actual", "Mínimo", "Unidad"};
        modelCriticos = new DefaultTableModel(colsCriticos, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableCriticos = new JTable(modelCriticos);
        tableCriticos.setFillsViewportHeight(true);
        tableCriticos.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (column == 2) {
                    setForeground(UITheme.DANGER);
                    setFont(UITheme.FONT_BOLD);
                } else {
                    setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                    setFont(UITheme.FONT_REGULAR);
                }
                return c;
            }
        });

        JScrollPane scrollCriticos = new JScrollPane(tableCriticos);
        scrollCriticos.setBorder(BorderFactory.createEmptyBorder());

        criticosContainer.add(lblCriticosTitle, BorderLayout.NORTH);
        criticosContainer.add(scrollCriticos, BorderLayout.CENTER);

        centerGrid.add(barChart);
        centerGrid.add(criticosContainer);

        // 4. Panel Inferior: Pestañas de Últimas Transacciones (Ventas y Compras)
        JTabbedPane tabbedTransactions = new JTabbedPane();
        tabbedTransactions.setFont(UITheme.FONT_BOLD);

        // Tab Ventas Recientes
        String[] colsVentas = {"ID", "Fecha", "Comprobante", "N° Doc", "Cliente", "Total"};
        modelVentas = new DefaultTableModel(colsVentas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableVentas = new JTable(modelVentas);
        JScrollPane scrollVentas = new JScrollPane(tableVentas);
        scrollVentas.setBorder(BorderFactory.createEmptyBorder());
        tabbedTransactions.addTab("🛒 Últimas Ventas Realizadas", scrollVentas);

        // Tab Compras Recientes
        String[] colsCompras = {"ID", "Fecha", "Comprobante", "N° Doc", "Proveedor", "Total"};
        modelCompras = new DefaultTableModel(colsCompras, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableCompras = new JTable(modelCompras);
        JScrollPane scrollCompras = new JScrollPane(tableCompras);
        scrollCompras.setBorder(BorderFactory.createEmptyBorder());
        tabbedTransactions.addTab("📥 Últimas Compras Registradas", scrollCompras);

        // Ensamble Vertical
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        cardsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));
        centerGrid.setPreferredSize(new Dimension(800, 270));
        centerGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        tabbedTransactions.setPreferredSize(new Dimension(800, 220));

        contentPanel.add(headerPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 14)));
        contentPanel.add(cardsPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 14)));
        contentPanel.add(centerGrid);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 14)));
        contentPanel.add(tabbedTransactions);

        add(contentPanel, BorderLayout.CENTER);

        // Cargar datos
        reloadMetrics();
    }

    public void reloadMetrics() {
        SwingWorker<DashboardMetrics, Void> worker = new SwingWorker<>() {
            @Override
            protected DashboardMetrics doInBackground() throws Exception {
                return dashboardDAO.getDashboardMetrics();
            }

            @Override
            protected void done() {
                try {
                    DashboardMetrics m = get();
                    cardIngresos.setValue(FormatUtils.formatMoney(m.getTotalIngresos()));
                    cardEgresos.setValue(FormatUtils.formatMoney(m.getTotalEgresos()));
                    cardUtilidad.setValue(FormatUtils.formatMoney(m.getUtilidadBruta()));
                    cardInventario.setValue(FormatUtils.formatMoney(m.getValoracionTotalInventario()));
                    cardInventario.setSubtext(m.getTotalProductos() + " productos activos");

                    // Gráfica
                    barChart.setData(m.getFlujoIngresosEgresos());

                    // Stock Crítico
                    modelCriticos.setRowCount(0);
                    for (Product p : m.getProductosCriticos()) {
                        modelCriticos.addRow(new Object[]{
                            p.getCodigo(),
                            p.getNombre(),
                            FormatUtils.formatQuantity(p.getStockActual()),
                            FormatUtils.formatQuantity(p.getStockMinimo()),
                            p.getUnidadMedida()
                        });
                    }

                    // Últimas Ventas
                    modelVentas.setRowCount(0);
                    for (Sale s : m.getUltimasVentas()) {
                        modelVentas.addRow(new Object[]{
                            s.getIdVenta(),
                            FormatUtils.formatDateTime(s.getFecha()),
                            s.getTipoComprobante(),
                            s.getSerieNumero(),
                            s.getClienteNombre(),
                            FormatUtils.formatMoney(s.getTotal())
                        });
                    }

                    // Últimas Compras
                    modelCompras.setRowCount(0);
                    for (Purchase c : m.getUltimasCompras()) {
                        modelCompras.addRow(new Object[]{
                            c.getIdCompra(),
                            FormatUtils.formatDateTime(c.getFecha()),
                            c.getTipoComprobante(),
                            c.getSerieNumero(),
                            c.getProveedorNombre(),
                            FormatUtils.formatMoney(c.getTotal())
                        });
                    }

                } catch (Exception ex) {
                    // Si la BD no está aún iniciada, se muestra aviso amigable sin crashear la UI
                    System.err.println("No se pudieron cargar métricas de BD: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }
}
