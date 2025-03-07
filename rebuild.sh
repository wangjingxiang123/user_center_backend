#!/bin/bash

# 删除旧镜像
docker rmi user-center-backend:latest 2> /dev/null

# 重新构建镜像（使用阿里云代理）
docker build -t user-center-backend:latest . 

# 显示构建结果
docker images | grep user-center-backend