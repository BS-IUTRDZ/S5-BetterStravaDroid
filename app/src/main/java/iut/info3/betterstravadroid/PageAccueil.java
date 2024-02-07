package iut.info3.betterstravadroid;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polyline;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import iut.info3.betterstravadroid.api.PathApi;
import iut.info3.betterstravadroid.api.UserApi;
import iut.info3.betterstravadroid.databinding.PageAccueilBinding;
import iut.info3.betterstravadroid.preferences.UserPreferences;

public class PageAccueil extends Fragment {

    private PageAccueilBinding binding;
    private Context context;
    private SharedPreferences preferences;
    private RequestBuilder helper;
    private ToastMaker toastMaker;

    public PageAccueil() {
        //Require empty public constructor
    }

    public static PageAccueil newInstance() {
        PageAccueil pageAccueil = new PageAccueil();
        return pageAccueil;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = PageAccueilBinding.inflate(inflater, container, false);
        View vue = binding.getRoot();
        context = vue.getContext();

        binding.cardLastRun.map.setDestroyMode(false);

        //Gestion des preferences
        preferences = this.getActivity().getSharedPreferences(UserPreferences.PREFERENCES_FILE, MODE_PRIVATE);

        helper = new RequestBuilder(vue.getContext());

        afficherUserInfos();
        afficherParcours();

        return vue;


    }

    /**
     * Accès au serveur API pour récupérer et mettre à jour
     * le dernier parcours de l'utilisateur
     */
    public void afficherParcours() {
        JSONObject body = new JSONObject();
        try {
            body.put(UserPreferences.USER_KEY_TOKEN, preferences.getString(UserPreferences.USER_KEY_TOKEN, "None"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        // On envoie la requête de recupération du dernier parcours de l'utilisateur
        helper.onSucces(this::setViewParcours)
                .onError(this::handleError)
                .withBody(body)
                .newJSONObjectRequest(PathApi.PATH_API_LAST)
                .send();
    }

    /**
     * Accès au serveur API pour récupérer et mettre à jour
     * les informations de l'utilisateur
     */
    private void afficherUserInfos() {
        JSONObject body = new JSONObject();
        try {
            body.put(UserPreferences.USER_KEY_TOKEN, preferences.getString(UserPreferences.USER_KEY_TOKEN, "None"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        // On envoie la requête de recupération des infos de l'utilisateur
        helper.onSucces(this::setViewContent)
                .onError(this::handleError)
                .withBody(body)
                .newJSONObjectRequest(UserApi.USER_API_INFOS)
                .send();
    }

    /**
     * En cas de réponse positive de la route "/user/getInfo"
     * Positionne sur l'UI:
     * <ul>
     *     <li>le dernier parcours de l'utilisateur</li>
     *     <li>stats de l'utilisateur sur 30 jours</li>
     *     <li>stats de l'utilisateur depuis la création de son compte</li>
     * </ul>
     * @param object réponse de l'API
     */
    public void setViewContent(Object object) {
        JSONObject response = (JSONObject) object;
        try {
            // Date du jour
            binding.tvDateDuJour.setText(LocalDate.now().format(
                    DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", Locale.FRANCE)));

            // Infos de l'utilisateur
            JSONObject infoUser = (JSONObject) response.get("user");

            binding.tvBonjourUtilisateur.setText(
                    String.format(getString(R.string.tv_bonjour_utilisateur), infoUser.getString(UserPreferences.USER_KEY_SURNAME)));

            // Stats 30 derniers jours
            JSONObject stats30Jours = (JSONObject) response.get("30jours");

            binding.tvDistance30J.setText(stats30Jours.getString(UserPreferences.STAT_KEY_DISTANCE));

            float dureeParcours30Jours = Float.parseFloat(stats30Jours.getString(UserPreferences.STAT_KEY_TIME));
            float heureParcours30Jours = dureeParcours30Jours / 3600;
            float minParcours30Jours = dureeParcours30Jours  % 3600 / 60;
            binding.tvTpsParcoursHeure30J.setText(Integer.toString(Math.round(heureParcours30Jours)));
            binding.tvTpsParcoursMinute30J.setText(Integer.toString(Math.round(minParcours30Jours)));

            binding.tvParcoursCrees30J.setText(stats30Jours.getString(UserPreferences.STAT_KEY_NB_PATH));

            // Stats globales
            JSONObject statsGlobales = (JSONObject) response.get("global");
            binding.tvDistanceGlob.setText(statsGlobales.getString(UserPreferences.STAT_KEY_DISTANCE));

            float dureeParcoursGlobal = Float.parseFloat(statsGlobales.getString(UserPreferences.STAT_KEY_TIME));
            float heureParcoursGlobal = dureeParcoursGlobal / 3600;
            float minParcoursGlobal = dureeParcoursGlobal % 3600 / 60;
            binding.tvTpsParcoursHeureGlob.setText(Integer.toString(Math.round(heureParcoursGlobal)));
            binding.tvTpsParcoursMinuteGlob.setText(Integer.toString(Math.round(minParcoursGlobal)));

            binding.tvParcoursCreesGlob.setText(statsGlobales.getString(UserPreferences.STAT_KEY_NB_PATH));

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * En cas de réponse positive de la route "/path/lastPath"
     * Positionne sur l'UI:
     * <ul>
     *     <li>la carte du dernier parcours</li>
     *     <li>le titre du dernier parcours</li>
     *     <li>la description du dernier parcours</li>
     * </ul>
     * @param object réponse de l'API
     */
    public void setViewParcours(Object object) {
        JSONObject response = (JSONObject) object;
        try {
            // Dernier parcours
            binding.cardLastRun.titreDernierParcours.setText(
                    response.getString(UserPreferences.PATH_KEY_NAME)
            );
            binding.cardLastRun.descriptionDernierParcours.setText(
                    response.getString(UserPreferences.PATH_KEY_DESCRIPTION)
            );

            // Gestion de la carte en arrière plan
            Polyline line = new Polyline(binding.cardLastRun.map);
            List<GeoPoint> trajet = new ArrayList<>();

            // Création du trajet
            JSONObject points = response.getJSONObject("points");
            for (int i = 0; i < points.length(); i++) {
                double latitude = (double) points.getJSONObject("point" + i).get("latitude");
                double longitude = (double) points.getJSONObject("point" + i).get("longitude");
                trajet.add(new GeoPoint(latitude, longitude));
            }

            line.getOutlinePaint().setColor(Color.RED);
            line.getOutlinePaint().setStrokeWidth(10);
            line.setPoints(trajet);
            line.setGeodesic(true);

            binding.cardLastRun.map.zoomToBoundingBox(line.getBounds(), false);

            // Ajout de l'overlay du trajet sur la carte
            binding.cardLastRun.map.getOverlayManager().add(line);

            binding.cardLastRun.map.addOnFirstLayoutListener((v, left, top, right, bottom) -> {
                binding.cardLastRun.map.zoomToBoundingBox(line.getBounds(), false, 200);
                binding.cardLastRun.map.getController().setCenter(line.getBounds().getCenterWithDateLine());

                // On laisse de la place vers le bas pour que le trajet ne soit pas caché par la
                // cardview qui contient les infos du trajet
                binding.cardLastRun.map.scrollBy(0, 100);
                binding.cardLastRun.map.invalidate();
            });
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Affichage d'un toast en cas d'erreur de l'API
     * @param error erreur envoyée par l'API
     */
    public void handleError(VolleyError error) {
        try {
            JSONObject reponse = new JSONObject(new String(error.networkResponse.data));
            String message = reponse.optString("erreur");
            toastMaker.makeText(context, message, Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            toastMaker.makeText(context,
                                "Erreur lors de la récupération des informations",
                                Toast.LENGTH_LONG)
                                .show();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.i("PageAccueil", "onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        Log.i("PageAccueil", "onPause");
        super.onPause();
    }

    @Override
    public void onResume() {
        Log.i("PageAccueil", "onResume");
        super.onResume();
    }
}
