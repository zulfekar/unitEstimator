// JavaScript Document

/* Author Z */

jQuery(document).ready(function()
{

	hideShowPositioningPanel();
	enableTextBoxHighlightOnFocus();
	arrangePartDetailsCellsWidhts();
	hideShowHistoryTable();
	placeHistoryTableCaption();
	// handleScroll();
	setPopupMarginTopTo33();
	setPopupMarginTopToMinus167();
	resizeLeftPanelDetailsSheet();
	rearrangePartPricingMethodInfoPanel();
	resizeGiantPopupInnerScrolledContainer();
	handleWindowResizing();
	resizeImportNeoProductPanel();
	initializeImportWidth();

});

// //SESSION TIMEOUT MANAGEMENT ////
var ue = ue ||
{};

ue.timeoutmanagement = (function()
{
	'use strict';

	var noteBarTimer, remainingTime, countdownTimer, _popup;
	var _expire_url, _expire_timeout, _interval, _remainingTimeParam, _mcontext;

	function extendSession()
	{
		initNotification();
		timeoutTimer(_expire_url, _expire_timeout);
		intervalTimer(_interval, _remainingTimeParam, _mcontext);
	}

	function initNotification()
	{

		if (undefined != countdownTimer)
		{
			window.clearInterval(countdownTimer);
		}

		if (undefined != noteBarTimer)
		{
			window.clearInterval(noteBarTimer);
		}

		if (undefined != _popup)
		{
			_popup.goAway();
		}
	}

	function timeoutTimer(url, timeout)
	{

		_expire_url = url;
		_expire_timeout = timeout;

		if (undefined != _popup)
		{
			_popup.goAway();
		}

		// Reset old expire timer
		if (window.unitEstimatorTimeoutId)
		{
			window.clearTimeout(window.unitEstimatorTimeoutId);
		}

		// Start a new timer for the next extension
		window.unitEstimatorTimeoutId = setTimeout(function()
		{
			window.location = url;
		}, timeout);
	}

	function intervalTimer(interval, remainingTimeParam, context)
	{

		_interval = interval;
		_remainingTimeParam = remainingTimeParam;
		_mcontext = context;

		initNotification();
		remainingTime = remainingTimeParam;

		noteBarTimer = window.setInterval(function()
		{
			noteBar.show();
			throwCountdown();

			showTimeoutPopup(interval, remainingTimeParam, context);

		}, interval);

		if (typeof noteBar != "undefined")
		{
			noteBar.hide();
		}
	}

	function throwCountdown()
	{
		window.clearInterval(noteBarTimer);
		jQuery("#remainingTimeId").text(" " + remainingTime + " ");

		countdownTimer = window.setInterval(function()
		{
			initCountdown();
		}, 1000);
	}

	function initCountdown()
	{
		if (remainingTime > 0)
		{
			remainingTime = --remainingTime;
			jQuery("#remainingTimeId").text(" " + remainingTime + " ");
		} else
		{
			window.clearInterval(countdownTimer);

			if (undefined != _popup)
			{
				_popup.goAway();
			}

			var extendSessionBt = jQuery("input[id*='extendSessionBt']");
			extendSessionBt.attr("disabled", true);
			extendSessionBt.removeClass("neoCmdBtn");
			extendSessionBt.addClass("iceCmdBtn-dis");
		}
	}

	function showTimeoutPopup(interval, remainingTimeParam, context)
	{

		var hidden = "hid";

		if (document[hidden] != undefined)
		{
			if (document.body.className != "vis" || !document.hasFocus())
			{
				_popup = createTimeOutPopup(interval, remainingTimeParam,
						context);
			}
		} else if (!document.hasFocus())
		{
			_popup = createTimeOutPopup(interval, remainingTimeParam, context);
		}

		if (undefined != _popup)
		{
			window.onbeforeunload = function(e)
			{
				if (undefined != _popup)
				{
					_popup.goAway();
				}
			};
		}

		// Test IDEA
		// _popup = createTimeOutPopup(interval, remainingTimeParam, context);
	}

	function createTimeOutPopup(interval, remainingTimeParam, context)
	{
		var alert_title = jQuery("#SessionTimeoutTitle").html();
		var alert_session_to_be_expired = jQuery(
				"#SessionTimeoutSessionToBeExpired").html();
		var alert_session_to_be_expired_in_seconds = jQuery(
				"#SessionTimeoutSessionToBeExpiredInSeconds").html();
		var alert_button_text = jQuery("#SessionTimeoutSessionButton").html();

		// Render new Alert popup
		var Alert = new CustomAlert();

		Alert
				.setParam(remainingTimeParam, interval, alert_title,
						alert_session_to_be_expired,
						alert_session_to_be_expired_in_seconds,
						alert_button_text, context,
						"width=400,height=210,resizable=no,scrollbars=no,toolbar=no,status=no");

		Alert.preRender();
		Alert.render();
		Alert.startCountDown();
		Alert.postRender();

		return Alert;
	}

	// @DEV Mantis 7130: This is the custom alert with styles and scripts to
	// manage its coundown timer.
	function CustomAlert()
	{

		var remainningSeconds, extendBtn, countdownTimer, reopeningTimer;
		var _alert_title, _alert_session_to_be_expired, _alert_session_to_be_expired_in_seconds, _alert_button_text, _options, _interval, _remainingTimeParam, _context;

		var winTimeOutPopup =
		{};
		var _refresh_time = 15; // Blinking effect every 15s

		this.setParam = function(remainingTimeParam, interval, alert_title,
				alert_session_to_be_expired,
				alert_session_to_be_expired_in_seconds, alert_button_text,
				context, options)
		{
			_remainingTimeParam = remainingTimeParam;
			_interval = interval;
			_alert_title = alert_title;
			_alert_session_to_be_expired = alert_session_to_be_expired;
			_alert_session_to_be_expired_in_seconds = alert_session_to_be_expired_in_seconds;
			_alert_button_text = alert_button_text;
			_context = context;
			_options = options;
		};

		this.preRender = function()
		{
			closeIfOpen();
		};

		this.render = function()
		{
			draw();
		};

		this.postRender = function()
		{
			reopeningTimer = window.setInterval(function()
			{

				_remainingTimeParam = _remainingTimeParam - _refresh_time;

				// Stop open-close-open-close if Popup has focus
				if (!winTimeOutPopup.document.hasFocus())
				{
					closeIfOpen();
					draw();
					countingDown();
				}

			}, _refresh_time * 1000);
		};

		function closeIfOpen()
		{
			if (winTimeOutPopup.closed === false)
			{
				shutDown();
			}
		}

		this.startCountDown = function()
		{
			countingDown();
		};

		this.goAway = function()
		{
			stopReopeningTimer();
			// Close itself in 50 ms
			setTimeout(function()
			{
				winTimeOutPopup.close();
			}, 50);
		};

		function shutDown()
		{
			stopCountDownTimer();
			winTimeOutPopup.close();
		}

		function stopCountDownTimer()
		{
			if (undefined !== countdownTimer)
			{
				window.clearInterval(countdownTimer);
			}
		}

		function stopReopeningTimer()
		{
			if (undefined !== reopeningTimer)
			{
				window.clearInterval(reopeningTimer);
			}
		}

		function clearAllTimers()
		{
			stopCountDownTimer();
			stopReopeningTimer();
		}

		function countingDown()
		{

			var remainingTime;

			startIntervalTimer(_interval, _remainingTimeParam);

			function startIntervalTimer(interval, remainingTimeParam)
			{
				stopCountDownTimer();
				remainingTime = remainingTimeParam;
				throwCountdown();
			}

			function throwCountdown()
			{
				remainningSeconds.innerHTML = remainingTime;
				countdownTimer = window.setInterval(function()
				{
					beginCountdown();
				}, 1000);
			}

			function beginCountdown()
			{
				if (remainingTime > 0)
				{
					remainingTime = --remainingTime;
					remainningSeconds.innerHTML = remainingTime;
				} else
				{
					clearAllTimers();

					extendBtn.disabled = 'true';
					extendBtn.style.cssText = "outline: none !important;";

					// Close itself in 50 ms
					setTimeout(function()
					{
						winTimeOutPopup.close();
					}, 50);
				}
			}
		}

		function draw()
		{
			// Open new popup == new browser window
			winTimeOutPopup = open("", "", _options);
			var d = winTimeOutPopup.document;

			// Define doc type
			d
					.write('<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd"><html><head><title>unitEstimator reminder</title></head>');
			d.write('<body></body></html>');
			d.close();

			// Add body
			var body = d.getElementsByTagName("body")[0];
			body.style.cssText = "margin: 0;padding: 0;color: #000;background: #579;";

			var modalContainer = body.appendChild(d.createElement("div"));
			modalContainer.id = "modalContainer";
			modalContainer.style.cssText = "display:none;background-color:rgba(0, 0, 0, 0.3);position:absolute;width:100%;height:100%;top:0px;left:0px;z-index:10000;";
			modalContainer.style.height = d.documentElement.scrollHeight + "px";

			var alertObj = modalContainer.appendChild(d.createElement("div"));
			alertObj.id = "alertBox";
			alertObj.style.cssText = "display:none;position:absolute;margin:auto;left:50px;width:300px;min-height:110px;margin-top:50px;border:1px solid #666;background-color:#fff;background-repeat:no-repeat;background-position:20px 30px;";

			var header = alertObj.appendChild(d.createElement("h1"));
			header.appendChild(d.createTextNode(_alert_title));
			header.style.cssText = "margin:0;font:bold 13px verdana,arial;background: none repeat scroll 0% 0% #A13;color:#FFF;border-bottom:4px solid #D41;padding:7px 0px;padding-left:10px;";

			var countDown = alertObj.appendChild(d.createElement("div"));
			countDown.id = "countId";
			countDown.style.cssText = "width:280px;margin-top:10px;margin-left:10px;";

			var session_to_be_expired = countDown.appendChild(d
					.createElement("span"));
			session_to_be_expired.id = "span1";
			session_to_be_expired.style.cssText = "margin-right:3px;font-family: Arial, Helvetica, sans-serif;font-size:12px;color:#333;text-align:left;line-height:18px;";
			session_to_be_expired.innerHTML = _alert_session_to_be_expired;

			remainningSeconds = countDown.appendChild(d.createElement("span"));
			remainningSeconds.id = "remainingTimeId";
			remainningSeconds.style.cssText = "font-family: Arial, Helvetica, sans-serif;font-size:12px;color:#333;text-align:left;line-height:18px;";

			var seconds = countDown.appendChild(d.createElement("span"));
			seconds.id = "span2";
			seconds.style.cssText = "margin-left:3px;font-family: Arial, Helvetica, sans-serif;font-size:12px;color:#333;text-align:left;line-height:18px;";
			seconds.innerHTML = _alert_session_to_be_expired_in_seconds;

			var extendBtnContainer = alertObj.appendChild(d
					.createElement("div"));
			extendBtnContainer.id = "extendBtnContainer";
			extendBtnContainer.style.cssText = "text-align:center;";

			var inputElement = null;

			// Try the IE8 way; this fails on standards-compliant browsers
			try
			{
				inputElement = d.createElement('<input type="submit">');
			} catch (err)
			{
			}

			// Non-IE browser; use canonical method to create typed element
			if (!inputElement || inputElement.nodeName != 'INPUT')
			{
				inputElement = d.createElement("input");
				inputElement.type = "submit";
			}

			extendBtn = extendBtnContainer.appendChild(inputElement);
			extendBtn.id = "extendBtn";
			extendBtn.style.cssText = "color: #FFF !important;font-family: Arial, Helvetica, sans-serif;font-size:12px;cursor: pointer;height:25px;border: 1px solid #32506c;padding: 3px 6px;margin-right: 2px;font-family: Arial, Helvetica, sans-serif;background: #32506c;outline: none !important;display:block;margin: 0 auto;margin-top:10px;margin-bottom:10px;width:140px;text-align: center;";
			extendBtn.readOnly = true;
			extendBtn.value = _alert_button_text;

			extendBtn.onclick = function()
			{

				// Terminate timers in notification bar. Also close itself in
				// 50ms
				winTimeOutPopup.opener.ue.timeoutmanagement.extendSession();

				jQuery.ajax(
				{
					type : "POST",
					url : _context + "/extendunitEstimatorSession",
					data : "action=1",
					success : function()
					{
					}
				});
			};

			extendBtn.onfocus = function()
			{
				setFocus(extendBtn.id);
			};

			extendBtn.onblur = function()
			{
				setFocus('');
			};

			winTimeOutPopup.onbeforeunload = function()
			{
				stopCountDownTimer();
			};

			modalContainer.style.display = "block";
			alertObj.style.display = "block";
			alertObj.style.visibility = "visible";

			// /// END ADDING HTML elements /////
			winTimeOutPopup.focus();
		}
	}

	return
	{
		init : function()
		{

		},

		extendSession : function()
		{
			// To be called from timeout popup to extend session in the
			// unitEstimator
			// page
			extendSession();
		},

		initNotification : function()
		{
			initNotification();
		},

		setTimeoutTimer : function(url, timeout)
		{
			timeoutTimer(url, timeout);
		},

		setIntervalTimer : function(interval, remainingTimeParam, context)
		{
			intervalTimer(interval, remainingTimeParam, context);
		}
	};

}());

// //END SESSION TIMEOUT MANAGEMENT ////

// / VISIBILITY CHANGE MANAGEMENT ///
ue.visibilitychange = (function()
{
	'use strict';

	var hidden = "hid";

	// Standards:
	if (hidden in document)
		document.addEventListener("visibilitychange", onchange);
	else if ((hidden = "mozHidden") in document)
		document.addEventListener("mozvisibilitychange", onchange);
	else if ((hidden = "webkitHidden") in document)
		document.addEventListener("webkitvisibilitychange", onchange);
	else if ((hidden = "msHidden") in document)
		document.addEventListener("msvisibilitychange", onchange);
	// IE 9 and lower:
	else if ("onfocusin" in document)
		document.onfocusin = document.onfocusout = onchange;
	// All others:
	else
		window.onpageshow = window.onpagehide = window.onfocus = window.onblur = onchange;

	function onchange(evt)
	{
		var v = "vis", h = "hid", evtMap =
		{
			focus : v,
			focusin : v,
			pageshow : v,
			blur : h,
			focusout : h,
			pagehide : h
		};

		evt = evt || window.event;

		if (evt.type in evtMap)
			document.body.className = evtMap[evt.type];
		else
			document.body.className = this[hidden] ? "hid" : "vis";
	}

	return
	{
		init : function()
		{
			onchange(
			{
				type : document[hidden] ? "blur" : "focus"
			});
		}
	};
}());

ue.popup = (function()
{
	'use strict';

	function popupBlockerChecker()
	{
		try
		{

			var popUp = window.open(window.location, '_blank');
			if (popUp === null || typeof popUp === 'undefined')
			{

				jQuery("#domainName").text(window.location.hostname);

			} else
			{
				popUp.close();
			}

		} catch (e)
		{
			console.log(e);
		} finally
		{

		}

	}

	return
	{
		blockerChecker : function()
		{
			popupBlockerChecker();
		}
	};
}());

// / END VISIBILITY CHANGE MANAGEMENT ///

function resizeGiantPopupInnerScrolledContainer()
{
	var containerWidth = $(window).width() - 275;
	if (($('.innerScrolledContainer').children().width() + 20) > containerWidth)
	{
		$('.innerScrolledContainer').css("width", containerWidth);
	}
}

function handleWindowResizing()
{
	$(window).resize(function()
	{
		resizeGiantPopupInnerScrolledContainer();
	});
}

function initializeImportNeoProductPanel()
{
	var neoContentWrapperNoFilterHeight = $('.neoContentWrapperNoFilter')
			.height();
	if ($('.leftContainer').height() < 220)
	{
		var importNeoProductPanelHeight = neoContentWrapperNoFilterHeight * 68 / 100;
		$(".importNeoProductPanel").css("height", importNeoProductPanelHeight);
	} else
	{
		var importNeoProductPanelHeight = neoContentWrapperNoFilterHeight * 76 / 100;
		$(".importNeoProductPanel").css("height", importNeoProductPanelHeight);
	}
}

function initializeImportNeoProductPanelForDeletion()
{
	var neoContentWrapperNoFilterHeight = $(
			'.neoContentWrapperNoFilterForDeletion').height();
	var importNeoProductPanelHeight = neoContentWrapperNoFilterHeight * 75 / 100;
	$(".importNeoProductPanelForDeletion").css("height",
			importNeoProductPanelHeight);
}

function resizeImportNeoProductPanel()
{
	var neoContentWrapperNoFilterHeight = $('.neoContentWrapperNoFilter')
			.height();
	if ($('.leftContainer').height() < 295)
	{
		var importNeoProductPanelHeight = neoContentWrapperNoFilterHeight * 77 / 100;
		$(".importNeoProductPanel").css("height", importNeoProductPanelHeight);
	} else
	{
		var importNeoProductPanelHeight = neoContentWrapperNoFilterHeight * 82 / 100;
		$(".importNeoProductPanel").css("height", importNeoProductPanelHeight);
	}
}

function resizeImportNeoProductPanelForDeletion()
{
	var neoContentWrapperNoFilterHeight = $(
			'.neoContentWrapperNoFilterForDeletion').height();
	var importNeoProductPanelHeight = neoContentWrapperNoFilterHeight * 80 / 100;
	$(".importNeoProductPanelForDeletion").css("height",
			importNeoProductPanelHeight);
}

function rearrangePartPricingMethodInfoPanel()
{
	$('.neoTbxWidthMPartPricingMethodLabel').css("width",
			$('.neoTbxWidthMPartPricing').width());
}

function setPopupMarginTopTo33()
{
	var marginTop = $(window).scrollTop() + 33;
	$(".scrolledPopup33").css("top", marginTop);
}

function setPopupMarginTopToMinus167()
{
	var marginTop = $(window).scrollTop() - 167;
	$(".scrolledPopup167").css("top", marginTop);
}

function placeHistoryTableCaption()
{
	var captionWidth = $("#beforeunitEstimator").width()
			+ $("#formerPrice").width() + $("#currentPrice").width()
			+ $("#temporaryPrice").width() + $("#targetPrice").width()
			+ $("#recommendedPrice").width() + $("#validatedPrice").width()
			+ $("#nextLevelPrice").width();
	$(".captionTitle").css("width", captionWidth);
}

function resizeLeftPanelDetailsSheet()
{

	if ($('#neoProductPriceColLft2').height() < ($('#neoProductPriceColRgt1')
			.height() + 35))
	{
		var infoContentHeight = ($("#neoProductPriceColRgt1").height() + 35) - 165;
		$(".infoContent").css("height", infoContentHeight);
	}

}

function hideShowHistoryTable()
{
	$(".partHistoryTable").slideUp();
	$(".accordionHideIcon").hide();

	var clickHandlerWitdh = $(".neoTblColTitle1Accordion").width() + 50;

	$(".clickHandler").css("width", clickHandlerWitdh);
	$(".clickHandler").css("left", -clickHandlerWitdh);
	$(".clickHandler").click(switchHistoryTableView);
	$(".historyTableHeader").click(placeHistoryTableCaption);

}

function showHistoryTable()
{
	$(".partHistoryTable").css("display", "relative");
	$(".accordionShowIcon").hide();
	$(".accordionHideIcon").show();
	$(".partHistoryTable").slideDown();
	_tableHidden = false;
}

function hideHistoryTable()
{
	$(".accordionShowIcon").show();
	$(".accordionHideIcon").hide();
	$(".partHistoryTable").slideUp();
	_tableHidden = true;
}

function switchHistoryTableView()
{
	if (_tableHidden)
	{
		showHistoryTable();
	} else
	{
		hideHistoryTable();
	}
	placeHistoryTableCaption();
}

function arrangePartDetailsCellsWidhts()
{
	if ($("#detailTableCellRef0").width() > $("#detailTableCellRef2").width())
	{
		if ($("#detailTableCellRef0").width() > $("#detailTableCellRef4")
				.width())
		{
			$("#detailTableCellRef2").prop('width',
					$("#detailTableCellRef0").width());
			$("#detailTableCellRef4").prop('width',
					$("#detailTableCellRef0").width());
		} else
		{
			$("#detailTableCellRef2").prop('width',
					$("#detailTableCellRef4").width());
			$("#detailTableCellRef0").prop('width',
					$("#detailTableCellRef4").width());
		}
	} else
	{
		if ($("#detailTableCellRef4").width() > $("#detailTableCellRef2")
				.width())
		{
			$("#detailTableCellRef2").prop('width',
					$("#detailTableCellRef4").width());
			$("#detailTableCellRef0").prop('width',
					$("#detailTableCellRef4").width());
		} else
		{
			$("#detailTableCellRef4").prop('width',
					$("#detailTableCellRef2").width());
			$("#detailTableCellRef0").prop('width',
					$("#detailTableCellRef2").width());
		}
	}
	if ($("#detailTableCellRef1").width() > $("#detailTableCellRef3").width())
	{
		$("#detailTableCellRef3").prop('width',
				$("#detailTableCellRef1").width());
	} else
	{
		$("#detailTableCellRef1").prop('width',
				$("#detailTableCellRef3").width());
	}
}

function enableTextBoxHighlightOnFocus()
{
	jQuery(':text, :password, textarea').focus(function()
	{
		jQuery(this).addClass('textHighlight');
	});
	jQuery(':text,:password, textarea').blur(function()
	{
		jQuery(this).removeClass('textHighlight');
	});
}

// To avoid Queueing events during hide / show Panel
function stopHideShowFilterPanelAnimations()
{
	jQuery("#neoColRight").stop();
	jQuery("#neoFilterPanel").stop();
	jQuery("#neoColLeft").stop();
}
function hideShowFilterPanel()
{
	jQuery("#neoHideFilterPanel").click(
			function()
			{

				stopHideShowFilterPanelAnimations();

				if (jQuery("#neoColLeft").hasClass("neoColLeftCentralRule"))
				{
					jQuery("#neoColLeft").removeClass(
							"neoColLeftCentralRuleForcedWidth");
				}

				jQuery("#neoColRight").animate(
				{
					marginLeft : "0px"
				}, 200);

				if (jQuery("#neoColRight").hasClass("neoColRightCentralRule"))
				{
					jQuery("#neoColRight").removeClass(
							"neoColRightCentralRuleForcedMargin");
					jQuery("#neoFilterPanel").animate(
					{
						marginLeft : "-370px"
					}, 400);
				} else
				{
					jQuery("#neoFilterPanel").animate(
					{
						marginLeft : "-335px"
					}, 400);
				}

				jQuery("#neoColLeft").animate(
				{
					width : "0px",
					opacity : 0
				}, 400);
				jQuery("#neoShowFilterPanel").show("normal").animate(
				{
					width : "28px",
					opacity : 1
				}, 200);
			});
	jQuery("#neoShowFilterPanel").click(function()
	{

		stopHideShowFilterPanelAnimations();

		if (jQuery("#neoColLeft").hasClass("neoColLeftCentralRule"))
		{
			jQuery("#neoColLeft").addClass("neoColLeftCentralRuleForcedWidth");
		}

		if (jQuery("#neoColRight").hasClass("neoColRightCentralRule"))
		{
			jQuery("#neoColRight").animate(
			{
				marginLeft : "370px"
			}, 200);
			jQuery("#neoFilterPanel").animate(
			{
				marginLeft : "0px"
			}, 400);
			jQuery("#neoColLeft").animate(
			{
				width : "374px",
				opacity : 1
			}, 400);
			resizePositioningTableToNormalSize();

			jQuery("#neoShowFilterPanel").animate(
			{
				width : "0px",
				opacity : 0
			}, 200).hide("slow");
		} else
		{
			jQuery("#neoColRight").animate(
			{
				marginLeft : "335px"
			}, 200);
			jQuery("#neoFilterPanel").animate(
			{
				marginLeft : "0px"
			}, 400);
			jQuery("#neoColLeft").animate(
			{
				width : "334px",
				opacity : 1
			}, 400);
			resizePositioningTableToNormalSize();

			jQuery("#neoShowFilterPanel").animate(
			{
				width : "0px",
				opacity : 0
			}, 200).hide("slow");
		}
	});
}

function clickSearchIfEnter(form, e)
{
	if (e.keyCode == 13)
	{
		invokCLick(form, "search");
		return false;
	}
	return true;
}

function hideFilterPanel()
{
	jQuery("#ruleSearchResultsMainContainer").animate(
	{
		marginLeft : "-374px",
		width : "1305px"
	}, 400);
	jQuery("#neoShowFilterPanel").css("z-index", 2000);
}

function showFilterPanel()
{
	jQuery("#ruleSearchResultsMainContainer").animate(
	{
		marginLeft : "0px",
		width : "1118px"
	}, 400);
	jQuery("#neoShowFilterPanel").css("z-index", 0);
}
/*
 * =FilterPanel (End) -----------------------------------------------
 */

/*
 * =Language Selector (Begin) -----------------------------------------------
 */
jQuery(document).ready(
		function($)
		{

			// --- language langsel --- //

			function initLanguageSelection()
			{
				if ($(".neoLangSel").length !== 0)
				{
					return;
				}
				var $form = $("div#neoLangSelect form");
				$form.hide();
				var $source = $form.find(".country-options");
				$source.removeAttr("autocomplete");
				var $options = $source.find("option");
				var $selected = $options.filter(":selected");
				var $target = $('<dl>',
				{
					id : "target",
					'class' : "neoLangSel"
				});
				$("#neoLangSelect").append($target);
				$target.append(
						'<dt><a href="" onclick="setChoosenTabIndex();"><span class="flag"></span><em>'
								+ $selected.text() + '</em></a></dt>').append(
						'<dd><ul></ul></dd>');
				var $languageList = $target.find("ul");
				$options.each(function()
				{
					var $option = $(this);
					var $line = $(
							'<li><a href="" onclick="setChoosenTabIndex();"><span class="flag"></span><em>'
									+ $option.text() + '</em></a></li>').data(
							"lang", $option.val());
					$languageList.append($line);
				});

				var $dropTrigger = $target.find("dt a");

				// when a language is clicked, make the selection and then hide
				// the list
				$target.find("dd li").click(function()
				{
					var clickedValue = $(this).data("lang");
					var clickedTitle = $(this).find("em").text();
					$target.find("dt em").text(clickedTitle);
					$dropTrigger.removeClass("active");
					$languageList.hide();

					// trigger 'on change' event on hidden icefaces form
					$source.val(clickedValue).change();
					return false;
				});

				// open and close list when button is clicked
				$dropTrigger.toggle(function()
				{
					$languageList.slideDown(200);
					$dropTrigger.addClass("active");
				}, function()
				{
					$languageList.slideUp(200);
					jQuery(this).removeClass("active");
				});
			}

			// first time
			initLanguageSelection();
			// on refresh
			ice.onAfterUpdate(initLanguageSelection);

			// close list when anywhere else on the screen is clicked
			jQuery(document).bind('click', function(e)
			{
				// the click event on the language selection doesn't bubble up
				// to document.
				// recalculate each time because we might have reloaded the page
				var $langsel = $(".neoLangSel");
				$langsel.find("ul").slideUp(200);
				$langsel.find("dt a").removeClass("active");
			});
		});

/*
 * =Language Selector (End) -----------------------------------------------
 */

/*
 * =Miniatures, Zoom and Flash (Begin)
 * -----------------------------------------------
 */

// MES GLOBALS
var _imageSelected = null;
var _lastImageSelected = null;
var _image1 = null;
var _image2 = null;
var _image3 = null;
var _flash = null;
var _id = null;
var _context = null;
var _imageToDisplay = null;
var _imageParam = null;
var _imageZoomed = false;
var _isImageLarge = false;
var _tableHidden = true;

$(document).ready(
		function()
		{
			$("#zoomContainerSpan").click(function()
			{
				if (_isImageLarge)
				{
					if (_imageZoomed)
					{
						$("#zoomContainerSpan").removeClass('zoomOutCursor');
						$("#zoomContainerSpan").addClass('zoomInCursor');
						_imageZoomed = false;
					} else
					{
						$("#zoomContainerSpan").removeClass('zoomInCursor');
						$("#zoomContainerSpan").addClass('zoomOutCursor');
						_imageZoomed = true;
					}
				}
			});

			$("#zoomLink").click(
					function()
					{
						openPopupZoomPhoto($("#imagePreview").attr("lang"), $(
								"#imagePreview").attr("title"), $(
								"#imagePreview").attr("rel"), 2, true);
					});
		});

function goToDisplay()
{
	jQuery("#zoomPhotoPartieGestion").hide();
	jQuery("#zoomPhotoPartieUpload").hide();
	jQuery("#zoomPhotoPartieAffichage").show();
	getDisplay();

}

function goToSetting()
{
	jQuery("#zoomPhotoPartieAffichage").hide();
	jQuery("#zoomPhotoPartieUpload").hide();
	jQuery("#zoomPhotoPartieGestion").show();
	getSetting();
}

function goToUpload()
{
	document.getElementById('test:uploadMsg').innerHTML = "";
	document.getElementById('test:uploadMsg').className = "";
	jQuery("#zoomPhotoPartieAffichage").hide();
	jQuery("#zoomPhotoPartieGestion").hide();
	jQuery("#zoomPhotoPartieUpload").show();
	getUpload();
}

function getUpload()
{
	jQuery('#test\\:inputHiddenPartId').val(_id);
	jQuery('#test\\:formPartId').submit();
}

function openPopupZoomPhoto(context, img, id, type, isMarkedImg)
{

	_context = context;
	_imageSelected = jQuery(img);
	_id = id;
	_type = type;
	_photoToUpload = 1;

	_imgSelectedWidth = _imageSelected.width();
	_imgSelectedHeight = _imageSelected.height();

	// init la taille de la derniere image cliquee
	if (_lastImageSelected != null && isMarkedImg == true)
	{

		_lastImageSelected.parents('td').last().removeClass('neoImgClicked');
		_lastImageSelected.parents('td').last().css('width', _imgSelectedWidth);
		_lastImageSelected.parents('td').last().css('height',
				_imgSelectedHeight);
	}

	_lastImageSelected = jQuery(_imageSelected);
	raffraichirPage();

	/* POSITIONNING OF THE PHOTO POP-UP */

	var scrollTop = 33 + $(window).scrollTop();
	if (/MSIE (\d+\.\d+);/.test(navigator.userAgent))
	{ // test for MSIE x.x;

		var ieversion = new Number(RegExp.$1) // capture x.x portion and store
		// as a number

		if (ieversion == 6)
		{
			if (scrollTop < 412)
			{
				hideSelect();
			}
		}
	}

	$("#divPopupZoomImage").show();
	$("#divPopupZoomImage").css(
	{
		'display' : 'block',
		'position' : 'absolute',
		'left' : '212px',
		'top' : scrollTop
	});

	cacheDiv();
	goToDisplay();

	if (isMarkedImg == true)
	{

		// PUT SELECTED IMAGE IN FOCUS
		_imageSelected.parents('td').last().css('width', _imgSelectedWidth - 6);
		_imageSelected.parents('td').last().css('height',
				_imgSelectedHeight - 6);
		_imageSelected.parents('td').last().addClass('neoImgClicked');
	}
}

function cacheDiv()
{
	jQuery("#cacheDiv").show();
}

function hideDiv(divName)
{
	jQuery(divName).hide();
	jQuery("#cacheDiv").hide();
}

function getSetting()
{
	resetSetting();

	var cacheBusting = "?bust=" + ((new Date()).getTime());
	if ((_image1 == '' || _image1 == 'null')
			&& (_image2 == '' || _image2 == 'null')
			&& (_image3 == '' || _image3 == 'null')
			&& (_flash == '' || _flash == 'null'))
	{
		jQuery("#messageRienASupprimer").show();
	} else
	{
		var listeImage = [ _image1, _image2, _image3 ];
		var j = 0;
		var domConstruction = "";
		for (i = 0; i < listeImage.length; i++)
		{
			if (j == 0)
			{
				domConstruction += "<div class='lignePhoto'>";
			}

			if (listeImage[i] != '' && listeImage[i] != 'null')
			{
				domConstruction += "<div id='photo"
						+ (i + 1)
						+ "' class='medium' >"
						+ "<img class='photoMedium' src='"
						+ _context
						+ listeImage[i]
						+ "?paramPhoto="
						+ _imageParam
						+ cacheBusting
						+ "' />"
						+ "<img class='buttonDelete' src='"
						+ _context
						+ "/themes/unitEstimator-theme-theme/images/export_wizard/bin.png' onclick='surToDelete("
						+ (i + 1) + ")' />" + "</div>";

				j++;

			}

		}

		if (_flash != '' && _flash != null)
		{
			if (j % 3 == 0)
			{
				domConstruction += "</div><div class='lignePhoto'>";
			}
			domConstruction += "<div id='photo4" + "' class='medium' ></div>";

		}

		domConstruction += "</div>";
		// CREATE DOM
		jQuery("#listePhotosMin").append(domConstruction);
		jQuery("#listePhotosMin").css("height", 210);
		// INSERT IF FLASH EXISTS
		if (_flash != '')
		{
			jQuery("#photo4").flash(
			{
				swf : _context + _flash,
				width : 160,
				height : 120
			});
			jQuery("#photo4")
					.append(
							"<img class='buttonDelete' src='"
									+ _context
									+ "/themes/unitEstimator-theme-theme/images/export_wizard/bin.png' onclick='surToDelete("
									+ (i + 1) + ")' />");

		}
	}

}

// THE FOLLOWING 3 FUNCTIONS DISPLAY ZOOM AND 3 MINIATURE ON IMAGE CLICK

function getDisplay()
{
	resetLargeImage2();

	var cacheBusting = "?bust=" + ((new Date()).getTime());

	if (_imageSelected.attr("src") != '' && _imageSelected.attr("lang") != '')
	{
		var NumberImageToDisplay = _imageToDisplay.substring(_imageToDisplay
				.lastIndexOf(".") - 1, _imageToDisplay.lastIndexOf("."));

		var aTag = "<img src='" + _context + _imageToDisplay
				+ "' width='720'/>";

		jQuery("#zoomContainerSpan").append(aTag);
		addZoomCursorsClasses($('#zoomContainerSpan'), _context
				+ _imageToDisplay);

		$('#zoomContainerSpan').zoom(
		{
			url : _context + _imageToDisplay,
			on : 'click'
		});
	}

	var listeImage = [ _image1, _image2, _image3 ];
	for (i = 0; i < 3; i++)
	{
		if (listeImage[i] != '' && listeImage[i] != 'null')
		{
			var imagePath = _context
					+ encodeURI(listeImage[i].substring(0,
							listeImage[i].length - 6)
							+ (i + 1)) + ".jpeg?paramPhoto=" + _imageParam
					+ cacheBusting;
			var imageMinPath = _context
					+ encodeURI(listeImage[i].substring(0,
							listeImage[i].length - 9)
							+ "MIN_000" + (i + 1)) + ".jpeg?paramPhoto="
					+ _imageParam + cacheBusting;
			jQuery("#miniature" + (i + 1)).append(
					"<img src='" + imageMinPath + "'"
							+ " class='photo_miniature'"
							+ " onmouseover=extendMiniature('" + imagePath
							+ "') />");
			if (NumberImageToDisplay == (i + 1))
			{
				jQuery("#input" + (i + 1)).append(
						creerButtonRadio((i + 1), true, _id, _context));
			} else
			{
				jQuery("#input" + (i + 1)).append(
						creerButtonRadio((i + 1), false, _id, _context));
			}

		} else
		{
			jQuery("#miniature" + (i + 1)).append(
					"<img src='" + _context
							+ "/images/no_photo.jpg' class='photo_miniature'"
							+ " />");
		}
	}

	if (_flash != null && _flash != '')
	{
		jQuery("#flash").append(
				"<img src='" + _context + "/images/flash.jpeg'"
						+ " class='photo_miniature'"
						+ " onmouseover=extendMiniatureForFlash('" + _context
						+ _flash + "')" + " onclick=recupererInformation(4,'"
						+ _context + "'," + _id + "); />");
	}

}

function creerButtonRadio(value, checked, idArticle, context)
{
	var radio = "<input type='radio'name='photoToDisplay' value='" + value
			+ "' onclick='changePhotoToDisplay(this)'";

	if (checked == true)
	{
		radio += " checked=checked";
	}

	radio += "  />";

	return radio;
}

// ON MOUSE OVER, CHANGE BIG IMAGE IN ZOOM MODULE

function extendMiniature(name)
{
	jQuery("#zoomContainerSpan").empty();
	jQuery("#messageFlash").hide();

	var aTag = "<img src='" + name + "' width='720'/>";

	jQuery("#zoomContainerSpan").append(aTag);
	addZoomCursorsClasses($('#zoomContainerSpan'), name);

	$('#zoomContainerSpan').zoom(
	{
		url : name,
		on : 'click'
	});
}

/*
 * Verify the with of the image, if the with is equal to 720px, then zoom cursors are not added
 */
function addZoomCursorsClasses(imageContainer, imageSrc)
{

	_imageZoomed = false;

	var temporaryImage = new Image();
	temporaryImage.src = imageSrc;
	temporaryImage.onload = function()
	{

		if (temporaryImage.width > 720)
		{
			imageContainer.removeClass('zoomOutCursor');
			imageContainer.addClass('zoomInCursor');
			_isImageLarge = true;
		} else
		{
			imageContainer.removeClass('zoomInCursor');
			imageContainer.removeClass('zoomOutCursor');
			_isImageLarge = false;
		}
	};

	return;
}

function menuCorrection()
{
	$(".neoNavMenu").css('margin-top', '-11px');
}

/*
 * This function activates the zoom funtionality(jquery.jqzoom-core.js) on the
 * image. When the cursor hovers the image, a window opens with a high
 * resolution image next to the original image. According to the cursor
 * movement the image is moving in the window.
 */
function callZoom()
{
	$(".zoomWrapper").css(
	{
		cursor : 'pointer'
	});
	$('.jqzoom').jqzoom(
	{
		zoomType : 'standard',
		lens : true,
		preloadImages : false,
		alwaysOn : false,
		zoomWidth : 720,
		zoomHeight : 304
	});
}

function extendMiniatureForFlash(name)
{
	jQuery("#zoomContainerSpan").empty();
	jQuery("#zoomContainerSpan").flash(
	{
		swf : name,
		width : 640,
		height : 480
	});
	jQuery("#messageFlash").show();
}

function changePhotoToDisplay(obj)
{
	var radio = jQuery(obj);
	var numero = radio.attr("value");
	var lang = _imageSelected.attr("lang");
	jQuery.ajax(
	{
		type : "POST",
		url : _context + "/changePhotoToDisplay",
		data : "action=1&numero=" + numero + "&id=" + _id,
		success : function()
		{
			var regExp = new RegExp("_000[1-3]\.jpeg");
			var srcMiniature = _imageSelected.attr("src");
			var oldImageToDisplay = _imageSelected.attr("lang");
			var newSrcMiniature = srcMiniature.replace(regExp, "_000" + numero
					+ ".jpeg");
			var newImageToDisplay = oldImageToDisplay.replace(regExp, "_000"
					+ numero + ".jpeg");
			raffraichirPage();
		}
	});
}

function surToDelete(numero)
{
	jQuery("#miniPopupToDelete").show();
	var deleteEvent = 'click.delete';
	jQuery("#buttonDeleteYes").unbind(deleteEvent).bind(deleteEvent, function()
	{
		deletePhoto(numero);
	});
}

function deletePhoto(numero)
{
	jQuery("#miniPopupToDelete").hide();
	jQuery.ajax(
	{
		type : "POST",
		url : _context + "/changePhotoToDisplay",
		data : "action=0&numero=" + numero + "&id=" + _id,
		success : function(photoToDisplay)
		{
			switch (numero)
			{
				case 1:
					_image1 = '';
					break;
				case 2:
					_image2 = '';
					break;
				case 3:
					_image3 = '';
					break;
				case 4:
					_flash = '';
					break;

				default:
					break;
			}

			raffraichirPage();
			getSetting();

		}
	});
}

function raffraichirPage()
{
	// force refresh of the image.
	// if the name didn't change, the browser won't re-download the image.
	var cacheBusting = "?bust=" + ((new Date()).getTime());
	jQuery.ajax(
	{
		type : "POST",
		async : false,
		dataType : "html",
		url : _context + "/changePhotoToDisplay",
		data : "action=2&id=" + _id,
		success : function(msg)
		{
			var listePhoto = msg.substring(3, msg.length - 4).split(";");
			var imageMinToDisplay = listePhoto[0];
			_imageToDisplay = listePhoto[1] + cacheBusting;
			_image1 = listePhoto[2];
			_image2 = listePhoto[3];
			_image3 = listePhoto[4];
			_flash = listePhoto[5];
			_imageParam = listePhoto[6];
			switch (_type)
			{
				case 1:
					if (imageMinToDisplay == '' || imageMinToDisplay == null)
					{
						imageMinToDisplay = '/images/no_photo.jpg';
						_imageToDisplay = '/images/no_photo.jpg';
					}
					_imageSelected.attr(
					{
						src : _context + imageMinToDisplay + cacheBusting,
						lang : _imageToDisplay + cacheBusting
					});

					break;
				case 2:
					if (_imageToDisplay == '' || _imageToDisplay == null)
					{
						_imageToDisplay = '/images/no_photo.jpg';
					}

					_imageSelected.attr(
					{
						src : _context + _imageToDisplay + cacheBusting,
						lang : _imageToDisplay + cacheBusting
					});

					var listeImage = [ _image1, _image2, _image3 ];
					for (i = 0; i < listeImage.length; i++)
					{
						if (listeImage[i] == '' || listeImage[i] == null)
						{
							jQuery(".image" + (i + 1)).empty();
						} else
						{
							jQuery(
									"#bigImage_produit_detail .image" + (i + 1)
											+ " img").attr(
									{
										src : _context
												+ listeImage[i].replace("_000"
														+ (i + 1) + ".jpeg",
														"_MIN_000" + (i + 1)
																+ ".jpeg")
												+ cacheBusting,
										lang : listeImage[i] + cacheBusting
									});
						}
					}

					break;
				case 3:
					var listeImage = [ _image1, _image2, _image3 ];
					for (i = 0; i < listeImage.length; i++)
					{
						if (listeImage[i] == '' || listeImage[i] == null)
						{
							jQuery(".image" + (i + 1)).attr(
							{
								src : _context + '/images/no_photo.jpg',
								lang : '/images/no_photo.jpg'
							});
						} else
						{
							jQuery(".image" + (i + 1)).attr(
							{
								src : _context + listeImage[i] + cacheBusting,
								lang : listeImage[i] + cacheBusting
							});
						}
					}

					break;
				case 4:
					if (imageMinToDisplay == '' || imageMinToDisplay == null)
					{
						imageMinToDisplay = '/images/no_photo.jpg';
						_imageToDisplay = '/images/no_photo.jpg';
					}
					_imageSelected.attr(
					{
						src : _context + _imageToDisplay + cacheBusting,
						lang : _imageToDisplay + cacheBusting
					});

					break;
				default:
					break;
			}

		}
	});
}

function resetLargeImage2()
{
	jQuery("#maxImage").empty();
	jQuery("#maxImage").hide();
	// jQuery("#divImageZoom").empty();
	jQuery("#zoomContainerSpan").empty();
	jQuery("#miniature1").empty();
	jQuery("#miniature2").empty();
	jQuery("#miniature3").empty();
	jQuery("#input1").empty();
	jQuery("#input2").empty();
	jQuery("#input3").empty();
	jQuery("#flash").empty();
	jQuery("#messageFlash").hide();
}

function resetSetting()
{
	jQuery("#listePhotosMin").empty();
	jQuery("#miniPopupToDelete").hide();
	jQuery("#messageRienASupprimer").hide();
}

function closePopup()
{
	jQuery("#divPopupZoomImage").hide();
	resetLargeImage2();
	resetSetting();
}

function agrandir_miniature(URI, img)
{
	var obj = jQuery(img);
	if (obj.attr("src") == '' || obj.attr("lang") == '')
	{
		return;
	}

	var name = obj.attr("lang");

	var largeImage = new Image();
	jQuery("#bigImage_produit_detail img").attr("src", URI + name);
	jQuery("#bigImage_produit_detail img").attr("lang", name);
}

function getLargeImage(URI, img, event, listSize)
{
	var obj = jQuery(img);
	if (obj.attr("lang") == '')
	{
		return;
	}
	var name = obj.attr("lang");
	var largeImage = new Image();
	jQuery(largeImage).attr("src", URI + name);
	jQuery(largeImage).css(
	{
		'padding' : '5px',
		'width' : '97%',
		'height' : '96%'
	});
	var tempX = 0;
	var tempY = 0;
	var cY = event.clientY;
	var cX = event.clientX;
	var scrollHeight = jQuery(document).scrollTop();
	var wWidth = document.body.offsetWidth;
	var filterPanelMarginLeft = $("#neoFilterPanel").css("margin-left");

	if (cY < 460)
	{
		tempX = wWidth / 3;
		tempY = cY + jQuery(document).scrollTop() + 30;

	} else
	{
		tempX = wWidth / 3;
		tempY = cY + jQuery(document).scrollTop() - 280;
	}

	if (filterPanelMarginLeft != '0px')
	{
		// filter panel toggle off
		tempX = tempX - 334;
	}

	jQuery("#maxImage").css(
	{
		'position' : 'absolute',
		'left' : tempX,
		'top' : tempY,
		'z-index' : '10'
	});
	jQuery("#maxImage").append(largeImage);
	jQuery("#maxImage").show();
}

function resetLargeImage()
{
	jQuery("#maxImage").empty();
	jQuery("#maxImage").hide();
}
/*
 * =Miniatures, Zoom and Flash (End)
 * -----------------------------------------------
 */

/*
 * function to select/deselect all check boxes in Dynamic Popup page boolean = true =>
 * select all else unselect all
 */
function check(clazz, status)
{
	jQuery(clazz + " input:checkbox").each(function()
	{
		// Check - uncheck only if input is not disabled
		if (!jQuery(this).is(':disabled'))
		{
			jQuery(this).prop('checked', status);
		}
	})
}

function checkAllCheckBoxesByClass(clazz)
{
	jQuery("input:checkbox.checkbox").each(function()
	{
		jQuery(this).prop('checked', true);
	})
}

function uncheckAllCheckBoxesByClass(clazz)
{
	jQuery("input:checkbox.checkbox").each(function()
	{
		jQuery(this).prop('checked', false);
	})
}

function checkOrUncheck(targetCheckboxClazz, callingCheckboxClazz)
{
	jQuery("input:checkbox" + targetCheckboxClazz).each(
			function()
			{
				// Check - uncheck only if input is not disabled
				if (!jQuery(this).attr('disabled'))
				{
					jQuery(this).attr('checked',
							jQuery(callingCheckboxClazz).is(':checked'));
				}
			})
}

/* to erase all FacesMessage content appeared when errors occurred */
function eraseAllMessages(theMsgClass)
{

	jQuery(theMsgClass).each(function()
	{
		jQuery(this).html('');
		jQuery(this).css('border', 'none');
		jQuery(this).css('background', 'none');
	});
}

/* Entry control */
function controle_saisie(objet, nb, form, event)
{
	var value = jQuery(objet).attr("value");
	if (nb == 4)
	{
		if (value == "1" || value == "2" || value == "3" || value == "4"
				|| value == "0" || value == "")
		{

		} else
		{
			jQuery(objet).attr("value", "0");
		}
	} else if (nb == 3)
	{
		if (value == "1" || value == "2" || value == "3" || value == "0"
				|| value == "")
		{
		} else
		{
			jQuery(objet).attr("value", "0");
		}
	}
}

function controle_saisie_suite(objet)
{
	var value = jQuery(objet).attr("value");
	if (value == "")
	{
		jQuery(objet).attr("value", "0");
	}
}

function isNumeric(value)
{
	if (value == '' || value == ' ')
	{
		return false;
	}
	if (value == 0 || value == 1 || value == 2 || value == 3 || value == 4
			|| value == 5 || value == 6 || value == 7 || value == 8
			|| value == 9)
	{
		return true;
	}
	return false;
}

function isNumericFlot(value)
{

	return !isNaN(parseFloat(value)) && isFinite(value);
}

function isPoint(value)
{
	if (value == '.' && value != '' && value != ' ')
	{
		return true;
	}
	return false;
}

function isNegativeSign(value)
{
	if (value == '-' && value != '' && value != ' ')
	{
		return true;
	}
	return false;
}

function controle_saisie_chiffre_point(objet)
{
	var numberValue = jQuery(objet).val();
	var originalValue = numberValue;
	// Suppress every character which is not a number or - or .
	var decimalNumberRegEx = /[^\d\-\.]/g;
	numberValue = numberValue.replace(decimalNumberRegEx, "");
	// Remove - characters in the middle of the value.
	numberValue = numberValue.replace(/(.+)-(.*)/, "\$1\$2");
	// Prevents multiple '.' characters if any. Ex:'5...3' -> '5.3' and '5.4.3'
	// -> '5.43'
	numberValue = numberValue.replace(/(\.)(?=\1)/g, "");
	numberValue = numberValue.replace(/(.+)\.+(.*)\./g, "\$1\.\$2");

	if (numberValue == "")
	{
		numberValue = "0.0";
	}
	if (numberValue != originalValue)
	{
		jQuery(objet).val(numberValue);
	}
}

function eraseIfNotNumber(objet)
{
	var numberValue = jQuery(objet).val();
	var originalValue = numberValue;
	// Suppress every character which is not a number or - or .
	var decimalNumberRegEx = /[^\d\-\.]/g;
	numberValue = numberValue.replace(decimalNumberRegEx, "");
	// Remove - characters in the middle of the value.
	numberValue = numberValue.replace(/(.+)-(.*)/, "\$1\$2");
	// Prevents multiple '.' characters if any. Ex:'5...3' -> '5.3' and '5.4.3'
	// -> '5.43'
	numberValue = numberValue.replace(/(\.)(?=\1)/g, "");
	numberValue = numberValue.replace(/(.+)\.+(.*)\./g, "\$1\.\$2");

	if (numberValue == "")
	{
		numberValue = "";
	}
	if (numberValue != originalValue)
	{
		jQuery(objet).val(numberValue);
	}
}

function getIntValue(component)
{
	var decimalNumberRegEx = /^-?\d*$/;
	var numberValue = component.value;
	var valueLength = numberValue.length;

	if (numberValue.search(decimalNumberRegEx) != -1)
	{
		component.value = numberValue;
	} else
	{
		component.value = numberValue.substring(0, valueLength - 1);
	}
}

function controle_saisie_chiffre_point_control_unconsistant_value(objet)
{
	var currentValue = jQuery(objet).val();
	var newValue = '';
	for (var i = 0; i < currentValue.length; i++)
	{
		var digit = currentValue.charAt(i);
		if (isNumeric(digit) || isPoint(digit) || isNegativeSign(digit))
		{
			newValue += digit;
		}
	}

	if (newValue != currentValue)
	{
		jQuery(objet).val('0');
	} else
	{
		if (isNumericFlot(newValue))
		{
			if (newValue < 100)
			{
				jQuery(objet).val(newValue);
			} else
			{
				jQuery(objet).val('10');
			}
		} else
		{
			jQuery(objet).val('0');
		}
	}

}

function controle_saisie_chiffre_point_control_ng(objet)
{
	var currentValue = jQuery(objet).val();
	var newValue = '';
	for (var i = 0; i < currentValue.length; i++)
	{
		var digit = currentValue.charAt(i);
		if (isNumeric(digit) || isPoint(digit) || isNegativeSign(digit))
		{
			newValue += digit;
		}
	}

	if (isNumericFlot(newValue))
	{
		jQuery(objet).val(newValue);
	} else
	{
		jQuery(objet).val('0');
	}
}

function controle_saisie_chiffre(objet)
{
	var currentValue = jQuery(objet).attr("value");
	var newValue = '';
	// loop on characters
	for (var i = 0; i < currentValue.length; i++)
	{
		var digit = currentValue.charAt(i);
		if (isNumeric(digit))
		{
			// if number or point
			newValue += digit;
		}
	}
	if (newValue != currentValue)
	{
		jQuery(objet).attr("value", newValue);
	}
}

function controle_saisie_chiffre_replace_with_zero(objet)
{
	var currentValue = jQuery(objet).attr("value");
	var newValue = '';
	// loop on characters
	for (var i = 0; i < currentValue.length; i++)
	{
		var digit = currentValue.charAt(i);
		if (i == 0 && !isNumeric(digit))
		{
			newValue = 0;
			break;
		} else
		{
			if (isNumeric(digit))
			{
				// if number or point
				newValue += digit;
			}
		}
	}
	if (newValue != currentValue)
	{
		jQuery(objet).attr("value", newValue);
	} else if (newValue == 0)
	{
		jQuery(objet).attr("value", newValue);
	}
}

function limite_taille_saisie(field, maxLength)
{
	if (field.value.length > maxLength)
	{
		field.value = field.value.substring(0, maxLength)
	}
}

function default_value_zero(objet)
{
	var currentValue = jQuery(objet).attr("value");
	if (currentValue == '')
	{
		jQuery(objet).attr("value", "0");
	}
}

function controlWeights()
{
	jQuery("input[id*='subFamilyCompetitorWeight']").each(
			function()
			{
				if ((jQuery(this).attr("value") == "" || jQuery(this).attr(
						"value") == "0")
						&& !jQuery(this).parent().parent().find(
								":checkbox[id*=competitorSelection]").attr(
								"checked"))
				{
					jQuery(this).attr("readOnly", "true");
					jQuery(this).removeClass("iceInpTxt");
					jQuery(this).addClass("iceInpTxt-dis");
				}
			});

	jQuery("input[id*='countryRuleCompetitorWeight']").each(
			function()
			{
				if ((jQuery(this).attr("value") == "" || jQuery(this).attr(
						"value") == "0")
						&& !jQuery(this).parent().parent().find(
								":checkbox[id*=competitorSelection]").attr(
								"checked"))
				{
					jQuery(this).attr("readOnly", "true");
					jQuery(this).removeClass("iceInpTxt");
					jQuery(this).addClass("iceInpTxt-dis");
				}
			});
}

function changeWeigth(checkbox)
{
	var input = jQuery(checkbox).parent().parent().find("input:text");
	if (!jQuery(checkbox).is(":checked"))
	{
		input.val("0").prop("readOnly", "true").removeClass("iceInpTxt")
				.addClass("iceInpTxt-dis");
	} else
	{
		input.removeProp("readOnly").removeClass("iceInpTxt-dis").addClass(
				"iceInpTxt");
	}
}

function reinitTabsCookie()
{
	jQuery.cookie('ui-tabs-1', null);
	jQuery.cookie('ui-tabs-2', null);
	jQuery.cookie('ui-tabs-3', null);
	jQuery.cookie('ui-tabs-4', null);
	jQuery.cookie('ui-tabs-5', null);
}

function choixWorkflow(statut)
{
	jQuery(".iceSelOneRb").find("input").each(function()
	{
		var val = this.value;
		if (val == statut)
		{
			this.checked = "checked";
		}
		if (val == statut)
		{
			this.checked = "checked";
		}
		if (val == statut)
		{
			this.checked = "checked";
		}
	});
}

function selectTabsFragment(index)
{
	var tab = document.getElementById('tabs');
	if (tab === null)
	{
		return;
	} else
	{
		$("#tabs").tabs(
		{
			selected : index
		});
	}
}

function getSelectedTabIndex()
{
	var tab = document.getElementById('tabs');
	if (tab === null)
	{
		return;
	} else
	{
		return $("#tabs").tabs('option', 'selected');
	}
}

function setChoosenTabIndex()
{
	var index = getSelectedTabIndex();
	if (typeof index === 'undefined')
	{
		return;
	} else
	{
		document
				.getElementById('localeSelectionForm:hiddenChoosenTabIndexInput').value = index;
	}
}

function reloadFilterMenu()
{
	jQuery("#tabs, #tabs2, #subtabs, #subtabs2, #subtabs3").tabs(
	{
		cookie :
		{
			expires : 0
		}
	});
	auroraLoad();
}

function initTable(tableClass)
{
	jQuery(tableClass).tableHover(
	{
		colClass : 'hover',
		cellClass : 'hovercell',
		clickClass : 'click'
	});
	jQuery(".neoImgClicked").removeClass("neoImgClicked");
}

function checkMaxSelectedCheckBoxConstraint(clazz, objet, nb)
{
	var checkNB = jQuery(clazz + " input:checked").length;
	if (checkNB > nb)
	{
		jQuery(objet).attr("checked", false);
		checkMaxSelectedConstraint.show();
	}
}

function clearSpace()
{
}

function eraseNotNumberValue(component)
{
	oldValue = component.defaultValue;
	currentValue = component.value;
	firstValidValue = "";
	pointFound = false;
	newValue = "";

	if ((component.value.trim() == ".") || (component.value.trim() == ""))
	{
		component.value = "0.0";
		component.defaultValue = component.value;
		return;
	}

	for (var i = 0; i < currentValue.length; i++)
	{
		currentChar = currentValue.charAt(i);
		if (!isNumeric(currentChar) && !isNegativeSign(currentChar)
				&& !isPoint(currentChar))
		{
			continue;
		}
		if (isNumeric(currentChar) && firstValidValue == "")
		{
			firstValidValue = currentChar;
		}
		if (isNegativeSign(currentChar) && firstValidValue == "")
		{
			firstValidValue = currentChar;
			newValue += firstValidValue;
		}
		if (!pointFound && isPoint(currentChar))
		{
			newValue += currentChar;
			pointFound = true;
		} else if (isNumeric(currentChar))
		{
			newValue += currentChar;
		}
		if (newValue.length > 1 && newValue.charAt(0) == "-"
				&& isPoint(newValue.charAt(1)))
		{
			newValue = "-";
			pointFound = false;
		}
	}
	if (newValue != currentValue)
	{
		component.value = oldValue;
	}
	component.defaultValue = component.value;
}

function controlIntegerValue(component)
{
	currentValue = component.value;
	var newValue = '';
	for (var i = 0; i < currentValue.length; i++)
	{
		var digit = currentValue.charAt(i);
		if (newValue == '' && isNegativeSign(digit))
		{
			newValue += digit;
		}
		if (isNumeric(digit))
		{
			newValue += digit;
		}
	}
	if (newValue != currentValue)
	{
		component.value = newValue;
	}
}

function eraseNotNumberValueSetZero(component)
{
	if (component.value != "-" && isNaN(component.value))
	{
		component.value = "0.0";
	}
}

function deleteDash(component)
{
	if (component.value == "-" || component.value == "")
	{
		component.value = "0.0";
	}
}

function deleteDashSetEmpty(component)
{
	if (component.value == "-" || component.value == ".")
	{
		component.value = "";
	}
}

function openCustomFeaturesPopup(partType)
{
	document.getElementById("featureForm:openCustomFeaturesPopup_" + partType)
			.click();
}

function invokCLick(idStartWith, idEndsWith)
{
	jQuery('input[id$="' + idEndsWith + '"][id^="' + idStartWith + '"]').each(
			function()
			{
				document.getElementById(this.id).click();
			});
}

function setDefaultValue(component, value)
{
	if (component.value == '' || component.value == '-')
	{
		component.value = value;
	}
}

function setMaxWidth(width)
{
	jQuery("#neoColRight").css("max-width", width);
}

function resetEmptyInputToZero(e, component)
{
	if (e.keyCode == 13)
	{

		if (component.value == null || component.value == "")
		{
			component.value = "0.0";
		}

		return false;
	} else
	{
		return true;
	}
}
var keypressed = null;
function treatEnterKeypressAsOnchange(event, component)
{
	keypressed = null;
	if (event.keyCode == 13)
	{
		event.preventDefault();
		component.onchange();
		return false;
	} else if (event.keyCode == 9)
	{
		keypressed = event.keyCode;
	}
}

function applyFocusToNextFocusableElement(event)
{
	if (undefined != event && 9 == keypressed)
	{
		var fields = jQuery(document).find('button,input,textarea,select,a');
		var index = fields.index(event.target);

		if (index > -1 && (index + 1) < fields.length)
		{
			var i = 0;
			for (i = index + 1; i < fields.length; i++)
			{

				/*
				 * Depending on the browser, if the id is undefined, =>
				 * undefined => 'undefined' or => false
				 */
				if (undefined != fields.eq(i).attr('id')
						&& fields.eq(i).attr('id') != 'undefined'
						&& fields.eq(i).attr('id') != false)
				{
					ice.applyFocus(fields.eq(i).attr('id'));
					break;
				}
			}
		}
	}
	keypressed = null;
}

/*
 * BLOCK UI ON SUBMIT
 */
var submitting = false;

function canSubmit()
{
	if (submitting)
	{
		return false;
	}

	submitting = true;
	return true;
}

function resetSubmitting()
{
	submitting = false;
}
/*
 * END BLOCK UI ON SUBMIT
 */

function wasTheEnterKeyPressed(event)
{
	if (event.keyCode == 13)
		return true; // The enter key was pressed
	else
		return false; // The enter key has not been pressed
}

function expand(show)
{
	var speed = 150;
	jQuery('.customauroramenu').each(
			function()
			{
				jQuery(this).children('li').children('ul').each(
						function()
						{
							if (show == 1)
							{
								jQuery(this).slideDown(speed);
								jQuery(this).siblings('a').attr('href',
										'javascript:expand(0);');
								jQuery(this).parent().children('.aurorahide')
										.css("display", "inline");
								jQuery(this).parent().children('.aurorashow')
										.css("display", "none");
							} else
							{
								jQuery(this).slideUp(speed);
								jQuery(this).siblings('a').attr('href',
										'javascript:expand(1);');
								jQuery(this).parent().children('.aurorahide')
										.css("display", "none");
								jQuery(this).parent().children('.aurorashow')
										.css("display", "inline");
							}
						});
			});
}

function chklen()
{
	var un = document.getElementById("featureForm:start").value
	var len = un.length;
	if (len > 20)
	{
		document.getElementById("featureForm:start").value = un
				.substring(0, 19); // remove excess characters
		return false;
	}

	var un1 = document.getElementById("featureForm:end").value
	var len1 = un1.length;
	if (len1 > 20)
	{
		document.getElementById("featureForm:end").value = un1.substring(0, 19); // remove
		// excess
		// characters
		return false;
	}
}

var clone;

function blurNavMenu()
{
	if (navigator.appVersion.indexOf("MSIE 7.") != -1)
	{
		$('.neoNavMenu ul').css('z-index', -1);
		$("#neoPartnerLogo").css('opacity', '0.2');

		clone = $("#neoHideFilterPanel").clone();
		$("#neoHideFilterPanel").remove();
	}
}

function sharpenNavMenu()
{
	if (navigator.appVersion.indexOf("MSIE 7.") != -1)
	{
		$('.neoNavMenu ul').css('z-index', '597');
		$("#neoPartnerLogo").css('opacity', '1.0');
		// V.1
		$("#neoFilterPanel").prepend(clone);
		$("#linkOff").css('opacity', '1.0');
	}
}

function setPopupMarginTopToMinusHeaderHeight()
{
	if (navigator.appVersion.indexOf("MSIE 7.") != -1)
	{
		var marginTop = $(window).scrollTop() - $("#neoHeader").height()
				+ parseFloat($("#neoHeader").css("margin-bottom"))
				+ parseFloat($("#neoHeader").css("margin-top"));
		$(".scrolledPopup167").css("top", marginTop);
	} else
	{
		var marginTop = $(window).scrollTop() - 167;
		$(".scrolledPopup167").css("top", marginTop);
	}
}

function emptyIfNoAuthor(context, component, actionId)
{
	if (component.value.trim() != "")
	{
		_context = context;
		jQuery.ajax(
		{
			type : "POST",
			url : _context + "/validateAuthor",
			async : false,
			dataType : "html",
			data : "action=" + actionId + "&author=" + component.value,
			success : function(resp)
			{
				var resultCode = resp.substring(3, 4);
				if (resultCode == "0")
				{
					component.value = "";

				}

			}
		});
	}

}

function emptyInvalidAuthor(authors, component)
{
	var resultCode = "0";
	if (component.value.trim() != "")
	{
		$.each(JSON.parse(authors), function(index, value)
		{

			if (value.name == component.value)
			{
				resultCode = "1";
			}

		});

		if (resultCode == "0")
		{
			component.value = "";
		}
	}

}

function adaptFileInputToIE()
{
	if (/* @cc_on!@ */false)
	{
		document.documentElement.className += ' ie10';
	}
	var popupWidth = $("div[id*='panelGroupPopupid']").width();
	if (popupWidth == 600)
	{
		$("div[id*='panelGroupPopupid']").children('table').each(function()
		{
			$(this).css('width', '600px');
		});
		$('.uploadLeftPanel').css('width', '100%');
	}

}

function disableDateTimePicker()
{
	$('.ui-datepicker-trigger').attr('disabled', 'disabled');
}

function removeTitleAttribute()
{
	document.getElementsByTagName("body")[0].removeAttribute("title");
}

function initializeImportWidth()
{
	var scheduledContainer = $('#scheduledRadioContainer').width();
	var importNameLabel = $('#importNameLabel').width();
	var fileUploadNote = $('#fileUploadNote').width();
	var widthToApply = Math.max(scheduledContainer, importNameLabel,
			fileUploadNote);

	/* for PicturesMassDeletion */
	var scheduledContainerDeletion = $('#scheduledRadioContainerDeletion')
			.width();
	var importNameLabelDeletion = $('#importNameLabelDeletion').width();
	var fileUploadNoteDeletion = $('#fileUploadNoteDeletion').width();
	var widthToApplyDeletion = Math.max(scheduledContainerDeletion,
			importNameLabelDeletion, fileUploadNoteDeletion);

	$('.adjustInputs').css("width", widthToApply);
	$('.adjustInputsDeletion').css("width", widthToApplyDeletion);
}

function adjustHeight()
{
	var outerHeight = 0;
	var numOfMsg = 0;
	$('.warningAttr').each(function()
	{
		outerHeight += $(this).outerHeight(true);
		numOfMsg += 1;
	});

	if (numOfMsg <= 8 && outerHeight < 400)
	{
		$("#table-scroll").css(
		{
			"width" : "620px",
			"margin-top" : "10px",
			"margin-bottom" : "10px",
			"height" : outerHeight + "px"
		});
	} else
	{
		$("#table-scroll").css(
		{
			"width" : "620px",
			"margin-top" : "10px",
			"margin-bottom" : "10px",
			"overflow-y" : "scroll",
			"overflow-x" : "scroll",
			"height" : "400px"
		});
	}
}

/*
 * Define whether to collapse or expand an item, show = 1 to expand show = 0 to
 * collapse
 */
function initAuroraItem(id, show)
{
	var a = $(id).attr("href")
	var b = '';
	if (typeof a != 'undefined')
	{
		for (i = 0; i < a.length; i++)
		{
			if ("" + parseInt(a[i]) != "NaN" || a[i] == ',')
				b = b + a[i];
		}

		try
		{
			if (typeof b.split(",")[0] !== 'undefined')
			{
				var num1 = b.split(",")[0].match(/\d+/);
				if (typeof b.split(",")[1] !== 'undefined')
				{
					var num2 = b.split(",")[1].match(/\d+/);
					auroraMenuItem(num1, num2, show);
				}
			}
		} catch (e)
		{
			console.log(e);
		}
	}
}

function showTable()
{
	jQuery("#neoColLeft").animate(
	{
		width : "698px"
	}, 300);

	jQuery("#neoColLeft").css(
	{
		'position' : 'absolute',
		'width' : '698px'
	});

	if ($.browser.msie && parseFloat($.browser.version) < 8)
	{
		jQuery("#neoColLeft").css(
		{
			'left' : '0'
		});
	}

	jQuery("#scrollTable").removeClass("collapseTable");
	jQuery("#scrollTable").addClass("expandTable");

	$("#neoShowPositioningPanel").hide();
	$("#neoHidePositioningPanel").show();

}

function addPositioningClass()
{
	if (jQuery("#neoColLeft").width() == 698)
	{

		if ($(".positioningTable").width() < 200)
		{
			resizePositioningTableToNormalSize();
		}

		jQuery("#scrollTable").removeClass("collapseTable");
		jQuery("#scrollTable").addClass("expandTable");
		$("#neoShowPositioningPanel").hide();
		$("#neoHidePositioningPanel").show();
	} else
	{
		jQuery("#scrollTable").removeClass("expandTable");
		jQuery("#scrollTable").addClass("collapseTable");
		$("#neoShowPositioningPanel").show();
		$("#neoHidePositioningPanel").hide();
	}
}

function hideShowPositioningPanel()
{
	jQuery("#neoShowPositioningPanel").click(function()
	{
		$("#neoSummaryTableContainer").removeClass("floatRight");
		$("#scrollTable").addClass("adjustScrollTable");
		showTable();
		controllingLeftMenuHeightOnExpand();
	});

	jQuery("#matrixTilteSummary")
			.click(
					function()
					{

						var numCols = ($("#pivotDetailsContainerIe7_5").find(
								'tr')[0].cells.length) / 2;

						if (numCols > 2)
						{
							$("#neoSummaryTableContainer").removeClass(
									"floatRight");
							$("#scrollTable").addClass("adjustScrollTable");
							showTable();
							controllingLeftMenuHeightOnExpand();
						}
					});

	jQuery("#neoHidePositioningPanel").click(function()
	{
		resizePositioningTableToNormalSize();
		$("#neoSummaryTableContainer").addClass("floatRight");
		$("#scrollTable").removeClass("adjustScrollTable");

	});
}

function resizePositioningTableToNormalSize()
{
	jQuery("#neoColLeft").animate(
	{
		width : "334px"
	}, 1);

	jQuery("#neoColLeft").css(
	{
		'width' : '334px'
	});

	$("#neoColLeft").css("position", "");
	$("#neoContentWrapperWithFilter").css("height", "auto");

	jQuery("#scrollTable").removeClass("expandTable");
	jQuery("#scrollTable").addClass("collapseTable");

	$("#neoShowPositioningPanel").show();
	$("#neoHidePositioningPanel").hide();
}

function applyCssForPopupDisplayOnIe7()
{
	if ($.browser.msie && parseFloat($.browser.version) < 8)
	{
		$("#neoNavMenu").addClass("popupDisplayOnIe7");
		$("#graphHeaderContainer").addClass("popupDisplayOnIe7");
		$("#pivotDetailsContainerIe7 > li").addClass("removeRelativePosition");
		$("#pivotDetailsContainerIe7_2 > li")
				.addClass("removeRelativePosition");
		$("#pivotDetailsContainerIe7_3 > li")
				.addClass("removeRelativePosition");
		$("#pivotDetailsContainerIe7_4 > li")
				.addClass("removeRelativePosition");
		$("#pivotDetailsContainerIe7_5").addClass("removeRelativePosition");
		$("#pivotDetailsContainerIe7_6").addClass("removeRelativePosition");
		$("#pivotDetailsContainerIe7_7").addClass("removeRelativePosition");
		$("#pivotDetailsContainerIe7_8").addClass("removeRelativePosition");
		$("#neoHideFilterPanel").addClass("removeRelativePosition");
	}
}
function removeCssAfterPopupCloseActionIe7()
{
	if ($.browser.msie && parseFloat($.browser.version) < 8)
	{
		$("#neoNavMenu").removeClass("popupDisplayOnIe7");
		$("#graphHeaderContainer").removeClass("popupDisplayOnIe7");
		$("#pivotDetailsContainerIe7 > li").removeClass(
				"removeRelativePosition");
		$("#pivotDetailsContainerIe7_2 > li").removeClass(
				"removeRelativePosition");
		$("#pivotDetailsContainerIe7_3 > li").removeClass(
				"removeRelativePosition");
		$("#pivotDetailsContainerIe7_4 > li").removeClass(
				"removeRelativePosition");
		$("#pivotDetailsContainerIe7_5").removeClass("removeRelativePosition");
		$("#pivotDetailsContainerIe7_6").removeClass("removeRelativePosition");
		$("#pivotDetailsContainerIe7_7").removeClass("removeRelativePosition");
		$("#pivotDetailsContainerIe7_8").removeClass("removeRelativePosition");
		$("#neoHideFilterPanel").removeClass("removeRelativePosition");

	}
}

function hideArrowHidePanelWhenPopupOpenedIe7()
{
	if ($.browser.msie && parseFloat($.browser.version) < 8)
	{
		$("#neoHideFilterPanel").addClass("arrowLeftMenuIe7");
	}
}

function displayArrowHidePanelWhenPopupClosedIe7()
{
	if ($.browser.msie && parseFloat($.browser.version) < 8)
	{
		$("#neoHideFilterPanel").removeClass("arrowLeftMenuIe7");
	}
}

/*
 * To fix firefox limitation bug on title attribute - code need to be improved
 */
function customiseTitleForFirefox()
{
	if (navigator.userAgent.toLowerCase().indexOf('firefox') > -1)
	{
		var titleVal = "";
		if ($("#matrixTilteSummary > a").html() != undefined)
		{
			titleVal = $("#matrixTilteSummary > a").html();
		} else if ($("#matrixTilteSummary > span").html() != undefined)
		{
			titleVal = $("#matrixTilteSummary > span").html();
		}
		if (titleVal.length > 79)
		{
			var output = titleVal.substr(0, 79) + " " + titleVal.substr(79);
			$("#matrixTilteSummary > span").removeAttr("title");
			$("#matrixTilteSummary > span").attr("title", function()
			{
				return output;
			});
			$("#matrixTilteSummary > a").removeAttr("title");
			$("#matrixTilteSummary > a").attr("title", function()
			{
				return output;
			});
		}
	}
}

function controllingLeftMenuHeightOnExpand()
{
	var centralContainerHeight;
	if ($("#neoColLeft").css("position") == "absolute"
			&& $("#neoColLeft").height() > $("#neoContentWrapperWithFilter")
					.height()
			&& $("#neoColLeft").height() > $("#neoColRight").height())
	{
		centralContainerHeight = $("#neoColLeft").height() + 30;
		$("#neoContentWrapperWithFilter").css("height", centralContainerHeight);
	}
}