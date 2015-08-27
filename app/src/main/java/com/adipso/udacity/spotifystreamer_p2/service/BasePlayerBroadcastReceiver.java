package com.adipso.udacity.spotifystreamer_p2.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Fabien on 26/08/2015.
 */
public abstract class BasePlayerBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_UPDATE_STATE = "com.adipso.udacity.spotifystreamer_p2.service.action.UPDATE_STATE";

    public static final String EXTRA_ON_PREPARED = "com.adipso.udacity.spotifystreamer_p2.service.extra.ON_PREPARED";
    public static final String EXTRA_ON_COMPLETED = "com.adipso.udacity.spotifystreamer_p2.service.extra.ON_COMPLETED";
    public static final String EXTRA_DURATION = "com.adipso.udacity.spotifystreamer_p2.service.extra.DURATION";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            if (intent.hasExtra(EXTRA_ON_PREPARED) && intent.getBooleanExtra(EXTRA_ON_PREPARED, false)) {
                int duration = 0;
                if (intent.hasExtra(EXTRA_DURATION)) {
                    duration = intent.getIntExtra(EXTRA_DURATION, 0);
                }

                onReceive_onPrepared(context, intent, duration);
            }
            else if (intent.hasExtra(EXTRA_ON_COMPLETED) && intent.getBooleanExtra(EXTRA_ON_COMPLETED, false)) {
                onReceive_onCompletion(context, intent);
            }
        }
    }


    public abstract void onReceive_onPrepared(Context context, Intent intent, int duration);
    public abstract void onReceive_onCompletion(Context context, Intent intent);
}
