FROM bde2020/spark-worker:2.4.5-hadoop2.7

ENV SPARK_HOME /spark

RUN apk add --no-cache busybox-extras

# TODO change path to local
ADD apps/node-agent-*.jar /apps/node-agent.jar

COPY entrypoint.sh /
COPY spark-env.sh /spark/conf/

EXPOSE 7100

ENTRYPOINT ["/bin/bash", "/entrypoint.sh"]
