package com.adipso.udacity.spotifystreamer_p2.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Fabien on 11/07/2015.
 */
public class CustomAlbum implements Serializable {
    private Long _ID;
    private String id;
    private String name;
    private String largeImageUrl;
    private String smallImageUrl;

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

    public String getLargeImageUrl() {
        return largeImageUrl;
    }
    public void setLargeImageUrl(String largeImageUrl) {
        this.largeImageUrl = largeImageUrl;
    }

    public String getSmallImageUrl() {
        return smallImageUrl;
    }
    public void setSmallImageUrl(String smallImageUrl) {
        this.smallImageUrl = smallImageUrl;
    }

}
