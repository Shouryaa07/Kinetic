# Stage 1: build the WAR (runs the unit tests too)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package

# Stage 2: run it on Tomcat 10.1 (Jakarta Servlet 5/6)
FROM tomcat:10.1-jre17
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/kinetic.war /usr/local/tomcat/webapps/ROOT.war

# H2 database files live on a volume so data survives restarts.
# Prefer a managed MySQL/PostgreSQL in production (set KINETIC_DB_URL).
ENV KINETIC_DB_URL="jdbc:h2:file:/data/kinetic;DB_CLOSE_DELAY=-1" \
    KINETIC_COOKIE_SECURE="true"
VOLUME /data
EXPOSE 8080
CMD ["catalina.sh", "run"]
