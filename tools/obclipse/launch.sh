#!/bin/bash

BASE_DIR=.
#JAVA_HOME=/usr/lib64/jvm/java/jre
JAVA_HOME=/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home

OBCLIPSE_CONFIG_FILE=$BASE_DIR/obclipse.properties

$JAVA_HOME/bin/java -Xmx1024m -classpath "$BASE_DIR/obclipse.jar:$BASE_DIR/lib/proguard.jar" mfb2.tools.obclipse.Obclipse $OBCLIPSE_CONFIG_FILE
