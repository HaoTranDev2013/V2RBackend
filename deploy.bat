@echo off
echo ========================================
echo V2RBackend Docker Deployment Script
echo ========================================
echo.

echo Step 1: Cleaning up existing containers...
docker-compose down -v
echo.

echo Step 2: Building and starting containers...
docker-compose up --build -d
echo.

echo Step 3: Waiting for SQL Server to start (30 seconds)...
timeout /t 30 /nobreak
echo.

echo Step 4: Creating database V2RDB...
docker exec sqlserver_container /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "YourStrong@Passw0rd" -C -Q "IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'V2RDB') CREATE DATABASE V2RDB"
if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Database V2RDB created!
) else (
    echo [WARNING] Database creation failed or already exists
)
echo.

echo Step 5: Verifying database...
docker exec sqlserver_container /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "YourStrong@Passw0rd" -C -Q "SELECT name FROM sys.databases WHERE name = 'V2RDB'"
echo.

echo Step 6: Restarting Spring Boot to reconnect...
docker restart springboot_container
timeout /t 15 /nobreak
echo.

echo Step 7: Checking container status...
docker ps
echo.

echo Step 8: Checking Spring Boot logs...
echo --- Spring Boot Logs (last 30 lines) ---
docker logs springboot_container --tail 30
echo.

echo ========================================
echo Deployment Complete!
echo ========================================
echo.
echo Application URLs:
echo - Spring Boot API: http://localhost:8081
echo - Swagger UI: http://localhost:8081/swagger-ui.html
echo.
echo Useful commands:
echo - View logs: docker-compose logs -f
echo - Stop: docker-compose stop
echo - Remove all: docker-compose down -v
echo - Fix database: fix-database.bat
echo.
pause
