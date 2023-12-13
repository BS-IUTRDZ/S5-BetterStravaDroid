package iut.info3.betterstravadroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class PageConnexion extends AppCompatActivity {

    public static PageConnexion instance;

    public static PageConnexion getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.page_connexion);
    }

    public void goToInscription(View view) {
        Intent intent = new Intent(this, PageInscription.class);
        startActivity(intent);
    }
}