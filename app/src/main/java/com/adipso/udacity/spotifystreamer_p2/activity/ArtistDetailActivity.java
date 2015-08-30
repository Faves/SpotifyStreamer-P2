package com.adipso.udacity.spotifystreamer_p2.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.adipso.udacity.spotifystreamer_p2.R;

/**
 * An activity representing a single Artist detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ArtistListActivity}.
 * <p/>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link ArtistDetailFragment}.
 */
public class ArtistDetailActivity extends AppCompatActivity {
    private static final String ARG_ARTIST_NAME = "artistName";

    public static void startActivity(Activity activity, String id, String artistName) {
        Intent intent = new Intent(activity, ArtistDetailActivity.class);
        editIntentToStartActivity(intent, id, artistName);
        activity.startActivity(intent);
    }
    public static void editIntentToStartActivity(Intent intent, String id, String artistName) {
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(ArtistDetailFragment.ARG_ITEM_ID, id);
        intent.putExtra(ARG_ARTIST_NAME, artistName);
    }


    private String mIdArt;
    private String mArtistName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_detail);

        //params
        mIdArt = null;
        mArtistName = null;
        if (getIntent() != null) {
            if (getIntent().hasExtra(ArtistDetailFragment.ARG_ITEM_ID)) {
                mIdArt = getIntent().getStringExtra(ArtistDetailFragment.ARG_ITEM_ID);
            }
            if (getIntent().hasExtra(ARG_ARTIST_NAME)) {
                mArtistName = getIntent().getStringExtra(ARG_ARTIST_NAME);
            }
        }

        //actionbar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setSubtitle(mArtistName);
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Fragment fragment = ArtistDetailFragment.createFragment(mIdArt, mArtistName);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.artist_detail_container, fragment)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tracks_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        //
        // The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        // cf singleTop for ArtistListActivity
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(this, R.string.dialog_msg_no_settings, Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
