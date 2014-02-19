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
package org.osmsurround.ae.entity;

import org.codehaus.jackson.annotate.JsonValue;

public class Node {

	public enum OsmType {
		NODE("n"), WAY("w"), RELATION("r");

		private String discriminator;

		OsmType(String discriminator) {
			this.discriminator = discriminator;
		}

		@JsonValue
		@Override
		public String toString() {
			return discriminator;
		}

		public static OsmType getEnum(String value) {
			if (value == null) {
				throw new IllegalArgumentException();
			} else {
				for (OsmType v : values()) {
					if (value.equalsIgnoreCase(v.discriminator)) {
						return v;
					}
				}
				throw new IllegalArgumentException();
			}
		}
	}

	private OsmType osmType;
	private long nodeId;
	private double lon;
	private double lat;

	public Node(OsmType osmType, long nodeId, double lon, double lat) {
		this.osmType = osmType;
		this.nodeId = nodeId;
		this.lon = lon;
		this.lat = lat;
	}

	public OsmType getOsmType() {
		return osmType;
	}

	public long getNodeId() {
		return nodeId;
	}

	public double getLon() {
		return lon;
	}

	public double getLat() {
		return lat;
	}

}
