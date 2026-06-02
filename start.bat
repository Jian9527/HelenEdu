@echo off
chcp 65001 >nul
title HelenEdu 一键启动
echo ============================================
echo   HelenEdu 作业管理系统 - 一键启动
echo ============================================
echo.

:: 设置项目根目录
set PROJECT_ROOT=%~dp0
set BACKEND_DIR=%PROJECT_ROOT%helenedu-backend
set FRONTEND_DIR=%PROJECT_ROOT%helenedu-frontend

:: ========== 1. 停止已有服务 ==========
echo [1/4] 停止已有服务...

:: 停止后端 (端口 8888)
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8888" ^| findstr "LISTENING"') do (
    echo   停止后端进程 PID: %%a
    taskkill /PID %%a /F >nul 2>&1
)

:: 停止前端 (端口 5173)
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":5173" ^| findstr "LISTENING"') do (
    echo   停止前端进程 PID: %%a
    taskkill /PID %%a /F >nul 2>&1
)

echo   已有服务已停止
echo.

:: ========== 2. 启动 MySQL ==========
echo [2/4] 检查 MySQL 服务...
sc query MySQL80 | find "RUNNING" >nul 2>&1
if errorlevel 1 (
    echo   MySQL 未运行，正在启动...
    net start MySQL80
    if errorlevel 1 (
        echo   [错误] MySQL 启动失败，请手动启动后重试
        echo   提示：可能需要以管理员身份运行此脚本
        pause
        exit /b 1
    )
    echo   MySQL 启动成功
) else (
    echo   MySQL 已在运行
)
echo.

:: ========== 3. 启动后端 ==========
echo [3/4] 启动后端服务 (端口 8888)...
start "HelenEdu-Backend" cmd /k "cd /d %BACKEND_DIR% && echo 后端启动中... && mvn spring-boot:run"

:: 等待后端启动
echo   等待后端启动完成...
timeout /t 8 /nobreak >nul
set /a retry=0
:wait_backend
set /a retry+=1
if %retry% gtr 15 (
    echo   [警告] 后端启动超时，请检查后端窗口
    goto start_frontend
)
powershell -Command "try { $c = New-Object System.Net.Sockets.TcpClient('localhost', 8888); $c.Close(); exit 0 } catch { exit 1 }" >nul 2>&1
if errorlevel 1 (
    echo   等待后端... (%retry%/15)
    timeout /t 2 /nobreak >nul
    goto wait_backend
)
echo   后端启动成功！
echo.

:: ========== 4. 启动前端 ==========
:start_frontend
echo [4/4] 启动前端 H5 服务 (端口 5173)...
start "HelenEdu-Frontend" cmd /k "cd /d %FRONTEND_DIR% && echo 前端启动中... && npm.cmd run dev:h5"

echo.
echo ============================================
echo   启动完成！
echo   后端: http://localhost:8888
echo   前端: http://localhost:5173
echo ============================================
echo.
echo 按任意键打开浏览器...
pause >nul
start http://localhost:5173
