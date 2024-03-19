package iut.info3.betterstravadroid;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import java.util.HashMap;

import iut.info3.betterstravadroid.api.PathApi;
import iut.info3.betterstravadroid.databinding.PageSyntheseBinding;
import iut.info3.betterstravadroid.preferences.UserPreferences;

import iut.info3.betterstravadroid.utils.MapHandler;

public class PageSynthese extends AppCompatActivity {

    public static final String KEY_PAGE = "page";
    public static final String HOME_PAGE = "home";
    public static final String PATH_PAGE = "path";
    public static final String KEY_FORCE_REFRESH = "refresh";

    private PageSyntheseBinding binding;
    private SharedPreferences preferences;
    private RequestBuilder requestBuilder;
    private ToastMaker toastMaker;

    private String pathId;

    private Context context;

    private AlertDialog popup;

    private ActivityResultLauncher<Intent> lanceur;
    private boolean forceRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = PageSyntheseBinding.inflate(getLayoutInflater());
        View vue = binding.getRoot();
        context = vue.getContext();
        setContentView(vue);

        lanceur = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::retourModif);

        Intent intention = getIntent();
        pathId  = intention.getStringExtra("pathId");

        //Gestion des preferences
        preferences = getSharedPreferences(UserPreferences.PREFERENCE_FILE, MODE_PRIVATE);

        requestBuilder = new RequestBuilder(vue.getContext());
        toastMaker = new ToastMaker();

        binding.topbar.ivBackIcon.setOnClickListener(view -> clickNavbar(PATH_PAGE));
        binding.topbar.ivEditIcon.setOnClickListener(view -> {toEdit();});

        binding.navbar.playButton.setVisibility(View.INVISIBLE);
        binding.navbar.pauseButton.setVisibility(View.INVISIBLE);

        binding.navbar.homeButtonInactive.setOnClickListener(v -> clickNavbar(HOME_PAGE));
        binding.navbar.pathButtonInactive.setOnClickListener(v -> clickNavbar(PATH_PAGE));
        binding.topbar.ivTrashIcon.setOnClickListener(view -> {affichPopUpSupr(view);});

        forceRefresh = false;
        getPath(pathId);
    }


    private void clickNavbar(String pageName) {
        Intent intent = new Intent();
        intent.putExtra(KEY_PAGE, pageName);
        intent.putExtra(KEY_FORCE_REFRESH, forceRefresh);
        setResult(Activity.RESULT_OK, intent);
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
    private void setViewContent(Object object) {
        JSONObject response = (JSONObject) object;
        try {

            Calendar c = Calendar.getInstance();
            c.setTimeInMillis((Long) response.get("date"));
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
            binding.cardRun.parcourDate.setText(sdf.format(c.getTime()));
            binding.cardRun.tvTitre.setText((String) response.get("nom"));
            binding.cardRun.tvDescription.setText((String) response.get("description"));

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
            MapHandler.setMapViewContent(points, binding.mapview, this, true);

            JSONArray pointsInterets = response.getJSONArray("pointsInterets");
            binding.interestStat.tvSyntheseStat.setText(String.valueOf(pointsInterets.length()));
            MapHandler.setMapViewInterestPoint(pointsInterets, binding.mapview, this);

            for (int i = 0; i < pointsInterets.length(); i++) {
                JSONObject pointInt = pointsInterets.getJSONObject(i);
                String titre = (String) pointInt.get("nom");
                String description = (String) pointInt.get("description");

                View card = View.inflate(this, R.layout.interest_point_item, binding.syntheseMain);
                TextView textViewTitre = card.findViewById(R.id.pi_titre);
                textViewTitre.setText(titre);
                TextView textViewDescription = card.findViewById(R.id.pi_description);
                textViewDescription.setText(description);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void toEdit() {
        Intent intent =
                new Intent(PageSynthese.this,
                        PageModifParcours.class);

        String description = binding.cardRun.tvDescription.getText().toString();
        String titre = binding.cardRun.tvTitre.getText().toString();

        intent.putExtra("titre",titre);
        intent.putExtra("description",description );
        intent.putExtra("id",pathId);

        lanceur.launch(intent);

    }

    public void retourModif(ActivityResult result){
        Intent intent = result.getData();

        if (result.getResultCode() == Activity.RESULT_OK) {
            binding.cardRun.tvDescription.setText(intent.getStringExtra("description"));
            forceRefresh = true;
        }
    }

    public void affichPopUpSupr(View view){
        AlertDialog.Builder popup_builder = new AlertDialog.Builder(context);

        View customLayout = getLayoutInflater().inflate(R.layout.popup_supression_parcours, null);

        customLayout.findViewById(R.id.btn_confirm).setOnClickListener(view1 -> {confirmSupr(view1);});
        customLayout.findViewById(R.id.btn_cancel).setOnClickListener(view1 -> {popup.dismiss();});

        popup_builder.setView(customLayout);
        popup = popup_builder.create();
        popup.show();


    }

    public void confirmSupr(View view){

        JSONObject body = new JSONObject();
        HashMap<String,String> header = new HashMap<>();
        try {
            body.put("id", pathId);
            header.put(UserPreferences.USER_KEY_TOKEN, preferences.getString(UserPreferences.USER_KEY_TOKEN, "None"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        requestBuilder.withHeader(header)
                .onError(this::handleError)
                .onSucces(o -> {
                    Intent intent = new Intent();
                    intent.putExtra(KEY_PAGE, PATH_PAGE);
                    intent.putExtra(KEY_FORCE_REFRESH, true);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                })
                .withBody(body)
                .method(Request.Method.PUT)
                .newJSONObjectRequest(PathApi.PATH_API_SUPR)
                .send();

        popup.dismiss();
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
                            "Erreur lors de la modification de la description",
                            Toast.LENGTH_LONG)
                    .show();
        }
    }

}