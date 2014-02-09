package org.osmsurround.ae.templates.serializer;

import java.io.IOException;
import java.util.Locale;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.osm.preset.schema.Optional;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

public class OptionalSerializer extends JsonSerializer<Optional> {
	private MessageSource messageSource;

	public OptionalSerializer(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@Override
	public void serialize(Optional value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
			JsonProcessingException {
		jgen.writeStartObject();

		jgen.writeFieldName("optionalElements");
		jgen.writeStartArray();
		for (Object optionalElements : value.getOptionalElements()) {
			jgen.writeStartObject();
			String simpleName = optionalElements.getClass().getSimpleName();
			jgen.writeStringField("type", simpleName);
			jgen.writeObjectField("object", optionalElements);
			jgen.writeEndObject();
		}
		jgen.writeEndArray();

		Locale locale = LocaleContextHolder.getLocale();
		if (value.getText() != null) {
			String text = messageSource.getMessage(value.getText(), null, value.getText(), locale);
			jgen.writeStringField("text", text);
		}

		if (value.getTextContext() != null) {
			String textContext = messageSource.getMessage(value.getTextContext(), null, value.getTextContext(), locale);
			jgen.writeStringField("text_context", textContext);
		}

		jgen.writeEndObject();
	}
}
