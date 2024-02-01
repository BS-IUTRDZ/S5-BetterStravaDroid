package iut.info3.betterstravadroid;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.view.View;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import android.content.SharedPreferences;
import android.widget.EditText;
import android.widget.Toast;

import androidx.test.annotation.UiThreadTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;

@RunWith(AndroidJUnit4.class)
public class PageConnexionTest {

    private PageConnexion pageConnexion;

    private EditText courriel;

    private EditText motDePasse;

    private RequestBuilder helper;
    private ToastMaker toastMaker;

    private Toast toast;



    @Before
    public void setUp() {
        toast = mock(Toast.class);
        doNothing().when(toast).show();
        toastMaker = mock(ToastMaker.class);
        when(toastMaker.makeText(any(),any(),anyInt())).thenReturn(toast);

    }


    @Test
    @UiThreadTest
    public void boutonConnexionWithEmptyField() {
        // Given un ou plusieur champ laisser vide par l'utilisateur
        courriel = PageInscriptionTest.setUpMock("");
        motDePasse = PageInscriptionTest.setUpMock("");
        pageConnexion = new PageConnexion(courriel, motDePasse);
        View view = mock(View.class);
        pageConnexion.setHelper(helper);
        pageConnexion.setToastMaker(toastMaker);
        // When il clique sur le bouton se connecter
        pageConnexion.boutonConnexion(view);
        // Then un message d'erreur est afficher lui indiquant qu'il doit
        // finir de remplir les champs
        verify(toastMaker).makeText(any(),eq("Veuillez saisir votre courriel et votre mot de passe"),anyInt());

    }

    @Test
    @UiThreadTest
    public void handleErrorApi() {

        // Given une erreur envoyé par l'api lors de l'appel
        pageConnexion = new PageConnexion();
        String errorMessage = "{\"erreur\" : \"Erreur api\" }";
        VolleyError error = new VolleyError(
                new NetworkResponse(errorMessage.getBytes()));
        pageConnexion.setToastMaker(toastMaker);
        // When elle est reçut par l'application
        pageConnexion.handleError(error);
        // Then un toast est afficher avec le message
        verify(toastMaker).makeText(any(), eq("Erreur api"), anyInt());


    }

    @Test
    @UiThreadTest
    public void handleErrorJsonFormatException() {
        // Given un message provenant de l'api qui ne respecte
        // pas le format JSON
        pageConnexion = new PageConnexion();
        String errorMessage = "\"erreur\" : \"Erreur api\" }";
        VolleyError error = new VolleyError(
                new NetworkResponse(errorMessage.getBytes()));
        pageConnexion.setToastMaker(toastMaker);

        // When il est reçut par l'api
        pageConnexion.handleError(error);
        // Alors un message d'erreur générique est afficher
        verify(toastMaker).makeText(any(), eq("Erreur lors de la connexion"), anyInt());
    }


    @Test
    @UiThreadTest
    public void handleRespone() throws JSONException {

        // Given un message de succès avec un token renvoyer par l'api
        SharedPreferences.Editor editor = mock(SharedPreferences.Editor.class);
        pageConnexion = new PageConnexion();
        pageConnexion.setEditor(editor);
        pageConnexion.setToastMaker(toastMaker);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token","tokenApi");
        // When il est reçut par l'application
        pageConnexion.handleResponse(jsonObject);
        // Then il est stocker dans les préférences de l'application
        verify(editor).putString("token", "tokenApi");
        verify(editor).apply();
        // And un message de succès est affiché
        verify(toastMaker).makeText(any(), eq("Utilisateur connecté"),anyInt());

    }
}