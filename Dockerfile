# Match the Gradle version in gradle/wrapper/gradle-wrapper.properties.
FROM gradle:9.5.0-jdk21 AS build
USER root
WORKDIR /workspace

COPY settings.gradle build.gradle ./
COPY project-rest-api/ project-rest-api/
COPY project-web-app/ project-web-app/

RUN --mount=type=cache,target=/var/cache/gradle \
    gradle --gradle-user-home /var/cache/gradle --no-daemon :project-rest-api:bootJar :project-web-app:bootJar

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
USER 10001:10001
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

FROM runtime AS api
COPY --from=build /workspace/project-rest-api/build/libs/project-rest-api.jar /app/app.jar

FROM runtime AS web
COPY --from=build /workspace/project-web-app/build/libs/project-web-app.jar /app/app.jar
