@echo off
chcp 65001 >nul
title HelenEdu 停止服务
echo ============================================
echo   HelenEdu 作业管理系统 - 停止所有服务
echo ============================================
echo.

echo 停止后端服务 (端口 8888)...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8888" ^| findstr "LISTENING"') do (
    echo   停止进程 PID: %%a
    taskkill /PID %%a /F >nul 2>&1
)

echo 停止前端服务 (端口 5173)...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":5173" ^| findstr "LISTENING"') do (
    echo   停止进程 PID: %%a
    taskkill /PID %%a /F >nul 2>&1
)

echo.
echo 所有服务已停止
