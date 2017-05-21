package com.syzible.loinnir.network;

/**
 * Created by ed on 17/05/2017.
 */

public class Endpoints {
    private static final int API_VERSION = 1;
    private static final String LOCAL_ENDPOINT = "http://10.0.2.2:3000";
    private static final String REMOTE_ENDPOINT = "http://13.94.249.94";
    private static final String BASE_URL = LOCAL_ENDPOINT + "/api/v" + API_VERSION;

    public static final String CREATE_USER = "/users/create";
    public static final String EDIT_USER = "/users/edit";
    public static final String DELETE_USER = "/users/delete";

    public static final String UPDATE_USER_LOCATION = "/users/update-location";
    public static final String GET_NEARBY_COUNT = "/users/get-nearby-count";

    public static final String GET_USER = "/users/get";
    public static final String GET_ALL_USERS = "/users/get-all";
    public static final String GET_OTHER_USERS = "/users/get-others";
    public static final String GET_NEARBY_USERS = "/users/get-nearby";
    public static final String GET_RANDOM_USER = "/users/get-random";

    public static final String GET_PARTNER_MESSAGES = "/messages/get-partner-messages";
    public static final String GET_LOCALITY_MESSAGES = "/messages/get-locality-messages";
    public static final String SEND_PARTNER_MESSAGE = "/messages/send-partner-message";
    public static final String SEND_LOCALITY_MESSAGE = "/messages/send-locality-message";

    public static final String GET_PARTNER_IDS = "/messages/get-partner-ids";
    public static final String SUBSCRIBE_TO_PARTNER = "/messages/subscribe-partner-conversation";
    public static final String GET_PAST_CONVERSATION_PREVIEWS = "/messages/get-past-conversation-previews";

    public static String getAbsoluteURL(String endpoint) {
        return BASE_URL + endpoint;
    }
}
