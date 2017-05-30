package com.syzible.loinnir.fragments.authentication;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.syzible.loinnir.R;
import com.syzible.loinnir.activities.MainActivity;
import com.syzible.loinnir.location.LocationClient;
import com.syzible.loinnir.network.Endpoints;
import com.syzible.loinnir.network.RestClient;
import com.syzible.loinnir.services.TokenService;
import com.syzible.loinnir.utils.DisplayUtils;
import com.syzible.loinnir.utils.EmojiUtils;
import com.syzible.loinnir.utils.FacebookUtils;
import com.syzible.loinnir.utils.LocalStorage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import cz.msebera.android.httpclient.Header;

/**
 * Created by ed on 08/05/2017.
 */

public class LoginFrag extends Fragment {

    private CallbackManager callbackManager;
    private LoginButton facebookLoginButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // the user has to agree to terms and conditions of the use of the app first
        if (!LocalStorage.getBooleanPref(LocalStorage.Pref.first_run, getActivity())) {
            // default to on, has to be set at some point, may as well be here instead of overriding on each start
            LocalStorage.setBooleanPref(LocalStorage.Pref.should_share_location, true, getActivity());

            ContextThemeWrapper darkTheme = new ContextThemeWrapper(getActivity(), R.style.DarkerAppTheme);
            new AlertDialog.Builder(darkTheme)
                    .setTitle("Fan Soicind Led' Thoil")
                    .setMessage("Sula dtosaíonn tú ag baint úsáide as seirbhísí Loinnir, an nglacann tú leis na coinníollacha a ghabhann le h-úsáid na h-aipe seo? " + EmojiUtils.getEmoji(EmojiUtils.HAPPY))
                    .setPositiveButton("Glacaim leo", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            LocalStorage.setBooleanPref(LocalStorage.Pref.first_run, true, getActivity());
                            DisplayUtils.generateSnackbar(getActivity(), "Is féidir tuilleadh a léamh faoi na coinníollacha seirbhíse sna socruithe " + EmojiUtils.getEmoji(EmojiUtils.COOL));
                        }
                    })
                    .setNegativeButton("Ní ghlacaim leo", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DisplayUtils.generateToast(getActivity(), "Ní féidir leat an aip seo a úsáid gan glacadh leis na coinníollacha úsáide");
                            getActivity().finish();
                        }
                    })
                    .setCancelable(false)
                    .create()
                    .show();
        }

        callbackManager = CallbackManager.Factory.create();
    }

    private void startMain() {
        getActivity().finish();
        startActivity(new Intent(getActivity(), MainActivity.class));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_frag, container, false);
        facebookLoginButton = (LoginButton) view.findViewById(R.id.login_fb_login_button);
        facebookLoginButton.setFragment(this);
        facebookLoginButton.setReadPermissions("public_profile");
        registerFacebookCallback();

        return view;
    }

    private void registerFacebookCallback() {
        facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                String accessToken = loginResult.getAccessToken().getToken();
                FacebookUtils.saveToken(accessToken, getActivity());

                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject o, GraphResponse response) {
                                try {
                                    String id = o.getString("id");
                                    String name = o.getString("name");
                                    String pic = "https://graph.facebook.com/" + id + "/picture?type=large";

                                    JSONObject postData = new JSONObject();
                                    postData.put("fb_id", id);
                                    postData.put("name", URLEncoder.encode(name, "UTF-8"));
                                    postData.put("profile_pic", pic);
                                    postData.put("lat", LocationClient.GOOSEBERRY_HILL.latitude);
                                    postData.put("lng", LocationClient.GOOSEBERRY_HILL.longitude);
                                    postData.put("show_location", true);

                                    LocalStorage.setStringPref(LocalStorage.Pref.id, id, getActivity());
                                    LocalStorage.setStringPref(LocalStorage.Pref.name, name, getActivity());
                                    LocalStorage.setStringPref(LocalStorage.Pref.profile_pic, pic, getActivity());

                                    Intent startFCMTokenService = new Intent(getActivity(), TokenService.class);
                                    getActivity().startService(startFCMTokenService);

                                    RestClient.post(getActivity(), Endpoints.CREATE_USER, postData, new BaseJsonHttpResponseHandler<JSONObject>() {
                                        @Override
                                        public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONObject response) {
                                            startMain();
                                        }

                                        @Override
                                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, JSONObject errorResponse) {
                                            DisplayUtils.generateSnackbar(getActivity(), "Thit earáid amach (" + statusCode + ") " + EmojiUtils.getEmoji(EmojiUtils.SAD));
                                        }

                                        @Override
                                        protected JSONObject parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                                            return new JSONObject(rawJsonData);
                                        }
                                    });

                                } catch (JSONException | UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }

                            }
                        });

                Bundle parameters = new Bundle();
                parameters.putString("Fields", "id,first_name,last_name,email,gender");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                DisplayUtils.generateSnackbar(getActivity(), "Cuireadh an logáil isteach le Facebook ar ceal " + EmojiUtils.getEmoji(EmojiUtils.TONGUE));
            }

            @Override
            public void onError(FacebookException e) {
                DisplayUtils.generateSnackbar(getActivity(), "Thit earáid amach leis an logáil isteach " + EmojiUtils.getEmoji(EmojiUtils.SAD));
                FacebookUtils.deleteToken(getActivity());
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
