FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY src/ src/

RUN ./mvnw clean package -DskipTests


FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup --system spring \
    && adduser --system --ingroup spring spring

COPY --from=build \
    --chown=spring:spring \
    /app/target/cinema-booking-platform-0.0.1-SNAPSHOT.jar \
    application.jar

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "application.jar"]