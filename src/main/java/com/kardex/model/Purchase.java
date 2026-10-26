package com.kardex.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Purchase {
    private int idCompra;
    private int idProveedor;
    private String proveedorNombre;
    private int idUsuario;
    private String usuarioNombre;
    private String tipoComprobante = "Factura";
    private String serieNumero;
    private LocalDateTime fecha = LocalDateTime.now();
    private BigDecimal total = BigDecimal.ZERO;
    private String observaciones;
    private List<PurchaseDetail> detalles = new ArrayList<>();

    public Purchase() {}

    public int getIdCompra() { return idCompra; }
    public void setIdCompra(int idCompra) { this.idCompra = idCompra; }

    public int getIdProveedor() { return idProveedor; }
    public void setIdProveedor(int idProveedor) { this.idProveedor = idProveedor; }

    public String getProveedorNombre() { return proveedorNombre; }
    public void setProveedorNombre(String proveedorNombre) { this.proveedorNombre = proveedorNombre; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

    public String getTipoComprobante() { return tipoComprobante; }
    public void setTipoComprobante(String tipoComprobante) { this.tipoComprobante = tipoComprobante; }

    public String getSerieNumero() { return serieNumero; }
    public void setSerieNumero(String serieNumero) { this.serieNumero = serieNumero; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public List<PurchaseDetail> getDetalles() { return detalles; }
    public void setDetalles(List<PurchaseDetail> detalles) {
        this.detalles = detalles;
        recalculateTotal();
    }

    public void addDetail(PurchaseDetail item) {
        this.detalles.add(item);
        recalculateTotal();
    }

    public void recalculateTotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (PurchaseDetail d : detalles) {
            if (d.getSubtotal() != null) {
                sum = sum.add(d.getSubtotal());
            }
        }
        this.total = sum;
    }
}
