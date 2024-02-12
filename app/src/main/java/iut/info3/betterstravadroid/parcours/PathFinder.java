package iut.info3.betterstravadroid.parcours;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import iut.info3.betterstravadroid.R;
import iut.info3.betterstravadroid.RequestBuilder;
import iut.info3.betterstravadroid.databinding.ActivityMainBinding;
import iut.info3.betterstravadroid.databinding.ListeParcoursBinding;
import iut.info3.betterstravadroid.preferences.UserPreferences;

public class PathFinder {


    private static final String tag = "PathFinder";


    private Activity activity;
    private ListeParcoursBinding binding;
    private RequestBuilder builder;

    private List<ParcoursItem> parcoursItemList;

    public PathFinder(Activity activity, ListeParcoursBinding binding) {
        this.activity = activity;
        this.binding = binding;

        builder = new RequestBuilder(activity);
    }





    public List<ParcoursItem> findPaths(String token,
                                        @Nullable String dateInf,
                                        @Nullable String dateSup,
                                        @Nullable String parcourName) throws VolleyError {

        String query = ParcoursItem.GET_ALL_PARCOUR + "?";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        if (dateInf == null) dateInf = "01/01/2024";
        if (dateSup == null) dateSup = formatter.format(LocalDate.now());
        if (parcourName == null) parcourName = "";

        query += "dateInf=" + dateInf;
        query += "&dateSup=" + dateSup;
        query += "&nom=" + parcourName;

        Log.i(tag, query);
        Log.i(tag, token);

        RequestBuilder.RequestFinaliser finaliser = builder.onError(this::handleError)
                .onSucces(this::handleSucces)
                .addHeader(UserPreferences.USER_KEY_TOKEN, token)
                .newJSONArrayRequest(query);
        // TODO verifier que le header est bien transmis
        Log.i(tag, finaliser.get().getHeaders().toString());
        finaliser.send();
        if (parcoursItemList == null) throw new VolleyError();
        return parcoursItemList;
    }

    private void handleError(VolleyError error) {
        parcoursItemList = null;
        error.printStackTrace();
    }

    private void handleSucces(Object body) {
        parcoursItemList = new ArrayList<>();

        JSONArray array = (JSONArray) body;
        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                parcoursItemList.add(new ParcoursItem(jsonObject));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }
}
