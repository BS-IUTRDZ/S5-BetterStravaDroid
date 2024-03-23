package iut.info3.betterstravadroid.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import iut.info3.betterstravadroid.R;
import iut.info3.betterstravadroid.tools.api.RequestBuilder;
import iut.info3.betterstravadroid.tools.api.PathApi;
import iut.info3.betterstravadroid.databinding.ActivityUpdatePathBinding;
import iut.info3.betterstravadroid.preferences.UserPreferences;

public class UpdatePathActivity extends AppCompatActivity {

    /** Object responsible for linking this class to the update page layout */
    ActivityUpdatePathBinding binding;

    private Context context;

    /** API request builder */
    private RequestBuilder helper;

    /** Current Route Id */
    private String pathId;

    /** The preferences of the application */
    private SharedPreferences preferences;

    @Override
    public void onCreate (Bundle savedInstance) {
        super.onCreate(savedInstance);

        binding = ActivityUpdatePathBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.topbar.ivEditIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.edit_active));

        Intent intention = getIntent();
        pathId = intention.getStringExtra("id");

        String titre = intention.getStringExtra("titre");
        String description = intention.getStringExtra("description");
        binding.editTitre.setText(titre);
        binding.editTitre.setEnabled(false);
        binding.editDescription.setText(description);
        binding.topbar.ivTrashIcon.setVisibility(View.GONE);
        binding.navbar.pauseButton.setVisibility(View.INVISIBLE);

        binding.topbar.ivBackIcon.setOnClickListener(view -> this.finish());

        binding.btnAnnuler.setOnClickListener(view -> {onClickCancel();});
        binding.btnValider.setOnClickListener(view -> {onClickValidate();});

        //Preferences management
        preferences = this.getSharedPreferences(UserPreferences.PREFERENCE_FILE, MODE_PRIVATE);

        context = binding.getRoot().getContext();

        helper = new RequestBuilder(this);
    }

    /**
     * Associated with the cancel button.
     * Back to the summary page of the course without
     * applying any modification.
     */
    public void onClickCancel() {
        Intent intentionRetour = new Intent();
        setResult(Activity.RESULT_CANCELED, intentionRetour);
        this.finish();
    }

    /**
     * Associated with the change validation button.
     * Back to the summary page of the course by applying
     * the modifications (send a request to the API).
     */
    public void onClickValidate() {
        String newDescritpion = binding.editDescription.getText().toString();

        JSONObject body = new JSONObject();
        HashMap<String,String> header = new HashMap<>();
        try {
            body.put("id", pathId);
            body.put("description",newDescritpion);
            header.put(UserPreferences.USER_KEY_TOKEN, preferences.getString(UserPreferences.USER_KEY_TOKEN, "None"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        helper.onSucces(this::backToSynthesis)
                .onError(this::handleError)
                .withHeader(header)
                .withBody(body)
                .newJSONObjectRequest(PathApi.API_PATH_UPDATE)
                .send();
    }

    /**
     * If the request to update the route description is successful,
     * redirects the user to the summary page with the new information.
     * @param object response sent by the API
     */
    public void backToSynthesis(Object object) {
        Intent intentionRetour = new Intent();
        String description = binding.editDescription.getText().toString();
        intentionRetour.putExtra("description", description);
        intentionRetour.putExtra("id", pathId);
        setResult(Activity.RESULT_OK, intentionRetour);

        this.finish();
    }

    /**
     * Display a toast in case of API error.
     * @param error error sent by the API
     */
    public void handleError(VolleyError error) {
        if (error.networkResponse != null){
            try {
                JSONObject reponse = new JSONObject(new String(error.networkResponse.data));
                String message = reponse.optString("erreur");
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                Toast.makeText(context,
                                getString(R.string.update_path_error),
                                Toast.LENGTH_LONG)
                        .show();
            }
        } else {
            Toast.makeText(this, getString(R.string.api_network_error), Toast.LENGTH_LONG).show();
        }
    }
}
