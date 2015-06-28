package com.nanodegree.ivan.spotifystreamer;


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Pager;


/**
 * A placeholder fragment containing a simple view.
 */
public class SearchFragment extends Fragment {

    private ArtistAdapter mSpotifyAdapter;
    FetchSpotyArtistTask spotify;
    EditText inputSearch;

    public SearchFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        List<Artist> lista = new ArrayList<>();
        mSpotifyAdapter =
                new ArtistAdapter(getActivity(),
                        R.layout.list_item_spotify, // The current context (this activity)
                        lista);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_spotify);
        listView.setAdapter(mSpotifyAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                Artist value = mSpotifyAdapter.getItem(position);
                Intent downloadIntent = new Intent(getActivity(), PopularActivity.class);
                downloadIntent.putExtra("ArtistID", value.id);
                downloadIntent.putExtra("ArtistName", value.name);
                startActivity(downloadIntent);
            }
        });
        try {
            doSearch(rootView);
        }catch (Exception e)
        {
            Log.e("onCreateView", e.toString() + " " + e.getMessage());
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
                    if(mSpotifyAdapter!= null) mSpotifyAdapter.clear();
                }
            }
        });
    }

    public class FetchSpotyArtistTask extends AsyncTask<String, Void,  List<Artist>> {

        private final String LOG_TAG = FetchSpotyArtistTask.class.getSimpleName();

        private String getDataFromArtist(Artist artista){
            return artista.name;
        }

        @Override
        protected  List<Artist> doInBackground(String... params) {
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

            Log.v(LOG_TAG, lista.toString());
            return lista;
        }

        @Override
        protected void onPostExecute(List<Artist> result) {
            if (result != null) {
                mSpotifyAdapter.clear();
                if (result.size() == 0) {
                    int duration = Toast.LENGTH_SHORT;
                    Toast.makeText(getActivity(),getString(R.string.noalbums_message) , duration).show();
                } else {
                    for (Artist artista : result) {
                        mSpotifyAdapter.add(artista);
                    }
                }
            }
        }
    }
}
