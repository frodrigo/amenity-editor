package org.osmsurround.ae.templates.serializer;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.osm.preset.schema.Chunk;
import org.osm.preset.schema.Reference;
import org.xnap.commons.i18n.I18n;

public class ReferenceSerializer extends JsonSerializer<Reference> {
	private I18n i18n ;

	public ReferenceSerializer(I18n i18n ) {
		this.i18n = i18n;
	}

	@Override
	public void serialize(Reference value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
			JsonProcessingException {
		jgen.writeStartObject();
		jgen.writeStringField("ref", ((Chunk) value.getRef()).getId());
		jgen.writeEndObject();
	}
}