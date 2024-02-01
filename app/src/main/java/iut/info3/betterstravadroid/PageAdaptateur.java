package iut.info3.betterstravadroid;

import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class PageAdaptateur extends FragmentStateAdapter {

    /** Nombre de fragments gérés par cet adaptateur */
    private static final int NB_FRAGMENT = 3;

    /* Position des différentes pages dans l'application */
    public static final int PAGE_ACCUEIL = 0;
    public static final int PAGE_PARCOURS = 1;
    public static final int PAGE_LISTE_PARCOURS = 2;
    private final MainActivity activity;


    public PageAdaptateur(MainActivity activity) {
        super(activity);
        this.activity = activity;
    }

    @Override
    public Fragment createFragment(int position) {

        switch(position) {
            case PAGE_ACCUEIL:
                return PageAccueil.newInstance();
            case PAGE_PARCOURS:
                return PageParcours.newInstance();
            case PAGE_LISTE_PARCOURS:
                return PageListeParcours.newInstance();
            default :
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return NB_FRAGMENT;
    }
}
