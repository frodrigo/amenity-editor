package org.osmsurround.ae.templates.serializer;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ser.CustomSerializerFactory;
import org.osm.preset.schema.Chunk;
import org.osm.preset.schema.Item;
import org.osm.preset.schema.Reference;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component("jacksonObjectMapper")
public class CustomObjectMapper extends ObjectMapper {

	public CustomObjectMapper(MessageSource messageSource) {
		CustomSerializerFactory sf = new CustomSerializerFactory();
		sf.addSpecificMapping(Item.class, new ItemSerializer(messageSource));
		sf.addSpecificMapping(Chunk.class, new ChunkSerializer(messageSource));
		sf.addSpecificMapping(Reference.class, new ReferenceSerializer(messageSource));
		this.setSerializerFactory(sf);
	}
}
