package iut.info3.betterstravadroid;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import iut.info3.betterstravadroid.databinding.ListeParcoursBinding;
import iut.info3.betterstravadroid.parcours.DatePickerFilter;
import iut.info3.betterstravadroid.parcours.ParcoursAdaptateur;
import iut.info3.betterstravadroid.parcours.ParcoursItem;
import iut.info3.betterstravadroid.preferences.UserPreferences;

public class PageListeParcours extends Fragment implements View.OnClickListener {

    private static final String tag = "PageListeParcours";

    private ListeParcoursBinding binding;
    private List<ParcoursItem> parcoursItemList;
    private ParcoursAdaptateur parcoursAdaptateur;

    private SharedPreferences preferences;

    private RequestBuilder builder;

    private DatePickerDialog datePickerFrom;
    private DatePickerDialog datePickerTo;

    private Activity activity;




    public PageListeParcours() {
        //Require empty public constructor
    }




    public static PageListeParcours newInstance(Activity activity) {
        PageListeParcours pageListeParcours = new PageListeParcours();
        pageListeParcours.builder = new RequestBuilder(activity);
        pageListeParcours.preferences =
                activity.getSharedPreferences("BetterStrava", Context.MODE_PRIVATE);
        pageListeParcours.binding = ListeParcoursBinding.inflate(activity.getLayoutInflater());
        pageListeParcours.activity = activity;
        return pageListeParcours;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = ListeParcoursBinding.inflate(inflater, container, false);
        datePickerFrom = new DatePickerFilter(activity, this, binding.btnDepuis);
        datePickerTo = new DatePickerFilter(activity, this, binding.btnJusqua);

        View vue = binding.getRoot();
        //Gestion du RecyclerView
        fetchParcours(null, null, null);
        binding.btnJusqua.setOnClickListener(this);
        binding.btnDepuis.setOnClickListener(this);

        LinearLayoutManager gestionnaireLineaire = new LinearLayoutManager(this.getContext());
        binding.recyclerView.setLayoutManager(gestionnaireLineaire);



        return vue;

    }



    public void fetchParcours(@Nullable String dateInf,
                              @Nullable String dateSup,
                              @Nullable String parcourName) {
        String query = ParcoursItem.GET_ALL_PARCOUR + "?";
        Log.i(tag, "fetchParcours()");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        if (dateInf == null) {
            dateInf = formatter.format(LocalDate.MIN);
        }
        if (dateSup == null) {
            dateSup = formatter.format(LocalDate.MAX);
        }
        if (parcourName == null) {
            parcourName = "";
        }
        query += "dateInf=" + dateInf;
        query += "&dateSup=" + dateSup;
        query += "&nom=" + parcourName;

        String token = preferences.getString(UserPreferences.USER_KEY_TOKEN,"None");

        Log.i(tag, token);
        Log.i(tag, query);
        parcoursItemList = new ArrayList<>();//Stub
        builder.onError(this::handleError)
                .onSucces(this::handleSucces)
                .addHeader(UserPreferences.USER_KEY_TOKEN, token)
                .newJSONArrayRequest(query).send();



    }

    private void handleError(VolleyError error) {
        error.printStackTrace();
    }

    private void handleSucces(Object body) {
        Log.i(tag, "Succès");
        JSONArray array = (JSONArray) body;
        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                parcoursItemList.add(new ParcoursItem(jsonObject));
                Log.i(tag, parcoursItemList.toString());
            }
            parcoursAdaptateur = new ParcoursAdaptateur(parcoursItemList);
            binding.recyclerView.setAdapter(parcoursAdaptateur);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

    public void setListParcours(ParcoursAdaptateur parcours) {
        binding.recyclerView.setAdapter(parcours);
    }






    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_jusqua) {
            datePickerTo.show();

        } else if (view.getId() == R.id.btn_depuis) {
            datePickerFrom.show();
        }
    }
}
