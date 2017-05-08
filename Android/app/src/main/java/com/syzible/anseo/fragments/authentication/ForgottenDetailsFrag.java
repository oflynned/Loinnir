package com.syzible.anseo.fragments.authentication;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.syzible.anseo.R;

/**
 * Created by ed on 08/05/2017.
 */

public class ForgottenDetailsFrag extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.forgotten_details_frag, container, false);
    }
}
