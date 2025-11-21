@echo off
echo ============================================
echo FIX: Create V2RDB Database
echo ============================================
echo.

echo Step 1: Creating database V2RDB in SQL Server...
docker exec sqlserver_container /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "YourStrong@Passw0rd" -C -Q "IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'V2RDB') CREATE DATABASE V2RDB"

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Database created successfully!
) else (
    echo [ERROR] Failed to create database. Checking if SQL Server is running...
    docker ps | findstr sqlserver_container
)

echo.
echo Step 2: Verifying database exists...
docker exec sqlserver_container /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "YourStrong@Passw0rd" -C -Q "SELECT name FROM sys.databases WHERE name = 'V2RDB'"

echo.
echo Step 3: Restarting Spring Boot container...
docker restart springboot_container

echo.
echo Step 4: Waiting for Spring Boot to start (15 seconds)...
timeout /t 15 /nobreak

echo.
echo Step 5: Checking Spring Boot logs...
docker logs springboot_container --tail 20

echo.
echo ============================================
echo Done! Check if the application started successfully.
echo ============================================
pause
