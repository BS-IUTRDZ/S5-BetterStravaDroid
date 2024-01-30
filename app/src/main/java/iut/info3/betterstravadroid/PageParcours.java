package iut.info3.betterstravadroid;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;

import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.Objects;

import android.location.Criteria;


public class PageParcours extends AppCompatActivity {
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView map = null;
    private MyLocationNewOverlay myLocationOverlay;
    private boolean isOnMap = false;
    LocationManager locationManager = null;

    ArrayList<GeoPoint> trajet;
    Polyline line = new Polyline(map);

    private Boolean play = true;

    private String fournisseur;
    ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
    private double gLatitute;
    private double gLongitude;
    TextView title;
    TextView description;
    private AlertDialog popup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_parcours);

        trajet = new ArrayList<>();

        checkLocationPermission();

        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        map = findViewById(R.id.mapview);

        //Dezoome avec doigt et pas avec boutons
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);

        centerMapOnUser();

        initialiserLocalisation();

        CompassOverlay compassOverlay = new CompassOverlay(this, map);
        compassOverlay.enableCompass();
        map.getOverlays().add(compassOverlay);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Configuration de la couche de localisation de l'utilisateur
            GpsMyLocationProvider locationProvider = new GpsMyLocationProvider(this);
            myLocationOverlay = new MyLocationNewOverlay(locationProvider, map);
            myLocationOverlay.enableFollowLocation();

            myLocationOverlay.enableMyLocation();
            map.getOverlays().add(myLocationOverlay);
        }
    }

    // Vérifier la permission d'accès à la localisation
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void centerMapOnUser() {
        if (map != null && map.getController() != null) {
            // Vérifier si la permission d'accès à la localisation est accordée
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                // Obtenir la position actuelle de l'utilisateur
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (lastKnownLocation != null) {
                    double latitude = lastKnownLocation.getLatitude();
                    double longitude = lastKnownLocation.getLongitude();

                    // Créer un GeoPoint avec les coordonnées
                    GeoPoint userLocation = new GeoPoint(latitude, longitude);

                    trajet.add(userLocation);

                    // Créer MyLocationNewOverlay
                    GpsMyLocationProvider locationProvider = new GpsMyLocationProvider(this);
                    myLocationOverlay = new MyLocationNewOverlay(locationProvider, map);
                    myLocationOverlay.enableMyLocation();

                    // Ajouter MyLocationNewOverlay à la carte
                    map.getOverlays().add(myLocationOverlay);

                    // Centrer la carte sur la position de l'utilisateur
                    map.getController().setCenter(userLocation);
                    map.getController().setZoom(18.0);

                } else {
                    // La dernière position connue n'est pas disponible, vous pouvez gérer cela en affichant un message par exemple
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        if (map != null) {
            map.onResume();
            isOnMap = true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Configuration.getInstance().save(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        if (map != null) {
            map.onPause();
            isOnMap = false; // L'utilisateur n'est plus sur la carte, désactive le suivi
        }

        if (myLocationOverlay != null) {
            myLocationOverlay.disableMyLocation(); // Désactive le suivi de la localisation
        }
    }

    // Manipulation de la réponse à la demande de permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission accordée centrage de la carte
                centerMapOnUser();
            } else {
                // Permission refusée
                // TODO message d'erreur disant que sans accepter bah l'appli elle marche pas
            }
        }
    }


    LocationListener ecouteurGPS = new LocationListener() {
        @Override
        public void onLocationChanged(Location localisation) {

            if (play) {
                trajet.add(new GeoPoint(localisation.getLatitude(), localisation.getLongitude()));

                gLatitute = localisation.getLatitude();
                gLongitude = localisation.getLongitude();

                //line.setSubDescription(Polyline.class.getCanonicalName());
                line.setWidth(10f);
                line.setColor(Color.RED);
                line.setPoints(trajet);
                line.setGeodesic(true);
                //line.setInfoWindow(new BasicInfoWindow(R.layout.bonuspack_bubble, map));
                map.getOverlayManager().add(line);

                //map.invalidate();
            }
        }
    };

    private void initialiserLocalisation()
    {
        if (locationManager == null)
        {
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            Criteria criteres = new Criteria();

            /*// la précision  : (ACCURACY_FINE pour une haute précision ou ACCURACY_COARSE pour une moins bonne précision)
            criteres.setAccuracy(Criteria.ACCURACY_FINE);

            // l'altitude
            criteres.setAltitudeRequired(true);

            // la direction
            criteres.setBearingRequired(true);

            // la vitesse
            criteres.setSpeedRequired(true);

            // la consommation d'énergie demandée
            criteres.setCostAllowed(true);
            //criteres.setPowerRequirement(Criteria.POWER_HIGH);
            criteres.setPowerRequirement(Criteria.POWER_MEDIUM);*/

            fournisseur = locationManager.getBestProvider(criteres, true);
        }

        if (fournisseur != null)
        {
            // dernière position connue
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                return;
            }

            Location localisation = locationManager.getLastKnownLocation(fournisseur);
            if(localisation != null)
            {
                // on notifie la localisation
                ecouteurGPS.onLocationChanged(localisation);
            }

            // on configure la mise à jour automatique : au moins 10 mètres et 15 secondes
            locationManager.requestLocationUpdates(fournisseur, 5000, 5, ecouteurGPS);
        }
    }

    public void pauseButton(View view){

        play = false;
        view.setVisibility(View.INVISIBLE);
        findViewById(R.id.playButton).setVisibility(View.VISIBLE);

    }

    public void playButton(View view){

        play = true;
        view.setVisibility(View.INVISIBLE);
        findViewById(R.id.pauseButton).setVisibility(View.VISIBLE);

    }

    public void addPoint(View view){
        showAboutPopup();
    }

    private void showAboutPopup() {

        AlertDialog.Builder popup_builder = new AlertDialog.Builder(this);


        View customLayout = getLayoutInflater().inflate(R.layout.popup_interest_point, null);
        title = customLayout.findViewById(R.id.et_titre);
        description = customLayout.findViewById(R.id.et_description);


        popup_builder.setView(customLayout);

        popup = popup_builder.create();
        popup.show();

    }

    public void confirmTitleDescription(View view){

        items.add(new OverlayItem(title.getText().toString(), description.getText().toString(), new GeoPoint(gLatitute, gLongitude)));

        ItemizedOverlayWithFocus<OverlayItem> mOverlay = new ItemizedOverlayWithFocus<OverlayItem>(getApplicationContext(), items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        Toast.makeText( getApplicationContext(),
                                item.getTitle() + "\n" + item.getSnippet(), Toast.LENGTH_LONG).show();
                        return true;
                    }
                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return false;
                    }
                });
        //mOverlay.setFocusItemsOnTap(true);
        map.getOverlays().add(mOverlay);
        popup.dismiss();
    }

    public void cancelTitleDescription(View view){
        popup.dismiss();
    }

    public void start(View view){
        view.setVisibility(View.INVISIBLE);
        findViewById(R.id.btn_stop).setVisibility(View.VISIBLE);
        ((CardView) findViewById(R.id.cardViewStop)).setCardBackgroundColor(0xFFC3363E);
    }

    public void stop(View view){
        view.setVisibility(View.INVISIBLE);
        findViewById(R.id.btn_start).setVisibility(View.VISIBLE);
        ((CardView) findViewById(R.id.cardViewStop)).setCardBackgroundColor(0xFF4478c2);
    }


}
