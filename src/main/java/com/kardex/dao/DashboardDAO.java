package com.kardex.dao;

import com.kardex.config.DatabaseConfig;
import com.kardex.model.DashboardMetrics;
import com.kardex.model.Product;
import com.kardex.model.Purchase;
import com.kardex.model.Sale;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardDAO {

    public DashboardMetrics getDashboardMetrics() throws SQLException {
        DashboardMetrics m = new DashboardMetrics();

        try (Connection conn = DatabaseConfig.getConnection()) {

            // 1. Total Ingresos y Costo Estimado de Ventas
            String sqlSales = "SELECT COALESCE(SUM(total), 0) AS total_ingresos, " +
                              "COALESCE(SUM(costo_total_estimado), 0) AS total_costo " +
                              "FROM ventas";
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sqlSales)) {
                if (rs.next()) {
                    BigDecimal ingresos = rs.getBigDecimal("total_ingresos");
                    BigDecimal costo = rs.getBigDecimal("total_costo");
                    m.setTotalIngresos(ingresos);
                    m.setUtilidadBruta(ingresos.subtract(costo));
                }
            }

            // 2. Total Egresos (Compras)
            String sqlPurchases = "SELECT COALESCE(SUM(total), 0) AS total_egresos FROM compras";
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sqlPurchases)) {
                if (rs.next()) {
                    m.setTotalEgresos(rs.getBigDecimal("total_egresos"));
                }
            }

            // 3. Valoración del Inventario, Conteo de Productos y Bajo Stock
            String sqlInventory = "SELECT " +
                                  "COALESCE(SUM(stock_actual * costo_promedio), 0) AS valoracion_total, " +
                                  "COUNT(*) AS total_prods, " +
                                  "COALESCE(SUM(CASE WHEN stock_actual <= stock_minimo THEN 1 ELSE 0 END), 0) AS prods_bajo_stock " +
                                  "FROM productos WHERE estado = TRUE";
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sqlInventory)) {
                if (rs.next()) {
                    m.setValoracionTotalInventario(rs.getBigDecimal("valoracion_total"));
                    m.setTotalProductos(rs.getInt("total_prods"));
                    m.setProductosBajoStock(rs.getInt("prods_bajo_stock"));
                }
            }

            // 4. Productos con stock crítico
            String sqlCriticos = "SELECT p.id_producto, p.codigo, p.nombre, p.stock_actual, p.stock_minimo, p.unidad_medida, " +
                                 "p.costo_promedio, p.precio_venta, c.nombre AS categoria_nombre " +
                                 "FROM productos p " +
                                 "INNER JOIN categorias c ON p.id_categoria = c.id_categoria " +
                                 "WHERE p.estado = TRUE AND p.stock_actual <= p.stock_minimo " +
                                 "ORDER BY p.stock_actual ASC LIMIT 6";
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sqlCriticos)) {
                while (rs.next()) {
                    Product p = new Product();
                    p.setIdProducto(rs.getInt("id_producto"));
                    p.setCodigo(rs.getString("codigo"));
                    p.setNombre(rs.getString("nombre"));
                    p.setStockActual(rs.getBigDecimal("stock_actual"));
                    p.setStockMinimo(rs.getBigDecimal("stock_minimo"));
                    p.setUnidadMedida(rs.getString("unidad_medida"));
                    p.setCostoPromedio(rs.getBigDecimal("costo_promedio"));
                    p.setPrecioVenta(rs.getBigDecimal("precio_venta"));
                    p.setCategoriaNombre(rs.getString("categoria_nombre"));
                    m.getProductosCriticos().add(p);
                }
            }

            // 5. Flujo de Ingresos vs Egresos de los últimos 6 meses
            Map<String, BigDecimal[]> flujo = new LinkedHashMap<>();
            DateTimeFormatter mesFmt = DateTimeFormatter.ofPattern("MMM yyyy");
            LocalDate hoy = LocalDate.now();

            // Inicializar últimos 6 meses en orden cronológico
            for (int i = 5; i >= 0; i--) {
                LocalDate mesIter = hoy.minusMonths(i);
                String clave = mesIter.format(mesFmt);
                flujo.put(clave, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            }

            // Ventas por mes
            String sqlVentasMes = "SELECT DATE_FORMAT(fecha, '%b %Y') AS mes, " +
                                  "COALESCE(SUM(total), 0) AS total_mes, " +
                                  "YEAR(fecha) AS y, MONTH(fecha) AS m " +
                                  "FROM ventas " +
                                  "WHERE fecha >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH) " +
                                  "GROUP BY y, m, mes " +
                                  "ORDER BY y ASC, m ASC";
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sqlVentasMes)) {
                while (rs.next()) {
                    String mes = rs.getString("mes");
                    if (flujo.containsKey(mes)) {
                        flujo.get(mes)[0] = rs.getBigDecimal("total_mes");
                    }
                }
            }

            // Compras por mes
            String sqlComprasMes = "SELECT DATE_FORMAT(fecha, '%b %Y') AS mes, " +
                                   "COALESCE(SUM(total), 0) AS total_mes, " +
                                   "YEAR(fecha) AS y, MONTH(fecha) AS m " +
                                   "FROM compras " +
                                   "WHERE fecha >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH) " +
                                   "GROUP BY y, m, mes " +
                                   "ORDER BY y ASC, m ASC";
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sqlComprasMes)) {
                while (rs.next()) {
                    String mes = rs.getString("mes");
                    if (flujo.containsKey(mes)) {
                        flujo.get(mes)[1] = rs.getBigDecimal("total_mes");
                    }
                }
            }

            m.setFlujoIngresosEgresos(flujo);

            // 6. Últimas 5 Ventas
            SaleDAO saleDAO = new SaleDAO();
            List<Sale> ventas = saleDAO.listAll(null, null);
            m.setUltimasVentas(ventas.size() > 5 ? ventas.subList(0, 5) : ventas);

            // 7. Últimas 5 Compras
            PurchaseDAO purchaseDAO = new PurchaseDAO();
            List<Purchase> compras = purchaseDAO.listAll(null, null);
            m.setUltimasCompras(compras.size() > 5 ? compras.subList(0, 5) : compras);
        }

        return m;
    }
}
