@echo off
echo [*] Cleaning port 8080...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080') do (
    taskkill /F /PID %%a >nul 2>&1
)
echo [*] Starting Spring Boot Backend on port 8080...
call "D:\Chien\bai\IntelliJ IDEA 2026.1.3\plugins\maven\lib\maven3\bin\mvn.cmd" spring-boot:run
