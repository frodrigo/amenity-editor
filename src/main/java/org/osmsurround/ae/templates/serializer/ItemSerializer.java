package org.osmsurround.ae.templates.serializer;

import java.io.IOException;
import java.util.Locale;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.osm.preset.schema.Item;
import org.springframework.context.i18n.LocaleContextHolder;
import org.xnap.commons.i18n.I18n;

public class ItemSerializer extends JsonSerializer<Item> {
	private I18n i18n;

	public ItemSerializer(I18n i18n) {
		this.i18n = i18n;
	}

	@Override
	public void serialize(Item value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
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

		String name = i18n.tr(value.getName());
		jgen.writeStringField("name", name);

		if (value.getIcon() != null) {
			jgen.writeStringField("icon", value.getIcon());
		}

		if (value.getType() != null) {
			jgen.writeStringField("type", value.getType());
		}

		if (value.getNameTemplate() != null) {
			jgen.writeStringField("name_template", value.getNameTemplate());
		}

		if (value.getNameTemplateFilter() != null) {
			jgen.writeStringField("name_template_filter", value.getNameTemplateFilter());
		}

		jgen.writeEndObject();
	}
}