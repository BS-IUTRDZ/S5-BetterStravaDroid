package iut.info3.betterstravadroid.parcours;

import iut.info3.betterstravadroid.api.ApiConfiguration;

/**
 * Un item de la liste des parcours
 */
public class ParcoursItem {

    public static final String GET_ALL_PARCOUR = ApiConfiguration.API_BASE_URL + "path/findPath";
    public static final String GET_ALL_DEFAULT_PARCOUR = ApiConfiguration.API_BASE_URL + "path/findDefaultPaths";

    private String date;

    private String title;

    private String description;


    public ParcoursItem(String date, String title, String description) {
        this.date = date;
        this.title = title;
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public String getTitre() {
        return title;
    }

    public String getDescription() {
        return description;

    }
}
