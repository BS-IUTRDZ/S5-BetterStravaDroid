package iut.info3.betterstravadroid.parcours.entity;

import org.json.JSONException;
import org.json.JSONObject;

public class PointEntity {

    private final double lat;
    private final double lon;
    private final double alt;

    public PointEntity(final double lat, final double lon, final double alt) {
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
    }

    public PointEntity getCopy() {
        return new PointEntity(lat, lon, alt);
    }

    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("lat", lat);
        obj.put("lon", lon);
        obj.put("alt", alt);

        return obj;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public double getAlt() {
        return alt;
    }
}
