#!/bin/bash

# Define the path to the CLI JAR
CLI_JAR_PATH="$(pwd)/cli/build/libs/ci-tool.jar"

# Check if the JAR file exists
if [ ! -f "$CLI_JAR_PATH" ]; then
    echo "[ERROR] ci-tool.jar not found at $CLI_JAR_PATH"
    echo "==>> Make sure you have built the CLI with: ./gradlew clean build <<=="
    exit 1
fi

# Create the wrapper script in /usr/local/bin
# Install as 'pipr' for pipeline runner
echo '#!/bin/bash' | sudo tee /usr/local/bin/pipr > /dev/null
echo "java -jar $CLI_JAR_PATH \"\$@\"" | sudo tee -a /usr/local/bin/pipr > /dev/null
sudo chmod +x /usr/local/bin/pipr

echo "[SUCCESS] Installation complete! You can now use 'pipr' as a command."
echo "==>> Try running: pipr --help <<=="