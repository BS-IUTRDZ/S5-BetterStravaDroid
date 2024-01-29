package iut.info3.betterstravadroid;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;

import org.json.JSONObject;

import iut.info3.betterstravadroid.api.UserApi;
import iut.info3.betterstravadroid.databinding.PageInscriptionBinding;

public class PageInscription extends AppCompatActivity {

    private PageInscriptionBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = PageInscriptionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    public void backToConnexion(View view) {
        this.finish();
    }

    public void boutonInscription(View view) {
        String nom = binding.etNom.getText().toString();
        String prenom = binding.etPrenom.getText().toString();
        String courriel = binding.etCourriel.getText().toString();
        String password = binding.etMotDePasse.getText().toString();
        String confirmPassword = binding.etRepeterMotDePasse.getText().toString();

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
                        UserApi.USER_API_CREATE_ACCOUNT,
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
