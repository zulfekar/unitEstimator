package fr.unitEstimator.service.competitive.exports.impl;

import java.math.BigDecimal;

import fr.unitEstimator.util.Money;
import fr.unitEstimator.util.StringUtil;

public class HistoryUpdateDTO
{

	private Money previousPriceBeforeunitEstimator;
	private Money actualPriceBeforeunitEstimator;
	private String actualID;
	private String previousID;
	private Money previousValidatedPrice;
	private Money actualValidatedPrice;
	private Money previousFormerRetailPrice;
	private Money actualFormerRetailPrice;
	private Money previousCurrentRetailPrice;
	private Money actualCurrentRetailPrice;
	private Money previousTargetPrice;
	private Money actualTargetPrice;
	private Money previousRecommendedPrice;
	private Money actualRecommendedPrice;
	private Money previousPurchaseCost;
	private Money actualPurchaseCost;
	private Integer previousQuantity;
	private Integer actualQuantity;
	private BigDecimal previousDiscount;
	private BigDecimal actualDiscount;
	private BigDecimal previousMass;
	private BigDecimal actualMass;
	private BigDecimal previousLength;
	private BigDecimal actualLength;
	private BigDecimal previousWidth;
	private BigDecimal actualWidth;
	private BigDecimal previousHeight;
	private BigDecimal actualHeight;
	private BigDecimal previousSurface;
	private BigDecimal actualSurface;
	private BigDecimal previousVolume;
	private BigDecimal actualVolume;

	private String nextID;
	private Money nextPriceBeforeunitEstimator;
	private Money nextFormerRetailPrice;
	private Money nextCurrentRetailPrice;
	private Money nextTargetPrice;
	private Money nextRecommendedPrice;
	private Money nextPurchaseCost;
	private Integer nextQuantity;
	private BigDecimal nextDiscount;
	private BigDecimal nextMass;
	private BigDecimal nextWidth;
	private BigDecimal nextLength;
	private BigDecimal nextHeight;
	private BigDecimal nextSurface;
	private BigDecimal nextVolume;
	private Money nextValidatedPrice;

	private static final String EMPTY_STRING = StringUtil.BLANK;

	public void increment()
	{
		previousID = actualID;
		previousPriceBeforeunitEstimator = actualPriceBeforeunitEstimator;
		previousFormerRetailPrice = actualFormerRetailPrice;
		previousCurrentRetailPrice = actualCurrentRetailPrice;
		previousTargetPrice = actualTargetPrice;
		previousRecommendedPrice = actualRecommendedPrice;
		previousValidatedPrice = actualValidatedPrice;
		previousPurchaseCost = actualPurchaseCost;
		previousQuantity = actualQuantity;
		previousDiscount = actualDiscount;
		previousMass = actualMass;
		previousLength = actualLength;
		previousWidth = actualWidth;
		previousHeight = actualHeight;
		previousSurface = actualSurface;
		previousVolume = actualVolume;

		actualID = null;
		actualPriceBeforeunitEstimator = null;
		actualFormerRetailPrice = null;
		actualCurrentRetailPrice = null;
		actualTargetPrice = null;
		actualRecommendedPrice = null;
		actualValidatedPrice = null;
		actualPurchaseCost = null;
		actualQuantity = null;
		actualDiscount = null;
		actualMass = null;
		actualLength = null;
		actualWidth = null;
		actualHeight = null;
		actualSurface = null;
		actualVolume = null;

		nextID = null;
		nextPriceBeforeunitEstimator = null;
		nextFormerRetailPrice = null;
		nextCurrentRetailPrice = null;
		nextTargetPrice = null;
		nextRecommendedPrice = null;
		nextValidatedPrice = null;
		nextPurchaseCost = null;
		nextQuantity = null;
		nextDiscount = null;
		nextMass = null;
		nextLength = null;
		nextWidth = null;
		nextHeight = null;
		nextSurface = null;
		nextVolume = null;

	}

	public EvolutionEnum getPriceBeforeunitEstimatorEvolution()
	{
		return getPriceEvolution(previousPriceBeforeunitEstimator, actualPriceBeforeunitEstimator);
	}

	public EvolutionEnum getFormerRetailPriceEvolution()
	{
		return getPriceEvolution(previousFormerRetailPrice, actualFormerRetailPrice);
	}

	public EvolutionEnum getCurrentRetailPriceEvolution()
	{
		return getPriceEvolution(previousCurrentRetailPrice, actualCurrentRetailPrice);
	}

	public EvolutionEnum getTargetPriceEvolution()
	{
		return getPriceEvolution(previousTargetPrice, actualTargetPrice);
	}

	public EvolutionEnum getRecommendedPriceEvolution()
	{
		return getPriceEvolution(previousRecommendedPrice, actualRecommendedPrice);
	}

	public EvolutionEnum getValidatedPriceEvolution()
	{
		return getPriceEvolution(previousValidatedPrice, actualValidatedPrice);
	}

	public EvolutionEnum getPurchaseCostEvolution()
	{
		return getPriceEvolution(previousPurchaseCost, actualPurchaseCost);
	}

	public EvolutionEnum getQuantityEvolution()
	{
		return getQuantityEvolution(previousQuantity, actualQuantity);
	}

	public EvolutionEnum getDiscountEvolution()
	{
		return getDiscountEvolution(previousDiscount, actualDiscount);
	}

	private EvolutionEnum getDiscountEvolution(BigDecimal previousDiscount, BigDecimal actualDiscount)
	{

		if (previousDiscount == null && actualDiscount != null && actualDiscount.signum() > 0)
		{
			return EvolutionEnum.INCREASE;
		}

		if (previousDiscount != null && actualDiscount != null && actualDiscount.compareTo(previousDiscount) > 0)
		{
			return EvolutionEnum.INCREASE;
		}

		if (actualDiscount == null && previousDiscount != null && previousDiscount.signum() > 0)
		{
			return EvolutionEnum.DECREASE;
		}

		if (actualDiscount != null && previousDiscount != null && previousDiscount.compareTo(actualDiscount) > 0)
		{
			return EvolutionEnum.DECREASE;
		}

		return EvolutionEnum.UNCHANGED;
	}

	private EvolutionEnum getQuantityEvolution(Integer previousQuantity, Integer actualQuantity)
	{
		if (previousQuantity == null && actualQuantity != null && actualQuantity > 0)
		{
			return EvolutionEnum.INCREASE;
		}

		if (previousQuantity != null && actualQuantity != null && actualQuantity > (previousQuantity))
		{
			return EvolutionEnum.INCREASE;
		}

		if (actualQuantity == null && previousQuantity != null && previousQuantity > 0)
		{
			return EvolutionEnum.DECREASE;
		}

		if (actualQuantity != null && previousQuantity != null && previousQuantity > (actualQuantity))
		{
			return EvolutionEnum.DECREASE;
		}

		return EvolutionEnum.UNCHANGED;
	}

	private EvolutionEnum getPriceEvolution(Money priceBefore, Money priceAfter)
	{

		if (priceBefore == null && priceAfter != null && priceAfter.isPositive())
		{
			return EvolutionEnum.INCREASE;
		}

		if (priceBefore != null && priceAfter != null && priceAfter.isGreaterThan(priceBefore))
		{
			return EvolutionEnum.INCREASE;
		}

		if (priceAfter == null && priceBefore != null && priceBefore.isPositive())
		{
			return EvolutionEnum.DECREASE;
		}

		if (priceAfter != null && priceBefore != null && priceBefore.isGreaterThan(priceAfter))
		{
			return EvolutionEnum.DECREASE;
		}

		return EvolutionEnum.UNCHANGED;
	}

	public boolean isPriceBeforeunitEstimatorUpdated()
	{
		return isPriceUpdated(actualPriceBeforeunitEstimator, nextPriceBeforeunitEstimator);
	}

	private boolean isPriceUpdated(Money priceCurrent, Money priceAfter)
	{
		if (StringUtil.isEmptyValue(nextID) || !nextID.equals(actualID))
		{
			return false;
		}

		if (priceAfter == null && priceCurrent == null)
		{
			return false;
		}

		if (priceAfter == null && priceCurrent != null)
		{
			return true;
		}

		if (priceAfter != null && priceCurrent == null)
		{
			return true;
		}

		if (Money.isDifferentThan(priceCurrent, priceAfter))
		{
			return true;
		}
		return false;
	}

	public boolean isFormerRetailPriceUpdated()
	{

		return isPriceUpdated(actualFormerRetailPrice, nextFormerRetailPrice);
	}

	public boolean isValidatedPriceUpdated()
	{
		return isPriceUpdated(actualValidatedPrice, nextValidatedPrice);
	}

	public boolean isCurrentRetailPriceUpdated()
	{

		return isPriceUpdated(actualCurrentRetailPrice, nextCurrentRetailPrice);
	}

	public boolean isTargetPriceUpdated()
	{

		return isPriceUpdated(actualTargetPrice, nextTargetPrice);
	}

	public boolean isRecommendedPriceUpdated()
	{
		return isPriceUpdated(actualRecommendedPrice, nextRecommendedPrice);
	}

	public boolean isPurchaseCostUpdated()
	{
		return isPriceUpdated(actualPurchaseCost, nextPurchaseCost);
	}

	public boolean isQuantityUpdated()
	{
		return isQtyUpdated(actualQuantity, nextQuantity);
	}

	private boolean isQtyUpdated(Integer qtyCurrent, Integer qtyAfter)
	{
		if (StringUtil.isEmptyValue(nextID) || !nextID.equals(actualID))
		{
			return false;
		}

		if (qtyAfter == null && qtyCurrent == null)
		{
			return false;
		}

		if (qtyAfter == null && qtyCurrent != null)
		{
			return true;
		}

		if (qtyAfter != null && qtyCurrent == null)
		{
			return true;
		}

		if (!qtyCurrent.equals(qtyAfter))
		{
			return true;
		}

		return false;
	}

	private boolean isMeasurementUpdated(BigDecimal measurementCurrent, BigDecimal measurementAfter)
	{
		if (StringUtil.isEmptyValue(nextID) || !nextID.equals(actualID))
		{
			return false;
		}

		if (measurementAfter == null && measurementCurrent == null)
		{
			return false;
		}

		if (measurementAfter == null && measurementCurrent != null)
		{
			return true;
		}

		if (measurementAfter != null && measurementCurrent == null)
		{
			return true;
		}

		if (measurementCurrent.compareTo(measurementAfter) != 0)
		{
			return true;
		}
		return false;
	}

	public boolean isDiscountUpdated()
	{
		return isMeasurementUpdated(actualDiscount, nextDiscount);

	}

	public boolean isMassUpdated()
	{
		return isMeasurementUpdated(actualMass, nextMass);
	}

	public boolean isLengthUpdated()
	{
		return isMeasurementUpdated(actualLength, nextLength);
	}

	public boolean isWidthUpdated()
	{
		return isMeasurementUpdated(actualWidth, nextWidth);
	}

	public boolean isHeightUpdated()
	{

		return isMeasurementUpdated(actualHeight, nextHeight);
	}

	public boolean isSurfaceUpdated()
	{

		return isMeasurementUpdated(actualSurface, nextSurface);
	}

	public boolean isVolumeUpdated()
	{
		return isMeasurementUpdated(actualVolume, nextVolume);
	}

	public Money getPreviousPriceBeforeunitEstimator()
	{
		return previousPriceBeforeunitEstimator;
	}

	public void setPreviousPriceBeforeunitEstimator(Money previousPriceBeforeunitEstimator)
	{
		this.previousPriceBeforeunitEstimator = previousPriceBeforeunitEstimator;
	}

	public Money getActualPriceBeforeunitEstimator()
	{
		return actualPriceBeforeunitEstimator;
	}

	public void setActualPriceBeforeunitEstimator(Money actualPriceBeforeunitEstimator)
	{
		this.actualPriceBeforeunitEstimator = actualPriceBeforeunitEstimator;
	}

	public String getActualID()
	{
		return actualID;
	}

	public void setActualID(String actualID)
	{
		this.actualID = actualID;
	}

	public String getPreviousID()
	{
		return previousID;
	}

	public void setPreviousID(String previousID)
	{
		this.previousID = previousID;
	}

	public Money getPreviousValidatedPrice()
	{
		return previousValidatedPrice;
	}

	public void setPreviousValidatedPrice(Money previousValidatedPrice)
	{
		this.previousValidatedPrice = previousValidatedPrice;
	}

	public Money getActualValidatedPrice()
	{
		return actualValidatedPrice;
	}

	public void setActualValidatedPrice(Money actualValidatedPrice)
	{
		this.actualValidatedPrice = actualValidatedPrice;
	}

	public Money getPreviousFormerRetailPrice()
	{
		return previousFormerRetailPrice;
	}

	public void setPreviousFormerRetailPrice(Money previousFormerRetailPrice)
	{
		this.previousFormerRetailPrice = previousFormerRetailPrice;
	}

	public Money getActualFormerRetailPrice()
	{
		return actualFormerRetailPrice;
	}

	public void setActualFormerRetailPrice(Money actualFormerRetailPrice)
	{
		this.actualFormerRetailPrice = actualFormerRetailPrice;
	}

	public Money getPreviousCurrentRetailPrice()
	{
		return previousCurrentRetailPrice;
	}

	public void setPreviousCurrentRetailPrice(Money previousCurrentRetailPrice)
	{
		this.previousCurrentRetailPrice = previousCurrentRetailPrice;
	}

	public Money getActualCurrentRetailPrice()
	{
		return actualCurrentRetailPrice;
	}

	public void setActualCurrentRetailPrice(Money actualCurrentRetailPrice)
	{
		this.actualCurrentRetailPrice = actualCurrentRetailPrice;
	}

	public Money getPreviousTargetPrice()
	{
		return previousTargetPrice;
	}

	public void setPreviousTargetPrice(Money previousTargetPrice)
	{
		this.previousTargetPrice = previousTargetPrice;
	}

	public Money getActualTargetPrice()
	{
		return actualTargetPrice;
	}

	public void setActualTargetPrice(Money actualTargetPrice)
	{
		this.actualTargetPrice = actualTargetPrice;
	}

	public Money getPreviousRecommendedPrice()
	{
		return previousRecommendedPrice;
	}

	public void setPreviousRecommendedPrice(Money previousRecommendedPrice)
	{
		this.previousRecommendedPrice = previousRecommendedPrice;
	}

	public Money getActualRecommendedPrice()
	{
		return actualRecommendedPrice;
	}

	public void setActualRecommendedPrice(Money actualRecommendedPrice)
	{
		this.actualRecommendedPrice = actualRecommendedPrice;
	}

	public Money getPreviousPurchaseCost()
	{
		return previousPurchaseCost;
	}

	public void setPreviousPurchaseCost(Money previousPurchaseCost)
	{
		this.previousPurchaseCost = previousPurchaseCost;
	}

	public Money getActualPurchaseCost()
	{
		return actualPurchaseCost;
	}

	public void setActualPurchaseCost(Money actualPurchaseCost)
	{
		this.actualPurchaseCost = actualPurchaseCost;
	}

	public BigDecimal getPreviousMass()
	{
		return previousMass;
	}

	public void setPreviousMass(BigDecimal previousMass)
	{
		this.previousMass = previousMass;
	}

	public BigDecimal getActualMass()
	{
		return actualMass;
	}

	public void setActualMass(BigDecimal actualMass)
	{
		this.actualMass = actualMass;
	}

	public BigDecimal getPreviousLength()
	{
		return previousLength;
	}

	public void setPreviousLength(BigDecimal previousLength)
	{
		this.previousLength = previousLength;
	}

	public BigDecimal getActualLength()
	{
		return actualLength;
	}

	public void setActualLength(BigDecimal actualLength)
	{
		this.actualLength = actualLength;
	}

	public BigDecimal getPreviousWidth()
	{
		return previousWidth;
	}

	public void setPreviousWidth(BigDecimal previousWidth)
	{
		this.previousWidth = previousWidth;
	}

	public BigDecimal getActualWidth()
	{
		return actualWidth;
	}

	public void setActualWidth(BigDecimal actualWidth)
	{
		this.actualWidth = actualWidth;
	}

	public BigDecimal getPreviousHeight()
	{
		return previousHeight;
	}

	public void setPreviousHeight(BigDecimal previousHeight)
	{
		this.previousHeight = previousHeight;
	}

	public BigDecimal getActualHeight()
	{
		return actualHeight;
	}

	public void setActualHeight(BigDecimal actualHeight)
	{
		this.actualHeight = actualHeight;
	}

	public BigDecimal getPreviousSurface()
	{
		return previousSurface;
	}

	public void setPreviousSurface(BigDecimal previousSurface)
	{
		this.previousSurface = previousSurface;
	}

	public BigDecimal getActualSurface()
	{
		return actualSurface;
	}

	public void setActualSurface(BigDecimal actualSurface)
	{
		this.actualSurface = actualSurface;
	}

	public BigDecimal getPreviousVolume()
	{
		return previousVolume;
	}

	public void setPreviousVolume(BigDecimal previousVolume)
	{
		this.previousVolume = previousVolume;
	}

	public BigDecimal getActualVolume()
	{
		return actualVolume;
	}

	public void setActualVolume(BigDecimal actualVolume)
	{
		this.actualVolume = actualVolume;
	}

	public Integer getPreviousQuantity()
	{
		return previousQuantity;
	}

	public void setPreviousQuantity(Integer previousQuantity)
	{
		this.previousQuantity = previousQuantity;
	}

	public Integer getActualQuantity()
	{
		return actualQuantity;
	}

	public void setActualQuantity(Integer actualQuantity)
	{
		this.actualQuantity = actualQuantity;
	}

	public BigDecimal getPreviousDiscount()
	{
		return previousDiscount;
	}

	public void setPreviousDiscount(BigDecimal previousDiscount)
	{
		this.previousDiscount = previousDiscount;
	}

	public BigDecimal getActualDiscount()
	{
		return actualDiscount;
	}

	public void setActualDiscount(BigDecimal actualDiscount)
	{
		this.actualDiscount = actualDiscount;
	}

	public Money getNextValidatedPrice()
	{
		return nextValidatedPrice;
	}

	public void setNextValidatedPrice(Money nextValidatedPrice)
	{
		this.nextValidatedPrice = nextValidatedPrice;
	}

	public String getNextID()
	{
		return nextID;
	}

	public void setNextID(String nextID)
	{
		this.nextID = nextID;
	}

	public Money getNextPriceBeforeunitEstimator()
	{
		return nextPriceBeforeunitEstimator;
	}

	public void setNextPriceBeforeunitEstimator(Money nextPriceBeforeunitEstimator)
	{
		this.nextPriceBeforeunitEstimator = nextPriceBeforeunitEstimator;
	}

	public Money getNextFormerRetailPrice()
	{
		return nextFormerRetailPrice;
	}

	public void setNextFormerRetailPrice(Money nextFormerRetailPrice)
	{
		this.nextFormerRetailPrice = nextFormerRetailPrice;
	}

	public Money getNextCurrentRetailPrice()
	{
		return nextCurrentRetailPrice;
	}

	public void setNextCurrentRetailPrice(Money nextCurrentRetailPrice)
	{
		this.nextCurrentRetailPrice = nextCurrentRetailPrice;
	}

	public Money getNextTargetPrice()
	{
		return nextTargetPrice;
	}

	public void setNextTargetPrice(Money nextTargetPrice)
	{
		this.nextTargetPrice = nextTargetPrice;
	}

	public Money getNextRecommendedPrice()
	{
		return nextRecommendedPrice;
	}

	public void setNextRecommendedPrice(Money nextRecommendedPrice)
	{
		this.nextRecommendedPrice = nextRecommendedPrice;
	}

	public Money getNextPurchaseCost()
	{
		return nextPurchaseCost;
	}

	public void setNextPurchaseCost(Money nextPurchaseCost)
	{
		this.nextPurchaseCost = nextPurchaseCost;
	}

	public Integer getNextQuantity()
	{
		return nextQuantity;
	}

	public void setNextQuantity(Integer nextQuantity)
	{
		this.nextQuantity = nextQuantity;
	}

	public BigDecimal getNextDiscount()
	{
		return nextDiscount;
	}

	public void setNextDiscount(BigDecimal nextDiscount)
	{
		this.nextDiscount = nextDiscount;
	}

	public BigDecimal getNextMass()
	{
		return nextMass;
	}

	public void setNextMass(BigDecimal nextMass)
	{
		this.nextMass = nextMass;
	}

	public BigDecimal getNextWidth()
	{
		return nextWidth;
	}

	public void setNextWidth(BigDecimal nextWidth)
	{
		this.nextWidth = nextWidth;
	}

	public BigDecimal getNextLength()
	{
		return nextLength;
	}

	public void setNextLength(BigDecimal nextLength)
	{
		this.nextLength = nextLength;
	}

	public BigDecimal getNextHeight()
	{
		return nextHeight;
	}

	public void setNextHeight(BigDecimal nextHeight)
	{
		this.nextHeight = nextHeight;
	}

	public BigDecimal getNextSurface()
	{
		return nextSurface;
	}

	public void setNextSurface(BigDecimal nextSurface)
	{
		this.nextSurface = nextSurface;
	}

	public BigDecimal getNextVolume()
	{
		return nextVolume;
	}

	public void setNextVolume(BigDecimal nextVolume)
	{
		this.nextVolume = nextVolume;
	}

}
