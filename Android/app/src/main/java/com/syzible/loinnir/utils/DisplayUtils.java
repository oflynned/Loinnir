package com.syzible.loinnir.utils;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.design.widget.Snackbar;

import com.syzible.loinnir.R;

/**
 * Created by ed on 13/05/2017.
 */

public class DisplayUtils {

    public static void generateSnackbar(Activity activity, String message) {
        Snackbar.make(activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                .show();
    }

}
