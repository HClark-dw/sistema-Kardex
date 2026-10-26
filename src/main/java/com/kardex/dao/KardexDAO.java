package com.kardex.dao;

import com.kardex.config.DatabaseConfig;
import com.kardex.model.KardexMovement;
import com.kardex.model.Product;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class KardexDAO {

    public List<KardexMovement> getKardexByProduct(int idProducto, LocalDate start, LocalDate end) throws SQLException {
        List<KardexMovement> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT k.id_kardex, k.id_producto, p.codigo AS prod_codigo, p.nombre AS prod_nombre, " +
            "k.fecha, k.tipo_movimiento, k.tipo_comprobante, k.documento_referencia, k.motivo, " +
            "k.entrada_cantidad, k.entrada_costo_unitario, k.entrada_costo_total, " +
            "k.salida_cantidad, k.salida_costo_unitario, k.salida_costo_total, " +
            "k.saldo_cantidad, k.saldo_costo_unitario, k.saldo_costo_total " +
            "FROM kardex k " +
            "INNER JOIN productos p ON k.id_producto = p.id_producto " +
            "WHERE k.id_producto = ? "
        );

        if (start != null) {
            sql.append("AND DATE(k.fecha) >= ? ");
        }
        if (end != null) {
            sql.append("AND DATE(k.fecha) <= ? ");
        }
        sql.append("ORDER BY k.fecha ASC, k.id_kardex ASC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            ps.setInt(paramIndex++, idProducto);

            if (start != null) {
                ps.setDate(paramIndex++, java.sql.Date.valueOf(start));
            }
            if (end != null) {
                ps.setDate(paramIndex++, java.sql.Date.valueOf(end));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    KardexMovement km = new KardexMovement();
                    km.setIdKardex(rs.getInt("id_kardex"));
                    km.setIdProducto(rs.getInt("id_producto"));
                    km.setProductoCodigo(rs.getString("prod_codigo"));
                    km.setProductoNombre(rs.getString("prod_nombre"));
                    Timestamp ts = rs.getTimestamp("fecha");
                    if (ts != null) km.setFecha(ts.toLocalDateTime());
                    km.setTipoMovimiento(rs.getString("tipo_movimiento"));
                    km.setTipoComprobante(rs.getString("tipo_comprobante"));
                    km.setDocumentoReferencia(rs.getString("documento_referencia"));
                    km.setMotivo(rs.getString("motivo"));

                    km.setEntradaCantidad(rs.getBigDecimal("entrada_cantidad"));
                    km.setEntradaCostoUnitario(rs.getBigDecimal("entrada_costo_unitario"));
                    km.setEntradaCostoTotal(rs.getBigDecimal("entrada_costo_total"));

                    km.setSalidaCantidad(rs.getBigDecimal("salida_cantidad"));
                    km.setSalidaCostoUnitario(rs.getBigDecimal("salida_costo_unitario"));
                    km.setSalidaCostoTotal(rs.getBigDecimal("salida_costo_total"));

                    km.setSaldoCantidad(rs.getBigDecimal("saldo_cantidad"));
                    km.setSaldoCostoUnitario(rs.getBigDecimal("saldo_costo_unitario"));
                    km.setSaldoCostoTotal(rs.getBigDecimal("saldo_costo_total"));

                    list.add(km);
                }
            }
        }
        return list;
    }

    public void insertMovement(Connection conn, KardexMovement km) throws SQLException {
        String sql = "INSERT INTO kardex (id_producto, fecha, tipo_movimiento, tipo_comprobante, documento_referencia, motivo, " +
                     "entrada_cantidad, entrada_costo_unitario, entrada_costo_total, " +
                     "salida_cantidad, salida_costo_unitario, salida_costo_total, " +
                     "saldo_cantidad, saldo_costo_unitario, saldo_costo_total) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, km.getIdProducto());
            ps.setTimestamp(2, km.getFecha() != null ? Timestamp.valueOf(km.getFecha()) : new Timestamp(System.currentTimeMillis()));
            ps.setString(3, km.getTipoMovimiento());
            ps.setString(4, km.getTipoComprobante());
            ps.setString(5, km.getDocumentoReferencia());
            ps.setString(6, km.getMotivo());

            ps.setBigDecimal(7, km.getEntradaCantidad() != null ? km.getEntradaCantidad() : BigDecimal.ZERO);
            ps.setBigDecimal(8, km.getEntradaCostoUnitario() != null ? km.getEntradaCostoUnitario() : BigDecimal.ZERO);
            ps.setBigDecimal(9, km.getEntradaCostoTotal() != null ? km.getEntradaCostoTotal() : BigDecimal.ZERO);

            ps.setBigDecimal(10, km.getSalidaCantidad() != null ? km.getSalidaCantidad() : BigDecimal.ZERO);
            ps.setBigDecimal(11, km.getSalidaCostoUnitario() != null ? km.getSalidaCostoUnitario() : BigDecimal.ZERO);
            ps.setBigDecimal(12, km.getSalidaCostoTotal() != null ? km.getSalidaCostoTotal() : BigDecimal.ZERO);

            ps.setBigDecimal(13, km.getSaldoCantidad() != null ? km.getSaldoCantidad() : BigDecimal.ZERO);
            ps.setBigDecimal(14, km.getSaldoCostoUnitario() != null ? km.getSaldoCostoUnitario() : BigDecimal.ZERO);
            ps.setBigDecimal(15, km.getSaldoCostoTotal() != null ? km.getSaldoCostoTotal() : BigDecimal.ZERO);

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    km.setIdKardex(rs.getInt(1));
                }
            }
        }
    }

    public void recordInitialBalance(Connection conn, Product p) throws SQLException {
        BigDecimal qty = p.getStockActual() != null ? p.getStockActual() : BigDecimal.ZERO;
        BigDecimal unitCost = p.getCostoPromedio() != null ? p.getCostoPromedio() : BigDecimal.ZERO;
        BigDecimal totalCost = qty.multiply(unitCost).setScale(2, RoundingMode.HALF_UP);

        KardexMovement km = new KardexMovement();
        km.setIdProducto(p.getIdProducto());
        km.setTipoMovimiento("SALDO_INICIAL");
        km.setTipoComprobante("INVENTARIO_INICIAL");
        km.setDocumentoReferencia("INI-" + String.format("%03d", p.getIdProducto()));
        km.setMotivo("Inventario inicial del producto");

        km.setEntradaCantidad(qty);
        km.setEntradaCostoUnitario(unitCost);
        km.setEntradaCostoTotal(totalCost);

        km.setSalidaCantidad(BigDecimal.ZERO);
        km.setSalidaCostoUnitario(BigDecimal.ZERO);
        km.setSalidaCostoTotal(BigDecimal.ZERO);

        km.setSaldoCantidad(qty);
        km.setSaldoCostoUnitario(unitCost);
        km.setSaldoCostoTotal(totalCost);

        insertMovement(conn, km);
    }
}
