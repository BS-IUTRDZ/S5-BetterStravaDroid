package iut.info3.betterstravadroid;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
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
import java.util.List;
import java.util.Locale;

import iut.info3.betterstravadroid.api.UserApi;
import iut.info3.betterstravadroid.databinding.PageAccueilBinding;
import iut.info3.betterstravadroid.preferences.UserPreferences;

public class PageAccueil extends Fragment {

    private PageAccueilBinding binding;

    private SharedPreferences preferences;
    private RequestBuilder helper;

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

        binding.cardLastRun.map.setDestroyMode(false);

        //Gestion des preferences
        preferences = this.getActivity().getSharedPreferences(UserPreferences.PREFERENCES_FILE, MODE_PRIVATE);

        helper = new RequestBuilder(vue.getContext());

        afficherUserInfos();
        afficherParcours();

        return vue;


    }

    public void afficherParcours() {
        Polyline line = new Polyline(binding.cardLastRun.map);
        List<GeoPoint> trajet = new ArrayList<>();
        GeoPoint centre;

        // Création du trajet
        // BOUCHON
        trajet.add(new GeoPoint(44.36336875796618, 2.5737746730180295));
        trajet.add(new GeoPoint(44.36164391301725, 2.5703912027612827));

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
    }

    private void afficherUserInfos() {
        JSONObject body = new JSONObject();
        try {
            body.put(UserPreferences.USER_KEY_TOKEN, preferences.getString(UserPreferences.USER_KEY_TOKEN, "None"));
            Log.i("BODYJson", body.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // On envoie la requête de connexion au serveur
        helper.onSucces(this::handleResponse)
                .onError(this::handleError)
                .withBody(body)
                .newJSONObjectRequest(UserApi.USER_API_INFOS)
                .send();
    }

    public void handleResponse(Object object) {
        JSONObject response = (JSONObject) object;
        try {
            // Date du jour
            binding.tvDateDuJour.setText(LocalDate.now().format(
                    DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", Locale.FRANCE)));

            // Infos de l'utilisateur
            JSONObject infoUser = (JSONObject) response.get("user");

            binding.tvBonjourUtilisateur.setText(
                    String.format(getString(R.string.tv_bonjour_utilisateur), infoUser.getString(UserPreferences.USER_KEY_NAME)));

            // Dernier parcours
            JSONObject infoDernierParcours = (JSONObject) response.get("parcours");
            binding.cardLastRun.titreDernierParcours.setText(
                    infoDernierParcours.getString(UserPreferences.PATH_KEY_NAME)
            );
            binding.cardLastRun.descriptionDernierParcours.setText(
                    infoDernierParcours.getString(UserPreferences.PATH_KEY_DESCRIPTION)
            );


            // Stats 30 derniers jours
            JSONObject stats30Jours = (JSONObject) response.get("30jours");

            binding.tvDistance30J.setText(stats30Jours.getString(UserPreferences.STAT_KEY_DISTANCE));

            float dureeParcours30Jours = Float.parseFloat(stats30Jours.getString(UserPreferences.STAT_KEY_TIME));
            float heureParcours30Jours = dureeParcours30Jours / 3600;
            float minParcours30Jours = dureeParcours30Jours % 60;
            binding.tvTpsParcoursHeure30J.setText(Integer.toString(Math.round(heureParcours30Jours)));
            binding.tvTpsParcoursMinute30J.setText(Integer.toString(Math.round(minParcours30Jours)));

            binding.tvParcoursCrees30J.setText(stats30Jours.getString(UserPreferences.STAT_KEY_NB_PATH));

            // Stats globales
            JSONObject statsGlobales = (JSONObject) response.get("global");
            binding.tvDistanceGlob.setText(statsGlobales.getString(UserPreferences.STAT_KEY_DISTANCE));

            float dureeParcoursGlobal = Float.parseFloat(statsGlobales.getString(UserPreferences.STAT_KEY_TIME));
            float heureParcoursGlobal = dureeParcoursGlobal / 3600;
            float minParcoursGlobal = dureeParcoursGlobal % 60;
            binding.tvTpsParcoursHeureGlob.setText(Integer.toString(Math.round(heureParcoursGlobal)));
            binding.tvTpsParcoursMinuteGlob.setText(Integer.toString(Math.round(minParcoursGlobal)));

            binding.tvParcoursCreesGlob.setText(statsGlobales.getString(UserPreferences.STAT_KEY_NB_PATH));

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }


    public void handleError(VolleyError error) {
        Log.i("ReponseKo", "Requete Ko");
        //TODO toast d'erreur
        try {
            JSONObject reponse = new JSONObject(new String(error.networkResponse.data));
//            String message = reponse.optString("erreur");
//            toastMaker.makeText(this, message, Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
//            toastMaker.makeText(this, "Erreur lors de la connexion", Toast.LENGTH_LONG).show();
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
