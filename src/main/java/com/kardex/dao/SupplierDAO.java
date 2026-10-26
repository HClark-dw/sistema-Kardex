package com.kardex.dao;

import com.kardex.config.DatabaseConfig;
import com.kardex.model.Supplier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO {

    public List<Supplier> listAll() throws SQLException {
        List<Supplier> list = new ArrayList<>();
        String sql = "SELECT id_proveedor, documento, razon_social, telefono, email, direccion, estado " +
                     "FROM proveedores WHERE estado = TRUE ORDER BY razon_social";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Supplier(
                    rs.getInt("id_proveedor"),
                    rs.getString("documento"),
                    rs.getString("razon_social"),
                    rs.getString("telefono"),
                    rs.getString("email"),
                    rs.getString("direccion"),
                    rs.getBoolean("estado")
                ));
            }
        }
        return list;
    }

    public boolean insert(Supplier s) throws SQLException {
        String sql = "INSERT INTO proveedores (documento, razon_social, telefono, email, direccion, estado) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, s.getDocumento());
            ps.setString(2, s.getRazonSocial());
            ps.setString(3, s.getTelefono());
            ps.setString(4, s.getEmail());
            ps.setString(5, s.getDireccion());
            ps.setBoolean(6, s.isEstado());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        s.setIdProveedor(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(Supplier s) throws SQLException {
        String sql = "UPDATE proveedores SET documento = ?, razon_social = ?, telefono = ?, email = ?, direccion = ?, estado = ? WHERE id_proveedor = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, s.getDocumento());
            ps.setString(2, s.getRazonSocial());
            ps.setString(3, s.getTelefono());
            ps.setString(4, s.getEmail());
            ps.setString(5, s.getDireccion());
            ps.setBoolean(6, s.isEstado());
            ps.setInt(7, s.getIdProveedor());

            return ps.executeUpdate() > 0;
        }
    }
}
