package iut.info3.betterstravadroid.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;

import org.json.JSONException;

import iut.info3.betterstravadroid.R;
import iut.info3.betterstravadroid.entities.UserEntity;
import iut.info3.betterstravadroid.tools.api.RequestBuilder;
import iut.info3.betterstravadroid.tools.api.UserApi;
import iut.info3.betterstravadroid.databinding.PageInscriptionBinding;

public class RegistrationActivity extends AppCompatActivity {

    private PageInscriptionBinding binding;

    private RequestBuilder helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = PageInscriptionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        helper = new RequestBuilder(this);
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
            UserEntity user = new UserEntity(nom, prenom, courriel, password, confirmPassword);

            helper.withBody(user.toJson())
                    .onError(this::handleError)
                    .onSucces(this::handleResponse)
                    .newJSONObjectRequest(UserApi.USER_API_CREATE_ACCOUNT).send();
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleResponse(Object object) {
        Toast.makeText(this, getString(R.string.registration_valid), Toast.LENGTH_LONG).show();
        this.finish();
    }

    public void handleError(VolleyError error) {
        if (error.networkResponse != null) {
            Toast.makeText(this,  getString(R.string.registration_error), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.api_network_error), Toast.LENGTH_LONG).show();
        }

    }

}
