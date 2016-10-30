package fr.unitEstimator.central.catalog;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.bean.CustomScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.comparators.ComparatorChain;
import org.apache.commons.collections.comparators.NullComparator;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.icesoft.faces.context.effects.JavascriptContext;

import fr.ue.shared.UserContext;
import fr.unitEstimator.bean.util.PartCustomAttributeWrapper;
import fr.unitEstimator.bean.util.SessionUtil;
import fr.unitEstimator.central.pricing.PartHistoryComponent;
import fr.unitEstimator.dao.competitive.CatalogCountryDao;
import fr.unitEstimator.dao.competitive.PartCountryDataDao;
import fr.unitEstimator.dao.core.PartDao;
import fr.unitEstimator.model.database.central.CentralRule;
import fr.unitEstimator.model.database.central.feature.PartSecondaryFeature;
import fr.unitEstimator.model.database.competitive.CatalogCountry;
import fr.unitEstimator.model.database.competitive.PartCountryData;
import fr.unitEstimator.model.database.competitive.PartCountryRecommendation;
import fr.unitEstimator.model.database.competitive.embedded.RecommendationStatusEnum;
import fr.unitEstimator.model.database.core.Catalog;
import fr.unitEstimator.model.database.core.Part;
import fr.unitEstimator.model.value.PricingObject;
import fr.unitEstimator.service.central.CentralRuleService;
import fr.unitEstimator.service.central.CustomAttributeServiceFacade;
import fr.unitEstimator.service.central.PartRuleResultService;
import fr.unitEstimator.service.central.PricingRuleService;
import fr.unitEstimator.service.central.UnitService;
import fr.unitEstimator.service.core.CatalogService;
import fr.unitEstimator.service.core.PartHistoryService;
import fr.unitEstimator.service.core.PartService;
import fr.unitEstimator.service.core.indicators.ActualGain;
import fr.unitEstimator.service.core.indicators.IndicatorService;
import fr.unitEstimator.service.core.indicators.Indicators;
import fr.unitEstimator.service.core.indicators.Margin;
import fr.unitEstimator.service.core.indicators.Turnover;
import fr.unitEstimator.service.process.ProcessFactory;
import fr.unitEstimator.service.process.ProcessManagementService;
import fr.unitEstimator.util.LanguageController;
import fr.unitEstimator.util.PrixStatutEnum;
import fr.unitEstimator.util.RegleStatutEnum;
import fr.unitEstimator.util.StringUtil;

@ManagedBean(name = "ProduitDetail")
@CustomScoped(value = "#{window}")
public class UnitDetailBean
{

	// private static final long serialVersionUID = -3763609741550374114L;
	private static final String GO_TO_VALIDATED_RULE_PAGE = "/portlet/rule/index.iface";
	private static final Logger LOGGER = Logger.getLogger(PartDetailBean.class);
	private static final String GO_TO_PART_DETAIL = "detail";

	private static Comparator<PartSecondaryFeature> TYPE_AND_LABEL_COMPARATOR = new Comparator<PartSecondaryFeature>()
	{
		// Ordering first by type, then by label
		@Override
		public int compare(PartSecondaryFeature o1, PartSecondaryFeature o2)
		{
			int result = o1.getSecondaryFeature().getParent().getName()
			        .compareTo(o2.getSecondaryFeature().getParent().getName());

			if (result == 0)
			{
				return o1.getSecondaryFeature().getName().compareTo(o2.getSecondaryFeature().getName());
			}

			return result;
		}
	};

	private Part part;
	private String viewOrigin = null;
	private String reference;
	private CentralRule rule;
	private int nbPhoto;
	private List<SelectItem> photoReferenceList = new ArrayList<SelectItem>();
	private List<PricingObject> listParamCoefficient = new ArrayList<PricingObject>();
	private List<PricingObject> listParamMonetaire = new ArrayList<PricingObject>();
	private String photoSelected;
	private String infoMasse;
	private String infoDimension;
	private String infoVolume;
	private String infoSurface;
	private Long catalogueId;
	private Catalog catalogue;
	private String locale;
	private List<String> remplacant;
	private List<String> remplaces;

	private List<CountryPojo> catalogCountries = new ArrayList<CountryPojo>();
	private boolean displayCountryList;
	private String comingFrompage;

	private boolean grayGoToValidatedRuleLink = false;

	private List<PartCustomAttributeWrapper> customAttrArticleDisplayWrapperList = new ArrayList<PartCustomAttributeWrapper>();
	private String IscustomAttrArticleDisplayWrapperListEmpty = new String();

	@ManagedProperty(value = "#{partServiceCore}")
	private PartService partService;

	@ManagedProperty(value = "#{centralRuleService}")
	private CentralRuleService centralRuleService;

	@ManagedProperty(value = "#{pricingRuleService}")
	private PricingRuleService pricingRuleService;

	@ManagedProperty(value = "#{partRuleResultService}")
	private PartRuleResultService partRuleResultService;

	@ManagedProperty(value = "#{unitServiceCore}")
	private UnitService uniteService;

	@ManagedProperty(value = "#{customAttributeServiceFacade}")
	private CustomAttributeServiceFacade customAttributeServiceFacade;

	@ManagedProperty(value = "#{catalogCountryDao}")
	private CatalogCountryDao countryDao;

	@ManagedProperty(value = "#{partCountryDataDao}")
	private PartCountryDataDao partCountryDataDao;

	@ManagedProperty(value = "#{indicatorService}")
	private IndicatorService indicatorService;

	@ManagedProperty(value = "#{catalogServiceCore}")
	CatalogService catalogService;

	@ManagedProperty(value = "#{partDao}")
	private PartDao partDao;

	@ManagedProperty(value = "#{partHistoryComponent}")
	private PartHistoryComponent partHistoryComponent;

	@ManagedProperty(value = "#{partHistoryService}")
	private PartHistoryService partHistoryService;

	@ManagedProperty(value = "#{userContext}")
	private UserContext userContext;

	@ManagedProperty(value = "#{processManagementService}")
	private ProcessManagementService processManagementService;

	@ManagedProperty(value = "#{processFactory}")
	private ProcessFactory processFactory;

	@PostConstruct
	public void init()
	{
		Locale localeObject = SessionUtil.getLocale(FacesContext.getCurrentInstance());
		locale = localeObject.getLanguage();

		catalogueId = SessionUtil.getCatalogueId(FacesContext.getCurrentInstance());
		catalogue = catalogService.get(SessionUtil.getCatalogue(FacesContext.getCurrentInstance()).getId());
		reference = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
		        .get("reference");

		if (reference != null)
		{

			// We get the item

			viewOrigin = getViewOriginFromRequest();
			if (viewOrigin != null)
			{
				if (viewOrigin.equals("CENTRAL_RULE"))
				{
					part = getPartFromRequest();
				} else
				{
					part = partService.getByReferenceAndFetchLabels(reference, catalogue, true);
				}
			} else
			{
				part = partService.getByReferenceAndFetchLabels(reference, catalogue, true);
			}

			// We obtain the validated rule, to which the unit is attached
			rule = centralRuleService.getConfigurationRegleBySubFamilyIdAndStatutAndFetchParameters(
			        part.getSousFamille().getId(), RegleStatutEnum.VALIDATE);

			listParamCoefficient.clear();
			listParamMonetaire.clear();
			// If exist
			if (rule != null)
			{
				
				part.getPartRuleResult(rule);
			}

			Collection<Part> parts = Lists.newArrayList(part);
			indicatorService.computeTurnovers(Turnover.CENTRAL_ALL_TURNOVERS, parts, true);
			indicatorService.computeMargins(Margin.CENTRAL_ALL_MARGINS, parts, true);
			indicatorService.computeActualGains(ActualGain.CENTRAL_ALL_GAINS, parts, true);

			// We clean to avoid cache problems
			photoReferenceList.clear();
			// We initialise the information on the units
			FacesContext facesContext = FacesContext.getCurrentInstance();
			infoMasse = uniteService.convertirUnite(1, part.getMasse(), locale,
			        SessionUtil.getClientInfos(facesContext).getDefaultCriteria());
			String longueur = uniteService.convertirUnite(2, part.getLongueur(), locale,
			        SessionUtil.getClientInfos(facesContext).getDefaultCriteria());
			String largeur = uniteService.convertirUnite(3, part.getLargeur(), locale,
			        SessionUtil.getClientInfos(facesContext).getDefaultCriteria());
			String hauteur = uniteService.convertirUnite(4, part.getHauteur(), locale,
			        SessionUtil.getClientInfos(facesContext).getDefaultCriteria());
			infoDimension = longueur + " x " + largeur + " x " + hauteur;

			infoVolume = uniteService.convertirUnite(6, part.getVolumeAsDouble(), locale,
			        SessionUtil.getClientInfos(facesContext).getDefaultCriteria());
			infoSurface = uniteService.convertirUnite(5, part.getSurfaceAsDouble(), locale,
			        SessionUtil.getClientInfos(facesContext).getDefaultCriteria());

			// Fetch list of customAttributeDisplayables with values and labels
			this.customAttrArticleDisplayWrapperList = customAttributeServiceFacade
			        .produceCustomAttributeList(catalogueId, part);
			this.IscustomAttrArticleDisplayWrapperListEmpty = String
			        .valueOf(customAttrArticleDisplayWrapperList.isEmpty());
			// On recupere les photos de l'article
			if (part.getImage1() != null)
			{
				photoReferenceList.add(new SelectItem(String.valueOf(1), ""));
			}
			if (part.getImage2() != null)
			{
				photoReferenceList.add(new SelectItem(String.valueOf(2), ""));
			}
			if (part.getImage3() != null)
			{
				photoReferenceList.add(new SelectItem(String.valueOf(3), ""));
			}
			photoSelected = String.valueOf(part.getImageDisplay());

			// Recover data on replaced units
			List<Long> remplacantArticleIds = new ArrayList<Long>();
			Long remplacantId = part.getArticleRemplacant();
			if (remplacantId != null)
			{
				remplacantArticleIds.add(remplacantId);
				remplacant = partService.getArticlesReferencesByIds(remplacantArticleIds);
			} else
			{
				remplacant = new ArrayList<String>();
				remplacant.add(StringUtil.DASH);
			}

			List<Long> remplacesIdList = part.getArticleRemplaces();
			if (remplacesIdList.size() > 0)
			{
				remplaces = partService.getArticlesReferencesByIds(remplacesIdList);
			} else
			{
				remplaces = new ArrayList<String>();
				remplaces.add(StringUtil.DASH);
			}

			/* Link to validated rule management */
			grayGoToValidatedRuleLink = !validatedRuleExists();
			partHistoryComponent.init(part);
		}
	}

	private String getViewOriginFromRequest()
	{
		String fromRequest = getRequestParameter("viewOrigin");
		String result = null;
		if (fromRequest != null)
		{
			result = String.valueOf(fromRequest);
		}
		return result;
	}

	private Part getPartFromRequest()
	{
		Part result = null;
		String partIdString = getRequestParameter("partId");

		if (!StringUtils.isEmpty(partIdString))
		{
			result = partDao.getByIdFetchFeaturesAndLabels(Long.parseLong(partIdString));
		}
		return result;
	}

	private String getRequestParameter(String key)
	{
		return FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(key);
	}

	public String detail()
	{
		init();
		return GO_TO_PART_DETAIL;
	}

	public void changePhoto(ValueChangeEvent ve) throws IOException, SQLException
	{
		if (ve.getNewValue() != null)
		{
			String photoSelectedTmp = ve.getNewValue().toString();
			setPhotoSelected(photoSelectedTmp);
			int numeroInt = Integer.parseInt(photoSelected);
			part.setImageDisplay(numeroInt);
			partService.save(part);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void addPartCountryData(ActionEvent av)
	{

		Collection list = null;
		// Step1 : remove lines related to unchecked countries.
		List<String> countryIsoCodes = new ArrayList<String>();
		for (CountryPojo countryPojo : catalogCountries)
		{
			PartPrice partPrice = findPartPrice(countryPojo);
			if (!countryPojo.isSelected() && partPrice != null)
			{
				getPartHistoryComponent().getPartPrices().remove(partPrice);
			} else if (countryPojo.isSelected() && partPrice == null)
			{
				countryIsoCodes.add(countryPojo.getCountryIsoCode());
			}
		}
		// Step2 : add new lines related to new checked countries.
		if (!countryIsoCodes.isEmpty())
		{
			List<PartCountryData> dbPartCountryDatas = partCountryDataDao.findPartCountryDatas(countryIsoCodes,
			        part.getId());
			for (String isoCode : countryIsoCodes)
			{
				PartCountryData partCountryData = findPartCountryData(isoCode, dbPartCountryDatas);
				StraightPartPrice partPrice = new StraightPartPrice();
				partPrice.setType(PartPriceEnum.COUNTRY);
				partPrice.setCountryName(new CountryPojo(isoCode).getCountryName());
				if (partCountryData != null)
				{
					// countries exchange
					partPrice.setCurrentRetailPrice(partCountryData.getCurrentRetailPrice());
					partPrice.setFormerCurrrentPrice(partCountryData.getFormerCurrentRetailPrice());
					partPrice.setPriceBeforeUnitEstimator(partCountryData.getBeforeUnitEstimatorPrice());
					partPrice.setQuantity1(partCountryData.getQuantity());
					partPrice.setQuantity2(partCountryData.getQuantityY1());
					partPrice.setQuantity3(partCountryData.getQuantityY2());
					partPrice.setCti(partCountryData.getCti() != null ? partCountryData.getCti().getValue() : null);
					partPrice.setLifecycle(partCountryData.getLifeCycle() == null ? null
					        : LanguageController.get(partCountryData.getLifeCycle().getKey()));
					partPrice.setNetworkDiscount(partCountryData.getDiscount());

					// Recommended Price, Target price, Status
					PartCountryRecommendation validatedRecommendation = partCountryData.getValidatedRecommendation();
					PartCountryRecommendation pendingRecommendation = partCountryData.getPendingRecommendation();

					Set<Margin> types = new HashSet<Margin>();
					types.add(Margin.BEFORE_UNITESTIMATOR_RETAIL_PRICE);
					types.add(Margin.FORMER_CURRENT_RETAIL_PRICE);
					types.add(Margin.CURRENT_RETAIL_PRICE);
					types.add(Margin.TARGET_PRICE);
					types.add(Margin.RECOMMENDED_PRICE);
					types.add(Margin.FINAL_PRICE);

					if (validatedRecommendation != null)
					{
						Boolean accordingRecommendation = validatedRecommendation.getAccordingRecommendation();
						partPrice.setTargetPrice(validatedRecommendation.getTargetPrice());
						partPrice.setRecommendedPrice(validatedRecommendation.getRecommendedPrice());
						partPrice.setValidatedUnitEstimatorPrice(validatedRecommendation.getFinalPrice());

						list = Lists.newArrayList(validatedRecommendation);
						indicatorService.computeMargins(types, list, true);

						partPrice.setMarginRecommendedPrice(
						        validatedRecommendation.getIndicators().getMarginRecommendedPrice());
						partPrice.setMarginFinalPrice(validatedRecommendation.getIndicators().getMarginFinalPrice());

						// Just for displaying flags
						partPrice.setAlerteAchat(validatedRecommendation.getCostAlert().isActivated());
						partPrice.setImplementationProgressive(
						        validatedRecommendation.getProgressiveImplementation().isActivated());

						populateArticleDataIndicators(partPrice, validatedRecommendation);

						if (pendingRecommendation != null)
						{
							partPrice.setChangeInProgressPrice(pendingRecommendation.getChangedPrice());

							list = Lists.newArrayList(pendingRecommendation);
							BigDecimal computeMarginOfChangedPrice = indicatorService
							        .computeMargins(Sets.newHashSet(Margin.CHANGED_PRICE), list, true)
							        .getMarginChangedPrice();
							partPrice.setMarginChangeInProgressPrice(computeMarginOfChangedPrice);
							if (accordingRecommendation)
								partPrice.setStatus(PrixStatutEnum.VALIDE_SP_ATT_DE_MOD.toString());
							else
								partPrice.setStatus(PrixStatutEnum.VALIDE_HP_ATT_DE_MOD.toString());
						} else
						{
							if (accordingRecommendation)
								partPrice.setStatus(PrixStatutEnum.VALIDE_SP.toString());
							else
								partPrice.setStatus(PrixStatutEnum.VALIDE_HP.toString());
						}
					} else
					{
						if (pendingRecommendation != null)
						{

							list = Lists.newArrayList(pendingRecommendation);
							indicatorService.computeMargins(types, list, true);

							partPrice.setAlerteAchat(pendingRecommendation.getCostAlert().isActivated());
							partPrice.setImplementationProgressive(
							        pendingRecommendation.getProgressiveImplementation().isActivated());

							populateArticleDataIndicators(partPrice, pendingRecommendation);


							partPrice.setChangeInProgressPrice(
							        pendingRecommendation.getProgressiveImplementation().getPriceStep2());

							partPrice.setTargetPrice(pendingRecommendation.getTargetPrice());
							partPrice.setRecommendedPrice(pendingRecommendation.getRecommendedPrice());
							partPrice.setMarginRecommendedPrice(
							        pendingRecommendation.getIndicators().getMarginRecommendedPrice());

							list = Lists.newArrayList(pendingRecommendation);
							BigDecimal computeMarginOfChangedPrice = indicatorService
							        .computeMargins(Sets.newHashSet(Margin.CHANGED_PRICE), list, true)
							        .getMarginChangedPrice();
							partPrice.setMarginChangeInProgressPrice(computeMarginOfChangedPrice);

							if (RecommendationStatusEnum.VALIDATED_HP_PENDING_UPDATE
							        .equals(pendingRecommendation.getStatus()))
							{
								partPrice.setStatus(PrixStatutEnum.VALIDE_HP_ATT_DE_MOD.toString());
							} else if (RecommendationStatusEnum.VALIDATED_SP_PENDING_UPDATE
							        .equals(pendingRecommendation.getStatus()))
							{
								partPrice.setStatus(PrixStatutEnum.VALIDE_SP_ATT_DE_MOD.toString());
							} else
							{
								partPrice.setStatus(PrixStatutEnum.AVT_UNITESTIMATOR_ATT_DE_VAL.toString());
							}
						} else
						{
							list = Lists.newArrayList(partCountryData);
							indicatorService.computeMargins(types, list, true);
							populateArticleDataIndicators(partPrice, partCountryData);
							partPrice.setStatus(PrixStatutEnum.AVT_UNITESTIMATOR.toString());
						}
					}
				}
				getPartHistoryComponent().getPartPrices().add(partPrice);
			}
		}
		displayCountryList = false;
	}

	private void populateArticleDataIndicators(PartPrice partPrice, PartCountryRecommendation reco)
	{
		Indicators indicators = reco.getIndicators();
		partPrice.setMarginBeforeUnitEstimatorRetailPrice(indicators.getMarginBeforeUnitEstimatorRetailPrice());
		partPrice.setMarginFormerCurrentRetailPrice(indicators.getMarginFormerCurrentRetailPrice());
		partPrice.setMarginCurrentRetailPrice(indicators.getMarginCurrentRetailPrice());
		partPrice.setMarginTargetPrice(indicators.getMarginTargetPrice());
	}

	private void populateArticleDataIndicators(PartPrice partPrice, PartCountryData data)
	{
		Indicators indicators = data.getIndicators();
		partPrice.setMarginBeforeUnitEstimatorRetailPrice(indicators.getMarginBeforeUnitEstimatorRetailPrice());
		partPrice.setMarginFormerCurrentRetailPrice(indicators.getMarginFormerCurrentRetailPrice());
		partPrice.setMarginCurrentRetailPrice(indicators.getMarginCurrentRetailPrice());
		partPrice.setMarginTargetPrice(indicators.getMarginTargetPrice());
	}

	private PartCountryData findPartCountryData(String isoCode, List<PartCountryData> dbPartCountryDatas)
	{
		for (PartCountryData data : dbPartCountryDatas)
		{
			if (data.getCountry().getIsoCode().equalsIgnoreCase(isoCode))
				return data;
		}
		return null;
	}

	private PartPrice findPartPrice(CountryPojo countryPojo)
	{
		for (PartPrice partPrice : partHistoryComponent.getPartPrices())
		{
			if (partPrice instanceof StraightPartPrice)
			{
				if (partPrice.getType().equals(PartPriceEnum.COUNTRY)
				        && ((StraightPartPrice) partPrice).getCountryName().equals(countryPojo.getCountryName()))
				{
					return partPrice;
				}
			}
		}
		return null;
	}

	public void hideCountryList(ActionEvent av)
	{
		displayCountryList = false;
	}

	@SuppressWarnings("unchecked")
	public void displayCountryList(ActionEvent av)
	{
		if (catalogCountries.isEmpty())
		{
			Set<CatalogCountry> dbCountries = new HashSet<CatalogCountry>();
			dbCountries.addAll(countryDao.findCountries());
			for (CatalogCountry dbCountry : dbCountries)
			{
				CountryPojo countryPojo = new CountryPojo(dbCountry.getIsoCode());
				catalogCountries.add(countryPojo);
			}
			ComparatorChain comparatorChain = new ComparatorChain();
			comparatorChain.addComparator(new BeanComparator("countryName", new NullComparator(true)));
			Collections.sort(catalogCountries, comparatorChain);
		}

		displayCountryList = true;

		JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "setPopupMarginTopTo33();");
	}

	/* Redirection to the validated rule which the part belongs to*/
	public void goToValidatedRule(ActionEvent ae)
	{
		long subfamilyId = part.getSousFamille().getId();
		RegleStatutEnum status = RegleStatutEnum.VALIDATE;
		CentralRule rule = centralRuleService.getConfigurationRegleBySubFamilyIdAndStatutAndFetchParameters(subfamilyId,
		        status);
		if (rule != null)
		{
			this.redirectToPage(GO_TO_VALIDATED_RULE_PAGE + "?viewOrigin=PART_DETAIL&partId=" + part.getId()
			        + "&ruleContext=CHECK&ruleId=" + rule.getId().toString());
		}
	}

	private void redirectToPage(String destination)
	{
		String contextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
		try
		{
			FacesContext.getCurrentInstance().getExternalContext().redirect(contextPath + destination);
		} catch (IOException e)
		{
			LOGGER.error(e.toString());
		}
	}

	public boolean validatedRuleExists()
	{
		long subfamilyId = part.getSousFamille().getId();
		RegleStatutEnum status = RegleStatutEnum.VALIDATE;
		CentralRule rule = centralRuleService.getConfigurationRegleBySubFamilyIdAndStatut(subfamilyId, status);
		return rule == null ? false : true;
	}

	public void setReference(String reference)
	{
		this.reference = reference;
	}

	public String getReference()
	{
		return reference;
	}

	public Part getArticle()
	{
		return part;
	}

	public CentralRule getConfigRegle()
	{
		return rule;
	}

	public void setConfigRegle(CentralRule configRegle)
	{
		this.rule = configRegle;
	}

	public int getNbPhoto()
	{
		return nbPhoto;
	}

	public void setNbPhoto(int nbPhoto)
	{
		this.nbPhoto = nbPhoto;
	}

	public List<SelectItem> getPhotoReferenceList()
	{
		return photoReferenceList;
	}

	public void setPhotoReferenceList(List<SelectItem> photoReferenceList)
	{
		this.photoReferenceList = photoReferenceList;
	}

	public String getPhotoSelected()
	{
		return photoSelected;
	}

	public void setPhotoSelected(String photoSelected)
	{
		this.photoSelected = photoSelected;
	}

	public String getInfoDimension()
	{
		return infoDimension;
	}

	public void setInfoDimension(String infoDimension)
	{
		this.infoDimension = infoDimension;
	}

	public String getInfoMasse()
	{
		return infoMasse;
	}

	public void setInfoMasse(String infoMasse)
	{
		this.infoMasse = infoMasse;
	}

	public List<PricingObject> getListParamCoefficient()
	{
		return listParamCoefficient;
	}

	public void setListParamCoefficient(List<PricingObject> listParamCoefficient)
	{
		this.listParamCoefficient = listParamCoefficient;
	}

	public List<PricingObject> getListParamMonetaire()
	{
		return listParamMonetaire;
	}

	public void setListParamMonetaire(List<PricingObject> listParamMonetaire)
	{
		this.listParamMonetaire = listParamMonetaire;
	}

	public List<PartCustomAttributeWrapper> getCustomAttrArticleDisplayWrapperList()
	{
		return customAttrArticleDisplayWrapperList;
	}

	public void setCustomAttrArticleDisplayWrapperList(
	        List<PartCustomAttributeWrapper> customAttrArticleDisplayWrapperList)
	{
		this.customAttrArticleDisplayWrapperList = customAttrArticleDisplayWrapperList;
	}

	public String getIscustomAttrArticleDisplayWrapperListEmpty()
	{
		return IscustomAttrArticleDisplayWrapperListEmpty;
	}

	public void setIscustomAttrArticleDisplayWrapperListEmpty(String iscustomAttrArticleDisplayWrapperListEmpty)
	{
		IscustomAttrArticleDisplayWrapperListEmpty = iscustomAttrArticleDisplayWrapperListEmpty;
	}

	public List<String> getRemplacant()
	{
		return remplacant;
	}

	public List<String> getRemplaces()
	{
		return remplaces;
	}

	public void setDisplayCountryList(boolean displayCountryList)
	{
		this.displayCountryList = displayCountryList;
	}

	public boolean isDisplayCountryList()
	{
		return displayCountryList;
	}

	public void setCatalogCountries(List<CountryPojo> catalogCountries)
	{
		this.catalogCountries = catalogCountries;
	}

	public List<CountryPojo> getCatalogCountries()
	{
		return catalogCountries;
	}

	public void setPartCountryDataDao(PartCountryDataDao partCountryDataDao)
	{
		this.partCountryDataDao = partCountryDataDao;
	}

	public PartCountryDataDao getPartCountryDataDao()
	{
		return partCountryDataDao;
	}

	public void setInfoVolume(String infoVolume)
	{
		this.infoVolume = infoVolume;
	}

	public String getInfoVolume()
	{
		return infoVolume;
	}

	public void setInfoSurface(String infoSurface)
	{
		this.infoSurface = infoSurface;
	}

	public String getInfoSurface()
	{
		return infoSurface;
	}

	public boolean isDisplayGoToValidatedRuleLink()
	{
		return grayGoToValidatedRuleLink;
	}

	public void setDisplayGoToValidatedRuleLink(boolean displayGoToValidatedRuleLink)
	{
		this.grayGoToValidatedRuleLink = displayGoToValidatedRuleLink;
	}

	public void setPart(Part part)
	{
		this.part = part;
	}

	public void setRule(CentralRule rule)
	{
		this.rule = rule;
	}

	public void setCatalogueId(Long catalogueId)
	{
		this.catalogueId = catalogueId;
	}

	public void setCatalogue(Catalog catalogue)
	{
		this.catalogue = catalogue;
	}

	public void setLocale(String locale)
	{
		this.locale = locale;
	}

	public void setGrayGoToValidatedRuleLink(boolean grayGoToValidatedRuleLink)
	{
		this.grayGoToValidatedRuleLink = grayGoToValidatedRuleLink;
	}

	public void setPartService(PartService partService)
	{
		this.partService = partService;
	}

	public void setCentralRuleService(CentralRuleService centralRuleService)
	{
		this.centralRuleService = centralRuleService;
	}

	public void setPartRuleResultService(PartRuleResultService partRuleResultService)
	{
		this.partRuleResultService = partRuleResultService;
	}

	public void setUniteService(UnitService uniteService)
	{
		this.uniteService = uniteService;
	}

	public void setCustomAttributeServiceFacade(CustomAttributeServiceFacade CustomAttributeServiceFacade)
	{
		this.customAttributeServiceFacade = CustomAttributeServiceFacade;
	}

	public void setCountryDao(CatalogCountryDao countryDao)
	{
		this.countryDao = countryDao;
	}

	public void setIndicatorService(IndicatorService indicatorService)
	{
		this.indicatorService = indicatorService;
	}

	public void setCatalogService(CatalogService catalogService)
	{
		this.catalogService = catalogService;
	}

	public void setUserContext(UserContext userContext)
	{
		this.userContext = userContext;
	}

	public void setProcessFactory(ProcessFactory processFactory)
	{
		this.processFactory = processFactory;
	}

	public void setProcessManagementService(ProcessManagementService processManagementService)
	{
		this.processManagementService = processManagementService;
	}

	public String getComingFrompage()
	{
		return comingFrompage;
	}

	public void setComingFrompage(String comingFrompage)
	{
		this.comingFrompage = comingFrompage;
	}

	public void setPricingRuleService(PricingRuleService pricingRuleService)
	{
		this.pricingRuleService = pricingRuleService;
	}

	public List<PartSecondaryFeature> getOrderedCoefficientFeatures()
	{
		List<PartSecondaryFeature> result = part.getCoefficientFeatures();
		Collections.sort(result, TYPE_AND_LABEL_COMPARATOR);
		return result;
	}

	public List<PartSecondaryFeature> getOrderedMonetaryFeatures()
	{
		List<PartSecondaryFeature> result = part.getMonetaryFeatures();
		Collections.sort(result, TYPE_AND_LABEL_COMPARATOR);
		return result;
	}

	public PartHistoryService getPartHistoryService()
	{
		return partHistoryService;
	}

	public void setPartHistoryService(PartHistoryService partHistoryService)
	{
		this.partHistoryService = partHistoryService;
	}

	public PartHistoryComponent getPartHistoryComponent()
	{
		return partHistoryComponent;
	}

	public void setPartHistoryComponent(PartHistoryComponent partHistoryComponent)
	{
		this.partHistoryComponent = partHistoryComponent;
	}

	public void setPartDao(PartDao partDao)
	{
		this.partDao = partDao;
	}
}