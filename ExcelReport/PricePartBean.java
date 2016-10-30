package fr.unitEstimator.central.partpricing;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static fr.unitEstimator.service.core.indicators.Margin.BEFORE_unitEstimator_RETAIL_PRICE;
import static fr.unitEstimator.service.core.indicators.Margin.CURRENT_RETAIL_PRICE;
import static fr.unitEstimator.service.core.indicators.Margin.FORMER_CURRENT_RETAIL_PRICE;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.icesoft.faces.context.effects.JavascriptContext;

import fr.ue.event.Event;
import fr.ue.event.Propagator;
import fr.unitEstimator.bean.util.PartCustomAttributeWrapper;
import fr.unitEstimator.bean.util.SessionUtil;
import fr.unitEstimator.central.pricing.PartHistoryComponent;
import fr.unitEstimator.central.pricing.PriceCreationBean;
import fr.unitEstimator.central.pricing.PriceUpdateBean;
import fr.unitEstimator.central.pricing.PriceValidationBean;
import fr.unitEstimator.dao.core.PartDao;
import fr.unitEstimator.event.unitEstimatorEventTypeRegistry;
import fr.unitEstimator.model.database.central.CentralRule;
import fr.unitEstimator.model.database.central.PartRuleResult;
import fr.unitEstimator.model.database.core.Part;
import fr.unitEstimator.model.database.core.PartTypeEnum;
import fr.unitEstimator.model.database.core.SubFamily;
import fr.unitEstimator.model.database.core.princingtype.ILaunchablePricingMethod;
import fr.unitEstimator.model.database.core.princingtype.GenericPricingMethod;
import fr.unitEstimator.model.database.core.princingtype.PricingMethodWithFeatures;
import fr.unitEstimator.model.database.core.princingtype.RangePricingMethod;
import fr.unitEstimator.service.central.CentralRuleService;
import fr.unitEstimator.service.central.CustomAttributeServiceFacade;
import fr.unitEstimator.service.central.PricingRuleService;
import fr.unitEstimator.service.core.PartHistoryService;
import fr.unitEstimator.service.core.PartService;
import fr.unitEstimator.service.core.SubFamilyService;
import fr.unitEstimator.service.core.indicators.IndicatorService;
import fr.unitEstimator.service.core.indicators.Indicators;
import fr.unitEstimator.shared.SegmentationChangeEventEntity;
import fr.unitEstimator.util.BeanLocator;
import fr.unitEstimator.util.LanguageController;
import fr.unitEstimator.util.Money;
import fr.unitEstimator.util.PartPriceUpdateOriginEnum;
import fr.unitEstimator.util.RegleStatutEnum;
import fr.unitEstimator.util.StringUtil;
import fr.unitEstimator.util.WorkflowStateEnum;

@Component("pricePart")
@Scope("view")
public class PricePartBean implements Serializable, ChangeListener
{

	private static final long serialVersionUID = 5336805471122172135L;

	private static final String METHODS_FOLDER = "pricing_method_info/";
	private static final String XHTML_SUFFIX = ".xhtml";
	private static final String LIST_REFERENCES_NON_TARIFEE = "ListReferencesNonTarifee";
	private static final String LIST_REFERENCES_NON_VALIDEES = "ListReferencesNonValidees";
	private static final String LIST_REFERENCES_FOR_MODIF_PRIX = "ListReferencesForModifPrix";
	private static final String GO_TO_VALIDATED_RULE_PAGE = "/portlet/rule/index.iface";

	private final static Logger LOGGER = Logger.getLogger(PricePartBean.class);

	@Autowired
	private PartHistoryComponent partHistoryComponent;
	@Autowired
	private PartHistoryService partHistoryService;
	@Autowired
	PartService partService;
	@Autowired
	PartDao partDao;
	@Autowired
	CentralRuleService centralRuleService;
	@Autowired
	PricingRuleService pricingRuleService;
	@Autowired
	private SubFamilyService subFamilleService;
	@Autowired
	private IndicatorService indicatorService;
	@Autowired
	private CustomAttributeServiceFacade customAttributeServiceFacade;
	@Autowired
	private Propagator propagator;

	private UIComponent selectedPricePanel;
	private PartPricingContextEnum partPricingContext;
	private Part part;
	private CentralRule rule;
	private PartRuleResult pricingInformation = null;
	private GenericPricingMethod pricingMethod;
	private String pricingMethodInfoPage;
	private Double selectedPrice = null;
	private boolean launchCalculationBtnClicked = false;

	/** Warnings messages */
	private List<String> messages = new ArrayList<String>();
	/** Custom Attributes for zone 1 */
	private List<PartCustomAttributeWrapper> customAttrArticleDisplayWrapperList = new ArrayList<PartCustomAttributeWrapper>();
	/** Zone 2 */
	private SegmentationComponentBean segmentationComponent;
	/** Zone 3 */
	private PartTypeComponentBean partTypeComponent;
	/** Zone 6 */
	private FeaturesConfigBean featuresConfig;

	/**
	 * Boolean flags for rendering conditionally popups
	 */
	private boolean openPriceValidationConfirmationPopup = false;
	private boolean openFeaturesUpdatedConfirmationPopup = false;
	private boolean validateAfterConfirmed = false;
	private boolean displayGoToValidatedRuleLink = false;
	private boolean openGoToValidatedRuleConfirmationPopup = false;
	private String comingFromContext;

	private Date applicationDate = new Date();

	@PostConstruct
	protected void load()
	{
		part = getPartFromRequest();
		if (part == null)
		{
			throw new RuntimeException("Cannot find the part in request.");
		}
		partPricingContext = getPartPricingContextFromRequest();
		if (partPricingContext == null)
		{
			throw new RuntimeException("Cannot find the context in request.");
		}

		segmentationComponent = new SegmentationComponentBean(this);
		segmentationComponent.init(part, false);

		partTypeComponent = new PartTypeComponentBean(this);
		partTypeComponent.init(part);

		/** For initializing values to be displayed */
		loadMethodAndPrice();

		/** For displaying ZONE 1 */
		initCustomAttributes();

		/** For displaying HISTORY table */
		partHistoryComponent.init(part);
	}

	protected void loadMethodAndPrice()
	{
		long subfamilyId = segmentationComponent.getSubFamilyMenu().getResultAssociatedWithSelectedItem() != null
		        ? segmentationComponent.getSubFamilyMenu().getResultAssociatedWithSelectedItem() : 0l;
		rule = centralRuleService.getConfigurationRegleBySubFamilyIdAndStatutAndFetchParameters(subfamilyId,
		        RegleStatutEnum.VALIDATE);
		if (rule != null)
		{
			pricingMethod = rule.getPricingMethod(PartTypeEnum.valueOf(partTypeComponent.getType()));
			setDisplayGoToValidatedRuleLink(true);
		} else
		{
			setDisplayGoToValidatedRuleLink(false);
			pricingMethod = null;
		}

		loadPricingMethod();
		pricePart();
	}

	private void loadPricingMethod()
	{
		pricingMethodInfoPage = findPricingMethodInfoPage();

		/**
		 * If pricingMethod is in {Range pricing, Perceived value with pivot,
		 * Perceived value w/o pivot} -> init FeatureConfig to display zone 6
		 */
		if (pricingMethod != null && pricingMethod instanceof PricingMethodWithFeatures)
		{

			if (pricingMethod instanceof RangePricingMethod)
			{
				featuresConfig = new RangePricingMethodFeaturesConfigBean();
			} else
			{
				featuresConfig = new FeaturesConfigBean();
			}
			featuresConfig.load((PricingMethodWithFeatures<?>) pricingMethod, part);
		} else
		{
			featuresConfig = null;
		}
	}

	private String findPricingMethodInfoPage()
	{
		String result = null;
		if (pricingMethod == null)
		{
			return result;
		}
		result = METHODS_FOLDER + pricingMethod.getPricingType().name().toLowerCase() + XHTML_SUFFIX;

		return result;
	}

	private void pricePart()
	{
		if (rule != null)
		{
			if (rule.getPricingMethod(part.getType()) == null)
			{
				Indicators indicators = indicatorService.computeMargins(
				        newHashSet(BEFORE_unitEstimator_RETAIL_PRICE, CURRENT_RETAIL_PRICE, FORMER_CURRENT_RETAIL_PRICE),
				        newArrayList(part), true);
				part.setIndicators(indicators);
			}
			pricingRuleService.computePricesAndIndicators(part, rule);
			pricingInformation = part.getPartRuleResult(rule);
			/**
			 * Must set RetailPrice only AFTER computePricesAndIndicators as
			 * THERE we PUT new PartRuleResults for NEW rule into the PART. And
			 * setSelectedRetailPrice() need the UPDATED PartRuleResults for NEW
			 * rules
			 */
			setSelectedRetailPrice();
		}
	}

	private void setSelectedRetailPrice()
	{
		if (part.getModifiedunitEstimatorPrice() != null)
		{
			setSelectedPrice(part.getModifiedunitEstimatorPrice().getAmount().doubleValue());
		} else
		{
			Double prix = null;
			if (pricingInformation != null)
			{
				Money prixPreconiseunitEstimator = pricingInformation.getPrixPreconiseunitEstimator();
				prix = (prixPreconiseunitEstimator == null) ? null : prixPreconiseunitEstimator.getAmount().doubleValue();
			}

			setSelectedPrice(prix);
		}

		/** Refresh selected retail price input textbox */
		if (selectedPricePanel != null)
		{
			selectedPricePanel.getChildren().clear();
		}
	}

	private PartPricingContextEnum getPartPricingContextFromRequest()
	{
		PartPricingContextEnum result = null;
		String contextAsString = getRequestParameter("context");

		if (!StringUtils.isEmpty(contextAsString))
		{
			result = PartPricingContextEnum.valueOf(contextAsString);
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

	/**
	 * For drop downs in Nomenclature: Called when Fam/Subfam/PartTypeChanged
	 */
	@Override
	public void onSelectedItemChange()
	{
		/** Set new part type */
		part.setType(PartTypeEnum.valueOf(partTypeComponent.getType()));

		if (rule != null)
		{
			if (rule.getPricingMethod(part.getType()) == null)
			{
				final PartRuleResult partRuleResult = part.getPartRuleResult(rule);
				if (partRuleResult != null)
				{
					partRuleResult.resetAll();
				}
			}
		}

		/** Update subfamily and part rule result */
		if (segmentationComponent.isSegmentationChanged()
		        && segmentationComponent.getSubFamilyMenu().getResultAssociatedWithSelectedItem() != 0)
		{
			SubFamily sousFamille = subFamilleService
			        .get(segmentationComponent.getSubFamilyMenu().getResultAssociatedWithSelectedItem());
			part.setSubFamily(sousFamille);
		}

		/** Load the new Rule and pricing method & re-calculate price */
		loadMethodAndPrice();

		JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "resizeLeftPanelDetailsSheet();");
	}

	/**
	 * Method Listener of the secondary feature selectOneMenu
	 * 
	 */
	public void secondaryFeatureValueChange(ValueChangeEvent vce)
	{
		if (launchCalculationBtnClicked)
		{
			// Initialize launchCalculationBtnClicked to activate the Validate
			// featuresConfig
			launchCalculationBtnClicked = false;
		}
	}

	/** For Launch Calculation button */
	public void launchCalculation(ActionEvent ae)
	{

		messages.clear();

		/** Update part if featureConfig changes */
		featuresConfig.writePartSelections(part);

		/** Recompute prices in pricing table */
		pricePart();

		setLaunchCalculationBtnClicked(true);
	}

	// Flag to decide if ZONE 8 would be displayed
	public boolean isPriceOriginDisplayed()
	{
		return (pricingMethod != null && pricingMethod instanceof ILaunchablePricingMethod
		        && pricingInformation != null) ? true : false;
	}

	// Return T if !empty pricePart.featuresConfig and
	// pricePart.featuresConfig.wasModified()
	public boolean isFeaturesConfigModified()
	{
		return (featuresConfig != null && featuresConfig.wasModified());
	}


	// 1. CREATE PRICE = submitPrice(ActionEvent ae) OR
	// confirmPriceValidationInTarifyReference()

	// 1A. CREATE PRICE CASE 1
	public String submitPrice()
	{
		if (validateInput("unitEstimator_tarification_price_not_updated_to_submit"))
		{
			// Fix mantis 5212
			boolean isForcedPrice = (pricingInformation == null && selectedPrice != null) || (pricingInformation != null
			        && selectedPrice != null && !selectedPrice.equals(pricingInformation.getPrixPreconiseunitEstimator()));

			partService.submitPrice(part, selectedPrice, segmentationComponent.getInitialSubFamilyId(), isForcedPrice);

			hisorisePriceIfSegmentationHasChanged();

			return goBack(false);
		} else
		{
			return "";
		}
	}
	// 2. MODIFY PRIX

	// 2A. MODIFY PRICE CASE 1
	public String modifyPrice()
	{
		if (validateInput("unitEstimator_tarification_price_not_updated_to_modify"))
		{
			partService.submitPrice(part, selectedPrice, segmentationComponent.getInitialSubFamilyId(), true);

			hisorisePriceIfSegmentationHasChanged();

			return goBack(false);
		} else
		{
			return "";
		}
	}

	// 2B. MODIFY PRICE CASE 2
	public void openFeaturesUpdatedConfirmationPopup(ActionEvent ae)
	{
		applicationDate = new Date();
		String action = ae.getComponent().getAttributes().get("actionAfterConfirm").toString();
		setValidateAfterConfirmed(WorkflowStateEnum.VALIDATE.equals(WorkflowStateEnum.valueOf(action)));
		setOpenFeaturesUpdatedConfirmationPopup(validateInput("unitEstimator_tarification_price_not_updated_to_validate"));
	}

	// 3. VALIDATE MODIFIED PRICE:

	// 3A. VALIDATE MODIFIED PRICE CASE 1 = confirmPriceValidation(ActionEvent
	// ae)
	// 3B. VALIDATE MODIFIED PRICE CASE 2 =
	// openFeaturesUpdatedConfirmationPopup(ActionEvent ae)


	// 4. VALIDATE PRICE

	// 4A. VALIDATE PRICE CASE1
	public String validatePrice()
	{
		if (applicationDate == null)
		{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
			        LanguageController.get("unitEstimator_price_no_application_date_message"), null));
			return "";
		}

		if (validateInput("unitEstimator_tarification_price_not_updated_to_validate"))
		{
			if (rule != null)
			{
				part.setApplicationDate(applicationDate);
				partService.validatePrice(part, selectedPrice, segmentationComponent.isSegmentationChanged(),
				        pricingInformation);
			}

			setOpenPriceValidationConfirmationPopup(false);

			historisePrice();
			boolean shouldUpdateSelectedIds = true;
			return goBack(shouldUpdateSelectedIds);
		} else
		{
			return "";
		}
	}

	// 4B. VALIDATE PRICE CASE 2 = confirmPriceValidation(ActionEvent ae)
	public void confirmPriceValidation(ActionEvent ae)
	{
		applicationDate = new Date();
		boolean showConfirmValidationPopup = this.validateInput("unitEstimator_tarification_price_not_updated_to_validate");
		setOpenPriceValidationConfirmationPopup(showConfirmValidationPopup);
	}

	private void hisorisePriceIfSegmentationHasChanged()
	{
		if (segmentationHasChanged())
		{
			historisePrice();

			propagator.publishEvent(new Event<SegmentationChangeEventEntity>(
			        unitEstimatorEventTypeRegistry.SEGEMENTATION_CHANGE, new SegmentationChangeEventEntity(part,
			                segmentationComponent.getInitialSubFamilyId(), part.getSubFamily().getId())));

			propagator.notifySubscribers();
		}
	}

	private void historisePrice()
	{
		PartPriceUpdateOriginEnum origin = null;
		if (segmentationHasChanged())
		{
			origin = PartPriceUpdateOriginEnum.SEGMENTATION;
		} else
		{
			origin = PartPriceUpdateOriginEnum.PRICE_VALIDATION;
		}

		partHistoryService.historiseCentralPartPrices(origin, Lists.newArrayList(part), false);
	}

	private boolean segmentationHasChanged()
	{
		return part.getSubFamily().getId().compareTo(segmentationComponent.getInitialSubFamilyId()) != 0;
	}

	/** 5. For Consult Rule button */
	public void consultRule(ActionEvent ae)
	{
		setOpenGoToValidatedRuleConfirmationPopup(true);

		JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "setPopupMarginTopTo33();");
	}

	/** 6. For Back to Catalog button */
	public String backToCatalog()
	{
		return goBack(false);
	}

	/** 7. For Go to validated Rule button */
	public void goToValidatedRule(ActionEvent ae)
	{
		setOpenGoToValidatedRuleConfirmationPopup(false);
		CentralRule ruleConfiguration = centralRuleService
		        .getConfigurationRegleBySubFamilyIdAndStatutAndFetchParameters(part.getSubFamily().getId(),
		                RegleStatutEnum.VALIDATE);
		if (ruleConfiguration != null)
		{
			redirectToPage(GO_TO_VALIDATED_RULE_PAGE + "?viewOrigin=PRICE_PART&partId=" + part.getId()
			        + "&partPricingContext=" + partPricingContext.toString() + "&ruleContext=CHECK&ruleId="
			        + ruleConfiguration.getId().toString());
		}
	}

	/**
	 * Event handlers for priceValicationConfirmation popup
	 */
	// Called when the user clicks on "No" on the price validation confirmation
	// popup or when he/she closes the
	// confirmation popup
	public void closePriceValidationConfirmationPopup(ActionEvent ae)
	{
		setOpenPriceValidationConfirmationPopup(false);
	}

	/**
	 * Event handlers for featuresUpdatedConfirmation.xhtml popup
	 */
	public void closeFeaturesUpdatedConfirmationPopup(ActionEvent ae)
	{
		setOpenFeaturesUpdatedConfirmationPopup(false);
	}

	/**
	 * Event handlers for goToValidatedRuleConfirmation.xhtml popup
	 */
	public void openGoToValidatedRuleConfirmationPopup(ActionEvent ae)
	{
		setOpenGoToValidatedRuleConfirmationPopup(true);
	}

	public void closeGoToValidatedRuleConfirmationPopup(ActionEvent ae)
	{
		setOpenGoToValidatedRuleConfirmationPopup(false);
	}

	// Private functions

	private boolean validateInput(String errorMessage)
	{
		messages.clear();

		/** Validate featuresConfig */
		if (featuresConfig != null && !launchCalculationBtnClicked)
		{
			/**
			 * Read features selection -> part to detect if selected features
			 * change
			 */
			featuresConfig.writePartSelections(part);
			if (featuresConfig.wasModified())
			{
				messages.add(LanguageController.get(errorMessage));
				return false;
			}
		}

		/** Validate selected retail price */
		if (isInvalidPrixRetenu())
		{
			messages.add(LanguageController.get("unitEstimator_tarification_price_not_set"));
			return false;
		}

		if (isDifferentSetAndRecommendedPrice())
		{
			/** Validate comment */
			if (isEmptyComment())
			{
				messages.add(LanguageController.get("unitEstimator_tarification_comment_empty"));
				return false;
			}

			/** Validate selected price */
			if (selectedPrice <= 0)
			{
				messages.add(LanguageController.get("unitEstimator_rules_price_not_superior_to_0"));
				return false;
			}
		}

		return true;
	}

	private String goBack(boolean shouldUpdateSelectedIds)
	{
		String rs = "";

		/**
		 * Refresh list of PN to be displayed by
		 * getDynamicTableArticle().clearCache()
		 */
		switch (partPricingContext)
		{
			case CREATION:
				rs = LIST_REFERENCES_NON_TARIFEE;
				final PriceCreationBean listOfNoPricedParts = BeanLocator.locateBean(LIST_REFERENCES_NON_TARIFEE);
				listOfNoPricedParts.getDynamicTableArticle().setRowIndex(0);
				listOfNoPricedParts.getDynamicTableArticle().clearCache();
				listOfNoPricedParts.getDynamicTableArticle().getWrappedData();
				break;
			case MODIFICATION:
				rs = LIST_REFERENCES_FOR_MODIF_PRIX;
				final PriceUpdateBean listOfNoModifiedPriceParts = BeanLocator
				        .locateBean(LIST_REFERENCES_FOR_MODIF_PRIX);
				listOfNoModifiedPriceParts.getDynamicTableArticle().setRowIndex(0);
				listOfNoModifiedPriceParts.getDynamicTableArticle().clearCache();
				listOfNoModifiedPriceParts.getDynamicTableArticle().getWrappedData();
				break;
			case VALIDATION:
				rs = LIST_REFERENCES_NON_VALIDEES;
				final PriceValidationBean listOfNoValidatedPriceParts = BeanLocator
				        .locateBean(LIST_REFERENCES_NON_VALIDEES);
				listOfNoValidatedPriceParts.getDynamicTableArticle().setRowIndex(0);
				listOfNoValidatedPriceParts.getDynamicTableArticle().clearCache();
				listOfNoValidatedPriceParts.getDynamicTableArticle().getWrappedData();
				if (listOfNoValidatedPriceParts.getDynamicTableArticle().getSelectedId().contains(part.getId())
				        && shouldUpdateSelectedIds)
				{
					listOfNoValidatedPriceParts.getDynamicTableArticle().getSelectedId().remove(part.getId());
				}

				break;
			default:
				LOGGER.info("Where are you going?");
				break;
		}
		return rs;
	}

	private void redirectToPage(String destination)
	{
		String contextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
		try
		{
			FacesContext.getCurrentInstance().getExternalContext().redirect(contextPath + destination);
		} catch (IOException e)
		{
			LOGGER.error(e);
		}
	}

	private boolean isDifferentSetAndRecommendedPrice()
	{
		return ((selectedPrice != null && pricingInformation != null
		        && pricingInformation.getPrixPreconiseunitEstimator() != null
		        && Money.isDifferentThan(pricingInformation.getPrixPreconiseunitEstimator(), new Money(selectedPrice)))
		        || (selectedPrice != null && pricingInformation != null
		                && pricingInformation.getPrixPreconiseunitEstimator() == null)
		        || (selectedPrice != null && pricingInformation == null));
	}

	private boolean isEmptyComment()
	{
		return (part.getComment() == null || part.getComment().isEmpty());
	}

	private boolean isInvalidPrixRetenu()
	{
		return (selectedPrice == null || selectedPrice.equals(Double.valueOf("0"))
		        || selectedPrice.toString().trim().equals(StringUtil.BLANK));
	}

	/** For ZONE 1 */
	private void initCustomAttributes()
	{
		customAttrArticleDisplayWrapperList = customAttributeServiceFacade
		        .produceCustomAttributeList(SessionUtil.getCatalogueId(FacesContext.getCurrentInstance()), part);
	}


	public Part getPart()
	{
		return part;
	}

	public SegmentationComponentBean getSegmentationComponent()
	{
		return segmentationComponent;
	}

	public PartTypeComponentBean getPartTypeComponent()
	{
		return partTypeComponent;
	}

	public String getPricingMethodInfoPage()
	{
		return pricingMethodInfoPage;
	}

	public void setPricingMethodInfoPage(String pricingMethodInfoPage)
	{
		this.pricingMethodInfoPage = pricingMethodInfoPage;
	}

	public FeaturesConfigBean getFeaturesConfig()
	{
		return featuresConfig;
	}

	public Double getSelectedPrice()
	{
		return selectedPrice;
	}

	public void setSelectedPrice(Double selectedPrice)
	{
		this.selectedPrice = selectedPrice;
	}

	public GenericPricingMethod getPricingMethod()
	{
		return pricingMethod;
	}

	public PartPricingContextEnum getPartPricingContext()
	{
		return partPricingContext;
	}

	public CentralRule getRule()
	{
		return rule;
	}

	public void setMessages(List<String> messages)
	{
		this.messages = messages;
	}

	public List<String> getMessages()
	{
		return messages;
	}

	public List<PartCustomAttributeWrapper> getCustomAttrArticleDisplayWrapperList()
	{
		return customAttrArticleDisplayWrapperList;
	}

	public boolean isOpenPriceValidationConfirmationPopup()
	{
		return openPriceValidationConfirmationPopup;
	}

	public void setOpenPriceValidationConfirmationPopup(boolean openPriceValidationConfirmationPopup)
	{
		this.openPriceValidationConfirmationPopup = openPriceValidationConfirmationPopup;
	}

	public boolean isOpenFeaturesUpdatedConfirmationPopup()
	{
		return openFeaturesUpdatedConfirmationPopup;
	}

	public void setOpenFeaturesUpdatedConfirmationPopup(boolean openFeaturesUpdatedConfirmationPopup)
	{
		this.openFeaturesUpdatedConfirmationPopup = openFeaturesUpdatedConfirmationPopup;
	}

	public void setValidateAfterConfirmed(boolean validateAfterConfirmed)
	{
		this.validateAfterConfirmed = validateAfterConfirmed;
	}

	public boolean isValidateAfterConfirmed()
	{
		return validateAfterConfirmed;
	}

	public boolean isDisplayGoToValidatedRuleLink()
	{
		return displayGoToValidatedRuleLink;
	}

	public void setDisplayGoToValidatedRuleLink(boolean displayGoToValidatedRuleLink)
	{
		this.displayGoToValidatedRuleLink = displayGoToValidatedRuleLink;
	}

	public boolean isOpenGoToValidatedRuleConfirmationPopup()
	{
		return openGoToValidatedRuleConfirmationPopup;
	}

	public void setOpenGoToValidatedRuleConfirmationPopup(boolean openGoToValidatedRuleConfirmationPopup)
	{
		this.openGoToValidatedRuleConfirmationPopup = openGoToValidatedRuleConfirmationPopup;
	}

	public String getComingFromContext()
	{
		return comingFromContext;
	}

	public void setComingFromContext(String comingFromContext)
	{
		this.comingFromContext = comingFromContext;
	}

	public PartRuleResult getPricingInformation()
	{
		return pricingInformation;
	}

	public UIComponent getSelectedPricePanel()
	{
		return selectedPricePanel;
	}

	public void setSelectedPricePanel(UIComponent selectedPricePanel)
	{
		this.selectedPricePanel = selectedPricePanel;
	}

	private void setLaunchCalculationBtnClicked(boolean launchCalculationBtnClicked)
	{
		this.launchCalculationBtnClicked = launchCalculationBtnClicked;
	}

	public Date getApplicationDate()
	{
		return applicationDate;
	}

	public void setApplicationDate(Date applicationDate)
	{
		this.applicationDate = applicationDate;
	}

	public PartHistoryComponent getPartHistoryComponent()
	{
		return partHistoryComponent;
	}

	public void setPartHistoryComponent(PartHistoryComponent partHistoryComponent)
	{
		this.partHistoryComponent = partHistoryComponent;
	}
}
