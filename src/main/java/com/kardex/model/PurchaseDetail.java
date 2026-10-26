package com.kardex.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PurchaseDetail {
    private int idDetalleCompra;
    private int idCompra;
    private int idProducto;
    private String productoCodigo;
    private String productoNombre;
    private BigDecimal cantidad = BigDecimal.ZERO;
    private BigDecimal costoUnitario = BigDecimal.ZERO;
    private BigDecimal subtotal = BigDecimal.ZERO;

    public PurchaseDetail() {}

    public PurchaseDetail(int idProducto, String productoCodigo, String productoNombre,
                          BigDecimal cantidad, BigDecimal costoUnitario) {
        this.idProducto = idProducto;
        this.productoCodigo = productoCodigo;
        this.productoNombre = productoNombre;
        this.cantidad = cantidad;
        this.costoUnitario = costoUnitario;
        this.subtotal = cantidad.multiply(costoUnitario).setScale(2, RoundingMode.HALF_UP);
    }

    public int getIdDetalleCompra() { return idDetalleCompra; }
    public void setIdDetalleCompra(int idDetalleCompra) { this.idDetalleCompra = idDetalleCompra; }

    public int getIdCompra() { return idCompra; }
    public void setIdCompra(int idCompra) { this.idCompra = idCompra; }

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

    public BigDecimal getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(BigDecimal costoUnitario) {
        this.costoUnitario = costoUnitario;
        recalculateSubtotal();
    }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public void recalculateSubtotal() {
        if (this.cantidad != null && this.costoUnitario != null) {
            this.subtotal = this.cantidad.multiply(this.costoUnitario).setScale(2, RoundingMode.HALF_UP);
        }
    }
}
