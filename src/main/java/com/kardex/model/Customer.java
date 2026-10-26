package com.kardex.model;

public class Customer {
    private int idCliente;
    private String documento;
    private String nombreRazonSocial;
    private String telefono;
    private String email;
    private String direccion;
    private boolean estado;

    public Customer() {}

    public Customer(int idCliente, String documento, String nombreRazonSocial, String telefono, String email, String direccion, boolean estado) {
        this.idCliente = idCliente;
        this.documento = documento;
        this.nombreRazonSocial = nombreRazonSocial;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
        this.estado = estado;
    }

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getNombreRazonSocial() { return nombreRazonSocial; }
    public void setNombreRazonSocial(String nombreRazonSocial) { this.nombreRazonSocial = nombreRazonSocial; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public boolean isEstado() { return estado; }
    public void setEstado(boolean estado) { this.estado = estado; }

    @Override
    public String toString() {
        return nombreRazonSocial + " (" + documento + ")";
    }
}
