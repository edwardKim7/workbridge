FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml ./pom.xml
COPY server/pom.xml ./server/pom.xml
COPY mock-erp/pom.xml ./mock-erp/pom.xml
RUN mvn -B -ntp -pl server -am dependency:go-offline

COPY server/src ./server/src
RUN mvn -B -ntp -pl server -am clean package -DskipTests

FROM eclipse-temurin:22-jre-jammy
WORKDIR /app
RUN groupadd --system --gid 10001 workbridge \
    && useradd --system --uid 10001 --gid workbridge --no-create-home --shell /usr/sbin/nologin workbridge
COPY --from=build /workspace/server/target/workbridge.war /app/workbridge.war
COPY server/docker-healthcheck.sh /app/docker-healthcheck.sh
RUN chmod 755 /app/docker-healthcheck.sh \
    && chown workbridge:workbridge /app/workbridge.war /app/docker-healthcheck.sh
USER workbridge
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-Duser.timezone=Asia/Seoul", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/workbridge.war"]
