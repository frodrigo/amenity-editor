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

import java.sql.Types;

import javax.sql.DataSource;

import org.osmsurround.ae.entity.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
public class NodeTagDelete extends SqlUpdate {

	@Autowired
	public NodeTagDelete(DataSource dataSource) {
		setDataSource(dataSource);
		setSql("DELETE FROM node_tags WHERE osm_type = ? AND node_id = ?");
		declareParameter(new SqlParameter(Types.CHAR));
		declareParameter(new SqlParameter(Types.INTEGER));
	}

	public void delete(Node.OsmType osmType, long nodeId) {
		update(osmType.toString(), nodeId);
	}
}
