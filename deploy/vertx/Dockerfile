# docker build -t deepro.io/cloudbrain/windmill-vertx -f deploy/vertx/Dockerfile .
# docker save deepro.io/cloudbrain/windmill-vertx > docker-vertx.tar
FROM openjdk:8-jre

MAINTAINER Zheng Jianhua <zhengjh@cloudbrain.ai>

COPY service/target/server-1.0-fat.jar /usr/local/cloudbrain/windmill/

WORKDIR /usr/local/cloudbrain/windmill

EXPOSE 8000

CMD ["java", "-jar", "server-1.0-fat.jar"]
