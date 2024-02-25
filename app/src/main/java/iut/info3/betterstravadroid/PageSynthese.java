package iut.info3.betterstravadroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import iut.info3.betterstravadroid.api.PathApi;
import iut.info3.betterstravadroid.databinding.PageSyntheseBinding;
import iut.info3.betterstravadroid.preferences.UserPreferences;
import iut.info3.betterstravadroid.utils.MapHandler;

public class PageSynthese extends AppCompatActivity {

    private PageSyntheseBinding binding;
    private SharedPreferences preferences;
    private RequestBuilder requestBuilder;
    private ToastMaker toastMaker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = PageSyntheseBinding.inflate(getLayoutInflater());
        View vue = binding.getRoot();
        setContentView(vue);

        Intent intention = getIntent();
        String pathId  = intention.getStringExtra("pathId");

        //Gestion des preferences
        preferences = getSharedPreferences(UserPreferences.PREFERENCE_FILE, MODE_PRIVATE);

        requestBuilder = new RequestBuilder(vue.getContext());
        toastMaker = new ToastMaker();

        binding.topbar.ivBackIcon.setOnClickListener(view -> back());

        getPath(pathId);
    }

    private void back() {
        finish();
    }


    /**
     * Accès au serveur API pour récupérer le parcours
     */
    private void getPath(String pathId) {
        String token = preferences.getString(UserPreferences.USER_KEY_TOKEN, "None");
        requestBuilder.onSucces(this::setViewContent)
                .onError(this::handleError)
                .addHeader(UserPreferences.USER_KEY_TOKEN, token)
                .newJSONObjectRequest(PathApi.PATH_API_ID + pathId)
                .send();
    }

    /**
     * En cas de réponse positive de la route "/path/id"
     * Positionne sur l'UI:
     * <ul>
     *     <li>le parcours correspondant à l'id</li>
     *     <li>stats de du parcours </li>
     *     <li>liste des points d'interets</li>
     * </ul>
     * @param object réponse de l'API
     */
    public void setViewContent(Object object) {
        JSONObject response = (JSONObject) object;
        try {

            Calendar c = Calendar.getInstance();
            c.setTimeInMillis((Long) response.get("date"));
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
            binding.cardRun.parcourDate.setText(sdf.format(c.getTime()));
            binding.cardRun.parcourTitre.setText((String) response.get("nom"));
            binding.cardRun.parcourDescription.setText((String) response.get("description"));

            JSONObject stats = response.getJSONObject("statistiques");
            binding.speedStat.tvSyntheseStat.setText(Double.toString((Double) stats.get("vitesseMoyenne")));

            float heureParcours = (int) stats.get("duree") / 3600;
            float minParcours = (float) (((int) stats.get("duree")) % 3600) / 60;
            String duree = String.valueOf((int) heureParcours) + ':' + String.valueOf((int) minParcours);
            binding.timeStat.tvSyntheseStat.setText(duree);

            binding.distanceStat.tvSyntheseStat.setText(Double.toString((Double) stats.get("distance")));
            String denivPos = "+ " + stats.get("denivPos");
            binding.altitudeStat.tvLeftSyntheseStat.setText(denivPos);
            String denivNeg = "- " + stats.get("denivNeg");
            binding.altitudeStat.tvRightSyntheseStat.setText(denivNeg);

            JSONArray points = response.getJSONArray("points");
            MapHandler.setMapViewContent(points, binding.mapview, this);

            JSONArray pointsInterets = response.getJSONArray("pointsInterets");
            binding.interestStat.tvSyntheseStat.setText(String.valueOf(pointsInterets.length()));
            MapHandler.setMapViewInterestPoint(pointsInterets, binding.mapview, this);

            for (int i = 0; i < pointsInterets.length(); i++) {
                JSONObject pointInt = pointsInterets.getJSONObject(i);
                String titre = (String) pointInt.get("nom");
                String description = (String) pointInt.get("description");

                View card = View.inflate(this, R.layout.interest_point_item, binding.syntheseMain);
                TextView textViewTitre = card.findViewById(R.id.tv_titre);
                textViewTitre.setText(titre);
                TextView textViewDescription = card.findViewById(R.id.tv_description);
                textViewDescription.setText(description);
            }
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
            toastMaker.makeText(this, message, Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            toastMaker.makeText(this,
                            "Erreur lors de la récupération des informations",
                            Toast.LENGTH_LONG)
                    .show();
        }
    }

}