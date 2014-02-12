package org.osmsurround.ae.templates.serializer;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.osm.preset.schema.Optional;
import org.xnap.commons.i18n.I18n;

public class OptionalSerializer extends JsonSerializer<Optional> {
	private I18n i18n;

	public OptionalSerializer(I18n i18n) {
		this.i18n = i18n;
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

		if (value.getText() != null) {
			String text = i18n.tr(value.getText());
			jgen.writeStringField("text", text);
		}

		if (value.getTextContext() != null) {
			String textContext = i18n.tr(value.getTextContext());
			jgen.writeStringField("text_context", textContext);
		}

		jgen.writeEndObject();
	}
}
