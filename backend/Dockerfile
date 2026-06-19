ARG SERVICE_NAME

FROM maven:3.9.11-eclipse-temurin-21 AS builder
ARG SERVICE_NAME
WORKDIR /workspace

COPY pom.xml ./pom.xml
COPY backend ./backend
RUN mvn -B -pl backend/${SERVICE_NAME} -am package -DskipTests

FROM eclipse-temurin:21-jre-alpine
ARG SERVICE_NAME
ENV TZ=Asia/Shanghai
WORKDIR /app
COPY --from=builder /workspace/backend/${SERVICE_NAME}/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

