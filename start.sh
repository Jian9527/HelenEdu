#!/bin/bash

echo "============================================"
echo "  HelenEdu 作业管理系统 - 一键启动"
echo "============================================"
echo ""

# 项目根目录（脚本所在目录）
PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
BACKEND_DIR="$PROJECT_ROOT/helenedu-backend"
FRONTEND_DIR="$PROJECT_ROOT/helenedu-frontend"

# ========== 1. 停止已有服务 ==========
echo "[1/3] 停止已有服务..."

# 停止后端 (端口 8888)
BACKEND_PID=$(lsof -ti:8888 2>/dev/null)
if [ -n "$BACKEND_PID" ]; then
    echo "  停止后端进程 PID: $BACKEND_PID"
    kill -9 $BACKEND_PID 2>/dev/null
fi

# 停止前端 (端口 5173)
FRONTEND_PID=$(lsof -ti:5173 2>/dev/null)
if [ -n "$FRONTEND_PID" ]; then
    echo "  停止前端进程 PID: $FRONTEND_PID"
    kill -9 $FRONTEND_PID 2>/dev/null
fi

echo "  已有服务已停止"
echo ""

# ========== 2. 启动后端 ==========
echo "[2/3] 启动后端服务 (端口 8888)..."

cd "$BACKEND_DIR" || exit 1
nohup mvn spring-boot:run > /tmp/helenedu-backend.log 2>&1 &
BACKEND_PID=$!
echo "  后端进程 PID: $BACKEND_PID"

# 等待后端启动
echo "  等待后端启动完成..."
RETRY=0
MAX_RETRY=30
while [ $RETRY -lt $MAX_RETRY ]; do
    if curl -s -o /dev/null --connect-timeout 2 http://localhost:8888 2>/dev/null; then
        echo "  后端启动成功！"
        break
    fi
    RETRY=$((RETRY + 1))
    echo "  等待后端... ($RETRY/$MAX_RETRY)"
    sleep 2
done

if [ $RETRY -eq $MAX_RETRY ]; then
    echo "  [警告] 后端启动超时，请检查日志: /tmp/helenedu-backend.log"
fi
echo ""

# ========== 3. 启动前端 ==========
echo "[3/3] 启动前端 H5 服务 (端口 5173)..."

cd "$FRONTEND_DIR" || exit 1
nohup npm run dev:h5 > /tmp/helenedu-frontend.log 2>&1 &
FRONTEND_PID=$!
echo "  前端进程 PID: $FRONTEND_PID"
echo ""

# 保存 PID 以便停止
echo "$BACKEND_PID" > /tmp/helenedu-backend.pid
echo "$FRONTEND_PID" > /tmp/helenedu-frontend.pid

echo "============================================"
echo "  启动完成！"
echo "  后端: http://localhost:8888"
echo "  前端: http://localhost:5173"
echo ""
echo "  日志文件:"
echo "    后端: /tmp/helenedu-backend.log"
echo "    前端: /tmp/helenedu-frontend.log"
echo ""
echo "  停止服务: ./stop.sh"
echo "============================================"
