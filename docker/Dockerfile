FROM openjdk:11
ARG PROJECT_VERSION

WORKDIR /app/conf
COPY ./docker/logback.xml logback.xml

WORKDIR /app
COPY ./target/gengine-ratelimiter-${PROJECT_VERSION}.jar gengine-ratelimiter.jar

EXPOSE 8002

ENTRYPOINT ["java",\
  "-Dspring.profiles.active=docker-test",\
  "-Dlogging.config=/app/conf/logback.xml",\
  "-jar", "gengine-ratelimiter.jar"]
