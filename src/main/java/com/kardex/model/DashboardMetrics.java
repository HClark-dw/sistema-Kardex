package com.kardex.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardMetrics {
    private BigDecimal totalIngresos = BigDecimal.ZERO;
    private BigDecimal totalEgresos = BigDecimal.ZERO;
    private BigDecimal utilidadBruta = BigDecimal.ZERO;
    private BigDecimal valoracionTotalInventario = BigDecimal.ZERO;
    private int totalProductos = 0;
    private int productosBajoStock = 0;

    // Métricas por periodo para gráficos (ej. últimos 6 meses o días: Etiqueta -> [Ingresos, Egresos])
    private Map<String, BigDecimal[]> flujoIngresosEgresos = new LinkedHashMap<>();

    private List<Product> productosCriticos = new ArrayList<>();
    private List<Sale> ultimasVentas = new ArrayList<>();
    private List<Purchase> ultimasCompras = new ArrayList<>();

    public DashboardMetrics() {}

    public BigDecimal getTotalIngresos() { return totalIngresos; }
    public void setTotalIngresos(BigDecimal totalIngresos) { this.totalIngresos = totalIngresos; }

    public BigDecimal getTotalEgresos() { return totalEgresos; }
    public void setTotalEgresos(BigDecimal totalEgresos) { this.totalEgresos = totalEgresos; }

    public BigDecimal getUtilidadBruta() { return utilidadBruta; }
    public void setUtilidadBruta(BigDecimal utilidadBruta) { this.utilidadBruta = utilidadBruta; }

    public BigDecimal getValoracionTotalInventario() { return valoracionTotalInventario; }
    public void setValoracionTotalInventario(BigDecimal valoracionTotalInventario) { this.valoracionTotalInventario = valoracionTotalInventario; }

    public int getTotalProductos() { return totalProductos; }
    public void setTotalProductos(int totalProductos) { this.totalProductos = totalProductos; }

    public int getProductosBajoStock() { return productosBajoStock; }
    public void setProductosBajoStock(int productosBajoStock) { this.productosBajoStock = productosBajoStock; }

    public Map<String, BigDecimal[]> getFlujoIngresosEgresos() { return flujoIngresosEgresos; }
    public void setFlujoIngresosEgresos(Map<String, BigDecimal[]> flujoIngresosEgresos) { this.flujoIngresosEgresos = flujoIngresosEgresos; }

    public List<Product> getProductosCriticos() { return productosCriticos; }
    public void setProductosCriticos(List<Product> productosCriticos) { this.productosCriticos = productosCriticos; }

    public List<Sale> getUltimasVentas() { return ultimasVentas; }
    public void setUltimasVentas(List<Sale> ultimasVentas) { this.ultimasVentas = ultimasVentas; }

    public List<Purchase> getUltimasCompras() { return ultimasCompras; }
    public void setUltimasCompras(List<Purchase> ultimasCompras) { this.ultimasCompras = ultimasCompras; }
}
