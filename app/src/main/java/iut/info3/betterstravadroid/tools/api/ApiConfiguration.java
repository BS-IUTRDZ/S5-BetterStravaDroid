package iut.info3.betterstravadroid.tools.api;

/**
 * Class containing the unique API access url. This url is the basis of all others.
 */
public class ApiConfiguration {

    private ApiConfiguration() {
        /* No need to instantiate the class, we can hide its constructor */
    }

    public static final String API_BASE_URL = "https://bsapi.alpha-line.xyz/api/";
}
