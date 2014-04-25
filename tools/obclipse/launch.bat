@echo off

SET BASE_DIR=.
SET JAVA_HOME=D:/Program Files/Java/jre6

SET OBCLIPSE_CONFIG_FILE=%BASE_DIR%/obclipse.properties

"%JAVA_HOME%/bin/java" -classpath "%BASE_DIR%/obclipse.jar;%BASE_DIR%/lib/proguard.jar" mfb2.tools.obclipse.Obclipse %OBCLIPSE_CONFIG_FILE%
