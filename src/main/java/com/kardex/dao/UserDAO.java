package com.kardex.dao;

import com.kardex.config.DatabaseConfig;
import com.kardex.model.User;
import com.kardex.util.PasswordHasher;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

  /*   public User authenticate(String username, String password) throws SQLException {
        String sql = "SELECT id_usuario, username, password_hash, nombre_completo, rol, estado, fecha_creacion " +
                     "FROM usuarios WHERE username = ? AND estado = TRUE";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (PasswordHasher.verify(password, storedHash)) {
                        User user = new User();
                        user.setIdUsuario(rs.getInt("id_usuario"));
                        user.setUsername(rs.getString("username"));
                        user.setPasswordHash(storedHash);
                        user.setNombreCompleto(rs.getString("nombre_completo"));
                        user.setRol(rs.getString("rol"));
                        user.setEstado(rs.getBoolean("estado"));
                        Timestamp ts = rs.getTimestamp("fecha_creacion");
                        if (ts != null) user.setFechaCreacion(ts.toLocalDateTime());
                        return user;
                    }
                }
            }
        }
        return null;
    }*/

        public User authenticate(String username, String password) throws SQLException {
    String sql = "SELECT id_usuario, username, password_hash, nombre_completo, rol, estado, fecha_creacion " +
                 "FROM usuarios WHERE username = ? AND estado = TRUE";
    
    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        
        ps.setString(1, username);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String storedPassword = rs.getString("password_hash");
                
                // Validación directa en texto plano
                if (password.equals(storedPassword)) {
                    User user = new User();
                    user.setIdUsuario(rs.getInt("id_usuario"));
                    user.setUsername(rs.getString("username"));
                    user.setPasswordHash(storedPassword);
                    user.setNombreCompleto(rs.getString("nombre_completo"));
                    user.setRol(rs.getString("rol"));
                    user.setEstado(rs.getBoolean("estado"));
                    Timestamp ts = rs.getTimestamp("fecha_creacion");
                    if (ts != null) user.setFechaCreacion(ts.toLocalDateTime());
                    return user;
                }
            }
        }
    }
    return null;
}

    public List<User> listAll() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT id_usuario, username, nombre_completo, rol, estado, fecha_creacion FROM usuarios ORDER BY id_usuario";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                User u = new User();
                u.setIdUsuario(rs.getInt("id_usuario"));
                u.setUsername(rs.getString("username"));
                u.setNombreCompleto(rs.getString("nombre_completo"));
                u.setRol(rs.getString("rol"));
                u.setEstado(rs.getBoolean("estado"));
                Timestamp ts = rs.getTimestamp("fecha_creacion");
                if (ts != null) u.setFechaCreacion(ts.toLocalDateTime());
                list.add(u);
            }
        }
        return list;
    }

    /*public boolean insert(User user, String rawPassword) throws SQLException {
        String sql = "INSERT INTO usuarios (username, password_hash, nombre_completo, rol, estado) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, PasswordHasher.hash(rawPassword));
            ps.setString(3, user.getNombreCompleto());
            ps.setString(4, user.getRol());
            ps.setBoolean(5, user.isEstado());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setIdUsuario(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }*/
   public boolean insert(User user, String rawPassword) throws SQLException {
    String sql = "INSERT INTO usuarios (username, password_hash, nombre_completo, rol, estado) VALUES (?, ?, ?, ?, ?)";
    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

        ps.setString(1, user.getUsername());
        ps.setString(2, rawPassword); // Cambio: Guarda la contraseña en texto plano
        ps.setString(3, user.getNombreCompleto());
        ps.setString(4, user.getRol());
        ps.setBoolean(5, user.isEstado());

        int affected = ps.executeUpdate();
        if (affected > 0) {
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setIdUsuario(rs.getInt(1));
                }
            }
            return true;
        }
    }
    return false;
}
}
