FROM bde2020/spark-master:2.4.5-hadoop2.7

ENV SPARK_HOME /spark

RUN apk add --no-cache busybox-extras

# TODO change path to local
ADD apps/node-agent-0.0.1-SNAPSHOT.jar /apps/node-agent.jar
# TODO to agent classpath
ADD apps/app-runner-java-0.0.1-SNAPSHOT-all.jar /apps/app-runner-java.jar
# TODO distirbute to master node and from classpath
ADD apps/aoptkm-1.0-SNAPSHOT-all.jar /apps/aoptkm.jar

COPY entrypoint.sh /

EXPOSE 7100

ENTRYPOINT ["/bin/bash", "/entrypoint.sh"]
