@echo off
chcp 65001 >nul
echo ========================================
echo   COMPILADOR - SISTEMA OSINT
echo ========================================
echo.

echo [1/2] Compilando ServidorOSINT.java...
javac -encoding UTF-8 ServidorOSINT.java
if %errorlevel% neq 0 (
    echo [ERRO] Falha ao compilar ServidorOSINT.java
    pause
    exit /b 1
)
echo [OK] ServidorOSINT.java compilado com sucesso!
echo.

echo [2/2] Compilando ClienteOSINT.java...
javac -encoding UTF-8 ClienteOSINT.java
if %errorlevel% neq 0 (
    echo [ERRO] Falha ao compilar ClienteOSINT.java
    pause
    exit /b 1
)
echo [OK] ClienteOSINT.java compilado com sucesso!
echo.

echo ========================================
echo   COMPILACAO CONCLUIDA COM SUCESSO!
echo ========================================
echo.
echo Arquivos gerados:
echo  - ServidorOSINT.class
echo  - ClienteOSINT.class
echo  - (e classes internas)
echo.
pause
