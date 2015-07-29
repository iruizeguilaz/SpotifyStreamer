package com.nanodegree.ivan.spotifystreamer;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Ivan on 24/06/2015.
 */
public class TrackAdapter extends ArrayAdapter<Track> {

    private final Activity context;
    private List<Track> tracks;

    static class ViewHolder {
        ImageView imageView;
        TextView albumName;
        TextView trackName;
    }

    public TrackAdapter(Activity context,
                        int resource, List<Track> tracks) {

        super(context, resource, tracks);
        this.context = context;
        this.tracks = tracks;

    }


    @Override
    public View getView(int position, View view, ViewGroup parent) {
        View rowView = view;
        if (rowView == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            rowView= inflater.inflate(R.layout.list_item_popular, null, true);
            ViewHolder holder = new ViewHolder();
            holder.albumName = (TextView) rowView.findViewById(R.id.list_item_album_textview);
            holder.trackName = (TextView) rowView.findViewById(R.id.list_item_track_textview);
            holder.imageView = (ImageView) rowView.findViewById(R.id.list_item_popular_imageview);
            rowView.setTag(holder);
        }
        ViewHolder holder = (ViewHolder) rowView.getTag();
        Track track = tracks.get(position);
        String url = null;
        List<Image> images = track.album.images;
        if (images != null && !images.isEmpty()) {
            url = images.get(0).url;
            Picasso.with(context).load(url).into(holder.imageView);

        }else{
            holder.imageView.setImageResource(R.drawable.spotify);

        }
        holder.albumName.setText(track.album.name);
        holder.trackName.setText(track.name);

        return rowView;
    }
}
