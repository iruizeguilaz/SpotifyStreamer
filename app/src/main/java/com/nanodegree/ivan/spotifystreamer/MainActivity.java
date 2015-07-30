package com.nanodegree.ivan.spotifystreamer;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import kaaes.spotify.webapi.android.models.Artist;

public class MainActivity extends AppCompatActivity implements SearchFragment.Callback {

    private static final String POPULARFRAGMENT_TAG = "PFTAG";
    public boolean mTwoPane;
    private final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.spotify_popular_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.spotify_popular_container, new PopularFragment(), POPULARFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            //this will get rid of an unnecessary shadow below the action bar for smaller screen devices like phones.
            // Then the action bar and Today item will appear to be on the same plane (as opposed to two different planes,
            // where one casts a shadow on the other).
            getSupportActionBar().setElevation(0f);
        }

    }

    @Override
    public void onItemSelected(Artist value) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            if (value != null) {
                args.putString("ArtistID", value.id);
                args.putString("ArtistName", value.name);
            }
            PopularFragment fragment = new PopularFragment();
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.spotify_popular_container, fragment, POPULARFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, PopularActivity.class);
            if (value != null) {
                intent.putExtra("ArtistID", value.id);
                intent.putExtra("ArtistName", value.name);
            }
            startActivity(intent);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


}
