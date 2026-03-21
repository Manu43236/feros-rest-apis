FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY target/api-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
  "-Xms256m", "-Xmx512m", \
  "-Dspring.profiles.active=prod", \
  "-jar", "app.jar"]
