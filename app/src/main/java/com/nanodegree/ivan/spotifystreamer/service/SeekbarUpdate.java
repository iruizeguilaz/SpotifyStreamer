package com.nanodegree.ivan.spotifystreamer.service;

import android.app.Activity;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

/**
 * Created by Ivan on 30/07/2015.
 */
public class SeekbarUpdate implements Runnable{

    ForegroundService mediaPlayerService;
    SeekBar seekbar;
    TextView currentTimeSeekBar;
    TextView totalTimeSeekBar;
    Activity act;
    int total = 0;
    int actualTime = 0;

    final static String TAG = "SeekbarUpdate";

    public SeekbarUpdate(Activity act, ForegroundService fs, SeekBar sb, TextView currentTimeSeekBar, TextView totalTimeSeekBar){
        this.act = act;
        seekbar = sb;
        mediaPlayerService = fs;
        this.totalTimeSeekBar = totalTimeSeekBar;
        this.currentTimeSeekBar = currentTimeSeekBar;
    }

    @Override
    public void run() {

        if (mediaPlayerService == null) return;
        total = mediaPlayerService.getTotalSongTime();
        seekbar.setMax(total);

        try {
            // code runs in a thread
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    totalTimeSeekBar.setText(String.valueOf(TimeUnit.MILLISECONDS.toSeconds(total)));
                }
            });
        } catch (final Exception ex) {
            Log.i("---","Exception in thread");
        }



        actualTime = mediaPlayerService.getActualSongTime();
        while (mediaPlayerService != null && mediaPlayerService.getmMediaPlayer().isPlaying() && actualTime < total) {
            try {
                Thread.sleep(1000);
                actualTime = mediaPlayerService.getActualSongTime();
            } catch (InterruptedException e) {
                Log.v(TAG, e.getMessage());
                return;
            } catch (Exception e) {
                Log.v(TAG, e.getMessage());
                return;
            }
            if (mediaPlayerService.getmMediaPlayer().isPlaying()) {
                seekbar.setProgress(actualTime);
                try {
                    // code runs in a thread
                    act.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            currentTimeSeekBar.setText(String.valueOf(TimeUnit.MILLISECONDS.toSeconds(actualTime)));
                        }
                    });
                } catch (final Exception ex) {
                    Log.i("---","Exception in thread");
                }

            }
        }
    }
}
