<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="notes">
		<html>
			<body>
				<h3>Notes</h3>
				<xsl:apply-templates />
			</body>
		</html>
		<!-- TODO: Auto-generated template -->
	</xsl:template>
	<xsl:template match="note">
		<div
			style="border: 1px solid; padding: 2px; margin: 2px; font-family: sans-serif;">
			<h4>
				<xsl:value-of select="@title" />, <xsl:value-of select="@date" />, <xsl:value-of select="@tag" />, public: <xsl:value-of select="@public" />
			</h4>
		</div>
		<p>
			<xsl:apply-templates disable-output-escaping="yes"/>
		</p>
	</xsl:template>
	
</xsl:stylesheet>