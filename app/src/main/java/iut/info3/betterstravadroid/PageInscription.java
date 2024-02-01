package iut.info3.betterstravadroid;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;

import org.json.JSONException;

import iut.info3.betterstravadroid.api.UserApi;
import iut.info3.betterstravadroid.databinding.PageInscriptionBinding;

public class PageInscription extends AppCompatActivity {

    private PageInscriptionBinding binding;

    private EditText nom, prenom, courriel, password, confirmPassword;
    private ToastMaker toastMaker;

    private RequestBuilder helper;




    public PageInscription() {

    }


    public PageInscription(EditText nom,
                           EditText prenom,
                           EditText courriel,
                           EditText password,
                           EditText confirmPassword) {
        this.nom = nom;
        this.prenom = prenom;
        this.courriel = courriel;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

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

            helper.withBody(userApi.toJson())
                    .onError(this::handleError)
                    .onSucces(this::handleResponse)
                    .newJSONObjectRequest(UserApi.USER_API_CREATE_ACCOUNT).send();
        } catch (IllegalArgumentException e) {
            Toast toast = toastMaker.makeText(this, e.getMessage(), Toast.LENGTH_SHORT);
            toast.show();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleResponse(Object object) {
        toastMaker.makeText(this, "Inscription réussie", Toast.LENGTH_LONG).show();
        this.finish();
    }

    public void handleError(VolleyError error) {
        toastMaker.makeText(this, "Erreur lors de l'inscription", Toast.LENGTH_SHORT).show();
        Log.e("PageInscription", error.toString());
    }

    public void setToastMaker(ToastMaker toastMaker) {
        this.toastMaker = toastMaker;
    }

    public void setRequestHelper(RequestBuilder helper) {this.helper = helper;}
}
