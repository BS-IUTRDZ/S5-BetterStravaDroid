package iut.info3.betterstravadroid.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import java.util.HashMap;

import iut.info3.betterstravadroid.R;
import iut.info3.betterstravadroid.tools.api.RequestBuilder;
import iut.info3.betterstravadroid.tools.api.PathApi;
import iut.info3.betterstravadroid.databinding.ComponentInterestPointItemBinding;
import iut.info3.betterstravadroid.databinding.ActivitySynthesisBinding;
import iut.info3.betterstravadroid.preferences.UserPreferences;

import iut.info3.betterstravadroid.tools.MapHandler;

public class SynthesisActivity extends AppCompatActivity {

    public static final String KEY_PAGE = "page";
    public static final String HOME_PAGE = "home";
    public static final String PATH_PAGE = "path";
    public static final String KEY_FORCE_REFRESH = "refresh";

    /** Object responsible for linking this class to the synthesis page layout */
    private ActivitySynthesisBinding binding;

    /** the preferences of the application */
    private SharedPreferences preferences;

    /** API request builder */
    private RequestBuilder requestBuilder;

    /** Current Route Id */
    private String pathId;

    private Context context;

    /** The route deletion popup */
    private AlertDialog popup;

    /** The change activity launcher associated with the course */
    private ActivityResultLauncher<Intent> launcher;

    /** Indicates if a refresh is needed after closing the synthesis */
    private boolean forceRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = ActivitySynthesisBinding.inflate(getLayoutInflater());
        View vue = binding.getRoot();
        context = vue.getContext();
        setContentView(vue);

        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::backFromEdit);

        Intent intention = getIntent();
        pathId  = intention.getStringExtra("pathId");

        // Preferences management
        preferences = getSharedPreferences(UserPreferences.PREFERENCE_FILE, MODE_PRIVATE);

        requestBuilder = new RequestBuilder(vue.getContext());

        binding.topbar.ivBackIcon.setOnClickListener(view -> clickNavbar(PATH_PAGE));
        binding.topbar.ivEditIcon.setOnClickListener(view -> {toEdit();});
        binding.topbar.ivTrashIcon.setOnClickListener(this::showDeletePopUp);

        // Hide the pause button
        binding.navbar.playButton.setVisibility(View.INVISIBLE);
        binding.navbar.pauseButton.setVisibility(View.INVISIBLE);

        binding.navbar.homeButtonInactive.setOnClickListener(v -> clickNavbar(HOME_PAGE));
        binding.navbar.pathButtonInactive.setOnClickListener(v -> clickNavbar(PATH_PAGE));

        forceRefresh = false;
        getPath(pathId);
    }

    /**
     * Navbar manager.
     * @param pageName the name of the page corresponding to the button pressed
     */
    private void clickNavbar(String pageName) {
        Intent intent = new Intent();
        intent.putExtra(KEY_PAGE, pageName);
        intent.putExtra(KEY_FORCE_REFRESH, forceRefresh);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    /**
     * Access to the API server to retrieve the route.
     * @param pathId the id of the path to collect
     */
    private void getPath(String pathId) {
        String token = preferences.getString(UserPreferences.USER_KEY_TOKEN, "None");
        requestBuilder.onSucces(this::setViewContent)
                .onError(this::handleError)
                .addHeader(UserPreferences.USER_KEY_TOKEN, token)
                .newJSONObjectRequest(PathApi.API_PATH_ID + pathId)
                .send();
    }

    /**
     * If the route "/path/id" responds positively. Position on UI:
     * <li>the path corresponding to the id</li>
     * <li>course stats </li>
     * <li>list of points of interest</li>
     * @param object API response
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
//            binding.speedStat.tvSyntheseStat.setText(Double.toString((Double) stats.get("vitesseMoyenne")));
            binding.speedStat.tvSyntheseStat.setText(String.format(Locale.FRANCE, "%.2f",
                    stats.getDouble("vitesseMoyenne")));


            float heureParcours = (int) stats.get("duree") / 3600;
            float minParcours = (float) (((int) stats.get("duree")) % 3600) / 60;
            String duree = String.valueOf((int) heureParcours) + ':' + String.valueOf((int) minParcours);
            binding.timeStat.tvSyntheseStat.setText(duree);

//            binding.distanceStat.tvSyntheseStat.setText(Double.toString((Double) stats.get("distance")));
            binding.distanceStat.tvSyntheseStat.setText(String.format(Locale.FRANCE, "%.2f",
                    stats.getDouble("distance")));

            String denivPos = "+ " + String.format(Locale.FRANCE, "%.2f", stats.getDouble("denivPos"));
            binding.altitudeStat.tvLeftSyntheseStat.setText(denivPos);
            String denivNeg = "- " + String.format(Locale.FRANCE, "%.2f", stats.getDouble("denivNeg"));;
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

                ComponentInterestPointItemBinding bindingPi = ComponentInterestPointItemBinding.inflate(getLayoutInflater(), null, false);
                CardView card = bindingPi.getRoot();

                bindingPi.piTitre.setText(titre);
                bindingPi.piDescription.setText(description);

                ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                lp.setMargins(0, 10, 0, 10);
                card.setLayoutParams(lp);


                binding.syntheseMain.addView(card);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Viewing the route edit page.
     */
    public void toEdit() {
        Intent intent =
                new Intent(SynthesisActivity.this,
                        UpdatePathActivity.class);

        String description = binding.cardRun.tvDescription.getText().toString();
        String titre = binding.cardRun.tvTitre.getText().toString();

        intent.putExtra("titre",titre);
        intent.putExtra("description",description );
        intent.putExtra("id",pathId);

        launcher.launch(intent);
    }

    /**
     * When returning from the modification page, forces the refresh
     * of the course if a change in the description took place.
     * @param result the result of the modification activity
     */
    public void backFromEdit(ActivityResult result){
        Intent intent = result.getData();

        if (result.getResultCode() == Activity.RESULT_OK) {
            binding.cardRun.tvDescription.setText(intent.getStringExtra("description"));
            forceRefresh = true;
        }
    }

    /**
     * Displays the route deletion popup.
     * @param view the current view
     */
    public void showDeletePopUp(View view){
        AlertDialog.Builder popup_builder = new AlertDialog.Builder(context);

        View customLayout = getLayoutInflater().inflate(R.layout.popup_delete_path, null);

        customLayout.findViewById(R.id.btn_confirm).setOnClickListener(view1 -> {
            confirmDelete(view1);});
        customLayout.findViewById(R.id.btn_cancel).setOnClickListener(view1 -> {popup.dismiss();});

        popup_builder.setView(customLayout);
        popup = popup_builder.create();
        popup.show();


    }

    /**
     * If the route removal is validated by the user,
     * send a request to the API server to delete the route.
     * @param view the current view
     */
    public void confirmDelete(View view){

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
                .newJSONObjectRequest(PathApi.API_PATH_DELETE)
                .send();

        popup.dismiss();
    }

    /**
     * Display a toast in case of API error.
     * @param error error sent by the API
     */
    public void handleError(VolleyError error) {
        if (error.networkResponse != null) {
            try {
                JSONObject reponse = new JSONObject(new String(error.networkResponse.data));
                String message = reponse.optString("erreur");
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                Toast.makeText(context, getString(R.string.synthesis_error), Toast.LENGTH_LONG)
                        .show();
            }
        } else {
        Toast.makeText(this, getString(R.string.api_network_error), Toast.LENGTH_LONG).show();
        }
    }

}
