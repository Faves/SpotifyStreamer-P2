package com.adipso.udacity.spotifystreamer_p2.model;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Fabien on 11/07/2015.
 */
public class SpotifyStreamerPrefs {
    private static final String PREFS_NAME = "com.adipso.udacity.spotifystreamer_p2.model.SpotifyStreamerPrefs";

    public static String getLastSearch(Context context) {
        SharedPreferences preference = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preference.getString("lastSearch", "");
    }
    public static void setLastSearch(Context context, String lastSearch) {
        SharedPreferences preference = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();
        editor.putString("lastSearch", lastSearch);
        editor.commit();
    }
}
