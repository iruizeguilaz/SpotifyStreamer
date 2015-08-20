package com.nanodegree.ivan.spotifystreamer;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.nanodegree.ivan.spotifystreamer.parceable.TrackParcelable;
import com.nanodegree.ivan.spotifystreamer.receiver.MusicResultReceiver;
import com.nanodegree.ivan.spotifystreamer.service.ForegroundService;
import com.nanodegree.ivan.spotifystreamer.service.SeekbarUpdate;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    boolean isDialog =  false;

    private static final String ACTION_PLAY = "com.example.action.PLAY";
    private static final String ACTION_PAUSE = "com.example.action.PAUSE";
    private static final String ACTION_NEXT = "com.example.action.NEXT";
    private static final String ACTION_PREVIOUS = "com.example.action.PREVIOUS";
    private MusicResultReceiver receiverForTest;

    TextView player_albumname;
    TextView player_artistname;
    TextView player_trackname;
    ImageView image;
    ImageButton pauseButton;
    ImageButton playButton;
    SeekBar seekbar;

    TextView currentTimeSeekBar;
    TextView totalTimeSeekBar;

    private final int RESPONSE_END = 0;
    private final int RESPONSE_PAUSE = 1;
    private final int RESPONSE_PLAY = 2;
    private final int RESPONSE_ERROR = 3;

    private ShareActionProvider mShareActionProvider;

    public static PlayerDialogFragment newInstance(ArrayList<TrackParcelable> listaTracks, int position, String artistName) {
        PlayerDialogFragment fm = new PlayerDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("ArtistName", artistName);
        bundle.putInt("Position", position);
        bundle.putParcelableArrayList("ListTracks", listaTracks);
        fm.setArguments(bundle);
        return fm;
    }

    // Setup the callback for when data is received from the service
    public void setupServiceReceiver() {
        receiverForTest = new MusicResultReceiver(new Handler());
        // This is where we specify what happens when data is received from the service
        receiverForTest.setReceiver(new MusicResultReceiver.Receiver() {
            @Override
            public void onReceiveResult(int resultCode, Bundle resultData) {
                switch (resultCode) {
                    case RESPONSE_END:
                        pauseButton.setVisibility(View.GONE);
                        playButton.setVisibility(View.VISIBLE);
                        break;
                    case RESPONSE_PLAY:
                        int track = resultData.getInt("CurrentTrack");
                        new Thread(new SeekbarUpdate(getActivity(), mediaPlayerService, seekbar, currentTimeSeekBar, totalTimeSeekBar)).start();
                        if (track != currentPosition) {
                            currentPosition = track;
                            LoadTrack();
                        }
                        pauseButton.setVisibility(View.VISIBLE);
                        playButton.setVisibility(View.GONE);
                        break;
                    case RESPONSE_PAUSE:
                        int time = resultData.getInt("CurrentTime");
                        seekbar.setProgress(time);
                        pauseButton.setVisibility(View.GONE);
                        playButton.setVisibility(View.VISIBLE);
                        break;
                    case RESPONSE_ERROR:
                        pauseButton.setVisibility(View.GONE);
                        playButton.setVisibility(View.VISIBLE);
                        currentTimeSeekBar.setText("0");
                        break;
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        View myView = inflater.inflate(R.layout.dialogfragment_player, container, false);
        Bundle arguments = getArguments();

        listaTracks = arguments.getParcelableArrayList("ListTracks");
        artistName = arguments.getString("ArtistName");
        currentPosition = arguments.getInt("Position");

        getArguments().remove("ListTracks");
        getArguments().remove("ArtistName");
        getArguments().remove("Position");

        //load layauts elements
        player_albumname = (TextView)myView.findViewById(R.id.player_albumname);
        player_artistname = (TextView)myView.findViewById(R.id.player_artistname);
        player_trackname = (TextView)myView.findViewById(R.id.player_trackname);
        image = (ImageView) myView.findViewById(R.id.player_imagealbum);
        playButton = (ImageButton) myView.findViewById(R.id.player_play);
        pauseButton = (ImageButton) myView.findViewById(R.id.player_pause);
        seekbar = (SeekBar)  myView.findViewById(R.id.seekBar);

        currentTimeSeekBar = (TextView)myView.findViewById(R.id.SeekBarCurrentTime);
        totalTimeSeekBar = (TextView)myView.findViewById(R.id.SeekBarTotalTime);

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayerService.setTimePosition(progress);
                    currentTimeSeekBar.setText(String.valueOf(TimeUnit.MILLISECONDS.toSeconds(progress)));
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.playerdialog, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, trackUrl);
        mShareActionProvider.setShareIntent(shareIntent);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupServiceReceiver();
        LoadTrack();

        Intent sendIntent = new Intent(getActivity(), ForegroundService.class);
        sendIntent.putExtra("Receiver", receiverForTest);
        getActivity().startService(sendIntent);
        getActivity().bindService(sendIntent, mConnection, getActivity().BIND_AUTO_CREATE);

    }

    @Override
    public void onDestroyView() {
        if (mBound) getActivity().unbindService(mConnection);
        super.onDestroyView();

    }

    @Override
    public void onPause() {
       //if (mBound == true) {
       //     mBound = false;
        //    getActivity().unbindService(mConnection);
       //}
        super.onPause();
        //if (isDialog) dismiss();
       // else getFragmentManager().popBackStack();
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
        // update share url (the first song is added onCreateOptionMenu
        if (mShareActionProvider!= null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, currentTrack.preview_url);
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    /** The system calls this only when creating the layout in a dialog. */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        isDialog = true;
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
            // play the song straigth away
            playButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
            mediaPlayerService.songEvents(ACTION_PLAY);
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
