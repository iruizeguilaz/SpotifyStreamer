package com.nanodegree.ivan.spotifystreamer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.nanodegree.ivan.spotifystreamer.service.ForegroundService;

public class PopularActivity extends AppCompatActivity {

    String trackUrl;
    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popular);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.spotify_popular_container, new PopularFragment())
                    .commit();
        }
    }

}
