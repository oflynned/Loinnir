package com.syzible.loinnir.fragments.authentication;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.syzible.loinnir.R;
import com.syzible.loinnir.activities.AuthenticationActivity;
import com.syzible.loinnir.utils.DisplayUtils;

/**
 * Created by ed on 08/05/2017.
 */

public class ForgottenDetailsFrag extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.forgotten_details_frag, container, false);

        Button resetDetailsBtn = (Button) view.findViewById(R.id.btn_reset_reset);
        resetDetailsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthenticationActivity.removeFragment(getFragmentManager());
                DisplayUtils.generateSnackbar(getActivity(), "Gheobhaidh tú ríomhphost go luath le nasc.");
            }
        });

        return view;
    }
}
