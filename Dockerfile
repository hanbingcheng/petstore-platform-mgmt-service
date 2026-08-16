FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app

# ビルド成果物（実行可能JAR）をコピー
COPY build/libs/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
