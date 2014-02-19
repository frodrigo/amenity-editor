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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBElement;

import org.osm.schema.ObjectFactory;
import org.osm.schema.OsmBasicType;
import org.osm.schema.OsmNd;
import org.osm.schema.OsmNode;
import org.osm.schema.OsmRelation;
import org.osm.schema.OsmRoot;
import org.osm.schema.OsmTag;
import org.osm.schema.OsmWay;
import org.osmsurround.ae.entity.Amenity;
import org.osmsurround.ae.entity.Node;
import org.osmsurround.ae.filter.NodeFilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OsmConvertService {

	@Autowired
	private NodeFilterService nodeFilterService;

	private ObjectFactory of = new ObjectFactory();

	public List<Amenity> osmToAmenity(OsmRoot osm) {
		Map<BigInteger, OsmNode> nodeIndex = new HashMap<BigInteger, OsmNode>();
		List<Amenity> allAmenities = new ArrayList<Amenity>();
		for (OsmNode node : osm.getNode()) {
			Amenity amenity = osmNodeToAmenity(node);
			if (nodeFilterService.isValidNode(amenity.getKeyValues())) {
				allAmenities.add(amenity);
			}

			nodeIndex.put(node.getId(), node);
		}

		for (OsmWay way : osm.getWay()) {
			Amenity amenity = osmWayToAmenity(way, nodeIndex);
			if (nodeFilterService.isValidNode(amenity.getKeyValues())) {
				allAmenities.add(amenity);
			}
		}

		return allAmenities;
	}

	public Amenity osmNodeToAmenity(OsmNode node) {
		Amenity amenity = new Amenity(Node.OsmType.NODE, node.getId().longValue(), node.getLon(), node.getLat());
		Map<String, String> data = amenity.getKeyValues();

		for (OsmTag tag : node.getTag()) {

			if (!nodeFilterService.isIgnoreTag(tag.getK().toLowerCase()))
				data.put(tag.getK(), tag.getV());
		}

		return amenity;
	}

	public Amenity osmWayToAmenity(OsmWay way, Map<BigInteger, OsmNode> nodeIndex) {
		float lon = 0, lat = 0;

		if (way.getNd().get(0).getRef().equals(way.getNd().get(way.getNd().size() - 1).getRef())) {
			// Circular way, take centroid
			int n = 0;
			for (OsmNd nd : way.getNd()) {
				BigInteger ref = nd.getRef();
				if (nodeIndex.containsKey(ref)) {
					OsmNode node = nodeIndex.get(ref);
					lon += node.getLon();
					lat += node.getLat();
					n++;
				}
			}
			lon /= n;
			lat /= n;
		} else {
			// Linear way
			int mid = (way.getNd().size() - 1) / 2;
			BigInteger ref = way.getNd().get(mid).getRef();
			if (nodeIndex.containsKey(ref)) {
				OsmNode node = nodeIndex.get(ref);
				lat = node.getLat();
				lon = node.getLon();

				if (way.getNd().size() % 2 == 0) {
					// Even string size
					ref = way.getNd().get(mid + 1).getRef();
					if (nodeIndex.containsKey(ref)) {
						node = nodeIndex.get(ref);
						lat = (lat + node.getLat()) / 2;
						lon = (lon + node.getLon()) / 2;
					}
				}
			}
		}

		Amenity amenity = new Amenity(Node.OsmType.WAY, way.getId().longValue(), lon, lat);
		Map<String, String> data = amenity.getKeyValues();

		for (OsmTag tag : way.getTag()) {
			if (!nodeFilterService.isIgnoreTag(tag.getK().toLowerCase()))
				data.put(tag.getK(), tag.getV());
		}

		return amenity;
	}

	public OsmNode amenityToNode(Amenity amenity) {
		OsmNode node = of.createOsmNode();
		node.setId(BigInteger.valueOf(amenity.getNodeId()));
		node.setLat((float)amenity.getLat());
		node.setLon((float)amenity.getLon());

		node.getTag().clear();
		setTags(node, amenity.getKeyValues());

		return node;
	}

	public void setTags(OsmBasicType amenity, Map<String, String> data) {
		for (Entry<String, String> entry : data.entrySet()) {
			OsmTag tag = of.createOsmTag();
			tag.setK(entry.getKey());
			tag.setV(entry.getValue());
			// Bad design, but it the simplest choose, better then complicate
			// the XSD
			if (amenity instanceof OsmNode) {
				((OsmNode) amenity).getTag().add(tag);
			} else if (amenity instanceof OsmWay) {
				((OsmWay) amenity).getTag().add(tag);
			} else if (amenity instanceof OsmRelation) {
				((OsmRelation) amenity).getTag().add(tag);
			}
		}
	}

	public OsmRoot createOsmRoot() {
		OsmRoot osm = of.createOsmRoot();
		osm.setVersion(BigDecimal.valueOf(0.6));
		osm.setGenerator("Amenity Editor");
		return osm;
	}

	public JAXBElement<OsmRoot> toJaxbElement(OsmRoot osm) {
		return of.createOsm(osm);
	}

	public JAXBElement<OsmRoot> toJaxbElement(OsmBasicType node) {
		OsmRoot osm = createOsmRoot();
		if (node instanceof OsmNode) {
			osm.getNode().add((OsmNode) node);
		} else if (node instanceof OsmWay) {
			osm.getWay().add((OsmWay) node);
		} else if (node instanceof OsmRelation) {
			osm.getRelation().add((OsmRelation) node);
		}
		return of.createOsm(osm);
	}
}
