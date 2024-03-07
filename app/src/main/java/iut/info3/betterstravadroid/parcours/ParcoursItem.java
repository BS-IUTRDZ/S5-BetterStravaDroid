package iut.info3.betterstravadroid.parcours;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import iut.info3.betterstravadroid.api.ApiConfiguration;

/**
 * Un item de la liste des parcours
 */
public class ParcoursItem {

    public static final String GET_ALL_PARCOUR = ApiConfiguration.API_BASE_URL + "path/findPath";
    public static final String GET_ALL_DEFAULT_PARCOUR = ApiConfiguration.API_BASE_URL + "path/findDefaultPaths";

    private long date;

    private String title;

    private String description;

    private String id;

    public ParcoursItem(JSONObject jsonObject) throws JSONException {
        this(jsonObject.getLong("date"),
             jsonObject.getString("nom"),
             jsonObject.getString("description"),
             jsonObject.getString("id"));
    }


    public ParcoursItem(long date, String title, String description, String id) {
        this.date = date;
        this.title = title;
        this.description = description;
        this.id = id;
    }

    public String getDate() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(date);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
        return sdf.format(c.getTime());
    }

    public String getTitre() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }
}
