package org.osmsurround.ae.templates;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.osm.preset.schema.Item;
import org.osmsurround.ae.TestBase;
import org.springframework.beans.factory.annotation.Autowired;

public class TemplatesServiceTest extends TestBase {

	@Autowired
	private TemplatesService templatesService;

	@Test
	public void testGetViewTemplates() throws Exception {
		List<Item> localizedNodeTemplates = templatesService.getViewTemplates();
		assertNotNull(localizedNodeTemplates);
	}
}
