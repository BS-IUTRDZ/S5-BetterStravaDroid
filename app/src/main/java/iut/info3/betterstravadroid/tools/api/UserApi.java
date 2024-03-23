package iut.info3.betterstravadroid.tools.api;

public class UserApi {

    private UserApi() {
        // No need to instantiate the class, we can hide its constructor
    }

    public static final String USER_API_CREATE_ACCOUNT = ApiConfiguration.API_BASE_URL + "users/createAccount";
    public static final String USER_API_LOGIN = ApiConfiguration.API_BASE_URL + "users/login";
    public static final String USER_API_INFOS = ApiConfiguration.API_BASE_URL + "users/getInfo";

}
