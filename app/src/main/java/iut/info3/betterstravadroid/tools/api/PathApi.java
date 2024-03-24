package iut.info3.betterstravadroid.tools.api;

/**
 * Class containing all API access URLs for route management
 */
public class PathApi {

    private PathApi() {
        // No need to instantiate the class, we can hide its constructor
    }

    public static final String API_PATH_LAST = ApiConfiguration.API_BASE_URL + "path/lastPath";
    public static final String API_PATH_ID = ApiConfiguration.API_BASE_URL + "path/";
    public static final String API_PATH_ALL = ApiConfiguration.API_BASE_URL + "path/findPath";
    public static final String API_PATH_UPDATE = ApiConfiguration.API_BASE_URL + "path/modifyDescription";
    public static final String API_PATH_DELETE = ApiConfiguration.API_BASE_URL + "path/archivingPath";
    public static final String API_PATH_CREATE = ApiConfiguration.API_BASE_URL + "path/createPath";
}
