package iut.info3.betterstravadroid.entity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PathEntity {

    private final String nom;
    private final String description;
    private final long date;
    private final List<PointEntity> points;
    private final List<PointInteretEntity> pointsInteret;
    private int duree;

    public PathEntity(final String nom, final String description, final long date) {
        this.nom = nom;
        this.description = description;
        this.date = date;

        points = new ArrayList<>();
        pointsInteret = new ArrayList<>();
    }

    public void addPoint(final PointEntity point) {
        points.add(point.getCopy());
    }

    public void addPointInteret(final PointInteretEntity pointInteret) {
        pointsInteret.add(pointInteret.getCopy());
    }

    public void setDuree(final int duree) {
        this.duree = duree;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();

        json.put("nom", nom);
        json.put("description", description);
        json.put("date", date);
        json.put("points", getPointsParcoursToJson());
        json.put("pointsInterets", getPointsInteretToJson());
        json.put("duree", duree);

        return json;
    }

    private JSONArray getPointsParcoursToJson() throws JSONException {
        JSONArray arr = new JSONArray();
        for (PointEntity p : points) {
            arr.put(p.toJson());
        }

        return arr;
    }

    private JSONArray getPointsInteretToJson() throws JSONException {
        JSONArray arr = new JSONArray();
        for (PointInteretEntity p : pointsInteret) {
            arr.put(p.toJson());
        }

        return arr;
    }

    public PointEntity getLastPosition() {
        if (points.size() > 0) {
            return points.get(points.size() - 1).getCopy();
        } else {
            return null;
        }
    }
}
