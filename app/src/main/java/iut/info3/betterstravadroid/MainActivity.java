package iut.info3.betterstravadroid;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import iut.info3.betterstravadroid.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /*
         * on associe au ViewPager un adaptateur (c'est lui qui organise le défilement
         * entre les fragments à afficher)
         */
        binding.activityMainViewpager.setAdapter(new PageAdaptateur(this));
        binding.activityMainViewpager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setNavbarIcon(position);
            }
        });
    }

    private void setNavbarIcon(int position) {

        switch(position) {
            case 0 :
                // Gestion des boutons sur la page d'accueil
                binding.navbar.homeButtonInactive.setVisibility(View.INVISIBLE);
                binding.navbar.homeButtonActive.setVisibility(View.VISIBLE);

                setParcoursButton();

                binding.navbar.pathButtonInactive.setVisibility(View.VISIBLE);
                binding.navbar.pathButtonActive.setVisibility(View.INVISIBLE);

                binding.activityMainViewpager.setUserInputEnabled(true);
                break;
            case 1 :
                // Gestion des boutons sur la page d'accueil
                binding.navbar.homeButtonInactive.setVisibility(View.VISIBLE);
                binding.navbar.homeButtonActive.setVisibility(View.INVISIBLE);

                setParcoursButton();

                binding.navbar.pathButtonInactive.setVisibility(View.VISIBLE);
                binding.navbar.pathButtonActive.setVisibility(View.INVISIBLE);

                binding.activityMainViewpager.setUserInputEnabled(false);
                break;
            case 2 :
                // Gestion des boutons de la page liste parcours
                binding.navbar.homeButtonInactive.setVisibility(View.VISIBLE);
                binding.navbar.homeButtonActive.setVisibility(View.INVISIBLE);

                setParcoursButton();

                binding.navbar.pathButtonInactive.setVisibility(View.INVISIBLE);
                binding.navbar.pathButtonActive.setVisibility(View.VISIBLE);

                binding.activityMainViewpager.setUserInputEnabled(true);

                break;
            default :
                break;
        }
    }

    private void setParcoursButton() {
        if (!PageParcours.play) {
            binding.navbar.pauseButton.setVisibility(View.INVISIBLE);
            binding.navbar.playButton.setVisibility(View.VISIBLE);
        } else {
            binding.navbar.pauseButton.setVisibility(View.VISIBLE);
            binding.navbar.playButton.setVisibility(View.INVISIBLE);
        }
    }

    /* Appui sur le bouton accueil de la navbar */
    public void goToHome(View view) {
        binding.activityMainViewpager.setCurrentItem(PageAdaptateur.PAGE_ACCUEIL);
    }

    /* Appui sur le bouton parcours de la navbar */
    public void goToPathList(View view) {
        binding.activityMainViewpager.setCurrentItem(PageAdaptateur.PAGE_LISTE_PARCOURS);
    }

    public void pauseButton(View view){

        //Si parcours pause du parcours
        PageParcours.play = false;
        view.setVisibility(View.INVISIBLE);
        binding.navbar.playButton.setVisibility(View.VISIBLE);

    }

    public void playButton(View view){
        //Si pas de parcours tp sur page parcours
        //Si parcours reprise du parcours

        if (PageParcours.parcours) {
            view.setVisibility(View.INVISIBLE);
            binding.navbar.pauseButton.setVisibility(View.VISIBLE);
            PageParcours.play = true;
        }

        binding.activityMainViewpager.setCurrentItem(PageAdaptateur.PAGE_PARCOURS);
    }
}