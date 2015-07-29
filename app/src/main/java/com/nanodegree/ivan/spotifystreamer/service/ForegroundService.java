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
import android.util.Log;

import com.nanodegree.ivan.spotifystreamer.MainActivity;
import com.nanodegree.ivan.spotifystreamer.R;
import java.io.IOException;

public class ForegroundService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener{


    private static final String ACTION_PLAY = "com.example.action.PLAY";
    private static final String ACTION_PAUSE = "com.example.action.PAUSE";
    final static String TAG = "ForegroundService";
    MediaPlayer mMediaPlayer = null;
    NotificationManager mNotificationManager;
    Notification mNotification = null;
    WifiManager.WifiLock mWifiLock;
    String trackURL;
    int trackPosition;

    private final IBinder mBinder = new ForegroundServiceBinder();

    final int NOTIFICATION_ID = 1;

    public ForegroundService() {
    }

    public void initMediaPlayer() {
        // ...initialize the MediaPlayer here...
        createMediaPlayerIfNeeded();
        mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
        mWifiLock.acquire();

        //mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // ... react appropriately ...
        // The MediaPlayer has moved to the Error state, must be reset!
        mMediaPlayer.reset();
        return true;
    }

    public void setSong(String url, int position){
        trackURL=url;
        trackPosition=position;
    }

    /**
     * Makes sure the media player exists and has been reset. This will create the media player
     * if needed, or reset the existing media player if one already exists.
     */
    void createMediaPlayerIfNeeded() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnErrorListener(this);
        }
        else
            mMediaPlayer.reset();
    }

    public class ForegroundServiceBinder extends Binder {
        public ForegroundService getService() {
            return ForegroundService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras = intent.getExtras();

        if (intent.getAction().equals(ACTION_PLAY)) {


            try {
                mMediaPlayer.setDataSource(extras.getString("URL"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            //mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.prepareAsync(); // prepare async to not block main thread
        }
        if (intent.getAction().equals(ACTION_PAUSE)) {
            mMediaPlayer.pause();
        }

        return START_STICKY;
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
        if (mMediaPlayer != null) mMediaPlayer.release();
        mWifiLock.release();
        //mState = State.Stopped;
    }

    /** Called when MediaPlayer is ready */
    public void onPrepared(MediaPlayer player) {
        player.start();
    }



    @Override
    public void onCompletion(MediaPlayer mp) {
        // TODO Auto-generated method stub

    }
}
