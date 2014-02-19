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
package org.osmsurround.ae.osm;

import java.util.List;

import org.osm.schema.OsmRoot;
import org.osmsurround.ae.dao.InternalDataService;
import org.osmsurround.ae.dao.InternalDataUpdate;
import org.osmsurround.ae.entity.Amenity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;

@Service
public class OsmUpdateService {

	@Autowired
	private InternalDataService internalDataService;
	@Autowired
	private RestOperations restOperations;
	@Autowired
	private OsmConvertService osmConvertService;

	@Value("${overpassApiBaseUrl}")
	private String overpassApiBaseUrl;

	@Value("${overpassApiKeyFilter}")
	private String overpassApiKeyFilter;

	private String[] overpassApiKeyFilterList;

	public List<Amenity> getOsmDataAsAmenities(BoundingBox boundingBox) {
		String url = createUrl(boundingBox);
		return osmConvertService.osmToAmenity(restOperations.getForObject(url, OsmRoot.class));
	}

	private String[] getOverpassApiKeyFilterList() {
		if (overpassApiKeyFilterList == null) {
			overpassApiKeyFilterList = overpassApiKeyFilter.split(",");
		}
		return overpassApiKeyFilterList;
	}

	private String createUrl(BoundingBox boundingBox) {
		String bbox = boundingBox.getSouth() + "," + boundingBox.getWest() + "," + boundingBox.getNorth() + ","
				+ boundingBox.getEast();
		
		StringBuffer sb = new StringBuffer(overpassApiBaseUrl + "?data=(");
		for (String key : getOverpassApiKeyFilterList()) {
			sb.append("node['").append(key).append("'](" + bbox + ");");
		}
		for (String key : getOverpassApiKeyFilterList()) {
			sb.append("way['").append(key).append("'](" + bbox + ");");
		}
		sb.append(");out body;>;out skel;");

		return sb.toString();
	}

	public void startUpdateThread(List<Amenity> amenities) {
		Thread thread = new Thread(new InternalDataUpdate(amenities, internalDataService));
		thread.start();
	}
}
