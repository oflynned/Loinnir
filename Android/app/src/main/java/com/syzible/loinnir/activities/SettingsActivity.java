package com.syzible.loinnir.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;
import android.view.ContextThemeWrapper;
import android.widget.Toast;

import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.syzible.loinnir.R;
import com.syzible.loinnir.network.Endpoints;
import com.syzible.loinnir.network.RestClient;
import com.syzible.loinnir.utils.DisplayUtils;
import com.syzible.loinnir.utils.JSONUtils;
import com.syzible.loinnir.utils.LocalStorage;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class SettingsActivity extends PreferenceActivity {

    private Activity context = SettingsActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    private class SettingsFragment extends PreferenceFragment {

        Preference updateProfilePic, locationUpdateFrequency, shouldShareLocation;
        Preference manageBlockedUsers, shareApp, aboutLoinnir, visitWebsite;
        Preference appVersion, licences, privacyPolicy, termsOfService;
        Preference logOut, cleanAccount, deleteAccount;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_preferences);

            initialisePreferences();

            setListenerLogOut();
            setListenerCleanAccount();
            setListenerDeleteAccount();

        }

        private void initialisePreferences() {
            updateProfilePic = findPreference("pref_update_profile_picture");
            locationUpdateFrequency = findPreference("pref_location_update_frequency");
            shouldShareLocation = findPreference("pref_should_share_location");
            manageBlockedUsers = findPreference("pref_manage_blocked_users");
            shareApp = findPreference("pref_share_app");
            aboutLoinnir = findPreference("pref_about_loinnir");
            visitWebsite = findPreference("pref_visit_website");
            appVersion = findPreference("pref_app_version");
            licences = findPreference("pref_licences");
            privacyPolicy = findPreference("pref_privacy_policy");
            termsOfService = findPreference("pref_tos");
            logOut = findPreference("pref_log_out");
            cleanAccount = findPreference("pref_clean_account");
            deleteAccount = findPreference("pref_delete_account");
        }

        private void setListenerLogOut() {
            final String accountName = LocalStorage.getPref(LocalStorage.Pref.name, context);
            logOut.setSummary("Cúntas reatha: " + accountName);

            logOut.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new AlertDialog.Builder(context)
                            .setTitle("Logáil Amach?")
                            .setMessage("Éireoidh tú logáilte amach de do chuid chúntais (" + accountName + "). Beidh tú in ann logáil isteach leis an gcúntas seo arís, nó le h-aon chúntas Facebook eile.")
                            .setPositiveButton("Logáil Amach", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    DisplayUtils.generateToast(context, "Logáil tú amach");
                                }
                            })
                            .setNegativeButton("Ná Logáil Amach", null)
                            .create()
                            .show();
                    return false;
                }
            });
        }

        private void setListenerCleanAccount() {

            // TODO set to matched count; need to make this endpoint lel
            RestClient.post(context, Endpoints.GET_UNMATCHED_COUNT, JSONUtils.getIdPayload(context),
                    new BaseJsonHttpResponseHandler<JSONObject>() {
                @Override
                public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String rawJsonResponse, JSONObject response) {

                }

                @Override
                public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, Throwable throwable, String rawJsonData, JSONObject errorResponse) {

                }

                @Override
                protected JSONObject parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                    return new JSONObject(rawJsonData);
                }
            });

            cleanAccount.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    return false;
                }
            });
        }

        private void setListenerDeleteAccount() {
            deleteAccount.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.DangerAppTheme))
                            .setTitle("Do Chúntas Loinnir a Scriosadh?")
                            .setMessage("Tá brón orainn go dteastaíonn uait imeacht! Má ghlacann tú le do chúntas a scriosadh, bainfear do shonraí ar fad ónar bhfreastalaithe, agus ní bheidh tú in ann do chuid chúntais a rochtain gan cúntas eile a chruthú arís.")
                            .setPositiveButton("Deimhnigh an Scriosadh", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    DisplayUtils.generateToast(context, "Scriosadh do chúntas");
                                }
                            })
                            .setNegativeButton("Ná Scrios!", null)
                            .create()
                            .show();
                    return false;
                }
            });
        }


        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

            switch (preference.getKey()) {
                case "pref_update_profile_picture":
                    break;
                case "pref_location_update_frequency":
                    break;
                case "pref_should_share_location":
                    break;
                case "pref_manage_blocked_users":
                    break;
                case "pref_share_app":
                    break;
                case "pref_about_loinnir":
                    break;
                case "pref_visit_website":
                    break;
                case "pref_app_version":
                    break;
                case "pref_licences":
                    break;
                case "pref_privacy_policy":
                    break;
                case "pref_tos":
                    break;
                case "pref_log_out":
                    break;
                case "pref_delete_account":
                    break;
            }

            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }

}
