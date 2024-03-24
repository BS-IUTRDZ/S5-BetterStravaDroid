package iut.info3.betterstravadroid.fragments;

import static androidx.appcompat.content.res.AppCompatResources.getDrawable;

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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.util.constants.OverlayConstants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.location.Criteria;

import com.android.volley.Request;

import iut.info3.betterstravadroid.activities.FragmentContainerActivity;
import iut.info3.betterstravadroid.R;
import iut.info3.betterstravadroid.tools.api.PathApi;
import iut.info3.betterstravadroid.tools.api.RequestBuilder;
import iut.info3.betterstravadroid.databinding.FragmentPathBinding;
import iut.info3.betterstravadroid.databinding.PopupInterestPointBinding;
import iut.info3.betterstravadroid.entities.PathEntity;
import iut.info3.betterstravadroid.entities.PointEntity;
import iut.info3.betterstravadroid.entities.PointInteretEntity;
import iut.info3.betterstravadroid.preferences.UserPreferences;


public class PathFragment extends Fragment {

    /** Visual indicator positioned on the person’s position map */
    private MyLocationNewOverlay myLocationOverlay;

    /**  Location manager through the phone sensors */
    private LocationManager locationManager = null;

    /** List of crossing points of the route */
    private ArrayList<GeoPoint> trajet;

    /** Drawing of the course on the map */
    private Polyline line;

    /** Boolean pause/ playback of the course recording */
    public static Boolean play = false;

    /** Indicates whether a recording takes place */
    public static Boolean parcours = false;

    private String fournisseur;

    /** List of items on the overlay */
    private ArrayList<OverlayItem> items = new ArrayList<>();

    /**  Popup manager for starting recording or positioning point of interest */
    private AlertDialog popup;

    /** Object responsible for linking this class to the path page layout */
    private FragmentPathBinding binding;

    private Context context;

    /** API request builder */
    private RequestBuilder helper;

    /** Thread in charge of calculating the duration of the journey */
    private Thread threadTimer;

    /** Thread in charge of calculating the average speed */
    private Thread threadVitesseMoyenne;

    /** Duration of current journey */
    private int dureeParcours;

    /** Entity representing the current journey */
    private PathEntity parcoursEnCours;

    /** List of points of interest positioned on the map, only the display */
    private List<ItemizedOverlayWithFocus<OverlayItem>> mapPointsInterets = new ArrayList<>();

    /** Geolocation request component */
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result) {
                    // PERMISSION GRANTED

                    // User Location Layer Configuration
                    GpsMyLocationProvider locationProvider = new GpsMyLocationProvider(context);
                    myLocationOverlay = new MyLocationNewOverlay(locationProvider, binding.mapview);
                    myLocationOverlay.enableFollowLocation();

                    myLocationOverlay.enableMyLocation();
                    binding.mapview.getOverlays().add(myLocationOverlay);

                    centerMapOnUser();
                } else {
                    // PERMISSION NOT GRANTED
                    Toast.makeText( context,
                            getString(R.string.path_permission_refused), Toast.LENGTH_LONG).show();
                }
            }
    );

    LocationListener ecouteurGPS = localisation -> {
        if (play) {
            addWaypointToPath(localisation);
        }
    };

    public PathFragment() {
        //Require empty public constructor
    }

    public static PathFragment newInstance() {
        return new PathFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentPathBinding.inflate(inflater, container, false);
        View vue = binding.getRoot();
        context = vue.getContext();

        helper = new RequestBuilder(context);

        trajet = new ArrayList<>();
        line = new Polyline(binding.mapview);
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);

        Configuration.getInstance().load(context,
                PreferenceManager.getDefaultSharedPreferences(context));


        //Zoom out with fingers and not with button
        binding.mapview.setBuiltInZoomControls(false);
        binding.mapview.setMultiTouchControls(true);
        binding.mapview.setDestroyMode(false);

        centerMapOnUser();

        initLocalisation();

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // User Location Layer Configuration
            GpsMyLocationProvider locationProvider = new GpsMyLocationProvider(context);
            myLocationOverlay = new MyLocationNewOverlay(locationProvider, binding.mapview);
            myLocationOverlay.enableFollowLocation();

            myLocationOverlay.enableMyLocation();
            binding.mapview.getOverlays().add(myLocationOverlay);
        }

        /* Listener button page */
        binding.btnAjout.setOnClickListener(v -> {
            if (play) {
                showPopupPoint(false);
            }
        });

        binding.btnStart.setOnClickListener(v -> {
            showPopupPoint(true);
        });

        binding.btnStop.setOnTouchListener((v, event) -> {
            if (v.isPressed() && event.getAction() == MotionEvent.ACTION_UP) {
                long eventDuration = event.getEventTime() - event.getDownTime();
                if (eventDuration > ViewConfiguration.getLongPressTimeout()) {
                    buttonStopPressed();
                }
            }
            return false;
        });

        binding.mapview.getOverlayManager().add(line);
        return vue;
    }

    /**
     * If the geolocation of the user is allowed, center the map on it.
     */
    private void centerMapOnUser() {
        if (binding.mapview != null && binding.mapview.getController() != null) {
            // Check if location access permission is granted
            Location lastKnownLocation = getCurrentUserLocation();

            if (lastKnownLocation != null) {
                double latitude = lastKnownLocation.getLatitude();
                double longitude = lastKnownLocation.getLongitude();

                // Create a GeoPoint with coordinates
                GeoPoint userLocation = new GeoPoint(latitude, longitude);

                trajet.add(userLocation);

                // Center the map on the user position
                binding.mapview.getController().setCenter(userLocation);
                binding.mapview.getController().setZoom(18.0);

            } else {
                Toast.makeText(context,
                        getString(R.string.path_no_position), Toast.LENGTH_LONG).show();
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
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Configuration.getInstance().save(context,
                PreferenceManager.getDefaultSharedPreferences(context));
        if (binding.mapview != null) {
            binding.mapview.onPause();
        }

        if (myLocationOverlay != null) {
            myLocationOverlay.disableMyLocation(); // Disables location tracking
        }
    }

    /**
     * Method to initialize localization. Called during fragment init.
     */
    private void initLocalisation()
    {
        if (locationManager == null)
        {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            Criteria criteres = new Criteria();

            fournisseur = locationManager.getBestProvider(criteres, true);
        }

        if (fournisseur != null)
        {
            // last known position
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                return;
            }

            Location localisation = locationManager.getLastKnownLocation(fournisseur);
            if(localisation != null)
            {
                // the location is notified
                ecouteurGPS.onLocationChanged(localisation);
            }

            // automatic update is configured: at least 10 meters and 15 seconds
            locationManager.requestLocationUpdates(fournisseur, 5000, 2, ecouteurGPS);
        }
    }

    /**
     * Method called when clicking on the "Start" button,
     * centers the map on the user, triggers the recording
     * and starting of the indicator calculations.
     */
    private void buttonStartPressed() {
        parcours = true;
        play = true;
        binding.btnStart.setVisibility(View.INVISIBLE);
        binding.btnStop.setVisibility(View.VISIBLE);
        binding.cardViewStop.setCardBackgroundColor(0xFFC3363E);
        ((FragmentContainerActivity) getLayoutInflater().getContext()).binding.navbar.pauseButton.setVisibility(View.VISIBLE);
        ((FragmentContainerActivity) getLayoutInflater().getContext()).binding.navbar.playButton.setVisibility(View.INVISIBLE);

        trajet.clear();
        centerMapOnUser();
        startThreadTimer();
        addWaypointToPath(getCurrentUserLocation());
    }

    /**
     * Method called when clicking the "Stop" button.
     */
    private void buttonStopPressed() {
        parcours = false;
        play = false;
        binding.btnStop.setVisibility(View.INVISIBLE);
        binding.btnStart.setVisibility(View.VISIBLE);
        binding.cardViewStop.setCardBackgroundColor(0xFF4478c2);
        ((FragmentContainerActivity) getLayoutInflater().getContext()).binding.navbar.pauseButton.setVisibility(View.INVISIBLE);
        ((FragmentContainerActivity) getLayoutInflater().getContext()).binding.navbar.playButton.setVisibility(View.VISIBLE);

        trajet.clear();
        line.setPoints(trajet);
        binding.mapview.getOverlays().removeAll(mapPointsInterets);
        mapPointsInterets.clear();

        // We stop the thread of the chronometer
        if (threadTimer != null) {
            threadTimer.interrupt();
            //We reinitialize the visual indicator of the clock
            binding.tvTpsParcoursHeure.setText("00");
            binding.tvTpsParcoursMinute.setText("00");
        }
        //Reset the visual speed indicator
        binding.tvVitesseMoyenne.setText(String.format(Locale.FRANCE, "%.2f", 0.00));

        //We send the route to the API
        sendPathToApi();
    }

    /**
     * Method called when clicking on the "Add a point of interest" button
     * and when creating a new route.
     */
    private void showPopupPoint(final boolean nouveauParcours) {

        PopupInterestPointBinding alertBinding = PopupInterestPointBinding.inflate(LayoutInflater.from(context));
        AlertDialog.Builder popup_builder = new AlertDialog.Builder(context);
        popup_builder.setView(alertBinding.getRoot());

        /* Listener buttons popup*/
        alertBinding.btnCancel.setOnClickListener(v -> popup.dismiss());
        alertBinding.btnConfirm.setOnClickListener(v -> {
            if (nouveauParcours) {
                // The popup is used to create a new route
                if(!alertBinding.etTitre.getText().toString().isEmpty()) {
                    parcoursEnCours = new PathEntity(alertBinding.etTitre.getText().toString(),
                            alertBinding.etDescription.getText().toString(),
                            Calendar.getInstance().getTime().getTime());
                    popup.dismiss();
                    buttonStartPressed();
                }
            } else {
                // The popup is used to add a point of interest
                if(!alertBinding.etTitre.getText().toString().isEmpty()) {
                    pointInteretConfirmTitleDescription(
                            alertBinding.etTitre.getText().toString(),
                            alertBinding.etDescription.getText().toString()
                    );
                }
            }
        });

        popup = popup_builder.create();
        popup.show();
    }

    /**
     * Method called when confirming title and point of interest description.
     */
    public void pointInteretConfirmTitleDescription(final String title, final String description) {

        PointEntity lastPosition = parcoursEnCours.getLastPosition();
        if (lastPosition == null) {
            Toast.makeText(context, getString(R.string.path_point_error), Toast.LENGTH_LONG).show();
        } else {
            // We add the point of interest on the overlay
            items.add(new OverlayItem(title, description,
                    new GeoPoint(lastPosition.getLat(), lastPosition.getLon())));

            // Set up the listener when clicking on the point of interest
            ItemizedOverlayWithFocus<OverlayItem> mOverlay =
                    new ItemizedOverlayWithFocus<OverlayItem>(items,
                            getDrawable(context, R.drawable.pin),
                            null,
                            OverlayConstants.NOT_SET,
                            new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                                @Override
                                public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                                    Toast.makeText(context,
                                            item.getTitle() + "\n" + item.getSnippet(), Toast.LENGTH_LONG).show();
                                    return true;
                                }

                                @Override
                                public boolean onItemLongPress(final int index, final OverlayItem item) {
                                    return false;
                                    //TODO si on veut supprimer point d'interet
                                }
                            },
                            context);

            // We add the point of interest to the course
            parcoursEnCours.addPointInteret(new PointInteretEntity(
                    lastPosition, title, description));

            //mOverlay.setFocusItemsOnTap(true);
            binding.mapview.getOverlays().add(mOverlay);
            mapPointsInterets.add(mOverlay);
            popup.dismiss();
        }
    }

    /**
     * Thread to create the timer of the duration of the course
     */
    private void startThreadTimer() {
        threadTimer = new Thread() {
            @Override
            public void run() {
                try {
                    // Reset the time
                    dureeParcours = 0;
                    long currentTime;

                    // Start the timer
                    while (!isInterrupted()) {
                        currentTime = System.currentTimeMillis();
                        if (play) {
                            dureeParcours += 1;
                        }

                        // We update the time if we are more than a minute apart
                        if (dureeParcours % 60 == 0) {
                            long finalTimeInSeconds = dureeParcours;
                            ((FragmentContainerActivity) getLayoutInflater().getContext()).runOnUiThread(() -> {
                                binding.tvTpsParcoursHeure.setText(String.format(Locale.FRANCE, "%02d", finalTimeInSeconds / 3600));
                                binding.tvTpsParcoursMinute.setText(String.format(Locale.FRANCE, "%02d",(finalTimeInSeconds % 3600) / 60));
                            });
                        }

                        // Waiting for the rest of the second that was not used during treatment
                        Thread.sleep(1000 - (System.currentTimeMillis() - currentTime));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        threadTimer.start();
    }

    /**
     * Average Speed Calculation Thread Declaration
     */
    private void averageSpeedCalculation() {

        if (threadVitesseMoyenne != null && threadVitesseMoyenne.isAlive()) { return; }

        // The average speed calculation system is defined
        threadVitesseMoyenne = new Thread(() -> {
            try {
                double distance = 0;

                // Calculate the distance travelled
                for (int i = 0; i < trajet.size() - 1; i++) {
                    distance += trajet.get(i).distanceToAsDouble(trajet.get(i + 1));
                }

                // The average speed is calculated
                double vitesseMoyenne = distance / dureeParcours;

                // The display is updated
                ((FragmentContainerActivity) getLayoutInflater().getContext()).runOnUiThread(() -> {
                    binding.tvVitesseMoyenne.setText(String.format(Locale.FRANCE, "%.2f", vitesseMoyenne));
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        threadVitesseMoyenne.start();
    }

    /**
     * Method of sending the path to the API to save it.
     */
    private void sendPathToApi() {

        // We put the final duration of the course
        parcoursEnCours.setDuree(dureeParcours);

        // Preparing the course JSON
        JSONObject parcoursJson = null;
        try {
            parcoursJson = parcoursEnCours.toJson();
        } catch (JSONException e) {
            Toast.makeText(context, getString(R.string.path_save_error), Toast.LENGTH_LONG).show();
        }

        // We send the route to the API
        if (parcoursJson != null) {
            HashMap<String, String> token = new HashMap<>();
            token.put("token", context.getSharedPreferences(UserPreferences.PREFERENCE_FILE,
                    Context.MODE_PRIVATE).getString(UserPreferences.USER_KEY_TOKEN, null));

            helper.withBody(parcoursJson)
                    .withHeader(token)
                    .onError((error) -> {
                        Toast.makeText(context, getString(R.string.path_save_error), Toast.LENGTH_LONG).show();
                    })
                    .onSucces((response) -> {
                        Toast.makeText(context, getString(R.string.path_save_complete), Toast.LENGTH_LONG).show();
                        FragmentContainerActivity fragmentContainerActivity = (FragmentContainerActivity) getActivity();
                        if (fragmentContainerActivity != null) {
                            fragmentContainerActivity.refreshAll();
                        }
                    })
                    .method(Request.Method.POST)
                    .newJSONObjectRequest(PathApi.API_PATH_CREATE)
                    .send();
        }
    }

    /**
     * Method to obtain the current position of the user
     * @return the current position of the user
     */
    public Location getCurrentUserLocation() {
        Location lastKnownLocation = null;
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            // Get the current position of the user
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        return lastKnownLocation;
    }

    /**
     * Method for adding a waypoint to the route.
     */
    public void addWaypointToPath(Location point) {
        if (point != null) {
            trajet.add(new GeoPoint(point.getLatitude(), point.getLongitude()));
            line.getOutlinePaint().setColor(Color.RED);
            line.getOutlinePaint().setStrokeWidth(10);
            line.setPoints(trajet);
            line.setGeodesic(true);

            parcoursEnCours.addPoint(new PointEntity(point.getLatitude(),
                                                     point.getLongitude(),
                                                     point.getAltitude()));

            averageSpeedCalculation();
        }
    }
}
