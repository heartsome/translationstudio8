ZoeeyDoc 介绍

ZoeeyDoc?是一个docbook文档构建工具，支持 single-html、html、chm、pdf 等格式导出。对中文支持良好。
优点

http://code.google.com/p/zoeeydoc/

========================
ZoeeyDoc help:
========================
-h/--help :
         打印此文档。

-c/--clean :
         清理缓存文件。

-t/--type :
         转换目标类型（chm,html,single/singlehtml），默认为 html。

-b/--book :
         文档目录。 文件配置文件为文件路径+"/config.kv"

获取帮助
java -jar ZoeeyDoc.jar -h

生成 html
java -jar ZoeeyDoc.jar -b docs/books/zoeeydoc -t html

生成 single html
java -jar ZoeeyDoc.jar -b docs/books/zoeeydoc -t single

生成 chm （需要指定hhc目录）
java -jar ZoeeyDoc.jar -b docs/books/zoeeydoc -t chm


========================
BUILD OUTPUT DESCRIPTION
========================

When you build an Java application project that has a main class, the IDE
automatically copies all of the JAR
files on the projects classpath to your projects dist/lib folder. The IDE
also adds each of the JAR files to the Class-Path element in the application
JAR files manifest file (MANIFEST.MF).

To run the project from the command line, go to the dist folder and
type the following:

java -jar "ZoeeyDoc.jar"

To distribute this project, zip up the dist folder (including the lib folder)
and distribute the ZIP file.

Notes:

* If two JAR files on the project classpath have the same name, only the first
JAR file is copied to the lib folder.
* Only JAR files are copied to the lib folder.
If the classpath contains other types of files or folders, none of the
classpath elements are copied to the lib folder. In such a case,
you need to copy the classpath elements to the lib folder manually after the build.
* If a library on the projects classpath also has a Class-Path element
specified in the manifest,the content of the Class-Path element has to be on
the projects runtime path.
* To set a main class in a standard Java project, right-click the project node
in the Projects window and choose Properties. Then click Run and enter the
class name in the Main Class field. Alternatively, you can manually type the
class name in the manifest Main-Class element.
