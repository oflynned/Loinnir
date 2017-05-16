package com.syzible.loinnir.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import com.syzible.loinnir.R;
import com.syzible.loinnir.fragments.authentication.LoginFrag;

/**
 * Created by ed on 08/05/2017.
 */

public class AuthenticationActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authentication_frag_holder);
        setFragment(getFragmentManager(), new LoginFrag());
    }

    public static void setFragment(FragmentManager fragmentManager, Fragment fragment) {
        fragmentManager.beginTransaction()
                .replace(R.id.authentication_frame, fragment)
                .addToBackStack(fragment.getClass().getName())
                .commit();
    }

    public static void removeFragment(FragmentManager fragmentManager) {
        fragmentManager.popBackStack();
    }
}