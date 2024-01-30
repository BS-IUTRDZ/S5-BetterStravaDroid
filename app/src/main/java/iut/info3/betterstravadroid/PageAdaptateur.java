package iut.info3.betterstravadroid;

import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class PageAdaptateur extends FragmentStateAdapter {

    /** Nombre de fragments gérés par cet adaptateur */
    private static final int NB_FRAGMENT = 2;
    private final MainActivity activity;


    public PageAdaptateur(MainActivity activity) {
        super(activity);
        this.activity = activity;
    }

    @Override
    public Fragment createFragment(int position) {

        switch(position) {
            case 0 :
                return PageAccueil.newInstance();
//            case 1 :
//                return .newInstance();
            case 1 :
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
