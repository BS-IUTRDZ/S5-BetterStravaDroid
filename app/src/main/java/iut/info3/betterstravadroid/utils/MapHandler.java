package iut.info3.betterstravadroid.utils;

import static androidx.appcompat.content.res.AppCompatResources.getDrawable;

import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

import iut.info3.betterstravadroid.R;

public class MapHandler {

    /**
     * Affiche sur un widget mapview les points qui constituent un parcours
     * @param points les points du parcours obtenus par l'API
     * @param map le widget mapview
     * @param context le context courant
     * @param isSynthese boolean pour indiquer si on provient de la page synthese
     *                   si true alors on enleve l'espace supplémentaire qui sureleve la carte
     */
    public static void setMapViewContent(JSONArray points, MapView map, Context context, boolean isSynthese) {
        // Gestion de la carte en arrière plan
        Polyline line = new Polyline(map);
        List<GeoPoint> trajet = new ArrayList<>();

        // Création du trajet
        try {
            for (int i = 0; i < points.length(); i++) {
                JSONObject point = points.getJSONObject(i);
                double latitude = (double) point.get("latitude");
                double longitude = (double) point.get("longitude");
                trajet.add(new GeoPoint(latitude, longitude));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        line.getOutlinePaint().setColor(Color.RED);
        line.getOutlinePaint().setStrokeWidth(10);
        line.setPoints(trajet);
        line.setGeodesic(true);
        line.setInfoWindow(null);

        map.zoomToBoundingBox(line.getBounds(), false);

        // Ajout de l'overlay du trajet sur la carte
        map.getOverlayManager().add(line);

        // Centrage de la carte
        map.zoomToBoundingBox(line.getBounds(), false, 200);
        map.getController().setCenter(line.getBounds().getCenterWithDateLine());

        // On laisse de la place vers le bas pour que le trajet ne soit pas caché par la
        // cardview qui contient les infos du trajet
        if (!isSynthese) {
            map.scrollBy(0, 100);
        }
        map.invalidate();

        // Desactivation du zoom
        map.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        }
        );

        // Icônes de départ et de fin du parcours
        if (!trajet.isEmpty()) {
            Marker startMarker = new Marker(map);
            startMarker.setPosition(trajet.get(0));
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            startMarker.setIcon(getDrawable(context, R.drawable.start));
            startMarker.setInfoWindow(null);
            map.getOverlays().add(startMarker);

            Marker finishMarker = new Marker(map);
            finishMarker.setPosition(trajet.get(trajet.size() - 1));
            finishMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            finishMarker.setIcon(getDrawable(context, R.drawable.finish));
            finishMarker.setInfoWindow(null);
            map.getOverlays().add(finishMarker);
        }
    }

    /**
     * Affiche sur un widget mapview les points d'interets d'un parcours
     * @param points les points d'interets du parcours
     * @param map le widget mapview
     * @param context le context courant
     */
    public static void setMapViewInterestPoint(JSONArray points, MapView map, Context context) {

        // Création des points d'interets
        try {
            for (int i = 0; i < points.length(); i++) {
                JSONObject point = points.getJSONObject(i);
                JSONObject coord = point.getJSONObject("coordonnees");
                double latitude = (double) coord.get("latitude");
                double longitude = (double) coord.get("longitude");

                Marker marker = new Marker(map);
                marker.setPosition(new GeoPoint(latitude, longitude));
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                marker.setIcon(getDrawable(context, R.drawable.pin));
                marker.setInfoWindow(null);
                map.getOverlays().add(marker);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

}
