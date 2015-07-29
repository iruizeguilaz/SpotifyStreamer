package com.nanodegree.ivan.spotifystreamer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by Ivan on 24/06/2015.
 */
public class ArtistAdapter extends ArrayAdapter<Artist> {

    private final Activity context;
    private List<Artist> artists;


    static class ViewHolder {
        ImageView imageView;
        TextView artistName;
    }

    public ArtistAdapter(Activity context,
                         int resource, List<Artist> artists) {

        super(context, resource, artists);
        this.context = context;
        this.artists = artists;

    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        View rowView = view;
        if (rowView == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            rowView= inflater.inflate(R.layout.list_item_spotify, null, true);
            ViewHolder holder = new ViewHolder();
            holder.artistName = (TextView) rowView.findViewById(R.id.list_item_spotify_textview);
            holder.imageView = (ImageView) rowView.findViewById(R.id.list_item_spotify_imageview);
            rowView.setTag(holder);
        }
        Artist artist = artists.get(position);
        ViewHolder holder = (ViewHolder) rowView.getTag();
        String url = null;
        List<Image> images = artist.images;
        if (images != null && !images.isEmpty()) {
            url = images.get(0).url;
            Picasso.with(context).load(url).into(holder.imageView);
        }else{
            holder.imageView.setImageResource(R.drawable.spotify);
        }
        holder.artistName.setText(artist.name);
        return rowView;
    }
}
