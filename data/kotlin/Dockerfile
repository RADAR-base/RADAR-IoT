FROM openjdk:8 as builder

RUN mkdir /builder
COPY ./gradle/wrapper /builder/gradle/wrapper
COPY ./gradlew /builder/gradlew
COPY ./build.gradle /builder/build.gradle
COPY ./settings.gradle /builder/settings.gradle
COPY ./commons /builder/commons
COPY ./data-uploader /builder/data-uploader
WORKDIR /builder
RUN ./gradlew shadowJar --stacktrace --no-daemon

FROM openjdk:8-jre-alpine

COPY --from=builder /builder/data-uploader/build/libs/data-uploader-all.jar app/lib/

ENTRYPOINT ["java", "-jar", "app/lib/data-uploader-all.jar"]