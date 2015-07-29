package com.nanodegree.ivan.spotifystreamer;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nanodegree.ivan.spotifystreamer.parceable.TrackParcelable;
import com.nanodegree.ivan.spotifystreamer.service.ForegroundService;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Ivan on 24/07/2015.
 */
public class PlayerDialogFragment extends DialogFragment  implements View.OnClickListener {

    ArrayList<TrackParcelable> listaTracks;
    String trackUrl;
    int currentPosition;
    String artistName;

    boolean mBound = false;
    ForegroundService mediaPlayerService;

    public static PlayerDialogFragment newInstance(ArrayList<TrackParcelable> listaTracks, int position, String artistName) {
        PlayerDialogFragment fm = new PlayerDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("ArtistName", artistName);
        bundle.putInt("Position", position);
        bundle.putParcelableArrayList("ListTracks", listaTracks);
        fm.setArguments(bundle);
        return fm;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout to use as dialog or embedded fragment
        View myView = inflater.inflate(R.layout.dialogfragment_player, container, false);
        Bundle arguments = getArguments();

        listaTracks = arguments.getParcelableArrayList("ListTracks");
        artistName = arguments.getString("ArtistName");
        currentPosition = arguments.getInt("Position");
        //LoadTrack();
        // set buttons onclick method
        ImageButton myButton = (ImageButton) myView.findViewById(R.id.player_previous);
        myButton.setOnClickListener(this);
        myButton = (ImageButton) myView.findViewById(R.id.player_pause);
        myButton.setOnClickListener(this);
        myButton = (ImageButton) myView.findViewById(R.id.player_play);
        myButton.setOnClickListener(this);
        myButton = (ImageButton) myView.findViewById(R.id.player_next);
        myButton.setOnClickListener(this);

        return myView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LoadTrack();
    }

    private void LoadTrack()
    {
        Track currentTrack = listaTracks.get(currentPosition).getTrack();
        trackUrl = currentTrack.preview_url;
        String albumName = currentTrack.album.name;
        TextView player_albumname = (TextView)getActivity().findViewById(R.id.player_albumname);
        player_albumname.setText(albumName);

        TextView player_artistname = (TextView)getActivity().findViewById(R.id.player_artistname);
        player_artistname.setText(artistName);
        String trackName = currentTrack.name;
        TextView player_trackname = (TextView)getActivity().findViewById(R.id.player_trackname);
        player_trackname.setText(trackName);
        List<Image> images = currentTrack.album.images;
        ImageView image = (ImageView) getActivity().findViewById(R.id.player_imagealbum);
        if (images != null && !images.isEmpty()) {
            String url = images.get(0).url;
            Picasso.with(getActivity()).load(url).into(image);
        } else {
            image.setImageResource(R.drawable.spotify);
        }
    }

    /** The system calls this only when creating the layout in a dialog. */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ForegroundService.ForegroundServiceBinder binder = (ForegroundService.ForegroundServiceBinder) service;
            mediaPlayerService = binder.getService();
            
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public void onClick(View view) {
        ImageButton button = (ImageButton)view;
        if (button.getId() == R.id.player_pause){
            button.setVisibility(View.GONE);
            ImageButton playButton = (ImageButton) getActivity().findViewById(R.id.player_play);
            playButton.setVisibility(View.VISIBLE);



        }
        if (button.getId() == R.id.player_play){

            button.setVisibility(View.GONE);
            ImageButton pauseButton = (ImageButton) getActivity().findViewById(R.id.player_pause);
            pauseButton.setVisibility(View.VISIBLE);

            Intent sendIntent = new Intent(getActivity(), ForegroundService.class);
            sendIntent.putExtra("URL", trackUrl);
            sendIntent.setAction("com.example.action.PLAY");
            //getActivity().startService(sendIntent);
            getActivity().bindService(sendIntent, mConnection, getActivity().BIND_AUTO_CREATE);
        }

        // implements your things
    }




}
