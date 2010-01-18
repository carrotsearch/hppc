<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns="http://www.w3.org/1999/xhtml" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:output indent="no" omit-xml-declaration="yes" method="text"
              doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
              doctype-system="DTD/xhtml1-transitional.dtd"
              media-type="text/plain" encoding="utf-8" />
              
  <xsl:strip-space elements="*" />

  <xsl:template match="/">
    <xsl:for-each select="//system-out">
      <xsl:value-of select="." />
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>
