package iut.info3.betterstravadroid.tools.path;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * An item from the list of courses
 */
public class PathItem {

    private long date;

    private String title;

    private String description;

    private String id;

    public PathItem(JSONObject jsonObject) throws JSONException {
        this(jsonObject.getLong("date"),
             jsonObject.getString("nom"),
             jsonObject.getString("description"),
             jsonObject.getString("id"));
    }


    public PathItem(long date, String title, String description, String id) {
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

    public JSONObject toJson() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
        jsonObject.put("date", date);
        jsonObject.put("nom", title);
        jsonObject.put("description", description);
        return jsonObject;
    }
}
