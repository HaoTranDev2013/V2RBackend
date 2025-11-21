# Sử dụng base image OpenJDK 17
FROM eclipse-temurin:17-jdk-alpine

# Đặt biến cho đường dẫn file JAR
ARG JAR_FILE=target/v2r-0.0.1-SNAPSHOT.jar

# Sao chép file JAR đã được build vào trong image
COPY ${JAR_FILE} app.jar

# Chạy ứng dụng khi container khởi động
ENTRYPOINT ["java","-jar","/app.jar"]
