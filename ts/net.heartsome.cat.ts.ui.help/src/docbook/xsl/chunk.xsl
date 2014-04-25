<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <!-- 请根据本机 DocBook XSL 目录修改以下路径 -->
  <xsl:import href="/usr/share/xml/docbook/docbook-xsl/html/chunk.xsl"/>
  <xsl:param name="html.stylesheet" select="'css/style.css'"/>
  <xsl:param name="chunker.output.encoding" select="'UTF-8'"/>
  <xsl:param name="chunker.output.indent" select="'yes'"/>
  <xsl:param name="ignore.image.scaling">1</xsl:param>
  <xsl:param name="chapter.autolabel" select="0"/>
  <xsl:param name="section.autolabel" select="1"/>
  <xsl:param name="section.autolabel.max.depth">1</xsl:param>
  <xsl:param name="toc.section.depth" select="2"/>
  <xsl:param name="suppress.footer.navigation">0</xsl:param>
  <xsl:param name="suppress.header.navigation" select="0"/>
  <xsl:param name="suppress.navigation" select="1"/>
  <xsl:param name="section.label.includes.component.label" select="0"/>
  <xsl:param name="generate.toc">
appendix  toc,title
article/appendix  nop
article   toc,title
book      title
chapter   title
part      toc,title
preface   toc,title
qandadiv  toc
qandaset  toc
reference toc,title
sect1     toc
sect2     toc
sect3     toc
sect4     toc
sect5     toc
section   toc
set       toc,title
</xsl:param>
  <!--Glossary-->
  <xsl:template match="glossentry">
    <xsl:choose>
      <xsl:when test="$glossentry.show.acronym = 'primary'">
        <div class="glossary_item_title">
          <xsl:call-template name="anchor">
            <xsl:with-param name="conditional">
              <xsl:choose>
                <xsl:when test="$glossterm.auto.link != 0">0</xsl:when>
                <xsl:otherwise>1</xsl:otherwise>
              </xsl:choose>
            </xsl:with-param>
          </xsl:call-template>
          <xsl:choose>
            <xsl:when test="acronym|abbrev">
              <xsl:apply-templates select="acronym|abbrev"/>
              <xsl:text> (</xsl:text>
              <xsl:apply-templates select="glossterm"/>
              <xsl:text>)</xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <xsl:apply-templates select="glossterm"/>
            </xsl:otherwise>
          </xsl:choose>
        </div>
      </xsl:when>
      <xsl:when test="$glossentry.show.acronym = 'yes'">
        <div class="glossary_item_title">
          <xsl:call-template name="anchor">
            <xsl:with-param name="conditional">
              <xsl:choose>
                <xsl:when test="$glossterm.auto.link != 0">0</xsl:when>
                <xsl:otherwise>1</xsl:otherwise>
              </xsl:choose>
            </xsl:with-param>
          </xsl:call-template>
          <xsl:apply-templates select="glossterm"/>
          <xsl:if test="acronym|abbrev">
            <xsl:text> (</xsl:text>
            <xsl:apply-templates select="acronym|abbrev"/>
            <xsl:text>)</xsl:text>
          </xsl:if>
        </div>
      </xsl:when>
      <xsl:otherwise>
        <div class="glossary_item_title">
          <xsl:call-template name="anchor">
            <xsl:with-param name="conditional">
              <xsl:choose>
                <xsl:when test="$glossterm.auto.link != 0">0</xsl:when>
                <xsl:otherwise>1</xsl:otherwise>
              </xsl:choose>
            </xsl:with-param>
          </xsl:call-template>
          <xsl:apply-templates select="glossterm"/>
        </div>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates select="indexterm|revhistory|glosssee|glossdef"/>
  </xsl:template>
  <xsl:template match="glossentry/glosssee">
    <xsl:variable name="otherterm" select="@otherterm"/>
    <xsl:variable name="targets" select="key('id', $otherterm)"/>
    <xsl:variable name="target" select="$targets[1]"/>
    <xsl:variable name="xlink" select="@xlink:href"/>
    <div class="glossary_item">
      <p>
        <xsl:variable name="template">
          <xsl:call-template name="gentext.template">
            <xsl:with-param name="context" select="'glossary'"/>
            <xsl:with-param name="name" select="'see'"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="title">
          <xsl:choose>
            <xsl:when test="$target">
              <a>
                <xsl:apply-templates select="." mode="common.html.attributes"/>
                <xsl:attribute name="href">
                  <xsl:call-template name="href.target">
                    <xsl:with-param name="object" select="$target"/>
                  </xsl:call-template>
                </xsl:attribute>
                <xsl:apply-templates select="$target" mode="xref-to"/>
              </a>
            </xsl:when>
            <xsl:when test="$xlink">
              <xsl:call-template name="simple.xlink">
                <xsl:with-param name="content">
                  <xsl:apply-templates/>
                </xsl:with-param>
              </xsl:call-template>
            </xsl:when>
            <xsl:when test="$otherterm != '' and not($target)">
              <xsl:message>
                <xsl:text>Warning: glosssee @otherterm reference not found: </xsl:text>
                <xsl:value-of select="$otherterm"/>
              </xsl:message>
              <xsl:apply-templates/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:apply-templates/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:call-template name="substitute-markup">
          <xsl:with-param name="template" select="$template"/>
          <xsl:with-param name="title" select="$title"/>
        </xsl:call-template>
      </p>
    </div>
  </xsl:template>
  <xsl:template match="glossentry/glossdef">
    <div class="glossary_item">
      <xsl:apply-templates select="*[local-name(.) != 'glossseealso']"/>
      <xsl:if test="glossseealso">
        <p>
          <xsl:variable name="template">
            <xsl:call-template name="gentext.template">
              <xsl:with-param name="context" select="'glossary'"/>
              <xsl:with-param name="name" select="'seealso'"/>
            </xsl:call-template>
          </xsl:variable>
          <xsl:variable name="title">
            <xsl:apply-templates select="glossseealso"/>
          </xsl:variable>
          <xsl:call-template name="substitute-markup">
            <xsl:with-param name="template" select="$template"/>
            <xsl:with-param name="title" select="$title"/>
          </xsl:call-template>
        </p>
      </xsl:if>
    </div>
  </xsl:template>
</xsl:stylesheet>
