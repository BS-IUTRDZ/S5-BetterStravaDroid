package iut.info3.betterstravadroid;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
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
import iut.info3.betterstravadroid.tools.SpacingItemDecorator;
import iut.info3.betterstravadroid.tools.SwipeHelper;

public class PageListeParcours extends Fragment implements RecyclerViewInterface {

    private List<ParcoursItem> parcoursItemList;

    private static final String TAG = "PageListeParcours";

    private ListeParcoursBinding binding;
    private ParcoursAdaptateur parcoursAdaptateur;

    private SharedPreferences preferences;

    private Activity activity;

    private ToastMaker toastMaker;

    private DatePickerDialog datePickerFrom;
    private DatePickerDialog datePickerTo;

    private PathFinder finder;
    private ActivityResultLauncher<Intent> launcher;






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
        pageListeParcours.parcoursItemList = new ArrayList<>();
        pageListeParcours.parcoursAdaptateur =
                new ParcoursAdaptateur(pageListeParcours.parcoursItemList, pageListeParcours);


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

        onClickShowMenuButton();
        initPathFinder();
        initRecyclerView();
        View vue = binding.getRoot();

        parcoursAdaptateur.setOnBottomReachedListener(position -> {
            finder.setNbPathAlreadyLoaded(position + 1);
        });

        SwipeHelper swipeHelper = new SwipeHelper(activity, binding.recyclerView) {
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
                underlayButtons.add(new UnderlayButton(activity, 0, Color.WHITE,
                        pos -> finder.deletePath(parcoursItemList.get(pos))));
            }
        };

        initDateSelector(binding);
        initTextSelector(binding);
        initLengthSelector(binding);

        LinearLayoutManager gestionnaireLineaire = new LinearLayoutManager(this.getContext());
        binding.recyclerView.setLayoutManager(gestionnaireLineaire);

        finder.findPaths();

        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::goToPage);

        return vue;
    }

    private void initDateSelector(ListeParcoursBinding binding) {
        datePickerFrom = new DatePickerDialog(activity);
        datePickerTo = new DatePickerDialog(activity);
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

        binding.lenghtMax.setOnClickListener(v -> {
            String text = binding.lenghtMax.getText().toString();
            try {
                finder.setLengthMax(Integer.parseInt(text));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        });

        binding.lenghtMin.setOnClickListener(v -> {
            String text = binding.lenghtMin.getText().toString();
            try {
                finder.setLengthMin(Integer.parseInt(text));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        });




    }

    @Override
    public void onItemClick(ParcoursItem parcoursItem) {
        // création d'une intention
        Intent intention = new Intent(getActivity(), PageSynthese.class);

        // transmission de l'id du parcours
        intention.putExtra("pathId", parcoursItem.getId());
        // lancement de l'activité fille
        launcher.launch(intention);
    }

    private void goToPage(ActivityResult result) {
        Intent intent = result.getData();

        String page = intent.getStringExtra(PageSynthese.KEY_PAGE);
        MainActivity mainActivity = (MainActivity) getActivity();
        if (page.equals(PageSynthese.HOME_PAGE)) {
            mainActivity.goToHome(getView());
        }
        if (page.equals(PageSynthese.PATH_PAGE)) {
            mainActivity.goToPathList(getView());
        }
    }

    private void onClickShowMenuButton() {
        Drawable closeMenu = AppCompatResources.getDrawable(activity, R.drawable.menu_close);
        Drawable openMenu  = AppCompatResources.getDrawable(activity, R.drawable.menu_open);
        binding.showMenu.setOnClickListener(v -> {
            int visibility = binding.menu.getVisibility();
            System.out.println(visibility);
            if (binding.menu.getVisibility() == View.VISIBLE) {
                binding.menu.setVisibility(View.GONE);
                binding.showMenu.setImageDrawable(closeMenu);
            } else {
                binding.showMenu.setImageDrawable(openMenu);
                binding.menu.setVisibility(View.VISIBLE);
            }

        });
    }


    private void initRecyclerView() {
        SpacingItemDecorator spacingItemDecorator = new SpacingItemDecorator(40);

        binding.recyclerView.setAdapter(parcoursAdaptateur);
        binding.recyclerView.addItemDecoration(spacingItemDecorator);
        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    parcoursAdaptateur.resetBottomReached();
                }
            }
        });


    }

    private void initPathFinder() {
        finder = new PathFinder(activity);
        finder.setOnPathsUpdate(parcoursItemList -> {
            this.parcoursItemList.clear();
            this.parcoursItemList.addAll(parcoursItemList);
            parcoursAdaptateur.notifyDataSetChanged();



        });

        finder.setOnError(error -> {
            toastMaker.makeText(activity,"Erreur lors du chargement des parcours", Toast.LENGTH_SHORT);
        });

    }




}
