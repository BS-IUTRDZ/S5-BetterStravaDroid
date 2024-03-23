package iut.info3.betterstravadroid.tools.api;

/**
 * Class containing all API access URLs for user management.
 */
public class UserApi {

    private UserApi() {
        // No need to instantiate the class, we can hide its constructor
    }

    public static final String API_USER_CREATE_ACCOUNT = ApiConfiguration.API_BASE_URL + "users/createAccount";
    public static final String API_USER_LOGIN = ApiConfiguration.API_BASE_URL + "users/login";
    public static final String API_USER_INFO = ApiConfiguration.API_BASE_URL + "users/getInfo";

}
