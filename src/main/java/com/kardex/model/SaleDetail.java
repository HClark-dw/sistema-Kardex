package com.kardex.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SaleDetail {
    private int idDetalleVenta;
    private int idVenta;
    private int idProducto;
    private String productoCodigo;
    private String productoNombre;
    private BigDecimal cantidad = BigDecimal.ZERO;
    private BigDecimal precioUnitario = BigDecimal.ZERO;
    private BigDecimal costoUnitarioHistorico = BigDecimal.ZERO;
    private BigDecimal subtotal = BigDecimal.ZERO;

    public SaleDetail() {}

    public SaleDetail(int idProducto, String productoCodigo, String productoNombre,
                      BigDecimal cantidad, BigDecimal precioUnitario, BigDecimal costoUnitarioHistorico) {
        this.idProducto = idProducto;
        this.productoCodigo = productoCodigo;
        this.productoNombre = productoNombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.costoUnitarioHistorico = costoUnitarioHistorico;
        this.subtotal = cantidad.multiply(precioUnitario).setScale(2, RoundingMode.HALF_UP);
    }

    public int getIdDetalleVenta() { return idDetalleVenta; }
    public void setIdDetalleVenta(int idDetalleVenta) { this.idDetalleVenta = idDetalleVenta; }

    public int getIdVenta() { return idVenta; }
    public void setIdVenta(int idVenta) { this.idVenta = idVenta; }

    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }

    public String getProductoCodigo() { return productoCodigo; }
    public void setProductoCodigo(String productoCodigo) { this.productoCodigo = productoCodigo; }

    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }

    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
        recalculateSubtotal();
    }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
        recalculateSubtotal();
    }

    public BigDecimal getCostoUnitarioHistorico() { return costoUnitarioHistorico; }
    public void setCostoUnitarioHistorico(BigDecimal costoUnitarioHistorico) {
        this.costoUnitarioHistorico = costoUnitarioHistorico;
    }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public void recalculateSubtotal() {
        if (this.cantidad != null && this.precioUnitario != null) {
            this.subtotal = this.cantidad.multiply(this.precioUnitario).setScale(2, RoundingMode.HALF_UP);
        }
    }

    public BigDecimal getCostoTotalLinea() {
        if (cantidad != null && costoUnitarioHistorico != null) {
            return cantidad.multiply(costoUnitarioHistorico).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
}
