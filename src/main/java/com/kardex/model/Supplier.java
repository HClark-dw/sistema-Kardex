package com.kardex.model;

public class Supplier {
    private int idProveedor;
    private String documento;
    private String razonSocial;
    private String telefono;
    private String email;
    private String direccion;
    private boolean estado;

    public Supplier() {}

    public Supplier(int idProveedor, String documento, String razonSocial, String telefono, String email, String direccion, boolean estado) {
        this.idProveedor = idProveedor;
        this.documento = documento;
        this.razonSocial = razonSocial;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
        this.estado = estado;
    }

    public int getIdProveedor() { return idProveedor; }
    public void setIdProveedor(int idProveedor) { this.idProveedor = idProveedor; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }

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
        return razonSocial + " (" + documento + ")";
    }
}
