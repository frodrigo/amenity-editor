package org.osmsurround.ae.templates.serializer;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.osm.preset.schema.Chunk;
import org.xnap.commons.i18n.I18n;

public class ChunkSerializer extends JsonSerializer<Chunk> {
	private I18n i18n;

	public ChunkSerializer(I18n i18n) {
		this.i18n = i18n;
	}

	@Override
	public void serialize(Chunk value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
			JsonProcessingException {
		jgen.writeStartObject();

		jgen.writeFieldName("tags");
		jgen.writeStartArray();
		for (Object labelOrSpaceOrLink : value.getLabelOrSpaceOrLink()) {
			jgen.writeStartObject();
			String simpleName = labelOrSpaceOrLink.getClass().getSimpleName();
			jgen.writeStringField("type", simpleName);
			jgen.writeObjectField("object", labelOrSpaceOrLink);
			jgen.writeEndObject();
		}
		jgen.writeEndArray();

		String id = i18n.tr(value.getId());
		jgen.writeStringField("id", id);

		jgen.writeStringField("icon", value.getIcon());

		jgen.writeEndObject();
	}
}