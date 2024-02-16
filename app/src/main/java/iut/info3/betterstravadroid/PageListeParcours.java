package iut.info3.betterstravadroid;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import iut.info3.betterstravadroid.databinding.ListeParcoursBinding;
import iut.info3.betterstravadroid.parcours.ParcoursAdaptateur;
import iut.info3.betterstravadroid.parcours.ParcoursItem;
import iut.info3.betterstravadroid.parcours.PathFinder;
import iut.info3.betterstravadroid.tools.SwipeHelper;

public class PageListeParcours extends Fragment  {


    private static final String tag = "PageListeParcours";

    private ParcoursAdaptateur parcoursAdaptateur;

    private Activity activity;

    private ToastMaker toastMaker;

    private DatePickerDialog datePickerFrom;
    private DatePickerDialog datePickerTo;

    private PathFinder finder;

    private List<ParcoursItem> parcoursItemList;



    public PageListeParcours() {
        //Require empty public constructor
    }




    public static PageListeParcours newInstance(Activity activity) {
        PageListeParcours pageListeParcours = new PageListeParcours();


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

        ListeParcoursBinding binding = ListeParcoursBinding.inflate(inflater, container, false);
        View vue = binding.getRoot();


        SwipeHelper swipeHelper = new SwipeHelper(activity, binding.recyclerView) {
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
                underlayButtons.add(new UnderlayButton("Delete", 0, Color.parseColor("#FF3C30"), new UnderlayButtonClickListener() {
                    @Override
                    public void onClick(int pos) {

                        parcoursItemList.remove(pos);
                        parcoursAdaptateur.notifyItemChanged(pos);

                    }
                }));
            }
        };



        datePickerFrom = new DatePickerDialog(activity);
        datePickerTo = new DatePickerDialog(activity);
        initDateSelector(binding);
        initTextSelector(binding);
        initLengthSelector(binding);

        finder = new PathFinder(activity);

        finder.setOnPathsUpdate(parcoursItemList -> {
            this.parcoursItemList = parcoursItemList;
            parcoursAdaptateur = new ParcoursAdaptateur(parcoursItemList);
            binding.recyclerView.setAdapter(parcoursAdaptateur);
        });

        finder.setOnError(error -> {
            toastMaker.makeText(activity,"Erreur lors du chargement des parcours", Toast.LENGTH_SHORT);
        });


        LinearLayoutManager gestionnaireLineaire = new LinearLayoutManager(this.getContext());
        binding.recyclerView.setLayoutManager(gestionnaireLineaire);

        finder.findPaths();

        return vue;

    }



    private void initDateSelector(ListeParcoursBinding binding) {


        datePickerFrom.setOnDateSetListener((view, year, month, dayOfMonth) -> {
            String date = LocalDate.of(year,month + 1,dayOfMonth + 1).
                    format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            binding.btnDepuis.setText(date);
            finder.setDateInf(date);
        });

        datePickerTo.setOnDateSetListener((view, year, month, dayOfMonth) -> {
            String date = LocalDate.of(year,month + 1,dayOfMonth + 1).
                    format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            binding.btnJusqua.setText(date);
            finder.setDateSup(date);
        });

        binding.btnJusqua.setOnClickListener(view -> {
            datePickerTo.show();
        });
        binding.btnDepuis.setOnClickListener(view -> {
            datePickerFrom.show();
        });


    }

    private void initTextSelector(ListeParcoursBinding binding) {
        binding.searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                finder.setTextSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    finder.setTextSearch("");
                }
                return true;
            }
        });
    }


    private void initLengthSelector(ListeParcoursBinding binding) {
        binding.lenghtMax.setMinValue(0);
        binding.lenghtMax.setMaxValue(300);
        binding.lenghtMin.setMinValue(0);
        binding.lenghtMin.setMaxValue(300);

        binding.lenghtMax.setOnValueChangedListener((numberPicker, oldValue, newValue) -> {
            finder.setLengthMax(newValue);
            numberPicker.setValue(newValue);
        });

        binding.lenghtMin.setOnValueChangedListener((numberPicker, oldValue, newValue) -> {
            finder.setLengthMin(newValue);
            numberPicker.setValue(newValue);

        });
    }









}
