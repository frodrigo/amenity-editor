package org.osmsurround.ae.templates.serializer;

import java.io.IOException;
import java.util.Locale;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.osm.preset.schema.Chunk;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

public class ChunkSerializer extends JsonSerializer<Chunk> {
	private MessageSource messageSource;

	public ChunkSerializer(MessageSource messageSource) {
		this.messageSource = messageSource;
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

		Locale locale = LocaleContextHolder.getLocale();
		String id = messageSource.getMessage(value.getId(), null, locale);
		jgen.writeStringField("id", id);

		jgen.writeStringField("icon", value.getIcon());

		jgen.writeEndObject();
	}
}