package com.adipso.udacity.spotifystreamer_p2.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.adipso.udacity.spotifystreamer_p2.model.SpotifyStreamerContract.TrackEntry;
import com.adipso.udacity.spotifystreamer_p2.model.SpotifyStreamerContract.AlbumEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Fabien on 20/08/2015.
 */
public class TrackDataSource {
    // Database fields
    private SQLiteDatabase database;
    private SpotifyStreamerSQLiteHelper dbHelper;

    public TrackDataSource(Context context) {
        dbHelper = new SpotifyStreamerSQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }
    public void close() {
        dbHelper.close();
    }

    public Long createTrack(CustomTrack track) {
        ContentValues values;

        Long idNewAlbum = null;
        CustomAlbum album = track.getAlbum();
        if (album != null) {
            values = new ContentValues();
            values.put(AlbumEntry.COLUMN_SERVER_KEY, album.getId());
            values.put(AlbumEntry.COLUMN_NAME, album.getName());
            values.put(AlbumEntry.COLUMN_LARGE_IMAGE_URL, album.getLargeImageUrl());
            values.put(AlbumEntry.COLUMN_SMALL_IMAGE_URL, album.getSmallImageUrl());
            idNewAlbum = database.insert(AlbumEntry.TABLE_NAME, null,
                    values);
        }

        values = new ContentValues();
        values.put(TrackEntry.COLUMN_SERVER_KEY, track.getId());
        values.put(TrackEntry.COLUMN_NAME, track.getName());
        values.put(TrackEntry.COLUMN_ALB_KEY, idNewAlbum);
        values.put(TrackEntry.COLUMN_DURATION_MS, track.getDurationMs());
        values.put(TrackEntry.COLUMN_PREVIEW_URL, track.getPreviewUrl());
        values.put(TrackEntry.COLUMN_ARTIST_NAME, track.getArtistName());
        Long idNew = database.insert(TrackEntry.TABLE_NAME, null,
                values);

        return idNew;
    }

    public CustomTrack getCustomTrack(Long id) {
        CustomTrack newActu = null;

        Cursor cursor = database.rawQuery("SELECT t."+TrackEntry._ID+
                ", t."+TrackEntry.COLUMN_SERVER_KEY+
                ", t."+TrackEntry.COLUMN_NAME+
                ", t."+TrackEntry.COLUMN_DURATION_MS+
                ", t."+TrackEntry.COLUMN_PREVIEW_URL+
                ", t."+TrackEntry.COLUMN_ARTIST_NAME+
                ", a."+AlbumEntry._ID+
                ", a."+AlbumEntry.COLUMN_SERVER_KEY+
                ", a."+AlbumEntry.COLUMN_NAME+
                ", a."+AlbumEntry.COLUMN_LARGE_IMAGE_URL+
                ", a."+AlbumEntry.COLUMN_SMALL_IMAGE_URL+
                " FROM " + TrackEntry.TABLE_NAME + " t" +
                " LEFT JOIN " + AlbumEntry.TABLE_NAME + " a ON (t." + TrackEntry.COLUMN_ALB_KEY + " = a." + AlbumEntry._ID + ")" +
                " WHERE t." + TrackEntry._ID+" = ?", new String[] {id.toString()});

        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            newActu = cursorToCustomTrack(cursor);
        }
        cursor.close();

        return newActu;
    }

    public List<CustomTrack> getAllCustomTracks() {
        ArrayList<CustomTrack> result = new ArrayList<>();

        Cursor cursor = database.rawQuery("SELECT t."+TrackEntry._ID+
                ", t."+TrackEntry.COLUMN_SERVER_KEY+
                ", t."+TrackEntry.COLUMN_NAME+
                ", t."+TrackEntry.COLUMN_DURATION_MS+
                ", t."+TrackEntry.COLUMN_PREVIEW_URL+
                ", t."+TrackEntry.COLUMN_ARTIST_NAME+
                ", a."+AlbumEntry._ID+
                ", a."+AlbumEntry.COLUMN_SERVER_KEY+
                ", a."+AlbumEntry.COLUMN_NAME+
                ", a."+AlbumEntry.COLUMN_LARGE_IMAGE_URL+
                ", a."+AlbumEntry.COLUMN_SMALL_IMAGE_URL+
                " FROM "+TrackEntry.TABLE_NAME+" t"+
                " LEFT JOIN "+AlbumEntry.TABLE_NAME+" a ON (t."+TrackEntry.COLUMN_ALB_KEY+" = a."+AlbumEntry._ID+")"+
                " ORDER BY t."+TrackEntry._ID, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            result.add(cursorToCustomTrack(cursor));

            cursor.moveToNext();
        }
        cursor.close();

        return result;
    }

    public boolean removeAllTrack() {
        int res = database.delete(TrackEntry.TABLE_NAME,
                "1", null);
        return (res > 0);
    }


    private CustomTrack cursorToCustomTrack(Cursor cursor) {
        CustomTrack track = new CustomTrack();
        track.set_ID(cursor.getLong(0));
        track.setId(cursor.getString(1));
        track.setName(cursor.getString(2));
        track.setDurationMs(!cursor.isNull(3) ? cursor.getLong(3) : 0L);
        track.setPreviewUrl(cursor.getString(4));
        track.setArtistName(cursor.getString(5));

        CustomAlbum album = new CustomAlbum();
        album.set_ID(cursor.getLong(6));
        album.setId(cursor.getString(7));
        album.setName(cursor.getString(8));
        album.setLargeImageUrl(cursor.getString(9));
        album.setSmallImageUrl(cursor.getString(10));
        track.setAlbum(album);
        return track;
    }
}
