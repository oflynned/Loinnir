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

    private static String getToken(Context context) {
        return LocalStorage.getPref(LocalStorage.Pref.fb_access_token, context);
    }

    public static boolean hasExistingToken(Context context) {
        return !getToken(context).equals("");
    }

    private static void clearToken(Context context) {
        LocalStorage.purgePref(LocalStorage.Pref.fb_access_token, context);
    }

    public static void getStoredPrefs(Context context) {
        for (LocalStorage.Pref pref : LocalStorage.Pref.values())
            System.out.println(pref.name() + ": " + LocalStorage.getPref(pref, context));
    }

    public static void deleteToken(Context context) {
        clearToken(context);
        LoginManager.getInstance().logOut();
    }
}
