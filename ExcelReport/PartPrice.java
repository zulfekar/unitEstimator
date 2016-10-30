package fr.unitEstimator.central.catalog;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpServletRequest;

import fr.unitEstimator.service.competitive.exports.impl.EvolutionEnum;
import com.icesoft.faces.context.effects.JavascriptContext;
import fr.unitEstimator.util.Money;

public abstract class PartPrice implements Serializable
{

	private static final long serialVersionUID = 8018039486331291642L;

	private static final String HISTORY_TOGGLER_OFF_ICON_URL = "/themes/unitEstimator-theme-theme/images/unitEstimator_theme/part_history_toggler_off_icon.png";
	private static final String HISTORY_TOGGLER_ON_ICON_URL = "/themes/unitEstimator-theme-theme/images/unitEstimator_theme/part_history_toggler_on_icon.png";
	private static final String HISTORY_TOGGLER_OFF_CSS_CLASS_NAME = "expandIcon";
	private static final String HISTORY_TOGGLER_ON_CSS_CLASS_NAME = "collapseIcon";

	private PartPriceHistoryTogglerListener partPriceHistoryTogglerListener;
	private PartPriceEnum type;

	private Money priceBeforeunitEstimator;
	private Money formerCurrrentPrice;
	private Money currentRetailPrice;
	private Money formerRetailPrice;
	private Money validatedunitEstimatorPrice;
	private Money modifiedunitEstimatorPrice;
	private Money temporaryunitEstimatorPrice;
	private Money recommendedPrice;
	private Money targetPrice;
	private Money changeInProgressPrice;
	private Money purchaseCost;

	private EvolutionEnum priceBeforeunitEstimatorEvolution;
	private EvolutionEnum formerCurrrentPriceEvolution;
	private EvolutionEnum currentRetailPriceEvolution;
	private EvolutionEnum targetPriceEvolution;
	private EvolutionEnum recommendedPriceEvolution;
	private EvolutionEnum validatedunitEstimatorPriceEvolution;
	private EvolutionEnum purchaseCostEvolution;
	private EvolutionEnum quantityEvolution;
	private EvolutionEnum discountEvolution;

	private boolean waitingValidation;

	/** indicators */
	private BigDecimal marginBeforeunitEstimatorRetailPrice = BigDecimal.ZERO;
	private BigDecimal marginFormerCurrentRetailPrice = BigDecimal.ZERO;
	private BigDecimal marginCurrentRetailPrice = BigDecimal.ZERO;
	private BigDecimal marginRecommendedPrice = BigDecimal.ZERO;
	private BigDecimal marginTargetPrice = BigDecimal.ZERO;
	private BigDecimal marginFinalPrice = BigDecimal.ZERO;
	private BigDecimal marginChangeInProgressPrice = BigDecimal.ZERO;;

	private Integer cti;
	private String lifecycle;
	private Boolean implementationProgressive;
	private Boolean AlerteAchat;
	private String changeComment;
	private Integer quantity1;
	private BigDecimal networkDiscount;
	private String Status;

	private List<PartPrice> history;
	private boolean showHistory = false;

	public abstract String getColCSSStyleClass();

	public abstract String getTextCSSStyleClass();

	public String getHistoryTogglerIconUrl()
	{
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
		        .getRequest();
		if (showHistory)
			return request.getContextPath() + HISTORY_TOGGLER_ON_ICON_URL;
		return request.getContextPath() + HISTORY_TOGGLER_OFF_ICON_URL;
	}

	public String getHistoryTogglerCssClassName()
	{
		if (showHistory)
			return HISTORY_TOGGLER_ON_CSS_CLASS_NAME;
		return HISTORY_TOGGLER_OFF_CSS_CLASS_NAME;
	}

	public Money getPriceBeforeunitEstimator()
	{
		return priceBeforeunitEstimator;
	}

	public void setPriceBeforeunitEstimator(Money priceBeforeunitEstimator)
	{
		this.priceBeforeunitEstimator = priceBeforeunitEstimator;
	}

	public Money getFormerCurrrentPrice()
	{
		return formerCurrrentPrice;
	}

	public void setFormerCurrrentPrice(Money formerCurrrentPrice)
	{
		this.formerCurrrentPrice = formerCurrrentPrice;
	}

	public Money getCurrentRetailPrice()
	{
		return currentRetailPrice;
	}

	public void setCurrentRetailPrice(Money currentRetailPrice)
	{
		this.currentRetailPrice = currentRetailPrice;
	}

	public Money getFormerRetailPrice()
	{
		return formerRetailPrice;
	}

	public void setFormerRetailPrice(Money formerRetailPrice)
	{
		this.formerRetailPrice = formerRetailPrice;
	}

	public Money getValidatedunitEstimatorPrice()
	{
		return validatedunitEstimatorPrice;
	}

	public void setValidatedunitEstimatorPrice(Money validatedunitEstimatorPrice)
	{
		this.validatedunitEstimatorPrice = validatedunitEstimatorPrice;
	}

	public Money getModifiedunitEstimatorPrice()
	{
		return modifiedunitEstimatorPrice;
	}

	public void setModifiedunitEstimatorPrice(Money modifiedunitEstimatorPrice)
	{
		this.modifiedunitEstimatorPrice = modifiedunitEstimatorPrice;
	}

	public Money getTemporaryunitEstimatorPrice()
	{
		return temporaryunitEstimatorPrice;
	}

	public void setTemporaryunitEstimatorPrice(Money temporaryunitEstimatorPrice)
	{
		this.temporaryunitEstimatorPrice = temporaryunitEstimatorPrice;
	}

	public Money getRecommendedPrice()
	{
		return recommendedPrice;
	}

	public void setRecommendedPrice(Money recommendedPrice)
	{
		this.recommendedPrice = recommendedPrice;
	}

	public boolean isWaitingValidation()
	{
		return waitingValidation;
	}

	public void setWaitingValidation(boolean waitingValidation)
	{
		this.waitingValidation = waitingValidation;
	}

	public void setTargetPrice(Money targetPrice)
	{
		this.targetPrice = targetPrice;
	}

	public Money getTargetPrice()
	{
		return targetPrice;
	}

	public void setType(PartPriceEnum type)
	{
		this.type = type;
	}

	public PartPriceEnum getType()
	{
		return type;
	}

	public void setCti(Integer cti)
	{
		this.cti = cti;
	}

	public Integer getCti()
	{
		return cti;
	}

	public void setLifecycle(String lifecycle)
	{
		this.lifecycle = lifecycle;
	}

	public String getLifecycle()
	{
		return lifecycle;
	}

	public void setChangeInProgressPrice(Money changeInProgressPrice)
	{
		this.changeInProgressPrice = changeInProgressPrice;
	}

	public Money getChangeInProgressPrice()
	{
		return changeInProgressPrice;
	}

	public BigDecimal getMarginBeforeunitEstimatorRetailPrice()
	{
		return marginBeforeunitEstimatorRetailPrice;
	}

	public void setMarginBeforeunitEstimatorRetailPrice(BigDecimal marginBeforeunitEstimatorRetailPrice)
	{
		this.marginBeforeunitEstimatorRetailPrice = marginBeforeunitEstimatorRetailPrice;
	}

	public BigDecimal getMarginFormerCurrentRetailPrice()
	{
		return marginFormerCurrentRetailPrice;
	}

	public void setMarginFormerCurrentRetailPrice(BigDecimal marginFormerCurrentRetailPrice)
	{
		this.marginFormerCurrentRetailPrice = marginFormerCurrentRetailPrice;
	}

	public BigDecimal getMarginCurrentRetailPrice()
	{
		return marginCurrentRetailPrice;
	}

	public void setMarginCurrentRetailPrice(BigDecimal marginCurrentRetailPrice)
	{
		this.marginCurrentRetailPrice = marginCurrentRetailPrice;
	}

	public BigDecimal getMarginRecommendedPrice()
	{
		return marginRecommendedPrice;
	}

	public void setMarginRecommendedPrice(BigDecimal marginRecommendedPrice)
	{
		this.marginRecommendedPrice = marginRecommendedPrice;
	}

	public BigDecimal getMarginTargetPrice()
	{
		return marginTargetPrice;
	}

	public void setMarginTargetPrice(BigDecimal marginTargetPrice)
	{
		this.marginTargetPrice = marginTargetPrice;
	}

	public BigDecimal getMarginFinalPrice()
	{
		return marginFinalPrice;
	}

	public void setMarginFinalPrice(BigDecimal marginFinalPrice)
	{
		this.marginFinalPrice = marginFinalPrice;
	}

	public BigDecimal getMarginChangeInProgressPrice()
	{
		return marginChangeInProgressPrice;
	}

	public void setMarginChangeInProgressPrice(BigDecimal marginChangeInProgressPrice)
	{
		this.marginChangeInProgressPrice = marginChangeInProgressPrice;
	}

	public void setImplementationProgressive(Boolean implementationProgressive)
	{
		this.implementationProgressive = implementationProgressive;
	}

	public Boolean getImplementationProgressive()
	{
		return implementationProgressive;
	}

	public void setAlerteAchat(Boolean alerteAchat)
	{
		AlerteAchat = alerteAchat;
	}

	public Boolean getAlerteAchat()
	{
		return AlerteAchat;
	}

	public List<PartPrice> getHistory()
	{
		return history;
	}

	public void setHistory(List<PartPrice> history)
	{
		this.history = history;
	}

	public boolean isShowHistory()
	{
		return showHistory;
	}

	public void setShowHistory(boolean expanded)
	{
		this.showHistory = expanded;
	}

	public void toggleHistory(ActionEvent event)
	{
		partPriceHistoryTogglerListener.apply(this);
	}

	public void setPartPriceHistoryTogglerListener(PartPriceHistoryTogglerListener partPriceHistoryTogglerListener)
	{
		this.partPriceHistoryTogglerListener = partPriceHistoryTogglerListener;
	}

	public Money getPurchaseCost()
	{
		return purchaseCost;
	}

	public void setPurchaseCost(Money purchaseCost)
	{
		this.purchaseCost = purchaseCost;
	}

	public Integer getQuantity1()
	{
		return quantity1;
	}

	public void setQuantity1(Integer quantity)
	{
		this.quantity1 = quantity;
	}

	public String getChangeComment()
	{
		return changeComment;
	}

	public void setChangeComment(String changeComment)
	{
		this.changeComment = changeComment;
	}

	public void setNetworkDiscount(BigDecimal networkDiscount)
	{
		this.networkDiscount = networkDiscount;
	}

	public BigDecimal getNetworkDiscount()
	{
		return networkDiscount;
	}

	public void setStatus(String status)
	{
		Status = status;
	}

	public String getStatus()
	{
		return Status;
	}

	public EvolutionEnum getPriceBeforeunitEstimatorEvolution()
	{
		return priceBeforeunitEstimatorEvolution;
	}

	public void setPriceBeforeunitEstimatorEvolution(EvolutionEnum priceBeforeunitEstimatorEvolution)
	{
		this.priceBeforeunitEstimatorEvolution = priceBeforeunitEstimatorEvolution;
	}

	public EvolutionEnum getFormerCurrrentPriceEvolution()
	{
		return formerCurrrentPriceEvolution;
	}

	public void setFormerCurrrentPriceEvolution(EvolutionEnum formerCurrrentPriceEvolution)
	{
		this.formerCurrrentPriceEvolution = formerCurrrentPriceEvolution;
	}

	public EvolutionEnum getCurrentRetailPriceEvolution()
	{
		return currentRetailPriceEvolution;
	}

	public void setCurrentRetailPriceEvolution(EvolutionEnum currentRetailPriceEvolution)
	{
		this.currentRetailPriceEvolution = currentRetailPriceEvolution;
	}

	public EvolutionEnum getTargetPriceEvolution()
	{
		return targetPriceEvolution;
	}

	public void setTargetPriceEvolution(EvolutionEnum targetPriceEvolution)
	{
		this.targetPriceEvolution = targetPriceEvolution;
	}

	public EvolutionEnum getRecommendedPriceEvolution()
	{
		return recommendedPriceEvolution;
	}

	public void setRecommendedPriceEvolution(EvolutionEnum recommendedPriceEvolution)
	{
		this.recommendedPriceEvolution = recommendedPriceEvolution;
	}

	public EvolutionEnum getValidatedunitEstimatorPriceEvolution()
	{
		return validatedunitEstimatorPriceEvolution;
	}

	public void setValidatedunitEstimatorPriceEvolution(EvolutionEnum validatedunitEstimatorPriceEvolution)
	{
		this.validatedunitEstimatorPriceEvolution = validatedunitEstimatorPriceEvolution;
	}

	public EvolutionEnum getPurchaseCostEvolution()
	{
		return purchaseCostEvolution;
	}

	public void setPurchaseCostEvolution(EvolutionEnum purchaseCostEvolution)
	{
		this.purchaseCostEvolution = purchaseCostEvolution;
	}

	public EvolutionEnum getQuantityEvolution()
	{
		return quantityEvolution;
	}

	public void setQuantityEvolution(EvolutionEnum quantityEvolution)
	{
		this.quantityEvolution = quantityEvolution;
	}

	public EvolutionEnum getDiscountEvolution()
	{
		return discountEvolution;
	}

	public void setDiscountEvolution(EvolutionEnum discountEvolution)
	{
		this.discountEvolution = discountEvolution;
	}

	@Override
	public String toString()
	{
		return "PartPrice [priceBeforeunitEstimatorEvolution=" + priceBeforeunitEstimatorEvolution
		        + ", formerCurrrentPriceEvolution=" + formerCurrrentPriceEvolution + ", currentRetailPriceEvolution="
		        + currentRetailPriceEvolution + ", targetPriceEvolution=" + targetPriceEvolution
		        + ", recommendedPriceEvolution=" + recommendedPriceEvolution + ", validatedunitEstimatorPriceEvolution="
		        + validatedunitEstimatorPriceEvolution + ", purchaseCostEvolution=" + purchaseCostEvolution
		        + ", quantityEvolution=" + quantityEvolution + ", discountEvolution=" + discountEvolution + "]";
	}

}
