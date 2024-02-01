package iut.info3.betterstravadroid;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.test.annotation.UiThreadTest;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.android.volley.Cache;
import com.android.volley.ExecutorDelivery;

import org.junit.jupiter.api.Disabled;
import org.junit.runner.RunWith;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.Network;
import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.ResponseDelivery;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;


import iut.info3.betterstravadroid.api.UserApi;

@RunWith(AndroidJUnit4.class)
public class PageInscriptionTest {



    private EditText nom, prenom, courriel, password, confirmPassword;

    private Toast toast;

    private ToastMaker toastMaker;

    @Before
    public void setUp() {
        nom = setUpMock("nom");
        prenom = setUpMock("prenom");
        courriel = setUpMock("courriel");
        password = setUpMock("password");
        confirmPassword = setUpMock("confirmPassword");

        toast = mock(Toast.class);
        doNothing().when(toast).show();
        toastMaker = mock(ToastMaker.class);
        when(toastMaker.makeText(any(),any(),anyInt())).thenReturn(toast);


    }




    @Test
    @UiThreadTest
    public void handleResponse() {
        // Given une réponse d'api correcte
        PageInscription pageInscription = new PageInscription();
        pageInscription.setToastMaker(toastMaker);
        // When elle est reçut par l'application
        pageInscription.handleResponse(new Object());
        // Then un message de succès est affiché
        verify(toastMaker).makeText(any(), eq("Inscription réussie"), anyInt());
    }


    @Test
    @UiThreadTest
    public void boutonInscription() {
        // Given un utilisateur ayant entrer une information incorrecte
        PageInscription pageInscription = new PageInscription(
                nom, prenom, courriel, password, confirmPassword
        );

        View view1 = mock(View.class);
        pageInscription.setToastMaker(toastMaker);
        // When Il clique sur le bouton s'inscrire
        pageInscription.boutonInscription(view1);
        // Alors un message d'erreur apparait
        verify(toastMaker).makeText(any(), eq(UserApi.ERROR_MESSAGE_NOT_SAME_PASSWORD),anyInt());

    }


    @Test
    @UiThreadTest
    @Disabled
    public void handleResponseWithRequest() throws Exception {
        // Given un utilisateur ayant entrer des information correctes
        setUpGoodMock();

        PageInscription pageInscription = spy(new PageInscription(
                nom, prenom, courriel, password, confirmPassword
        ));
        View view1 = mock(View.class);
        pageInscription.setToastMaker(toastMaker);
        // And une réponse de l'api correcte
        Network network = mock(Network.class);
        ResponseDelivery deliver = spy(new ExecutorDelivery(new Handler(Looper.getMainLooper())));
        Cache cache = mock(Cache.class);
        RequestQueue queue = spy(new RequestQueue(cache, network,4,deliver));
        RequestBuilder helper = spy(new RequestBuilder(queue));
        queue.start();
        RequestBuilder.RequestFinaliser<JsonObjectRequest> request = spy(helper
                .newJSONObjectRequest(""));
        when(helper.newJSONObjectRequest(any())).thenReturn(request);

        NetworkResponse response = new NetworkResponse((
                "{ \"message\" : \"Utilisateur correctement insérer \","
                +" \"utilisateur\" : \"guillaume\"}").getBytes());
        when(network.performRequest(any())).thenReturn(response);
        pageInscription.setRequestHelper(helper);

        // When il clique sur le bouton s'inscrire
        pageInscription.boutonInscription(view1);


        verify(queue, times(1)).add(any());
        verify(deliver, times(0)).postError(any(),any());
        verify(deliver, times(1)).postResponse(any(),any());
        // Then : Le test ne peut pas capter la notification sur listener
        // car sur un thread different on ne peut donc pas verifier si le toastMaker
        // a bien été appeler :
        // verify(toastMaker, times(1)).makeText(any(),eq("Test"),anyInt());
    }


    public RequestQueue getRequestQueue() {
        Network network = mock(Network.class);
        ResponseDelivery deliver = mock(ExecutorDelivery.class);
        Cache cache = mock(Cache.class);
        RequestQueue queue = spy(new RequestQueue(cache, network,4,deliver));
        return queue;
    }



    @Test
    @UiThreadTest
    public void handleError() {
        // Given une erreur réseau lors de l'appel pour l'inscription à l'api
        PageInscription pageInscription = new PageInscription();
        String messageError =  "";
        VolleyError error = new VolleyError(new NetworkResponse(messageError.getBytes()));
        pageInscription.setToastMaker(toastMaker);
        // When elle est reçut par l'application
        pageInscription.handleError(error);

        // Then un message d'erreur est afficher
        verify(toastMaker).makeText(any(), eq("Erreur lors de l'inscription"),anyInt());
    }


    private void setUpGoodMock() {
        nom = setUpMock("nom");
        prenom = setUpMock("prenom");
        courriel = setUpMock("guillaume.medard@iut-rodez.fr");
        password = setUpMock("password");
        confirmPassword = setUpMock("password");
    }

    public static EditText setUpMock(String text) {
        EditText mock = mock(EditText.class);
        Editable editable = mock(Editable.class);
        when(editable.toString()).thenReturn(text);
        when(mock.getText()).thenReturn(editable);
        return mock;
    }






}