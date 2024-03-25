package iut.info3.betterstravadroid.tools;

import static androidx.appcompat.content.res.AppCompatResources.getDrawable;

import android.content.Context;
import android.graphics.Color;

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

/**
 * Class in charge of managing mapview widget.
 * Allows display on the widget of routes and points of interest.
 */
public class MapHandler {

    /**
     * Displays on a mapview widget the points that constitute a route.
     * @param points the points of the route obtained by the API
     * @param map the mapview widget
     * @param context the current context
     * @param isSynthesis boolean to indicate if we come from the synthesis page
     *                   if true then we remove the additional space that overraises the map
     */
    public static void setMapViewContent(JSONArray points, MapView map, Context context, boolean isSynthesis) {
        //Deletion of the content of the card in the case of a refresh
        map.getOverlayManager().clear();

        // Map management
        Polyline line = new Polyline(map);
        List<GeoPoint> trajet = new ArrayList<>();

        // Path creation
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

        // Added route overlay on map
        map.getOverlayManager().add(line);

        // Map centering
        map.getController().setCenter(line.getBounds().getCenterWithDateLine());
        map.zoomToBoundingBox(line.getBounds(), false);
        map.getController().setZoom(map.getZoomLevelDouble() - 2.0);

        /* We leave room down so that the route is not hidden
           by the cardview that contains the route information */
        if (!isSynthesis) {
            map.scrollBy(0, 100);
        }
        map.invalidate();

        // Zoom deactivation
        map.setOnTouchListener((v, event) -> true);

        // Start and end icons of the route
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
     * Displays on a mapview widget the points of interest of a route.
     * @param points the points of interest of the route
     * @param map the mapview widget
     * @param context the current context
     */
    public static void setMapViewInterestPoint(JSONArray points, MapView map, Context context) {

        // Creation of points of interest
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
