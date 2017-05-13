package com.syzible.loinnir.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by ed on 08/05/2017.
 */

public class LocalStorage {
    public enum Pref {
        id, fb_access_token, first_name, last_name, email, gender, profile_pic
    }

    public static boolean isLoggedIn(Context context) {
        return !getID(context).equals("-1");
    }

    public static String getID(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("id", "-1");
    }

    public static String getPref(Pref key, Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key.name(), "-1");
    }

    public static void setPref(Pref key, String value, Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(key.name(), value)
                .apply();
    }

    public static void purgePref(Pref key, Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().apply();
    }
}
