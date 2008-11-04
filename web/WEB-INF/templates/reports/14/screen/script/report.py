from net.sf.chellow.monad.data import Hiber
from java.lang import System
from net.sf.chellow.monad.ui import UserException
from net.sf.chellow.monad.vf.bo import MonadDate
import math
from java.sql import Timestamp, ResultSet
from java.util import GregorianCalendar, Calendar, TimeZone, Locale, Date
from net.sf.chellow.physical import HhEndDate, SiteCode

start = System.currentTimeMillis()
siteCode = inv.getString("site-code")
cal = GregorianCalendar(TimeZone.getTimeZone("GMT"), Locale.UK)
year = inv.getInteger("year")
month = inv.getInteger("month")
day = inv.getInteger("day")
if not inv.isValid():
  raise UserException.newInvalidParameter()
cal.set(Calendar.YEAR, year)
cal.set(Calendar.MONTH, month - 1)
cal.set(Calendar.DAY_OF_MONTH, day)
cal.set(Calendar.HOUR_OF_DAY, 0)
cal.set(Calendar.MINUTE, 0)
cal.set(Calendar.SECOND, 0)
cal.set(Calendar.MILLISECOND, 0)
cal.add(Calendar.MINUTE, 30)
startDate = HhEndDate(cal.getTime()).getDate()
#raise UserException.newInvalidParameter("date " + str(startDate))
cal.add(Calendar.DAY_OF_MONTH, 1)
cal.add(Calendar.MINUTE, -30)
finishDate = HhEndDate(cal.getTime()).getDate()
site = organization.getSite(SiteCode(siteCode))
source.appendChild(site.toXml(doc))
source.appendChild(MonadDate.getMonthsXml(doc))
source.appendChild(MonadDate.getDaysXml(doc))
con = Hiber.session().connection()
stmt = con.prepareStatement("select hh_datum.value, hh_datum.end_date, hh_datum.status, channel.is_import, supply.name, source.code from hh_datum, channel, supply_generation, supply, site_supply, site, source where hh_datum.channel_id = channel.id and channel.supply_generation_id = supply_generation.id and supply_generation.supply_id = supply.id and supply.id = site_supply.supply_id and site_supply.site_id = site.id and supply.source_id = source.id and site.code = ? and channel.is_kwh is true and hh_datum.end_date >= ? and hh_datum.end_date <= ? and source.code != 'sub' order by hh_datum.end_date", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT)
stmt.setString(1, site.getCode().toString())
stmt.setTimestamp(2, Timestamp(startDate.getTime()))
stmt.setTimestamp(3, Timestamp(finishDate.getTime()))
stmt.setFetchSize(100)
rs = stmt.executeQuery()
hhDate = startDate.getTime()
siteSnagQuery = Hiber.session().createQuery("select count(*) from SnagSite snag where snag.site = :site and snag.startDate.date <= :finishDate and snag.finishDate.date >= :startDate and (snag.dateResolved.date is null or (snag.dateResolved.date is not null and snag.isIgnored is true))").setEntity("site", site)
if rs.next():
    hhChannelKwh = rs.getFloat("value")
    hhChannelEndDate = rs.getTimestamp("end_date")
    isImport = rs.getBoolean("is_import")
    status = rs.getString("status")
    sourceCode = rs.getString("code")
    supplyName = rs.getString("name")
    finishDateMillis = finishDate.getTime()
    startDateMillis = startDate.getTime()
    cal.clear()
    cal.setTime(hhChannelEndDate)
    previousMonth = cal.get(Calendar.MONTH)
    while hhDate <= finishDateMillis:
        exportedKwh = 0
        importedKwh = 0
        parasiticKwh = 0
        generatedKwh = 0
        displacedKwh = 0
        while hhChannelEndDate != None and hhChannelEndDate.getTime() == hhDate:
            if not isImport and sourceCode == "net":
                exportedKwh = exportedKwh + hhChannelKwh
            if isImport and sourceCode == "net":
                importedKwh = importedKwh + hhChannelKwh
            if isImport and (sourceCode == "lm" or sourceCode == "chp" or sourceCode == "turb"):
                generatedKwh = generatedKwh + hhChannelKwh
            if not isImport and (sourceCode == "lm" or sourceCode == "chp" or sourceCode == "turb"):
                parasiticKwh = parasiticKwh + hhChannelKwh
            if rs.next():
                sourceCode = rs.getString("code")
                supplyName = rs.getString("name")
                hhChannelKwh = rs.getFloat("value")
                hhChannelEndDate = rs.getTimestamp("end_date")
                isImport = rs.getBoolean("is_import")
                status = rs.getString("status")
                cal.clear()
                cal.setTime(hhChannelEndDate)
                month = cal.get(Calendar.MONTH)
            else:
                hhChannelEndDate = None
        displacedKwh = generatedKwh - exportedKwh - parasiticKwh
        usedKwh = importedKwh + displacedKwh
        hhElement = doc.createElement("hh")
        source.appendChild(hhElement)
        '''
        if siteSnagQuery.setTimestamp("startDate", startDate).setTimestamp("finishDate", monthFinishDate).uniqueResult() > 0:
            monthElement.setAttribute("has-site-snags", "true")
        '''
        hhElement.appendChild(HhEndDate(Date(hhDate)).toXml(doc))
        hhElement.setAttribute("exported-kwh", str(round(exportedKwh, 1)))
        hhElement.setAttribute("imported-kwh", str(round(importedKwh, 1)))
        hhElement.setAttribute("generated-kwh", str(round(generatedKwh, 1)))
        hhElement.setAttribute("parasitic-kwh", str(round(parasiticKwh, 1)))
        hhElement.setAttribute("displaced-kwh", str(round(displacedKwh, 1)))
        hhElement.setAttribute("used-kwh", str(round(usedKwh, 1)))
        hhDate = HhEndDate.getNext(cal, hhDate)