package com.syzible.loinnir.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by ed on 08/05/2017.
 */

public class LocalStorage {
    public enum Pref {
        id, fb_access_token, first_name, last_name, email, gender, profile_pic, name, first_run
    }

    public static boolean isLoggedIn(Context context) {
        return !getID(context).equals("");
    }

    public static String getID(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("id", "");
    }

    public static String getPref(Pref key, Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key.name(), "");
    }

    public static void setPref(Pref key, String value, Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(key.name(), value)
                .apply();
    }

    public static void purgePref(Pref key, Context context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(key.name(), "")
                .apply();
    }

    public static boolean isFirstRun(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(Pref.first_run.name(), true);
    }
}
