package iut.info3.betterstravadroid;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

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

        binding.cardLastRun.map.setDestroyMode(false);

        afficherParcour();

        return vue;


    }

    public void afficherParcour() {
        Polyline line = new Polyline(binding.cardLastRun.map);
        List<GeoPoint> trajet = new ArrayList<>();
        GeoPoint centre;

        // Création du trajet
        // BOUCHON
        trajet.add(new GeoPoint(44.36336875796618, 2.5737746730180295));
        trajet.add(new GeoPoint(44.36164391301725, 2.5703912027612827));

        line.getOutlinePaint().setColor(Color.RED);
        line.getOutlinePaint().setStrokeWidth(10);
        line.setPoints(trajet);
        line.setGeodesic(true);

        binding.cardLastRun.map.zoomToBoundingBox(line.getBounds(), false);

        // Ajout de l'overlay du trajet sur la carte
        binding.cardLastRun.map.getOverlayManager().add(line);

        binding.cardLastRun.map.addOnFirstLayoutListener((v, left, top, right, bottom) -> {
            binding.cardLastRun.map.zoomToBoundingBox(line.getBounds(), false, 200);
            binding.cardLastRun.map.getController().setCenter(line.getBounds().getCenterWithDateLine());

            // On laisse de la place vers le bas pour que le trajet ne soit pas caché par la
            // cardview qui contient les infos du trajet
            binding.cardLastRun.map.scrollBy(0, 100);
            binding.cardLastRun.map.invalidate();
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.i("PageAccueil", "onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        Log.i("PageAccueil", "onPause");
        super.onPause();
    }

    @Override
    public void onResume() {
        Log.i("PageAccueil", "onResume");
        super.onResume();
    }
}
