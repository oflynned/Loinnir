package com.syzible.loinnir.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.login.LoginManager;
import com.syzible.loinnir.objects.FacebookUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by ed on 12/05/2017.
 */

public class FacebookUtils {

    public static void saveToken(String token, Context context) {
        LocalStorage.setPref(LocalStorage.Pref.fb_access_token, token, context);
    }

    public static String getToken(Context context) {
        return LocalStorage.getPref(LocalStorage.Pref.fb_access_token, context);
    }

    public static boolean hasExistingToken(Context context) {
        return !getToken(context).equals("-1");
    }

    private static void clearToken(Context context) {
        LocalStorage.purgePref(LocalStorage.Pref.fb_access_token, context);
    }

    public static HashMap<String, String> getPrefs(Context context) {

        String id = LocalStorage.getPref(LocalStorage.Pref.id, context);
        String first_name = LocalStorage.getPref(LocalStorage.Pref.first_name, context);
        String last_name = LocalStorage.getPref(LocalStorage.Pref.last_name, context);
        String email = LocalStorage.getPref(LocalStorage.Pref.email, context);
        String gender = LocalStorage.getPref(LocalStorage.Pref.gender, context);
        String profile_url = LocalStorage.getPref(LocalStorage.Pref.profile_pic, context);

        HashMap<String, String> details = new HashMap<>();
        details.put(LocalStorage.Pref.id.name(), id);
        details.put(LocalStorage.Pref.first_name.name(), first_name);
        details.put(LocalStorage.Pref.last_name.name(), last_name);
        details.put(LocalStorage.Pref.email.name(), email);
        details.put(LocalStorage.Pref.gender.name(), gender);
        details.put(LocalStorage.Pref.profile_pic.name(), profile_url);

        return details;
    }

    private static void saveFacebookUserInfo(FacebookUser user, Context context) {
        LocalStorage.setPref(LocalStorage.Pref.id, user.getId(), context);
        LocalStorage.setPref(LocalStorage.Pref.first_name, user.getFirstName(), context);
        LocalStorage.setPref(LocalStorage.Pref.last_name, user.getLastName(), context);
        LocalStorage.setPref(LocalStorage.Pref.email, user.getEmail(), context);
        LocalStorage.setPref(LocalStorage.Pref.gender, user.getGender(), context);
        LocalStorage.setPref(LocalStorage.Pref.profile_pic, user.getProfilePicURL(), context);
    }

    public static void getStoredPrefs(Context context) {
        for(LocalStorage.Pref pref : LocalStorage.Pref.values())
            System.out.println(pref.name() + ": " + LocalStorage.getPref(pref, context));
    }

    public static JSONObject getFacebookData(JSONObject object, Context context) {
        JSONObject details = new JSONObject();

        try {
            String id = object.getString(LocalStorage.Pref.id.name());
            URL profile_pic;

            try {
                profile_pic = new URL("https://graph.facebook.com/" + id + "/picture?type=large");
                details.put(LocalStorage.Pref.profile_pic.name(), profile_pic.toString());
                details.put(LocalStorage.Pref.id.name(), id);

                if (object.has(LocalStorage.Pref.first_name.name()))
                    details.put(LocalStorage.Pref.first_name.name(), object.getString(LocalStorage.Pref.first_name.name()));
                if (object.has(LocalStorage.Pref.last_name.name()))
                    details.put(LocalStorage.Pref.last_name.name(), object.getString(LocalStorage.Pref.last_name.name()));
                if (object.has(LocalStorage.Pref.email.name()))
                    details.put(LocalStorage.Pref.email.name(), object.getString(LocalStorage.Pref.email.name()));
                if (object.has(LocalStorage.Pref.gender.name()))
                    details.put(LocalStorage.Pref.gender.name(), object.getString(LocalStorage.Pref.gender.name()));
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            }

            FacebookUser user = new FacebookUser(id,
                    object.getString("first_name"),
                    object.getString("last_name"),
                    object.getString("email"),
                    object.getString("gender"),
                    profile_pic.toString());

            saveFacebookUserInfo(user, context);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return details;
    }

    public static void deleteToken(final Context context) {
        new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken old, AccessToken curr) {
                if (curr == null) {
                    clearToken(context);
                    LoginManager.getInstance().logOut();
                }
            }
        };
    }
}
