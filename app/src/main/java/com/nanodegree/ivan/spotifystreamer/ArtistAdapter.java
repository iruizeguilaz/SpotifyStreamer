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

    public ArtistAdapter(Activity context,
                         int resource, List<Artist> artists) {

        super(context, resource, artists);
        this.context = context;
        this.artists = artists;

    }

    /*private static Bitmap getRoundedShape(Bitmap scaleBitmapImage) {
        int targetWidth = 50;
        int targetHeight = 50;
        Bitmap targetBitmap = Bitmap.createBitmap(targetWidth,
                targetHeight,Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(targetBitmap);
        Path path = new Path();
        path.addCircle(((float) targetWidth - 1) / 2,
                ((float) targetHeight - 1) / 2,
                (Math.min(((float) targetWidth),
                        ((float) targetHeight)) / 2),
                Path.Direction.CCW);

        canvas.clipPath(path);
        Bitmap sourceBitmap = scaleBitmapImage;
        canvas.drawBitmap(sourceBitmap,
                new Rect(0, 0, sourceBitmap.getWidth(),
                        sourceBitmap.getHeight()),
                new Rect(0, 0, targetWidth, targetHeight), null);
        return targetBitmap;
    }*/

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        Artist artist = artists.get(position);


        View rowView= inflater.inflate(R.layout.list_item_spotify, null, true);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.list_item_spotify_imageview);
        String url = null;
        List<Image> images = artist.images;
        if (images != null && !images.isEmpty()) {
            url = images.get(0).url;
            Picasso.with(context).load(url).into(imageView);
            //try {
                //imageView.setImageBitmap(Picasso.with(context).load(url).get());
            //} catch (IOException e) {
                //imageView.setImageBitmap(getRoundedShape(BitmapFactory.decodeResource(context.getResources(), R.drawable.spotify)));
                //e.printStackTrace();
            //}
        }else{
            //imageView.setImageBitmap(getRoundedShape(BitmapFactory.decodeResource(context.getResources(), R.drawable.spotify)));
            imageView.setImageResource(R.drawable.spotify);

        }
        TextView artistName = (TextView) rowView.findViewById(R.id.list_item_spotify_textview);
        artistName.setText(artist.name);

        return rowView;
    }
}
