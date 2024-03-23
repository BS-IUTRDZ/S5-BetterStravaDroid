package iut.info3.betterstravadroid.tools.path;

import android.content.Context;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import iut.info3.betterstravadroid.databinding.FragmentPathListBinding;
import iut.info3.betterstravadroid.tools.api.PathApi;
import iut.info3.betterstravadroid.tools.api.RequestBuilder;
import iut.info3.betterstravadroid.preferences.UserPreferences;

public class PathFinder {

    private final FragmentPathListBinding binding;

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

    public PathFinder(Context context, FragmentPathListBinding binding) {
        token = context.getSharedPreferences("BetterStrava", Context.MODE_PRIVATE)
                .getString(UserPreferences.USER_KEY_TOKEN,"None");
        this.builder = new RequestBuilder(context);
        nbPathAlreadyLoaded = 0;
        this.binding = binding;
    }

    public void findPaths() {

        String query = PathApi.API_PATH_ALL + "?";
        if (textSearch == null) textSearch = "";


        query += "dateInf=" + dateInf;
        query += "&dateSup=" + dateSup;
        query += "&nom=" + textSearch;
        query += "&distanceMin=" + lengthMin;
        if (lengthMin != 0 && lengthMax == 0) {
            query += "&distanceMax=" + 1000;
        } else {
            query += "&distanceMax=" + lengthMax;
        }

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
                    .newJSONObjectRequest(PathApi.API_PATH_DELETE).send();
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
            if (parcoursItemList.isEmpty()) {
                binding.aucunResultat.setVisibility(View.VISIBLE);
            } else {
                binding.aucunResultat.setVisibility(View.GONE);
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
