package com.nanodegree.ivan.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;

/**
 * A placeholder fragment containing a simple view.
 */
public class PopularFragnmet extends Fragment {

    private TrackAdapter mSpotifyAdapter;
    FetchSpotyTraskTask spotify;
    private String artista;

    public PopularFragnmet() {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent data = new Intent();
                data.putExtra("Artist", artista);
                getActivity().setResult(getActivity().RESULT_OK, data);
                getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_popular_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_popular, container, false);
        List<Track> lista = new ArrayList<>();
        mSpotifyAdapter =
                new TrackAdapter(getActivity(),
                        R.layout.list_item_popular,
                        lista);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_popular);
        listView.setAdapter(mSpotifyAdapter);

        Intent intent = getActivity().getIntent();
        artista = intent.getStringExtra("Artist");

        if (spotify != null) spotify.cancel(true);
        if (!artista.equals("")) {
            spotify = new FetchSpotyTraskTask();
            spotify.execute(artista);
        } else {
            if (mSpotifyAdapter != null) mSpotifyAdapter.clear();
        }
        return rootView;
    }
    public class FetchSpotyTraskTask extends AsyncTask<String, Void, List<Track>> {

        private final String LOG_TAG = FetchSpotyTraskTask.class.getSimpleName();


        @Override
        protected List<Track> doInBackground(String... params) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            TracksPager results = spotify.searchTracks(params[0]);
            Pager<Track> track = results.tracks;
            List<Track> lista = track.items;
            Log.v(LOG_TAG, "Spotify string: " + lista.toString());
            return lista;
        }

        @Override
        protected void onPostExecute(List<Track> result) {
            if (result != null) {
                mSpotifyAdapter.clear();
                if (result.size() == 0) {
                    int duration = Toast.LENGTH_SHORT;
                    Toast.makeText(getActivity(), getString(R.string.noalbums_message), duration).show();
                } else {
                    for (Track track : result) {
                        mSpotifyAdapter.add(track);
                    }
                }
            }
        }
    }
}
