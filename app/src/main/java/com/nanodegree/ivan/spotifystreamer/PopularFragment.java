package com.nanodegree.ivan.spotifystreamer;




import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;


import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.nanodegree.ivan.spotifystreamer.parceable.TrackParcelable;
import com.nanodegree.ivan.spotifystreamer.service.ForegroundService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

/**
 * A placeholder fragment containing a simple view.
 */
public class PopularFragment extends Fragment {

    private TrackAdapter mSpotifyAdapter;
    FetchSpotyTraskTask spotify;
    private String artistaID;
    private String artistaName = "Artist";
    private boolean mTwoPane;

    public PopularFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        if (artistaName != null)
            actionBar.setSubtitle(artistaName);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_popular, container, false);
        List<Track> lista = new ArrayList<>();
        if (mSpotifyAdapter == null) {
            mSpotifyAdapter =
                    new TrackAdapter(getActivity(),
                            R.layout.list_item_popular,
                            lista);
            ListView listView = (ListView) rootView.findViewById(R.id.listview_popular);
            listView.setAdapter(mSpotifyAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                    ArrayList<TrackParcelable> listaTracks = new ArrayList<TrackParcelable>();
                    for (int index = 0; index < mSpotifyAdapter.getCount(); index++)
                    {
                        listaTracks.add( new TrackParcelable( mSpotifyAdapter.getItem(index)));
                    }
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    PlayerDialogFragment myPlayer = new PlayerDialogFragment().newInstance(listaTracks, position ,artistaName);
                    if (!mTwoPane) {
                        FragmentTransaction transaction = fm.beginTransaction();
                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        transaction.add(android.R.id.content, myPlayer).addToBackStack(null).commit();
                    } else {
                        myPlayer.show(fm, "dialog");
                    }
                }
            });
        }
        Bundle arguments = getArguments();
        if (arguments != null) {
            artistaID = arguments.getString("ArtistID");
            artistaName = arguments.getString("ArtistName");
            mTwoPane = true;
        }else {
            Intent intent = getActivity().getIntent();
            artistaID = intent.getStringExtra("ArtistID");
            artistaName = intent.getStringExtra("ArtistName");
            mTwoPane = false;
        }
        if (spotify != null) spotify.cancel(true);
        if (artistaID != null && !artistaID.equals("")) {
            spotify = new FetchSpotyTraskTask();
            spotify.execute(artistaID);
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
            HashMap<String,Object> queryString = new HashMap<>();
            queryString.put(SpotifyService.COUNTRY, Locale.getDefault().getCountry());

            Tracks tracks = spotify.getArtistTopTrack(params[0], queryString);
            List<Track> lista = tracks.tracks;
            Log.v(LOG_TAG, lista.toString());
            return lista;
        }

        @Override
        protected void onPostExecute(List<Track> result) {
            if (result != null) {
                mSpotifyAdapter.clear();
                if (result.size() == 0) {
                    int duration = Toast.LENGTH_SHORT;
                    Toast.makeText(getActivity(), getString(R.string.notracks_message), duration).show();
                } else {
                    for (Track track : result) {
                        mSpotifyAdapter.add(track);
                    }
                }
            }
        }
    }
}
