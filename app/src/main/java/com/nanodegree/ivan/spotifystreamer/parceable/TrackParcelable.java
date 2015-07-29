package com.nanodegree.ivan.spotifystreamer.parceable;

import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Ivan on 28/07/2015.
 */
public class TrackParcelable implements Parcelable {

    private Track track;

    public TrackParcelable(Track point) {
        track = point;
    }

    public Track getTrack() {
        return track;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeValue(track);
    }

    public static final Parcelable.Creator<TrackParcelable> CREATOR
            = new Parcelable.Creator<TrackParcelable>() {
        public TrackParcelable createFromParcel(Parcel in) {
            return new TrackParcelable(in);
        }

        public TrackParcelable[] newArray(int size) {
            return new TrackParcelable[size];
        }
    };

    private TrackParcelable(Parcel in) {

    }

}
