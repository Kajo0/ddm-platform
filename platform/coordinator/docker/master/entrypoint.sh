#!/bin/bash

java -jar -Dspring.profiles.active=nodemaster,master /apps/node-agent.jar &

bash /master.sh
