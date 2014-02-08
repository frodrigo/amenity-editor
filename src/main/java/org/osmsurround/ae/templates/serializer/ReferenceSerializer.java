package org.osmsurround.ae.templates.serializer;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.osm.preset.schema.Chunk;
import org.osm.preset.schema.Reference;
import org.springframework.context.MessageSource;

public class ReferenceSerializer extends JsonSerializer<Reference> {
	private MessageSource messageSource;

	public ReferenceSerializer(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@Override
	public void serialize(Reference value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
			JsonProcessingException {
		jgen.writeStartObject();
		jgen.writeStringField("ref", ((Chunk) value.getRef()).getId());
		jgen.writeEndObject();
	}
}