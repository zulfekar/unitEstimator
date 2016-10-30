package fr.unitEstimator.service.competitive.exports;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fr.ue.adm.dao.AdmUserDao;
import fr.ue.adm.database.AdmUserModel;
import fr.ue.excel.ExcelUtil;
import fr.ue.excel.HeaderCell;
import fr.ue.excel.HeaderCellConverter;
import fr.ue.shared.UserContext;
import fr.unitEstimator.service.process.ExportProcessLimitationException;
import fr.unitEstimator.util.BundleUtil;
import fr.unitEstimator.util.Money;
import fr.unitEstimator.util.excel.ExcelStyle;
import fr.recod.util.ErrorKeyEnum;

/**
 * 
 * Contains utility methods for Excel generation. Classes used to generate
 * different versions of Excel ( for example <code>Xls</code> or
 * <code>Xlsx</code>) should extend {@link AbstractExcelSerialiser}.
 * 
 * @author Z
 * 
 */
public abstract class AbstractExcelSerialiser
{

	@Autowired
	private UserContext userContext;

	@Autowired
	private AdmUserDao admUserDao;

	protected Workbook workbook;
	private Sheet sheet;
	private Row row;

	private CellStyle headerStyle;
	private CellStyle percentStyle;
	private CellStyle decimalStyle;
	private CellStyle dateStyle;
	private CellStyle dateWithoutTimeStyle;
	private Map<String, CellStyle> currencyStyles = new HashMap<String, CellStyle>(1);

	private int rowIndex = 0;
	private int columnIndex = 0;

	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

	public AbstractExcelSerialiser()
	{
		workbook = createWorkbook();
		sheet = workbook.createSheet("Sheet 1");
		row = sheet.createRow(rowIndex++);

		headerStyle = ExcelStyle.initHeaderStyle(workbook);
		percentStyle = getPercentStyle();
		decimalStyle = getDecimalStyle();
		dateStyle = getDateStyle();
		dateWithoutTimeStyle = getDateWithoutTimeStyle();
		init();
	}

	public void addSheet(String sheetName)
	{
		sheet = null;
		row = null;
		rowIndex = 0;
		columnIndex = 0;

		sheet = workbook.createSheet(sheetName);
		row = sheet.createRow(rowIndex++);
	}

	public void renameSheet(int index, String newName)
	{
		workbook.setSheetName(index, newName);
	}

	public void addLines(List<HeaderCell> cells, int level)
	{
		validateNumberOfColumns(cells.size());
		new HeaderCellConverter().writeHeadersIntoExcelSheet(cells, level, sheet, headerStyle);
		rowIndex = level;
		addLineReturn();
	}

	public void addHeaderCell(String value)
	{
		validateColumnIndex();
		Cell labelCell = row.createCell(columnIndex);
		labelCell.setCellValue(createRichTextString(value));
		labelCell.setCellStyle(headerStyle);
		columnIndex++;
	}

	public void addHeaderCell(String value, int colIndex)
	{
		validateColumnIndex();
		Row row = sheet.getRow(0);

		if (row == null)
		{
			row = sheet.createRow(0);
		}

		Cell labelCell = row.createCell(colIndex);
		labelCell.setCellValue(createRichTextString(value));
		labelCell.setCellStyle(headerStyle);
	}

	public void addCellAsInteger(Integer value)
	{
		if (value != null)
		{
			Cell valueCell = row.createCell(columnIndex);
			valueCell.setCellValue(value);
		}
		columnIndex++;
	}

	public void addCellAsInteger(Integer value, CellStyle style)
	{
		if (value != null)
		{
			Cell valueCell = row.createCell(columnIndex);
			valueCell.setCellValue(value);
			CellStyle cellWithBackground = workbook.createCellStyle();
			cellWithBackground.setFillBackgroundColor(new XSSFColor(new byte[] { 40, 40, 40 }).getIndexed());
			cellWithBackground.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
			cellWithBackground.setAlignment(CellStyle.ALIGN_RIGHT);
			cellWithBackground.setWrapText(true);
			cellWithBackground.setFillForegroundColor(IndexedColors.PALE_BLUE.index);
			cellWithBackground.setFillPattern(CellStyle.SOLID_FOREGROUND);
			valueCell.setCellStyle(cellWithBackground);
		}
		columnIndex++;
	}

	public void addCellAsInteger(Integer value, int columnIndex)
	{
		if (value != null)
		{
			Cell valueCell = row.createCell(columnIndex);
			valueCell.setCellValue(value);
		}
	}

	public void addCellAsString(String value)
	{
		if (value != null)
		{
			Cell labelCell = row.createCell(columnIndex);
			labelCell.setCellValue(createRichTextString(value));
		}
		columnIndex++;
	}

	public void addCellAsString(String value, int columnIndex)
	{
		if (value != null)
		{
			Cell labelCell = row.createCell(columnIndex);
			labelCell.setCellValue(createRichTextString(value));
		}
	}

	public void addCellAsDate(Date value)
	{
		if (value != null)
		{
			Cell labelCell = row.createCell(columnIndex);
			labelCell.setCellValue(value);
			labelCell.setCellStyle(dateStyle);
		}
		columnIndex++;
	}

	public void addCellAsDate(Date value, int columnIndex)
	{
		if (value != null)
		{
			Cell labelCell = row.createCell(columnIndex);
			labelCell.setCellValue(value);
			labelCell.setCellStyle(dateStyle);
		}
	}

	public void addCellAsDateWithoutTime(Date value)
	{
		if (value != null)
		{
			Cell labelCell = row.createCell(columnIndex);
			labelCell.setCellValue(value);
			labelCell.setCellStyle(dateWithoutTimeStyle);
		}
		columnIndex++;
	}

	public void addCellAsMoney(Money value, String currencyCode)
	{
		if (value != null && !value.isZero())
		{
			Cell labelCell = row.createCell(columnIndex);
			labelCell.setCellValue(Money.floor(value).getAmount().doubleValue());
			labelCell.setCellStyle(getCurrencyStyle(currencyCode));
		}
		columnIndex++;
	}

	public void addCellAsMoney(Money value, String currencyCode, CellStyle style)
	{
		if (value != null && !value.isZero())
		{
			Cell labelCell = row.createCell(columnIndex);
			labelCell.setCellValue(Money.floor(value).getAmount().doubleValue());
			CellStyle cellWithBackground = workbook.createCellStyle();
			cellWithBackground.setFillBackgroundColor(new XSSFColor(new byte[] { 40, 40, 40 }).getIndexed());
			cellWithBackground.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
			cellWithBackground.setAlignment(CellStyle.ALIGN_RIGHT);
			cellWithBackground.setWrapText(true);
			cellWithBackground.setFillForegroundColor(IndexedColors.PALE_BLUE.index);
			cellWithBackground.setFillPattern(CellStyle.SOLID_FOREGROUND);
			final DataFormat format = workbook.createDataFormat();
			cellWithBackground.setDataFormat(format.getFormat("#,##0.00 " + Money.getCurrencySymbol(currencyCode)));
			labelCell.setCellStyle(cellWithBackground);
		}
		columnIndex++;
	}

	public void addCellAsPercent(BigDecimal value)
	{
		if (value != null)
		{
			Cell labelCell = row.createCell(columnIndex);
			labelCell.setCellValue(value.setScale(2, RoundingMode.HALF_UP).doubleValue());
			labelCell.setCellStyle(percentStyle);
		}
		columnIndex++;
	}

	public void addCellAsDecimal(BigDecimal value)
	{
		if (value != null)
		{
			Cell labelCell = row.createCell(columnIndex);
			labelCell.setCellValue(value.setScale(2, RoundingMode.HALF_UP).doubleValue());
			labelCell.setCellStyle(decimalStyle);
		}
		columnIndex++;
	}

	public void addCellAsDecimal(BigDecimal value, CellStyle style)
	{
		if (value != null)
		{
			Cell labelCell = row.createCell(columnIndex);
			labelCell.setCellValue(value.setScale(2, RoundingMode.HALF_UP).doubleValue());
			CellStyle cellWithBackground = workbook.createCellStyle();
			cellWithBackground.setFillBackgroundColor(new XSSFColor(new byte[] { 40, 40, 40 }).getIndexed());

			cellWithBackground.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
			cellWithBackground.setAlignment(CellStyle.ALIGN_RIGHT);
			cellWithBackground.setWrapText(true);
			cellWithBackground.setFillForegroundColor(IndexedColors.PALE_BLUE.index);
			cellWithBackground.setFillPattern(CellStyle.SOLID_FOREGROUND);
			final DataFormat format = workbook.createDataFormat();
			cellWithBackground.setDataFormat(format.getFormat("#,##0.00 "));
			labelCell.setCellStyle(cellWithBackground);
			labelCell.setCellStyle(cellWithBackground);
		}
		columnIndex++;
	}

	public void addCellAsDecimal(BigDecimal value, int columnIndex)
	{
		if (value != null)
		{
			Cell labelCell = row.createCell(columnIndex);
			labelCell.setCellValue(value.doubleValue());
			labelCell.setCellStyle(decimalStyle);
		}
	}

	public void incrementRowIndex(int rowsToIncrement)
	{
		rowIndex = rowsToIncrement - 1;
	}

	public void addLineReturn()
	{
		validateRowIndex();
		columnIndex = 0;
		row = sheet.createRow(rowIndex++);
	}

	public void setLocale(Locale locale)
	{
	}

	public void serialiseUserDetails(Long userId)
	{
		final AdmUserModel user = admUserDao.get(userId);
		if (user != null)
		{
			addCellAsString(user.getUserFirstName() + " " + user.getUserLastName());
		} else
		{
			addCellAsEmpty();
		}
	}

	public void addTranslatedCellAsString(String key)
	{
		String translation = BundleUtil.get(BundleUtil.unitEstimator_EXPORT, key, userContext.getLocale());
		addCellAsString(translation);
	}

	public void addTranslatedCellAsString(String key, Object... params)
	{
		String translation = BundleUtil.get(BundleUtil.unitEstimator_EXPORT, key, userContext.getLocale(), params);
		addCellAsString(translation);
	}

	public void addCellAsBoolean(Boolean value)
	{
		if (value != null)
		{
			addCellAsString(value.toString());
		} else
		{
			columnIndex++;
		}
	}

	public void clean()
	{
		workbook = null;
		headerStyle = null;
		percentStyle = null;
		decimalStyle = null;
		dateStyle = null;
		sheet = null;
		row = null;
		currencyStyles = null;
	}

	public byte[] getContent()
	{
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		try
		{
			workbook.write(os);
			return os.toByteArray();
		} catch (IOException e)
		{
			LOGGER.warn("Can't create Excel file", e);
		} finally
		{
			try
			{
				os.close();
			} catch (IOException e)
			{
				LOGGER.warn("Can't close Excel file", e);
			}
		}
		return null;
	}

	public void addCellAsEmpty()
	{
		columnIndex++;
	}

	public void resizeColumns()
	{
		ExcelUtil.resizeColumns(sheet);
	}

	public void setColumnIndex(int columnIndex)
	{
		this.columnIndex = columnIndex;
	}

	public int getColumnIndex()
	{
		return columnIndex;
	}

	protected abstract RichTextString createRichTextString(String value);

	protected abstract Workbook createWorkbook();

	protected abstract int getMaximumRowIndex();

	protected abstract ErrorKeyEnum getErrorKey();

	protected abstract int getMaximumColumnIndex();

	protected void init()
	{
	}

	private void validateRowIndex()
	{
		if (rowIndex > getMaximumRowIndex())
		{
			throw new ExportProcessLimitationException(getErrorKey());
		}
	}

	private void validateColumnIndex()
	{
		if (columnIndex > getMaximumColumnIndex())
		{
			throw new ExportProcessLimitationException(getErrorKey());
		}
	}

	private void validateNumberOfColumns(int noOfColumns)
	{
		int maximumAllowedColumns = getMaximumColumnIndex() + 1;
		if (noOfColumns > maximumAllowedColumns)
		{
			throw new ExportProcessLimitationException(getErrorKey());
		}

	}

	private CellStyle getPercentStyle()
	{
		return createStyle("#,##0.00\\%");
	}

	private CellStyle getDecimalStyle()
	{
		return createStyle("#,##0.00");
	}

	private CellStyle getDateStyle()
	{
		return createStyle("m/d/yy h:mm");
	}

	private CellStyle getDateWithoutTimeStyle()
	{
		return createStyle("m/d/yy");
	}

	private CellStyle createStyle(String dataFormat)
	{
		final CellStyle style = workbook.createCellStyle();
		final DataFormat format = workbook.createDataFormat();
		style.setDataFormat(format.getFormat(dataFormat));
		return style;
	}

	private CellStyle getCurrencyStyle(String currencyCode)
	{
		if (!currencyStyles.containsKey(currencyCode))
		{
			final CellStyle style = createStyle("#,##0.00 " + Money.getCurrencySymbol(currencyCode));
			currencyStyles.put(currencyCode, style);
		}
		return currencyStyles.get(currencyCode);
	}

	public Workbook getWorkbook()
	{
		return workbook;
	}

}
