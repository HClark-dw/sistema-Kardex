package com.kardex.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Product {
    private int idProducto;
    private String codigo;
    private String nombre;
    private int idCategoria;
    private String categoriaNombre;
    private String unidadMedida = "UND";
    private BigDecimal stockActual = BigDecimal.ZERO;
    private BigDecimal stockMinimo = new BigDecimal("5.00");
    private BigDecimal costoPromedio = BigDecimal.ZERO;
    private BigDecimal precioVenta = BigDecimal.ZERO;
    private boolean estado = true;

    public Product() {}

    public Product(int idProducto, String codigo, String nombre, int idCategoria, String unidadMedida,
                   BigDecimal stockActual, BigDecimal stockMinimo, BigDecimal costoPromedio,
                   BigDecimal precioVenta, boolean estado) {
        this.idProducto = idProducto;
        this.codigo = codigo;
        this.nombre = nombre;
        this.idCategoria = idCategoria;
        this.unidadMedida = unidadMedida;
        this.stockActual = stockActual != null ? stockActual : BigDecimal.ZERO;
        this.stockMinimo = stockMinimo != null ? stockMinimo : BigDecimal.ZERO;
        this.costoPromedio = costoPromedio != null ? costoPromedio : BigDecimal.ZERO;
        this.precioVenta = precioVenta != null ? precioVenta : BigDecimal.ZERO;
        this.estado = estado;
    }

    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getIdCategoria() { return idCategoria; }
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }

    public String getCategoriaNombre() { return categoriaNombre; }
    public void setCategoriaNombre(String categoriaNombre) { this.categoriaNombre = categoriaNombre; }

    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }

    public BigDecimal getStockActual() { return stockActual; }
    public void setStockActual(BigDecimal stockActual) { this.stockActual = stockActual; }

    public BigDecimal getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(BigDecimal stockMinimo) { this.stockMinimo = stockMinimo; }

    public BigDecimal getCostoPromedio() { return costoPromedio; }
    public void setCostoPromedio(BigDecimal costoPromedio) { this.costoPromedio = costoPromedio; }

    public BigDecimal getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(BigDecimal precioVenta) { this.precioVenta = precioVenta; }

    public boolean isEstado() { return estado; }
    public void setEstado(boolean estado) { this.estado = estado; }

    public boolean isLowStock() {
        return stockActual.compareTo(stockMinimo) <= 0;
    }

    public BigDecimal getValorTotalInventario() {
        return stockActual.multiply(costoPromedio).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre + " (Stock: " + stockActual + " " + unidadMedida + ")";
    }
}
