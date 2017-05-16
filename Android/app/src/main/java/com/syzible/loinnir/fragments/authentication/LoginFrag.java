package com.syzible.loinnir.fragments.authentication;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
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
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.syzible.loinnir.R;
import com.syzible.loinnir.activities.AuthenticationActivity;
import com.syzible.loinnir.activities.MainActivity;
import com.syzible.loinnir.network.RestClient;
import com.syzible.loinnir.utils.FacebookUtils;
import com.syzible.loinnir.utils.LocalStorage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Created by ed on 08/05/2017.
 */

public class LoginFrag extends Fragment {
    private EditText usernameEditText, passwordEditText;
    private View view;
    private CallbackManager callbackManager;
    private LoginButton facebookLoginButton;

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
        facebookLoginButton.setReadPermissions(Arrays.asList("public_profile", "email"));

        facebookLoginSetup();

        usernameEditText = (EditText) view.findViewById(R.id.et_login_email);
        passwordEditText = (EditText) view.findViewById(R.id.et_login_password);

        System.out.println("Token: " + FacebookUtils.getToken(getActivity()));

        return view;
    }

    private void facebookLoginSetup() {
        System.out.println("Facebook login ...");
        facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                System.out.println("Login result ...");
                String accessToken = loginResult.getAccessToken().getToken();
                FacebookUtils.saveToken(accessToken, getActivity());
                System.out.println(accessToken);

                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject o, GraphResponse response) {
                                JSONObject facebookData = FacebookUtils.getFacebookData(o, getActivity());

                                final JSONObject postData = new JSONObject();
                                JSONObject checkLogin = new JSONObject();

                                try {
                                    System.out.println(facebookData);

                                    String fbId = facebookData.getString(LocalStorage.Pref.id.name());
                                    String forename = facebookData.getString(LocalStorage.Pref.first_name.name());
                                    String surname = facebookData.getString(LocalStorage.Pref.last_name.name());
                                    String url = facebookData.getString(LocalStorage.Pref.profile_pic.name());

                                    postData.put("fb_id", fbId);
                                    postData.put("forename", forename);
                                    postData.put("surname", surname);
                                    postData.put("url", url);

                                    checkLogin.put("fb_id", fbId);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                /*
                                final NetworkCallback<JSONObject> createUserCB = new NetworkCallback<JSONObject>() {
                                    @Override
                                    public void onSuccess(JSONObject object) {
                                        startMain();
                                    }

                                    @Override
                                    public void onFailure() {
                                        System.out.println("Failure in create user");
                                    }
                                };

                                NetworkCallback<JSONObject> getUserCB = new NetworkCallback<JSONObject>() {
                                    @Override
                                    public void onSuccess(JSONObject object) {
                                        if (object.has("success")) {
                                            System.out.println(object.toString());
                                            try {
                                                // if user doesn't exist
                                                if (object.get("success") == "false") {
                                                    new SubJSONObject(createUserCB, Endpoints.CREATE_USER, postData).execute();
                                                } else {
                                                    // if so, start the main activity
                                                    startMain();
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        System.out.println(object.toString());
                                    }

                                    @Override
                                    public void onFailure() {
                                        System.out.println("Failure in get user");
                                    }
                                };

                                new SubJSONObject(getUserCB, Endpoints.LOGIN_USER, checkLogin).execute();
                                */
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
    }

    public String getUsername() {
        return usernameEditText.getText().toString();
    }

    public String getPassword() {
        return passwordEditText.getText().toString();
    }

    private void generateSnackbar(String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
                .show();
    }
}
