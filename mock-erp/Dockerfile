FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml ./pom.xml
COPY server/pom.xml ./server/pom.xml
COPY mock-erp/pom.xml ./mock-erp/pom.xml
RUN mvn -B -ntp -pl mock-erp -am dependency:go-offline

COPY mock-erp/src ./mock-erp/src
RUN mvn -B -ntp -pl mock-erp -am clean package -DskipTests

FROM eclipse-temurin:22-jre-jammy
WORKDIR /app
RUN groupadd --system --gid 10001 workbridge \
    && useradd --system --uid 10001 --gid workbridge --no-create-home --shell /usr/sbin/nologin workbridge
COPY --from=build /workspace/mock-erp/target/workbridge-mock-erp-1.0.0.jar /app/app.jar
COPY mock-erp/docker-healthcheck.sh /app/docker-healthcheck.sh
RUN chmod 755 /app/docker-healthcheck.sh \
    && chown workbridge:workbridge /app/app.jar /app/docker-healthcheck.sh
USER workbridge
EXPOSE 8081
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70.0", "-Duser.timezone=Asia/Seoul", "-jar", "/app/app.jar"]
