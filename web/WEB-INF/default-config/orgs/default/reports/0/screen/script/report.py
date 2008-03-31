from net.sf.chellow.monad import Hiber, XmlTree

organizationElement = organization.toXML(doc)
source.appendChild(organizationElement)
for dce in Hiber.session().createQuery("from Dce dce where dce.organization = :organization").setEntity('organization', organization).list():
    dceElement = dce.toXML(doc)
    organizationElement.appendChild(dceElement)
    for dceService in Hiber.session().createQuery("from DceService service where service.provider = :provider").setEntity('provider', dce).list():
        dceElement.appendChild(dceService.toXML(doc))