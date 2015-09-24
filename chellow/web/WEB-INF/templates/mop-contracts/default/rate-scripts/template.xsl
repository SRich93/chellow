<?xml version="1.0" encoding="us-ascii"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" encoding="US-ASCII"
		doctype-public="-//W3C//DTD HTML 4.01//EN" doctype-system="http://www.w3.org/TR/html4/strict.dtd"
		indent="yes" />
	<xsl:template match="/">
		<html>
			<head>
				<link rel="stylesheet" type="text/css"
					href="{/source/request/@context-path}/reports/19/output/" />
				<title>
					Chellow &gt; MOP Contracts &gt;
					<xsl:value-of select="/source/rate-scripts/mop-contract/@name" />
					&gt; Rate Scripts
				</title>
			</head>
			<body>
				<p>
					<a href="{/source/request/@context-path}/reports/1/output/">
						<xsl:value-of select="'Chellow'" />
					</a>
					&gt;
					<a href="{/source/request/@context-path}/reports/185/output/">
						<xsl:value-of select="'MOP Contracts'" />
					</a>
					&gt;
					<a
						href="{/source/request/@context-path}/reports/107/output/?mop-contract-id={/source/rate-scripts/mop-contract/@id}">
						<xsl:value-of select="/source/rate-scripts/mop-contract/@name" />
					</a>
					&gt; Rate Scripts
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
				<br />
				<form action="." method="post">
					<fieldset>
						<legend>Add a rate script</legend>
						<br />
						<fieldset>
							<legend>Start Date</legend>
							<input name="start-year" size="4">
								<xsl:choose>
									<xsl:when test="/source/request/parameter[@name='start-year']">
										<xsl:attribute name="value">
													<xsl:value-of
											select="/source/request/parameter[@name='start-year']/value/text()" />
												</xsl:attribute>
									</xsl:when>
									<xsl:otherwise>
										<xsl:attribute name="value">
													<xsl:value-of select="/source/date/@year" />
												</xsl:attribute>
									</xsl:otherwise>
								</xsl:choose>
							</input>
							<xsl:value-of select="'-'" />
							<select name="start-month">
								<xsl:for-each select="/source/months/month">
									<option value="{@number}">
										<xsl:choose>
											<xsl:when test="/source/request/parameter[@name='start-month']">
												<xsl:if
													test="/source/request/parameter[@name='start-month']/value/text() = number(@number)">
													<xsl:attribute name="selected" />
												</xsl:if>
											</xsl:when>
											<xsl:otherwise>
												<xsl:if test="/source/date/@month = @number">
													<xsl:attribute name="selected" />
												</xsl:if>
											</xsl:otherwise>
										</xsl:choose>
										<xsl:value-of select="@number" />
									</option>
								</xsl:for-each>
							</select>
							<xsl:value-of select="'-'" />
							<select name="start-day">
								<xsl:for-each select="/source/days/day">
									<option value="{@number}">
										<xsl:if
											test="/source/request/parameter[@name='start-day']/value/text() = @number">
											<xsl:attribute name="selected" />
										</xsl:if>
										<xsl:value-of select="@number" />
									</option>
								</xsl:for-each>
							</select>
							<xsl:value-of select="' '" />
							<select name="start-hour">
								<xsl:for-each select="/source/hours/hour">
									<option value="{@number}">
										<xsl:if
											test="/source/request/parameter[@name='start-hour']/value/text() = @number">
											<xsl:attribute name="selected" />
										</xsl:if>
										<xsl:value-of select="@number" />
									</option>
								</xsl:for-each>
							</select>
							<xsl:value-of select="':'" />
							<select name="start-minute">
								<xsl:for-each select="/source/hh-minutes/minute">
									<option value="{@number}">
										<xsl:if
											test="/source/request/parameter[@name='start-minute']/value/text() = @number">
											<xsl:attribute name="selected" />
										</xsl:if>
										<xsl:value-of select="@number" />
									</option>
								</xsl:for-each>
							</select>
						</fieldset>
						<br />
						<br />
						<input type="submit" value="Add" />
						<input type="reset" value="Reset" />
					</fieldset>
				</form>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>