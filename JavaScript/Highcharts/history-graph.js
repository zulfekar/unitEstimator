/**
 * Global variables
 */
var globalData = null, chart = null, displaySettings = null, graphExpanded = false;

function unixMilliSecondsToDate(unixTimeInMilliSeconds)
{
	var TimeStamp = new Date(unixTimeInMilliSeconds); // Unix timestamp in
	// milliseconds
	var dateString = ('0' + TimeStamp.getDate()).slice(-2) + '/'
			+ ('0' + (TimeStamp.getMonth() + 1)).slice(-2) + '/'
			+ TimeStamp.getFullYear() + ' '
			+ ('0' + TimeStamp.getHours()).slice(-2) + ':'
			+ ('0' + TimeStamp.getMinutes()).slice(-2) + ':'
			+ ('0' + TimeStamp.getSeconds()).slice(-2);
	return dateString;
}

function toggleGraphDisplay()
{

	if (graphExpanded)
	{
		$('.graph-expand-collapse')
				.css(
						'background',
						'url(/ue/themes/UnitEstimator-theme-theme/images/UnitEstimator_theme/pic-collaps-open.gif) no-repeat 50% 60%');
		$("#historyChartContainer").toggle(600, "swing");
		graphExpanded = false;
	} else
	{
		$('.graph-expand-collapse')
				.css(
						'background',
						'url(/ue/themes/UnitEstimator-theme-theme/images/UnitEstimator_theme/pic-collaps-close.gif) no-repeat 50% 60%');
		$("#historyChartContainer").toggle(600, "swing");
		graphExpanded = true;
	}
}

function loadHistoryChart(data)
{
	globalData = data;
	if (data == undefined)
	{
		return;
	}

	var displayedDatas = data;
	var left = $(".chartScrollBar").scrollLeft();
	var index = Math.ceil(left / 100);

	if (data.length > 12)
	{
		displayedDatas = data.slice(index, index + 12);
	}

	drawHistoryChart(displayedDatas);
}

function loadPartialHistoryChart()
{
	left = $(".chartScrollBar").scrollLeft();
	index = Math.ceil(left / 100);

	displayedDatas = globalData.slice(index, index + 12);
	drawHistoryChart(displayedDatas);
}

function displayChart()
{
	if (graphExpanded)
	{
		$('#historyChartContainer').show();
		$('.graph-expand-collapse')
				.css(
						'background',
						'url(/ue/themes/UnitEstimator-theme-theme/images/UnitEstimator_theme/pic-collaps-close.gif) no-repeat 50% 60%');
	} else
	{
		$('#historyChartContainer').hide();
		$('.graph-expand-collapse')
				.css(
						'background',
						'url(/ue/themes/UnitEstimator-theme-theme/images/UnitEstimator_theme/pic-collaps-open.gif) no-repeat 50% 60%');
	}
}

function getXAxisCategories(chartData)
{
	var result = [];

	for (i = 0; i < chartData.length; i++)
	{
		result.push(chartData[i].origin + ' '
				+ unixMilliSecondsToDate(chartData[i].date));
	}

	return result;
}

function getXAxisLabel(value)
{
	var splitted = value.split(" ");
	return '<b style="color: #000000">' + originLabels[splitted[0]]
			+ '</b><br><br/>'
			+ '<br><br/><span style="font-weight: bold; color: #C73110";>'
			+ splitted[1] + '</span><br/><b style="color: #000000">'
			+ splitted[2] + '</b>';
}

function drawHistoryChart(chartData)
{

	var chart =
	{
		zoomType : 'x'
	};
	var subtitle =
	{
		text : ''
	};
	var title =
	{
		text : labels['part_history']
	};
	var credits =
	{
		enabled : false
	};
	var xAxis =
	{
		categories : getXAxisCategories(chartData),
		crosshair : true,
		labels :
		{
			formatter : function()
			{
				return getXAxisLabel(this.value)
			}
		}

	};
	var yAxis = [

	{ // Primary yAxis
		title :
		{
			text : labels['price'],
			style :
			{
				color : Highcharts.getOptions().colors[1]
			}
		},
		labels :
		{
			format : '{value} ' + currencySymbol,
			id : 'price',
			style :
			{
				color : Highcharts.getOptions().colors[1]
			}
		}
	},

	{ // Secondary yAxis
		labels :
		{
			format : '{value} %',
			id : 'discount',
			style :
			{
				color : Highcharts.getOptions().colors[1]
			}
		},
		title :
		{
			text : labels['DISCOUNT'],
			style :
			{
				color : Highcharts.getOptions().colors[1]
			}
		}
	},

	{ // Tertiary yAxis
		gridLineWidth : 1,
		title :
		{
			text : labels['QUANTITE'],
			style :
			{
				color : Highcharts.getOptions().colors[1]
			}
		},
		labels :
		{
			format : '{value} ',
			id : 'quantity',
			style :
			{
				color : Highcharts.getOptions().colors[1]
			}
		},
		opposite : true
	} ];
	var tooltip =
	{
		shared : true
	};
	var legend =
	{
	/*
	 * layout: 'vertical', align: 'left', x: 120, verticalAlign: 'top', y: 100,
	 * floating: true, backgroundColor: (Highcharts.theme &&
	 * Highcharts.theme.legendBackgroundColor) || '#FFFFFF'
	 */
	};
	var series = [
	{
		name : labels['PRIX_AVANT_UNITESTIMATOR'],
		type : 'line',
		yAxis : 0,
		marker :
		{
			symbol : 'diamond'
		},
		color : '#990000',
		data : _.pluck(chartData, 'priceBeforeUnitEstimator'),
		tooltip :
		{
			valueSuffix : ' ' + currencySymbol
		}

	},
	{
		name : labels['PRIX_AVANT'],
		type : 'line',
		yAxis : 0,
		color : '#38C7C7',
		marker :
		{
			symbol : 'url(' + formerIconUrl + ')',
			width : 13,
			height : 13
		},
		data : _.pluck(chartData, 'formerRetailPrice'),
		tooltip :
		{
			valueSuffix : ' ' + currencySymbol
		}

	},
	{
		name : labels['PRIX_EN_VIGUEUR'],
		type : 'line',
		yAxis : 0,
		marker :
		{
			symbol : 'circle'
		},
		color : '#0066CC',
		data : _.pluck(chartData, 'currentRetailPrice'),
		tooltip :
		{
			valueSuffix : ' ' + currencySymbol
		}

	},
	{
		name : labels['PRIX_CIBLE_UNITESTIMATOR'],
		type : 'line',
		yAxis : 0,
		marker :
		{
			symbol : 'triangle'
		},
		color : '#FFCC00',
		data : _.pluck(chartData, 'targetPrice'),
		tooltip :
		{
			valueSuffix : ' ' + currencySymbol
		}

	},
	{
		name : labels['PRIX_PRECONISE_UNITESTIMATOR'],
		type : 'line',
		yAxis : 0,
		marker :
		{
			symbol : 'square'
		},
		color : '#99CC00',
		data : _.pluck(chartData, 'recommendedPrice'),
		tooltip :
		{
			valueSuffix : ' ' + currencySymbol
		}

	},
	{
		name : labels['PRIX_VALIDE_UNITESTIMATOR'],
		type : 'line',
		yAxis : 0,
		color : '#9900FF',
		marker :
		{
			symbol : 'url(' + validatedIconUrl + ')',
			width : 13,
			height : 13
		},
		data : _.pluck(chartData, 'validatedPrice'),
		tooltip :
		{
			valueSuffix : ' ' + currencySymbol
		}

	},
	{
		name : labels['PRIX_ACHAT'],
		type : 'line',
		yAxis : 1,
		marker :
		{
			symbol : 'triangle-down'
		},
		color : '#B05F3C',
		data : _.pluck(chartData, 'purchaseCost'),
		tooltip :
		{
			valueSuffix : ' ' + currencySymbol
		}

	},
	{
		name : labels['QUANTITE'],
		type : 'line',
		yAxis : 2,
		color : '#3399FF',
		marker :
		{
			symbol : 'square'
		},
		data : _.pluck(chartData, 'quantity'),

		tooltip :
		{
			valueSuffix : ' unit'
		}
	},
	{
		name : labels['DISCOUNT'],
		type : 'line',
		yAxis : 1,
		color : '#F17424',
		marker :
		{
			symbol : 'url(' + discountIconUrl + ')',
			width : 13,
			height : 13
		},
		data : _.pluck(chartData, 'discount'),
		tooltip :
		{
			valueSuffix : ' %'
		}
	} ];
	var scrollbar =
	{};

	var json =
	{};
	json.chart = chart;
	json.title = title;
	json.subtitle = subtitle;
	json.xAxis = xAxis;
	json.yAxis = yAxis;
	json.tooltip = tooltip;
	json.legend = legend;
	json.series = series;
	json.credits = credits;
	$('#graphContainer').highcharts(json);
	displayChart();
}
