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
package org.osmsurround.ae.templates;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.osm.preset.schema.Chunk;
import org.osm.preset.schema.GroupParent;
import org.osm.preset.schema.Root;
import org.springframework.stereotype.Service;

@Service
public class TemplatesService {

	private String file;

	private Root root;
	private Map<String, Chunk> viewValueTemplates = new LinkedHashMap<String, Chunk>();
	private List<Object> viewTemplates = new LinkedList<Object>();

	public TemplatesService() {
		file = "/preset-default.xml";
		root = unmarshalTemplate(file);
		initNodeTemplates(root);
	}

	private Root unmarshalTemplate(String file) {
		try {
			InputStream resource = TemplatesService.class.getResourceAsStream(file);
			Unmarshaller unmarshaller = JAXBContext.newInstance(Root.class).createUnmarshaller();
			JAXBElement<Root> root = unmarshaller.unmarshal(new StreamSource(resource), Root.class);
			return root.getValue();
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	private void initNodeTemplates(GroupParent root) {
		viewTemplates.add(root);
		List<Chunk> chunks = new ArrayList<Chunk>(root.getChunkOrGroupOrItem().size());
		for (Object chunkOrGroupOrItem : root.getChunkOrGroupOrItem()) {
			if (chunkOrGroupOrItem instanceof Chunk) {
				Chunk chunk = (Chunk) chunkOrGroupOrItem;
				viewValueTemplates.put(chunk.getId(), chunk);
				chunks.add(chunk);
			}
		}
		root.getChunkOrGroupOrItem().removeAll(chunks);
	}

	public Map<String, Chunk> getViewValueTemplates() {
		return viewValueTemplates;
	}

	public List<Object> getViewTemplates() {
		return viewTemplates;
	}
}
