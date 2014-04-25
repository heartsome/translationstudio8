#!/bin/bash

rm -f output/HSStudio8_Help_CN/html/*
rm -f output/HSStudio8_Help_CN/pdf/*
rm -f output/HSStudio8_Help_EN/html/*
rm -f output/HSStudio8_Help_EN/pdf/*
rm -f docs/books/HSStudio8_Help_CN/images/*
rm -f docs/books/HSStudio8_Help_CN/*.xml
rm -f docs/books/HSStudio8_Help_EN/images/*
rm -f docs/books/HSStudio8_Help_EN/*.xml

java -jar ZoeeyDoc.jar -c
