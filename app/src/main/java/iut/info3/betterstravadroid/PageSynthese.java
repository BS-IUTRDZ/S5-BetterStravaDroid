package iut.info3.betterstravadroid;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import iut.info3.betterstravadroid.databinding.PageSyntheseBinding;

public class PageSynthese extends AppCompatActivity {

    private PageSyntheseBinding binding;

    private ActivityResultLauncher<Intent> lanceur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = PageSyntheseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.topbar.ivEditIcon.setOnClickListener(view -> {toEdit(view);});
    }

    public void toEdit(View view) {
        Intent intent =
                new Intent(this,
                        PageModifParcours.class);

        String description = binding.cardRun.tvDescription.getText().toString();
        String titre = binding.cardRun.tvTitre.getText().toString();

        intent.putExtra("description",description );
        intent.putExtra("titre",titre);
        intent.putExtra("id","bouchon"); //TODO faire passer l'id du parcours selectionner

        lanceur = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::retourModif);
        lanceur.launch(intent);

    }

    public void retourModif(ActivityResult result){
        Intent intent = result.getData();
        String newTitre;
        String newDescription;

        if (result.getResultCode() == Activity.RESULT_OK) {
            newDescription = intent.getStringExtra("description");
            newTitre = intent.getStringExtra("titre");
        }else {
            newDescription = binding.cardRun.tvDescription.getText().toString();
            newTitre = binding.cardRun.tvTitre.getText().toString();
        }

        binding.cardRun.tvDescription.setText(newDescription);
        binding.cardRun.tvDescription.setText(newTitre);

    }

}