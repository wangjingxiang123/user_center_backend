# 构建阶段
FROM maven:3.8.6-eclipse-temurin-8-alpine as builder

WORKDIR /app

# 复制Maven配置和项目文件
COPY .mvn/settings.xml /root/.m2/
COPY pom.xml .

# 优化依赖解析步骤（使用阿里云镜像）
RUN mvn -B -s /root/.m2/settings.xml \
    dependency:go-offline \
    dependency:resolve-plugins \
    dependency:resolve \
    --no-transfer-progress \
    --fail-never \
    -Dmdep.prependGroupId=true 

# 复制源代码（最后复制以利用缓存）
COPY src ./src

# 构建应用（添加构建参数优化）
RUN mvn -B package -s /root/.m2/settings.xml \
    -DskipTests \
    -Dmaven.test.skip=true \
    -Dmaven.compile.fork=true \
    -Dmaven.artifact.threads=10 \
    --no-transfer-progress

# 生产阶段
FROM openjdk:8-jre-alpine

WORKDIR /app
# 从构建阶段复制jar包
COPY --from=builder /app/target/user-center-backend-*.jar /app/user-center-backend.jar

# 开放端口
EXPOSE 8080

# 启动应用
CMD ["java","-jar","/app/user-center-backend.jar","--spring.profiles.active=prod"]