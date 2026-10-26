package com.kardex.dao;

import com.kardex.config.DatabaseConfig;
import com.kardex.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    public List<Category> listAll() throws SQLException {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT id_categoria, nombre, descripcion, estado FROM categorias WHERE estado = TRUE ORDER BY nombre";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Category(
                    rs.getInt("id_categoria"),
                    rs.getString("nombre"),
                    rs.getString("descripcion"),
                    rs.getBoolean("estado")
                ));
            }
        }
        return list;
    }

    public boolean insert(Category category) throws SQLException {
        String sql = "INSERT INTO categorias (nombre, descripcion, estado) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, category.getNombre());
            ps.setString(2, category.getDescripcion());
            ps.setBoolean(3, category.isEstado());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        category.setIdCategoria(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(Category category) throws SQLException {
        String sql = "UPDATE categorias SET nombre = ?, descripcion = ?, estado = ? WHERE id_categoria = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, category.getNombre());
            ps.setString(2, category.getDescripcion());
            ps.setBoolean(3, category.isEstado());
            ps.setInt(4, category.getIdCategoria());

            return ps.executeUpdate() > 0;
        }
    }
}
