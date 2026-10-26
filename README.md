Valued Kardex & Desktop Inventory Management System

🇺🇸 English Version

Corporate desktop application developed in Java 21 with MySQL persistence, designed for comprehensive inventory management, stock valuation using the Moving Weighted Average (IAS 2 / NIC 2) accounting method, purchase and sales invoicing, an income/expense metrics dashboard, and exportable financial reports generation.

## 🚀 Key Features

### 1. 🔐 Login & Security Module

* **Secure Authentication:** User validation with SHA-256 encrypted passwords (with backwards-compatible fallback).

* **Role Management:** Differentiated access profiles (ADMINISTRATOR and OPERATOR).

* **MySQL Connection Wizard:** Integrated setup dialog to configure host, port, database, credentials, and test real-time connectivity without restarting.

## 2. 📈 Financial Metrics Dashboard (Income & Expenses)

### Real-Time KPI Cards:

* **Total Income:** Accumulated invoiced sales.

  * **Total Expenses:** Accumulated investment in supplier purchases.

  * **Gross Profit:** Real financial margin (Income - Historical Cost of Goods Sold).

  * **Inventory Valuation:** Total valued amount of current stock (Stock * Average Cost).

* **Comparative Chart:** High-resolution bar chart visualizing cash inflow vs. outflow trends over time.

* **Critical Stock Alerts:** Instant list of products whose stock levels are at or below minimum safety limits.

* **Transactional Monitoring:** Quick access feed to recently logged sales and purchases.

### 3. 📦 Inventory & Product Catalog Module

* **Comprehensive Management:** Create, edit, and deactivate products with SKU/barcode, name, category, and unit of measure (UNIT, KG, LT, BOX, COIL, etc.).

* **Strict Business Rules:** Stock and average cost are immutable via arbitrary manual edits; they update strictly through purchase and sale transactions to preserve Kardex accounting integrity.

* **Visual Stock Indicator:** Immediate red status (CRITICAL) when a product requires replenishment.

* **Direct Export:** Complete inventory export to Microsoft Excel-compatible CSV format.

### 4. 📥 Purchase Registration Module (Kardex Inflow)
- **Supplier Invoicing:** Selection of supplier, document type (Invoice, Receipt, Dispatch Note, etc.), series, and sequential number.
- **Moving Weighted Average Calculation:** Each purchase updates the unit average cost of the product by applying the accounting formula:
  $$\text{New Average Cost} = \frac{(\text{Previous Stock} \times \text{Previous Average Cost}) + (\text{Purchased Quantity} \times \text{Purchase Cost})}{\text{Previous Stock} + \text{Purchased Quantity}}$$
- **Movement Generation:** Automatic entry in the `kardex` table with `PURCHASE_ENTRY` type.
- **Detailed History:** Query of historical purchases with an inspection modal for line items.

### 5. 🛒 Sales Registration Module (Kardex Outflow)
- **Customer Sales:** Issuance of sales receipts (Receipt, Sales Invoice, Ticket) to registered customers.
- **Real-Time Stock Validation:** Automatic lock that prevents invoicing quantities higher than the available warehouse stock.
- **Historical Cost Capture:** Records the active average cost at the exact moment of outflow to calculate commercial profit without future alterations.
- **Immediate Update:** Atomic stock deduction and insertion of the `SALE_OUTFLOW` record in the Kardex.
- **History and Details:** Auditable record of all sales with profit margin calculation per operation.

### 6. 📑 Physical and Valued Kardex per Product
- **Official Kardex Ledger:** Detailed view of movements per product:
  * **Inflows:** Quantity, Unit Cost, and Total Cost.
  * **Outflows:** Quantity, Unit Cost, and Total Cost.
  * **Ending Balances:** Warehouse quantity, Moving Average Cost, and Total Valued Balance.
- **Date Range Filters:** Audit between specific dates (`yyyy-MM-dd`).
- **Accounting Export:** Download in CSV format ready for tax filings or accounting audits.

### 7. 📊 Reports Center
- **General Valued Inventory Report:** Stock levels, average costs, total valuation, and projected margin.
- **Sales and Gross Profit Report:** Summary of revenue, cost of goods sold, and profit margin percentage.
- **Purchases and Expenses Report:** Total purchases per period and supplier.
- **Critical Stock and Replenishment Report:** Analysis of inventory deficits and estimated financial cost to replenish to optimal levels.
- **Printing and PDF:** Support for direct printing or saving to PDF (`Ctrl + P` / Print Button).

---

## 🛠 Tech Stack

- **Language:** Java 21 (LTS).
- **Database:** MySQL 8.x / MariaDB.
- **Connection Pool:** HikariCP 5.1.0 (ultra-fast and resilient connections).
- **JDBC Driver:** MySQL Connector/J 8.3.0.
- **GUI Framework:** Modern Java Swing powered by **FlatLaf 3.4.1** (Dynamic Light and Dark Mode support, rounded corners, and high visual fidelity).
- **Build Tool:** Apache Maven 3.x.

---

## 🗄 Database Schema

The SQL script is located at `database/schema.sql` and includes the following normalized tables:

1. `usuarios`: Access accounts, SHA-256 hashed passwords, and roles.
2. `categorias`: Product categories.
3. `proveedores`: Tax data, contact info, and company name.
4. `clientes`: Commercial clients and individual customers.
5. `productos`: Catalog, stock levels, minimum stock, price, and average cost.
6. `compras` & `detalle_compras`: Transactional log for supplier purchases.
7. `ventas` & `detalle_ventas`: Transactional log for customer sales.
8. `kardex`: Physical-valued movement ledger with calculated balances.

---

## 📋 Initial Demo Credentials

Upon importing `database/schema.sql`, the following test users are created:

| User | Password | Role | Access Level |
| :--- | :--- | :--- | :--- |
| **`admin`** | `admin123` | **ADMINISTRATOR** | Full access to all features and settings |
| **`operador`** | `operador123` | **OPERADOR** | Operational sales, purchases, and queries |

---

## ⚙ Installation and Setup Instructions

### Step 1: Configure MySQL Database
1. Start your MySQL server (via XAMPP, Laragon, MySQL Server, or Docker).
2. Open your preferred database client (phpMyAdmin, MySQL Workbench, DBeaver, or terminal).
3. Execute the script:
   ```sql
   source database/schema.sql;

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
