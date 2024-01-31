package iut.info3.betterstravadroid;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;

import android.location.Criteria;

import iut.info3.betterstravadroid.databinding.PageParcoursBinding;


public class PageParcours extends Fragment implements View.OnClickListener{
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MyLocationNewOverlay myLocationOverlay;
    private boolean isOnMap = false;
    LocationManager locationManager = null;

    ArrayList<GeoPoint> trajet;
    Polyline line;

    private Boolean play = true;

    private String fournisseur;
    ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
    private double gLatitute;
    private double gLongitude;
    TextView title;
    TextView description;
    private AlertDialog popup;

    private PageParcoursBinding binding;

    private Context context;

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
                binding.mapview.getOverlayManager().add(line);

                //map.invalidate();
            }
        }
    };

    public PageParcours() {
        //Require empty public constructor
    }

    public static PageParcours newInstance() {
        PageParcours pageParcours = new PageParcours();
        return pageParcours;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = PageParcoursBinding.inflate(inflater, container, false);
        View vue = binding.getRoot();
        context = vue.getContext();

        trajet = new ArrayList<>();
        line = new Polyline(binding.mapview);
        checkLocationPermission(((MainActivity) inflater.getContext()));

        Configuration.getInstance().load(context,
                PreferenceManager.getDefaultSharedPreferences(context));


        //Dezoome avec doigt et pas avec boutons
        binding.mapview.setBuiltInZoomControls(false);
        binding.mapview.setMultiTouchControls(true);

        centerMapOnUser();

        initialiserLocalisation();

        CompassOverlay compassOverlay = new CompassOverlay(context, binding.mapview);
        compassOverlay.enableCompass();
        binding.mapview.getOverlays().add(compassOverlay);

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Configuration de la couche de localisation de l'utilisateur
            GpsMyLocationProvider locationProvider = new GpsMyLocationProvider(context);
            myLocationOverlay = new MyLocationNewOverlay(locationProvider, binding.mapview);
            myLocationOverlay.enableFollowLocation();

            myLocationOverlay.enableMyLocation();
            binding.mapview.getOverlays().add(myLocationOverlay);
        }

        /* Listener bouton page */
        binding.btnAjout.setOnClickListener(this);
        binding.btnStart.setOnClickListener(this);
        binding.btnStop.setOnClickListener(this);

        return vue;
    }

    // Vérifier la permission d'accès à la localisation
    private void checkLocationPermission(MainActivity activity) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void centerMapOnUser() {
        if (binding.mapview != null && binding.mapview.getController() != null) {
            // Vérifier si la permission d'accès à la localisation est accordée
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                // Obtenir la position actuelle de l'utilisateur
                LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (lastKnownLocation != null) {
                    double latitude = lastKnownLocation.getLatitude();
                    double longitude = lastKnownLocation.getLongitude();

                    // Créer un GeoPoint avec les coordonnées
                    GeoPoint userLocation = new GeoPoint(latitude, longitude);

                    trajet.add(userLocation);

                    // Créer MyLocationNewOverlay
                    GpsMyLocationProvider locationProvider = new GpsMyLocationProvider(context);
                    myLocationOverlay = new MyLocationNewOverlay(locationProvider, binding.mapview);
                    myLocationOverlay.enableMyLocation();

                    // Ajouter MyLocationNewOverlay à la carte
                    binding.mapview.getOverlays().add(myLocationOverlay);

                    // Centrer la carte sur la position de l'utilisateur
                    binding.mapview.getController().setCenter(userLocation);
                    binding.mapview.getController().setZoom(18.0);

                } else {
                    // La dernière position connue n'est pas disponible, vous pouvez gérer cela en affichant un message par exemple
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Configuration.getInstance().load(context,
                PreferenceManager.getDefaultSharedPreferences(context));
        if (binding.mapview != null) {
            binding.mapview.onResume();
            isOnMap = true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Configuration.getInstance().save(context,
                PreferenceManager.getDefaultSharedPreferences(context));
        if (binding.mapview != null) {
            binding.mapview.onPause();
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

    private void initialiserLocalisation()
    {
        if (locationManager == null)
        {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
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
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
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
            locationManager.requestLocationUpdates(fournisseur, 10000, 10, ecouteurGPS);
        }
    }

    public void pauseButton(View view){

        play = false;
        view.setVisibility(View.INVISIBLE);
//        findViewById(R.id.playButton).setVisibility(View.VISIBLE); //BOUTON Navbar de merde TODO

    }

    public void playButton(View view){

        play = true;
        view.setVisibility(View.INVISIBLE);
//        findViewById(R.id.pauseButton).setVisibility(View.VISIBLE); //BOUTON Navbar de merde TODO

    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.btn_ajout) {
            showAboutPopup();
        } else if (view.getId() == R.id.btn_start) {
            view.setVisibility(View.INVISIBLE);
            binding.btnStop.setVisibility(View.VISIBLE);
            binding.cardViewStop.setCardBackgroundColor(0xFFC3363E);
        } else if (view.getId() == R.id.btn_stop) {
            view.setVisibility(View.INVISIBLE);
            binding.btnStart.setVisibility(View.VISIBLE);
            binding.cardViewStop.setCardBackgroundColor(0xFF4478c2);
        } else if (view.getId() == R.id.btn_confirm) {
            confirmTitleDescription(view);
        } else if (view.getId() == R.id.btn_cancel) {
            popup.dismiss();
        }
    }

    private void showAboutPopup() {

        AlertDialog.Builder popup_builder = new AlertDialog.Builder(context);


        View customLayout = getLayoutInflater().inflate(R.layout.popup_interest_point, null);
        title = customLayout.findViewById(R.id.et_titre);
        description = customLayout.findViewById(R.id.et_description);

        /* Listener boutons popup*/
        customLayout.findViewById(R.id.btn_cancel).setOnClickListener(this);
        customLayout.findViewById(R.id.btn_confirm).setOnClickListener(this);


        popup_builder.setView(customLayout);

        popup = popup_builder.create();
        popup.show();

    }

    public void confirmTitleDescription(View view){

        items.add(new OverlayItem(title.getText().toString(), description.getText().toString(), new GeoPoint(gLatitute, gLongitude)));

        ItemizedOverlayWithFocus<OverlayItem> mOverlay = new ItemizedOverlayWithFocus<OverlayItem>(context, items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        Toast.makeText( context,
                                item.getTitle() + "\n" + item.getSnippet(), Toast.LENGTH_LONG).show();
                        return true;
                    }
                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return false;
                    }
                });
        //mOverlay.setFocusItemsOnTap(true);
        binding.mapview.getOverlays().add(mOverlay);
        popup.dismiss();
    }

}
