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


    private long date;

    private String title;

    private String description;

    public ParcoursItem(JSONObject jsonObject) throws JSONException {
        this(jsonObject.getLong("date"),
                jsonObject.getString("nom"),
                jsonObject.getString("description"));
    }


    public ParcoursItem(long date, String title, String description) {
        this.date = date;
        this.title = title;
        this.description = description;
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
}
