package iut.info3.betterstravadroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import iut.info3.betterstravadroid.databinding.PageSyntheseBinding;

public class PageSynthese extends AppCompatActivity {

    private PageSyntheseBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = PageSyntheseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intention = getIntent();
        String information  = intention.getStringExtra("idPath");

        binding.cardRun.parcourTitre.setText(information);
    }

}