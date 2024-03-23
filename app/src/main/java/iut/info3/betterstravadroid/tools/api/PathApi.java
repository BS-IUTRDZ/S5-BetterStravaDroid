package iut.info3.betterstravadroid.tools.api;

public class PathApi {

    private PathApi() {
        // No need to instantiate the class, we can hide its constructor
    }

    public static final String PATH_API_LAST = ApiConfiguration.API_BASE_URL + "path/lastPath";
    public static final String PATH_API_ID = ApiConfiguration.API_BASE_URL + "path/";
    public static final String PATH_API_MODIF = ApiConfiguration.API_BASE_URL + "path/modifyDescription";

    public static final String PATH_API_SUPR = ApiConfiguration.API_BASE_URL + "path/archivingPath";
}
