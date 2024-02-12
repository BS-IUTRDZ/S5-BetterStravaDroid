package iut.info3.betterstravadroid;

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

import iut.info3.betterstravadroid.api.PathApi;
import iut.info3.betterstravadroid.api.UserApi;
import iut.info3.betterstravadroid.databinding.PageModificationParcoursBinding;
import iut.info3.betterstravadroid.preferences.UserPreferences;

public class PageModifParcours extends AppCompatActivity {

    PageModificationParcoursBinding binding;

    private Context context;

    private RequestBuilder helper;

    private String idParcours;

    private SharedPreferences preferences;

    private ToastMaker toastMaker;

    @Override
    public void onCreate (Bundle savedInstance) {

        super.onCreate(savedInstance);

        binding = PageModificationParcoursBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.topbar.ivEditIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.edit_active));

        Intent intention = getIntent();
        idParcours = intention.getStringExtra("id");

        String titre = intention.getStringExtra("titre");
        String description = intention.getStringExtra("description");
        binding.editTitre.setText(titre);
        binding.editTitre.setEnabled(false);
        binding.editDescription.setText(description);

        binding.btnAnnuler.setOnClickListener(view -> {onClickAnnuler();});
        binding.btnAnnuler.setOnClickListener(view -> {onClickValider();});

        //Gestion des preferences
        preferences = this.getSharedPreferences(UserPreferences.PREFERENCE_FILE, MODE_PRIVATE);

        context = binding.getRoot().getContext();

        helper = new RequestBuilder(this);

    }

    public void onClickAnnuler() {
        Intent intentionRetour = new Intent();
        setResult(Activity.RESULT_CANCELED, intentionRetour);
        this.finish();
    }

    public void onClickValider() {

        String newDescritpion = binding.editDescription.getText().toString();

        JSONObject body = new JSONObject();
        HashMap<String,String> header = new HashMap<>();
        try {
            body.put("id",idParcours);
            body.put("description",newDescritpion);
            header.put(UserPreferences.USER_KEY_TOKEN, preferences.getString(UserPreferences.USER_KEY_TOKEN, "None"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        helper.onSucces(this::retourListe)
                .onError(this::handleError)
                .withHeader(header)
                .withBody(body)
                .newJSONObjectRequest(PathApi.PATH_API_MODIF)
                .send();

    }

    public void retourListe(Object object) {

        Intent intentionRetour = new Intent();
        String description = binding.editDescription.getText().toString();
        intentionRetour.putExtra("description",description );
        intentionRetour.putExtra("id",idParcours);
        setResult(Activity.RESULT_OK, intentionRetour);

        //TODO appel a l'api pout modifer description et titre

        this.finish();

    }

    public void handleError(VolleyError error) {
        try {
            JSONObject reponse = new JSONObject(new String(error.networkResponse.data));
            String message = reponse.optString("erreur");
            toastMaker.makeText(context, message, Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            toastMaker.makeText(context,
                            "Erreur lors de la modification de la description",
                            Toast.LENGTH_LONG)
                    .show();
        }
    }
}
