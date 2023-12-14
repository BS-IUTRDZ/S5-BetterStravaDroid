package iut.info3.betterstravadroid;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;

import org.json.JSONObject;

import iut.info3.betterstravadroid.api.UserApi;

public class PageInscription extends AppCompatActivity {

    private EditText nom, prenom, courriel, password, confirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_inscription);

        nom = findViewById(R.id.et_nom);
        prenom = findViewById(R.id.et_prenom);
        courriel = findViewById(R.id.et_courriel);
        password = findViewById(R.id.et_mot_de_passe);
        confirmPassword = findViewById(R.id.et_repeter_mot_de_passe);
    }

    public void backToConnexion(View view) {
        this.finish();
    }

    public void boutonInscription(View view) {
        String nom = this.nom.getText().toString();
        String prenom = this.prenom.getText().toString();
        String courriel = this.courriel.getText().toString();
        String password = this.password.getText().toString();
        String confirmPassword = this.confirmPassword.getText().toString();

        boolean formulaireOk = true;

        // Validation des champs du formulaire
        if (nom.isEmpty() || prenom.isEmpty() || courriel.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            formulaireOk = false;
        }

        if (formulaireOk && !password.equals(confirmPassword)) {
            Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
            formulaireOk = false;
        }

        if (formulaireOk && !Patterns.EMAIL_ADDRESS.matcher(courriel).matches()) {
            Toast.makeText(this, "Le courriel n'est pas valide", Toast.LENGTH_SHORT).show();
            formulaireOk = false;
        }

        if (formulaireOk && password.length() < 8) {
            Toast.makeText(this, "Le mot de passe doit contenir au moins 8 caractères", Toast.LENGTH_SHORT).show();
            formulaireOk = false;
        }

        if (formulaireOk) {

            // les champs du formulaires sont corrects, on crée le body de la requête POST
            JSONObject body = new JSONObject();
            try {
                body.put("nom", nom);
                body.put("prenom", prenom);
                body.put("email", courriel);
                body.put("motDePasse", password);

                // On envoie la requête
                RequestHelper.simpleJSONObjectRequest(
                        UserApi.USER_API_BASE_URL,
                        null,
                        body,
                        Request.Method.POST,
                        response -> {
                            Toast.makeText(this, "Inscription réussie", Toast.LENGTH_LONG).show();
                            this.finish();
                        },
                        error -> {
                            Toast.makeText(this, "Erreur lors de l'inscription", Toast.LENGTH_SHORT).show();
                            Log.e("PageInscription", error.toString());
                        }
                );

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }
}
