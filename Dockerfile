# 使用多阶段构建，第一阶段用于编译打包
FROM maven:3.8.6-openjdk-8 AS build
WORKDIR /app

# 复制 pom.xml 和源代码
COPY pom.xml .
COPY src ./src

# 打包应用，跳过测试以加快速度
RUN mvn clean package -DskipTests

# 第二阶段，使用轻量级 JRE 运行应用
FROM openjdk:8-jdk-alpine
WORKDIR /app

# 从构建阶段复制生成的 jar 包
COPY --from=build /app/target/*.jar app.jar

# 暴露应用端口
EXPOSE 8081

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]
