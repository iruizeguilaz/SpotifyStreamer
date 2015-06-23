package com.nanodegree.ivan.spotifystreamer;


import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Pager;


/**
 * A placeholder fragment containing a simple view.
 */
public class SearchFragment extends Fragment {

    private ArrayAdapter<String> mSpotifyAdapter;
    FetchSpotyArtistTask spotify;
    EditText inputSearch;

    public SearchFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //doSearch();
        List<String> weekForecast = new ArrayList<String>();

        // Now that we have some dummy forecast data, create an ArrayAdapter.
        // The ArrayAdapter will take data from a source (like our dummy forecast) and
        // use it to populate the ListView it's attached to.
        mSpotifyAdapter =
                new ArrayAdapter<String>(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_spotify, // The name of the layout ID.
                        R.id.list_item_spotify_textview, // The ID of the textview to populate.
                        weekForecast);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_spotify);
        listView.setAdapter(mSpotifyAdapter);


        try {
            doSearch(rootView);
        }catch (Exception e)
        {
            Log.e("ERRRRRRRORRRRRRRR", e.toString() + " " + e.getMessage());

        }

        return rootView;

    }

    private void doSearch(View rootView) {
        inputSearch = (EditText)rootView.findViewById(R.id.searchListSongs);
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (spotify != null) spotify.cancel(true);
                if (!inputSearch.getText().toString().equals("")) {
                    spotify = new FetchSpotyArtistTask();
                    spotify.execute(inputSearch.getText().toString());
                } else {
                    mSpotifyAdapter.clear();
                }
            }
        });


    }

    public class FetchSpotyArtistTask extends AsyncTask<String, Void,  String[]> {

        private final String LOG_TAG = FetchSpotyArtistTask.class.getSimpleName();

        /* The date/time conversion code is going to be moved outside the asynctask later,
             * so for convenience we're breaking it out into its own method now.
             */
        private String getDataFromArtist(Artist artista){
            return artista.name;
        }

        @Override
        protected  String[] doInBackground(String... params) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            ArtistsPager results = spotify.searchArtists(params[0]);
            Pager<Artist> artist =  results.artists;
            List<Artist> lista = artist.items;
            String[] resultStrs = new String[lista.size()];
            int index = 0;
            for (Artist artista: lista) {
                resultStrs[index] = getDataFromArtist(artista);
                index++;
            }
            Log.v(LOG_TAG, "Spotify string: " + lista.toString());
            return resultStrs;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {
                mSpotifyAdapter.clear();
                for(String albumSpotifyStr : result) {
                    mSpotifyAdapter.add(albumSpotifyStr);
                }
                // New data is back from the server.  Hooray!
            }
        }



    }


}
