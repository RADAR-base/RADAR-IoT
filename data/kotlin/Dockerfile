FROM gradle:5.4-jdk8-alpine as builder
USER root
WORKDIR /builder
ADD . /builder
RUN gradle shadowJar --stacktrace --no-daemon

FROM openjdk:8-jre-alpine

COPY --from=builder /builder/data-uploader/build/libs/data-uploader-all.jar app/lib/

ENTRYPOINT ["java", "-jar", "app/lib/data-uploader-all.jar"]