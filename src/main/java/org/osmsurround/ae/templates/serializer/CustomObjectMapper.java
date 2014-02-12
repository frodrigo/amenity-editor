package org.osmsurround.ae.templates.serializer;

import java.util.Locale;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ser.CustomSerializerFactory;
import org.osm.preset.schema.Check;
import org.osm.preset.schema.Checkgroup;
import org.osm.preset.schema.Chunk;
import org.osm.preset.schema.Item;
import org.osm.preset.schema.Label;
import org.osm.preset.schema.Link;
import org.osm.preset.schema.ListEntry;
import org.osm.preset.schema.Multiselect;
import org.osm.preset.schema.Optional;
import org.osm.preset.schema.Reference;
import org.osm.preset.schema.Role;
import org.osm.preset.schema.Text;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

@Component("jacksonObjectMapper")
public class CustomObjectMapper extends ObjectMapper {

	public CustomObjectMapper() {
		Locale locale = LocaleContextHolder.getLocale();
		I18n i18n = I18nFactory.getI18n(getClass(), "org.osm.preset.Messages", locale);

		CustomSerializerFactory sf = new CustomSerializerFactory();
		sf.addSpecificMapping(Item.class, new ItemSerializer(i18n));
		sf.addSpecificMapping(Chunk.class, new ChunkSerializer(i18n));
		sf.addSpecificMapping(Reference.class, new ReferenceSerializer(i18n));
		sf.addSpecificMapping(Optional.class, new OptionalSerializer(i18n));
		sf.addSpecificMapping(Checkgroup.class, new CheckgroupSerializer(i18n));
		sf.addSpecificMapping(Link.class, new LinkSerializer(i18n));
		sf.addSpecificMapping(Text.class, new IntrospectionSerializer(i18n));
		sf.addSpecificMapping(ListEntry.class, new IntrospectionSerializer(i18n));
		sf.addSpecificMapping(Label.class, new IntrospectionSerializer(i18n));
		sf.addSpecificMapping(Multiselect.class, new IntrospectionSerializer(i18n));
		sf.addSpecificMapping(Role.class, new IntrospectionSerializer(i18n));
		sf.addSpecificMapping(Check.class, new IntrospectionSerializer(i18n));
		this.setSerializerFactory(sf);
	}
}
