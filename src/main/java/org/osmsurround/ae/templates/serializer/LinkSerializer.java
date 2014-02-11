package org.osmsurround.ae.templates.serializer;

import java.io.IOException;
import java.util.Locale;

import javax.xml.namespace.QName;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.osm.preset.schema.Link;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

public class LinkSerializer extends JsonSerializer<Link> {
	private MessageSource messageSource;

	public LinkSerializer(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@Override
	public void serialize(Link value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
			JsonProcessingException {
		jgen.writeStartObject();

		Locale locale = LocaleContextHolder.getLocale();
		if (value.getText() != null) {
			String text = messageSource.getMessage(value.getText(), null, value.getText(), locale);
			jgen.writeStringField("text", text);
		}

		if (value.getTextContext() != null) {
			String textContext = messageSource.getMessage(value.getTextContext(), null, value.getTextContext(), locale);
			jgen.writeStringField("text_context", textContext);
		}

		if (value.getHref() != null) {
			QName lkey = new QName(locale.getLanguage() + ".href");
			String href = value.getOtherAttributes().containsKey(lkey) ? value.getOtherAttributes().get(lkey) : value
					.getHref();
			jgen.writeStringField("href", href);
		}

		jgen.writeEndObject();
	}
}
