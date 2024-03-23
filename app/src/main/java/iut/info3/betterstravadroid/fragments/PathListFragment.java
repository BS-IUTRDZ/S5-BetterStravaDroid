package iut.info3.betterstravadroid.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import iut.info3.betterstravadroid.activities.FragmentContainerActivity;
import iut.info3.betterstravadroid.activities.SynthesisActivity;
import iut.info3.betterstravadroid.R;
import iut.info3.betterstravadroid.interfaces.RecyclerViewInterface;
import iut.info3.betterstravadroid.databinding.FragmentPathListBinding;
import iut.info3.betterstravadroid.adapters.PathAdapter;
import iut.info3.betterstravadroid.tools.path.ParcoursItem;
import iut.info3.betterstravadroid.tools.path.PathFinder;
import iut.info3.betterstravadroid.tools.DatePickerButton;
import iut.info3.betterstravadroid.tools.SpacingItemDecorator;
import iut.info3.betterstravadroid.tools.SwipeHelper;

public class PathListFragment extends Fragment implements RecyclerViewInterface {

    private List<ParcoursItem> parcoursItemList;
    private FragmentPathListBinding binding;
    private PathAdapter pathAdapter;

    private Context context;

    private DatePickerButton datePickerFrom;
    private DatePickerButton datePickerTo;

    private PathFinder finder;
    private ActivityResultLauncher<Intent> launcher;

    public PathListFragment() {
        //Require empty public constructor
    }

    /**
     *
     * @param activity
     * @return
     */
    public static PathListFragment newInstance(Activity activity) {
        PathListFragment pathListFragment = new PathListFragment();

        pathListFragment.binding = FragmentPathListBinding.inflate(activity.getLayoutInflater());

        pathListFragment.parcoursItemList = new ArrayList<>();
        pathListFragment.pathAdapter =
                new PathAdapter(pathListFragment.parcoursItemList, pathListFragment);


        return pathListFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        binding = FragmentPathListBinding.inflate(inflater, container, false);
        View vue = binding.getRoot();
        context = vue.getContext();

        onClickShowMenuButton();
        initPathFinder();
        initRecyclerView();


        pathAdapter.setOnBottomReachedListener(position -> {
            finder.setNbPathAlreadyLoaded(position + 1);
        });

        SwipeHelper swipeHelper = new SwipeHelper(context, binding.recyclerView) {
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder,
                                                  List<UnderlayButton> underlayButtons) {
                underlayButtons.add(new UnderlayButton(context, 0, Color.RED,
                        pos -> {
                            try {
                                finder. deletePath(parcoursItemList.get(pos));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }));
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

    /**
     *
     * @param binding
     */
    private void initDateSelector(FragmentPathListBinding binding) {
        datePickerFrom = new DatePickerButton(context, binding.btnDepuis);
        datePickerTo = new DatePickerButton(context, binding.btnJusqua);
        datePickerFrom.setDateChangeListener((date) -> finder.setDateInf(date));
        datePickerTo.setDateChangeListener((date) -> finder.setDateSup(date));


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        datePickerFrom.setDate("01/01/2024");
        datePickerTo.setDate(formatter.format(LocalDate.now().plus(1, ChronoUnit.DAYS)));
    }

    /**
     *
     * @param binding
     */
    private void initTextSelector(FragmentPathListBinding binding) {
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

    /**
     *
     * @param binding
     */
    private void initLengthSelector(FragmentPathListBinding binding) {

        binding.lenghtMax.setOnClickListener(v -> {
            String text = binding.lenghtMax.getText().toString();
            try {
                finder.setLengthMax(Integer.parseInt(text));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                finder.setLengthMax(0);
            }
        });

        binding.lenghtMin.setOnClickListener(v -> {
            String text = binding.lenghtMin.getText().toString();
            try {
                finder.setLengthMin(Integer.parseInt(text));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                finder.setLengthMin(0);
            }
        });
    }

    /**
     *
     * @param parcoursItem
     */
    @Override
    public void onItemClick(ParcoursItem parcoursItem) {
        // création d'une intention
        Intent intention = new Intent(getActivity(), SynthesisActivity.class);

        // transmission de l'id du parcours
        intention.putExtra("pathId", parcoursItem.getId());
        // lancement de l'activité fille
        launcher.launch(intention);
    }

    /**
     *
     * @param result
     */
    private void goToPage(ActivityResult result) {
        Intent intent = result.getData();
        FragmentContainerActivity fragmentContainerActivity = (FragmentContainerActivity) getActivity();

        if (intent != null && fragmentContainerActivity != null) {
            String page = intent.getStringExtra(SynthesisActivity.KEY_PAGE);

            if (page != null && page.equals(SynthesisActivity.HOME_PAGE)) {
                fragmentContainerActivity.goToHome(getView());
            } else if (page != null && page.equals(SynthesisActivity.PATH_PAGE)) {
                fragmentContainerActivity.goToPathList(getView());
            }

            if (intent.getBooleanExtra(SynthesisActivity.KEY_FORCE_REFRESH, false)) {
                fragmentContainerActivity.refreshAll();
            }
        }

    }

    /**
     *
     */
    private void onClickShowMenuButton() {
        Drawable closeMenu = AppCompatResources.getDrawable(context, R.drawable.menu_close);
        Drawable openMenu  = AppCompatResources.getDrawable(context, R.drawable.menu_open);
        binding.showMenu.setOnClickListener(v -> {
            if (binding.menu.getVisibility() == View.VISIBLE) {
                binding.menu.setVisibility(View.GONE);
                binding.showMenu.setImageDrawable(closeMenu);
            } else {
                binding.showMenu.setImageDrawable(openMenu);
                binding.menu.setVisibility(View.VISIBLE);
            }

        });
    }

    /**
     *
     */
    private void initRecyclerView() {
        SpacingItemDecorator spacingItemDecorator = new SpacingItemDecorator(40);

        binding.recyclerView.setAdapter(pathAdapter);
        binding.recyclerView.addItemDecoration(spacingItemDecorator);
        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    pathAdapter.resetBottomReached();
                }
            }
        });


    }

    /**
     *
     */
    private void initPathFinder() {
        finder = new PathFinder(context, binding);
        finder.setOnPathsUpdate(parcoursItemList -> {
            this.parcoursItemList.clear();
            this.parcoursItemList.addAll(parcoursItemList);
            pathAdapter.notifyDataSetChanged();
        });

        finder.setOnError(error -> {
            Toast.makeText(context,"Erreur lors du chargement des parcours", Toast.LENGTH_SHORT).show();
        });

    }

    /**
     *
     */
    public void rafraichir() {
        finder.findPaths();
    }
}
