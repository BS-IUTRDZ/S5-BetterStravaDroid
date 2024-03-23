package iut.info3.betterstravadroid.adapters;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import iut.info3.betterstravadroid.tools.api.RequestBuilder;
import iut.info3.betterstravadroid.activities.FragmentContainerActivity;
import iut.info3.betterstravadroid.fragments.AccueilFragment;
import iut.info3.betterstravadroid.fragments.ListeParcoursFragment;
import iut.info3.betterstravadroid.fragments.ParcoursFragment;

public class PageAdapter extends FragmentStateAdapter {

    /** Nombre de fragments gérés par cet adaptateur */
    private static final int NB_FRAGMENT = 3;

    /* Position des différentes pages dans l'application */
    public static final int PAGE_ACCUEIL = 0;
    public static final int PAGE_PARCOURS = 1;
    public static final int PAGE_LISTE_PARCOURS = 2;
    private final FragmentContainerActivity activity;

    private RequestBuilder builder;


    public PageAdapter(FragmentContainerActivity activity) {
        super(activity);
        this.activity = activity;
        builder = new RequestBuilder(activity);
    }

    @Override
    public Fragment createFragment(int position) {

        switch(position) {
            case PAGE_ACCUEIL:
                return AccueilFragment.newInstance();
            case PAGE_PARCOURS:
                return ParcoursFragment.newInstance();
            case PAGE_LISTE_PARCOURS:
                return ListeParcoursFragment.newInstance(activity);
            default :
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return NB_FRAGMENT;
    }
}
