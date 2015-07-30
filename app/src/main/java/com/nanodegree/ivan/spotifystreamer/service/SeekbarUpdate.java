package com.nanodegree.ivan.spotifystreamer.service;

import android.util.Log;
import android.widget.SeekBar;

/**
 * Created by Ivan on 30/07/2015.
 */
public class SeekbarUpdate implements Runnable{

    ForegroundService mediaPlayerService;
    SeekBar seekbar;
    final static String TAG = "SeekbarUpdate";

    public SeekbarUpdate(ForegroundService fs, SeekBar sb){
        seekbar = sb;
        mediaPlayerService = fs;
    }

    @Override
    public void run() {
        if (mediaPlayerService == null) return;
        int total = mediaPlayerService.getTotalSongTime();
        seekbar.setMax(total);
        int actualTime = mediaPlayerService.getActualSongTime();
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
            if (mediaPlayerService.getmMediaPlayer().isPlaying()) seekbar.setProgress(actualTime);
        }
    }
}
