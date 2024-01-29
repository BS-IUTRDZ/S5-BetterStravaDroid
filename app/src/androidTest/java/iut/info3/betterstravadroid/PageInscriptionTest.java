package iut.info3.betterstravadroid;

import static androidx.test.espresso.matcher.RootMatchers.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;

import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.test.annotation.UiThreadTest;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import iut.info3.betterstravadroid.api.UserApi;

@RunWith(AndroidJUnit4.class)
public class PageInscriptionTest {
    @Rule
    public ActivityScenarioRule<PageInscription> activityScenarioRule =
            new ActivityScenarioRule<>(PageInscription.class);

    private View view;

    private ToastMaker toastMaker;
    @Before
    public void setUp() {
        activityScenarioRule.getScenario().onActivity(this::perform);
    }
    private void perform(PageInscription pageInscription) {


        toastMaker = mock(ToastMaker.class);
        view = pageInscription.getWindow().getDecorView();
        pageInscription.setToastMaker(toastMaker);
    }

    @Test
    public void onCreate() {

    }

    @Test
    public void backToConnexion() {
    }


    @Test
    @UiThreadTest
    public void boutonInscription() {
        Toast toast = mock(Toast.class);
        EditText nom = setUpMock("nom");
        EditText prenom = setUpMock("prenom");
        EditText courriel = setUpMock("courriel");
        EditText password = setUpMock("password");
        EditText confirmPassword = setUpMock("confirmPassword");
        ToastMaker maker = mock(ToastMaker.class);
        when(maker.makeText(any(),any(),anyInt())).thenReturn(toast);
        PageInscription pageInscription = new PageInscription(
                nom, prenom, courriel, password, confirmPassword
        );
        View view1 = mock(View.class);
        pageInscription.setToastMaker(maker);
        pageInscription.boutonInscription(view1);
        doNothing().when(toast).show();
        verify(maker).makeText(any(), any(),anyInt());

    }


    @Test
    public void handleResponse() {
    }

    @Test
    public void handleError() {
    }
    private EditText setUpMock(String text) {
        EditText mock = mock(EditText.class);
        Editable editable = mock(Editable.class);
        when(editable.toString()).thenReturn(text);
        when(mock.getText()).thenReturn(editable);
        return mock;
    }
}