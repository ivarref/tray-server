#!/usr/bin/env bash

trap 'trap - SIGTERM && kill -- -$$;' SIGINT SIGTERM EXIT

# https://jvns.ca/blog/2020/06/28/entr/
printf "src/java/TrayServer.java\n" | entr -rz ./dev.sh
