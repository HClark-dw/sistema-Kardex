package com.kardex.model;

import java.time.LocalDateTime;

public class User {
    private int idUsuario;
    private String username;
    private String passwordHash;
    private String nombreCompleto;
    private String rol; // 'ADMINISTRADOR', 'OPERADOR'
    private boolean estado;
    private LocalDateTime fechaCreacion;

    public User() {}

    public User(int idUsuario, String username, String nombreCompleto, String rol, boolean estado) {
        this.idUsuario = idUsuario;
        this.username = username;
        this.nombreCompleto = nombreCompleto;
        this.rol = rol;
        this.estado = estado;
    }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public boolean isEstado() { return estado; }
    public void setEstado(boolean estado) { this.estado = estado; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public boolean isAdmin() {
        return "ADMINISTRADOR".equalsIgnoreCase(this.rol);
    }

    @Override
    public String toString() {
        return nombreCompleto + " (" + username + ")";
    }
}
