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
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import iut.info3.betterstravadroid.databinding.ListeParcoursBinding;
import iut.info3.betterstravadroid.parcours.ParcoursAdaptateur;
import iut.info3.betterstravadroid.parcours.ParcoursItem;
import iut.info3.betterstravadroid.preferences.UserPreferences;

public class PageListeParcours extends Fragment  {

    private List<ParcoursItem> parcoursItemList;

    private static final String tag = "PageListeParcours";

    private ListeParcoursBinding binding;
    private ParcoursAdaptateur parcoursAdaptateur;

    private SharedPreferences preferences;

    private Activity activity;


    private DatePickerDialog datePickerFrom;
    private DatePickerDialog datePickerTo;
    private ToastMaker toastMaker;

    private String dateInf;

    private RequestBuilder builder;

    private String dateSup;

    private String textSearch;

    private int lengthMin;
    private int lengthMax;



    public PageListeParcours() {
        //Require empty public constructor
    }




    public static PageListeParcours newInstance(Activity activity) {
        PageListeParcours pageListeParcours = new PageListeParcours();
        pageListeParcours.preferences =
                activity.getSharedPreferences("BetterStrava", Context.MODE_PRIVATE);
        pageListeParcours.binding = ListeParcoursBinding.inflate(activity.getLayoutInflater());

        pageListeParcours.activity = activity;
        pageListeParcours.toastMaker = new ToastMaker();
        pageListeParcours.builder = new RequestBuilder(activity);
        return pageListeParcours;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        datePickerFrom = new DatePickerDialog(activity);
        datePickerTo = new DatePickerDialog(activity);
        datePickerFrom.setOnDateSetListener((view, year, month, dayOfMonth) -> {
            String date = LocalDate.of(year,month + 1,dayOfMonth + 1).
                    format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            binding.btnDepuis.setText(date);
            dateInf = date;
            refreshPaths();
        });

        datePickerTo.setOnDateSetListener((view, year, month, dayOfMonth) -> {
            String date = LocalDate.of(year,month + 1,dayOfMonth + 1).
                    format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            binding.btnJusqua.setText(date);
            dateSup = date;
            refreshPaths();
        });

        binding = ListeParcoursBinding.inflate(inflater, container, false);


        View vue = binding.getRoot();
        //Gestion du RecyclerView
        binding.btnJusqua.setOnClickListener(view -> {
            datePickerTo.show();
        });
        binding.btnDepuis.setOnClickListener(view -> {
            datePickerFrom.show();
        });

        binding.searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                textSearch = query;
                refreshPaths();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });

        binding.lenghtMax.setOnValueChangedListener((numberPicker, oldValue, newValue) -> {
            lengthMax = newValue;
            refreshPaths();
        });

        binding.lenghtMin.setOnValueChangedListener((numberPicker, oldValue, newValue) -> {
            lengthMin = newValue;
            refreshPaths();
        });

        LinearLayoutManager gestionnaireLineaire = new LinearLayoutManager(this.getContext());
        binding.recyclerView.setLayoutManager(gestionnaireLineaire);
        refreshPaths();

        return vue;

    }

    public void findPaths(String token,
                          @Nullable String dateInf,
                          @Nullable String dateSup,
                          @Nullable String parcourName) {

        String query = ParcoursItem.GET_ALL_PARCOUR + "?";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        if (dateInf == null) dateInf = "01/01/2024";
        if (dateSup == null) dateSup = formatter
                .format(LocalDate.now().plus(1, ChronoUnit.DAYS));
        if (parcourName == null) parcourName = "";

        query += "dateInf=" + dateInf;
        query += "&dateSup=" + dateSup;
        query += "&nom=" + parcourName;
        //query += "&lengthMin=" + lengthMin;
        //query += "&lengthMax=" + lengthMax;




        builder.addHeader("token", token)
                .onError(this::handleError)
                .onSucces(this::handleSucces)

                .newJSONArrayRequest(query).send();
    }

    private void handleError(VolleyError error) {
        parcoursItemList = null;
        error.printStackTrace();
        toastMaker.makeText(activity,"Erreur lors du chargement des parcours", Toast.LENGTH_SHORT);
    }

    private void handleSucces(Object body) {
        parcoursItemList = new ArrayList<>();

        JSONArray array = (JSONArray) body;
        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                parcoursItemList.add(new ParcoursItem(jsonObject));
            }
            parcoursAdaptateur = new ParcoursAdaptateur(parcoursItemList);
            binding.recyclerView.setAdapter(parcoursAdaptateur);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }


    public void refreshPaths() {
        String token = preferences.getString(UserPreferences.USER_KEY_TOKEN,"None");
        findPaths(token, dateInf, dateSup, textSearch);
    }








}
