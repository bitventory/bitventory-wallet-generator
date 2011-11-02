#!/bin/bash

# Compile the WalletGenerator
mkdir bin/
javac -cp lib/bcprov-jdk16-146.jar:lib/slf4j-api-1.6.2.jar -d bin/ src/com/bccapi/core/PRNG.java src/com/bitventory/tools/WalletGenerator.java src/com/bitventory/core/* src/com/bitventory/applet/* src/com/google/bitcoin/core/* src/com/google/bitcoin/discovery/* src/com/google/bitcoin/store/*

# Create the jar file
zip -r WalletGenerator.jar bin/ META-INF/

