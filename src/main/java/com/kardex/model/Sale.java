package com.kardex.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Sale {
    private int idVenta;
    private int idCliente;
    private String clienteNombre;
    private int idUsuario;
    private String usuarioNombre;
    private String tipoComprobante = "Boleta";
    private String serieNumero;
    private LocalDateTime fecha = LocalDateTime.now();
    private BigDecimal total = BigDecimal.ZERO;
    private BigDecimal costoTotalEstimado = BigDecimal.ZERO;
    private String observaciones;
    private List<SaleDetail> detalles = new ArrayList<>();

    public Sale() {}

    public int getIdVenta() { return idVenta; }
    public void setIdVenta(int idVenta) { this.idVenta = idVenta; }

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

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

    public BigDecimal getCostoTotalEstimado() { return costoTotalEstimado; }
    public void setCostoTotalEstimado(BigDecimal costoTotalEstimado) { this.costoTotalEstimado = costoTotalEstimado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public List<SaleDetail> getDetalles() { return detalles; }
    public void setDetalles(List<SaleDetail> detalles) {
        this.detalles = detalles;
        recalculateTotals();
    }

    public void addDetail(SaleDetail item) {
        this.detalles.add(item);
        recalculateTotals();
    }

    public void recalculateTotals() {
        BigDecimal totalSum = BigDecimal.ZERO;
        BigDecimal costSum = BigDecimal.ZERO;
        for (SaleDetail d : detalles) {
            if (d.getSubtotal() != null) {
                totalSum = totalSum.add(d.getSubtotal());
            }
            if (d.getCostoTotalLinea() != null) {
                costSum = costSum.add(d.getCostoTotalLinea());
            }
        }
        this.total = totalSum;
        this.costoTotalEstimado = costSum;
    }

    public BigDecimal getUtilidadBruta() {
        if (total != null && costoTotalEstimado != null) {
            return total.subtract(costoTotalEstimado);
        }
        return BigDecimal.ZERO;
    }
}
