package com.nanodegree.ivan.spotifystreamer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.util.Log;

import com.nanodegree.ivan.spotifystreamer.MainActivity;
import com.nanodegree.ivan.spotifystreamer.R;
import com.nanodegree.ivan.spotifystreamer.parceable.TrackParcelable;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;


public class ForegroundService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, AsyncResponse{

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
    int trackPosition;
    private ArrayList<TrackParcelable> listaTracks;
    ResultReceiver resultReceiver;

    private Bitmap largeIcon = null;

    // indicates the state our service:
    enum State {
        Retrieving,
        Stopped,
        Playing,
        Paused
    };

    State mState = State.Retrieving;
    private final IBinder mBinder = new ForegroundServiceBinder();
    final int NOTIFICATION_ID = 1;

    public ForegroundService() {
    }

    public void initMediaPlayer() {
        createMediaPlayer();
        mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
        mWifiLock.acquire();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
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
                    setUpAsForeground();
                }
                if (mState == State.Paused)
                {
                    mMediaPlayer.start();
                    mState = State.Playing;
                    Bundle resultData = new Bundle();
                    resultData.putInt("CurrentTrack", trackPosition);
                    setUpAsForeground();
                    if (resultReceiver!=null) resultReceiver.send(RESPONSE_PLAY, resultData);
                }
                if (mState == State.Stopped)
                {
                    createMediaPlayer();
                    playSong();
                    setUpAsForeground();
                }
                break;
            case ACTION_PAUSE:
                Bundle resultData = new Bundle();
                int currentTime = mMediaPlayer.getCurrentPosition();
                resultData.putInt("CurrentTime", currentTime);
                mMediaPlayer.pause();
                mState = State.Paused;
                setUpAsForeground();
                if (resultReceiver!=null) resultReceiver.send(RESPONSE_PAUSE, resultData);
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
        setUpAsForeground();
    }

    public void nextSong() {
        if (++trackPosition >= listaTracks.size()) trackPosition = 0;
        playSong();
        setUpAsForeground();
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
    public int onStartCommand(Intent intent, int flags, int startId) {
        // if comes form playerDiagloGFragment it would brind Receiver, if not is from notification
        if (intent.getParcelableExtra("Receiver") != null)
            resultReceiver = intent.getParcelableExtra("Receiver");
        // notifications flags
        String action = intent.getAction();
        if (action != null) {
            switch (action)
            {
                case "next":
                    nextSong();
                    break;
                case "prev":
                    previousSong();
                case "off":
                    songEvents(ACTION_PAUSE);
                    break;
                case "play":
                    songEvents(ACTION_PLAY);
                    break;
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //resultReceiver = intent.getParcelableExtra("Receiver");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        resultReceiver = null;
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        //resultReceiver = intent.getParcelableExtra("Receiver");

    }

    void updateNotification() {
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        mNotification.setLatestEventInfo(getApplicationContext(), "Spotify Player", listaTracks.get(trackPosition).getTrack().name, pi);
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }

    private void setUpAsForeground() {

        BitmapWorkerTask bitmapWorker = new BitmapWorkerTask();
        bitmapWorker.delegate = this;
        bitmapWorker.execute(listaTracks.get(trackPosition).getTrack().album.images.get(0).url);


    }


    @Override
    public void onCreate(){
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

        mNotificationManager.cancel(NOTIFICATION_ID);
        super.onDestroy();
        //mState = State.Stopped;
    }

    /** Called when MediaPlayer is ready */
    public void onPrepared(MediaPlayer player) {
        player.start();
        Bundle resultData = new Bundle();
        resultData.putInt("CurrentTrack", trackPosition);
        if (resultReceiver!=null) resultReceiver.send(RESPONSE_PLAY, resultData);
        if (mNotification == null) setUpAsForeground();
        else updateNotification();

    }

    public void deleteReceiver()
    {
        resultReceiver = null;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (trackPosition < (listaTracks.size() - 1)) nextSong();
        else {
            Bundle resultData = new Bundle();
            if (resultReceiver!=null) resultReceiver.send(RESPONSE_END, resultData);
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





    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {

        public AsyncResponse delegate = null;//Call back interface

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            String data = params[0];
            try {
                return Picasso.with(getApplicationContext()).load(data).get();
            } catch (IOException e) {
                return BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.drawable.spotify);
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
                delegate.processFinish(bitmap);
        }
    }


    public void processFinish(Bitmap output){
        largeIcon = output;


        Intent playInent = new Intent(getApplicationContext(), ForegroundService.class);
        playInent.setAction("play");
        PendingIntent playPendingIntent = PendingIntent.getService(getApplicationContext(), 3, playInent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent prevIntent = new Intent(getApplicationContext(), ForegroundService.class);
        prevIntent.setAction("prev");
        PendingIntent prevPendingIntent = PendingIntent.getService(getApplicationContext(), 1, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent nextIntent = new Intent(getApplicationContext(), ForegroundService.class);
        nextIntent.setAction("next");
        PendingIntent nextPendingIntent = PendingIntent.getService(getApplicationContext(), 2, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent offIntent = new Intent(getApplicationContext(), ForegroundService.class);
        offIntent.setAction("off");
        PendingIntent offPendingIntent = PendingIntent.getService(getApplicationContext(), 3, offIntent, PendingIntent.FLAG_UPDATE_CURRENT);



        if (mState == State.Playing)
        {
            mNotification = new Notification.Builder(this)
                    // Show controls on lock screen even when user hides sensitive content.
                    //.setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setSmallIcon(android.R.drawable.ic_media_play)
                            // Add media control buttons that invoke intents in your media service
                    .addAction(android.R.drawable.ic_media_previous, "Previous", prevPendingIntent) // #0
                    .addAction(android.R.drawable.ic_media_pause, "Pause", offPendingIntent)  // #1
                    .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent)     // #2
                            // Apply the media style template
                            //.setStyle(new Notification.MediaStyle()
                            //.setShowActionsInCompactView(1 /* #1: pause button */)
                            //.setMediaSession(null)
                            //)
                    .setLargeIcon(largeIcon)
                    .setContentTitle(listaTracks.get(trackPosition).getTrack().artists.get(0).name)
                    .setContentText(listaTracks.get(trackPosition).getTrack().name)
                    .build();
        } else {
            mNotification = new Notification.Builder(this)
                    // Show controls on lock screen even when user hides sensitive content.
                    //.setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setSmallIcon(android.R.drawable.ic_media_play)
                            // Add media control buttons that invoke intents in your media service
                    .addAction(android.R.drawable.ic_media_previous, "Previous", prevPendingIntent) // #0
                    .addAction(android.R.drawable.ic_media_play, "Play", playPendingIntent)  // #1
                    .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent)     // #2
                            // Apply the media style template
                            //.setStyle(new Notification.MediaStyle()
                            //.setShowActionsInCompactView(1 /* #1: pause button */)
                            //.setMediaSession(null)
                            //)
                    .setLargeIcon(largeIcon)
                    .setContentTitle(listaTracks.get(trackPosition).getTrack().artists.get(0).name)
                    .setContentText(listaTracks.get(trackPosition).getTrack().name)
                    .build();
        }



        startForeground(NOTIFICATION_ID, mNotification);
    }
}
