#!/bin/bash

# Parameter definieren
NUM_TERMINALS=4
BASE_IP="127.0.0."
BASE_DIR='C:\Users\Mario\Documents\HAW\RN\rnp-chat-protocol'
JAVA_CMD='java -cp "C:\Users\Mario\Documents\HAW\RN\rnp-chat-protocol\build\classes\java\main;C:\Users\Mario\Documents\HAW\RN\rnp-chat-protocol\build\libs\json-20230618.jar;C:\Users\Mario\Documents\HAW\RN\rnp-chat-protocol\build\libs\gson-2.10.1.jar" core.Main'

# Change to project directory
cd "$BASE_DIR" || exit 1

# Gradle build step
echo "Building project using Gradle..."
gradle build
if [ $? -ne 0 ]; then
  echo "Build failed. Exiting..."
  exit 1
fi

# Schleife zur Erstellung der Terminals
for ((i=1; i<=NUM_TERMINALS; i++)); do
  IP="${BASE_IP}${i}"

  # PowerShell-Befehl aufteilen: erst cd und dann java ausführen
  CMD="cd \"$BASE_DIR\"; echo IP:$IP; $JAVA_CMD $IP"

  # Windows PowerShell Terminal öffnen und Befehle einzeln ausführen
  start powershell.exe -NoExit -Command "$CMD"
done


