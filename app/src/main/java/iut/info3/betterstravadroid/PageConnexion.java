package iut.info3.betterstravadroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;

import org.json.JSONObject;

import iut.info3.betterstravadroid.api.UserApi;

public class PageConnexion extends AppCompatActivity {

    public static PageConnexion instance;

    public static PageConnexion getInstance() {
        return instance;
    }

    EditText courriel, motDePasse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.page_connexion);

        courriel = findViewById(R.id.et_email);
        motDePasse = findViewById(R.id.et_mot_de_passe);
    }

    public void goToInscription(View view) {
        Intent intent = new Intent(this, PageInscription.class);
        startActivity(intent);
    }


    public void boutonConnexion(View view) {
        String email = courriel.getText().toString();
        String mdp = motDePasse.getText().toString();

        if (email.isEmpty() || mdp.isEmpty()) {
            Toast.makeText(instance, "Veuillez saisir votre courriel et votre mot de passe", Toast.LENGTH_LONG).show();
        } else {

            // On envoie la requête de connexion au serveur
            RequestHelper.simpleJSONObjectRequest(
                    UserApi.USER_API_LOGIN + "?email=" + email + "&password=" + mdp,
                    null,
                    null,
                    Request.Method.GET,
                    (reponse) -> {
                        // L'utilisateur est connecté, on enregistre le token dans les préférences
                        String token = reponse.optString("token");
                        SharedPreferences.Editor editor = getSharedPreferences("BetterStrava", MODE_PRIVATE).edit();
                        editor.putString("token", token);
                        editor.apply();
                        Toast.makeText(instance, "Utilisateur connecté", Toast.LENGTH_SHORT).show();
                    },
                    error -> {
                        // L'utilisateur n'est pas connecté, on affiche un message d'erreur
                        try {
                            JSONObject reponse = new JSONObject(new String(error.networkResponse.data));
                            String message = reponse.optString("erreur");
                            Toast.makeText(instance, message, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(instance, "Erreur lors de la connexion", Toast.LENGTH_LONG).show();
                        }
                    }
            );
        }
    }

}