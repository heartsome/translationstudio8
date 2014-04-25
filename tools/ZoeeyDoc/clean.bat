del /f /q output\HSStudio8_Help_CN\html\*
del /f /q output\HSStudio8_Help_CN\pdf\*
del /f /q output\HSStudio8_Help_EN\html\*
del /f /q output\HSStudio8_Help_EN\pdf\*
del /f /q docs\books\HSStudio8_Help_CN\images\*
del /f /q docs\books\HSStudio8_Help_CN\*.xml
del /f /q docs\books\HSStudio8_Help_EN\images\*
del /f /q docs\books\HSStudio8_Help_EN\*.xml

java -jar ZoeeyDoc.jar -c

pause