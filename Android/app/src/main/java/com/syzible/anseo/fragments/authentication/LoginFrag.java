package com.syzible.anseo.fragments.authentication;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.syzible.anseo.R;
import com.syzible.anseo.activities.AuthenticationActivity;
import com.syzible.anseo.activities.MainActivity;

/**
 * Created by ed on 08/05/2017.
 */

public class LoginFrag extends Fragment {
    private EditText usernameEditText, passwordEditText;
    private View view;

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

            }
        });

        usernameEditText = (EditText) view.findViewById(R.id.et_login_username);
        passwordEditText = (EditText) view.findViewById(R.id.et_login_password);

        return view;
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
