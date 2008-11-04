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
					href="{/source/request/@context-path}/orgs/{/source/account-snags/supplier-contract/org/@id}/reports/9/stream/output/" />

				<title>
					<xsl:value-of
						select="/source/account-snags/supplier-contract/org/@name" />
					&gt; Supplier Contracts &gt;
					<xsl:value-of
						select="/source/account-snags/supplier-contract/@name" />
					&gt; Account Snags
				</title>
			</head>
			<body>
				<p>
					<a
						href="{/source/request/@context-path}/orgs/{/source/account-snags/supplier-contract/org/@id}/reports/0/screen/output/">
						<xsl:value-of
							select="/source/account-snags/supplier-contract/org/@name" />
					</a>
					&gt;
					<a
						href="{/source/request/@context-path}/orgs/{/source/account-snags/supplier-contract/org/@id}/reports/37/screen/output/">
						<xsl:value-of select="'Supplier Contracts'" />
					</a>
					&gt;
					<a
						href="{/source/request/@context-path}/orgs/{/source/account-snags/supplier-contract/org/@id}/reports/38/screen/output/?contract-id={/source/account-snags/supplier-contract/@id}">
						<xsl:value-of
							select="/source/account-snags/supplier-contract/@name" />
					</a>
					&gt;
					<xsl:value-of select="'Account Snags ['" />
					<a
						href="{/source/request/@context-path}/orgs/{/source/account-snags/supplier-contract/org/@id}/supplier-contracts/{/source/account-snags/supplier-contract/@id}/account-snags/">
						<xsl:value-of select="'edit'" />
					</a>
					<xsl:value-of select="']'" />
				</p>
				<br />
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
							<th>Chellow Id</th>
							<th>Account</th>
							<th>Start Date</th>
							<th>Finish Date</th>
							<th>Date Created</th>
							<th>Date Resolved</th>
							<th>Is Ignored?</th>
							<th>Description</th>
						</tr>
					</thead>
					<tbody>
						<xsl:for-each
							select="/source/account-snags/account-snag">
							<tr>
								<td>
									<a
										href="{/source/request/@context-path}/orgs/{/source/account-snags/supplier-contract/org/@id}/reports/51/screen/output/?snag-id={@id}">
										<xsl:value-of select="@id" />
									</a>
								</td>
								<td>
									<a
										href="{/source/request/@context-path}/orgs/{/source/account-snags/supplier-contract/org/@id}/reports/41/screen/output/?account-id={account/@id}">
										<xsl:value-of
											select="account/@reference" />
									</a>
								</td>
								<td>
									<xsl:value-of
										select="concat(hh-end-date[@label='start']/@year, '-', hh-end-date[@label='start']/@month, '-', hh-end-date[@label='start']/@day)" />
								</td>
								<td>
									<xsl:value-of
										select="concat(hh-end-date[@label='finish']/@year, '-', hh-end-date[@label='finish']/@month, '-', hh-end-date[@label='finish']/@day)" />
								</td>
								<td>
									<xsl:value-of
										select="concat(date[@label='created']/@year, '-', date[@label='created']/@month, '-', date[@label='created']/@day)" />
								</td>
								<td>
									<xsl:choose>
										<xsl:when
											test="hh-end-date[@label='resolved']">
											<xsl:value-of
												select="concat(hh-end-date[@label='resolved']/@year, '-', hh-end-date[@label='resolved']/@month, '-', hh-end-date[@label='resolved']/@day)" />
										</xsl:when>
										<xsl:otherwise>
											Unresolved
										</xsl:otherwise>
									</xsl:choose>
								</td>
								<td>
									<xsl:choose>
										<xsl:when
											test="@is-ignored = 'true'">
											Yes
										</xsl:when>
										<xsl:otherwise>
											No
										</xsl:otherwise>
									</xsl:choose>
								</td>
								<td>
									<xsl:value-of select="@description" />
								</td>
							</tr>
						</xsl:for-each>
					</tbody>
				</table>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>