package drones.util;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class MapObjectDeserialiser implements JsonDeserializer<MapObject> {

	/**
	 * Deserialiser for impassable map objects in GeoJSON
	 */
	@Override
	public MapObject deserialize(JsonElement json, Type t, JsonDeserializationContext ctxt)
			throws JsonParseException {
		MapObject building = new MapObject();
		JsonArray points = json.getAsJsonObject().getAsJsonObject("geometry").getAsJsonArray("coordinates");
		
		// Check for erroneous secondary array wrapper
		if (points.size() > 0 && points.get(0).getAsJsonArray().get(0).isJsonArray()) {
			points = points.get(0).getAsJsonArray();
		}

		// Iterate over points, marking the bounding box as it goes
		for (int i = 0; i < points.size(); i++) {
			JsonArray coord = points.get(i).getAsJsonArray();
			building.addPoint(coord.get(1).getAsDouble(), coord.get(0).getAsDouble());
		}
		
		return building;
	}

}
