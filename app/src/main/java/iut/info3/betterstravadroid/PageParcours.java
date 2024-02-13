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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.TextView;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import android.location.Criteria;

import com.android.volley.VolleyError;

import iut.info3.betterstravadroid.api.ApiConfiguration;
import iut.info3.betterstravadroid.databinding.PageParcoursBinding;
import iut.info3.betterstravadroid.preferences.UserPreferences;


public class PageParcours extends Fragment {
    private MyLocationNewOverlay myLocationOverlay;
    private boolean isOnMap = false;
    LocationManager locationManager = null;

    ArrayList<GeoPoint> trajet;
    Polyline line;

    public static Boolean play = false;

    public static Boolean parcours = false;
    private String fournisseur;
    ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
    private double gLatitude;
    private double gLongitude;
    TextView title;
    TextView description;
    private AlertDialog popup;

    private PageParcoursBinding binding;

    private Context context;
    private RequestBuilder helper;
    public static final String API_REQUEST_CREATE_PATH = ApiConfiguration.API_BASE_URL + "path/createPath";
    public static final String API_REQUEST_ADD_POINT = ApiConfiguration.API_BASE_URL + "path/addPoint";
    private String parcoursId;
    private String parcoursTitre;
    private String parcoursDescription;
    public Boolean nouveauParcours = false;
    private Thread threadTimer;
    private static final String TAG = "PageParcours";
    private int dureeParcours;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result) {
                    // PERMISSION GRANTED

                    // Configuration de la couche de localisation de l'utilisateur
                    GpsMyLocationProvider locationProvider = new GpsMyLocationProvider(context);
                    myLocationOverlay = new MyLocationNewOverlay(locationProvider, binding.mapview);
                    myLocationOverlay.enableFollowLocation();

                    myLocationOverlay.enableMyLocation();
                    binding.mapview.getOverlays().add(myLocationOverlay);

                    centerMapOnUser();
                } else {
                    // PERMISSION NOT GRANTED
                    Toast.makeText( context,
                            "Permission non accordée", Toast.LENGTH_LONG).show();
                }
            }
    );

    LocationListener ecouteurGPS = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location localisation) {

            if (play) {
                trajet.add(new GeoPoint(localisation.getLatitude(), localisation.getLongitude()));

                gLatitude = localisation.getLatitude();
                gLongitude = localisation.getLongitude();

                line.getOutlinePaint().setColor(Color.RED);
                line.getOutlinePaint().setStrokeWidth(10);
                line.setPoints(trajet);
                line.setGeodesic(true);

                pushGeoPoint();
            }
        }
    };

    public PageParcours() {
        //Require empty public constructor
    }

    public static PageParcours newInstance() {
        return new PageParcours();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = PageParcoursBinding.inflate(inflater, container, false);
        View vue = binding.getRoot();
        context = vue.getContext();

        helper = new RequestBuilder(context);

        trajet = new ArrayList<>();
        line = new Polyline(binding.mapview);
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);

        Configuration.getInstance().load(context,
                PreferenceManager.getDefaultSharedPreferences(context));


        //Dezoome avec doigt et pas avec boutons
        binding.mapview.setBuiltInZoomControls(false);
        binding.mapview.setMultiTouchControls(true);
        binding.mapview.setDestroyMode(false);

        centerMapOnUser();

        initialiserLocalisation();

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
        binding.btnAjout.setOnClickListener(v -> {
            if (play) {
                showPopupPoint();
            }
        });

        binding.btnStart.setOnClickListener(v -> {
            nouveauParcours = true;
            showPopupPoint();
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

                    // Centrer la carte sur la position de l'utilisateur
                    binding.mapview.getController().setCenter(userLocation);
                    binding.mapview.getController().setZoom(18.0);

                } else {
                    Toast.makeText(context,
                            "Aucune position trouvée", Toast.LENGTH_LONG).show();
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

    /**
     * Méthode pour initialiser la localisation. Appelée lors de l'init du fragment.
     */
    private void initialiserLocalisation()
    {
        if (locationManager == null)
        {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            Criteria criteres = new Criteria();

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
            locationManager.requestLocationUpdates(fournisseur, 5000, 2, ecouteurGPS);
        }
    }

    /**
     * Méthode appelée lors du clic sur le bouton "Start"
     */
    private void buttonStartPressed() {
        parcours = true;
        play = true;
        binding.btnStart.setVisibility(View.INVISIBLE);
        binding.btnStop.setVisibility(View.VISIBLE);
        binding.cardViewStop.setCardBackgroundColor(0xFFC3363E);
        ((MainActivity) getLayoutInflater().getContext()).binding.navbar.pauseButton.setVisibility(View.VISIBLE);
        ((MainActivity) getLayoutInflater().getContext()).binding.navbar.playButton.setVisibility(View.INVISIBLE);

        trajet.clear();
        centerMapOnUser();
        createPath();
        startThreadTimer();
    }

    /**
     * Méthode appelée lors du clic sur le bouton "Stop"
     */
    private void buttonStopPressed() {
        parcours = false;
        play = false;
        binding.btnStop.setVisibility(View.INVISIBLE);
        binding.btnStart.setVisibility(View.VISIBLE);
        binding.cardViewStop.setCardBackgroundColor(0xFF4478c2);
        ((MainActivity) getLayoutInflater().getContext()).binding.navbar.pauseButton.setVisibility(View.INVISIBLE);
        ((MainActivity) getLayoutInflater().getContext()).binding.navbar.playButton.setVisibility(View.VISIBLE);

        nouveauParcours = false;
        trajet.clear();
        line.setPoints(trajet);
        //items.clear();

        // On stoppe le thread du chrono
        if (threadTimer != null) {
            threadTimer.interrupt();
        }
    }

    /**
     * Méthode appelée lors du clic sur le bouton "Ajouter un point d'intérêt" et lors de
     * la création d'un nouveau parcours.
     */
    private void showPopupPoint() {

        AlertDialog.Builder popup_builder = new AlertDialog.Builder(context);

        View customLayout = getLayoutInflater().inflate(R.layout.popup_interest_point, null);
        title = customLayout.findViewById(R.id.et_titre);
        description = customLayout.findViewById(R.id.et_description);

        /* Listener boutons popup*/
        customLayout.findViewById(R.id.btn_cancel).setOnClickListener(v -> popup.dismiss());
        customLayout.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            if (nouveauParcours) {
                parcoursTitre = title.getText().toString();
                parcoursDescription = description.getText().toString();
                popup.dismiss();
                buttonStartPressed();
            } else{
                confirmTitleDescription(null);
            }
        });

        popup_builder.setView(customLayout);

        popup = popup_builder.create();
        popup.show();
    }

    /**
     * Méthode appelée lors de la confirmation du titre et de la description du point d'intérêt
     * @param view la vue (non utilisée)
     */
    public void confirmTitleDescription(View view){

        items.add(new OverlayItem(title.getText().toString(), description.getText().toString(), new GeoPoint(gLatitude, gLongitude)));

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
                        //TODO si on veut supprimer point d'interet
                    }
                });
        //mOverlay.setFocusItemsOnTap(true);
        binding.mapview.getOverlays().add(mOverlay);
        popup.dismiss();
    }

    /**
     * Méthode de création du parcours dans l'API. Permet d'obtenir l'id du parcours créé.
     */
    public void createPath() {
        try {
            JSONObject object = new JSONObject();
            object.put("description", parcoursDescription);
            object.put("nom", parcoursTitre);
            object.put("date", Calendar.getInstance().getTime().getTime());
            Log.i("PageParcours", object.toString());

            HashMap<String, String> token = new HashMap<>();
            token.put("token", context.getSharedPreferences(UserPreferences.PREFERENCE_FILE, Context.MODE_PRIVATE).getString(UserPreferences.USER_KEY_TOKEN, null));

            helper.withBody(object)
                    .withHeader(token)
                    .onError(error -> {
                        Log.i("ReponseKo", "Requete Ko");
                        try {
                            JSONObject reponse = new JSONObject(new String(error.networkResponse.data));
                        } catch (JSONException e) {
                            ToastMaker toastMaker = new ToastMaker();
                            Toast toast = toastMaker.makeText(context, "Erreur lors de la création du parcours", Toast.LENGTH_SHORT);
                        }
                    })
                    .onSucces(o -> {
                        try {
                            JSONObject response = (JSONObject) o;
                            parcoursId = response.getString("id");
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .newJSONObjectRequest(API_REQUEST_CREATE_PATH)
                    .send();
        } catch (IllegalArgumentException e) {
            //Toast toast = toastMaker.makeText(this, e.getMessage(), Toast.LENGTH_SHORT);
            //toast.show();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Méthode permettant d'ajouter un point géographique à un parcours via son id.
     */
    public void pushGeoPoint() {
        try {
            JSONObject object = new JSONObject();
            object.put("id", parcoursId);
            object.put("longitude", gLongitude);
            object.put("latitude", gLatitude);
            Log.i("IdParcours", parcoursId);

            new RequestBuilder(context).withBody(object)
                    .newJSONObjectRequest(API_REQUEST_ADD_POINT)
                    .send();
        } catch (IllegalArgumentException e) {
            //Toast toast = toastMaker.makeText(this, e.getMessage(), Toast.LENGTH_SHORT);
            //toast.show();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Thread pour créer le timer de la durée du parcours
     */
    private void startThreadTimer() {
        threadTimer = new Thread() {
            @Override
            public void run() {
                try {
                    // On réinitialise le temps
                    ((MainActivity) getLayoutInflater().getContext()).runOnUiThread(() -> {
                        binding.tvTpsParcoursHeure.setText("00");
                        binding.tvTpsParcoursMinute.setText("00");
                    });
                    dureeParcours = 0;
                    long currentTime;

                    // On démarre le timer
                    while (!isInterrupted()) {
                        currentTime = System.currentTimeMillis();
                        if (play) {
                            dureeParcours += 1;
                        }

                        // On met à jour le temps si on est à plus d'une minute de différence
                        if (dureeParcours % 60 == 0) {
                            long finalTimeInSeconds = dureeParcours;
                            ((MainActivity) getLayoutInflater().getContext()).runOnUiThread(() -> {
                                binding.tvTpsParcoursHeure.setText(String.format(Locale.FRANCE, "%02d", finalTimeInSeconds / 3600));
                                binding.tvTpsParcoursMinute.setText(String.format(Locale.FRANCE, "%02d",(finalTimeInSeconds % 3600) / 60));
                            });
                        }

                        // On attend le reste de la seconde qui n'a pas été utilisée pendant le traitement
                        Thread.sleep(1000 - (System.currentTimeMillis() - currentTime));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        threadTimer.start();
    }
}
