#!/bin/bash

echo "Removing class files from source code..."
find ./src/ -name '*.class' | xargs rm 
echo "...Class files successfully removed."