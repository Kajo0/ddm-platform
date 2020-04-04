#!/bin/bash

java -jar -Dspring.profiles.active=nodemaster /apps/node-agent.jar &

bash /master.sh
