<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="notes">
		<feed xmlns="http://www.w3.org/2005/Atom">
			<author>
				<name>Fabian Steeg</name>
			</author>
			<title>Geschreibsel</title>
			<id>urn:uuid:8689009107294786337L</id>
			<updated></updated>
			<xsl:apply-templates />
		</feed>

		<!-- TODO: Auto-generated template -->
	</xsl:template>
	<xsl:template match="note">
		<entry>
			<title><xsl:value-of select="@title" /></title>
			<link />
			<id></id>
			<updated><xsl:value-of select="@date" /></updated>
			<summary><xsl:value-of select="@tag" /></summary>
			<content><xsl:apply-templates /></content>
		</entry>
	</xsl:template>
</xsl:stylesheet>