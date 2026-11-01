# ---------- Etapa 1: build com Maven e JDK 25 ----------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Primeiro só o que define as dependências: enquanto o pom.xml não mudar,
# o Docker reaproveita esta camada e não baixa tudo de novo.
COPY .mvn .mvn
COPY mvnw pom.xml ./
# Garante final de linha Unix no mvnw, mesmo se o clone foi feito no Windows.
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src src
# Os testes rodam fora do Docker (mvnw test): os de integração usam Testcontainers,
# que precisariam de um Docker dentro do build.
RUN ./mvnw -B -q package -DskipTests

# ---------- Etapa 2: imagem final só com o JRE ----------
FROM eclipse-temurin:25-jre
WORKDIR /app

# Não roda a aplicação como root.
RUN groupadd --system app && useradd --system --gid app app
COPY --from=build /app/target/*.jar app.jar
USER app

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
