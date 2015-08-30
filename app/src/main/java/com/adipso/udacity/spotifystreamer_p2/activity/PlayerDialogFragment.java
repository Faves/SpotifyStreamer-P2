package com.adipso.udacity.spotifystreamer_p2.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
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
import com.adipso.udacity.spotifystreamer_p2.model.TrackDataSource;
import com.adipso.udacity.spotifystreamer_p2.service.BasePlayerBroadcastReceiver;
import com.adipso.udacity.spotifystreamer_p2.service.PlayerService;
import com.squareup.picasso.Picasso;


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
    private static final String STATE_DURATION_PLAYER = "duration_player";


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
    private ProgressHandler mHandler;
    private PlayerService mBoundService;
    private boolean mIsBound;
    private int mCurrentPositionPlayer;
    private int mCurrentDurationPlayer;
    private MyPlayerBroadcastReceiver mPlayerBroadcastReceiver;
    private OnPlayerDialogFragmentCallbacks mCallback;
    private boolean mHasSavedInstanceState;

    /*
     * to not start service to keep it alive if dialog is closing
     * cf ArtistListActivity.onWindowFocusChanged
     **/
    private boolean mIsDialogClosing;
    public void setIsDialogClosing(boolean isDialogClosing) {
        this.mIsDialogClosing = isDialogClosing;
    }

    public PlayerDialogFragment() {
        super();

        mIsBound = false;
        mBoundService = null;
        mPlayerBroadcastReceiver = null;
        mCallback = null;
        mIsDialogClosing = false;
        mHasSavedInstanceState = false;
    }

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
        mCurrentPositionPlayer = 0;
        mCurrentDurationPlayer = 0;
        mHasSavedInstanceState = (savedInstanceState != null);

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
            if (savedInstanceState.containsKey(STATE_POSITION_PLAYER) && savedInstanceState.containsKey(STATE_DURATION_PLAYER)) {
                mCurrentPositionPlayer = savedInstanceState.getInt(STATE_POSITION_PLAYER);
                mCurrentDurationPlayer = savedInstanceState.getInt(STATE_DURATION_PLAYER);
            }
        }
        //if no state value, get duration from item
        if (mTrack != null && mCurrentDurationPlayer <= 0) {
            mCurrentDurationPlayer = (int)mTrack.getDurationMs();
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
        if (!mHasSavedInstanceState) {
            doPlay();
            displayCommand();
        }

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
        outState.putInt(STATE_DURATION_PLAYER, mCurrentDurationPlayer);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof OnPlayerDialogFragmentCallbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }
        mCallback = (OnPlayerDialogFragmentCallbacks)activity;
        mCallback.onPlayerDialogFragmentAttached(this);

        //start listen messages of service to update view
        if (mPlayerBroadcastReceiver == null) {
            mPlayerBroadcastReceiver = new MyPlayerBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter(MyPlayerBroadcastReceiver.ACTION_UPDATE_STATE);
            LocalBroadcastManager.getInstance(activity).registerReceiver(mPlayerBroadcastReceiver, intentFilter);
        }

        //bind service
        doBindService();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        //ubind service
        doUnbindService();

        //stop listen messages of service (no view displayed)
        if (mPlayerBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mPlayerBroadcastReceiver);
            mPlayerBroadcastReceiver = null;
        }

        if (mCallback != null) {
            mCallback.onPlayerDialogFragmentDetached();
        }
        mCallback = null;
    }

    @Override
    public void onPause() {
        super.onPause();

        //if not closing and if playing, start PlayerService to keep it alive on rotation
        if (!mIsDialogClosing && mIsBound && mBoundService.isPlayingPlayer()) {
            Context context = getActivity().getApplicationContext();
            PlayerService.startActionOnPlay(context, mTrack);
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
        if (mIsBound) {
            mBoundService.doResetPlayer();

            displayCommand();
        }

        if (mIdTra != null) {
            doLoadTrackBy_ID(mIdTra + 1);
        }
    }

    private void cmd_play_onClick(View v) {
        if (mIsBound) {
            if (mBoundService.isPlayingPlayer()) {
                mBoundService.doPausePlayer();
            }
            else {
                doPlay();
            }

            displayCommand();
        }
    }

    private void cmd_prev_onClick(View v) {
        if (mIsBound) {
            mBoundService.doResetPlayer();

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

        //init with state values
        txt_duration_end.setText(getDurationToString(mCurrentDurationPlayer));
        seekBar.setMax(mCurrentDurationPlayer);
        seekBar.setProgress(mCurrentPositionPlayer);

        displayCommand();
    }

    private void displayCommand() {
        //can go back
        cmd_prev.setEnabled(mIdTra > mIdMin);

        //play or pause
        if (mIsBound && mBoundService.isPlayingPlayer()) {
            cmd_play.setImageResource(android.R.drawable.ic_media_pause);
        }
        else {
            cmd_play.setImageResource(android.R.drawable.ic_media_play);
        }

        //can go to next
        cmd_next.setEnabled(mIdTra < mIdMax);

        //seek to 0 if no prepared
        if (mIsBound && !mBoundService.getIsPreparedPlayer()) {
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
                doPlay();
                displayCommand();
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
            if (mIsBound) {
                mBoundService.doSeekToPlayer(progress);
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
        if (!mIsBound) {
            return;
        }

        //start playing
        mBoundService.doPlayPlayer(mTrack);
        //start update seeker
        mHandler.sendEmptyMessage(SHOW_PROGRESS);
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
        private PlayerService playerService = null;
        private SeekBar seekBar = null;

        public void setPlayerService(PlayerService playerService) {
            this.playerService = playerService;
        }
        public void setSeekBar(SeekBar seekBar) {
            this.seekBar = seekBar;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_PROGRESS:
                    //if can get position, show it
                    if (playerService != null && playerService.getIsPreparedPlayer()) {
                        int progress = playerService.getCurrentPositionPlayer();
                        seekBar.setProgress(progress);

                        //if playing, continue to update
                        if (playerService.isPlayingPlayer()) {
                            msg = obtainMessage(SHOW_PROGRESS);
                            sendMessageDelayed(msg, 500);
                        }
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


    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = ((PlayerService.SpotifyStreamerBinder)service).getService();
            mIsBound = true;

            //init
            mHandler.setPlayerService(mBoundService);

            //if the same track and duration already available, update seeker only
            if (mTrack.getId().equals(mBoundService.getCurrentTrackId()) &&
                    mBoundService.getIsPreparedPlayer()) {
                //init
                mCurrentDurationPlayer = mBoundService.getDurationPlayer();
                mCurrentPositionPlayer = mBoundService.getCurrentPositionPlayer();
            }
            //if not in saved state and if not already started, start to play
            else if (!mHasSavedInstanceState) {
                doPlay();
            }
            //show progress
            mHandler.sendEmptyMessage(SHOW_PROGRESS);

            displayCommand();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
            mIsBound = false;

            mHandler.removeMessages(SHOW_PROGRESS);
            mHandler.setPlayerService(null);
        }
    };

    void doBindService() {
        Context context = getActivity().getApplicationContext();

        // Establish a connection with the service.
        context.bindService(new Intent(context,
                PlayerService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    void doUnbindService() {
        Context context = getActivity().getApplicationContext();

        if (mIsBound) {
            // Detach our existing connection.
            context.unbindService(mConnection);
            mIsBound = false;
        }
    }

    private class MyPlayerBroadcastReceiver extends BasePlayerBroadcastReceiver {
        @Override
        public void onReceive_onPrepared(Context context, Intent intent, int duration) {
            //init max
            txt_duration_end.setText(getDurationToString(duration));
            seekBar.setMax(duration);
            mCurrentDurationPlayer = duration;
            //show progress
            mHandler.sendEmptyMessage(SHOW_PROGRESS);

            displayCommand();
        }

        @Override
        public void onReceive_onCompletion(Context context, Intent intent) {
            mHandler.removeMessages(SHOW_PROGRESS);
            seekBar.setProgress(0);

            displayCommand();
        }
    }


    public static interface OnPlayerDialogFragmentCallbacks {
        public void onPlayerDialogFragmentAttached(PlayerDialogFragment playerDialogFragment);
        public void onPlayerDialogFragmentDetached();
    }
}
