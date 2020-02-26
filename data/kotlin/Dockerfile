FROM openjdk:8 as builder

RUN mkdir /builder

COPY ./data/kotlin/gradle/wrapper /builder/gradle/wrapper
COPY ./data/kotlin/gradlew /builder/gradlew
COPY ./data/kotlin/build.gradle /builder/build.gradle
COPY ./data/kotlin/settings.gradle /builder/settings.gradle
COPY ./data/kotlin/commons /builder/commons
COPY ./data/kotlin/data-uploader /builder/data-uploader
WORKDIR /builder
RUN ./gradlew shadowJar --stacktrace --no-daemon

FROM openjdk:8-jre-alpine

COPY --from=builder /builder/data-uploader/build/libs/data-uploader-all.jar app/lib/

ENTRYPOINT ["java", "-jar", "app/lib/data-uploader-all.jar"]