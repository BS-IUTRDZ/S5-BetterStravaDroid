package iut.info3.betterstravadroid.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import iut.info3.betterstravadroid.adapters.PageAdapter;
import iut.info3.betterstravadroid.databinding.ActivityMainBinding;
import iut.info3.betterstravadroid.fragments.HomeFragment;
import iut.info3.betterstravadroid.fragments.PathListFragment;
import iut.info3.betterstravadroid.fragments.PathFragment;

/**
 * Main class, associated with the application fragment.
 */
public class FragmentContainerActivity extends AppCompatActivity {

    /** Object in charge of linking this class to the layout */
    public ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /*
         * the ViewPager is associated with an adapter
         * (it organizes the scrolling between the fragments to display)
         */
        binding.activityMainViewpager.setAdapter(new PageAdapter(this));
        binding.activityMainViewpager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setNavbarIcon(position);
            }
        });
    }

    /**
     * Sets the navbar icons according to the fragment displayed on the phone.
     * @param position fragment number displayed on the interface
     */
    private void setNavbarIcon(int position) {

        switch(position) {
            case 0 : // Managing Home Page Buttons

                binding.navbar.homeButtonInactive.setVisibility(View.INVISIBLE);
                binding.navbar.homeButtonActive.setVisibility(View.VISIBLE);

                setPauseButton();

                binding.navbar.pathButtonInactive.setVisibility(View.VISIBLE);
                binding.navbar.pathButtonActive.setVisibility(View.INVISIBLE);

                binding.activityMainViewpager.setUserInputEnabled(true);
                break;
            case 1 : // Managing Path Page Buttons
                binding.navbar.homeButtonInactive.setVisibility(View.VISIBLE);
                binding.navbar.homeButtonActive.setVisibility(View.INVISIBLE);

                setPauseButton();

                binding.navbar.pathButtonInactive.setVisibility(View.VISIBLE);
                binding.navbar.pathButtonActive.setVisibility(View.INVISIBLE);

                binding.activityMainViewpager.setUserInputEnabled(false);
                break;
            case 2 : // Manage the buttons of the page list route
                binding.navbar.homeButtonInactive.setVisibility(View.VISIBLE);
                binding.navbar.homeButtonActive.setVisibility(View.INVISIBLE);

                setPauseButton();

                binding.navbar.pathButtonInactive.setVisibility(View.INVISIBLE);
                binding.navbar.pathButtonActive.setVisibility(View.VISIBLE);

                binding.activityMainViewpager.setUserInputEnabled(true);

                break;
            default :
                break;
        }
    }

    /**
     * Managing the pause button of the navbar.
     */
    private void setPauseButton() {
        if (!PathFragment.play) {
            binding.navbar.pauseButton.setVisibility(View.INVISIBLE);
            binding.navbar.playButton.setVisibility(View.VISIBLE);
        } else {
            binding.navbar.pauseButton.setVisibility(View.VISIBLE);
            binding.navbar.playButton.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Press the home button of the navbar
     */
    public void goToHome(View view) {
        binding.activityMainViewpager.setCurrentItem(PageAdapter.HOME_PAGE);
    }

    /**
     * Press the navigation button on the navbar
     */
    public void goToPathList(View view) {
        binding.activityMainViewpager.setCurrentItem(PageAdapter.PATH_LIST_PAGE);
    }

    /**
     * Allows you to update the pause button.
     * If a course is in progress, pause it.
     * @param view the button
     */
    public void pauseButton(View view){

        //Si parcours pause du parcours
        PathFragment.play = false;
        view.setVisibility(View.INVISIBLE);
        binding.navbar.playButton.setVisibility(View.VISIBLE);

    }

    /**
     * Allows you to update the pause button.
     * If a course is in progress, it is resumed.
     * Otherwise, the user is redirected to the registration page.
     * @param view the button
     */
    public void playButton(View view){
        if (PathFragment.parcours) {
            view.setVisibility(View.INVISIBLE);
            binding.navbar.pauseButton.setVisibility(View.VISIBLE);
            PathFragment.play = true;
        }

        binding.activityMainViewpager.setCurrentItem(PageAdapter.PATH_PAGE);
    }

    /**
     * Refresh of all fragments
     */
    public void refreshAll() {
        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("f0");
        if (homeFragment != null) {
            homeFragment.showLastPath();
            homeFragment.showUserInfos();
        }

        PathListFragment pathListFragment = (PathListFragment) getSupportFragmentManager().findFragmentByTag("f2");
        if (pathListFragment != null) {
            pathListFragment.rafraichir();
        }

    }
}
