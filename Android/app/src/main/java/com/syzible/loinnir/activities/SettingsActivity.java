package com.syzible.loinnir.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.view.ContextThemeWrapper;

import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.syzible.loinnir.R;
import com.syzible.loinnir.fragments.settings.SettingsFragment;
import com.syzible.loinnir.network.Endpoints;
import com.syzible.loinnir.network.RestClient;
import com.syzible.loinnir.utils.DisplayUtils;
import com.syzible.loinnir.utils.JSONUtils;
import com.syzible.loinnir.utils.LocalStorage;

import org.json.JSONException;
import org.json.JSONObject;

public class SettingsActivity extends PreferenceActivity {

    private Activity context = SettingsActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    public static void setFragmentBackstack(FragmentManager fragmentManager, Fragment fragment) {
        fragmentManager.beginTransaction()
                .replace(android.R.id.content, fragment)
                .addToBackStack(fragment.getClass().getName())
                .commit();
    }

    public static void removeFragment(FragmentManager fragmentManager) {
        fragmentManager.popBackStack();
    }

}
