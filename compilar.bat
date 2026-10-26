@echo off
chcp 65001 > nul
echo =================================================================
echo  Compilando Sistema Kardex Valorado (Java 21) con Maven
echo =================================================================

where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] No se encontro 'mvn' en el PATH del sistema.
    echo Asegurate de tener Apache Maven instalado o abre el proyecto en tu IDE preferido
    echo (IntelliJ IDEA, Eclipse, VS Code o NetBeans) para compilar automaticamente.
    pause
    exit /b 1
)

call mvn clean package
if %errorlevel% equ 0 (
    echo.
    echo =================================================================
    echo  [OK] Compilacion exitosa. JAR generado en target/
    echo =================================================================
) else (
    echo.
    echo [ERROR] Fallo la compilacion. Revisa los mensajes anteriores.
)
pause
