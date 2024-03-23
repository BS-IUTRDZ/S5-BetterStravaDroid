package iut.info3.betterstravadroid.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import iut.info3.betterstravadroid.activities.FragmentContainerActivity;
import iut.info3.betterstravadroid.fragments.HomeFragment;
import iut.info3.betterstravadroid.fragments.PathListFragment;
import iut.info3.betterstravadroid.fragments.PathFragment;

/**
 * Application fragment management class. The application contains 3 fragments,
 * <li>the home page,</li>
 * <li>the registration page of the courses</li>
 * <li>and that of the list of saved courses.</li>
 */
public class PageAdapter extends FragmentStateAdapter {

    /** Number of fragments managed by this adapter */
    private static final int NB_FRAGMENT = 3;

    /* Position of fragments in the application */
    public static final int HOME_PAGE = 0;
    public static final int PATH_PAGE = 1;
    public static final int PATH_LIST_PAGE = 2;
    private final FragmentContainerActivity activity;

    /**
     * Creating the Fragment Adapter.
     * @param activity the main activity to which the fragments are attached
     */
    public PageAdapter(FragmentContainerActivity activity) {
        super(activity);
        this.activity = activity;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch(position) {
            case HOME_PAGE:
                return HomeFragment.newInstance();
            case PATH_PAGE:
                return PathFragment.newInstance();
            case PATH_LIST_PAGE:
                return PathListFragment.newInstance(activity);
            default :
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return NB_FRAGMENT;
    }
}
