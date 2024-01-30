package iut.info3.betterstravadroid;

import android.os.Bundle;
import android.util.Log;
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

        // on récupère un accès sur le ViewPager défini dans la vue (le layout)
        ViewPager2 pager = findViewById(R.id.activity_main_viewpager);
        /*
         * on associe au ViewPager un adaptateur (c'est lui qui organise le défilement
         * entre les fragments à afficher)
         */
        pager.setAdapter(new PageAdaptateur(this));
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setNavbarIcon(position);
            }
        });
    }

    private void setNavbarIcon(int position) {
        Log.i("MainACtivity", "position : " + position);
        switch(position) {
            case 0 :
                // Gestion des boutons sur la page d'accueil
                binding.navbar.homeButtonInactive.setVisibility(View.INVISIBLE);
                binding.navbar.homeButtonActive.setVisibility(View.VISIBLE);

                binding.navbar.pauseButton.setVisibility(View.INVISIBLE);
                binding.navbar.playButton.setVisibility(View.VISIBLE);

                binding.navbar.pathButtonInactive.setVisibility(View.VISIBLE);
                binding.navbar.pathButtonActive.setVisibility(View.INVISIBLE);
                break;
//            case 1 :
//                return .newInstance();
            case 1 :
                // Gestion des boutons de la page liste parcours
                binding.navbar.homeButtonInactive.setVisibility(View.VISIBLE);
                binding.navbar.homeButtonActive.setVisibility(View.INVISIBLE);

                binding.navbar.pauseButton.setVisibility(View.INVISIBLE);
                binding.navbar.playButton.setVisibility(View.VISIBLE);

                binding.navbar.pathButtonInactive.setVisibility(View.INVISIBLE);
                binding.navbar.pathButtonActive.setVisibility(View.VISIBLE);
                break;
            default :
                break;
        }
    }
}
