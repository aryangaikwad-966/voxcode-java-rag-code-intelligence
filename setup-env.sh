#!/bin/bash
# Setup environment for VoxCode project
export JAVA_HOME=/opt/homebrew/opt/openjdk@21
export PATH="$JAVA_HOME/bin:$PATH"
echo "Java 21 environment configured"
java -version