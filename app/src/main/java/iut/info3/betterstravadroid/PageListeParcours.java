package iut.info3.betterstravadroid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import iut.info3.betterstravadroid.databinding.ListeParcoursBinding;
import iut.info3.betterstravadroid.parcours.ParcoursAdaptateur;
import iut.info3.betterstravadroid.parcours.ParcoursItem;

public class PageListeParcours extends Fragment {

    private ListeParcoursBinding binding;
    private List<ParcoursItem> parcoursItemList;
    private ParcoursAdaptateur parcoursAdaptateur;

    public PageListeParcours() {
        //Require empty public constructor
    }

    public static PageListeParcours newInstance() {
        PageListeParcours pageListeParcours = new PageListeParcours();
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
        View vue = binding.getRoot();

        //Gestion du RecyclerView
        initialiseListeParcours();

        LinearLayoutManager gestionnaireLineaire = new LinearLayoutManager(this.getContext());
        binding.recyclerView.setLayoutManager(gestionnaireLineaire);

        parcoursAdaptateur = new ParcoursAdaptateur(parcoursItemList);
        binding.recyclerView.setAdapter(parcoursAdaptateur);

        return vue;

    }

    private void initialiseListeParcours() {
        parcoursItemList = new ArrayList<>();//Stub
        //TODO
    }
}
