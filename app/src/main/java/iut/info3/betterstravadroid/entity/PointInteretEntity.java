package iut.info3.betterstravadroid.entity;

import org.json.JSONException;
import org.json.JSONObject;

public class PointInteretEntity {

    private PointEntity pos;
    private String nom;
    private String description;

    public PointInteretEntity(final PointEntity pos, final String nom, final String description) {
        this.pos = pos;
        this.nom = nom;
        this.description = description;
    }

    public PointInteretEntity getCopy() {
        return new PointInteretEntity(pos.getCopy(), nom, description);
    }

    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("pos", pos.toJson());
        obj.put("nom", nom);
        obj.put("description", description);

        return obj;
    }
}
