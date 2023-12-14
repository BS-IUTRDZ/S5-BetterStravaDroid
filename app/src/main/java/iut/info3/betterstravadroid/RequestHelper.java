package iut.info3.betterstravadroid;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class RequestHelper {

    private static RequestQueue fileRequetes;

    /**
     * Méthode permettant de récupérer la file de requêtes
     * @return La file de requêtes
     */
    private static RequestQueue getFileRequetes() {
        if (fileRequetes == null) {
            fileRequetes = Volley.newRequestQueue(PageConnexion.getInstance());
        }
        return fileRequetes;
    }

    /**
     * Méthode permettant de faire une requête simple à une url et récupérer un JSONObject
     * @param url L'url de la requête
     * @param header Les headers de la requête
     * @param method La méthode de la requête (Voir {@link com.android.volley.Request.Method}})
     * @param processResponse La fonction à appeler en cas de succès
     * @param processError La fonction à appeler en cas d'erreur
     */
    public static void simpleJSONObjectRequest(String url,
                                               Map<String, String> header,
                                               JSONObject body,
                                               int method,
                                               Consumer<JSONObject> processResponse,
                                               Consumer<VolleyError> processError) {

        // Création de la requête
        JsonObjectRequest request = new JsonObjectRequest(
                method,
                url,
                body,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        processResponse.accept(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        processError.accept(error);
                    }
                }
        ) {
            // Headers
            @Override
            public Map<String, String> getHeaders() {
                if (header == null) {
                    return new HashMap<>();
                } else {
                    return header;
                }
            }
        };

        // Ajout de la requête à la file
        getFileRequetes().add(request);
    }


    /**
     * Méthode permettant de faire une requête simple à une url et de récupérer un tableau JSON
     * @param url L'url de la requête
     * @param header Les headers de la requête
     * @param method La méthode de la requête (Voir {@link com.android.volley.Request.Method}})
     * @param processResponse La fonction à appeler en cas de succès
     * @param processError La fonction à appeler en cas d'erreur
     */
    public static void simpleJSONArrayRequest(String url,
                                              Map<String, String> header,
                                              JSONArray body,
                                              int method,
                                              Consumer<JSONArray> processResponse,
                                              Consumer<VolleyError> processError) {

        // Création de la requête
        JsonArrayRequest request = new JsonArrayRequest(
                method,
                url,
                body,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        processResponse.accept(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        processError.accept(error);
                    }
                }
        ) {
            // Headers
            @Override
            public Map<String, String> getHeaders() {
                if (header == null) {
                    return new HashMap<>();
                } else {
                    return header;
                }
            }
        };

        // Ajout de la requête à la file
        getFileRequetes().add(request);
    }

}
