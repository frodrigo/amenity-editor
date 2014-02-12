package org.osmsurround.ae.templates.serializer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
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
						String text = object.toString();
						if (methodName.equals("getText")
								|| methodName.equals("getTextContext")
								|| (className.equals("ListEntry") && (methodName.equals("getDisplayValue") || methodName
										.equals("getShortDescription")))) {
							String t = i18n.tr(text);
							if (t != null) {
								text = t;
							}
						}
						String token = methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
						token = token.replaceAll("(\\B[A-Z])", "_$1").toLowerCase();
						jgen.writeStringField(token, text);
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
