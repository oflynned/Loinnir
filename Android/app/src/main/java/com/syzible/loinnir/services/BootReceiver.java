package com.syzible.loinnir.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by ed on 19/05/2017.
 */

public class BootReceiver extends BroadcastReceiver {
    private AlarmReceiver alarmReceiver = new AlarmReceiver();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
            alarmReceiver.setAlarm(context);
    }
}
