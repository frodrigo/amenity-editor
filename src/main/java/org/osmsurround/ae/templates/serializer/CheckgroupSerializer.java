package org.osmsurround.ae.templates.serializer;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.osm.preset.schema.Check;
import org.osm.preset.schema.Checkgroup;
import org.xnap.commons.i18n.I18n;

public class CheckgroupSerializer extends JsonSerializer<Checkgroup> {
	private I18n i18n ;

	public CheckgroupSerializer(I18n i18n ) {
		this.i18n = i18n;
	}

	@Override
	public void serialize(Checkgroup value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
			JsonProcessingException {
		jgen.writeStartObject();

		jgen.writeFieldName("check");
		jgen.writeStartArray();
		for (Check check : value.getCheck()) {
			jgen.writeStartObject();
			String simpleName = check.getClass().getSimpleName();
			jgen.writeStringField("type", simpleName);
			jgen.writeObjectField("object", check);
			jgen.writeEndObject();
		}
		jgen.writeEndArray();

		if (value.getColumns() != null) {
			jgen.writeNumberField("columns", value.getColumns().intValue());
		}

		jgen.writeEndObject();
	}
}
