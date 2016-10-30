package fr.unitEstimator.central.pricing;

import static fr.unitEstimator.central.pricing.PriceHistoryComponent.PriceHistorySortColumn.DATE;
import static fr.unitEstimator.central.pricing.PriceHistoryComponent.PriceHistorySortColumn.ORIGIN;
import static fr.unitEstimator.central.pricing.PriceHistoryComponent.PriceHistorySortColumn.APPLICATION_DATE;
import static fr.unitEstimator.central.pricing.PriceHistoryComponent.PriceHistorySortColumn.BEFORE_unitEstimator;
import static fr.unitEstimator.central.pricing.PriceHistoryComponent.PriceHistorySortColumn.FORMER;
import static fr.unitEstimator.central.pricing.PriceHistoryComponent.PriceHistorySortColumn.CURRENT;
import static fr.unitEstimator.central.pricing.PriceHistoryComponent.PriceHistorySortColumn.TEMPORARY;
import static fr.unitEstimator.central.pricing.PriceHistoryComponent.PriceHistorySortColumn.TARGET;
import static fr.unitEstimator.central.pricing.PriceHistoryComponent.PriceHistorySortColumn.RECOMMENDED;
import static fr.unitEstimator.central.pricing.PriceHistoryComponent.PriceHistorySortColumn.VALIDATED;
import static fr.unitEstimator.central.pricing.PriceHistoryComponent.PriceHistorySortColumn.NEXT_LEVEL;
import static fr.unitEstimator.central.pricing.PriceHistoryComponent.PriceHistorySortColumn.COMMENT;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.comparators.ComparatorChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import fr.ue.shared.UserContext;
import fr.unitEstimator.model.database.core.Part;
import fr.unitEstimator.model.exportObject.ExportTypeEnum;
import fr.unitEstimator.model.exportObject.config.ExportFormatEnum;
import fr.unitEstimator.service.competitive.exports.ExportConfiguration;
import fr.unitEstimator.service.core.PartHistoryService;
import fr.unitEstimator.service.core.PartPriceHistory;
import fr.unitEstimator.service.process.ueProcess;
import fr.unitEstimator.service.process.ProcessFactory;
import fr.unitEstimator.service.process.ProcessManagementService;
import fr.unitEstimator.util.LanguageController;
import fr.unitEstimator.util.SortableList;
import fr.recod.ProcessTypeEnum;

/**
 * Handles the sorting of each column in the central price history table.
 * 
 * @author Z
 *
 */

@Component
@Scope("prototype")
public class PriceHistoryComponent
{

	/*
	 * sort columns
	 */
	protected enum PriceHistorySortColumn
	{
		DATE("date"), ORIGIN("origin"), APPLICATION_DATE("applicationDate"), BEFORE_unitEstimator(
		        "priceBeforeunitEstimator"), FORMER("formerRetailPrice"), CURRENT("currentRetailPrice"), TEMPORARY(
		                "temporaryRetailPrice"), TARGET("targetPrice"), RECOMMENDED("recommendedPrice"), VALIDATED(
		                        "validatedPrice"), NEXT_LEVEL("nextLevelPrice"), COMMENT("comment");

		private BeanComparator comparator;

		/**
		 * 
		 * @param field
		 *            The name of the property (in {@link PartPriceHistory}) to
		 *            which this {@link SortColumn} corresponds.
		 */
		private PriceHistorySortColumn(String field)
		{
			this.comparator = new BeanComparator(field);
		}
	};

	@Autowired
	private UserContext userContext;

	@Autowired
	private PartHistoryService partHistoryService;

	@Autowired
	private ProcessFactory processFactory;

	@Autowired
	private ProcessManagementService processManagementService;

	private List<PartPriceHistory> priceHistories;

	private SortableList sort;

	private Part part;

	public PriceHistoryComponent()
	{
	}

	public void init(Part part)
	{
		this.part = part;
		this.priceHistories = partHistoryService.loadPartPriceHistory(part);
		sort = createSortableList();
	}

	public SortableList getSort()
	{
		return sort;
	}

	public void exportHistory(ActionEvent ae)
	{
		ExportConfiguration config = new ExportConfiguration();
		config.setType(ExportTypeEnum.PART_HISTORY_EXPORT);
		config.setExportLanguage(userContext.getLocale().getLanguage());
		config.setCompetitiveActivated(false);
		config.setScheduledStartDate(null);
		config.setPart(part);
		config.setExportFormat(ExportFormatEnum.XLS);

		ueProcess process = processFactory.createCompetitiveExportProcess(config, userContext.getCatalogId(),
		        ProcessTypeEnum.PART_HISTORY_EXPORT, userContext.getUser().getId(), 1,
		        userContext.getLocale().getLanguage());

		processManagementService.register(process);

		FacesMessage message = new FacesMessage();
		message.setSeverity(FacesMessage.SEVERITY_INFO);
		message.setSummary(LanguageController.get("exportWizard_launch_start_msg", userContext.getLocale()));
		FacesContext.getCurrentInstance().addMessage(null, message);
	}

	public String getDate()
	{
		return DATE.name();
	}

	public String getOrigin()
	{
		return ORIGIN.name();
	}

	public String getApplicationDate()
	{
		return APPLICATION_DATE.name();
	}

	public String getPriceBeforeunitEstimator()
	{
		return BEFORE_unitEstimator.name();
	}

	public String getFormerRetailPrice()
	{
		return FORMER.name();
	}

	public String getCurrentRetailPrice()
	{
		return CURRENT.name();
	}

	public String getTemporaryRetailPrice()
	{
		return TEMPORARY.name();
	}

	public String getTargetPrice()
	{
		return TARGET.name();
	}

	public String getRecommendedPrice()
	{
		return RECOMMENDED.name();
	}

	public String getValidatedPrice()
	{
		return VALIDATED.name();
	}

	public String getNextLevelPrice()
	{
		return NEXT_LEVEL.name();
	}

	public String getComment()
	{
		return COMMENT.name();
	}

	public Collection<PartPriceHistory> getPriceHistories()
	{
		sort.sortIfParametersHaveChanged();

		return priceHistories;
	}

	private SortableList createSortableList()
	{
		return new SortableList(DATE.name())
		{

			@SuppressWarnings("unchecked")
			@Override
			protected void sort()
			{
				ComparatorChain comparatorChain = new ComparatorChain();
				PriceHistorySortColumn sortColumn = PriceHistorySortColumn.valueOf(sortColumnName);

				comparatorChain.addComparator(DATE.comparator);
				if (sortColumn != null && !sortColumn.equals(DATE))
				{
					comparatorChain.addComparator(sortColumn.comparator);
				}

				if (ascending)
				{
					Collections.sort(priceHistories, comparatorChain);
				} else
				{
					Collections.sort(priceHistories, Collections.reverseOrder(comparatorChain));
				}
			}

			@Override
			protected boolean isDefaultAscending(String sortColumn)
			{
				/*
				 * all sort columns are sorted ascending by default except for
				 * date
				 */
				if (sortColumn.equals(PriceHistorySortColumn.DATE.name()))
				{
					return false;
				} else
				{
					return true;
				}
			}
		};
	}
}
