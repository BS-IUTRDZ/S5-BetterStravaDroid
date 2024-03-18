package iut.info3.betterstravadroid.parcours;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import iut.info3.betterstravadroid.RequestBuilder;
import iut.info3.betterstravadroid.api.ApiConfiguration;
import iut.info3.betterstravadroid.databinding.ListeParcoursBinding;
import iut.info3.betterstravadroid.preferences.UserPreferences;

public class PathFinder {

    public static final String GET_ALL_PARCOUR = ApiConfiguration.API_BASE_URL + "path/findPath";
    public static final String DELETE_PARCOUR = ApiConfiguration.API_BASE_URL + "path/archivingPath";


    private String dateSup;
    private String dateInf;

    private int lengthMin;

    private int lengthMax;

    private String textSearch;

    private RequestBuilder builder;

    private String token;

    private PathsUpdateListener updateListener;
    private Response.ErrorListener errorListener;

    private int nbPathAlreadyLoaded;

    public PathFinder(Activity activity) {
        token = activity.getSharedPreferences("BetterStrava", Context.MODE_PRIVATE)
                .getString(UserPreferences.USER_KEY_TOKEN,"None");
        this.builder = new RequestBuilder(activity);
        nbPathAlreadyLoaded = 0;


    }

    public void findPaths() {

        String query = GET_ALL_PARCOUR + "?";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        if (dateInf == null) dateInf = "01/01/2024";
        if (dateSup == null) dateSup = formatter
                .format(LocalDate.now().plus(1, ChronoUnit.DAYS));
        if (textSearch == null) textSearch = "";

        query += "dateInf=" + dateInf;
        query += "&dateSup=" + dateSup;
        query += "&nom=" + textSearch;
        query += "&distanceMin=" + lengthMin;
        query += "&distanceMax=" + lengthMax;


        builder.addHeader("token", token)
                .addHeader("nbPathAlreadyLoaded", nbPathAlreadyLoaded + "")
                .onError(this::handleError)
                .onSucces(this::updatePaths)
                .newJSONArrayRequest(query).send();
    }

    public void deletePath(ParcoursItem itemToDelete) {
        try {
            JSONObject jsonObject = itemToDelete.toJson();
            builder.addHeader("token", token)
                    .onError(this::handleError)
                    .onSucces(d -> findPaths())
                    .withBody(jsonObject)
                    .method(Request.Method.PUT)
                    .newJSONObjectRequest(DELETE_PARCOUR).send();
        } catch (JSONException e) {
        }

    }



    public void updatePaths(Object object) {
        List<ParcoursItem> parcoursItemList = new ArrayList<>();

        JSONArray array = (JSONArray) object;
        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                parcoursItemList.add(new ParcoursItem(jsonObject));
            }
            if (updateListener != null) {
                updateListener.onUpdate(parcoursItemList);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleError(VolleyError error) {
        if (errorListener != null) {
            errorListener.onErrorResponse(error);
        }
    }

    public void setOnPathsUpdate(PathsUpdateListener updateListener) {
        this.updateListener = updateListener;
    }

    public void setOnError(Response.ErrorListener errorListener) {
        this.errorListener = errorListener;
    }

    public void setDateSup(String dateSup) {
        this.dateSup = dateSup;
        findPaths();
    }

    public void setDateInf(String dateInf) {
        this.dateInf = dateInf;
        findPaths();
    }

    public void setLengthMin(int lengthMin) {
        this.lengthMin = lengthMin;
        findPaths();
    }

    public void setLengthMax(int lengthMax) {
        this.lengthMax = lengthMax;
        findPaths();
    }

    public void setTextSearch(String textSearch) {
        this.textSearch = textSearch;
        findPaths();
    }

    public void setNbPathAlreadyLoaded(int nbPathAlreadyLoaded) {
        this.nbPathAlreadyLoaded = nbPathAlreadyLoaded;
        findPaths();
    }


    public interface PathsUpdateListener {

        void onUpdate(List<ParcoursItem> parcoursItemList);

    }

}
