package iut.info3.betterstravadroid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import iut.info3.betterstravadroid.databinding.PageAccueilBinding;

public class PageAccueil extends Fragment {

    private PageAccueilBinding binding;

    public PageAccueil() {
        //Require empty public constructor
    }

    public static PageAccueil newInstance() {
        PageAccueil pageAccueil = new PageAccueil();
        return pageAccueil;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = PageAccueilBinding.inflate(inflater, container, false);
        View vue = binding.getRoot();

        return vue;

    }

    public void goToPathList(View view) {

    }

}
