/*
 
 Copyright 2005 Meniscus Systems Ltd
 
 This file is part of Chellow.

 Chellow is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 Chellow is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Chellow; if not, write to the Free Software
 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

 */

package net.sf.chellow.billing;

import java.util.Date;

import net.sf.chellow.data08.MpanCoreRaw;
import net.sf.chellow.monad.DeployerException;
import net.sf.chellow.monad.DesignerException;
import net.sf.chellow.monad.Hiber;
import net.sf.chellow.monad.Invocation;
import net.sf.chellow.monad.MonadUtils;
import net.sf.chellow.monad.ProgrammerException;
import net.sf.chellow.monad.Urlable;
import net.sf.chellow.monad.UserException;
import net.sf.chellow.monad.XmlTree;
import net.sf.chellow.monad.types.MonadInteger;
import net.sf.chellow.monad.types.MonadUri;
import net.sf.chellow.monad.types.UriPathElement;
import net.sf.chellow.physical.HhEndDate;
import net.sf.chellow.physical.MpanCore;
import net.sf.chellow.physical.Organization;
import net.sf.chellow.physical.PersistentEntity;
import net.sf.chellow.physical.Supply;

import org.hibernate.HibernateException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class GenDelta extends PersistentEntity implements Urlable {
	public static GenDelta getGenDelta(Long id) throws UserException,
			ProgrammerException {
		GenDelta genDelta = (GenDelta) Hiber.session().get(GenDelta.class, id);
		if (genDelta == null) {
			throw UserException.newOk("There isn't a gen delta with that id.");
		}
		return genDelta;
	}

	public static void deleteGenDelta(GenDelta genDelta)
			throws ProgrammerException {
		try {
			Hiber.session().delete(genDelta);
			Hiber.flush();
		} catch (HibernateException e) {
			throw new ProgrammerException(e);
		}
	}

	private Organization organization;

	private Supply supply;

	private HhEndDate startDate;

	private int kwhPerMonth;

	public GenDelta() {
		setTypeName("use-delta");
	}

	public GenDelta(Organization organization, Supply supply, HhEndDate startDate,
			int kwhPerMonth) {
		this();
		setOrganization(organization);
		update(supply, startDate, kwhPerMonth);
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public Supply getSupply() {
		return supply;
	}

	public void setSupply(Supply supply) {
		this.supply = supply;
	}

	public HhEndDate getStartDate() {
		return startDate;
	}

	protected void setStartDate(HhEndDate startDate) {
		this.startDate = startDate;
	}

	public int getKwhPerMonth() {
		return kwhPerMonth;
	}

	public void setKwhPerMonth(int kwhPerMonth) {
		this.kwhPerMonth = kwhPerMonth;
	}

	public void update(Supply supply, HhEndDate startDate, int kwhPerMonth) {
		setSupply(supply);
		setStartDate(startDate);
		setKwhPerMonth(kwhPerMonth);
	}

	public Node toXML(Document doc) throws ProgrammerException, UserException {
		Element element = (Element) super.toXML(doc);
		startDate.setLabel("start");
		element.appendChild(startDate.toXML(doc));
		element.setAttributeNode(MonadInteger.toXml(doc, "kwh-per-month",
				kwhPerMonth));
		return element;
	}

	public void httpPost(Invocation inv) throws ProgrammerException,
			UserException, DesignerException, DeployerException {
		String mpanCoreStr = inv.getString("mpan-core");
		Date date = inv.getDate("start-date");
		int kwhPerMonth = inv.getInteger("kwh-per-month");
		if (!inv.isValid()) {
			throw UserException.newInvalidParameter(document());
		}
		try {
			MpanCore mpanCore = organization.getMpanCore(new MpanCoreRaw(mpanCoreStr));
			update(mpanCore.getSupply(), HhEndDate
					.roundUp(date), kwhPerMonth);
		} catch (UserException e) {
			e.setDocument(document());
			throw e;
		}
		Hiber.commit();
		inv.sendOk(document());
	}

	private Document document() throws ProgrammerException, UserException,
			DesignerException {
		Document doc = MonadUtils.newSourceDocument();
		Element source = doc.getDocumentElement();
		source
				.appendChild(getXML(new XmlTree("organization").put("supply"),
						doc));
		return doc;
	}

	public void httpGet(Invocation inv) throws DesignerException,
			ProgrammerException, UserException, DeployerException {
		inv.sendOk(document());
	}

	public MonadUri getUri() throws ProgrammerException, UserException {
		return organization.genDeltasInstance().getUri().resolve(getUriId())
				.append("/");
	}

	public Urlable getChild(UriPathElement uriId) throws ProgrammerException,
			UserException {
		throw UserException.newNotFound();
	}

	public void httpDelete(Invocation inv) throws ProgrammerException,
			DesignerException, UserException, DeployerException {
		deleteGenDelta(this);
		inv.sendOk();
	}
}