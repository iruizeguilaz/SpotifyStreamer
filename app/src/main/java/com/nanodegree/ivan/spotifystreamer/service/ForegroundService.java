package com.nanodegree.ivan.spotifystreamer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.util.Log;

import com.nanodegree.ivan.spotifystreamer.MainActivity;
import com.nanodegree.ivan.spotifystreamer.R;
import com.nanodegree.ivan.spotifystreamer.parceable.TrackParcelable;

import java.io.IOException;
import java.util.ArrayList;

public class ForegroundService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener{

    private final int RESPONSE_END = 0;
    private final int RESPONSE_PAUSE = 1;
    private final int RESPONSE_PLAY = 2;


    private static final String ACTION_PLAY = "com.example.action.PLAY";
    private static final String ACTION_PAUSE = "com.example.action.PAUSE";
    private static final String ACTION_NEXT = "com.example.action.NEXT";
    private static final String ACTION_PREVIOUS = "com.example.action.PREVIOUS";

    final static String TAG = "ForegroundService";
    MediaPlayer mMediaPlayer = null;
    NotificationManager mNotificationManager;
    Notification mNotification = null;
    WifiManager.WifiLock mWifiLock;
    String trackURL;
    int trackPosition;
    private ArrayList<TrackParcelable> listaTracks;
    ResultReceiver resultReceiver;

    // indicates the state our service:
    enum State {
        Retrieving, // the MediaRetriever is retrieving music
        Stopped,    // media player is stopped and not prepared to play
        Preparing,  // media player is preparing...
        Playing,    // playback active (media player ready!). (but the media player may actually be
        // paused in this state if we don't have audio focus. But we stay in this state
        // so that we know we have to resume playback once we get focus back)
        Paused      // playback paused (media player ready!)
    };

    State mState = State.Retrieving;

    private final IBinder mBinder = new ForegroundServiceBinder();

    final int NOTIFICATION_ID = 1;

    public ForegroundService() {
    }

    public void initMediaPlayer() {
        // ...initialize the MediaPlayer here...
        createMediaPlayer();
        mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
        mWifiLock.acquire();
        //mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    public MediaPlayer getmMediaPlayer()
    {
        return mMediaPlayer;
    }

    public int getActualSongTime()
    {
        if (mMediaPlayer.isPlaying()) return mMediaPlayer.getCurrentPosition();
        return 0;
    }

    public int getTotalSongTime()
    {
        if (mMediaPlayer.isPlaying()) return mMediaPlayer.getDuration();
        return 0;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // ... react appropriately ...
        // The MediaPlayer has moved to the Error state, must be reset!

        mMediaPlayer.reset();
        return true;
    }

    public void songEvents(String action)
    {
        switch (action) {
            case ACTION_PLAY:
                // si esta en pause o no, retomar o empezar
                if (mState == State.Retrieving)
                {
                    playSong();
                }
                if (mState == State.Paused)
                {
                    mMediaPlayer.start();
                    mState = State.Playing;
                    Bundle resultData = new Bundle();
                    resultData.putInt("CurrentTrack", trackPosition);
                    resultReceiver.send(RESPONSE_PLAY, resultData);
                }
                if (mState == State.Stopped)
                {
                    createMediaPlayer();
                    playSong();
                }
                break;
            case ACTION_PAUSE:
                Bundle resultData = new Bundle();
                int currentTime = mMediaPlayer.getCurrentPosition();
                resultData.putInt("CurrentTime", currentTime);
                mMediaPlayer.pause();
                mState = State.Paused;
                resultReceiver.send(RESPONSE_PAUSE, resultData);
                break;
            case ACTION_NEXT:
                nextSong();
                break;
            case ACTION_PREVIOUS:
                previousSong();
                break;
        }
    }

    private void previousSong() {
        if (trackPosition > 0) trackPosition--;
        else trackPosition = listaTracks.size() -1;
        playSong();
    }

    public void nextSong() {
        if (++trackPosition >= listaTracks.size()) trackPosition = 0;
        playSong();
    }

    private void playSong() {
        try {
            if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(listaTracks.get(trackPosition).getTrack().preview_url);
            mMediaPlayer.prepareAsync();
            mState = State.Playing;
        } catch (IOException e) {
            Log.v(TAG, e.getMessage());
            mState = State.Retrieving;
        }
    }

    public void setTracks(ArrayList<TrackParcelable> listaTracks, int position){
        this.listaTracks=listaTracks;
        trackPosition=position;
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mState = State.Stopped;
        }
    }
    /**
     * Makes sure the media player exists and has been reset. This will create the media player
     * if needed, or reset the existing media player if one already exists.
     */
    void createMediaPlayer() {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnErrorListener(this);
    }

    public class ForegroundServiceBinder extends Binder {
        public ForegroundService getService() {
            return ForegroundService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        resultReceiver = intent.getParcelableExtra("Receiver");

        return mBinder;
    }

    private void setUpAsForeground(String text) {
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        mNotification = new Notification();
        mNotification.tickerText = text;
        mNotification.icon = R.drawable.spotify;
        mNotification.flags |= Notification.FLAG_ONGOING_EVENT;
        mNotification.setLatestEventInfo(getApplicationContext(), "RandomMusicPlayer",
                text, pi);
        startForeground(NOTIFICATION_ID, mNotification);
    }

    @Override
    public void onCreate(){
        //create the service
        initMediaPlayer();
    }

    @Override
    public void onDestroy() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mWifiLock.release();
        //mState = State.Stopped;
    }

    /** Called when MediaPlayer is ready */
    public void onPrepared(MediaPlayer player) {
        player.start();
        Bundle resultData = new Bundle();
        resultData.putInt("CurrentTrack", trackPosition);
        resultReceiver.send(RESPONSE_PLAY, resultData);
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        if (trackPosition < (listaTracks.size() - 1)) nextSong();
        else {
            Bundle resultData = new Bundle();
            resultReceiver.send(RESPONSE_END, resultData);
            mState = State.Retrieving;
        }
    }

    // in case user touch seek bar
    public void setTimePosition(int position)
    {
        if (mMediaPlayer!= null && (mState == State.Playing || mState == State.Paused)) {
            mMediaPlayer.seekTo(position);
        }
    }
}
