#!/bin/bash

# -CHANGEME- 请改为您本机的工作空间 (CVS) 或代码仓库 (Git) 路径
ws_path=/Volumes/iMac-User/Projects/hsgit/translation-studio/ts
# -CHANGEME- 请改为您本机的 FOP 可执行文件路径
fop_path=/usr/share/xml/docbook/fop/fop
# -CHANGEME- 请改为您本机的 FOP 字体配置文件路径
fop_xconf_path=/usr/share/xml/docbook/fop/conf/fop.xconf

# 若无特殊情况（如修改了插件、包名或目录结构），以下路径可以不必修改
plugin_name=net.heartsome.cat.ts.ui.help
src_path=src/docbook
html_path=html
output_dir=scripts/output
# DocBook XML 文件名前缀、同时也作为输出文件的
db_xml_prefix=ts8help_
chunk_xsl=chunk.xsl
fop_xsl=fop.xsl
html_xsl=html.xsl

script_name=$0
err_msg1="参数错误，请指定正确的输出格式 chunk|rtf|html|pdf 和语言 zh|en 参数。"
err_msg2="举例一，输出中文 HTML（分页）：$script_name chunk zh"
err_msg3="举例二，输出英文 RTF：$script_name rtf en"
err_msg4="举例三，输出中文 HTML（单个）：$script_name html zh"
err_msg5="举例四，输出英文 PDF：$script_name pdf en"

# 判断输入参数是否正确
if [ $# -ne 2 ]
then
    echo $err_msg1
    echo $err_msg2
    echo $err_msg3
    echo $err_msg4
    echo $err_msg5
    exit 1
fi
format=$1
if [ "$format" != chunk ] && [ "$format" != rtf ] && [ "$format" != html ] && [ "$format" != pdf ]
then
    echo $err_msg1
    echo $err_msg2
    echo $err_msg3
    echo $err_msg4
    echo $err_msg5
    exit 1
fi
lng=$2
if [ "$lng" != zh ] && [ "$lng" != en ]
then
    echo $err_msg1
    echo $err_msg2
    echo $err_msg3
    echo $err_msg4
    echo $err_msg5
    exit 1
fi

# 根据语言参数拼出完整文件路径
db_xml_path=$ws_path/$plugin_name/$src_path/$lng/$db_xml_prefix$lng.xml

if [ "$format" = chunk ]
then
    # 分页 HTML 以目录为输出、使用修改过的 XSL 样式表
    output_path=$ws_path/$plugin_name/$html_path/$lng/
    xsl_path=$ws_path/$plugin_name/$src_path/xsl/$chunk_xsl
elif [ "$format" = html ]
then
    # 单个 HTML 以文件为输出、使用定义了 UTF-8 编码的样式表
    output_path=$ws_path/$plugin_name/$output_dir/$lng/$db_xml_prefix$lng.$format
    xsl_path=$ws_path/$plugin_name/$src_path/xsl/$html_xsl
else
    # RTF 和 PDF 使用定义了中文字体的 FOP 样式表
    output_path=$ws_path/$plugin_name/$output_dir/$db_xml_prefix$lng.$format
    xsl_path=$ws_path/$plugin_name/$src_path/xsl/$fop_xsl
fi

echo 源文件：
echo $db_xml_path
echo 样式表：
echo $xsl_path
echo 输出到：
echo $output_path

echo '以上信息正确吗？Y/n'
read confirm
#if [ "$confirm" = y ] || [ "$confirm" = Y ] || [ "$confirm" = Yes ] || [ "$confirm" = yes ] || [ "$confirm" = YES ]
if [ "$confirm" != n ] && [ "$confirm" != N ] && [ "$confirm" != No ] && [ "$confirm" != no ] && [ "$confirm" != NO ]
then
    echo 开始转换...
    if [ "$format" = chunk ] || [ "$format" = html ]
    then
        # 使用 xsltproc 工具输出 HTML 文件
        xsltproc --xinclude --output $output_path $xsl_path $db_xml_path
    elif [ "$format" = rtf ] || [ "$format" = pdf ]
    then
        # 使用 fop 工具输出 RTF 文件
    	$fop_path -c $fop_xconf_path -xml $db_xml_path -xsl $xsl_path -$format $output_path
    fi
else
    echo 已取消转换。
    exit 0
fi
exit 0
