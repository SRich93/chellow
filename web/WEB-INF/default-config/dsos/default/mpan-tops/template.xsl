<?xml version="1.0" encoding="us-ascii"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" encoding="US-ASCII"
		doctype-public="-//W3C//DTD HTML 4.01//EN"
		doctype-system="http://www.w3.org/TR/html4/strict.dtd" indent="yes" />

	<xsl:template match="/">
		<html>
			<head>
				<link rel="stylesheet" type="text/css"
					href="{/source/request/@context-path}/orgs/1/reports/9/stream/output/" />
				<title>
					Chellow &gt; DSOs &gt;
					<xsl:value-of
						select="concat(/source/mpan-tops/dso/@code, ' &gt; MPAN tops')" />
				</title>
			</head>

			<body>
				<p>
					<a href="{/source/request/@context-path}/">
						<img
							src="{/source/request/@context-path}/logo/" />
						<span class="logo">Chellow</span>
					</a>
					<xsl:value-of select="' &gt; '" />
					<a href="{/source/request/@context-path}/dsos/">
						<xsl:value-of select="'DSOs'" />
					</a>
					<xsl:value-of select="' &gt; '" />
					<a
						href="{/source/request/@context-path}/dsos/{/source/mpan-tops/dso/@id}/">
						<xsl:value-of
							select="/source/mpan-tops/dso/@code" />
					</a>
					<xsl:value-of select="' &gt; Mpan tops '" />
				</p>

				<xsl:if test="//message">
					<ul>
						<xsl:for-each select="//message">
							<li>
								<xsl:value-of select="@description" />
							</li>
						</xsl:for-each>
					</ul>
				</xsl:if>
				<table>
					<thead>
						<tr>
							<th>Id</th>
							<th>Profile Class</th>
							<th>Meter Timeswitch</th>
							<th>Line Loss Factor</th>
						</tr>
					</thead>
					<tbody>
						<xsl:for-each
							select="/source/mpan-tops/mpan-top">
							<tr>
								<td>
									<a href="{@id}/">
										<xsl:value-of select="@id" />
									</a>
								</td>
								<td>
									<xsl:value-of
										select="concat(profile-class/@code, ' - ', profile-class/@description)" />
								</td>
								<td>
									<xsl:value-of
										select="concat(meter-timeswitch/@code, ' - ', meter-timeswitch/@description)" />
								</td>
								<td>
									<xsl:value-of
										select="concat(line-loss-factor/@code, ' - ', line-loss-factor/@description)" />
								</td>
							</tr>
						</xsl:for-each>
					</tbody>
				</table>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
