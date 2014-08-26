<?xml version="1.0"?>
<!-- 
    https://gist.github.com/wmanth/5413400 
-->
    
<!-- <xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gpx="http://www.topografix.com/GPX/1/0"> -->
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:gpx="http://www.topografix.com/GPX/1/1">

<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" />

<xsl:template match="/">
  <kml xmlns="http://www.opengis.net/kml/2.2"
    xmlns:gx="http://www.google.com/kml/ext/2.2"
    xmlns:kml="http://www.opengis.net/kml/2.2"
    xmlns:atom="http://www.w3.org/2005/Atom">
    <xsl:apply-templates select="gpx:gpx"/>
  </kml>
</xsl:template>

<xsl:template match="gpx:gpx">
  <Document>
    <Style id="route">
      <LineStyle>
        <color>a02020ff</color>
         <width>4</width>
      </LineStyle>
    </Style>
    <xsl:apply-templates select="gpx:trk"/>
  </Document>
</xsl:template>


<xsl:template match="gpx:trk">
  <Placemark>
    <name><xsl:value-of select="gpx:name"/></name>
    <description><xsl:value-of select="gpx:type"/> - <xsl:value-of select="gpx:desc"/></description>
    <styleUrl>#route</styleUrl>
    <xsl:apply-templates select="gpx:trkseg"/>
  </Placemark>
</xsl:template>

<xsl:template match="gpx:trkseg">
  <LineString>
    <tessellate>1</tessellate>
    <coordinates>
      <xsl:for-each select="gpx:trkpt">
        <xsl:value-of select="@lon"/>,<xsl:value-of select="@lat"/>,<xsl:value-of select="gpx:ele"/>
        <xsl:text> </xsl:text> 
      </xsl:for-each>
    </coordinates>
  </LineString>
</xsl:template>

</xsl:stylesheet>