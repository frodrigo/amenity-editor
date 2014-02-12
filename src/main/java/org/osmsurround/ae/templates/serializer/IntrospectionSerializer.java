package org.osmsurround.ae.templates.serializer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.xml.namespace.QName;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.osm.preset.schema.Link;
import org.osm.preset.schema.Space;
import org.xnap.commons.i18n.I18n;

public class IntrospectionSerializer extends JsonSerializer<Object> {
	private I18n i18n;

	public IntrospectionSerializer(I18n i18n) {
		this.i18n = i18n;
	}

	@Override
	public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
			JsonProcessingException {
		jgen.writeStartObject();

		String className = value.getClass().getSimpleName();
		Method[] methods = value.getClass().getMethods();
		for (Method method : methods) {
			String methodName = method.getName();
			if (methodName.startsWith("get") && !methodName.equals("getOtherAttributes")
					&& !methodName.equals("getClass")) {
				try {
					Object object = method.invoke(value);
					if (object != null) {
						if (object instanceof Iterable<?>) {

							// Iterate on sub objects and add type
							jgen.writeFieldName("tags");
							jgen.writeStartArray();
							for (Object ele : (Iterable<?>) object) {
								jgen.writeStartObject();
								String simpleName = ele.getClass().getSimpleName();
								jgen.writeStringField("type", simpleName);
								if (!(ele instanceof Space)) {
									// Space make serialize fail
									jgen.writeObjectField("object", ele);
								}
								jgen.writeEndObject();
							}
							jgen.writeEndArray();
						} else if (className.equals("Link") && methodName.equals("getHref")) {

							// Pick the href for current language
							Link link = (Link) value;
							QName lkey = new QName(i18n.getLocale().getLanguage() + ".href");
							String href = link.getOtherAttributes().containsKey(lkey) ? link.getOtherAttributes().get(
									lkey) : link.getHref();
							jgen.writeStringField("href", href);
						} else {

							// Dump all other textual attributs
							String text = object.toString();
							if (methodName.equals("getText")
									|| methodName.equals("getTextContext")
									|| (className.equals("Chunk") && methodName.equals("getName"))
									|| (className.equals("Item") && methodName.equals("getName"))
									|| (className.equals("ListEntry") && (methodName.equals("getDisplayValue") || methodName
											.equals("getShortDescription")))) {
								// Translate
								String t = i18n.tr(text);
								if (t != null) {
									text = t;
								}
							}

							String token = methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
							token = token.replaceAll("(\\B[A-Z])", "_$1").toLowerCase();
							jgen.writeStringField(token, text);
						}
					}
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(e);
				} catch (InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			}
		}

		jgen.writeEndObject();
	}
}
