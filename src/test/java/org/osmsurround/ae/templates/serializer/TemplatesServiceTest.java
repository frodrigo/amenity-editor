package org.osmsurround.ae.templates.serializer;

import java.io.ByteArrayOutputStream;

import org.junit.Assert;
import org.junit.Test;
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
		Object localizedNodeTemplates = templatesService.getViewTemplate(null);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		mapper.writeValue(out, localizedNodeTemplates);
		Assert.assertTrue(out.size() > 0);
	}
}
