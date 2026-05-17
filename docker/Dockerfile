FROM alpine:3.22.4 AS base

RUN apk add git openjdk25

RUN git clone https://github.com/P-a-r-a-d-i-s-e/subscription-tracking-app.git
WORKDIR subscription-tracking-app
RUN ./gradlew bootJar

FROM alpine:3.22.4 AS result

RUN apk add openjdk25

WORKDIR /app
COPY --from=base /subscription-tracking-app/build/libs/subscription-tracking-*.jar ./service.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "service.jar"]
