package iut.info3.betterstravadroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import iut.info3.betterstravadroid.databinding.PageModificationParcoursBinding;

public class PageModifParcours extends AppCompatActivity {

    PageModificationParcoursBinding binding;

    @Override
    public void onCreate (Bundle savedInstance) {

        super.onCreate(savedInstance);

        binding = PageModificationParcoursBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intention = getIntent();
        String titre = intention.getStringExtra("titre");
        String description = intention.getStringExtra("description");
        binding.editTitre.setText(titre);
        binding.editTitre.setEnabled(false);
        binding.editDescription.setText(description);

        binding.btnAnnuler.setOnClickListener(view -> {onClickAnnuler();});
        binding.btnAnnuler.setOnClickListener(view -> {onClickValider();});

    }

    public void onClickAnnuler() {
        Intent intentionRetour = new Intent();
        setResult(Activity.RESULT_CANCELED, intentionRetour);
        this.finish();
    }

    public void onClickValider() {

        String newDescritpion = binding.editDescription.getText().toString();
        Intent intentionRetour = new Intent();
        intentionRetour.putExtra("description", newDescritpion) ;
        setResult(Activity.RESULT_OK, intentionRetour);

        //TODO appel a l'api pout modifer description et titre

        this.finish();

    }

}
