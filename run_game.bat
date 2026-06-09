@echo off
setlocal

echo ===========================================================
echo   INICIANDO TOWER DEFENSE (MODO COMPATIBILIDAD JAVA 21)
echo ===========================================================

:: Intenta buscar automaticamente donde se instalo Java 21 para bypassear Java 25
for /d %%I in ("C:\Program Files\Eclipse Adoptium\jdk-21*") do (
    set "JAVA_HOME=%%I"
    goto :encontrado
)
for /d %%I in ("C:\Program Files\Microsoft\jdk-21*") do (
    set "JAVA_HOME=%%I"
    goto :encontrado
)
for /d %%I in ("C:\Program Files\Java\jdk-21*") do (
    set "JAVA_HOME=%%I"
    goto :encontrado
)

:encontrado
if defined JAVA_HOME (
    echo [OK] Java 21 detectado en: %JAVA_HOME%
    echo [OK] Gradle se aislara de tu Java 25 global.
) else (
    echo [ALERTA] No se encontro una instalacion de Java 21 automaticamente.
    echo Intentando correr el juego con el Java por defecto...
)

echo.
call .\gradlew.bat run
echo.
pause
endlocal
