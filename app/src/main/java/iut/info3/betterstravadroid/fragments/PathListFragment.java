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
import iut.info3.betterstravadroid.tools.path.PathItem;
import iut.info3.betterstravadroid.tools.path.PathFinder;
import iut.info3.betterstravadroid.tools.DatePickerButton;
import iut.info3.betterstravadroid.tools.SpacingItemDecorator;
import iut.info3.betterstravadroid.tools.SwipeHelper;

public class PathListFragment extends Fragment implements RecyclerViewInterface {

    /** List containing all routes  */
    private List<PathItem> pathItemList;

    /** Object responsible for linking this class to the path list page layout */
    private FragmentPathListBinding binding;

    /**  Adapter in charge of the list of routes visible to the user */
    private PathAdapter pathAdapter;

    private Context context;

    /** Start date selector for search */
    private DatePickerButton datePickerFrom;

    /** End date selector for search*/
    private DatePickerButton datePickerTo;

    /** Route Search Service */
    private PathFinder finder;

    /** The synthesis activity launcher */
    private ActivityResultLauncher<Intent> launcher;

    public PathListFragment() {
        //Require empty public constructor
    }

    /**
     * @param activity fragment parent activity
     * @return the instance of the list of routes
     */
    public static PathListFragment newInstance(Activity activity) {
        PathListFragment pathListFragment = new PathListFragment();

        pathListFragment.binding = FragmentPathListBinding.inflate(activity.getLayoutInflater());

        pathListFragment.pathItemList = new ArrayList<>();
        pathListFragment.pathAdapter =
                new PathAdapter(pathListFragment.pathItemList, pathListFragment);

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
                                finder. deletePath(pathItemList.get(pos));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }));
            }
        };

        initDateSelector();
        initTextSelector();
        initLengthSelector();

        LinearLayoutManager gestionnaireLineaire = new LinearLayoutManager(this.getContext());
        binding.recyclerView.setLayoutManager(gestionnaireLineaire);

        finder.findPaths();

        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::goToPage);

        return vue;
    }

    /**
     * Initialization of the visible text on the date selection buttons.
     */
    private void initDateSelector() {
        datePickerFrom = new DatePickerButton(context, binding.btnDepuis);
        datePickerTo = new DatePickerButton(context, binding.btnJusqua);
        datePickerFrom.setDateChangeListener((date) -> finder.setDateInf(date));
        datePickerTo.setDateChangeListener((date) -> finder.setDateSup(date));


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        datePickerFrom.setDate("01/01/2024");
        datePickerTo.setDate(formatter.format(LocalDate.now().plus(1, ChronoUnit.DAYS)));
    }

    /**
     * Initializing text visible in the search bar.
     */
    private void initTextSelector() {
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
     * Initialization of fields in charge of filters on the interval of the distance traveled.
     */
    private void initLengthSelector() {

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
     * When a tap on a path is detected, opens the summary of the path concerned.
     * @param pathItem the container of the route on which the user has supported
     */
    @Override
    public void onItemClick(PathItem pathItem) {
        // creating an intention
        Intent intention = new Intent(getActivity(), SynthesisActivity.class);

        // transmission of the route id
        intention.putExtra("pathId", pathItem.getId());
        // launch of the daughter activity
        launcher.launch(intention);
    }

    /**
     * On return of the intention of synthesis needle the user
     * on the selected page in function with the navbar or topbar.
     * @param result the result of the intention
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
     * Management of the menu containing the search fields.
     * This management includes opening and closing the menu.
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
     * Initializes the list of paths when displaying the fragment for the first time.
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
     * Initializes the scan service when the fragment is first displayed.
     */
    private void initPathFinder() {
        finder = new PathFinder(context, binding);
        finder.setOnPathsUpdate(parcoursItemList -> {
            this.pathItemList.clear();
            this.pathItemList.addAll(parcoursItemList);
            pathAdapter.notifyDataSetChanged();
        });

        finder.setOnError(error -> {
            Toast.makeText(context,getString(R.string.path_list_error), Toast.LENGTH_SHORT).show();
        });

    }

    /**
     * Refresh list
     */
    public void refresh() {
        finder.findPaths();
    }
}
