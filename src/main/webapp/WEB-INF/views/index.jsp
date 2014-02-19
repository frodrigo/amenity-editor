<!DOCTYPE html>
<%@page import="org.osmsurround.ae.filter.FilterManager;"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="wt" uri="http://www.grundid.de/webtools" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" buffer="128kb" %>
<html>
<head>
<title>Amenity Editor for OpenStreetMap</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="description" lang="en" content="Simple online editing tool for amenities/POIs in the OpenStreetMap.">
<meta name="keywords" lang="en" content="osm, openstreetmap, amenity, editor, poi, pois, form">
<meta name="description" lang="de" content="Einfaches Online Werkzeug um Amenity/POIs Knoten in OpenStreetMap zu bearbeiten.">
<meta name="keywords" lang="de" content="osm, openstreetmap, amenity, editor, poi, pois, formular">
<meta name="author" content="GrundID GmbH, www.grundid-gmbh.de">

<link rel="StyleSheet" type="text/css" href="<wt:ue>/stylesheet.css</wt:ue>"
	media="screen" />
<link rel="StyleSheet" type="text/css" href="<wt:ue>/js/chosen.css</wt:ue>"
	media="screen" />
<link rel="icon" type="image/png" href="favicon.png" />
<script src="<wt:ue>/OpenLayers.js</wt:ue>" type="text/javascript"></script>
<script src="http://openstreetmap.org/openlayers/OpenStreetMap.js"
	type="text/javascript"></script>
<script src="<wt:ue>/js/prototype-1.7.1.js</wt:ue>" type="text/javascript"></script>
<script src="<wt:ue>/js/scriptaculous.js?load=effects,controls</wt:ue>" type="text/javascript"></script>
<script src="<wt:ue>/js/ae.js</wt:ue>" type="text/javascript"></script>
<script src="<wt:ue>/js/chosen.proto.js</wt:ue>" type="text/javascript"></script>
<script src="<wt:ue>/js/accordion.js</wt:ue>" type="text/javascript"></script>

<script type="text/javascript">

		var MSG = {
			buttonStopCreating : '<spring:message code="button.stop.creating" />',
			buttonCreateNode : '<spring:message code="button.create.node" />', 
			templateInfo : '<spring:message code="template.info" />',  
			ebOsmButton : '<spring:message code="eb.osm.button" />', 
			ebOsmButtonTitle : '<spring:message code="eb.osm.button.label" />', 
			ebMoveButton : '<spring:message code="eb.move.button" />', 
			ebDeleteButton : '<spring:message code="eb.delete.button" />', 
			ebSaveButton : '<spring:message code="eb.save.button" />', 
			ebCloseButton : '<spring:message code="eb.close.button" />'
		};

		var URL = {
			search : "<wt:ue>/ae/search</wt:ue>",
			amenity : "<wt:ue>/ae/amenity</wt:ue>",
			acKey : "<wt:ue>/ae/ac/key</wt:ue>",
			acValue: "<wt:ue>/ae/ac/value</wt:ue>",
			osmUpdate : "<wt:ue>/ae/osmUpdate</wt:ue>",
			templates : "<wt:ue>/ae/templates</wt:ue>"
		};

		var keyValueTemplates = {

		};
		
        var wizardData = { 

		};

        var map;
        var layerMapnik = new OpenLayers.Layer.OSM.Mapnik("Mapnik");

		var idCounter = 0;
		var addNewNode = false;
		var movingNode = false;	
		var MIN_ZOOM_FOR_EDIT = 16;
		var MAX_ZOOM = 18;
		var nextFocus = false;

		var contextPath = '<wt:ue ignoreServletName="true"></wt:ue>';

		var oauthTokensAvailable = false;
		<c:if test="${startParameters.oauthTokenAvailable}">oauthTokensAvailable = true;</c:if>


        OpenLayers.Control.Click = OpenLayers.Class(OpenLayers.Control, {
            defaultHandlerOptions: {
                'single': true,
                'double': false,
                'pixelTolerance': 0,
                'stopSingle': false,
                'stopDouble': false
            },
            initialize: function(options) {
                this.handlerOptions = OpenLayers.Util.extend(
                    {}, this.defaultHandlerOptions
                );
                OpenLayers.Control.prototype.initialize.apply(
                    this, arguments
                );
                this.handler = new OpenLayers.Handler.Click(
                    this, {
                        'click': this.trigger
                    }, this.handlerOptions
                );
            },
            trigger: function(e) {
                try
                {
					if (addNewNode)
					{
						AE.removeNewAmenityFeature();
						var lonlat = map.getLonLatFromViewPortPx(e.xy);
						
						var amenity = new Amenity(x2lon(lonlat.lon),y2lat(lonlat.lat));
						
						var feature = AE.createFeature(amenity);
						feature.popupClicked = true;
						AE.layerNew.addMarker(feature.marker);
						AE.newAmenityFeature = feature;
					}
					if (AE.isMoving())
					{
						AE.removeNewAmenityFeature();
						var lonlat = map.getLonLatFromViewPortPx(e.xy);

						AE.movingAmenity.newLon = x2lon(lonlat.lon);
						AE.movingAmenity.newLat = y2lat(lonlat.lat); 

						var feature = AE.createFeature(AE.movingAmenity,true);
						feature.popupClicked = true;
						AE.layerNew.addMarker(feature.marker);
						AE.newAmenityFeature = feature;
					}
				}
				catch (e)
				{
					alert(e);
				}
              }
              
            
          });
		

        function switchAdding()
        {
        	addNewNode = !addNewNode;
        	$("newNodeButton").value = addNewNode ? MSG.buttonStopCreating : MSG.buttonCreateNode; 
        	if (!addNewNode)
        	{
        		AE.removeNewAmenityFeature();
        	}

        }

        function Amenity(lon,lat)
        {
            this.osmType = 'n';
            this.nodeId = 0;
            this.lon = lon;
            this.lat = lat;
			this.keyValues = new Object();
			this.keyValues["amenity"] = "";
        }

        function plusfacteur(a) { return a * (20037508.34 / 180); }
        function moinsfacteur(a) { return a / (20037508.34 / 180); }
        function y2lat(a) { return 180/Math.PI * (2 * Math.atan(Math.exp(moinsfacteur(a)*Math.PI/180)) - Math.PI/2); }
        function lat2y(a) { return plusfacteur(180/Math.PI * Math.log(Math.tan(Math.PI/4+a*(Math.PI/180)/2))); }
        function x2lon(a) { return moinsfacteur(a); }
        function lon2x(a) { return plusfacteur(a); }
        function lonLatToMercator(ll) {
          return new OpenLayers.LonLat(lon2x(ll.lon), lat2y(ll.lat));
        }
        
        function loadTemplate()
        {
			new Ajax.Request(URL.templates+"?template="+$('ae-template-select').value, {
				method: 'get', 
					onSuccess: function(transport) {
						var jsonData = transport.responseJSON;
			
						keyValueTemplates = jsonData.keyValueTemplates;
						wizardData = jsonData.wizardData;  		  	
					}
			});
		}

        function init()
        {
			loadTemplate();

	        	map = new OpenLayers.Map('map', {
	                    maxExtent: new OpenLayers.Bounds(-20037508,-20037508,20037508,20037508),
	                    restrictedExtent: new OpenLayers.Bounds(-20037508,-20037508,20037508,20037508),
	                      numZoomLevels: 18,
	                      displayProjection: new OpenLayers.Projection("EPSG:4326"),
	                      units: 'm'
	                     });
	
	            AE.init(map, contextPath);
	            layerMapnik.attribution = null;
	            map.addLayers([layerMapnik, AE.layerNew, AE.layerMarkers]);
	            map.addControl(new OpenLayers.Control.LayerSwitcher());
	            map.addControl(new OpenLayers.Control.ScaleLine());
	            map.addControl(new OpenLayers.Control.Permalink());
	            map.addControl(new OpenLayers.Control.MousePosition());
	            var panel = new OpenLayers.Control.Panel();

	            map.setCenter(lonLatToMercator(new OpenLayers.LonLat(${startParameters.geoLocation.longitude},${startParameters.geoLocation.latitude})), ${startParameters.zoom}); 
	            map.events.register('moveend', map, updateAmenities);
	            map.events.register('zoomend', map, updateZoomStatus);
	
	            var click = new OpenLayers.Control.Click();
	            map.addControl(click);
	            click.activate();
	
	            loadSettingsFromCookie();
	            updateZoomStatus();
	            updateAmenities(null);
        }

        function autoCompleteCallBack(inputField, queryString)
        {
            var prev = inputField.previousSiblings();
            var keyValue = "";
            for (var x = 0; x < prev.length;x++)
            {
				var elem = prev[x];
				if (elem.tagName == "INPUT")
				{
					keyValue = elem.value;
					break;
				}
					
            }
			return queryString+"&key="+keyValue;
        }

        var extWest = 0;
        var extEast = 0;
        var extNorth = 0;
        var extSouth = 0;        

        function updateAmenities(event,url,forceUpdate) 
        {
			if (AE.isMoving())
				return;
			
        	url = url || URL.search;
			var coords = map.getCenter();
			var lon = x2lon(coords.lon);
			var lat = y2lat(coords.lat);
			var zoom = map.getZoom();
			if (zoom > MIN_ZOOM_FOR_EDIT)
			{
			
				var bounds = map.getExtent().toArray();
				var south = y2lat(bounds[1]);
				var north = y2lat(bounds[3]);
				var west = x2lon(bounds[0]);
				var east = x2lon(bounds[2]);

				if (forceUpdate || (extWest > west || extEast < east || extSouth > south || extNorth < north) || (url != URL.search))
				{
					south = extSouth = (Math.floor(south*100)/100)-0.001;
					north = extNorth = (Math.ceil(north*100)/100)+0.001;
					west = extWest = (Math.floor(west*100)/100)-0.001;
					east = extEast = (Math.ceil(east*100)/100)+0.001;
					
					var params = $("filterform").serialize(true);
					params["north"] = north;
					params["south"] = south;
					params["west"] = west;
					params["east"] = east;
					delete params["saveincookie"];
					
					$("loading").show();

					new Ajax.Request(url, {
				  		  method: 'get', parameters : params, 
				  		  onSuccess: function(transport) {
				  		  	var jsonData = transport.responseJSON;
				  		  	if (jsonData.message)
				  		  	{
					  		  	$("loading").hide();					  		  	
					  		  	alert(jsonData.message);
				  		  	}
				  		  	else
								AE.refreshAmenities(jsonData);
				  		  }
				  		});
				}
			}
		}

        function createKeyValues(osmType, nodeId, formTag, amenity, tags, create)
        {
        	var treadedTags = [];
			for (var i = 0; i < tags.length; i++)
			{
				var object = tags[i].object;
				switch (tags[i].type)
				{
					case "Space":
						formTag.insert(new Element("br"));
						break;
					case "Link":
						formTag.insert(createLinkIcon("anchor.png",object.href,"Show Wiki"));
						break;
					case "Text":
						if (create || object.key == "" || amenity.keyValues[object.key] == null)
						{
							amenity.keyValues[object.key] = amenity.keyValues[object.key] || object.default || "";
							formTag.insert(createViewText(osmType, nodeId, object, amenity.keyValues[object.key]));
							treadedTags.push(object.key);
						}
						break;
					case "Combo":
						if (create || amenity.keyValues[object.key] == null)
						{
							amenity.keyValues[object.key] = amenity.keyValues[object.key] || object.default || "";
							formTag.insert(createViewCombo(osmType, nodeId, object, amenity.keyValues[object.key]));
							treadedTags.push(object.key);
						}
						break;
					case "Multiselect":
						if (create || amenity.keyValues[object.key] == null)
						{
							amenity.keyValues[object.key] = amenity.keyValues[object.key] || object.default || "";
							formTag.insert(createViewMultiselect(osmType, nodeId, object, amenity.keyValues[object.key]));
							treadedTags.push(object.key);
						}
						break;
					case "Checkgroup":
						treadedTags = treadedTags.concat(createKeyValues(osmType, nodeId, formTag, amenity, object.tags, create));
						break;
					case "Check":
						if (create || amenity.keyValues[object.key] == null)
						{
							if (!amenity.keyValues[object.key])
							{
								if (object.default)
								{
									amenity.keyValues[object.key] = object.default == "ON" ? "true" : "false";
								} else {
									amenity.keyValues[object.key] = "";
								}
							}
							formTag.insert(createViewCheck(osmType, nodeId, object, amenity.keyValues[object.key]));
							treadedTags.push(object.key);
						}
						break;
					case "Separator":
						formTag.insert(new Element("hr"));
						break;
					case "Reference":
						var ref = keyValueTemplates[object.ref];
						if (!ref)
						{
							formTag.insert(new Element("div").update("Missing ref "+object.ref));
						} else {
							treadedTags = treadedTags.concat(createKeyValues(osmType, nodeId, formTag, amenity, ref.tags, create));
						}
						break;
					case "Key":
						if (create || amenity.keyValues[object.key] == null)
						{
							amenity.keyValues[object.key] = amenity.keyValues[object.key] || object.value;
							formTag.insert(createViewKey(osmType, nodeId, object, amenity.keyValues[object.key]));
							treadedTags.push(object.key);
						}
						break;
					case "Optional":
						var fieldset = new Element("fieldset");
						fieldset.insert(new Element("legend").update(object.text));
						treadedTags = treadedTags.concat(createKeyValues(osmType, nodeId, fieldset, amenity, object.tags, create));
						formTag.insert(fieldset);
						break;
				}
			}
			return treadedTags;
        }

		function fetchViewKey(tags, collect)
		{
			for (var i = 0; i < tags.length; i++)
			{
				var object = tags[i].object;
				switch (tags[i].type)
				{
					case "Reference":
						var ref = keyValueTemplates[object.ref];
						if (ref)
						{
							collect = collect.concat(fetchViewKey(ref.tags, []));
						}
						break;
					case "Key":
						collect.push([object.key, object.value]);
						break;
					case "Optional":
						collect = collect.concat(fetchViewKey(object.tags, []));
						break;
				}
			}
			return collect
        }

		function fetchView(amenity, groupData)
		{
           	var osmType = amenity.osmType;
			for (var i=0; i<groupData.length; i++)
			{
				var object = groupData[i].object;
				switch (groupData[i].type)
				{
					case "Group":
						var ret = fetchView(amenity, object.tags);
						if(ret)
						{
							return ret;
						}
						break;
					case "Item":
						var types = object.type && object.type.split(',');
						if (!types ||
							(amenity.osmType == 'n' && types.indexOf('node') > -1) ||
							(amenity.osmType == 'w' && (types.indexOf('way') > -1 || types.indexOf('closedway') > -1)) ||
							(amenity.osmType == 'r' && types.indexOf('relation') > -1))
						{
							var keys = fetchViewKey(object.tags, []);
							if(keys.length > 0)
							{
								var filtered = keys.filter(function (k) {
									return amenity.keyValues[k[0]] && amenity.keyValues[k[0]] == k[1];
								});
								if(filtered.length == keys.length)
								{
									return object;
								}
							}
						}
						break;
				}
			}
		}

        function createKeyValueTable(amenity, views)
        {
            var osmType = amenity.osmType;
            var nodeId = amenity.nodeId;

            var formTag = new Element("form",{"id":"form_"+osmType+nodeId,"action":URL.amenity,"method":"post"});
            formTag.insert(new Element("input",{"type":"hidden","name":"_osmType","value":osmType}));
            formTag.insert(new Element("input",{"type":"hidden","name":"_nodeId","value":nodeId}));
            formTag.insert(new Element("input",{"type":"hidden","name":"lon","value":amenity.lon}));
            formTag.insert(new Element("input",{"type":"hidden","name":"lat","value":amenity.lat}));
            if (amenity.newLon && amenity.newLat)
            {
                formTag.insert(new Element("input",{"type":"hidden","name":"newlon","value":amenity.newLon}));
	            formTag.insert(new Element("input",{"type":"hidden","name":"newlat","value":amenity.newLat}));
            }   
            formTag.insert(new Element("input",{"type":"hidden","name":"_method","value":nodeId > 0 ? "put" : "post"}));

			var treadedTags = [];
			if (!views)
			{
				views = fetchView(amenity, wizardData.tags);
			}

			if (views)
			{
				if (views.icon)
				{
					formTag.insert(new Element("img",{"src":contextPath+views.icon}));
				}
	            formTag.insert(new Element("span",{}).update(views.name));
	            treadedTags = createKeyValues(osmType, nodeId, formTag, amenity, views.tags, true);
	        }
	        
			for (var key in amenity.keyValues)
			{
				if (treadedTags.indexOf(key) < 0)
				{
					formTag.insert(createTagValue(osmType, nodeId, key, amenity.keyValues[key]));
				}
			}

			return formTag;
        }

        function updateKeyValueTable(osmType, nodeId)
        {
			var params = new Object();
			params["osmType"] = osmType;
			params["nodeId"] = nodeId;

        	new Ajax.Request(URL.amenity, {
      		  method: 'get', parameters : params, 
      		  onSuccess: function(transport) {
                createEditBox($("amenity_"+transport.responseJSON.osmType+transport.responseJSON.nodeId), transport.responseJSON);
                alert("OK");
      		  }
      		});

        }

        function createLinkIcon(iconName, url, title)
        {
        	var elem = new Element("a",{"href":url,target:"_blank","class":"ae-url-icon"});
	    	elem.insert(new Element("img",{src: contextPath+"/img/icons/"+iconName,"title":title}));
	    	return elem;
        }

        function createViewBase(osmType, nodeId, view)
        {
			var keyId = "k_"+(idCounter)+"_"+osmType+nodeId;
            var newDiv = new Element("div");
			newDiv.insert(new Element("div").update(view.text));
			if (view.key == "")
			{
				nextFocus = nextFocus || keyId;
				newDiv.insert(new Element("input", {type:"text", id:keyId, name: "key", class: "inputkey", size:24, value: view.key}));
			} else {
				newDiv.insert(new Element("input", {type:"hidden", id:keyId, name: "key", "value":view.key}));
				newDiv.insert(new Element("input", {type:"text", id:keyId+"_" , name: "key_", "class":"inputkey", size:24, "value":view.key, "disabled":"disabled"}));
			}
			newDiv.insert(new Element("span").update("&nbsp;"));
			
			return newDiv;
		}

        function createViewText(osmType, nodeId, view, value)
        {
            var newDiv = createViewBase(osmType, nodeId, view);
			var valueId = "v_"+(idCounter++)+"_"+osmType+nodeId;
			nextFocus = nextFocus || valueId;
			newDiv.insert(new Element("input", {type:"text", id:valueId, name: "value", class: "inputvalue", size:32, value: value}));
			if (view.key == "url" && value != "")
			{
				newDiv.insert(createLinkIcon("world.png",value,"Show URL"));				
			}

			newDiv.insert(new Element("div", {id:valueId+"_choices", "class":"autocomplete"}));
			newDiv.insert(new Element("script").update("new Ajax.Autocompleter('"+valueId+"', '"+valueId+"_choices', '"+URL.acValue+"', {paramName: 'input', method: 'get', minChars: 1, frequency: 0.5, callback:autoCompleteCallBack});"));

			return newDiv;
        }

        function createViewCombo(osmType, nodeId, view, value)
        {
            var newDiv = createViewBase(osmType, nodeId, view);
			var valueId = "v_"+(idCounter++)+"_"+osmType+nodeId;
			nextFocus = nextFocus || valueId;
			var select = new Element("select", {type:"text", id:valueId, name: "value", class: "inputvalue", "data-placeholder": "<spring:message code="combo.select_or_type" />"});
			var values = view.values.split(",");
			var is_selected = false;
			for (var i=0; i<values.length; i++)
			{
				var selected = value==values[i];
				select.insert(new Element("option", {value: values[i], selected: selected ? "selected" : null}).update(values[i]));
				is_selected = is_selected || selected;
			}
			if (!is_selected && value != "")
			{
				select.insert(new Element("option", {value: value, selected: "selected"}).update(value));
				is_selected = true;
			}
			select.insert(new Element("option", {value: "", selected: !is_selected ? "selected" : null}).update(""));
			newDiv.insert(select);
			newDiv.insert(new Element("script").update("new Chosen($('"+valueId+"'),{create_option:true, persistent_create_option:true, skip_no_results:true, width:'260px'});"));

			return newDiv;
        }

        function createViewMultiselect(osmType, nodeId, view, value)
        {
            var newDiv = createViewBase(osmType, nodeId, view);
			var valueId = "v_"+(idCounter++)+"_"+osmType+nodeId;
			nextFocus = nextFocus || valueId;
			var select = new Element("select", {type:"text", id:valueId, name: "value", class: "inputvalue", multiple: "multiple", "data-placeholder": "<spring:message code="combo.select_or_type" />"});
			value = value.split(";");
			var values = view.values.split(",");
			for (var i=0; i<values.length; i++)
			{
				var index = value.indexOf(values[i]);
				var selected = index > -1;
				if (index > -1)
				{
					value.splice(index, 1);
				}
				select.insert(new Element("option", {value: values[i], selected: selected ? "selected" : null}).update(values[i]));
			}
			for (var i=0; i<value.length; i++)
			{
				select.insert(new Element("option", {value: value[i], selected: "selected"}).update(value[i]));
			}
			newDiv.insert(select);
			newDiv.insert(new Element("script").update("new Chosen($('"+valueId+"'),{create_option:true, persistent_create_option:true, skip_no_results:true, width:'260px'});"));

			return newDiv;
        }

        function createViewCheck(osmType, nodeId, view, value)
        {
            var newDiv = createViewBase(osmType, nodeId, view);
			var valueId = "v_"+(idCounter++)+"_"+osmType+nodeId;
			nextFocus = nextFocus || valueId;
			var select = new Element("select", {type:"text", id:valueId, name: "value", class: "inputvalue"});
			select.insert(new Element("option", {value: "", selected: ["true", "false", "yes", "no"].indexOf(value) < 0 ? "selected" : null}).update(""));
			select.insert(new Element("option", {value: "true", selected: ["true", "yes"].indexOf(value) > -1 ? "selected" : null}).update("<spring:message code="check.true" />"));
			select.insert(new Element("option", {value: "false", selected: ["false", "no"].indexOf(value) > -1 ? "selected" : null}).update("<spring:message code="check.false" />"));
			newDiv.insert(select);

			return newDiv;
        }

        function createViewKey(osmType, nodeId, view, value)
        {
            var newDiv = createViewBase(osmType, nodeId, view);
			var valueId = "v_"+(idCounter++)+"_"+osmType+nodeId;
			newDiv.insert(new Element("input", {type:"hidden", id:valueId , name: "value", "value":value}));
			var valueInput = new Element("input", {type:"text", id:valueId+"_", name: "value", "class":"inputvalue", size:32, "value":value, "disabled":"disabled"});
			newDiv.insert(valueInput);

			return newDiv;
        }        

        function createTagValue(osmType, nodeId, key, value)
        {
			var keyId = "k_"+(idCounter)+"_"+osmType+nodeId;
			var valueId = "v_"+(idCounter++)+"_"+osmType+nodeId;
			var keyIdChoices = keyId+"_choices";
            var newDiv = new Element("div");
			newDiv.insert(new Element("input", {type:"text", id:keyId , name: "key", "class":"inputkey", size:24, "value":key}));	
			newDiv.insert(new Element("div", {id: keyIdChoices, "class":"autocomplete"}));
			newDiv.insert(new Element("script").update("new Ajax.Autocompleter('"+keyId+"', '"+keyId+"_choices', '"+URL.acKey+"', {paramName: 'input', method: 'get', minChars: 1, frequency: 0.5});"));
			newDiv.insert(new Element("span").update("&nbsp;"));
			var valueInput = new Element("input", {type:"text", id:valueId, name: "value", "class":"inputvalue", size:32, "value":value});
			newDiv.insert(valueInput);
			if (key == "url" && value != "")
			{
				newDiv.insert(createLinkIcon("world.png",value,"Show URL"));				
			}
			if ((key == "amenity") || (key == "shop"))
			{
				newDiv.insert(createLinkIcon("anchor.png","http://wiki.openstreetmap.org/wiki/Tag:"+key+"="+value,"Show Wiki"));
			}
			newDiv.insert(new Element("div", {id:valueId+"_choices", "class":"autocomplete"}));
			newDiv.insert(new Element("script").update("new Ajax.Autocompleter('"+valueId+"', '"+valueId+"_choices', '"+URL.acValue+"', {paramName: 'input', method: 'get', minChars: 1, frequency: 0.5, callback:autoCompleteCallBack});"));

			return newDiv;
        }

        function addTags(osmType, nodeId, tags)
        {
            var amenity = AE.getAmenity(osmType, nodeId);
            var formTag = $("form_"+osmType+nodeId);
            createKeyValues(osmType, nodeId, formTag, amenity, tags, false);
			if (nextFocus)
			{
				$(nextFocus).focus();
				nextFocus = false;
			}
        }

        

        function createAddTagIcon(osmType, nodeId, iconUrl, iconTitle, tags)
        {
        	var elem = new Element("a",{"href":"#","class":"ae-add-tag-icon",onclick:"addTags('"+osmType+"','"+nodeId+"',"+Object.toJSON(tags)+")"});
        	elem.insert(new Element("img",{"src":iconUrl,"title":iconTitle,"alt":iconTitle}));
        	return elem;
        }

        function createNewAmenityWizardGroup(amenity, elem, groupData, accordion_id)
        {
            var osmType = amenity.osmType;
            var nodeId = amenity.nodeId;
			for (var i=0; i<groupData.length; i++)
			{
				var object = groupData[i].object;
				switch (groupData[i].type)
				{
					case "Group":
						var title = new Element("h2",{class:"accordion_toggle_"+accordion_id});
						if (object.icon)
						{
							title.insert(new Element("img",{src:contextPath+object.icon}));
						}
						title.insert(object.name);
						elem.insert(title);
						var wrizard_group_id = "wrizard_group_"+(idCounter++);
						var group = new Element("div",{id:wrizard_group_id,class:"ae-create-amenity-group accordion_content_"+accordion_id});
						var sub_accordion_id = idCounter++;
						createNewAmenityWizardGroup(amenity, group, object.tags, sub_accordion_id);
						elem.insert(group);
						elem.insert(new Element("script").update("new accordion('"+wrizard_group_id+"',{classNames:{toggle:'accordion_toggle_"+sub_accordion_id+"',content:'accordion_content_"+sub_accordion_id+"',toggleActive:'accordion_toggle_active_"+sub_accordion_id+"'}});"));
						break;
					case "Item":
						var a = new Element("a",{"href":"#","class":"ae-create-amenity",onclick:"addDefaultTags('"+osmType+"','"+nodeId+"',"+Object.toJSON(object)+")"});
						if (object.icon)
						{
							a.insert(new Element("img",{src:contextPath+object.icon}));
						} else {
							a.insert(new Element("div"));
						}
						a.insert(new Element("br"));
						a.insert(object.name);
						elem.insert(a);
						break;
					case "Sparator":
						elem.insert(new Element("hr"));
						break;
				}
			}
        }

        function createNewAmenityWizard(amenity)
        {
        	var elem = new Element("div");
        	elem.insert(new Element("div",{class:"ae-simple-text"}).update(MSG.templateInfo));
			var wrizard_group_id = "wrizard_group_"+(idCounter++);
        	var groups = new Element("div",{id:wrizard_group_id});
        	var accordion_id = idCounter++;
			createNewAmenityWizardGroup(amenity, groups, wizardData.tags, accordion_id);
			groups.insert(new Element("script").update("new accordion('"+wrizard_group_id+"',{classNames:{toggle:'accordion_toggle_"+accordion_id+"',content:'accordion_content_"+accordion_id+"',toggleActive:'accordion_toggle_active_"+accordion_id+"'}});"));
			elem.insert(groups);
        	return elem;			
        }

        function addDefaultTags(osmType, nodeId, wizard)
        {
            var amenity = AE.getAmenity(osmType, nodeId);
            amenity.keyValues = new Object();
            $("keyvaluetab_"+osmType+nodeId).update(createKeyValueTable(amenity,wizard));
            $("naw_"+osmType+nodeId).hide();
            $("keyvaluetab_"+osmType+nodeId).show();
            
			if (nextFocus)
			{
				$(nextFocus).focus();
				nextFocus = false;
			}            
        }

        function createTitleDiv(amenity)
        {
            var div = new Element("div",{"class":"ae-nodedetails"});
            if (amenity.nodeId != 0)
            {
            	if (amenity.osmType == 'n')
	            	div.insert(new Element("a",{"href":"http://www.openstreetmap.org/browse/node/"+amenity.nodeId,"target":"_blank"}).update("Id: n"+amenity.nodeId));
            	else if (amenity.osmType == 'w')
	            	div.insert(new Element("a",{"href":"http://www.openstreetmap.org/browse/way/"+amenity.nodeId,"target":"_blank"}).update("Id: w"+amenity.nodeId));
            	else if (amenity.osmType == 'r')
	            	div.insert(new Element("a",{"href":"http://www.openstreetmap.org/browse/relation/"+amenity.nodeId,"target":"_blank"}).update("Id: r"+amenity.nodeId));
	        }
            else
                div.insert("<spring:message code="eb.newnode" />");
            div.insert(" lon: "+amenity.lon+" lat: "+amenity.lat);
            return div;
        }

        function createEditBox(newDiv, amenity, feature)
        {
			newDiv.update(new Element("div",{"class":"ae-nodename"}).update("<spring:message code="eb.nodename" /> "+amenity.name));
			newDiv.insert(createTitleDiv(amenity));
			var editArea = new Element("div",{"class":"ae-editarea"});
			var keyValueTab = new Element("div",{"class":"ae-keyvaluetab","id":"keyvaluetab_"+amenity.osmType+amenity.nodeId}).update(createKeyValueTable(amenity, null));
			if (amenity.nodeId == 0)
			{
				keyValueTab.hide();
				var newAmenityWizard = new Element("div",{"class":"ae-keyvaluetab","id":"naw_"+amenity.osmType+amenity.nodeId}).update(createNewAmenityWizard(amenity));
				editArea.insert(newAmenityWizard);
			}
			editArea.insert(keyValueTab);
			newDiv.insert(editArea);

			var buttonDiv1 = new Element("div",{"class":"ae-buttons-top"});

			for (var id in keyValueTemplates)
			{
				var template = keyValueTemplates[id];
				if (template.icon)
				{
				    buttonDiv1.insert(createAddTagIcon(amenity.osmType,amenity.nodeId,contextPath+template.icon,
						template.name,template.tags));
				}
			}

			newDiv.insert(buttonDiv1); 

			var buttonDiv = new Element("div",{"class":"ae-buttons"});
			var updateOsmButton = new Element("input",{"type":"button","class":"ae-edit-button","value":MSG.ebOsmButton,"title":MSG.ebOsmButtonTitle,"onclick":"updateKeyValueTable('"+amenity.osmType+"',"+amenity.nodeId+")"});
			if ((amenity.nodeId == 0) || AE.isMoving())
				updateOsmButton.setAttribute("disabled","disabled");
			buttonDiv.insert(updateOsmButton);

			var moveButton = new Element("input",{type:"button","class":"ae-edit-button",value:MSG.ebMoveButton,onclick:"moveAmenity('"+amenity.osmType+"',"+amenity.nodeId+")"});
			if ((amenity.osmtType != 'n') || (amenity.nodeId == 0) || AE.isMoving())
				moveButton.setAttribute("disabled","disabled");
			buttonDiv.insert(moveButton);

			var deleteButton = new Element("input",{type:"button","class":"ae-edit-button",value:MSG.ebDeleteButton,onclick:"deleteAmenity('"+amenity.osmType+"',"+amenity.nodeId+")"});
			if ((amenity.osmtType == 'n') && ((amenity.nodeId == 0) || AE.isMoving()))
				deleteButton.setAttribute("disabled","disabled");
			buttonDiv.insert(deleteButton);
			buttonDiv.insert(new Element("input",{type:"button","class":"ae-edit-button",value:MSG.ebSaveButton,onclick:"saveAmenity('"+amenity.osmType+"',"+amenity.nodeId+")"}));
			var closeButton = new Element("input",{type:"button","class":"ae-edit-button",value:MSG.ebCloseButton});
			buttonDiv.insert(closeButton);			

   			var closeIcon = new Element("div",{"class":"ae-closeicon"});
			closeIcon.update(new Element("a",{"href":"#"}));
			buttonDiv.insert(closeIcon);
			newDiv.insert(buttonDiv);   
			closeIcon.observe('click', AE.closePopupHandler.bindAsEventListener(amenity));
			closeButton.observe('click', AE.closePopupHandler.bindAsEventListener(amenity));
        }

        function checkboxesChanged(cbs)
        {
        	for (var x = 0; x < cbs.length;x++)
        	{
				if (cbs[x].defaultChecked != cbs[x].checked)
				{
					return true;
				}
        	}
        	return false;
        }

        function saveFilterSettings()
        {
        	$('filter').hide();

        	var f = $('filterform');

        	if (f.lon.value)
            	f.lon.value = f.lon.value.replace(/,/,".");
        	if (f.lat.value)
            	f.lat.value = f.lat.value.replace(/,/,".");
        	
        	var filterChanged = checkboxesChanged(f.getInputs("checkbox","show")) || checkboxesChanged(f.getInputs("checkbox","hide"));
        	
        	if (filterChanged)
        	{
	        	updateAmenities(null,null,true);
        	}

        	if (f.saveincookie.checked)
        	{
	        	var formJson = Object.toJSON(f.serialize(true));
	    		var cookie_str = escape(formJson);
	
	    		var date = new Date();
				date.setTime(date.getTime() + (1000*60*60*24*28));	// 28 tage
				var cookie_expires = '; expires=' + date.toGMTString();
	    		try {
	    			document.cookie = "settings="+cookie_str + cookie_expires;
	    		} catch (e) {
					alert(e);
	    		}
        	}
        }

        function loadSettingsFromCookie()
        {
            var formData = null;
    		var cookies = document.cookie.match('settings=(.*?)(;|$)');
    		if (cookies) 
        	{
    			formData = (unescape(cookies[1])).evalJSON();
	    		var f = $('filterform');
	
				f.saveincookie.checked = formData.saveincookie && formData.saveincookie == 1;
				f.lon.value = formData.lon ? formData.lon : "";
				f.lat.value = formData.lat ? formData.lat : "";

				var cbs = f.getInputs("checkbox","show");
	        	for (var x = 0; x < cbs.length;x++)
	        	{
					cbs[x].checked = (cbs[x].value == formData.show) || (formData.show instanceof Array && (formData.show.indexOf(cbs[x].value) != -1));
	        	}
				cbs = f.getInputs("checkbox","hide");
	        	for (var x = 0; x < cbs.length;x++)
	        	{
					cbs[x].checked = (cbs[x].value == formData.hide) || (formData.hide instanceof Array && (formData.hide.indexOf(cbs[x].value) != -1));
	        	}
				
				
<c:if test="${!startParameters.permalink}">	
			if (formData.lon && formData.lat)
		            map.setCenter(lonLatToMercator(new OpenLayers.LonLat(formData.lon,formData.lat)),MIN_ZOOM_FOR_EDIT+1);
</c:if>
    		}
        }

        function goToHomeBase()
        {
    		var f = $('filterform');
			if (f.lon.value && f.lat.value)
			{
	            map.setCenter(lonLatToMercator(new OpenLayers.LonLat(f.lon.value,f.lat.value)),MIN_ZOOM_FOR_EDIT+1);
			}
			else
			{
				alert("<spring:message code="alert.no.base" />");
			}
            
        }

        function showFilterSettings()
        {
        	var f = $('filterform');
        	var cbs = f.getInputs("checkbox");
        	for (var x = 0; x < cbs.length;x++)
        	{
				cbs[x].defaultChecked = cbs[x].checked;
        	}
            
        	$('filter').toggle();
        }     

        function moveAmenity(osmType, nodeId)
        {
            $("moving").show();
        	document.body.style.cursor='crosshair';
            AE.movingAmenity = AE.getAmenity(osmType, nodeId);
            AE.removeFeature(osmType, nodeId);
        }

        function cancelMoving()
        {
        	$("moving").hide();
        	document.body.style.cursor='auto';        	
        	AE.movingAmenity.newLon = null;
        	AE.movingAmenity.newLat = null;
        	AE.addFeature(AE.createFeature(AE.movingAmenity));
        	AE.movingAmenity = null;      
    		AE.removeNewAmenityFeature();
        }

        function checkAccessRights()
        {
            if (!oauthTokensAvailable)
            {
                alert("<spring:message code="alert.acceptlicense" />");
            }
            else
              return true;
        }
        

        function saveAmenity(osmType, nodeId)
        {
			var f = $("form_"+osmType+nodeId);
			if (checkAccessRights())
        	{
            	var params = new Object();
            	params = Object.extend(params, f.serialize(true));

            	if ((params["_nodeId"] == 0 || AE.isMoving()) && (params.key.indexOf("highway") != -1 || params.key.indexOf("railway") != -1))
            	{
                	alert("<spring:message code="no.highway.edit" />");
                	return;	
            	}

            	var requestMethod = 'post';
            	if (params["_nodeId"] != 0)
                	requestMethod = 'put';
            	
            	
				$("storing").show();            	            	

            	new Ajax.Request(URL.amenity, {
          		  method: requestMethod, parameters : params, 
          		  onSuccess: function(transport) {
        			AE.closePopup(osmType,nodeId);
					updateAmenities(null,null,true);
					$("storing").hide();
					$("moving").hide();
		        	document.body.style.cursor='auto';					
        			AE.removeNewAmenityFeature();
        			AE.movingAmenity = null;
        			alert(transport.responseJSON.message+"\n\n<spring:message code="save.action.info" />");
          		  }
          		});
        	}
        }            

        function deleteAmenity(osmType, nodeId)
        {
            if (confirm("<spring:message code="confirm.delete" />"))
            {
				var f = $("form_"+osmType+nodeId);
				if (checkAccessRights())
	        	{
	            	var params = new Object();
	            	params = Object.extend(params, f.serialize(true));
	            	
	            	new Ajax.Request(URL.amenity, {
	          		  method: 'delete', parameters : params, 
	          		  onSuccess: function(transport) {
	        			AE.closePopup(osmType,nodeId);
						updateAmenities(null,null,true);
	        			alert(transport.responseJSON.message);
	          		  }
	          		});
	        	}
            }
        }      

        function updateZoomStatus()
        {
			var zoom = map.getZoom();
			if (zoom <= MIN_ZOOM_FOR_EDIT)
			{
				$("zoomStatus").show();
				$("newNodeButton").disable();
				$("loadOsmDataButton").disable();
			}
			else
			{
				$("zoomStatus").hide();
				$("newNodeButton").enable();
				$("loadOsmDataButton").enable();
			}            
        }      

        function loadBbox()
        {
        	updateAmenities(null,URL.osmUpdate,true); 
        }
        function setMapCenterAsHomeBase()
        {
			var coords = map.getCenter();
			$("filterform").lon.value = x2lon(coords.lon);
			$("filterform").lat.value = y2lat(coords.lat);
        }

        function setMaxZoom()
        {
        	map.zoomTo(MAX_ZOOM);
        }        

        function selectAll()
        {
            var elements = new Selector('.filter-positive-cb').findElements($('filterform'));
			$A(elements).each(function(element) {
				element.checked = true;
			});
        }

        function deselectAll()
        {
            var elements = new Selector('.filter-positive-cb').findElements($('filterform'));
			$A(elements).each(function(element) {
				element.checked = false;
			});
        }

        function invertSelection()
        {
            var elements = new Selector('.filter-positive-cb').findElements($('filterform'));
			$A(elements).each(function(element) {
				element.checked = !element.checked;
			});
        }    

        window.onload=init;
    </script>
</head>

<body>
<div id="content" style="overflow: hidden; height: 100%">
<div style="padding:5px;height: 32px; position:absolute; left: 60px; top: 0px; right:50px; z-index:750; -moz-border-radius:0px 0px 6px 6px; border-radius:0px 0px 6px 6px; background-color: #FFFFFF; background-repeat: repeat-x; border:1px solid #000000;border-top:none">
<div style="font-weight:bold;font-size:14px;">OSM Amenity Editor (${startParameters.version})</div>
<div style="position:relative; bottom:-5px; left:0px;font-size:9px;font-weight: normal"><spring:message code="info.credit" />, <spring:message code="info.contact" /></div>
<div style="position:absolute; right:5px; top: 5px; width:900px;text-align: right">
<c:if test="${startParameters.oauthTokenAvailable}"><span style="padding:2px;background-color:#ffe7cd;font-size:14px">OAuth OK!</span></c:if>
<input type="button" class="ae-small-button" value="<spring:message code="button.max.zoom" />" title="<spring:message code="button.max.zoom.hint" />" onclick="setMaxZoom();" />
<input type="button" class="ae-small-button" value="<spring:message code="button.home.base" />" title="<spring:message code="button.home.base.hint" />" onclick="goToHomeBase();" />
<select id="ae-template-select" class="ae-small-button" onchange="loadTemplate();">
	<c:set var="templates"><spring:eval expression="@propertyConfigurer['templates']" /></c:set>
	<c:forEach items="${fn:split(templates, ',')}" var="preset">
		<option value="${preset}">${preset}</option>
	</c:forEach>
</select>
<input type="button" class="ae-small-button" value="<spring:message code="button.create.node" />" id="newNodeButton" title="<spring:message code="button.create.node.hint" />" onclick="switchAdding();" />
<input type="button" class="ae-small-button" value="<spring:message code="button.rod" />" id="loadOsmDataButton" title="<spring:message code="button.rod.hint" />" onclick="loadBbox();"/>
<input type="button" class="ae-small-button" value="<spring:message code="button.settings" />" title="<spring:message code="button.settings.hint" />" onclick="showFilterSettings();">
<input type="button" class="ae-small-button" value="<spring:message code="button.help" />" title="<spring:message code="button.help.hint" />" onclick="$('help').toggle();">
<input type="button" class="ae-small-button" value="<spring:message code="button.oauth" />" title="<spring:message code="button.oauth.hint" />" onclick="window.location.href='<wt:ue>/ae/oauthRequest</wt:ue>';" />
</div>
</div>
<div style="position:absolute; left: 80px; top: 55px; right:60px; z-index:1000;">
<center>
<div class="infobox" style="width:800px;display:none;text-align: left" id="filter">
<form id="filterform">
<fieldset style="width:520px;float:left">
<legend><spring:message code="settings.show" /></legend>
<c:forEach items="<%= FilterManager.getInstance().getFiltersPositive() %>" var="filter">
<div style="width:170px;float:left;">
<input type="checkbox" class="filter-positive-cb" name="show" value="${filter.name}" <c:if test="${filter.defaultSelected}">checked="checked" </c:if> id="cb_${filter.name}" />
&nbsp;<label for="cb_${filter.name}"><spring:message code="filter.${filter.name}" />&nbsp;<img src="<wt:ue>/img/${filter.icon}</wt:ue>" width="12" height="12"></label></div>
</c:forEach>
<div style="clear:both"><input type="button" class="ae-small-button" value="<spring:message code="button.select.all" />" onclick="selectAll();" />
<input type="button" class="ae-small-button" value="<spring:message code="button.deselect.all" />" onclick="deselectAll();"/>
<input type="button" class="ae-small-button" value="<spring:message code="button.invert.selection" />" onclick="invertSelection();"/></div>
</fieldset>
<fieldset style="width:240px">
<legend><spring:message code="settings.hide" /></legend>
<c:forEach items="<%= FilterManager.getInstance().getFiltersNegative() %>" var="filter">
<input type="checkbox" name="hide" value="${filter.name}" id="cb_${filter.name}" />
&nbsp;<label for="cb_${filter.name}"><spring:message code="filter.${filter.name}" /></label><br/>
</c:forEach>
</fieldset>
<fieldset style="clear:both">
<legend><spring:message code="settings.userpass" /></legend>
<div><spring:message code="settings.noaccount" /></div>
</fieldset>
<fieldset>
<legend><spring:message code="settings.base" /></legend>
<div style="height: 22px;position: relative;">
<span style="vertical-align: middle;"><spring:message code="settings.lon" />:&nbsp;</span><input type="text" name="lon" size="12" style="width:100px;">
<span style="vertical-align: middle;"><spring:message code="settings.lat" />:&nbsp;</span><input type="text" name="lat" size="12" style="width:100px;"></div> 
<input type="button" class="ae-small-button" value="<spring:message code="settings.button.center" />" onclick="setMapCenterAsHomeBase();">
</fieldset>
<fieldset>
<legend><spring:message code="settings.cookies" /></legend>
<input type="checkbox" name="saveincookie" value="1" id="cb_saveincookie" />&nbsp;<label for="cb_saveincookie"><spring:message code="settings.saveincookie" /></label>
</fieldset>
</form>
<p>
<input type="button" class="ae-small-button" value="<spring:message code="settings.button.apply" />" onclick="saveFilterSettings();">
<input type="button" class="ae-small-button" value="<spring:message code="settings.button.close" />" onclick="$('filter').hide();">
</div>
<%@ include file="includes/infobox.jspf" %>
<div class="infobox" style="width:250px;display:none" id="feamenities">
<spring:message code="info.fewamenities" />
</div>
<div class="infobox" style="width:350px;display:none" id="moving">
<spring:message code="move.info" /><br/>
<input type="button" class="ae-small-button" value="<spring:message code="move.button" />" title="<spring:message code="move.button.hint" />" onclick="cancelMoving()">
</div>
<div class="infobox" style="width:170px;display:none" id="loading">
<spring:message code="status.loading.data" /><br><img src="<wt:ue>/img/throbber.gif</wt:ue>" >
</div>
<div class="infobox" style="width:170px;display:none" id="storing">
<spring:message code="status.saving.data" /><br><img src="<wt:ue>/img/throbber.gif</wt:ue>" >
</div>
<div class="infobox" style="width:400px;display:none" id="zoomStatus">
<spring:message code="status.zoom.to.small" /><br>
<input type="button" class="ae-small-button" value="<spring:message code="button.adujst.zoom" />" title="<spring:message code="button.adujst.zoom.hint" />" onclick="map.zoomTo(MIN_ZOOM_FOR_EDIT+1)">
</div>
</center>
</div>
<div style="height: 100%" id="map"></div>
</div>
<noscript><spring:message code="no.javascript" /></noscript>
</body>
</html>