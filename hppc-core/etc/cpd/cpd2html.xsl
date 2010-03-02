<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output indent="yes" omit-xml-declaration="yes"
       doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
       doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
       media-type="text/html" encoding="UTF-8" />
  
  <xsl:template match="/">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>Code duplication report</title>
    <style>
      body {
        font-family: Arial, sans-serif;
        font-size: 12px;
      }

      div.code {
        white-space: pre;
        font-family: "Lucida Sans Unicode", monospace;
        font-size: 12px;
        background-color: #f8f8f8;
        border: 1px solid #808080;
        margin: 20px;
      }

      h2 {
        font-size: 26px;
        font-weight: bold;
        color: #909090;
        margin-top: 0.5ex;
        margin-bottom: 0ex;
      }

      h3 {
        font-size: 16px;
        font-weight: bold;
        color: #909090;
        margin-top: 3ex;
        margin-bottom: 1ex;
      }

      span.lines {
        color: #202020;
      }

      span.path {
        font-size: 12px;
        font-family: "Lucida Sans Unicode", monospace;
      }

      div.files {
        margin-left: 20px;
      }

      div.files span.line {
        font-weight: bold;
        color: #202020;
      }
    </style>
  </head>

  <body>
    <xsl:apply-templates select="pmd-cpd" mode="stats" />
    <xsl:apply-templates select="pmd-cpd/duplication" />
  </body>
</html>
  </xsl:template>

  <xsl:template match="pmd-cpd" mode="stats">
    <h2><span class="lines"><xsl:value-of select="count(duplication)" /> fragments</span> duplicated</h2>
    <h2><span class="lines"><xsl:value-of select="sum(duplication/@lines)" /> lines</span> duplicated</h2>
  </xsl:template>

  <xsl:template match="duplication">
    <div>
      <h3><span class="lines"><xsl:value-of select="@lines" /> lines</span> duplicated in:</h3>
      <div class="files">
        <xsl:apply-templates select="file" />
      </div>
      <div class="code">
        <xsl:apply-templates select="codefragment" />
      </div>
    </div>
  </xsl:template>

  <xsl:template match="file">
    <div>
      <xsl:variable name="translated"><xsl:value-of select="translate(@path, '\', '/')" /></xsl:variable>
      <span class="line">Line <xsl:value-of select="@line" /></span> of 
     
      <span class="path">
      <xsl:choose>
        <xsl:when test="contains($translated,'/core')">
          <xsl:value-of select="substring($translated, string-length(substring-before($translated, '/core')) + 1)" />
        </xsl:when>
        <xsl:when test="contains($translated,'/applications')">
          <xsl:value-of select="substring($translated, string-length(substring-before($translated, '/applications')) + 1)" />
        </xsl:when>
        <xsl:when test="contains($translated,'/workbench')">
          <xsl:value-of select="substring($translated, string-length(substring-before($translated, '/workbench')) + 1)" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$translated" />
        </xsl:otherwise>
      </xsl:choose>
      </span>
    </div>
  </xsl:template>
</xsl:stylesheet>
