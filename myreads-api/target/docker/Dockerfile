FROM anapsix/alpine-java:8_server-jre_unlimited

ENV APP_HOME /app

COPY app_start $APP_HOME/
COPY target/*.jar $APP_HOME/

ENTRYPOINT ["/bin/sh", "-c"]
CMD ["/app/app_start"]