FROM bde2020/spark-master:2.4.5-hadoop2.7

ENV SPARK_HOME /spark

RUN apk add --no-cache busybox-extras

# TODO change path to local
ADD apps/node-agent-*.jar /apps/node-agent.jar
# TODO to agent classpath
ADD apps/app-runner-java-*.jar /apps/app-runner-java.jar

COPY entrypoint.sh /

EXPOSE 7100

ENTRYPOINT ["/bin/bash", "/entrypoint.sh"]
