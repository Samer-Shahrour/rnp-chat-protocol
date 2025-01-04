#!/bin/bash

# Parameters
NUM_TERMINALS=4
BASE_IP="127.0.0."
BASE_DIR=$(dirname "$(readlink -f "$0")") # Get the script directory dynamically
JAVA_CMD='java -cp "build/classes/java/main;build/libs/json-20230618.jar;build/libs/gson-2.10.1.jar" core.Main'

# Change to project directory
cd "$BASE_DIR" || exit 1

# Gradle build step
echo "Building project using Gradle..."
./gradlew build # Use relative gradlew wrapper if available
if [ $? -ne 0 ]; then
  echo "Build failed. Exiting..."
  exit 1
fi

# Loop to open terminals
for ((i=1; i<=NUM_TERMINALS; i++)); do
  IP="${BASE_IP}${i}"

  # PowerShell command using relative paths
  CMD="cd \"$BASE_DIR\"; echo IP:$IP; $JAVA_CMD $IP"

  # Open Windows PowerShell terminal
  start powershell.exe -NoExit -Command "$CMD"
done
