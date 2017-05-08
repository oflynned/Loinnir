package com.syzible.anseo.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.syzible.anseo.R;
import com.syzible.anseo.fragments.authentication.LoginFrag;

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

    private void generateSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
                .show();
    }

    public static void setFragment(FragmentManager fragmentManager, Fragment fragment) {
        fragmentManager.beginTransaction()
                .replace(R.id.authentication_frame, fragment)
                .addToBackStack(null)
                .commit();
    }
}
