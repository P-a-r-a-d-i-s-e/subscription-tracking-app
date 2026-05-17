FROM eclipse-temurin:25-jdk-alpine-3.22 AS base

RUN apk add --no-cache git

RUN git clone https://github.com/P-a-r-a-d-i-s-e/subscription-tracking-app.git
WORKDIR subscription-tracking-app
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:25-jdk-alpine-3.22 AS result

WORKDIR /app
COPY --from=base /subscription-tracking-app/build/libs/subscription-tracking-*.jar ./service.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "service.jar"]
