package fr.unitEstimator.service.competitive.exports.impl;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static fr.unitEstimator.model.exportObject.config.ExportAttrEnum.*;
import static fr.unitEstimator.util.StringUtil.SPACE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.math.BigDecimal;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import edu.emory.mathcs.backport.java.util.Collections;
import fr.ue.shared.ParametreCatalogueEnum;
import fr.unitEstimator.dao.core.PartDao;
import fr.unitEstimator.model.database.core.Part;
import fr.unitEstimator.model.database.core.SubFamily;
import fr.unitEstimator.model.exportObject.config.PartStatusFilter;
import fr.unitEstimator.service.competitive.exports.ExportConfiguration;
import fr.unitEstimator.service.competitive.exports.ExportSerialiser;
import fr.unitEstimator.service.core.PartFeature;
import fr.unitEstimator.service.core.PartHistoryService;
import fr.unitEstimator.service.core.PartPriceHistory;
import fr.unitEstimator.shared.chunks.executors.PartsChunkedExecutor;
import fr.unitEstimator.shared.chunks.executors.PartsProcessor;
import fr.unitEstimator.shared.chunks.executors.PartsReader;
import fr.unitEstimator.shared.chunks.executors.SubfamilyReader;
import fr.unitEstimator.util.BundleUtil;
import fr.unitEstimator.util.Money;
import fr.unitEstimator.util.TypeParametrageEnum;

@Service("exportPartHistoryService")
@Scope("prototype")
public class ExportUnitHistoryServiceImpl extends AbstractExportPartService
{
	@Autowired
	private PartHistoryService partHistoryService;
	@Autowired
	private PartDao partDao;

	@Override
	public int exportData(final ExportConfiguration config, ExportSerialiser serialiser)
	{
		initExport(config, serialiser);
		config.setExportCurrency(
		        catalog.getParametersMap().get(ParametreCatalogueEnum.unitEstimator_DEVISE).getValue());

		addAllHeaders();

		if (config.getPart() != null)
		{
			List<PartPriceHistory> partPriceHistories = partHistoryService.loadPartPriceHistory(config.getPart());
			Collections.sort(partPriceHistories, referenceAndDateCombinedComparator);
			exportPartHistories(partPriceHistories);
			lineNb = partPriceHistories.size();
			return lineNb;
		}

		final boolean rulesActivated;
		if (isNotEmpty(config.getCentralRuleStatuses()))
		{
			rulesActivated = true;
		} else
		{
			rulesActivated = false;
		}

		final PartStatusFilter partStatusFilter = config.getStatusFilter();
		final Predicate<Part> partsFilterPredicate = getPartsFilterPredicate(partStatusFilter, rulesActivated);

		final List<PartPriceHistory> allHistories = new ArrayList<PartPriceHistory>();
		PartsChunkedExecutor chuckynator = new PartsChunkedExecutor(SF_CHUNK_SIZE, PARTS_CHUNK_SIZE);

		chuckynator.setSubfamilyReader(new SubfamilyReader()
		{
			public Collection<Long> read()
			{
				List<SubFamily> subFamilies = findSubfamilies(config, rulesActivated);
				return getSubFamilyIds(subFamilies);
			}
		});

		chuckynator.setPartsReader(new PartsReader()
		{
			public Collection<Part> read(Collection<Long> partIds)
			{
				return partDao.findPartsForExportPartHistory(partIds);
			}

			@Override
			public Collection<Part> filter(Collection<Part> parts)
			{
				return parts;
			}
		});

		chuckynator.setPartsProcessor(new PartsProcessor()
		{
			public void process(Collection<Part> parts)
			{
				Collection<Part> filteredParts = Collections2.filter(parts, partsFilterPredicate);
				// Generate export
				List<PartPriceHistory> partPriceHistories = partHistoryService.loadPartPriceHistory(filteredParts,
				        config.getStartDate(), config.getEndDate());
				allHistories.addAll(partPriceHistories);
			}

		});

		chuckynator.execute();
		Collections.sort(allHistories, referenceAndDateCombinedComparator);
		exportPartHistories(allHistories);
		serialiser.resizeColumns();

		return lineNb;
	}

	@Override
	public int getLineNb(ExportConfiguration config)
	{
		throw new UnsupportedOperationException("Not Yet Implemented");
	}

	private void addAllHeaders()
	{
		serialiser.addHeaderCell(getTraducedHeader(PART_NUMBER));
		serialiser.addHeaderCell(getTraducedHeader(DATE));
		serialiser.addHeaderCell(getTraducedHeader(AUTHOR));
		serialiser.addHeaderCell(getTraducedHeader(PART_UPDATE_ORIGIN));
		serialiser.addHeaderCell(getTraducedHeader(APPLICATION_DATE));
		serialiser.addHeaderCell(getTraducedHeader(BEFORE_unitEstimator_PRICE));
		serialiser.addHeaderCell(getTraducedHeader(FORMER_PRICE));
		serialiser.addHeaderCell(getTraducedHeader(CURRENT_PRICE));
		serialiser.addHeaderCell(getTraducedHeader(TARGET_PRICE));
		serialiser.addHeaderCell(getTraducedHeader(RECOMMENDED_PRICE));
		serialiser.addHeaderCell(getTraducedHeader(VALIDATED_PRICE));
		serialiser.addHeaderCell(getTraducedHeader(PURCHASE_COST));
		serialiser.addHeaderCell(getTraducedHeader(QUANTITY));
		serialiser.addHeaderCell(getTraducedHeader(DISCOUNT));
		serialiser.addHeaderCell(getTraducedHeader(PART_STATUS));
		serialiser.addHeaderCell(getTraducedHeader(COMMENT));
		serialiser.addHeaderCell(getTraducedHeader(WEIGHT));
		serialiser.addHeaderCell(getTraducedHeader(LENGTH));
		serialiser.addHeaderCell(getTraducedHeader(WIDTH));
		serialiser.addHeaderCell(getTraducedHeader(HEIGHT));
		serialiser.addHeaderCell(getTraducedHeader(SURFACE));
		serialiser.addHeaderCell(getTraducedHeader(VOLUME));
		serialiser.addHeaderCell(getTraducedHeader(FAMILY_CODE));
		serialiser.addHeaderCell(getTraducedHeader(FAMILY_NAME));
		serialiser.addHeaderCell(getTraducedHeader(SUBFAMILY_CODE));
		serialiser.addHeaderCell(getTraducedHeader(SUBFAMILY_NAME));
	}

	private void exportPartHistories(List<PartPriceHistory> partPriceHistories)
	{
		int featureSize = 0;
		lineNb += partPriceHistories.size();

		HistoryUpdateDTO updateTracking = new HistoryUpdateDTO();

		int rowIndex = 1;
		for (PartPriceHistory history : partPriceHistories)
		{

			updateActualHistoryElement(updateTracking, history);
			updateNextHistoryElement(updateTracking, partPriceHistories, rowIndex);

			serialiser.addLineReturn();
			serialiser.addCellAsString(history.getPartNumber());
			serialiser.addCellAsDate(history.getDate());
			StringBuilder name = new StringBuilder();
			if (history.getAuthor() == null)
			{
				name.append(SPACE);
			} else
			{
				name.append(history.getAuthor().getUserLastName()).append(SPACE)
				        .append(history.getAuthor().getUserFirstName());
			}
			serialiser.addCellAsString(name.toString());
			serialiser.addCellAsString(
			        BundleUtil.get(BundleUtil.unitEstimator_EXPORT, history.getOrigin().getShortKey(), locale));
			serialiser.addCellAsDate(history.getApplicationDate());

			addCellAsMoney(updateTracking.isPriceBeforeunitEstimatorUpdated(), history.getPriceBeforeunitEstimator());
			addCellAsMoney(updateTracking.isFormerRetailPriceUpdated(), history.getFormerRetailPrice());
			addCellAsMoney(updateTracking.isCurrentRetailPriceUpdated(), history.getCurrentRetailPrice());
			addCellAsMoney(updateTracking.isTargetPriceUpdated(), history.getTargetPrice());
			addCellAsMoney(updateTracking.isRecommendedPriceUpdated(), history.getRecommendedPrice());
			addCellAsMoney(updateTracking.isValidatedPriceUpdated(), history.getValidatedPrice());
			addCellAsMoney(updateTracking.isPurchaseCostUpdated(), history.getPurchaseCost());
			addCellAsInteger(updateTracking.isQuantityUpdated(), history.getQuantity());
			addCellAsDecimal(updateTracking.isDiscountUpdated(), history.getDiscount());

			String statusKey = history.getPriceStatus() == null ? ""
			        : "unitEstimator_export." + history.getPriceStatus().toLowerCase();
			serialiser.addCellAsString(getTraducedElement(statusKey));
			serialiser.addCellAsString(history.getComment());

			addCellAsDecimal(updateTracking.isMassUpdated(), history.getMass());
			addCellAsDecimal(updateTracking.isLengthUpdated(), history.getLength());
			addCellAsDecimal(updateTracking.isWidthUpdated(), history.getWidth());
			addCellAsDecimal(updateTracking.isHeightUpdated(), history.getHeight());
			addCellAsDecimal(updateTracking.isSurfaceUpdated(), history.getSurface());
			addCellAsDecimal(updateTracking.isVolumeUpdated(), history.getVolume());

			// non-numerical data
			serialiser.addCellAsString(history.getFamilyCode());
			serialiser.addCellAsString(history.getFamilyName());
			serialiser.addCellAsString(history.getSubFamilyCode());
			serialiser.addCellAsString(history.getSubFamilyName());

			int featureSz = serialiseFeatures(serialiser, history.getPartFeatures());
			featureSize = Math.max(featureSize, featureSz);
			updateTracking.increment();
			rowIndex++;
		}

		addFeaturesHeaders(serialiser, featureSize);
	}

	private void updateNextHistoryElement(HistoryUpdateDTO updateTracking, List<PartPriceHistory> partPriceHistories,
	        int rowIndex)
	{

		if (CollectionUtils.isEmpty(partPriceHistories))
		{
			return;
		}

		if (partPriceHistories.size() - 1 <= rowIndex)
		{
			return;
		}

		PartPriceHistory nextElement = partPriceHistories.get(rowIndex);
		updateTracking.setNextID(nextElement.getPartNumber());
		updateTracking.setNextValidatedPrice(nextElement.getValidatedPrice());
		updateTracking.setNextPriceBeforeunitEstimator(nextElement.getPriceBeforeunitEstimator());
		updateTracking.setNextFormerRetailPrice(nextElement.getFormerRetailPrice());
		updateTracking.setNextCurrentRetailPrice(nextElement.getCurrentRetailPrice());
		updateTracking.setNextTargetPrice(nextElement.getTargetPrice());
		updateTracking.setNextRecommendedPrice(nextElement.getRecommendedPrice());
		updateTracking.setNextPurchaseCost(nextElement.getPurchaseCost());
		updateTracking.setNextQuantity(nextElement.getQuantity());
		updateTracking.setNextDiscount(nextElement.getDiscount());
		updateTracking.setNextMass(nextElement.getMass());
		updateTracking.setNextLength(nextElement.getLength());
		updateTracking.setNextWidth(nextElement.getWidth());
		updateTracking.setNextHeight(nextElement.getHeight());
		updateTracking.setNextSurface(nextElement.getSurface());
		updateTracking.setNextVolume(nextElement.getVolume());
	}

	private void updateActualHistoryElement(HistoryUpdateDTO updateTracking, PartPriceHistory history)
	{
		updateTracking.setActualID(history.getPartNumber());
		updateTracking.setActualPriceBeforeunitEstimator(history.getPriceBeforeunitEstimator());
		updateTracking.setActualFormerRetailPrice(history.getFormerRetailPrice());
		updateTracking.setActualCurrentRetailPrice(history.getCurrentRetailPrice());
		updateTracking.setActualTargetPrice(history.getTargetPrice());
		updateTracking.setActualRecommendedPrice(history.getRecommendedPrice());
		updateTracking.setActualValidatedPrice(history.getValidatedPrice());
		updateTracking.setActualPurchaseCost(history.getPurchaseCost());
		updateTracking.setActualQuantity(history.getQuantity());
		updateTracking.setActualDiscount(history.getDiscount());
		updateTracking.setActualMass(history.getMass());
		updateTracking.setActualLength(history.getLength());
		updateTracking.setActualWidth(history.getWidth());
		updateTracking.setActualHeight(history.getHeight());
		updateTracking.setActualSurface(history.getSurface());
		updateTracking.setActualVolume(history.getVolume());
	}

	private void addCellAsMoney(boolean isChanged, Money price)
	{
		if (isChanged)
		{
			serialiser.addCellAsMoney(price, config.getExportCurrency(), null);

		} else
		{
			serialiser.addCellAsMoney(price, config.getExportCurrency());
		}
	}

	private void addCellAsInteger(boolean isChanged, Integer price)
	{
		if (isChanged)
		{
			serialiser.addCellAsInteger(price, null);

		} else
		{
			serialiser.addCellAsInteger(price);
		}
	}

	private void addCellAsDecimal(boolean isChanged, BigDecimal price)
	{
		if (isChanged)
		{
			serialiser.addCellAsDecimal(price, null);

		} else
		{
			serialiser.addCellAsDecimal(price);
		}
	}

	private void addFeaturesHeaders(ExportSerialiser serialiser, int featureSize)
	{
		int startCol = 26;
		for (int i = 0; i < featureSize * 2; i++)
		{
			int featureIndex = (i / 2) + 1;
			if (i % 2 == 0)
			{
				serialiser.addHeaderCell(getTraducedElement("coefficient_feature") + " " + featureIndex,
				        (startCol + i));
			} else
			{
				serialiser.addHeaderCell(getTraducedElement("monetary_feature") + " " + featureIndex, (startCol + i));
			}
		}
	}

	private int serialiseFeatures(ExportSerialiser serialiser, Collection<PartFeature> partFeatures)
	{
		List<PartFeature> coeffs = Lists.newArrayList();
		List<PartFeature> monetaries = Lists.newArrayList();

		if (CollectionUtils.isEmpty(partFeatures))
		{
			return 0;
		}

		for (PartFeature pf : partFeatures)
		{
			if (pf.getMonetaryValue() == null)
			{
				coeffs.add(pf);
			} else
			{
				monetaries.add(pf);
			}
		}

		int maxSize = Math.max(coeffs.size(), monetaries.size());

		for (int i = 0; i < maxSize * 2; i++)
		{
			if (i % 2 == 0)
			{
				if ((i / 2) < coeffs.size())
				{
					serialiser.addCellAsString(addFeature(coeffs.get(i / 2), TypeParametrageEnum.COEFFICIENT));
				} else
				{
					serialiser.addCellAsEmpty();
				}
			} else
			{
				if ((i / 2) < monetaries.size())
				{
					serialiser.addCellAsString(addFeature(monetaries.get(i / 2), TypeParametrageEnum.MONETAIRE));
				} else
				{
					serialiser.addCellAsEmpty();
				}
			}
		}

		return maxSize;
	}

	private String addFeature(PartFeature feature, TypeParametrageEnum type)
	{
		if (TypeParametrageEnum.COEFFICIENT.equals(type))
		{
			return feature.getMainFeatureName() + ":" + feature.getSecondaryFeatureName();
		} else
		{
			return feature.getMainFeatureName() + ":" + feature.getSecondaryFeatureName() + ":"
			        + feature.getMonetaryValue();
		}
	}

	private Comparator<PartPriceHistory> referenceAndDateCombinedComparator = new Comparator<PartPriceHistory>()
	{

		@Override
		public int compare(PartPriceHistory o1, PartPriceHistory o2)
		{
			if (o2 == null || o2.getPartNumber() == null)
			{
				return 1;
			}

			if (o1 == null || o1.getPartNumber() == null)
			{
				return -1;
			}

			if (o1.getPartNumber().equalsIgnoreCase(o2.getPartNumber()))
			{
				if (o1 == null || o1.getDate() == null)
				{
					return 1;
				}

				if (o2 == null || o2.getDate() == null)
				{
					return -1;
				}

				return o2.getDate().compareTo(o1.getDate());
			}

			return o1.getPartNumber().compareToIgnoreCase(o2.getPartNumber());
		}
	};
}
