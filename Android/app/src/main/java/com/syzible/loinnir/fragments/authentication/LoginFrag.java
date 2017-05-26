package com.syzible.loinnir.fragments.authentication;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.syzible.loinnir.R;
import com.syzible.loinnir.activities.AuthenticationActivity;
import com.syzible.loinnir.activities.MainActivity;
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
    private EditText usernameEditText, passwordEditText;
    private View view;

    CallbackManager callbackManager;
    LoginButton facebookLoginButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callbackManager = CallbackManager.Factory.create();
    }

    private void startMain() {
        getActivity().finish();
        startActivity(new Intent(getActivity(), MainActivity.class));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.login_frag, container, false);

        Button loginBtn = (Button) view.findViewById(R.id.btn_login_login);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
                startActivity(new Intent(getActivity(), MainActivity.class));
            }
        });

        TextView registerTextView = (TextView) view.findViewById(R.id.tv_login_register);
        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthenticationActivity.setFragment(getFragmentManager(), new RegisterFrag());
            }
        });

        TextView resetTextView = (TextView) view.findViewById(R.id.tv_login_forgotten_details);
        resetTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthenticationActivity.setFragment(getFragmentManager(), new ForgottenDetailsFrag());
            }
        });

        facebookLoginButton = (LoginButton) view.findViewById(R.id.login_fb_login_button);
        facebookLoginButton.setFragment(this);
        facebookLoginButton.setReadPermissions("public_profile");

        usernameEditText = (EditText) view.findViewById(R.id.et_login_email);
        passwordEditText = (EditText) view.findViewById(R.id.et_login_password);

        facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                String accessToken = loginResult.getAccessToken().getToken();
                FacebookUtils.saveToken(accessToken, getActivity());

                final GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
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

                                    LocalStorage.setPref(LocalStorage.Pref.id, id, getActivity());
                                    LocalStorage.setPref(LocalStorage.Pref.name, name, getActivity());
                                    LocalStorage.setPref(LocalStorage.Pref.profile_pic, pic, getActivity());

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

                System.out.println("Login successful");
            }

            @Override
            public void onCancel() {
                System.out.println("Login cancelled");
            }

            @Override
            public void onError(FacebookException e) {
                System.out.println("Login error");
                FacebookUtils.deleteToken(getActivity());
                e.printStackTrace();
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public String getUsername() {
        return usernameEditText.getText().toString();
    }

    public String getPassword() {
        return passwordEditText.getText().toString();
    }
}
