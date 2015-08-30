package com.adipso.udacity.spotifystreamer_p2.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Fabien on 11/07/2015.
 */
public class CustomTrack implements Parcelable {
    private Long _ID;
    private String id;
    private String name;
    private long durationMs;
    private String previewUrl;
    private CustomAlbum album;
    private String artistId;
    private String artistName;

    public CustomTrack() {
    }

    public Long get_ID() {
        return _ID;
    }
    public void set_ID(Long _ID) {
        this._ID = _ID;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public long getDurationMs() {
        return durationMs;
    }
    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }
    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public CustomAlbum getAlbum() {
        return album;
    }
    public void setAlbum(CustomAlbum album) {
        this.album = album;
    }

    public String getArtistId() {
        return artistId;
    }
    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    public String getArtistName() {
        return artistName;
    }
    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(_ID);
        out.writeString(id);
        out.writeString(name);
        out.writeLong(durationMs);
        out.writeString(previewUrl);
        out.writeSerializable(album);
        out.writeString(artistId);
        out.writeString(artistName);
    }

    public static final Creator<CustomTrack> CREATOR
            = new Creator<CustomTrack>() {
        public CustomTrack createFromParcel(Parcel in) {
            return new CustomTrack(in);
        }

        public CustomTrack[] newArray(int size) {
            return new CustomTrack[size];
        }
    };

    private CustomTrack(Parcel in) {
        _ID = in.readLong();
        id = in.readString();
        name = in.readString();
        durationMs = in.readLong();
        previewUrl = in.readString();
        album = (CustomAlbum)in.readSerializable();
        artistId = in.readString();
        artistName = in.readString();
    }
}
