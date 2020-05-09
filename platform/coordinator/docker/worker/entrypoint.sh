#!/bin/bash

java -jar -Dspring.profiles.active=nodemaster,worker /apps/node-agent.jar &

bash /worker.sh
