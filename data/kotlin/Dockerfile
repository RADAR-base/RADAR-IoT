FROM gradle:6.5.1-jdk11 as builder

RUN mkdir /builder

COPY ./data/kotlin/build.gradle /builder/build.gradle
COPY ./data/kotlin/settings.gradle /builder/settings.gradle
COPY ./data/kotlin/commons /builder/commons
COPY ./data/kotlin/data-uploader /builder/data-uploader
WORKDIR /builder
RUN gradle shadowJar --stacktrace --no-daemon

FROM openjdk:11.0.3-jre

COPY --from=builder /builder/data-uploader/build/libs/data-uploader-all.jar app/lib/

ENTRYPOINT ["java", "-jar", "app/lib/data-uploader-all.jar"]