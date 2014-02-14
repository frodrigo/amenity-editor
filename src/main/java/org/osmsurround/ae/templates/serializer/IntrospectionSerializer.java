package org.osmsurround.ae.templates.serializer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.namespace.QName;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.osm.preset.schema.Link;
import org.osm.preset.schema.Separator;
import org.osm.preset.schema.Space;
import org.springframework.context.i18n.LocaleContextHolder;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

public class IntrospectionSerializer extends JsonSerializer<Object> {

	private static Map<String, I18n> I18N_CACHE = new HashMap<String, I18n>();

	private static I18n getI18n() {
		Locale locale = LocaleContextHolder.getLocale();
		String key = locale.toString();

		if (I18N_CACHE.containsKey(key)) {
			return I18N_CACHE.get(key);
		} else {
			I18n i18n = I18nFactory.getI18n(IntrospectionSerializer.class, "org.osm.preset.Messages", locale);
			I18N_CACHE.put(key, i18n);
			return i18n;
		}
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
								if (!(ele instanceof Space) && !(ele instanceof Separator)) {
									// Space make serialize fail
									jgen.writeObjectField("object", ele);
								}
								jgen.writeEndObject();
							}
							jgen.writeEndArray();
						} else if (className.equals("Link") && methodName.equals("getHref")) {

							// Pick the href for current language
							Link link = (Link) value;
							Locale locale = LocaleContextHolder.getLocale();
							QName lkey = new QName(locale.getLanguage().substring(1, 2) + ".href");
							String href = link.getOtherAttributes().containsKey(lkey) ? link.getOtherAttributes().get(
									lkey) : link.getHref();
							jgen.writeStringField("href", href);
						} else {

							// Dump all other textual attributs
							String text = object.toString();
							if (methodName.equals("getText")
									|| methodName.equals("getTextContext")
									|| (className.equals("Group") && methodName.equals("getName"))
									|| (className.equals("Chunk") && methodName.equals("getName"))
									|| (className.equals("Item") && methodName.equals("getName"))
									|| (className.equals("ListEntry") && (methodName.equals("getDisplayValue") || methodName
											.equals("getShortDescription")))) {
								// Translate
								String t = getI18n().tr(text);
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
