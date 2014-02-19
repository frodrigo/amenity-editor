package org.osmsurround.ae.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.osmsurround.ae.TestBase;
import org.osmsurround.ae.entity.Amenity;
import org.osmsurround.ae.entity.Node;
import org.springframework.beans.factory.annotation.Autowired;

public class InternalDataServiceTest extends TestBase {

	@Autowired
	private InternalDataService internalDataService;

	private Map<String, String> newDataMap;

	@Before
	public void setup() throws Exception {
		newDataMap = new HashMap<String, String>();
		newDataMap.put("key", "value");
		newDataMap.put("name", "myname");
	}

	@Test
	public void testUpdateInternalData() throws Exception {
		Amenity amenity = createAmenity(Node.OsmType.NODE, 1);
		internalDataService.updateInternalData(amenity.getOsmType(), 1, amenity);
	}

	@Test
	public void testInsertInternalData() throws Exception {
		Amenity amenity = new Amenity(Node.OsmType.NODE, 200, 49.1, 10.0, newDataMap);
		amenity.setVersion(2);
		internalDataService.insertInternalData(amenity.getOsmType(), amenity.getNodeId(), amenity);
	}

	@Test
	public void testDeleteInternalData() throws Exception {
		internalDataService.deleteInternalData(Node.OsmType.NODE, 1);
		internalDataService.deleteInternalData(Node.OsmType.NODE, 2);
	}

	@Test
	public void testUpdateAmenities() throws Exception {
		List<Amenity> amenities = new ArrayList<Amenity>();
		amenities.add(createAmenity(Node.OsmType.NODE, 1));
		amenities.add(createAmenity(Node.OsmType.NODE, 2));
		amenities.add(createAmenity(Node.OsmType.NODE, 3));
		internalDataService.updateAmenities(amenities);

	}

	private Amenity createAmenity(Node.OsmType osmType, long nodeId) {
		Amenity amenity = new Amenity(osmType, nodeId, 49.1, 10.0, newDataMap);
		amenity.setVersion(2);
		return amenity;
	}

}
