#!/usr/bin/env bash
FROM eclipse-temurin:11
RUN mkdir /opt/app
COPY ${project.build.directory}/${project.build.finalName}.jar /opt/app/app.jar
COPY maven/ /opt/app
RUN sh -x -c 'touch /opt/app/app.jar'
RUN sh -x -c 'chmod -v +x /opt/app/app.jar'

#ENTRYPOINT ["tail", "-f", "/dev/null"]
ENTRYPOINT exec java $JAVA_OPTS -jar /opt/app/app.jar