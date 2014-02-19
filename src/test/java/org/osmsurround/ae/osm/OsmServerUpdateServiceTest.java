package org.osmsurround.ae.osm;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.osmsurround.ae.TestBase;
import org.osmsurround.ae.entity.Amenity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.social.test.client.MockRestServiceServer;
import org.springframework.social.test.client.RequestMatchers;
import org.springframework.social.test.client.ResponseCreators;
import org.springframework.web.client.RestTemplate;

public class OsmServerUpdateServiceTest extends TestBase {

	@Autowired
	private OsmUpdateService osmServerUpdateService;
	@Autowired
	private RestTemplate restTemplate;
	private MockRestServiceServer mockServer;
	@Value("${overpassApiBaseUrl}")
	protected String overpassApiBaseUrl;

	@Before
	public void setup() {
		mockServer = MockRestServiceServer.createServer(restTemplate);
	}

	@Test
	public void testGetOsmData() throws Exception {

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(MediaType.APPLICATION_XML);
		mockServer
				.expect(RequestMatchers
						.requestTo(overpassApiBaseUrl
								+ "?data=(node%5B'amenity'%5D(49.27,9.15,49.3,9.19);node%5B'shop'%5D(49.27,9.15,49.3,9.19);node%5B'man_made'%5D(49.27,9.15,49.3,9.19);way%5B'amenity'%5D(49.27,9.15,49.3,9.19);way%5B'shop'%5D(49.27,9.15,49.3,9.19);way%5B'man_made'%5D(49.27,9.15,49.3,9.19););out%20body;%3E;out%20skel;"))
				.andExpect(RequestMatchers.method(HttpMethod.GET))
				.andRespond(
						ResponseCreators.withResponse(new ClassPathResource("/bbox.xml", getClass()), responseHeaders));

		BoundingBox boundingBox = new BoundingBox();
		boundingBox.setWest(9.15);
		boundingBox.setEast(9.19);
		boundingBox.setNorth(49.30);
		boundingBox.setSouth(49.27);

		List<Amenity> amenities = osmServerUpdateService.getOsmDataAsAmenities(boundingBox);
		assertEquals(76, amenities.size());
	}
}
