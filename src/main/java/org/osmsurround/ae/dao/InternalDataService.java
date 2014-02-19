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
package org.osmsurround.ae.dao;

import org.apache.log4j.Logger;
import org.osmsurround.ae.entity.Amenity;
import org.osmsurround.ae.entity.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class InternalDataService {

	private Logger log = Logger.getLogger(InternalDataService.class);

	@Autowired
	private NodeUpdate nodeUpdate;
	@Autowired
	private NodeInsert nodeInsert;
	@Autowired
	private NodeDelete nodeDelete;
	@Autowired
	private NodeTagInsert nodeTagInsert;
	@Autowired
	private NodeTagDelete nodeTagDelete;

	public void updateInternalData(Node.OsmType osmType, long nodeId, Amenity amenity) {
		log.debug("Internal Update with osmType: " + osmType + " nodeId: " + nodeId + " amenity: " + amenity);
		nodeUpdate.updateNode(amenity);
		updateNodeTags(osmType, nodeId, amenity);
	}

	private void updateNodeTags(Node.OsmType osmType, long nodeId, Amenity amenity) {
		nodeTagDelete.delete(osmType, nodeId);
		nodeTagInsert.insert(osmType, nodeId, amenity.getKeyValues());
	}

	public void insertInternalData(Node.OsmType osmType, long nodeId, Amenity amenity) {
		log.debug("Internal Update with osmType: " + osmType + "nodeId: " + nodeId + " amenity: " + amenity);
		nodeInsert.insert(amenity);
		updateNodeTags(osmType, nodeId, amenity);
	}

	public void deleteInternalData(Node.OsmType osmType, long nodeId) {
		nodeDelete.delete(osmType, nodeId);
		nodeTagDelete.delete(osmType, nodeId);
	}

	public void updateAmenities(Iterable<Amenity> amenities) {
		for (Amenity amenity : amenities) {
			deleteInternalData(amenity.getOsmType(), amenity.getNodeId());
			nodeInsert.insert(amenity);
			nodeTagInsert.insert(amenity.getOsmType(), amenity.getNodeId(), amenity.getKeyValues());
		}
	}

}
