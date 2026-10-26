package com.kardex.dao;

import com.kardex.config.DatabaseConfig;
import com.kardex.model.KardexMovement;
import com.kardex.model.Sale;
import com.kardex.model.SaleDetail;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SaleDAO {

    public boolean registerSale(Sale sale) throws SQLException {
        String sqlSale = "INSERT INTO ventas (id_cliente, id_usuario, tipo_comprobante, serie_numero, fecha, total, costo_total_estimado, observaciones) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlDetail = "INSERT INTO detalle_ventas (id_venta, id_producto, cantidad, precio_unitario, costo_unitario_historico, subtotal) " +
                           "VALUES (?, ?, ?, ?, ?, ?)";
        String sqlSelectProduct = "SELECT stock_actual, costo_promedio, nombre FROM productos WHERE id_producto = ? FOR UPDATE";

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            // 1. Validar existencias de stock y capturar costo promedio actual para cada producto
            BigDecimal sumCostoTotalVenta = BigDecimal.ZERO;
            for (SaleDetail detail : sale.getDetalles()) {
                try (PreparedStatement psProd = conn.prepareStatement(sqlSelectProduct)) {
                    psProd.setInt(1, detail.getIdProducto());
                    try (ResultSet rsProd = psProd.executeQuery()) {
                        if (!rsProd.next()) {
                            throw new SQLException("El producto ID " + detail.getIdProducto() + " no fue encontrado.");
                        }
                        BigDecimal stockActual = rsProd.getBigDecimal("stock_actual");
                        BigDecimal costoPromedio = rsProd.getBigDecimal("costo_promedio");
                        String prodNombre = rsProd.getString("nombre");

                        if (stockActual.compareTo(detail.getCantidad()) < 0) {
                            throw new SQLException(String.format(
                                "Stock insuficiente para '%s'. Disponible: %s, Solicitado: %s",
                                prodNombre, stockActual, detail.getCantidad()
                            ));
                        }

                        // Guardar costo histórico al momento de la venta
                        detail.setCostoUnitarioHistorico(costoPromedio);
                        BigDecimal costoLinea = detail.getCantidad().multiply(costoPromedio).setScale(2, RoundingMode.HALF_UP);
                        sumCostoTotalVenta = sumCostoTotalVenta.add(costoLinea);
                    }
                }
            }
            sale.setCostoTotalEstimado(sumCostoTotalVenta);

            // 2. Insertar Cabecera de Venta
            try (PreparedStatement ps = conn.prepareStatement(sqlSale, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, sale.getIdCliente());
                ps.setInt(2, sale.getIdUsuario());
                ps.setString(3, sale.getTipoComprobante());
                ps.setString(4, sale.getSerieNumero());
                ps.setTimestamp(5, sale.getFecha() != null ? Timestamp.valueOf(sale.getFecha()) : new Timestamp(System.currentTimeMillis()));
                ps.setBigDecimal(6, sale.getTotal());
                ps.setBigDecimal(7, sale.getCostoTotalEstimado());
                ps.setString(8, sale.getObservaciones());

                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        sale.setIdVenta(rs.getInt(1));
                    }
                }
            }

            KardexDAO kardexDAO = new KardexDAO();

            // 3. Insertar Detalles, descontar stock y registrar salida en Kardex
            for (SaleDetail detail : sale.getDetalles()) {
                detail.setIdVenta(sale.getIdVenta());

                // Insertar detalle
                try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {
                    psDetail.setInt(1, detail.getIdVenta());
                    psDetail.setInt(2, detail.getIdProducto());
                    psDetail.setBigDecimal(3, detail.getCantidad());
                    psDetail.setBigDecimal(4, detail.getPrecioUnitario());
                    psDetail.setBigDecimal(5, detail.getCostoUnitarioHistorico());
                    psDetail.setBigDecimal(6, detail.getSubtotal());
                    psDetail.executeUpdate();
                }

                // Descontar inventario
                BigDecimal currentStock = BigDecimal.ZERO;
                BigDecimal currentAvgCost = detail.getCostoUnitarioHistorico();
                try (PreparedStatement psProd = conn.prepareStatement(sqlSelectProduct)) {
                    psProd.setInt(1, detail.getIdProducto());
                    try (ResultSet rsProd = psProd.executeQuery()) {
                        if (rsProd.next()) {
                            currentStock = rsProd.getBigDecimal("stock_actual");
                        }
                    }
                }

                BigDecimal newStock = currentStock.subtract(detail.getCantidad());
                BigDecimal newTotalVal = newStock.multiply(currentAvgCost).setScale(2, RoundingMode.HALF_UP);
                BigDecimal salidaCostoTotal = detail.getCantidad().multiply(currentAvgCost).setScale(2, RoundingMode.HALF_UP);

                // Actualizar stock del producto (el costo unitario promedio se mantiene constante en una venta)
                ProductDAO.updateStockAndCost(conn, detail.getIdProducto(), newStock, currentAvgCost);

                // Movimiento Kardex de salida
                KardexMovement km = new KardexMovement();
                km.setIdProducto(detail.getIdProducto());
                km.setFecha(sale.getFecha());
                km.setTipoMovimiento("SALIDA_VENTA");
                km.setTipoComprobante(sale.getTipoComprobante());
                km.setDocumentoReferencia(sale.getSerieNumero());
                km.setMotivo("Venta a cliente #" + sale.getIdCliente());

                km.setEntradaCantidad(BigDecimal.ZERO);
                km.setEntradaCostoUnitario(BigDecimal.ZERO);
                km.setEntradaCostoTotal(BigDecimal.ZERO);

                km.setSalidaCantidad(detail.getCantidad());
                km.setSalidaCostoUnitario(currentAvgCost);
                km.setSalidaCostoTotal(salidaCostoTotal);

                km.setSaldoCantidad(newStock);
                km.setSaldoCostoUnitario(currentAvgCost);
                km.setSaldoCostoTotal(newTotalVal);

                kardexDAO.insertMovement(conn, km);
            }

            conn.commit();
            return true;
        } catch (SQLException ex) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw ex;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public List<Sale> listAll(LocalDate start, LocalDate end) throws SQLException {
        List<Sale> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT v.id_venta, v.id_cliente, c.nombre_razon_social AS cliente_nombre, " +
            "v.id_usuario, u.nombre_completo AS usuario_nombre, v.tipo_comprobante, v.serie_numero, " +
            "v.fecha, v.total, v.costo_total_estimado, v.observaciones " +
            "FROM ventas v " +
            "INNER JOIN clientes c ON v.id_cliente = c.id_cliente " +
            "INNER JOIN usuarios u ON v.id_usuario = u.id_usuario " +
            "WHERE 1=1 "
        );

        if (start != null) sql.append("AND DATE(v.fecha) >= ? ");
        if (end != null) sql.append("AND DATE(v.fecha) <= ? ");
        sql.append("ORDER BY v.fecha DESC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            if (start != null) ps.setDate(idx++, java.sql.Date.valueOf(start));
            if (end != null) ps.setDate(idx++, java.sql.Date.valueOf(end));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Sale s = new Sale();
                    s.setIdVenta(rs.getInt("id_venta"));
                    s.setIdCliente(rs.getInt("id_cliente"));
                    s.setClienteNombre(rs.getString("cliente_nombre"));
                    s.setIdUsuario(rs.getInt("id_usuario"));
                    s.setUsuarioNombre(rs.getString("usuario_nombre"));
                    s.setTipoComprobante(rs.getString("tipo_comprobante"));
                    s.setSerieNumero(rs.getString("serie_numero"));
                    Timestamp ts = rs.getTimestamp("fecha");
                    if (ts != null) s.setFecha(ts.toLocalDateTime());
                    s.setTotal(rs.getBigDecimal("total"));
                    s.setCostoTotalEstimado(rs.getBigDecimal("costo_total_estimado"));
                    s.setObservaciones(rs.getString("observaciones"));
                    list.add(s);
                }
            }
        }
        return list;
    }

    public List<SaleDetail> getDetails(int idVenta) throws SQLException {
        List<SaleDetail> list = new ArrayList<>();
        String sql = "SELECT d.id_detalle_venta, d.id_venta, d.id_producto, p.codigo, p.nombre, " +
                     "d.cantidad, d.precio_unitario, d.costo_unitario_historico, d.subtotal " +
                     "FROM detalle_ventas d " +
                     "INNER JOIN productos p ON d.id_producto = p.id_producto " +
                     "WHERE d.id_venta = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SaleDetail d = new SaleDetail();
                    d.setIdDetalleVenta(rs.getInt("id_detalle_venta"));
                    d.setIdVenta(rs.getInt("id_venta"));
                    d.setIdProducto(rs.getInt("id_producto"));
                    d.setProductoCodigo(rs.getString("codigo"));
                    d.setProductoNombre(rs.getString("nombre"));
                    d.setCantidad(rs.getBigDecimal("cantidad"));
                    d.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    d.setCostoUnitarioHistorico(rs.getBigDecimal("costo_unitario_historico"));
                    d.setSubtotal(rs.getBigDecimal("subtotal"));
                    list.add(d);
                }
            }
        }
        return list;
    }
}
