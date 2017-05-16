package com.syzible.loinnir.fragments.authentication;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.syzible.loinnir.R;
import com.syzible.loinnir.activities.AuthenticationActivity;
import com.syzible.loinnir.network.RestClient;
import com.syzible.loinnir.utils.DisplayUtils;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by ed on 08/05/2017.
 */

public class RegisterFrag extends Fragment {
    private EditText etForename, etSurname, etEmail, etPassword, etConfirmPassword;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.register_frag, container, false);

        TextView existingAccount = (TextView) view.findViewById(R.id.tv_login_register);
        existingAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthenticationActivity.removeFragment(getFragmentManager());
            }
        });

        etForename = (EditText) view.findViewById(R.id.et_register_forename);
        etSurname = (EditText) view.findViewById(R.id.et_register_surname);
        etEmail = (EditText) view.findViewById(R.id.et_register_email);
        etPassword = (EditText) view.findViewById(R.id.et_register_password);
        etConfirmPassword = (EditText) view.findViewById(R.id.et_register_password_confirmation);

        Button registerBtn = (Button) view.findViewById(R.id.btn_register_register);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidInput()) {
                    // postRegistrationData();
                    AuthenticationActivity.removeFragment(getFragmentManager());
                    DisplayUtils.generateSnackbar(getActivity(), "Is féidir leat logáil isteach anois :D");
                }
            }
        });

        return view;
    }

    private boolean isValidInput() {
        String forename = etForename.getText().toString();
        String surname = etSurname.getText().toString();
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        String confirmPass = etConfirmPassword.getText().toString();

        if (!isValidName(forename)) {
            DisplayUtils.generateSnackbar(getActivity(), "Níl an réamhainm bailí");
            return false;
        }

        if (!isValidName(surname)) {
            DisplayUtils.generateSnackbar(getActivity(), "Níl an sloinne bailí");
            return false;
        }

        if (!isValidEmail(email)) {
            DisplayUtils.generateSnackbar(getActivity(), "Níl an ríomhphost baillí");
            return false;
        }

        if (!isValidPassword(password)) {
            DisplayUtils.generateSnackbar(getActivity(), "Níl an pasfhocal baillí. Ba chóir do 6 caractar a bheith ann, agus ar a laghad 1 charactar speisialta, agus 1 uimhir.");
            return false;
        }

        if(!doPasswordsMatch(password, confirmPass)) {
            DisplayUtils.generateSnackbar(getActivity(), "Ní mheaitseáileann na pasfhocail");
            return false;
        }

        return true;
    }

    private boolean isValidName(String name) {
        return !(name.equals("") || name.length() < 2);
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        if (password.length() == 0)
            return false;

        boolean isLongEnough = password.length() > 5;
        // TODO add other fields for at least one special character, at least one number, ...

        return isLongEnough;
    }

    private boolean doPasswordsMatch(String pass1, String pass2) {
        return pass1.trim().equals(pass2.trim());
    }

    private void postRegistrationData() {
        JSONObject params = new JSONObject();
        try {
            params.put("forename", etForename.getText());
            params.put("surname", etSurname.getText());
            params.put("email", etEmail.getText());
            params.put("password", etPassword.getText());
            params.put("is_fb", false);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RestClient.post(getActivity(), RestClient.CREATE_USER, params, new BaseJsonHttpResponseHandler<JSONObject>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONObject response) {
                try {
                    boolean result = response.getBoolean("success");
                    if (result) {
                        String greeting = "Fáilte romhat go dtí " +
                                getResources().getString(R.string.app_name) +
                                ", a " + etForename.getText() + "!";
                        DisplayUtils.generateSnackbar(getActivity(), greeting);
                    } else {
                        // TODO return reasons if user exists etc
                        String reason = response.getString("reason");
                        DisplayUtils.generateSnackbar(getActivity(), reason);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, JSONObject errorResponse) {
                DisplayUtils.generateSnackbar(getActivity(), "Tharla fadhb leis an gclárúchán. Seiceáil do rochtain idirlín.");
            }

            @Override
            protected JSONObject parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return new JSONObject(rawJsonData);
            }
        });
    }
}
