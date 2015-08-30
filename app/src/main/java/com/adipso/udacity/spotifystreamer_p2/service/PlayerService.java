package com.adipso.udacity.spotifystreamer_p2.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;

import com.adipso.udacity.spotifystreamer_p2.R;
import com.adipso.udacity.spotifystreamer_p2.activity.ArtistListActivity;
import com.adipso.udacity.spotifystreamer_p2.model.CustomTrack;

public class PlayerService extends Service implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {
    private static final String ACTION_PLAY = "com.adipso.udacity.spotifystreamer_p2.service.action.PLAY";
    private static final String EXTRA_TRACK = "com.adipso.udacity.spotifystreamer_p2.service.extra.TRACK";

    /**
     * Starts this service to perform action OnPlay with the given parameters.
     */
    public static void startActionOnPlay(Context context, CustomTrack track) {
        Intent intent = new Intent(context, PlayerService.class);
        intent.setAction(ACTION_PLAY);
        intent.putExtra(EXTRA_TRACK, track);
        context.startService(intent);
    }


    // This is the object that receives interactions from clients.
    private final IBinder mBinder = new SpotifyStreamerBinder();

    private MediaPlayer mMediaPlayer;
    private CustomTrack mCurrentTrack;
    private boolean mIsMediaPlayerPreparing;
    private boolean mIsMediaPlayerPrepared;

    public PlayerService() {
        mMediaPlayer = null;
        mCurrentTrack = null;
        mIsMediaPlayerPreparing = mIsMediaPlayerPrepared = false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initMediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = super.onStartCommand(intent, flags, startId);

        if (intent.getAction().equals(ACTION_PLAY)) {
            final CustomTrack track = intent.getParcelableExtra(EXTRA_TRACK);
            doPlayPlayer(track);
        }

        return result;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;   //false to call onRebind
    }

    private void initMediaPlayer() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            //"To ensure that the CPU continues running while your MediaPlayer is playing"
            mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnCompletionListener(this);
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mIsMediaPlayerPreparing = false;

        if (mMediaPlayer != null) {
            mMediaPlayer.start();
            mIsMediaPlayerPrepared = true;

            Intent intentBroadcast = new Intent(BasePlayerBroadcastReceiver.ACTION_UPDATE_STATE);
            intentBroadcast.putExtra(BasePlayerBroadcastReceiver.EXTRA_ON_PREPARED, true);
            intentBroadcast.putExtra(BasePlayerBroadcastReceiver.EXTRA_DURATION, mMediaPlayer.getDuration());
            LocalBroadcastManager.getInstance(this).sendBroadcast(intentBroadcast);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // The MediaPlayer has moved to the Error state, must be reset!
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mIsMediaPlayerPrepared = false;
        }

        return false;   //false to call onCompletionListener
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Intent intentBroadcast = new Intent(BasePlayerBroadcastReceiver.ACTION_UPDATE_STATE);
        intentBroadcast.putExtra(BasePlayerBroadcastReceiver.EXTRA_ON_COMPLETED, true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentBroadcast);
    }


    public String getCurrentTrackId() {
        return (mCurrentTrack != null ? mCurrentTrack.getId() : null);
    }

    public void doPlayPlayer(CustomTrack track) {
        if (mMediaPlayer != null) {
            //if new or (not in preparing and not prepared)
            if (
                    (mCurrentTrack == null) ||
                    (track != null && !track.getId().equals(mCurrentTrack.getId())) ||
                    (!mIsMediaPlayerPreparing && !mIsMediaPlayerPrepared)
                    ) {
                if (track != null) {
                    mCurrentTrack = track;
                }

                if (mCurrentTrack != null) {
                    try {
                        mMediaPlayer.reset();

                        mMediaPlayer.setDataSource(mCurrentTrack.getPreviewUrl());
                        mMediaPlayer.prepareAsync(); // prepare async to not block main thread
                        mIsMediaPlayerPreparing = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            //if prepared and track already set
            else if (mIsMediaPlayerPrepared) {
                mMediaPlayer.start();
            }
        }
    }

    public boolean getIsPreparedPlayer() {
        return mIsMediaPlayerPrepared;
    }

    public boolean isPlayingPlayer() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.isPlaying();
        }
        else {
            return false;
        }
    }

    public void doSeekToPlayer(int msec) {
        if (mMediaPlayer != null && mIsMediaPlayerPrepared) {
            mMediaPlayer.seekTo(msec);
        }
    }

    public int getCurrentPositionPlayer() {
        if (mMediaPlayer != null && mIsMediaPlayerPrepared) {
            return mMediaPlayer.getCurrentPosition();
        }
        else {
            return 0;
        }
    }
    public int getDurationPlayer() {
        if (mMediaPlayer != null && mIsMediaPlayerPrepared) {
            return mMediaPlayer.getDuration();
        }
        else {
            return 0;
        }
    }

    public void doPausePlayer() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    public void doStopPlayer() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
    }
    public void doResetPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mIsMediaPlayerPrepared = false;
        }
    }


    /**
     * Class for clients to access.
     */
    public class SpotifyStreamerBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }
}
