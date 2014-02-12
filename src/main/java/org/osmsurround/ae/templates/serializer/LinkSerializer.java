package org.osmsurround.ae.templates.serializer;

import java.io.IOException;

import javax.xml.namespace.QName;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.osm.preset.schema.Link;
import org.xnap.commons.i18n.I18n;

public class LinkSerializer extends JsonSerializer<Link> {
	private I18n i18n;

	public LinkSerializer(I18n i18n) {
		this.i18n = i18n;
	}

	@Override
	public void serialize(Link value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
			JsonProcessingException {
		jgen.writeStartObject();

		if (value.getText() != null) {
			String text = i18n.tr(value.getText());
			jgen.writeStringField("text", text);
		}

		if (value.getTextContext() != null) {
			String textContext = i18n.tr(value.getTextContext());
			jgen.writeStringField("text_context", textContext);
		}

		if (value.getHref() != null) {
			QName lkey = new QName(i18n.getLocale().getLanguage() + ".href");
			String href = value.getOtherAttributes().containsKey(lkey) ? value.getOtherAttributes().get(lkey) : value
					.getHref();
			jgen.writeStringField("href", href);
		}

		jgen.writeEndObject();
	}
}
