package com.kardex.dao;

import com.kardex.config.DatabaseConfig;
import com.kardex.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    public List<Customer> listAll() throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT id_cliente, documento, nombre_razon_social, telefono, email, direccion, estado " +
                     "FROM clientes WHERE estado = TRUE ORDER BY nombre_razon_social";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Customer(
                    rs.getInt("id_cliente"),
                    rs.getString("documento"),
                    rs.getString("nombre_razon_social"),
                    rs.getString("telefono"),
                    rs.getString("email"),
                    rs.getString("direccion"),
                    rs.getBoolean("estado")
                ));
            }
        }
        return list;
    }

    public boolean insert(Customer c) throws SQLException {
        String sql = "INSERT INTO clientes (documento, nombre_razon_social, telefono, email, direccion, estado) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getDocumento());
            ps.setString(2, c.getNombreRazonSocial());
            ps.setString(3, c.getTelefono());
            ps.setString(4, c.getEmail());
            ps.setString(5, c.getDireccion());
            ps.setBoolean(6, c.isEstado());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        c.setIdCliente(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(Customer c) throws SQLException {
        String sql = "UPDATE clientes SET documento = ?, nombre_razon_social = ?, telefono = ?, email = ?, direccion = ?, estado = ? WHERE id_cliente = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getDocumento());
            ps.setString(2, c.getNombreRazonSocial());
            ps.setString(3, c.getTelefono());
            ps.setString(4, c.getEmail());
            ps.setString(5, c.getDireccion());
            ps.setBoolean(6, c.isEstado());
            ps.setInt(7, c.getIdCliente());

            return ps.executeUpdate() > 0;
        }
    }
}
