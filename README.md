# 📊 Sistema Desktop de Gestión de Inventarios y Kardex Valorado

Aplicación de escritorio corporativa desarrollada en **Java 21** con persistencia en **MySQL**, diseñada para la administración integral de inventarios, valoración de existencias mediante el método contable de **Promedio Ponderado Móvil (IAS 2 / NIC 2)**, facturación de compras y ventas, dashboard de métricas de ingresos/egresos y generación de reportes financieros exportables.

---

## 🚀 Características Principales

### 1. 🔐 Módulo de Login y Seguridad
* **Autenticación Segura:** Validación de usuarios con contraseñas cifradas en **SHA-256** (con fallback compatible).
* **Gestión de Roles:** Perfiles diferenciados (`ADMINISTRADOR` y `OPERADOR`).
* **Asistente de Conexión MySQL:** Diálogo integrado para configurar host, puerto, base de datos, credenciales y realizar pruebas de conexión en tiempo real sin reiniciar.

### 2. 📈 Dashboard de Métricas Financieras (Ingresos y Egresos)
* **Tarjetas KPI en Tiempo Real:**
  * **Ingresos Totales:** Ventas acumuladas facturadas.
  * **Egresos Totales:** Inversión acumulada en compras a proveedores.
  * **Utilidad Bruta:** Margen financiero real (`Ingresos - Costo de Ventas Histórico`).
  * **Valoración del Inventario:** Monto total valorizado del stock actual (`Stock * Costo Promedio`).
* **Gráfica Comparativa:** Gráfico de barras de alta resolución que visualiza el flujo de ingresos vs. egresos a lo largo del tiempo.
* **Alertas de Stock Crítico:** Lista instantánea de productos cuyas existencias están en o por debajo del stock mínimo.
* **Monitoreo Transaccional:** Acceso directo a las últimas ventas y compras registradas.

### 3. 📦 Módulo de Inventario y Catálogo de Productos
* **Gestión Integral:** Registro, edición y desactivación de productos con SKU / código de barras, nombre, categoría y unidad de medida (`UND`, `KG`, `LT`, `CAJA`, `BOBINA`, etc.).
* **Reglas de Negocio:** El stock y el costo promedio son inmutables por edición manual arbitraria; se actualizan estrictamente a través de transacciones de compras y ventas para preservar la integridad contable del Kardex.
* **Semáforo Visual de Stock:** Identificación inmediata en color rojo y estado `CRÍTICO` cuando el producto requiere reabastecimiento.
* **Exportación Directa:** Exportación del inventario completo a formato **CSV** compatible con Microsoft Excel.

### 4. 📥 Módulo de Registro de Compras (Entradas a Kardex)
* **Facturación a Proveedores:** Selección de proveedor, tipo de comprobante (Factura, Boleta, Guía de Remisión, etc.), serie y correlativo.
* **Cálculo de Promedio Ponderado Móvil:** Cada compra actualiza el costo promedio unitario del producto aplicando la fórmula contable:
  $$\text{Nuevo Costo Promedio} = \frac{(\text{Stock Anterior} \times \text{Costo Promedio Anterior}) + (\text{Cantidad Comprada} \times \text{Costo Compra})}{\text{Stock Anterior} + \text{Cantidad Comprada}}$$
* **Generación de Movimiento:** Registro automático en la tabla `kardex` con tipo `ENTRADA_COMPRA`.
* **Historial Detallado:** Consulta de compras históricas con modal de inspección por líneas de producto.

### 5. 🛒 Módulo de Registro de Ventas (Salidas de Kardex)
* **Venta a Clientes:** Emisión de comprobantes (Boleta de Venta, Factura, Ticket) a clientes registrados.
* **Validación de Existencias en Tiempo Real:** Bloqueo automático que impide facturar cantidades superiores al stock disponible en almacén.
* **Captura de Costo Histórico:** Registra el costo promedio vigente al momento exacto de la salida para calcular la ganancia comercial sin alteraciones futuras.
* **Actualización Inmediata:** Descuento atómico del stock e inserción del registro `SALIDA_VENTA` en el Kardex.
* **Historial y Detalle:** Registro auditable de todas las ventas con cálculo de margen de ganancia por operación.

### 6. 📑 Kardex Físico y Valorado por Producto
* **Libro Kardex Oficial:** Visualización detallada de movimientos por producto:
  * **Entradas:** Cantidad, Costo Unitario y Costo Total.
  * **Salidas:** Cantidad, Costo Unitario y Costo Total.
  * **Saldos Finales:** Cantidad en almacén, Costo Promedio Móvil y Saldo Total Valorado.
* **Filtros por Rango de Fechas:** Auditoría entre fechas específicas (`yyyy-MM-dd`).
* **Exportación Contable:** Descarga en formato CSV listo para declaraciones o auditorías contables.

### 7. 📊 Centro de Reportes
* **Reporte de Inventario Valorado General:** Existencias, costos promedio, valoración total y margen proyectado.
* **Reporte de Ventas y Utilidad Bruta:** Resumen de ingresos, costo de la mercadería vendida y porcentaje de margen de utilidad.
* **Reporte de Compras y Egresos:** Totalización de compras por período y proveedor.
* **Reporte de Stock Crítico y Reposición:** Análisis del déficit de inventario y costo financiero estimado para reabastecer a niveles óptimos.
* **Impresión y PDF:** Soporte para impresión directa o guardado en PDF (`Ctrl + P` / Botón Imprimir).

---

## 🛠 Tecnologías Utilizadas

* **Lenguaje:** Java 21 (LTS).
* **Base de Datos:** MySQL 8.x / MariaDB.
* **Pool de Conexiones:** HikariCP 5.1.0 (conexiones ultrarrápidas y resilientes).
* **Controlador JDBC:** MySQL Connector/J 8.3.0.
* **Interfaz Gráfica (GUI):** Java Swing moderno potenciado con **FlatLaf 3.4.1** (soporte de Modo Claro y Modo Oscuro dinámico, esquinas redondeadas y alta fidelidad visual).
* **Gestor de Construcción:** Apache Maven 3.x.

---

## 🗄 Estructura de la Base de Datos

El script SQL se encuentra en `database/schema.sql` e incluye las siguientes tablas normalizadas:

1. `usuarios`: Cuentas de acceso, contraseñas hash SHA-256 y roles.
2. `categorias`: Familias de productos.
3. `proveedores`: Datos fiscales, contacto y razón social.
4. `clientes`: Clientes comerciales y personas naturales.
5. `productos`: Catálogo, existencias, stock mínimo, precio y costo promedio.
6. `compras` y `detalle_compras`: Registro transaccional de compras a proveedores.
7. `ventas` y `detalle_ventas`: Registro transaccional de ventas a clientes.
8. `kardex`: Libro de movimientos físico-valorados con saldos calculados.

---

## 📋 Credenciales de Demostración Inicial

Al importar `database/schema.sql`, se crean los siguientes usuarios de prueba:

| Usuario | Contraseña | Rol | Acceso |
| :--- | :--- | :--- | :--- |
| **`admin`** | `admin123` | **ADMINISTRADOR** | Acceso total a todas las funciones y ajustes |
| **`operador`** | `operador123` | **OPERADOR** | Operaciones de ventas, compras y consultas |

---

## ⚙ Instrucciones de Instalación y Ejecución

### Paso 1: Configurar la Base de Datos MySQL
1. Inicie su servidor MySQL (mediante XAMPP, Laragon, MySQL Server o Docker).
2. Abra su cliente de base de datos preferido (phpMyAdmin, MySQL Workbench, DBeaver o terminal).
3. Ejecute el script:
   ```sql
   source database/schema.sql;
   ```
   *Esto creará la base de datos `kardex_db`, todas las tablas necesarias y datos iniciales de demostración.*

### Paso 2: Configurar Conexión (Opcional)
Por defecto, la aplicación se conecta a:
* **Host:** `localhost`
* **Puerto:** `3306`
* **Base de Datos:** `kardex_db`
* **Usuario:** `root`
* **Contraseña:** *(vacía)*

Si sus credenciales son distintas, puede editarlas en `src/main/resources/database.properties` o simplemente abrir la aplicación y hacer clic en **"⚙ Configurar Conexión MySQL"** en la pantalla de inicio de sesión.

### Paso 3: Compilar y Ejecutar

#### Opción A: Mediante Maven
```bash
# Compilar y empaquetar el JAR ejecutable
mvn clean package

# Ejecutar el aplicativo
java -jar target/sistema-kardex-1.0.0.jar
```

#### Opción B: Desde cualquier IDE (IntelliJ IDEA, Eclipse, VS Code, NetBeans)
1. Abra el proyecto importando el archivo `pom.xml`.
2. Asegúrese de que el SDK del proyecto esté configurado en **Java 21**.
3. Ejecute la clase principal:
   `com.kardex.Main`

---

## 📁 Arquitectura del Código Fuente

```text
sistema-kardex/
├── pom.xml                                   # Configuración de dependencias y build Java 21
├── README.md                                 # Documentación técnica
├── database/
│   └── schema.sql                            # Estructura DDL y datos demo de MySQL
└── src/
    └── main/
        ├── resources/
        │   └── database.properties           # Archivo de propiedades de conexión MySQL
        └── java/
            └── com/kardex/
                ├── Main.java                 # Punto de entrada de la aplicación
                ├── config/
                │   └── DatabaseConfig.java   # Configuración de HikariCP y DataSource
                ├── model/                    # Entidades del dominio
                │   ├── User.java
                │   ├── Product.java
                │   ├── Category.java
                │   ├── Supplier.java
                │   ├── Customer.java
                │   ├── Purchase.java & PurchaseDetail.java
                │   ├── Sale.java & SaleDetail.java
                │   ├── KardexMovement.java
                │   └── DashboardMetrics.java
                ├── dao/                      # Acceso a datos con transacciones ACID
                │   ├── UserDAO.java
                │   ├── ProductDAO.java
                │   ├── CategoryDAO.java
                │   ├── SupplierDAO.java
                │   ├── CustomerDAO.java
                │   ├── PurchaseDAO.java
                │   ├── SaleDAO.java
                │   ├── KardexDAO.java
                │   └── DashboardDAO.java
                ├── ui/                       # Vistas e interfaz gráfica de usuario
                │   ├── LoginFrame.java       # Ventana de autenticación
                │   ├── MainFrame.java        # Ventana principal y menú lateral
                │   ├── components/           # Componentes visuales personalizados
                │   │   ├── MetricCard.java   # Tarjetas KPI redondeadas
                │   │   └── SimpleBarChart.java # Gráfico de barras comparativo Ingresos/Egresos
                │   └── panels/               # Paneles de cada módulo
                │       ├── DashboardPanel.java
                │       ├── InventoryPanel.java
                │       ├── PurchasesPanel.java
                │       ├── SalesPanel.java
                │       ├── KardexPanel.java
                │       └── ReportsPanel.java
                └── util/                     # Utilidades
                    ├── PasswordHasher.java   # Cifrado SHA-256
                    ├── FormatUtils.java      # Formateo de moneda, cantidades y fechas
                    ├── Exporter.java         # Exportador a formato CSV / Excel
                    └── UITheme.java          # Configuración de FlatLaf y paleta de colores
```
