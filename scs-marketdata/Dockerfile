# Each self-contained system ships as its own image and is deployed on its own.
FROM maven:3.9.16-eclipse-temurin-25-alpine AS build
WORKDIR /build
COPY pom.xml ./
COPY scs-marketdata/ scs-marketdata/
COPY scs-portfolio/ scs-portfolio/
RUN mvn -B -q -pl scs-marketdata/bootstrap -am package -DskipTests

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app && mkdir -p /app/data && chown -R app:app /app
COPY --from=build --chown=app:app /build/scs-marketdata/bootstrap/target/marketdata.jar app.jar
USER app
EXPOSE 8081
ENV MARKETDATA_DB_PATH=/app/data/marketdata
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s \
    CMD wget -qO- http://localhost:8081/actuator/health | grep -q '"status":"UP"'
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
