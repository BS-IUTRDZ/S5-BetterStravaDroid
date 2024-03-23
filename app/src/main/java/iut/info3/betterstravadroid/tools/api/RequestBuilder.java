package iut.info3.betterstravadroid.tools.api;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class RequestBuilder {

    private static final String TAG = "RequestBuilder";
    private RequestQueue fileRequetes;

    private Map<String, String> header;
    private Object body;
    private int method;

    private Consumer<VolleyError> onError;
    private Consumer<Object> onSucces;

    public RequestBuilder(RequestBuilder requestBuilder) {
        fileRequetes = requestBuilder.fileRequetes;
        header = requestBuilder.header;
        body = requestBuilder.body;
        method = requestBuilder.method;
        onError = requestBuilder.onError;
        onSucces = requestBuilder.onSucces;
    }

    public RequestBuilder(Context context) {
        this(Volley.newRequestQueue(context));
    }


    public RequestBuilder(RequestQueue fileRequetes) {
        this.fileRequetes = fileRequetes;
        reset();
    }




    /**
     * Créer une nouvelle requête de type JsonObjectRequest.
     * Embarque la files des requêtes ainsi que la requête
     * dans un nouvel objet dissociable
     * @param url l'url de la requête
     * @return un nouvel objet embarquant la file de requête
     *         et la requête
     */
    public RequestFinaliser<JsonObjectRequest> newJSONObjectRequest(String url) {
        Map<String, String> headers = new HashMap<>(header);

        JsonObjectRequest request = new JsonObjectRequest(method,
                url, (JSONObject) body,
                onSucces::accept, onError::accept) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }
        };

        return new RequestFinaliser(this, request);

    }

    /**
     * Créer une nouvelle requête de type JsonArrayRequest.
     * Embarque la files des requêtes ainsi que la requête
     * dans un nouvel objet dissociable
     * @param url l'url de la requête
     * @return un nouvel objet embarquant la file de requête
     *         et la requête
     */
    public RequestFinaliser<JsonArrayRequest> newJSONArrayRequest(String url) {
        Map<String, String> headers = new HashMap<>(header);
        JsonArrayRequest request = new JsonArrayRequest(
                method, url, (JSONArray) body,
                onSucces::accept, onError::accept) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }
        };

        return new RequestFinaliser<>(this, request);

    }


    /**
     * Définit le corp de la requête à envoyer.
     * Par défaut le type de la requête sera changer a POST
     * @param body le corp
     * @return le builder
     */
    public RequestBuilder withBody(Object body) {
        method(Request.Method.POST);
        this.body = body;
        return this;
    }

    /**
     * Définit le type d'appel
     * @param method le type d'appel
     * @return le builder
     */
    public RequestBuilder method(int method) {
        this.method = method;
        return this;
    }

    /**
     * Défini le header a envoyer pour la requête
     * @param header le header à envoyer
     * @return le builder
     */
    public RequestBuilder withHeader(Map<String, String> header) {
        this.header = header;
        return this;
    }

    /**
     * Ajoute un attribut à l'header de la requête
     * @param key la clé
     * @param value la valeur
     * @return le builder
     */
    public RequestBuilder addHeader(String key, String value) {
        header.put(key, value);
        return this;
    }
    /**
     * Défini les actions a effectuer lors de la réception de message d'erreur.
     * @param onError la listes des instruction
     * @return le builder
     */
    public RequestBuilder onError(Consumer<VolleyError> onError) {
        this.onError = onError;
        return this;
    }

    /**
     * Défini les actions a effectuer lors de la réception de message de succès.
     * @param onSucces la listes des instruction
     * @return le builder
     */
    public RequestBuilder onSucces(Consumer<Object> onSucces) {
        this.onSucces = onSucces;
        return this;
    }

    /**
     * Envoie d'une requête json déjà faite dans la file
     * @param jsonRequest requête a envoyer
     */
    public void send(JsonRequest jsonRequest) {
        try {
            Log.i(TAG,jsonRequest.getHeaders().toString());
        } catch (AuthFailureError e) {
            throw new RuntimeException(e);
        }
        fileRequetes.add(jsonRequest);
    }


    /**
     * Finaliseur de requête json.
     * Permet de séparer l'étape d'envoie de la requête
     * de l'étape de création tout en gardant l'aspect chainer.
     * @param <T> le type de la requête a envoyer.
     */
    public static class RequestFinaliser<T extends JsonRequest> {
        private RequestBuilder builder;
        private T request;

        private RequestFinaliser(RequestBuilder builder, T request) {
            this.builder = builder;
            this.request = request;
        }

        public void send() {
            builder.send(request);
            builder.reset();
        }

        public T get() {
            return request;
        }

    }


    private void reset() {
        method(Request.Method.GET);
        withHeader(new HashMap<>());
        onError(Throwable::printStackTrace);
        onSucces(System.out::println);
        body = null;
    }




}
