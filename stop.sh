#!/bin/bash

echo "============================================"
echo "  HelenEdu 作业管理系统 - 停止所有服务"
echo "============================================"
echo ""

# 停止后端 (端口 8888)
echo "停止后端服务 (端口 8888)..."
BACKEND_PID=$(lsof -ti:8888 2>/dev/null)
if [ -n "$BACKEND_PID" ]; then
    echo "  停止进程 PID: $BACKEND_PID"
    kill -9 $BACKEND_PID 2>/dev/null
    echo "  后端已停止"
else
    echo "  后端未运行"
fi

# 停止前端 (端口 5173)
echo "停止前端服务 (端口 5173)..."
FRONTEND_PID=$(lsof -ti:5173 2>/dev/null)
if [ -n "$FRONTEND_PID" ]; then
    echo "  停止进程 PID: $FRONTEND_PID"
    kill -9 $FRONTEND_PID 2>/dev/null
    echo "  前端已停止"
else
    echo "  前端未运行"
fi

# 清理 PID 文件
rm -f /tmp/helenedu-backend.pid /tmp/helenedu-frontend.pid

echo ""
echo "所有服务已停止"
