package org.osmsurround.ae.templates.serializer;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ser.CustomSerializerFactory;
import org.osm.preset.schema.Check;
import org.osm.preset.schema.Checkgroup;
import org.osm.preset.schema.Chunk;
import org.osm.preset.schema.Item;
import org.osm.preset.schema.Label;
import org.osm.preset.schema.Link;
import org.osm.preset.schema.ListEntry;
import org.osm.preset.schema.Multiselect;
import org.osm.preset.schema.Optional;
import org.osm.preset.schema.Reference;
import org.osm.preset.schema.Role;
import org.osm.preset.schema.Text;
import org.springframework.stereotype.Component;

@Component("jacksonObjectMapper")
public class CustomObjectMapper extends ObjectMapper {

	public CustomObjectMapper() {
		CustomSerializerFactory sf = new CustomSerializerFactory();
		sf.addSpecificMapping(Item.class, new IntrospectionSerializer());
		sf.addSpecificMapping(Chunk.class, new IntrospectionSerializer());
		sf.addSpecificMapping(Reference.class, new ReferenceSerializer());
		sf.addSpecificMapping(Optional.class, new IntrospectionSerializer());
		sf.addSpecificMapping(Checkgroup.class, new IntrospectionSerializer());
		sf.addSpecificMapping(Link.class, new IntrospectionSerializer());
		sf.addSpecificMapping(Text.class, new IntrospectionSerializer());
		sf.addSpecificMapping(ListEntry.class, new IntrospectionSerializer());
		sf.addSpecificMapping(Label.class, new IntrospectionSerializer());
		sf.addSpecificMapping(Multiselect.class, new IntrospectionSerializer());
		sf.addSpecificMapping(Role.class, new IntrospectionSerializer());
		sf.addSpecificMapping(Check.class, new IntrospectionSerializer());
		this.setSerializerFactory(sf);
	}
}
