package iut.info3.betterstravadroid.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import iut.info3.betterstravadroid.R;
import iut.info3.betterstravadroid.tools.api.RequestBuilder;
import iut.info3.betterstravadroid.tools.api.UserApi;
import iut.info3.betterstravadroid.databinding.ActivityConnectionBinding;
import iut.info3.betterstravadroid.preferences.UserPreferences;

public class ConnectionActivity extends AppCompatActivity {

    /** API request builder */
    private RequestBuilder helper;

    /** the preferences of the application */
    private SharedPreferences.Editor editor;

    /** Object responsible for linking this class to the login page layout */
    private ActivityConnectionBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityConnectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        helper = new RequestBuilder(this);
        editor = getSharedPreferences(UserPreferences.PREFERENCE_FILE, MODE_PRIVATE).edit();
    }

    public void goToInscription(View view) {
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
    }

    public void boutonConnexion(View view) {
        String email = binding.etEmail.getText().toString();
        String mdp = binding.etMotDePasse.getText().toString();

        if (email.isEmpty() || mdp.isEmpty()) {
            Toast.makeText(this, getString(R.string.connection_invalid_field), Toast.LENGTH_LONG).show();
        } else {

            // On envoie la requête de connexion au serveur
            helper.onSucces(this::handleResponse)
                    .onError(this::handleError)
                    .newJSONObjectRequest(UserApi.API_USER_LOGIN
                            + "?email=" + email + "&password=" + mdp)
                    .send();
        }
    }

    /**
     * Insert the user token in the application preferences.
     * @param object sent by the API
     */
    public void handleResponse(Object object) {
        JSONObject response = (JSONObject) object;
        String token = response.optString("token");
        editor.putString("token", token);
        editor.apply();
        goToHome();
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
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                Toast.makeText(this, getString(R.string.connection_error), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, getString(R.string.api_network_error), Toast.LENGTH_LONG).show();
        }

    }

    private void goToHome() {
        Intent intent = new Intent(this, FragmentContainerActivity.class);
        startActivity(intent);
    }

}