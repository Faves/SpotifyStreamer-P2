package com.adipso.udacity.spotifystreamer_p2.activity;

import android.app.Dialog;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.adipso.udacity.spotifystreamer_p2.R;
import com.adipso.udacity.spotifystreamer_p2.model.CustomTrack;
import com.adipso.udacity.spotifystreamer_p2.model.CustomTrackHelper;
import com.adipso.udacity.spotifystreamer_p2.model.TrackDataSource;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;


public class PlayerDialogFragment extends DialogFragment implements
        LoaderManager.LoaderCallbacks<CustomTrack>,
        View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    public static final String ARG_ID = "id";
    public static final String ARG_ID_MIN = "id_min";
    public static final String ARG_ID_MAX = "id_max";
    public static final String ARG_TRACK = "track";

    public static DialogFragment createFragment(Long _idTra, CustomTrack track, Long idMin, Long idMax) {
        PlayerDialogFragment newFragment = new PlayerDialogFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_ID, _idTra);
        args.putParcelable(ARG_TRACK, track);
        args.putLong(ARG_ID_MIN, idMin);
        args.putLong(ARG_ID_MAX, idMax);
        newFragment.setArguments(args);
        return newFragment;
    }
    public static void showDialog(FragmentManager fragmentManager, Long _idTra, CustomTrack track, Long idMin, Long idMax) {
        DialogFragment newFragment = createFragment(_idTra, track, idMin, idMax);

        // The device is using a large layout, so show the fragment as a dialog
        newFragment.show(fragmentManager, "dialog");
    }

    private static final int LOADER_A = 1;
    private static final String LOAD_ARG_ID = "id";

    private static final int SHOW_PROGRESS = 1;
    private static final String STATE_POSITION_PLAYER = "position_player";


    private TextView txt_artist;
    private TextView txt_album;
    private TextView txt_title;
    private ImageView img_track;
    private TextView txt_current_position, txt_duration_end;
    private SeekBar seekBar;
    private View cmd_prev, cmd_next;
    private ImageButton cmd_play;

    private Long mIdTra, mIdMin, mIdMax;
    private CustomTrack mTrack;
    private Picasso mPicasso;
    private MediaPlayer mediaPlayer;
    private ProgressHandler mHandler;
    private PlayerTasks mPlayerTasks;
    private boolean mIsMediaPlayerPrepared;
    private int mCurrentPositionPlayer;

    /** The system calls this only when creating the layout in a dialog. */
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    /** The system calls this to get the DialogFragment's layout, regardless
     of whether it's being displayed as a dialog or an embedded fragment. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout to use as dialog or embedded fragment
        View rootView = inflater.inflate(R.layout.dialog_player, container, false);
        Context context = rootView.getContext();

        //init
        mPicasso = Picasso.with(context);
        mHandler = new ProgressHandler();
        mediaPlayer = null;
        mPlayerTasks = null;
        mIsMediaPlayerPrepared = false;
        mCurrentPositionPlayer = 0;

        //params
        mIdTra = mIdMin = mIdMax = null;
        mTrack = null;
        if (getArguments() != null) {
            if (getArguments().containsKey(ARG_ID)) {
                mIdTra = getArguments().getLong(ARG_ID);
            }
            if (getArguments().containsKey(ARG_TRACK)) {
                mTrack = getArguments().getParcelable(ARG_TRACK);
            }
            if (getArguments().containsKey(ARG_ID_MIN) && getArguments().containsKey(ARG_ID_MAX)) {
                mIdMin = getArguments().getLong(ARG_ID_MIN);
                mIdMax = getArguments().getLong(ARG_ID_MAX);
            }
        }
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(ARG_ID)) {
                mIdTra = savedInstanceState.getLong(ARG_ID);
            }
            if (savedInstanceState.containsKey(ARG_TRACK)) {
                mTrack = savedInstanceState.getParcelable(ARG_TRACK);
            }
            if (savedInstanceState.containsKey(ARG_ID_MIN) && savedInstanceState.containsKey(ARG_ID_MAX)) {
                mIdMin = savedInstanceState.getLong(ARG_ID_MIN);
                mIdMax = savedInstanceState.getLong(ARG_ID_MAX);
            }
            if (savedInstanceState.containsKey(STATE_POSITION_PLAYER)) {
                mCurrentPositionPlayer = savedInstanceState.getInt(STATE_POSITION_PLAYER);
            }
        }

        //view
        txt_artist = (TextView)rootView.findViewById(R.id.txt_artist);
        txt_album = (TextView)rootView.findViewById(R.id.txt_album);
        txt_title = (TextView)rootView.findViewById(R.id.txt_title);
        img_track = (ImageView)rootView.findViewById(R.id.img_track);
        txt_current_position = (TextView)rootView.findViewById(R.id.txt_current_position);
        txt_duration_end = (TextView)rootView.findViewById(R.id.txt_duration_end);
        seekBar = (SeekBar)rootView.findViewById(R.id.seekBar);
        cmd_prev = rootView.findViewById(R.id.cmd_prev);
        cmd_play = (ImageButton)rootView.findViewById(R.id.cmd_play);
        cmd_next = rootView.findViewById(R.id.cmd_next);

        cmd_prev.setOnClickListener(this);
        cmd_play.setOnClickListener(this);
        cmd_next.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);

        mHandler.setSeekBar(seekBar);

        //datas
        displayDatas();

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong(ARG_ID, mIdTra);
        outState.putParcelable(ARG_TRACK, mTrack);
        outState.putLong(ARG_ID_MIN, mIdMin);
        outState.putLong(ARG_ID_MAX, mIdMax);
        outState.putInt(STATE_POSITION_PLAYER, mCurrentPositionPlayer);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mediaPlayer != null) {
            mediaPlayer.start();
            mHandler.sendEmptyMessage(SHOW_PROGRESS);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mediaPlayer != null) {
            if (mHandler != null) {
                mHandler.setMediaPlayer(null);
            }
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }

            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_player, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(getActivity(), R.string.dialog_msg_no_settings, Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        int idView = v.getId();
        switch (idView) {
            case R.id.cmd_prev:
                cmd_prev_onClick(v);
                break;
            case R.id.cmd_play:
                cmd_play_onClick(v);
                break;
            case R.id.cmd_next:
                cmd_next_onClick(v);
                break;
        }
    }

    private void cmd_next_onClick(View v) {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mIsMediaPlayerPrepared = false;

            displayCommand();
        }

        if (mIdTra != null) {
            doLoadTrackBy_ID(mIdTra + 1);
        }
    }

    private void cmd_play_onClick(View v) {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
            else {
                if (mIsMediaPlayerPrepared) {
                    mediaPlayer.start();
                    mHandler.sendEmptyMessage(SHOW_PROGRESS);
                }
                else {
                    doPlay();
                }
            }

            displayCommand();
        }
    }

    private void cmd_prev_onClick(View v) {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mIsMediaPlayerPrepared = false;

            displayCommand();
        }

        if (mIdTra != null) {
            doLoadTrackBy_ID(mIdTra-1);
        }
    }


    private void displayDatas() {
        if (mTrack == null) {
            return;
        }

        txt_artist.setText(mTrack.getArtistName());

        if (mTrack.getAlbum() != null) {
            txt_album.setText(mTrack.getAlbum().getName());
        }

        txt_title.setText(mTrack.getName());

        if (mTrack.getAlbum() != null && !TextUtils.isEmpty(mTrack.getAlbum().getLargeImageUrl())) {
            mPicasso.load(mTrack.getAlbum().getLargeImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.list_no_image)
                    .into(img_track);
        } else {
            mPicasso.load(R.drawable.list_no_image)
                    .into(img_track);
        }

        String duration = getDurationToString(mTrack.getDurationMs());
        txt_duration_end.setText(duration);

        doPlay();

        displayCommand();
    }

    private void displayCommand() {
        //can go back
        cmd_prev.setEnabled(mIdTra > mIdMin);

        //play or pause
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            cmd_play.setImageResource(android.R.drawable.ic_media_pause);
        }
        else {
            cmd_play.setImageResource(android.R.drawable.ic_media_play);
        }

        //can go to next
        cmd_next.setEnabled(mIdTra < mIdMax);

        //seek to 0 if no prepared
        if (!mIsMediaPlayerPrepared) {
            seekBar.setProgress(0);
        }
    }

    private void doLoadTrackBy_ID(long _ID) {
        Bundle args = new Bundle();
        args.putLong(LOAD_ARG_ID, _ID);
        getLoaderManager().restartLoader(LOADER_A, args, this);
    }
    @Override
    public Loader<CustomTrack> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_A) {
            Long idToLoad = null;
            if (args != null && args.containsKey(LOAD_ARG_ID)) {
                idToLoad = args.getLong(LOAD_ARG_ID);
            }
            return new GetTrackTask(getActivity(), idToLoad);
        }
        return null;
    }
    @Override
    public void onLoadFinished(Loader<CustomTrack> loader, CustomTrack data) {
        int id = loader.getId();
        if (id == LOADER_A) {
            if (data != null) {
                mTrack = data;
                mIdTra = data.get_ID();

                displayDatas();
            }
        }
    }
    @Override
    public void onLoaderReset(Loader<CustomTrack> loader) {
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        //set seek for MediaPlayer
        if (fromUser) {
            if (mediaPlayer != null && mIsMediaPlayerPrepared) {
                mediaPlayer.seekTo(progress);
            }
        }
        //update text
        txt_current_position.setText(getDurationToString(progress));
        mCurrentPositionPlayer = progress;
    }
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        //update manualy -> stop auto
        mHandler.removeMessages(SHOW_PROGRESS);
    }
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        //stop update manualy -> restart auto
        mHandler.sendEmptyMessage(SHOW_PROGRESS);
    }


    private void doPlay() {
        if (mPlayerTasks != null) {
            return;
        }

        mPlayerTasks = new PlayerTasks();
        mPlayerTasks.execute(mTrack.getPreviewUrl());
    }
    private class PlayerTasks extends AsyncTask<String, Void, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mHandler.removeMessages(SHOW_PROGRESS);
                        seekBar.setProgress(0);

                        displayCommand();
                    }
                });
                mHandler.setMediaPlayer(mediaPlayer);
            }
        }

        @Override
        protected Integer doInBackground(String... params) {
            Integer duration = null;

            String url = (params.length > 0 ? params[0] : null);
            if (!TextUtils.isEmpty(url)) {
                mIsMediaPlayerPrepared = false;

                try {
                    mediaPlayer.reset();

                    mediaPlayer.setDataSource(mTrack.getPreviewUrl());
                    mediaPlayer.prepare(); // might take long! (for buffering, etc)
                    mediaPlayer.start();

                    duration = mediaPlayer.getDuration();
                    if (mCurrentPositionPlayer > 0 && mCurrentPositionPlayer < duration) {
                        mediaPlayer.seekTo(mCurrentPositionPlayer);
                    }

                    mIsMediaPlayerPrepared = true;
                } catch (Exception e) {
                    e.printStackTrace();

                    mediaPlayer.reset();
                }
            }
            return duration;
        }

        @Override
        protected void onPostExecute(Integer duration) {
            super.onPostExecute(duration);
            mPlayerTasks = null;

            if (mIsMediaPlayerPrepared) {
                txt_duration_end.setText(getDurationToString(duration));
                seekBar.setMax(duration);
                mHandler.sendEmptyMessage(SHOW_PROGRESS);
            }

            displayCommand();
        }
    }


    private static class GetTrackTask extends AsyncTaskLoader<CustomTrack> {
        private TrackDataSource trackDataSource;
        private Long idTrack;

        public GetTrackTask(Context context, Long idTrack) {
            super(context);

            this.idTrack = idTrack;
            trackDataSource = new TrackDataSource(context);
        }

        @Override
        public CustomTrack loadInBackground() {
            CustomTrack result = null;

            if (idTrack != null && idTrack != 0L) {
                try {
                    trackDataSource.open();
                    result = trackDataSource.getCustomTrack(idTrack);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    trackDataSource.close();
                }
            }
            return result;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }
    }


    private static class ProgressHandler extends Handler {
        private MediaPlayer mediaPlayer = null;
        private SeekBar seekBar = null;

        public void setMediaPlayer(MediaPlayer mediaPlayer) {
            this.mediaPlayer = mediaPlayer;
        }
        public void setSeekBar(SeekBar seekBar) {
            this.seekBar = seekBar;
        }

        @Override
        public void handleMessage(Message msg) {
            int pos;
            switch (msg.what) {
                case SHOW_PROGRESS:
                    pos = seekBar.getProgress();
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {//!mDragging && mShowing &&
                        int progress = mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(progress);

                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 500);
                    }
                    break;
            }
        }
    }

    private static String getDurationToString(long msDuration) {
        int seconds = (int)(msDuration/1000L) % 60;
        int minutes = (int)(msDuration/60000L) % 60;
        int hours = (int)(msDuration/3600000L);
        String duration="";
        if (hours > 0) {
            duration = hours+":";
        }
        if (minutes<=9 && !TextUtils.isEmpty(duration)) {
            duration += "0";
        }
        duration += minutes+":";
        if (seconds<=9) {
            duration += "0";
        }
        duration += seconds;
        return duration;
    }
}
