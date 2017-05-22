package com.syzible.loinnir.activities;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;

import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.syzible.loinnir.R;
import com.syzible.loinnir.network.Endpoints;
import com.syzible.loinnir.network.RestClient;
import com.syzible.loinnir.utils.EmojiUtils;
import com.syzible.loinnir.utils.LocalStorage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ed on 16/05/2017.
 */

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new PreferenceFragment() {
            @Override
            public void onCreate(@Nullable Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setTheme(R.style.DarkerAppTheme);
                createSettings();
            }
        }).commit();
    }

    private void createSettings() {
        Context context = SettingsActivity.this;
        PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(context);

        PreferenceCategory accountSettings, appSettings, blockedUsersSettings, aboutAppInfo;

        accountSettings = new PreferenceCategory(context);
        accountSettings.setTitle("Socruithe Cúntais");

        appSettings = new PreferenceCategory(context);
        appSettings.setTitle("Socruithe na hAipe");

        blockedUsersSettings = new PreferenceCategory(context);
        blockedUsersSettings.setTitle("Úsáideoirí ar Cosc");

        aboutAppInfo = new PreferenceCategory(context);
        aboutAppInfo.setTitle("Faoin Aip");

        preferenceScreen.addPreference(accountSettings);
        preferenceScreen.addPreference(appSettings);
        preferenceScreen.addPreference(blockedUsersSettings);
        preferenceScreen.addPreference(aboutAppInfo);

        // account settings
        Preference profilePicture = new Preference(context);
        profilePicture.setTitle("Nuashonraigh an Pictiúr Próifíle");
        profilePicture.setSummary("Gheofar do phictiúr próifíle reatha ó Facebook");
        accountSettings.addPreference(profilePicture);

        Preference shareApp = new Preference(context);
        shareApp.setTitle("Roinn Loinnir ar Léibheann Sóisialta");
        shareApp.setSummary("Taispeáin do leantóirí nó chairde an chéad aip shóisialta don Ghaeilge " + EmojiUtils.getEmoji(EmojiUtils.COOL));
        accountSettings.addPreference(shareApp);

        Preference deleteAccount = new Preference(context);
        deleteAccount.setTitle("Scrios do Cúntas Loinnir");
        deleteAccount.setSummary("Rabhadh! Scriosfar na sonraí ar fad gan a bheith in ann aisdul.");
        accountSettings.addPreference(deleteAccount);

        // app settings
        Preference notificationFrequency = new Preference(context);
        notificationFrequency.setTitle("Minicíocht Fhograí");
        notificationFrequency.setSummary("Sioncronú cumasaithe ar gach aon 15 nóiméad");
        appSettings.addPreference(notificationFrequency);

        Preference locationSettings = new SwitchPreference(context);
        locationSettings.setTitle("Taispeáin do Cheantar");
        locationSettings.setSummary("Muna bhfuil an rogha cumasaithe, ní bheidh tú in ann ceantair gharbha na n-úsáideoirí eile a fheiceáil");
        appSettings.addPreference(locationSettings);

        // blocked users
        JSONObject payload = new JSONObject();
        try {
            payload.put("fb_id", LocalStorage.getID(context));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final Preference viewBlockedUsers = new Preference(context);
        viewBlockedUsers.setTitle("Bainistigh Úsáideoirí");
        RestClient.post(context, Endpoints.GET_BLOCKED_USERS, payload, new BaseJsonHttpResponseHandler<JSONArray>() {
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String rawJsonResponse, JSONArray response) {
                viewBlockedUsers.setSummary(response.length() + " úsáideoir ar cosc faoi láthair");
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, Throwable throwable, String rawJsonData, JSONArray errorResponse) {

            }

            @Override
            protected JSONArray parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return new JSONArray(rawJsonData);
            }
        });
        blockedUsersSettings.addPreference(viewBlockedUsers);

        // about the app
        Preference aboutLoinnir = new Preference(context);
        aboutLoinnir.setTitle("Faoi Loinnir");
        aboutLoinnir.setSummary("Níos mó eolais i dtaobh na h-aipe");
        aboutAppInfo.addPreference(aboutLoinnir);

        Preference visitWebsite = new Preference(context);
        visitWebsite.setTitle("Tabhair Cuairt Dúinn!");
        visitWebsite.setSummary("Téigh chuig Loinnir.ie as níos mó eolais");
        aboutAppInfo.addPreference(visitWebsite);

        Preference version = new Preference(context);
        version.setTitle("Leagan Aipe");
        version.setSummary(getResources().getString(R.string.app_version) + " " + EmojiUtils.getEmoji(EmojiUtils.HEART_EYES));
        aboutAppInfo.addPreference(version);

        setPreferenceScreen(preferenceScreen);
    }
}
