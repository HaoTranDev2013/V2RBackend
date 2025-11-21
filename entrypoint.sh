#!/bin/bash

# Start SQL Server in the background
/opt/mssql/bin/sqlservr &

# Wait for SQL Server to start
sleep 30s

# Create the database if it doesn't exist
/opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P YourStrong@Passw0rd -C -Q "IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'V2RDB') CREATE DATABASE V2RDB"

# Keep the container running
wait
