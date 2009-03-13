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
					href="{/source/request/@context-path}/style/" />

				<title>
					Chellow &gt; Supplier Contracts &gt;
					<xsl:value-of
						select="/source/account-snag/account/supplier-contract/@name" />
					&gt; Account Snags &gt;
					<xsl:value-of select="/source/account-snag/@id" />
				</title>
			</head>
			<body>
				<xsl:if test="//message">
					<ul>
						<xsl:for-each select="//message">
							<li>
								<xsl:value-of select="@description" />
							</li>
						</xsl:for-each>
					</ul>
				</xsl:if>
				<p>
					<a href="{/source/request/@context-path}/">
						<img
							src="{/source/request/@context-path}/logo/" />
						<span class="logo">Chellow</span>
					</a>
					&gt;
					<a
						href="{/source/request/@context-path}/supplier-contracts/">
						<xsl:value-of select="'Supplier Contracts'" />
					</a>
					&gt;
					<a
						href="{/source/request/@context-path}/supplier-contracts/{/source/account-snag/account/supplier-contract/@id}/">
						<xsl:value-of
							select="/source/account-snag/account/supplier-contract/@name" />
					</a>
					&gt;
					<a
						href="{/source/request/@context-path}/supplier-contracts/{/source/account-snag/account/supplier-contract/@id}/account-snags/">
						<xsl:value-of select="'Account Snags'" />
					</a>
					&gt;
					<xsl:value-of
						select="concat(/source/account-snag/@id, ' [')" />
					<a
						href="{/source/request/@context-path}/reports/103/output/?snag-id={/source/account-snag/@id}">
						<xsl:value-of select="'view'" />
					</a>
					<xsl:value-of select="']'" />
				</p>
				<br />
				<table>
					<tr>
						<th>Chellow Id</th>
						<td>
							<xsl:value-of
								select="/source/account-snag/@id" />
						</td>
					</tr>
					<tr>
						<th>Account</th>
						<td>
							<a
								href="{/source/request/@context-path}/supplier-contracts/{/source/account-snag/account/supplier-contract/@id}/accounts/{/source/account-snag/account/@id}/">
								<xsl:value-of
									select="/source/account-snag/account/@reference" />
							</a>
						</td>
					</tr>
					<tr>
						<th>Start Date</th>
						<td>
							<xsl:value-of
								select="concat(' ', /source/account-snag/hh-end-date[@label='start']/@year, '-', /source/account-snag/hh-end-date[@label='start']/@month, '-', /source/account-snag/hh-end-date[@label='start']/@day)" />
						</td>
					</tr>
					<tr>
						<th>Finish Date</th>
						<td>
							<xsl:value-of
								select="concat(' ', /source/account-snag/hh-end-date[@label='finish']/@year, '-', /source/account-snag/hh-end-date[@label='finish']/@month, '-', /source/account-snag/hh-end-date[@label='finish']/@day)" />
						</td>
					</tr>
					<tr>
						<th>Date Created</th>
						<td>
							<xsl:value-of
								select="concat(' ', /source/account-snag/date[@label='created']/@year, '-', /source/account-snag/date[@label='created']/@month, '-', /source/account-snag/date[@label='created']/@day)" />
						</td>
					</tr>
					<tr>
						<th>Date Resolved</th>
						<td>
							<xsl:choose>
								<xsl:when
									test="/source/account-snag/hh-end-date[@label='resolved']">
									<xsl:value-of
										select="concat(/source/account-snag/hh-end-date[@label='resolved']/@year, '-', /source/account-snag/hh-end-date[@label='resolved']/@month, '-', /source/account-snag/hh-end-date[@label='resolved']/@day)" />
								</xsl:when>
								<xsl:otherwise>
									Unresolved
								</xsl:otherwise>
							</xsl:choose>
						</td>
					</tr>
					<tr>
						<th>Is Ignored?</th>
						<td>
							<xsl:choose>
								<xsl:when
									test="/source/account-snag/@is-ignored = 'true'">
									Yes
								</xsl:when>
								<xsl:otherwise>No</xsl:otherwise>
							</xsl:choose>
						</td>
					</tr>
					<tr>
						<th>Description</th>
						<td>
							<xsl:value-of
								select="/source/account-snag/@description" />
						</td>
					</tr>
				</table>
				<br />
				<xsl:if
					test="not(/source/account-snag/date[@label='resolved']) or (/source/account-snag/date[@label='resolved'] and /source/account-snag/@is-ignored='true')">
					<form action="." method="post">
						<fieldset>
							<legend>Update snag</legend>
							<input type="hidden" name="ignore">
								<xsl:attribute name="value">
									<xsl:choose>
										<xsl:when
											test="/source/account-snag/@is-ignored='true'">
											<xsl:value-of
												select="'false'" />
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of
												select="'true'" />
										</xsl:otherwise>
									</xsl:choose>
								</xsl:attribute>
							</input>
							<input type="submit">
								<xsl:attribute name="value">
									<xsl:choose>
										<xsl:when
											test="/source/account-snag/@is-ignored='true'">
											<xsl:value-of
												select="'Un-ignore'" />
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of
												select="'Ignore'" />
										</xsl:otherwise>
									</xsl:choose>
								</xsl:attribute>
							</input>
						</fieldset>
					</form>
				</xsl:if>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>