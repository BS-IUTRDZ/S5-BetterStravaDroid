package iut.info3.betterstravadroid.entities;

import android.util.Patterns;

import org.json.JSONException;
import org.json.JSONObject;

public class UserEntity {

    public static final String ERROR_MESSAGE_ALL_FIELD_NOT_FILL =
            "Veuillez remplir tous les champs";

    public static final String ERROR_MESSAGE_NOT_SAME_PASSWORD =
            "Les mots de passe ne correspondent pas";

    public static final String ERROR_MESSAGE_EMAIL_INVALID =
            "Le courriel n'est pas valide";

    public static final String ERROR_MESSAGE_NOT_STRONG_PASSWORD =
            "Le mot de passe doit contenir au moins 8 caractères";

    private String nom;
    private String prenom;
    private String courriel;
    private String password;

    public UserEntity (String nom, String prenom, String courriel,
                       String password, String confirmPassword) {

        if (nom.isEmpty() || prenom.isEmpty() || courriel.isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()) {
            throw new IllegalArgumentException(ERROR_MESSAGE_ALL_FIELD_NOT_FILL);
        }

        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException(ERROR_MESSAGE_NOT_SAME_PASSWORD);
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(courriel).matches()) {
            throw new IllegalArgumentException(ERROR_MESSAGE_EMAIL_INVALID);

        }
        if (password.length() < 8) {
            throw new IllegalArgumentException(ERROR_MESSAGE_NOT_STRONG_PASSWORD);
        }

        this.prenom = prenom;
        this.nom = nom;
        this.password = password;
        this.courriel = courriel;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("nom", nom);
        object.put("prenom", prenom);
        object.put("email", courriel);
        object.put("motDePasse", password);
        return object;
    }
}
