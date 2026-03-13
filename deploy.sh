#!/bin/bash

# 检查 Docker 是否安装
if ! command -v docker &> /dev/null; then
    echo "错误: 未找到 Docker。请先安装 Docker。"
    exit 1
fi

# 检查 Docker Compose 是否安装
if ! command -v docker-compose &> /dev/null; then
    echo "错误: 未找到 Docker Compose。请先安装 Docker Compose。"
    exit 1
fi

echo "开始部署..."

# 停止并移除旧容器
echo "停止旧容器..."
docker-compose down

# 构建并启动新容器
echo "构建并启动新容器..."
docker-compose up -d --build

echo "部署完成！"
echo "应用正在运行在端口 8081"
echo "查看日志: docker-compose logs -f"
