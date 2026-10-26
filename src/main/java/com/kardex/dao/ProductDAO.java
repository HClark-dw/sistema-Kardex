package com.kardex.dao;

import com.kardex.config.DatabaseConfig;
import com.kardex.model.Product;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public List<Product> listAll(String filter) throws SQLException {
        List<Product> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT p.id_producto, p.codigo, p.nombre, p.id_categoria, c.nombre AS categoria_nombre, " +
            "p.unidad_medida, p.stock_actual, p.stock_minimo, p.costo_promedio, p.precio_venta, p.estado " +
            "FROM productos p " +
            "INNER JOIN categorias c ON p.id_categoria = c.id_categoria " +
            "WHERE p.estado = TRUE "
        );

        if (filter != null && !filter.trim().isEmpty()) {
            sql.append("AND (p.codigo LIKE ? OR p.nombre LIKE ? OR c.nombre LIKE ?) ");
        }
        sql.append("ORDER BY p.nombre ASC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            if (filter != null && !filter.trim().isEmpty()) {
                String term = "%" + filter.trim() + "%";
                ps.setString(1, term);
                ps.setString(2, term);
                ps.setString(3, term);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product p = new Product();
                    p.setIdProducto(rs.getInt("id_producto"));
                    p.setCodigo(rs.getString("codigo"));
                    p.setNombre(rs.getString("nombre"));
                    p.setIdCategoria(rs.getInt("id_categoria"));
                    p.setCategoriaNombre(rs.getString("categoria_nombre"));
                    p.setUnidadMedida(rs.getString("unidad_medida"));
                    p.setStockActual(rs.getBigDecimal("stock_actual"));
                    p.setStockMinimo(rs.getBigDecimal("stock_minimo"));
                    p.setCostoPromedio(rs.getBigDecimal("costo_promedio"));
                    p.setPrecioVenta(rs.getBigDecimal("precio_venta"));
                    p.setEstado(rs.getBoolean("estado"));
                    list.add(p);
                }
            }
        }
        return list;
    }

    public Product findById(int id) throws SQLException {
        String sql = "SELECT p.id_producto, p.codigo, p.nombre, p.id_categoria, c.nombre AS categoria_nombre, " +
                     "p.unidad_medida, p.stock_actual, p.stock_minimo, p.costo_promedio, p.precio_venta, p.estado " +
                     "FROM productos p " +
                     "INNER JOIN categorias c ON p.id_categoria = c.id_categoria " +
                     "WHERE p.id_producto = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Product p = new Product();
                    p.setIdProducto(rs.getInt("id_producto"));
                    p.setCodigo(rs.getString("codigo"));
                    p.setNombre(rs.getString("nombre"));
                    p.setIdCategoria(rs.getInt("id_categoria"));
                    p.setCategoriaNombre(rs.getString("categoria_nombre"));
                    p.setUnidadMedida(rs.getString("unidad_medida"));
                    p.setStockActual(rs.getBigDecimal("stock_actual"));
                    p.setStockMinimo(rs.getBigDecimal("stock_minimo"));
                    p.setCostoPromedio(rs.getBigDecimal("costo_promedio"));
                    p.setPrecioVenta(rs.getBigDecimal("precio_venta"));
                    p.setEstado(rs.getBoolean("estado"));
                    return p;
                }
            }
        }
        return null;
    }

    public Product findByCode(String code) throws SQLException {
        String sql = "SELECT p.id_producto, p.codigo, p.nombre, p.id_categoria, c.nombre AS categoria_nombre, " +
                     "p.unidad_medida, p.stock_actual, p.stock_minimo, p.costo_promedio, p.precio_venta, p.estado " +
                     "FROM productos p " +
                     "INNER JOIN categorias c ON p.id_categoria = c.id_categoria " +
                     "WHERE p.codigo = ? AND p.estado = TRUE";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Product p = new Product();
                    p.setIdProducto(rs.getInt("id_producto"));
                    p.setCodigo(rs.getString("codigo"));
                    p.setNombre(rs.getString("nombre"));
                    p.setIdCategoria(rs.getInt("id_categoria"));
                    p.setCategoriaNombre(rs.getString("categoria_nombre"));
                    p.setUnidadMedida(rs.getString("unidad_medida"));
                    p.setStockActual(rs.getBigDecimal("stock_actual"));
                    p.setStockMinimo(rs.getBigDecimal("stock_minimo"));
                    p.setCostoPromedio(rs.getBigDecimal("costo_promedio"));
                    p.setPrecioVenta(rs.getBigDecimal("precio_venta"));
                    p.setEstado(rs.getBoolean("estado"));
                    return p;
                }
            }
        }
        return null;
    }

    public boolean insert(Product p) throws SQLException {
        String sql = "INSERT INTO productos (codigo, nombre, id_categoria, unidad_medida, stock_actual, stock_minimo, costo_promedio, precio_venta, estado) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getNombre());
            ps.setInt(3, p.getIdCategoria());
            ps.setString(4, p.getUnidadMedida());
            ps.setBigDecimal(5, p.getStockActual() != null ? p.getStockActual() : BigDecimal.ZERO);
            ps.setBigDecimal(6, p.getStockMinimo() != null ? p.getStockMinimo() : BigDecimal.ZERO);
            ps.setBigDecimal(7, p.getCostoPromedio() != null ? p.getCostoPromedio() : BigDecimal.ZERO);
            ps.setBigDecimal(8, p.getPrecioVenta() != null ? p.getPrecioVenta() : BigDecimal.ZERO);
            ps.setBoolean(9, p.isEstado());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        p.setIdProducto(rs.getInt(1));
                    }
                }
                // Si se registró con stock inicial mayor a cero, registrar movimiento de saldo inicial en Kardex
                if (p.getStockActual() != null && p.getStockActual().compareTo(BigDecimal.ZERO) > 0) {
                    KardexDAO kardexDAO = new KardexDAO();
                    kardexDAO.recordInitialBalance(conn, p);
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(Product p) throws SQLException {
        String sql = "UPDATE productos SET codigo = ?, nombre = ?, id_categoria = ?, unidad_medida = ?, stock_minimo = ?, precio_venta = ? " +
                     "WHERE id_producto = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getNombre());
            ps.setInt(3, p.getIdCategoria());
            ps.setString(4, p.getUnidadMedida());
            ps.setBigDecimal(5, p.getStockMinimo());
            ps.setBigDecimal(6, p.getPrecioVenta());
            ps.setInt(7, p.getIdProducto());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int idProducto) throws SQLException {
        String sql = "UPDATE productos SET estado = FALSE WHERE id_producto = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            return ps.executeUpdate() > 0;
        }
    }

    public static void updateStockAndCost(Connection conn, int idProducto, BigDecimal newStock, BigDecimal newAvgCost) throws SQLException {
        String sql = "UPDATE productos SET stock_actual = ?, costo_promedio = ? WHERE id_producto = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, newStock);
            ps.setBigDecimal(2, newAvgCost);
            ps.setInt(3, idProducto);
            ps.executeUpdate();
        }
    }
}
