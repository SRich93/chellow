/*
 
 Copyright 2005-2008 Meniscus Systems Ltd
 
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

package net.sf.chellow.physical;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.chellow.billing.Account;
import net.sf.chellow.billing.Dce;
import net.sf.chellow.billing.DceService;
import net.sf.chellow.billing.Invoice;
import net.sf.chellow.billing.Supplier;
import net.sf.chellow.billing.SupplierService;
import net.sf.chellow.monad.DeployerException;
import net.sf.chellow.monad.DesignerException;
import net.sf.chellow.monad.Hiber;
import net.sf.chellow.monad.Invocation;
import net.sf.chellow.monad.MonadUtils;
import net.sf.chellow.monad.ProgrammerException;
import net.sf.chellow.monad.Urlable;
import net.sf.chellow.monad.UserException;
import net.sf.chellow.monad.XmlTree;
import net.sf.chellow.monad.types.MonadDate;
import net.sf.chellow.monad.types.MonadLong;
import net.sf.chellow.monad.types.MonadUri;
import net.sf.chellow.monad.types.UriPathElement;

import org.hibernate.HibernateException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SupplyGeneration extends PersistentEntity implements Urlable {
	static public SupplyGeneration getSupplyGeneration(MonadLong id)
			throws ProgrammerException, UserException {
		return getSupplyGeneration(id.getLong());
	}

	static public SupplyGeneration getSupplyGeneration(Long id)
			throws ProgrammerException, UserException {
		try {
			SupplyGeneration supplyGeneration = (SupplyGeneration) Hiber
					.session().get(SupplyGeneration.class, id);
			if (supplyGeneration == null) {
				throw UserException
						.newOk("There is no supply generation with that id.");
			}
			return supplyGeneration;
		} catch (HibernateException e) {
			throw new ProgrammerException(e);
		}
	}

	private Supply supply;

	private Set<SiteSupplyGeneration> siteSupplyGenerations;

	private HhEndDate startDate;

	private HhEndDate finishDate;

	private Mpan importMpan;

	private Mpan exportMpan;

	private Set<Mpan> mpans;

	private Meter meter;

	SupplyGeneration() {
		setTypeName("supply-generation");
	}

	SupplyGeneration(Supply supply, HhEndDate startDate, HhEndDate finishDate,
			Meter meter) throws ProgrammerException, UserException {
		this();
		this.supply = supply;
		setSiteSupplyGenerations(new HashSet<SiteSupplyGeneration>());
		setMpans(new HashSet<Mpan>());
		intrinsicUpdate(startDate, finishDate, meter);
	}

	void setSupply(Supply supply) {
		this.supply = supply;
	}

	public Supply getSupply() {
		return supply;
	}

	public Set<SiteSupplyGeneration> getSiteSupplyGenerations() {
		return siteSupplyGenerations;
	}

	protected void setSiteSupplyGenerations(
			Set<SiteSupplyGeneration> siteSupplyGenerations) {
		this.siteSupplyGenerations = siteSupplyGenerations;
	}

	public HhEndDate getStartDate() {
		return startDate;
	}

	void setStartDate(HhEndDate startDate) {
		this.startDate = startDate;
		startDate.setLabel("start");
	}

	public HhEndDate getFinishDate() {
		return finishDate;
	}

	void setFinishDate(HhEndDate finishDate) {
		this.finishDate = finishDate;
		if (finishDate != null) {
			finishDate.setLabel("finish");
		}
	}

	public Mpan getImportMpan() {
		return importMpan;
	}

	void setImportMpan(Mpan importMpan) {
		this.importMpan = importMpan;
		if (importMpan != null) {
			importMpan.setLabel("import");
		}
	}

	public Mpan getMpan(IsImport isImport) {
		return isImport.getBoolean() ? getImportMpan() : getExportMpan();
	}

	public Dso getDso() {
		Dso dso = null;
		if (getImportMpan() != null) {
			dso = getImportMpan().getMpanCore().getDso();
		}
		if (dso == null && getExportMpan() != null) {
			dso = getExportMpan().getMpanCore().getDso();
		}
		return dso;
	}

	public void attachSite(Site site) throws ProgrammerException, UserException {
		attachSite(site, false);
	}

	public void attachSite(Site site, boolean isLocation)
			throws ProgrammerException, UserException {
		boolean alreadyThere = false;
		for (SiteSupplyGeneration siteSupplyGeneration : siteSupplyGenerations) {
			if (siteSupplyGeneration.getSite().equals(site)) {
				alreadyThere = true;
				break;
			}
		}
		if (alreadyThere) {
			throw UserException
					.newInvalidParameter("The site is already attached to this supply.");
		} else {
			SiteSupplyGeneration siteSupplyGeneration = new SiteSupplyGeneration(
					site, this, false);
			siteSupplyGenerations.add(siteSupplyGeneration);
			site.attachSiteSupplyGeneration(siteSupplyGeneration);
		}
		if (isLocation) {
			setPhysicalLocation(site);
		}
	}

	public void detachSite(Site site) throws UserException, ProgrammerException {
		if (siteSupplyGenerations.size() < 2) {
			throw UserException
					.newInvalidParameter("A supply has to be attached to at least one site.");
		}
		SiteSupplyGeneration siteSupplyGeneration = (SiteSupplyGeneration) Hiber
				.session()
				.createQuery(
						"from SiteSupplyGeneration siteSupplyGeneration where siteSupplyGeneration.supplyGeneration = :supplyGeneration and siteSupplyGeneration.site = :site")
				.setEntity("supplyGeneration", this).setEntity("site", site)
				.uniqueResult();
		if (siteSupplyGeneration == null) {
			throw UserException
					.newInvalidParameter("Can't detach this site, as it wasn't attached in the first place.");
		}
		siteSupplyGenerations.remove(siteSupplyGeneration);
		siteSupplyGenerations.iterator().next().setIsPhysical(true);
		Hiber.flush();
		site.detachSiteSupplyGeneration(siteSupplyGeneration);
		Hiber.flush();
	}

	public void addOrUpdateMpans(MpanTop importMpanTop,
			MpanCore importMpanCore, DceService importContractDce,
			Account importAccountSupplier,
			SupplierService importContractSupplier, boolean importHasImportKwh,
			boolean importHasImportKvarh, boolean importHasExportKwh,
			boolean importHasExportKvarh, Integer importAgreedSupplyCapacity,
			MpanTop exportMpanTop, MpanCore exportMpanCore,
			DceService exportContractDce, Account exportAccountSupplier,
			SupplierService exportContractSupplier, boolean exportHasImportKwh,
			boolean exportHasImportKvarh, boolean exportHasExportKwh,
			boolean exportHasExportKvarh, Integer exportAgreedSupplyCapacity)
			throws ProgrammerException, UserException, DesignerException {
		if (importMpanCore == null && exportMpanCore == null) {
			throw UserException.newInvalidParameter(document(),
					"A supply generation must have at least one MPAN.");
		}
		if (importMpanCore == null) {
			mpans.remove(importMpan);
			setImportMpan(null);
		} else {
			if (!importMpanCore.getSupply().equals(getSupply())) {
				throw UserException
						.newInvalidParameter("This import MPAN core is not attached to this supply.");
			}
			if (!importMpanTop.getLineLossFactor().getIsImport()) {
				throw UserException.newInvalidParameter(document(),
						"The import line loss factor '"
								+ importMpanTop.getLineLossFactor()
								+ "' says that the MPAN is actually export.");
			}
			if (importMpan == null) {
				setImportMpan(new Mpan(this, importMpanTop, importMpanCore,
						importContractDce, importAccountSupplier,
						importContractSupplier, importHasImportKwh,
						importHasImportKvarh, importHasExportKwh,
						importHasExportKvarh, importAgreedSupplyCapacity));
				mpans.add(importMpan);
			} else {
				importMpan.update(importMpanTop, importMpanCore,
						importContractDce, importAccountSupplier,
						importContractSupplier, importHasImportKwh,
						importHasImportKvarh, importHasExportKwh,
						importHasExportKvarh, importAgreedSupplyCapacity);
			}
		}
		if (exportMpanCore == null) {
			mpans.remove(exportMpan);
			setExportMpan(null);
		} else {
			if (!exportMpanCore.getSupply().equals(getSupply())) {
				throw UserException
						.newInvalidParameter("This export MPAN core is not attached to this supply.");
			}
			if (exportMpanTop.getLineLossFactor().getIsImport()) {
				throw UserException
						.newOk("Problem with the export MPAN with core '"
								+ exportMpanCore + "'. The Line Loss Factor '"
								+ exportMpanTop.getLineLossFactor()
								+ "' says that the MPAN is actually import.");
			}
			if (exportMpan == null) {
				setExportMpan(new Mpan(this, exportMpanTop, exportMpanCore,
						exportContractDce, exportAccountSupplier,
						exportContractSupplier, exportHasImportKwh,
						exportHasImportKvarh, exportHasExportKwh,
						exportHasExportKvarh, exportAgreedSupplyCapacity));
				mpans.add(exportMpan);
			} else {
				exportMpan.update(exportMpanTop, exportMpanCore,
						exportContractDce, exportAccountSupplier,
						exportContractSupplier, exportHasImportKwh,
						exportHasImportKvarh, exportHasExportKwh,
						exportHasExportKvarh, exportAgreedSupplyCapacity);
			}
		}

		Hiber.flush();

		// Check that if settlement MPANs then they're the same DSO.
		if (importMpanCore != null && exportMpanCore != null) {
			if (importMpanCore.getDso().isSettlement()
					&& exportMpanCore.getDso().isSettlement()
					&& !importMpanCore.getDso().equals(exportMpanCore.getDso())) {
				throw UserException
						.newOk("Two settlement MPAN generations on the same supply must have the same DSO.");
			}
			if (!importMpanTop.getLineLossFactor().getVoltageLevel().equals(
					exportMpanTop.getLineLossFactor().getVoltageLevel())) {
				throw UserException
						.newOk("The voltage level indicated by the Line Loss Factor must be the same for both the MPANs.");
			}
		}
		Dso dso = getDso();
		if (dso != null && dso.getCode().equals(new DsoCode("22"))) {
			/*
			 * if (importMpan != null) { LineLossFactorCode code =
			 * importLineLossFactor.getCode(); if ((code.equals(new
			 * LineLossFactorCode("520")) || code.equals(new
			 * LineLossFactorCode("550")) || code .equals(new
			 * LineLossFactorCode("580"))) && getExportMpan() == null) { throw
			 * UserException .newOk("The Line Loss Factor of the import MPAN
			 * says that there should be an export MPAN, but there isn't one."); } }
			 */
			if (getExportMpan() != null && getImportMpan() != null) {
				int code = getImportMpan().getMpanTop().getLineLossFactor()
						.getCode();
				if (code != 520 && code != 550 && code != 580) {
					throw UserException
							.newOk("The DSO is 22, there's an export MPAN and the Line Loss Factor of the import MPAN "
									+ getImportMpan()
									+ " can only be 520, 550 or 580.");
				}
			}
		}

		Hiber.flush();
		// more optimization possible here, doesn't necessarily need to check
		// data.
		getSupply().checkAfterUpdate(true, getStartDate(), getFinishDate());
	}

	public Mpan getExportMpan() {
		return exportMpan;
	}

	public void setExportMpan(Mpan exportMpan) {
		this.exportMpan = exportMpan;
		if (exportMpan != null) {
			exportMpan.setLabel("export");
		}
	}

	public Set<Mpan> getMpans() {
		return mpans;
	}

	void setMpans(Set<Mpan> mpans) {
		this.mpans = mpans;
	}

	public Meter getMeter() {
		return meter;
	}

	void setMeter(Meter meter) {
		this.meter = meter;
	}

	public DceService getDceService(boolean isImport, boolean isKwh) {
		DceService dceService = null;
		if (importMpan != null) {
			dceService = importMpan.getDceService(isImport, isKwh);
		}
		if (dceService == null && exportMpan != null) {
			dceService = exportMpan.getDceService(isImport, isKwh);
		}
		return dceService;
	}

	public MpanCore getMpanCore(boolean isImport, boolean isKwh) {
		MpanCore mpanCore = null;
		if (importMpan != null
				&& importMpan.getDceService(isImport, isKwh) != null) {
			mpanCore = importMpan.getMpanCore();
		}
		if (mpanCore == null && exportMpan != null
				&& exportMpan.getDceService(isImport, isKwh) != null) {
			mpanCore = exportMpan.getMpanCore();
		}
		return mpanCore;
	}

	void intrinsicUpdate(HhEndDate startDate, HhEndDate finishDate, Meter meter)
			throws ProgrammerException, UserException {
		if (finishDate != null
				&& startDate.getDate().after(finishDate.getDate())) {
			throw UserException
					.newOk("The generation start date can't be after the finish date.");
		}
		if (startDate == null) {
			throw new ProgrammerException("start date can't be null.");
		}
		setStartDate(startDate);
		setFinishDate(finishDate);
		setMeter(meter);
	}

	void delete() throws UserException, ProgrammerException {
		List<SiteSupplyGeneration> ssGens = new ArrayList<SiteSupplyGeneration>();
		for (SiteSupplyGeneration ssGen : siteSupplyGenerations) {
			ssGens.add(ssGen);
		}
		for (SiteSupplyGeneration ssGen : ssGens) {
			siteSupplyGenerations.remove(ssGen);
			Hiber.flush();
			ssGen.getSite().detachSiteSupplyGeneration(ssGen);
			Hiber.flush();
		}
	}

	public void update(HhEndDate startDate, HhEndDate finishDate, Meter meter)
			throws ProgrammerException, UserException {
		HhEndDate originalStartDate = getStartDate();
		HhEndDate originalFinishDate = getFinishDate();
		if (startDate.equals(originalStartDate)
				&& ((finishDate != null && originalFinishDate != null && finishDate
						.equals(originalFinishDate)) || (finishDate == null && originalFinishDate == null))) {
			return;
		}
		SupplyGeneration previousSupplyGeneration = supply
				.getGenerationPrevious(this);
		if (previousSupplyGeneration != null) {
			if (!previousSupplyGeneration.getStartDate().getDate().before(
					startDate.getDate())) {
				throw UserException
						.newInvalidParameter("The start date must be after the start date of the previous generation.");
			}
			previousSupplyGeneration.intrinsicUpdate(previousSupplyGeneration
					.getStartDate(), startDate.getPrevious(), meter);
		}
		SupplyGeneration nextSupplyGeneration = supply.getGenerationNext(this);
		if (nextSupplyGeneration != null) {
			if (finishDate == null) {
				throw UserException
						.newInvalidParameter("The finish date must be before the finish date of the next generation.");
			}
			if (nextSupplyGeneration.getFinishDate() != null
					&& !finishDate.getDate().before(
							nextSupplyGeneration.getFinishDate().getDate())) {
				throw UserException
						.newInvalidParameter("The finish date must be before the finish date of the next generation.");
			}
			nextSupplyGeneration.intrinsicUpdate(finishDate.getNext(),
					nextSupplyGeneration.getFinishDate(), nextSupplyGeneration
							.getMeter());
		}
		intrinsicUpdate(startDate, finishDate, meter);
		Hiber.flush();
		HhEndDate checkFinishDate = null;
		if (originalFinishDate != null && finishDate != null) {
			checkFinishDate = finishDate.getDate().after(
					originalFinishDate.getDate()) ? finishDate
					: originalFinishDate;
		}
		supply.checkAfterUpdate(true, startDate.getDate().before(
				originalStartDate.getDate()) ? startDate : originalStartDate,
				checkFinishDate);
	}

	public int compareTo(Object obj) {
		return getFinishDate().getDate().compareTo(
				((SupplyGeneration) obj).getFinishDate().getDate());
	}

	public void deleteMpan(Mpan mpan) throws UserException, ProgrammerException {
		if (mpans.size() < 2) {
			throw UserException
					.newOk("There must be at least one MPAN generation in each supply generation.");
		}
		mpans.remove(mpan);
	}

	public Element toXML(Document doc) throws ProgrammerException,
			UserException {
		Element element = (Element) super.toXML(doc);
		element.appendChild(startDate.toXML(doc));
		if (finishDate != null) {
			element.appendChild(finishDate.toXML(doc));
		}
		return element;
	}

	public void httpGet(Invocation inv) throws DesignerException,
			ProgrammerException, UserException, DeployerException {
		inv.sendOk(document());
	}

	private Organization organization() {
		return getSiteSupplyGenerations().iterator().next().getSite()
				.getOrganization();
	}

	@SuppressWarnings("unchecked")
	private Document document() throws ProgrammerException, UserException,
			DesignerException {
		Document doc = MonadUtils.newSourceDocument();
		Element source = doc.getDocumentElement();
		source.appendChild(getXML(new XmlTree("siteSupplyGenerations",
				new XmlTree("site", new XmlTree("organization"))).put("meter")
				.put("supply", new XmlTree("source").put("mpanCores")).put(
						"mpans",
						new XmlTree("mpanCore").put(
								"mpanTop",
								new XmlTree("meterTimeswitch")
										.put("lineLossFactor")).put(
								"dceService").put("supplierAccount",
								new XmlTree("provider")).put("supplierService",
								new XmlTree("provider"))), doc));
		// addVoltageLevelsXML(source);
		Organization organization = organization();
		for (Dce dce : (List<Dce>) Hiber.session().createQuery(
				"from Dce dce where dce.organization = :organization")
				.setEntity("organization", organization).list()) {
			Element dceElement = dce.toXML(doc);
			source.appendChild(dceElement);
			for (DceService dceService : (List<DceService>) Hiber
					.session()
					.createQuery(
							"from DceService service where service.provider = :dce")
					.setEntity("dce", dce).list()) {
				dceElement.appendChild(dceService.toXML(doc));
			}
		}
		source.appendChild(MonadDate.getMonthsXml(doc));
		source.appendChild(MonadDate.getDaysXml(doc));
		source.appendChild(new MonadDate().toXML(doc));
		for (ProfileClass profileClass : (List<ProfileClass>) Hiber
				.session()
				.createQuery(
						"from ProfileClass profileClass order by profileClass.code")
				.list()) {
			source.appendChild(profileClass.toXML(doc));
		}
		return doc;
	}

	public void httpPost(Invocation inv) throws ProgrammerException,
			UserException, DesignerException, DeployerException {
		if (inv.hasParameter("delete")) {
			supply.deleteGeneration(this);
			Hiber.close();
			inv.sendSeeOther(supply.getUri());
		} else if (inv.hasParameter("attach")) {
			SiteCode siteCode = inv.getValidatable(SiteCode.class, "site-code");
			if (!inv.isValid()) {
				throw UserException.newInvalidParameter(document());
			}
			Site site = organization().getSite(siteCode);
			attachSite(site);
			Hiber.commit();
			inv.sendOk(document());
		} else if (inv.hasParameter("detach")) {
			Long siteId = inv.getLong("site-id");
			if (!inv.isValid()) {
				throw UserException.newInvalidParameter(document());
			}
			Site site = organization().getSite(siteId);
			detachSite(site);
			Hiber.commit();
			inv.sendOk(document());
		} else if (inv.hasParameter("set-location")) {
			Long siteId = inv.getLong("site-id");
			if (!inv.isValid()) {
				throw UserException.newInvalidParameter();
			}
			Site site = organization().getSite(siteId);
			setPhysicalLocation(site);
			Hiber.commit();
			inv.sendOk(document());
		} else {
			Organization organization = organization();
			MpanTop importMpanTop = null;
			MpanCore importMpanCore = null;
			Integer importAgreedSupplyCapacity = null;
			Date startDate = inv.getDate("start-date");
			Date finishDate = null;
			String meterSerialNumber = inv.getString("meter-serial-number");
			DceService importDceService = null;
			Account importSupplierAccount = null;
			SupplierService importSupplierService = null;
			boolean importHasImportKwh = false;
			boolean importHasImportKvarh = false;
			boolean importHasExportKwh = false;
			boolean importHasExportKvarh = false;
			boolean isEnded = inv.getBoolean("is-ended");
			if (isEnded) {
				finishDate = inv.getDate("finish-date");
			}
			try {
				Boolean hasImportMpan = inv.getBoolean("has-import-mpan");

				if (hasImportMpan) {
					Long importMpanCoreId = inv.getLong("import-mpan-core-id");
					importMpanCore = MpanCore.getMpanCore(importMpanCoreId);
					Long importProfileClassId = inv
							.getLong("import-profile-class-id");
					ProfileClass importProfileClass = ProfileClass
							.getProfileClass(importProfileClassId);
					int importLlfCode = inv
							.getInteger("import-line-loss-factor-code");
					LineLossFactor importLineLossFactor = importMpanCore
							.getDso().getLineLossFactor(importLlfCode);
					MeterTimeswitchCode importMeterTimeswitchCode = inv
							.getValidatable(MeterTimeswitchCode.class,
									"import-meter-timeswitch-code");
					MeterTimeswitch importMeterTimeswitch = MeterTimeswitch
							.getMeterTimeswitch(importMpanCore.getDso(),
									importMeterTimeswitchCode);
					importMpanTop = MpanTop.getMpanTop(importProfileClass,
							importMeterTimeswitch, importLineLossFactor);
					importAgreedSupplyCapacity = inv
							.getInteger("import-agreed-supply-capacity");
					importHasImportKwh = inv
							.getBoolean("import-has-import-kwh");
					importHasImportKvarh = inv
							.getBoolean("import-has-import-kvarh");
					importHasExportKwh = inv
							.getBoolean("import-has-export-kwh");
					importHasExportKvarh = inv
							.getBoolean("import-has-export-kvarh");
					if (importHasImportKwh || importHasImportKvarh
							|| importHasExportKwh || importHasExportKvarh) {
						String importDceServiceStr = inv
								.getString("import-dce-service-id");
						if (!importDceServiceStr.equals("null")) {
							importDceService = DceService.getDceService(inv
									.getLong("import-dce-service-id"));
						}
					}
					String importSupplierName = inv
							.getString("import-supplier-name");
					String importSupplierServiceName = inv
							.getString("import-supplier-service-name");
					Supplier importSupplier = organization
							.findSupplier(importSupplierName);
					if (importSupplier == null) {
						throw UserException
								.newInvalidParameter("Can't find an import supplier with that name.");
					}
					importSupplierService = importSupplier
							.getService(importSupplierServiceName);
					String importSupplierAccountReference = inv
							.getString("import-supplier-account-reference");
					importSupplierAccount = importSupplier
							.getAccount(importSupplierAccountReference);
				}
				MpanTop exportMpanTop = null;
				MpanCore exportMpanCore = null;
				Integer exportAgreedSupplyCapacity = null;
				DceService exportDceService = null;
				Account exportSupplierAccount = null;
				SupplierService exportSupplierService = null;
				boolean exportHasImportKwh = false;
				boolean exportHasImportKvarh = false;
				boolean exportHasExportKwh = false;
				boolean exportHasExportKvarh = false;
				boolean hasExportMpan = inv.getBoolean("has-export-mpan");
				if (hasExportMpan) {
					MonadLong exportMpanCoreId = inv
							.getMonadLong("export-mpan-core-id");
					exportMpanCore = MpanCore.getMpanCore(exportMpanCoreId);
					Long exportProfileClassId = inv
							.getLong("export-profile-class-id");
					ProfileClass exportProfileClass = ProfileClass
							.getProfileClass(exportProfileClassId);
					int exportLineLossFactorCode = inv
							.getInteger("export-line-loss-factor-code");
					LineLossFactor exportLineLossFactor = exportMpanCore
							.getDso().getLineLossFactor(
									exportLineLossFactorCode);
					MeterTimeswitchCode exportMeterTimeswitchCode = inv
							.getValidatable(MeterTimeswitchCode.class,
									"export-meter-timeswitch-code");
					MeterTimeswitch exportMeterTimeswitch = MeterTimeswitch
							.getMeterTimeswitch(exportMpanCore.getDso(),
									exportMeterTimeswitchCode);
					exportMpanTop = MpanTop.getMpanTop(exportProfileClass,
							exportMeterTimeswitch, exportLineLossFactor);
					exportAgreedSupplyCapacity = inv
							.getInteger("export-agreed-supply-capacity");
					exportHasImportKwh = inv
							.getBoolean("export-has-import-kwh");
					exportHasImportKvarh = inv
							.getBoolean("export-has-import-kvarh");
					exportHasExportKwh = inv
							.getBoolean("export-has-export-kwh");
					exportHasExportKvarh = inv
							.getBoolean("export-has-export-kvarh");
					if (exportHasImportKwh || exportHasImportKvarh
							|| exportHasExportKwh || exportHasExportKvarh) {
						String exportDceServiceStr = inv
								.getString("export-dce-service-id");
						if (!exportDceServiceStr.equals("null")) {
							exportDceService = DceService.getDceService(inv
									.getLong("export-dce-service-id"));
						}
					}
					String exportSupplierName = inv
							.getString("export-supplier-name");
					String exportSupplierServiceName = inv
							.getString("export-supplier-service-name");
					Supplier exportSupplier = organization
							.findSupplier(exportSupplierName);
					if (exportSupplier == null) {
						throw UserException
								.newInvalidParameter("Can't find an export supplier with that name.");
					}
					exportSupplierService = exportSupplier
							.getService(exportSupplierServiceName);
					String exportSupplierAccountReference = inv
							.getString("export-supplier-account-reference");
					exportSupplierAccount = exportSupplier
							.getAccount(exportSupplierAccountReference);
				}
				addOrUpdateMpans(importMpanTop, importMpanCore,
						importDceService, importSupplierAccount,
						importSupplierService, importHasImportKwh,
						importHasImportKvarh, importHasExportKwh,
						importHasExportKvarh, importAgreedSupplyCapacity,
						exportMpanTop, exportMpanCore, exportDceService,
						exportSupplierAccount, exportSupplierService,
						exportHasImportKwh, exportHasImportKvarh,
						exportHasExportKwh, exportHasExportKvarh,
						exportAgreedSupplyCapacity);
				Meter meter = null;
				if (meterSerialNumber != null
						&& meterSerialNumber.length() != 0) {
					meter = supply.findMeter(meterSerialNumber);
					if (meter == null) {
						meter = supply.insertMeter(meterSerialNumber);
					}
				}
				update(new HhEndDate(startDate), finishDate == null ? null
						: new HhEndDate(finishDate), meter);
			} catch (UserException e) {
				e.setDocument(document());
				throw e;
			}
			Hiber.commit();
			inv.sendOk(document());
		}
	}

	public MonadUri getUri() throws ProgrammerException, UserException {
		return supply.getSupplyGenerationsInstance().getUri().resolve(
				getUriId()).append("/");
	}

	public Urlable getChild(UriPathElement uriId) throws ProgrammerException,
			UserException {
		throw UserException.newNotFound();
	}

	public void httpDelete(Invocation inv) throws ProgrammerException,
			DesignerException, UserException, DeployerException {
		// TODO Auto-generated method stub

	}

	public String toString() {
		return "SupplyGeneration id " + getId();
	}

	public void setPhysicalLocation(Site site) throws ProgrammerException,
			UserException {
		SiteSupplyGeneration targetSiteSupply = null;
		for (SiteSupplyGeneration siteSupply : siteSupplyGenerations) {
			if (site.equals(siteSupply.getSite())) {
				targetSiteSupply = siteSupply;
			}
		}
		if (targetSiteSupply == null) {
			throw UserException
					.newOk("The site isn't attached to this supply.");
		}
		for (SiteSupplyGeneration siteSupply : siteSupplyGenerations) {
			siteSupply.setIsPhysical(siteSupply.equals(targetSiteSupply));
		}
		Hiber.flush();
	}

	public RegisterRead insertRegisterRead(RegisterReadRaw rawRegisterRead,
			Invoice invoice) throws UserException, ProgrammerException {
		Mpan importMpan = getImportMpan();
		Mpan exportMpan = getExportMpan();
		RegisterRead read = null;
		if (importMpan != null
				&& importMpan.getMpanRaw().equals(rawRegisterRead.getMpanRaw())) {
			read = invoice.insertRead(importMpan, rawRegisterRead);
		} else if (exportMpan != null
				&& exportMpan.getMpanRaw().equals(rawRegisterRead.getMpanRaw())) {
			read = invoice.insertRead(exportMpan, rawRegisterRead);
		} else {
			throw UserException.newInvalidParameter("For the supply " + getId()
					+ " neither the import MPAN " + importMpan
					+ " or the export MPAN " + exportMpan
					+ " match the register read MPAN "
					+ rawRegisterRead.getMpanRaw() + ".");
		}
		return read;
	}
}