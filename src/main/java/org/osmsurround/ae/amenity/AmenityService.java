/**
 * This file is part of Amenity Editor for OSM.
 * Copyright (c) 2001 by Adrian Stabiszewski, as@grundid.de
 *
 * Amenity Editor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Amenity Editor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Amenity Editor.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.osmsurround.ae.amenity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.log4j.Logger;
import org.osm.schema.OsmBasicType;
import org.osm.schema.OsmNode;
import org.osm.schema.OsmRelation;
import org.osm.schema.OsmWay;
import org.osmsurround.ae.dao.InternalDataService;
import org.osmsurround.ae.entity.Amenity;
import org.osmsurround.ae.entity.Node;
import org.osmsurround.ae.model.NewPosition;
import org.osmsurround.ae.osm.OsmConvertService;
import org.osmsurround.ae.osmrequest.OsmDeleteRequest;
import org.osmsurround.ae.osmrequest.OsmInsertRequest;
import org.osmsurround.ae.osmrequest.OsmOperations;
import org.osmsurround.ae.osmrequest.OsmUpdateRequest;
import org.osmsurround.ae.osmrequest.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AmenityService {

	private Logger log = Logger.getLogger(AmenityService.class);

	@Autowired
	private OsmDeleteRequest osmDeleteRequest;
	@Autowired
	private OsmUpdateRequest osmUpdateRequest;
	@Autowired
	private OsmInsertRequest osmInsertRequest;
	@Autowired
	private InternalDataService internalDataService;
	@Autowired
	private OsmConvertService osmConvertService;
	@Autowired
	private OsmOperations osmOperations;

	public Amenity getAmenity(Node.OsmType osmType, long nodeId) {
		switch (osmType) {
		case NODE:
			return osmConvertService.osmToAmenity(osmOperations.getForNode(nodeId)).get(0);
		case WAY:
			return osmConvertService.osmToAmenity(osmOperations.getForWay(nodeId)).get(0);
		case RELATION:
			return osmConvertService.osmToAmenity(osmOperations.getForRelation(nodeId)).get(0);
		default:
			return null;
		}
	}

	private OsmNode getNode(long nodeId) {
		return osmOperations.getForNode(nodeId).getNode().get(0);
	}

	private OsmWay getWay(long wayId) {
		return osmOperations.getForWay(wayId).getWay().get(0);
	}

	private OsmRelation getRelation(long relationId) {
		return osmOperations.getForRelation(relationId).getRelation().get(0);
	}

	
	public void deleteAmenity(Node.OsmType osmType, long nodeId) {
		HttpResponse httpResponse = osmDeleteRequest.execute(getNode(nodeId));
		if (httpResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_OK) {
			internalDataService.deleteInternalData(osmType, nodeId);
		}
		else {
			throw RequestUtils.createExceptionFromHttpResponse(httpResponse);
		}
	}

	public void updateAmenity(Node.OsmType osmType, long nodeId, Map<String, String> data, NewPosition newPosition) {
		OsmBasicType amenity = null;
		switch (osmType) {
		case NODE:
			OsmNode amenityNode = getNode(nodeId);
			amenity = amenityNode;
			// save the original amenity position
			newPosition.setLat(Double.valueOf(amenityNode.getLat()));
			newPosition.setLon(Double.valueOf(amenityNode.getLon()));
			updateAmenityValues(amenityNode, data, newPosition);
			break;
		case WAY:
			amenity = getWay(nodeId);
			updateAmenityValues(amenity, data);
			break;
		case RELATION:
			amenity = getRelation(nodeId);
			updateAmenityValues(amenity, data);
			break;
		}

		HttpResponse httpResponse = osmUpdateRequest.execute(amenity);
		if (httpResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_OK) {
			Amenity amenityFromOsm = getAmenity(osmType, nodeId);
			internalDataService.updateInternalData(osmType, nodeId, amenityFromOsm);
		}
		else {
			throw RequestUtils.createExceptionFromHttpResponse(httpResponse);
		}
	}

	private void updateAmenityValues(OsmNode amenity, Map<String, String> data, NewPosition newPosition) {
		updateAmenityValues(amenity, data);

		if (newPosition.hasNewPosition()) {
			amenity.setLon(newPosition.getNewlon().floatValue());
			amenity.setLat(newPosition.getNewlat().floatValue());
		}
		else if (newPosition.hasPosition()) {
			amenity.setLon(newPosition.getLon().floatValue());
			amenity.setLat(newPosition.getLat().floatValue());
		}
		else {
			throw new RuntimeException("amenity has no position");
		}
	}

	private void updateAmenityValues(OsmBasicType amenity, Map<String, String> data) {
		// Bad design, but it the simplest choose, better then complicate the XSD
		if (amenity instanceof OsmNode) {
			((OsmNode) amenity).getTag().clear();
		} else if (amenity instanceof OsmWay) {
			((OsmWay) amenity).getTag().clear();
		} else if (amenity instanceof OsmRelation) {
			((OsmRelation) amenity).getTag().clear();
		}
		osmConvertService.setTags(amenity, data);
	}

	public void insertAmenity(Map<String, String> data, NewPosition newPosition) {
		OsmNode amenity = osmConvertService.amenityToNode(new Amenity());
		updateAmenityValues(amenity, data, newPosition);

		HttpResponse httpResponse = osmInsertRequest.execute(amenity);
		if (httpResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_OK) {
			long nodeId;
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
				nodeId = Long.parseLong(reader.readLine());
				internalDataService.insertInternalData(Node.OsmType.NODE, nodeId, getAmenity(Node.OsmType.NODE, nodeId));
			}
			catch (Exception e) {
				log.warn("", e);
			}
		}
		else {
			throw RequestUtils.createExceptionFromHttpResponse(httpResponse);
		}
	}
}
