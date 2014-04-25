<?xml version="1.0" encoding="UTF-8"?>

<!--
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version='1.0'>
                    
    <xsl:output method="html" encoding="gb2312" indent="no"/>
    <xsl:param name="html.encoding">gb2312</xsl:param>
    <xsl:param name="htmlhelp.encoding">gb2312</xsl:param>
    <xsl:param name="suppress.navigation" select="0"/>
    <xsl:param name="use.id.as.filename" select="1"/>

<!--<xsl:param name="toc.section.depth">5</xsl:param>-->
<!--    <xsl:param name="htmlhelp.show.advanced.search" select="1"/>-->
    <xsl:param name="htmlhelp.remember.window.position" select="1"/>
    <xsl:param name="generate.index" select="1"/>
    <xsl:param name="htmlhelp.generate.index" select="1"/>
    <xsl:param name="index.links.to.section" select="1" />
    <xsl:param name="htmlhelp.use.hhk" select="1" />
    <xsl:param name="htmlhelp.chm">zoeeydoc.chm</xsl:param>
    <xsl:param name="l10n.gentext.default.language" select="'zh_cn'"/>

    
    
<!--
 
    <xsl:param name="htmlhelp.show.favorities" select="1"/>
    <xsl:text>","toc.hhc","index.hhk","</xsl:text>

    <xsl:param name="highlight.source" select="0"/>
    <xsl:param name="highlight.default.language" select="java"/>
    <xsl:param name="highlight.xslthl.config">file:///D:/Project/Netbeans/Java/ZoeeyDoc/docs/temp/docbook-xsl/highlighting/xslthl-config.xml</xsl:param>

    -->


<!-- Activate Graphics -->
    <xsl:param name="admon.graphics" select="1"/>
    <xsl:param name="admon.graphics.path">images/</xsl:param>
    <xsl:param name="admon.graphics.extension">.gif</xsl:param>
    <xsl:param name="callout.graphics" select="1" />
    <xsl:param name="callout.graphics.path">images/callouts/</xsl:param>
    <xsl:param name="callout.graphics.extension">.gif</xsl:param>

    <xsl:param name="table.borders.with.css" select="1"/>
    <xsl:param name="html.stylesheet">css/styles.css</xsl:param>
    <xsl:param name="html.stylesheet.type">text/css</xsl:param>
    <xsl:param name="generate.toc">book toc,title</xsl:param>

    <xsl:param name="admonition.title.properties">text-align: left</xsl:param>

  <!-- Label Chapters and Sections (numbering) -->
    <xsl:param name="chapter.autolabel" select="1"/>
    <xsl:param name="section.autolabel" select="1"/>
    <xsl:param name="section.autolabel.max.depth" select="1"/>

    <xsl:param name="section.label.includes.component.label" select="1"/>
    <xsl:param name="table.footnote.number.format" select="'1'"/>

<!-- Remove "Chapter" from the Chapter titles... -->
    <xsl:param name="local.l10n.xml" select="document('')"/>
    <l:i18n xmlns:l="http://docbook.sourceforge.net/xmlns/l10n/1.0">
        <l:l10n language="en">
            <l:context name="title-numbered">
                <l:template name="chapter" text="%n.&#160;%t"/>
                <l:template name="section" text="%n&#160;%t"/>
            </l:context>
        </l:l10n>
    </l:i18n>
</xsl:stylesheet>
