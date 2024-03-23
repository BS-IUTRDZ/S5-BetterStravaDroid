package iut.info3.betterstravadroid;

import static junit.framework.TestCase.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.text.Editable;
import android.widget.EditText;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import iut.info3.betterstravadroid.tools.api.UserApi;
@RunWith(MockitoJUnitRunner.class)
public class UserApiTest {





    @Test
    public void createNewUserWithNotSamePassword() {
        EditText nom = setUpMock("nom");
        EditText prenom = setUpMock("prenom");
        EditText courriel = setUpMock("guillaume.medard@iut-rodez.fr");
        EditText password = setUpMock("password");
        EditText confirmPassword = setUpMock("confirmPassword");

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> new UserApi(nom, prenom, courriel, password, confirmPassword));
        assertEquals(UserApi.ERROR_MESSAGE_NOT_SAME_PASSWORD,exception.getMessage());

    }

    @Test
    public void createNewUserWithUnvalidEmail() {
        EditText nom = setUpMock("nom");
        EditText prenom = setUpMock("prenom");
        EditText courriel = setUpMock("unvalidEmail");
        EditText password = setUpMock("password");
        EditText confirmPassword = setUpMock("password");

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> new UserApi(nom, prenom, courriel, password, confirmPassword));
        assertEquals(UserApi.ERROR_MESSAGE_EMAIL_INVALID,exception.getMessage());

    }

    @Test
    public void createNewUserWithOneOrMoreFieldEmpty() {
        EditText nom = setUpMock("");
        EditText prenom = setUpMock("");
        EditText courriel = setUpMock("guillaume.medard@iut-rodez.fr");
        EditText password = setUpMock("password");
        EditText confirmPassword = setUpMock("password");

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> new UserApi(nom, prenom, courriel, password, confirmPassword));
        assertEquals(UserApi.ERROR_MESSAGE_ALL_FIELD_NOT_FILL,exception.getMessage());

    }

    @Test
    public void createNewUserWithNotStrongEnoughPassword() {
        EditText nom = setUpMock("nom");
        EditText prenom = setUpMock("prenom");
        EditText courriel = setUpMock("guillaume.medard@iut-rodez.fr");
        EditText password = setUpMock("e");
        EditText confirmPassword = setUpMock("e");

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> new UserApi(nom, prenom, courriel, password, confirmPassword));
        assertEquals(UserApi.ERROR_MESSAGE_NOT_STRONG_PASSWORD,exception.getMessage());

    }

    @Test
    public void createNewUserWithEverythingOk() {
        EditText nom = setUpMock("nom");
        EditText prenom = setUpMock("prenom");
        EditText courriel = setUpMock("guillaume.medard@iut-rodez.fr");
        EditText password = setUpMock("password");
        EditText confirmPassword = setUpMock("password");

        assertDoesNotThrow(() -> new UserApi(nom, prenom, courriel, password, confirmPassword));
    }

    @Test
    public void toJson() throws JSONException {
        EditText nom = setUpMock("nom");
        EditText prenom = setUpMock("prenom");
        EditText courriel = setUpMock("guillaume.medard@iut-rodez.fr");
        EditText password = setUpMock("password");
        EditText confirmPassword = setUpMock("password");
        UserApi userApi = new UserApi(nom, prenom, courriel, password, confirmPassword);
        JSONObject get = userApi.toJson();

        assertEquals("nom", get.get("nom"));
        assertEquals("prenom", get.get("prenom"));
        assertEquals("guillaume.medard@iut-rodez.fr", get.get("email"));
        assertEquals("password", get.get("motDePasse"));
    }


    private EditText setUpMock(String text) {
        EditText mock = mock(EditText.class);
        Editable editable = mock(Editable.class);
        when(editable.toString()).thenReturn(text);
        when(mock.getText()).thenReturn(editable);
        return mock;
    }

}
