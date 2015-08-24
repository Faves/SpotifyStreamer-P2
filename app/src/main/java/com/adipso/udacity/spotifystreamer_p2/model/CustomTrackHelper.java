package com.adipso.udacity.spotifystreamer_p2.model;

import android.text.TextUtils;
import android.util.Log;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Fabien on 11/07/2015.
 */
public class CustomTrackHelper {
    public static CustomTrack convertTrackToCustomTrack(Track track, String artistName) {
        CustomTrack customTrack = null;
        if (track != null) {
            customTrack = new CustomTrack();
            customTrack.setId(track.id);
            customTrack.setName(track.name);
            customTrack.setDurationMs(track.duration_ms);
            customTrack.setPreviewUrl(track.preview_url);
            customTrack.setArtistName(artistName);

            if (track.album != null) {
                CustomAlbum customAlbum = new CustomAlbum();
                customAlbum.setId(track.album.id);
                customAlbum.setName(track.album.name);
                if (track.album.images != null && !track.album.images.isEmpty()) {
                    //search for exact dimensions
                    for (Image image : track.album.images) {
                        if (image.width != null && image.height != null &&
                            image.width == 640 && image.height == 640) {
                            customAlbum.setLargeImageUrl(image.url);
                            Log.d("CustomTrackHelper", "large:640:"+image.url);
                        }
                        else if (image.width != null && image.height != null &&
                                image.width == 200 && image.height == 200) {
                            customAlbum.setSmallImageUrl(image.url);
                            Log.d("CustomTrackHelper", "small:200:" + image.url);
                        }
                    }
                    //if not found (according tests on API, the image list is ordered by size desc)
                    //-get second or first for small
                    if (TextUtils.isEmpty(customAlbum.getSmallImageUrl())) {
                        if (track.album.images.size() > 1) {
                            Image image = track.album.images.get(1);
                            customAlbum.setSmallImageUrl(image.url);
                            Log.d("CustomTrackHelper", "small-1:"+image.width+"x"+image.height+":" + image.url);
                        }
                        else {
                            Image image = track.album.images.get(0);
                            customAlbum.setSmallImageUrl(image.url);
                            Log.d("CustomTrackHelper", "small-0:"+image.width+"x"+image.height+":" + image.url);
                        }
                    }
                    //-get first for large
                    if (TextUtils.isEmpty(customAlbum.getLargeImageUrl())) {
                        Image image = track.album.images.get(0);
                        customAlbum.setLargeImageUrl(image.url);
                        Log.d("CustomTrackHelper", "large-0:" + image.width + "x" + image.height + ":" + image.url);
                    }
                }

                customTrack.setAlbum(customAlbum);
            }
        }
        return customTrack;
    }
}
