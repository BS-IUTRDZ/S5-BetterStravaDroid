package iut.info3.betterstravadroid;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import iut.info3.betterstravadroid.api.UserApi;
import iut.info3.betterstravadroid.databinding.ListeParcoursBinding;
import iut.info3.betterstravadroid.databinding.PageConnexionBinding;
import iut.info3.betterstravadroid.parcours.ParcoursAdaptateur;
import iut.info3.betterstravadroid.parcours.ParcoursItem;
import iut.info3.betterstravadroid.preferences.UserPreferences;

public class PageListeParcours extends Fragment {

    private ListeParcoursBinding binding;
    private List<ParcoursItem> parcoursItemList;
    private ParcoursAdaptateur parcoursAdaptateur;

    private SharedPreferences preferences;

    private RequestBuilder builder;

    private DatePickerDialog datePickerFrom;
    private DatePickerDialog datePickerTo;



    public void setBuilder(RequestBuilder builder) {
        this.builder = builder;
    }

    public PageListeParcours() {
        //Require empty public constructor
    }

    interface OnDateSelectedListener {
        void onDateSelected(Date date);
    }

    public static PageListeParcours newInstance(Activity activity) {
        PageListeParcours pageListeParcours = new PageListeParcours();
        pageListeParcours.builder = new RequestBuilder(activity);
        pageListeParcours.preferences =
                activity.getSharedPreferences("BetterStrava", Context.MODE_PRIVATE);
        pageListeParcours.binding = ListeParcoursBinding.inflate(activity.getLayoutInflater());
        DatePickerDialog datePickerFrom = new DatePickerDialog(activity) {
            @Override
            public void onDateChanged(@androidx.annotation.NonNull DatePicker view, int year, int month, int dayOfMonth) {

            }
        };


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
        View vue = binding.getRoot();
        //Gestion du RecyclerView
        initialiseListeParcours();

        LinearLayoutManager gestionnaireLineaire = new LinearLayoutManager(this.getContext());
        binding.recyclerView.setLayoutManager(gestionnaireLineaire);



        return vue;

    }



    private void initialiseListeParcours() {
        parcoursItemList = new ArrayList<>();//Stub
        builder.onError(this::handleError)
                .onSucces(this::handleSucces)
                .addHeader(UserPreferences.USER_KEY_TOKEN, preferences.getString(UserPreferences.USER_KEY_TOKEN,"None"))
                .newJSONArrayRequest(ParcoursItem.GET_ALL_DEFAULT_PARCOUR).send();

    }

    private void handleError(VolleyError error) {
        error.printStackTrace();
    }

    private void handleSucces(Object body) {

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

    public void showDatePickerFrom(View view) {

    }


    public void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this.getActivity());
        datePickerDialog.show();

    }



}
