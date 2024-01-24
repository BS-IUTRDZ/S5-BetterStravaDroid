package iut.info3.betterstravadroid;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;

import org.json.JSONException;
import org.json.JSONObject;

import iut.info3.betterstravadroid.api.UserApi;
import iut.info3.betterstravadroid.databinding.PageInscriptionBinding;

public class PageInscription extends AppCompatActivity {

    private PageInscriptionBinding binding;

    private ToastMaker toastMaker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = PageInscriptionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        toastMaker = new ToastMaker();
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

        try {
            UserApi userApi = new UserApi(nom, prenom, courriel, password, confirmPassword);
            RequestHelper.simpleJSONObjectRequest(
                    UserApi.USER_API_CREATE_ACCOUNT,
                    null,
                    userApi.toJson(),
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
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }
}
