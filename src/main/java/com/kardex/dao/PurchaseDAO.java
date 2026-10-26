package com.kardex.dao;

import com.kardex.config.DatabaseConfig;
import com.kardex.model.KardexMovement;
import com.kardex.model.Purchase;
import com.kardex.model.PurchaseDetail;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PurchaseDAO {

    public boolean registerPurchase(Purchase purchase) throws SQLException {
        String sqlPurchase = "INSERT INTO compras (id_proveedor, id_usuario, tipo_comprobante, serie_numero, fecha, total, observaciones) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlDetail = "INSERT INTO detalle_compras (id_compra, id_producto, cantidad, costo_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";
        String sqlSelectProduct = "SELECT stock_actual, costo_promedio FROM productos WHERE id_producto = ? FOR UPDATE";

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            // 1. Insertar Cabecera de Compra
            try (PreparedStatement ps = conn.prepareStatement(sqlPurchase, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, purchase.getIdProveedor());
                ps.setInt(2, purchase.getIdUsuario());
                ps.setString(3, purchase.getTipoComprobante());
                ps.setString(4, purchase.getSerieNumero());
                ps.setTimestamp(5, purchase.getFecha() != null ? Timestamp.valueOf(purchase.getFecha()) : new Timestamp(System.currentTimeMillis()));
                ps.setBigDecimal(6, purchase.getTotal());
                ps.setString(7, purchase.getObservaciones());

                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        purchase.setIdCompra(rs.getInt(1));
                    }
                }
            }

            KardexDAO kardexDAO = new KardexDAO();

            // 2. Procesar Detalles y Actualizar Kardex
            for (PurchaseDetail detail : purchase.getDetalles()) {
                detail.setIdCompra(purchase.getIdCompra());

                // Insertar detalle
                try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {
                    psDetail.setInt(1, detail.getIdCompra());
                    psDetail.setInt(2, detail.getIdProducto());
                    psDetail.setBigDecimal(3, detail.getCantidad());
                    psDetail.setBigDecimal(4, detail.getCostoUnitario());
                    psDetail.setBigDecimal(5, detail.getSubtotal());
                    psDetail.executeUpdate();
                }

                // Obtener stock y costo promedio actual con bloqueo de fila
                BigDecimal currentStock = BigDecimal.ZERO;
                BigDecimal currentAvgCost = BigDecimal.ZERO;
                try (PreparedStatement psProd = conn.prepareStatement(sqlSelectProduct)) {
                    psProd.setInt(1, detail.getIdProducto());
                    try (ResultSet rsProd = psProd.executeQuery()) {
                        if (rsProd.next()) {
                            currentStock = rsProd.getBigDecimal("stock_actual");
                            currentAvgCost = rsProd.getBigDecimal("costo_promedio");
                        }
                    }
                }

                // Cálculo Promedio Ponderado Móvil (NIC 2 / IAS 2)
                BigDecimal currentTotalVal = currentStock.multiply(currentAvgCost);
                BigDecimal purchaseTotalVal = detail.getCantidad().multiply(detail.getCostoUnitario());

                BigDecimal newStock = currentStock.add(detail.getCantidad());
                BigDecimal newTotalVal = currentTotalVal.add(purchaseTotalVal);
                BigDecimal newAvgCost = newStock.compareTo(BigDecimal.ZERO) > 0
                        ? newTotalVal.divide(newStock, 4, RoundingMode.HALF_UP)
                        : detail.getCostoUnitario();

                // Actualizar producto
                ProductDAO.updateStockAndCost(conn, detail.getIdProducto(), newStock, newAvgCost);

                // Insertar movimiento en Kardex
                KardexMovement km = new KardexMovement();
                km.setIdProducto(detail.getIdProducto());
                km.setFecha(purchase.getFecha());
                km.setTipoMovimiento("ENTRADA_COMPRA");
                km.setTipoComprobante(purchase.getTipoComprobante());
                km.setDocumentoReferencia(purchase.getSerieNumero());
                km.setMotivo("Compra a proveedor #" + purchase.getIdProveedor());

                km.setEntradaCantidad(detail.getCantidad());
                km.setEntradaCostoUnitario(detail.getCostoUnitario());
                km.setEntradaCostoTotal(purchaseTotalVal.setScale(2, RoundingMode.HALF_UP));

                km.setSalidaCantidad(BigDecimal.ZERO);
                km.setSalidaCostoUnitario(BigDecimal.ZERO);
                km.setSalidaCostoTotal(BigDecimal.ZERO);

                km.setSaldoCantidad(newStock);
                km.setSaldoCostoUnitario(newAvgCost);
                km.setSaldoCostoTotal(newTotalVal.setScale(2, RoundingMode.HALF_UP));

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

    public List<Purchase> listAll(LocalDate start, LocalDate end) throws SQLException {
        List<Purchase> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT c.id_compra, c.id_proveedor, p.razon_social AS proveedor_nombre, " +
            "c.id_usuario, u.nombre_completo AS usuario_nombre, c.tipo_comprobante, c.serie_numero, " +
            "c.fecha, c.total, c.observaciones " +
            "FROM compras c " +
            "INNER JOIN proveedores p ON c.id_proveedor = p.id_proveedor " +
            "INNER JOIN usuarios u ON c.id_usuario = u.id_usuario " +
            "WHERE 1=1 "
        );

        if (start != null) sql.append("AND DATE(c.fecha) >= ? ");
        if (end != null) sql.append("AND DATE(c.fecha) <= ? ");
        sql.append("ORDER BY c.fecha DESC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            if (start != null) ps.setDate(idx++, java.sql.Date.valueOf(start));
            if (end != null) ps.setDate(idx++, java.sql.Date.valueOf(end));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Purchase pur = new Purchase();
                    pur.setIdCompra(rs.getInt("id_compra"));
                    pur.setIdProveedor(rs.getInt("id_proveedor"));
                    pur.setProveedorNombre(rs.getString("proveedor_nombre"));
                    pur.setIdUsuario(rs.getInt("id_usuario"));
                    pur.setUsuarioNombre(rs.getString("usuario_nombre"));
                    pur.setTipoComprobante(rs.getString("tipo_comprobante"));
                    pur.setSerieNumero(rs.getString("serie_numero"));
                    Timestamp ts = rs.getTimestamp("fecha");
                    if (ts != null) pur.setFecha(ts.toLocalDateTime());
                    pur.setTotal(rs.getBigDecimal("total"));
                    pur.setObservaciones(rs.getString("observaciones"));
                    list.add(pur);
                }
            }
        }
        return list;
    }

    public List<PurchaseDetail> getDetails(int idCompra) throws SQLException {
        List<PurchaseDetail> list = new ArrayList<>();
        String sql = "SELECT d.id_detalle_compra, d.id_compra, d.id_producto, p.codigo, p.nombre, " +
                     "d.cantidad, d.costo_unitario, d.subtotal " +
                     "FROM detalle_compras d " +
                     "INNER JOIN productos p ON d.id_producto = p.id_producto " +
                     "WHERE d.id_compra = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idCompra);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PurchaseDetail d = new PurchaseDetail();
                    d.setIdDetalleCompra(rs.getInt("id_detalle_compra"));
                    d.setIdCompra(rs.getInt("id_compra"));
                    d.setIdProducto(rs.getInt("id_producto"));
                    d.setProductoCodigo(rs.getString("codigo"));
                    d.setProductoNombre(rs.getString("nombre"));
                    d.setCantidad(rs.getBigDecimal("cantidad"));
                    d.setCostoUnitario(rs.getBigDecimal("costo_unitario"));
                    d.setSubtotal(rs.getBigDecimal("subtotal"));
                    list.add(d);
                }
            }
        }
        return list;
    }
}
