copy ..\..\ts\net.heartsome.cat.ts.ui.help\src\docbook\zh\ts8help_zh.xml docs\books\HSStudio8_Help_CN
java -jar ZoeeyDoc.jar -b docs/books/HSStudio8_Help_CN -t html
del ..\..\ts\net.heartsome.cat.ts.ui.help\html\zh\ch*.html
xcopy /e output\HSStudio8_Help_CN\html\ch*.html ..\..\ts\net.heartsome.cat.ts.ui.help\html\zh
xcopy /y ..\..\ts\net.heartsome.cat.ts.ui.help\html\zh\images\* docs\books\HSStudio8_Help_CN\images
java -jar ZoeeyDoc.jar -b docs/books/HSStudio8_Help_CN -t pdf

copy ..\..\ts\net.heartsome.cat.ts.ui.help\src\docbook\en\ts8help_en.xml docs\books\HSStudio8_Help_EN
java -jar ZoeeyDoc.jar -b docs/books/HSStudio8_Help_EN -t html
del ..\..\ts\net.heartsome.cat.ts.ui.help\html\en\ch*.html
xcopy /e output\HSStudio8_Help_EN\html\ch*.html ..\..\ts\net.heartsome.cat.ts.ui.help\html\en
xcopy /y ..\..\ts\net.heartsome.cat.ts.ui.help\html\en\images\* docs\books\HSStudio8_Help_EN\images
java -jar ZoeeyDoc.jar -b docs/books/HSStudio8_Help_EN -t pdf

pause