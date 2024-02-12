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
import android.widget.Toast;

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
import iut.info3.betterstravadroid.parcours.PathFinder;
import iut.info3.betterstravadroid.preferences.UserPreferences;

public class PageListeParcours extends Fragment implements View.OnClickListener {

    private static final String tag = "PageListeParcours";

    private ListeParcoursBinding binding;
    private ParcoursAdaptateur parcoursAdaptateur;

    private SharedPreferences preferences;

    private Activity activity;

    private PathFinder pathFinder;

    private DatePickerDialog datePickerFrom;
    private DatePickerDialog datePickerTo;
    private ToastMaker toastMaker;



    public PageListeParcours() {
        //Require empty public constructor
    }




    public static PageListeParcours newInstance(Activity activity) {
        PageListeParcours pageListeParcours = new PageListeParcours();
        pageListeParcours.preferences =
                activity.getSharedPreferences("BetterStrava", Context.MODE_PRIVATE);
        pageListeParcours.binding = ListeParcoursBinding.inflate(activity.getLayoutInflater());
        pageListeParcours.pathFinder = new PathFinder(activity, pageListeParcours.binding);

        pageListeParcours.activity = activity;
        pageListeParcours.toastMaker = new ToastMaker();
        return pageListeParcours;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        datePickerFrom = new DatePickerFilter(activity, binding.btnDepuis);
        datePickerTo = new DatePickerFilter(activity, binding.btnJusqua);
        String token = preferences.getString(UserPreferences.USER_KEY_TOKEN,"None");

        binding = ListeParcoursBinding.inflate(inflater, container, false);


        View vue = binding.getRoot();
        //Gestion du RecyclerView
        binding.btnJusqua.setOnClickListener(this);
        binding.btnDepuis.setOnClickListener(this);

        LinearLayoutManager gestionnaireLineaire = new LinearLayoutManager(this.getContext());
        binding.recyclerView.setLayoutManager(gestionnaireLineaire);
        List<ParcoursItem> parcoursItemList = null;
        try {
            parcoursItemList = pathFinder.findPaths(token,null,null,null);
            parcoursAdaptateur = new ParcoursAdaptateur(parcoursItemList);
            binding.recyclerView.setAdapter(parcoursAdaptateur);
        } catch (VolleyError e) {
            toastMaker.makeText(activity,"Erreur lors du chargement des parcours", Toast.LENGTH_SHORT);
        }

        return vue;

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
