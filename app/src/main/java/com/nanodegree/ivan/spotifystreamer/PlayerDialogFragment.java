package com.nanodegree.ivan.spotifystreamer;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.nanodegree.ivan.spotifystreamer.parceable.TrackParcelable;
import com.nanodegree.ivan.spotifystreamer.service.ForegroundService;
import com.nanodegree.ivan.spotifystreamer.service.SeekbarUpdate;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Ivan on 24/07/2015.
 */
public class PlayerDialogFragment extends DialogFragment  implements View.OnClickListener  {

    ArrayList<TrackParcelable> listaTracks;
    String trackUrl;
    int currentPosition;
    String artistName;

    boolean mBound = false;
    ForegroundService mediaPlayerService;

    final static String TAG = "PlayerDialogFragment";

    private static final String ACTION_PLAY = "com.example.action.PLAY";
    private static final String ACTION_PAUSE = "com.example.action.PAUSE";
    private static final String ACTION_NEXT = "com.example.action.NEXT";
    private static final String ACTION_PREVIOUS = "com.example.action.PREVIOUS";

    TextView player_albumname;
    TextView player_artistname;
    TextView player_trackname;
    ImageView image;
    ImageButton pauseButton;
    ImageButton playButton;
    SeekBar seekbar;

    private final int RESPONSE_END = 0;
    private final int RESPONSE_PAUSE = 1;
    private final int RESPONSE_PLAY = 2;

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

        //load layauts elements
        player_albumname = (TextView)myView.findViewById(R.id.player_albumname);
        player_artistname = (TextView)myView.findViewById(R.id.player_artistname);
        player_trackname = (TextView)myView.findViewById(R.id.player_trackname);
        image = (ImageView) myView.findViewById(R.id.player_imagealbum);
        playButton = (ImageButton) myView.findViewById(R.id.player_play);
        pauseButton = (ImageButton) myView.findViewById(R.id.player_pause);
        seekbar = (SeekBar)  myView.findViewById(R.id.seekBar);

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayerService.setTimePosition(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // nothing
            }

        });


        // onclick events
        ImageButton myButton = (ImageButton) myView.findViewById(R.id.player_previous);
        myButton.setOnClickListener(this);
        pauseButton.setOnClickListener(this);
        playButton.setOnClickListener(this);
        myButton = (ImageButton) myView.findViewById(R.id.player_next);
        myButton.setOnClickListener(this);

        return myView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LoadTrack();

        Intent sendIntent = new Intent(getActivity(), ForegroundService.class);

        sendIntent.putExtra("Receiver", new ResultReceiver(null) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                switch (resultCode) {
                    case RESPONSE_END:
                        pauseButton.setVisibility(View.GONE);
                        playButton.setVisibility(View.VISIBLE);
                        // TODO stop seekbar
                        break;
                    case RESPONSE_PLAY:
                        int track = resultData.getInt("CurrentTrack");
                        new Thread(new SeekbarUpdate(mediaPlayerService, seekbar)).start();
                        if (track != currentPosition) {
                            currentPosition = track;
                            LoadTrack();
                        }
                        break;
                    case RESPONSE_PAUSE:
                        // TODO stop seekbar
                        int time = resultData.getInt("CurrentTime");
                        seekbar.setProgress(time);
                        break;
                }
            }
        });
        getActivity().startService(sendIntent);
        getActivity().bindService(sendIntent, mConnection, getActivity().BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroyView() {
        getActivity().unbindService(mConnection);
        super.onDestroyView();

    }

    private void LoadTrack()
    {
        Track currentTrack = listaTracks.get(currentPosition).getTrack();
        trackUrl = currentTrack.preview_url;
        String albumName = currentTrack.album.name;
        player_albumname.setText(albumName);
        player_artistname.setText(artistName);
        String trackName = currentTrack.name;
        player_trackname.setText(trackName);
        List<Image> images = currentTrack.album.images;
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
            mediaPlayerService = ((ForegroundService.ForegroundServiceBinder) service).getService();
            mediaPlayerService.setTracks(listaTracks, currentPosition);
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
            playButton.setVisibility(View.VISIBLE);
            mediaPlayerService.songEvents(ACTION_PAUSE);
        }
        if (button.getId() == R.id.player_play){
            button.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
            mediaPlayerService.songEvents(ACTION_PLAY);
        }
        if (button.getId() == R.id.player_next){
            pauseButton.setVisibility(View.VISIBLE);
            playButton.setVisibility(View.GONE);
            mediaPlayerService.songEvents(ACTION_NEXT);
        }
        if (button.getId() == R.id.player_previous){
            pauseButton.setVisibility(View.VISIBLE);
            playButton.setVisibility(View.GONE);
            mediaPlayerService.songEvents(ACTION_PREVIOUS);
        }
    }



}
