package iut.info3.betterstravadroid.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import iut.info3.betterstravadroid.activities.SynthesisActivity;
import iut.info3.betterstravadroid.R;
import iut.info3.betterstravadroid.tools.api.RequestBuilder;
import iut.info3.betterstravadroid.tools.api.PathApi;
import iut.info3.betterstravadroid.tools.api.UserApi;
import iut.info3.betterstravadroid.databinding.FragmentHomepageBinding;
import iut.info3.betterstravadroid.preferences.UserPreferences;
import iut.info3.betterstravadroid.tools.MapHandler;

public class HomeFragment extends Fragment {

    /** Object responsible for linking this class to the home page layout */
    private FragmentHomepageBinding binding;

    private Context context;

    /** the preferences of the application */
    private SharedPreferences preferences;

    /** API request builder */
    private RequestBuilder helper;

    /** The synthesis activity launcher associated with the last course */
    private ActivityResultLauncher<Intent> launcher;

    public HomeFragment() {
        //Require empty public constructor
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentHomepageBinding.inflate(inflater, container, false);
        View vue = binding.getRoot();
        context = vue.getContext();

        binding.cardLastRun.map.setDestroyMode(false);

        preferences = this.getActivity().getSharedPreferences(UserPreferences.PREFERENCE_FILE, MODE_PRIVATE);

        helper = new RequestBuilder(vue.getContext());

        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::backFromSynthesis);

        showUserInfos();
        showLastPath();

        return vue;
    }

    /**
     * In charge of the return of the synthesis page for the last course.
     * @param result result from intention
     */
    private void backFromSynthesis(ActivityResult result) {
        //Does nothing, necessary for compilation
    }

    /**
     * Redirects the user to the summary page of his last journey.
     * @param result the last route, retrieved by the API
     */
    public void goToSynthesis(JSONObject result) throws JSONException {
        // creating an intention
        Intent intention = new Intent(getActivity(), SynthesisActivity.class);

        // transmission of the route id
        intention.putExtra("pathId", result.getString("id"));
        // launch of the daughter activity
        launcher.launch(intention);

    }

    /**
     * Access to the API server to retrieve and display the user’s last journey.
     */
    public void showLastPath() {
        Map<String, String> header = new HashMap<>();
        header.put(UserPreferences.USER_KEY_TOKEN, preferences.getString(UserPreferences.USER_KEY_TOKEN, "None"));
        // Sending the request to retrieve the user’s last journey
        helper.onSucces(this::setViewParcours)
                .onError(this::handleError)
                .withHeader(header)
                .newJSONObjectRequest(PathApi.API_PATH_LAST)
                .send();
    }

    /**
     * Access to the API server to retrieve and display user information.
     */
    public void showUserInfos() {
        JSONObject body = new JSONObject();
        try {
            body.put(UserPreferences.USER_KEY_TOKEN, preferences.getString(UserPreferences.USER_KEY_TOKEN, "None"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        // Sending the request to retrieve user information
        helper.onSucces(this::setViewContent)
                .onError(this::handleError)
                .withBody(body)
                .newJSONObjectRequest(UserApi.API_USER_INFO)
                .send();
    }

    /**
     * If the route "/user/getInfo" responds positively, position on the UI:
     * <li>the last user journey</li>
     * <li>30-day user stats</li>
     * <li>user stats since account creation</li>
     * @param object API response
     */
    private void setViewContent(Object object) {
        JSONObject response = (JSONObject) object;
        try {
            // Current date
            binding.tvDateDuJour.setText(LocalDate.now().format(
                    DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", Locale.FRANCE)));

            // User info
            JSONObject infoUser = (JSONObject) response.get("user");

            binding.tvBonjourUtilisateur.setText(
                    String.format(getString(R.string.tv_bonjour_utilisateur), infoUser.getString(UserPreferences.USER_KEY_SURNAME)));

            // Stats last 30 days
            JSONObject stats30Jours = (JSONObject) response.get("30jours");

            binding.tvDistance30J.setText(String.format(Locale.FRANCE, "%.2f",
                    stats30Jours.getDouble(UserPreferences.STAT_KEY_DISTANCE)));

            float dureeParcours30Jours = Float.parseFloat(stats30Jours.getString(UserPreferences.STAT_KEY_TIME));
            float heureParcours30Jours = dureeParcours30Jours / 3600;
            float minParcours30Jours = dureeParcours30Jours % 3600 / 60;
            binding.tvTpsParcoursHeure30J.setText(String.format(Locale.FRANCE, "%.0f", heureParcours30Jours));
            binding.tvTpsParcoursMinute30J.setText(String.format(Locale.FRANCE, "%.0f", minParcours30Jours));

            binding.tvParcoursCrees30J.setText(stats30Jours.getString(UserPreferences.STAT_KEY_NB_PATH));

            // Overall stats
            JSONObject statsGlobales = (JSONObject) response.get("global");
            binding.tvDistanceGlob.setText(String.format(Locale.FRANCE, "%.2f",
                    statsGlobales.getDouble(UserPreferences.STAT_KEY_DISTANCE)));

            float dureeParcoursGlobal = Float.parseFloat(statsGlobales.getString(UserPreferences.STAT_KEY_TIME));
            float heureParcoursGlobal = dureeParcoursGlobal / 3600;
            float minParcoursGlobal = dureeParcoursGlobal % 3600 / 60;
            binding.tvTpsParcoursHeureGlob.setText(String.format(Locale.FRANCE, "%.0f", heureParcoursGlobal));
            binding.tvTpsParcoursMinuteGlob.setText(String.format(Locale.FRANCE, "%.0f", minParcoursGlobal));

            binding.tvParcoursCreesGlob.setText(statsGlobales.getString(UserPreferences.STAT_KEY_NB_PATH));

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * If the route "/path/lastPath" responds positively, position on the UI:
     * <li>last route map</li>
     * <li>the last course title</li>
     * <li>description of last route</li>
     * @param object API response
     */
    private void setViewParcours(Object object) {
        JSONObject response = (JSONObject) object;
        try {
            // Last run information
            binding.cardLastRun.titreDernierParcours.setText(
                    response.getString(UserPreferences.PATH_KEY_NAME)
            );
            binding.cardLastRun.descriptionDernierParcours.setText(
                    response.getString(UserPreferences.PATH_KEY_DESCRIPTION)
            );

            // route creation
            JSONArray points = response.getJSONArray("points");
            MapHandler.setMapViewContent(points, binding.cardLastRun.map, context, false);

            // setting up the Onclick on the cardview
            binding.cardLastRun.cadre.setOnClickListener(v -> {
                try {
                    goToSynthesis(response);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

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
                Toast.makeText(context,
                                getString(R.string.home_error),
                                Toast.LENGTH_LONG)
                        .show();
            }
        } else {
            Toast.makeText(context, getString(R.string.api_network_error), Toast.LENGTH_LONG).show();
        }
    }

}
