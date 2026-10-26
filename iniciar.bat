@echo off
chcp 65001 > nul
echo =================================================================
echo  Iniciando Sistema Kardex Valorado (Java 21)
echo =================================================================

if exist "target\sistema-kardex-1.0.0.jar" (
    java -jar "target\sistema-kardex-1.0.0.jar"
) else (
    echo [AVISO] No se encontro el archivo empaquetado 'target\sistema-kardex-1.0.0.jar'.
    echo Ejecutando compilacion primero...
    call compilar.bat
    if exist "target\sistema-kardex-1.0.0.jar" (
        java -jar "target\sistema-kardex-1.0.0.jar"
    )
)
