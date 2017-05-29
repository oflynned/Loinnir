package com.syzible.loinnir.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.preference.TwoStatePreference;
import android.support.annotation.Nullable;
import android.view.ContextThemeWrapper;

import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.syzible.loinnir.R;
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

    private class SettingsFragment extends PreferenceFragment {

        Preference updateProfilePic, locationUpdateFrequency;
        SwitchPreference shouldShareLocation;
        Preference manageBlockedUsers, shareApp, aboutLoinnir, visitWebsite;
        Preference appVersion, licences, privacyPolicy, termsOfService;
        Preference logOut, cleanAccount, deleteAccount;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_preferences);

            initialisePreferences();

            // account settings
            setListenerUpdateProfilePicture();
            setListenerLocationUpdateFrequency();
            setListenerShareLocation();
            setListenerBlockedUsers();

            // about the app
            setListenerShareApp();
            setListenerAboutLoinnir();
            setListenerVisitWebsite();

            // legal affairs
            setListenerLicences();
            setListenerPrivacyPolicy();
            setListenerTOS();

            // danger area
            setListenerLogOut();
            setListenerCleanAccount();
            setListenerDeleteAccount();

        }

        private void initialisePreferences() {
            updateProfilePic = findPreference("pref_update_profile_picture");
            locationUpdateFrequency = findPreference("pref_location_update_frequency");
            shouldShareLocation = (SwitchPreference) findPreference("pref_should_share_location");
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

        private void setListenerUpdateProfilePicture() {
            
        }

        private void setListenerLocationUpdateFrequency() {

        }

        private void setListenerShareLocation() {
            boolean isSharingLocation = LocalStorage.getBooleanPref(LocalStorage.Pref.should_share_location, context);
            shouldShareLocation.setChecked(isSharingLocation);
        }

        // TODO make custom list fragment to list users and action to unblock
        private void setListenerBlockedUsers() {
            RestClient.post(context, Endpoints.GET_BLOCKED_USERS, JSONUtils.getIdPayload(context), new BaseJsonHttpResponseHandler<JSONObject>() {
                @Override
                public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String rawJsonResponse, JSONObject response) {
                    try {
                        int count = response.getInt("count");
                        String summary = "Tá " + count + " úsáideoir ann a bhfuil cosc curtha orthu";
                        manageBlockedUsers.setSummary(summary);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, Throwable throwable, String rawJsonData, JSONObject errorResponse) {
                    manageBlockedUsers.setSummary("Úsáideoir ar bith ar a bhfuil cosc curtha air/uirthi");
                }

                @Override
                protected JSONObject parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                    return new JSONObject(rawJsonData);
                }
            });
        }

        private void setListenerShareApp() {
            shareApp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    String shareSubText = "Loinnir - Ag Fionnadh Pobail";
                    String shareBodyText = shareSubText + " https://play.google.com/store/apps/details?id=com.syzible.loinnir&hl=en";
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareSubText);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareBodyText);
                    startActivity(Intent.createChooser(shareIntent, "Roinn le"));

                    return false;
                }
            });
        }

        private void setListenerAboutLoinnir() {

        }

        private void setListenerVisitWebsite() {
            visitWebsite.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Endpoints.openLink(context, Endpoints.getFrontendURL(""));
                    return false;
                }
            });
        }

        private void setListenerLicences() {
            termsOfService.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Endpoints.openLink(context, Endpoints.getFrontendURL(Endpoints.LICENCES));
                    return false;
                }
            });
        }

        private void setListenerPrivacyPolicy() {
            termsOfService.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Endpoints.openLink(context, Endpoints.getFrontendURL(Endpoints.PRIVACY_POLICIES));
                    return false;
                }
            });
        }

        private void setListenerTOS() {
            termsOfService.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Endpoints.openLink(context, Endpoints.getFrontendURL(Endpoints.TERMS_OF_SERVICE));
                    return false;
                }
            });
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
            cleanAccount.setSummary("Á lódáil ...");

            RestClient.post(context, Endpoints.GET_MATCHED_COUNT, JSONUtils.getIdPayload(context),
                    new BaseJsonHttpResponseHandler<JSONObject>() {
                        @Override
                        public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers,
                                              String rawJsonResponse, JSONObject response) {
                            try {
                                int count = response.getInt("count");
                                String summary = count + " úsáideoir lena raibh teagmháil agat";
                                cleanAccount.setSummary(summary);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers,
                                              Throwable throwable, String rawJsonData, JSONObject errorResponse) {
                            cleanAccount.setSummary("0 úsáideoir lena raibh teagmháil agat");
                        }

                        @Override
                        protected JSONObject parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                            return new JSONObject(rawJsonData);
                        }
                    });

            cleanAccount.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.DangerAppTheme))
                            .setTitle("Do Chúntas Loinnir a Ghlanadh?")
                            .setMessage("Glanfar do chuid chúntais ionas nach mbeidh aon duine faoi mheaitseáil agat mar a bhí ag am súiteáil na h-aipe. Bainfear na daoine ar fad de do chúntas. Ní bheidh tú in ann aisdul tar éis an ghnímh seo.")
                            .setPositiveButton("Deimhnigh an Glanadh", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    DisplayUtils.generateToast(context, "Glanadh do chúntas");
                                }
                            })
                            .setNegativeButton("Ná glan!", null)
                            .create()
                            .show();
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
    }

}
