package com.kardex.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class KardexMovement {
    private int idKardex;
    private int idProducto;
    private String productoCodigo;
    private String productoNombre;
    private LocalDateTime fecha;
    private String tipoMovimiento; // SALDO_INICIAL, ENTRADA_COMPRA, SALIDA_VENTA, AJUSTE_POSITIVO, AJUSTE_NEGATIVO
    private String tipoComprobante;
    private String documentoReferencia;
    private String motivo;

    // Entradas
    private BigDecimal entradaCantidad = BigDecimal.ZERO;
    private BigDecimal entradaCostoUnitario = BigDecimal.ZERO;
    private BigDecimal entradaCostoTotal = BigDecimal.ZERO;

    // Salidas
    private BigDecimal salidaCantidad = BigDecimal.ZERO;
    private BigDecimal salidaCostoUnitario = BigDecimal.ZERO;
    private BigDecimal salidaCostoTotal = BigDecimal.ZERO;

    // Saldos Resultantes
    private BigDecimal saldoCantidad = BigDecimal.ZERO;
    private BigDecimal saldoCostoUnitario = BigDecimal.ZERO;
    private BigDecimal saldoCostoTotal = BigDecimal.ZERO;

    public KardexMovement() {}

    public int getIdKardex() { return idKardex; }
    public void setIdKardex(int idKardex) { this.idKardex = idKardex; }

    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }

    public String getProductoCodigo() { return productoCodigo; }
    public void setProductoCodigo(String productoCodigo) { this.productoCodigo = productoCodigo; }

    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(String tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }

    public String getTipoComprobante() { return tipoComprobante; }
    public void setTipoComprobante(String tipoComprobante) { this.tipoComprobante = tipoComprobante; }

    public String getDocumentoReferencia() { return documentoReferencia; }
    public void setDocumentoReferencia(String documentoReferencia) { this.documentoReferencia = documentoReferencia; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public BigDecimal getEntradaCantidad() { return entradaCantidad; }
    public void setEntradaCantidad(BigDecimal entradaCantidad) { this.entradaCantidad = entradaCantidad; }

    public BigDecimal getEntradaCostoUnitario() { return entradaCostoUnitario; }
    public void setEntradaCostoUnitario(BigDecimal entradaCostoUnitario) { this.entradaCostoUnitario = entradaCostoUnitario; }

    public BigDecimal getEntradaCostoTotal() { return entradaCostoTotal; }
    public void setEntradaCostoTotal(BigDecimal entradaCostoTotal) { this.entradaCostoTotal = entradaCostoTotal; }

    public BigDecimal getSalidaCantidad() { return salidaCantidad; }
    public void setSalidaCantidad(BigDecimal salidaCantidad) { this.salidaCantidad = salidaCantidad; }

    public BigDecimal getSalidaCostoUnitario() { return salidaCostoUnitario; }
    public void setSalidaCostoUnitario(BigDecimal salidaCostoUnitario) { this.salidaCostoUnitario = salidaCostoUnitario; }

    public BigDecimal getSalidaCostoTotal() { return salidaCostoTotal; }
    public void setSalidaCostoTotal(BigDecimal salidaCostoTotal) { this.salidaCostoTotal = salidaCostoTotal; }

    public BigDecimal getSaldoCantidad() { return saldoCantidad; }
    public void setSaldoCantidad(BigDecimal saldoCantidad) { this.saldoCantidad = saldoCantidad; }

    public BigDecimal getSaldoCostoUnitario() { return saldoCostoUnitario; }
    public void setSaldoCostoUnitario(BigDecimal saldoCostoUnitario) { this.saldoCostoUnitario = saldoCostoUnitario; }

    public BigDecimal getSaldoCostoTotal() { return saldoCostoTotal; }
    public void setSaldoCostoTotal(BigDecimal saldoCostoTotal) { this.saldoCostoTotal = saldoCostoTotal; }
}
