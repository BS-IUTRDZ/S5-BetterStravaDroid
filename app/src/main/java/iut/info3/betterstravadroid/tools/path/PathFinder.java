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

/**
 * Encapsulator of parameters to search paths.
 */
public class PathFinder {
    /**
     * When a request for path is perform only path with a date of registration under
     * dateSup are fetched
     */
    private String dateSup;

    /**
     * When a request for path is perform only path with a date of registration over
     * dateInf are fetched
     */
    private String dateInf;
    /**
     * When a request for path is perform only path with a length over
     * lengthMin are fetched
     */
    private int lengthMin;
    /**
     * When a request for path is perform only path with a length under
     * lengthMax are fetched
     */
    private int lengthMax;
    /** Text searched in description and title of path */
    private String textSearch;
    /** Http request builder  */
    private RequestBuilder builder;
    /** User token to authenticate him to the API */
    private String token;
    /** Listener triggered when a request for path list succeed, can be null*/
    private PathsUpdateListener updateListener;

    /** Listener triggered when a volley error pop, can be null */
    private Response.ErrorListener errorListener;
    /** Number of path already loaded useful for api pagination */
    private int nbPathAlreadyLoaded;

    /**
     *
     * @param context
     */
    public PathFinder(Context context) {
        token = context.getSharedPreferences("BetterStrava", Context.MODE_PRIVATE)
                .getString(UserPreferences.USER_KEY_TOKEN,"None");
        this.builder = new RequestBuilder(context);
        nbPathAlreadyLoaded = 0;
    }

    /**
     * Perform an HTTP request to the API to fetch paths.
     * Filter can be changed by setters of this class.
     * Among them we can find filter for :
     * - Minimal date of creation of the path
     * - Maximal date of creation of the path
     * - Minimal length of the path
     * - Maximal length of the path
     * - String to search on the title and the description of the path
     * We also can send to the api a number of path already loaded
     * to ask it for more path.
     */
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

    /**
     * Request to the api to archive one path.
     * Method : Put
     * If the operation succeed : refresh the local list of path
     * else : execute the error listener if exist
     * @param itemToDelete the path entity to delete
     */
    public void deletePath(PathItem itemToDelete) {
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

    /**
     * Handle a JsonArray object to transform it into an arrayList of PathItem.
     * After the creation of the arraylist, send a event to a
     * potential listener with the arraylist.
     * @param object JsonArray object with the list of path entities
     *               fetched from the api
     *
     */
    public void updatePaths(Object object) {
        List<PathItem> pathItemList = new ArrayList<>();

        JSONArray array = (JSONArray) object;
        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                pathItemList.add(new PathItem(jsonObject));
            }
            if (updateListener != null) {
                updateListener.onUpdate(pathItemList);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Catch an volley error by executing the listener previously settle.
     * @param error the volley error to catch
     */
    public void handleError(VolleyError error) {
        if (errorListener != null) {
            errorListener.onErrorResponse(error);
        }
    }

    /**
     * Set up a listener to handle the refresh list paths event.
     * @param updateListener Operations to do after a refresh list path event.
     */
    public void setOnPathsUpdate(PathsUpdateListener updateListener) {
        this.updateListener = updateListener;
    }

    /**
     * Set up a listener to handle a volley error
     * @param errorListener Operations to do after a volley error has been propagated
     */
    public void setOnError(Response.ErrorListener errorListener) {
        this.errorListener = errorListener;
    }

    /**
     * Set the filter for sup date at the given date.
     * The given date has to be with dd/MM/YYYY format.
     * Then make a call to the api to refresh the list of paths
     * @param dateSup max date selected by user on a datePicker
     */
    public void setDateSup(String dateSup) {
        this.dateSup = dateSup;
        findPaths();
    }

    /**
     * Set the filter for sup date at the given date.
     * The given date has to be with dd/MM/YYYY format
     * Then make a call to the api to refresh the list of paths
     * @param dateInf min date selected by user on a datePicker
     */
    public void setDateInf(String dateInf) {
        this.dateInf = dateInf;
        findPaths();
    }
    /**
     * Set the filter for sup length of paths at the given integer.
     * Then make a call to the api to refresh the list of paths
     * @param lengthMin min date selected by user on a datePicker
     */
    public void setLengthMin(int lengthMin) {
        this.lengthMin = lengthMin;
        findPaths();
    }
    /**
     * Set the filter for sup length of paths at the given integer.
     * Then make a call to the api to refresh the list of paths
     * @param lengthMax min date selected by user on a datePicker
     */
    public void setLengthMax(int lengthMax) {
        this.lengthMax = lengthMax;
        findPaths();
    }

    /**
     * Set the text filter of paths at the given string.
     * Then make a call to the api to refresh the list of paths
     * @param textSearch string to search on title and description paths
     */
    public void setTextSearch(String textSearch) {
        this.textSearch = textSearch;
        findPaths();
    }

    /**
     * Set the number of paths already loaded on the application.
     * It is useful for the API to limit number of paths to load
     * and for the pagination
     * @param nbPathAlreadyLoaded number of paths already loaded on the
     *                            paths scrollview container.
     */
    public void setNbPathAlreadyLoaded(int nbPathAlreadyLoaded) {
        this.nbPathAlreadyLoaded = nbPathAlreadyLoaded;
        findPaths();
    }

    /**
     * Listener to perform operation when the list of paths
     * is updated.
     */
    public interface PathsUpdateListener {

        void onUpdate(List<PathItem> pathItemList);

    }

}
