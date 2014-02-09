package org.osmsurround.ae.templates.serializer;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.osm.preset.schema.Item;
import org.osmsurround.ae.TestBase;
import org.osmsurround.ae.templates.TemplatesService;
import org.springframework.beans.factory.annotation.Autowired;

public class TemplatesServiceTest extends TestBase {

	@Autowired
	private TemplatesService templatesService;

	@Autowired
	private CustomObjectMapper mapper;

	@Test
	public void testSerialization() throws Exception {
		List<Item> localizedNodeTemplates = templatesService.getViewTemplates();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		mapper.writeValue(out, localizedNodeTemplates);
		Assert.assertTrue(out.size() > 0);
	}
}