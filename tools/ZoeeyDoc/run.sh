#!/bin/bash

## 生成中文帮助
# 复制源文件
cp -f ../../ts/net.heartsome.cat.ts.ui.help/src/docbook/zh/ts8help_zh.xml docs/books/HSStudio8_Help_CN/
# 复制生成 PDF 所需的 images 文件，因 ZoeeyDoc 自带的 copy 功能不起作用
cp -f ../../ts/net.heartsome.cat.ts.ui.help/html/zh/images/* docs/books/HSStudio8_Help_CN/images/

# 生成 HTML 文件
java -jar ZoeeyDoc.jar -b docs/books/HSStudio8_Help_CN -t html
# 删除 net.heartsome.cat.ts.ui.help 原有的 HTML 文件
rm -f ../../ts/net.heartsome.cat.ts.ui.help/html/zh/ch*.html
# 将新的 HTML 文件复制到 net.heartsome.cat.ts.ui.help 插件
cp -f output/HSStudio8_Help_CN/html/ch*.html ../../ts/net.heartsome.cat.ts.ui.help/html/zh/

# 生成 PDF 文件
java -jar ZoeeyDoc.jar -b docs/books/HSStudio8_Help_CN -t pdf


## 生成英文帮助，步骤与中文相同
cp -f ../../ts/net.heartsome.cat.ts.ui.help/src/docbook/en/ts8help_en.xml docs/books/HSStudio8_Help_EN/
cp -f ../../ts/net.heartsome.cat.ts.ui.help/html/en/images/* docs/books/HSStudio8_Help_EN/images/

java -jar ZoeeyDoc.jar -b docs/books/HSStudio8_Help_EN -t html
rm -f ../../ts/net.heartsome.cat.ts.ui.help/html/en/ch*.html
cp -f output/HSStudio8_Help_EN/html/ch*.html ../../ts/net.heartsome.cat.ts.ui.help/html/en/

java -jar ZoeeyDoc.jar -b docs/books/HSStudio8_Help_EN -t pdf
