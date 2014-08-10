<!--
     gpxs2kml.xsl expects file "rid.gpx" for route number "rid"

     Converts the GPX files referenced in input file of this format to KML:
       <gpxs>
         <route color="FFE60000" name="Grande Corniche round">5724749</route>
         ...
       </gpxs>
-->
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:gpx="http://www.topografix.com/GPX/1/1"
>
  <xsl:template match="/gpxs">

    <!-- add redirect.gmap.xsl PI if self-referencing url is provided -->
    <xsl:if test="@url">
      <xsl:processing-instruction name="xml-stylesheet">
        type="text/xsl" 
        href="redirect.gmap.xsl?<xsl:value-of select="@url"/>"
      </xsl:processing-instruction>
    </xsl:if>

<kml xmlns="http://earth.google.com/kml/2.2">
  <Document>
    <name>many gpx maps</name>
    <visibility>1</visibility>
    <open>1</open>
    <Snippet>created using gpxs2kml.xsl</Snippet>
    <Folder id="Routes">
      <name>Routes</name>
      <visibility>1</visibility>

      <!-- have open folder initially -->
      <open>1</open>

      <xsl:apply-templates select="route"/>
    </Folder>
  </Document>
</kml>
  </xsl:template>


  <xsl:template match="route">
    <xsl:apply-templates select="document(concat(.,'.gpx'))/gpx:gpx/gpx:rte">
      <xsl:with-param name="color" select="@color"/>
      <xsl:with-param name="name"  select="@name"/>
      <xsl:with-param name="r"     select="."/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- output GPX route as Kml Placemark -->
  <xsl:template match="/gpx:gpx/gpx:rte">
    <xsl:param name="color"/>
    <xsl:param name="name"/>
    <xsl:param name="r"/>

    <Placemark>
      <!-- prepend gmap route display link to info text -->
      <name>
        <xsl:value-of select="concat('http://www.gmap-pedometer.com/?r=',$r)"/>
        <xsl:text>&#160;&#160;</xsl:text>
        <xsl:value-of select="$name"/>
      </name>

      <Style>
        <LineStyle>
          <color><xsl:value-of select="$color"/></color>
          <width>4</width>
        </LineStyle>
      </Style>

      <MultiGeometry>
        <LineString>
          <!-- prevent lines hidden by terrain -->
          <tessellate>1</tessellate>

          <!-- take all GPX route points as Kml Linestring coordinates -->
          <coordinates><xsl:apply-templates select="gpx:rtept"/></coordinates>
        </LineString>
      </MultiGeometry>
    </Placemark>
  </xsl:template>

  <!-- output single route point in "lon,lat,0 " format -->
  <xsl:template match="gpx:rtept">
    <xsl:value-of select="@lon"/>,<xsl:value-of select="@lat"/>
    <xsl:text>,0 </xsl:text>
  </xsl:template>

</xsl:stylesheet>
