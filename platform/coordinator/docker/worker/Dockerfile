FROM bde2020/spark-worker:2.4.5-hadoop2.7

ENV SPARK_HOME /spark

RUN apk add --no-cache busybox-extras

COPY data/training_data.txt /execution/
COPY data/test_data.txt /execution/

# TODO change path to local
ADD apps/node-agent-0.0.1-SNAPSHOT.jar /apps/node-agent.jar

COPY entrypoint.sh /

EXPOSE 7100

ENTRYPOINT ["/bin/bash", "/entrypoint.sh"]
