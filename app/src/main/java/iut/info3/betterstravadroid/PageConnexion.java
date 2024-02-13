package iut.info3.betterstravadroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import iut.info3.betterstravadroid.api.UserApi;
import iut.info3.betterstravadroid.databinding.PageConnexionBinding;
import iut.info3.betterstravadroid.preferences.UserPreferences;

public class PageConnexion extends AppCompatActivity {

    private RequestBuilder helper;

    private ToastMaker toastMaker;
    private EditText courriel, motDePasse;

    private SharedPreferences.Editor editor;


    public static PageConnexion instance;

    public static PageConnexion getInstance() {
        return instance;
    }

    public PageConnexion() {

    }

    private PageConnexionBinding binding;

    public PageConnexion(EditText courriel, EditText motDePasse) {
        this.courriel = courriel;
        this.motDePasse = motDePasse;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = PageConnexionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setHelper(new RequestBuilder(this));
        toastMaker = new ToastMaker();

        helper = new RequestBuilder(this);
        editor = getSharedPreferences(UserPreferences.PREFERENCE_FILE, MODE_PRIVATE).edit();
    }

    public void goToInscription(View view) {
        Intent intent = new Intent(this, PageInscription.class);
        startActivity(intent);
    }

    public void boutonConnexion(View view) {
        String email = binding.etEmail.getText().toString();
        String mdp = binding.etMotDePasse.getText().toString();

        if (email.isEmpty() || mdp.isEmpty()) {
            toastMaker.makeText(this, "Veuillez saisir votre courriel et votre mot de passe", Toast.LENGTH_LONG).show();
        } else {

            // On envoie la requête de connexion au serveur
            helper.onSucces(this::handleResponse)
                    .onError(this::handleError)
                    .newJSONObjectRequest(UserApi.USER_API_LOGIN
                            + "?email=" + email + "&password=" + mdp)
                    .send();
        }
    }

    public void handleResponse(Object object) {
        JSONObject response = (JSONObject) object;
        String token = response.optString("token");
        editor.putString("token", token);
        editor.apply();
        toastMaker.makeText(this, "Utilisateur connecté", Toast.LENGTH_SHORT).show();
        goToHome();
    }


    public void handleError(VolleyError error) {
        try {
            JSONObject reponse = new JSONObject(new String(error.networkResponse.data));
            String message = reponse.optString("erreur");
            toastMaker.makeText(this, message, Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            toastMaker.makeText(this, "Erreur lors de la connexion", Toast.LENGTH_LONG).show();
        }
    }

    public void setHelper(RequestBuilder helper) {
        this.helper = helper;
    }

    public void setToastMaker(ToastMaker toastMaker) {
        this.toastMaker = toastMaker;
    }

    public void setEditor(SharedPreferences.Editor editor) {
        this.editor = editor;
    }

    private void goToHome() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}