<!DOCTYPE html>
<%@page import="org.osmsurround.ae.filter.FilterManager;"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="wt" uri="http://www.grundid.de/webtools"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" buffer="128kb"%>
<html>
<head>
<title>Amenity Editor for OpenStreetMap</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="description" lang="en" content="Simple online editing tool for amenities/POIs in the OpenStreetMap.">
<meta name="keywords" lang="en" content="osm, openstreetmap, amenity, editor, poi, pois, form">
<meta name="description" lang="de"
	content="Einfaches Online Werkzeug um Amenity/POIs Knoten in OpenStreetMap zu bearbeiten.">
<meta name="keywords" lang="de" content="osm, openstreetmap, amenity, editor, poi, pois, formular">
<meta name="author" content="GrundID GmbH, www.grundid-gmbh.de">

<link rel="StyleSheet" type="text/css" href="<wt:ue>/stylesheet.css</wt:ue>" media="screen" />
<link rel="StyleSheet" type="text/css" href="<wt:ue>/js/chosen.css</wt:ue>" media="screen" />
<link rel="icon" type="image/png" href="favicon.png" />
<script src="<wt:ue>/OpenLayers.js</wt:ue>" type="text/javascript"></script>
<script src="http://openstreetmap.org/openlayers/OpenStreetMap.js" type="text/javascript"></script>
<script src="<wt:ue>/js/prototype-1.7.1.js</wt:ue>" type="text/javascript"></script>
<script src="<wt:ue>/js/scriptaculous.js?load=effects,controls</wt:ue>" type="text/javascript"></script>
<script src="<wt:ue>/js/ae.js</wt:ue>" type="text/javascript"></script>
<script src="<wt:ue>/js/chosen.proto.js</wt:ue>" type="text/javascript"></script>
<script src="<wt:ue>/js/accordion.js</wt:ue>" type="text/javascript"></script>

<script type="text/javascript">
		var MSG = {
			buttonStopCreating : "<spring:message code="button.stop.creating" />",
			buttonCreateNode : "<spring:message code="button.create.node" />", 
			templateInfo : "<spring:message code="template.info" />",  
			ebOsmButton : "<spring:message code="eb.osm.button" />", 
			ebOsmButtonTitle : "<spring:message code="eb.osm.button.label" />", 
			ebMoveButton : "<spring:message code="eb.move.button" />", 
			ebDeleteButton : "<spring:message code="eb.delete.button" />", 
			ebSaveButton : "<spring:message code="eb.save.button" />", 
			ebCloseButton : "<spring:message code="eb.close.button" />",
			ebNewNode : "<spring:message code="eb.newnode" />",
			ebNodeName : "<spring:message code="eb.nodename" />",
			linkShowWiki : "<spring:message code="link.show_wiki" />",
			comboSelectOrType : "<spring:message code="combo.select_or_type" />",
			checkTrue : "<spring:message code="check.true" />",
			checkFalse : "<spring:message code="check.false" />",
			alertNoBase : "<spring:message code="alert.no.base" />",
			alertAcceptLicense : "<spring:message code="alert.acceptlicense" />",
			noHighwayEdit : "<spring:message code="no.highway.edit" />",
			saveActionInfo : "<spring:message code="save.action.info" />",
			confirmDelete : "<spring:message code="confirm.delete" />"
		};

		var URL = {
			search : "<wt:ue>/ae/search</wt:ue>",
			amenity : "<wt:ue>/ae/amenity</wt:ue>",
			acKey : "<wt:ue>/ae/ac/key</wt:ue>",
			acValue: "<wt:ue>/ae/ac/value</wt:ue>",
			osmUpdate : "<wt:ue>/ae/osmUpdate</wt:ue>",
			templates : "<wt:ue>/ae/templates</wt:ue>"
		};

		var PARAM = {
			startLongitude: ${startParameters.geoLocation.longitude},
			startLatitude: ${startParameters.geoLocation.latitude},
			startZoom: ${startParameters.zoom},
			permalink: <c:if test="${!startParameters.permalink}">true</c:if><c:if test="${startParameters.permalink}">false</c:if>,
		};

		var contextPath = '<wt:ue ignoreServletName="true"></wt:ue>';

		var oauthTokensAvailable = false;
		<c:if test="${startParameters.oauthTokenAvailable}">oauthTokensAvailable = true;</c:if>
</script>
<script src="<wt:ue>/js/map.js</wt:ue>" type="text/javascript"></script>
</head>

<body>
	<div id="content" style="overflow: hidden; height: 100%">
		<div
			style="padding: 5px; height: 32px; position: absolute; left: 60px; top: 0px; right: 50px; z-index: 750; -moz-border-radius: 0px 0px 6px 6px; border-radius: 0px 0px 6px 6px; background-color: #FFFFFF; background-repeat: repeat-x; border: 1px solid #000000; border-top: none">
			<div style="font-weight: bold; font-size: 14px;">OSM Amenity Editor (${startParameters.version})</div>
			<div style="position: relative; bottom: -5px; left: 0px; font-size: 9px; font-weight: normal">
				<spring:message code="info.credit" />
				,
				<spring:message code="info.contact" />
			</div>
			<div style="position: absolute; right: 5px; top: 5px; width: 900px; text-align: right">
				<c:if test="${startParameters.oauthTokenAvailable}">
					<span style="padding: 2px; background-color: #ffe7cd; font-size: 14px">OAuth OK!</span>
				</c:if>
				<input type="button" class="ae-small-button" value="<spring:message code="button.max.zoom" />"
					title="<spring:message code="button.max.zoom.hint" />" onclick="setMaxZoom();" />
				<input type="button" class="ae-small-button" value="<spring:message code="button.home.base" />"
					title="<spring:message code="button.home.base.hint" />" onclick="goToHomeBase();" />
				<select id="ae-template-select" class="ae-small-button" onchange="loadTemplate();">
					<c:set var="templates">
						<spring:eval expression="@propertyConfigurer['templates']" />
					</c:set>
					<c:forEach items="${fn:split(templates, ',')}" var="preset">
						<option value="${preset}">${preset}</option>
					</c:forEach>
				</select>
				<input type="button" class="ae-small-button" value="<spring:message code="button.create.node" />" id="newNodeButton"
					title="<spring:message code="button.create.node.hint" />" onclick="switchAdding();" />
				<input type="button" class="ae-small-button" value="<spring:message code="button.rod" />" id="loadOsmDataButton"
					title="<spring:message code="button.rod.hint" />" onclick="loadBbox();" />
				<input type="button" class="ae-small-button" value="<spring:message code="button.settings" />"
					title="<spring:message code="button.settings.hint" />" onclick="showFilterSettings();">
				<input type="button" class="ae-small-button" value="<spring:message code="button.help" />"
					title="<spring:message code="button.help.hint" />" onclick="$('help').toggle();">
				<input type="button" class="ae-small-button" value="<spring:message code="button.oauth" />"
					title="<spring:message code="button.oauth.hint" />"
					onclick="window.location.href='<wt:ue>/ae/oauthRequest</wt:ue>';" />
			</div>
		</div>
		<div style="position: absolute; left: 80px; top: 55px; right: 60px; z-index: 1000;">
			<center>
				<div class="infobox" style="width: 800px; display: none; text-align: left" id="filter">
					<form id="filterform">
						<fieldset style="width: 520px; float: left">
							<legend>
								<spring:message code="settings.show" />
							</legend>
							<c:forEach items="<%=FilterManager.getInstance().getFiltersPositive()%>" var="filter">
								<div style="width: 170px; float: left;">
									<input type="checkbox" class="filter-positive-cb" name="show" value="${filter.name}"
										<c:if test="${filter.defaultSelected}">checked="checked" </c:if> id="cb_${filter.name}" />
									&nbsp;
									<label for="cb_${filter.name}">
										<spring:message code="filter.${filter.name}" />
										&nbsp;
										<img src="<wt:ue>/img/${filter.icon}</wt:ue>" width="12" height="12">
									</label>
								</div>
							</c:forEach>
							<div style="clear: both">
								<input type="button" class="ae-small-button" value="<spring:message code="button.select.all" />"
									onclick="selectAll();" />
								<input type="button" class="ae-small-button" value="<spring:message code="button.deselect.all" />"
									onclick="deselectAll();" />
								<input type="button" class="ae-small-button" value="<spring:message code="button.invert.selection" />"
									onclick="invertSelection();" />
							</div>
						</fieldset>
						<fieldset style="width: 240px">
							<legend>
								<spring:message code="settings.hide" />
							</legend>
							<c:forEach items="<%=FilterManager.getInstance().getFiltersNegative()%>" var="filter">
								<input type="checkbox" name="hide" value="${filter.name}" id="cb_${filter.name}" />&nbsp;
<label for="cb_${filter.name}">
									<spring:message code="filter.${filter.name}" />
								</label>
								<br />
							</c:forEach>
						</fieldset>
						<fieldset style="clear: both">
							<legend>
								<spring:message code="settings.userpass" />
							</legend>
							<div>
								<spring:message code="settings.noaccount" />
							</div>
						</fieldset>
						<fieldset>
							<legend>
								<spring:message code="settings.base" />
							</legend>
							<div style="height: 22px; position: relative;">
								<span style="vertical-align: middle;">
									<spring:message code="settings.lon" />
									:&nbsp;
								</span>
								<input type="text" name="lon" size="12" style="width: 100px;">
								<span style="vertical-align: middle;">
									<spring:message code="settings.lat" />
									:&nbsp;
								</span>
								<input type="text" name="lat" size="12" style="width: 100px;">
							</div>
							<input type="button" class="ae-small-button" value="<spring:message code="settings.button.center" />"
								onclick="setMapCenterAsHomeBase();">
						</fieldset>
						<fieldset>
							<legend>
								<spring:message code="settings.cookies" />
							</legend>
							<input type="checkbox" name="saveincookie" value="1" id="cb_saveincookie" />
							&nbsp;
							<label for="cb_saveincookie">
								<spring:message code="settings.saveincookie" />
							</label>
						</fieldset>
					</form>
					<p>
						<input type="button" class="ae-small-button" value="<spring:message code="settings.button.apply" />"
							onclick="saveFilterSettings();">
						<input type="button" class="ae-small-button" value="<spring:message code="settings.button.close" />"
							onclick="$('filter').hide();">
				</div>
				<%@ include file="includes/infobox.jspf"%>
				<div class="infobox" style="width: 250px; display: none" id="feamenities">
					<spring:message code="info.fewamenities" />
				</div>
				<div class="infobox" style="width: 350px; display: none" id="moving">
					<spring:message code="move.info" />
					<br />
					<input type="button" class="ae-small-button" value="<spring:message code="move.button" />"
						title="<spring:message code="move.button.hint" />" onclick="cancelMoving()">
				</div>
				<div class="infobox" style="width: 170px; display: none" id="loading">
					<spring:message code="status.loading.data" />
					<br>
					<img src="<wt:ue>/img/throbber.gif</wt:ue>">
				</div>
				<div class="infobox" style="width: 170px; display: none" id="storing">
					<spring:message code="status.saving.data" />
					<br>
					<img src="<wt:ue>/img/throbber.gif</wt:ue>">
				</div>
				<div class="infobox" style="width: 400px; display: none" id="zoomStatus">
					<spring:message code="status.zoom.to.small" />
					<br>
					<input type="button" class="ae-small-button" value="<spring:message code="button.adujst.zoom" />"
						title="<spring:message code="button.adujst.zoom.hint" />" onclick="map.zoomTo(MIN_ZOOM_FOR_EDIT+1)">
				</div>
			</center>
		</div>
		<div style="height: 100%" id="map"></div>
	</div>
	<noscript>
		<spring:message code="no.javascript" />
	</noscript>
</body>
</html>