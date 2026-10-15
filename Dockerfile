# ============================================================
# mgmt-service Dockerfile
# ビルドコンテキストは親ディレクトリ（docker-compose.yml で context: .. を指定）
# ============================================================

# ---------- ビルドステージ ----------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

# common モジュールをローカルMavenリポジトリにインストール
COPY petstore-platform-common common
RUN cd common && ./gradlew publishToMavenLocal --no-daemon

# mgmt-service をビルド（bootJar で実行可能JARを生成）
COPY petstore-platform-mgmt-service mgmt-service
RUN cd mgmt-service && ./gradlew clean bootJar --no-daemon

# ---------- 実行ステージ ----------
FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app

# ビルド成果物（実行可能JAR）をコピー
COPY --from=build /workspace/mgmt-service/build/libs/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
