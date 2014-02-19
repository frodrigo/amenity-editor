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
package org.osmsurround.ae.osmrequest;

import org.osm.schema.OsmRoot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;

@Service
public class OsmTemplate implements OsmOperations {

	@Autowired
	private RestOperations restOperations;
	@Value("${osmApiBaseUrl}")
	protected String osmApiBaseUrl;

	@Override
	public OsmRoot getForNode(long nodeId) {
		return restOperations.getForObject(osmApiBaseUrl + "/api/0.6/node/{nodeId}", OsmRoot.class,
				String.valueOf(nodeId));
	}

	@Override
	public OsmRoot getForWay(long nodeId) {
		return restOperations.getForObject(osmApiBaseUrl + "/api/0.6/way/{nodeId}", OsmRoot.class,
				String.valueOf(nodeId));
	}

	@Override
	public OsmRoot getForRelation(long nodeId) {
		return restOperations.getForObject(osmApiBaseUrl + "/api/0.6/relation/{nodeId}", OsmRoot.class,
				String.valueOf(nodeId));
	}
}
