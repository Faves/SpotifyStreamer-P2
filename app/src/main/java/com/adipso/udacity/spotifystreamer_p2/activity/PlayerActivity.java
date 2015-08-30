package com.adipso.udacity.spotifystreamer_p2.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.adipso.udacity.spotifystreamer_p2.R;
import com.adipso.udacity.spotifystreamer_p2.model.CustomTrack;
import com.adipso.udacity.spotifystreamer_p2.service.PlayerService;

public class PlayerActivity extends AppCompatActivity implements
        PlayerDialogFragment.OnPlayerDialogFragmentCallbacks {

    public static void startActivity(Activity activity, Long _idTra, CustomTrack track, Long idMin, Long idMax) {
        Intent intent = new Intent(activity, PlayerActivity.class);
        intent.putExtra(PlayerDialogFragment.ARG_ID, _idTra);
        intent.putExtra(PlayerDialogFragment.ARG_TRACK, track);
        intent.putExtra(PlayerDialogFragment.ARG_ID_MIN, idMin);
        intent.putExtra(PlayerDialogFragment.ARG_ID_MAX, idMax);
        activity.startActivity(intent);
    }


    private CustomTrack mTrack;
    private PlayerDialogFragment mCurrentPlayerDialogFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        //args
        Long idTra = 0L, idMin = 0L, idMax = 0L;
        mTrack = null;
        if (getIntent() != null &&
                getIntent().hasExtra(PlayerDialogFragment.ARG_ID) &&
                getIntent().hasExtra(PlayerDialogFragment.ARG_TRACK)) {
            idTra = getIntent().getLongExtra(PlayerDialogFragment.ARG_ID, 0L);
            mTrack = getIntent().getParcelableExtra(PlayerDialogFragment.ARG_TRACK);
            idMin = getIntent().getLongExtra(PlayerDialogFragment.ARG_ID_MIN, 0L);
            idMax = getIntent().getLongExtra(PlayerDialogFragment.ARG_ID_MAX, 0L);
        }

        //view
        if (savedInstanceState == null) {
            Fragment fragment = PlayerDialogFragment.createFragment(idTra, mTrack, idMin, idMax);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        doStopMediaPlayer();
    }

    private void doStopMediaPlayer() {
        //if fragment available, say it to not keep service alive because we don't neet it
        if (mCurrentPlayerDialogFragment != null) {
            mCurrentPlayerDialogFragment.setIsDialogClosing(true);
        }
        //if "hasFocus", the dialog is not visible => stop MediaPlayer
        stopService(new Intent(this, PlayerService.class));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        //
        // The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        // cf singleTop for ArtistDetailActivity
        int id = item.getItemId();

        //intercept Home/Up button to stop MediaPlayer
        if (id == android.R.id.home) {
            doStopMediaPlayer();
            return false;   //return false to use default implementation
        }
        if (id == R.id.action_settings) {
            Toast.makeText(this, R.string.dialog_msg_no_settings, Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onPlayerDialogFragmentAttached(PlayerDialogFragment playerDialogFragment) {
        this.mCurrentPlayerDialogFragment = playerDialogFragment;
    }
    @Override
    public void onPlayerDialogFragmentDetached() {
        this.mCurrentPlayerDialogFragment = null;
    }
}
